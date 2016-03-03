package napster.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import napster.server.IndexServer;

/**
 * the implementation of indexService
 *
 */
public class IndexServiceImpl extends UnicastRemoteObject implements IndexService {
	// index server
	private IndexServer indexServer = null;

	public IndexServiceImpl(IndexServer indexServer) throws RemoteException {
		this.indexServer = indexServer;
	}

	private static final long serialVersionUID = -4703041478460087258L;

	@Override
	public boolean put(String key, String value) throws RemoteException {
		if (indexServer.getIndexes().containsKey(key))
			return false;
		else {
			indexServer.getIndexes().put(key, value);
			return true;
		}
	}

	@Override
	public String get(String key) throws RemoteException {
		return indexServer.getIndexes().get(key);
	}

	@Override
	public boolean del(String key) throws RemoteException {
		if (indexServer.getIndexes().containsKey(key)) {
			indexServer.getIndexes().remove(key);
			return true;
		} else {
			return false;
		}
	}

}
