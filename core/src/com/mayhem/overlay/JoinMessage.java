package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class JoinMessage extends Message {
	private static final long serialVersionUID = -3322417816542869716L;
	Id sender;

	public JoinMessage(Id sender) {
		this.sender = sender;
	}

	@Override
	public void execute(ClientApplication app) {
		if (app.getIsCoordinator()) {
			// this.regionMembers.add(msg.getSender());
			Region r = app.getRegion();
			app.getRegion().addPlayer(
					new PlayerState(this.getSender(), r.x + 1, r.y + 1));
			System.out.println("Join:" + this.getSender());

			app.routMessage(
					this.getSender(),
					new JoinReplyMessage(this.getMessageId(), true, app
							.getChannelName(), app.getLocalNodeId(), r, r.x,
							r.y));
		}
		// Otherwise I will forward the message to my coordinator, he may
		// help him
		else {
			app.routMessage(app.getRegionController(), this);
		}
	}

	public Id getSender() {
		return sender;
	}

	public String toString() {
		return "Join Message: -sender:" + sender;
	}
}
