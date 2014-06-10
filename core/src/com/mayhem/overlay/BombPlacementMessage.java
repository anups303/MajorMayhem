package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class BombPlacementMessage extends Message {
	private static final long serialVersionUID = 4266857723473558476L;
	private Id sender;
	private long messageId;
	private int x, y;
	
	public BombPlacementMessage(Id sender, int x, int y) {
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
