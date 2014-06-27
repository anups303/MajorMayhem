package com.mayhem.overlay;

import rice.p2p.scribe.ScribeContent;
import rice.p2p.commonapi.Id;

public class ChannelContent implements ScribeContent {
	private static final long serialVersionUID = 6100170606923714050L;

	private Id sender;

	public ChannelContent(Id sender) {
		this.sender = sender;
	}

	public Id getSender() {
		return sender;
	}

}
