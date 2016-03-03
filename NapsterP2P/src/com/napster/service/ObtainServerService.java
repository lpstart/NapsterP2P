package com.napster.service;

/**
 * services of ObtainServerService can provide
 */
public interface ObtainServerService {
	/**
	 * Provider the service of get the content of a file
	 * 
	 * @param fileName
	 *            the name of file
	 * @return the content of this file
	 */
	public byte[] obtain(String fileName);

	/**
	 * Provider the service of get the specified content of a file
	 * 
	 * @param fileName
	 *            the name of file
	 * @param position
	 *            the start position of specified content in file
	 * @param length
	 *            the length of the specified content
	 * @return the specified content
	 */
	public byte[] obtain(String fileName, long position, long length);
}
