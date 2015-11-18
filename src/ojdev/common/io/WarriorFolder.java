package ojdev.common.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ojdev.common.warriors.WarriorBase;
import ojdev.common.warriors.WarriorBase.UnusableWeaponException;

/**
 * Simple helper class for loading, saving, and listing Warriors.
 */
public class WarriorFolder {
	private final Path pathToDirectory;
	
	public WarriorFolder(Path pathToDirectory) {
		this.pathToDirectory = pathToDirectory;
	}
	
	public List<File> getListOfWarriorFiles(Set<String> fileExtensions) {
		return Arrays.asList(pathToDirectory.toFile().listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if(pathname.isFile() == false) {
					return false;
				}
				
				String name = pathname.getName().toLowerCase();
				int extentionMarker = name.lastIndexOf('.');			
				String extention = name.substring(extentionMarker+1);
				
				return fileExtensions.contains(extention);
			}
		}));
	}
	
	public void deleteWarrior(WarriorBase warrior) throws IOException {
		deleteWarrior(warrior.getFileName());
	}
	
	public void deleteWarrior(String warriorFileName) throws IOException {
		Files.delete(getFileForWarrior(warriorFileName).toPath());
	}
	
	public File getFileForWarrior(WarriorBase warrior) {
		return getFileForWarrior(warrior.getFileName());
	}
	
	public File getFileForWarrior(String warriorFileName) {
		return pathToDirectory.resolve(warriorFileName).toFile();
	}
	
	public WarriorBase loadWarrior(String warriorFileName) throws IOException, ClassNotFoundException, InvocationTargetException, UnusableWeaponException {
		WarriorBase warrior;
		try (
				FileInputStream fis = 
					new FileInputStream(getFileForWarrior(warriorFileName));
		){
			warrior = WarriorBase.readFromOutputStream(fis);
		}
		
		return warrior;
	}
	
	public void saveWarrior(WarriorBase warrior) throws IOException {
		
		try (
			FileOutputStream fos = new FileOutputStream(getFileForWarrior(warrior), false);
		){
			warrior.writeToOutputStream(fos);
		}
	}
}
