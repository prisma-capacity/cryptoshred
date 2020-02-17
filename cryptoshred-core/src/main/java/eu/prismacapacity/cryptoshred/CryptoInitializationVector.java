package eu.prismacapacity.cryptoshred;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class CryptoInitializationVector {
	@NonNull
	String initVector;

	public byte[] getBytes() {
		try {

			return Arrays.copyOf(initVector.getBytes("UTF-8"), 16);

		} catch (UnsupportedEncodingException e) {
			// must not happen
			throw new IllegalStateException("UTF-8 not a valid charset!?");
		}
	}
}
