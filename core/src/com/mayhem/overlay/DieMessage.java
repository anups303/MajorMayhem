package com.mayhem.overlay;

import rice.p2p.commonapi.Id;

//This message will be send by the player who has killed to RC
//contain Id of the killer
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
			//If there is killer and I have not killed myself
			//(because no point considered in the case that user kill himself
			//except he has to wait for 5 seconds to get back to the game)
			if (this.killedByPlayer != null
					&& this.getSender() != this.killedByPlayer)
				
				//If I am the RC 
				if (app.isCoordinator) {
					//if the killer is not in this region anymore
					//(since there's a 5 second gap between bomb placement and blast,
					//and considering the fact that killer could still move across the regions
					//we have to make sure that the killer is still in RC list					
					if (app.getRegion().indexOf(this.killedByPlayer) >= 0) {
						app.getRegion().increaseScore(killedByPlayer);
						app.getRegion().setAlive(this.getSender(), false);
					} 
					//Otherwise, this message will be forwarded to the killer
					//(because here it is not clear who is RC of the killer.
					//and then killer will forward the message to its RC
					else {
						app.routeMessage(this.killedByPlayer, this);
					}
				} 
				//this is the case that killer moves to another region 
				//before the bomb explosion
				//so it just need to forward the message to its RC
				//to get the score
				else {
					app.routeMessage(app.getRegionController(), this);
				}

			//Propagate the region state to players
			app.publishRegionState();
		} catch (Exception ex) {
			//TODO: log here!
		}
	}
}
