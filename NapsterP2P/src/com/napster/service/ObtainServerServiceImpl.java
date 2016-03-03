package com.napster.service;

import java.io.RandomAccessFile;
import com.napster.peer.PeerClient;

/**
 * 
 * The class implements the service of ObtainServerService
 */
public class ObtainServerServiceImpl implements ObtainServerService {

	@Override
	public byte[] obtain(String fileName) {
		RandomAccessFile raf = null;
		try {
			// get the length of this file
			raf = new RandomAccessFile(PeerClient.localFiles.get(fileName), "r");
			long length = raf.length();
			raf.close();

			// return the content
			return obtain(fileName, 0, length);
		} catch (Exception e) {
			System.err.println("Someone want to get " + fileName + ",but there is no this File!");
		}
		return "There is no this File!".getBytes();
	}

	@Override
	public byte[] obtain(String fileName, long position, long length) {
		try {
			// open file
			RandomAccessFile raf = new RandomAccessFile(PeerClient.localFiles.get(fileName), "r");
			// allocate a memory for specified content
			byte[] content = new byte[(int) length];
			// jump the position of start
			raf.seek(position);
			// read the content
			raf.read(content);
			raf.close();
			// return the content
			return content;
		} catch (Exception e) {
			System.err.println("Someone want to get " + fileName + ",but there is no this File!");
		}
		return "There is no this File!".getBytes();
	}

}
