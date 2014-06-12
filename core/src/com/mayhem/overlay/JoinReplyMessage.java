package com.mayhem.overlay;

import java.util.List;

import rice.p2p.commonapi.Id;

public class JoinReplyMessage extends Message {
	private static final long serialVersionUID = 2582711063525648800L;
	String channelName;
	Id coordinatorId;
	private List<PlayerState> playerStateList;

	public JoinReplyMessage(String channelName, Id coordinatorId,
			List<PlayerState> playerStateList) {
		this.channelName = channelName;
		this.coordinatorId = coordinatorId;
		this.playerStateList = playerStateList;
	}

	public String getChannelName() {
		return channelName;
	}

	public Id getCoordinatorId() {
		return coordinatorId;
	}

	public List<PlayerState> getPlayerStateList() {
		return playerStateList;
	}

}
