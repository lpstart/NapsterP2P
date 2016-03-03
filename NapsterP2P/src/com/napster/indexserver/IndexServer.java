package com.napster.indexserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.napster.rpc.RPC;
import com.napster.service.IndexServerService;
import com.napster.service.IndexServerServiceImpl;
import com.napster.service.Server;
import com.napster.util.Message;
import com.napster.util.Peer;

/**
 * central indexing server which is security
 *
 */
public class IndexServer {
	// the only indexServer
	private static IndexServer indexServer = null;
	// indexes
	private ConcurrentHashMap<String, List<Peer>> indexes;

	private int indexServerPort;

	/**
	 * private constructor for single instance
	 */
	private IndexServer(int port) {
		super();
		indexes = new ConcurrentHashMap<String, List<Peer>>(16, 0.75f, 100);
		this.setIndexServerPort(port);
	}

	/**
	 * return the single indexServer instance
	 * 
	 * @return
	 */
	public static synchronized IndexServer getIndexServer() {
		return getIndexServer(Message.INDEXSERVERPORT);
	}

	public static synchronized IndexServer getIndexServer(int port) {
		if (indexServer == null) {
			indexServer = new IndexServer(port);
			return indexServer;
		} else {
			return indexServer;
		}
	}

	/**
	 * register a file
	 * 
	 * @param id
	 *            Peer which including IP address and socket port
	 * @param fileName
	 *            the name of file
	 * @return result success or failed
	 */
	public synchronized String registry(Peer id, String fileName) {
		List<Peer> peers = indexes.get(fileName);
		if (peers == null) {
			peers = Collections.synchronizedList(new ArrayList<Peer>());
			indexes.put(fileName, peers);
		}
		if (peers.add(id)) {
			return Message.REGISTRYSUCCESS;
		} else {
			return Message.REGISTRYFAILD;
		}
	}

	/**
	 * delete a sharing peer of fileName
	 * 
	 * @param id
	 *            Peer which including IP address and socket port
	 * @param fileName
	 *            the name of file
	 * @return result success or failed
	 */
	public synchronized String deleteIndex(Peer id, String fileName) {
		List<Peer> peers = indexes.get(fileName);
		if (peers == null) {
			peers = Collections.synchronizedList(new ArrayList<Peer>());
			indexes.put(fileName, peers);
		}
		if (peers.remove(id)) {
			return Message.REGISTRYSUCCESS;
		} else {
			return Message.REGISTRYFAILD;
		}
	}

	/**
	 * search peers which share the file with the fileName
	 * 
	 * @param fileName
	 * @return
	 */
	public ArrayList<Peer> search(String fileName) {
		ArrayList<Peer> owners = new ArrayList<Peer>();
		for (Peer peer : indexes.get(fileName))
			owners.add(peer);
		return owners;
	}

	/**
	 * indexServer register its indexService
	 */
	public void registryIndexService() {
		Server server = new RPC.RPCIndexServer(indexServerPort);
		System.out.println("register service: " + IndexServerService.class.getName());
		server.registerService(IndexServerService.class.getName(), new IndexServerServiceImpl());
		server.start();
	}

	/**
	 * the entrance of this indexServer
	 * 
	 * @param port
	 *            the socket port of indexServer, default is 4444
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			if (args[0].equals("-h") || args[0].equals("--help")) {
				System.out.println("USAGE:java IndexServe.jar [port]");
				System.out.println("	[port] is the port of index server,default is 4444");
				System.exit(0);
			}
		}

		if (args.length == 0) {
			IndexServer indexServer = IndexServer.getIndexServer();
			indexServer.registryIndexService();
		} else if (args.length == 1) {
			int port = Integer.parseInt(args[0]);
			IndexServer indexServer = IndexServer.getIndexServer(port);
			indexServer.registryIndexService();
		}
	}

	public int getIndexServerPort() {
		return indexServerPort;
	}

	public void setIndexServerPort(int indexServerPort) {
		this.indexServerPort = indexServerPort;
	}

}
