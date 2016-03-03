package napster.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import napster.service.IndexServiceImpl;

/**
 * the class indexServer
 *
 */
public class IndexServer extends Thread {
	// indexes: the key is fileName,and value is peerClients which can provide
	// the download service for this file
	private Map<String, String> indexes = Collections.synchronizedMap(new HashMap<String, String>());
	// the indexServer's port
	private int port;

	public IndexServer(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public Map<String, String> getIndexes() {
		return indexes;
	}

	@Override
	public void run() {
		try {
			LocateRegistry.createRegistry(port);
			IndexServiceImpl dls = new IndexServiceImpl(this);
			Naming.rebind("rmi://localhost:" + port + "/indexservice", dls);
			System.out.println("index service port is :" + port + ". indexServer ready...");
		} catch (RemoteException | MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
