package eu.prismacapacity.cryptoshred.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import eu.prismacapacity.cryptoshred.core.CryptoEngineException;

public class CryptoEngineExceptionTest {

	@Test
	public void testCryptoEngineException() throws Exception {
		assertThrows(NullPointerException.class, () -> {
			new CryptoEngineException(null);
		});

		new CryptoEngineException(new IOException());
	}

}
