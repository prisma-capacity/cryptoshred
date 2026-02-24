/*
 * Copyright © 2020-2026 PRISMA European Capacity Platform GmbH
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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import java.util.UUID;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import java.util.UUID;
import lombok.*;
import org.junit.jupiter.api.*;

class RoundtripTest {
  ObjectMapper om;

  private InMemCryptoKeyRepository keyRepository;

  @BeforeEach
  void setup() {
    CryptoEngine engine = new JDKCryptoEngine("mysecret", false);
    keyRepository = new InMemCryptoKeyRepository(engine);
    om = new ObjectMapper();
    om.registerModule(new CryptoModule(engine, keyRepository));
  }


    @SneakyThrows
    @Test
    void testStaticIV(){
        CryptoEngine engine = new JDKCryptoEngine("mysecret", false);
        keyRepository = new InMemCryptoKeyRepository(engine);
        om = new ObjectMapper();
        om.registerModule(new CryptoModule(engine, keyRepository));

        // arrange
        CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());

        // act
        Foo foo = new Foo();
        foo.name = new CryptoContainer<>("Peter", id);
        String json1 = om.writeValueAsString(foo);
        String json2 = om.writeValueAsString(foo);

        assertEquals(json1,json2);
    }


    @SneakyThrows
  @Test
  void testRandomIV(){
      CryptoEngine engine = new JDKCryptoEngine("mysecret", true);
      keyRepository = new InMemCryptoKeyRepository(engine);
      om = new ObjectMapper();
      om.registerModule(new CryptoModule(engine, keyRepository));

      // arrange
      CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());

      // act

      Foo foo = new Foo();
      foo.name = new CryptoContainer<>("Peter", id);

      String json1 = om.writeValueAsString(foo);
      String json2 = om.writeValueAsString(foo);


      assertNotEquals(json1,json2);
  }

  @SneakyThrows@Test
  void testFailureWithRandomIVUsed(){

          CryptoEngine engine = new JDKCryptoEngine("mysecret", true);
          keyRepository = new InMemCryptoKeyRepository(engine);
          om = new ObjectMapper();
          om.registerModule(new CryptoModule(engine, keyRepository));

      CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());
      Foo foo = new Foo();
      foo.name = new CryptoContainer<>("Peter", id);
      String json = om.writeValueAsString(foo);

      // deserialize with random IV
      Foo foo2 = om.readValue(json,Foo.class);
      assertEquals(foo.bar,foo2.bar);
      assertEquals(foo.name.get(),foo2.name.get());

      // remove IV from json
      JsonNode tree = om.readTree(json);
      ObjectNode on = (ObjectNode) tree.get("name");
      on.remove("iv");

      // must fail due to unmatching IV
      Foo fooWithoutIV = om.readValue(tree.toString(), Foo.class);
      assertFalse(fooWithoutIV.name.isPresent()); // disappeared due to decryption failure



  }

    @SneakyThrows
    @Test
    void testDownwardsCompatibility(){
        CryptoEngine engine = new JDKCryptoEngine("mysecret", false);
        keyRepository = new InMemCryptoKeyRepository(engine);
        om = new ObjectMapper();
        om.registerModule(new CryptoModule(engine, keyRepository));

        CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());

        Foo foo = new Foo();
        foo.name = new CryptoContainer<>("Peter", id);

        String json = om.writeValueAsString(foo);

        Foo foo2 = om.readValue(json,Foo.class);
        assertEquals(foo.bar,foo2.bar);
        assertEquals(foo.name.get(),foo2.name.get());

        // remove IV from json
        JsonNode tree = om.readTree(json);
        ObjectNode on = (ObjectNode) tree.get("name");
        on.remove("iv");

        // should fall back to configured IV
        Foo foo3 = om.readValue(tree.toString(),Foo.class);
        assertEquals(foo.bar,foo3.bar);
        assertEquals(foo.name.get(),foo3.name.get());
    }

  @Test
  void testHappyPath() throws Exception {
    // arrange
    CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());

    // act

    Foo foo = new Foo();
    foo.name = new CryptoContainer<>("Peter", id);

    String json = om.writeValueAsString(foo);

    Foo foo2 = om.readValue(json, Foo.class);

    assertEquals(foo.bar, foo2.bar);
    String fooName = foo.name.get();
    String foo2Name = foo2.name.get();
    assertEquals(fooName, foo2Name);

    Bar b = new Bar();
    b.pair = new CryptoContainer<>(new Pair<>("hubba", 77), id);
    json = om.writeValueAsString(b);
    Bar b2 = om.readValue(json, Bar.class);

    assertEquals(b.pair.get(), b2.pair.get());

    Baz baz = new Baz();
    baz.name = new CryptoContainer<>("Peter", id);
    baz.pair = new CryptoContainer<>(new Pair<>("hubba", 77), id);
    json = om.writeValueAsString(baz);
    Baz baz2 = om.readValue(json, Baz.class);

    assertEquals(baz.name.get(), baz2.name.get());
    assertEquals(baz.pair.get(), baz2.pair.get());

    CryptoContainer<String> c = new CryptoContainer<>("hubbi", id);
    json = om.writeValueAsString(c);
    // if we need to deserialize a container without a surrounding bean (so without
    // type info), we need to pass a type-reference
    CryptoContainer<String> c2 =
        om.readValue(json, new TypeReference<CryptoContainer<String>>() {});
    assertEquals(String.class, c2.get().getClass());
    assertEquals("hubbi", c2.get());
  }

  @Test
  void testHappyPathLegacy() throws Exception {
    // arrange
    CryptoSubjectId id =
        CryptoSubjectId.of(UUID.fromString("a1f6280b-eddf-454d-a2d1-15ed4c716e8e"));
    CryptoKey k = CryptoKey.fromBase64("Mniow0ZBV2TEAUsoxH/hT+2e4yetncAjnpNCAHEbR+c=");
    keyRepository.keys.put(id, k);

    // act
    Foo fooNew = new Foo();
    fooNew.name = new CryptoContainer<>("Peter", id);
    String jsonNew = om.writeValueAsString(fooNew);
    // reload
    fooNew = om.readValue(jsonNew, Foo.class);

    // serialised with default iv
    String jsonLegacy =
        "{\"bar\":7,\"name\":{\"algo\":\"AES\",\"ksize\":256,\"id\":\"a1f6280b-eddf-454d-a2d1-15ed4c716e8e\",\"enc\":\"SylOMKTrag8qCH84xVhfqQ==\"}}";

    Foo fooLegacy = om.readValue(jsonLegacy, Foo.class);

    // not set on this old instance
    assertNull(fooLegacy.getName().getInitializationVector());

    // after encryption, iv must be set
    assertTrue(jsonNew.contains("\"iv\""));
    // and must be included in the container
    assertNotNull(fooNew.getName().getInitializationVector());

    String fooName = fooNew.name.get();
    String foo2Name = fooLegacy.name.get();
    assertEquals(fooName, foo2Name);
  }

  @Test
  void testDeAndReSerialization() throws Exception {
    // arrange

    CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());
    Foo foo = new Foo();
    foo.name = new CryptoContainer<>("Peter", id);
    // just need a crypto-json that can be deserialized for later
    String json = om.writeValueAsString(foo);

    // act

    final Foo deserializedFoo = om.readValue(json, new TypeReference<Foo>() {});
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
