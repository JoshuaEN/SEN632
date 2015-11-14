package ojdev.common.message_handlers;

import ojdev.common.messages.*;
import ojdev.common.messages.client.*;

public interface ClientMessageHandler extends MessageHandler {

	public abstract void handleSetWarriorMessage(SetWarriorMessage message);

	public abstract void handleSendTextMessage(SendTextMessage message);

	public abstract void handleTakeActionMessage(TakeActionMessage message);
	
	public abstract void handleSendTextToAllMessage(SendTextToAllMessage sendTextToAllMessage);
	
	public abstract void handleGetConnectedClientsListMessage(
			GetConnectedClientsListMessage getConnectedClientsListMessage);

	public default AllowedMessageContext getAllowedContext() {
		return AllowedMessageContext.ClientToServer;
	}

}