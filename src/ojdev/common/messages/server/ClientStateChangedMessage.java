package ojdev.common.messages.server;

import ojdev.common.ConnectedClientState;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class ClientStateChangedMessage extends ClientStateMessage {

	private static final long serialVersionUID = 7874030353124083145L;

	public ClientStateChangedMessage(ConnectedClientState connectedClientState) {
		super(connectedClientState);
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleClientStateChangedMessage(this);
	}

	@Override
	public String toString() {
		return String.format(
				"ClientStateChangeMessage[getConnectedClientState()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getConnectedClientState(), getAllowedContext(), getCreatedAt());
	}

}
