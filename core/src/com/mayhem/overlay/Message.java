package com.mayhem.overlay;


public class Message implements rice.p2p.commonapi.Message{
	
	public int getPriority() {
		return Message.LOW_PRIORITY;
	}
}
