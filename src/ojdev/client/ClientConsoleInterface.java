package ojdev.client;

import java.io.IOException;
import java.net.Socket;

import ojdev.common.SharedConstant;
import ojdev.common.connections.SocketConnection;
import ojdev.common.message_handlers.ServerMessageHandler;
import ojdev.common.messages.InvalidMessage;
import ojdev.common.messages.server.*;

// TODO Code
public class ClientConsoleInterface implements ServerMessageHandler, ClientUserInterface {

	private Client client;
	private Thread clientThread;
	
	public void example() {
		// Example Code
		/*
		 * This is an example of code for establishing a connection,
		 * and then listening for messages from the server.
		 */
		try {
			/* 
			 * Create a new client interface,
			 * giving it a Connection created with input from the user,
			 * and a reference to this so it can relay messages.
			 */
			client = new Client(
					new SocketConnection(new Socket("user_provided_host", SharedConstant.DEFAULT_PORT)), 
					this
			);
		} catch (IOException e) {
			// Notify the user via some UI feature
		}
		
		/*
		 * Create a new thread, passing in the instance of Client
		 * This works because Client implements Runnable
		 */
		clientThread = new Thread(client);
		
		client.safeListen();
		/*
		 * Start the thread
		 * This will call client.safeListen() in a new thread.
		 * This means the client will start listening for messages from
		 * the server, and upon receiving one it will take any logic related
		 * action it needs to, then notify the Interface via the
		 * common ServerMessageHandler interface.
		 * 
		 * If an exception occurs, the Client instance will
		 * notify the Interface via the ClientUserInterface interface.
		 * 
		 * It is up to the UI code to determine what to do in any of
		 * those cases, the Client instance ultimately is just there to
		 * help.
		 */
		clientThread.start();
	}
	
	@Override
	public void notifyException(Exception e) {
		
	}
	
	@Override
	public void handleInvalidMessage(InvalidMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRelayedTextMessage(RelayedTextMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleServerTextMessage(ServerTextMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleSetClientIdMessage(SetClientIdMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleConnectedClientsListMessage(ConnectedClientsListMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEngagementCombatResultMessage(EngagementCombatResultMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEngagementStartedMessage(EngagementStartedMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEngagementEndedMessage(EngagementEndedMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEngagementActionSelectedMessage(EngagementActionSelectedMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleClientConnectedMessage(ClientConnectedMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleClientDisconnectedMessage(ClientDisconnectedMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleClientStateChangedMessage(ClientStateChangedMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRelayedTextToAllMessage(RelayedTextToAllMessage message) {
		// TODO Auto-generated method stub
		
	}

}