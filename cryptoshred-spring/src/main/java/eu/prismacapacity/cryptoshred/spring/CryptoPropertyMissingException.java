package eu.prismacapacity.cryptoshred.spring;

public class CryptoPropertyMissingException extends IllegalArgumentException {

	public CryptoPropertyMissingException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
