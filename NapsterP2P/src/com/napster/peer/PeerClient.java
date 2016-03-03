package com.napster.peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.napster.rpc.RPC;
import com.napster.service.IndexServerService;
import com.napster.service.ObtainServerService;
import com.napster.service.ObtainServerServiceImpl;
import com.napster.service.Server;
import com.napster.util.Message;
import com.napster.util.Peer;

/**
 * Peer client
 */
public class PeerClient {
	// local file resources
	public static Map<String, String> localFiles;

	static {
		localFiles = Collections.synchronizedMap(new HashMap<String, String>());
	}

	private IndexServerService indexServerService = null;
	// local socket port for sharing service
	private int downloadPort;

	public PeerClient() {
		this("localhost", Message.INDEXSERVERPORT, Message.OBTAINSERVERPORT);
	}

	public PeerClient(String indexServerHostName) {
		this(indexServerHostName, Message.INDEXSERVERPORT, Message.OBTAINSERVERPORT);
	}

	public PeerClient(String indexServerHostName, int serverPort) {
		this(indexServerHostName, serverPort, Message.OBTAINSERVERPORT);
	}

	public PeerClient(String serverHost, int serverPort, int downloadPort) {
		super();
		// initialize the indexServerService with the serverHostName
		this.indexServerService = RPC.getIndexServerServiceProxy(serverHost, serverPort);
		// initialize the obatinServerPort with the downloadPort
		this.downloadPort = downloadPort;

	}

	/**
	 * register its sharing service
	 */
	public void registryObatinService() {
		// register a obtainService with downloadPort and start the server
		Server obatinServer = new RPC.RPCObatinServer(downloadPort);
		obatinServer.registerService(ObtainServerService.class.getName(), new ObtainServerServiceImpl());
		obatinServer.start();
	}

	/**
	 * register a file resource on indexServer
	 * 
	 * @param fileName
	 *            the name will hand in to indexServer
	 * @param localPath
	 *            the local path of fileName for other peers downloading
	 * @throws UnknownHostException
	 */
	public void registry(String fileName, String localPath) throws UnknownHostException {
		File localFile = new File(localPath);

		if (localFile.exists()) {
			// maintain local relation of shared file and local fileSystem
			localFiles.put(fileName, localPath);
			// register on indexServer
			String result = indexServerService.registry(new Peer(InetAddress.getLocalHost(), downloadPort), fileName);

			System.out.println("register " + fileName + " result:" + result);
		}
	}

	/**
	 * search a file resource on indexServer
	 * 
	 * @param fileName
	 * @return the peer clients which share the file resource
	 */
	public ArrayList<Peer> search(String fileName) {
		return indexServerService.search(fileName);
	}

	/**
	 * search a file resource on indexServer
	 * 
	 * @param fileName
	 * @return the peer clients which share the file resource
	 */
	public void searchAndPrint(String fileName) {
		ArrayList<Peer> peers = indexServerService.search(fileName);
		System.out.println("The peers which include this file are:");
		if (peers != null) {
			for (Peer peer : peers) {
				System.out.println("Peers ip address:" + peer.getInetAddress().toString() + ", port:" + peer.getPort());
			}
		} else {
			System.out.println("Null");
		}

	}

	/**
	 * download the file resource from certain peer
	 * 
	 * @param peer
	 * @param fileName
	 * @return the content of file in byte form
	 */
	public byte[] obtain(Peer peer, String fileName) {
		ObtainServerService obtainServerService = RPC.getObtainServerService(peer);
		return obtainServerService.obtain(fileName);
	}

	/**
	 * download the file resource from certain peer and store it
	 * 
	 * @param peer
	 * @param fileName
	 * @param storeFileStr
	 */
	public void obtainAndStore(Peer peer, String fileName, String storeFileStr) {
		ObtainServerService obtainServerService = RPC.getObtainServerService(peer);
		byte[] result = obtainServerService.obtain(fileName);
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(storeFileStr, "rw");
			raf.write(result);
			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("display file" + storeFileStr);
	}

	/**
	 * test method
	 * 
	 * @throws IOException
	 */
	public void testSearch() throws IOException {
		String registerDirStr = "testFileDir/testRegister1";
		String searchStr = "test4.zip";
		File registerDir = new File(registerDirStr);
		System.out.println("register default dirctory on indexServer.");
		for (File file : registerDir.listFiles()) {
			try {
				registry(file.getName(), file.getAbsolutePath());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		System.out.println("start search test file on indexServer 1000 times...");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			search(searchStr);
		}
		System.out.println("It takes time: " + (System.currentTimeMillis() - start) / 1000f + "s");
	}

	/**
	 * search certain file N times
	 * 
	 * @param searchStr
	 * @param timesStr
	 */
	public void searchNtimes(String searchStr, String timesStr) {
		int times = Integer.parseInt(timesStr);
		System.out.println("start search test file on indexServer...");
		long start = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			search(searchStr);
		}
		System.out.println("It takes time: " + (System.currentTimeMillis() - start) / 1000f + "s");
	}

	/**
	 * the entrance of peerClient
	 * 
	 * @param port
	 *            the socket port for downloading of other peers
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 1) {
			if (args[0].equals("-h") || args[0].equals("--help")) {
				System.out.println(
						"USAGE:java PeerClient.jar [indexServerHostName [indexServerPort [obtainServerPort]]]");
				System.out.println("	[indexServerHostName] is the hostName of index server,default is localhost");
				System.out.println("	[indexServerPort] is the port of index server,default is 4444");
				System.out.println("	[obtainServerPort] is the port of obtain server,default is 4443");
				System.exit(0);
			}
		}

		PeerClient pc = null;
		String registerStr = null;
		String registerLocalPathStr = null;
		String searchStr = null;
		boolean run = true;
		String choose = "";

		if (args.length == 0) {
			// peerClient with default obtain server port:4443 and default
			// IndexServerHostName:localhost and default indexServerPort:4444
			pc = new PeerClient();
		} else if (args.length == 1) {
			// peerClient with default obtain server port:4443 and with the
			// IndexServerHostName from args and default indexServerPort:4444
			pc = new PeerClient(args[0]);
		} else if (args.length == 2) {
			// peerClient with default obtain server port:4443 and with the
			// IndexServerHostName from args and with indexServerPort from args
			int indexServerPort = Integer.parseInt(args[1]);
			pc = new PeerClient(args[0], indexServerPort);
		} else if (args.length == 3) {
			// peerClient with obtain server port from args and with the
			// IndexServerHostName from args and with indexServerPort from args
			int indexServerPort = Integer.parseInt(args[1]);
			int obatinServerPort = Integer.parseInt(args[2]);
			pc = new PeerClient(args[0], indexServerPort, obatinServerPort);
		}
		pc.registryObatinService();

		Scanner sc = new Scanner(System.in);
		while (run) {
			printMenu();
			choose = sc.nextLine();
			switch (choose) {
			case "1":
				// register a file on indexServer
				System.out.println("please input the name on indexServer:");
				registerStr = sc.nextLine();
				System.out.println("please input the file path on local:");
				registerLocalPathStr = sc.nextLine();
				pc.registry(registerStr, registerLocalPathStr);
				break;
			case "2":
				// search a file on indexServer
				System.out.println("please input the file name on indexServer:");
				searchStr = sc.nextLine();
				pc.searchAndPrint(searchStr);
				break;
			case "3":
				// download a file from other peer
				System.out.println("please input the ip address:");
				String ipaddressTemp = sc.nextLine();
				System.out.println("please input the port:");
				String portTemp = sc.nextLine();
				Peer peer = new Peer(ipaddressTemp, portTemp);
				System.out.println("please input the file name:");
				searchStr = sc.nextLine();
				System.out.println("please input the path to store the file:");
				String storeStr = sc.nextLine();
				pc.obtainAndStore(peer, searchStr, storeStr);
				break;
			case "4":
				// search some file on indexServer N times
				System.out.println("please input the file name:");
				searchStr = sc.nextLine();
				System.out.println("please input search times:");
				String timesTemp = sc.nextLine();
				pc.searchNtimes(searchStr, timesTemp);
				break;
			case "5":
				// register the default dirctory on indexServer and search the
				// default file 1000 times
				pc.testSearch();
				break;
			case "6":
				// exit
				run = false;
				sc.close();
				break;
			default:
				System.out.println("choose again,please......");
				break;
			}
		}
		System.exit(1);
	}

	public static void printMenu() {
		System.out.println("**************");
		System.out.println("1.register a file on indexServer.");
		System.out.println("2.search a file on indexServer.");
		System.out.println("3.download a file from other peer.");
		System.out.println("4.search some file on indexServer N times.");
		System.out.println("5.register the default dirctory on indexServer and search the default file 1000 times.");
		System.out.println("6.exit.");
		System.out.println("Please choose:");
	}
}
