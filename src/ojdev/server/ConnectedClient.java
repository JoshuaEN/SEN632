package ojdev.server;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import ojdev.common.ConnectedClientState;
import ojdev.common.SelectedAction;
import ojdev.common.SharedConstant;
import ojdev.common.WarriorCombatResult;
import ojdev.common.connections.Connection;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.exceptions.InvalidClientId;
import ojdev.common.exceptions.InvalidMessageTypeException;
import ojdev.common.exceptions.WarriorAlreadyEngagedException;
import ojdev.common.messages.*;
import ojdev.common.messages.client.*;
import ojdev.common.messages.server.*;
import ojdev.common.test.DebugMode;
import ojdev.common.warriors.WarriorBase;
import ojdev.server.Engagement.InvalidActionSelectionException;
import ojdev.server.Engagement.WarriorActionAlreadySelectedException;
import ojdev.server.Engagement.WarriorNotReadyException;

/**
 * This is the link between a Client and the rest of the Server. It handles Client Messages, talking to the Moderator or Engagement when needed.
 */
public class ConnectedClient implements ojdev.common.message_handlers.ClientMessageHandler, EngagedWarrior {

	private final int clientId;

	private final Connection connection;

	private volatile WarriorBase warrior;

	private final Moderator moderator;

	private volatile Engagement currentEngagement;
	
	private volatile boolean listening = false;
	
	private volatile boolean disconnected = false;

	private final Object engagementLock = new Object();
	private final Object toConnectedClientStateLock = new Object();

	/**
	 * 
	 * @param moderator the moderator which manages this
	 * @param connection the connection to the client
	 * @param clientId the client ID of the client
	 */
	public ConnectedClient(Moderator moderator, Connection connection, int clientId) {
		super();
		this.moderator = moderator;
		this.connection = connection;
		this.clientId = clientId;
		
		safeSendMessage(new SetClientIdMessage(getClientId()));
		getModerator().sendMessageToAll(new ClientConnectedMessage(toConnectedClientState()));
	}
	
	public int getClientId() {
		return clientId;
	}

	public WarriorBase getWarrior() {
		return warrior;
	}
	
	private void setWarrior(WarriorBase warriorBase) {
		this.warrior = warriorBase;
		getModerator().sendMessageToAll(new ClientStateChangedMessage(toConnectedClientState()));
	}

	public boolean isEngaged() {
		return currentEngagement == null ? false : true;
	}

	public Engagement getCurrentEngagement() {
		return currentEngagement;
	}
	
	private void setCurrentEngagement(Engagement engagement) {
		this.currentEngagement = engagement;
		getModerator().sendMessageToAll(new ClientStateChangedMessage(toConnectedClientState()));
	}
	
	private boolean isListening() {
		return listening;
	}
	
	private boolean isDisconnected() {
		return disconnected;
	}
	
	private Moderator getModerator() {
		return moderator;
	}

	/**
	 * Starts the ConnectedClient listening for messages from the Client. Blocking.
	 */
	public void listen() {
		synchronized (connection) {
			if(isListening() || isDisconnected()) {
				return;
			}
			
			listening = true;
		}
		
		while(connection.isClosed() == false && isDisconnected() == false) {
			
			MessageBase message;
			
			try {
				message = connection.receiveMessage();
			} catch (InvalidMessageTypeException e) {
				System.err.println("Connection: FATAL: Client Sent an invalid message: " + e);
				
				if(SharedConstant.DEBUG) {
					e.printStackTrace();
				}
				
				disconnect();
				break;
			} catch (EOFException e) {
				if(SharedConstant.DEBUG) {
					System.out.println("Connection: NOTICE: EOF received: " + e);
				}
				disconnect();
				break;
			} catch (SocketException e) {
				if(connection.isClosed() && e.getMessage().equals("socket closed")) {
					System.out.println("Connection: NOTICE: Socket Closed: " + e);
				}
				disconnect();
				break;
			} catch (IOException e) {
				System.err.println("Connection: FATAL: IO Error receiving message: " + e);
				
				if(SharedConstant.DEBUG) {
					e.printStackTrace();
				}
				
				disconnect();
				break;
			}
			
			try {
				message.handleWith(this);
			} catch (IllegalMessageContext e) {
				System.err.println("Connection: ERROR: Client Sent an message with an invalid conext: " + e);
				
				if(SharedConstant.DEBUG) {
					e.printStackTrace();
				}
				
				safeSendMessage(new InvalidMessage(message, getAllowedContext(), e.getMessage()));
			}
		}
	}

	/**
	 * Sends the given message to the client.
	 * 
	 * @param message the message to send
	 * @throws DisconnectedException if the client is disconnected
	 * @throws IOException if an error occurs with the underlying connection
	 */
	public void sendMessage(MessageBase message) throws DisconnectedException, IOException {
		
		if(isDisconnected())
			throw new DisconnectedException();
		
		try {
			connection.sendMessage(message);
		} catch (IOException e) {
			// The state of the connection may be in an undefined state after an exception, disconnect.
			disconnect();
			
			throw e;
		}
		
	}
	
	/**
	 * Handles possible exceptions from sendMessage gracefully.
	 * Private so as to require external calls to acknowledge the possibility of errors.
	 * 
	 * @param message the message to send
	 * @return indication of if the message was successfully sent.
	 */
	private boolean safeSendMessage(MessageBase message) {
		try {
			sendMessage(message);
			return true;
		} catch (DisconnectedException e) {
			System.err.println("Connection: WARN: Failed to send message: " + message + " because: " + e);
			if(SharedConstant.DEBUG && SharedConstant.DEBUG_MODE == DebugMode.VERBOSE) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			if(SharedConstant.DEBUG && SharedConstant.DEBUG_MODE == DebugMode.VERBOSE) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Disconnects the Client and informs the Current Engagement and Moderator.
	 */
	public void disconnect() {
		synchronized (connection) {
			if(isDisconnected())
				return;
			
			disconnected = true;
		}
		
		if(connection.isClosed() == false) {
			try {
				connection.close();
			} catch (IOException e) {
				System.err.println("Connection: ERROR: IO Error closing connection: " + e);
				e.printStackTrace();
			}
		}
		
		if(isEngaged()) {
			currentEngagement.endEngagement();
		}
		
		moderator.connectionClosed(this);
		
		// Call after notifying the moderator of the connection being closed to avoid having the message (pointlessly) reflected back.
		moderator.sendMessageToAll(new ClientDisconnectedMessage(toConnectedClientState()));
	}

	/**
	 * 
	 * @return the public state information of this connected client considered useful (Client ID, Warrior, and Engaged Status)
	 */
	public ConnectedClientState toConnectedClientState() {
		/* Why is this synchronized?
		 * This is used to address a potential sequencing issue, an example of which is:
		 * 1. A thread (#1) creates a state update message for this client
		 * 2. Thread #1 is interrupted before it can send this message on to Client X
		 * 3. This client is changed in some way
		 * 4. Another thread (#2) creates a state update message for this client and sends it successfully to Client X
		 * 5. Thread #1 ends up running again and sends the (now out dated) state to Client X
		 * 
		 * If, in the above example, the client cannot tell the difference between messages,
		 * then Client X could end up not knowing the current information.
		 * 
		 * Adding a timestamp to the ConnectedClientState doesn't, in of itself, solve the issue
		 * because creation of two ConnectedClientStates might be interleaved. Thus, mutual exclusion
		 * must be used.
		 * 
		 * Thus, this solution works because ConnectedClientState stores the date it was created at,
		 * and only one thread at a time can get the new client state of this ConnectedClient,
		 * it is guaranteed that the createdAt times will correctly reflect how new the information is.
		 *  
		 * Of course, these messages can still be sent in any order; it is up to the client to figure out
		 * if a newly received message has newer information. Because of the createdAt timestamps though,
		 * this is trivial.
		 */
		synchronized (toConnectedClientStateLock) {
			return new ConnectedClientState(getClientId(), getWarrior(), isEngaged());
		}
	}

	/*
	 * Implementation for EngagedWarrior Interface
	 */

	@Override
	public void notifyEngagementCombatResult(List<WarriorCombatResult> warriorCombatResults) {
		boolean foundOurClientIdInResult;
		
		if(SharedConstant.DEBUG) {
			foundOurClientIdInResult = false;
		}
		
		// Find our Client, update their health, and update everyone about their state.
		for(WarriorCombatResult warriorCombatResult : warriorCombatResults) {
			if(warriorCombatResult.getClientId() == getClientId()) {
				
				getWarrior().setHealth(getWarrior().getHealth() - warriorCombatResult.getHealthLost());
				
				getModerator().sendMessageToAll(new ClientStateChangedMessage(toConnectedClientState()));
				
				if(SharedConstant.DEBUG) {
					foundOurClientIdInResult = true;
				}
				
				break;
			}
		}
		
		if(SharedConstant.DEBUG && foundOurClientIdInResult == false) {
			throw new IllegalArgumentException("Notified of Engagement not a member of");
		}
		
		safeSendMessage(new EngagementCombatResultMessage(warriorCombatResults));	
	}

	@Override
	public void notifyEngagementStarted(Engagement engagement, int startedByClientId, List<Integer> involvedClientIds) {
		synchronized (engagementLock) {		
			if(isEngaged() == false) {
				setCurrentEngagement(engagement);
				safeSendMessage(new EngagementStartedMessage(startedByClientId, involvedClientIds));
				getModerator().sendMessageToAll(new ClientStateChangedMessage(toConnectedClientState()));
				return;
			}				
		}
		
		// If we've made it this far, there was already an engagement set, which shouldn't ever happen.
		System.err.println("Connection: FATAL: Attempted to set engagement while another engagement was still active.");
		disconnect();
	}

	@Override
	public void notifyEngagementEnded(List<Integer> involvedClientIds) {
		synchronized (engagementLock) {
			
			if(isEngaged() == true) {
				setCurrentEngagement(null);
				safeSendMessage(new EngagementEndedMessage(involvedClientIds));
				getModerator().sendMessageToAll(new ClientStateChangedMessage(toConnectedClientState()));
				return;
			}
		}
		
		// If we've made it this far, there was no engagement, which shouldn't ever happen.
		System.err.println("Connection: WARN: Attempted to clear engagement when none exists.");
		
		if(SharedConstant.DEBUG && SharedConstant.DEBUG_MODE == DebugMode.VERBOSE) {
			Thread.dumpStack();
		}
	}

	@Override
	public void notifyEngagementActionSelected(SelectedAction selectedAction) {
		safeSendMessage(new EngagementActionSelectedMessage(selectedAction));
	}
	
	/*
	 * Implementation for ClientMessageHandler Interface
	 */
	
	@Override
	public void handleInvalidMessage(InvalidMessage message) {
		System.err.printf("Connection: WARN: Client reports Invalid Message: %s", message);
	}

	@Override
	public void handleSetWarriorMessage(SetWarriorMessage message) {
		if(isEngaged()) {
			safeSendMessage(new InvalidMessage(message, getAllowedContext(), "Warrior cannot be changed while in an Engagement."));
			return;
		} else {
			WarriorBase warrior = message.getWarrior();
			
			if(warrior.getUsableWeapons().contains(warrior.getEquippedWeapon()) == false) {
				safeSendMessage(new InvalidMessage(message, getAllowedContext(), "That Warrior can't wield that Weapon"));
				return;
			}
			
			setWarrior(message.getWarrior());
		}
	}

	@Override
	public void handleSendTextMessage(SendTextMessage message) {
		try {
			getModerator().sendMessageTo(
					message.getReceivers(), 
					new RelayedTextMessage(message.getMessage(), message.getReceivers(), getClientId())
			);
		} catch (InvalidClientId e) {
			safeSendMessage(new InvalidMessage(message, getAllowedContext(), e.getMessage()));
		}		
	}

	@Override
	public void handleTakeActionMessage(TakeActionMessage message) {

		synchronized (engagementLock) {
			if(isEngaged() == false) {
				List<Integer> list = new ArrayList<Integer>();
				list.add(getClientId());
				list.add(message.getTargetClientId());
				
				try {
					getModerator().createEngagementBetween(list, getClientId());
				} catch (WarriorAlreadyEngagedException | IllegalArgumentException | WarriorNotReadyException e) {
					safeSendMessage(new InvalidMessage(message, getAllowedContext(), e.getMessage()));
					return;
				}
			}
		}
		
		try {
			getCurrentEngagement().selectAction(getClientId(), message.getTargetClientId(), message.getAction());
		} catch (InvalidClientId | InvalidActionSelectionException | WarriorActionAlreadySelectedException e) {
			safeSendMessage(new InvalidMessage(message, getAllowedContext(), e.getMessage()));
		}
		
	}
	
	@Override
	public void handleSendTextToAllMessage(SendTextToAllMessage message) {
		getModerator().sendMessageToAll(new RelayedTextToAllMessage(message.getMessage(), getClientId()));		
	}

	@Override
	public void handleGetConnectedClientsListMessage(GetConnectedClientsListMessage message) {
		safeSendMessage(new ConnectedClientsListMessage(getModerator().getConnectedClientStates()));	
	}
	
	/*
	 * End of Interface Implementations
	 */
	
	@Override
	public String toString() {
		return String.format(
				"ConnectedClient[getClientId()=%s, getWarrior()=%s, isEngaged()=%s, getCurrentEngagement()=%s, isListening()=%s, isDisconnected()=%s, getModerator()=%s]",
				getClientId(), getWarrior(), isEngaged(), getCurrentEngagement(), isListening(), isDisconnected(),
				getModerator());
	}
	
	public class DisconnectedException extends Exception
	{
		private static final long serialVersionUID = 308483355119812363L;

		public DisconnectedException(String message) {
			super(message);
		}
		
		public DisconnectedException() {
			super("Connection is closed and cannot be used");
		}
	}
	
}