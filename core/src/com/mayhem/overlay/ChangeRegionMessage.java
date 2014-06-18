package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class ChangeRegionMessage extends Message {
	private static final long serialVersionUID = 5921572407345035176L;
	private Id sender;

	public Id getSender() {
		return this.sender;
	}
}
