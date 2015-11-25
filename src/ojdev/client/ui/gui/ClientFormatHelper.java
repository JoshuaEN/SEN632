package ojdev.client.ui.gui;

import ojdev.common.ConnectedClientState;
import ojdev.common.warriors.WarriorBase;

public final class ClientFormatHelper {

	public static String getMasterNameFromState(ConnectedClientState state, boolean isUs) {
		
		WarriorBase warrior = state.getWarrior();
		

		if(warrior != null) {
			return String.format(
				"%s %s, %s",
				(isUs ? "Your" : state.getClientId() + "'s"),
				warrior.getTypeName(),
				warrior.getName()
			);
		} else {
			return String.format(
				"%s, Spectating",
				(isUs ? "You" : state.getClientId())
			);
		}
		
		
	}
	
	public static String getMasterNameFromState(ConnectedClientState state) {
		return getMasterNameFromState(state, false);
	}

}
