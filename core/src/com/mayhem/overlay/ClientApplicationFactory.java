package com.mayhem.overlay;

import rice.pastry.PastryNode;

public class ClientApplicationFactory {

	public ClientApplicationFactory(){
		
	}

	public ClientApplication getClientApplication(PastryNode node, boolean isNewGame) {
		return new ClientApplication(node, isNewGame);
	}
}
