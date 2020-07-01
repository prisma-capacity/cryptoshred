package eu.prismacapacity.cryptoshred.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CryptoAlgorithmTest {

	@Test
	public void testOf()  {
		assertThrows(NullPointerException.class, () -> CryptoAlgorithm.of(null));

		// must not fail
		CryptoAlgorithm.of("AES");
	}

}
