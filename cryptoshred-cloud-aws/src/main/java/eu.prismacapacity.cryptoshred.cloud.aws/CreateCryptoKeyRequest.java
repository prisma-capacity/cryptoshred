package eu.prismacapacity.cryptoshred.cloud.aws;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import eu.prismacapacity.cryptoshred.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.keys.CryptoKeySize;
import lombok.NonNull;
import lombok.Value;

import java.nio.ByteBuffer;
import java.util.HashMap;

@Value(staticConstructor = "of")
public class CreateCryptoKeyRequest {
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
        return new UpdateItemRequest()
                .withTableName(tableName)
                .withKey(Utils.subjectIdToKeyAttributeMap(subjectId))
                .withConditionExpression("attribute_not_exists(#k)")
                .withUpdateExpression("SET #k = :v")
                .withExpressionAttributeNames(new HashMap<String, String>() {{
                    put("#k", Utils.generateKeyPropertyName(algorithm, size));
                }})
                .withReturnValues(ReturnValue.ALL_NEW)
                .withExpressionAttributeValues(
                        new HashMap<String, AttributeValue>() {{
                            put(":v", new AttributeValue()
                                    .withB(ByteBuffer.wrap(key.getBytes()))
                            );
                        }}
                );
    }
}
