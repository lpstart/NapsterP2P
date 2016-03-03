package napster.server;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import napster.service.IndexService;
import napster.util.HashUtil;
import napster.util.PeerIdentify;

/**
 * the class peerClient
 *
 */
public class PeerClient {
	// index service of this peer
	private IndexServer indexServer = null;
	// hash tool
	private HashUtil<PeerIdentify> hashUtil = null;

	public PeerClient(String configureFilePath, String numStr) throws Exception {
		List<PeerIdentify> servers = new ArrayList<>();
		Scanner scanner = new Scanner(new File(configureFilePath));
		int postion = Integer.parseInt(numStr);
		int indexServerPort = -1;
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("\t");
			servers.add(new PeerIdentify(line[0], line[1]));
		}
		scanner.close();
		hashUtil = new HashUtil<>(servers);

		try {
			indexServerPort = servers.get(postion - 1).getPort();
			indexServer = new IndexServer(indexServerPort);
			indexServer.setDaemon(true);
			indexServer.start();
		} catch (Exception e) {
			throw new Exception("Start indexServer failed!");
		}

	}

	public IndexService getIndexService(String indexServerIPAddress, int indexServerPort)
			throws MalformedURLException, RemoteException, NotBoundException {
		// get the index service through RMI
		IndexService indexService = (IndexService) Naming
				.lookup("rmi://" + indexServerIPAddress + ":" + indexServerPort + "/indexservice");
		return indexService;
	}

	/**
	 * put key:value to network
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean put(String key, String value) {
		boolean flag = false;
		// get peers on this network by hash tool
		List<PeerIdentify> destPeer = hashUtil.getShardInfo(key);
		for (PeerIdentify destPeerTemp : destPeer) {
			System.out.println(
					"put " + key + ":" + value + " on " + destPeerTemp.getAddress() + ":" + destPeerTemp.getPort());
			IndexService indexService;
			try {
				// get the RMI server
				indexService = getIndexService(destPeerTemp.getAddress().getHostAddress(), destPeerTemp.getPort());
				// put
				flag = flag || indexService.put(key, value);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}

	/**
	 * get key from network
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		String value = null;
		// get peers on this network by hash tool
		List<PeerIdentify> destPeer = hashUtil.getShardInfo(key);
		for (PeerIdentify destPeerTemp : destPeer) {
			System.out.println("get " + key + " from " + destPeerTemp.getAddress() + ":" + destPeerTemp.getPort());
			IndexService indexService;
			try {
				// get the RMI server
				indexService = getIndexService(destPeerTemp.getAddress().getHostAddress(), destPeerTemp.getPort());
				// get
				value = indexService.get(key);
				if (value != null)
					break;
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	/**
	 * delete key from network
	 * 
	 * @param keyStr
	 * @return
	 */
	public boolean del(String keyStr) {
		boolean flag = false;
		// get peers on this network by hash tool
		List<PeerIdentify> destPeer = hashUtil.getShardInfo(keyStr);
		for (PeerIdentify destPeerTemp : destPeer) {
			System.out.println("del " + keyStr + " from " + destPeerTemp.getAddress() + ":" + destPeerTemp.getPort());
			IndexService indexService;
			try {
				// get the RMI server
				indexService = getIndexService(destPeerTemp.getAddress().getHostAddress(), destPeerTemp.getPort());
				// del
				flag = flag || indexService.del(keyStr);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}

	/**
	 * test the put function
	 * 
	 * @param timesStr
	 */
	public void putNtimes(String timesStr) {
		Random random = new Random();
		int times = Integer.parseInt(timesStr);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			put("helloworld" + random.nextInt(times), "helloworld");
		}
		System.out.println("It takes " + (System.currentTimeMillis() - startTime) / 1000f + "s");
		System.out.println("average time is " + (System.currentTimeMillis() - startTime) / 1000f / times + "s");
	}

	/**
	 * test the get function
	 * 
	 * @param timesStr
	 */
	public void getNtimes(String timesStr) {
		put("helloworld", "helloworldvalue");
		int times = Integer.parseInt(timesStr);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			get("helloworld");
		}
		System.out.println("It takes " + (System.currentTimeMillis() - startTime) / 1000f + "s");
		System.out.println("average time is " + (System.currentTimeMillis() - startTime) / 1000f / times + "s");
	}

	/**
	 * test the delete function
	 * 
	 * @param timesStr
	 */
	public void delNtimes(String timesStr) {
		int times = Integer.parseInt(timesStr);
		for (int i = 0; i < times; i++) {
			put("helloworld", "helloworldvalue");
		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			del("helloworld");
		}
		System.out.println("It takes " + (System.currentTimeMillis() - startTime) / 1000f + "s");
		System.out.println("average time is " + (System.currentTimeMillis() - startTime) / 1000f / times + "s");
	}

	/**
	 * test put, get, delete function
	 * 
	 * @param timesStr
	 */
	public void putGetDelNtimes(String timesStr) {
		int times = Integer.parseInt(timesStr);
		ArrayList<String> putKeys = new ArrayList<>();
		Random random = new Random();
		for (int i = 0; i < times; i++) {
			putKeys.add(random.nextInt(times * 100) + "helloworld" + random.nextInt(times));
		}

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			put(putKeys.get(i), "helloworldvalue" + i);
		}
		System.out
				.println("put " + timesStr + " times takes " + (System.currentTimeMillis() - startTime) / 1000f + "s");
		System.out.println("average time is " + (System.currentTimeMillis() - startTime) / 1000f / times + "s");

		startTime = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			get(putKeys.get(i));
		}
		System.out
				.println("get " + timesStr + " times takes " + (System.currentTimeMillis() - startTime) / 1000f + "s");
		System.out.println("average time is " + (System.currentTimeMillis() - startTime) / 1000f / times + "s");

		startTime = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			del(putKeys.get(i));
		}
		System.out
				.println("del " + timesStr + " times takes " + (System.currentTimeMillis() - startTime) / 1000f + "s");
		System.out.println("average time is " + (System.currentTimeMillis() - startTime) / 1000f / times + "s");
	}

	/**
	 * the entrance of peerClient
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		PeerClient pc = null;
		boolean run = true;
		String choose = "";

		String confFile = "./servers/servers";
		String numStr = null;
		for (int i = 0, len = args.length; i < len; i++) {
			if (args[i].equals("-conf") && (i + 1) < len) {
				confFile = args[i + 1];
			} else if (args[i].equals("-num") && (i + 1) < len) {
				numStr = args[i + 1];
			}
		}
		if (numStr == null) {
			System.out.println("USAGE:java PeerClient.jar [-conf file] -num num");
			System.out.println("  -conf file  configure file of server, default is './servers/servers'");
			System.out.println("  -num num  get the position of this server's configuration in configure file");
			System.exit(1);
		}
		pc = new PeerClient(confFile, numStr);

		String keyStr = null;
		String valueStr = null;
		String timesStr = null;
		Scanner sc = new Scanner(System.in);
		while (run) {
			printMenu();
			choose = sc.nextLine();
			switch (choose) {
			case "1":
				// put on network
				System.out.println("please input key:");
				keyStr = sc.nextLine();
				System.out.println("please input value:");
				valueStr = sc.nextLine();
				System.out.println(pc.put(keyStr, valueStr));
				break;
			case "2":
				// get from network
				System.out.println("please input key:");
				keyStr = sc.nextLine();
				System.out.println(pc.get(keyStr));
				break;
			case "3":
				// delete from network
				System.out.println("please input key:");
				keyStr = sc.nextLine();
				System.out.println(pc.del(keyStr));
				break;
			case "4":
				// test put get del N times with default key and value
				System.out.println("please input times:");
				timesStr = sc.nextLine();
				pc.putGetDelNtimes(timesStr);
				break;
			case "5":
				// test put N times with default key and value
				System.out.println("please input times:");
				timesStr = sc.nextLine();
				pc.putNtimes(timesStr);
				break;
			case "6":
				// test get N times with default key
				System.out.println("please input times:");
				timesStr = sc.nextLine();
				pc.getNtimes(timesStr);
				break;
			case "7":
				// test del N times with default key
				System.out.println("please input times:");
				timesStr = sc.nextLine();
				pc.delNtimes(timesStr);
				break;
			case "8":
				// exit
				run = false;
				sc.close();
				break;
			default:
				System.out.println("choose again,please......");
				break;
			}
		}
		System.exit(0);
	}

	public static void printMenu() {
		System.out.println("**************");
		System.out.println("1.Put the key and the responding value.");
		System.out.println("2.Get the key.");
		System.out.println("3.Delete the key.");
		System.out.println("4.Testing put get del N times with default key and value.");
		System.out.println("5.Testing put N times with key and value.");
		System.out.println("6.Testing get N times with key.");
		System.out.println("7.Testing delete N times with default key.");
		System.out.println("8.exit.");
		System.out.println("**************");
		System.out.println("Please choose:");
	}
}
