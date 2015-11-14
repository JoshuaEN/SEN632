package ojdev.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ojdev.common.actions.Action;
import ojdev.common.actions.ActionDirection;
import ojdev.common.weapons.Weapon;

public class Armory {

	/**
	 * Available Actions
	 */
	public static final List<Action> ACTIONS;
	
	public static final Action OVERHEAD_SWING = new Action("Overhead Strike", ActionDirection.High, -20, 20);
	public static final Action LEFT_SWING = new Action("Left Swing", ActionDirection.Left, 0, 0);
	public static final Action RIGHT_SWING = new Action("Right Swing", ActionDirection.Right, 0, 0);
	public static final Action LOW_SWING = new Action("Low Swing", ActionDirection.Low, -10, -20);

	public static final Action HIGH_THRUST = new Action("High Thrust", ActionDirection.High, 10, 10);
	public static final Action LOW_THRUST = new Action("Low Thrust", ActionDirection.Low, 10, 5);

	public static final Action BLOCK_HIGH = new Action("Block High", ActionDirection.High, 20);
	public static final Action BLOCK_LEFT = new Action("Block Left", ActionDirection.Left, 0);
	public static final Action BLOCK_RIGHT = new Action("Block Right", ActionDirection.Right, 0);
	public static final Action BLOCK_LOW = new Action("Block Low", ActionDirection.Low, 20);
	
	public static final Action INVALID_TEST_ACTION = new Action("Invalid Test Action", ActionDirection.None, -999, -999, -999);

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
		
		
		if(SharedConstant.DEBUG) {
			tmpActionList.add(INVALID_TEST_ACTION);
		}
		
		
		ACTIONS = Collections.unmodifiableList(tmpActionList);
	}
	
	/**
	 * Available Weapons
	 */
	public static final List<Weapon> WEAPONS;
	
	public static final Weapon NO_WEAPON = new Weapon(
			"No Weapon", 
			"", 
			0, 0, 0, 
			new ArrayList<Action>(0)
	);

	public static final Weapon GREAT_SWORD = new Weapon(
			"Bidenhänder",
			"",
			50, 5, 30,
			OVERHEAD_SWING,
			LEFT_SWING,
			RIGHT_SWING,
			LOW_SWING,
			
			BLOCK_HIGH,
			BLOCK_LEFT,
			BLOCK_RIGHT,
			BLOCK_LOW
	);
	
	public static final Weapon SWORD = new Weapon(
			"Gladius", 
			"", 
			20, 45, 40,
			HIGH_THRUST,
			LOW_THRUST,
			
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
		
		WEAPONS = Collections.unmodifiableList(tmpWeaponList);
	}
}