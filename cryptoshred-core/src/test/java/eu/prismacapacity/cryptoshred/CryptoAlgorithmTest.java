package eu.prismacapacity.cryptoshred;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CryptoAlgorithmTest {

	@Test
	public void testOf() throws Exception {
		assertThrows(NullPointerException.class, () -> {
			CryptoAlgorithm.of(null);
		});

		// must not fail
		CryptoAlgorithm.of("AES");
	}

}
