package network;

import java.util.ArrayList;

public class ServerInvite {
	public enum InviteState {
		Pending,
		Confirmed,
		Failed
	}
	
	ServerPlayer[] players = null;
	ServerPlayer starter = null;
	int accepted = 1;
	boolean failed = false;
	
	public ServerInvite(ServerPlayer inputStarter, ServerPlayer... inputPlayers) {
		players = inputPlayers;
		starter = inputStarter;
		
		for (ServerPlayer p : players) {
			if (p != starter) {
				ArrayList<String> otherPlayers = new ArrayList<String>();
				
				for (ServerPlayer otherP : inputPlayers) {
					otherPlayers.add(otherP.getName());
				}
				
				p.net().askINVIT(otherPlayers.toArray(new String[0]));
			}
		}
	}
	
	public boolean isInviter(ServerPlayer p) {
		return p == starter;
	}
	
	public boolean isInvited(ServerPlayer p) {
		for (ServerPlayer inviteP : players) {
			if (p == inviteP) {
				return true;
			}
		}
		
		return false;
	}
	
	public void processResponse(ServerPlayer p, String response) {
		if (isInvited(p)) {
			if (response.equals(INVITStatus.Accept.toString())) {
				accepted++;
			} else if (response.equals(INVITStatus.Denied.toString())) {
				setInviteFailed();
			} else if (response.equals("LEAVE")) {
				setInviteFailed();
			}
		}
	}
	
	private void setInviteFailed() {
		failed = true;
		for (ServerPlayer p : players) {
			p.net().tellINVIT();
		}
	}

	public InviteState getInviteState() {
		if (failed) {
			return InviteState.Failed;
		} else if (accepted == players.length) {
			return InviteState.Confirmed;
		} else {
			return InviteState.Pending;
		}
	}
	
	public ServerPlayer[] getPlayers() {
		return players;
	}
}
