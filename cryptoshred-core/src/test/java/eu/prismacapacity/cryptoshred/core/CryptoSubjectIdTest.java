/*
 * Copyright © 2020 PRISMA European Capacity Platform GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
