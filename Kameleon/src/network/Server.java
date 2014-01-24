package network;

public class Server {
	public static final int SERVER_PORT = 8494;

	public Server() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		RolitSocket server = new RolitSocket(SERVER_PORT);
		RolitSocket client = new RolitSocket("localhost", SERVER_PORT);
		
//		Scanner scr = new Scanner("TOKEN TEKON\n");
//		System.out.println(scr.next());
//		System.out.println(scr.next());
//		System.out.println(scr.nextLine().length() + "");
		
		server.start();
		client.start();
		
		System.out.println(PKISocket.getRandomString());
		
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
		
		while (server.getQueuedMsgType() == RolitSocket.MessageType.FB_PROTO) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("A message seems to have arrived!");
		
		
		System.out.println(server.getQueuedMsg());
		
		System.out.print("Closing client. ");
		client.close();
		System.out.println("Client status: " + client.isRunning());
		System.out.print("Closing Server. ");
		server.close();
		System.out.println("Server status: " + server.isRunning());

		try {
			System.out.println("Joining client");
			System.out.println("Client running: " + client.isRunning());
			client.join();
			
			System.out.println("Joining server");
			System.out.println("Server running: " + server.isRunning());
			server.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Profit?");
		
		// Done!
	}

}
