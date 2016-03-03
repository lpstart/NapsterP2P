package napster.util;

import java.io.Serializable;
import java.net.InetAddress;

public class PeerIdentify implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -871986449215397086L;
	private InetAddress address;
	private int port;

	public PeerIdentify(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	public PeerIdentify(String ipaddress, String port) throws Exception {
		String[] addressParts = ipaddress.split("\\.");
		if (addressParts.length == 4) {
			address = InetAddress.getByAddress(
					new byte[] { (byte) Integer.parseInt(addressParts[0]), (byte) Integer.parseInt(addressParts[1]),
							(byte) Integer.parseInt(addressParts[2]), (byte) Integer.parseInt(addressParts[3]) });
			this.port = Integer.parseInt(port);
		} else {
			throw new Exception("invalid ip address!");
		}
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
