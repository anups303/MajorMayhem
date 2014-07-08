package com.mayhem.overlay;

import java.util.List;
import rice.p2p.commonapi.Id;

public class RegionStateChannelContent extends ChannelContent {
	private static final long serialVersionUID = 2522899394681916995L;

	private Region region;
	private Id coordinator;

	public RegionStateChannelContent(Region region, Id coordinator) {
		this.region = region;
		this.coordinator = coordinator;
	}

	public Region getRegion() {
		return this.region;
	}

	public Id getCoordinator() {
		return this.coordinator;
	}

	public String toString() {
		String result = "";

		// if (playerList != null)
		// for (PlayerState player : this.playerList) {
		// result += player.getId() + "(" + player.getX() + ","
		// + player.getY() + "), ";
		// }

		return result;
	}
}
