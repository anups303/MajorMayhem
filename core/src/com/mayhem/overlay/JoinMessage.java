package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

//This message will be send right after a node join the overlay
//and it is supposed to signal a node to find a RC to connect to
public class JoinMessage extends Message {
	private static final long serialVersionUID = -3322417816542869716L;

	public JoinMessage(Id sender, Id receiver) {
		super(sender, receiver);
	}

	@Override
	public void execute(ClientApplication app) {
		//If this node is RC, it can handle the request
		if (app.getIsCoordinator()) {
			Region r = app.getRegion();
			
			//Assign a position to the player			
			app.getRegion().addPlayer(
					new PlayerState(this.getSender(), r.x + 1, r.y + 1));
			System.out.println("Join:" + this.getSender());

			//Confirm the join process by sending JoinReply to the requester
			//and inform him about basic information of this region such as
			//channel name and region base position
			app.routeMessage(this.getSender(),
					new JoinReplyMessage(this.getSender(), this.getMessageId(),
							true, app.getChannelName(), app.getLocalNodeId(),
							r, r.x, r.y));
		}
		// Otherwise It will forward the message to its coordinator
		else {
			app.routeMessage(app.getRegionController(), this);
		}
	}

	public String toString() {
		return "Join Message: -sender:" + this.getSender();
	}
}
