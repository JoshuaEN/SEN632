package ojdev.common.messages.server;

import java.util.Collections;
import java.util.List;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class RelayedTextMessage extends ServerMessage {

	private static final long serialVersionUID = -8465051585506996868L;

	private final int senderClientId;

	private final List<Integer> receiversClientIds;

	private final String message;

	public RelayedTextMessage(String message, List<Integer> receiversClientIds, int senderClientId) {
		super();
		this.message = message;
		this.receiversClientIds = Collections.unmodifiableList(receiversClientIds);
		this.senderClientId = senderClientId;
	}

	public int getSenderClientId() {
		return senderClientId;
	}

	public List<Integer> getReceiversClientIds() {
		return receiversClientIds;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleRelayedTextMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((receiversClientIds == null) ? 0 : receiversClientIds.hashCode());
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
		if (!(obj instanceof RelayedTextMessage)) {
			return false;
		}
		RelayedTextMessage other = (RelayedTextMessage) obj;
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
		if (senderClientId != other.senderClientId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"RelayedTextMessage[getSenderClientId()=%s, getReceiversClientIds()=%s, getMessage()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getSenderClientId(), getReceiversClientIds(), getMessage(), getAllowedContext(), getCreatedAt());
	}

}