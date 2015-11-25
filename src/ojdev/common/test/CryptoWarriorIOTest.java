package ojdev.common.test;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ojdev.client.WarriorFolder;
import ojdev.common.warriors.CryptoWarrior;
import ojdev.common.warriors.WarriorBase;

public class CryptoWarriorIOTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSavingAndLoading() throws Exception {
		WarriorFolder folder = new WarriorFolder(Paths.get("."));
		
		WarriorBase savedWarrior = new CryptoWarrior("CryptoTest", "Someplace", "Somewhere", 50, "CRYPTOTEST");
		
		folder.saveWarrior(savedWarrior);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("unlockKey", "CRYPTOTEST");
		
		WarriorBase loadedWarrior = folder.loadWarrior(savedWarrior.getFileName(), map);
		
		assertEquals("Saved warrior should match loaded warrior", savedWarrior, loadedWarrior);
	}

}
