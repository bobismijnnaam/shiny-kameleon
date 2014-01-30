package tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import network.ServerBouncer;

public class ServerBouncerTest {
	public static final int PORT = 2014;
	
	ServerBouncer sb;
	Thread sbThread;
	
	Socket sock;
	Socket sbSock;
	
	BufferedReader sockIn;
	BufferedWriter sockOut;
	BufferedReader sbIn;
	BufferedWriter sbOut;
	
	PrintStream os;
	
	public void eval(String test, boolean result, boolean shouldBe) {
		eval(test, Boolean.toString(result), Boolean.toString(shouldBe));
	}
	
	public void eval(String test, String result, String shouldBe) {
		if (!result.equals(shouldBe)) {
			os.println("Testing " + test);
			os.println("Expected: \"" + shouldBe + "\"");
			os.println("Result: \"" + result + "\"");
		} else {
			os.println(test + " works");
		}
	}
	
	public void pause(int s) {
		try {
			Thread.sleep(s);
		} catch (InterruptedException e) {
			System.out.println("Waiting was interrupted!");
		}
	}

	public ServerBouncerTest() {
		os = System.out;
			
		// Disable output
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int arg0) throws IOException {
	
			}
		}));
	}
	
	public void setup() throws IOException {
		sb = new ServerBouncer(PORT);
		sbThread = new Thread(sb);
		sbThread.start();
		
		sock = new Socket("localhost", PORT);
		
		while (!(sb.isStartedProperly() == ServerBouncer.START_SUCCESS)) {
			pause(100);
		}
	}
	
	public void close() {
		sb.close();
		
		try {
			sbThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void testIsNewConnection() throws IOException {
		setup();
		
		while (!sb.isNewConnection()) {
			pause(100);
		}
		
		Socket sock2 = sb.getNewConnection();
		
		eval("isNewConnection", sb.isNewConnection(), false);
		
		eval("getNewConnection1", sock2 != null, true);
		
		eval("getNewConnection2", sb.getNewConnection() == null, true);
		
		sock2.close();
		
		close();
	}
	
	public void testClose() throws IOException {
		setup();
		
		Socket s1 = new Socket("localhost", PORT);
		Socket s2 = new Socket("localhost", PORT);
		Socket s3 = new Socket("localhost", PORT);
		
		close();
		
		eval("close", sb.isNewConnection(), false);
	}
	
	public void testX() throws IOException {
		setup();
		
		close();
	}
	
	public static void main(String[] args) throws IOException {
		ServerBouncerTest sbt = new ServerBouncerTest();
		
		sbt.testIsNewConnection();
		
		sbt.testClose();
	}
}
