package com.mayhem.overlay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
	private Region region;

	protected PastryNode node;
	protected ClientApplication app;
	protected Map<Long, Object> recievedAcks;

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
			long startTime = System.currentTimeMillis();
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
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;

			System.out.println("Join overlay: " + elapsedTime + "ms");
		}

		System.out.println("Finished creating new node " + node);
		recievedAcks = new HashMap<Long, Object>();
		app.addActionAcknowledgmentListener(this);
		app.addRegionStateListener(regionStateListener);

		if (!isNewGame) {
			// Assume our bootstrapper is also the region controller
			// it will change if it's not the region controller when the node
			// received the replyJoinMessage
			this.app.setRegionController(regionControllerFinder(bootaddress)
					.getId());

			// We successfully connected to the overlay and find the
			// Coordinator.
			// So we should talk to him
			long msgId = app.SendJoinMessage(this.app.getRegionController());
			try {
				synchronized (lock) {
					// We will wait until the ACK receives
					while (true) {
						lock.wait();
						Iterator<Long> itr = recievedAcks.keySet().iterator();
						while (itr.hasNext())
							if (msgId == itr.next()) {
								if (recievedAcks.get(msgId) instanceof Region)
									region = (Region) recievedAcks.get(msgId);
								return;
							}
					}
				}
			} catch (Exception ex) {
				// return false;
			}

		} else {
			app.subscribe(this.node.getId().toString());
			this.app.setRegionController(node.getLocalHandle().getId());
		}

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
		long startTime = System.currentTimeMillis();

		long msgId = this.SendCoordinatorMovementMessageAsync(x, y);
		try {
			synchronized (lock) {
				// We will wait until the ACK receives
				while (true) {
					lock.wait();
					Iterator<Long> itr = recievedAcks.keySet().iterator();
					while (itr.hasNext())
						if (msgId == itr.next()) {
							long stopTime = System.currentTimeMillis();
							long elapsedTime = stopTime - startTime;
//							System.out.println("MSG#" + msgId + ": "
//									+ elapsedTime + "ms");
							return true;
						}
				}
			}
		} catch (Exception ex) {
			return false;
		}
	}

	public long SendCoordinatorMovementMessageAsync(int x, int y) {
		return app.SendMovementMessage(this.app.getRegionController(), x, y);
	}

	public void acknowledgmentReceived(long messageId, Object result) {
		synchronized (lock) {
			recievedAcks.put(messageId, result);
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
					Iterator<Long> itr = recievedAcks.keySet().iterator();
					while (itr.hasNext())
						if (msgId == itr.next())
							return true;
				}
			}

		} catch (Exception ex) {
			return false;
		}
	}

	public long SendCoordinatorBombPlacementMessageAsync(int x, int y) {
		return app.SendBombPlacementMessage(this.app.getRegionController(), x,
				y);
	}

	public Id GetNodeId() {
		return node.getId();
	}

	public Region getRegion() {
		return this.region;
	}

	public void leaveGame() {
		app.SendLeaveGameMessage(this.app.getRegionController());
	}
	
	public void leaveGame(Id killedByPlayer) {
		app.SendLeaveGameMessage(this.app.getRegionController(), killedByPlayer);
	}
	
	public void died(Id killedByPlayer) {
		app.SendDieMessage(this.app.getRegionController(), killedByPlayer);
	}
}
