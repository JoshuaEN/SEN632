package ojdev.common.connections;

import java.io.IOException;
import ojdev.common.exceptions.InvalidMessageTypeException;
import ojdev.common.messages.MessageBase;

/**
 * Simple wrapper for Java connections.
 */
public abstract class Connection {

	public abstract void sendMessage(MessageBase message) throws IOException;

	public abstract MessageBase receiveMessage() throws InvalidMessageTypeException, IOException;

	public abstract boolean isClosed();

	public abstract void close() throws IOException;

}