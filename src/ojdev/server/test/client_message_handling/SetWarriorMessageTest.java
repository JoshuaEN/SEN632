package ojdev.server.test.client_message_handling;

import static org.junit.Assert.*;

import org.junit.Test;

import ojdev.common.messages.InvalidMessage;
import ojdev.common.messages.client.SetWarriorMessage;
import ojdev.common.messages.client.TakeActionMessage;
import ojdev.common.messages.server.ClientStateChangedMessage;
import ojdev.common.warriors.Warrior;
import ojdev.common.warriors.WarriorBase;
import ojdev.server.test.ServerTestConstant;
import ojdev.common.Armory;
import ojdev.common.ConnectedClientState;

public class SetWarriorMessageTest extends ClientMessageHandlerTest {

	/**
	 * Sending a SetWarriorMessage should result in:
	 * 1. Setting the Client's Warrior on the Server.
	 * 2. Notifying all Clients of the change with ClientStateChangeMessage
	 * 
	 * Both conditions are tested here for a valid Warrior.
	 */
	@Test
	public void testValidMessage() throws Exception {
		Warrior warrior = new Warrior("Test_Warrior", "Someplace", "Something", 100, Armory.GREAT_SWORD);
		getConnectionA().sendMessage(new SetWarriorMessage(warrior));
		
		int clientIdOfA = getConnectionAQuery().getClientId();
		
		getConnectionAQuery().getMessageOf(ClientStateChangedMessage.class);
		
		WarriorBase foundWarrior = null;
		
		if(ServerTestConstant.VERBOSE_MESSAGES) {
			System.out.printf("Connected Clients: %d | %d%n", getServer().getConnectedClientsCount(), getServer().getConnectedClientStates().size());
		}
		
		for(ConnectedClientState state : getServer().getConnectedClientStates()) {
			if(ServerTestConstant.VERBOSE_MESSAGES) {
				System.out.printf("State: %s%n", state);
			}
			
			if(state.getClientId() == clientIdOfA) {
				foundWarrior = state.getWarrior();
			}
		}
		
		if(ServerTestConstant.VERBOSE_MESSAGES) {
			System.out.printf("Expected Warrior: %s%n Actual Warrior:  %s%n", warrior, foundWarrior);
		}
		
		assertNotEquals("Client must be in server list", null, foundWarrior);
		
		assertEquals("Warrior was not correctly set", warrior, foundWarrior);
	}
	
	@Test
	public void testDisallowedSetContext() throws Exception {
		
		// Set Player's Warriors, since each player must have a Warrior to engage in combat.
		Warrior warrior = new Warrior("Test_Warrior", "Someplace", "Something", 100, Armory.GREAT_SWORD);
		warrior.setEquippedWeapon(Armory.GREAT_SWORD);
		
		SetWarriorMessage setWarriorMessage = new SetWarriorMessage(warrior);
		getConnectionA().sendMessage(setWarriorMessage);
		getConnectionB().sendMessage(setWarriorMessage);
		
		// Enter an encounter
		getConnectionA().sendMessage(new TakeActionMessage(Armory.BLOCK_HIGH, getConnectionBQuery().getClientId()));
		
		// Try to set the Warrior Again
		getConnectionA().sendMessage(setWarriorMessage);
		
		// Check that the server reported the selection was invalid.
		InvalidMessage invalidMessage = getConnectionAQuery().getMessageOf(InvalidMessage.class);
		
		if(ServerTestConstant.VERBOSE_MESSAGES) {
			System.out.println(invalidMessage);
		}
		
		assertEquals("Must disallow setting warrior while in an Encounter", setWarriorMessage, invalidMessage.getMessage());
	}

}
