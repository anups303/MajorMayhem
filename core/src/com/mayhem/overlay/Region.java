package com.mayhem.overlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rice.p2p.commonapi.Id;

import java.util.concurrent.CopyOnWriteArrayList;

public class Region implements Serializable {
	private static final long serialVersionUID = 5702811196026168131L;
	protected static final int WIDTH = 20, HEIGHT = 20;

	protected int mapId;
	protected long x, y;
	protected List<PlayerState> players;
	protected List<BombState> bombs;

	public Region(int mapId) {
		this.players = new CopyOnWriteArrayList<PlayerState>();
		this.bombs = new ArrayList<BombState>();
		x = y = 0;
		if (mapId == -1) {
			mapId = (new Random().nextInt(10) + 1);
			System.out.println(mapId);
		}
		this.mapId = mapId;
	}

	public void addPlayer(PlayerState ps) {
		this.players.add(ps);
	}

	public void addBomb(BombState b) {
		this.bombs.add(b);
	}

	public List<PlayerState> getPlayers() {
		return this.players;
	}

	public List<BombState> getBombs() {
		return this.bombs;
	}

	public int getMapId() {
		return mapId;
	}

	public boolean removePlayerById(Id playerId) {
		int j = -1;
		for (int i = 0; i < this.players.size(); i++) {
			if (this.players.get(i).getId() == playerId) {
				j = i;
				break;
			}
		}
		if (j >= 0) {
			this.players.remove(j);
			return true;
		}

		return false;
	}

	public void setPosition(long x, long y) {
		this.x = x;
		this.y = y;
	}
}
