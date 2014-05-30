package com.mayhem.overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import rice.environment.Environment;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.Id;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.IPNodeIdFactory;

public class NodeLauncher {
	protected PastryNode node;
	protected ClientApplication app;

	protected NodeLauncher(){
		
	}
	
	public NodeLauncher(int bindport, InetSocketAddress bootaddress,
			Environment env, boolean isNewGame) throws Exception {

		NodeIdFactory nidFactory = new IPNodeIdFactory(
				InetAddress.getLocalHost(), bindport, env);

		PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory,
				bindport, env);

		node = factory.newNode();
		app = new ClientApplication(node, isNewGame);
		node.boot(bootaddress);
		synchronized (node) {
			while (!node.isReady() && !node.joinFailed()) {
				// delay so we don't busy-wait
				node.wait(500);

				// abort if can't join
				if (node.joinFailed()) {
					throw new IOException(
							"Could not join the FreePastry ring.  Reason:"
									+ node.joinFailedReason());
				}
			}
		}

		System.out.println("Finished creating new node " + node);

		app.subscribe();

	}
}
