package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

//This message will be send to the RC and indicate the placement of a bomb by a player
public class BombPlacementMessage extends Message {
	private static final long serialVersionUID = 4266857723473558476L;
	private int x, y;

	public BombPlacementMessage(Id sender, Id receiver, int x, int y) {
		super(sender, receiver);
		this.x = x;
		this.y = y;
	}

	public void execute(ClientApplication app) {
		// TODO: validating the bomb
		// in case of valid bomb placement, coordinator must acknowledge it.
		
		//To make it faster for the player who wanted to place the bomb,
		//send the message directly to it
		app.routeMessage(
				this.getSender(),
				new ActionAcknowledgmentMessage(this.getSender(), this
						.getMessageId(), true));

		// and also propagate new game state on the channel
		//which is contain the new bomb
		app.addBomb(new BombState(this.getSender(), this.getX(), this.getY()));
		app.publishRegionState();
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}
}
