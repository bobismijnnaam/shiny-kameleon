package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

import utility.Utils;

// Should actually be quite easy 0- it was, kinda

public class PKISocket extends Thread {
	public static final String PKI_SERVER_ADDR = "ss-security.student.utwente.nl";
	public static final int PKI_SERVER_PORT = 2013;
	public static final String PKI_USER_INVALID = new String("INVALID USER");
	
	private static SecureRandom random = new SecureRandom();

	private String user;
	private String pass;
	private String privateKey = null;
	
	private boolean running = false;
	
	private Lock pantryLock = new ReentrantLock();
	private Map<String, String> pubkeys = null;
	private List<String> leftovers = null;
		
	public PKISocket(String inputUser, String inputPass) {
		user = inputUser;
		pass = inputPass;
	}
	
	public PKISocket() {
//		user = inputUser;
//		pass = null;
		pubkeys = new HashMap<String, String>();
		leftovers = new ArrayList<String>();
	}
	
	public void run() {
		if (pubkeys == null && leftovers == null) {
			retrievePrivateKey();
		} else {
			retrievePublicKey();
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void retrievePublicKey() {
		BufferedWriter out;
		BufferedReader in;
		Socket sock;
		
		System.out.println("[PKI] Opening PKI socket...");
		
		try {
			sock = new Socket(PKI_SERVER_ADDR, PKI_SERVER_PORT);
			
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			
			running = true;
			
			System.out.println("[PKI] PKI Socket intialization successful");
			
			while (running) {
				pantryLock.lock();
				boolean leftoversAvailable = !leftovers.isEmpty();
				pantryLock.unlock();
				
				if (leftoversAvailable) {
					pantryLock.lock();
					String requestedUser = leftovers.remove(0);
					pantryLock.unlock();
					
					out.write("PUBLICKEY player_" + requestedUser + System.lineSeparator());
					out.flush();
					
					while (!in.ready()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							System.out.println("[PKI] Wait was interrupted");
							sock.close();
							return;
						}
					}
					
					String response = in.readLine();
					String[] resploded = response.split(" ");
					
					pantryLock.lock();
					if (resploded[0].equals("PUBKEY")) {
						System.out.println("Received pubkey: " + resploded[1]);
						pubkeys.put(requestedUser, resploded[1]);
					} else if (resploded[0].equals("ERROR")) {
						System.out.println("[PKI] " + Utils.join(resploded));
						pubkeys.put(requestedUser, PKI_USER_INVALID);
						System.out.println("mapkey: " + pubkeys.get(requestedUser));
					} else {
						System.out.println("[PKI] UNKNOWN ERROR: " + Utils.join(resploded));
						pubkeys.put(requestedUser, new String("PKI_USER_INVALID"));
					}
					pantryLock.unlock();
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.out.println("[PKI] Wait was interrupted");
					sock.close();
					return;
				}	
			}
			
			sock.close();
			
		} catch (IOException e) {
			System.out.println("[PKI] Network error: something went wrong (?)");
			return;
		}
		
		return;
	}
	
	public boolean hasKey(String requestedUser) {
		if (leftovers == null && pubkeys == null) {
			throw new NullPointerException("YOU ARE TRYING TO GET PKI IN THE WRONG MODE");
		}
		
		pantryLock.lock();
		boolean keyAvailable = pubkeys.containsKey(requestedUser);
		pantryLock.unlock();
		return keyAvailable;
	}
	
	public String getPublicKey(String requestedUser) {
		if (leftovers == null && pubkeys == null) {
			throw new NullPointerException("YOU ARE TRYING TO GET PKI IN THE WRONG MODE");
		}
		
		pantryLock.lock();
		if (!hasKey(requestedUser)) {
			pantryLock.unlock();
			return null;
		} else {
			String theKey = pubkeys.get(requestedUser);
			pantryLock.unlock();
			return theKey;
		}
	}
	
	public void requestPublicKey(String requestedUser) {
		if (leftovers == null && pubkeys == null) {
			throw new NullPointerException("YOU ARE TRYING TO GET PKI IN THE WRONG MODE");
		}
		
		pantryLock.lock();
		leftovers.add(requestedUser);
		pantryLock.unlock();
	}
	
	public void close() {
		if (leftovers == null && pubkeys == null) {
			throw new NullPointerException("YOU ARE TRYING TO GET PKI IN THE WRONG MODE");
		}
		
		running = false;
	}
	
	public void retrievePrivateKey() {
		Scanner in;
		BufferedWriter out;
		Socket sock;
		
		System.out.println("[PKI] Opening socket...");
//		System.out.println("[PKI] (This is for private key)");
		
		try {
			sock = new Socket(PKI_SERVER_ADDR, PKI_SERVER_PORT);
		} catch (IOException e) {
			System.out.println("[PKI] Network error: could not open socket");
			return;
		}
		
//		System.out.println("Socket opened. Opening streams...");
		
		try {
			in = new Scanner(new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		} catch (IOException e) {
			System.out.println("[PKI] Network error: could not open socket streams");
			return;
		}
		
//		System.out.println("Streams opened. Sending request...");
		
		try {
			out.write("IDPLAYER player_" + user + " " + pass + System.lineSeparator());
			out.flush();
		} catch (IOException e) {
			System.out.println("[PKI] Could not write to outputstream");
			return;
		}
		
//		System.out.println("Message sent! Waiting for response...");
		
		while (!in.hasNext()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("[PKI] Wait was interrupted");
				return;
			}
			
			System.out.print("|");
		}
		
//		System.out.println("Received repsonse!");
		
		in.next();
		privateKey = in.next();
		
//		System.out.println("Closing PKI socket...");
		
		try {
			sock.close();
		} catch (IOException e) {
			System.out.println("[PKI] Couldn't close socket");
			return;
		}
		
		System.out.println("[PKI] Socket PKI closed! Private key: \"" + privateKey + "\"");
	}
	
	public boolean isPrivateKeyReady() {
		return privateKey != null;
	}
	
	public String getPrivateKey() {
		return new String(privateKey);
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
	
	public static String signMessage(String plain, String privatekey) {
		// Loosely copied from week 8 manual
		// Construct key
		byte[] rawKey = Base64.decodeBase64(privatekey);
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
	
	public static boolean verifySignature(String plain, String publickey, String signature64) {
		// Loosely copied from week 8 manual
		byte[] rawKey = Base64.decodeBase64(publickey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(rawKey);		
		
		KeyFactory fact;
		PublicKey pub;
		Signature sig;
		byte[] signature = Base64.decodeBase64(signature64);
	
		try {
			fact = KeyFactory.getInstance("RSA");
			pub = fact.generatePublic(keySpec);
			sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(pub);
			sig.update(plain.getBytes());
			Boolean check = sig.verify(signature);
			return check;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Algorithm unknown!");
		} catch (InvalidKeySpecException e) {
			System.out.println("Invalid key specification! Fuuu");
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			System.out.println("Invalid key!");
		} catch (SignatureException e) {
			System.out.println("Signature exception!");
		}
		
		return false;
		
//		String expectedSignature = PKISocket.getSignature(plain, key);
//		return expectedSignature.equals(signature64);
	}
	
	public static void main(String[] args) {
		PKISocket pkiServer = new PKISocket();
//		PKISocket pkiClient = new PKISocket("player_test1", "test1");
		
		try {
			pkiServer.start();
			pkiServer.requestPublicKey("test1");
			pkiServer.requestPublicKey("test2");
			pkiServer.requestPublicKey("dne");
			
			while (!pkiServer.hasKey("test1")) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.out.println("[PKI] Wait was interrupted");
					return;
				}
			}
			
			while (!pkiServer.hasKey("test2")) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.out.println("[PKI] Wait was interrupted");
					return;
				}
			}
			
			while (!pkiServer.hasKey("dne")) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.out.println("[PKI] Wait was interrupted");
					return;
				}
			}
			
			System.out.println("test1: " + pkiServer.getPublicKey("test1"));
			System.out.println("test2: " + pkiServer.getPublicKey("test2"));
			System.out.println("dne: " + pkiServer.getPublicKey("dne"));
			
			pkiServer.close();
			pkiServer.join();
//			System.out.println("server closed");
//			
//			System.out.println("\n");
//			
//			pkiClient.start();
//			pkiClient.join();
//			System.out.println("client closed");
//			
//			System.out.println("\n");
//			
//			String signature = PKISocket.signMessage("hi", pkiClient.getPrivateKey());
//			
//			Boolean verified = PKISocket.verifySignature("hi", pkiServer.getPublicKey(),
//					signature);
//			
//			if (verified) {
//				System.out.println("VERIFIED");
//			} else {
//				System.out.println("NOT VERIFIED");
//			}
		} catch (InterruptedException e) {
			System.out.println("Thread got interruped!");
		}
	}

}
