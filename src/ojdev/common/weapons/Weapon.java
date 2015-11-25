package ojdev.common.weapons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ojdev.common.Armory;
import ojdev.common.actions.Action;
import ojdev.common.actions.ActionDamageType;
import ojdev.common.actions.ActionStance;

public class Weapon implements Serializable {

	private static final long serialVersionUID = -97943577687541392L;

	private String name;
	
	private String description;
	
	private int attackPower;
	private int attackSpeed;
	private int defensePower;
	
	private int thrustAttackPowerMod;
	private int swingAttackPowerMod;
	private int otherAttackPowerMod;
	
	private WeaponDamageType thrustDamageType;
	private WeaponDamageType swingDamageType;
	private WeaponDamageType otherDamageType;

	private List<Action> actions;

	public Weapon(
			String name, String description, 
			int attackPower, int attackSpeed, int defensePower, 
			int thrustAttackPowerMod, int swingAttackPowerMod, int otherAttackPowerMod, 
			WeaponDamageType thrustDamageType, WeaponDamageType swingDamageType, WeaponDamageType otherDamageType, 
			List<Action> actions
	) {
		this.name = name;
		this.description = description;
		setAttackPower(attackPower);
		setAttackSpeed(attackSpeed);
		setDefensePower(defensePower);
		setThrustAttackPowerMod(thrustAttackPowerMod);
		setSwingAttackPowerMod(swingAttackPowerMod);
		setOtherAttackPowerMod(otherAttackPowerMod);
		setThrustDamageType(thrustDamageType);
		setSwingDamageType(swingDamageType);
		setOtherDamageType(otherDamageType);
		this.actions = actions;
	}
	
	public Weapon(
			String name, String description, 
			int attackPower, int attackSpeed, int defensePower, 
			int thrustAttackPowerMod, int swingAttackPowerMod, int otherAttackPowerMod,
			WeaponDamageType thrustDamageType, WeaponDamageType swingDamageType, WeaponDamageType otherDamageType, 
			Action... actions
	) {
		this(
				name, description, 
				attackPower, attackSpeed, defensePower, 
				thrustAttackPowerMod, swingAttackPowerMod, otherAttackPowerMod, 
				thrustDamageType, swingDamageType, otherDamageType,
				new ArrayList<Action>(Arrays.asList(actions)));
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public int getAttackPower() {
		return attackPower;
	}

	protected void setAttackPower(int attackPower) {
		this.attackPower = attackPower;
	}

	public int getAttackSpeed() {
		return attackSpeed;
	}

	protected void setAttackSpeed(int attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	public int getDefensePower() {
		return defensePower;
	}

	protected void setDefensePower(int defensePower) {
		if(defensePower < 0) {
			throw new IllegalArgumentException("Defense Power must be non-negative");
		}
		this.defensePower = defensePower;
	}

	public int getThrustAttackPowerMod() {
		return thrustAttackPowerMod;
	}

	protected void setThrustAttackPowerMod(int thrustAttackPowerMod) {
		this.thrustAttackPowerMod = thrustAttackPowerMod;
	}

	public int getSwingAttackPowerMod() {
		return swingAttackPowerMod;
	}

	protected void setSwingAttackPowerMod(int swingAttackPowerMod) {
		this.swingAttackPowerMod = swingAttackPowerMod;
	}

	public int getOtherAttackPowerMod() {
		return otherAttackPowerMod;
	}

	protected void setOtherAttackPowerMod(int otherAttackPowerMod) {
		this.otherAttackPowerMod = otherAttackPowerMod;
	}

	public WeaponDamageType getThrustDamageType() {
		return thrustDamageType;
	}

	protected void setThrustDamageType(WeaponDamageType thrustDamageType) {
		this.thrustDamageType = thrustDamageType;
	}

	public WeaponDamageType getSwingDamageType() {
		return swingDamageType;
	}

	protected void setSwingDamageType(WeaponDamageType swingDamageType) {
		this.swingDamageType = swingDamageType;
	}

	public WeaponDamageType getOtherDamageType() {
		return otherDamageType;
	}

	protected void setOtherDamageType(WeaponDamageType otherDamageType) {
		this.otherDamageType = otherDamageType;
	}
	
	public WeaponDamageType getDamageTypeForAction(Action action) {
		return getDamageTypeForAction(action.getDamageType());
	}
	
	public WeaponDamageType getDamageTypeForAction(ActionDamageType actionDamageType) {

		switch (actionDamageType) {
		case THRUST:
			return getThrustDamageType();

		case SWING:
			return getSwingDamageType();
			
		case OTHER:
			return getOtherDamageType();
		
		case NONE:
			return WeaponDamageType.NONE;
			
		default:
			assert false : "Missing switch for Action Damage Types";
			return WeaponDamageType.NONE;
		}
	}

	public int getEffectiveAttackPower(Action action) {
		if(action.getDamageType() == ActionDamageType.NONE) {
			return 0;
		} else {
			return getEffectiveValue(
					getAttackPower(), 
					action.getAttackPowerModifier(), 
					action.getStance(), 
					action.getDamageType(), 
					action.isGeneric()
			);
		}
	}
	
	public int getEffectiveAttackSpeed(Action action) {
		return getEffectiveValue(
				getAttackSpeed(), 
				action.getAttackSpeedModifier(), 
				action.getStance(), 
				action.getDamageType(), 
				action.isGeneric()
		);
	}
	
	public int getEffectiveDefensePower(Action action) {
		return getEffectiveValue(
				getDefensePower(), 
				action.getDefensePowerModifier(), 
				action.getStance(), 
				action.getDamageType(), 
				action.isGeneric()
		);
	}
	
	protected int getEffectiveValue(
			int base, 
			int actionMod, 
			ActionStance stance, 
			ActionDamageType actionDamageType, 
			boolean isGeneric
	) {
		if(isGeneric) {
			return actionMod;
		}
		
		if(stance == ActionStance.NONE) {
			return 0;
		}
		
		return base + actionMod + getDamageTypeModifier(actionDamageType);		
	}
	
	public int getDamageTypeModifier(ActionDamageType actionDamageType) {
		switch (actionDamageType) {
		case THRUST:
			return getThrustAttackPowerMod();
		case SWING:
			return getSwingAttackPowerMod();
		case OTHER:
			return getOtherAttackPowerMod();
		case NONE:
			return 0;
		default:
			assert false : "Missing case for Action Damage Types";
			return 0;
		}
	}

	public List<Action> getActions() {
		List<Action> tmpList = new ArrayList<Action>(actions);
		tmpList.addAll(Armory.GENERIC_ACTIONS);
		
		return Collections.unmodifiableList(tmpList);
	}
	
	public boolean canUseAction(Action action) {
		return action.isGeneric() || getActions().contains(action);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + attackPower;
		result = prime * result + attackSpeed;
		result = prime * result + defensePower;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + otherAttackPowerMod;
		result = prime * result + swingAttackPowerMod;
		result = prime * result + thrustAttackPowerMod;
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
		if (!(obj instanceof Weapon)) {
			return false;
		}
		Weapon other = (Weapon) obj;
		if (actions == null) {
			if (other.actions != null) {
				return false;
			}
		} else if (!actions.equals(other.actions)) {
			return false;
		}
		if (attackPower != other.attackPower) {
			return false;
		}
		if (attackSpeed != other.attackSpeed) {
			return false;
		}
		if (defensePower != other.defensePower) {
			return false;
		}
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
		if (otherAttackPowerMod != other.otherAttackPowerMod) {
			return false;
		}
		if (swingAttackPowerMod != other.swingAttackPowerMod) {
			return false;
		}
		if (thrustAttackPowerMod != other.thrustAttackPowerMod) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"Weapon[getName()=%s, getDescription()=%s, getAttackPower()=%s, getAttackSpeed()=%s, getDefensePower()=%s, getActions()=%s]",
				getName(), getDescription(), getAttackPower(), getAttackSpeed(), getDefensePower(), getActions());
	}

}