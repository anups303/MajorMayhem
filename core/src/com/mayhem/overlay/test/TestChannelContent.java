package com.mayhem.overlay.test;

import com.mayhem.overlay.ChannelContent;
import rice.p2p.commonapi.NodeHandle;

public class TestChannelContent extends ChannelContent {
	String message;
	NodeHandle from;

	public TestChannelContent(NodeHandle from, String msg) {
		super(null);
		this.from = from;
		this.message = msg;
	}

	public String toString() {
		return "Content:" + message + " ,from:" + from;
	}
}
