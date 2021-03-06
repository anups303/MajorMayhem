package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

//When a RC being changed, this message will send to the direct neighbors (left, right, top, bottom)
//to let them change their corresponding neighbor
public class NeighborCoordinatorChangedMessage extends Message {
	private static final long serialVersionUID = -5570937915455995938L;
	private Id newCoordinator;
	private int location;

	public NeighborCoordinatorChangedMessage(Id receiver, Id newCoordinator,
			int location) {
		super(null, receiver);
		this.newCoordinator = newCoordinator;
		this.location = location;
	}

	@Override
	public void execute(ClientApplication app) {
		switch (location) {
		case 0:
			app.leftCoordinator = newCoordinator;
			break;
		case 1:
			app.rightCoordinator = newCoordinator;
			break;
		case 2:
			app.topCoordinator = newCoordinator;
			break;
		case 3:
			app.bottomCoordinator = newCoordinator;
			break;

		default:
			break;
		}

	}
}
