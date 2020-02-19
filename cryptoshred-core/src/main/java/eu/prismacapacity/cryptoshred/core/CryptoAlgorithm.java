package eu.prismacapacity.cryptoshred.core;

import lombok.NonNull;
import lombok.Value;

// not an enum for a reason...
@Value(staticConstructor = "of")
public class CryptoAlgorithm {
	public static final CryptoAlgorithm AES_CBC = of("AES");
	@NonNull
	final String id;
}
