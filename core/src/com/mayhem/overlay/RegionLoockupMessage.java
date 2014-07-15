package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class RegionLoockupMessage extends Message {
	private static final long serialVersionUID = -4211559943073625414L;
	private long x;
	private long y;

	public RegionLoockupMessage(Id sender, Id receiver, long x, long y) {
		super(sender, receiver);
		this.x = x;
		this.y = y;
	}

	@Override
	public void execute(ClientApplication app) {
		if (app.leftCoordinator != null)
			app.routeMessage(this.getSender(), new ActionAcknowledgmentMessage(
					this.getSender(), this.getMessageId(), true));

	}

}
