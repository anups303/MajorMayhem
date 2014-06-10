package com.mayhem.overlay;

import java.util.List;

public class RegionStateChannelContent extends ChannelContent {
	private static final long serialVersionUID = 2522899394681916995L;

	private List<PlayerState> playerList;
	private List<BombState> bombList;

	public RegionStateChannelContent(List<PlayerState> playerList,
			List<BombState> bombList) {
		this.playerList = playerList;
		this.bombList = bombList;
	}

	public List<PlayerState> getPlayerList() {
		return this.playerList;
	}

	public List<BombState> getBombList() {
		return this.bombList;
	}

	public String toString() {
		String result = "";

		if (playerList != null)
			for (PlayerState player : this.playerList) {
				result += player.getId() + "(" + player.getX() + ","
						+ player.getY() + "), ";
			}

		return result;
	}
}
