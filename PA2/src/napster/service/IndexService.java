package napster.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * Index service
 */
public interface IndexService extends Remote {
	/**
	 * put key:value on network
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @throws RemoteException
	 */
	public boolean put(String key, String value) throws RemoteException;

	/**
	 * get the value of key from network
	 * 
	 * @param key
	 * @return
	 * @throws RemoteException
	 */
	public String get(String key) throws RemoteException;

	/**
	 * delete the key from network
	 * 
	 * @param key
	 * @return
	 * @throws RemoteException
	 */
	public boolean del(String key) throws RemoteException;
}
