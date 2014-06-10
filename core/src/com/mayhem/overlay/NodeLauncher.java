package com.mayhem.overlay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.*;

import rice.p2p.commonapi.Id;
import rice.pastry.leafset.LeafSet;
import rice.environment.Environment;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
//import rice.pastry.Id;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketNodeHandle;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.IPNodeIdFactory;

public class NodeLauncher implements IActionAcknowledgmentListner {
	private final Object lock = new Object();

	protected PastryNode node;
	protected NodeHandle regionController;
	protected ClientApplication app;
	protected List<Long> recievedAcks;

	protected NodeLauncher() {

	}

	public NodeLauncher(int bindport, InetSocketAddress bootaddress,
			Environment env, boolean isNewGame,
			IRegionStateListener regionStateListener) throws Exception {
		this(bindport, bootaddress, env, new ClientApplicationFactory(),
				isNewGame, regionStateListener);
	}

	public NodeLauncher(int bindport, InetSocketAddress bootaddress,
			Environment env, ClientApplicationFactory clientApplicationFactory,
			boolean isNewGame, IRegionStateListener regionStateListener)
			throws Exception {

		NodeIdFactory nidFactory = new IPNodeIdFactory(
				InetAddress.getLocalHost(), bindport, env);

		PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory,
				bindport, env);

		node = factory.newNode();
		app = clientApplicationFactory.getClientApplication(node, isNewGame);
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

		app.addActionAcknowledgmentListener(this);
		if (!isNewGame) {
			// Assume our bootstrapper is also the region controller
			this.regionController = regionControllerFinder(bootaddress);

			// We successfully connected to the overlay and find the
			// Coordinator.
			// So we should talk to him
			app.SendJoinMessage(this.regionController);
		} else {
			app.subscribe(this.node.getId().toString());
			this.regionController = node.getLocalHandle();
		}

		app.addRegionStateListener(regionStateListener);
		recievedAcks = new ArrayList();
	}

	protected NodeHandle regionControllerFinder(
			InetSocketAddress regionControllAddress) {

		LeafSet leafSet = node.getLeafSet();

		try {
			for (int i = -leafSet.ccwSize(); i <= leafSet.cwSize(); i++) {
				NodeHandle nh = leafSet.get(i);
				SocketNodeHandle snh = (SocketNodeHandle) nh;

				InetSocketAddress isa = snh.getAddress().getAddress(0);
				if (isa.getPort() == regionControllAddress.getPort()
						&& isa.getAddress()
								.getHostAddress()
								.equalsIgnoreCase(
										regionControllAddress.getAddress()
												.getHostAddress()))
					return nh;

			}
		} catch (Exception ex) {

		}

		return null;
	}

	public ClientApplication getApplication() {
		return app;
	}

	public boolean SendCoordinatorMovementMessage(int x, int y) {
		long msgId = this.SendCoordinatorMovementMessageAsync(x, y);
		try {
			synchronized (lock) {
				// We will wait until the ACK receives
				while (true) {
					lock.wait();
					for (int i = 0; i < recievedAcks.size(); i++)
						if (msgId == recievedAcks.get(i))
							return true;
				}
			}
		} catch (Exception ex) {
			return false;
		}
	}

	public long SendCoordinatorMovementMessageAsync(int x, int y) {
		return app.SendMovementMessage(regionController, x, y);
	}

	public void acknowledgmentReceived(long messageId) {
		synchronized (lock) {
			recievedAcks.add(messageId);
			lock.notifyAll();
		}
	}

	public boolean SendCoordinatorBombPlacementMessage(int x, int y) {
		long msgId = this.SendCoordinatorBombPlacementMessageAsync(x, y);
		try {
			synchronized (lock) {
				// We will wait until the ACK receives
				while (true) {
					lock.wait();
					for (int i = 0; i < recievedAcks.size(); i++)
						if (msgId == recievedAcks.get(i))
							return true;
				}
			}

		} catch (Exception ex) {
			return false;
		}
	}

	public long SendCoordinatorBombPlacementMessageAsync(int x, int y) {
		return app.SendBombPlacementMessage(regionController, x, y);
	}

	public Id GetNodeId() {
		return node.getId();
	}
}
