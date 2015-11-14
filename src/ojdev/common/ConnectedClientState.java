package ojdev.common;

import java.util.Date;

import ojdev.common.warriors.WarriorBase;

/**
 * Represents useful internal state information about the ConnectedClient.
 * This is an inner class specifically to help ensure only ConnectedClient directly
 * creates instances of this class using the toConnectedClientState method.
 *
 */
public class ConnectedClientState implements java.io.Serializable {

	private static final long serialVersionUID = -850216526734974513L;

	private final int clientId;

	private final WarriorBase warrior;

	private final boolean inEngagement;
	
	private final Date createdAt;

	public ConnectedClientState(int clientId , WarriorBase warrior, Boolean inEngagement) {
		this.clientId = clientId;
		this.warrior = warrior;
		this.inEngagement = inEngagement;
		this.createdAt = new Date();
	}

	public int getClientId() {
		return clientId;
	}

	public WarriorBase getWarrior() {
		return warrior;
	}

	public boolean isInEngagement() {
		return inEngagement;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + clientId;
		result = prime * result + (inEngagement ? 1231 : 1237);
		result = prime * result + ((warrior == null) ? 0 : warrior.hashCode());
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
		if (!(obj instanceof ConnectedClientState)) {
			return false;
		}
		ConnectedClientState other = (ConnectedClientState) obj;
		if (clientId != other.clientId) {
			return false;
		}
		if (inEngagement != other.inEngagement) {
			return false;
		}
		if (warrior == null) {
			if (other.warrior != null) {
				return false;
			}
		} else if (!warrior.equals(other.warrior)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"ConnectedClientState[getClientId()=%s, getWarrior()=%s, isInEngagement()=%s, getCreatedAt()=%s]",
				getClientId(), getWarrior(), isInEngagement(), getCreatedAt());
	}
}