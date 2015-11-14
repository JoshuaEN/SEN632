package ojdev.common;

import java.io.Serializable;
import java.util.HashMap;

import ojdev.common.weapons.Weapon;

public class WarriorCombatResult implements Serializable {

	private static final long serialVersionUID = 2593534978654749344L;
	
	private final SelectedAction selectedAction;
	
	private final int heathLost;

	private final HashMap<Integer, Integer> healthLostFrom;

	private final Weapon weaponUsed;

	public WarriorCombatResult(SelectedAction selectedAction, int healthLost, HashMap<Integer, Integer> healthLostFrom, Weapon weaponUsed) {
		this.selectedAction = selectedAction;
		this.heathLost = healthLost;
		this.healthLostFrom = healthLostFrom;
		this.weaponUsed = weaponUsed;
	}

	public int getClientId() {
		return getSelectedAction().getClientId();
	}

	public int getHealthLost() {
		return this.heathLost;
	}

	public HashMap<Integer, Integer> getHealthLostFrom() {
		return healthLostFrom;
	}

	public Weapon getWeaponUsed() {
		return weaponUsed;
	}

	public SelectedAction getSelectedAction() {
		return selectedAction;
	}

	@Override
	public String toString() {
		return String.format(
				"WarriorCombatResult[getClientId()=%s, getHealthLost()=%s, getHealthLostFrom()=%s, getWeaponUsed()=%s, getSelectedAction()=%s]",
				getClientId(), getHealthLost(), getHealthLostFrom(), getWeaponUsed(), getSelectedAction());
	}

}