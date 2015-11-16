package ojdev.common.exceptions;

import ojdev.common.messages.MessageBase;

public class InvalidMessageTypeException extends Exception {

	private static final long serialVersionUID = 5869817016936087227L;

	public InvalidMessageTypeException(Object object) {
		super(String.format("Invalid Message Type of %s, expected %s", object.getClass(), MessageBase.class));
	}
	
	public InvalidMessageTypeException(String message) {
		super(message);
	}
	
	public InvalidMessageTypeException() {
		this("Invalid Message Type");
	}

	public InvalidMessageTypeException(String message, Throwable e) {
		super(message, e);
	}
}
