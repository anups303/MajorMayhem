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
	protected List<Pair<Integer, Integer>> destroyedBlocks;

	public Region(int mapId) {
		this.players = new CopyOnWriteArrayList<PlayerState>();
		this.bombs = new ArrayList<BombState>();
		this.destroyedBlocks = new ArrayList<Pair<Integer, Integer>>();
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

	public List<Pair<Integer, Integer>> getDestroyedBlocks() {
		return this.destroyedBlocks;
	}

	private int indexOf(Id playerId) {
		int j = -1;
		for (int i = 0; i < this.players.size(); i++) {
			if (this.players.get(i).getId() == playerId) {
				j = i;
				break;
			}
		}
		return j;
	}

	public boolean removePlayerById(Id playerId) {
		int j = indexOf(playerId);

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

	@SuppressWarnings("unchecked")
	public Region clone() {
		try {
			Region r = new Region(this.mapId);
			if (this.players != null)
				r.players = (CopyOnWriteArrayList<PlayerState>) ((CopyOnWriteArrayList<PlayerState>) this.players)
						.clone();
			if (this.bombs != null)
				r.bombs = (ArrayList<BombState>) ((ArrayList<BombState>) this.bombs)
						.clone();
			if (this.destroyedBlocks != null)
				r.destroyedBlocks = (ArrayList<Pair<Integer, Integer>>) ((ArrayList<Pair<Integer, Integer>>) this.destroyedBlocks)
						.clone();
			r.x = this.x;
			r.y = this.y;
			return r;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public void increaseScore(Id playerId) {
		int j = indexOf(playerId);

		if (j >= 0)
			this.players.get(j).increaseScore();
	}

	public void setAlive(Id playerId, boolean alive) {
		int j = indexOf(playerId);

		if (j >= 0)
			this.players.get(j).setAlive(alive);
	}

	public Id RegionId() {
		return Region.RegionId(this.x, this.y);
	}

	public static Id RegionId(long x, long y) {
		String threeZeroes = "000";
		String strX = Long.toHexString(x / 20), strY = Long.toHexString(y / 20);
		if (strX.length() > 3)
			strX = strX.substring(0, 3);
		else
			strX = threeZeroes.substring(0, 3 - strX.length()) + strX;
		if (strY.length() > 3)
			strY = strY.substring(0, 3);
		else
			strY = threeZeroes.substring(0, 3 - strY.length()) + strY;

		return rice.pastry.Id.build(strX + strY);
	}
}
