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
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.IntNode;

import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;

public class CryptoModule extends SimpleModule {

	private final CryptoEngine engine;
	private final CryptoKeyRepository keyRepo;
	private final CryptoAlgorithm algo;
	private final CryptoKeySize keySize;
	private final CryptoMetrics metrics;
	private ObjectMapper om;

	public CryptoModule(CryptoEngine engine, CryptoKeyRepository keyRepo) {
		this(engine, keyRepo, CryptoAlgorithm.AES_CBC, CryptoKeySize.BIT_256, new CryptoMetrics.NOP());
	}

	public CryptoModule(CryptoEngine engine, CryptoKeyRepository keyRepo, CryptoAlgorithm algo, CryptoKeySize keySize,
			CryptoMetrics metrics) {
		this.engine = engine;
		this.keyRepo = keyRepo;
		this.algo = algo;
		this.keySize = keySize;
		this.metrics = metrics;

		addSerializer(CryptoContainer.class, new CryptoContainerSerializer());
		addDeserializer(CryptoContainer.class, new CryptoContainerDeserializer());
	}

	private static final String JSON_KEY_ENCRYPTED_BYTES = "enc";

	private static final String JSON_KEY_SUBJECT_ID = "id";

	private static final String JSON_KEY_KEY_SIZE = "ksize";

	private static final String JSON_KEY_ALGO = "algo";

	public class CryptoContainerDeserializer extends JsonDeserializer<CryptoContainer<?>>
			implements
				ContextualDeserializer {

		private JavaType contextualType;

		@Override
		public CryptoContainer<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			JavaType boundType = contextualType.getBindings().getBoundType(0);
			if (boundType == null) {
				throw new IllegalArgumentException(
						"Cannot infer the container's parameter type. Avoid using RAW-types or use 'new TypeReference<CryptoContainer<String>>() {}' depending on your context.");
			}

			Class<?> targetType = boundType.getRawClass();

			JsonNode tree = jp.getCodec().readTree(jp);
			int keySize = (Integer) ((IntNode) tree.get(JSON_KEY_KEY_SIZE)).numberValue();
			String subjectId = tree.get(JSON_KEY_SUBJECT_ID).asText();
			String algo = tree.get(JSON_KEY_ALGO).asText();
			byte[] encrypted = tree.get(JSON_KEY_ENCRYPTED_BYTES).binaryValue();

			return CryptoContainer.fromDeserialization(targetType, CryptoAlgorithm.of(algo), CryptoKeySize.of(keySize),
					CryptoSubjectId.of(UUID.fromString(subjectId)), encrypted, engine, keyRepo, metrics, om);
		}

		@Override
		public JsonDeserializer<CryptoContainer<?>> createContextual(DeserializationContext ctx, BeanProperty prop) {
			contextualType = ctx.getContextualType();
			return this;
		}

	}

	public class CryptoContainerSerializer extends JsonSerializer<CryptoContainer> {

		@Override
		public void serialize(CryptoContainer value, JsonGenerator jgen, SerializerProvider serializers)
				throws IOException {

			value.encrypt(keyRepo, engine, om);

			jgen.writeStartObject();
			jgen.writeStringField(JSON_KEY_ALGO, value.getAlgo().getId());
			jgen.writeNumberField(JSON_KEY_KEY_SIZE, value.getSize().asInt());
			jgen.writeStringField(JSON_KEY_SUBJECT_ID, value.getSubjectId().getId().toString());
			jgen.writeBinaryField(JSON_KEY_ENCRYPTED_BYTES, value.getEncryptedBytes());
			jgen.writeEndObject();
		}

	}

	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);
		this.om = context.getOwner();
	}
}
