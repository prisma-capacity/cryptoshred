package eu.prismacapacity.cryptoshred.core;

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
			// make sure, we have 16 bytes there
			StringBuffer sb = new StringBuffer(initVector);
			while (sb.length() < 16)
				sb.append(initVector);

			byte[] bytes = sb.toString().getBytes("UTF-8");
			// take the first 16 bytes
			return Arrays.copyOf(bytes, 16);
		} catch (UnsupportedEncodingException e) {
			// must not happen
			throw new IllegalStateException("UTF-8 not a valid charset!?");
		}
	}
}
