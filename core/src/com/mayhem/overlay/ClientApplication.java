package com.mayhem.overlay;

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
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.transport.SocketAdapter;

public class ClientApplication implements Application, ScribeClient{
	boolean isCoordinator;
	Node node;
	CancellableTask publishTask;
	Scribe scribe;
	Topic topic;
	protected Endpoint endpoint;

	public ClientApplication(Node node, boolean isNewGame) {
		this.isCoordinator = isNewGame;
		this.node = node;
		this.endpoint = node.buildEndpoint(this, "instance");
		scribe = new ScribeImpl(node, "scribeInstance");
		topic = new Topic(new PastryIdFactory(node.getEnvironment()),
				"R#1Channel");
		endpoint.register();
	}

//	public void SendTestMessage(Id id, String msg) {
//		routMessage(id, new TestMessage(msg));
//	}
//
//	public void SendJoinMessage(Id id, JoinMessage msg) {
//		routMessage(id, msg);
//	}

	private void routMessage(Id id, com.mayhem.overlay.Message msg) {
		
//		bootHandle = ((SocketPastryNodeFactory)factory).getNodeHandle(bootaddress);
		endpoint.route(id, msg, null);
	}

	private void routeMyMsgDirect(NodeHandle nh, com.mayhem.overlay.Message msg) {
		endpoint.route(null, msg, nh);
	}

	public void deliver(Id id, Message message) {
		// if (message instanceof PublishContent) {
		// // sendMulticast();
		// // sendAnycast();
		// }
		// else

		if (message instanceof com.mayhem.overlay.JoinMessage) {
			com.mayhem.overlay.JoinMessage msg = (com.mayhem.overlay.JoinMessage) message;
			System.out.println("Join Message received from:" + msg.getSender());
//			this.SendTestMessage(msg.getSender(), "TestBack");
		} 
		else if (message instanceof com.mayhem.overlay.Message) {
			System.out.println(this + " received " + message);
			com.mayhem.overlay.Message msg = (com.mayhem.overlay.Message) message;

			sendMulticast(message.toString());
		}

	}

	public void subscribe() {
		scribe.subscribe(topic, this);
	}

	public void subscribeFailed(Topic topic) {
	}

	public boolean forward(RouteMessage message) {
		return true;
	}

	public void update(NodeHandle handle, boolean joined) {

	}
	
	public void deliver(Topic topic, ScribeContent content) {
		System.out.println("ChannelContent received:" + topic + "," + content + ")");
	}
	
	public boolean anycast(Topic topic, ScribeContent content) {
		return true;
	}

	public void childAdded(Topic topic, NodeHandle child) {
	}

	public void childRemoved(Topic topic, NodeHandle child) {
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
//		ChannelContent message = new TestChannelContent(
//				endpoint.getLocalNodeHandle(), msg);
//		scribe.publish(topic, message);
	}

//	public void deliver(Id id, Message message) {
//		super.deliver(id, message);
//		
//		if (message instanceof overlay.Message) {
//			System.out.println(this + " received " + message);
//			overlay.Message msg = (overlay.Message) message;
//
//			sendMulticast(message.toString());
//		}
//	}
}
