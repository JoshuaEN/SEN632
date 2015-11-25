package ojdev.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ojdev.common.actions.Action;
import ojdev.common.actions.ActionDamageType;
import ojdev.common.actions.ActionDirection;
import ojdev.common.actions.ActionStance;
import ojdev.common.weapons.Weapon;
import static ojdev.common.weapons.WeaponDamageType.*;

public class Armory {

	/**
	 * Available Actions
	 */
	public static final List<Action> ACTIONS;
	public static final List<Action> GENERIC_ACTIONS;
	
	public static final Action OVERHEAD_SWING = 
			new Action(
					"Overhead Strike", 
					ActionDirection.High, ActionStance.ATTACK, ActionDamageType.SWING, 
					-20, 20
			);
	public static final Action LEFT_SWING = 
			new Action(
					"Left Swing", 
					ActionDirection.Left, ActionStance.ATTACK, ActionDamageType.SWING,
					0, 0
			);
	public static final Action RIGHT_SWING = 
			new Action(
					"Right Swing", 
					ActionDirection.Right, ActionStance.ATTACK, ActionDamageType.SWING,
					0, 0
			);
	public static final Action LOW_SWING = 
			new Action(
					"Low Swing", 
					ActionDirection.Low, ActionStance.ATTACK, ActionDamageType.SWING,
					-10, -20
			);
	
	public static final Action HIGH_THRUST = 
			new Action(
					"High Thrust", 
					ActionDirection.High, ActionStance.ATTACK, ActionDamageType.THRUST,
					10, 10
				);
	public static final Action LOW_THRUST = 
			new Action(
					"Low Thrust", 
					ActionDirection.Low, ActionStance.ATTACK, ActionDamageType.THRUST,
					10, 5
			);

	public static final Action BLOCK_HIGH = 
			new Action(
					"Block High", 
					ActionDirection.High, ActionStance.DEFENSE_BLOCK, ActionDamageType.NONE,
					20
			);
	public static final Action BLOCK_LEFT = 
			new Action(
					"Block Left", 
					ActionDirection.Left, ActionStance.DEFENSE_BLOCK, ActionDamageType.NONE,
					0
			);
	public static final Action BLOCK_RIGHT = 
			new Action(
					"Block Right",
					ActionDirection.Right, ActionStance.DEFENSE_BLOCK, ActionDamageType.NONE,
					0
			);
	public static final Action BLOCK_LOW = 
			new Action(
					"Block Low", 
					ActionDirection.Low, ActionStance.DEFENSE_BLOCK, ActionDamageType.NONE,
					20
			);
	
	public static final Action PARRY_HIGH =
			new Action(
					"Parry High", 
					ActionDirection.High, ActionStance.DEFENSE_COUNTER, ActionDamageType.SWING,
					20
			);
	public static final Action PARRY_LOW =
			new Action(
					"Parry Low", 
					ActionDirection.Low, ActionStance.DEFENSE_COUNTER, ActionDamageType.SWING,
					20
			);
	public static final Action PARRY_LEFT =
			new Action(
					"Parry Left", 
					ActionDirection.Left, ActionStance.DEFENSE_COUNTER, ActionDamageType.SWING,
					20
			);
	public static final Action PARRY_RIGHT =
			new Action(
					"Parry Right", 
					ActionDirection.Right, ActionStance.DEFENSE_COUNTER, ActionDamageType.SWING,
					20
			);
	
	public static final Action RETREAT = 
			new Action(
					"Retreat", 
					"Withdraw from the Engagement", 
					ActionDirection.None, ActionStance.NONE, ActionDamageType.NONE,
					0, 0, 20, 
					true, true
			);
	
	public static final Action DEATH = 
			new Action(
					"Death", 
					"May you rest in peace", 
					ActionDirection.None, ActionStance.NONE, ActionDamageType.NONE,
					0, 0, -100, 
					true, true
			);
	
	public static final Action INVALID_TEST_ACTION = 
			new Action(
					"Invalid Test Action", 
					ActionDirection.None, ActionStance.NONE, ActionDamageType.NONE,
					-999, -999, -999
			);

	static {
		List<Action> tmpActionList = new ArrayList<Action>(); 
		
		tmpActionList.add(OVERHEAD_SWING);
		tmpActionList.add(LEFT_SWING);
		tmpActionList.add(RIGHT_SWING);
		tmpActionList.add(LOW_SWING);

		tmpActionList.add(HIGH_THRUST);
		tmpActionList.add(LOW_THRUST);

		tmpActionList.add(BLOCK_HIGH);
		tmpActionList.add(BLOCK_LEFT);
		tmpActionList.add(BLOCK_RIGHT);
		tmpActionList.add(BLOCK_LOW);
		
		tmpActionList.add(PARRY_HIGH);
		tmpActionList.add(PARRY_LEFT);
		tmpActionList.add(PARRY_RIGHT);
		tmpActionList.add(PARRY_LOW);
		
		tmpActionList.add(RETREAT);
		tmpActionList.add(DEATH);
		
		if(SharedConstant.DEBUG) {
			tmpActionList.add(INVALID_TEST_ACTION);
		}
		
		if(SharedConstant.DEBUG) {
			checkForDuplicateActions(tmpActionList);
		}
		ACTIONS = Collections.unmodifiableList(tmpActionList);
	}
	
	static {
		List<Action> tmpActionList = new ArrayList<Action>(); 
		
		tmpActionList.add(RETREAT);
		tmpActionList.add(DEATH);
		
		if(SharedConstant.DEBUG) {
			checkForDuplicateActions(tmpActionList);
		}
		GENERIC_ACTIONS = Collections.unmodifiableList(tmpActionList);
	}
	
	/**
	 * Available Weapons
	 */
	public static final List<Weapon> WEAPONS;
	
	public static final Weapon NO_WEAPON = new Weapon(
			"No Weapon", 
			"",
			0, 0, 0,
			0, 0, 0,
			NONE, NONE, NONE,
			new ArrayList<Action>(0)
	);

	public static final Weapon GREAT_SWORD = new Weapon(
			"Bidenhänder",
			"",
			
			50, 5, 30,
			5, 35, 0,
			PIERCING, CUTTING, NONE,
			
			OVERHEAD_SWING,
			LEFT_SWING,
			RIGHT_SWING,
			LOW_SWING,
			
			HIGH_THRUST,
			LOW_THRUST,
			
			BLOCK_HIGH,
			BLOCK_LEFT,
			BLOCK_RIGHT,
			BLOCK_LOW,
			
			// PARRY_HIGH intentionally missing
			PARRY_LEFT,
			PARRY_RIGHT,
			PARRY_LOW
	);
	
	public static final Weapon SWORD = new Weapon(
			"Gladius", 
			"", 
			
			20, 45, 40,
			20, 20, 0,
			PIERCING, CUTTING, NONE,
			
			HIGH_THRUST,
			LOW_THRUST,
			OVERHEAD_SWING,
			LEFT_SWING,
			RIGHT_SWING,
			LOW_SWING,
			
			BLOCK_HIGH,
			BLOCK_LEFT,
			BLOCK_RIGHT,
			BLOCK_LOW,
			
			PARRY_HIGH,
			PARRY_LEFT,
			PARRY_RIGHT,
			PARRY_LOW
	);
	
	public static final Weapon SPEAR = new Weapon(
			"Dory", 
			"", 
			
			40, 50, 10,
			40, 0, 0,
			PIERCING, NONE, NONE,
			
			HIGH_THRUST,
			LOW_THRUST,
			
			PARRY_HIGH,
			PARRY_LEFT,
			PARRY_RIGHT,
			PARRY_LOW
	);
	
	public static final Weapon STAFF = new Weapon(
			"Quarterstaff", 
			"", 
			
			20, 60, 20,
			-20, 20, 0,
			BLUNT, BLUNT, NONE,
			
			HIGH_THRUST,
			LOW_THRUST,
			OVERHEAD_SWING,
			LEFT_SWING,
			RIGHT_SWING,
			LOW_SWING,
			
			PARRY_HIGH,
			PARRY_LEFT,
			PARRY_RIGHT,
			PARRY_LOW
	);
	
	public static final Weapon HALBERD = new Weapon(
			"Hellebarde",
			"",
			
			50, 10, 30,
			10, 40, 0,
			PIERCING, CUTTING, NONE,
			
			OVERHEAD_SWING,
			LEFT_SWING,
			RIGHT_SWING,
			LOW_SWING,
			
			HIGH_THRUST,
			LOW_THRUST,
			
			BLOCK_HIGH,
			BLOCK_LEFT,
			BLOCK_RIGHT,
			BLOCK_LOW,
			
			PARRY_HIGH,
			PARRY_LEFT,
			PARRY_RIGHT,
			PARRY_LOW
	);
	
	public static final Weapon BONE_CLUB =  new Weapon(
			"Bone Club", 
			"A Club made from the bones of the warrior who wields it.", 
			20, 60, 10,
			0, 20, 0,
			BLUNT, BLUNT, NONE,
			OVERHEAD_SWING,
			LEFT_SWING,
			RIGHT_SWING,
			LOW_SWING,
			
			BLOCK_HIGH,
			BLOCK_LEFT,
			BLOCK_RIGHT,
			BLOCK_LOW
	);
	
	static {
		List<Weapon> tmpWeaponList = new ArrayList<Weapon>();
		
		tmpWeaponList.add(NO_WEAPON);
		
		tmpWeaponList.add(GREAT_SWORD);
		tmpWeaponList.add(SWORD);
		tmpWeaponList.add(SPEAR);
		tmpWeaponList.add(STAFF);
		
		tmpWeaponList.add(HALBERD);
		
		tmpWeaponList.add(BONE_CLUB);
		
		if(SharedConstant.DEBUG) {
			checkForDuplicateWeapons(tmpWeaponList);
		}
		WEAPONS = Collections.unmodifiableList(tmpWeaponList);
	}

	public static Weapon getWeaponFromName(String name) {
		for(Weapon weapon : WEAPONS) {
			if(weapon.getName().equals(name)) {
				return weapon;
			}
		}

		return null;
	}
	
	public static Action getActionFromName(String name) {
		for(Action action : ACTIONS) {
			if(action.getName().equals(name)) {
				return action;
			}
		}

		return null;
	}
	
	private static void checkForDuplicateWeapons(List<Weapon> list){
		Set<String> set = new HashSet<String>(list.size());
		
		for(Weapon weapon : list) {
			if(set.add(weapon.getName()) == false) {
				throw new IllegalArgumentException("Weapon List Contains Duplicate Names!");
			}
		}
	}
	
	private static void checkForDuplicateActions(List<Action> list){
		Set<String> set = new HashSet<String>(list.size());
		
		for(Action action : list) {
			if(set.add(action.getName()) == false) {
				throw new IllegalArgumentException("Action List Contains Duplicate Names!");
			}
		}
	}
}