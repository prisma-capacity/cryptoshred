package eu.prismacapacity.cryptoshred;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import eu.prismacapacity.cryptoshred.keys.CryptoKeySize;
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
