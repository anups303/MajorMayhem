package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class ActionAcknowledgmentMessage extends Message {
	private static final long serialVersionUID = -5801612284171523108L;

	long actionMessageId;
	boolean valid;

	public ActionAcknowledgmentMessage(Id receiver, long actionMessageId,
			boolean valid) {
		super(null, receiver);
		this.actionMessageId = actionMessageId;
		this.valid = valid;
	}

	@Override
	public void execute(ClientApplication app) {
		if (this.getValid()) {
			// System.out.println("action " + msg.getActionMessageId()
			// + " is valid");
		}
		app.raiseActionAcknowledgmentEvent(this.getActionMessageId(), null);
	}

	public long getActionMessageId() {
		return this.actionMessageId;
	}

	public boolean getValid() {
		return this.valid;
	}

}
