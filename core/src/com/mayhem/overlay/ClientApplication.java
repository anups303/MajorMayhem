package com.mayhem.overlay;

import java.util.*;

import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.CancellableTask;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.ScribeClient;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeImpl;
import rice.p2p.scribe.Topic;
import rice.pastry.commonapi.PastryIdFactory;

public class ClientApplication implements Application, ScribeClient {
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
		this.region = new Region();
		this.isCoordinator = isNewGame;
		this.node = node;
		this.endpoint = node.buildEndpoint(this, "instance");
		scribe = new ScribeImpl(node, "scribeInstance");

		endpoint.register();
		this.region.addPlayer(new PlayerState(node.getId()));
	}

	public void addActionAcknowledgmentListener(
			IActionAcknowledgmentListner toAdd) {
		actionAcknowledgmentlisteners.add(toAdd);
	}

	public void addRegionStateListener(IRegionStateListener toAdd) {
		regionStateListeners.add(toAdd);
	}

	public void SendJoinMessage(Id coordinatorHandle) {
		this.routMessage(coordinatorHandle, new JoinMessage(this.node.getId()));
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
		LeaveMessage msg = new LeaveMessage(sender);
		this.routMessage(coordinatorHandle, msg);
	}

	protected void routMessage(Id id, com.mayhem.overlay.Message msg) {
		// bootHandle =
		// ((SocketPastryNodeFactory)factory).getNodeHandle(bootaddress);
		endpoint.route(id, msg, null);
	}

	protected void routeMessageDirect(NodeHandle nh,
			com.mayhem.overlay.Message msg) {
		endpoint.route(null, msg, nh);
	}

	public void deliver(Id id, Message message) {
		if (message instanceof com.mayhem.overlay.Message) {
			((com.mayhem.overlay.Message) message).execute(this);
		}
	}

	public void subscribe(String channelName) {
		if (topic != null) {
			scribe.unsubscribe(topic, this);
		}
		System.out.println("subscribe to:" + channelName);
		this.channelName = channelName;
		topic = new Topic(new PastryIdFactory(node.getEnvironment()),
				channelName);
		scribe.subscribe(topic, this);
	}

	public void subscribeFailed(Topic topic) {
	}

	public boolean forward(RouteMessage message) {
		return true;
	}

	public void update(NodeHandle handle, boolean joined) {
		if (joined)
			// TODO: may be useful to change the way of bootstrapping and
			// problem of finding coordinator id
			System.out.println("Added to leafset:" + handle);
		else {
			if (isCoordinator)
				this.SendLeaveGameMessage(this.node.getLocalNodeHandle()
						.getId(), handle.getId());
		}
	}

	public void deliver(Topic topic, ScribeContent content) {
		if (content instanceof RegionStateChannelContent) {
			RegionStateChannelContent msg = (RegionStateChannelContent) content;
			for (IRegionStateListener rsl : regionStateListeners)
				rsl.regionStateReceived(msg.getRegion());
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
		if (isCoordinator) {
			System.out.println("childRemoved");
			this.SendLeaveGameMessage(this.node.getLocalNodeHandle().getId(),
					child.getId());
		}
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
		this.publish(new RegionStateChannelContent(this.getRegion()));
	}

	protected Region getRegion() {
		return this.region;
	}

	protected void raiseRegionStateEvent() {
		for (IRegionStateListener rsl : regionStateListeners)
			rsl.regionStateReceived(this.region);
	}

	protected void raiseActionAcknowledgmentEvent(long id) {
		for (IActionAcknowledgmentListner hl : actionAcknowledgmentlisteners)
			hl.acknowledgmentReceived(id);
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
}
