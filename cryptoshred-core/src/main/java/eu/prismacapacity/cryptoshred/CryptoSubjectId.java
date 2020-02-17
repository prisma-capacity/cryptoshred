package eu.prismacapacity.cryptoshred;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value(staticConstructor = "of")
public class CryptoSubjectId {
	final UUID subjectId;

	@JsonCreator
	protected CryptoSubjectId(@JsonProperty("subjectId") UUID subjectId) {
		this.subjectId = subjectId;
	}
}
