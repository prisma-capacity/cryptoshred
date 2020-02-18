package eu.prismacapacity.cryptoshred;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;

import eu.prismacapacity.cryptoshred.keys.CryptoKeySize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

//@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(using = CryptoContainer.Deserializer.class)
@JsonSerialize(using = CryptoContainer.Serializer.class)
public class CryptoContainer<T> {
	private static final String JSON_KEY_ENCRYPTED_BYTES = "enc";
	private static final String JSON_KEY_SUBJECT_ID = "id";
	private static final String JSON_KEY_KEY_SIZE = "ksize";
	private static final String JSON_KEY_ALGO = "algo";

	public static class Deserializer extends JsonDeserializer<CryptoContainer<?>> implements ContextualDeserializer {

		private JavaType contextualType;

		@Override
		public CryptoContainer<?> deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {

			Class<?> targetType = contextualType.getBindings().getBoundType(0).getRawClass();

			JsonNode tree = jp.getCodec().readTree(jp);
			int keySize = (Integer) ((IntNode) tree.get(JSON_KEY_KEY_SIZE)).numberValue();
			String subjectId = tree.get(JSON_KEY_SUBJECT_ID).asText();
			String algo = tree.get(JSON_KEY_ALGO).asText();
			byte[] encrypted = tree.get(JSON_KEY_ENCRYPTED_BYTES).binaryValue();

			CryptoContainer<?> cc = new CryptoContainer<>(targetType, CryptoAlgorithm.of(algo),
					CryptoKeySize.of(keySize), CryptoSubjectId.of(UUID.fromString(subjectId)), encrypted);

			cc.mapper = (CryptoObjectMapper) ctxt.findInjectableValue(CryptoObjectMapper.JACKSON_INJECT_NAME,
					new BeanProperty.Bogus(), cc);

			return cc;
		}

		@Override
		public JsonDeserializer<CryptoContainer<?>> createContextual(DeserializationContext ctx, BeanProperty prop)
				throws JsonMappingException {
			contextualType = ctx.getContextualType();
			return this;
		}

	}

	public static class Serializer extends JsonSerializer<CryptoContainer<?>> {

		@Override
		public void serialize(CryptoContainer<?> value, JsonGenerator jgen, SerializerProvider serializers)
				throws IOException {
			jgen.writeStartObject();
			jgen.writeStringField(JSON_KEY_ALGO, value.getAlgo().getId());
			jgen.writeNumberField(JSON_KEY_KEY_SIZE, value.getSize().getKeySize());
			jgen.writeStringField(JSON_KEY_SUBJECT_ID, value.getSubjectId().getId().toString());
			jgen.writeBinaryField(JSON_KEY_ENCRYPTED_BYTES, value.getEncryptedBytes());
			jgen.writeEndObject();
		}

	}

	private CryptoContainer(@NonNull Class<T> type, @NonNull CryptoAlgorithm algo, @NonNull CryptoKeySize size,
			@NonNull CryptoSubjectId subjectId, @NonNull byte[] encryptedBytes) {
		this.type = type;
		this.algo = algo;
		this.size = size;
		this.subjectId = subjectId;
		this.encryptedBytes = encryptedBytes;
	}

	private CryptoContainer(@NonNull Class<T> class1, @NonNull CryptoAlgorithm algorithm,
			@NonNull CryptoKeySize keySize, @NonNull CryptoSubjectId id, @NonNull byte[] encryptedBytes,
			@NonNull T value, @NonNull CryptoObjectMapper cryptoObjectMapper) {
		this.cachedValue = Optional.ofNullable(value);
		this.mapper = cryptoObjectMapper;
		this.type = class1;
		this.algo = algorithm;
		this.size = keySize;
		this.encryptedBytes = encryptedBytes;
		this.subjectId = id;
	}

	static <T> CryptoContainer<T> fromValue(@NonNull Class<T> type, @NonNull CryptoAlgorithm algorithm,
			@NonNull CryptoKeySize keySize, @NonNull CryptoSubjectId id, @NonNull byte[] encryptedBytes,
			@NonNull T value, CryptoObjectMapper cryptoObjectMapper) {
		return new CryptoContainer<T>(type, algorithm, keySize, id, encryptedBytes, value, cryptoObjectMapper);
	}

	@Getter
	private Class<?> type;

	@Getter
	private CryptoAlgorithm algo;

	@Getter
	private CryptoKeySize size;

	@Getter
	private CryptoSubjectId subjectId;

	// the encrypted value
	@Getter(value = AccessLevel.PACKAGE)
	private byte[] encryptedBytes;

	// set after decryption or before encryption for short circuit retrieval
	private transient Optional<T> cachedValue;

	private transient CryptoObjectMapper mapper;

	private T value() {
		if (cachedValue == null) {
			cachedValue = Optional.ofNullable(mapper.unwrap(this));
		}
		return cachedValue.orElse(null);
	}

	////////////////////////////////////////
	// stolen from optional
	////////////////////////////////////////
	public T get() {
		if (value() == null) {
			throw new NoSuchElementException("No value present");
		}
		return value();
	}

	public boolean isPresent() {
		return value() != null;
	}

	public void ifPresent(Consumer<? super T> consumer) {
		if (value() != null)
			consumer.accept(value());
	}

	public Optional<T> filter(Predicate<? super T> predicate) {
		Objects.requireNonNull(predicate);
		if (!isPresent())
			return Optional.empty();
		else
			return predicate.test(value()) ? cachedValue : Optional.empty();
	}

	public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		if (!isPresent())
			return Optional.empty();
		else {
			return Optional.ofNullable(mapper.apply(value()));
		}
	}

	public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
		Objects.requireNonNull(mapper);
		if (!isPresent())
			return Optional.empty();
		else {
			return Objects.requireNonNull(mapper.apply(value()));
		}
	}

	public T orElse(T other) {
		return value() != null ? value() : other;
	}

	public T orElseGet(Supplier<? extends T> other) {
		return value() != null ? value() : other.get();
	}

	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (value() != null) {
			return value();
		} else {
			throw exceptionSupplier.get();
		}
	}

}
