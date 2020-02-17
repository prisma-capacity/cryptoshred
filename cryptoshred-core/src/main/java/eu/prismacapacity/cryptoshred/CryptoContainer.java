package eu.prismacapacity.cryptoshred;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import eu.prismacapacity.cryptoshred.keys.CryptoKeySize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CryptoContainer<T> {

	public static final String CYPTOSHREDDING_KEY_REPOSITORY = "cyptoshredding.keyRepository";

	public static final String CYPTOSHREDDING_ALGORITHM_FACTORY = "cyptoshredding.algorithmFactory";

	public static final String CYPTOSHREDDING_METRICS = "cyptoshredding.metrics";

	@JsonCreator
	protected CryptoContainer() {
	}

	public CryptoContainer(Class<T> type, CryptoAlgorithm algo, CryptoKeySize size, CryptoSubjectId subjectId,
			byte[] encryptedBytes) {
		this.type = type;
		this.algo = algo;
		this.size = size;
		this.subjectId = subjectId;
		this.encryptedBytes = encryptedBytes;

	}

	@Getter
	private Class<?> type;

	@Getter
	@JsonUnwrapped
	private CryptoAlgorithm algo;

	@Getter
	@JsonUnwrapped
	private CryptoKeySize size;

	@Getter
	@JsonUnwrapped
	private CryptoSubjectId subjectId;

	// the encrypted value
	@Getter(value = AccessLevel.PROTECTED)
	@JsonProperty
	private byte[] encryptedBytes;

	@JsonIgnore
	// set after decryption or before encryption for short circuit retrieval
	private transient Optional<T> cachedValue;

	@JsonIgnore
	@JacksonInject(value = CryptoObjectMapper.JACKSON_INJECT_NAME)
	private transient CryptoObjectMapper mapper;

	public Optional<T> optional() {
		if (cachedValue == null) {
			cachedValue = mapper.unwrap(this);
		}
		return cachedValue;
	}

}
