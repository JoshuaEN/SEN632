package ojdev.server.test.client_message_handling;

import static org.junit.Assert.*;

import org.junit.Test;

import ojdev.common.Armory;
import ojdev.common.messages.client.SetWarriorMessage;
import ojdev.common.messages.client.TakeActionMessage;
import ojdev.common.messages.server.EngagementActionSelectedMessage;
import ojdev.common.messages.server.EngagementCombatResultMessage;
import ojdev.common.warriors.Warrior;

public class TakeActionMessageTest extends ClientMessageHandlerTest {

	@SuppressWarnings("unused")
	@Test
	public void testValidMessageChain() throws Exception {
		
		// Set Player's Warriors, since each player must have a Warrior to engage in combat.
		Warrior warrior = new Warrior("Test_Warrior", "Someplace", "Something", 100, Armory.GREAT_SWORD);
		warrior.setEquippedWeapon(Armory.GREAT_SWORD);

		SetWarriorMessage setWarriorMessage = new SetWarriorMessage(warrior);
		getConnectionA().sendMessage(setWarriorMessage);
		getConnectionB().sendMessage(setWarriorMessage);
		
		TakeActionMessage takeActionMessage = 
				new TakeActionMessage(Armory.OVERHEAD_SWING, getConnectionBQuery().getClientId());
		
		getConnectionA().sendMessage(takeActionMessage);
		
		EngagementActionSelectedMessage engagementActionSelectedMessageA =
				getConnectionAQuery().getMessageOf(EngagementActionSelectedMessage.class);
		
		assertTrue("Selected action report should equal taken action", 
				compareTakeActionToActionSelected(
						getConnectionAQuery().getClientId(),
						takeActionMessage, 
						engagementActionSelectedMessageA
				)
			);
		
		EngagementActionSelectedMessage engagementActionSelectedMessageB =
				getConnectionBQuery().getMessageOf(EngagementActionSelectedMessage.class);
		
		// Equality is asserted on the selected action, instead of the message as a whole,
		// because the time stamps will be slightly different.
		assertEquals(
				"Selected action report should match for all clients", 
				engagementActionSelectedMessageA.getSelectedAction(), 
				engagementActionSelectedMessageB.getSelectedAction()
		);
		
		takeActionMessage = 
				new TakeActionMessage(Armory.LEFT_SWING, getConnectionBQuery().getClientId());
		
		getConnectionB().sendMessage(takeActionMessage);
		
		engagementActionSelectedMessageA =
				getConnectionAQuery().getMessageFromConnectionOf(EngagementActionSelectedMessage.class);
		
		assertTrue("Selected action report should equal taken action", 
				compareTakeActionToActionSelected(
						getConnectionBQuery().getClientId(),
						takeActionMessage, 
						engagementActionSelectedMessageA
				)
			);
		
		engagementActionSelectedMessageB =
				getConnectionBQuery().getMessageFromConnectionOf(EngagementActionSelectedMessage.class);

		assertEquals(
				"Selected action report should match for all clients", 
				engagementActionSelectedMessageA.getSelectedAction(), 
				engagementActionSelectedMessageB.getSelectedAction()
		);
		
		EngagementCombatResultMessage combatResultMessageForA = 
				getConnectionAQuery().getMessageOf(EngagementCombatResultMessage.class);
		
		EngagementCombatResultMessage combatResultMessageForB = 
				getConnectionBQuery().getMessageOf(EngagementCombatResultMessage.class);
		
		// TODO Test combat results
		
	}
	
	private boolean compareTakeActionToActionSelected(int clientId, TakeActionMessage takeActionMessage, EngagementActionSelectedMessage engagementActionSelectedMessage) {
		assertEquals("Action should be equal", takeActionMessage.getAction(), engagementActionSelectedMessage.getSelectedAction().getAction());
		assertEquals("Client ID should be equal", clientId, engagementActionSelectedMessage.getSelectedAction().getClientId());
		assertEquals("Target Client ID should be equal", takeActionMessage.getTargetClientId(), engagementActionSelectedMessage.getSelectedAction().getTargetClientId());
		
		return true;
	}
	
}
