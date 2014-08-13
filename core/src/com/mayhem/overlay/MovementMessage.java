package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class MovementMessage extends Message implements IAcknowledgeable {
	private static final long serialVersionUID = 6561350713073687226L;
	private int x, y;

	public MovementMessage(Id sender, Id receiver, int x, int y) {
		super(sender, receiver);
		this.x = x;
		this.y = y;
	}

	@Override
	public void execute(ClientApplication app) {
		app.routeMessage(
				this.getSender(),
				new ActionAcknowledgmentMessage(this.getSender(), this
						.getMessageId(), true));
		boolean find = false;
		for (PlayerState player : app.region.getPlayers()) {
			// If the sender is a member of my region
			if (player.getId() == this.getSender()) {
				find = true;
				player.setAlive(true);
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

						if (coordinator == null) {
							coordinator = app.FindRegionController(x, y);
						}
						app.leftCoordinator = doTheJob(app, coordinator, x, y,
								null, app.node.getId(), null, null,
								player.getScore());
					}

					if (rightRegion) {
						Id coordinator = app.rightCoordinator;
						long x = app.region.x + (20 + 1), y = app.region.y;

						if (coordinator == null) {
							coordinator = app.FindRegionController(x, y);
						}
						app.rightCoordinator = doTheJob(app, coordinator, x, y,
								app.node.getId(), null, null, null,
								player.getScore());
					}

					if (topRegion) {
						Id coordinator = app.topCoordinator;
						long x = app.region.x, y = app.region.y + (20 + 1);

						if (coordinator == null) {
							coordinator = app.FindRegionController(x, y);
						}

						app.topCoordinator = doTheJob(app, coordinator, x, y,
								null, null, null, app.node.getId(),
								player.getScore());
					}
					if (bottomRegion) {
						Id coordinator = app.bottomCoordinator;
						long x = app.region.x, y = app.region.y - (20 + 1);

						if (coordinator == null) {
							coordinator = app.FindRegionController(x, y);
						}

						app.bottomCoordinator = doTheJob(app, coordinator, x,
								y, null, null, app.node.getId(), null,
								player.getScore());
					}

				} else {
					// Stays in the same region
					player.setX(this.getX());
					player.setY(this.getY());
					break;
				}
			}
		}
		if (find)
			// Then Coordinator has to propagate new game state on the
			// channel
			app.publishRegionState();
		// app.routeMessage(
		// this.getSender(),
		// new ActionAcknowledgmentMessage(this.getSender(), this
		// .getMessageId(), find));
	}

	protected Id doTheJob(ClientApplication app, Id coordinator, long x,
			long y, Id leftCoordinator, Id rightCoordinator, Id topCoordinator,
			Id bottomCoordinator, int ps) {
		Id result = coordinator;

		// There is no known RC for the new region
		if (coordinator == null) {

			// If I'm the region controller and I'm moving to
			// another region I have to let another node in the
			// region to be coordinator
			if (app.node.getId() == this.getSender()) {
				// I'm the only one in the region and I'm moving
				// to an empty region
				if (app.region.players.size() == 0) {
					if (app.leftCoordinator != null)
						app.routeMessage(app.leftCoordinator,
								new NeighborCoordinatorChangedMessage(
										app.leftCoordinator, null, 1));
					if (app.rightCoordinator != null)
						app.routeMessage(app.rightCoordinator,
								new NeighborCoordinatorChangedMessage(
										app.rightCoordinator, null, 0));
					if (app.topCoordinator != null)
						app.routeMessage(app.topCoordinator,
								new NeighborCoordinatorChangedMessage(
										app.topCoordinator, null, 3));

					if (app.bottomCoordinator != null)
						app.routeMessage(app.bottomCoordinator,
								new NeighborCoordinatorChangedMessage(
										app.bottomCoordinator, null, 2));

					// I will become RC of the empty region
					app.routeMessage(this.getSender(),
							new BecomeRegionControllerMessage(this.getSender(),
									null, null, null, null, this.x, this.y, x,
									y, ps));
				}
				// There are some other guys in region
				// I choose the first one as the new region controller
				// and I set myself as a the region controller of the new region
				// that I'm moving in
				else {
					Id newCoordinator = app.region.players.get(0).getId();

					app.publishRegionState(newCoordinator);

					// Id l = null, r = null, t = null, b = null;
					if (leftCoordinator != null) {
						app.routeMessage(newCoordinator,
								new BecomeRegionControllerMessage(
										newCoordinator, null, leftCoordinator,
										null, null, 0, 0, 0, 0, app.region, ps));

						app.routeMessage(
								this.getSender(),
								new BecomeRegionControllerMessage(this
										.getSender(), newCoordinator, null,
										null, null, this.x, this.y, x, y, ps));
					} else if (rightCoordinator != null) {
						app.routeMessage(newCoordinator,
								new BecomeRegionControllerMessage(
										newCoordinator, rightCoordinator, null,
										null, null, 0, 0, 0, 0, app.region, ps));

						app.routeMessage(
								this.getSender(),
								new BecomeRegionControllerMessage(this
										.getSender(), null, newCoordinator,
										null, null, this.x, this.y, x, y, ps));
					} else if (bottomCoordinator != null) {
						app.routeMessage(newCoordinator,
								new BecomeRegionControllerMessage(
										newCoordinator, null, null,
										bottomCoordinator, null, 0, 0, 0, 0,
										app.region, ps));

						app.routeMessage(
								this.getSender(),
								new BecomeRegionControllerMessage(this
										.getSender(), null, null, null,
										newCoordinator, this.x, this.y, x, y,
										ps));
					} else if (topCoordinator != null) {
						app.routeMessage(newCoordinator,
								new BecomeRegionControllerMessage(
										newCoordinator, null, null, null,
										topCoordinator, 0, 0, 0, 0, app.region,
										ps));

						app.routeMessage(
								this.getSender(),
								new BecomeRegionControllerMessage(this
										.getSender(), null, null,
										newCoordinator, null, this.x, this.y,
										x, y, ps));
					}
				}
			}
			// If I'm not region controller and I'm moving to a new region,
			// I should become the region controller of the new region
			else {
				result = this.getSender();
				app.routeMessage(this.getSender(),
						new BecomeRegionControllerMessage(this.getSender(),
								leftCoordinator, rightCoordinator,
								topCoordinator, bottomCoordinator, this.x,
								this.y, x, y, ps));
			}
		} else {

			// If I'm the region controller
			if (app.node.getId() == this.getSender()) {
				app.isCoordinator = false;

				// I should let the next node in the region to become
				// regionController
				if (app.region.players.size() > 0) {
					Id newCoordinator = app.region.players.get(0).getId();
					app.publishRegionState(newCoordinator);

					app.routeMessage(newCoordinator,
							new BecomeRegionControllerMessage(newCoordinator,
									app.leftCoordinator, app.rightCoordinator,
									app.topCoordinator, app.bottomCoordinator,
									0, 0, 0, 0, app.region, ps));
				} else {
					// TODO: Neighbors should be inform about this movement
					Id regionId = app.region.RegionId();
					app.routeMessage(regionId,
							new RegionControllerChangedMessage(null, null,
									regionId, null));
				}
			}
			app.routeMessage(coordinator,
					new ChangeRegionMessage(this.getSender(), coordinator,
							this.x, this.y, ps));
		}
		return result;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

}
