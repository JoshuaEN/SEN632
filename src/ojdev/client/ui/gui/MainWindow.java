package ojdev.client.ui.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import java.awt.Color;
import javax.swing.JLabel;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import ojdev.client.Client;
import ojdev.client.ClientUserInterface;
import ojdev.client.WarriorFolder;
import ojdev.common.Armory;
import ojdev.common.ConnectedClientState;
import ojdev.common.SharedConstant;
import ojdev.common.WarriorCombatResult;
import ojdev.common.actions.Action;
import ojdev.common.connections.Connection;
import ojdev.common.connections.SocketConnection;
import ojdev.common.exceptions.CryptoKeyProblemUnlockException;
import ojdev.common.message_handlers.ServerMessageHandler;
import ojdev.common.messages.InvalidMessage;
import ojdev.common.messages.client.GetConnectedClientsListMessage;
import ojdev.common.messages.client.SendTextToAllMessage;
import ojdev.common.messages.client.SetWarriorMessage;
import ojdev.common.messages.client.TakeActionMessage;
import ojdev.common.messages.server.ClientConnectedMessage;
import ojdev.common.messages.server.ClientDisconnectedMessage;
import ojdev.common.messages.server.ClientStateChangedMessage;
import ojdev.common.messages.server.ConnectedClientsListMessage;
import ojdev.common.messages.server.EngagementActionSelectedMessage;
import ojdev.common.messages.server.EngagementCombatResultMessage;
import ojdev.common.messages.server.EngagementEndedMessage;
import ojdev.common.messages.server.EngagementStartedMessage;
import ojdev.common.messages.server.RelayedTextMessage;
import ojdev.common.messages.server.RelayedTextToAllMessage;
import ojdev.common.messages.server.ServerTextMessage;
import ojdev.common.messages.server.SetClientIdMessage;
import ojdev.common.warriors.CryptoWarrior;
import ojdev.common.warriors.UndeadWarrior;
import ojdev.common.warriors.Warrior;
import ojdev.common.warriors.WarriorBase;
import ojdev.common.warriors.WarriorBase.UnusableWeaponException;

import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.FlowLayout;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import java.awt.Dimension;
import java.awt.Font;

@SuppressWarnings("serial")
public class MainWindow 
	extends 
		JFrame 
	implements 
		ServerMessageHandler, 
		ClientUserInterface, 
		WarriorViewDialogCallbackInterface, 
		EngagedWarriorCallbackInterface,
		ChatAreaNotifyInterface {

	private JPanel contentPane;
	private JSplitPane splitPane;
	private JTabbedPane tabbedPane;
	private JPanel panelServer;
	private JPanel panelMastersList;
	private JPanel panelEngagement;
	private JPanel panelWarriors;
	private ChatArea chatAreaTournament;
	private ChatArea chatAreaConsole;
	private JLabel lblNewJgoodiesLabel;
	private JTextField textFieldAddress;
	private JLabel lblPort;
	private JSpinner textFieldPort;
	private JButton btnJoinTournament;
	private JTable table;
	private DefaultTableModel tableModel;

	private Client client;
	private Thread clientThread;
	private JList<String> listWarriors;
	private JPanel panel;
	private JButton btnDeleteWarrior;
	private JToggleButton btnToggleSelectWarrior;
	private JButton btnNewWarrior;
	private JPanel panelNewWarrior;
	private JPanel panelExistingWarrior;
	private JButton btnNewButton;
	
	private Map<Integer, WarriorBase> loadedWarriors = new HashMap<Integer, WarriorBase>();
	private Map<Integer, File> loadedParamsReqWarriors = new HashMap<Integer, File>();
	private WarriorFolder warriorFolder = new WarriorFolder(Paths.get("."));
	private WarriorBase currentWarrior = null;
	private int targetClientId = -1;
	private Map<Integer, EngagedWarrior> engagedWarriors = new HashMap<Integer, EngagedWarrior>(2);
	private ChatArea chatAreaEngagement;
	
	private List<ChatWindow> chatWindows = new ArrayList<ChatWindow>();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		assert SwingUtilities.isEventDispatchThread() : "Must be called from Event Dispatch Thread";
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// Don't care
		}
		setTitle("Tournament Master");
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 845, 700);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		splitPane = new JSplitPane();
		splitPane.setResizeWeight(1.0);
		splitPane.setBackground(Color.WHITE);
		splitPane.setDividerLocation(500);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Color.WHITE);
		splitPane.setLeftComponent(tabbedPane);
		
		panelServer = new JPanel();
		tabbedPane.addTab("Tournament", null, panelServer, null);
		panelServer.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		lblNewJgoodiesLabel = DefaultComponentFactory.getInstance().createLabel("Address");
		lblNewJgoodiesLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		panelServer.add(lblNewJgoodiesLabel, "4, 4, right, default");
		
		textFieldAddress = new JTextField();
		textFieldAddress.setText("localhost");
		lblNewJgoodiesLabel.setLabelFor(textFieldAddress);
		panelServer.add(textFieldAddress, "6, 4, fill, default");
		textFieldAddress.setColumns(10);
		
		lblPort = DefaultComponentFactory.getInstance().createLabel("Port");
		lblPort.setHorizontalAlignment(SwingConstants.TRAILING);
		panelServer.add(lblPort, "4, 6");
		
		textFieldPort = new JSpinner();
		lblPort.setLabelFor(textFieldPort);
		textFieldPort.setModel(new SpinnerNumberModel(0, 0, 65535, 1));
		JSpinner.NumberEditor portNumberEditor = new JSpinner.NumberEditor(textFieldPort,"#");
		portNumberEditor.getTextField().setHorizontalAlignment(JTextField.LEADING);
		textFieldPort.setEditor(portNumberEditor);
		textFieldPort.setValue(SharedConstant.DEFAULT_PORT);
		
		panelServer.add(textFieldPort, "6, 6");
		
		panel_1 = new JPanel();
		panelServer.add(panel_1, "6, 8, fill, fill");
		panel_1.setLayout(new FlowLayout(FlowLayout.TRAILING, 0, 0));
		
		btnJoinTournament = new JButton("Join Tournament");
		panel_1.add(btnJoinTournament);
		
		btnLeaveTournament = new JButton("Leave Tournament");
		btnLeaveTournament.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				leaveTournament();
			}
		});
		panel_1.add(btnLeaveTournament);
		btnJoinTournament.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				joinTournament();
			}
		});
		
		panelMastersList = new JPanel();
		panelMastersList.setBackground(Color.WHITE);
		panelMastersList.setBorder(null);
		tabbedPane.addTab("Masters List", null, panelMastersList, null);
		panelMastersList.setLayout(new BorderLayout(0, 0));
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setBorder(null);
		tableModel = (new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
					"ID", "E", "Type", "Name", "HP", "Weapon"
				}
			) {
				Class<?>[] columnTypes = new Class<?>[] {
					Integer.class, Boolean.class, String.class, String.class, Integer.class, Object.class
				};
				public Class<?> getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
				boolean[] columnEditables = new boolean[] {
					false, false, false, false, false, false
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
		});
		table.setModel(tableModel);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {	
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateMasterListOptionState();
			}
		});

		
		
		table.getColumnModel().getColumn(0).setPreferredWidth(25);
		table.getColumnModel().getColumn(1).setPreferredWidth(20);
		table.getColumnModel().getColumn(2).setPreferredWidth(90);
		table.getColumnModel().getColumn(4).setPreferredWidth(30);
		table.getColumnModel().getColumn(5).setPreferredWidth(90);
		panelMastersList.add(new JScrollPane(table), BorderLayout.CENTER);
		
		panelMastersListOptions = new JPanel();
		panelMastersList.add(panelMastersListOptions, BorderLayout.SOUTH);
		panelMastersListOptions.setLayout(new FlowLayout(FlowLayout.TRAILING, 5, 5));
		
		btnEngage = new JButton("Engage");
		btnEngage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startEngagement();
			}
		});
		panelMastersListOptions.add(btnEngage);
		
		btnWarriorDetails = new JButton("View Warrior Details");
		btnWarriorDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ConnectedClientState state = getMasterConnectedClientState(table.getSelectedRow());
				
				if(state == null) {
					JOptionPane.showMessageDialog(MainWindow.this, "Unable to load Master State Information", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					displayWarriorViewDialog(state.getWarrior(), WarriorView.EDIT_MODE_NONE);
				}
			}
		});
		panelMastersListOptions.add(btnWarriorDetails);
		
		btnSendMessage = new JButton("Send Message");
		btnSendMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ConnectedClientState state = getMasterConnectedClientState(table.getSelectedRow());
				
				if(state == null) {
					JOptionPane.showMessageDialog(MainWindow.this, "Unable to load Master State Information", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					List<Integer> targets = new ArrayList<Integer>();
					targets.add(state.getClientId());
					createOrActivatePrivateChat(targets);
				}
			}
		});
		panelMastersListOptions.add(btnSendMessage);
		tabbedPane.setEnabledAt(1, true);
		
		panelEngagement = new JPanel();
		tabbedPane.addTab("Engagement", null, panelEngagement, null);
		tabbedPane.setEnabledAt(2, false);
		panelEngagement.setLayout(new BorderLayout(0, 0));
		
		panelEngagementActionBar = new JPanel();
		panelEngagement.add(panelEngagementActionBar, BorderLayout.SOUTH);
		panelEngagementActionBar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		btnEngagementSelectAction = new JButton("Select Action");
		panelEngagementActionBar.add(btnEngagementSelectAction);
		btnEngagementSelectAction.setMaximumSize(new Dimension(9999, 9999));
		
		btnAcceptDeath = new JButton("Accept Death");
		btnAcceptDeath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				engagementAcceptDeath();
			}
		});
		btnAcceptDeath.setVisible(false);
		panelEngagementActionBar.add(btnAcceptDeath);
		
		btnDefyDeath = new JButton("Defy Death");
		btnDefyDeath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engagementDefyDeath();
			}
		});
		btnDefyDeath.setVisible(false);
		panelEngagementActionBar.add(btnDefyDeath);
		btnEngagementSelectAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectActionTargeting();
			}
		});
		
		panelEngagementWarriors = new JPanel();
		panelEngagementWarriors.setBorder(null);
		engagementScrollPane = new JScrollPane(panelEngagementWarriors);
		engagementScrollPane.setBorder(null);
		panelEngagementWarriors.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		panelEngagement.add(engagementScrollPane, BorderLayout.CENTER);
		
		
		
		panelWarriors = new JPanel();
		tabbedPane.addTab("Warriors", null, panelWarriors, null);
		panelWarriors.setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		panel.setBorder(null);
		panel.setBackground(Color.WHITE);
		panelWarriors.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		panelNewWarrior = new JPanel();
		panelNewWarrior.setBackground(Color.WHITE);
		panel.add(panelNewWarrior, BorderLayout.WEST);
		panelNewWarrior.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 2));
		
		btnNewWarrior = new JButton("New");
		btnNewWarrior.setToolTipText("Create a New Warrior");
		btnNewWarrior.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newWarrior();
			}
		});
		panelNewWarrior.add(btnNewWarrior);
		
		btnNewButton = new JButton("Refresh");
		btnNewButton.setToolTipText("Refresh the list of Warriors");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadWarriorListing();
			}
		});
		panelNewWarrior.add(btnNewButton);
		
		panelExistingWarrior = new JPanel();
		panelExistingWarrior.setBackground(Color.WHITE);
		panelExistingWarrior.setVisible(false);
		panel.add(panelExistingWarrior, BorderLayout.CENTER);
		panelExistingWarrior.setLayout(new FlowLayout(FlowLayout.TRAILING, 2, 2));
		
		btnToggleSelectWarrior = new JToggleButton("Use");
		btnToggleSelectWarrior.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleSelectedWarrior();
			}
		});
		btnToggleSelectWarrior.setToolTipText("Choose Selected Warrior to represent you");
		panelExistingWarrior.add(btnToggleSelectWarrior);
		
		btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editWarrior();
			}
		});
		btnEdit.setToolTipText("Edit Selected Warrior");
		panelExistingWarrior.add(btnEdit);
		
		btnDeleteWarrior = new JButton("Del");
		btnDeleteWarrior.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteWarrior(true);
			}
		});
		btnDeleteWarrior.setToolTipText("Delete Selected Warrior");
		panelExistingWarrior.add(btnDeleteWarrior);
				
		listWarriors = new JList<String>();
		listWarriors.setFont(new Font("Monospaced", Font.PLAIN, 11));
		listWarriors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listWarriors.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateWarriorSelectionState();
				updateBtnToggleSelectedState();
			}
		});
		listWarriors.setModel(new DefaultListModel<String>());
		panelWarriors.add(new JScrollPane(listWarriors), BorderLayout.CENTER);
		
		JTabbedPane tabbedPaneChat = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPaneChat.setBackground(Color.WHITE);
		tabbedPaneChat.setBorder(null);
		splitPane.setRightComponent(tabbedPaneChat);
		
		chatAreaTournament = new ChatArea(this);
		chatAreaTournament.setClosed(true);
		tabbedPaneChat.add("Tournament", chatAreaTournament);

		chatAreaEngagement = new ChatArea(null);
		tabbedPaneChat.add("Engagement Log", chatAreaEngagement);
		
		chatAreaConsole = new ChatArea(null);
		tabbedPaneChat.add("Console", chatAreaConsole);
		
		
		loadWarriorListing();
		updateServerConnectionRelatedState();
		updateMasterListOptionState();
	}

	@Override
	public void handleInvalidMessage(InvalidMessage message) {
		chatAreaConsole.appendText("Sent Message was Rejected by Server | Message: %s | Reason: %s %n", message.getMessage().getClass().getSimpleName(), message.getReason());
	}

	@Override
	public void notifyException(Exception e) {
		if(e instanceof IOException) {
			disconnect();
		}
		chatAreaConsole.appendText("Client Exception: %s%n", e);
	}

	@Override
	public void notifyCurrentWarriorChanged(WarriorBase currentWarrior) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRelayedTextMessage(RelayedTextMessage message) {
		List<Integer> reciversList = new ArrayList<Integer>(message.getReceiversClientIds().size());
		
		// Add in the sender, because we want a list of all clients taking part in the chat.
		reciversList.add(message.getSenderClientId());
		
		for(Integer clientId : message.getReceiversClientIds()) {
			if(clientId == client.getClientId()) {
				continue;
			}
			reciversList.add(clientId);
		}
		
		createOrAppendPrivateChat(message.getSenderClientId(), reciversList, message.getMessage());
	}

	@Override
	public void handleServerTextMessage(ServerTextMessage message) {
		chatAreaTournament.appendText("[Tournament] %s%n", message.getMessage());
	}

	@Override
	public void handleSetClientIdMessage(SetClientIdMessage message) {
		chatAreaTournament.appendText("Tournament Organizers have assigned you the Master Identifier of %d%n", message.getClientId());
	}

	@Override
	public void handleConnectedClientsListMessage(ConnectedClientsListMessage message) {
		chatAreaTournament.appendText("Received a fresh list of participating Masters from the Tournament Organizers%n");
		for(ConnectedClientState state : message.getWarriors()) {
			updateMasterListRow(state.getClientId());
		}
	}

	@Override
	public void handleEngagementCombatResultMessage(EngagementCombatResultMessage message) {
		for(Entry<Integer, EngagedWarrior> entry : engagedWarriors.entrySet()) {
			entry.getValue().selectedActionStale();
			
			for(WarriorCombatResult result : message.getWarriorCombatResults()) {
				if(entry.getKey() == result.getClientId()) {
					entry.getValue().setCombatResult(result);
				}
			}
		}
		
		updateEngagementState();
	}

	@Override
	public void handleEngagementStartedMessage(EngagementStartedMessage message) {
		tabbedPaneEnablePanel(panelEngagement, true);
		panelEngagementWarriors.removeAll();
		engagedWarriors.clear();
		
		List<Integer> orderedInvolvedClientIds = new ArrayList<Integer>(message.getInvolvedClientIds());
		
		// Adjust the ordering so our client is always listed first
		boolean removeSuccess = orderedInvolvedClientIds.remove((Integer)client.getClientId());
		assert removeSuccess : "Involved client ID list should include our client ID";			
		orderedInvolvedClientIds.add(0, client.getClientId());
		
		
		for(int clientId : orderedInvolvedClientIds) {
			EngagedWarrior engagedWarrior = new EngagedWarrior(
					client, 
					client.getConnectedClientById(clientId), 
					this
			);
			
			engagedWarriors.put(
					clientId, 
					engagedWarrior
			);

			panelEngagementWarriors.add(engagedWarrior);
		}
		
		// Target the first opponent by default
		engagedWarriors.get(orderedInvolvedClientIds.get(1)).setTarget(true);
		
		panelEngagementWarriors.validate();
		chatAreaEngagement.appendText("An Engagement has Started%n");
		updateEngagementState();
	}

	@Override
	public void handleEngagementEndedMessage(EngagementEndedMessage message) {
		engagedWarriors.clear();
		tabbedPaneEnablePanel(panelEngagement, false);
		chatAreaEngagement.appendText("The Engagement has Ended%n");
		updateEngagementState();
	}

	@Override
	public void handleEngagementActionSelectedMessage(EngagementActionSelectedMessage message) {
		EngagedWarrior engagedWarrior = engagedWarriors.get(message.getSelectedAction().getClientId());
		
		if(engagedWarrior != null) {
			engagedWarrior.setSelectedAction(message.getSelectedAction());
		} else {
			chatAreaConsole.appendText("Received Action Selected Message regarding %d, who we do not appear to be engaged with at present", message.getSelectedAction().getClientId());
		}
		updateEngagementState();
	}

	@Override
	public void handleClientConnectedMessage(ClientConnectedMessage message) {
		updateMasterListRow(message.getConnectedClientState().getClientId());
		chatAreaTournament.appendText("A Master has joined the Tournament, they have been assigned the Identifier %d%n", message.getConnectedClientState().getClientId());
	}

	@Override
	public void handleClientDisconnectedMessage(ClientDisconnectedMessage message) {
		updateMasterListRow(message.getConnectedClientState().getClientId());
		chatAreaTournament.appendText("The Master identified as %d has left the Tournament%n", message.getConnectedClientState().getClientId());
	}

	@Override
	public void handleClientStateChangedMessage(ClientStateChangedMessage message) {
		updateMasterListRow(message.getConnectedClientState().getClientId());
		updateMasterListOptionState();
		
		if(message.getConnectedClientState().getClientId() == client.getClientId()) {
			if(currentWarrior != null && 
					client.getCurrentWarrior() != null && 
					currentWarrior.getName().equals(client.getCurrentWarrior()) == false) {
				chatAreaTournament.appendText("The Tournament Organizers welcome your %s, %s, to the field of battle %n", client.getCurrentWarrior().getTypeName(), client.getCurrentWarrior().getName());
			}
			
			updateCurrentWarrior(client.getCurrentWarrior());
		}
		
		EngagedWarrior engagedWarrior = engagedWarriors.get(message.getConnectedClientState().getClientId());
		
		if(engagedWarrior != null) {
			ConnectedClientState state = client.getConnectedClientById(message.getConnectedClientState().getClientId());
			if(state != null) {
				engagedWarrior.setState(state);
			}
		}
		
		updateEngagementState();
	}

	@Override
	public void handleRelayedTextToAllMessage(RelayedTextToAllMessage message) {
		
		if(message.getSenderClientId() == client.getClientId()) {
			return;
		}
		
		String name;
		
		ConnectedClientState state = client.getConnectedClientById(message.getSenderClientId());
		
		if(state != null) {
			name = ClientFormatHelper.getMasterNameFromState(state, false);
		} else {
			name = ((Integer)message.getSenderClientId()).toString();
		}
		
		chatAreaTournament.appendText("%s > %s%n", name, message.getMessage());
	}
	
	@Override
	public void onWarriorViewDialogClosed(WarriorBase warrior) {
		if(warrior != null) {
			presistChangedWarrior(warrior);
		}
	}
	
	@Override
	public void notifyTargetStatusChanged(EngagedWarrior sender, boolean isTargeted) {
		if(isTargeted == false) {
			if(sender.getClientId() == targetClientId) {
				targetClientId = -1;
			}
		} else {
			targetClientId = sender.getClientId();
			for(Entry<Integer, EngagedWarrior> entry : engagedWarriors.entrySet()) {
				if(targetClientId != entry.getKey()) {
					entry.getValue().setTarget(false);
				}
			}
		}
	}
	
	@Override
	public void notifyOpenPrivateChat(int clientId) {
		List<Integer> chatTargets = new ArrayList<Integer>();
		chatTargets.add(clientId);
		createOrActivatePrivateChat(chatTargets);
	}
	
	public void notifyOpenWarriorDetails(WarriorBase warrior) {
		displayWarriorViewDialog(warrior, WarriorView.EDIT_MODE_NONE);
	}

	@Override
	public boolean notifyTextEntered(ChatArea source, String text) {
		
		if(source == chatAreaTournament && client != null) {
			try {
				client.sendMessage(new SendTextToAllMessage(text));
			} catch (IOException e) {
				source.appendText("Failed to send message: %s", e);
				return false;
			}
		}
		
		return true;
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

	private void joinTournament() {
		Connection connection;
		try {
			connection = new SocketConnection(new Socket(textFieldAddress.getText(), (int)textFieldPort.getValue()));
		} catch (IOException e) {
			chatAreaConsole.appendText("Unable to join Tournament: %s%n", e.getMessage());
			return;
		}
		client = new SwingSafeClient(connection, this);
		clientThread = new Thread(client);
		clientThread.start();
		
		requestMasterList();
		
		if(currentWarrior != null) {
			updateCurrentWarriorToServer(currentWarrior);
		}
		updateServerConnectionRelatedState();
		chatAreaTournament.setClosed(false);
	}
	
	private void leaveTournament() {
		disconnect();
	}
	
	private void disconnect() {
		try {
			if(client != null) {
				client.getConnection().close();
			}
		} catch (IOException e) {
			// Don't care
		}
		
		if(client != null) {
			chatAreaTournament.appendText("--Left Tournament--%n");
		}
		
		client = null;
		clientThread = null;
		clearMasterList();
		clearPrivateChats();
		updateEngagementState();
		updateServerConnectionRelatedState();
		chatAreaTournament.setClosed(true);
	}
	
	private void requestMasterList() {
		if(client == null) {
			chatAreaConsole.appendText("Unable to send client message request: %s%n", "No Connection");
			return;
		}
		
		try {
			client.sendMessage(new GetConnectedClientsListMessage());
		} catch (IOException e) {
			chatAreaConsole.appendText("Unable to send client message request: %s%n", e.getMessage());
			disconnect();
			return;
		}
	}
	
	private void updateMasterListRow(int clientId) {

		if(client == null) {
			clearMasterList();
			return;
		}


		ConnectedClientState state = client.getConnectedClientById(clientId);

		int row = findMasterRowInList(clientId);

		if(row == -1 && state != null) {
			tableModel.addRow(new Object[]{clientId});
			row = findMasterRowInList(clientId);
			assert row != -1;
			if(row != -1) {
				setMasterListRowAt(state, row);
			}
		} else if(state == null) {
			tableModel.removeRow(row);
		} else {
			setMasterListRowAt(state, row);
		}			
	}
	
	private void clearMasterList() {
		while(tableModel.getRowCount() != 0) {
			tableModel.removeRow(0);
		}
	}
	
	private int findMasterRowInList(int clientId) {
		for(int row = 0; row < tableModel.getRowCount(); row++) {
			if((int)tableModel.getValueAt(row, 0) == clientId) {
				return row;
			}
		}
		return -1;
	}
	
	private void setMasterListRowAt(ConnectedClientState state, int row) {
		synchronized (tableModel) {		
			int column = 0;
			// "ID", "E", "Warrior: Name", "Type", "HP", "Weapon"
			tableModel.setValueAt(state.getClientId(), row, column++);
			tableModel.setValueAt(state.isInEngagement(), row, column++);
			
			WarriorBase warrior = state.getWarrior();
			
			String name = "", type = "Spectator", weapon = "";
			Integer health = null;
			
			if(warrior != null) {
				name = warrior.getName();
				type = warrior.getTypeName();
				weapon = warrior.getEquippedWeapon().getName();
				health = warrior.getHealth();
			}
			
			tableModel.setValueAt(type, row, column++);
			tableModel.setValueAt(name, row, column++);
			tableModel.setValueAt(health, row, column++);
			tableModel.setValueAt(weapon, row, column++);
		}
	}
	
	
	private JButton btnEdit;
	private JPanel panelEngagementActionBar;
	private JPanel panelEngagementWarriors;
	private JPanel panelMastersListOptions;
	private JButton btnEngage;
	private JButton btnSendMessage;
	private JButton btnWarriorDetails;
	private JButton btnEngagementSelectAction;
	private JScrollPane engagementScrollPane;
	private JPanel panel_1;
	private JButton btnLeaveTournament;
	private JButton btnAcceptDeath;
	private JButton btnDefyDeath;
	
	private static final Set<String> KNOWN_WARRIOR_FILE_EXTENTIONS;
	static {
		Set<String> tmpList = new HashSet<String>();
		
		tmpList.add(Warrior.FILE_EXTENSION);
		tmpList.add(UndeadWarrior.FILE_EXTENSION);
		
		KNOWN_WARRIOR_FILE_EXTENTIONS = Collections.unmodifiableSet(tmpList);
	}
	
	private static final Set<String> KNOWN_PARAM_REQ_WARRIOR_FILE_EXTENTIONS;
	static {
		Set<String> tmpList = new HashSet<String>();
		
		tmpList.add(CryptoWarrior.FILE_EXTENSION);
		
		KNOWN_PARAM_REQ_WARRIOR_FILE_EXTENTIONS = Collections.unmodifiableSet(tmpList);
	}
	
	private void loadWarriorListing() {
		loadedWarriors.clear();
		
		String prevSelected = listWarriors.getSelectedValue();
		
		DefaultListModel<String> model = (DefaultListModel<String>)(listWarriors.getModel()); 
		model.clear();
		for(File file : warriorFolder.getListOfWarriorFiles(KNOWN_WARRIOR_FILE_EXTENTIONS)) {
			WarriorBase warrior;
			try {
				warrior = warriorFolder.loadWarrior(file.getName());
			} catch (ClassNotFoundException | InvocationTargetException | IOException | UnusableWeaponException e) {
				chatAreaConsole.appendText("Loading Warrior in file %s (%s) failed: %s%n",
						file.getName(),
						file.getAbsolutePath(),
						e
				);
				continue;
			}
			
			model.addElement(getWarriorListString(warrior));
			loadedWarriors.put(model.getSize()-1, warrior);
		}
		
		for(File file : warriorFolder.getListOfWarriorFiles(KNOWN_PARAM_REQ_WARRIOR_FILE_EXTENTIONS)) {
			
			String hshName = file.getName().split("\\.")[0];
			
			if(hshName.length() > 7) {
				hshName = hshName.substring(0, 8);
			}
			
			model.addElement(
					String.format("%18s %s", "???", hshName)
			);
			loadedParamsReqWarriors.put(model.getSize()-1, file);
		}
		
		listWarriors.setSelectedValue(prevSelected, true);
	}
	
	private WarriorBase getWarriorFromSelected() {
		if(listWarriors.isSelectionEmpty()) {
			return null;
		}
		
		WarriorBase warrior = loadedWarriors.get(listWarriors.getSelectedIndex());
		
		if(warrior == null) {
			File file = loadedParamsReqWarriors.get(listWarriors.getSelectedIndex());
			
			if(file != null) {			
				warrior = loadParamsReqWarrior(file.getName());
				
				if(warrior != null) {
					loadedWarriors.put(listWarriors.getSelectedIndex(), warrior);
					DefaultListModel<String> model = (DefaultListModel<String>)(listWarriors.getModel()); 
					model.set(listWarriors.getSelectedIndex(), getWarriorListString(warrior));
				}
				return warrior;
			}
		}
		
		if(warrior == null) {
			chatAreaConsole.appendText("Unable to locate index to load warrior%n");
			assert false : "The loaded warriors list and visual warrior list should be in perfect sync";
			return null;
		}
		
		return warrior;
	}
	
	private String getWarriorListString(WarriorBase warrior) {
		return String.format("%18s %-15s wielding %s", warrior.getTypeName(), warrior.getName(), warrior.getEquippedWeapon().getName());
	}
	
	private WarriorBase loadParamsReqWarrior(String name) {
		WarriorBase warrior;
		Map<String, String> params = new HashMap<String, String>();
		while(true) {
			try {
				try {
					warrior = warriorFolder.loadWarrior(name, params);
				} catch(InvocationTargetException e) {
					throw e.getCause();
				}
			} catch (CryptoKeyProblemUnlockException e) {

				JPasswordField passwordField = new JPasswordField();
				Object[] promptFields = {"Crypto Key Required", passwordField};
				
				int result = JOptionPane.showOptionDialog(this, promptFields, e.getMessage(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
				
				//String key = JOptionPane.showInputDialog(this, "Crypto Key Required", e.getMessage(), JOptionPane.INFORMATION_MESSAGE);
				
				if(JOptionPane.OK_OPTION != result) {
					return null;
				}
				
				params.put("unlockKey", new String(passwordField.getPassword()));
				continue;
			} catch (Throwable e) {
				if(e instanceof Exception) {
					JOptionPane.showMessageDialog(this, "Error loading Warrior: " + e, "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return null;
				} else if(e instanceof Error){
					throw (Error)e;
				}
				assert false : "Should cover all exception cases";
				throw new IllegalStateException(e);
			}
			break;
		}
		return warrior;
	}
	
	private void newWarrior() {
		displayWarriorViewDialog(null, WarriorView.EDIT_MODE_NEW);
	}
	
	private void presistChangedWarrior(WarriorBase warrior) {
		try {
			warriorFolder.saveWarrior(warrior);
			
			if(currentWarrior != null && warrior.getName().equals(currentWarrior.getName())) {
				updateCurrentWarrior(warrior);
				updateCurrentWarriorToServer(warrior);
			}
			
			updateCachedLoadedWarrior(warrior);
			
			loadWarriorListing();
		} catch (IOException e) {
			chatAreaConsole.appendText("Failed to save new Warrior due to I/O error: %s%n", e);
		}
	}
	
	private void toggleSelectedWarrior() {
		WarriorBase warrior = getWarriorFromSelected();
		
		if(warrior != null) {
			if(currentWarrior != null && warrior.getName().equals(currentWarrior.getName())) {
				warrior = null;
			}
			updateCurrentWarrior(warrior);
			updateCurrentWarriorToServer(warrior);
			
			updateBtnToggleSelectedState();
		}
	}
	
	private void editWarrior() {
		WarriorBase warrior = getWarriorFromSelected();
		
		if(warrior != null) {
			displayWarriorViewDialog(warrior, WarriorView.EDIT_MODE_EDIT);
		}
	}
	
	private void deleteWarrior(boolean prompt) {
		WarriorBase warrior = getWarriorFromSelected();
		
		if(warrior == null) {
			return;
		} else if(currentWarrior != null && currentWarrior.getName().equals(warrior.getName())) {
			chatAreaConsole.appendText("Unable to delete the in-use Warrior %s", currentWarrior.getName());
			return;
		}
		if(prompt) {
			int result = JOptionPane.showConfirmDialog(
					MainWindow.this, 
					String.format("Are you sure you want to delete warrior %s?", warrior.getName()), 
					"Confirm Delete", 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			
			if(result != JOptionPane.YES_OPTION) {
				return;
			}
		}

		try {
			warriorFolder.deleteWarrior(warrior);
			loadWarriorListing();
		} catch (IOException e) {
			chatAreaConsole.appendText("Failed to delete warrior: %s%n", e);
		}
	}

	private void updateCurrentWarriorToServer(WarriorBase warrior){
		if(client != null) {
			try {
				client.sendMessage(new SetWarriorMessage(this.currentWarrior));
			} catch (IOException e) {
				
				disconnect();
				return;
			}
		}
	}
	
	private void updateCurrentWarrior(WarriorBase warrior) {
		saveCurrentWarrior();
		
		if(this.currentWarrior != null && warrior != null &&
				this.currentWarrior.getName().equals(warrior.getName()) && 
				this.currentWarrior.getClass() == warrior.getClass() && 
				this.currentWarrior instanceof CryptoWarrior) {
			CryptoWarrior oldCWarrior = (CryptoWarrior)this.currentWarrior;
			CryptoWarrior newCWarrior = (CryptoWarrior)warrior;
			
			newCWarrior.setKey(oldCWarrior.getKey());
			newCWarrior.setSalt(oldCWarrior.getSalt());
			newCWarrior.setIterations(oldCWarrior.getIterations());
		}
		
		this.currentWarrior = warrior;
	}
	
	private void saveCurrentWarrior() {
		if(currentWarrior != null) {
			try {
				warriorFolder.saveWarrior(currentWarrior);
			} catch (IOException e) {
				chatAreaConsole.appendText("Failed to save Warrior: %s%n", e);
			}
			
			updateCachedLoadedWarrior(currentWarrior);
		}
	}

	private void updateCachedLoadedWarrior(WarriorBase warriorBase) {
		for(Entry<Integer, WarriorBase> entry : loadedWarriors.entrySet()) {
			if(entry.getValue().getName().equals(warriorBase.getName())) {
				entry.setValue(warriorBase);
			}
		}
	}

	private void updateBtnToggleSelectedState() {
		WarriorBase selectedWarrior = getWarriorFromSelected();
		if(selectedWarrior != null && currentWarrior != null && selectedWarrior.getName().equals(currentWarrior.getName())) {
			btnToggleSelectWarrior.setSelected(true);
			btnToggleSelectWarrior.setText("In Use");
		} else {
			btnToggleSelectWarrior.setSelected(false);
			btnToggleSelectWarrior.setText("Use");
		}	
	}

	private void updateMasterListOptionState() {
		panelMastersListOptions.setVisible(table.getSelectedRow() > -1);
		
		ConnectedClientState state = getMasterConnectedClientState(table.getSelectedRow());
		
		if(state == null) {
			btnEngage.setEnabled(false);
			btnEngage.setToolTipText("Error loading Master State Data");
		} else if (state.getClientId() == client.getClientId()) {
			btnEngage.setEnabled(false);
			btnEngage.setToolTipText("Can't start an engagement with yourself");
		} else if (currentWarrior == null) {
			btnEngage.setEnabled(false);
			btnEngage.setToolTipText("Can't start an engagement until you select a Warrior");
		} else if (currentWarrior.getEquippedWeapon().equals(Armory.NO_WEAPON)) {
			btnEngage.setEnabled(false);
			btnEngage.setToolTipText("Can't start an engagement until you select a Weapon for your Warrior");
		} else if(currentWarrior.isDead()) {
			btnEngage.setEnabled(false);
			btnEngage.setToolTipText("Can't start an engagement with Dead Warrior selected");
		} else if (state.isInEngagement() == true) {
			btnEngage.setEnabled(false);
			btnEngage.setToolTipText("Master is already in an Engagement");
		} else if(state.getWarrior() == null) {
			btnEngage.setEnabled(false);
			btnEngage.setToolTipText("Master is spectating and cannot be engaged");
		} else if (state.getWarrior().getEquippedWeapon().equals(Armory.NO_WEAPON)) {
			btnEngage.setEnabled(false);
			btnEngage.setToolTipText("Warrior has no Weapon Equipped");
		} else if(state.getWarrior().isDead()) {
			btnEngage.setEnabled(false);
			btnEngage.setToolTipText("Warrior is Dead");
		} else {
			btnEngage.setEnabled(true);
			btnEngage.setToolTipText(null);
		}
		
		if(state == null) {
			btnWarriorDetails.setEnabled(false);
			btnWarriorDetails.setToolTipText("Error loading Master State Data");
		} else if(state.getWarrior() == null) {
			btnWarriorDetails.setEnabled(false);
			btnWarriorDetails.setToolTipText("Master has not chosen a Warrior");
		} else {
			btnWarriorDetails.setEnabled(true);
			btnWarriorDetails.setToolTipText(null);
		}
		
		if(state == null) {
			btnSendMessage.setEnabled(false);
			btnSendMessage.setToolTipText("Error loading Master State Data");
		} else if (state.getClientId() == client.getClientId()) {
			btnSendMessage.setEnabled(false);
			btnSendMessage.setToolTipText("Can't message yourself");
		} else {
			btnSendMessage.setEnabled(true);
			btnSendMessage.setToolTipText(null);
		}
	}
	
	private void updateServerConnectionRelatedState() {
		boolean connected = client != null && client.getConnection().isClosed() == false;
		
		btnJoinTournament.setVisible(!connected);
		btnLeaveTournament.setVisible(connected);
		
		textFieldAddress.setEnabled(!connected);
		textFieldPort.setEnabled(!connected);
		
		tabbedPaneEnablePanel(panelMastersList, connected);
	}
	
	private void updateEngagementState() {
		
		if(client == null || client.isInEngagement() == false) {
			tabbedPaneEnablePanel(panelEngagement, false);
		} else if(client.isInEngagement()) {
			tabbedPaneEnablePanel(panelEngagement, true);
		}
		
		boolean isAlive, isDead;
		isAlive = isDead = false;
		
		if(currentWarrior != null) {
			isAlive = currentWarrior.isAlive();
			isDead = currentWarrior.isDead();
		}
		
		btnEngagementSelectAction.setVisible(isAlive);
		btnAcceptDeath.setVisible(isDead);
		btnDefyDeath.setVisible(isDead);
		
		
		boolean actionSelected = true;
		String stateToolTip = "Not Connected";
		
		if(client != null) {
			
			if(client.isInEngagement() == false) {
				actionSelected = true;
				stateToolTip = "Engagement Over";
			} else if (client.isActionSelected()) {
				actionSelected = true;
				stateToolTip = "Action already selected for this round";
			} else {
				actionSelected = false;
				stateToolTip = null;
			}
		}
		
		btnEngagementSelectAction.setEnabled(!actionSelected);
		btnEngagementSelectAction.setToolTipText(stateToolTip);
		btnAcceptDeath.setEnabled(!actionSelected);
		btnAcceptDeath.setToolTipText(stateToolTip);
		btnDefyDeath.setEnabled(!actionSelected);
		btnDefyDeath.setToolTipText(stateToolTip);
		
		if(currentWarrior instanceof UndeadWarrior) {
			btnDefyDeath.setEnabled(false);
			btnDefyDeath.setToolTipText("Death shall not be defied again");
		} else if(currentWarrior instanceof CryptoWarrior) {
			btnDefyDeath.setEnabled(false);
			btnDefyDeath.setToolTipText("The Warrior's Crypto seal renders them unable to become undead");
		}
		
		{
			boolean isEngaged = false;
			String toolTip = null;
			
			if(client != null) {
				isEngaged = client.isInEngagement();
				
				if(isEngaged) {
					toolTip = "Cannot be used while in an engagement";
				}
			}
			
			btnEdit.setEnabled(!isEngaged);
			btnEdit.setToolTipText(toolTip);
			btnToggleSelectWarrior.setEnabled(!isEngaged);
			btnToggleSelectWarrior.setToolTipText(toolTip);
		}
	}
	
	private void updateWarriorSelectionState() {
		panelExistingWarrior.setVisible(!listWarriors.isSelectionEmpty());		
	}
	
	private ConnectedClientState getMasterConnectedClientState(int row) {
		if(row < 0 || client == null) {
			return null;
		}
		
		Object value = table.getValueAt(row, 0);
		
		if(value == null || value instanceof Integer == false) {
			return null;
		}
		
		return client.getConnectedClientById((int)value);
	}
	
	private void startEngagement() {
		ConnectedClientState state = getMasterConnectedClientState(table.getSelectedRow());
		
		if(currentWarrior == null) {
			JOptionPane.showMessageDialog(this, "Please Select a Warrior to use first on the Warriors tab", "Please Select a Warrior", JOptionPane.ERROR_MESSAGE);
			return;
		} else if(currentWarrior.getEquippedWeapon().equals(Armory.NO_WEAPON)) {
			JOptionPane.showMessageDialog(this, "Please Equip your Warrior with a weapon first on the Warriors tab", "Please give your Warrior a Weapon", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(state != null) {
			if(isAlreadyEngagedInformer(state)) {
				return;
			}
			
			ActionSelectorDialog actionSelectorDialog = 
					createActionSelectDialog(currentWarrior.getEquippedWeapon().getActions());

			String result = actionSelectorDialog.showDialog();
			
			if(result.equals("OK")) {
				
				if(isAlreadyEngagedInformer(state)) {
					return;
				}
				
				Action selectedAction = actionSelectorDialog.getSelectedAction();
				
				if(selectedAction == null) {
					JOptionPane.showMessageDialog(this, "Please Select an Action by highlighting it on the list to carry out the Engagement", "Please Select an Action", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				try {
					client.sendMessage(new TakeActionMessage(selectedAction, state.getClientId()));
				} catch (IOException e) {
					disconnect();
					JOptionPane.showMessageDialog(this, "Starting Engagement Failed: " + e, "Network Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				tabbedPane.setSelectedComponent(panelEngagement);
			} else {
				return;
			}
			
		}
	}
	
	private void selectActionTargeting() {
		int clientId = -1;
		for(EngagedWarrior engagedWarrior : engagedWarriors.values()) {
			if(engagedWarrior.isTarget()) {
				clientId = engagedWarrior.getClientId();
				break;
			}
		}
		
		if(clientId == -1) {
			JOptionPane.showMessageDialog(this, "Please select a Warrior to target first", "No Target Selected", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ConnectedClientState state = client.getConnectedClientById(clientId);
		
		if(state == null) {
			JOptionPane.showMessageDialog(this, "Unable to load Target's State", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ActionSelectorDialog actionSelectorDialog = 
				createActionSelectDialog(currentWarrior.getEquippedWeapon().getActions());

		String result = actionSelectorDialog.showDialog();
		
		if(result.equals("OK")) {
			Action selectedAction = actionSelectorDialog.getSelectedAction();
			
			if(selectedAction == null) {
				JOptionPane.showMessageDialog(this, "Please Select an Action by highlighting it on the list to carry out the Engagement", "Please Select an Action", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			try {
				client.sendMessage(new TakeActionMessage(selectedAction, state.getClientId()));
			} catch (IOException e) {
				disconnect();
				JOptionPane.showMessageDialog(this, "Starting Engagement Failed: " + e, "Network Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			tabbedPane.setSelectedComponent(panelEngagement);
		} else {
			return;
		}
		
	}
	
	private void engagementAcceptDeath() {
		try {
			client.sendMessage(new TakeActionMessage(Armory.DEATH, client.getClientId()));
		} catch (IOException e) {
			disconnect();
			JOptionPane.showMessageDialog(this, "Failed to accept death due to a connection error: " + e, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void engagementDefyDeath() {
		try {
			UndeadWarrior warrior = new UndeadWarrior(
					currentWarrior.getName(), 
					currentWarrior.getOriginLocation(), 
					currentWarrior.getDescription(), 
					SharedConstant.UNDEAD_STARTING_HEALTH,
					Armory.BONE_CLUB
			);
			updateCurrentWarrior(warrior);
			updateCurrentWarriorToServer(warrior);
		} catch (UnusableWeaponException e) {
			assert false : "Weapon should never be unusable";
			JOptionPane.showMessageDialog(MainWindow.this, "Failed to defy death because the Warrior was unable to break a bone off to use as a weapon: " + e, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private boolean isAlreadyEngagedInformer(ConnectedClientState state) {
		if(state.isInEngagement()) {
			JOptionPane.showMessageDialog(this, "This Master is already in an Engagement.", "Master already Engaged", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		return false;
	}

	private void tabbedPaneEnablePanel(JPanel panel, boolean enable) {
		for(int i = 0; i < tabbedPane.getTabCount(); i++) {
			if(tabbedPane.getComponentAt(i).equals(panel)) {
				tabbedPane.setEnabledAt(i, enable);
			}
		}
	}
	
	private void createOrAppendPrivateChat(Integer from, List<Integer> chatTargets, String text) {
		ChatWindow chatWindow = getPrivateChatWindow(chatTargets);
		
		if(chatWindow != null) {
			chatWindow.appendText(from, text);
		} else {
			createPrivateChat(chatTargets).appendText(from, text);
		}
	}
	
	private void createOrActivatePrivateChat(List<Integer> chatTargets) {
		ChatWindow chatWindow = getPrivateChatWindow(chatTargets);
		
		if(chatWindow == null) {
			chatWindow = createPrivateChat(chatTargets);
		}
		
		if(chatWindow.getExtendedState() == JFrame.ICONIFIED) {
			chatWindow.setExtendedState(JFrame.NORMAL);
		}
		
		chatWindow.toFront();
		chatWindow.repaint();
	}
	
	private ChatWindow createPrivateChat(List<Integer> chatTargets) {
		ChatWindow chatWindow = new ChatWindow(client, chatTargets);
		chatWindow.setLocationRelativeTo(this);
		chatWindow.setVisible(true);
		chatWindows.add(chatWindow);
		return chatWindow;
	}
	
	private ChatWindow getPrivateChatWindow(List<Integer> chatTargets) {
		for(ChatWindow chatWindow : chatWindows) {
			if(chatWindow.getChatTargets().size() == chatTargets.size() && 
					chatWindow.getChatTargets().containsAll(chatTargets)) {
				return chatWindow;
			}
		}
		return null;
	}
	
	private void clearPrivateChats() {
		for(ChatWindow chatWindow : chatWindows) {
			chatWindow.closed();
		}
		
		chatWindows.clear();
	}
	
	private void displayWarriorViewDialog(WarriorBase warrior, int editingMode) {
		WarriorViewDialog warriorViewDialog = new WarriorViewDialog(warrior, editingMode, this);
		warriorViewDialog.setLocationRelativeTo(this);
		warriorViewDialog.setVisible(true);
	}
	
	private ActionSelectorDialog createActionSelectDialog(List<Action> actions) {
		ActionSelectorDialog dialog = new ActionSelectorDialog(actions);
		dialog.setLocationRelativeTo(this);

		return dialog;
	}
}
