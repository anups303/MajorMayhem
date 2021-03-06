package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

//Receiver will be the region controller of specified region
public class BecomeRegionControllerMessage extends Message {
	private static final long serialVersionUID = -5687474576945358060L;

	private Id rightCoordinator, leftCoordinator, topCoordinator,
			bottomCoordinator;
	private long playerX, playerY, regionX, regionY;
	private int score;
	private Region region;

	public BecomeRegionControllerMessage(Id receiver, Id leftCoordinator,
			Id rightCoordinator, Id topCoordinator, Id bottomCoordinator,
			long x, long y, long regionX, long regionY, int score) {
		this(receiver, leftCoordinator, rightCoordinator, topCoordinator,
				bottomCoordinator, x, y, regionX, regionY, null, score);

	}

	public BecomeRegionControllerMessage(Id receiver, Id leftCoordinator,
			Id rightCoordinator, Id topCoordinator, Id bottomCoordinator,
			long x, long y, long regionX, long regionY, Region region, int score) {
		super(null, receiver);
		this.rightCoordinator = rightCoordinator;
		this.leftCoordinator = leftCoordinator;
		this.topCoordinator = topCoordinator;
		this.bottomCoordinator = bottomCoordinator;
		this.score = score;
		this.playerX = x;
		this.playerY = y;
		this.regionX = regionX;
		this.regionY = regionY;
		this.region = region;
	}

	@Override
	public void execute(ClientApplication app) {
		app.isCoordinator = true;
		app.setRegionController(app.getLocalNodeId());
		app.subscribe(app.getLocalNodeId().toString());

		// If there's no information about the region
		// create a new region
		if (this.region == null) {
			app.region = new Region(-1);
			app.region.setPosition(regionX, regionY);
			app.region.addPlayer(new PlayerState(app.getLocalNodeId(), playerX,
					playerY, score));
		}
		//otherwise, load the previous status of region
		//and let the players of region know that RC has changed 
		//by sending JoinReplyMessage
		else {
			app.region = this.region;
			Region r = app.region;
			for (PlayerState player : app.region.getPlayers())
				if (player.getId() != app.getLocalNodeId())
					app.routeMessage(
							player.getId(),
							new JoinReplyMessage(player.getId(), this
									.getMessageId(), true,
									app.getChannelName(), app.getLocalNodeId(),
									r, r.x, r.y));

		}
		Id regionId = app.region.RegionId();
		
		//Here we send a message to a node which is responsible for this region id
		//to know that this node is the RC of corresponding region
		app.routeMessage(regionId, new RegionControllerChangedMessage(null,
				null, regionId, app.getLocalNodeId()));

		//Also the direct neighbor should be contacted 
		//to know about this change (change their neighbors)
		app.leftCoordinator = this.getLeftCoordinator();
		if (app.leftCoordinator != null)
			app.routeMessage(app.leftCoordinator,
					new NeighborCoordinatorChangedMessage(app.leftCoordinator,
							app.getLocalNodeId(), 1));

		app.rightCoordinator = this.getRightCoordinator();
		if (app.rightCoordinator != null)
			app.routeMessage(app.rightCoordinator,
					new NeighborCoordinatorChangedMessage(app.rightCoordinator,
							app.getLocalNodeId(), 0));

		app.topCoordinator = this.getTopCoordinator();
		if (app.topCoordinator != null)
			app.routeMessage(app.topCoordinator,
					new NeighborCoordinatorChangedMessage(app.topCoordinator,
							app.getLocalNodeId(), 3));

		app.bottomCoordinator = this.getBottomCoordinator();
		if (app.bottomCoordinator != null)
			app.routeMessage(app.bottomCoordinator,
					new NeighborCoordinatorChangedMessage(
							app.bottomCoordinator, app.getLocalNodeId(), 2));

		System.out.println("BecomeRegionController:" + app.getLocalNodeId()
				+ "[" + app.leftCoordinator + "," + app.rightCoordinator + ","
				+ app.topCoordinator + "," + app.bottomCoordinator + "]");

		app.raiseRegionStateEvent();
	}

	public Id getRightCoordinator() {
		return this.rightCoordinator;
	}

	public Id getLeftCoordinator() {
		return this.leftCoordinator;
	}

	public Id getTopCoordinator() {
		return this.topCoordinator;
	}

	public Id getBottomCoordinator() {
		return this.bottomCoordinator;
	}
}
