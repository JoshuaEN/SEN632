package ojdev.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ojdev.common.ConnectedClientState;
import ojdev.common.SharedConstant;
import ojdev.common.connections.Connection;
import ojdev.common.connections.SocketConnection;
import ojdev.common.exceptions.InvalidClientId;
import ojdev.common.exceptions.WarriorAlreadyEngagedException;
import ojdev.common.messages.MessageBase;
import ojdev.common.messages.server.ServerTextMessage;
import ojdev.common.test.DebugMode;
import ojdev.server.ConnectedClient.DisconnectedException;
import ojdev.server.Engagement.WarriorNotReadyException;

/**
 * The coordinator and mediator for all ConnectedClients.
 */
public class Moderator {

	private final ConcurrentHashMap<Integer, ConnectedClient> connectedClients = new ConcurrentHashMap<Integer, ConnectedClient>();

	private int nextClientId = 0;

	private final int maxClients;
	
	private volatile boolean stop = false;
	
	private final ServerSocket serverSocket;
	
	private final Object createEngagementLock = new Object();
	private final ReadWriteLock shutdownLock = new ReentrantReadWriteLock(true); // Make it "fair" so that indefinite postponement cannot occur
	
	/**
	 *  
	 * @param maxClients the maximum number of clients who can be connected at one time
	 * @param listenPort the port to listen for connections on
	 * @param backlog the maximum number of clients who can be waiting to connect at one time
	 * @throws IOException if the underlying server connection cannot be created for any reason
	 */
	public Moderator(int maxClients, int listenPort, int backlog) throws IOException {
		this.maxClients = maxClients;
		this.serverSocket = new ServerSocket(listenPort, backlog);
	}

	public List<ConnectedClientState> getConnectedClientStates() {
		List<ConnectedClientState> connectedClientStates = new ArrayList<ConnectedClientState>(connectedClients.size());
			
		for(Entry<Integer, ConnectedClient> connectedClientSet : connectedClients.entrySet()) {
			connectedClientStates.add(connectedClientSet.getValue().toConnectedClientState());
		}
		
		return Collections.unmodifiableList(connectedClientStates);
	}
	
	public int getMaxClients() {
		return maxClients;
	}

	/**
	 * Starts listening for connections. Blocking.
	 * Can safely be run over several threads to increase concurrency, 
	 * though this may be of limited use though as some degree of synchronization is required
	 * 
	 * @throws IOException if an error occurs in the underlying connection when accepting the connection
	 */
	public void startServer() throws IOException {
		System.out.printf("Starting server, listening on %s:%s%n", serverSocket.getInetAddress(), serverSocket.getLocalPort());
		System.out.printf("Maximum Connections: %d%n", getMaxClients());
		
		while(serverSocket.isClosed() == false && isStopped() == false) {
			Connection connection;
			
			try {
				connection = new SocketConnection(serverSocket.accept());
			} catch(SocketException e) {
				// Since Java sends a generic SocketException if the socket is closed,
				// we need to check if that was the given message.
				if(serverSocket.isClosed() && e.getMessage().equals("socket closed")) {
					
					if(SharedConstant.DEBUG) {
						System.out.println("Moderator: NOTICE: Socket Closed: " + e);
					}
					
					stopServer();
					return;
				}
				
				throw e;
			}
			
			if(SharedConstant.DEBUG) {
				System.out.printf("Recieved New Connection %s%n", connection);
			}
			
			if(isStopped()) {
				connection.sendMessage(new ServerTextMessage("Shutting down."));
				connection.close();
				break;
			}
			
			int newClientId;
			
			synchronized (serverSocket) {
				if(getConnectedClientsCount() >= getMaxClients()) {
					System.err.printf("Moderator: WARN: Rejected Client Connection: Server Capacity of %d reached", getMaxClients());
					connection.sendMessage(new ServerTextMessage(String.format("Server Capacity of %d reached", getMaxClients())));
					connection.close();
					continue;
				}
				newClientId = nextClientId++;
			}
			
			ConnectedClient connectedClient = new ConnectedClient(this, connection, newClientId);
			addConnectedClient(connectedClient);
			
			if(SharedConstant.DEBUG) {
				System.out.printf("Added Client: %s%n", connectedClient.getClientId());
			}
			
			new Thread(new Runnable() {			
				@Override
				public void run() {
					connectedClient.listen();					
				}
			}).start();
		}
	}
	
	/**
	 * Stops the server.
	 * Notifies all connected clients they need to disconnect then closes the server socket.
	 * 
	 * @throws IOException if an error occurs in the underlying connection when closing it
	 */
	public void stopServer() throws IOException {
		shutdownLock.writeLock().lock();
		try {
			if(isStopped())
				return;
			
			stop = true;
		} finally {
			shutdownLock.writeLock().unlock();
		}
		
		for(Entry<Integer, ConnectedClient> connectedClientSet : connectedClients.entrySet()) {
			connectedClientSet.getValue().disconnect();
		}
		
		if(serverSocket != null && serverSocket.isClosed() == false) {
			serverSocket.close();
		}
	}

	/**
	 * Creates a new Engagement between the given client IDs
	 * 
	 * @param clientIds the client IDs of the combatants
	 * @param startedByClientId the client ID of the combatant who started the Engagement
	 * @throws WarriorAlreadyEngagedException if at least one of the given combatants is already engaged
	 * @throws WarriorNotReadyException if at least one of the given combatants is not ready
	 * @throws IllegalArgumentException if too few or too many combatants are specified
	 */
	public void createEngagementBetween(List<Integer> clientIds, int startedByClientId) throws WarriorAlreadyEngagedException, WarriorNotReadyException, IllegalArgumentException {
		List<EngagedWarrior> engagedWarriors = new ArrayList<EngagedWarrior>(clientIds.size());
		
		synchronized (createEngagementLock) {
			for(int clientId : clientIds) {
				ConnectedClient connectedClient = connectedClients.get(clientId);
				
				if(connectedClient.isEngaged()) {
					throw new WarriorAlreadyEngagedException();
				}
						
				engagedWarriors.add(connectedClient);
			}
			
			new Engagement(engagedWarriors, startedByClientId);
		}
	}
	
	/**
	 * Sends a message to the given ConnectedClient
	 * 
	 * @param connectedClient the connected client to send the message to
	 * @param message the message to send
	 */
	public void sendMessageTo(ConnectedClient connectedClient, MessageBase message) {
		if(isStopped())
			return;
		
		try {
			connectedClient.sendMessage(message);
		} catch (DisconnectedException | IOException e) {
			// Any exceptions are not of much, if any, concern.
			// They indicate the message could not be sent for whatever reason.
			// There is no recovery mechanism in place to rectify such an issue, thus there's nothing the Moderator can do.
			// Logged because eating exceptions without any reporting is rarely--if every--a good idea.
			System.err.printf("Moderator: WARN: Failed to send message %s to %s because: %s", message, connectedClient, e.getMessage());
			
			if(SharedConstant.DEBUG && SharedConstant.DEBUG_MODE == DebugMode.VERBOSE) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends a message to the given client ID
	 * 
	 * @param clientId the client ID of the client to send the message to
	 * @param message the message to send
	 * @throws InvalidClientId if the client ID isn't known
	 */
	public void sendMessageTo(int clientId, MessageBase message) throws InvalidClientId {
		ConnectedClient connectedClient = getConnectedClient(clientId);
		if(connectedClient == null) {
			throw new InvalidClientId(clientId);
		}
		
		sendMessageTo(connectedClient, message);
	}

	/**
	 * Sends a message to the given client IDs
	 * 
	 * @param clientIds the client IDs of the clients to send the message to
	 * @param message the message to send
	 * @throws InvalidClientId if one or more of the client IDs aren't known
	 */
	public void sendMessageTo(List<Integer> clientIds, MessageBase message) throws InvalidClientId {
		for(ConnectedClient connectedClient : getConnectedClientsSubset(clientIds)) {
			sendMessageTo(connectedClient, message);
		}
	}
	
	/**
	 * Sends a message to all clients
	 * 
	 * @param message the message to send
	 */
	public void sendMessageToAll(MessageBase message) {
		for(Entry<Integer, ConnectedClient> connectedClientSet : connectedClients.entrySet()) {
			sendMessageTo(connectedClientSet.getValue(), message);
		}
	}
	
	private List<ConnectedClient> getConnectedClientsSubset(List<Integer> clientIds) throws InvalidClientId {
		List<ConnectedClient> connectedClients = new ArrayList<ConnectedClient>(clientIds.size());
		
		for(int clientId : clientIds) {
			ConnectedClient connectedClient = getConnectedClient(clientId);
			
			if(connectedClient == null) {
				throw new InvalidClientId(clientId);
			}
			
			connectedClients.add(connectedClient);
		}
		
		return connectedClients;
	}

	/**
	 * Used by ConnectedClients to notify the Moderator the connection was closed for some reason.
	 * 
	 * @param connectedClient the ConnectedClient which had its connection closed
	 */
	public void connectionClosed(ConnectedClient connectedClient) {
		removeConnectedClient(connectedClient);
	}

	public boolean isStopped() {
		return stop;
	}
	
	public int getMaxConnectedClients() {
		return maxClients;
	}
	
	public int getConnectedClientsCount() {
		return connectedClients.size();
	}

	private void addConnectedClient(ConnectedClient connectedClient) {
		/* Ensure that, during shutdown, either the connectedClient is added to the list
		 * before the server is stopped, or the connectedClient is disconnected.
		 */
		shutdownLock.readLock().lock();
		try {
			if(isStopped()) {
				connectedClient.disconnect();
			} else {
				connectedClients.put(connectedClient.getClientId(), connectedClient);
			}
		} finally {
			shutdownLock.readLock().unlock();
		}
	}

	private void removeConnectedClient(ConnectedClient connectedClient) {
		connectedClients.remove(connectedClient.getClientId());
	}
	
	private ConnectedClient getConnectedClient(int clientId){
		return connectedClients.get(clientId);
	}

	@Override
	public String toString() {
		return String.format(
				"Moderator[serverSocket=%s, getConnectedClientStates()=%s, getMaxClients()=%s, isStopped()=%s, getMaxConnectedClients()=%s, getConnectedClientsCount()=%s]",
				serverSocket, getConnectedClientStates(), getMaxClients(), isStopped(), getMaxConnectedClients(),
				getConnectedClientsCount());
	}

}