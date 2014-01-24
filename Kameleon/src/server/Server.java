package server;

public class Server {
	public static final int SERVER_PORT = 8494;

	public Server() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		// RolitSocket server = new RolitSocket
		
//		Thread server = new Thread() {
//			public void run() {
//				RolitSocket server = new RolitSocket(SERVER_PORT);
//			}
//		};
//		
//		Thread client = new Thread() {
//			public void run() {
//				RolitSocket client = new RolitSocket("localhost", SERVER_PORT);
//			}
//		};
		
//		server.start();
//		client.start();
		
		RolitSocket server = new RolitSocket(SERVER_PORT);
		RolitSocket client = new RolitSocket("localhost", SERVER_PORT);
		
		System.out.println("Initialized variables. Waiting for them to connect...");
		
		while (!server.isRunning() || !client.isRunning()) {
			// System.out.println(server.isRunning() + " " + client.isRunning());
			// Let the thread sleep so the other processes can get some cpu time
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("They seem to both have connected and started properly!");
		
		
		server.askProto();
		
		while (server.getQueuedMsgType() == RolitSocket.MessageType.FB_PROTO) {
			// Waiting for response...
		}
		
		System.out.println("A message seems to have arrived!");
		
		System.out.println(server.getQueuedMsg());
		
		client.close();
		server.close();
	}

}
