package ojdev.common.messages.server;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.MessageHandler;
import ojdev.common.message_handlers.ServerMessageHandler;
import ojdev.common.messages.AllowedMessageContext;
import ojdev.common.messages.MessageBase;

/**
 * A command the server sends to the client.
 */
public abstract class ServerMessage extends MessageBase {

	private static final long serialVersionUID = -9140039998128427109L;

	public ServerMessage() {
		super();
	}

	public void handleWith(MessageHandler handler) throws IllegalMessageContext {
		if(handler instanceof ServerMessageHandler) {
			handleWith((ServerMessageHandler)handler);
		} else {
			throw new IllegalArgumentException(String.format("Handler is of the wrong Type, expected %s, got %s", ServerMessageHandler.class, handler.getClass()));
		}
	}
	
	public abstract void handleWith(ServerMessageHandler handler) throws IllegalMessageContext;

	public final AllowedMessageContext getAllowedContext() {
		return AllowedMessageContext.ServerToClient;
	}
	
}