package ojdev.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
		
		if(this.pathToDirectory.toFile().exists() == false) {
			throw new IllegalArgumentException("Path MUST exist");
		} else if(this.pathToDirectory.toFile().isDirectory() == false) {
			throw new IllegalArgumentException("Path must point to a Directory");
		}
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
		checkFileName(warriorFileName);
		return pathToDirectory.resolve(warriorFileName).toFile();
	}
	
	private void checkFileName(String warriorFileName) {
		if(warriorFileName.matches("^[\\w.]+$") == false) {
			throw new IllegalArgumentException("Invalid File Name of: " + warriorFileName);
		}
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

	public WarriorBase loadWarrior(String warriorFileName, Map<String, String> params) throws IOException, ClassNotFoundException, InvocationTargetException, UnusableWeaponException {
		WarriorBase warrior;
		try (
				FileInputStream fis = 
					new FileInputStream(getFileForWarrior(warriorFileName));
				ByteArrayInputStream sReader = paramsToInputStream(params);
				SequenceInputStream sequenceInputStream = new SequenceInputStream(fis, sReader);				
		){
			
			warrior = WarriorBase.readFromOutputStream(sequenceInputStream);
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
	
	protected ByteArrayInputStream paramsToInputStream(Map<String, String> params) {
		StringWriter stringWriter = new StringWriter();
		Formatter formatter = new Formatter(stringWriter);
		
		for(Entry<String, String> entry : params.entrySet()) {
			WarriorBase.writeValueToOutputStream(formatter, entry.getKey(), entry.getValue());
		}
		formatter.flush();
		
		return new ByteArrayInputStream(stringWriter.getBuffer().toString().getBytes());
	}
}
