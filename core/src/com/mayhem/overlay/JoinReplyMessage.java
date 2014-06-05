package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class JoinReplyMessage extends Message {
	private static final long serialVersionUID = 2582711063525648800L;
	String channelName;
	Id coordinatorId;

	public JoinReplyMessage(String channelName, Id coordinatorId) {
		this.channelName = channelName;
		this.coordinatorId = coordinatorId;
	}

	public String getChannelName() {
		return channelName;
	}

	public Id getCoordinatorId() {
		return coordinatorId;
	}

}
