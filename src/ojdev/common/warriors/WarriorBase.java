package ojdev.common.warriors;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import ojdev.common.Armory;
import ojdev.common.weapons.Weapon;

/**
 * Base Abstract class for Warrior Types
 */
public abstract class WarriorBase implements java.io.Serializable {

	private static final long serialVersionUID = 2513148787247654417L;

	private final String name;

	private final String originLocation;

	private final String description;

	private int health;

	private Weapon equippedWeapon;
	
	public WarriorBase(String name, String originLocation, String description, int health) throws UnusableWeaponException {
		this(name, originLocation, description, health, Armory.NO_WEAPON);
	}
	
	public WarriorBase(String name, String originLocation, String description, int health, Weapon equippedWeapon) throws UnusableWeaponException {
		this.name = name;
		this.originLocation = originLocation;
		this.description = description;
		this.setHealth(health);
		this.setEquippedWeapon(equippedWeapon);
	}
	
	public WarriorBase(Map<String, String> values) throws UnusableWeaponException {
		this(
			parseName(values),
			parseOriginLocation(values),
			parseDescription(values),
			parseHealth(values),
			parseEquippedWeapon(values)
		);
	}

	public String getName() {
		return name;
	}

	public String getOriginLocation() {
		return originLocation;
	}

	public String getDescription() {
		return description;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		setHealth(health, false);
	}
	
	/**
	 * Sets the Warrior's health
	 * @param health the health of the warrior
	 * @param autoAdjust determines if illegal health values (h < 0 or h > 100) should be automatically adjusted to be legal
	 */
	public void setHealth(int health, boolean autoAdjust) {
		if(health > 100) {
			if(autoAdjust) {
				health = 100;
			} else {
				throw new IllegalArgumentException("Health can't be over 100");
			}
		} else if(health < 0) {
			if(autoAdjust) {
				health = 0;
			} else {
				throw new IllegalArgumentException("Health can't lower than 0");
			}
		}
		this.health = health;
	}
	
	public Weapon getEquippedWeapon() {
		return equippedWeapon;
	}

	public void setEquippedWeapon(Weapon weapon) throws UnusableWeaponException {
		if(weapon == null) {
			throw new IllegalArgumentException("Weapon can't be null");
		}
		
		if(canUseWeapon(weapon) != true) {
			throw new UnusableWeaponException();
		}
		
		this.equippedWeapon = weapon;
	}

	public String getFileName() {
		return String.format("%s.%s", name, getFileExtension());
	}
	
	public boolean canUseWeapon(Weapon weapon) {
		return Armory.NO_WEAPON == weapon || getUsableWeapons().contains(weapon);
	}
	
	public boolean hasNoWeaponEquipped() {
		return Armory.NO_WEAPON == getEquippedWeapon();
	}

	public abstract String getFileExtension();
	
	public abstract List<Weapon> getUsableWeapons();
	
	public boolean isDead() {
		return !isAlive();
	}
	
	public boolean isAlive() {
		return health > 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((originLocation == null) ? 0 : originLocation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof WarriorBase)) {
			return false;
		}
		WarriorBase other = (WarriorBase) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (originLocation == null) {
			if (other.originLocation != null) {
				return false;
			}
		} else if (!originLocation.equals(other.originLocation)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"WarriorBase[getName()=%s, getOriginLocation()=%s, getDescription()=%s, getHealth()=%s, getEquippedWeapon()=%s]",
				getName(), getOriginLocation(), getDescription(), getHealth(), getEquippedWeapon());
	}
	
	public void writeToOutputStream(OutputStream outputStream) {
		writeToOutputStream(new Formatter(outputStream));
	}
	
	protected void writeToOutputStream(Formatter writer) {
		writeValueToOutputStream(writer, "type", getClass().getName());
		writeValueToOutputStream(writer, "name", getName());
		writeValueToOutputStream(writer, "origin", getOriginLocation());
		writeValueToOutputStream(writer, "description", getDescription());
		writeValueToOutputStream(writer, "health", getHealth());
		writeValueToOutputStream(writer, "weapon", getEquippedWeapon().getName());
		
		// We have to flush here because the formatter is never closed
		// (but the underlying OutputStream is)
		writer.flush();
	}
	
	protected final void writeValueToOutputStream(Formatter writer, String key, Object value) {
		writer.format(" %s=%s%n", key, value);
	}
	
	@SuppressWarnings("unchecked")
	public final static WarriorBase readFromOutputStream(InputStream inputStream) 
			throws 
				ClassNotFoundException, 
				UnusableWeaponException,
				InvocationTargetException
		{
		Scanner reader = new Scanner(inputStream);
		
		Map<String, String> input = new HashMap<String, String>();
		
		while(reader.hasNext()) {
			String next = reader.nextLine();
			
			// Ignore blank lines and comment lines
			if(next.startsWith("#") || next.trim().isEmpty()) {
				continue;
			}
			
			int splitIdx = next.indexOf('=');
			
			if(splitIdx < 0) {
				throw new IllegalArgumentException("Invalid Input Line of: " + next);
			}
			
			String name = next.substring(0, splitIdx).trim();
			String value = next.substring(splitIdx+1).trim();
			input.put(name, value);
		}
		
		if(input.containsKey("type") == false) {
			throw new IllegalArgumentException("type must be given");
		}
		
		Class<?> cls = Class.forName(input.get("type"));
		
		if(!WarriorBase.class.isAssignableFrom(cls)) {
			throw new IllegalArgumentException("Invalid type of: " + input.get("type"));
		}
		try {
			return ((Class<WarriorBase>)cls).getDeclaredConstructor(Map.class).newInstance(input);
		} catch(
				NoSuchMethodException | 
				InstantiationException |
                IllegalAccessException |
                IllegalArgumentException e) 
		{
			throw new UnsupportedOperationException("Type does not support loading from text file", e);
		}
	}

	/*
	 * Parsers for converting from Text. If Java didn't require the call to the
	 * super constructor or another constructor of the class to
	 * be the absolute first thing in the a constructor this
	 * could be packed neatly into the constructor.
	 */
	
	private static String parseName(Map<String, String> values) {
		String tmpName;
		if(values.containsKey("name")) {
			tmpName = values.get("name");
		} else {
			throw new IllegalArgumentException("name must be given");
		}
		return tmpName;
	}
	
	private static String parseOriginLocation(Map<String, String> values) {
		String tmpOriginLocation;
		if(values.containsKey("origin")) {
			tmpOriginLocation = values.get("origin");
		} else {
			throw new IllegalArgumentException("origin must be given");
		}
		return tmpOriginLocation;
	}
	
	private static String parseDescription(Map<String, String> values) {
		String tmpDescription;
		if(values.containsKey("description")) {
			tmpDescription = values.get("description");
		} else {
			throw new IllegalArgumentException("description must be given");
		}
		return tmpDescription;
	}
	
	private static int parseHealth(Map<String, String> values) {
		int tmpHealth;
		if(values.containsKey("health")) {
		try {
			tmpHealth = Integer.parseInt(values.get("health"));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid health value of: " + values.get("health"), e);
			}
		} else {
			throw new IllegalArgumentException("health must be given");
		}
		return tmpHealth;
	}

	private static Weapon parseEquippedWeapon(Map<String, String> values) {
		Weapon tmpEquippedWeapon;
		if(values.containsKey("weapon")) {
			tmpEquippedWeapon = Armory.getWeaponFromName(values.get("weapon"));

			if(tmpEquippedWeapon == null) {
				throw new IllegalArgumentException("Invalid Weapon: " + values.get("weapon"));
			}
		} else {
			throw new IllegalArgumentException("weapon must be given");
		}	
		return tmpEquippedWeapon;
	}
	
	/**
	 * Indicates a weapon cannot be used by a Warrior.
	 */
	public class UnusableWeaponException extends Exception {

		private static final long serialVersionUID = 4585041860617232209L;
		
		public UnusableWeaponException(String message) {
			super(message);
		}
		
		public UnusableWeaponException() {
			this("Weapon cannot be used by this Warrior");
		}

	}

}