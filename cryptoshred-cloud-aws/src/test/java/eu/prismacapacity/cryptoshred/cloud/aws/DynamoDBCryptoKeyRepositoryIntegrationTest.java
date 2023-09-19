/*
 * Copyright © 2020-2023 PRISMA European Capacity Platform GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.prismacapacity.cryptoshred.cloud.aws;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import eu.prismacapacity.cryptoshred.cloud.aws.utils.TestIntegration;
import eu.prismacapacity.cryptoshred.core.*;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;
import lombok.NonNull;
import lombok.val;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Testcontainers
class DynamoDBCryptoKeyRepositoryIntegrationTest {
  private static String TABLE_NAME = "foo";

  CryptoEngine engine = new JDKCryptoEngine(CryptoInitializationVector.of("mysecret"));
  CryptoMetrics metrics = new CryptoMetrics.NOP();

  @Container
  static LocalStackContainer localstack =
      new LocalStackContainer().withServices(LocalStackContainer.Service.DYNAMODB);

  @BeforeAll
  public static void setUp() {
    val client = getClient();

    client.createTable(
        CreateTableRequest.builder()
            .tableName(TABLE_NAME)
            .keySchema(
                KeySchemaElement.builder().keyType(KeyType.HASH).attributeName("subjectId").build())
            .provisionedThroughput(
                ProvisionedThroughput.builder()
                    .readCapacityUnits(10L)
                    .writeCapacityUnits(10L)
                    .build())
            .attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("subjectId")
                    .attributeType(ScalarAttributeType.S)
                    .build())
            .build());

    val describeTable = DescribeTableRequest.builder().tableName(TABLE_NAME).build();

    client.waiter().waitUntilTableExists(describeTable);
  }

  @TestIntegration
  void testFindKeyForAbsent() {
    val client = getClient();

    val subjectId = CryptoSubjectId.of(UUID.randomUUID());
    val size = CryptoKeySize.of(128);
    val algorithm = CryptoAlgorithm.AES_CBC;

    val uut = new DynamoDBCryptoKeyRepository(engine, client, metrics, TABLE_NAME);
    val result = uut.findKeyFor(subjectId, algorithm, size);

    Assert.assertFalse(result.isPresent());
  }

  @TestIntegration
  void testFindKeyForPresent() {
    val client = getClient();

    val subjectId = CryptoSubjectId.of(UUID.randomUUID());
    val size = CryptoKeySize.of(128);
    val algorithm = CryptoAlgorithm.AES_CBC;
    val key = engine.generateKey(algorithm, size);

    addExampleKey(subjectId, algorithm, size, key);

    val uut = new DynamoDBCryptoKeyRepository(engine, client, metrics, TABLE_NAME);
    val result = uut.findKeyFor(subjectId, algorithm, size);

    Assert.assertTrue(result.isPresent());
    assertEquals(result.get().getBase64(), key.getBase64());
  }

  @TestIntegration
  void testGetOrCreateKeyForSameKeyAndSameSubjectId() {
    val client = getClient();

    val subjectId = CryptoSubjectId.of(UUID.randomUUID());
    val size = CryptoKeySize.of(128);
    val algorithm = CryptoAlgorithm.AES_CBC;

    val uut = new DynamoDBCryptoKeyRepository(engine, client, metrics, TABLE_NAME);
    val createdKey = uut.getOrCreateKeyFor(subjectId, algorithm, size);
    val foundKey = uut.findKeyFor(subjectId, algorithm, size);

    Assert.assertTrue(foundKey.isPresent());
    assertEquals(createdKey, foundKey.get());
  }

  @TestIntegration
  void testGetOrCreateKeyForMultipleSizesButSameSubjectId() {
    val client = getClient();

    val subjectId = CryptoSubjectId.of(UUID.randomUUID());
    val size128 = CryptoKeySize.of(128);
    val size192 = CryptoKeySize.of(192);
    val algorithm = CryptoAlgorithm.AES_CBC;

    val uut = new DynamoDBCryptoKeyRepository(engine, client, metrics, TABLE_NAME);

    val createdKey128 = uut.getOrCreateKeyFor(subjectId, algorithm, size128);
    val createdKey192 = uut.getOrCreateKeyFor(subjectId, algorithm, size192);
    val getOrCreateKey128 = uut.getOrCreateKeyFor(subjectId, algorithm, size128);
    val findKey128 = uut.findKeyFor(subjectId, algorithm, size128);
    val getOrCreateKey192 = uut.getOrCreateKeyFor(subjectId, algorithm, size192);
    val findKey192 = uut.findKeyFor(subjectId, algorithm, size192);

    assertEquals(createdKey128, getOrCreateKey128);
    assertTrue(findKey128.isPresent());
    assertEquals(getOrCreateKey128, findKey128.get());
    assertNotEquals(createdKey128, createdKey192);
    assertTrue(findKey192.isPresent());
    assertEquals(findKey192.get(), createdKey192);
    assertEquals(createdKey192, getOrCreateKey192);
  }

  @TestIntegration
  void testGetOrCreateKeyForRaceConditionBetweenMultipleClients() {
    val client = getClient();

    val subjectId = CryptoSubjectId.of(UUID.randomUUID());
    val size128 = CryptoKeySize.of(128);
    val algorithm = CryptoAlgorithm.AES_CBC;
    val key = engine.generateKey(algorithm, size128);

    val uut = new RaceConditionCryptoKeyRepository(engine, client, metrics, TABLE_NAME, key);

    val createdKey = uut.getOrCreateKeyFor(subjectId, algorithm, size128);

    assertEquals(createdKey, key);
  }

  class RaceConditionCryptoKeyRepository extends DynamoDBCryptoKeyRepository {
    private final CryptoKey key;

    public RaceConditionCryptoKeyRepository(
        @NonNull CryptoEngine engine,
        @NonNull DynamoDbClient dynamoDB,
        @NonNull CryptoMetrics metrics,
        @NonNull String tableName,
        CryptoKey key) {
      super(engine, dynamoDB, metrics, tableName);

      this.key = key;
    }

    @Override
    protected CryptoKey createCryptoKey(
        CryptoSubjectId subjectId, CryptoAlgorithm algorithm, CryptoKeySize size) {
      addExampleKey(subjectId, algorithm, size, key);

      return super.createCryptoKey(subjectId, algorithm, size);
    }
  }

  private void addExampleKey(
      CryptoSubjectId subjectId, CryptoAlgorithm algorithm, CryptoKeySize size, CryptoKey key) {
    val client = getClient();

    val item = new HashMap<String, AttributeValue>();
    item.put("subjectId", AttributeValue.fromS(subjectId.getId().toString()));
    item.put(
        Utils.generateKeyPropertyName(algorithm, size),
        AttributeValue.fromB(SdkBytes.fromByteArray(key.getBytes())));

    val request = PutItemRequest.builder().tableName(TABLE_NAME).item(item).build();

    client.putItem(request);
  }

  private static DynamoDbClient getClient() {
    final URI endpointOverride =
        localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB);
    return DynamoDbClient.builder()
        .endpointOverride(endpointOverride)
        .region(Region.of(localstack.getRegion()))
        .build();
  }
}
