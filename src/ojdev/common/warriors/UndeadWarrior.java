package ojdev.common.warriors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ojdev.common.Armory;
import ojdev.common.actions.Action;
import ojdev.common.actions.ActionDirection;
import ojdev.common.weapons.Weapon;
import ojdev.common.weapons.WeaponDamageType;

public class UndeadWarrior extends WarriorBase {

	private static final long serialVersionUID = 7714431789378509884L;

	public static final String TYPE_NAME = "Skeleton Warrior";
	
	public static final String FILE_EXTENSION = "wdat";
	
	public static final List<Weapon> USEABLE_WEAPONS;
	
	static {
		List<Weapon> tempUsableWeapons = new ArrayList<Weapon>();
		
		tempUsableWeapons.add(Armory.NO_WEAPON);
		tempUsableWeapons.add(Armory.BONE_CLUB);
		
		USEABLE_WEAPONS = Collections.unmodifiableList(tempUsableWeapons);
	}

	public UndeadWarrior(String name, String originLocation, String description, int health, Weapon equippedWeapon) throws UnusableWeaponException {
		super(name, originLocation, description, health, equippedWeapon);
	}
	
	public UndeadWarrior(String name, String originLocation, String description, int health) throws UnusableWeaponException {
		super(name, originLocation, description, health);
	}
	
	public UndeadWarrior(Map<String, String> values) throws UnusableWeaponException {
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

	@Override
	public int getDamageDone(Weapon attackersWeapon, Action attackersAction, Action ourAction) {
		int base = super.getDamageDone(attackersWeapon, attackersAction, ourAction);
		
		int result = base;
		
		if(base <= 0)
			return base;
		
		WeaponDamageType weaponDamageType = attackersWeapon.getDamageTypeForAction(attackersAction.getDamageType());
		
		// The skull is quite strong, and isn't of an importance to such a thing as this.
		if(attackersAction.getDirection() == ActionDirection.High) {
			base -= 5;
		}

		// It's very difficult to do anything effective by poking a skeleton
		if(weaponDamageType == WeaponDamageType.PIERCING) {
			 result = base / 6;
			 
		// Blunt weapons on the other hand are very effective at pulverizing large amounts of bone
		} else if(weaponDamageType == WeaponDamageType.BLUNT) {
			result = (int) (base * 1.5);
			
			if (result / 4 > getHealth()) {
				result = base * 2;
				
				if(result+5 > getHealth()) {
					result = getHealth();
				}
			}
		}
		
		if(base > 12 && result < 1) {
			result = 1;
		} else if(result < 0) {
			result = 0;
		}
		
		return result;
	}
}
