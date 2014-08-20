package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class Message implements rice.p2p.commonapi.Message {
	private static final long serialVersionUID = -537925666393304992L;
	private long messageId;
	private long sentTime;
	private Id receiver, sender;
	private int ttl = 3;

	public Message(Id sender, Id receiver) {
		messageId = new java.util.Random().nextLong();
		sentTime = System.currentTimeMillis();
		this.receiver = receiver;
		this.sender = sender;
	}

	public int getPriority() {
		return Message.LOW_PRIORITY;
	}

	public void execute(ClientApplication app) {

	}

	public long getMessageId() {
		return messageId;
	}

	public Id getSender() {
		return this.sender;
	}

	public Id getReceiver() {
		return this.receiver;
	}

	public long getSentTime() {
		return this.sentTime;
	}

	public void decreaseTTL() {
		ttl--;
	}

	public int getTTL() {
		return ttl;
	}

	public void setTTL(int value) {
		this.ttl = value;
	}

	@Override
	public String toString() {
		return this.getClass().toString() + "-Receiver:" + this.receiver;
	}
}
