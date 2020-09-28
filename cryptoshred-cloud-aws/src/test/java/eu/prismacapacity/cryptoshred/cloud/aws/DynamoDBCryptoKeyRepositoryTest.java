package eu.prismacapacity.cryptoshred.cloud.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoEngine;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;
import eu.prismacapacity.cryptoshred.core.metrics.MetricsCallable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DynamoDBCryptoKeyRepositoryTest {
  @Mock CryptoEngine engine;

  @Mock AmazonDynamoDB dynamoDB;

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
                new AttributeValue().withB(ByteBuffer.wrap(key.getBytes())));
          }
        };

    val responseMock = mock(GetItemResult.class);

    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(responseMock);
    when(responseMock.getItem()).thenReturn(item);

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

    val responseMock = mock(GetItemResult.class);

    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(responseMock);
    when(responseMock.getItem()).thenReturn(null);

    val result = uut.findKeyFor(subjectId, algorithm, size);

    assertFalse(result.isPresent());

    verify(dynamoDB).getItem(any(GetItemRequest.class));
  }

  @Test
  void testGetOrCreateKeyForAlreadyExistingKey() {
    val uut = new DynamoDBCryptoKeyRepository(engine, dynamoDB, metrics, TABLE_NAME);

    val responseMock = mock(GetItemResult.class);
    val item =
        Maps.of(
            Utils.generateKeyPropertyName(algorithm, size),
            new AttributeValue().withB(ByteBuffer.wrap(key.getBytes())));

    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(responseMock);
    when(responseMock.getItem()).thenReturn(item);

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
            new AttributeValue().withB(ByteBuffer.wrap(key.getBytes())));

    val getResponseMock = mock(GetItemResult.class);
    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(getResponseMock);
    when(getResponseMock.getItem()).thenReturn(null);

    val updateResponseMock = mock(UpdateItemResult.class);
    when(dynamoDB.updateItem(any(UpdateItemRequest.class))).thenReturn(updateResponseMock);
    when(updateResponseMock.getAttributes()).thenReturn(item);

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

    val getResponseMock = mock(GetItemResult.class);
    when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(getResponseMock);
    val item =
        new HashMap<String, AttributeValue>() {
          {
            put(
                Utils.generateKeyPropertyName(algorithm, size),
                new AttributeValue().withB(ByteBuffer.wrap(key.getBytes())));
          }
        };

    // noinspection unchecked
    when(getResponseMock.getItem()).thenReturn(null, item);

    when(dynamoDB.updateItem(any(UpdateItemRequest.class)))
        .thenThrow(new ConditionalCheckFailedException("foo"));

    val resultKey = uut.getOrCreateKeyFor(subjectId, algorithm, size);

    assertEquals(key, resultKey);

    verify(dynamoDB, times(2)).getItem(any(GetItemRequest.class));
    verify(dynamoDB).updateItem(any(UpdateItemRequest.class));
    verifyNoMoreInteractions(dynamoDB);
  }

  static class DummyMetrics extends CryptoMetrics.Base {}
}
