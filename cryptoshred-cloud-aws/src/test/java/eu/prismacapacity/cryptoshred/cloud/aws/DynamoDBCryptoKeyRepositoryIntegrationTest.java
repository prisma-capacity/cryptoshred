package eu.prismacapacity.cryptoshred.cloud.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.waiters.WaiterParameters;
import eu.prismacapacity.cryptoshred.cloud.aws.utils.TestIntegration;
import eu.prismacapacity.cryptoshred.core.*;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;
import lombok.NonNull;
import lombok.val;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.*;


@Testcontainers
class DynamoDBCryptoKeyRepositoryIntegrationTest {
    private static String TABLE_NAME = "foo";

    CryptoEngine engine = new JDKCryptoEngine(CryptoInitializationVector.of("mysecret"));
    CryptoMetrics metrics = new CryptoMetrics.NOP();

    @Container
    static LocalStackContainer localstack = new LocalStackContainer()
            .withServices(LocalStackContainer.Service.DYNAMODB);

    @BeforeAll
    public static void setUp() {
        val client = getClient();

        client.createTable(new CreateTableRequest()
                .withTableName(TABLE_NAME)
                .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH).withAttributeName("subjectId"))
                .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("subjectId").withAttributeType(ScalarAttributeType.S))
        );

        val describeTable = new DescribeTableRequest().withTableName(TABLE_NAME);
        val waitParams = new WaiterParameters<DescribeTableRequest>().withRequest(describeTable);

        client.waiters().tableExists().run(waitParams);

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

        public RaceConditionCryptoKeyRepository(@NonNull CryptoEngine engine, @NonNull AmazonDynamoDB dynamoDB, @NonNull CryptoMetrics metrics, @NonNull String tableName, CryptoKey key) {
            super(engine, dynamoDB, metrics, tableName);

            this.key = key;
        }

        @Override
        protected CryptoKey createCryptoKey(CryptoSubjectId subjectId, CryptoAlgorithm algorithm, CryptoKeySize size) {
            addExampleKey(subjectId, algorithm, size, key);

            return super.createCryptoKey(subjectId, algorithm, size);
        }
    }


    private void addExampleKey(CryptoSubjectId subjectId, CryptoAlgorithm algorithm, CryptoKeySize size, CryptoKey key) {
        val client = getClient();

        val item = new HashMap<String, AttributeValue>();
        item.put("subjectId", new AttributeValue(subjectId.getId().toString()));
        item.put(Utils.generateKeyPropertyName(algorithm, size), new AttributeValue().withB(ByteBuffer.wrap(key.getBytes())));

        val request = new PutItemRequest()
                .withTableName(TABLE_NAME)
                .withItem(item);

        client.putItem(request);
    }

    private static AmazonDynamoDB getClient() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.DYNAMODB))
                .withCredentials(localstack.getDefaultCredentialsProvider())
                .build();
    }
}