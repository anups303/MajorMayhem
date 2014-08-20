package com.mayhem.overlay;

import rice.pastry.PastryNode;

//Factory method pattern for client application
public class ClientApplicationFactory {

	public ClientApplicationFactory(){
		
	}

	public ClientApplication getClientApplication(PastryNode node, boolean isNewGame) {
		return new ClientApplication(node, isNewGame);
	}
}
