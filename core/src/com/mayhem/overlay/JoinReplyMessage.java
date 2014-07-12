package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class JoinReplyMessage extends ActionAcknowledgmentMessage {
	private static final long serialVersionUID = 2582711063525648800L;
	String channelName;
	Id coordinatorId;
	private Region region;
	private long playerX, playerY;

	public JoinReplyMessage(Id receiver, long actionMessageId, boolean valid,
			String channelName, Id coordinatorId, Region region, long playerX,
			long playerY) {
		super(receiver, actionMessageId, valid);

		this.channelName = channelName;
		this.coordinatorId = coordinatorId;
		this.region = region;
		this.playerX = playerX;
		this.playerY = playerY;
	}

	@Override
	public void execute(ClientApplication app) {
		app.region = this.region;

		app.setRegionController(this.getCoordinatorId());

		System.out.println("JoinReply:" + this.getChannelName());
		app.subscribe(this.getChannelName());

		if (this.getActionMessageId() != -1)
			app.raiseActionAcknowledgmentEvent(this.getActionMessageId(),
					region);
		app.raiseRegionStateEvent(true);
	}

	public String getChannelName() {
		return channelName;
	}

	public Id getCoordinatorId() {
		return coordinatorId;
	}

	public Region getRegion() {
		return this.region;
	}

	public long getPlayerX() {
		return this.playerX;
	}

	public long getPlayerY() {
		return this.playerY;
	}

}
