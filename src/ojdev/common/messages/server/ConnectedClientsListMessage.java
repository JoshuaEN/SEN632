package ojdev.common.messages.server;

import java.util.Collections;
import java.util.List;

import ojdev.common.ConnectedClientState;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class ConnectedClientsListMessage extends ServerMessage {

	private static final long serialVersionUID = 5961505182453021117L;
	
	private final List<ConnectedClientState> warriors;

	public ConnectedClientsListMessage(List<ConnectedClientState> warriors) {
		super();
		this.warriors = Collections.unmodifiableList(warriors);
	}

	public List<ConnectedClientState> getWarriors() {
		return warriors;
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleConnectedClientsListMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((warriors == null) ? 0 : warriors.hashCode());
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
		if (!(obj instanceof ConnectedClientsListMessage)) {
			return false;
		}
		ConnectedClientsListMessage other = (ConnectedClientsListMessage) obj;
		if (warriors == null) {
			if (other.warriors != null) {
				return false;
			}
		} else if (!warriors.equals(other.warriors)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("ConnectedClientsListMessage[getWarriors()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getWarriors(), getAllowedContext(), getCreatedAt());
	}

}