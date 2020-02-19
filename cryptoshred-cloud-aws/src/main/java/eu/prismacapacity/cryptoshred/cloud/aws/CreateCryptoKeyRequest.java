package eu.prismacapacity.cryptoshred.cloud.aws;

import java.nio.ByteBuffer;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import lombok.NonNull;
import lombok.Value;

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
				.withReturnValues(ReturnValue.ALL_NEW)
				.withExpressionAttributeValues(Maps.of(":v", new AttributeValue().withB(ByteBuffer.wrap(key.getBytes()))));
	}
}
