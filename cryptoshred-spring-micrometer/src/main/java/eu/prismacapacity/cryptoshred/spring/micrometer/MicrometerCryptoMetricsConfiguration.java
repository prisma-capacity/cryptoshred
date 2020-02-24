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
package eu.prismacapacity.cryptoshred.spring.micrometer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.prismacapacity.cryptoshred.micrometer.MicrometerCryptoMetrics;

@Configuration
public class MicrometerCryptoMetricsConfiguration {
	@Bean
	public MicrometerCryptoMetrics micrometerCryptoMetrics() {
		return new MicrometerCryptoMetrics();
	}
}
