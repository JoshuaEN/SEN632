package ojdev.common.warriors;

import java.util.List;

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