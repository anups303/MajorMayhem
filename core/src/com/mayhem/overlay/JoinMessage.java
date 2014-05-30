package com.mayhem.overlay;

import rice.pastry.Id;

public class JoinMessage extends Message {
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
