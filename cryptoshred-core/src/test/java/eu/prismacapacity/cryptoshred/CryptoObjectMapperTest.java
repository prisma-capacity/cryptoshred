package eu.prismacapacity.cryptoshred;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.prismacapacity.cryptoshred.keys.CryptoKeySize;
import lombok.Data;

public class CryptoObjectMapperTest {

	@Test
	void testHappyPath() throws Exception {

		CryptoEngine engine = new JDKCryptoEngine(CryptoInitializationVector.of("mysecret"));

		CryptoObjectMapper om = CryptoObjectMapper.builder(new InMemCryptoKeyRepository(engine), engine)
				.defaultKeySize(CryptoKeySize.of(128)).build();
		CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());
		String json = om.writeValueAsString(new Foo(om.wrap("Peter", id)));
		System.out.println("serialized to json: " + json);

		Foo foo2 = om.readValue(json, Foo.class);
		System.out.println("foo2.bar: " + foo2.bar);
		System.out.println("foo2.name: " + foo2.name.optional().orElse("lost"));

	}

	@Data
	public static class Foo {
		int bar = 7;
		CryptoContainer<String> name;

		@JsonCreator
		public Foo(@JsonProperty("name") CryptoContainer<String> name) {
			this.name = name;
		}
	}

}
