package ojdev.common.connections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import ojdev.common.SharedConstant;
import ojdev.common.exceptions.InvalidMessageTypeException;
import ojdev.common.io.LookAheadObjectInputStream;
import ojdev.common.messages.*;

public class SocketConnection extends Connection {

	private final Socket socket;
	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;
	
	public SocketConnection(Socket socket) throws IOException {
		this.socket = socket;
		this.writer = new ObjectOutputStream(socket.getOutputStream());
		this.writer.flush();
		this.reader = new LookAheadObjectInputStream(socket.getInputStream());
	}

	@Override
	public void sendMessage(MessageBase message) throws IOException {
		synchronized (writer) {
			writer.writeObject(message);
			writer.flush();
		}
	}

	@Override
	public MessageBase receiveMessage() throws InvalidMessageTypeException, IOException {
		Object object;
		try {
			synchronized (reader) {
				object = reader.readObject();
			}
		} catch (ClassNotFoundException e) {
			throw new InvalidMessageTypeException(e.getMessage(), e);
		}
		
		if(object instanceof MessageBase) {
			
			if(SharedConstant.DEBUG) {
				System.out.printf("Recieved Message: %s%n", object);
			}
			
			return (MessageBase)object;
		} else {
			throw new InvalidMessageTypeException(object);
		}
	}

	@Override
	public boolean isClosed() {
		return this.socket.isClosed();
	}

	@Override
	public void close() throws IOException {
		this.socket.close();		
	}
	
	@Override
	public String toString() {
		return String.format("SocketConnection[socket=%s, reader=%s, writer=%s]", socket, reader, writer);
	}
}