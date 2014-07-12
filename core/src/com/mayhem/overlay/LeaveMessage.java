package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class LeaveMessage extends Message {
	private static final long serialVersionUID = -2042122046463742754L;

	Id sender;

	public LeaveMessage(Id sender, Id receiver) {
		super(receiver);
		this.sender = sender;
	}

	@Override
	public void execute(ClientApplication app) {
		try {
			app.getRegion().removePlayerById(this.getSender());
			System.out.println("Leave:" + this.getSender());
			app.raiseRegionStateEvent();

		} catch (Exception ex) {

		}
	}

	public Id getSender() {
		return sender;
	}

	public String toString() {
		return "Leave Message: -sender:" + sender;
	}
}
