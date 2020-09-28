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

import java.nio.ByteBuffer;

import lombok.NonNull;
import lombok.Value;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;

@Value(staticConstructor = "of")
class CreateCryptoKeyRequest {
	@NonNull
	private final CryptoSubjectId subjectId;

	@NonNull
	private final CryptoAlgorithm algorithm;

	@NonNull
	private final CryptoKeySize size;

	@NonNull
	private final CryptoKey key;

	@NonNull
	private final String tableName;

	UpdateItemRequest toDynamoRequest() {
		return new UpdateItemRequest().withTableName(tableName).withKey(Utils.subjectIdToKeyAttributeMap(subjectId))
				.withConditionExpression("attribute_not_exists(#k)").withUpdateExpression("SET #k = :v")
				.withExpressionAttributeNames(Maps.of("#k", Utils.generateKeyPropertyName(algorithm, size)))
				.withReturnValues(ReturnValue.ALL_NEW).withExpressionAttributeValues(
						Maps.of(":v", new AttributeValue().withB(ByteBuffer.wrap(key.getBytes()))));
	}
}
