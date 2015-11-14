package ojdev.common.message_handlers;

import ojdev.common.messages.AllowedMessageContext;
import ojdev.common.messages.InvalidMessage;

public interface MessageHandler {

	public default AllowedMessageContext getAllowedContext() {
		return AllowedMessageContext.Either;
	}

	public abstract void handleInvalidMessage(InvalidMessage message);

}