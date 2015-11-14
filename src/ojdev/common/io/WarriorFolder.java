package ojdev.common.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ojdev.common.warriors.WarriorBase;

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
	
	public WarriorBase loadWarrior(String warriorFileName) throws IOException, ClassNotFoundException {
		LookAheadObjectInputStream ois = null;
		WarriorBase warrior;
		try {
			ois = new LookAheadObjectInputStream(new FileInputStream(getFileForWarrior(warriorFileName)));
			warrior = (WarriorBase)ois.readObject();
		} finally {
			if(ois != null) {
				ois.close();
			}
		}
		
		return warrior;
	}
	
	public void saveWarrior(WarriorBase warrior) throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(getFileForWarrior(warrior), false));
			oos.writeObject(warrior);
		} finally {	
			if(oos != null) {
				oos.close();
			}
		}
	}
}
