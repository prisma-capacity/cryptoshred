<div align="right"><a target="myNextJob" href="https://www.prisma-capacity.eu/careers#job-offers">
    <img class="inline" src="prisma.png">
</a></div>

# cryptoshred

![Java CI](https://github.com/prisma-capacity/cryptoshred/workflows/Java%20CI/badge.svg?branch=main)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/333bfd567a6a447895212994b414f077)](https://app.codacy.com/gh/prisma-capacity/cryptoshred?utm_source=github.com&utm_medium=referral&utm_content=prisma-capacity/cryptoshred&utm_campaign=Badge_Grade_Settings)
[![codecov](https://codecov.io/gh/prisma-capacity/cryptoshred/branch/main/graph/badge.svg)](https://codecov.io/gh/prisma-capacity/cryptoshred)
[![MavenCentral](https://img.shields.io/maven-central/v/eu.prismacapacity/cryptoshred)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22eu.prismacapacity%22)
<a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img class="inline" src="https://img.shields.io/badge/license-ASL2-green.svg?style=flat">
</a>


### Motivation

Cryptoshredding is a well-known technique to 'erase' encrypted data by throwing away the key necessary for decryption. 
Two out of numerous advantages are

* You do not have to actually remove (or update) data, so that it can be immutable
* You don't have to keep track of where it is used, as long as it stays encrypted

see https://en.wikipedia.org/wiki/Crypto-shredding

### cryptoshred library

In order to make use of that technique, you need to encrypt data before writing in the first place and maintain keys for the subjects. This is what cryptoshred can do for you.

#### Features

* Pluggable Metrics interface
  * optional Micrometer impl.
* Pluggable CryptoEngine
  * JDK-based impl.
* Pluggable Key Repository
  * optional Amazon DynamoDB impl.
* optional Spring-Boot autoconfiguration module
* Jackson based deserialization to Java Objects
* Evolutionary approach to key definition creation and migration

### Encryption

In order to encrypt a particular piece of data, you wrap it in a CryptoContainer before persisting (serializing).

```java
@Data
public class Person { // might be an Enitity, a POJO you serialize or anything you want to persist
  int age;
  CryptoContainer<String> name;
}
```
In order to wrap the data you'll also need to provide a 'CryptoSubjectId', that references the actual key, that you'd want to throw away in order to do the 'deletion' afterwards)

```java
CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID()); // simple value object

Person p = new Person();
p.name = new CryptoContainer<>("Peter", id);
p.age = 30;

// go persist the Person
```
A CryptoContainer consists of the following data:

* SubjectId (which is associated to the actual key used for en/de-cryption)
* Algorithm used
* KeySize used
* Type of Object // String in this example
* Encoded byte array of the serialized form of the Object wrapped.

You can wrap any Jackson-serializable object like for instance

```java
CryptoKeyRepository repo = new InMemCryptoKeyRepository(engine); // don't do this at home
ObjectMapper om = new ObjectMapper();
om.registerModule(new CryptoModule(engine,repo)); // not needed when using Spring Boot autoconfiguration

CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID()); // simple value object, you would want to use a userId for that, and not a silly random.

Person p = new Person();
p.name = new CryptoContainer<>("Peter", id);
p.age = 30;
p.creditcard = new CryptoContainer<>(new CreditCardInfo("12341234",CrediCardTypes.VISA),id);
// go persist the Person
```

### Decryption

When using ObjectMapper to deserialize a 'CryptoContainer', all necessary dependencies are injected into the Container automatically, so you can use the 'CryptoContainer' just like an Optional (it copied all the methods from 'java.util.Optional').

```java

String json = om.writeValueAsString(p);

Person p2 = om.readValue(json, Person.class);
assertTrue(p2.name.isPresent());
assertEquals("Peter",p2.name.get());

// if you deleted the key in between, name.isPresent() would be false.
// of course you should rather use p2.name.orElse("unknown") or something rather than get, but you know all that from using Optional...
```

### Spring Boot Compatibility

| Library version | Spring Boot version |
|-----------------|---------------------|
| 1.x.x           | 2.7+                |
| 2.x.x           | 3.1+                |
| 3.x.x           | 4.0+                |