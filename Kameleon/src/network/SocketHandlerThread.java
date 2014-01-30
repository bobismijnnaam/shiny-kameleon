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
	private boolean isLobby = true;
	private boolean kill = false;
	
	public SocketHandlerThread(ClientRolitSocket inputCrs, Game inputGame, Lobby inputLobby) {
		crs = inputCrs;
		game = inputGame;
		lobby = inputLobby;
		isLobby = true;
	}
	
	public SocketHandlerThread(ClientRolitSocket inputCrs, Game inputGame, 
			MainGamePanel inputOnlineGame) {
		crs = inputCrs;
		game = inputGame;
		online = inputOnlineGame;
		isLobby = false;
	}
	
	public void handleLobby(MessageType inputServerMessageType, String[] inputNewMessage) {
		switch (inputServerMessageType) {
			case X_NONE:
				break;
			case AL_CHATM:
				String chatMessage;
				String[] realMessage = new String[inputNewMessage.length - 1];
				System.arraycopy(inputNewMessage, 1, realMessage, 0, 
						inputNewMessage.length - 1);
				chatMessage = Utils.join(realMessage);
				lobby.addChatMessage(inputNewMessage[0], chatMessage);
				break;
			case LO_INVIT:
				System.out.println("Received an invite");
				if (inputNewMessage[0].equals("R")) {
					lobby.answerInvite(inputNewMessage[1]);
				} else {
					lobby.invitDenied();
				}
				break;
			case LO_START:
				String[] players = new String[4];
				String[] settings = lobby.getSettings();
				String playerModus = lobby.getPlayerModus();
				if (playerModus.equals("human")) {
					playerModus = "networkyou";
				}
				for (int m = 0; m < inputNewMessage.length; m++) {
					if (settings[0].equals(inputNewMessage[m])) {
						players[m] = playerModus;
					} else {
						players[m] = "network";
					}
				}
				try {
					game.setNextState(Game.STATE_ONLINE, players, crs);
					kill = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case LO_LJOIN:
				System.out.println("Received new join");
				lobby.addPlayer(inputNewMessage[0]);
				break;
			default:
				break;
		}
	}
	
	public void handleOnline(MessageType inputServerMessageType, String[] inputNewMessage) {
		switch (inputServerMessageType) {
			case X_NONE:
				break;
			case AL_CHATM:
				String chatMessage;
				String[] realMessage = new String[inputNewMessage.length - 1];
				System.arraycopy(inputNewMessage, 1, realMessage, 0, 
						inputNewMessage.length - 1);
				chatMessage = Utils.join(realMessage);
				online.addChatMessage(inputNewMessage[0], chatMessage);
				break;
			case IG_GMOVE:
				online.setOnlineMove(inputNewMessage[1] , inputNewMessage[2]);
				break;
			case IG_GTURN:
				break;
			default:
				break;
		}
	}
	
	@Override
	public void run() {
		String[] newMessage;
		
		while (crs.isRunning() && !kill) {
			if (crs.isNewMsgQueued()) {
				try {
					serverMessageType = crs.getQueuedMsgType();
					newMessage = crs.getQueuedMsgArray();
					if (isLobby) {
						handleLobby(serverMessageType, newMessage);
					} else {
						handleOnline(serverMessageType, newMessage);
					}
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}	
}
