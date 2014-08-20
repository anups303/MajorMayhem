package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

//When a player moves across the regions
//this message will send to the RC of the region player intended to move into it
//so RC will add the player to its list
//and send JoinReply message to the node
public class ChangeRegionMessage extends Message {
	private static final long serialVersionUID = 5921572407345035176L;
	private long playerX, playerY;
	private int score;

	public ChangeRegionMessage(Id sender, Id receiver, long playerX,
			long playerY, int score) {
		super(sender, receiver);
		this.playerX = playerX;
		this.playerY = playerY;
		this.score = score;
	}

	@Override
	public void execute(ClientApplication app) {
		if (app.isCoordinator) {
			if (app.leftCoordinator == this.getSender())
				app.leftCoordinator = null;
			if (app.rightCoordinator == this.getSender())
				app.rightCoordinator = null;
			if (app.topCoordinator == this.getSender())
				app.topCoordinator = null;
			if (app.bottomCoordinator == this.getSender())
				app.bottomCoordinator = null;

			app.region.addPlayer(new PlayerState(this.getSender(),
					this.playerX, this.playerY, this.score));
			Region r = app.getRegion();
			app.routeMessage(
					this.getSender(),
					new JoinReplyMessage(this.getSender(), -1, true, app
							.getChannelName(), app.getLocalNodeId(), r, r.x,
							r.y));
			app.publishRegionState();
		}
	}
}
