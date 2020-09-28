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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import java.util.Map;
import java.util.Optional;
import lombok.val;

class Utils {
  static Map<String, AttributeValue> subjectIdToKeyAttributeMap(CryptoSubjectId subjectId) {
    return Maps.of("subjectId", new AttributeValue(subjectId.getId().toString()));
  }

  static String generateKeyPropertyName(CryptoAlgorithm algorithm, CryptoKeySize size) {
    return algorithm.getId() + size.asInt();
  }

  static Optional<CryptoKey> extractCryptoKeyFromItem(
      CryptoAlgorithm algorithm, CryptoKeySize size, Map<String, AttributeValue> item) {
    val keyAttributePath = Utils.generateKeyPropertyName(algorithm, size);
    val keyAttributeValue = item.get(keyAttributePath);

    if (keyAttributeValue == null) {
      return Optional.empty();
    }

    val bytes = keyAttributeValue.getB();

    if (bytes == null) {
      return Optional.empty();
    }

    return Optional.of(CryptoKey.fromBytes(bytes.array()));
  }
}
