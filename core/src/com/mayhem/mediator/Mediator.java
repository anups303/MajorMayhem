package com.mayhem.mediator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;

import com.mayhem.overlay.ClientApplication;
import com.mayhem.overlay.IRegionStateListener;
import com.mayhem.overlay.NodeLauncher;
import com.mayhem.overlay.Region;

//Mediating between GUI and overlay
public class Mediator {
	protected Environment environment;
	protected NodeLauncher nodeLauncher;
	protected ClientApplication app;

	public Region joinGame(String bootIp, int bootport,
			IRegionStateListener regionStateListener) {
		return joinGame(bootIp, bootport, regionStateListener, 9001);
	}

	//Join the overlay and an existing game 
	public Region joinGame(String bootIp, int bootport,
			IRegionStateListener regionStateListener, int bindPort) {
		try {

			if (bootIp == null)
				bootIp = InetAddress.getLocalHost().getHostAddress();
			// More than one instance of game running on a same machine
			if (bootIp.equalsIgnoreCase(InetAddress.getLocalHost()
					.getHostAddress()) && bootport == bindPort)
				bindPort += Math.random() * (1000);

			environment = new Environment();

			//Disable NAT (Built-in Freepastry)
			environment.getParameters().setString("nat_search_policy", "never");
			InetAddress bootaddr = InetAddress.getByName(bootIp);
			InetSocketAddress bootaddress = new InetSocketAddress(bootaddr,
					bootport);

			nodeLauncher = new NodeLauncher(bindPort, bootaddress, environment,
					false, regionStateListener);

			app = nodeLauncher.getApplication();
		} catch (Exception e) {
			// TODO: log the exception
			System.out.println(e);
			return null;
		}
		return nodeLauncher.getRegion();
	}

	// Create and configure a new environment for overlay
	public int newGame(IRegionStateListener regionStateListener) {

		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			int bootport, bindport = 9001;
			bootport = bindport;
			InetAddress bootaddr = InetAddress.getByName(ip);
			InetSocketAddress bootaddress = new InetSocketAddress(bootaddr,
					bootport);
			
			environment = new Environment();
			
			//Disable NAT (Built-in Freepastry)
			environment.getParameters().setString("nat_search_policy", "never");
			nodeLauncher = new NodeLauncher(bindport, bootaddress, environment,
					true, regionStateListener);
			app = nodeLauncher.getApplication();
		} catch (Exception e) {
			return -1;
		}

		return app.getRegion().getMapId();
	}

	//Let coordinator knows about new position and wait for ACK
	public boolean updatePosition(int x, int y) {
		if (nodeLauncher != null)
			return nodeLauncher.SendCoordinatorMovementMessage(x, y);
		else
			return false;
	}

	//Let coordinator knows about bomb placement and wait for ACK
	public boolean bombPlacement(int x, int y) {
		return nodeLauncher.SendCoordinatorBombPlacementMessage(x, y);
	}

	//Graceful leave
	public void leaveGame() {
		nodeLauncher.leaveGame();
	}

	//Killed by player: 
	public void died(Id killedByPlayer) {
		nodeLauncher.died(killedByPlayer);
	}

	public Id GetNodeId() {
		return nodeLauncher.GetNodeId();
	}

	public Region getRegionState() {
		return nodeLauncher.getApplication().getRegion();
	}

	//Ask coordinator to get players' score
	public HashMap<String, Integer> getPlayersScore() {
		return nodeLauncher.getPlayersScore();
	}
}
