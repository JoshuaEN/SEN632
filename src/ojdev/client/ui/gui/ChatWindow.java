package ojdev.client.ui.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ojdev.client.Client;
import ojdev.common.ConnectedClientState;
import ojdev.common.messages.client.SendTextMessage;

@SuppressWarnings("serial")
public class ChatWindow extends JFrame implements ChatAreaNotifyInterface {

	private JPanel contentPane;
	private ChatArea chatArea;
	private final Client client;
	private final List<Integer> chatTargets;

	/**
	 * Create the frame.
	 */
	public ChatWindow(Client client, List<Integer> chatTargets) {
		this.client = client;
		this.chatTargets = Collections.unmodifiableList(chatTargets);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		chatArea = new ChatArea(this);
		contentPane.add(chatArea);
		
		StringBuilder titleBuilder = new StringBuilder();
		
		for(int i = 0; i < chatTargets.size(); i++) {
			
			if(i > 0) {
				titleBuilder.append(',');
				
				if(i == chatTargets.size()-1) {
					titleBuilder.append(" and ");
				} else {
					titleBuilder.append(' ');
				}
			}
			titleBuilder.append(chatTargets.get(i));
		}
		
		setTitle("Private Chat with " + titleBuilder.toString());
	}
	
	public List<Integer> getChatTargets() {
		return chatTargets;
	}

	@Override
	public boolean notifyTextEntered(ChatArea source, String text) {
		
		if(text.trim().isEmpty()) {
			return true;
		}
		
		if(client == null) {
			return false;
		}
		
		try {
			client.sendMessage(new SendTextMessage(text, chatTargets));
		} catch (IOException e) {
			chatArea.appendText("! Message Delivery failed: %s%n", e);
			return false;
		}
		
		return true;
	}

	public void appendText(Integer from, String text) {
		ConnectedClientState state = client.getConnectedClientById(from);
		
		String fromStr;
		
		if(state != null) {
			fromStr = ClientFormatHelper.getMasterNameFromState(state, client.getClientId() == from);
		} else {
			fromStr = from.toString();
		}
		chatArea.appendText("%s > %s%n", fromStr, text);		
	}
	
	public void closed() {
		if(chatArea.isClosed()) {
			return;
		}
		
		chatArea.setClosed(true);
		setTitle("(Closed) " + getTitle());
	}

	@Override
	public String getChatDisplayName() {
		ConnectedClientState state = client.getConnectedClientById(client.getClientId());
		
		if(state == null) {
			return ((Integer)client.getClientId()).toString();
		} else {
			return ClientFormatHelper.getMasterNameFromState(state, true);
		}
	}

}
