package com.mayhem.mediator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;

import com.mayhem.overlay.ClientApplication;
import com.mayhem.overlay.IRegionStateListener;
import com.mayhem.overlay.NodeLauncher;
import com.mayhem.overlay.PlayerState;
import com.mayhem.overlay.Region;

public class Mediator {
	protected Environment environment;
	protected NodeLauncher nodeLauncher;
	protected ClientApplication app;

	// Create and configure a new environment for overlay
	public Region joinGame(String bootIp, int bootport,
			IRegionStateListener regionStateListener) {

		int bindport;

		try {
			bindport = 9001;

			if (bootIp == null)
				bootIp = InetAddress.getLocalHost().getHostAddress();
			// More than one instance of game running on a same machine
			if (bootIp.equalsIgnoreCase(InetAddress.getLocalHost()
					.getHostAddress()))
				bindport += Math.random() * (1000);

			environment = new Environment();

			environment.getParameters().setString("nat_search_policy", "never");

			InetAddress bootaddr = InetAddress.getByName(bootIp);

			InetSocketAddress bootaddress = new InetSocketAddress(bootaddr,
					bootport);

			nodeLauncher = new NodeLauncher(bindport, bootaddress, environment,
					false, regionStateListener);

			app = nodeLauncher.getApplication();
		} catch (Exception e) {
			// TODO: log the exception
			System.out.println(e);
			return null;
		}
		return nodeLauncher.getRegion();
	}

	public int newGame(IRegionStateListener regionStateListener) {

		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			int bootport, bindport = 9001;
			bootport = bindport;

			environment = new Environment();

			environment.getParameters().setString("nat_search_policy", "never");

			InetAddress bootaddr = InetAddress.getByName(ip);

			InetSocketAddress bootaddress = new InetSocketAddress(bootaddr,
					bootport);

			nodeLauncher = new NodeLauncher(bindport, bootaddress, environment,
					true, regionStateListener);

			app = nodeLauncher.getApplication();
		} catch (Exception e) {
			return -1;
		}

		return app.getRegion().getMapId();
	}

	public boolean updatePosition(int x, int y) {
		if (nodeLauncher != null)
			return nodeLauncher.SendCoordinatorMovementMessage(x, y);
		else
			return false;
	}

	public boolean bombPlacement(int x, int y) {
		return nodeLauncher.SendCoordinatorBombPlacementMessage(x, y);
	}

	public void leaveGame() {
		nodeLauncher.leaveGame();
	}

	public Id GetNodeId() {
		return nodeLauncher.GetNodeId();
	}
}
