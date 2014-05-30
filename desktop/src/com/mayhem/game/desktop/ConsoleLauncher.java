package com.mayhem.game.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.mayhem.overlay.test.TestNodeLauncher;
import rice.environment.Environment;

public class ConsoleLauncher {

	public static void main(String[] args) throws IOException {
		String ip = "";
		int bindport, bootport;
		boolean newGame = false;

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("--Major Mayhem--");
		System.out.println("1:New game 2:Join a game");

		String opt = br.readLine();
		if (opt.equalsIgnoreCase("1")) {
			ip = InetAddress.getLocalHost().getHostAddress();
			bootport = 9001;
			bindport = 9001;
			newGame = true;
		} else if (opt.equalsIgnoreCase("2")) {
			System.out.println("JOIN A GAME");
			System.out.println("IP:");
			ip = br.readLine();
			System.out.println("Port:");
			bootport = Integer.parseInt(br.readLine());
			bindport = 9001;
			if (ip.equalsIgnoreCase(InetAddress.getLocalHost().getHostAddress()))
				bindport += Math.random() * (1000);

		} else
			return;

		Environment env = new Environment();

		env.getParameters().setString("nat_search_policy", "never");

		try {

			InetAddress bootaddr = InetAddress.getByName(ip);

			InetSocketAddress bootaddress = new InetSocketAddress(bootaddr,
					bootport);

			TestNodeLauncher dt = new TestNodeLauncher(bindport, bootaddress, env,
					newGame);
		} catch (Exception e) {
		}
	}
}
