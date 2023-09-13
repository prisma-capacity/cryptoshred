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

import java.util.HashMap;
import java.util.UUID;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import lombok.val;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class UtilsTest {

  @Test
  void subjectIdToKeyAttributeMap() {
    val subjectId = CryptoSubjectId.of(UUID.randomUUID());

    val attributeMap = Utils.subjectIdToKeyAttributeMap(subjectId);

    Assert.assertEquals(attributeMap.get("subjectId").s(), subjectId.getId().toString());
    Assert.assertEquals(attributeMap.size(), 1);
  }

  @Test
  void generateKeyPropertyName() {
    val algorithm = CryptoAlgorithm.AES_CBC;
    val size = CryptoKeySize.BIT_256;

    val propertyName = Utils.generateKeyPropertyName(algorithm, size);

    Assert.assertEquals(propertyName, "AES256");
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

    Assert.assertTrue(key.isPresent());
    Assert.assertEquals(key.get().getBytes(), bytes);
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

    Assert.assertFalse(key.isPresent());
  }

  @Test
  void testExtractCryptoKeyFromItemForMissingProperty() {
    val algorithm = CryptoAlgorithm.AES_CBC;
    val size = CryptoKeySize.BIT_256;

    val item = new HashMap<String, AttributeValue>();
    val key = Utils.extractCryptoKeyFromItem(algorithm, size, item);

    Assert.assertFalse(key.isPresent());
  }
}
