package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class JoinMessage extends Message {
	private static final long serialVersionUID = -3322417816542869716L;
	Id sender;

	public JoinMessage(Id sender) {
		this.sender = sender;
	}
	
	public Id getSender(){
		return sender;
	}

	public String toString() {
		return "Join Message: -sender:" + sender;
	}
}
