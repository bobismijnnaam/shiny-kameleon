package network;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerInviteManager {

	private List<ServerPlayer> lobby;
	private List<ServerInvite> invites;
	
	public ServerInviteManager(List<ServerPlayer> inputLobby) {
		lobby = inputLobby;
		invites = new ArrayList<ServerInvite>();
	}
	
	public boolean hasInvite(ServerPlayer p) {
		for (ServerInvite invite : invites) {
			if (invite.isInvited(p)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void startInvite(ServerPlayer starter, String[] inputInvitees) {
		ServerPlayer[] invitees = new ServerPlayer[inputInvitees.length + 1];
		for (int i = 0; i < inputInvitees.length; i++) {
			for (ServerPlayer p : lobby) {
				if (p.getName().equals(inputInvitees[i])) {
					invitees[i] = p;
				}
			}
		}
		invitees[invitees.length - 1] = starter;
		invites.add(new ServerInvite(starter, invitees));
	}
	
	public void processResponse(ServerPlayer p, String response) {
		for (ServerInvite i : invites) {
			i.processResponse(p, response);
		}
	}
	
	public ServerGame processInvites() {
		Iterator<ServerInvite> i = invites.iterator();;
		while (i.hasNext()) {
			ServerInvite game = i.next();
			if (game.getInviteState() == ServerInvite.InviteState.Confirmed) {
				ServerPlayer[] participants = game.getPlayers();
				ServerGame theGame = new ServerGame(participants);
				i.remove();
				return theGame;
			} else if (game.getInviteState() == ServerInvite.InviteState.Failed) {
				i.remove();
			}
		}
		
		return null;
	}

}
