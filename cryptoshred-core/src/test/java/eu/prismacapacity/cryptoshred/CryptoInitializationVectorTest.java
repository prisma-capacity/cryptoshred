package eu.prismacapacity.cryptoshred;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CryptoInitializationVectorTest {
	@Test
	void testNullContracts() throws Exception {
		assertThrows(NullPointerException.class, () -> {
			CryptoInitializationVector.of(null);
		});

		CryptoInitializationVector.of("hey");
	}

	@Test
	void testExtensionTo16Byte() throws Exception {
		assertEquals(16, CryptoInitializationVector.of("hey").getBytes().length);
	}

	@Test
	void testTruncationTo16Byte() throws Exception {
		assertEquals(16, CryptoInitializationVector.of("MhmmmmmmmmmThisIsATastyBurger").getBytes().length);
	}

}
