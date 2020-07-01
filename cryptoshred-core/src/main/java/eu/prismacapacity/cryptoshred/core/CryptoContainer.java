/*
 * Copyright © 2020 PRISMA European Capacity Platform GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.prismacapacity.cryptoshred.core;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;

import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;

@JsonDeserialize(using = CryptoContainer.Deserializer.class)
@JsonSerialize(using = CryptoContainer.Serializer.class)
public class CryptoContainer<T> extends OptionalBehavior<T> {
	private static final String JSON_KEY_ENCRYPTED_BYTES = "enc";

	private static final String JSON_KEY_SUBJECT_ID = "id";

	private static final String JSON_KEY_KEY_SIZE = "ksize";

	private static final String JSON_KEY_ALGO = "algo";

	public static class Deserializer extends JsonDeserializer<CryptoContainer<?>> implements ContextualDeserializer {

		private JavaType contextualType;

		@Override
		public CryptoContainer<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			JavaType boundType = contextualType.getBindings().getBoundType(0);
			if (boundType == null)
				throw new IllegalArgumentException(
						"Cannot infer the container's parameter type. Avoid using RAW-types or use 'new TypeReference<CryptoContainer<String>>() {}' depending on your context.");

			Class<?> targetType = boundType.getRawClass();

			JsonNode tree = jp.getCodec().readTree(jp);
			int keySize = (Integer) ((IntNode) tree.get(JSON_KEY_KEY_SIZE)).numberValue();
			String subjectId = tree.get(JSON_KEY_SUBJECT_ID).asText();
			String algo = tree.get(JSON_KEY_ALGO).asText();
			byte[] encrypted = tree.get(JSON_KEY_ENCRYPTED_BYTES).binaryValue();

			CryptoObjectMapper om = (CryptoObjectMapper) ctxt
					.findInjectableValue(CryptoObjectMapper.JACKSON_INJECT_NAME, new BeanProperty.Bogus(), null);
			CryptoContainer<?> cc = new CryptoContainer<>(targetType, CryptoAlgorithm.of(algo),
					CryptoKeySize.of(keySize), CryptoSubjectId.of(UUID.fromString(subjectId)), encrypted, om);

			return cc;
		}

		@Override
		public JsonDeserializer<CryptoContainer<?>> createContextual(DeserializationContext ctx, BeanProperty prop) {
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
			jgen.writeNumberField(JSON_KEY_KEY_SIZE, value.getSize().asInt());
			jgen.writeStringField(JSON_KEY_SUBJECT_ID, value.getSubjectId().getId().toString());
			jgen.writeBinaryField(JSON_KEY_ENCRYPTED_BYTES, value.getEncryptedBytes());
			jgen.writeEndObject();
		}

	}

	protected CryptoContainer(@NonNull Class<T> type, @NonNull CryptoAlgorithm algo, @NonNull CryptoKeySize size,
			@NonNull CryptoSubjectId subjectId, @NonNull byte[] encryptedBytes,
			@NonNull CryptoObjectMapper cryptoObjectMapper) {
		this.type = type;
		this.algo = algo;
		this.size = size;
		this.subjectId = subjectId;
		this.encryptedBytes = encryptedBytes;
		this.mapper = cryptoObjectMapper;
	}

	protected CryptoContainer(@NonNull Class<T> class1, @NonNull CryptoAlgorithm algorithm,
			@NonNull CryptoKeySize keySize, @NonNull CryptoSubjectId id, byte[] encryptedBytes, T value,
			@NonNull CryptoObjectMapper cryptoObjectMapper) {
		this.cachedValue = Optional.ofNullable(value);
		this.mapper = cryptoObjectMapper;
		this.type = class1;
		this.algo = algorithm;
		this.size = keySize;
		this.encryptedBytes = encryptedBytes;
		this.subjectId = id;
	}

	@Getter
	private final Class<?> type;

	@Getter
	private final CryptoAlgorithm algo;

	@Getter
	private final CryptoKeySize size;

	@Getter
	private final CryptoSubjectId subjectId;

	// the encrypted value
	@Getter(value = AccessLevel.PACKAGE)
	private final byte[] encryptedBytes;

	// set after decryption or before encryption for short circuit retrieval
	private transient Optional<T> cachedValue;

	private final transient CryptoObjectMapper mapper;

	@Override
	protected T value() {
		if (cachedValue == null) {
			cachedValue = Optional.ofNullable(mapper.unwrap(this));
		}
		return cachedValue.orElse(null);
	}

}
