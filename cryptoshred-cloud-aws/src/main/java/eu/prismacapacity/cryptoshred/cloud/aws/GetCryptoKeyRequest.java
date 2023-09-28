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

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import lombok.NonNull;
import lombok.Value;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

@Value(staticConstructor = "of")
class GetCryptoKeyRequest {
  @NonNull private final CryptoSubjectId subjectId;

  @NonNull private final CryptoAlgorithm algorithm;

  @NonNull private final CryptoKeySize size;

  @NonNull private final String tableName;

  GetItemRequest toDynamoRequest() {
    return GetItemRequest.builder()
        .tableName(tableName)
        .key(Utils.subjectIdToKeyAttributeMap(subjectId))
        .consistentRead(true)
        .projectionExpression(Utils.generateKeyPropertyName(algorithm, size))
        .build();
  }
}
