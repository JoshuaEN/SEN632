package ojdev.client.ui.console;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ojdev.client.WarriorFolder;
import ojdev.common.actions.Action;
import ojdev.common.connections.Connection;
import ojdev.common.exceptions.InvalidMessageTypeException;
import ojdev.common.messages.MessageBase;
import ojdev.common.messages.client.GetConnectedClientsListMessage;
import ojdev.common.messages.client.SendTextMessage;
import ojdev.common.messages.client.SendTextToAllMessage;
import ojdev.common.messages.client.SetWarriorMessage;
import ojdev.common.messages.client.TakeActionMessage;
import ojdev.common.warriors.Warrior;
import ojdev.common.weapons.Weapon;

public class ClientConsoleInterfaceControllerTest {

	public ClientConsoleInterfaceController controller;
	public QueueConnection connection;
	public StringWriter writer;
	public Thread thisThread;
	public WarriorFolder folder;
	public Path path = Paths.get("test/output/ClientConsoleInterfaceController");

	@Before
	public void setUp() throws Exception {
		connection = new QueueConnection();
		writer = new StringWriter();
		thisThread = Thread.currentThread();
		folder = new WarriorFolder(path);
		
		controller = new ClientConsoleInterfaceController(writer, null, folder) {
			@Override
			protected void connect(String address, int port) throws IOException {
				connect(connection);				
			}
		};
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConnection() throws Exception {
		controller.parse("join localhost 12345");
		
		assertTrue("Connection should have recieved GetConnectedClientsListMessage", 
				connection.queueContainsType(GetConnectedClientsListMessage.class));
		
		controller.parse("leave");
		
		assertTrue("Connection should have been closed",
				connection.isClosed());
	}
	
	@Test
	public void testWarriorSet() throws Exception {
		String newFormat = "warrior new %s %s %s %s %s";
		String name1 = "war1",
				name2 = "war2",
				name3 = "war3",
				type = "Warrior",
				originLocation = "someplace",
				description = "somewhere";
		int health = 99;
		
		path.resolve(name1 + "." + Warrior.FILE_EXTENSION).toFile().delete();
		path.resolve(name2 + "." + Warrior.FILE_EXTENSION).toFile().delete();
		path.resolve(name3 + "." + Warrior.FILE_EXTENSION).toFile().delete();
		
		controller.parse("warrior list");
		
		assertTrue("Warrior listing should be empty", writer.toString().trim().isEmpty());
		clearWriter();
		
		controller.parse(String.format(newFormat, type, name1, health, "", ""));
		
		
		controller.parse("warrior list");
		
		assertThat("Warrior listing output", writer.toString(), containsString(name1));
		clearWriter();
		
		controller.parse(String.format(newFormat, type, name2, health, originLocation, ""));
		
		controller.parse("warrior list");
		
		assertThat("Warrior listing output", writer.toString(), containsString(name1));
		assertThat("Warrior listing output", writer.toString(), containsString(name2));
		clearWriter();
		
		controller.parse(String.format(newFormat, type, name3, health, originLocation, description));
		
		controller.parse("warrior list details");
		
		assertThat("Warrior listing output", writer.toString(), containsString(name1));
		assertThat("Warrior listing output", writer.toString(), containsString(name2));
		assertThat("Warrior listing output", writer.toString(), containsString(name3));
		assertThat("Warrior listing output", writer.toString(), containsString(""+health));
//		assertThat("Warrior listing output", writer.toString(), containsString(originLocation));
//		assertThat("Warrior listing output", writer.toString(), containsString(description));
		clearWriter();
		
		
		controller.connect(connection);
		
		controller.parse("warrior set " + name1 + ".wdat");
		
		assertTrue("Connection should have recieved SetWarriorMessage", 
				connection.queueContainsType(SetWarriorMessage.class));
		
		assertEquals("SetWarriorMessage should set correct warrior", 
				folder.loadWarrior(name1 + ".wdat"), 
				connection.getFirstOfType(SetWarriorMessage.class).getWarrior()
		);
		
		clearWriter();
		controller.parse("weapon list");
		
		for(Weapon weapon : folder.loadWarrior(name1 + ".wdat").getUsableWeapons()) {
			assertThat("Weapon listing output", writer.toString(), containsString(weapon.getName()));
		}
		clearWriter();
		
		
		for(Weapon weapon : folder.loadWarrior(name1 + ".wdat").getUsableWeapons()) {
			controller.parse("weapon equip " + weapon.getName());
					
			controller.parse("action list");
			for(Action action : weapon.getActions()) {
				assertThat("Action listing output", writer.toString(), containsString(action.getName()));
				controller.parse("action use 0 " + action.getName());
				
				assertEquals("TakeActionMessage should use correct action", 
						action, 
						connection.getFirstOfType(TakeActionMessage.class).getAction()
				);
			}
			clearWriter();
			
			assertEquals("SetWarriorMessage should set correct weapon", 
					weapon, 
					connection.getFirstOfType(SetWarriorMessage.class).getWarrior().getEquippedWeapon()
			);
		}
		clearWriter();
		
		controller.parse("warrior set " + name2 + ".wdat");
		
		
		assertEquals("SetWarriorMessage should set correct warrior", 
				folder.loadWarrior(name2 + ".wdat"), 
				connection.getFirstOfType(SetWarriorMessage.class).getWarrior()
		);
		
		
		
		controller.parse("warrior delete " + name1 + ".wdat");
		
		clearWriter();
		controller.parse("warrior list");
		
		assertThat("Warrior listing output", writer.toString(), not(containsString(name1)));
		clearWriter();
		
		controller.parse("warrior delete " + name2 + ".wdat");
		controller.parse("warrior delete " + name3 + ".wdat");
		
		clearWriter();
		controller.parse("warrior list");
		
		assertTrue("Warrior listing should be empty", writer.toString().trim().isEmpty());
		clearWriter();
	}
	
	@Test
	public void testMessage() throws Exception {
		int a = 0, b = 1;
		String text = "this is a test";
		
		controller.connect(connection);
		
		controller.parse("message * " + text);
		
		assertTrue("Connection should have recieved SendTextToAllMessage", 
				connection.queueContainsType(SendTextToAllMessage.class));
		
		SendTextToAllMessage allMsg = connection.getFirstOfType(SendTextToAllMessage.class);
		
		assertEquals("Sent message should match", text, allMsg.getMessage());
		
		List<Integer> targets = new ArrayList<Integer>();
		targets.add(a);
		
		controller.parse("message " + a + " " + text);
		
		assertTrue("Connection should have recieved SendTextMessage", 
				connection.queueContainsType(SendTextMessage.class));
		
		SendTextMessage msg = connection.getFirstOfType(SendTextMessage.class);
		
		assertTrue("Recievers list should contain targets", targets.containsAll(msg.getReceivers()));
		assertEquals("Recievers list should only contain those targets", targets.size(), msg.getReceivers().size());
		assertEquals("Sent message should match", text, msg.getMessage());
		
		targets.add(b);
		
		controller.parse("message " + a + "," + b + " " + text);
		
		assertTrue("Connection should have recieved SendTextMessage", 
				connection.queueContainsType(SendTextMessage.class));
		
		 msg = connection.getFirstOfType(SendTextMessage.class);
		
		assertTrue("Recievers list should contain targets", targets.containsAll(msg.getReceivers()));
		assertEquals("Recievers list should only contain those targets", targets.size(), msg.getReceivers().size());
		assertEquals("Sent message should match", text, msg.getMessage());
	}

	private void clearWriter() {
		writer.getBuffer().setLength(0);
	}
	
	private class QueueConnection extends Connection {
		
		private final LinkedList<MessageBase> sendQueue = new LinkedList<MessageBase>();
		private boolean closed = false;

		@Override
		public void sendMessage(MessageBase message) throws IOException {
			sendQueue.addFirst(message);
		}
		
		@SuppressWarnings("unused")
		public LinkedList<MessageBase> getQueue() {
			return sendQueue;
		}
		
		public boolean queueContainsType(Class<? extends MessageBase> type) {
			return getFirstOfType(type) != null;
		}
		
		@SuppressWarnings("unchecked")
		public <T extends MessageBase> T getFirstOfType(Class<T> type) {
			for(MessageBase message : sendQueue) {
				if(message.getClass() == type)
					return (T)message;
			}
			return null;
		}

		@Override
		public MessageBase receiveMessage() throws InvalidMessageTypeException, IOException {
			// Block
			if(Thread.currentThread() != thisThread) {
				try {
					Thread.sleep(100000L);
				} catch (InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
			throw new IllegalStateException();
		}

		@Override
		public boolean isClosed() {
			return closed;
		}

		@Override
		public void close() throws IOException {
			closed = true;
		}
		
	}
	
}
