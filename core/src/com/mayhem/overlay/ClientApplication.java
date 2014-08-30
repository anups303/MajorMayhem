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
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.leafset.LeafSet;

public class ClientApplication implements Application, ScribeClient,
		DeliveryNotification {

	private final Object lookupReplylock = new Object();
	private final Object scoreReplylock = new Object();
	protected HashSet<Long> messageId;
	protected Map<Long, Id> recievedLookupReplyAcks;
	protected Map<Long, Object> recievedScoreReplyAcks;

	protected List<IActionAcknowledgmentListner> actionAcknowledgmentlisteners = new ArrayList<IActionAcknowledgmentListner>();
	protected List<IRegionStateListener> regionStateListeners = new ArrayList<IRegionStateListener>();
	protected HashMap<Id, Id> coordinatorList = new HashMap<Id, Id>();

	protected Id regionController;
	protected Id rightCoordinator, leftCoordinator, topCoordinator,
			bottomCoordinator;

	protected boolean isCoordinator;
	protected PastryNode node;
	protected CancellableTask publishTask;
	protected Scribe scribe;
	protected Topic topic;
	protected String channelName;
	protected Endpoint endpoint;
	protected Region region;

	public ClientApplication(PastryNode node, boolean isNewGame) {
		messageId = new HashSet<Long>();
		recievedLookupReplyAcks = new HashMap<Long, Id>();
		recievedScoreReplyAcks = new HashMap<Long, Object>();

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
		JoinMessage msg = new JoinMessage(this.node.getId(), coordinatorHandle);
		this.routeMessage(coordinatorHandle, msg);

		return msg.getMessageId();
	}

	public long SendMovementMessage(Id coordinatorHandle, int x, int y) {
		MovementMessage msg = new MovementMessage(this.node.getId(),
				coordinatorHandle, x, y);
		this.routeMessage(coordinatorHandle, msg);

		return msg.getMessageId();
	}

	public long SendBombPlacementMessage(Id coordinatorHandle, int x, int y) {
		BombPlacementMessage msg = new BombPlacementMessage(this.node.getId(),
				coordinatorHandle, x, y);
		this.routeMessage(coordinatorHandle, msg);

		return msg.getMessageId();
	}

	public void SendDieMessage(Id coordinatorHandle, Id killedByPlayer) {
		if (this.isCoordinator) {
			DieMessage msg = new DieMessage(this.node.getId(),
					this.node.getId(), killedByPlayer);
			msg.execute(this);
		} else {
			DieMessage msg = new DieMessage(this.node.getId(),
					coordinatorHandle, killedByPlayer);
			this.routeMessage(coordinatorHandle, msg);
		}
	}

	public void SendLeaveGameMessage(Id coordinatorHandle) {
		this.SendLeaveGameMessage(coordinatorHandle, this.node.getId(), null);
	}

	public void SendLeaveGameMessage(Id coordinatorHandle, Id killedByPlayer) {
		this.SendLeaveGameMessage(coordinatorHandle, this.node.getId(),
				killedByPlayer);
	}

	protected void SendLeaveGameMessage(Id coordinatorHandle, Id sender,
			Id killedByPlayer) {
		if (this.isCoordinator) {
			if (this.region.removePlayerById(sender)) {
				// TODO: Let the neighbor coordinator know the new coordinator
				if (this.region.players.size() > 0) {

					if (killedByPlayer != null)
						this.getRegion().increaseScore(killedByPlayer);

					PlayerState newCoordinator = this.region.players.get(0);
					this.routeMessage(
							newCoordinator.getId(),
							new BecomeRegionControllerMessage(newCoordinator
									.getId(), leftCoordinator,
									rightCoordinator, topCoordinator,
									bottomCoordinator, 0, 0, 0, 0, this.region,
									newCoordinator.getScore()));

					this.publishRegionState(newCoordinator.getId());
				}
			}

		} else {
			LeaveMessage msg = new LeaveMessage(sender, coordinatorHandle,
					killedByPlayer);
			this.routeMessage(coordinatorHandle, msg);
		}
	}

	protected void routeMessage(Id receiver, com.mayhem.overlay.Message msg) {
		// MessageReceipt mr =
		endpoint.route(receiver, msg, null, this);
	}

	protected void routeMessageDirect(NodeHandle receiver,
			com.mayhem.overlay.Message msg) {
		endpoint.route(null, msg, receiver);
	}

	public void deliver(Id id, Message message) {

		if (this.getLocalNodeId() != id)
			System.out
					.println(this.getLocalNodeId() + ":" + id + "-" + message);
		if (message instanceof com.mayhem.overlay.Message) {
			com.mayhem.overlay.Message msg = (com.mayhem.overlay.Message) message;
			if (msg.getTTL() > 0 && messageId.add(msg.getMessageId())) {
				msg.decreaseTTL();
				class OneShotTask implements Runnable {
					com.mayhem.overlay.Message msg;
					ClientApplication app;

					OneShotTask(com.mayhem.overlay.Message msg,
							ClientApplication app) {
						this.msg = msg;
						this.app = app;
					}

					public void run() {
						msg.execute(app);
					}
				}

				Thread t = new Thread(new OneShotTask(msg, this));
				t.start();
			}

			// We have received a message which doesn't belong to us!
			// if (this.getLocalNodeId() != msg.getReceiver()) {
			// System.out
			// .println(this.getLocalNodeId() + "received" + message);
			// }

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
		if (!this.isCoordinator) {
			if (arg0.getMessage() instanceof com.mayhem.overlay.Message) {
				com.mayhem.overlay.Message msg = (com.mayhem.overlay.Message) arg0
						.getMessage();
				System.out.println("failed to send:" + msg + "-Latency:"
						+ (System.currentTimeMillis() - msg.getSentTime())
						/ 1000 + "s");

				this.routeMessage(
						msg.getSender(),
						new ActionAcknowledgmentMessage(msg.getSender(), msg
								.getMessageId(), false));

				if (this.region.players.size() > 1) {
					PlayerState newCoordinator = this.region.players.get(1);
					this.region.players.remove(0);

					endpoint.route(
							newCoordinator.getId(),
							new BecomeRegionControllerMessage(newCoordinator
									.getId(), leftCoordinator,
									rightCoordinator, topCoordinator,
									bottomCoordinator, newCoordinator.getX(),
									newCoordinator.getY(), region.x, region.y,
									region, newCoordinator.getScore()), null,
							this);

					// if (newCoordinator.getId() == this.getLocalNodeId()) {
					//
					// }
				}
			}
		}
	}

	@Override
	public void sent(MessageReceipt arg0) {
		// System.out.println(arg0);
	}

	public void regionControllerLookupReplyReceived(long messageId,
			Id coordinator) {
		synchronized (lookupReplylock) {
			recievedLookupReplyAcks.put(messageId, coordinator);
			lookupReplylock.notifyAll();
		}
	}

	protected Id FindRegionController(long x, long y) {
		Id regionId = Region.RegionId(x, y);
		RegionControllerLookup msg = new RegionControllerLookup(
				this.getLocalNodeId(), null, regionId);
		long msgId = msg.getMessageId();
		System.out.println(Thread.currentThread().getId());
		this.routeMessage(regionId, msg);
		try {
			int c = 0;
			// We will wait until the ACK receives
			while (true) {
				c++;
				synchronized (lookupReplylock) {
					if (recievedLookupReplyAcks.containsKey(msgId))
						return recievedLookupReplyAcks.get(msgId);
					lookupReplylock.wait(500);
				}
				// return null after 2 seconds
				if (c > 1) {
					if (recievedLookupReplyAcks.containsKey(msgId))
						return recievedLookupReplyAcks.get(msgId);
					return null;
				}
			}
		} catch (Exception ex) {
			System.out.println(ex);
			// return false;
		}
		return null;
	}

	public void scoreReplyReceived(long messageId,
			HashMap<String, Integer> scores) {
		synchronized (scoreReplylock) {
			recievedScoreReplyAcks.put(messageId, scores);
			scoreReplylock.notifyAll();
		}
	}

	public HashMap<String, Integer> getPlayersScore(int depth) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		for (PlayerState player : region.players)
			result.put(player.getId().toString(), player.getScore());

//		if (depth > 0) {
//			HashMap<String, Integer> tmp = getNeighborScore(
//					this.leftCoordinator, depth);
//			if (tmp != null)
//				result.putAll(tmp);
//			tmp = getNeighborScore(this.rightCoordinator, depth);
//			if (tmp != null)
//				result.putAll(tmp);
//			tmp = getNeighborScore(this.topCoordinator, depth);
//			if (tmp != null)
//				result.putAll(tmp);
//			tmp = getNeighborScore(this.bottomCoordinator, depth);
//			if (tmp != null)
//				result.putAll(tmp);
//		}
		return result;
	}

	private HashMap<String, Integer> getNeighborScore(Id neighbor, int depth) {
		if (neighbor != null) {
			ScoreMessage msg = new ScoreMessage(this.getLocalNodeId(), neighbor);
			msg.setTTL(depth);
			long msgId = msg.getMessageId();
			this.routeMessage(neighbor, msg);
			try {
				int c = 0;
				// We will wait until the ACK receives
				while (true) {
					c++;
					synchronized (scoreReplylock) {
						if (recievedScoreReplyAcks.containsKey(msgId))
							return (HashMap<String, Integer>) recievedScoreReplyAcks
									.get(msgId);
						lookupReplylock.wait(500);
					}
					// return null after 2 seconds
					if (c > 1) {
						if (recievedScoreReplyAcks.containsKey(msgId))
							return (HashMap<String, Integer>) recievedScoreReplyAcks
									.get(msgId);
						return null;
					}
				}
			} catch (Exception ex) {
				System.out.println(ex);
				// return false;
			}
		}
		return null;
	}
}
