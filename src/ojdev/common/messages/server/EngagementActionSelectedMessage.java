package ojdev.common.messages.server;

import ojdev.common.SelectedAction;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

/**
 * 
 */
public class EngagementActionSelectedMessage extends EngagementMessage {

	private static final long serialVersionUID = -1714095846446947778L;

	private final SelectedAction selectedAction;

	public EngagementActionSelectedMessage(SelectedAction selectedAction) {
		super();
		this.selectedAction = selectedAction;
	}

	public SelectedAction getSelectedAction() {
		return selectedAction;
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleEngagementActionSelectedMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((selectedAction == null) ? 0 : selectedAction.hashCode());
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
		if (!(obj instanceof EngagementActionSelectedMessage)) {
			return false;
		}
		EngagementActionSelectedMessage other = (EngagementActionSelectedMessage) obj;
		if (selectedAction == null) {
			if (other.selectedAction != null) {
				return false;
			}
		} else if (!selectedAction.equals(other.selectedAction)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"EngagementActionSelectedMessage[getSelectedAction()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getSelectedAction(), getAllowedContext(), getCreatedAt());
	}

}