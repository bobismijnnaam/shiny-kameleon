package network;

import gamepanels.Game;
import gamepanels.Lobby;
import gamepanels.MainGamePanel;

import java.io.IOException;

import network.RolitSocket.MessageType;
import utility.Utils;

public class SocketHandlerThread extends Thread {
	private ClientRolitSocket crs;
	private MessageType serverMessageType;
	private Game game;
	private Lobby lobby;
	private MainGamePanel online;
	private boolean state =  false;
	
	public SocketHandlerThread(ClientRolitSocket inputCrs, Game inputGame, Lobby inputLobby) {
		crs = inputCrs;
		game = inputGame;
		lobby = inputLobby;
		state = false;
		System.out.println("Entered lobby");
	}
	
	public SocketHandlerThread(ClientRolitSocket inputCrs, Game inputGame, 
			MainGamePanel inputOnlineGame) {
		crs = inputCrs;
		game = inputGame;
		online = inputOnlineGame;
		state = true;
		System.out.println("Entered online game-modus");
	}
	
	public void handleLobby(MessageType inputServerMessageType, String[] inputNewMessage) {
		// check the type
		switch (inputServerMessageType) {
			case X_NONE:
				//System.out.println("No action");
				break;
			case AL_CHATM:
				String chatMessage;
				System.out.println("Received chatmessage");
				String[] realMessage = new String[inputNewMessage.length - 1];
				System.arraycopy(inputNewMessage, 1, realMessage, 0, 
						inputNewMessage.length - 1);
				chatMessage = Utils.join(realMessage);
				lobby.addChatMessage(inputNewMessage[0], chatMessage);
				break;
			case LO_INVIT:
				System.out.println("Request for starting game");
				System.out.println("Display accept or deny window");
				break;
			case LO_START:
				System.out.println("WOOOW EEN SPEL GAAT STARTEN!, wat spannend!");
				String[] players = new String[4];
				for (int m = 0; m < inputNewMessage.length; m++) {
					players[m] = "network";
				}
				try {
					game.setNextState(game.STATE_ONLINE, players);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
	}
	
	public void handleOnline(MessageType inputServerMessageType, String[] inputNewMessage) {
		// check the type
		switch (inputServerMessageType) {
			case X_NONE:
				//System.out.println("No action");
				break;
			default:
				break;
		}
	}
	
	@Override
	public void run() {
		String[] newMessage;
		System.out.println("Started the socket handler");
		while (crs.isRunning()) {
			if (crs.isNewMsgQueued()) {
				try {
					serverMessageType = crs.getQueuedMsgType();
					System.out.println(serverMessageType.toString());
					newMessage = crs.getQueuedMsgArray();
					System.out.println(Utils.join(newMessage));
					if (!state) {
						handleLobby(serverMessageType, newMessage);
					} else {
						handleOnline(serverMessageType, newMessage);
					}
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}	
}
