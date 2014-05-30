package com.mayhem.overlay.test;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Node;

import com.mayhem.overlay.ClientApplication;


public class TestClientApplication extends ClientApplication {

	public TestClientApplication(Node node, boolean isNewGame) {
		super(node, isNewGame);
	}
	
	public void SendTestMessage(Id id, String msg) {
		routMessage(id, new TestMessage(msg));
	}
}
