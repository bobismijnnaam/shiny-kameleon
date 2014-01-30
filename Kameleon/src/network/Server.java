package network;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import network.RolitSocket.MessageType;
import network.ServerPlayer.PlayerAuthState;
import utility.Stopwatch;
import utility.Utils;

// TODO: Alive pings :D
// TODO: Scoreboard
// TODO: Client should handle exceptions/errors like loginfault
// TODO: Client should close sockets on x'ing out
// TODO: LJOIN spam

// public class Server extends Thread {
public class Server extends JFrame implements Runnable {
	public static final int SERVER_PORT = 2014;
	
	private int usePort = 0;

	private List<ServerPlayer> frontline;
	private List<ServerPlayer> lobby;
	private ServerInviteManager invites;
	private List<ServerGame> games;
	
	private JTextArea servertext;
	
	private PlayerQueue playerQ;
	
	private Stopwatch aliveTimer;

	private boolean running = false;

	ServerBouncer sb; // Incoming connections handler
	PKISocket pki;

	public Server() {
		super("Honeybadger Console");
		// Let's do this!
		frontline = new ArrayList<ServerPlayer>();
		lobby = new ArrayList<ServerPlayer>();
		invites = new ServerInviteManager(lobby);
		games = new ArrayList<ServerGame>();
		
		playerQ = new PlayerQueue();

		running = false;
		
		usePort = SERVER_PORT;
	}
	
	public Server(int port) {
		super("Honeybadger Console");
		// Let's do this!
		frontline = new ArrayList<ServerPlayer>();
		lobby = new ArrayList<ServerPlayer>();
		invites = new ServerInviteManager(lobby);
		games = new ArrayList<ServerGame>();
		
		playerQ = new PlayerQueue();
		
		running = false;
		
		usePort = port;
	}
	
	private void out(String msg) {
		System.out.println(msg);
	}

	private void serverSays(String msg) {
		// out("[Server] " + msg);
		servertext.append("[Server] " + msg + "\n");
	}
	
	private boolean isPlayerInServer(String player) {
		for (ServerPlayer p : frontline) {
			if (p.getName() != null) {
				if (p.getName().equals(player)) {
					return true;
				}
			}
		}
		
		for (ServerPlayer p : lobby) {
			if (p.getName().equals(player)) {
				return true;
			}
		}
		
		for (ServerGame sg : games) {
			if (sg.isPlayerInGame(player)) {
				return true;
			}
		}
		
		return false;
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
	
	private void broadcastPlayerJoin(ServerPlayer player) {
		for (ServerPlayer lobbyist : lobby) {
			lobbyist.net().tellLJOIN(player.getName());
		}
	}
	
	private void broadcastPlayerLeave(ServerPlayer[] players) {
		for (ServerPlayer p : players) {
			broadcastPlayerLeave(p);
		}
	}
	
	private void broadcastPlayerLeave(ServerPlayer player) {
		for (ServerPlayer p : lobby) {
			if (p != player) {
				p.net().tellLEAVE(player.getName());
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
						
						if (isPlayerInServer(username)) {
							p.net().tellERROR(RolitSocket.Error.LogInFailedException,
									"User already connected or still in a game.");
							p.net().close();
							frontline.remove(p);
							serverSays("Player " + p.getName() + " tried to duplicate connect");
						} else {
							String authKey = PKISocket.getRandomString(50);
							p.setAuthKeySent(username, authKey);
							pki.requestPublicKey(username);
	
							serverSays("Player " + username
									+ " wants to shake hands. "
									+ "Extended hand to " + username);
						}
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
						
						for (ServerPlayer otherP : lobby) {
							p.net().tellLJOIN(otherP.getName());
						}
						
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
					frontline.remove(p);
					broadcastPlayerLeave(p);
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
					p.net().tellBCAST("Welcome to HONEYBADGER's Controlit Server! "
							+ "If your name is not Michiel we welcome you. "
							+ "Otherwise, the exit is in the top right corner.");
				} else if (p.getAuthState() == PlayerAuthState.Authenticated) {
					serverSays("Shitty handshake, " + p.getName() + ". 1/10");
					p.net().tellERROR(RolitSocket.Error.LogInFailedException,
							"Wrong username/password #bitch cheater!");
					p.net().close();
				}
			}
		}
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
						otherP.net().tellCHATM(p.getName(), chatmsg);
					}

					break;
				case AL_LEAVE:
					serverSays(p.getName() + " left");
					p.net().close();
					lobby.remove(p);
					broadcastPlayerLeave(p);
					playerQ.removePlayer(p);
					// TODO: Clean up pending invites
					break;
				case AL_STATE:
					if (playerQ.isQueued(p)) {
						p.net().tellSTATE(PlayerState.WAITING);
					} else {
						p.net().tellSTATE(PlayerState.LOBBY);
					}
					break;
				case LO_NGAME:
					// TODO: Check if player has no invites
					playerQ.removePlayer(p);
					if (msg[0].equals("D") || msg[0].equals("H")) {
						playerQ.addDuoer(p);
					} else if (msg[0].equals("I")) {
						playerQ.addTrioer(p);
					} else if (msg[0].equals("J")) {
						playerQ.addQuatroer(p);
					} else {
						p.net().tellERROR(RolitSocket.Error.IllegalArgumentException,
								"NGAME only supports D, H, I, J");
					}
					break;
				case LO_PLIST:
					ArrayList<String> playersAvailable = new ArrayList<String>();
					for (ServerPlayer otherP : lobby) {
						playersAvailable.add(otherP.getName());
					}
					p.net().tellPLIST(playersAvailable.toArray(new String[0]));
					break;
				case LO_INVIT:
					if (msg[0].equals("R")) {
						if (!invites.hasInvite(p)) {
							String[] invitees = Arrays.copyOfRange(msg, 1, msg.length);
							invites.startInvite(p, invitees);
							playerQ.removePlayer(p);
						}
					} else {
						serverSays("Got response from " + p.getName() + ": " + msg[0]);
						invites.processResponse(p, msg[0]);
					}
					break;
				default:
					p.net().tellERROR(RolitSocket.Error.UnexpectedOperationException,
							msgType.toString());
					break;
			}
		}
	}
	
	private void handleQueues() {
		while (playerQ.hasDuo()) {
			ServerPlayer[] players = playerQ.getDuo();
			lobby.remove(players[0]);
			lobby.remove(players[1]);
			broadcastPlayerLeave(players);
			
			ServerGame game = new ServerGame(players);
			game.start();
			games.add(game);
		}
		while (playerQ.hasTrio()) {
			ServerPlayer[] players = playerQ.getTrio();
			lobby.remove(players[0]);
			lobby.remove(players[1]);
			lobby.remove(players[2]);
			broadcastPlayerLeave(players);
			
			ServerGame game = new ServerGame(players);
			game.start();
			games.add(game);
		}
		while (playerQ.hasQuatro()) {
			ServerPlayer[] players = playerQ.getQuatro();
			lobby.remove(players[0]);
			lobby.remove(players[1]);
			lobby.remove(players[2]);
			lobby.remove(players[3]);
			broadcastPlayerLeave(players);
			
			ServerGame game = new ServerGame(players);
			game.start();
			games.add(game);
		}
	}
	
	private void handleInvites() {
		ServerGame newGame = invites.processInvites();
		if (newGame != null) {
			newGame.start();
			ServerPlayer[] participants = newGame.getPlayers();
			for (ServerPlayer p : participants) {
				lobby.remove(p);
			}
			for (ServerPlayer p : participants) {
				broadcastPlayerLeave(p);
			}
			games.add(newGame);
			
			newGame = invites.processInvites();
		}
	}

	public void run() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		servertext = new JTextArea();
		getContentPane().add(servertext, BorderLayout.CENTER);
		setSize(640, 480);
		setVisible(true);
		
		serverSays("HONEYBADGER ON DUTY!");

		sb = new ServerBouncer(usePort);
		Thread sbThread = new Thread(sb);
		sbThread.start();
		serverSays("Started server socket");
		
		pki = new PKISocket();
		pki.start();
		serverSays("Started PKI handler");
		
		aliveTimer = new Stopwatch();
		
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

			// Check front line for clients wanting to get in
			for (int i = 0; i < frontline.size(); i++) {
				handleFrontline(frontline.get(i));
			}

			// Handle lobby mechanics
			for (int i = 0; i < lobby.size(); i++) {
				handleLobby(lobby.get(i));
			}
			handleQueues();
			
			handleInvites();
			
			// Check if any games are finished and if so, return players to the lobby
			Iterator<ServerGame> i = games.iterator();
			while (i.hasNext()) {
				ServerGame sg = i.next();
				if (sg.isFinished()) {
					ServerPlayer[] players = sg.getPlayers();
					for (ServerPlayer p : players) {
						if (p != null) {
							lobby.add(p);
							broadcastPlayerJoin(p);
							serverSays("Player " + p.getName()
									+ " entered the lobby");
						}
					}
					
					i.remove();
				}
			}
			
			// TODO
			if (aliveTimer.getElapsedTimeS() >= 5) {
				aliveTimer.restartMS();
			}

			try {
				Thread.sleep(17); // Magic constant, only the best primes work
			} catch (InterruptedException e) {
				System.out.println("Haha no way"); // ya way
			}
		}
	}

	public static void main(String[] args) {
		int port = Integer.parseInt(JOptionPane.showInputDialog(
				"Enter port number", String.valueOf(Server.SERVER_PORT)));
		
		
		Server serv = new Server(port);
		Thread servThread = new Thread(serv);
		servThread.start();

		try {
			servThread.join();
		} catch (InterruptedException e) {
			System.out.println("Interruped Exception?");
			return;
		}

		System.out.println("-------- All done! --------");
		System.out.println("-------- HoneyBadger HRTP Server wishes you a good day! --------");
	}
}