package eu.prismacapacity.cryptoshred;

import java.util.UUID;

import lombok.Value;

@Value(staticConstructor = "of")
public class CryptoSubjectId {
	final UUID id;
}
