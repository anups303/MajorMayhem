package com.mayhem.overlay;

public class Message implements rice.p2p.commonapi.Message {
	private static final long serialVersionUID = -537925666393304992L;

	public int getPriority() {
		return Message.LOW_PRIORITY;
	}

	public void execute(ClientApplication app) {

	}
}
