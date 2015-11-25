package ojdev.client.ui.console;

import java.io.IOException;

import ojdev.client.Client;
import ojdev.client.ClientUserInterface;
import ojdev.common.connections.Connection;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.exceptions.InvalidMessageTypeException;
import ojdev.common.message_handlers.ServerMessageHandler;
import ojdev.common.messages.MessageBase;

class ClientWithLock extends Client {

	private final Object lock;
	
	public ClientWithLock(Connection connection, ServerMessageHandler serverMessageHandler,
			ClientUserInterface clientInterface, Object lock) {
		super(connection, serverMessageHandler, clientInterface);
		this.lock = lock;
	}

	public ClientWithLock(Connection connection, Object clientUI, Object lock) {
		super(connection, clientUI);
		this.lock = lock;
	}
	
	@Override
	public void listen() throws InvalidMessageTypeException, IOException, IllegalMessageContext {
		while(getConnection().isClosed() == false) {
			MessageBase message = getConnection().receiveMessage();

			synchronized (lock) {
				handleMessage(message);
			}
		}
	}

}
