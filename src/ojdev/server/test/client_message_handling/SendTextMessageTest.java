package ojdev.server.test.client_message_handling;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ojdev.common.messages.InvalidMessage;
import ojdev.common.messages.client.SendTextMessage;
import ojdev.common.messages.server.RelayedTextMessage;
import ojdev.server.test.ServerTestConstant;

public class SendTextMessageTest extends ClientMessageHandlerTest {

	/**
	 * Sending a SendTextMessage should result in:
	 * <ul>
	 * <li>A RelayedTextMessage being sent to all specified Receivers</li>
	 * <li>The RelayedTextMessage must:
	 * <ul>
	 * 		<li>Have the correct Sender</li>
	 * 		<li>Have the correct Receivers</li>
	 * 		<li>Have the correct Message</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * These conditions are tested here for a Valid Message
	 */
	@Test
	public void testValidMessage() throws Exception {
		String testTextMessage = "This is a test";
		int textMessageTargetId = getConnectionBQuery().getClientId();
		List<Integer> receivers = new ArrayList<Integer>();
		receivers.add(textMessageTargetId);
		
		SendTextMessage sentTextMessage = new SendTextMessage(testTextMessage, receivers);
		
		getConnectionA().sendMessage(sentTextMessage);
		
		
		RelayedTextMessage relayedTextMessage = getConnectionBQuery().getMessageFromConnectionOf(RelayedTextMessage.class);
		
		if(ServerTestConstant.VERBOSE_MESSAGES) {
			System.out.printf("Sent Text Message: %s%nRecived Text Message: %s%n", sentTextMessage, relayedTextMessage);
		}
		
		assertEquals("send text should equate to recieved text", testTextMessage, relayedTextMessage.getMessage());
		assertEquals("sender client ID should be accurate", getConnectionAQuery().getClientId(), relayedTextMessage.getSenderClientId());
		assertTrue("recievers lists should contain the same values", receivers.containsAll(relayedTextMessage.getReceiversClientIds()));
		assertEquals("recievers lists should be of equal length", receivers.size(), relayedTextMessage.getReceiversClientIds().size());
		
	}

	/**
	 * The server MUST handle the following failure conditions as described:
	 * <ul>
	 * <li>The Server MUST reject invalid Client IDs.</li>
	 * <li>The Server MUST not relay the message to anyone if any Client ID is invalid.</li>
	 * </ul>
	 * 
	 * This tests the first.
	 */
	@Test
	public void testInvalidReceiverId() throws Exception {
		String testTextMessage = "This is an invalid test";
		int textMessageTargetId = 9999;
		
		List<Integer> receivers = new ArrayList<Integer>();
		receivers.add(textMessageTargetId);
		
		SendTextMessage sentTextMessage = new SendTextMessage(testTextMessage, receivers);
		
		getConnectionA().sendMessage(sentTextMessage);
		
		
		InvalidMessage invalidMessage = getConnectionAQuery().getMessageFromConnectionOf(InvalidMessage.class);
		
		if(ServerTestConstant.VERBOSE_MESSAGES) {
			System.out.printf("Sent Text Message: %s%nRecived Invalid Message: %s%n", sentTextMessage, invalidMessage);
		}
		
		assertEquals("Should report message as invalid", sentTextMessage.getClass(), invalidMessage.getMessage().getClass());
		assertEquals("Should report message as invalid", sentTextMessage.getMessage(), ((SendTextMessage)invalidMessage.getMessage()).getMessage());
		
	}
}
