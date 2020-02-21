package eu.prismacapacity.cryptoshred.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoEngine;
import eu.prismacapacity.cryptoshred.core.CryptoInitializationVector;
import eu.prismacapacity.cryptoshred.core.CryptoObjectMapper;
import eu.prismacapacity.cryptoshred.core.JDKCryptoEngine;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import lombok.NonNull;

@Configuration
public class CryptoShredConfiguration {

	@Bean
	public CryptoObjectMapper cryptoObjectMapper(

			@Value("${cryptoshred.defaults.algorithm:AES}") String algo,

			@Value("${cryptoshred.defaults.keySize:256}") int size,

			@Value("${cryptoshred.initVector:#{null}}") String initVector,

			@NonNull CryptoKeyRepository repository,

			@Autowired(required = false) CryptoEngine engine,

			@Autowired(required = false) ObjectMapper om

	) {

		if (engine == null) {
			// then we'll need an initVector
			if (initVector == null || initVector.length() < 1)
				throw new CryptoPropertyMissingException(
						"cryptoshred.initVector is required and must be a non-empty String");

			engine = new JDKCryptoEngine(CryptoInitializationVector.of(initVector));
		}

		CryptoObjectMapper.Builder b = CryptoObjectMapper.builder(repository, engine);
		if (om != null)
			b.mapper(om);

		if (size < 128) {
			throw new IllegalArgumentException("keySize is out or range: " + size);
		}

		b.defaultAlgo(CryptoAlgorithm.of(algo));
		b.defaultKeySize(CryptoKeySize.of(size));

		return b.build();

	}

}
