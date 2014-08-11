package com.mayhem.overlay.test;

import rice.p2p.commonapi.Id;
import rice.pastry.NodeHandle;
import rice.pastry.PastryNode;

import com.mayhem.overlay.ClientApplication;

public class TestClientApplication extends ClientApplication {

	public TestClientApplication(PastryNode node, boolean isNewGame) {
		super(node, isNewGame);
	}

	public void SendTestMessage(Id id, String msg) {
		routeMessage(id, new TestMessage(msg));
	}

	public void SendTestMessage(NodeHandle hn, String msg) {
		routeMessageDirect(hn, new TestMessage(msg));
	}
}
