package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import network.RolitSocket.MessageType;
import network.ServerPlayer.PlayerAuthState;

public class Server extends Thread { // Extends JFrame?? Or do that seperately?
	public static final int SERVER_PORT = 8494;
	
	private List<ServerPlayer> frontline;
	private List<ServerPlayer> nonlobby;
//	private List<ServerPlayer> lobby;
//	private List<Invite> invites;
//	private List<ServerGame> games;
	
	private boolean running;

	public Server() {
		// Let's do this!
		frontline = new ArrayList<ServerPlayer>();
		nonlobby = new ArrayList<ServerPlayer>();
		
		running = false;
	}
	
	private void out(String msg) {
		System.out.println(msg);
	}
	
	private void clientConnected() {
		out("A client connected!");
	}
	
	public void run() {
		System.out.println("HONEYBADGER ACTIVATED!");
		out("HONEYBADGER ACTIVATED!");
		
		ServerBouncer sb = new ServerBouncer(SERVER_PORT);
		Thread sbThread = new Thread(sb);
		sbThread.start();
		
		// Set server loop to GOGOGOGOGOGOGO 
		running = true;
		
		while (running) {
			// Check if there connected a client
			// If so, add it to the frontlines! CHARGE
			while (sb.isNewConnection()) {
				// Extract the socket and create a new serverplayer
				ServerPlayer newPlayer = new ServerPlayer(sb.getNewConnection());
				newPlayer.net().start();
				frontline.add(newPlayer);
				// Log the connect
				clientConnected();
			}
			
			// Check front line for clients wanting to get in
			for (int i = 0; i < frontline.size(); i++) {
				// Transfer the reference
				// Store it in a value for quick access
				ServerPlayer p = frontline.get(i);

				// Only do stuff when socket is running!
				// Otherwise stream errors and shit				
				if (p.net().isRunning()) {
					try {
					// Check for incoming messages
						if (p.net().isNewMsgQueued()) {
							System.out.println(" check");
							switch (p.net().getQueuedMsgType()) {
								case AC_LOGIN:
									System.out.println("Someone trying to login");
									if (p.getAuthState() == PlayerAuthState.Unathenticated) { 
										String username = p.net().getQueuedMsg();
										String authKey = PKISocket.getRandomString(50);
										p.net().askVSIGN(authKey);
										p.setAuthKeySent(username, authKey);
									} else {
										p.net().tellERROR(
												RolitSocket.Error.UnexpectedOperationException,
												"L2AUTH 1");
									}
									
									break;
								case AC_VSIGN:
									if (p.getAuthState() == PlayerAuthState.KeySent) {
										p.setAuthKeyReceived(p.net().getQueuedMsg());
									} else {
										p.net().tellERROR(
												RolitSocket.Error.UnexpectedOperationException,
												"L2AUTH 2");
									}
									break;
								default:
									
									break;
							}
						}
						
						if (p.getAuthState() == PlayerAuthState.SignatureAwaitsChecking) {
							p.tryVerifySignature();
							if (p.getAuthState() == PlayerAuthState.Authenticated) {
								System.out.println("Player " + p.getName() + " was authenticated!");
							} else if (p.getAuthState() == PlayerAuthState.Unathenticated) {
								System.out.println("Player " + p.getName()
										+ " has not been verified!");
								
							}
						}
					} catch (IOException e) {
						p.net().close();
					}
				}
				
				// TODO: Checking for what type of lobby client can handle
				if (p.getAuthState() == PlayerAuthState.Authenticated) {
					try {
						p.net().tellHELLO();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					frontline.remove(i);
					nonlobby.add(p);
					out("Player entered the lobby!");
				}
			}
			
			try {
				Thread.sleep(100);
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
		System.out.println("-------- HoneyBadger HRTP Server wishes you a good day! --------");
	}
}

//RolitSocket server = new RolitSocket(SERVER_PORT);
//RolitSocket client = new RolitSocket("localhost", SERVER_PORT);
//
//server.start();
//client.start();
//
//System.out.println("Initialized variables. Waiting for them to connect...");
//
//while (!server.isRunning() || !client.isRunning()) {
//	// Let the thread sleep so the other processes can get some cpu time
//	try {
//		Thread.sleep(100);
//	} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//}
//
//System.out.println("They seem to both have connected and started properly!");
//
//server.askPROTO();
//
//while (server.getQueuedMsgType() != RolitSocket.MessageType.FB_PROTO) {
//	try {
//		Thread.sleep(100);
//	} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//}
//
//System.out.println("A message seems to have arrived!");
//
//System.out.println("Message: \"" + server.getQueuedMsg() + "\"");
//
//System.out.println("\n");
//
//System.out.print("Closing client, server. ");
//client.close();
//
//try {
//	Thread.sleep(200);
//} catch (InterruptedException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//
//server.close();
//
//try {
//	client.join();
//	server.join();
//} catch (InterruptedException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//
//System.out.println("Server still alive: " + server.isAlive());
//System.out.println("Client still alive: " + client.isAlive());
//System.out.println("Profit?");
