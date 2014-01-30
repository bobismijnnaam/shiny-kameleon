package tests;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import network.ServerGame;
import network.ServerPlayer;

public class ServerGameTest {
	
	public void pause(int s) {
		try {
			Thread.sleep(s);
		} catch (InterruptedException e) {
			System.out.println("Waiting was interrupted!");
		}
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
	
	private ServerGame sg;
	PrintStream os;
	
	ServerPlayer ruben = new ServerPlayer(null);
	ServerPlayer bob = new ServerPlayer(null);
	ServerPlayer[] players = {ruben, bob};

	public ServerGameTest() {
		// Disable output
		os = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int arg0) throws IOException {

			}
		}));
		
		ruben.setName("ruben");
		bob.setName("bob");
	}
	
	public void setup() {
		sg = new ServerGame(players);
	}
	
	public void testIsPlayerInGame() {
		setup();
		
		boolean result1 = sg.isPlayerInGame("ruben");
		boolean result2 = sg.isPlayerInGame("bob");
		boolean result3 = sg.isPlayerInGame("henky");
		
		eval("isPlayerInGame1", result1, true);
		eval("isPlayerInGame2", result2, true);
		eval("isPlayerInGame3", result3, false);
	}
	
	public void testGetPlayers() {
		setup();
		
		ServerPlayer[] playerInGame = sg.getPlayers();
		
		boolean result1 = playerInGame[0] == players[0] || playerInGame[0] == players[1];
		boolean result2 = playerInGame[1] == players[0] || playerInGame[1] == players[1];
		
		eval("getPlayers1", result1, true);
		eval("getPlayers2", result2, true);
	}
	
	public void testNextTurn() {
		setup();
		
		sg.nextTurn();
		sg.nextTurn();
		sg.nextTurn();
		
		int result1 = sg.getTurn();
		
		sg.nextTurn();
		
		int result2 = sg.getTurn();
		
		eval("nextTurn1", result1 == 1, true);
		eval("nextTurn2", result2 == 0, true);
	}
	
	public static void main(String[] args) {
		ServerGameTest sgt = new ServerGameTest();
		
		sgt.testIsPlayerInGame();
		
		sgt.testGetPlayers();
		
		sgt.testNextTurn();
	}

}
