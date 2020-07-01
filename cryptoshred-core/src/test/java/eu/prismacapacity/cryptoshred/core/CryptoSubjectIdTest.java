package eu.prismacapacity.cryptoshred.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class CryptoSubjectIdTest {
	@Test
	void testNullContracts() throws Exception {
		assertThrows(NullPointerException.class, () -> CryptoSubjectId.of(null));

		CryptoSubjectId.of(UUID.randomUUID());
	}
}
