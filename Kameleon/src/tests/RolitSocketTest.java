package tests;

import static network.RolitSocket.MessageType.*;

import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import utility.Utils;
import network.ClientRolitSocket;
import network.ClientRolitSocket.NGAMEFlags;
import network.PlayerState;
import network.RolitSocket;
import network.ServerRolitSocket;

public class RolitSocketTest {
	
	public static final int PORT = 2014;

	ClientRolitSocket crs;
	ServerRolitSocket srs;
	
	PrintStream os;
	
	public RolitSocketTest() {
		os = System.out;
		
		// Disable output
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int arg0) throws IOException {

			}
		}));
	}
	
	public void pause(int s) {
		try {
			Thread.sleep(s);
		} catch (InterruptedException e) {
			System.out.println("Waiting was interrupted!");
		}
	}
	
	public void waitFor(RolitSocket.MessageType mt, RolitSocket sock) {
		while (sock.getQueuedMsgType() != mt) {
			pause(17);
		}
	}
	
	public void setup() throws IOException {
		ServerSocket ssock = new ServerSocket(PORT);
		
		crs = new ClientRolitSocket("localhost", PORT);
		crs.start();
		
		Socket serverSocket = ssock.accept();
		
		srs = new ServerRolitSocket(serverSocket);
		srs.start();
		
		while (!crs.isRunning() || !srs.isRunning()) {
			pause(17);
		}
		
		ssock.close();
	}
	
	public void close() {
		crs.close();
		srs.close();
	}
	
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
	
	public void testProto() throws IOException {
		setup();
		
		crs.askPROTO();
		
		waitFor(FB_PROTO, crs);
		
		String result = crs.getQueuedMsg();
		
		eval("PROTO", result, "INFB 1.4.0");
		
		close();
	}
	
	public void testSinfo() throws IOException {
		setup();
		
		crs.askSINFO();
		
		waitFor(FB_SINFO, crs);
		
		String result = crs.getQueuedMsg();
		
		eval("SINFO", result, "HONEYBADGER 66.79.66");
		
		close();
	}
	
	public void testError() throws IOException {
		setup();
		
		crs.tellERROR(RolitSocket.Error.AuthenticationServerConnectionException);
		
		waitFor(FB_ERROR, srs);
		
		String result1 = srs.getQueuedMsg();
		
		srs.tellERROR(RolitSocket.Error.InvalidLocationException, "ERRORTEST");
		
		waitFor(FB_ERROR, crs);
		
		String result2 = crs.getQueuedMsg();
		
		eval("ERROR1", result1, "AuthenticationServerConnectionException");
		eval("ERROR2", result2, "InvalidLocationException ERRORTEST");
		
		close();
	}
	
	public void testHello() throws IOException {
		setup();
		
		crs.tellHELLO();
		
		waitFor(AC_HELLO, srs);
		
		String result = srs.getQueuedMsg();
		
		eval("HELLO", result, "CL");
		
		close();
	}
	
	public void testAlive() throws IOException {
		setup();
		
		crs.askALIVE();
		
		eval("ALIVE", crs.isRunning(), true);
		
		close();
	}
	
	public void testLogin() throws IOException {
		setup();
		
		crs.askLOGIN("test");
		
		waitFor(AC_LOGIN, srs);
		
		String result = srs.getQueuedMsg();
		
		eval("LOGIN", result, "test");
		
		close();
	}
	
	// TODO: testVSIGN?
	
	public void testState() throws IOException {
		setup();
		
		crs.askSTATE();
		
		waitFor(AL_STATE, srs);
		
		String result1 = srs.getQueuedMsg();
		
		srs.tellSTATE(PlayerState.PLAYING);
		
		waitFor(AL_STATE, crs);
		
		String result2 = crs.getQueuedMsg();
		
		eval("STATE1", result1, "");
		eval("STATE2", result2, "PLAYING");
		
		close();
	}
	
	public void testNgame() throws IOException {
		setup();
		
		crs.askNGAME(NGAMEFlags.FourPlayerGame);
		
		waitFor(LO_NGAME, srs);
		
		String result = srs.getQueuedMsg();
		
		eval("NGAME", result, "J");
		
		close();
	}
	
	public void testGmove() throws IOException {
		setup();
		
		crs.tellGMOVE(4, 2);
		
		waitFor(IG_GMOVE, srs);
		
		String result1 = srs.getQueuedMsg();
		
		srs.tellGMOVE(2, 4, 2);
		
		waitFor(IG_GMOVE, crs);
		
		String result2 = crs.getQueuedMsg();
		
		eval("GMOVE1", result1, "4 2");
		eval("GMOVE2", result2, "2 4 2");
		
		close();
	}
	
	public void testBoard() throws IOException {
		setup();
		
		crs.askBOARD();
		
		waitFor(IG_BOARD, srs);
		
		String result1 = srs.getQueuedMsg();
		
		int[] board = new int[64];
		for (int i = 0; i < 64; i++) {
			board[i] = i % 2;
		}
		
		srs.tellBOARD(board);
		
		waitFor(IG_BOARD, crs);
		
		String result2 = crs.getQueuedMsg();

		String expected2 = "";
		expected2 = Utils.join(board, " ");
		
		eval("BOARD1", result1, "");
		eval("BOARD2", result2, expected2);
		
		close();
	}
	
	public void testGplist() throws IOException {
		setup();
		
		crs.askGPLST();
		
		waitFor(IG_GPLST, srs);
		
		String result1 = srs.getQueuedMsg();
		
		String[] players = {"Bob", "Ruben"};
		srs.tellGPLST(players);
		
		waitFor(IG_GPLST, crs);
		
		String result2 = crs.getQueuedMsg();
		
		eval("GPLIST1", result1, "");
		eval("GPLIST2", result2, "Bob Ruben");
		
		close();
	}
	
	public void testX() throws IOException {
		setup();
		
		close();
	}
	
	public static void main(String[] args) throws IOException {
		RolitSocketTest rst = new RolitSocketTest();
		rst.testProto();
		
		rst.testSinfo();
		
		rst.testError();
		
		rst.testHello();
		
		rst.testAlive();
		
		rst.testLogin();
		
		rst.testState();
		
		rst.testNgame();
		
		rst.testGmove();
		
		rst.testBoard();
		
		rst.testGplist();
	}
	
}
