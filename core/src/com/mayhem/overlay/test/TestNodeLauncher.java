package com.mayhem.overlay.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import rice.environment.Environment;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.Id;
import rice.pastry.PastryNodeFactory;
import rice.pastry.direct.DirectPastryNodeFactory;
import rice.pastry.dist.DistPastryNodeFactory;
import rice.pastry.socket.SocketNodeHandle;
import rice.pastry.socket.SocketNodeHandleFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.IPNodeIdFactory;

import com.mayhem.overlay.ClientApplication;
import com.mayhem.overlay.ClientApplicationFactory;
import com.mayhem.overlay.NodeLauncher;

public class TestNodeLauncher {//extends NodeLauncher {

//	public TestNodeLauncher(int bindport, InetSocketAddress bootaddress,
//			Environment env, boolean isNewGame) throws Exception {
//		super(bindport, bootaddress, env, new TestClientApplicationFactory(),
//				isNewGame);
//
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//
//		if (isNewGame) {
//			System.out.println("Message to publish on scribe channel:");
//			String opt = br.readLine();
//			
//		} else {
////			System.out.println("Message to publish on scribe channel:");
//		}
//
//	}
//
//	private static String getIpAsString(InetAddress address) {
//		byte[] ipAddress = address.getAddress();
//		StringBuffer str = new StringBuffer();
//		for (int i = 0; i < ipAddress.length; i++) {
//			if (i > 0)
//				str.append('.');
//			str.append(ipAddress[i] & 0xFF);
//		}
//		return str.toString();
//	}
}
