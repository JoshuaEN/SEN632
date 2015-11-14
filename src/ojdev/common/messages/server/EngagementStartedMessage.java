package ojdev.common.messages.server;

import java.util.Collections;
import java.util.List;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class EngagementStartedMessage extends EngagementMessage {

	private static final long serialVersionUID = 9096634434893890760L;

	private final int startedByClientId;

	private final List<Integer> involvedClientIds;

	public EngagementStartedMessage(int startedByClientId, List<Integer> involvedClientIds) {
		super();
		this.startedByClientId = startedByClientId;
		this.involvedClientIds = Collections.unmodifiableList(involvedClientIds);
	}

	public int getStartedByClientId() {
		return startedByClientId;
	}

	public List<Integer> getInvolvedClientIds() {
		return involvedClientIds;
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleEngagementStartedMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((involvedClientIds == null) ? 0 : involvedClientIds.hashCode());
		result = prime * result + startedByClientId;
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
		if (!(obj instanceof EngagementStartedMessage)) {
			return false;
		}
		EngagementStartedMessage other = (EngagementStartedMessage) obj;
		if (involvedClientIds == null) {
			if (other.involvedClientIds != null) {
				return false;
			}
		} else if (!involvedClientIds.equals(other.involvedClientIds)) {
			return false;
		}
		if (startedByClientId != other.startedByClientId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"EngagementStartedMessage[getStartedByClientId()=%s, getInvolvedClientIds()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getStartedByClientId(), getInvolvedClientIds(), getAllowedContext(), getCreatedAt());
	}

}