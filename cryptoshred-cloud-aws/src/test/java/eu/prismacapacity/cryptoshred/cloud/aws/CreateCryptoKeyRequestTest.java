package eu.prismacapacity.cryptoshred.cloud.aws;

import eu.prismacapacity.cryptoshred.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.keys.CryptoKeySize;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.Assert.*;

class CreateCryptoKeyRequestTest {
    CryptoSubjectId subjectId = CryptoSubjectId.of(UUID.randomUUID());
    CryptoAlgorithm algorithm = CryptoAlgorithm.AES_CBC;
    CryptoKeySize size = CryptoKeySize.BIT_256;
    CryptoKey key = CryptoKey.fromBytes("foo".getBytes());

    @Test
    void testToDynamoRequestForConditionExpression() {
        val uut = CreateCryptoKeyRequest.of(subjectId, algorithm, size, key, "foo");

        val dynamoRequest = uut.toDynamoRequest();

        assertTrue(dynamoRequest
                .getConditionExpression()
                .contains("attribute_not_exists(#k)"));
    }
}