package ojdev.common.actions;

import java.io.Serializable;

public class Action implements Serializable {

	private static final long serialVersionUID = 8706948391992000444L;

	private final String name;
	
	private final String description;

	private final ActionDirection direction;
	
	private final int attackPowerModifier;

	private final int attackSpeedModifier;
	
	private final int defensePowerModifier;
	
	private final boolean generic;
	
	private final boolean endsEngagement;
	
	public Action(String name, String description, ActionDirection direction, int attackSpeedModifier, int attackPowerModifier, int defensePowerModifier, boolean generic, boolean endsEngagement) {
		this.name = name;
		this.description = description;
		this.direction = direction;
		this.attackSpeedModifier = attackSpeedModifier;
		this.attackPowerModifier = attackPowerModifier;
		this.defensePowerModifier = defensePowerModifier;
		this.generic = generic;
		this.endsEngagement = endsEngagement;
	}
	
	public Action(String name, String description, ActionDirection direction, int attackSpeedModifier, int attackPowerModifier, int defensePowerModifier, boolean generic) {
		this(name, description, direction, attackPowerModifier, attackPowerModifier, defensePowerModifier, generic, false);
	}
	
	public Action(String name, ActionDirection direction, int attackSpeedModifier, int attackPowerModifier, int defensePowerModifier, boolean generic) {
		this(name, "", direction, attackPowerModifier, attackPowerModifier, defensePowerModifier, generic, false);
	} 
	
	public Action(String name, String description, ActionDirection direction, int attackSpeedModifier, int attackPowerModifier, int defensePowerModifier) {
		this(name, description, direction, attackPowerModifier, attackPowerModifier, defensePowerModifier, false, false);
	}
	
	public Action(String name, ActionDirection direction, int attackSpeedModifier, int attackPowerModifier, int defensePowerModifier) {
		this(name, "", direction, attackPowerModifier, attackPowerModifier, defensePowerModifier, false, false);
	}
	
	public Action(String name, String description, ActionDirection direction, int attackSpeedModifier, int attackPowerModifier) {
		this(name, description, direction, attackSpeedModifier, attackPowerModifier, 0);
	}
	
	public Action(String name, ActionDirection direction, int attackSpeedModifier, int attackPowerModifier) {
		this(name, "", direction, attackSpeedModifier, attackPowerModifier, 0);
	}
	
	public Action(String name, String description, ActionDirection direction, int defensePowerModifier) {
		this(name, description, direction, 0, 0, defensePowerModifier);
	}
	
	public Action(String name, ActionDirection direction, int defensePowerModifier) {
		this(name, "", direction, 0, 0, defensePowerModifier);
	}
	
	public Action(String name, String description, ActionDirection direction) {
		this(name, description, direction, 0, 0, 0);
	}
	
	public Action(String name, ActionDirection direction) {
		this(name, "", direction, 0, 0, 0);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
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

	public boolean isGeneric() {
		return generic;
	}

	public boolean isEngagementEnder() {
		return endsEngagement;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attackPowerModifier;
		result = prime * result + attackSpeedModifier;
		result = prime * result + defensePowerModifier;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + (endsEngagement ? 1231 : 1237);
		result = prime * result + (generic ? 1231 : 1237);
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
		if (attackPowerModifier != other.attackPowerModifier) {
			return false;
		}
		if (attackSpeedModifier != other.attackSpeedModifier) {
			return false;
		}
		if (defensePowerModifier != other.defensePowerModifier) {
			return false;
		}
		if (direction != other.direction) {
			return false;
		}
		if (endsEngagement != other.endsEngagement) {
			return false;
		}
		if (generic != other.generic) {
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