package eu.prismacapacity.cryptoshred.core.keys;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class CryptoKeyNotFoundAfterCreatingExceptionTest {

	@Test
	public void testCryptoKeyNotFoundAfterCreatingExceptionException() throws Exception {

		Exception io = new IOException();
		assertSame(io, new CryptoKeyNotFoundAfterCreatingException(io).getCause());
	}

	@Test
	public void testCryptoKeyNotFoundAfterCreatingExceptionString() throws Exception {
		String io = "io";
		assertSame(io, new CryptoKeyNotFoundAfterCreatingException(io).getMessage());
	}

}
