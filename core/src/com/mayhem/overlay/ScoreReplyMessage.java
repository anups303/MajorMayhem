package com.mayhem.overlay;

import java.util.HashMap;

import rice.p2p.commonapi.Id;

public class ScoreReplyMessage extends ActionAcknowledgmentMessage {
	private static final long serialVersionUID = 187084047557659274L;
	private HashMap<String, Integer> scores;

	public ScoreReplyMessage(Id sender, Id receiver, long actionMessageId,
			HashMap<String, Integer> scores) {
		super(receiver, actionMessageId, true);
		this.scores = scores;
	}

	public void execute(ClientApplication app) {
		if (this.getActionMessageId() != -1)
			app.raiseActionAcknowledgmentEvent(this.getActionMessageId(),
					this.scores);
	}
}