package eu.prismacapacity.cryptoshred.keys;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value(staticConstructor = "of")
public class CryptoKeySize {
	@JsonCreator
	protected CryptoKeySize(@JsonProperty("keySize") int keySize) {
		this.keySize = keySize;
	}

	public static final CryptoKeySize BIT_256 = of(256);
	int keySize;

	@Override
	public String toString() {
		return String.valueOf(keySize);
	}

}
