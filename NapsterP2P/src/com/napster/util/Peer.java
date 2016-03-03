package com.napster.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * The class defined the identify of a Peer:InetAddress and port of socket
 * 
 */
public class Peer {
	private InetAddress inetAddress;
	private int port;

	public Peer() {
		super();
	}

	public Peer(InetAddress inetAddress, int port) {
		super();
		this.inetAddress = inetAddress;
		this.port = port;
	}

	public Peer(String ipaddress, String port) throws UnknownHostException {
		super();
		String[] ipaddresses = ipaddress.split("\\.");
		if (ipaddresses.length == 4) {
			this.inetAddress = InetAddress.getByAddress(
					new byte[] { (byte) Integer.parseInt(ipaddresses[0]), (byte) Integer.parseInt(ipaddresses[1]),
							(byte) Integer.parseInt(ipaddresses[2]), (byte) Integer.parseInt(ipaddresses[3]) });
			this.port = Integer.parseInt(port);
		} else {
			throw new UnknownHostException();
		}
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public synchronized void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public int getPort() {
		return port;
	}

	public synchronized void setPort(int port) {
		this.port = port;
	}

	@Override
	public boolean equals(Object obj) {
		// override the equals method to judge whether peer A equal to peer B
		// if A's inetAddress and socket port equals to B, they are equal
		if (obj.getClass().getName().equals(Peer.class.getName())) {
			Peer other = (Peer) obj;
			if (other.getInetAddress().equals(this.getInetAddress()) && other.getPort() == this.getPort()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

}
