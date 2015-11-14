package ojdev.server.test.client_message_handling;

import static org.junit.Assert.*;

import org.junit.Test;

import ojdev.common.messages.client.SendTextToAllMessage;
import ojdev.common.messages.server.RelayedTextToAllMessage;

public class SendTextToAllMessageTest extends ClientMessageHandlerTest {

	@Test
	public void testValidMessage() throws Exception {
		String testMessage = "This is a test";
		
		getConnectionA().sendMessage(new SendTextToAllMessage(testMessage));
		
		RelayedTextToAllMessage messageSentToA = getConnectionAQuery().getMessageOf(RelayedTextToAllMessage.class);
		RelayedTextToAllMessage messageSentToB = getConnectionBQuery().getMessageOf(RelayedTextToAllMessage.class);
		
		assertEquals("All Clients should recieve the same message", messageSentToA, messageSentToB);
		
		assertEquals("Clients should recieve the correct message text", testMessage, messageSentToA.getMessage());
		assertEquals("Clients should recieve the correct sender ID", getConnectionAQuery().getClientId(), messageSentToA.getSenderClientId());
	}

}
