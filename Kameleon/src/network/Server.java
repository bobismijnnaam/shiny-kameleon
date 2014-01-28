package network;

import java.util.ArrayList;
import java.util.List;

import utility.Utils;
import network.RolitSocket.MessageType;
import network.ServerPlayer.PlayerAuthState;

// TODO: Make sure you can only login once, otherwise error!
// TODO: Implement new PKISocket!
public class Server extends Thread {
	public static final int SERVER_PORT = 8494;

	private List<ServerPlayer> frontline;
	private List<ServerPlayer> lobby;
	// private List<Invite> invites;
	// private List<ServerGame> games;

	private boolean running;

	ServerBouncer sb; // Incoming connections handler
	PKISocket pki;

	public Server() {
		// Let's do this!
		frontline = new ArrayList<ServerPlayer>();
		lobby = new ArrayList<ServerPlayer>();

		running = false;
	}

	private void out(String msg) {
		System.out.println(msg);
	}

	private void serverSays(String msg) {
		out("[Server] " + msg);
	}

	private void playerSays(ServerPlayer player, String msg) {
		out("[" + player.getName() + "] " + msg);
	}

	private void handleNewConnections() {
		while (sb.isNewConnection()) {
			// Extract the socket and create a new serverplayer
			ServerPlayer newPlayer = new ServerPlayer(sb.getNewConnection());
			newPlayer.net().start();
			frontline.add(newPlayer);
			// Log the connect
			serverSays("A new client connected");
		}
	}

	private void garbageCollectPlayer(List<ServerPlayer> l, ServerPlayer p) {
		if (p.net().isCloseCalled()) {
			l.remove(p);
		}
	}
	
	private void broadcastPlayerJoin(ServerPlayer player) {
		for (ServerPlayer lobbyist : lobby) {
			if (player != lobbyist) {
				lobbyist.net().tellLJOIN(player.getName());
			}
		}
	}

	private void handleFrontline(ServerPlayer p) {
		// Only do stuff when socket is running!
		// Otherwise stream errors and shit
		if (!p.net().isRunning()) {
			return;
		}

		// Check for incoming messages
		while (p.net().isNewMsgQueued()) {
			MessageType msgType = p.net().getQueuedMsgType();
			String[] msg = p.net().getQueuedMsgArray();
			
			switch (msgType) {
				case AC_LOGIN:
					if (p.getAuthState() == PlayerAuthState.Unauthenticated) {
						String username = msg[0];
						String authKey = PKISocket.getRandomString(50);
						p.setAuthKeySent(username, authKey);
						pki.requestPublicKey(username);

						serverSays("Player " + username
								+ " wants to shake hands. "
								+ "Extended hand to " + username);
					} else {
						p.net().tellERROR(
								RolitSocket.Error.UnexpectedOperationException,
								"L2AUTH 1");
					}

					break;
				case AC_VSIGN:
					if (p.getAuthState() == PlayerAuthState.KeySent) {
						p.setAuthKeyReceived(msg[0]);

						serverSays(p.getName()
								+ " returned the handshake. Grading handshake...");
					} else {
						p.net().tellERROR(
								RolitSocket.Error.UnexpectedOperationException,
								"L2AUTH 2");
					}
					break;
				case AC_HELLO:
					if (p.getAuthState() == PlayerAuthState.Authenticated) {
						p.setClientType(msg[0]);
						frontline.remove(p);
						lobby.add(p);
						broadcastPlayerJoin(p);
						serverSays("Player " + p.getName()
								+ " entered the lobby");
					} else {
						p.net().tellERROR(
								RolitSocket.Error.UnexpectedOperationException,
								"L2AUTH 2");
					}
					break;
				case AL_LEAVE:
					serverSays("Player " + p.getName()
							+ " has disconnected before completing handshake");
					p.net().close();
					break;
				default:
					p.net().tellERROR(
							RolitSocket.Error.UnexpectedOperationException, 
									p.net().getQueuedMsgType().toString()
									+ " (Either we implemented shit wrong or"
									+ "you just went full retard.\n\n"
									+ "\nNever go full retard man.)");
					p.net().getQueuedMsg();
					break;
			}
		}
		
		// Signature magic
		if (p.getAuthState() == PlayerAuthState.SignatureAwaitsChecking
				&& pki.hasKey(p.getName())) {
			String publicKey = pki.getPublicKey(p.getName());
			if (publicKey.equals(PKISocket.PKI_USER_INVALID)) {
				serverSays("That handshake feels fake, " + p.getName() + "! [PKI_USER_INVALID]");
				p.net().tellERROR(RolitSocket.Error.LogInFailedException);
				p.net().close();
			} else {
				if (p.tryVerifySignature(publicKey) == PlayerAuthState.Authenticated) {
					serverSays("Pretty good handshake, " + p.getName() + "! 8/10");
					p.net().tellHELLO();
				} else if (p.getAuthState() == PlayerAuthState.Authenticated) {
					serverSays("Shitty handshake, " + p.getName() + ". 1/10");
					p.net().tellERROR(RolitSocket.Error.LogInFailedException,
							"Wrong username/password #bitch cheater!");
					p.net().close();
				}
			}
		}

		garbageCollectPlayer(frontline, p);
	}

	private void handleLobby(ServerPlayer p) {
		if (!p.net().isRunning()) {
			return;
		}

		while (p.net().isNewMsgQueued()) {
			MessageType msgType = p.net().getQueuedMsgType();
			String[] msg = p.net().getQueuedMsgArray();
			switch (msgType) {
				case AL_CHATM:
					String chatmsg = Utils.join(msg);
					playerSays(p, chatmsg);

					for (ServerPlayer otherP : lobby) {
						if (otherP != p) {
							otherP.net().tellCHATM(p.getName(), chatmsg);
						}
					}

					break;
				case AL_LEAVE:
					serverSays(p.getName() + " left");
					p.net().close();
					break;
				case AL_STATE:
					p.net().tellSTATE(PlayerState.LOBBY);
					break;
				case LO_NGAME: // TODO: Queues and shit
					break;
				case LO_PLIST:
					ArrayList<String> playersAvailable = new ArrayList<String>();
					for (ServerPlayer otherP : lobby) {
						if (otherP != p) {
							playersAvailable.add(otherP.getName());
						}
					}
					p.net().tellPLIST(playersAvailable.toArray(new String[0]));
					break;
				case LO_INVIT:
					break;
				default:
					break;
			}
		}

		garbageCollectPlayer(lobby, p);
	}

	public void run() {
		serverSays("HONEYBADGER ON DUTY!");

		sb = new ServerBouncer(SERVER_PORT);
		Thread sbThread = new Thread(sb);
		sbThread.start();
		serverSays("Started server socket");
		
		pki = new PKISocket();
		pki.start();
		serverSays("Started PKI handler");
		
		while (!pki.isRunning() && !sb.isRunning()) {
			try {
				Thread.sleep(17);
			} catch (InterruptedException e) {
				System.out.println("Haha no way"); // ya way
			}
		}

		// Set server loop to GOGOGOGOGOGOGO
		running = true;

		// Server loop
		while (running) {
			// Check if there connected a client
			// If so, add it to the frontlines! CHARGE
			handleNewConnections();

			ServerPlayer p;

			// Check front line for clients wanting to get in
			for (int i = 0; i < frontline.size(); i++) {
				handleFrontline(frontline.get(i));
			}

			// Handle lobby mechanics
			for (int i = 0; i < lobby.size(); i++) {
				handleLobby(lobby.get(i));
			}

			try {
				Thread.sleep(17);
			} catch (InterruptedException e) {
				System.out.println("Haha no way"); // ya way
			}
		}
	}

	public static void main(String[] args) {
		Server serv = new Server();
		serv.start();

		try {
			serv.join();
		} catch (InterruptedException e) {
			System.out.println("Interruped Exception?");
			return;
		}

		System.out.println("-------- All done! --------");
		System.out
				.println("-------- HoneyBadger HRTP Server wishes you a good day! --------");
	}
}

// RolitSocket server = new RolitSocket(SERVER_PORT);
// RolitSocket client = new RolitSocket("localhost", SERVER_PORT);
//
// server.start();
// client.start();
//
// System.out.println("Initialized variables. Waiting for them to connect...");
//
// while (!server.isRunning() || !client.isRunning()) {
// // Let the thread sleep so the other processes can get some cpu time
// try {
// Thread.sleep(100);
// } catch (InterruptedException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// }
//
// System.out.println("They seem to both have connected and started properly!");
//
// server.askPROTO();
//
// while (server.getQueuedMsgType() != RolitSocket.MessageType.FB_PROTO) {
// try {
// Thread.sleep(100);
// } catch (InterruptedException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// }
//
// System.out.println("A message seems to have arrived!");
//
// System.out.println("Message: \"" + server.getQueuedMsg() + "\"");
//
// System.out.println("\n");
//
// System.out.print("Closing client, server. ");
// client.close();
//
// try {
// Thread.sleep(200);
// } catch (InterruptedException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
//
// server.close();
//
// try {
// client.join();
// server.join();
// } catch (InterruptedException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
//
// System.out.println("Server still alive: " + server.isAlive());
// System.out.println("Client still alive: " + client.isAlive());
// System.out.println("Profit?");
