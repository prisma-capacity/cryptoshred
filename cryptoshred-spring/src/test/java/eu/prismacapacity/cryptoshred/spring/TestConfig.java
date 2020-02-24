package eu.prismacapacity.cryptoshred.spring;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;

@Configuration
public class TestConfig {

	static {
		System.setProperty("cryptoshred.initVector", "1");
	}

	@Bean
	public CryptoKeyRepository repo() {
		return Mockito.mock(CryptoKeyRepository.class);
	}

}
