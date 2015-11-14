package ojdev.server;

import java.util.List;

import ojdev.common.SelectedAction;
import ojdev.common.WarriorCombatResult;
import ojdev.common.warriors.WarriorBase;

/**
 * Used by Engagements to signal ConnectedClients.
 */
public interface EngagedWarrior {

	public abstract WarriorBase getWarrior();

	public abstract int getClientId();

	public abstract void notifyEngagementCombatResult(List<WarriorCombatResult> warriorCombatResults);

	public abstract void notifyEngagementStarted(Engagement engagement, int startedByClientId, List<Integer> involvedClientIds);

	public abstract void notifyEngagementEnded(List<Integer> involvedClientIds);

	public abstract void notifyEngagementActionSelected(SelectedAction selectedAction);

}