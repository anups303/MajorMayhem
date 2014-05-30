package com.mayhem.overlay.test;

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
import rice.pastry.socket.SocketNodeHandleFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.IPNodeIdFactory;

import com.mayhem.overlay.ClientApplication;
import com.mayhem.overlay.NodeLauncher;

public class TestNodeLauncher extends NodeLauncher {

	public TestNodeLauncher(int bindport, InetSocketAddress bootaddress,
			Environment env, boolean isNewGame) throws Exception {
		super();
		
		NodeIdFactory nidFactory = new IPNodeIdFactory(
				InetAddress.getLocalHost(), bindport, env);

		PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory,
				bindport, env);

		node = factory.newNode();
		app = new TestClientApplication(node, isNewGame);
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
		
		if (!isNewGame) {
			nidFactory = new IPNodeIdFactory(bootaddress.getAddress(),
					bootaddress.getPort(), env);

			Id coordinatorId = nidFactory.generateNodeId();
			
			
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				while (true) {
					System.out.print("Message to RC:");

					((TestClientApplication)app).SendTestMessage(coordinatorId, br.readLine());
				}
			} catch (Exception ex) {

			}
		}
	}
	
	private static String getIpAsString(InetAddress address) {
		byte[] ipAddress = address.getAddress();
		StringBuffer str = new StringBuffer();
		for(int i=0; i<ipAddress.length; i++) {
			if(i > 0) str.append('.');
			str.append(ipAddress[i] & 0xFF);				
		}
		return str.toString();
	}
}
