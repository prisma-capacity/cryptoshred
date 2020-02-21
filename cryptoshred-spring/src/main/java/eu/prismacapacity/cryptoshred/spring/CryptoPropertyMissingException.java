package eu.prismacapacity.cryptoshred.spring;

import lombok.NonNull;

public class CryptoPropertyMissingException extends IllegalArgumentException {

	public CryptoPropertyMissingException(@NonNull String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
