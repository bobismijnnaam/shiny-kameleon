package network;

public class Server {
	public static final int SERVER_PORT = 8494;

	public Server() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		RolitSocket server = new RolitSocket(SERVER_PORT);
		RolitSocket client = new RolitSocket("localhost", SERVER_PORT);
		
		server.start();
		client.start();
		
		System.out.println("Initialized variables. Waiting for them to connect...");
		
		while (!server.isRunning() || !client.isRunning()) {
			// Let the thread sleep so the other processes can get some cpu time
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("They seem to both have connected and started properly!");
		
		server.askPROTO();
		
		while (server.getQueuedMsgType() != RolitSocket.MessageType.FB_PROTO) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("A message seems to have arrived!");
		
		System.out.println("Message: \"" + server.getQueuedMsg() + "\"");
		
		System.out.println("\n");
		
		System.out.print("Closing client, server. ");
		client.close();
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		server.close();
		
		try {
			client.join();
			server.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Server still alive: " + server.isAlive());
		System.out.println("Client still alive: " + client.isAlive());
		System.out.println("Profit?");
		
		// Done!
	}

}
