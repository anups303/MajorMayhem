package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

//This message will send to the RC when the user want to close the game window
//(graceful leave)
public class LeaveMessage extends Message {
	private static final long serialVersionUID = -2042122046463742754L;

	private Id killedByPlayer;

	public LeaveMessage(Id sender, Id receiver, Id killedByPlayer) {
		super(sender, receiver);
		this.killedByPlayer = killedByPlayer;
	}

	@Override
	public void execute(ClientApplication app) {
		try {
			if (this.killedByPlayer != null)
				app.getRegion().increaseScore(killedByPlayer);
			app.getRegion().removePlayerById(this.getSender());
			System.out.println("Leave:" + this.getSender());
			app.publishRegionState();
		} catch (Exception ex) {
			//TODO: log here!
		}
	}

	public Id getKilledByPlayer() {
		return this.killedByPlayer;
	}

	public String toString() {
		return "Leave Message: -sender:" + this.getSender();
	}
}
