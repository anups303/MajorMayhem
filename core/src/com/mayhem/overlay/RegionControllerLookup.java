package com.mayhem.overlay;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.leafset.LeafSet;

//This message is used when a movement to a new region required and
//it is necessary to make sure that there is no RC for this region
//because there are some situation that direct links to neighbors (left, right, top, bottom)
//are not sufficient. 
//so a message will send toward the regionID and the node which is responsible for that address 
//will return the RC if there is such a node.

//There is another possible that the node which was in charge of that address, is not anymore
//because a node with nearer address join the network and it will receive the message 
//and since it was node in charge before, it would know about the RC
//to avoid it in case that current node does not know about the region
//the message will be forward to the leaf set
//messageId and TLL are used to avoid loops.
public class RegionControllerLookup extends Message {

	private static final long serialVersionUID = 5650569319571535086L;
	private Id regionId;

	public RegionControllerLookup(Id sender, Id receiver, Id regionId) {
		super(sender, receiver);
		this.regionId = regionId;
	}

	@Override
	public void execute(ClientApplication app) {
		System.out.println("Lookup for Region ID:" + regionId);
		if (app.coordinatorList.containsKey(regionId)) {
			app.routeMessage(
					this.getSender(),
					new RegionControllerLookupReply(app.getLocalNodeId(), this
							.getSender(), app.coordinatorList.get(regionId),
							this.getMessageId()));
		} else {
			LeafSet leafSet = app.node.getLeafSet();
			for (int i = -leafSet.ccwSize(); i <= leafSet.cwSize(); i++) {
				if (i != 0) { // don't send to self
					NodeHandle nh = leafSet.get(i);
					app.routeMessageDirect(nh, this);
				}
			}
		}
	}
}
