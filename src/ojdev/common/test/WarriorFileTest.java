package ojdev.common.test;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ojdev.common.Armory;
import ojdev.common.io.WarriorFolder;
import ojdev.common.warriors.Warrior;
import ojdev.common.warriors.WarriorBase;

public class WarriorFileTest {

	private static final Path testPath = Paths.get(".", "test", "output", "WarriorFileTest");
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testPath.toFile().mkdirs();
	}
	
	@Test
	public void testLoadAndSave() throws Exception {
		
		Warrior warrior = new Warrior("Bigfoot", "Someplace", "???", 95, Armory.GREAT_SWORD);
		
		WarriorFolder warriorFolder = new WarriorFolder(testPath);

		warriorFolder.saveWarrior(warrior);
		
		warriorFolder = new WarriorFolder(testPath);
		
		WarriorBase loadedWarrior = warriorFolder.loadWarrior(warrior.getFileName());
		
		assertEquals("Original Warrior should Equal loaded warrior", warrior, loadedWarrior);
	}
	
	@Test
	public void testListing() throws Exception {
		Warrior warrior = new Warrior("Bigfoot", "Someplace", "???", 95, Armory.GREAT_SWORD);
		
		Path localTestPath = testPath.resolve("testListing");
		
		localTestPath.toFile().mkdirs();
		
		WarriorFolder warriorFolder = new WarriorFolder(localTestPath);

		warriorFolder.saveWarrior(warrior);
		
		warriorFolder = new WarriorFolder(testPath);
		
		Set<String> fileExtensions = new HashSet<String>();
		fileExtensions.add(warrior.getFileExtension());
		
		List<File> warriors = warriorFolder.getListOfWarriorFiles(fileExtensions);
		
		assertEquals("There should be one warrior in the path", 1, warriors.size());
		assertEquals("The warrior's file name should match ours", warrior.getFileName(), warriors.get(0).getName());
	}

}
