package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class JoinReplyMessage extends Message {
	private static final long serialVersionUID = 2582711063525648800L;
	String channelName;
	Id coordinatorId;
	private Region region;

	public JoinReplyMessage(String channelName, Id coordinatorId, Region region) {
		this.channelName = channelName;
		this.coordinatorId = coordinatorId;
		this.region = region;
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

}
