package ojdev.common.warriors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ojdev.common.Armory;
import ojdev.common.weapons.Weapon;

public class Warrior extends WarriorBase {

	private static final long serialVersionUID = 1814946151379871816L;
	
	private static final List<Weapon> USEABLE_WEAPONS;
	
	static {
		List<Weapon> tempUsableWeapons = new ArrayList<Weapon>();
		
		tempUsableWeapons.add(Armory.GREAT_SWORD);
		
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
	public String getFileExtension() {
		return "wdat";
	}

	@Override
	public List<Weapon> getUsableWeapons() {
		return USEABLE_WEAPONS;
	}

}
