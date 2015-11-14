package ojdev.common.messages.server;

import java.util.Collections;
import java.util.List;

import ojdev.common.WarriorCombatResult;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;

public class EngagementCombatResultMessage extends EngagementMessage {

	private static final long serialVersionUID = -7185077846965184893L;
	
	private final List<WarriorCombatResult> warriorCombatResults;

	public EngagementCombatResultMessage(List<WarriorCombatResult> warriorCombatResults) {
		super();
		this.warriorCombatResults = Collections.unmodifiableList(warriorCombatResults);
	}

	public List<WarriorCombatResult> getWarriorCombatResults() {
		return warriorCombatResults;
	}

	@Override
	public void handleWith(ServerMessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleEngagementCombatResultMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((warriorCombatResults == null) ? 0 : warriorCombatResults.hashCode());
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
		if (!(obj instanceof EngagementCombatResultMessage)) {
			return false;
		}
		EngagementCombatResultMessage other = (EngagementCombatResultMessage) obj;
		if (warriorCombatResults == null) {
			if (other.warriorCombatResults != null) {
				return false;
			}
		} else if (!warriorCombatResults.equals(other.warriorCombatResults)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"EngagementCombatResultMessage[getWarriorCombatResults()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getWarriorCombatResults(), getAllowedContext(), getCreatedAt());
	}

}