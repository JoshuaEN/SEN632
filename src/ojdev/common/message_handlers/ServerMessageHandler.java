package ojdev.common.message_handlers;

import ojdev.common.messages.AllowedMessageContext;
import ojdev.common.messages.server.*;

public interface ServerMessageHandler extends MessageHandler {

	public abstract void handleRelayedTextMessage(RelayedTextMessage message);

	public abstract void handleServerTextMessage(ServerTextMessage message);

	public abstract void handleSetClientIdMessage(SetClientIdMessage message);

	public abstract void handleConnectedClientsListMessage(ConnectedClientsListMessage message);

	public abstract void handleEngagementCombatResultMessage(EngagementCombatResultMessage message);

	public abstract void handleEngagementStartedMessage(EngagementStartedMessage message);

	public abstract void handleEngagementEndedMessage(EngagementEndedMessage message);

	public abstract void handleEngagementActionSelectedMessage(EngagementActionSelectedMessage message);
	
	public abstract void handleClientConnectedMessage(ClientConnectedMessage message);
	
	public abstract void handleClientDisconnectedMessage(ClientDisconnectedMessage message);
	
	public abstract void handleClientStateChangedMessage(ClientStateChangedMessage clientStateChangedMessage);
	
	public abstract void handleRelayedTextToAllMessage(RelayedTextToAllMessage relayedTextToAllMessage);

	public default AllowedMessageContext getAllowedContext() {
		return AllowedMessageContext.ServerToClient;
	}

}