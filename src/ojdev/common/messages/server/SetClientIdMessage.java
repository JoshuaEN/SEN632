package ojdev.common.messages.server;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class SetClientIdMessage extends ServerMessage {

	private static final long serialVersionUID = 3469305137567722070L;
	
	private final int clientId;

	public SetClientIdMessage(int clientId) {
		super();
		this.clientId = clientId;
	}

	public int getClientId() {
		return clientId;
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleSetClientIdMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + clientId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof SetClientIdMessage)) {
			return false;
		}
		SetClientIdMessage other = (SetClientIdMessage) obj;
		if (clientId != other.clientId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("SetClientIdMessage[getClientId()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getClientId(), getAllowedContext(), getCreatedAt());
	}

}