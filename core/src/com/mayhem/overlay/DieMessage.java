package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class DieMessage extends Message {
	private Id killedByPlayer;

	private static final long serialVersionUID = 7534046286366853498L;

	public DieMessage(Id sender, Id receiver, Id killedByPlayer) {
		super(sender, receiver);
		this.killedByPlayer = killedByPlayer;
	}

	@Override
	public void execute(ClientApplication app) {
		try {
			if (this.killedByPlayer != null
					&& this.getSender() != this.killedByPlayer)
				if (app.isCoordinator) {
					if (app.getRegion().indexOf(this.killedByPlayer) >= 0) {
						app.getRegion().increaseScore(killedByPlayer);
						app.getRegion().setAlive(this.getSender(), false);
					} else {
						app.routeMessage(this.killedByPlayer, this);
					}
				} else {
					app.routeMessage(app.getRegionController(), this);
				}

			app.publishRegionState();
		} catch (Exception ex) {

		}
	}
}
