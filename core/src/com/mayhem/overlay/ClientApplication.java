package com.mayhem.overlay;

import java.util.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import com.mayhem.overlay.test.TestChannelContent;
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
	protected List<Id> regionMembers;
	protected Endpoint endpoint;
	protected Region region;

	public ClientApplication(Node node, boolean isNewGame) {
		this.region = new Region();
		this.isCoordinator = isNewGame;
		this.regionMembers = new ArrayList<Id>();
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
		// if (message instanceof PublishContent) {
		// // sendMulticast();
		// // sendAnycast();
		// }
		// else
		if (message instanceof MovementMessage) {
			MovementMessage msg = (MovementMessage) message;

			// TODO: validating movement
			// in case of valid movement, coordinator must acknowledge it.

			// try {
			// this.node.getEnvironment().getTimeSource().sleep(1000);
			// } catch (Exception w) {
			// }

			this.routMessage(msg.getSender(), new ActionAcknowledgmentMessage(
					msg.getMessageId(), true));

			for (PlayerState player : this.region.getPlayers()) {
				if (player.getId() == msg.getSender()) {
					player.setX(msg.getX());
					player.setY(msg.getY());
					break;
				}
			}
			// Then Coordinator has to propagate new game state on the channel
			scribe.publish(topic, new RegionStateChannelContent(this.region));

		} else if (message instanceof ActionAcknowledgmentMessage) {
			ActionAcknowledgmentMessage msg = (ActionAcknowledgmentMessage) message;
			if (msg.getValid()) {
				// System.out.println("action " + msg.getActionMessageId()
				// + " is valid");
			}
			for (IActionAcknowledgmentListner hl : actionAcknowledgmentlisteners)
				hl.acknowledgmentReceived(msg.getActionMessageId());

		} else if (message instanceof BombPlacementMessage) {
			BombPlacementMessage msg = (BombPlacementMessage) message;

			// TODO: validating the bomb
			// in case of valid bomb placement, coordinator must acknowledge it.

			this.routMessage(msg.getSender(), new ActionAcknowledgmentMessage(
					msg.getMessageId(), true));

			// Then Coordinator has to propagate new game state on the channel
			region.bombs.add(new BombState(msg.getSender(), msg.getX(), msg
					.getY()));
			scribe.publish(topic, new RegionStateChannelContent(region));

		} else if (message instanceof ChangeRegionMessage) {
			ChangeRegionMessage msg = (ChangeRegionMessage) message;

			if (this.rightCoordinator == null) {
				this.routMessage(
						msg.getSender(),
						new BecomeRegionControllerMessage(null, this.node
								.getId(), null, null));
			}
			this.region.removePlayerById(msg.getSender());
			scribe.publish(topic, new RegionStateChannelContent(region));
		} else if (message instanceof JoinMessage) {
			JoinMessage msg = (JoinMessage) message;
			// If I'm the region controller, then welcome to the region
			if (this.isCoordinator) {
				this.regionMembers.add(msg.getSender());
				this.region.addPlayer(new PlayerState(msg.getSender()));
				System.out.println("Join:" + msg.getSender());
				this.routMessage(msg.getSender(), new JoinReplyMessage(
						this.channelName, this.node.getId(), this.region));
			}
			// Otherwise I will forward the message to my coordinator, he may
			// help him
			else {
				this.routMessage(this.regionController, msg);
			}
		} else if (message instanceof JoinReplyMessage) {
			JoinReplyMessage msg = (JoinReplyMessage) message;

			this.setRegionController(msg.getCoordinatorId());

			System.out.println("JoinReply:" + msg.getChannelName());
			this.subscribe(msg.getChannelName());
		
			for (IRegionStateListener rsl : regionStateListeners)
				rsl.regionStateReceived(msg.getRegion());

		} else if (message instanceof BecomeRegionControllerMessage) {
			BecomeRegionControllerMessage msg = (BecomeRegionControllerMessage) message;
			this.isCoordinator = true;
			this.leftCoordinator = msg.getLeftCoordinator();
			this.rightCoordinator = msg.getRightCoordinator();
			this.topCoordinator = msg.getTopCoordinator();
			this.bottomCoordinator = msg.getBottomCoordinator();
		}

		else if (message instanceof LeaveMessage) {
			LeaveMessage msg = (LeaveMessage) message;
			try {
				this.regionMembers.remove(msg.getSender());
				for (int i = 0; i < this.region.getPlayers().size(); i++)
					if (this.region.getPlayers().get(i).getId() == msg
							.getSender()) {
						this.region.getPlayers().remove(i);
						break;
					}
				System.out.println("Leave:" + msg.getSender());
				for (IRegionStateListener rsl : regionStateListeners)
					rsl.regionStateReceived(this.region);
			} catch (Exception ex) {

			}
		} else if (message instanceof com.mayhem.overlay.Message) {
			System.out.println(this + " received " + message);
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

	public void setRegionController(Id regionController) {
		this.regionController = regionController;
	}

}
