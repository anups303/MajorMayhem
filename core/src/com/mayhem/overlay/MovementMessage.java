package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class MovementMessage extends Message implements IAcknowledgeable {
	private static final long serialVersionUID = 6561350713073687226L;
	private Id sender;
	private long messageId;
	private int x, y;

	public MovementMessage(Id sender, int x, int y) {
		messageId = new java.util.Random().nextLong();
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

	public long getMessageId() {
		return messageId;
	}
}
