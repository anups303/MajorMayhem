package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class ChangeRegionMessage extends Message {
	private static final long serialVersionUID = 5921572407345035176L;
	private Id sender;
	private long playerX, playerY;

	public ChangeRegionMessage(Id sender, Id receiver, long playerX,
			long playerY) {
		 super(receiver);
		this.sender = sender;
		this.playerX = playerX;
		this.playerY = playerY;
	}

	@Override
	public void execute(ClientApplication app) {
		if (app.isCoordinator) {
			if (app.leftCoordinator == this.sender)
				app.leftCoordinator = null;
			if (app.rightCoordinator == this.sender)
				app.rightCoordinator = null;
			if (app.topCoordinator == this.sender)
				app.topCoordinator = null;
			if (app.bottomCoordinator == this.sender)
				app.bottomCoordinator = null;

			app.region.addPlayer(new PlayerState(this.sender, this.playerX,
					this.playerY));
			Region r = app.getRegion();
			app.routeMessage(
					this.getSender(),
					new JoinReplyMessage(this.getSender(), -1, true, app
							.getChannelName(), app.getLocalNodeId(), r, r.x,
							r.y));
			app.publishRegionState();
		}
	}

	public Id getSender() {
		return this.sender;
	}
}
