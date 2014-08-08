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
				app.getRegion().increaseScore(killedByPlayer);
			app.getRegion().setAlive(this.getSender(), false);

			app.publishRegionState();
		} catch (Exception ex) {

		}
	}
}
