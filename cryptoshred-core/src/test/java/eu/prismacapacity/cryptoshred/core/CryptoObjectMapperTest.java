package eu.prismacapacity.cryptoshred.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import eu.prismacapacity.cryptoshred.core.CryptoContainer;
import eu.prismacapacity.cryptoshred.core.CryptoContainerFactory;
import eu.prismacapacity.cryptoshred.core.CryptoEngine;
import eu.prismacapacity.cryptoshred.core.CryptoInitializationVector;
import eu.prismacapacity.cryptoshred.core.CryptoObjectMapper;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import eu.prismacapacity.cryptoshred.core.JDKCryptoEngine;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CryptoObjectMapperTest {

	@Test
	void testHappyPath() throws Exception {
		// arrange
		CryptoEngine engine = new JDKCryptoEngine(CryptoInitializationVector.of("mysecret"));

		CryptoObjectMapper om = CryptoObjectMapper.builder(new InMemCryptoKeyRepository(engine), engine)
				.defaultKeySize(CryptoKeySize.of(128)).build();

		CryptoContainerFactory factory = om; // optional

		CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());

		// act

		Foo foo = new Foo();
		foo.name = factory.wrap("Peter", id);

		String json = om.writeValueAsString(foo);

		// remove
		System.out.println("serialized to json: " + json);

		Foo foo2 = om.readValue(json, Foo.class);

		assertEquals(foo.bar, foo2.bar);
		String fooName = foo.name.get();
		String foo2Name = foo2.name.get();
		assertEquals(fooName, foo2Name);

		Bar b = new Bar();
		b.pair = factory.wrap(new Pair<>("hubba", 77), id);
		json = om.writeValueAsString(b);
		System.out.println(json);
		Bar b2 = om.readValue(json, Bar.class);

		assertEquals(b.pair.get(), b2.pair.get());

		CryptoContainer<String> c = factory.wrap("hubbi", id);
		json = om.writeValueAsString(c);
		System.out.println(json);
		// if we need to deserialize a container without a surrounding bean (so without
		// type info), we need to pass a type-reference
		CryptoContainer<String> c2 = om.readValue(json, new TypeReference<CryptoContainer<String>>() {
		});
		assertEquals(String.class, c2.get().getClass());
		assertEquals("hubbi", c2.get());

	}

	@Data
	public static class Foo {
		int bar = 7;
		CryptoContainer<String> name;
	}

	@Data
	public static class Bar {
		int bar = 7;
		CryptoContainer<Pair<String, Integer>> pair;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@EqualsAndHashCode
	public static class Pair<K, V> {
		K left;
		V right;
	}
}
