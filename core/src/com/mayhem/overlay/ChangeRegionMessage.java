package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class ChangeRegionMessage extends Message {
	private static final long serialVersionUID = 5921572407345035176L;
	private Id sender;
	private long playerX, playerY;

	public ChangeRegionMessage(Id sender, long playerX, long playerY) {
		this.sender = sender;
		this.playerX = playerX;
		this.playerY = playerY;
	}

	@Override
	public void execute(ClientApplication app) {

		app.region.addPlayer(new PlayerState(this.sender, this.playerX,
				this.playerY));
		Region r = app.getRegion();
		app.routMessage(this.getSender(),
				new JoinReplyMessage(app.getChannelName(),
						app.getLocalNodeId(), r, r.x, r.y));
		app.publishRegionState();
	}

	public Id getSender() {
		return this.sender;
	}
}
