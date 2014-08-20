package com.mayhem.overlay;

import java.io.Serializable;
import rice.p2p.commonapi.Id;


//Data-Object class which maintain state of a player
public class PlayerState implements Serializable {
	private static final long serialVersionUID = 4147854283318896390L;
	private Id id;
	private long x, y;
	private int score;
	private boolean alive;

	public PlayerState(Id id) {
		this(id, -1, -1, 0);
	}

	public PlayerState(long x, long y) {
		this(null, x, y, 0);
	}

	public PlayerState(Id id, long x, long y) {
		this(id, x, y, 0);
	}

	public PlayerState(Id id, long x, long y, int score) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.score = score;
		setAlive(true);
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

	public int getScore() {
		return this.score;
	}

	public void increaseScore() {
		this.score++;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
}
