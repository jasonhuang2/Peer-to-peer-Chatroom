/**
 * CPSC 559 Winter 2021 Project Component ITERATION 2
 * 
 *  FILE: SnippetManagement.java
 *  DESCRIPTION: 
 *  
 *  - Iteration 2: Responsible for handling UDP requests, snip
 *  
 *  
 * @author Jason Huang 
 * @UCID 10149037
 * 
 */
package registry;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class SnippetManagement implements Runnable{
	Peer[] peersSent;
	boolean running = true;
	String message;
	
	String ipAddress;
	int port;
	
	int timeStamp = -1;
	String snippetMessage;
	String snippetIPAddress;
	int snippetPort;
	SnippetGUI snippetGUI;
	static String receivedUDPMessages = "";
	
	static String snippetMessageLog = "";
	DatagramSocket udpSock;
	
	Scanner userInput = new Scanner(System.in);
	
	public SnippetManagement(Peer[] peersSent, DatagramSocket udpSock, String snippetIPAddress, int snippetPort) throws SocketException {
		this.peersSent = peersSent;
		this.udpSock = udpSock;
		this.snippetIPAddress = snippetIPAddress;
		this.snippetPort = snippetPort;
	}

	public void run() {
		try {
			while(hasNextLine()) {	// Extremely important line. Thread will not be killed because it's being hung on scanner. 
				String userInputMessage = "";
				userInputMessage = userInput.nextLine();
				//BASE CASE: If user enters "enter" key, basically a empty input message
				if(userInputMessage.equals("")) {
					continue;
				} 
				userInputMessage = snippetLimiter(userInputMessage); //Limit to 14 ASCII characters		
				timeStamp = timeStamp + 1;
				String message = "";
				message = message + "snip" + Integer.toString(timeStamp) + " ";
				message = message + userInputMessage;
				message = message + "\n";
				snippetMessageLog = snippetMessageLog + Integer.toString(timeStamp) + " " + 
				userInputMessage + " " + snippetIPAddress + ":" + Integer.toString(snippetPort) + "\n";
				//System.out.println("SnippetManagement.java - " + snippetMessageLog);
				System.out.println(snippetMessageLog);
				sendMessage(message);	
			}
			userInput.close();
		} catch (Exception e) {
			System.out.println("SnippetManagement.java - Multithreading Exception caught");
			e.printStackTrace();
		}
	}

	/*
	 * hasNextLine()
	 * - Checks to see if there's anything in the scanner. If thread is trying to be killed, it will hang 
	 * on scanner which is bad. 
	 * 
	 * PARAMETER
	 * - None
	 * 
	 * RETURNS
	 * - None
	 */
	public boolean hasNextLine() throws IOException {
	    while (System.in.available() == 0) {
	        if (running == false) {
	            return false;
	        }
	    }
	    return userInput.hasNextLine();
	}
	/*
	 * retrieveSnippet()
	 * - Function responsible for recording snippets from foreign peers. Called from UDPServer.java
	 * 
	 * PARAMETERS: 
	 * - String timeStamp: the foreign lamport timestamp
	 * - String snippetMessage: the foreign message
	 * - String snippetIPAddress: the foreign UDP IP Address that sent the snippet
	 * - int snippetPport: the foreign UDP port that sent the snippet
	 * 
	 * RETURNS
	 * - None
	 * 
	 */
	public void retrieveSnippet(String timeStamp, String snippetMessage, String snippetIPAddress, int snippetPort) {
		updateTimeStamp(timeStamp);
		this.snippetMessage = snippetMessage;
		this.snippetIPAddress = snippetIPAddress;
		//this.snippetPort = snippetPort;
	
		String message = "";
		message = message + timeStamp + " " + snippetMessage + " " + snippetIPAddress + ":" + Integer.toString(snippetPort) + "\n";
		
		snippetMessageLog = snippetMessageLog + message; //Update our logs for get report
		receivedUDPMessages = receivedUDPMessages + message; //Update our logs to be sent to get report
	}
	public static String getReceivedUDPMessages() {
		return snippetMessageLog;
	}
	/*
	 * updateTimeStamp()
	 * - Update our own lamport timestamp. If foreign timestamp is greater than ours, update ours to foreign timestamp + 1
	 */
	public void updateTimeStamp(String timeStampSnip) {
		if(Integer.valueOf(timeStampSnip) > timeStamp) {
			timeStamp = Integer.valueOf(timeStampSnip);
		}
	}
	public String printSnippetMessageLog() {
		return snippetMessageLog;
	}
	/*
	 * Updates peerlist with the latest peer list from GroupManagement thread
	 */
	public void updatePeerList(Peer[] peersSent) {
		this.peersSent = peersSent;
	}
	/*
	 * Set our server's IP Address
	 */
	public void setIPAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	/*
	 * Set our server's UDP port
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/*
	 * sendMessage()
	 * - Sends the message to all peers in the peer list
	 * 
	 * PARAMETERS
	 * - String message: the message to be sent
	 * 
	 * RETURN
	 * - None
	 */
	public void sendMessage(String message) throws IOException {
		byte[] buf;
		String udpIPAddress;
		int udpPort;
		buf = message.getBytes();
		for(int i = 0; i < peersSent.length; i++) {
			udpIPAddress = peersSent[i].address;
			udpPort = peersSent[i].port;
			DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(udpIPAddress), udpPort);
			udpSock.send(packet);
		}
	}
	/*
	 * Limit the number of snip messages to 14 ASCII characters
	 */
	public String snippetLimiter(String userInput) {
		if(userInput.length() > 24) {
			return userInput.substring(0, 20);
		} else {
			return userInput;
		}
	}
	/*
	 * Kill off the thread
	 */
	public void killThread() {
		running = false;
	}
}