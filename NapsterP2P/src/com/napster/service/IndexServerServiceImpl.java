package com.napster.service;

import java.util.ArrayList;

import com.napster.indexserver.IndexServer;
import com.napster.util.Peer;

/**
 *
 * The class implements the service of IndexServerService can provide
 */
public class IndexServerServiceImpl implements IndexServerService {
	private IndexServer indexServer = IndexServer.getIndexServer();

	@Override
	public String registry(Peer id, String fileName) {
		return indexServer.registry(id, fileName);
	}

	@Override
	public ArrayList<Peer> search(String fileName) {
		return indexServer.search(fileName);
	}

	@Override
	public String delete(Peer id, String fileName) {

		return null;
	}

}
