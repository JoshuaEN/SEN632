package ojdev.client.ui.gui;

import ojdev.common.warriors.WarriorBase;

public interface EngagedWarriorCallbackInterface {
	public void notifyTargetStatusChanged(EngagedWarrior sender, boolean isTargeted);
	
	public void notifyOpenPrivateChat(int clientId);
	
	public void notifyOpenWarriorDetails(WarriorBase warriorBase);
}
