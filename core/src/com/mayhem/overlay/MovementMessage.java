package com.mayhem.overlay;

import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Setters.Bones;

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
				boolean leftRegion = (player.getX() / 20) == (this.getX() / 20) + 1;
				boolean rightRegion = (player.getX() / 20) + 1 == (this.getX() / 20);
				boolean topRegion = (player.getY() / 20) + 1 == (this.getY() / 20);
				boolean bottomRegion = (player.getY() / 20) == (this.getY() / 20) + 1;

				if ((leftRegion || rightRegion || topRegion || bottomRegion)
						&& (player.getX() != -1 && player.getY() != -1)) {

					// User's about to move to another region!
					app.region.removePlayerById(this.getSender());

					if (leftRegion) {
						Id coordinator = app.leftCoordinator;
						long x = app.region.x - (20 + 1), y = app.region.y;
						app.leftCoordinator = doTheJob(app, coordinator, x, y,
								null, app.node.getId(), null, null);
					}

					if (rightRegion) {
						Id coordinator = app.rightCoordinator;
						long x = app.region.x + (20 + 1), y = app.region.y;
						app.rightCoordinator = doTheJob(app, coordinator, x, y,
								app.node.getId(), null, null, null);
					}

					if (topRegion) {
						Id coordinator = app.topCoordinator;
						long x = app.region.x, y = app.region.y + (20 + 1);
						app.topCoordinator = doTheJob(app, coordinator, x, y,
								null, null, null, app.node.getId());
					}
					if (bottomRegion) {
						Id coordinator = app.bottomCoordinator;
						long x = app.region.x, y = app.region.y - (20 + 1);
						app.bottomCoordinator = doTheJob(app, coordinator, x,
								y, null, null, app.node.getId(), null);
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

	protected Id doTheJob(ClientApplication app, Id coordinator, long x,
			long y, Id leftCoordinator, Id rightCoordinator, Id topCoordinator,
			Id bottomCoordinator) {
		Id result = coordinator;

		if (coordinator == null) {

			// If I'm the region controller and I'm moving to
			// another region I have to let another node in the
			// region to be coordinator
			if (app.node.getId() == this.getSender()) {
				// I'm the only one in the region and I'm moving
				// to an empty region
				if (app.region.players.size() == 0) {
					app.routMessage(this.getSender(),
							new BecomeRegionControllerMessage(null, null, null,
									null, this.x, this.y, x, y));
				} else {
					Id newCoordinator = app.region.players.get(0).getId();
					
					app.publishRegionState(newCoordinator);
					
					Id l = null, r = null, t = null, b = null;
					if (leftCoordinator != null) {
						app.routMessage(newCoordinator,
								new BecomeRegionControllerMessage(null,
										leftCoordinator, null, null, 0, 0, 0,
										0, app.region));

						app.routMessage(this.getSender(),
								new BecomeRegionControllerMessage(
										newCoordinator, null, null, null,
										this.x, this.y, x, y));
					} else if (rightCoordinator != null) {
						app.routMessage(newCoordinator,
								new BecomeRegionControllerMessage(rightCoordinator,
										null, null, null, 0, 0, 0,
										0, app.region));

						app.routMessage(this.getSender(),
								new BecomeRegionControllerMessage(
										null, newCoordinator, null, null,
										this.x, this.y, x, y));
					} else if (bottomCoordinator != null) {
						app.routMessage(newCoordinator,
								new BecomeRegionControllerMessage(null, null,
										bottomCoordinator, null, 0, 0, 0, 0,
										app.region));

						app.routMessage(this.getSender(),
								new BecomeRegionControllerMessage(null, null,
										null, newCoordinator, this.x, this.y,
										x, y));
					}else if (topCoordinator != null) {
						app.routMessage(newCoordinator,
								new BecomeRegionControllerMessage(null, null,
										null, topCoordinator, 0, 0, 0, 0,
										app.region));

						app.routMessage(this.getSender(),
								new BecomeRegionControllerMessage(null, null,
										newCoordinator, null, this.x, this.y,
										x, y));
					}
				}
			} else {
				result = this.getSender();
				app.routMessage(this.getSender(),
						new BecomeRegionControllerMessage(leftCoordinator,
								rightCoordinator, topCoordinator,
								bottomCoordinator, this.x, this.y, x, y));
			}
		} else {
			// I'm the only one in the region and I'm leaving
			// so I shouldn't be coordinator anymore
			if (app.region.players.size() == 0) {
				app.isCoordinator = false;
			}
			app.routMessage(coordinator,
					new ChangeRegionMessage(this.getSender(), this.x, this.y));
		}
		return result;
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
