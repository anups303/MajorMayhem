package com.mayhem.mediator;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import rice.environment.Environment;

import com.mayhem.overlay.NodeLauncher;

public class Mediator {
	Environment environment;
	NodeLauncher nodeLauncher;
	
	// Create and configure a new environment for overlay
	public boolean JoinGame(String bootIp, int bootport) {

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

			nodeLauncher = new NodeLauncher(bindport, bootaddress,
					environment, false);
		} catch (Exception e) {
			//TODO: log the exception
			return false;
		}
		return true;
	}

}
