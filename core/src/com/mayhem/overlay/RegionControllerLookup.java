package com.mayhem.overlay;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.leafset.LeafSet;

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
			// app.routeMessage(regionId, this);
			// app.routeMessage(
			// this.getSender(),
			// new RegionControllerLookupReply(app.getLocalNodeId(), this
			// .getSender(), null, this.getMessageId()));
			// TODO: Ask neighbors
		}
	}
}
