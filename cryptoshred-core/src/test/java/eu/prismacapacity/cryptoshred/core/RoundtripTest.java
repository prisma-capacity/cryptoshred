package eu.prismacapacity.cryptoshred.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.*;

public class RoundtripTest {
    ObjectMapper om;

    @BeforeEach
    void setup() {
        CryptoEngine engine = new JDKCryptoEngine(CryptoInitializationVector.of("mysecret"));
        InMemCryptoKeyRepository keyRepository = new InMemCryptoKeyRepository(engine);
        om = new ObjectMapper();
        om.registerModule(new CryptoModule(engine, keyRepository));
    }


    @Test
    void testHappyPath() throws Exception {
        // arrange

        CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());

        // act

        Foo foo = new Foo();
        foo.name = new CryptoContainer<>("Peter", id);

        String json = om.writeValueAsString(foo);

        // remove
        System.out.println("serialized to json: " + json);

        Foo foo2 = om.readValue(json, Foo.class);

        assertEquals(foo.bar, foo2.bar);
        String fooName = foo.name.get();
        String foo2Name = foo2.name.get();
        assertEquals(fooName, foo2Name);

        Bar b = new Bar();
        b.pair = new CryptoContainer<>(new Pair<>("hubba", 77), id);
        json = om.writeValueAsString(b);
        System.out.println(json);
        Bar b2 = om.readValue(json, Bar.class);

        assertEquals(b.pair.get(), b2.pair.get());

        CryptoContainer<String> c = new CryptoContainer<>("hubbi", id);
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
