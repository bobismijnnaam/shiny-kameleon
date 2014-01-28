package network;

import java.util.LinkedList;
import java.util.List;

public class PlayerQueue {
	private List<ServerPlayer> duoQ;
	private List<ServerPlayer> trioQ;
	private List<ServerPlayer> quatroQ;
	
	public PlayerQueue() {
		duoQ = new LinkedList<ServerPlayer>();
		trioQ = new LinkedList<ServerPlayer>();
		quatroQ = new LinkedList<ServerPlayer>();
	}
	
	public boolean isQueued(ServerPlayer p) {
		return duoQ.contains(p) || trioQ.contains(p) || quatroQ.contains(p);
	}
	
	public void addDuoer(ServerPlayer p) {
		if (!isQueued(p)) {
			duoQ.add(p);
		}
	}
	
	public void addTrioer(ServerPlayer p) {
		if (!isQueued(p)) {
			trioQ.add(p);
		}
	}
	
	public void addQuatroer(ServerPlayer p) {
		if (!isQueued(p)) {
			quatroQ.add(p);
		}
	}
	
	public boolean hasDuo() {
		return duoQ.size() >= 2;
	}
	
	public boolean hasTrio() {
		return trioQ.size() >= 3;
	}
	
	public boolean hasQuatro() {
		return quatroQ.size() >= 4;
	}
	
	public ServerPlayer[] getDuo() {
		ServerPlayer[] res = new ServerPlayer[2];
		if (hasDuo()) {
			res[0] = duoQ.remove(0);
			res[1] = duoQ.remove(0);
			return res;
		} else {
			return null;
		}
	}
	
	public ServerPlayer[] getTrio() {
		ServerPlayer[] res = new ServerPlayer[3];
		if (hasTrio()) {
			res[0] = duoQ.remove(0);
			res[1] = duoQ.remove(0);
			res[2] = duoQ.remove(0);
			return res;
		} else {
			return null;
		}
	}
	
	public ServerPlayer[] getQuatro() {
		ServerPlayer[] res = new ServerPlayer[4];
		if (hasQuatro()) {
			res[0] = duoQ.remove(0);
			res[1] = duoQ.remove(0);
			res[2] = duoQ.remove(0);
			res[3] = duoQ.remove(0);
			return res;
		} else {
			return null;
		}
	}
	
}
