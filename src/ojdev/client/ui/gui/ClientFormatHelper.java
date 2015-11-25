package ojdev.client.ui.gui;

import ojdev.common.ConnectedClientState;

public final class ClientFormatHelper {

	public static String getMasterNameFromState(ConnectedClientState state, boolean isUs) {
		return String.format(
				"%s %s, %s",
				(isUs ? "Your" : state.getClientId() + "'s"),
				state.getWarrior().getTypeName(),
				state.getWarrior().getName()
		);
	}
	
	public static String getMasterNameFromState(ConnectedClientState state) {
		return getMasterNameFromState(state, false);
	}

}
