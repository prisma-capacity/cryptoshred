/*
 * Copyright © 2020-2025 PRISMA European Capacity Platform GmbH 
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import java.util.HashMap;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class UtilsTest {

  @Test
  void subjectIdToKeyAttributeMap() {
    val subjectId = CryptoSubjectId.of(UUID.randomUUID());

    val attributeMap = Utils.subjectIdToKeyAttributeMap(subjectId);

    assertEquals(attributeMap.get("subjectId").s(), subjectId.getId().toString());
    assertEquals(1, attributeMap.size());
  }

  @Test
  void generateKeyPropertyName() {
    val algorithm = CryptoAlgorithm.AES_CBC;
    val size = CryptoKeySize.BIT_256;

    val propertyName = Utils.generateKeyPropertyName(algorithm, size);

    assertEquals("AES256", propertyName);
  }

  @Test
  void testExtractCryptoKeyFromItemForValidItem() {
    val algorithm = CryptoAlgorithm.AES_CBC;
    val size = CryptoKeySize.BIT_256;

    val propertyName = Utils.generateKeyPropertyName(algorithm, size);
    val bytes = "foo".getBytes();

    val item =
        new HashMap<String, AttributeValue>() {
          {
            put(propertyName, AttributeValue.fromB(SdkBytes.fromByteArray(bytes)));
          }
        };

    val key = Utils.extractCryptoKeyFromItem(algorithm, size, item);

    assertTrue(key.isPresent());
    assertArrayEquals(key.get().getBytes(), bytes);
  }

  @Test
  void testExtractCryptoKeyFromItemForInvalidAttributeType() {
    val algorithm = CryptoAlgorithm.AES_CBC;
    val size = CryptoKeySize.BIT_256;

    val propertyName = Utils.generateKeyPropertyName(algorithm, size);
    val item =
        new HashMap<String, AttributeValue>() {
          {
            put(propertyName, AttributeValue.fromS("foo"));
          }
        };

    val key = Utils.extractCryptoKeyFromItem(algorithm, size, item);

    assertFalse(key.isPresent());
  }

  @Test
  void testExtractCryptoKeyFromItemForMissingProperty() {
    val algorithm = CryptoAlgorithm.AES_CBC;
    val size = CryptoKeySize.BIT_256;

    val item = new HashMap<String, AttributeValue>();
    val key = Utils.extractCryptoKeyFromItem(algorithm, size, item);

    assertFalse(key.isPresent());
  }
}
