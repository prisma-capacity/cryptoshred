package eu.prismacapacity.cryptoshred.keys;

import lombok.Value;

@Value(staticConstructor = "of")
public class CryptoKeySize {

	public static final CryptoKeySize BIT_256 = of(256);

	int keySize;

	@Override
	public String toString() {
		return String.valueOf(keySize);
	}

}
