package ojdev.common.messages.server;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class ServerTextMessage extends ServerMessage {

	private static final long serialVersionUID = -3714503093704934792L;
	
	private final String message;

	public ServerTextMessage(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleServerTextMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		if (!(obj instanceof ServerTextMessage)) {
			return false;
		}
		ServerTextMessage other = (ServerTextMessage) obj;
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("ServerTextMessage[getMessage()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getMessage(), getAllowedContext(), getCreatedAt());
	}
}