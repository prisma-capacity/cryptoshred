package eu.prismacapacity.cryptoshred.keys;

import lombok.NonNull;

public class CryptoKeyNotFoundAfterCreatingException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	public CryptoKeyNotFoundAfterCreatingException(@NonNull Exception e) {
		super(e);
	}

}
