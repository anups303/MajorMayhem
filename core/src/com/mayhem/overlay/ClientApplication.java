package com.mayhem.overlay;

import java.util.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import com.sun.corba.se.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.se.impl.protocol.giopmsgheaders.MessageHandler;
import com.sun.corba.se.impl.protocol.giopmsgheaders.ReplyMessage;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.servicecontext.ServiceContexts;

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
import rice.pastry.socket.SocketNodeHandle;
import rice.pastry.socket.SocketNodeHandleFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.transport.SocketAdapter;

public class ClientApplication implements Application, ScribeClient {
	boolean isCoordinator;
	Node node;
	CancellableTask publishTask;
	Scribe scribe;
	Topic topic;
	String channelName;
	protected List<Id> regionMembers;
	protected Endpoint endpoint;

	public ClientApplication(Node node, boolean isNewGame) {
		this.isCoordinator = isNewGame;
		this.regionMembers = new ArrayList<Id>();
		this.node = node;
		this.endpoint = node.buildEndpoint(this, "instance");
		scribe = new ScribeImpl(node, "scribeInstance");

		endpoint.register();
	}

	public void SendJoinMessage(NodeHandle coordinatorHandle) {
		routeMessageDirect(coordinatorHandle,
				new JoinMessage(this.node.getId()));
	}

	public void SendMovementMessage(NodeHandle coordinatorHandle, int x, int y) {
		routeMessageDirect(coordinatorHandle,
				new MovementMessage(this.node.getId(), x, y));
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
		// if (message instanceof PublishContent) {
		// // sendMulticast();
		// // sendAnycast();
		// }
		// else
		if (message instanceof MovementMessage) {
			MovementMessage msg = (MovementMessage) message;

			System.out.println("Movement:" + msg.getSender() + " to ("
					+ msg.getX() + "," + msg.getY() + ")");


		} else if (message instanceof JoinMessage) {
			JoinMessage msg = (JoinMessage) message;
			this.regionMembers.add(msg.getSender());
			System.out.println("Join:" + msg.getSender());
			this.routMessage(msg.getSender(), new JoinReplyMessage(
					this.channelName, this.node.getId()));
		} else if (message instanceof JoinReplyMessage) {
			JoinReplyMessage msg = (JoinReplyMessage) message;
			System.out.println("JoinReply:" + msg.getChannelName());
			this.subscribe(msg.getChannelName());
		} else if (message instanceof com.mayhem.overlay.Message) {
			System.out.println(this + " received " + message);
			// com.mayhem.overlay.Message msg = (com.mayhem.overlay.Message)
			// message;
			sendMulticast(message.toString());
		}
	}

	public void subscribe(String channelName) {
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
	}

	public void deliver(Topic topic, ScribeContent content) {
		System.out.println("ChannelContent received:" + topic + "," + content
				+ ")");
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
		// ChannelContent message = new TestChannelContent(
		// endpoint.getLocalNodeHandle(), msg);
		// scribe.publish(topic, message);
	}
}
