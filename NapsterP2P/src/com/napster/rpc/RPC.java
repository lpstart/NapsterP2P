package com.napster.rpc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.napster.service.IndexServerService;
import com.napster.service.ObtainServerService;
import com.napster.service.Server;
import com.napster.util.Message;
import com.napster.util.Peer;
import com.napster.util.RPCBean;

/**
 * 
 * the classes related with PRC
 */
public class RPC {

	/**
	 * get a indexServer instance by RPC
	 * 
	 * @param serverHost
	 *            the host name of server
	 * @return IndexServerService
	 */
	public static IndexServerService getIndexServerServiceProxy(final String serverHost, final int serverPort) {
		InvocationHandler invocationHandler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				try {
					return new RPC.RPCClient(serverHost, serverPort).invoke(IndexServerService.class.getName(), method,
							args);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				return null;
			}
		};

		return (IndexServerService) Proxy.newProxyInstance(RPC.class.getClassLoader(),
				new Class[] { IndexServerService.class }, invocationHandler);
	}

	/**
	 * get a obtainServerService instance by RPC
	 * 
	 * @param peer
	 * @return
	 */
	public static ObtainServerService getObtainServerService(final Peer peer) {
		InvocationHandler invocationHandler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return new RPC.RPCClient(peer.getInetAddress(), peer.getPort())
						.invoke(ObtainServerService.class.getName(), method, args);
			}
		};

		return (ObtainServerService) Proxy.newProxyInstance(RPC.class.getClassLoader(),
				new Class[] { ObtainServerService.class }, invocationHandler);
	}

	/**
	 * RPCClient which is used to invoke certain method of certain class on
	 * server
	 *
	 */
	public static class RPCClient {
		private int port;
		private InetAddress inetAddress;

		public RPCClient() throws UnknownHostException {
			this(InetAddress.getLocalHost(), Message.INDEXSERVERPORT);
		}

		public RPCClient(InetAddress inetAddress) {
			this(inetAddress, Message.INDEXSERVERPORT);
		}

		public RPCClient(String serverHost, int port) throws UnknownHostException {
			this(InetAddress.getByName(serverHost), port);
		}

		public RPCClient(InetAddress inetAddress, int port) {
			super();
			this.inetAddress = inetAddress;
			this.port = port;
		}

		public Object invoke(String serviceName, Method method, Object[] args) {
			try {
				// start a socket for communicating with server
				Socket socket = new Socket(inetAddress, port);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

				RPCBean rb = new RPCBean();
				rb.setAskServiceMethod(method.getName());
				rb.setAskServiceName(serviceName);
				rb.setAskServiceParameters(args);
				rb.setAskServiceParametersClass(method.getParameterTypes());
				String rbJson = JSONObject.toJSONString(rb);

				// send a ask to server
				bw.write(rbJson + System.lineSeparator());
				bw.flush();

				// read a result from server
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				String resultJson = br.readLine();
				Object resultObj = JSON.parseObject(resultJson, method.getGenericReturnType());
				bw.close();
				br.close();
				if (socket != null && !socket.isClosed())
					socket.close();
				// return the result of processing on server
				return resultObj;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	/**
	 * RPCObatinServer which is used to process the asking of certain method of
	 * obtain service from RPCClient
	 */
	public static class RPCObatinServer implements Server {
		private boolean running;
		private int port;
		// the map of the 'object' that provide the service of 'String'
		private HashMap<String, Object> services;

		public RPCObatinServer() {
			this(Message.OBTAINSERVERPORT);
		}

		public RPCObatinServer(int port) {
			super();
			this.port = port;
			this.running = false;
			this.services = new HashMap<String, Object>();
		}

		@Override
		public void stop() {
			this.running = false;
		}

		@Override
		public void start() {
			this.running = true;
			new RPCObtaionServerThread(this).start();
		}

		@Override
		public void registerService(String serviceName, Object serviceImpl) {
			this.services.put(serviceName, serviceImpl);
		}

		@Override
		public boolean isRunning() {
			return this.running;
		}

		@Override
		public int getPort() {
			return port;
		}

	}

	/**
	 * RPCObatinServer's subThread which is used to process the asking of
	 * certain method of obtain service from RPCClient
	 */
	private static class RPCObtaionServerThread extends Thread {
		private RPCObatinServer prcos = null;
		private ServerSocket serverSocket = null;

		public RPCObtaionServerThread(RPCObatinServer prcos) {
			this.prcos = prcos;
		}

		@Override
		public void run() {
			try {
				System.out.println("obtain server listen the port " + prcos.getPort());
				serverSocket = new ServerSocket(prcos.getPort());
				while (prcos.isRunning()) {
					Socket socket = serverSocket.accept();
					System.out.println("someone want to download...");
					new RPC().new ServerListener(socket, prcos.services).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

	}

	/**
	 * RPCIndexServer which is used to process the asking of certain method of
	 * obtain service from RPCClient
	 */
	public static class RPCIndexServer implements Server {
		// the map of the 'object' that provide the service of 'String'
		private HashMap<String, Object> services;
		private boolean running;
		private ServerSocket serverSocket;
		private int port;

		public RPCIndexServer() {
			this(Message.INDEXSERVERPORT);
		}

		public RPCIndexServer(int port) {
			super();
			this.port = port;
			this.running = false;
			this.services = new HashMap<String, Object>();
		}

		@Override
		public void stop() {
			this.running = false;
		}

		@Override
		public void start() {
			this.running = true;
			System.out.println("listen port:" + getPort());
			try {
				serverSocket = new ServerSocket(getPort());
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
			while (isRunning()) {
				Socket socket = null;
				try {
					socket = serverSocket.accept();
					System.out.println("client connected!");
					new RPC().new ServerListener(socket, services).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void registerService(String serviceName, Object serviceImpl) {
			services.put(serviceName, serviceImpl);

		}

		@Override
		public boolean isRunning() {
			return running;
		}

		@Override
		public int getPort() {
			return port;
		}

	}

	/**
	 * ServerListener which is used to process the asking of certain method of
	 * some service from RPCClient
	 */
	private class ServerListener extends Thread {
		private Socket socket;
		private HashMap<String, Object> services;

		public ServerListener(Socket socket, HashMap<String, Object> services) {
			this.socket = socket;
			this.services = services;
		}

		@Override
		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				String askMessage = br.readLine();
				System.out.println("ask:" + askMessage);

				RPCBean rpcBean = JSONObject.parseObject(askMessage, RPCBean.class);
				if (rpcBean != null) {
					String serviceName = rpcBean.getAskServiceName();
					String methodName = rpcBean.getAskServiceMethod();
					Class<?>[] paramsClasses = rpcBean.getAskServiceParametersClass();
					Object[] paramsObjects = rpcBean.getAskServiceParameters();

					if (paramsClasses[0].getName().equals(Peer.class.getName()))
						paramsObjects[0] = JSONObject.parseObject(paramsObjects[0].toString(), paramsClasses[0]);

					if (serviceName != null && methodName != null && paramsObjects != null && paramsClasses != null) {
						// get the instance and method
						Object object = services.get(serviceName);
						Method method = object.getClass().getMethod(methodName, paramsClasses);
						// invoke the method and get the result
						Object result = method.invoke(object, paramsObjects);
						// write the result to remote socket
						String resultJson = JSON.toJSONString(result);
						BufferedWriter bw = new BufferedWriter(
								new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
						bw.write(resultJson + System.lineSeparator());
						bw.flush();
						br.close();
						bw.close();
					} else {
						System.out.println(serviceName + "  method: " + methodName + "  params: " + paramsClasses);
					}
				} else {
					System.out.println("rpcBean is null");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (socket != null && !socket.isClosed())
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
