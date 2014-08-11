package com.mayhem.overlay;

import java.util.Iterator;

import rice.p2p.commonapi.Id;

public class RegionControllerChangedMessage extends Message {

	private static final long serialVersionUID = 7114773048475928706L;
	private Id regionId, coordinatorId;

	public RegionControllerChangedMessage(Id sender, Id receiver, Id regionId,
			Id coordinatorId) {
		super(sender, receiver);
		this.regionId = regionId;
		this.coordinatorId = coordinatorId;
	}

	@Override
	public void execute(ClientApplication app) {
		System.out.println("Save RC:" + coordinatorId + ", regionId:"
				+ regionId);
		if (app.coordinatorList.containsValue(coordinatorId)) {
			Iterator<Id> itr = app.coordinatorList.keySet().iterator();
			Id region = null;
			while (itr.hasNext()) {
				Id tmp = itr.next();
				if (coordinatorId == app.coordinatorList.get(tmp))
					region = tmp;
			}
			if (region != null)
				app.coordinatorList.remove(region);
		}

		app.coordinatorList.put(regionId, coordinatorId);
	}
}
