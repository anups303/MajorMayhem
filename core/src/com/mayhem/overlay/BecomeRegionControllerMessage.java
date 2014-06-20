package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class BecomeRegionControllerMessage extends Message {
	private static final long serialVersionUID = -5687474576945358060L;

	private Id rightCoordinator, leftCoordinator, topCoordinator,
			bottomCoordinator;
	private long playerX, playerY, regionX, regionY;

	public BecomeRegionControllerMessage(Id rightCoordinator,
			Id leftCoordinator, Id topCoordinator, Id bottomCoordinator,
			long x, long y, long regionX, long regionY) {
		this.rightCoordinator = rightCoordinator;
		this.leftCoordinator = leftCoordinator;
		this.topCoordinator = topCoordinator;
		this.bottomCoordinator = bottomCoordinator;
		this.playerX = x;
		this.playerY = y;
		this.regionX = regionX;
		this.regionY = regionY;
	}

	@Override
	public void execute(ClientApplication app) {
		app.isCoordinator = true;
		app.setRegionController(app.getLocalNodeId());

		app.region = new Region();
		app.region.setPosition(regionX, regionY);
		app.region.addPlayer(new PlayerState(app.getLocalNodeId(), playerX,
				playerY));
		app.subscribe(app.getLocalNodeId().toString());

		app.leftCoordinator = this.getLeftCoordinator();
		app.rightCoordinator = this.getRightCoordinator();
		app.topCoordinator = this.getTopCoordinator();
		app.bottomCoordinator = this.getBottomCoordinator();

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
