package ojdev.common;

import java.io.Serializable;

import ojdev.common.actions.Action;

/**
 * Represents a single selected action by an engaged Warrior.
 */
public class SelectedAction implements Serializable {

	private static final long serialVersionUID = 7877278732631185643L;

	private final Action action;

	private final int targetClientId;

	private final int clientId;

	public SelectedAction(int clientId, int targetClientId, Action action) {
		this.clientId = clientId;
		this.targetClientId = targetClientId;
		this.action = action;
	}

	public Action getAction() {
		return action;
	}

	public int getTargetClientId() {
		return targetClientId;
	}

	public int getClientId() {
		return clientId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + clientId;
		result = prime * result + targetClientId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SelectedAction)) {
			return false;
		}
		SelectedAction other = (SelectedAction) obj;
		if (action == null) {
			if (other.action != null) {
				return false;
			}
		} else if (!action.equals(other.action)) {
			return false;
		}
		if (clientId != other.clientId) {
			return false;
		}
		if (targetClientId != other.targetClientId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("SelectedAction[getAction()=%s, getTargetClientId()=%s, getClientId()=%s]", getAction(),
				getTargetClientId(), getClientId());
	}

}