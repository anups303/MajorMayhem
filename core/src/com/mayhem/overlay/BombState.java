package com.mayhem.overlay;

import java.io.Serializable;

import rice.p2p.commonapi.Id;

public class BombState implements Serializable {
	private static final long serialVersionUID = -2509591107374397748L;
	private Id playerId;
	private int x, y;

	public BombState(Id playerId, int x, int y) {
		this.playerId = playerId;
		this.x = x;
		this.y = y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Id getId() {
		return this.playerId;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}
}
