package ojdev.server.test.client_message_handling;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.Timeout;

import ojdev.common.connections.Connection;
import ojdev.common.connections.SocketConnection;
import ojdev.common.exceptions.InvalidMessageTypeException;
import ojdev.common.messages.MessageBase;
import ojdev.common.messages.server.SetClientIdMessage;
import ojdev.server.Moderator;
import ojdev.server.test.ServerTestConstant;

public class ClientMessageHandlerTest {
	
	@Rule
	public Timeout globalTimeout = new Timeout(10, TimeUnit.SECONDS);
	
	private Moderator server;
	private Thread serverThread;
	private Connection connectionA;
	private Connection connectionB;
	private ConnectionQuery connectionAQuery;
	private ConnectionQuery connectionBQuery;
	
	protected ClientMessageHandlerTest() {
		
	}
	
	public Connection getConnectionA() {
		return connectionA;
	}
	
	public Connection getConnectionB() {
		return connectionB;
	}
	
	public Moderator getServer() {
		return server;
	}

	public ConnectionQuery getConnectionAQuery() {
		return connectionAQuery;
	}

	public ConnectionQuery getConnectionBQuery() {
		return connectionBQuery;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.server = new Moderator(20, ServerTestConstant.DEFAULT_PORT, 20);
		this.serverThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					server.startServer();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		this.serverThread.start();
		this.connectionA = new SocketConnection(new Socket("localhost", ServerTestConstant.DEFAULT_PORT));
		this.connectionAQuery = new ConnectionQuery(this.connectionA);
		this.connectionB = new SocketConnection(new Socket("localhost", ServerTestConstant.DEFAULT_PORT));
		this.connectionBQuery = new ConnectionQuery(this.connectionB);
	}

	@After
	public void tearDown() throws Exception {
		this.connectionA.close();
		this.connectionB.close();
		this.server.stopServer();
		this.server = null;
		this.serverThread = null;
		this.connectionA = null;
		this.connectionB = null;	 
	}

	
	public class ConnectionQuery
	{
		private final Connection connection;
		private final List<MessageBase> messageHistoryList = new ArrayList<MessageBase>();
	
		public ConnectionQuery(Connection connection) {
			this.connection = connection;
		}
		
		public <T extends MessageBase> T getMessageOf(Class<T> type) throws InvalidMessageTypeException, IOException {
			T lastFromHistory = getNewestMessageFromHistroyOf(type);
			
			if(lastFromHistory != null)
				return lastFromHistory;
			
			T lastFromConnection = getMessageFromConnectionOf(type);
			
			return lastFromConnection;
		}
		
		@SuppressWarnings("unchecked")
		public <T extends MessageBase> T getNewestMessageFromHistroyOf(Class<T> type) {
			MessageBase messageBase;
			
			for(int i = messageHistoryList.size() - 1; i >= 0; i -= 1) {
				messageBase = messageHistoryList.get(i);
				if(messageBase.getClass() == type) {
					return (T)messageBase;
				}
			}
			
			return null;
		}
		
		@SuppressWarnings("unchecked")
		public <T extends MessageBase> T getMessageFromConnectionOf(Class<T> type) throws InvalidMessageTypeException, IOException {
			while(true) {
				MessageBase messageBase = connection.receiveMessage();

				messageHistoryList.add(messageBase);
				
				if(messageBase.getClass() == type) {
					return (T)messageBase;
				}
			}
		}
		
		public int getClientId() throws InvalidMessageTypeException, IOException {
			return getMessageOf(SetClientIdMessage.class).getClientId();
		}
	}
}
