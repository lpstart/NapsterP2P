package com.napster.service;

import java.util.ArrayList;

import com.napster.util.Peer;

/**
 * services of IndexServerService can provide
 */
public interface IndexServerService {
	/**
	 * delete a index from indexServer
	 * 
	 * @param id
	 * @param fileName
	 * @return
	 */
	public String delete(Peer id, String fileName);

	/**
	 * register a index from indexServer
	 * 
	 * @param id
	 *            the obtain server information of this file
	 * @param fileName
	 *            the name of file
	 * @return result of registering
	 */
	public String registry(Peer id, String fileName);

	/**
	 * search obtain servers who can provide the obtain service for the file
	 * 
	 * @param fileName
	 *            the name of file
	 * @return the obtain servers
	 */
	public ArrayList<Peer> search(String fileName);
}
