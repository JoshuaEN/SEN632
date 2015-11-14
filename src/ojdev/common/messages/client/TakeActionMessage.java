package ojdev.common.messages.client;

import ojdev.common.actions.Action;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ClientMessageHandler;

public class TakeActionMessage extends ClientMessage {

	private static final long serialVersionUID = -105431914454106899L;

	private final Action action;

	private final int targetClientId;

	public TakeActionMessage(Action action, int targetClientId) {
		super();
		this.action = action;
		this.targetClientId = targetClientId;
	}

	public Action getAction() {
		return action;
	}

	public int getTargetClientId() {
		return targetClientId;
	}

	@Override
	public void handleWith(ClientMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleTakeActionMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + targetClientId;
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
		if (!(obj instanceof TakeActionMessage)) {
			return false;
		}
		TakeActionMessage other = (TakeActionMessage) obj;
		if (action == null) {
			if (other.action != null) {
				return false;
			}
		} else if (!action.equals(other.action)) {
			return false;
		}
		if (targetClientId != other.targetClientId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"TakeActionMessage[getAction()=%s, getTargetClientId()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getAction(), getTargetClientId(), getAllowedContext(), getCreatedAt());
	}

}