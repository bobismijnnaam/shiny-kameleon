package network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.security.SecureRandom;

public class PKISocket extends Thread {
	public static final String PKI_SERVER_ADDR = "ss-security.student.utwente.nl";
	public static final int PKI_SERVER_PORT = 2013;
	
	 private static SecureRandom random = new SecureRandom();

	String user;
	String pass;
	String key = null;
		
	public PKISocket(String inputUser, String inputPass) {
		user = inputUser;
		pass = inputPass;
	}
	
	public void run() {
		Scanner in;
		BufferedWriter out;
		Socket sock;
		
		System.out.println("Opening socket...");
		
		try {
			sock = new Socket(PKI_SERVER_ADDR, PKI_SERVER_PORT);
		} catch (IOException e) {
			System.out.println("Network error: could not open socket");
			return;
		}
		
		System.out.println("Socket opened. Opening streams...");
		
		try {
			in = new Scanner(new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		} catch (IOException e) {
			System.out.println("Network error: could not open socket streams");
			return;
		}
		
		System.out.println("Streams opened. Sending request...");
		
		try {
			out.write("IDPLAYER " + user + " " + pass + System.lineSeparator());
			out.flush();
		} catch (IOException e) {
			System.out.println("Could not write to outputstream");
			return;
		}
		
		System.out.println("Message sent! Waiting for response...");
		
		while (!in.hasNext()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("Wait was interrupted");
				return;
			}
			
			System.out.print("|");
		}
		
		System.out.println("Received repsonse!");
		
		System.out.print("Response: \"");
		key = in.nextLine();
		System.out.print(key);
		System.out.println("\"");
		
		System.out.println("Closing socket...");
		
		try {
			sock.close();
		} catch (IOException e) {
			System.out.println("Couldn't close socket");
			return;
		}
		
		System.out.println("Socket closed! Processing key...");
		
	}
	
	public boolean keyReady() {
		return key != null;
	}
	
	public String getKey() {
		return new String(key);
	}
	
	public static String getRandomString() {
		return PKISocket.getRandomString(15);
	}
	
	public static String getRandomString(int l) {
		ArrayList<Character> chr = new ArrayList<Character>(60);
		
		for (int i = 97; i <= 122; i++) {
			chr.add(new Character((char) i));
		}
		for (int i = 65; i <= 90; i++) {
			chr.add(new Character((char) i));
		}
		for (int i = 48; i <= 57; i++) {
			chr.add(new Character((char) i));
		}
		
		String randomString = new String();
		for (int i = 0; i < l; i++) {
			randomString += chr.get((int) Math.floor(random.nextFloat() * chr.size()));
		}
		
		return randomString;
	}
	
	
	
	public static void main(String[] args) {
		PKISocket pki = new PKISocket("player_rub", "hond");
		pki.start();
	}

}
