package com.mayhem.overlay;

import java.util.*;

import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.CancellableTask;
import rice.p2p.commonapi.DeliveryNotification;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.MessageReceipt;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.ScribeClient;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeImpl;
import rice.p2p.scribe.Topic;
import rice.pastry.commonapi.PastryIdFactory;

public class ClientApplication implements Application, ScribeClient,
		DeliveryNotification {
	protected List<IActionAcknowledgmentListner> actionAcknowledgmentlisteners = new ArrayList<IActionAcknowledgmentListner>();
	protected List<IRegionStateListener> regionStateListeners = new ArrayList<IRegionStateListener>();

	protected Id regionController;
	protected Id rightCoordinator, leftCoordinator, topCoordinator,
			bottomCoordinator;

	protected boolean isCoordinator;
	protected Node node;
	protected CancellableTask publishTask;
	protected Scribe scribe;
	protected Topic topic;
	protected String channelName;
	protected Endpoint endpoint;
	protected Region region;

	public ClientApplication(Node node, boolean isNewGame) {
		this.isCoordinator = isNewGame;
		this.node = node;
		this.endpoint = node.buildEndpoint(this, "instance");
		scribe = new ScribeImpl(node, "scribeInstance");

		endpoint.register();
		if (isNewGame) {
			this.region = new Region(-1);
			this.region.addPlayer(new PlayerState(node.getId()));
		} else
			this.region = new Region(1);
	}

	public void addActionAcknowledgmentListener(
			IActionAcknowledgmentListner toAdd) {
		actionAcknowledgmentlisteners.add(toAdd);
	}

	public void addRegionStateListener(IRegionStateListener toAdd) {
		regionStateListeners.add(toAdd);
	}

	public long SendJoinMessage(Id coordinatorHandle) {
		JoinMessage msg = new JoinMessage(this.node.getId());
		this.routMessage(coordinatorHandle, msg);

		return msg.getMessageId();
	}

	public long SendMovementMessage(Id coordinatorHandle, int x, int y) {
		MovementMessage msg = new MovementMessage(this.node.getId(), x, y);
		this.routMessage(coordinatorHandle, msg);

		return msg.getMessageId();
	}

	public long SendBombPlacementMessage(Id coordinatorHandle, int x, int y) {
		BombPlacementMessage msg = new BombPlacementMessage(this.node.getId(),
				x, y);
		this.routMessage(coordinatorHandle, msg);

		return msg.getMessageId();
	}

	public void SendLeaveGameMessage(Id coordinatorHandle) {
		this.SendLeaveGameMessage(coordinatorHandle, this.node.getId());
	}

	protected void SendLeaveGameMessage(Id coordinatorHandle, Id sender) {
		if (this.isCoordinator) {
			if (this.region.removePlayerById(sender)) {
				// TODO: Let the neighbor coordinator know the new coordinator
				if (this.region.players.size() > 0) {
					Id newCoordinator = this.region.players.get(0).getId();
					this.routMessage(newCoordinator,
							new BecomeRegionControllerMessage(leftCoordinator,
									rightCoordinator, topCoordinator,
									bottomCoordinator, 0, 0, 0, 0, this.region));

					this.publishRegionState(newCoordinator);
				}
			}

		} else {
			LeaveMessage msg = new LeaveMessage(sender);
			this.routMessage(coordinatorHandle, msg);
		}
	}

	protected void routMessage(Id id, com.mayhem.overlay.Message msg) {
		// bootHandle =
		// ((SocketPastryNodeFactory)factory).getNodeHandle(bootaddress);

		MessageReceipt mr = endpoint.route(id, msg, null, this);

	}

	protected void routeMessageDirect(NodeHandle nh,
			com.mayhem.overlay.Message msg) {
		endpoint.route(null, msg, nh);
	}

	public void deliver(Id id, Message message) {
		System.out.println(message);
		if (message instanceof com.mayhem.overlay.Message) {
			((com.mayhem.overlay.Message) message).execute(this);
		}
	}

	public void subscribe(String channelName) {
		if (channelName == this.channelName)
			return;
		if (topic != null) {
			System.out.println("unsubscribe form:" + topic);
			scribe.unsubscribe(topic, this);
		}

		this.channelName = channelName;
		topic = new Topic(new PastryIdFactory(node.getEnvironment()),
				channelName);
		scribe.subscribe(topic, this);
		System.out.println("subscribe to:" + channelName + "topic:" + topic);
	}

	public void subscribeFailed(Topic topic) {
		System.out.println("failed to subscribe to:" + channelName + "topic:"
				+ topic);
	}

	public boolean forward(RouteMessage message) {
		return true;
	}

	public void update(NodeHandle handle, boolean joined) {

	}

	// if (joined)
	// // TODO: may be useful to change the way of bootstrapping and
	// // problem of finding coordinator id
	// System.out.println("Added to leafset:" + handle);
	// else {
	// if (isCoordinator)
	// this.SendLeaveGameMessage(this.node.getLocalNodeHandle()
	// .getId(), handle.getId());
	// }
	// }

	public void deliver(Topic topic, ScribeContent content) {
		if (content instanceof RegionStateChannelContent) {
			RegionStateChannelContent msg = (RegionStateChannelContent) content;

			if (msg.getRegion().x == this.region.x
					&& msg.getRegion().y == this.region.y) {
				if (!this.isCoordinator) {
					if (this.regionController != msg.getCoordinator()) {
						this.regionController = msg.getCoordinator();
						// this.subscribe(this.regionController.toString());
					}
					this.region = msg.getRegion();
				}
				for (IRegionStateListener rsl : regionStateListeners)
					rsl.regionStateReceived(msg.getRegion());
			}
		}
		// System.out.println("ChannelContent received:" + topic + "," + content
		// + ")");
	}

	public boolean anycast(Topic topic, ScribeContent content) {
		return true;
	}

	public void childAdded(Topic topic, NodeHandle child) {
	}

	public void childRemoved(Topic topic, NodeHandle child) {
		// if (isCoordinator) {
		// System.out.println("childRemoved");
		// this.SendLeaveGameMessage(this.node.getLocalNodeHandle().getId(),
		// child.getId());
		// }
	}

	public void sendAnycast(String msg) {
		// System.out.println("Node " + endpoint.getLocalNodeHandle()
		// + " anycasting " + seqNum);
		// MyScribeContent myMessage = new MyScribeContent(
		// endpoint.getLocalNodeHandle(), seqNum, msg);
		// scribe.anycast(topic, myMessage);
	}

	public void sendMulticast(String msg) {
		System.out.println("Node " + endpoint.getLocalNodeHandle()
				+ " multicasting " + msg);
		// ChannelContent message = new TestChannelContent(
		// endpoint.getLocalNodeHandle(), msg);
		// scribe.publish(topic, message);
	}

	public Id getRegionController() {
		return this.regionController;
	}

	protected void setRegionController(Id regionController) {
		this.regionController = regionController;
	}

	protected void addBomb(BombState bomb) {
		this.region.addBomb(bomb);
	}

	private void publish(ChannelContent content) {
		scribe.publish(topic, content);
	}

	protected void publishRegionState() {
		publishRegionState(this.node.getId());
	}

	protected void publishRegionState(Id coordinator) {
		Region r = this.getRegion().clone();
		r.destroyedBlocks = null;
		this.publish(new RegionStateChannelContent(r, coordinator));
		// this.region.destroyedBlocks.add(arg0);
		for (BombState bs : this.region.bombs) {
			this.region.destroyedBlocks.add(new Pair<Integer, Integer>(bs
					.getX() + 1, bs.getY()));
			this.region.destroyedBlocks.add(new Pair<Integer, Integer>(bs
					.getX() - 1, bs.getY()));
			this.region.destroyedBlocks.add(new Pair<Integer, Integer>(bs
					.getX(), bs.getY() + 1));
			this.region.destroyedBlocks.add(new Pair<Integer, Integer>(bs
					.getX(), bs.getY() - 1));
		}
		this.region.bombs.clear();
	}

	public Region getRegion() {
		return this.region;
	}

	protected void raiseRegionStateEvent() {
		raiseRegionStateEvent(false);
	}

	protected void raiseRegionStateEvent(boolean destroyedBlocks) {
		raiseRegionStateEvent(destroyedBlocks, this.region);
	}

	protected void raiseRegionStateEvent(boolean destroyedBlocks, Region region) {
		Region r = region.clone();
		if (!destroyedBlocks)
			r.destroyedBlocks = null;
		for (IRegionStateListener rsl : regionStateListeners)
			rsl.regionStateReceived(r);
	}

	protected void raiseActionAcknowledgmentEvent(long id, Object result) {
		for (IActionAcknowledgmentListner hl : actionAcknowledgmentlisteners)
			hl.acknowledgmentReceived(id, result);
	}

	protected boolean getIsCoordinator() {
		return this.isCoordinator;
	}

	protected String getChannelName() {
		return this.channelName;
	}

	protected Id getLocalNodeId() {
		return this.node.getId();
	}

	@Override
	public void sendFailed(MessageReceipt arg0, Exception arg1) {
		// TODO Auto-generated method stub
		System.out.println(arg0);

	}

	@Override
	public void sent(MessageReceipt arg0) {
		System.out.println(arg0);
		// TODO Auto-generated method stub

	}
}
