package ojdev.common.messages.client;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ClientMessageHandler;
import ojdev.common.message_handlers.MessageHandler;
import ojdev.common.messages.AllowedMessageContext;
import ojdev.common.messages.MessageBase;

/**
 * A command the client sends to the server.
 */
public abstract class ClientMessage extends MessageBase {

	private static final long serialVersionUID = -9029968101331591174L;

	public ClientMessage() {
		super();
	}

	public void handleWith(MessageHandler handler) throws IllegalMessageContext {
		if(handler instanceof ClientMessageHandler) {
			handleWith((ClientMessageHandler)handler);
		} else {
			throw new IllegalArgumentException(String.format("Handler is of the wrong Type, expected %s, got %s", ClientMessageHandler.class, handler.getClass()));
		}
	}
	
	public abstract void handleWith(ClientMessageHandler handler) throws IllegalMessageContext;

	public final AllowedMessageContext getAllowedContext() {
		return AllowedMessageContext.ClientToServer;
	}

}