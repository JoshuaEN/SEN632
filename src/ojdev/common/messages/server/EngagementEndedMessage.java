package ojdev.common.messages.server;

import java.util.Collections;
import java.util.List;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class EngagementEndedMessage extends EngagementMessage {

	private static final long serialVersionUID = -2195466498587007652L;
	
	public final List<Integer> involvedClientIds;

	public EngagementEndedMessage(List<Integer> involvedClientIds) {
		super();
		this.involvedClientIds = Collections.unmodifiableList(involvedClientIds);
	}

	public List<Integer> getInvolvedClientIds() {
		return involvedClientIds;
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleEngagementEndedMessage(this);		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((involvedClientIds == null) ? 0 : involvedClientIds.hashCode());
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
		if (!(obj instanceof EngagementEndedMessage)) {
			return false;
		}
		EngagementEndedMessage other = (EngagementEndedMessage) obj;
		if (involvedClientIds == null) {
			if (other.involvedClientIds != null) {
				return false;
			}
		} else if (!involvedClientIds.equals(other.involvedClientIds)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"EngagementEndedMessage[getInvolvedClientIds()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getInvolvedClientIds(), getAllowedContext(), getCreatedAt());
	}

}