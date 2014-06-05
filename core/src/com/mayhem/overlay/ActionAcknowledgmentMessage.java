package com.mayhem.overlay;

public class ActionAcknowledgmentMessage extends Message {
	private static final long serialVersionUID = -5801612284171523108L;

	long actionMessageId;
	boolean valid;

	public ActionAcknowledgmentMessage(long actionMessageId, boolean valid) {
		this.actionMessageId = actionMessageId;
		this.valid = valid;
	}

	public long getActionMessageId() {
		return this.actionMessageId;
	}

	public boolean getValid() {
		return this.valid;
	}

}
