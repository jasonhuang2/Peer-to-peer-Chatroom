/**
 * CPSC 559 Winter 2021 Project Component ITERATION 2
 * 
 *  FILE: GetLocationResponse.java
 *  DESCRIPTION: 
 *  - Responsible for handling the "get location" request from the registry
 *  - Send the peer which is the UDP server's IP address and port
 *  
 *  
 * @author Jason Huang 
 * @UCID 10149037
 * 
 */
package registry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class GetLocationResponse {
	BufferedWriter writer;
	Socket sock;
	String udpIPAddress;
	String udpPort;
	/*
	 * Constructor for GetLocationResponse
	 * PARAMETERS:
	 * - Socket sock: The TCP socket that is connected to the registry
	 * - BufferedWriter writer: The writer 
	 * - String udpIPAddress: The IP address for our UDP server
	 * - String udpPport: The port for our UDP server
	 */
	public GetLocationResponse(Socket sock, BufferedWriter writer, String udpIPAddress, String udpPort) {
		this.sock = sock;
		this.writer = writer;
		this.udpIPAddress = udpIPAddress;
		this.udpPort = udpPort;
	}
	/*
	 * sendCode()
	 * Basically this code sends builds out our message and answers the get location response
	 * PARAMETERS:
	 * - NONE
	 * 
	 * RETURN:
	 * - None
	 */
	public void sendCode() throws IOException {
		writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		String message = "";
		//DEBUG
//		System.out.println("udpIPAddress: " + udpIPAddress);
//		System.out.println("udpPort: " + udpPort);
		Peer temp = peerCreator(udpIPAddress, Integer.parseInt(udpPort));
		String locAddress = temp.address;
		int locPort = temp.port;

		message = message + locAddress + ":";
		message = message + locPort;
		message = message + "\n";
		
		//DEBUG
		//System.out.println("Sending the following location: " + message);
		writer.write(message);
		writer.flush();
		System.out.println("GetLocationResponse.java - Code sent");
	}
	/*
	 * peerCreator()
	 * Creates a peer object that takes in address and port
	 * PARAMETERS:
	 * - String address: The IP address
	 * - int port: The port 
	 * 
	 */
	public Peer peerCreator(String address, int port) {
		Peer peerTemp = new Peer();
		peerTemp.address = address;
		peerTemp.port = port;
		return peerTemp;
	}
}