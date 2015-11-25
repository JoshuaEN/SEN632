package ojdev.server.test.client_message_handling;

import static org.junit.Assert.*;

import org.junit.Test;

import ojdev.common.messages.client.GetConnectedClientsListMessage;
import ojdev.common.messages.server.ConnectedClientsListMessage;

public class GetConnectedClientsListMessageTest extends ClientMessageHandlerTest {

	@Test
	public void testValidMessage() throws Exception {
		getConnectionB().sendMessage(new GetConnectedClientsListMessage());
		
		ConnectedClientsListMessage connectedClientsListMessage = 
				getConnectionBQuery().getMessageOf(ConnectedClientsListMessage.class);
		
		assertConnectionListsMatch(connectedClientsListMessage);
		
		getConnectionB().close();
		
		getConnectionA().sendMessage(new GetConnectedClientsListMessage());
		connectedClientsListMessage = 
				getConnectionAQuery().getMessageOf(ConnectedClientsListMessage.class);
		
		assertConnectionListsMatch(connectedClientsListMessage);
		
	}
	
	private void assertConnectionListsMatch(ConnectedClientsListMessage connectedClientsListMessage) {
		assertTrue("Server Connection List matches Client List",
				connectedClientsListMessage.getWarriors().containsAll(getServer().getConnectedClientStates()) &&
				connectedClientsListMessage.getWarriors().size() == getServer().getConnectedClientsCount()
			);
	}

}
