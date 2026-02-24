/*
 * Copyright © 2026 PRISMA European Capacity Platform GmbH 
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.Value;

public class Benchmark {
  static ObjectMapper om;
  static InMemCryptoKeyRepository keyRepository;

  static {
    CryptoEngine engine = new JDKCryptoEngine("mysecret",false);
    keyRepository = new InMemCryptoKeyRepository(engine);
    om = new ObjectMapper();
    om.registerModule(new CryptoModule(engine, keyRepository));

    try {
      om.writeValueAsString(
          new Dto(new CryptoContainer<>("init", CryptoSubjectId.of(UUID.randomUUID()))));

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  public static void main(String[] args) {
    int dataSize = 100_000;
    System.out.println("Preparing " + dataSize + " data points for encryption...");
    Dto[] unencrypted = generateTestData(dataSize);

    System.out.println("now encrypting...");
    encrypt(dataSize, unencrypted);
  }

  private static void encrypt(int dataSize, Dto[] unencrypted) throws JsonProcessingException {

    long start = System.currentTimeMillis();
    long[][] individualTimes = new long[3][dataSize];
    int idx = 0;
    int lenMultiplier = 0;

    int capacity = 50_000_000;
    StringBuffer sb = new StringBuffer(capacity);

    for (Dto dto : unencrypted) {
      long startIndividual = System.currentTimeMillis();
      sb.append(om.writeValueAsString(dto));
      long endIndividual = System.currentTimeMillis();

      if (capacity - sb.length() < 200_000) {
        sb.delete(0, sb.length());
      }

      individualTimes[lenMultiplier++][idx] = endIndividual - startIndividual;

      if (lenMultiplier == 3) {
        lenMultiplier = 0;
      }
    }

    long end = System.currentTimeMillis();
    System.out.println(
        "Encryption took " + (end - start) + "ms or " + ((end - start) / 1_000) + "s");
    System.out.println();

    for (lenMultiplier = 0; lenMultiplier < 3; lenMultiplier++) {
      long durations = 0;
      long[] individualTime = individualTimes[lenMultiplier];
      for (int i = 0; i < dataSize; i++) {
        durations += individualTime[i];
      }
      String caption = getCaption(lenMultiplier);
      int average = (int) (durations / dataSize);
      System.out.println(caption + ": " + average + "ms");
    }

    // hope that compiler does not optimize too much away if we do this
    idx = new Random().nextInt(sb.length() - 1);
    System.out.println(sb.substring(idx, idx + 1));
  }

  private static String getCaption(int lenMultiplier) {
    if (lenMultiplier == 0) {
      return "small data size";
    }
    if (lenMultiplier == 1) {
      return "medium data size";
    }

    return "large data size";
  }

  private static Dto[] generateTestData(int dataSize) {
    Dto[] dtos = new Dto[dataSize];

    int lenMultiplier = 1;
    for (int i = 0; i < dataSize; i++) {
      CryptoSubjectId id = CryptoSubjectId.of(UUID.randomUUID());
      String data = generateString((int) (100 * Math.pow(10, lenMultiplier++)));
      Dto dto = new Dto(new CryptoContainer<>(data, id));
      dtos[i] = dto;

      if (lenMultiplier == 4) {
        lenMultiplier = 1;
      }
    }

    return dtos;
  }

  private static String generateString(int targetStringLength) {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    Random random = new Random();

    return random
        .ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  @Value
  static class Dto {
    CryptoContainer<String> crypto;
  }
}
