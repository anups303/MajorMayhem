package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class BecomeRegionControllerMessage extends Message {
	private static final long serialVersionUID = -5687474576945358060L;

	private Id rightCoordinator, leftCoordinator, topCoordinator,
			bottomCoordinator;

	public BecomeRegionControllerMessage(Id rightCoordinator,
			Id leftCoordinator, Id topCoordinator, Id bottomCoordinator) {
		this.rightCoordinator = rightCoordinator;
		this.leftCoordinator = leftCoordinator;
		this.topCoordinator = topCoordinator;
		this.bottomCoordinator = bottomCoordinator;
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
