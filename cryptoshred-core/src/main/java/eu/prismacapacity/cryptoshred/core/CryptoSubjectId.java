package eu.prismacapacity.cryptoshred.core;

import java.util.UUID;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class CryptoSubjectId {
    @NonNull
	final UUID id;
}
