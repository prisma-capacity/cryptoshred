package eu.prismacapacity.cryptoshred.cloud.aws;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.Assert.*;

class GetCryptoKeyRequestTest {
    CryptoSubjectId subjectId = CryptoSubjectId.of(UUID.randomUUID());
    CryptoAlgorithm algorithm = CryptoAlgorithm.AES_CBC;
    CryptoKeySize size = CryptoKeySize.BIT_256;

    @Test
    void toDynamoRequest() {
        val request = GetCryptoKeyRequest.of(subjectId, algorithm, size, "foo");

        val dynamoRequest = request.toDynamoRequest();

        assertTrue(dynamoRequest.getConsistentRead());
    }
}