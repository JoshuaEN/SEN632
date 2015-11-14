package ojdev.common.actions;

import java.io.Serializable;

public class Action implements Serializable {

	private static final long serialVersionUID = 8706948391992000444L;

	private final String name;

	private final ActionDirection direction;
	
	private final int attackPowerModifier;

	private final int attackSpeedModifier;
	
	private final int defensePowerModifier;
	
	public Action(String name, ActionDirection direction, int attackSpeedModifier, int attackPowerModifier, int defensePowerModifier) {
		this.name = name;
		this.direction = direction;
		this.attackSpeedModifier = attackSpeedModifier;
		this.attackPowerModifier = attackPowerModifier;
		this.defensePowerModifier = defensePowerModifier;
	}
	
	public Action(String name, ActionDirection direction, int attackSpeedModifier, int attackPowerModifier) {
		this(name, direction, attackSpeedModifier, attackPowerModifier, 0);
	}
	
	public Action(String name, ActionDirection direction, int defensePowerModifier) {
		this(name, direction, 0, 0, defensePowerModifier);
	}
	
	public Action(String name, ActionDirection direction) {
		this(name, direction, 0, 0, 0);
	}

	public String getName() {
		return name;
	}

	public ActionDirection getDirection() {
		return direction;
	}
	
	public int getAttackPowerModifier() {
		return attackPowerModifier;
	}

	public int getAttackSpeedModifier() {
		return attackSpeedModifier;
	}
	
	public int getDefensePowerModifier() {
		return defensePowerModifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
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
		if (!(obj instanceof Action)) {
			return false;
		}
		Action other = (Action) obj;
		if (direction != other.direction) {
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
		return String.format("Action[getName()=%s, getDirection()=%s]", getName(), getDirection());
	}
}