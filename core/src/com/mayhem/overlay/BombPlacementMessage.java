package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class BombPlacementMessage extends Message {
	private static final long serialVersionUID = 4266857723473558476L;
	private Id sender;
	// private long messageId;
	private int x, y;

	public BombPlacementMessage(Id sender, Id receiver, int x, int y) {
		super(receiver);
		this.sender = sender;
		this.x = x;
		this.y = y;
	}

	public void execute(ClientApplication app) {
		// TODO: validating the bomb
		// in case of valid bomb placement, coordinator must acknowledge it.

		app.routeMessage(
				this.getSender(),
				new ActionAcknowledgmentMessage(this.getSender(), this
						.getMessageId(), true));

		// Then Coordinator has to propagate new game state on the channel
		app.addBomb(new BombState(this.getSender(), this.getX(), this.getY()));
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

	// public long getMessageId() {
	// return messageId;
	// }
}
