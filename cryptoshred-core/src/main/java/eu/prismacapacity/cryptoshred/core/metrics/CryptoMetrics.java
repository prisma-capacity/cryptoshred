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
package eu.prismacapacity.cryptoshred.core.metrics;

import lombok.NonNull;

public interface CryptoMetrics {

	void notifyMissingKey();

	void notifyDecryptionSuccess();

	void notifyDecryptionFailure(@NonNull Exception e);

	static abstract class Base implements CryptoMetrics {

		@Override
		public void notifyMissingKey() {
			// intentionally empty
		}

		@Override
		public void notifyDecryptionSuccess() {
			// intentionally empty
		}

		@Override
		public void notifyDecryptionFailure(@NonNull Exception e) {
			// intentionally empty
		}
	}

	final static class NOP extends Base {
	}

}
