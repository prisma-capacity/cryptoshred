package eu.prismacapacity.cryptoshred.keys;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CryptoKeySize {

	public static final CryptoKeySize BIT_256 = of(256);

	private final int keySize;

	public static CryptoKeySize of(int i) {
		if (i < 1)
			throw new IllegalArgumentException("keylength is out of range");
		return new CryptoKeySize(i);
	}

	public int asInt() {
		return keySize;
	}
}
