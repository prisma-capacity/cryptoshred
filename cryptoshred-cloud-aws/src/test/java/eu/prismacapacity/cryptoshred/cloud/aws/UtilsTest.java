package eu.prismacapacity.cryptoshred.cloud.aws;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import eu.prismacapacity.cryptoshred.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.keys.CryptoKeySize;
import lombok.val;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

class UtilsTest {

    @Test
    void subjectIdToKeyAttributeMap() {
        val subjectId = CryptoSubjectId.of(UUID.randomUUID());

        val attributeMap = Utils.subjectIdToKeyAttributeMap(subjectId);

        Assert.assertEquals(attributeMap.get("subjectId").getS(), subjectId.getId().toString());
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

        val item = new HashMap<String, AttributeValue>() {{
            put(propertyName, new AttributeValue().withB(ByteBuffer.wrap(bytes)));
        }};

        val key = Utils.extractCryptoKeyFromItem(algorithm, size, item);

        Assert.assertTrue(key.isPresent());
        Assert.assertEquals(key.get().getBytes(), bytes);
    }

    @Test
    void testExtractCryptoKeyFromItemForInvalidAttributeType() {
        val algorithm = CryptoAlgorithm.AES_CBC;
        val size = CryptoKeySize.BIT_256;

        val propertyName = Utils.generateKeyPropertyName(algorithm, size);
        val item = new HashMap<String, AttributeValue>() {{
            put(propertyName, new AttributeValue().withS("foo"));
        }};

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