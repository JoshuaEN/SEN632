package ojdev.server.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ojdev.server.Moderator;

public class ModeratorTest {

	private Moderator moderator;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		moderator = new Moderator(20, ServerTestConstant.DEFAULT_PORT, 20);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testValidStart() {
		
	}

}
