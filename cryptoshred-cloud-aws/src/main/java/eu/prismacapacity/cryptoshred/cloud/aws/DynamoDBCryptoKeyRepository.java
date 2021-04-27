/*
 * Copyright © 2020 PRISMA European Capacity Platform GmbH
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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoEngine;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyNotFoundAfterCreatingException;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * CryptoKeyRepository implementation based on AWS DynamoDB. Supports multiple keys (algorithm +
 * size) for the same subject
 *
 * @author otbe
 */
@RequiredArgsConstructor
public class DynamoDBCryptoKeyRepository implements CryptoKeyRepository {
  @NonNull private final CryptoEngine engine;

  @NonNull private final AmazonDynamoDB dynamoDB;

  @NonNull private final CryptoMetrics metrics;

  @NonNull private final String tableName;

  @Override
  public Optional<CryptoKey> findKeyFor(
      @NonNull CryptoSubjectId subjectId,
      @NonNull CryptoAlgorithm algorithm,
      @NonNull CryptoKeySize size) {
    metrics.notifyKeyLookUp();

    val getRequest = GetCryptoKeyRequest.of(subjectId, algorithm, size, tableName);

    val item = metrics.timedFindKey(() -> dynamoDB.getItem(getRequest.toDynamoRequest()).getItem());

    if (item == null) {
      return Optional.empty();
    }

    return Utils.extractCryptoKeyFromItem(algorithm, size, item);
  }

  @Override
  public CryptoKey getOrCreateKeyFor(
      @NonNull CryptoSubjectId subjectId,
      @NonNull CryptoAlgorithm algorithm,
      @NonNull CryptoKeySize size)
      throws CryptoKeyNotFoundAfterCreatingException {
    return findKeyFor(subjectId, algorithm, size)
        .orElseGet(() -> createCryptoKey(subjectId, algorithm, size));
  }

  protected CryptoKey createCryptoKey(
      CryptoSubjectId subjectId, CryptoAlgorithm algorithm, CryptoKeySize size) {
    val key = engine.generateKey(algorithm, size);
    val createRequest = CreateCryptoKeyRequest.of(subjectId, algorithm, size, key, tableName);

    try {
      val result =
          metrics.timedCreateKey(() -> dynamoDB.updateItem(createRequest.toDynamoRequest()));

      val resultKey = Utils.extractCryptoKeyFromItem(algorithm, size, result.getAttributes());

      if (!resultKey.isPresent()) {
        // should never ever happen because that would indicate a broken DynamoDB API
        // contract
        throw new CryptoKeyNotFoundAfterCreatingException(
            "Something weird happened. Check DynamoDB config.");
      }

      metrics.notifyKeyCreation();

      return resultKey.get();
    } catch (ConditionalCheckFailedException ignored) {
      // this happens when the key was not found in the first step but someone created
      // one in the meantime the updateItem call checks that the key for algorithm
      // and size does not exist before updating/creating the item
      // so we can safely (consistent) read from the table and get our key
      val item = findKeyFor(subjectId, algorithm, size);

      if (!item.isPresent()) {
        throw new IllegalStateException("DynamoDB consistent read failed.");
      }

      metrics.notifyKeyCreationAfterConflict();

      return item.get();
    }
  }
}
