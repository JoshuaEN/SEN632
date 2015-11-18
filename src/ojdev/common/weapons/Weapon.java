package ojdev.common.weapons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ojdev.common.actions.Action;

public class Weapon implements Serializable {

	private static final long serialVersionUID = -97943577687541392L;

	private String name;
	
	private String description;

	private int attackPower;

	private int attackSpeed;

	private int defensePower;

	private List<Action> actions;

	public Weapon(String name, String description, int attackPower, int attackSpeed, int defensePower, List<Action> actions) {
		this.name = name;
		this.description = description;
		this.attackPower = attackPower;
		this.attackSpeed = attackSpeed;
		this.defensePower = defensePower;
		this.actions = actions;
	}
	
	public Weapon(String name, String description, int attackPower, int attackSpeed, int defensePower, Action... actions) {
		this(name, description, attackPower, attackSpeed, defensePower, new ArrayList<Action>(Arrays.asList(actions)));
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
		this.defensePower = defensePower;
	}

	public int getEffectiveAttackPower(Action action) {
		return action.getAttackPowerModifier() + getAttackPower();
	}
	
	public int getEffectiveAttackSpeed(Action action) {
		return action.getAttackSpeedModifier() + getAttackSpeed();
	}
	
	public int getEffectiveDefensePower(Action action) {
		return action.getDefensePowerModifier() + getDefensePower();
	}

	public List<Action> getActions() {
		return Collections.unmodifiableList(actions);
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
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"Weapon[getName()=%s, getDescription()=%s, getAttackPower()=%s, getAttackSpeed()=%s, getDefensePower()=%s, getActions()=%s]",
				getName(), getDescription(), getAttackPower(), getAttackSpeed(), getDefensePower(), getActions());
	}

}