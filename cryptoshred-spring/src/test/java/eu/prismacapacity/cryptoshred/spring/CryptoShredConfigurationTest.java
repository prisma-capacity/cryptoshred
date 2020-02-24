package eu.prismacapacity.cryptoshred.spring;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = { CryptoShredConfiguration.class, TestConfig.class })
public class CryptoShredConfigurationTest {

	@Test
	void testSuccessfulStartup() {
		// "container startup has worked"
		assertTrue(true);
	}

}
