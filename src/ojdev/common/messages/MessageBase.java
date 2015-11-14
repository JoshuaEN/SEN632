package ojdev.common.messages;

import java.util.Date;

import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.MessageHandler;

/**
 * Abstract base class for Messages sent between the Client and Server.
 */
public abstract class MessageBase implements java.io.Serializable {

	private static final long serialVersionUID = 3298013600682733116L;
	
	private final Date createdAt;

	public MessageBase() {
		this.createdAt = new Date();
	}

	public abstract AllowedMessageContext getAllowedContext();

	public Date getCreatedAt() {
		return createdAt;
	}

	public abstract void handleWith(MessageHandler handler) throws IllegalMessageContext;
	
	/**
	 * Checks that handler's context is compatible with the Message.
	 * 
	 * @param handler the handler to check the compatibility of
	 * @throws IllegalMessageContext if the context of the Handler is not compatible with the Message
	 */
	protected final void checkHandler(MessageHandler handler) throws IllegalMessageContext {
		if(handler.getAllowedContext() != getAllowedContext() && 
				getAllowedContext() != AllowedMessageContext.Either) {
			throw new IllegalMessageContext(String.format("Handler's Expected Context of %s does not match Message Allowed Context of %s", handler.getAllowedContext(), getAllowedContext()));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
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
		if (!(obj instanceof MessageBase)) {
			return false;
		}
		MessageBase other = (MessageBase) obj;
		if (createdAt == null) {
			if (other.createdAt != null) {
				return false;
			}
		} else if (!createdAt.equals(other.createdAt)) {
			return false;
		}
		return true;
	}
}