package com.mayhem.overlay;

import java.io.Serializable;
import rice.p2p.commonapi.Id;

public class PlayerState implements Serializable {
	private static final long serialVersionUID = 4147854283318896390L;
	private Id id;
	private long x, y;

	public PlayerState(Id id) {
		this.id = id;
		x = y = -1;
	}

	public PlayerState(long x, long y) {
		this.x = x;
		this.y = y;
	}

	public PlayerState(Id id, long x, long y) {
		this.id = id;
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
		return this.id;
	}

	public long getX() {
		return this.x;
	}

	public long getY() {
		return this.y;
	}
}
