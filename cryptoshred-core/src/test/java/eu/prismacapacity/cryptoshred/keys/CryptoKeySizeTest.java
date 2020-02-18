package eu.prismacapacity.cryptoshred.keys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CryptoKeySizeTest {

	@Test
	public void testOf() throws Exception {

		assertThrows(IllegalArgumentException.class, () -> {
			CryptoKeySize.of(0);
		});

		assertEquals(128, CryptoKeySize.of(128).asInt());
		assertTrue(CryptoKeySize.of(128).toString().contains("128"));
	}

}
