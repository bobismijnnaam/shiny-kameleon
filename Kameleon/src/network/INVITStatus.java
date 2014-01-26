package network;

public enum INVITStatus {
//	Request, // You can perform this enum value through
	// the proper channel askInvit(String...) overload
	
	Accept,
	Denied,
	Failed;
	
	private String s;
	
	static {
//		Request.s = "R";
		
		Accept.s = "A";
		Denied.s = "D";
		Failed.s = "F";
	}
	
	public String toString() {
		return s;
	}
}
