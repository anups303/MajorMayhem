package com.mayhem.mediator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import rice.environment.Environment;

import com.mayhem.overlay.ClientApplication;
import com.mayhem.overlay.NodeLauncher;
import com.mayhem.overlay.test.TestNodeLauncher;

public class Mediator {
	protected Environment environment;
	protected NodeLauncher nodeLauncher;
	protected ClientApplication app;

	// Create and configure a new environment for overlay
	public boolean joinGame(String bootIp, int bootport) {

		int bindport;

		try {
			bindport = 9001;

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
					false);

			app = nodeLauncher.getApplication();
		} catch (Exception e) {
			// TODO: log the exception
			return false;
		}
		return true;
	}

	public boolean newGame() {

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
					true);

			app = nodeLauncher.getApplication();
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public boolean updatePosition(int x, int y) {
		nodeLauncher.SendCoordinatorMovementMessage(x, y);
		return true;
	}
}
