package eu.prismacapacity.cryptoshred.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CryptoInitializationVectorTest {
  @Test
  void testNullContracts() {
    assertThrows(NullPointerException.class, () -> CryptoInitializationVector.of(null));

    CryptoInitializationVector.of("hey");
  }

  @Test
  void testExtensionTo16Byte() {
    assertEquals(16, CryptoInitializationVector.of("hey").getBytes().length);
  }

  @Test
  void testTruncationTo16Byte() {
    assertEquals(
        16, CryptoInitializationVector.of("MhmmmmmmmmmThisIsATastyBurger").getBytes().length);
  }
}
