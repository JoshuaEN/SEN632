package ojdev.client.ui.console;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import ojdev.client.ClientUserInterface;
import ojdev.client.WarriorFolder;
import ojdev.common.Armory;
import ojdev.common.ConnectedClientState;
import ojdev.common.SharedConstant;
import ojdev.common.WarriorCombatResult;
import ojdev.common.actions.Action;
import ojdev.common.connections.Connection;
import ojdev.common.connections.SocketConnection;
import ojdev.common.message_handlers.ServerMessageHandler;
import ojdev.common.messages.InvalidMessage;
import ojdev.common.messages.client.GetConnectedClientsListMessage;
import ojdev.common.messages.client.SendTextMessage;
import ojdev.common.messages.client.SendTextToAllMessage;
import ojdev.common.messages.client.SetWarriorMessage;
import ojdev.common.messages.client.TakeActionMessage;
import ojdev.common.messages.server.*;
import ojdev.common.test.DebugMode;
import ojdev.common.warriors.Warrior;
import ojdev.common.warriors.WarriorBase;
import ojdev.common.warriors.WarriorBase.UnusableWeaponException;
import ojdev.common.weapons.Weapon;

public class ClientConsoleInterfaceController implements ServerMessageHandler, ClientUserInterface {

	private final boolean DEBUG_DISABLE_CHECKS = false;
	
	private final ConsoleFormattedPrinter printer;
	private final Scanner scanner;

	private final WarriorFolder warriorFolder;
	
	private ClientWithLock client = null;
	private Thread clientThread = null;
	
	private WarriorBase currentWarrior = null;

	private static String INVALID_MASTER_IDENTIFIER = "Those symbols do not appear to be a number, let alone a Master Identifier";
	private static String UNKNOWN_MASTER_IDENTIFIER = "The scribe assigned by the Tournament to you claims to have no record of that Master Identifier";
	private static String WARRIOR_NOT_SELECTED = ": Your assistant points out you haven't chosen your warrior...";
	private static String NOT_CONNECTED = ": Your assistant reminds you you haven't joined a Tournament yet...";
	
	public ClientConsoleInterfaceController(Writer out, Reader in, WarriorFolder warriorFolder) {
		this.printer = new ConsoleFormattedPrinter(new PrintWriter(out));
		this.warriorFolder = warriorFolder;
		if(in != null) {
			this.scanner = new Scanner(in);
		} else {
			this.scanner = null;
		}
	}
	
	private static final Set<String> KNOWN_WARRIOR_FILE_EXTENTIONS;
	static {
		Set<String> tmpList = new HashSet<String>();
		
		tmpList.add(Warrior.FILE_EXTENSION);
		
		KNOWN_WARRIOR_FILE_EXTENTIONS = Collections.unmodifiableSet(tmpList);
	}
	
	private static final Set<String> KNOWN_WARRIOR_TYPES;
	static {
		Set<String> tmpList = new HashSet<String>();
		
		tmpList.add(Warrior.TYPE_NAME);
		
		KNOWN_WARRIOR_TYPES = Collections.unmodifiableSet(tmpList);
	}
	
	private final Object clientLock = new Object();
	
	private void updateCurrentWarriorToServer(WarriorBase warrior) throws IOException {
		if(client != null) {
			client.sendMessage(new SetWarriorMessage(this.currentWarrior));
		}
	}
	
	private void updateCurrentWarrior(WarriorBase warrior) {
		saveCurrentWarrior();		
		this.currentWarrior = warrior;	
	}
	
	public void start() {
		printer.printString(
				"Hello Master, how may I serve you?%n"
		);
		do {
			printer.printString("> ");
			parse(scanner.nextLine());
		} while(true);
	}
	
	/**
	 * Parses the given command, performing the request action if possible.
	 * @param fullCommand the command to parsed
	 * @return boolean indicating if an error was encountered
	 * @throws Exception 
	 */
	public boolean parse(String fullCommand) {
		try {
			parseMainCommandSet(fullCommand);
			return true;
		} catch(IOException e) {
			printer.printError(": Communication between you and the Tournament has broken down and the \"experts\" just keep babbling a cryptic message: ", e);
			
			if(SharedConstant.DEBUG && SharedConstant.DEBUG_MODE == DebugMode.VERBOSE) {
				printer.printStackTrace(e);
			}
			
			disconnect();
		} catch(IllegalArgumentException | IllegalStateException e) {
			printer.printError("%s%n", e.getMessage());
			if(SharedConstant.DEBUG && SharedConstant.DEBUG_MODE == DebugMode.VERBOSE) {
				printer.printStackTrace(e);
			}
		} catch(Exception e) {
			printer.printError(e);
			if(SharedConstant.DEBUG && SharedConstant.DEBUG_MODE == DebugMode.VERBOSE) {
				printer.printStackTrace(e);
			}
		}
		return false;
	}
	
	private void parseMainCommandSet(String fullCommand) throws Exception {
		String[] commandSplit = parseCommand(fullCommand);
		
		String command = null,
				params = null;
		
		// Disregard empty commands.
		if(fullCommand.length() == 0) {
			printer.printString(": Your assistant jumps to attention, but slowly lowers themselves back into their seat as it becomes clear you don't intend to actually say anything%n");
			return;
		}

		command = commandSplit[0].toLowerCase();
		
		if(commandSplit.length > 1) {
			params = commandSplit[1];
		} else {
			params = "";
		}
				
		synchronized (clientLock) {
			
			// Help / ?
			if(commandMatcher(command, new String[]{"help"}, new String[]{"?"})) {
				commandHelp(command, params);
				
			// Masters
			} else if(commandMatcher(command, new String[]{"masters"})) {
				commandMasterList(command, params);
				
			// Message / Chat / Text / Send
			} else if(commandMatcher(command, new String[]{"message", "msg", "text", "send"}, new String[]{"chat"})) {
				commandMessage(command, params);
				
			// Connect / Join
			} else if(commandMatcher(command, new String[]{"connect", "join"})) {
				commandConnect(command, params);
			
			// Disconnect / Leave
			} else if(commandMatcher(command, new String[]{"disconnect", "leave"}, new String[]{"dc"})) {
				commandDisconnect(command, params);
				
			// Weapon
			} else if(commandMatcher(command, new String[]{}, new String[]{"we", "wep", "wea", "weap", "weapo", "weapon"})) {
				parseWeaponCommandSet(command, params);

			// Warrior
			} else if(commandMatcher(command, new String[]{"warrior"})) {
				parseWarriorCommandSet(command, params);
				
			// Action
			} else if(commandMatcher(command, new String[]{"action"})) {
				parseActionCommandSet(command, params);
			
			// Exit / Quit
			} else if(commandMatcher(command, new String[]{"exit", "quit"})) {
				gracefulExit();
				return;
				
			} else {
				commandUnknown(command, params);
			}
		}
	}
	
	private String[] parseCommand(String fullCommand) {
		return fullCommand.trim().split(" +", 2);
	}
	
	private String[] parseParams(String params, int expected) {
		return parseParams(params, expected, expected);
	}
	
	private String[] parseParams(String params, int minimum, int maximum) {
		String[] paramArray = params.trim().split(" +", maximum);
		
		if(paramArray.length < minimum || (params.trim().length() == 0 && minimum > 0)) {
			throw new IllegalArgumentException(
					String.format(": Your assistant insists there needs to be least %d of these \"parameters\", and you only gave %d%n", minimum, paramArray.length)
			);
		}
		
		return paramArray;
	}
	
	private void commandHelp(String command, String params) {
		printer.printString(": Your assistant starts reciting their vocabulary%n");
		
		printHelp(
				"join", 
				new String[][] {
					{"address", "the address of the tournament"}
				},
				new String[][] {
					{"port", "the port; if one is not selected I shall enter the default one"}
				},
				"joins a tournament at the given address and port"
		);
		
		printHelp(
				"leave", 
				new String[][] {},
				new String[][] {},
				"leaves the current tournament"
		);
		
		printHelp(
				"masters", 
				new String[][] {},
				new String[][] {},
				"lists the masters in the current tournament"
		);
		
		printHelp(
				"warrior list", 
				new String[][] {},
				new String[][] {
					{"details", "show detailed information about each warrior (slower)"}
				},
				"lists all of warriors awaiting your command"
		);
		
		printHelp(
				"warrior types", 
				new String[][] {},
				new String[][] {},
				"lists all possible warrior types"
		);
		
		printHelp(
				"warrior new", 
				new String[][] {
					{"type", "warrior type, which--should your mind need refreshing--can be viewed with <warrior types>"},
					{"name", "the name of the warrior; if this name is already in use, the existing warrior will be \"replaced\""},
					{"health", "the health of the warrior; must be between 0 to 100, inclusive"}
				},
				new String[][] {
					{"origin location", "the origin location of the warrior"},
					{"description", "the description of the warrior"}
				},
				"command your Mages to construct a warrior of the given properties"
		);
		
		printHelp(
				"warrior set", 
				new String[][] {
					{"name", "name of an existing warrior"}
				},
				new String[][] {},
				"chooses which warrior you shall send into battle"
		);

		printHelp(
				"warrior delete", 
				new String[][] {
					{"name", "name of an existing warrior"}
				},
				new String[][] {},
				"command your Mages to destruct the given warrior"
		);
		
		printHelp(
				"weapon list", 
				new String[][] {},
				new String[][] {},
				"examine the weapons available to your current warrior"
		);
		
		printHelp(
				"weapon equip", 
				new String[][] {
					{"name", "the weapon you wish your warrior to use"}
				},
				new String[][] {},
				"command your warrior to use the chosen weapon"
		);

		printHelp(
				"message", 
				new String[][] {
					{"recipients", "a comma separated list of Master Identifiers--the number the Tournament Guild insists every participant use to refer to their competitors--or * to communicate with all of the other masters"},
					{"text", "the message to convey"}
				},
				new String[][] {},
				"sends a message to other Masters via message carrier"
		);

		printHelp(
				"action list", 
				new String[][] {},
				new String[][] {},
				"an assistant recites the actions which your selected warrior can perform"
		);

		printHelp(
				"action use", 
				new String[][] {
					{"target", "the Master Identifier whom you wish your warrior to target"},
					{"action", "the action you wish your warrior to use"}
				},
				new String[][] {},
				"command your warrior to take an action against the target"
		);

		printHelp(
				"exit", 
				new String[][] {},
				new String[][] {},
				"Leave this distraction behind"
		);
		
		printHelp(
				"help", 
				new String[][] {},
				new String[][] {},
				"order your assistant to repeat this list, but slower and more clearly"
		);

	}
	
	private void commandConnect(String command, String params) throws UnknownHostException, IOException {
		
		if(client != null && client.getConnection() != null) {
			printer.printInfo(": Your assistant points out your already in a tournament and the rules require you to <leave> the tournament before you may join another");
			return;
		}
		
		String[] paramArray = parseParams(params, 1, 2);
		
		String address = paramArray[0];
		int port = SharedConstant.DEFAULT_PORT;
		
		if(paramArray.length > 1) {
			try {
				port = Integer.parseInt(paramArray[1]);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(": This arrangement of symbols doesn't indicate a number, let alone a port", e);
			}
		}
		
		connect(address, port);
	}
	
	private void commandDisconnect(String command, String params) throws IOException {
		if(client == null || client.getConnection() == null) {
			printer.printInfo(": Your assistant decides not to point out that you aren't in a tournament and pretends to leave it anyway%n");
			return;
		}
		disconnect();
	}
	
	private void commandMasterList(String command, String params) {
		if(client == null) {
			throw new IllegalStateException(NOT_CONNECTED);
		}
		printer.printClientList(client.getClientId(), client.getConnectedClientsList());
	}
	
	private void commandMessage(String command, String params) throws IOException {
		String[] paramArray = parseParams(params, 2);
		
		String message = paramArray[1];
		
		if(paramArray[0].equals("*")) {
			client.sendMessage(new SendTextToAllMessage(message));
			return;
		}
		
		List<Integer> targetClientIds;
		
		try {
			
			String[] clientIds = paramArray[0].split(" *, *");
			targetClientIds = new ArrayList<Integer>(clientIds.length);
			
			for(String clientId : clientIds) {
				int parsedClientId = Integer.parseInt(clientId);
				
				if(DEBUG_DISABLE_CHECKS == false && client.getConnectedClientById(parsedClientId) == null) {
					throw new IllegalArgumentException(INVALID_MASTER_IDENTIFIER);
				}
				
				targetClientIds.add(parsedClientId);
			}
			
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(UNKNOWN_MASTER_IDENTIFIER, e);
		}
		
		client.sendMessage(new SendTextMessage(message, targetClientIds));
	}
	
	private void parseWarriorCommandSet(String outerCommand, String outerParams) throws Exception {
		String[] commandSplit = parseCommand(outerParams);
		
		String command = null,
				params = null;
		
		// Disregard empty commands.
		if(commandSplit.length == 0) {
			throw new IllegalArgumentException(outerCommand + " requires at least one subcommand");
		}

		command = commandSplit[0].toLowerCase();
		
		if(commandSplit.length > 1) {
			params = commandSplit[1];
		} else {
			params = "";
		}
		
		// List
		if(commandMatcher(command, new String[]{"list"})) {
			commandWarriorList(command, params);
			
		// Types
		} else if(commandMatcher(command, new String[]{"types"})) {
			commandWarriorTypes(command, params);
			
		// New / Create
		} else if(commandMatcher(command, new String[]{"new", "create"})) {
			commandWarriorNew(command, params);
			
		// Set / Use / Equip
		} else if(commandMatcher(command, new String[]{"set", "use", "equip"})) {
			commandWarriorSet(command, params);
			
		// Delete / Destroy
		} else if(commandMatcher(command, new String[]{"delete", "destroy"})) {
			commandWarriorDelete(command, params);
			
		} else {
			commandUnknown(command, params);
		}

	}
	
	private void commandWarriorList(String command, String params) {
		List<File> files = warriorFolder.getListOfWarriorFiles(KNOWN_WARRIOR_FILE_EXTENTIONS);
		
		String[] paramArray = parseParams(params, 0, 1);
		
		boolean details = false;
		
		if(paramArray.length > 0 && paramArray[0].trim().isEmpty() == false) {

			// Details
			if(commandMatcher(paramArray[0].trim(), new String[]{"details"})) {
				details = true;
				
				
			} else {
				throw new IllegalArgumentException(": Your assistant is sure \"" + paramArray[0] + "\" is meaningless");
			}
		}
		
		for(File file : files) {
			printer.printFileListItem(file); printer.printString(" ");
			if(details) {
				try {
					printer.printWarrior(warriorFolder.loadWarrior(file.getName()));
				} catch (Exception e) {
					printer.printError(": Your assistant isn't able to get details for this Warrior, he relays rambles off some cryptic message about why: ", e);
				}
			}
			printer.printLine();
		}
	}
	
	private void commandWarriorTypes(String command, String params) {
		printer.printCollection(KNOWN_WARRIOR_TYPES); printer.printLine();
	}
	
	private void commandWarriorNew(String command, String params) throws Exception {
		// [type] [name] [health] (origin location) (description)
		String[] paramArray = parseParams(params, 3, 5);
		
		String type = paramArray[0];
		
		String name = paramArray[1];
		
		String healthString = paramArray[2];
		int health;
		try {
			health = Integer.parseInt(healthString);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(": Your assistant informs you that isn't an acceptable health value", e);
		}
		
		String originLocation = "";
		if(paramArray.length > 3) {
			originLocation = paramArray[3];
		}
		
		String description = "";
		if(paramArray.length > 4) {
			description = paramArray[4];
		}
		
		WarriorBase warrior = null;
		if(type.equalsIgnoreCase(Warrior.TYPE_NAME)) {
			try {
				warrior = new Warrior(name, originLocation, description, health);
			} catch (UnusableWeaponException e) {
				assert false;
				throw new IllegalStateException("This shouldn't have happened, but it did", e);
			}
		} else {
			throw new IllegalArgumentException(": Your Mages say such a Warrior Type is not known in all the lands");
		}
		
		try {
			warriorFolder.saveWarrior(warrior);
		} catch (IOException e) {
			throw new Exception("Failed to save Warrior", e);
		}
	}

	private void commandWarriorSet(String command, String params) throws Exception {
		String[] paramArray = parseParams(params, 1);
		
		String name = paramArray[0];
		
		WarriorBase warrior;
		
		try {
			warrior = warriorFolder.loadWarrior(name);
		} catch(IOException e) { 
			throw new Exception(": The Mages failed to animate the Warrior due to Filesystem Error", e);
		} catch(ClassNotFoundException e) {
			throw new Exception(": The Mages failed to animate the Warrior; Unknown Type", e);
		} catch (InvocationTargetException e) {
			throw new Exception(": The Mages failed to animate the Warrior; Warrior internally failed", e);
		} catch (UnusableWeaponException e) {
			throw new Exception(": The Warrior tried to awake, but was unable to lift the weapon they had been given", e);
		}
		
		updateCurrentWarrior(warrior);
		
		try {
			updateCurrentWarriorToServer(warrior);
		} catch(IOException e) {
			throw new Exception(": Failed to set Warrior due to a communication error", e);
		}
		
		printer.printString(": %s is awakened%n", warrior.getName());
	}
	
	private void commandWarriorDelete(String command, String params) {
		String[] paramArray = parseParams(params, 1);
		
		try {
			warriorFolder.deleteWarrior(paramArray[0]);
		} catch (IOException e) {
			printer.printError(": The Mages have reported they Failed to dismantle the Warrior: ", e);
		}
	}
	
	private void parseWeaponCommandSet(String outerCommand, String outerParams) throws Exception {
		String[] commandSplit = parseCommand(outerParams);

		String command = null,
				params = null;

		// Disregard empty commands.
		if(commandSplit.length == 0) {
			throw new IllegalArgumentException(outerCommand + " requires at least one subcommand");
		}

		command = commandSplit[0].toLowerCase();

		if(commandSplit.length > 1) {
			params = commandSplit[1];
		} else {
			params = "";
		}
		
		// List
		if(commandMatcher(command, new String[]{"list"})) {
			commandWeaponList(command, params);
			
		// Equip / Use / Set
		} else if(commandMatcher(command, new String[]{"equip", "use", "set"})) {
			commandWeaponEquip(command, params);
			
		} else {
			commandUnknown(command, params);
		}
	}
	
	private void commandWeaponList(String command, String params) {
		if(currentWarrior == null) {
			throw new IllegalStateException(WARRIOR_NOT_SELECTED);
		}
		
		for(Weapon weapon : currentWarrior.getUsableWeapons()) {
			printer.printString(" %s: deals %d damage in %d and can block %d damage%n",
					weapon.getName(),
					weapon.getAttackPower(),
					weapon.getAttackSpeed(),
					weapon.getDefensePower()
				);
		}
	}
	
	private void commandWeaponEquip(String command, String params) throws Exception {
		if(currentWarrior == null) {
			throw new IllegalStateException(WARRIOR_NOT_SELECTED);
		}
		
		String[] paramArray = parseParams(params, 1);
		String weaponName = paramArray[0];
		
		Weapon weapon = Armory.getWeaponFromName(weaponName);
		
		if(weapon == null) {
			throw new IllegalArgumentException("Unknown Weapon: " + weaponName);
		}
		
		if(currentWarrior.canUseWeapon(weapon) == false) {
			throw new IllegalArgumentException(": The warrior attempts to take the weapon you hand them, but fails (the Warrior is unable to use this weapon)");
		}
		
		currentWarrior.setEquippedWeapon(weapon);
		
		updateCurrentWarrior(currentWarrior);
		
		try {
			updateCurrentWarriorToServer(currentWarrior);
		} catch (IOException e) {
			if(client != null) {
				currentWarrior = client.getCurrentWarrior();
				printer.printWarning("Reverting change to warrior due to Error...");
			}
			throw new Exception("Failed to update warrior fully due to an I/O exception", e);
		}
	}
	
	private void parseActionCommandSet(String outerCommand, String outerParams) throws Exception {
		String[] commandSplit = parseCommand(outerParams);

		String command = null,
				params = null;

		// Disregard empty commands.
		if(commandSplit.length == 0) {
			throw new IllegalArgumentException(outerCommand + " requires at least one subcommand");
		}

		command = commandSplit[0].toLowerCase();

		if(commandSplit.length > 1) {
			params = commandSplit[1];
		} else {
			params = "";
		}

		// List
		if(commandMatcher(command, new String[] {"list"})) {
			commandActionList(command, params);
			
		// Equip / Use / Set / Attack / Defend
		} else if (commandMatcher(command, new String[] {"equip", "use", "set", "attack", "defend"})) {
			commandActionUse(command, params);
			
		} else {
			commandUnknown(command, params);
		}
	}
	
	private void commandActionList(String command, String params) {
		if(currentWarrior == null) {
			throw new IllegalStateException(WARRIOR_NOT_SELECTED);
		}
		
		Weapon weapon = currentWarrior.getEquippedWeapon();
		
		printer.printString("%s %s wielding %s can%n",
				currentWarrior.getTypeName(),
				currentWarrior.getName(),
				weapon.getName()
		);
		for(Action action : weapon.getActions()) {
			printer.printString(" %s%n", warriorActionToStatisticalResult(weapon, action));
		}
	}
	
	private void commandActionUse(String command, String params) throws IOException {
		if(currentWarrior == null) {
			throw new IllegalStateException(WARRIOR_NOT_SELECTED);
		}
		
		if(client == null) {
			throw new IllegalStateException(NOT_CONNECTED);
		}
		
		String[] paramArray = parseParams(params, 2);
		String targetString = paramArray[0];
		int target;
		try {
			target = Integer.parseInt(targetString);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(INVALID_MASTER_IDENTIFIER, e);
		}
		
		if(DEBUG_DISABLE_CHECKS == false && client.getConnectedClientById(target) == null) {
			throw new IllegalArgumentException(UNKNOWN_MASTER_IDENTIFIER);
		}
		
		String actionName = paramArray[1];
		
		Action action = Armory.getActionFromName(actionName);
		
		if(action == null) {
			throw new IllegalArgumentException(": Your assistant carefully inspects the rulebook three times, each slower than the last, but still can't find an action " + action);
		}
		
		if(currentWarrior.getEquippedWeapon().canUseAction(action) == false) {
			throw new IllegalArgumentException(": When you command your warrior to pick up the weapon, they look at you dumbfounded [the Warrior's currently equipped weapon cannot be used to make the given action]");
		}
		
		client.sendMessage(new TakeActionMessage(action, target));
		
	}
	
	private void commandUnknown(String command, String params) {
		throw new IllegalArgumentException(": Your assistant doesn't seem to understand what you mean by \"" + command + "\", perhaps it would <help> to have them recite their vocabulary?");
	}
	
	private void printHelp(String name, String[][] requiredParams, String[][] optionalParams, String description) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(name);
		
		for(String[] param : requiredParams) {
			sb.append(" [" + param[0] + "]");
		}
		
		for(String[] param : optionalParams) {
			sb.append(" (" + param[0] + ")");
		}
		
		sb.append("%n");
		
		sb.append(" " + description + "%n");
		
		for(String[] param : requiredParams) {
			sb.append(" " + param[0] + ": " + param[1] + " [required]%n");
		}
		
		for(String[] param : optionalParams) {
			sb.append(" " + param[0] + ": " + param[1] + " (optional)%n");
		}
		
		printer.printString(sb.toString() + "%n");
	}
	
	protected void connect(String address, int port) throws IOException {
		Connection connection = new SocketConnection(new Socket(address, port));			
		connect(connection);
	}
	
	protected void connect(Connection connection) throws IOException {
		client = new ClientWithLock(connection, this, clientLock);
		
		clientThread = new Thread(client);
		clientThread.start();
		
		client.sendMessage(new GetConnectedClientsListMessage());
		
		if(currentWarrior != null) {
			updateCurrentWarriorToServer(currentWarrior);
		}
	}
		
	protected void disconnect() {
		if(client == null || client.getConnection() == null) {
			return;
		}
		try {
			client.getConnection().close();
		} catch (IOException e) {
			printer.printInfo(": These \"experts\" can't even close a communication link properly, they claim it's because \"%s\", whatever that means", e);
		}
		client = null;
		clientThread = null;
	}


	private void exit() {
		System.exit(0);
	}
	
	private void gracefulExit() {
		disconnect();
		saveCurrentWarrior();
		printer.printString(": Fair-well Master *your assistant bows deeply*%n");
		exit();
	}
	
	private void saveCurrentWarrior() {
		if(currentWarrior != null) {
			try {
				warriorFolder.saveWarrior(currentWarrior);
			} catch (IOException e) {
				printer.printError("Failed to save Warrior: ", e);
			}
		}
	}

	@Override
	public void notifyException(Exception e) {
		if(e instanceof IOException) {
			if(client != null) {
				disconnect();
			}
		}
		
		if(e instanceof IOException) {
			printer.printError(": Communication between you and the Tournament has broken down and the \"experts\" keep babbling a cryptic message: ", e);
		} else {
			printer.printError(e);
		}
	}
	
	@Override
	public void notifyCurrentWarriorChanged(WarriorBase currentWarrior) {
		updateCurrentWarrior(currentWarrior);
	}
	
	@Override
	public void handleInvalidMessage(InvalidMessage message) {
		printer.printWarning(
				"] The Tournament organizers have returns a %s message; they say it's \"%s\"!%n", 
				message.getMessage().getClass().getSimpleName(), 
				message.getReason()
		);
	}

	@Override
	public void handleRelayedTextMessage(RelayedTextMessage message) {
		printer.printTextMessage(client, message);
	}

	@Override
	public void handleServerTextMessage(ServerTextMessage message) {
		printer.printTextMessage(message);
	}

	@Override
	public void handleSetClientIdMessage(SetClientIdMessage message) {
		printer.printInfo("] The Tournament organizers inform you that your Master Identifier is %d%n", message.getClientId());
	}

	@Override
	public void handleConnectedClientsListMessage(ConnectedClientsListMessage message) {
		printer.printString("] A fresh list of Participating Masters arrives from the Tournament organizers%n");
	}

	@Override
	public void handleEngagementCombatResultMessage(EngagementCombatResultMessage message) {
		printer.printString(
				"] *Tournament Announcer* The round has ended, the results are in:%n"
		);
		for(WarriorCombatResult result : message.getWarriorCombatResults()) {
			printer.printWarriorCombatResult(client, result); printer.printLine();
		}
	}

	@Override
	public void handleEngagementStartedMessage(EngagementStartedMessage message) {
		printer.printString("] An engagement, started by %s, has begun between you",
				(message.getStartedByClientId() == client.getClientId() ? "you" : message.getStartedByClientId()));
		
		printer.printEngagementParticipatintList(client, message.getInvolvedClientIds());

		printer.printString(" Here's how things stack up:");
		printer.printEngagementStackup(client, message.getInvolvedClientIds());
	}

	@Override
	public void handleEngagementEndedMessage(EngagementEndedMessage message) {
		printer.printString("] The engagement between you");
		
		printer.printEngagementParticipatintList(client, message.getInvolvedClientIds());
		
		printer.printString(" has ended");
		
		printer.printString(" Here's how things stand:");
		printer.printEngagementStackup(client, message.getInvolvedClientIds());
	}

	@Override
	public void handleEngagementActionSelectedMessage(EngagementActionSelectedMessage message) {
		printer.printString("] *Tournament Announcer* %s has told their warrior to target %s with %s%n",
				clientIdToString(message.getSelectedAction().getClientId()),
				clientIdToString(message.getSelectedAction().getTargetClientId()),
				message.getSelectedAction().getAction().getName()
		);
		
		ConnectedClientState state = client.getConnectedClientById(message.getSelectedAction().getClientId());
		WarriorBase warrior = state.getWarrior();
		
		if(state != null && warrior != null) {
			printer.printString(" For those who don't recall, %s %s wielding %s using %s%n", 
					warrior.getTypeName(),
					warrior.getName(),
					warrior.getEquippedWeapon().getName(),
					warriorActionToStatisticalResult(
							warrior.getEquippedWeapon(),
							message.getSelectedAction().getAction()
					)
			);
		} else {
			printer.printString(" It's anyone's guess what damage that will do");
		}
	}

	@Override
	public void handleClientConnectedMessage(ClientConnectedMessage message) {
		printer.printClientStateNotification("] %s has joined the Tournament", message.getConnectedClientState());
	}

	@Override
	public void handleClientDisconnectedMessage(ClientDisconnectedMessage message) {
		printer.printClientStateNotification("] %s has left Tournament", message.getConnectedClientState());
	}

	@Override
	public void handleClientStateChangedMessage(ClientStateChangedMessage message) {
		//printer.printClientStateNotification("] %s changed", message.getConnectedClientState());
	}

	@Override
	public void handleRelayedTextToAllMessage(RelayedTextToAllMessage message) {
		printer.printTextMessage(client, message);		
	}
	
	
	private String clientIdToString(Integer clientId) {
		return (clientId == client.getClientId() ? "you" : clientId.toString());
	}
	
	private String warriorActionToStatisticalResult(Weapon weapon, Action action) {
		return String.format(
				"%s inflicts %d damage in %d and can stop %d damage",
				action.getName(),
				weapon.getEffectiveAttackPower(action),
				weapon.getEffectiveAttackSpeed(action),
				weapon.getEffectiveDefensePower(action)
		);
	}
	
	private boolean commandMatcher(String command, String[] fuzzy) {
		return commandMatcher(command, fuzzy, new String[]{});
	}
	
	private boolean commandMatcher(String command, String[] fuzzy, String[] exact) {
		outerLoop:
		for(String toMatch : fuzzy) {

			if(command.length() > toMatch.length())
				continue;
			
			for(int i = 0; i < command.length(); i++) {
				if(toMatch.charAt(i) != command.charAt(i) || command.charAt(i) == ' ') {
					continue outerLoop;
				}
			}
			
			return true;
		}
		for(String toMatch : exact) {
			if(command.equals(toMatch)) {
				return true;
			}
		}
		return false;
	}
}