package eu.prismacapacity.cryptoshred.cloud.aws;

import com.amazonaws.services.dynamodbv2.model.GetItemRequest;

import eu.prismacapacity.cryptoshred.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.keys.CryptoKeySize;
import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
class GetCryptoKeyRequest {
	@NonNull
	private final CryptoSubjectId subjectId;

	@NonNull
	private final CryptoAlgorithm algorithm;

	@NonNull
	private final CryptoKeySize size;

	@NonNull
	private final String tableName;

	GetItemRequest toDynamoRequest() {
		return new GetItemRequest().withTableName(tableName).withKey(Utils.subjectIdToKeyAttributeMap(subjectId))
				.withConsistentRead(true).withProjectionExpression(Utils.generateKeyPropertyName(algorithm, size));
	}
}
