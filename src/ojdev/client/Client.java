package ojdev.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ojdev.common.connections.Connection;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.exceptions.InvalidMessageTypeException;
import ojdev.common.message_handlers.ServerMessageHandler;
import ojdev.common.messages.InvalidMessage;
import ojdev.common.messages.MessageBase;
import ojdev.common.messages.server.*;
import ojdev.common.warriors.WarriorBase;
import ojdev.common.ConnectedClientState;
import ojdev.common.SelectedAction;

/**
 * Helper class for UI Clients; provides generic logic not related to displaying information.
 */
public class Client implements Runnable, ServerMessageHandler {

	private final Connection connection;

	private final ServerMessageHandler serverMessageHandler;
	
	private final ClientUserInterface clientInterface;

	private volatile int currentClientId = -1;

	private volatile WarriorBase currentWarrior;

	private volatile Map<Integer, ConnectedClientState> connectedClients = new HashMap<Integer, ConnectedClientState>();
	
	private volatile Map<Integer, Date> connectedClientsRemovalList = new HashMap<Integer, Date>();
	
	private volatile Integer engagementStartedBy = null;
	
	private volatile List<Integer> engagementParticipantClientIds = null;
	
	private volatile Map<Integer, SelectedAction> engagementCurrentlySelectedActions = null;

	/**
	 * @param connection 
	 * @param serverMessageHandler
	 */
	public Client(Connection connection, ServerMessageHandler serverMessageHandler, ClientUserInterface clientInterface) {
		this.connection = connection;
		this.serverMessageHandler = serverMessageHandler;
		this.clientInterface = clientInterface;
	}
	
	public Client(Connection connection, Object clientUI) {
		// One would, logically, check these types but, alas, Java insists the
		// constructor be the first statement so.
		this(connection, (ServerMessageHandler)clientUI, (ClientUserInterface)clientUI);
	}
	
	@Override
	public void run() {
		safeListen();
	}
	
	/**
	 * Notifies specified clientInterface of any exceptions instead
	 * of passing the exception upwards.
	 * @throws IllegalArgumentException if clientInterface is null
	 */
	public void safeListen() throws IllegalArgumentException {
		
		// Ensure we don't allow exceptions to be swallowed accidently.
		if(clientInterface == null) {
			throw new IllegalArgumentException("A Client Interface MUST be specified to use this method. Use listen() instead to avoid this requirement."); 
		}
		
		try {
			listen();
		} catch (InvalidMessageTypeException | IOException | IllegalMessageContext e) {
			notifyException(e);
		}
	}
	
	public void listen() throws InvalidMessageTypeException, IOException, IllegalMessageContext {
		while(connection.isClosed() == false) {
			MessageBase message = connection.receiveMessage();

			handleMessage(message);
		}
	}
	
	protected void handleMessage(MessageBase message) throws IllegalMessageContext {
		message.handleWith(this);

		if(serverMessageHandler != null) {
			message.handleWith(serverMessageHandler);
		}
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public int getClientId() {
		return currentClientId;
	}
	
	private void SetClientId(int clientId) {
		this.currentClientId = clientId;
	}

	public WarriorBase getCurrentWarrior() {
		return currentWarrior;
	}

	private void setCurrentWarrior(WarriorBase currentWarrior) {
		this.currentWarrior = currentWarrior;
		
		notifyCurrentWarriorChanged(currentWarrior);
	}
	
	protected void notifyCurrentWarriorChanged(WarriorBase currentWarrior) {
		if(clientInterface != null) {
			clientInterface.notifyCurrentWarriorChanged(currentWarrior);
		}
	}
	
	protected void notifyException(Exception e) {
		if(clientInterface != null) {
			clientInterface.notifyException(e);
		}
	}

	public Map<Integer, ConnectedClientState> getConnectedClientsMap() {
		return Collections.unmodifiableMap(connectedClients);
	}
	
	public List<ConnectedClientState> getConnectedClientsList() {
		return Collections.unmodifiableList(new ArrayList<ConnectedClientState>(connectedClients.values()));
	}
	
	public ConnectedClientState getConnectedClientById(int clientId) {
		return connectedClients.get(clientId);
	}
	
	protected ServerMessageHandler getExternalMessageHandler() {
		return serverMessageHandler;
	}
	
	protected ClientUserInterface getUserInterface() {
		return clientInterface;
	}
	
	private void setConnectedClientById(int clientId, ConnectedClientState connectedClientState) {
		connectedClients.put(clientId, connectedClientState);
		if(clientId == getClientId()) {
			setCurrentWarrior(connectedClientState.getWarrior());
		}
	}
	
	private void removeConnectedClientById(int clientId, ConnectedClientState connectedClientState) {
		connectedClientsRemovalList.put(clientId, connectedClientState.getCreatedAt());
		connectedClients.remove(clientId);
	}

	private void setConnectedClients(List<ConnectedClientState> newConnectedClientsList) {
		// keySet returns an unmodifiable Set, we need to modify it so we create a new Set based on it
		Set<Integer> existingRemovedClients = new HashSet<Integer>(getConnectedClientsMap().keySet());
		
		for(ConnectedClientState connectedClientState : newConnectedClientsList) {
			ConnectedClientState currentConnectedClientState = getConnectedClientById(connectedClientState.getClientId());
			
			if(checkUpdateCurrentConnectedClientAction(currentConnectedClientState, connectedClientState)) {
				setConnectedClientById(connectedClientState.getClientId(), connectedClientState);
			}
			
			existingRemovedClients.remove(connectedClientState.getClientId());
		}
		
		for(int clientId : existingRemovedClients) {
			removeConnectedClientById(clientId, getConnectedClientById(clientId));
		}
	}
	
	private void addConnectedClient(ConnectedClientState newConnectedClientState) {
		updateConnectedClient(newConnectedClientState);
	}
	
	private void removeConnectedClient(ConnectedClientState newConnectedClientState) {
		ConnectedClientState currentConnectedClientState = getConnectedClientById(newConnectedClientState.getClientId());
		
		if(checkUpdateCurrentConnectedClientAction(currentConnectedClientState, newConnectedClientState)) {
			removeConnectedClientById(newConnectedClientState.getClientId(), newConnectedClientState);
		}
	}
	
	private void updateConnectedClient(ConnectedClientState newConnectedClientState) {
		ConnectedClientState currentConnectedClientState = getConnectedClientById(newConnectedClientState.getClientId());
		
		if(checkUpdateCurrentConnectedClientAction(currentConnectedClientState, newConnectedClientState)) {
			setConnectedClientById(newConnectedClientState.getClientId(), newConnectedClientState);
		}
	}
	
	/**
	 * 
	 * @param existing the existing client state to compare, may be null
	 * @param prospect the prospective client state to compare, may not be null
	 * @return if the prospective client state should replace the existing client state
	 */
	private boolean checkUpdateCurrentConnectedClientAction(ConnectedClientState existing, ConnectedClientState prospect) {
		Date comparePoint;
		if(existing == null) {
			comparePoint = connectedClientsRemovalList.get(prospect.getClientId());
		} else {
			comparePoint = existing.getCreatedAt();
		}
		
		if(comparePoint == null) {
			return true;
		}
		
		// If the dates are the same, defer to the order they were received (last received = newest)
		if(prospect.getCreatedAt().before(comparePoint) == false) {
			return true;
		}
		
		return false;
	}
	
	public Integer getEngagementStartedByClientId() {
		return engagementStartedBy;
	}
	
	private void setEngagementStartedByClientId(Integer engagementStartedBy) {
		this.engagementStartedBy = engagementStartedBy;
	}
	
	public List<Integer> getEngagementParticipantClientIds() {
		return engagementParticipantClientIds;
	}
	
	private void setEngagementParticipantClientIds(List<Integer> engagementParticipantClientIds) {
		this.engagementParticipantClientIds = engagementParticipantClientIds;
	}
	
	public Map<Integer, SelectedAction> getEngagementCurrentlySelectedActions() {
		return engagementCurrentlySelectedActions;
	}
	
	private void setEngagementCurrentlySelectedAction(SelectedAction selectedAction) {
		engagementCurrentlySelectedActions.put(selectedAction.getClientId(), selectedAction);
	}
	
	private void initEngagementCurrentlySelectedActions() {
		engagementCurrentlySelectedActions = new HashMap<Integer, SelectedAction>();
	}
	
	private void clearEngagementCurrentlySelectedActions() {
		engagementCurrentlySelectedActions.clear();
	}
	
	private void nullEngagementCurrentlySelectedActions() {
		engagementCurrentlySelectedActions = null;
	}
	
	public boolean isInEngagement() {
		return engagementCurrentlySelectedActions != null &&
				engagementParticipantClientIds != null &&
				engagementStartedBy != null;
	}
	
	public boolean isActionSelected() {
		if(engagementCurrentlySelectedActions == null) {
			return false;
		}
		
		for(Entry<Integer, SelectedAction> entry : engagementCurrentlySelectedActions.entrySet()) {
			if(entry.getKey() == getClientId()) {
				if(entry.getValue() == null) {
					return false;
				} else {
					return true;
				}
			}
		}
		
		return false;
	}

	public void sendMessage(MessageBase message) throws IOException {
		connection.sendMessage(message);
	}

	@Override
	public void handleInvalidMessage(InvalidMessage message) {
		// NOOP
	}

	@Override
	public void handleRelayedTextMessage(RelayedTextMessage message) {
		// NOOP
	}

	@Override
	public void handleServerTextMessage(ServerTextMessage message) {
		// NOOP
	}

	@Override
	public void handleSetClientIdMessage(SetClientIdMessage message) {
		SetClientId(message.getClientId());
	}

	@Override
	public void handleConnectedClientsListMessage(ConnectedClientsListMessage message) {
		setConnectedClients(message.getWarriors());
	}

	@Override
	public void handleEngagementCombatResultMessage(EngagementCombatResultMessage message) {
		clearEngagementCurrentlySelectedActions();
	}

	@Override
	public void handleEngagementStartedMessage(EngagementStartedMessage message) {
		setEngagementStartedByClientId(message.getStartedByClientId());
		setEngagementParticipantClientIds(message.getInvolvedClientIds());
		initEngagementCurrentlySelectedActions();
	}

	@Override
	public void handleEngagementEndedMessage(EngagementEndedMessage message) {
		setEngagementStartedByClientId(null);
		setEngagementParticipantClientIds(null);
		nullEngagementCurrentlySelectedActions();
	}

	@Override
	public void handleEngagementActionSelectedMessage(EngagementActionSelectedMessage message) {
		setEngagementCurrentlySelectedAction(message.getSelectedAction());
	}

	@Override
	public void handleClientConnectedMessage(ClientConnectedMessage message) {
		addConnectedClient(message.getConnectedClientState());
	}

	@Override
	public void handleClientDisconnectedMessage(ClientDisconnectedMessage message) {
		removeConnectedClient(message.getConnectedClientState());
	}

	@Override
	public void handleClientStateChangedMessage(ClientStateChangedMessage message) {
		updateConnectedClient(message.getConnectedClientState());
	}

	@Override
	public void handleRelayedTextToAllMessage(RelayedTextToAllMessage message) {
		// NOOP
	}
}