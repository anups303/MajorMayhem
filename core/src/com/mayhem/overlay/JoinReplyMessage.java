package com.mayhem.overlay;

public class JoinReplyMessage  extends Message {
	private static final long serialVersionUID = 2582711063525648800L;
	String channelName;
	
	public JoinReplyMessage(String channelName){
		this.channelName = channelName;
	}
	
	public String getChannelName(){
		return channelName;
	}
	
	
}
