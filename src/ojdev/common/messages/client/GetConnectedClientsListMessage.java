package ojdev.common.messages.client;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ClientMessageHandler;

public class GetConnectedClientsListMessage extends ClientMessage {

	private static final long serialVersionUID = 1469743929197665031L;

	public GetConnectedClientsListMessage() {
		super();
	}

	@Override
	public void handleWith(ClientMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleGetConnectedClientsListMessage(this);
	}

	@Override
	public String toString() {
		return String.format("GetConnectedClientsListMessage[getAllowedContext()=%s, getCreatedAt()=%s]",
				getAllowedContext(), getCreatedAt());
	}

}
