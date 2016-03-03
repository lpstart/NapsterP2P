package com.napster.service;

/**
 * The interface of server in this project
 */
public interface Server {
	/**
	 * stop the server
	 */
	public void stop();

	/**
	 * start the server
	 */
	public void start();

	/**
	 * register a service on this server
	 * 
	 * @param serviceName
	 *            the name of service
	 * @param serviceImpl
	 *            the provider of corresponding service
	 */
	public void registerService(String serviceName, Object serviceImpl);

	/**
	 * the state of this server
	 * 
	 * @return true:It's running; false:It's stopped.
	 */
	public boolean isRunning();

	/**
	 * get the server's socket port
	 * 
	 * @return
	 */
	public int getPort();
}
