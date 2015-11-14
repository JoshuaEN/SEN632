package ojdev.common.messages.server;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class RelayedTextToAllMessage extends ServerMessage {

	private static final long serialVersionUID = 5959811628068340265L;

	private final int senderClientId;
	
	private final String message;
	
	public RelayedTextToAllMessage(String message, int senderClientId) {
		super();
		this.senderClientId = senderClientId;
		this.message = message;
	}
	
	public int getSenderClientId() {
		return senderClientId;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleRelayedTextToAllMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + senderClientId;
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
		if (!(obj instanceof RelayedTextToAllMessage)) {
			return false;
		}
		RelayedTextToAllMessage other = (RelayedTextToAllMessage) obj;
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (senderClientId != other.senderClientId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"RelayedTextToAllMessage[getSenderClientId()=%s, getMessage()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getSenderClientId(), getMessage(), getAllowedContext(), getCreatedAt());
	}
	
}
