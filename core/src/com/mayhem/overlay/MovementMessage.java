package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class MovementMessage extends Message implements IAcknowledgeable {
	private static final long serialVersionUID = 6561350713073687226L;
	private Id sender;
	private int x, y;

	public MovementMessage(Id sender, int x, int y) {
		this.sender = sender;
		this.x = x;
		this.y = y;
	}

	@Override
	public void execute(ClientApplication app) {
		app.routMessage(this.getSender(),
				new ActionAcknowledgmentMessage(this.getMessageId(), true));

		for (PlayerState player : app.region.getPlayers()) {
			if (player.getId() == this.getSender()) {
				boolean leftRegion = (player.getX() / 10) == (this.getX() / 10) + 1;
				boolean rightRegion = (player.getX() / 10) + 1 == (this.getX() / 10);
				boolean topRegion = (player.getY() / 10) == (this.getY() / 10) + 1;
				boolean bottomRegion = (player.getY() / 10) + 1 == (this.getY() / 10);

				if (leftRegion || rightRegion || topRegion || bottomRegion) {
					// User's about to move to another region!
					app.region.removePlayerById(this.getSender());

					if (leftRegion) {
						if (app.leftCoordinator == null) {
							app.leftCoordinator = this.getSender();
							app.routMessage(
									this.getSender(),
									new BecomeRegionControllerMessage(app.node
											.getId(), null, null, null, this.x,
											this.y, app.region.x - (10 + 1),
											app.region.y));
						} else {
							app.routMessage(
									app.leftCoordinator,
									new ChangeRegionMessage(this.getSender(),
											this.x, this.y));
						}
					}

					if (rightRegion) {
						if (app.rightCoordinator == null) {
							app.rightCoordinator = this.getSender();
							app.routMessage(this.getSender(),
									new BecomeRegionControllerMessage(null,
											app.node.getId(), null, null,
											this.x, this.y, app.region.x
													+ (10 + 1), app.region.y));
						} else {
							app.routMessage(app.rightCoordinator,
									new ChangeRegionMessage(this.getSender(),
											this.x, this.y));
						}
					}

				} else {
					// Stays in the same region
					player.setX(this.getX());
					player.setY(this.getY());
					break;
				}
			}
		}
		// Then Coordinator has to propagate new game state on the channel
		app.publishRegionState();
	}

	public Id getSender() {
		return this.sender;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	
}
