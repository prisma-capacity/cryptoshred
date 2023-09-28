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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoEngine;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;
import eu.prismacapacity.cryptoshred.core.metrics.MetricsCallable;
import lombok.val;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@ExtendWith(MockitoExtension.class)
class DynamoDBCryptoKeyRepositoryTest {
  @Mock CryptoEngine engine;

  @Mock DynamoDbClient dynamoDB;

  @Mock(answer = Answers.CALLS_REAL_METHODS)
  DummyMetrics metrics;

  private static final String TABLE_NAME = "foo";

  CryptoSubjectId subjectId = CryptoSubjectId.of(UUID.randomUUID());
  CryptoAlgorithm algorithm = CryptoAlgorithm.AES_CBC;
  CryptoKeySize size = CryptoKeySize.BIT_256;
  CryptoKey key = CryptoKey.fromBytes("foo".getBytes());

  @Test
  void testNullContracts() {
    val uut = new DynamoDBCryptoKeyRepository(engine, dynamoDB, metrics, TABLE_NAME);

    assertThrows(NullPointerException.class, () -> uut.findKeyFor(null, algorithm, size));

    assertThrows(NullPointerException.class, () -> uut.findKeyFor(subjectId, null, size));

    assertThrows(NullPointerException.class, () -> uut.findKeyFor(subjectId, algorithm, null));

    assertThrows(NullPointerException.class, () -> uut.getOrCreateKeyFor(null, algorithm, size));

    assertThrows(NullPointerException.class, () -> uut.getOrCreateKeyFor(subjectId, null, size));

    assertThrows(
        NullPointerException.class, () -> uut.getOrCreateKeyFor(subjectId, algorithm, null));
  }

  @Test
  void testFindKeyForValidItem() {
    val uut = new DynamoDBCryptoKeyRepository(engine, dynamoDB, metrics, TABLE_NAME);

    val item =
        new HashMap<String, AttributeValue>() {
          {
            put(
                Utils.generateKeyPropertyName(algorithm, size),
                AttributeValue.fromB(SdkBytes.fromByteArray(key.getBytes())));
          }
        };

    val responseMock = mock(GetItemResponse.class);

    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(responseMock);
    when(responseMock.item()).thenReturn(item);

    val result = uut.findKeyFor(subjectId, algorithm, size);

    assertTrue(result.isPresent());
    assertEquals(result.get(), key);

    verify(dynamoDB).getItem(any(GetItemRequest.class));
    verify(metrics).notifyKeyLookUp();
    verify(metrics).timedFindKey(any(MetricsCallable.class));
  }

  @Test
  void testFindKeyForIfAbsent() {
    val uut = new DynamoDBCryptoKeyRepository(engine, dynamoDB, metrics, TABLE_NAME);

    val responseMock = mock(GetItemResponse.class);

    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(responseMock);
    //    when(responseMock.isPrimitive()).thenReturn(null);

    val result = uut.findKeyFor(subjectId, algorithm, size);

    assertFalse(result.isPresent());

    verify(dynamoDB).getItem(any(GetItemRequest.class));
  }

  @Test
  void testGetOrCreateKeyForAlreadyExistingKey() {
    val uut = new DynamoDBCryptoKeyRepository(engine, dynamoDB, metrics, TABLE_NAME);

    val responseMock = mock(GetItemResponse.class);
    val item =
        Maps.of(
            Utils.generateKeyPropertyName(algorithm, size),
            AttributeValue.fromB(SdkBytes.fromByteArray(key.getBytes())));

    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(responseMock);
    when(responseMock.item()).thenReturn(item);

    val resultKey = uut.getOrCreateKeyFor(subjectId, algorithm, size);

    assertEquals(key, resultKey);

    verify(dynamoDB).getItem(any(GetItemRequest.class));
    verifyNoMoreInteractions(dynamoDB);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetOrCreateKeyForNewKey() {
    val uut = new DynamoDBCryptoKeyRepository(engine, dynamoDB, metrics, TABLE_NAME);

    val item =
        Maps.of(
            Utils.generateKeyPropertyName(algorithm, size),
            AttributeValue.fromB(SdkBytes.fromByteArray(key.getBytes())));

    val getResponseMock = mock(GetItemResponse.class);
    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(getResponseMock);
    when(getResponseMock.item()).thenReturn(null);

    val updateResponseMock = mock(UpdateItemResponse.class);
    when(dynamoDB.updateItem(any(UpdateItemRequest.class))).thenReturn(updateResponseMock);
    when(updateResponseMock.attributes()).thenReturn(item);

    when(engine.generateKey(algorithm, size)).thenReturn(key);

    val resultKey = uut.getOrCreateKeyFor(subjectId, algorithm, size);

    assertEquals(key, resultKey);

    verify(dynamoDB).getItem(any(GetItemRequest.class));
    verify(dynamoDB).updateItem(any(UpdateItemRequest.class));
    verifyNoMoreInteractions(dynamoDB);
    verify(metrics).notifyKeyLookUp();
    verify(metrics).notifyKeyCreation();
    verify(metrics).timedCreateKey(any(MetricsCallable.class));
    verify(metrics).timedFindKey(any(MetricsCallable.class));
  }

  @Test
  void testGetOrCreateKeyForRaceConditionBetweenMultipleClients() {
    val uut = new DynamoDBCryptoKeyRepository(engine, dynamoDB, metrics, TABLE_NAME);

    when(engine.generateKey(algorithm, size)).thenReturn(key);

    val getResponseMock = mock(GetItemResponse.class);
    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(getResponseMock);
    val item =
        new HashMap<String, AttributeValue>() {
          {
            put(
                Utils.generateKeyPropertyName(algorithm, size),
                AttributeValue.fromB(SdkBytes.fromByteArray(key.getBytes())));
          }
        };

    // noinspection unchecked
    when(getResponseMock.item()).thenReturn(null, item);

    when(dynamoDB.updateItem(any(UpdateItemRequest.class)))
        .thenThrow(ConditionalCheckFailedException.builder().message("foo").build());

    val resultKey = uut.getOrCreateKeyFor(subjectId, algorithm, size);

    assertEquals(key, resultKey);

    verify(dynamoDB, times(2)).getItem(any(GetItemRequest.class));
    verify(dynamoDB).updateItem(any(UpdateItemRequest.class));
    verifyNoMoreInteractions(dynamoDB);
  }

  static class DummyMetrics extends CryptoMetrics.Base {}
}
