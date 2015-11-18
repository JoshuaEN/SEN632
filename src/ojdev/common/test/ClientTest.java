package ojdev.common.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ojdev.client.Client;
import ojdev.common.Armory;
import ojdev.common.ConnectedClientState;
import ojdev.common.SelectedAction;
import ojdev.common.messages.server.ClientConnectedMessage;
import ojdev.common.messages.server.ClientDisconnectedMessage;
import ojdev.common.messages.server.ClientStateChangedMessage;
import ojdev.common.messages.server.ConnectedClientsListMessage;
import ojdev.common.messages.server.EngagementActionSelectedMessage;
import ojdev.common.messages.server.EngagementEndedMessage;
import ojdev.common.messages.server.EngagementStartedMessage;
import ojdev.common.messages.server.SetClientIdMessage;
import ojdev.common.warriors.Warrior;
import ojdev.common.warriors.WarriorBase;
import ojdev.common.warriors.WarriorBase.UnusableWeaponException;

public class ClientTest {

	private Client client;
	
	private static final WarriorBase WARRIOR_A;
	private static final WarriorBase WARRIOR_B;
	private static final WarriorBase WARRIOR_C;
	private static final WarriorBase WARRIOR_D;
	
	static {
		try {
			WARRIOR_A = new Warrior("Test", "Someplace", "Something", 99);
			WARRIOR_B = new Warrior("TestB", "SomeplaceB", "SomethingB", 98);
			WARRIOR_C = new Warrior("TestC", "SomeplaceC", "SomethingC", 97);
			WARRIOR_D = new Warrior("TestD", "SomeplaceD", "SomethingD", 96);
		} catch (UnusableWeaponException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Before
	public void setup() {
		client = new Client(null, null);
	}

	@Test
	public void testClientStateMessages() throws Exception {
		// Because the timing is used, it's important to pause long enough to ensure
		// new date objects will be different, otherwise tests would randomly fail
		Long sleepTime = 2L;
		
		// Testing client connect handling
		
		ConnectedClientState connectedClientState1 = new ConnectedClientState(0, WARRIOR_A, false);
		Thread.sleep(sleepTime);
		ConnectedClientState connectedClientState2 = new ConnectedClientState(1, WARRIOR_B, false);
		Thread.sleep(sleepTime);
		ConnectedClientState connectedClientState3 = new ConnectedClientState(0, WARRIOR_A, true);
		
		ClientConnectedMessage clientConnectedMessage = new ClientConnectedMessage(connectedClientState2);
		
		client.handleClientConnectedMessage(clientConnectedMessage);
		assertEquals("Connected client should be correct", connectedClientState2, client.getConnectedClientsList().get(0));
		assertEquals("Should have one connected client", 1, client.getConnectedClientsList().size());
			
		clientConnectedMessage = new ClientConnectedMessage(connectedClientState3);
		client.handleClientConnectedMessage(clientConnectedMessage);
		assertEquals("Connected client should match sent connected client", connectedClientState3, client.getConnectedClientById(0));
		assertEquals("Should have two connected clients", 2, client.getConnectedClientsList().size());
		
		// Testing handling for out-of-sync message recept
		
		clientConnectedMessage = new ClientConnectedMessage(connectedClientState1);
		client.handleClientConnectedMessage(clientConnectedMessage);
		assertNotEquals("Connected client update should be ingored for older message", connectedClientState1, client.getConnectedClientById(0));
		assertEquals("Connected client update being ingored should leave newest connected client update in place", connectedClientState3, client.getConnectedClientById(0));
		assertEquals("Should still have two connected clients", 2, client.getConnectedClientsList().size());
		
		// Testing for client disconnect messages
		
		Thread.sleep(sleepTime);
		ConnectedClientState connectedClientState4 = new ConnectedClientState(0, WARRIOR_A, false);
		
		ClientDisconnectedMessage clientDisconnectedMessage = new ClientDisconnectedMessage(connectedClientState4);
		client.handleClientDisconnectedMessage(clientDisconnectedMessage);
		assertEquals("There should now only be one connected client", 1, client.getConnectedClientsList().size());
		assertEquals("Remaining connected client should be correct", connectedClientState2, client.getConnectedClientsList().get(0));
		
		// Testing clients list message
		
		Thread.sleep(sleepTime);
		ConnectedClientState connectedClientState5 = new ConnectedClientState(2, WARRIOR_A, false);
		clientConnectedMessage = new ClientConnectedMessage(connectedClientState5);
		client.handleClientConnectedMessage(clientConnectedMessage);
		
		Thread.sleep(sleepTime);
		ConnectedClientState connectedClientState6 = new ConnectedClientState(3, WARRIOR_C, false);
		
		List<ConnectedClientState> connectedClientStates = new ArrayList<ConnectedClientState>(2);
		connectedClientStates.add(connectedClientState5);
		connectedClientStates.add(connectedClientState6);
		
		ConnectedClientsListMessage connectedClientsListMessage = new ConnectedClientsListMessage(connectedClientStates);
		client.handleConnectedClientsListMessage(connectedClientsListMessage);
		
		assertEquals("Should contain previously added client", connectedClientState5, client.getConnectedClientById(2));
		assertEquals("Should contain newly added client", connectedClientState6, client.getConnectedClientById(3));
		assertEquals("Should still have two connected clients", 2, client.getConnectedClientsList().size());
		
		Thread.sleep(sleepTime);
		ConnectedClientState connectedClientState7 = new ConnectedClientState(3, WARRIOR_C, true);
		ClientStateChangedMessage clientStateChangedMessage = new ClientStateChangedMessage(connectedClientState7);
		client.handleClientStateChangedMessage(clientStateChangedMessage);
		
		assertEquals("State change should be correctly reflected", connectedClientState7, client.getConnectedClientById(3));
		
		
		// Testing client update for current ClientID being reflected correctly
		SetClientIdMessage setClientIdMessage = new SetClientIdMessage(4);
		client.handleSetClientIdMessage(setClientIdMessage);
		
		assertEquals("Client ID should be correctly set", 4, client.getClientId());
		
		Thread.sleep(sleepTime);
		ConnectedClientState connectedClientState8 = new ConnectedClientState(4, WARRIOR_D, false);
		clientStateChangedMessage = new ClientStateChangedMessage(connectedClientState8);
		client.handleClientStateChangedMessage(clientStateChangedMessage);
		
		assertEquals("Warrior correctly updated by ClientUpdateMessage", connectedClientState8.getWarrior(), client.getCurrentWarrior());
	}
	
	@Test
	public void testEncounterMessages() {
		
		client.handleSetClientIdMessage(new SetClientIdMessage(0));
		
		// Connect two clients
		ConnectedClientState connectedClientState1 = new ConnectedClientState(0, WARRIOR_A, false);
		ConnectedClientState connectedClientState2 = new ConnectedClientState(1, WARRIOR_B, false);
		
		List<ConnectedClientState> connectedClientStates = new ArrayList<ConnectedClientState>(2);
		connectedClientStates.add(connectedClientState1);
		connectedClientStates.add(connectedClientState2);
		
		ConnectedClientsListMessage connectedClientsListMessage = new ConnectedClientsListMessage(connectedClientStates);
		client.handleConnectedClientsListMessage(connectedClientsListMessage);
		
		assertEquals("Engagement should not be reported as started if it isn't", false, client.isInEngagement());
		
		// Start engagement
		
		List<Integer> involvedClientIds = new ArrayList<Integer>();
		involvedClientIds.add(0);
		involvedClientIds.add(1);
		
		Integer startedByClientId = 0;
		
		EngagementStartedMessage engagementStartedMessage = new EngagementStartedMessage(startedByClientId, involvedClientIds);
		
		client.handleEngagementStartedMessage(engagementStartedMessage);
		
		assertEquals("Engagement started status should be correctly reflected", true, client.isInEngagement());
		
		assertTrue("Engaged client ID list should be correct (contents)", client.getEngagementParticipantClientIds().containsAll(involvedClientIds));
		assertEquals("Engaged client ID list should be correct (size)", involvedClientIds.size(), client.getEngagementParticipantClientIds().size());
		
		assertEquals("Engagement started by client ID should be correct", startedByClientId, client.getEngagementStartedByClientId());
		
		assertEquals("Engagement selected actions should be empty", 0, client.getEngagementCurrentlySelectedActions().size());
		
		// Actions
		
		SelectedAction selectedActionA = new SelectedAction(0, 1, Armory.INVALID_TEST_ACTION);
		SelectedAction selectedActionB = new SelectedAction(1, 0, Armory.INVALID_TEST_ACTION);
		
		EngagementActionSelectedMessage engagementActionSelectedMessage = new EngagementActionSelectedMessage(selectedActionA);
		client.handleEngagementActionSelectedMessage(engagementActionSelectedMessage);
		
		assertEquals("Engagement selected action should be correctly reflected", selectedActionA, client.getEngagementCurrentlySelectedActions().get(0));
		assertEquals("Engagement selected actions should have one item", 1, client.getEngagementCurrentlySelectedActions().size());
		
		engagementActionSelectedMessage = new EngagementActionSelectedMessage(selectedActionB);
		client.handleEngagementActionSelectedMessage(engagementActionSelectedMessage);
		
		assertEquals("Engagement selected action should be correctly reflected", selectedActionB, client.getEngagementCurrentlySelectedActions().get(1));
		assertEquals("Engagement selected actions should have two items", 2, client.getEngagementCurrentlySelectedActions().size());
		
		engagementActionSelectedMessage = new EngagementActionSelectedMessage(selectedActionB);
		client.handleEngagementActionSelectedMessage(engagementActionSelectedMessage);
		
		assertEquals("Engagement selected action should be correctly reflected", selectedActionB, client.getEngagementCurrentlySelectedActions().get(1));
		assertEquals("Engagement selected actions should have two items", 2, client.getEngagementCurrentlySelectedActions().size());
		
		// Ended
		
		EngagementEndedMessage engagementEndedMessage = new EngagementEndedMessage(involvedClientIds);
		client.handleEngagementEndedMessage(engagementEndedMessage);
		
		assertEquals("Engagemet should reflect ended status", false, client.isInEngagement());
	}


}
