package ojdev.client.ui.console;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ojdev.client.Client;
import ojdev.common.ConnectedClientState;
import ojdev.common.WarriorCombatResult;
import ojdev.common.connections.Connection;
import ojdev.common.messages.server.RelayedTextMessage;
import ojdev.common.messages.server.RelayedTextToAllMessage;
import ojdev.common.messages.server.ServerTextMessage;
import ojdev.common.warriors.WarriorBase;

class ConsoleFormattedPrinter {
	
	private final PrintWriter out;
	
	public ConsoleFormattedPrinter(PrintWriter out) {
		this.out = out;
	}
	
	public void printFatal(String format, Object... args) {
		printFatal("Fatal Error: " + format, args);
	}
	
	public void printError(String format, Object... args) {
		print("" + format, args);
	}
	
	public void printError(Throwable e) {
		printError("", e);
	}
	
	public void printError(String message, Throwable e) {
		printError("%s%s: %s%n", message, e.getClass().getSimpleName(), e.getMessage());
	}

	public void printWarning(String format, Object... args) {
		print("" + format, args);
	}
	
	public void printInfo(String format, Object... args) {
		print("" + format, args);
	}
	
	public void printMessage(String format, Object... args) {
		print("   " + format, args);
	}
	
	public void printPrompt(String string, Object... args) {
		print(string, args);
	}
	
	public void printList(List<Object> list) {
		for(int i = 0; i < list.size(); i++) {
			printListItem(i+1, list.get(i));
		}
	}
	
	public void printListItem(Object number, Object item) {
		print("%s. %s%n", number, item);
	}
	
	public void printHeader(String pageTitle, Client client) {
		
		if(client != null) {
			Connection connection = client.getConnection();
			WarriorBase warrior = client.getCurrentWarrior();
			
			if(connection == null) {
				print("Not Connected%n");
			} else {
				print("Connected to %s along with %d other%s%n",
						connection,
						client.getConnectedClientsList().size(),
						(client.getConnectedClientsList().size() == 1 ? "" : "")
				);
			}
			
			if(warrior == null) {
				if(connection == null) {
					print("No Warrior Selected%n");
				} else {
					print("No Warrior Selected, Spectating%n");
				}
			} else {
				printWarrior(warrior); printLine();
				
				if(client.isInEngagement()) {
					int opponentCount = 0;
					
					for(int clientId : client.getEngagementParticipantClientIds()) {
						if(clientId == client.getClientId()) {
							continue;
						}
						
						if(opponentCount > 0) {
							print("and ");
						}
						opponentCount += 1;
						print("fighting agasint "); printWarrior(client.getConnectedClientById(clientId).getWarrior()); printLine();
					}
				}
			}
		}
		
		print("%s%n", pageTitle);
	}
	
	public void printWarrior(WarriorBase warrior) {
		if(warrior == null) {
			print("Spectator");
			return;
		}
		print(warriorString(warrior));
	}
	
	public void printClientList(Integer ourId, List<ConnectedClientState> list) {
		for(ConnectedClientState state : list) {
			print("[%d]%s ", state.getClientId(), (state.getClientId() == ourId ? "[you]" : ""));
			printWarrior(state.getWarrior());
			print("; %sengaged in combat%n", (state.isInEngagement() ? "" : "not "));
		}
	}
	
	public void printFileList(List<File> files) {
		for(File file : files) {
			printFileListItem(file);
		}
	}
	
	public void printFileListItem(File file) {
		print("%s", file.getName());
	}
	
	public void printCollection(Collection<?> collection) {
		for(Object object : collection) {
			print("%s%n", object);
		}
	}
	
	private static final String LINE_CLEAR_STRING = new String(new char[2]).replace("\0", System.lineSeparator());
	public void printClear() {
		print(LINE_CLEAR_STRING);
	}
	
	public void printPageHeader(String pageTitle, Client client) {
		printClear();
		printHeader(pageTitle, client);
	}
	
	public void printLine() {
		print("%n");
	}
	
	public void printString(String string, Object... args) {
		print(string, args);
	}
	
	public void printTip(String string) {
		print("[Tip: " + string);
	}
	
	private synchronized void print(String format, Object... args) {
		out.printf(format, args);
		out.flush();
	}

	public void printTextMessage(Client client, RelayedTextMessage message) {
		
		String sender = clientDisplayName(message.getSenderClientId(), client);

		StringBuilder recievers = new StringBuilder();
		
		// We received this message, we don't need to list ourselves though
		// so we aren't important to the number of receivers
		int importantSize = message.getReceiversClientIds().size()-1;
		
		for(int i = 0; i < importantSize; i++) {
			int clientId = message.getReceiversClientIds().get(i);
			
			if(clientId == client.getClientId()) {
				continue;
			}
			
			if(recievers.length() > 0 && importantSize > 2) {
				recievers.append(",");
			}
			if(i > 0 && i == importantSize) {
				recievers.append(" &");
			}
			
			recievers.append(clientDisplayName(clientId, client));
		}
		
		printTextMessage(message.getCreatedAt(), sender, recievers.toString(), message.getMessage());
	}

	public void printTextMessage(ServerTextMessage message) {
		printTextMessage(message.getCreatedAt(), "#server", "", message.getMessage());
	}
	
	public void printTextMessage(Client client, RelayedTextToAllMessage message) {
		printTextMessage(message.getCreatedAt(), clientDisplayName(message.getSenderClientId(), client), "#everyone", message.getMessage());
	}
	
	private void printTextMessage(Date timestamp, String sender, String recievers, String message) {
		print("A message arrived from %s, your assistant reads it aloud:%n %s%n", timestamp, sender, message);
		
		if(recievers.isEmpty() == false) {
			print(" Reportedly, it was also sent to %s%n", recievers);
		}
	}
	
	private String clientDisplayName(int clientId, Client client) {
		ConnectedClientState state = null;
		
		if(client != null) {
			state = client.getConnectedClientById(clientId);
		}
		
		return clientDisplayName(clientId, state);
	}
	
	private String clientDisplayName(int clientId, ConnectedClientState state) {
		return String.format("%d", clientId);
	}
	
	private String warriorString(WarriorBase warrior) {
		return String.format("%s %s is wielding %s and has %d health (of %d)",
				warrior.getTypeName(),
				warrior.getName(),
				warrior.getEquippedWeapon().getName(),
				warrior.getHealth(),
				warrior.getMaxHealth());
	}

	public void printClientStateNotification(String string, ConnectedClientState state) {
		print(
				string,
				String.format(
						"%s",
						//connectedClientState.getCreatedAt(),
						clientDisplayName(state.getClientId(), state)
				)
		);
//		print(" %s",
//				(state == null ? "???" : 
//					(state.getWarrior() == null ? "Spectator" : warriorString(state.getWarrior()) )
//			));
		printLine();
	}

	public void printWarriorCombatResult(Client client, WarriorCombatResult result) {
		print(
				"%s Warrior lost %d health", 
				(result.getClientId() == client.getClientId() ? "Your" : String.format("%d's", result.getClientId())),
				result.getHealthLost()
		);
	}
	
	public void printEngagementStackup(Client client, List<Integer> clientIds) {
		for(int clientId : clientIds) {
			if(clientId == client.getClientId()) {
				continue;
			}
			
			printString(" Fighting for %s there's ", (clientId == client.getClientId() ? "you" : clientId));
			printWarrior(client.getConnectedClientById(clientId).getWarrior());
			printLine();
		}
	}
	
	
	public void printEngagementParticipatintList(Client client, List<Integer> clientIds) {
		for(int clientId : clientIds) {
			if(clientId == client.getClientId()) {
				continue;
			}
			
			printString(" and %s", clientId);
		}
	}

	public void printStackTrace(Throwable e) {
		for(StackTraceElement element : e.getStackTrace()) {
			print("%s%n",
					element
			);
		}
	}
}
