package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class BecomeRegionControllerMessage extends Message {
	private static final long serialVersionUID = -5687474576945358060L;

	private Id rightCoordinator, leftCoordinator, topCoordinator,
			bottomCoordinator;
	private long playerX, playerY, regionX, regionY;
	private Region region;

	public BecomeRegionControllerMessage(Id receiver, Id leftCoordinator,
			Id rightCoordinator, Id topCoordinator, Id bottomCoordinator,
			long x, long y, long regionX, long regionY) {
		this(receiver, leftCoordinator, rightCoordinator, topCoordinator,
				bottomCoordinator, x, y, regionX, regionY, null);
	}

	public BecomeRegionControllerMessage(Id receiver, Id leftCoordinator,
			Id rightCoordinator, Id topCoordinator, Id bottomCoordinator,
			long x, long y, long regionX, long regionY, Region region) {
		super(receiver);
		this.rightCoordinator = rightCoordinator;
		this.leftCoordinator = leftCoordinator;
		this.topCoordinator = topCoordinator;
		this.bottomCoordinator = bottomCoordinator;
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

		if (this.region == null) {
			app.region = new Region(-1);
			app.region.setPosition(regionX, regionY);
			app.region.addPlayer(new PlayerState(app.getLocalNodeId(), playerX,
					playerY));
		} else {
			app.region = this.region;
			Region r = app.region;
			for (PlayerState player : app.region.getPlayers())
				if (player.getId() != app.getLocalNodeId())
					app.routMessage(
							player.getId(),
							new JoinReplyMessage(player.getId(), this
									.getMessageId(), true,
									app.getChannelName(), app.getLocalNodeId(),
									r, r.x, r.y));

		}

		app.leftCoordinator = this.getLeftCoordinator();
		app.rightCoordinator = this.getRightCoordinator();
		app.topCoordinator = this.getTopCoordinator();
		app.bottomCoordinator = this.getBottomCoordinator();

		System.out.println("BecomeRegionController:" + app.getLocalNodeId()
				+ "[" + app.leftCoordinator + "," + app.rightCoordinator + ","
				+ app.topCoordinator + "," + app.bottomCoordinator + "]");

		app.publishRegionState();

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
