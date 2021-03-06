package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

//This message is a reply to RegionControllerLookup
//and will send to the sender of RegionControllerLookup to inform it about RC of queried region
public class RegionControllerLookupReply extends Message {

	private static final long serialVersionUID = -5389766882795085505L;
	private Id coordinatorId;
	private long lookupMessageId;

	public RegionControllerLookupReply(Id sender, Id receiver,
			Id coordinatorId, long lookupMessageId) {
		super(sender, receiver);
		this.coordinatorId = coordinatorId;
		this.lookupMessageId = lookupMessageId;
	}

	@Override
	public void execute(ClientApplication app) {
		app.regionControllerLookupReplyReceived(this.lookupMessageId,
				coordinatorId);
	}
}
