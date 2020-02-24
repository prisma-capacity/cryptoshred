package eu.prismacapacity.cryptoshred.core.keys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CryptoKeyTest {
	@Test
	void testNullContracts() throws Exception {
		assertThrows(NullPointerException.class, () -> {
			CryptoKey.fromBase64(null);
		});
		assertThrows(NullPointerException.class, () -> {
			CryptoKey.fromBytes(null);
		});

	}

	@Test
	void testSymetry() throws Exception {
		CryptoKey key1 = CryptoKey.fromBytes("Foo".getBytes());
		CryptoKey key2 = CryptoKey.fromBase64(key1.getBase64());

		assertArrayEquals(key1.getBytes(), key2.getBytes());
		assertEquals(key1.getBase64(), key2.getBase64());
		assertEquals(key1, key2);
	}

	@Test
	void testToString() throws Exception {
		CryptoKey key1 = CryptoKey.fromBytes("Foo".getBytes());

		assertEquals(key1.getBase64(), key1.toString());

	}

}
