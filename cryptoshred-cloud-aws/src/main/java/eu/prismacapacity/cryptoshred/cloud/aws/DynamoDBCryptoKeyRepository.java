package eu.prismacapacity.cryptoshred.cloud.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import eu.prismacapacity.cryptoshred.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.CryptoEngine;
import eu.prismacapacity.cryptoshred.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.keys.CryptoKeyNotFoundAfterCreatingException;
import eu.prismacapacity.cryptoshred.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.metrics.CryptoMetrics;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

/**
 * CryptoKeyRepository implementation based on AWS DynamoDB. Supports multiple
 * keys (algorithm + size) for the same subject
 *
 * @author otbe
 */
@RequiredArgsConstructor
public class DynamoDBCryptoKeyRepository implements CryptoKeyRepository {
    @NonNull
    private final CryptoEngine engine;

    @NonNull
    private final AmazonDynamoDB dynamoDB;

    @NonNull
    private final CryptoMetrics metrics;

    @NonNull
    private final String tableName;

    @Override
    public Optional<CryptoKey> findKeyFor(@NonNull CryptoSubjectId subjectId, @NonNull CryptoAlgorithm algorithm,
                                          @NonNull CryptoKeySize size) {
        metrics.notifyKeyLookUp();

        val getRequest = GetCryptoKeyRequest.of(subjectId, algorithm, size, tableName);

        val item = metrics.timed("findKeyInDynamoDbTable",
                () -> dynamoDB.getItem(getRequest.toDynamoRequest()).getItem());

        if (item == null) {
            return Optional.empty();
        }

        return Utils.extractCryptoKeyFromItem(algorithm, size, item);
    }

    @Override
    public CryptoKey getOrCreateKeyFor(@NonNull CryptoSubjectId subjectId, @NonNull CryptoAlgorithm algorithm,
                                       @NonNull CryptoKeySize size) throws CryptoKeyNotFoundAfterCreatingException {
        return findKeyFor(subjectId, algorithm, size).orElseGet(() -> createCryptoKey(subjectId, algorithm, size));
    }

    protected CryptoKey createCryptoKey(CryptoSubjectId subjectId, CryptoAlgorithm algorithm, CryptoKeySize size) {
        val key = engine.generateKey(algorithm, size);
        val createRequest = CreateCryptoKeyRequest.of(subjectId, algorithm, size, key, tableName);

        metrics.notifyKeyCreation();

        try {
            val result = metrics.timed("createKeyInDynamoDbTable",
                    () -> dynamoDB.updateItem(createRequest.toDynamoRequest())
            );

            val resultKey = Utils.extractCryptoKeyFromItem(algorithm, size, result.getAttributes());

            if (!resultKey.isPresent()) {
                // should never ever happen because that would indicate a broken DynamoDB API
                // contract
                throw new CryptoKeyNotFoundAfterCreatingException("Something weird happened. Check DynamoDB config.");
            }

            return resultKey.get();
        } catch (ConditionalCheckFailedException ignored) {
            // this happens when the key was not found in the first step but someone created
            // one in the meantime
            // the updateItem call checks that the key for algorithm and size does not exist
            // before updating/creating the item
            // so we can safely (consistent) read from the table and get our key
            val item = findKeyFor(subjectId, algorithm, size);

            if (!item.isPresent()) {
                throw new IllegalStateException("DynamoDB consistent read failed.");
            }

            return item.get();
        }
    }
}
