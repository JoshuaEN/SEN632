package ojdev.client;

import ojdev.common.warriors.WarriorBase;

/**
 * Interface used by Client to communicate with the UI
 */
public interface ClientUserInterface {
	public abstract void notifyException(Exception e);

	public abstract void notifyCurrentWarriorChanged(WarriorBase currentWarrior);
}
