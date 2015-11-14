package ojdev.common.messages.server;

import ojdev.common.ConnectedClientState;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class ClientDisconnectedMessage extends ClientStateMessage {

	private static final long serialVersionUID = -1471072993517587022L;

	public ClientDisconnectedMessage(ConnectedClientState connectedClientState) {
		super(connectedClientState);
	}
	
	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleClientDisconnectedMessage(this);
	}

	@Override
	public String toString() {
		return String.format(
				"ClientDisconnectedMessage[getConnectedClientState()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getConnectedClientState(), getAllowedContext(), getCreatedAt());
	}

}
