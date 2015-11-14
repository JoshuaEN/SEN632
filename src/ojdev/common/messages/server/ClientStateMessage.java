package ojdev.common.messages.server;

import ojdev.common.ConnectedClientState;

public abstract class ClientStateMessage extends ServerMessage {

	private static final long serialVersionUID = -3992436621835357915L;
	
	private final ConnectedClientState connectedClientState;
	
	public ClientStateMessage(ConnectedClientState connectedClientState) {
		this.connectedClientState = connectedClientState;
	}

	public ConnectedClientState getConnectedClientState() {
		return connectedClientState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((connectedClientState == null) ? 0 : connectedClientState.hashCode());
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
		if (!(obj instanceof ClientStateMessage)) {
			return false;
		}
		ClientStateMessage other = (ClientStateMessage) obj;
		if (connectedClientState == null) {
			if (other.connectedClientState != null) {
				return false;
			}
		} else if (!connectedClientState.equals(other.connectedClientState)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"ClientStateMessage[getConnectedClientState()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getConnectedClientState(), getAllowedContext(), getCreatedAt());
	}
	
}
