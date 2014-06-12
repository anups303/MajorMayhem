package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class LeaveMessage extends Message {
	private static final long serialVersionUID = -2042122046463742754L;

	Id sender;

	public LeaveMessage(Id sender) {
		this.sender = sender;
	}

	public Id getSender() {
		return sender;
	}

	public String toString() {
		return "Leave Message: -sender:" + sender;
	}
}
