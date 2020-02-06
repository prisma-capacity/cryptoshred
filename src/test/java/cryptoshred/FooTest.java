package cryptoshred;

import org.junit.jupiter.api.Test;

public class FooTest {

	@Test
	public void testCovered() throws Exception {
		new Foo().covered();
	}

	@Test
	public void testUncovered() throws Exception {
		new Foo().uncovered();
	}
breakTheBuild
}
