package ojdev.common.messages.client;

import java.util.Collections;
import java.util.List;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ClientMessageHandler;

public class SendTextMessage extends ClientMessage {

	private static final long serialVersionUID = -6506388499197784275L;

	private final List<Integer> receiversClientIds;

	private final String message;

	public SendTextMessage(String message, List<Integer> receiversClientIds) {
		super();
		this.message = message;
		this.receiversClientIds = Collections.unmodifiableList(receiversClientIds);
	}

	public List<Integer> getReceivers() {
		return receiversClientIds;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public void handleWith(ClientMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleSendTextMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((receiversClientIds == null) ? 0 : receiversClientIds.hashCode());
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
		if (!(obj instanceof SendTextMessage)) {
			return false;
		}
		SendTextMessage other = (SendTextMessage) obj;
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (receiversClientIds == null) {
			if (other.receiversClientIds != null) {
				return false;
			}
		} else if (!receiversClientIds.equals(other.receiversClientIds)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"SendTextMessage[getReceivers()=%s, getMessage()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getReceivers(), getMessage(), getAllowedContext(), getCreatedAt());
	}

}