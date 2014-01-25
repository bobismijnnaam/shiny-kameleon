package network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

public class PKISocket extends Thread {
	public static final String PKI_SERVER_ADDR = "ss-security.student.utwente.nl";
	public static final int PKI_SERVER_PORT = 2013;
	
	private static SecureRandom random = new SecureRandom();

	String user;
	String pass;
	String privateKey = null;
	String publicKey = null;
		
	public PKISocket(String inputUser, String inputPass) {
		user = inputUser;
		pass = inputPass;
	}
	
	public PKISocket(String inputUser) {
		user = inputUser;
		pass = null;
	}
	
	public void run() {
		if (pass == null && user != null) {
			retrievePublicKey();
		} else if (pass != null && user != null) {
			retrievePrivateKey();
		}
	}
	
	public void retrievePrivateKey() {
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
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Wait was interrupted");
				return;
			}
			
			System.out.print("|");
		}
		
		System.out.println("Received repsonse!");
		
		in.next();
		privateKey = in.next();
		System.out.println("Private key: \"" + privateKey + "\"");
		
		System.out.println("Closing socket...");
		
		try {
			sock.close();
		} catch (IOException e) {
			System.out.println("Couldn't close socket");
			return;
		}
		
		System.out.println("Socket closed!");
	}
	
	public void retrievePublicKey() {
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
			out.write("PUBLICKEY " + user + System.lineSeparator());
			out.flush();
		} catch (IOException e) {
			System.out.println("Could not write to outputstream");
			return;
		}
		
		System.out.println("Message sent! Waiting for response...");
		
		while (!in.hasNext()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Wait was interrupted");
				return;
			}
			
			System.out.print("|");
		}
		
		System.out.println("Received repsonse!");
		
		in.next();
		publicKey = in.next(); 
		System.out.println("Public key: \"" + publicKey + "\"");
		
		System.out.println("Closing socket...");
		
		try {
			sock.close();
		} catch (IOException e) {
			System.out.println("Couldn't close socket");
			return;
		}
		
		System.out.println("Socket closed!");
	}
	
	public boolean isPrivateKeyReady() {
		return privateKey != null;
	}
	
	public boolean isPublicKeyReady() {
		return publicKey != null;
	}
	
	public String getPrivateKey() {
		return new String(privateKey);
	}
	
	public String getPublicKey() {
		return new String(publicKey);
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
	
	public static String getSignature(String plain, String key) {
		// Loosely copied from week 8 manual
		// Construct key
		byte[] rawKey = Base64.decodeBase64(key);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(rawKey);		
		
		KeyFactory fact;
		PrivateKey priv;
		Signature sig;
		byte[] signature = null;
		try {
			fact = KeyFactory.getInstance("RSA");
			priv = fact.generatePrivate(keySpec);
		
			// Sign message
			sig = Signature.getInstance("SHA1withRSA");
			sig.initSign(priv);
		
			sig.update(plain.getBytes());
			signature = sig.sign();
		} catch (SignatureException e) {
			System.out.println("Signature error: signature exception (see docs)");
			return null;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Algorithm error: Given algorithm doesn't exist.");
			return null;
		} catch (InvalidKeySpecException e) {
			System.out.println("Key error: invalid key specification");
			return null;
		} catch (InvalidKeyException e) {
			System.out.println("Key error: invalid key");
			return null;
		}
		
		String signature64 = Base64.encodeBase64String(signature);
		
		return signature64;
	}
	
	public static boolean verifySignature(String plain, String key, String signature64) {
		String expectedSignature = PKISocket.getSignature(plain, key);
		return expectedSignature.equals(signature64);
	}
	
	public static void main(String[] args) {
		PKISocket pki = new PKISocket("player_rub", "hond");
		
		try {
			pki.start();
			pki.join();
			System.out.println("\n\n");
			pki = new PKISocket("player_rub");
			pki.start();
			pki.join();
		} catch (InterruptedException e) {
			System.out.println("Thread got interruped!");
		}
	}

}