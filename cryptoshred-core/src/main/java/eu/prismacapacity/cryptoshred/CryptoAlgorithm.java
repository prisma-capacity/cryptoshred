package eu.prismacapacity.cryptoshred;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

// not an enum for a reason...
@Value(staticConstructor = "of")
public class CryptoAlgorithm {
	public static final CryptoAlgorithm AES_CBC = of("AES");
	final String id;

	@JsonCreator
	protected CryptoAlgorithm(@JsonProperty("id") String id) {
		this.id = id;
	}
}
