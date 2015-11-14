package ojdev.common.messages.client;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ClientMessageHandler;

public class SendTextToAllMessage extends ClientMessage {

	private static final long serialVersionUID = 7944339680597233022L;

	private final String message;
	
	public SendTextToAllMessage(String message) {
		super();
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	@Override
	public void handleWith(ClientMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleSendTextToAllMessage(this);
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
		if (!(obj instanceof SendTextToAllMessage)) {
			return false;
		}
		SendTextToAllMessage other = (SendTextToAllMessage) obj;
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
		return String.format("SendTextToAllMessage[getMessage()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getMessage(), getAllowedContext(), getCreatedAt());
	}

}
