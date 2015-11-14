package ojdev.common.messages;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.MessageHandler;

public class InvalidMessage extends AgnosticMessage {

	private static final long serialVersionUID = 5334166850460225231L;

	private final MessageBase message;

	private final AllowedMessageContext attemptedContext;

	private final String reason;

	public InvalidMessage(MessageBase message, AllowedMessageContext attemptedContext, String reason) {
		super();
		this.message = message;
		this.attemptedContext = attemptedContext;
		this.reason = reason;
	}

	public MessageBase getMessage() {
		return message;
	}

	public AllowedMessageContext getAttemptedContext() {
		return attemptedContext;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public void handleWith(MessageHandler handler) throws IllegalMessageContext {
		checkHandler(handler);
		handler.handleInvalidMessage(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((attemptedContext == null) ? 0 : attemptedContext.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
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
		if (!(obj instanceof InvalidMessage)) {
			return false;
		}
		InvalidMessage other = (InvalidMessage) obj;
		if (attemptedContext != other.attemptedContext) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (reason == null) {
			if (other.reason != null) {
				return false;
			}
		} else if (!reason.equals(other.reason)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"InvalidMessage[getMessage()=%s, getAttemptedContext()=%s, getReason()=%s, getAllowedContext()=%s, getCreatedAt()=%s]",
				getMessage(), getAttemptedContext(), getReason(), getAllowedContext(), getCreatedAt());
	}

}