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
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(using = CryptoContainer.Deserializer.class)
@JsonSerialize(using = CryptoContainer.Serializer.class)
public class CryptoContainer<T> {
	private static final String JSON_KEY_ENCRYPTED_BYTES = "enc";
	private static final String JSON_KEY_SUBJECT_ID = "id";
	private static final String JSON_KEY_KEY_SIZE = "ksize";
	private static final String JSON_KEY_ALGO = "algo";

	public static class Deserializer extends JsonDeserializer<CryptoContainer<?>> implements ContextualDeserializer {

		private JavaType javaType;

		@Override
		public CryptoContainer<?> deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {

			Class<?> targetType = javaType.getBindings().getBoundType(0).getRawClass();

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
			javaType = ctx.getContextualType();

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
			jgen.writeStringField(JSON_KEY_SUBJECT_ID, value.getSubjectId().getSubjectId().toString());
			jgen.writeBinaryField(JSON_KEY_ENCRYPTED_BYTES, value.getEncryptedBytes());
			jgen.writeEndObject();
		}

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
	private CryptoAlgorithm algo;

	@Getter
	private CryptoKeySize size;

	@Getter
	private CryptoSubjectId subjectId;

	// the encrypted value
	@Getter(value = AccessLevel.PROTECTED)
	private byte[] encryptedBytes;

	// set after decryption or before encryption for short circuit retrieval
	private transient T cachedValue;

	private transient CryptoObjectMapper mapper;

	// stolen from optional
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
			return predicate.test(value()) ? Optional.of(cachedValue) : Optional.empty();
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

	private T value() {
		if (cachedValue == null) {
			cachedValue = mapper.unwrap(this);
		}
		return cachedValue;
	}

}
