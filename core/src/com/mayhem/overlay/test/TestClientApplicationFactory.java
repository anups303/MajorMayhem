package com.mayhem.overlay.test;

import rice.pastry.PastryNode;

import com.mayhem.overlay.ClientApplication;
import com.mayhem.overlay.ClientApplicationFactory;

public class TestClientApplicationFactory extends ClientApplicationFactory {
	
	public ClientApplication getClientApplication(PastryNode node, boolean isNewGame) {
		return new TestClientApplication(node, isNewGame);
	}
	
}
