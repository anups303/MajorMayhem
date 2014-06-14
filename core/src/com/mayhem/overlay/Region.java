package com.mayhem.overlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Region implements Serializable {
	private static final long serialVersionUID = 5702811196026168131L;
	protected static final int WIDTH = 20, HEIGHT = 20;

	protected long x, y;
	protected List<PlayerState> players;
	protected List<BombState> bombs;

	public Region() {
		this.players = new ArrayList<PlayerState>();
		this.bombs = new ArrayList<BombState>();
	}
	
	public void addPlayer(PlayerState ps){
		this.players.add(ps);
	}

	public List<PlayerState> getPlayers() {
		return this.players;
	}

	public List<BombState> getBombs() {
		return this.bombs;
	}

}
