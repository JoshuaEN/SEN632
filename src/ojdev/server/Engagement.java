package ojdev.server;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ojdev.common.Armory;
import ojdev.common.SelectedAction;
import ojdev.common.WarriorCombatResult;
import ojdev.common.actions.Action;
import ojdev.common.actions.ActionDamageType;
import ojdev.common.actions.ActionDirection;
import ojdev.common.actions.ActionStance;
import ojdev.common.exceptions.InvalidClientId;
import ojdev.common.warriors.WarriorBase;
import ojdev.common.weapons.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Represents a single combat engagement between two, or (in theory) more Warriors. Acts as the Moderator for any Engagement actions (recording actions, calculating combat results, notifying Warriors of interactions). Uses the EngagedWarrior interface to communicate with  the ConnectedCients for the respective Warriors.
 */
public class Engagement {

	private final List<EngagedWarrior> engagedWarriors;
	private final List<Integer> engagedClientIds;

	private final int startedByClientId;

	private final HashMap<Integer, SelectedAction> selectedWarriorActions;
	
	private boolean ended = false;

	public Engagement(List<EngagedWarrior> engagedWarriors, int startedByClientId) throws WarriorNotReadyException, IllegalArgumentException {
		
		if(engagedWarriors.size() < 2) {
			throw new IllegalArgumentException("An Engagement must involve at least two warriors.");
		} else if(engagedWarriors.size() > 2) {
			throw new IllegalArgumentException("Engagements currently do not support more than two warriors.");
		} else if(engagedWarriors.get(0).getClientId() == engagedWarriors.get(1).getClientId()) {
			throw new IllegalArgumentException("Engagements must involve at least two DIFFERENT warriors.");
		}

		
		for(EngagedWarrior engagedWarrior : engagedWarriors) {
			
			if(engagedWarrior.getWarrior() == null) {
				throw new WarriorNotReadyException("No Warrior has been Chosen to Represent this Player");
			}
			
			if(engagedWarrior.getWarrior().hasNoWeaponEquipped()) {
				throw new WarriorNotReadyException("Warrior has no Weapon Equipped");
			}
			
			if(engagedWarrior.getWarrior().isDead()) {
				throw new WarriorNotReadyException("Warrior is dead");
			}
		}
		
		this.engagedWarriors = engagedWarriors;
		this.startedByClientId = startedByClientId;
		this.selectedWarriorActions = new HashMap<Integer, SelectedAction>(engagedWarriors.size());
		this.engagedClientIds = new ArrayList<Integer>(engagedWarriors.size());
		for(EngagedWarrior warrior : engagedWarriors) {
			this.selectedWarriorActions.put(warrior.getClientId(), null);
			this.engagedClientIds.add(warrior.getClientId());
		}
		
		for(EngagedWarrior warrior : engagedWarriors) {
			warrior.notifyEngagementStarted(this, getStartedByClientId(), getEngagedClientIds());
		}
	}

	public List<EngagedWarrior> getEngagedWarriors() {
		if(isEnded()) {
			return Collections.unmodifiableList(new ArrayList<EngagedWarrior>()); 
		} else {
			return Collections.unmodifiableList(engagedWarriors);
		}
	}

	public int getStartedByClientId() {
		return startedByClientId;
	}

	public List<Integer> getEngagedClientIds() {
		if(isEnded()) {
			return Collections.unmodifiableList(new ArrayList<Integer>()); 
		} else {
			return Collections.unmodifiableList(engagedClientIds);
		}
	}
	
	public boolean isEnded() {
		return ended;
	}
	
	private synchronized EngagedWarrior getEngagedWarriorFromClientId(int clientId) {
		for(EngagedWarrior engagedWarrior : engagedWarriors) {
			if(engagedWarrior.getClientId() == clientId)
				return engagedWarrior;
		}
		
		return null;
	}
	
	public synchronized void selectAction(int clientId, int targetClientId, Action action) 
			throws InvalidClientId, WarriorActionAlreadySelectedException, InvalidActionSelectionException
	{
		if(isEnded()) {
			return;
		}
		
		if(engagedClientIds.contains(clientId) == false) {
			throw new ojdev.common.exceptions.InvalidClientId(clientId);
		}
		
		if(engagedClientIds.contains(targetClientId) == false) {
			throw new ojdev.common.exceptions.InvalidClientId(targetClientId);
		}

		if(selectedWarriorActions.get(clientId) != null) {
			throw new WarriorActionAlreadySelectedException();
		}
		
		/* 
		 * Check if the Action is known
		 * This isn't needed because if the Warrior's Weapon
		 * can use the selected Action, which if the action is unknown
		 * then it certainly cannot be used with the Weapon,
		 * however it provides better error messages to the client.
		 */
		if(Armory.ACTIONS.contains(action) == false) {
			throw new InvalidActionSelectionException("Unknown Action");
		}

		/*
		 * Check if the Warror's Weapon can use the selected action.
		 */
		WarriorBase warriorBase = getEngagedWarriorFromClientId(clientId).getWarrior();
		if(warriorBase.getEquippedWeapon().canUseAction(action) == false) {
			throw new InvalidActionSelectionException("Warrior's Equipped Weapon cannot use selected Action");
		}
		
		
		SelectedAction selectedAction = new SelectedAction(clientId, targetClientId, action);
		selectedWarriorActions.put(clientId, selectedAction);

		
		for(EngagedWarrior warrior : engagedWarriors) {
			warrior.notifyEngagementActionSelected(selectedAction);
		}

		if(allWarriorsReady()) {
			calculateCombatResult();
		}
	}

	/**
	 * Ends the engagement, notifying all clients. Intended to be used by the Engagement itself, or by ConnectedClients in the event the underlying connection was either closed remotely or locally. Not intended for direct access by Clients through a Message; instead, the Client should use the RetreatAction to end the engagement "normally".
	 */
	public synchronized void endEngagement() {
		ended = true;
		
		for(EngagedWarrior warrior : engagedWarriors) {
			warrior.notifyEngagementEnded(getEngagedClientIds());
		}
	}

	/**
	 * Core of the Engagement. Determines the result of a combat round based on the actions selected by each Warrior.
	 */
	private synchronized void calculateCombatResult() {
		/*
		 *  Pass 1
		 *  Get effective attack speed and attack/defense power for each Warrior.
		 */
		Map<Integer, ActionStats> warriorActionStats = new HashMap<Integer, ActionStats>(selectedWarriorActions.size());
		Map<Integer, List<ActionResult>> intermResults = new HashMap<Integer, List<ActionResult>>();
		Map<Integer, Weapon> weaponUsedById = new HashMap<Integer, Weapon>(selectedWarriorActions.size());
		
		boolean endEngagementFlag = false;
		
		for(int i = 0; i < engagedWarriors.size(); i++) {
			EngagedWarrior engagedWarrior = engagedWarriors.get(i);
			SelectedAction selectedAction = selectedWarriorActions.get(engagedWarrior.getClientId());
			Weapon chosenWeapon = engagedWarrior.getWarrior().getEquippedWeapon();
			Action chosenAction = selectedAction.getAction();
			
			ActionStats stats;
			
			if(chosenWeapon == null || engagedWarrior.getWarrior().isDead()) {
				stats = new ActionStats(
							engagedWarrior.getClientId(), 
							selectedAction.getTargetClientId(), 
							engagedWarrior.getWarrior(), chosenWeapon, selectedAction, 
							0, 0, 0
						);
			} else {
				stats = new ActionStats(
						engagedWarrior.getClientId(), 
						selectedAction.getTargetClientId(), 
						engagedWarrior.getWarrior(), chosenWeapon, selectedAction
					);
			}
			
			if(chosenAction.isEngagementEnder()) {
				endEngagementFlag = true;
			}
			
			
			warriorActionStats.put(stats.getClientId(), stats);
			intermResults.put(stats.getClientId(), new ArrayList<ActionResult>());
			weaponUsedById.put(stats.getClientId(), chosenWeapon);
		}
		
		/*
		 * Pass 2
		 * 
		 * Attack Calculations
		 * 
		 * For an attack to be successful it:
		 * * Must not be blocked by the oppoent's defense power being higher than the attacks attack power
		 * * Must not be interrupted by the oppoent's attack
		 */

		
		for(ActionStats stats : warriorActionStats.values()) {
			ActionResult result = stats.calculateResult(warriorActionStats.get(stats.getTargetClientId()));
			
			if(result == null) {
				continue;
			}
			
			intermResults.get(result.getClientId()).add(result);
		}
		
		/*
		 * Pass 3
		 * 
		 * Result merging
		 */
		List<WarriorCombatResult> warriorCombatResults = new ArrayList<WarriorCombatResult>(engagedWarriors.size());
		
		for(Entry<Integer, List<ActionResult>> entry : intermResults.entrySet()) {
			int healthLost = 0;
			HashMap<Integer, Integer> healthLostFrom = new HashMap<Integer, Integer>();

			for(ActionResult result : entry.getValue()) {
				healthLost += result.getHealthLost();
				healthLostFrom.put(result.getAttackerClientId(), result.getHealthLost());
			}
			
			warriorCombatResults.add(
					new WarriorCombatResult(
							selectedWarriorActions.get(entry.getKey()), 
							healthLost, 
							healthLostFrom, 
							weaponUsedById.get(entry.getKey())
					)
			);
		}

		/*
		 * Pass 4
		 * 
		 * Notify
		 */
		
		for(EngagedWarrior warrior : engagedWarriors) {
			warrior.notifyEngagementCombatResult(warriorCombatResults);
		}
		
		if(endEngagementFlag) {
			endEngagement();
		}
		
		unreadyAllWarriors();
	}
	
	private synchronized boolean allWarriorsReady() {
		if(isEnded()) {
			return false;
		}
		
		for(Entry<Integer, SelectedAction> selectedActionSet : selectedWarriorActions.entrySet()) {
			if(selectedActionSet.getValue() == null)
				return false;
		}

		return true;
	}
	
	private synchronized void unreadyAllWarriors() {
		for(Entry<Integer, SelectedAction> selectedActionSet : selectedWarriorActions.entrySet()) {
			selectedActionSet.setValue(null);
		}
	}

	@Override
	public String toString() {
		return String.format(
				"Engagement[selectedWarriorActions=%s, getEngagedWarriors()=%s, getStartedByClientId()=%s, getEngagedClientIds()=%s, isEnded()=%s]",
				selectedWarriorActions, getEngagedWarriors(), getStartedByClientId(), getEngagedClientIds(), isEnded());
	}
	
	public class WarriorActionAlreadySelectedException extends Exception
	{
		private static final long serialVersionUID = 7498547899404568874L;

		public WarriorActionAlreadySelectedException() {
			super("Warrior has already selected their action.");
		}
		
		public WarriorActionAlreadySelectedException(String message) {
			super(message);
		}
	}
	
	public class InvalidActionSelectionException extends Exception
	{
		private static final long serialVersionUID = -1807522645229510795L;
				
		public InvalidActionSelectionException() {
			super("This action cannot be selected");
		}
		
		public InvalidActionSelectionException(String message) {
			super(message);
		}
	}
	
	public class WarriorNotReadyException extends Exception
	{
		private static final long serialVersionUID = -1807522645229510795L;
				
		public WarriorNotReadyException() {
			super("A Warrior is Not Ready for an Engagement");
		}
		
		public WarriorNotReadyException(String message) {
			super(message);
		}
	}
	
	private class ActionStats
	{
		private final int clientId;
		private final int targetClientId;
		private final SelectedAction selectedAction;
		private final Weapon weaponUsed;
		private final WarriorBase warriorUsed;
		private final int attackSpeed;
		private final int attackPower;
		private final int defensePower;
		
		private ActionStats(final int clientId, final int targetClientId, WarriorBase warriorUsed, Weapon weaponUsed, SelectedAction selectedAction) {
			this(
					clientId, 
					targetClientId, 
					warriorUsed,
					weaponUsed, 
					selectedAction, 
					weaponUsed.getEffectiveAttackSpeed(selectedAction.getAction()), 
					weaponUsed.getEffectiveAttackPower(selectedAction.getAction()), 
					weaponUsed.getEffectiveDefensePower(selectedAction.getAction())
			);
		}
		
		private ActionStats(final int clientId, final int targetClientId, WarriorBase warriorUsed, Weapon weaponUsed, SelectedAction selectedAction, int attackSpeed, int attackPower, int defensePower) {
			this.clientId = clientId;
			this.targetClientId = targetClientId;
			this.weaponUsed = weaponUsed;
			this.selectedAction = selectedAction;
			this.warriorUsed = warriorUsed;
			this.attackSpeed = attackSpeed;
			this.attackPower = attackPower;
			this.defensePower = defensePower;
		}
		
		private int getClientId() {
			return clientId;
		}
		
		private SelectedAction getSelectedAction() {
			return selectedAction;
		}
		
		private Weapon getWeaponUsed() {
			return weaponUsed;
		}
		
		private WarriorBase getWarriorUsed() {
			return warriorUsed;
		}
		
		private int getTargetClientId() {
			return targetClientId;
		}

		private int getAttackSpeed() {
			return attackSpeed;
		}

		private int getAttackPower() {
			return attackPower;
		}

		private int getDefensePower() {
			return defensePower;
		}
		
		private ActionResult calculateResult(ActionStats target) {
			
			Action action = selectedAction.getAction();
			
			// Null action or otherwise non-offensive action,
			// or the warrior is dead and unable to do anything.
			if(action.getStance() == ActionStance.NONE || 
				action.getDamageType() == ActionDamageType.NONE ||
				getWarriorUsed().isDead()) {
				return null;
			}
					
			// If player isn't attacking, no result to calculate.
			if(action.getStance() != ActionStance.ATTACK) {
				return null;
			}
			
			Action targetsAction = target.getSelectedAction().getAction();
			int possibleDamageToTarget = target.getWarriorUsed().getDamageDone(getWeaponUsed(), action, targetsAction);
			

			// If client attacked themselves, it's assumed they put up no defense
			if(targetClientId == clientId) {
				return new ActionResult(targetClientId, possibleDamageToTarget, clientId);
			}
			
			int possibleDamageFromTarget = getWarriorUsed().getDamageDone(target.getWeaponUsed(), targetsAction, action);
			int possibleDamageFromTargetCounter = 
					getWarriorUsed().getDamageDone(
							target.getWeaponUsed(), 
							new Action("<SYSTEM> Counter Action", ActionDirection.None, ActionStance.ATTACK, action.getDamageType()), 
							action
					);
			
			// Targets puts up no defense
			if(targetsAction.getStance() == ActionStance.NONE || target.getWarriorUsed().isDead()) {
				return new ActionResult(targetClientId, possibleDamageToTarget, clientId);
			}
			
			// If the Target has a chance at blocking.
			if(target.getTargetClientId() == clientId) {
				if (targetsAction.getDirection() == action.getDirection() || 
						targetsAction.getDirection() == ActionDirection.None)
				{

					// Attack blocked successfully
					if(
							(
									targetsAction.getStance() == ActionStance.DEFENSE_BLOCK ||
									targetsAction.getStance() == ActionStance.DEFENSE_COUNTER
									) && getAttackPower() < target.getDefensePower()
							) {

						// No counter attack
						if(targetsAction.getStance() == ActionStance.DEFENSE_BLOCK) {
							return null;

							// Counter attacked
						} else {
							return new ActionResult(clientId, possibleDamageFromTargetCounter, targetClientId);
						}

						// Attack blocked by both sides attacking the same side,
						// and having less attack power
					} else if (
							target.getTargetClientId() == getClientId() &&
							targetsAction.getStance() == ActionStance.ATTACK &&
							getAttackPower() <= target.getAttackPower()
							) {
						// The other player's attack will succeed, we don't want to double the damage applied.
						// Still, we take attrition damage
						return new ActionResult(clientId, 2, targetClientId);
					}
					
				// If they are targeting us from a different direction, check if they cancel our attack
				} else if(warriorUsed.isOverPainThreshold(possibleDamageFromTarget)) {
					// OK, so we both have enough attack to do damage, who is faster though?

					// They hit first, our attack is interrupted and failed
					if(target.getAttackSpeed() > attackSpeed) {
						return null;
					}
				}
			}

			return new ActionResult(targetClientId, possibleDamageToTarget, clientId);
		}
		
	}
	
	private class ActionResult
	{
		private final int clientId;
		private final int healthLost;
		private final int attackerClientId;
		
		private ActionResult(int clientId, int healthLost, int attackerClientId) {
			this.clientId = clientId;
			this.healthLost = healthLost;
			this.attackerClientId = attackerClientId;
		}

		public int getClientId() {
			return clientId;
		}
		
		public int getAttackerClientId() {
			return attackerClientId;
		}
		
		public int getHealthLost(){ 
			return healthLost;
		}
	}
}