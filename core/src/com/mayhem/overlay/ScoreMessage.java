package com.mayhem.overlay;

import java.util.HashMap;
import java.util.Iterator;

import rice.p2p.commonapi.Id;

//This message will be send to gather score of neighbor regions
public class ScoreMessage extends Message {
	private static final long serialVersionUID = -2267061994769492176L;

	public ScoreMessage(Id sender, Id receiver) {
		super(sender, receiver);
	}

	public void execute(ClientApplication app) {
		if (app.isCoordinator) {
			app.routeMessage(
					this.getSender(),
					new ScoreReplyMessage(this.getReceiver(), this.getSender(),
							this.getMessageId(), app.getPlayersScore(this
									.getTTL())));
		}
	}
}
