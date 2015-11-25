package ojdev.client.ui.gui;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import ojdev.client.Client;
import ojdev.client.ClientUserInterface;
import ojdev.common.connections.Connection;
import ojdev.common.exceptions.IllegalMessageContext;
import ojdev.common.message_handlers.ServerMessageHandler;
import ojdev.common.messages.MessageBase;
import ojdev.common.warriors.WarriorBase;

/**
 * A subclass of Client that overrides notification pathways between the Client and UI
 * to ensure that UI elements are only interacted with in the Swing Event Dispatch Thread.
 * 
 * Invoke and Wait is used to ensure ordering is not damaged. It's considered absoutely 
 */
public class SwingSafeClient extends Client {

	public SwingSafeClient(Connection connection, ServerMessageHandler serverMessageHandler,
			ClientUserInterface clientInterface) {
		super(connection, serverMessageHandler, clientInterface);
	}

	public SwingSafeClient(Connection connection, Object clientUI) {
		super(connection, clientUI);
	}

	@Override
	protected void handleMessage(MessageBase message) throws IllegalMessageContext {
		message.handleWith(this);

		ServerMessageHandler serverMessageHandler = getExternalMessageHandler();
		
		if(serverMessageHandler != null) {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					
					@Override
					public void run() {
						try {
							message.handleWith(serverMessageHandler);
						} catch (IllegalMessageContext e) {
							notifyExceptionInDispatchThread(e);
						}
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				notifyException(e);
			}
		}
	}
	
	@Override
	protected void notifyCurrentWarriorChanged(WarriorBase currentWarrior) {
		ClientUserInterface clientInterface = getUserInterface();
		
		if(clientInterface != null) {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					
					@Override
					public void run() {
						clientInterface.notifyCurrentWarriorChanged(currentWarrior);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				notifyException(e);
			}		
		}
	}
	
	@Override
	protected void notifyException(Exception e) {
		ClientUserInterface clientUserInterface = getUserInterface();
		if(clientUserInterface != null) {
			try {
				EventQueue.invokeAndWait(new Runnable() {				
					@Override
					public void run() {
						notifyExceptionInDispatchThread(e);
					}
				});
			} catch (InvocationTargetException | InterruptedException e1) {
				// Don't attempt to notify to prevent infinite loop, print error and give up
				e1.printStackTrace();
			}
		}
	}
	
	private void notifyExceptionInDispatchThread(Exception e) {
		if(SwingUtilities.isEventDispatchThread() == false) {
			throw new IllegalStateException("Must be in event dispatch thread");
		}
		ClientUserInterface clientUserInterface = getUserInterface();
		if(clientUserInterface != null) {
			clientUserInterface.notifyException(e);
		}
	}
}
