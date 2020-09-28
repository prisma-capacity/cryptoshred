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
package eu.prismacapacity.cryptoshred.core.keys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CryptoKeyTest {
	@Test
	void testNullContracts() {
		assertThrows(NullPointerException.class, () -> CryptoKey.fromBase64(null));
		assertThrows(NullPointerException.class, () -> CryptoKey.fromBytes(null));
	}

	@Test
	void testSymetry() {
		CryptoKey key1 = CryptoKey.fromBytes("Foo".getBytes());
		CryptoKey key2 = CryptoKey.fromBase64(key1.getBase64());

		assertArrayEquals(key1.getBytes(), key2.getBytes());
		assertEquals(key1.getBase64(), key2.getBase64());
		assertEquals(key1, key2);
	}

	@Test
	void testToString() {
		CryptoKey key1 = CryptoKey.fromBytes("Foo".getBytes());

		assertEquals(key1.getBase64(), key1.toString());
	}
}
