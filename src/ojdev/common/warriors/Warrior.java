package ojdev.common.warriors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ojdev.common.Armory;
import ojdev.common.weapons.Weapon;

public class Warrior extends WarriorBase {

	private static final long serialVersionUID = 1814946151379871816L;
	
	public static final String TYPE_NAME = "Warrior";
	
	public static final String FILE_EXTENSION = "wdat";
	
	public static final List<Weapon> USEABLE_WEAPONS;
	
	static {
		List<Weapon> tempUsableWeapons = new ArrayList<Weapon>();
		
		tempUsableWeapons.add(Armory.NO_WEAPON);
		tempUsableWeapons.add(Armory.GREAT_SWORD);
		tempUsableWeapons.add(Armory.SWORD);
		tempUsableWeapons.add(Armory.SPEAR);
		tempUsableWeapons.add(Armory.STAFF);
		
		USEABLE_WEAPONS = Collections.unmodifiableList(tempUsableWeapons);
	}

	public Warrior(String name, String originLocation, String description, int health, Weapon equippedWeapon) throws UnusableWeaponException {
		super(name, originLocation, description, health, equippedWeapon);
	}
	
	public Warrior(String name, String originLocation, String description, int health) throws UnusableWeaponException {
		super(name, originLocation, description, health);
	}
	
	public Warrior(Map<String, String> values) throws UnusableWeaponException {
		super(values);
	}
	
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	@Override
	public String getFileExtension() {
		return FILE_EXTENSION;
	}

	@Override
	public List<Weapon> getUsableWeapons() {
		return USEABLE_WEAPONS;
	}

}
