package eu.prismacapacity.cryptoshred.cloud.aws;

import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import lombok.val;

class Utils {
	static Map<String, AttributeValue> subjectIdToKeyAttributeMap(CryptoSubjectId subjectId) {
		return Maps.of("subjectId", new AttributeValue(subjectId.getId().toString()));
	}

	static String generateKeyPropertyName(CryptoAlgorithm algorithm, CryptoKeySize size) {
		return algorithm.getId() + size.asInt();
	}

	static Optional<CryptoKey> extractCryptoKeyFromItem(CryptoAlgorithm algorithm, CryptoKeySize size,
			Map<String, AttributeValue> item) {
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
