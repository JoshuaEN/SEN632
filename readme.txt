# Tournament Master
A school project for National University class SEN632 Software Architecture Applied Team Project

## Introduction
Tournament Master is a simple turn based combat game. At it's core players-called Masters--choose, or create, a Warrior to represent them then enter into an Engagement with another Master (Engagements are always one on one). 

Once in an Engagement, each turn both Masters select an Action to perform. Once all Masters have selected an Action, the Tournament Moderator determine the net result. 

This continues until one of the Masters withdraws.

## Quick Start

1. Download the jar\server.jar and jar\client_gui.jar, and optionally the jar\server_win.bat
2. Start the server, either by running the runnable server.jar, running the server_win.bat, or running server.jar via java -jar server.jar command. 

  The latter two are equivalent on windows, the bat file is just a shortcut for having to manually run it as a jar.
  Running it via -jar is desirably as simply running the jar will run it in the background and not display console output.
3. Start the client by running client_gui.jar.
  4. Connect to the server you started--the default settings should work fine.
  5. Go to the Warriors tab and either select an existing warrior, by clicking the Warrior and clicking the `Use` button, or create a new Warrior to use.
6. Start another client so you have someone to enter an engagement with, and repeat the above step for that client.
7. Go to the Masters List tab, select an opponent (not yourself), and click Engage.
8. Select a starting action to take in the dialog window and click OK.
9. In the other client, click the Engagement tab.
10. Select an action to take.
11. Repeat above step in both clients as desired.

## Technical Overview
Tournament Master uses a client/server architecture, where two or more clients connect to a central server. 

Once connected to a server server, they can enter into an Engagement with other clients connected to the same server. 

The network communication is done using Java Sockets via specific Message Class objects passed via serialization.

## Credits
* Olumide Ahmed
* Joshua Neuenhoff

## Notes
* The password for the included Crypto Warrior (showed as ??? 0dfd7feb in the Warriors list) is SEN632