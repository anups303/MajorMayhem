package com.mayhem.game.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.mayhem.mediator.Mediator;
import com.mayhem.overlay.IRegionStateListener;
import com.mayhem.overlay.Region;
import com.mayhem.overlay.test.TestNodeLauncher;

import rice.environment.Environment;

public class ConsoleLauncher implements IRegionStateListener {

	private Mediator mediator = new Mediator();

	public static void main(String[] args) throws IOException {
		new ConsoleLauncher().launch();
	}

	private void launch() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("MajorMayhem");
		System.out.println("1-New Game");
		System.out.println("2-Join Game");

		int input = Integer.parseInt(br.readLine());
		if (input == 1) {
			int mapId = mediator.newGame(this);
		} else if (input == 2) {
			int bootstrapperPort = 9001, localPort = 9002;
			System.out.print("Bootrapper IP (null=local):");
			String ip = br.readLine();
			if (ip.equals(""))
				ip = null;
			System.out.print("Bootrapper Port (null=9001)");
			String port = br.readLine();
			if (!port.equals(""))
				bootstrapperPort = Integer.parseInt(port);

			System.out.print("Bind Port (null=9002)");
			port = br.readLine();
			if (!port.equals(""))
				localPort = Integer.parseInt(port);

			Region init = mediator.joinGame(ip, bootstrapperPort, this,
					localPort);
		}
	}

	@Override
	public void regionStateReceived(Region region) {
		// TODO Auto-generated method stub

	}
}
