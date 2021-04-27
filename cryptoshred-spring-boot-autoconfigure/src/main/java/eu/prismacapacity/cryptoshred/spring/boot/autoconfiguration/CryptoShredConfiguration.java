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
package eu.prismacapacity.cryptoshred.spring.boot.autoconfiguration;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.prismacapacity.cryptoshred.core.*;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;

@Configuration
public class CryptoShredConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public CryptoModule cryptoModule(
          @NonNull @Value("${cryptoshred.defaults.algorithm:AES}") String algo,
          @Value("${cryptoshred.defaults.keySize:256}") int size,
          @Value("${cryptoshred.initVector:#{null}}") String initVector, @NonNull CryptoKeyRepository repository,
          @Autowired(required = false) CryptoEngine engine, @Autowired(required = false) CryptoMetrics metrics
  ) {
    if (engine == null) {
      // then we'll need an initVector
      if (initVector == null || initVector.length() < 1) {
        throw new CryptoPropertyMissingException(
                "cryptoshred.initVector (non-empty String) is required unless you define a CryptoEngine.");
      }
      engine = new JDKCryptoEngine(CryptoInitializationVector.of(initVector));
    }

		if (metrics == null) {
			metrics = new CryptoMetrics.NOP();
		}
    return new CryptoModule(engine, repository, CryptoAlgorithm.of(algo), CryptoKeySize.of(size), metrics);
  }


  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper(CryptoModule cm){
    return new ObjectMapper().registerModule(cm);
  }

  public static class CryptoPropertyMissingException extends IllegalArgumentException {

    public CryptoPropertyMissingException(@NonNull String string) {
      super(string);
    }

    private static final long serialVersionUID = 1L;

  }
}
