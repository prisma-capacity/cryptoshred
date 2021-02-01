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

        Baz baz = new Baz();
        baz.name = new CryptoContainer<>("Peter", id);
        baz.pair = new CryptoContainer<>(new Pair<>("hubba", 77), id);
        json = om.writeValueAsString(baz);
        System.out.println(json);
        Baz baz2 = om.readValue(json, Baz.class);

        assertEquals(baz.name.get(), baz2.name.get());
        assertEquals(baz.pair.get(), baz2.pair.get());

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

    @Test
    void testDeAndReSerialization() throws Exception{
        // arrange

        CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());
        Foo foo = new Foo();
        foo.name = new CryptoContainer<>("Peter", id);
        // just need a crypto-json that can be deserialized for later
        String json = om.writeValueAsString(foo);

        // act

        final Foo deserializedFoo = om.readValue(json, new TypeReference<Foo>() {
        });
        // not accessing deserializedFoo.name here, so that the lazy decryption does not occur
        final String reserializedFoo = om.writeValueAsString(deserializedFoo);

        // assert that it worked and we got a result and no exception

        assertNotNull(reserializedFoo);
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

    @Data
    public static class Baz {
        CryptoContainer<String> name;
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
