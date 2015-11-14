package ojdev.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

import ojdev.common.SharedConstant;

public class ServerMain {

	public static void main(String[] args) {

		Iterator<String> iterator = Arrays.asList(args).listIterator();
		
		int port = SharedConstant.DEFAULT_PORT,
			maxConnections = ServerConstant.DEFAULT_MAX_CONNECTIONS,
			backlog = -1;
		
		while(iterator.hasNext()) {
			String arg = iterator.next();
			arg = arg.replaceAll("-", "");
			
			switch (arg) {
			
			case "h":
			case "help":
				// This is correctly aligned in the Console (for whatever reason).
				System.out.printf(
					"-b --backlog [integer] 		| Maximum number of users who can be waiting to connect concurrently, defaults to value of max-connections%n" +
					"-h --help 			| Print Help%n" +
					"-m --max-connections [integer]	| Maximum number of users who can be connected concurrently, defaults to %d%n" +
					"-p --port [integer]		| Listen port for the server, defaults to %d%n",
					ServerConstant.DEFAULT_MAX_CONNECTIONS, SharedConstant.DEFAULT_PORT
				);
				System.exit(1);
				return;
				
			case "b":
			case "backlog":
				try {
					backlog = Integer.parseInt(iterator.next());
				} catch (NumberFormatException e) {
					System.err.println("FATAL: Invalid Backlog: " + e);
					System.exit(1);
					return;
				}
				
				if(backlog < 0) {
					System.err.println("FATAL: Invalid Backlog: Must be at least 0");
					System.exit(1);
					return;
				}
				break;
				
			case "m":
			case "max-connections":
				try {
					maxConnections = Integer.parseInt(iterator.next());
				} catch (NumberFormatException e) {
					System.err.println("FATAL: Invalid Max Connections: " + e);
					System.exit(1);
					return;
				}
				
				if(maxConnections < 1) {
					System.err.println("FATAL: Invalid Max Connections: Must be at least 1");
					System.exit(1);
					return;
				}
				
				break;
				
			case "p":
			case "port":
				try {
					port = Integer.parseInt(iterator.next());
				} catch (NumberFormatException e) {
					System.err.println("FATAL: Invalid Port: " + e);
					System.exit(1);
					return;
				}
				break;
				
			default:
				System.err.printf("WARN: Invalid or Unknown Argument Command of %s%n", arg);
				break;
			}
		}
		
		if(backlog == -1) {
			backlog = maxConnections;
		}
		
		Moderator moderator;
		
		try {
			moderator = new Moderator(maxConnections, port, backlog);
		} catch (IOException e) {
			System.err.println("FATAL: Failed to Initalize Server: " + e);
			e.printStackTrace();
			System.exit(1);
			return;
		}
		
		Thread mainThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					moderator.startServer();
				} catch (IOException e) {
					System.err.println("FATAL: Failed to Start Server: " + e);
					e.printStackTrace();
					System.exit(1);
					return;
				}
			}
		});
		
		mainThread.start();
		
		Scanner scanner = new Scanner(System.in);
		
		do {
			System.out.println("Type Exit to exit.");
		} while(scanner.nextLine().toLowerCase().equals("exit") == false);
		
		scanner.close();
		
		try {
			moderator.stopServer();
		} catch (IOException e) {
			System.err.println("FATAL: Failed to Stop Server Correctly: " + e);
			e.printStackTrace();
			System.exit(1);
			return;
		}
		
		System.exit(0);
		return;
	}

}
