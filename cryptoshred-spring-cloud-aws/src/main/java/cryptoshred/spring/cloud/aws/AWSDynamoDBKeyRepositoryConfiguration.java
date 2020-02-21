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
package cryptoshred.spring.cloud.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import eu.prismacapacity.cryptoshred.cloud.aws.DynamoDBCryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.CryptoEngine;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;

@Configuration
public class AWSDynamoDBKeyRepositoryConfiguration {
	@Bean
	public CryptoKeyRepository cryptoKeyRepository(CryptoEngine engine, AmazonDynamoDB dynamoDB,

			@Value("${cryptoshred.cloud.aws.dynamo.tablename:#{null}}") String tableName) {

		if (tableName == null)
			throw new IllegalArgumentException("Property 'cryptoshred.cloud.aws.dynamo.tablename' is required.");

		return new DynamoDBCryptoKeyRepository(engine, dynamoDB, tableName);
	}
}
