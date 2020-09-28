package eu.prismacapacity.cryptoshred.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

public class CryptoSubjectIdTest {
  @Test
  void testNullContracts() {
    assertThrows(NullPointerException.class, () -> CryptoSubjectId.of((UUID) null));

    assertThrows(NullPointerException.class, () -> CryptoSubjectId.of(() -> null).getId());

    CryptoSubjectId.of(UUID.randomUUID());
  }

  @Test
  void testLaziness() {
    UUID[] uuids = new UUID[1];
    CryptoSubjectId subjectId = CryptoSubjectId.of(() -> uuids[0]);

    UUID uuid = UUID.randomUUID();
    uuids[0] = uuid;

    assertEquals(uuid, subjectId.getId());
  }
}
