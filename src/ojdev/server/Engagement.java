package ojdev.server;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import ojdev.common.Armory;
import ojdev.common.SelectedAction;
import ojdev.common.WarriorCombatResult;
import ojdev.common.actions.Action;
import ojdev.common.actions.RetreatAction;
import ojdev.common.exceptions.InvalidClientId;
import ojdev.common.warriors.WarriorBase;
import ojdev.common.weapons.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

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
		}

		
		for(EngagedWarrior engagedWarrior : engagedWarriors) {
			
			if(engagedWarrior.getWarrior() == null) {
				throw new WarriorNotReadyException("No Warrior has been Chosen to Represent this Player");
			}
			
			if(engagedWarrior.getWarrior().hasNoWeaponEquipped()) {
				throw new WarriorNotReadyException("Warrior has no Weapon Equipped");
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
		HashMap<Integer, ActionStats> warriorActionStats = new HashMap<Integer, ActionStats>(selectedWarriorActions.size());
		TreeMap<Integer, List<Integer>> warriorAttackSpeedGroups = new TreeMap<Integer, List<Integer>>();
		HashMap<Integer, WarriorNode> warriorNodeMap = new HashMap<Integer, WarriorNode>(selectedWarriorActions.size());
		
		boolean retreatFlag = false;
		
		for(EngagedWarrior engagedWarrior : engagedWarriors) {
			SelectedAction selectedAction = selectedWarriorActions.get(engagedWarrior.getClientId());
			Weapon chosenWeapon = engagedWarrior.getWarrior().getEquippedWeapon();
			Action chosenAction = selectedAction.getAction();
			
			ActionStats stats;
			
			if(chosenWeapon == null) {
				stats = new ActionStats(0, 0, 0);
			} else {
				stats = new ActionStats(
						chosenWeapon.getEffectiveAttackSpeed(chosenAction), 
						chosenWeapon.getEffectiveAttackPower(chosenAction), 
						chosenWeapon.getEffectiveDefensePower(chosenAction)
					);
			}
			
			
			int attackSpeed = stats.getAttackSpeed();

			if(warriorAttackSpeedGroups.containsKey(attackSpeed) == false) {
				warriorAttackSpeedGroups.put(attackSpeed, new ArrayList<Integer>());
			}
			warriorAttackSpeedGroups.get(attackSpeed).add(engagedWarrior.getClientId());
			
			/* Use instance of instead of directly comparing class to allow for
			 * extensions of the RetreatAction to be correctly detected.
			 */
			if(retreatFlag == false && chosenAction instanceof RetreatAction) {
				retreatFlag = true;
			}
			
			
			warriorActionStats.put(
				engagedWarrior.getClientId(),
				stats
			);
			
			warriorNodeMap.put(
				engagedWarrior.getClientId(),
				new WarriorNode(engagedWarrior.getClientId(), selectedAction.getTargetClientId(), stats, chosenWeapon, chosenAction)
			);
		}
		
		/*
		 * Pass 2
		 * Fill the node data in
		 */
		for(Entry<Integer, WarriorNode> nodeMapEntry : warriorNodeMap.entrySet()) {
			WarriorNode currentNode = nodeMapEntry.getValue();
			WarriorNode oppoentNode = 
					warriorNodeMap.get(
							selectedWarriorActions.get(
									currentNode.getClientId()
							).getTargetClientId()
					);
			
			Edge edge = new Edge(currentNode, oppoentNode);
			
			currentNode.addCombatEdge(edge);
			oppoentNode.addCombatEdge(edge);
		}
		
		/*
		 * Pass 3
		 * Generate Result data
		 * 
		 * TODO Make this support more than 2 warriors max.
		 * Right now, having more than two Warriors will "work",
		 * but it ignores the complex interactions.
		 * (Each attack is effectively isolated, with only
		 * the cumulative result of all of the attacks being
		 * noted).
		 * 
		 * E.g. Warrior A successfully attacks Warrior B,
		 * Warrior B though successfully attacks Warrior C,
		 * even though had Warrior B attacked Warrior A,
		 * they wouldn't have been successful because Warrior A's
		 * attack speed is faster (and thus Warrior B's attack would
		 * have been interrupted).
		 * 
		 * The difficultly with doing so is that there can be cycles, as a result of the rules.
		 * This significantly complicates the process (Warrior A attacks Warrior B, Warrior B
		 * attacks Warrior C, Warrior C attacks Warrior A; headache ensues because each
		 * attack could interrupt another attack, so the big picture must be considered
		 * rather than simply looking at individual combat between two Warriors).
		 */
		List<WarriorCombatResult> warriorCombatResults = new ArrayList<WarriorCombatResult>(engagedWarriors.size());
		
		for(Entry<Integer, WarriorNode> warriorNodeEntry : warriorNodeMap.entrySet()) {
			int clientId = warriorNodeEntry.getKey();
			WarriorNode warriorNode = warriorNodeEntry.getValue();
			warriorCombatResults.add(
				new WarriorCombatResult(
						new SelectedAction(clientId, warriorNode.getTargetClientId(), warriorNode.getActionUsed()), 
						warriorNode.getHealthLost(), 
						warriorNode.getHealthLostFrom(), 
						warriorNode.getWeaponUsed()
				)
			);
		}
		
		for(EngagedWarrior warrior : engagedWarriors) {
			warrior.notifyEngagementCombatResult(warriorCombatResults);
		}
		
		if(retreatFlag) {
			endEngagement();
		}
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
		private final int attackSpeed;
		private final int attackPower;
		private final int defensePower;
		
		private ActionStats(final int attackSpeed, final int attackPower, final int defensePower) {
			this.attackSpeed = attackSpeed;
			this.attackPower = attackPower;
			this.defensePower = defensePower;
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
		
		
	}
	
	private class Edge
	{
		private final WarriorNode warriorA;
		private final WarriorNode warriorB;
		private List<WarriorNode> winners = new ArrayList<WarriorNode>(2);
		
		public Edge(WarriorNode warriorA, WarriorNode warriorB) {
			this.warriorA = warriorA;
			this.warriorB = warriorB;
			calculateCombatResult();
		}
		
		public List<WarriorNode> getWinners() {
			return Collections.unmodifiableList(winners);			
		}

		private void calculateCombatResult() {
			ActionStats aStats = warriorA.getStats();
			ActionStats bStats = warriorB.getStats();
			
			boolean aAttackOK = false, 
					bAttackOK = false;
			
			if(aStats.getAttackPower() > bStats.getDefensePower()) {
				aAttackOK = true;
			}
			
			if(bStats.getAttackPower() > aStats.getDefensePower()) {
				bAttackOK = true;
			}
			
			if(aAttackOK && bAttackOK) {
				if(aStats.getAttackSpeed() >= bStats.getAttackSpeed()) {
					winners.add(warriorA);
				}
				if(bStats.getAttackSpeed() >= aStats.getAttackSpeed()) {
					winners.add(warriorB);
				}
			} else if(aAttackOK) {
				winners.add(warriorA);
			} else if(bAttackOK){
				winners.add(warriorB);
			} else {
				throw new RuntimeException("Logic Error");
			}
			
			int healthLost;
			if(winners.contains(warriorA)) {
				healthLost = aStats.getAttackPower() - bStats.getDefensePower();
				warriorB.setHealthLost(warriorB.getHealthLost() + healthLost);
				warriorB.addHealthLostFrom(warriorA.getClientId(), healthLost);
			}
			if(winners.contains(warriorB)) {
				healthLost = bStats.getAttackPower() - aStats.getDefensePower();
				warriorA.setHealthLost(warriorA.getHealthLost() + healthLost);
				warriorA.addHealthLostFrom(warriorB.getClientId(), healthLost);
			}
		}
		
		public boolean nodeInEdge(WarriorNode node) {
			return node.getClientId() == warriorA.getClientId() || node.getClientId() == warriorB.getClientId();
		}

	}
	
	private class WarriorNode
	{
		private HashSet<Edge> combats = new HashSet<Edge>();
		private final int clientId;
		private final int targetClientId;
		private ActionStats stats;
		private final Weapon weaponUsed; 
		private final Action actionUsed;
		private int healthLost = -1;
		private HashMap<Integer, Integer> healthLostFrom = new HashMap<Integer, Integer>();
		
		public WarriorNode(int clientId, int targetClientId, ActionStats stats, Weapon weaponUsed, Action actionUsed) {
			this.clientId = clientId;
			this.targetClientId = targetClientId;
			this.stats = stats;
			this.weaponUsed = weaponUsed;
			this.actionUsed = actionUsed;
		}

		public void addHealthLostFrom(int fromClientId, int givenHealthLost) {
			healthLostFrom.put(fromClientId, givenHealthLost);
		}

		public int getTargetClientId() {
			return targetClientId;
		}

		public Weapon getWeaponUsed() {
			return weaponUsed;
		}

		public Action getActionUsed() {
			return actionUsed;
		}

		public void addCombatEdge(Edge edge) {
			for(Edge edge2 : combats) {
				if(edge2.nodeInEdge(edge.warriorA) && edge2.nodeInEdge(edge.warriorB)) {
					return;
				}
			}
			
			combats.add(edge);
		}

		public HashSet<Edge> getCombats() {
			return combats;
		}
		
		public int getClientId() {
			return clientId;
		}
		
		public ActionStats getStats() {
			return stats;
		}

		public int getHealthLost() {
			return healthLost;
		}

		public void setHealthLost(int healthLost) {
			this.healthLost = healthLost;
		}
		
		public HashMap<Integer, Integer> getHealthLostFrom() {
			return healthLostFrom;
		}
	}
}