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

import lombok.*;

import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RequiredArgsConstructor
@SuppressWarnings("java:S3329")
public final class CryptoInitializationVector {

    @Getter
  private final byte[] bytes;

  public static CryptoInitializationVector of(@NonNull String initVector) {
    return new CryptoInitializationVector(toBytes(initVector));
  }

  static byte[] toBytes(@NonNull String initVector) {
    // make sure, we have 16 bytes there
    StringBuilder sb = new StringBuilder(initVector);
    while (sb.length() < 16) sb.append(initVector);

    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    // take the first 16 bytes
    return Arrays.copyOf(bytes, 16);
  }

  public IvParameterSpec getIvParameterSpec() {
    return new IvParameterSpec(bytes);
  }
}
