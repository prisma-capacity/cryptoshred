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

import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;

class GetCryptoKeyRequestTest {
  CryptoSubjectId subjectId = CryptoSubjectId.of(UUID.randomUUID());
  CryptoAlgorithm algorithm = CryptoAlgorithm.AES_CBC;
  CryptoKeySize size = CryptoKeySize.BIT_256;

  @Test
  void toDynamoRequest() {
    val request = GetCryptoKeyRequest.of(subjectId, algorithm, size, "foo");

    val dynamoRequest = request.toDynamoRequest();

    assertTrue(dynamoRequest.consistentRead());
  }
}
