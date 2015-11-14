package ojdev.common.messages.client;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ClientMessageHandler;
import ojdev.common.warriors.WarriorBase;

public class SetWarriorMessage extends ClientMessage {

	private static final long serialVersionUID = 2575501887388966137L;
	
	private final WarriorBase warrior;

	public SetWarriorMessage(WarriorBase warrior) {
		super();
		this.warrior = warrior;
	}

	public WarriorBase getWarrior() {
		return warrior;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((warrior == null) ? 0 : warrior.hashCode());
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
		if (!(obj instanceof SetWarriorMessage)) {
			return false;
		}
		SetWarriorMessage other = (SetWarriorMessage) obj;
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
	public void handleWith(ClientMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleSetWarriorMessage(this);
	}

	@Override
	public String toString() {
		return String.format("SetWarriorMessage[getWarrior()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getWarrior(), getAllowedContext(), getCreatedAt());
	}

}