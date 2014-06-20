package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

public class LeaveMessage extends Message {
	private static final long serialVersionUID = -2042122046463742754L;

	Id sender;

	public LeaveMessage(Id sender) {
		this.sender = sender;
	}

	@Override
	public void execute(ClientApplication app) {
		try {
			// this.regionMembers.remove(msg.getSender());
			for (int i = 0; i < app.getRegion().getPlayers().size(); i++)
				if (app.getRegion().getPlayers().get(i).getId() == this
						.getSender()) {
					app.getRegion().getPlayers().remove(i);
					break;
				}
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
