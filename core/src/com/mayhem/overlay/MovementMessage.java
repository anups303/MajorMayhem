package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class MovementMessage extends Message {
	private static final long serialVersionUID = 6561350713073687226L;
	Id sender;
	int x, y;

	public MovementMessage(Id sender, int x, int y) {
		this.sender = sender;
		this.x = x;
		this.y = y;
	}

	public Id getSender() {
		return this.sender;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

}
