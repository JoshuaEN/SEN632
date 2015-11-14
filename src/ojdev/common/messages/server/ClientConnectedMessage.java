package ojdev.common.messages.server;

import ojdev.common.ConnectedClientState;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class ClientConnectedMessage extends ClientStateMessage {

	private static final long serialVersionUID = -4573419868516932959L;
	
	public ClientConnectedMessage(ConnectedClientState connectedClientState) {
		super(connectedClientState);
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleClientConnectedMessage(this);
	}

	@Override
	public String toString() {
		return String.format(
				"ClientConnectedMessage[getConnectedClientState()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getConnectedClientState(), getAllowedContext(), getCreatedAt());
	}
	
}
