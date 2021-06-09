/**
 * CPSC 559 Winter 2021 Project Component ITERATION 2
 * 
 *  FILE: GroupManagement.java
 *  DESCRIPTION: 
 *  - Iteration 2: Responsible for handling the UDP peer messages. 
 *  - Adds peers into peer list 
 *  - Randomly chooses a peer and sends it to every peer in the list 
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

//REFERENCE: https://www.baeldung.com/udp-in-java
public class GroupManagement implements Runnable {
	volatile Peer[] peersSent;
	volatile ConcurrentHashMap<Peer, TimeStamp> peersReceived = new ConcurrentHashMap<Peer, TimeStamp>(); //key: the peer that sent the list. value: the time the peer sent the list

	String chosenUDPIPAddress; //The randomly chosen UDP IP Address to be sent to everyone in the peer list
	String chosenUDPPort; //The random chosen UDP port to be sent to everyone in the peer list
	String sentUDPPeersLog = ""; //Message used to build the get report. Peers that were received via UDP
	Peer randomlyChosenPeer; // The randomly chosen peer
	int port; //OUR UDP server's port
	String ipAddress; //UDP server's IP Address
	final boolean DEBUG = true; //Prod of test. DEBUG == true, send UDP peer messages every 5 seconds, else every 30 seconds
	boolean running = true; //Required for thread to start
	DatagramSocket udpSock;

	public GroupManagement(Peer[] peersSent, String ipAddress, int port, DatagramSocket udpSock) throws SocketException {
		this.peersSent = peersSent;
		this.port = port;
		this.udpSock = udpSock;
		this.ipAddress = ipAddress;
	}
	/*
	 * TODO: Randomly choose UDP IP Address and Port from peersSent Peer[]
	 * TODO: Send <peer><peer info> to chosen UDP IP Address and Port with a random interval between 60 seconds (prod) 
	 * 5 secs (test) MORE INFO: https://d2l.ucalgary.ca/d2l/le/358021/discussions/threads/1268001/View
	 * TODO: Mark peer inactive after 3 to 5 mins of inactivity
	 * 
	 * TODO: UDPServer.java will listen for incoming <peer><peer info> messages. Update peersSent[] 
	 * 
	 * 
	 */
	/*
	 * run()
	 * When thread starts, this starts. Responsible for managing UDP peer messages. Once kill() function
	 * is called, running will be false and function exits
	 * 
	 * PARAMETERS
	 * - NONE
	 * 
	 * RETURNS
	 * - NONE
	 * 
	 */
	public void run() {
		try {
			while(running) {
				randomlyChosenPeer = chooseRandomPeer();
				chosenUDPIPAddress = randomlyChosenPeer.address;
				chosenUDPPort = Integer.toString(randomlyChosenPeer.port);
				
				String message = "";
				message = message + "peer";
				message = message + chosenUDPIPAddress + ":" + chosenUDPPort;
				message = message + "\n";
				
				//DEBUG
//				System.out.println("GroupManagement.java / run() - The message: " + message);
//				System.out.println("GroupManagement.java / run() - Sending...");
//				Send message using UDP to randomly chosen targeted UDP IP and Port 
//				byte[] buf;
//				buf = message.getBytes();
//				DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(chosenUDPIPAddress), Integer.parseInt(chosenUDPPort));
//				udpSock.send(packet);
				sendMessage(message);
				buildUDPLog(chosenUDPIPAddress, chosenUDPPort, ipAddress, port); //Build the UDP received log for get report
				//DEBUG
//				System.out.println("GroupManagement.java / run() - Length of peersSent Peer[] is: " + peersSent.length);
//				for(int i = 0; i < peersSent.length; i++) {
//					System.out.println(printPeer(peersSent[i]));
//				}
				if(DEBUG) {
					Thread.sleep(5000);
				} else {
					Thread.sleep(30000);
				}
			}	
		} catch (Exception e) {
			System.out.println("EXCEPTION ERROR CAUGHT IN GroupManagement.java - run()");
			e.printStackTrace();
		}	
	}
	/*
	 * buildUDPLog function
	 * 
	 * - Responsible for building the UDP log for our get report
	 * 
	 * PARAMETERS
	 * - String ipAddress: The IP address of the peer that was received from foreign peer
	 * - String port: The port of the peer that was received from foreign peer
	 * - String recipientIPAddress: The IP address of the foreign peer that supplied the information of ipAddress and port
	 * - int recipientPort: The port of the foreign peer that supplied the information of ipAddress and port
	 * 
	 * RETURN
	 * - None
	 */
	public void buildUDPLog(String ipAddress, String port, String recipientIPAddress, int recipientPort) {
		String[] dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).split("[-]|[ ]|[:]");
		TimeStamp timeStamp = new TimeStamp(dateTime[0],dateTime[1],dateTime[2],dateTime[3],dateTime[4],dateTime[5]);
		
		String message = ipAddress + ":" + port + " " + recipientIPAddress + ":" + recipientPort + " " + timeStamp.print() + "\n";
		sentUDPPeersLog = sentUDPPeersLog + message;
	}
	/*
	 * getSEntUDPPeersLog()
	 * - get function for retrieving the sent UDP peer log
	 * 
	 * PARAMETERS
	 * - NONE
	 * 
	 * RETURNS
	 * - String
	 */
	public String getSentUDPPeersLog() {
		return sentUDPPeersLog;
	}	
	/*
	 * sendMessage()
	 * - Responsible for sending peer messages to ALL peers in the Peer list
	 * 
	 * PARAMETERS
	 * - String message: The peer message
	 * 
	 * RETURNS
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
	 * killThread()
	 * - Kills the thread
	 * 
	 * PARAMETERS
	 * - None
	 * 
	 * RETURNS
	 * - None
	 */
	public void killThread() {
		running = false;
	}
	/*
	 * updatePeerList()
	 * - Updates the peer list
	 * 
	 * PARAMETERS
	 * - Peer peer: the peer to be included
	 * 
	 * RETURNS
	 * - NONE
	 */
	public void updatePeerList(Peer peer) {
		if(checkDuplicate(peer)) {
			//DUPLICATE IS DETECTED, DON'T UPDATE PEERLIST
		} else {
			System.out.println("Peer added: " + printPeer(peer));
			peersSent = updatePeerArray(peer, peersSent);	
			String[] dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).split("[-]|[ ]|[:]");
			peersReceived.put(peer, new TimeStamp(dateTime[0],dateTime[1],dateTime[2],dateTime[3],dateTime[4],dateTime[5]));			
			//Debug
//			for (Peer eachPeer : peersReceived.keySet()) {
//				System.out.println("key(): " + eachPeer.address + ":" + eachPeer.port + " and the time: " + peersReceived.get(eachPeer).print());
//			}	
		}
	}
//	public ConcurrentHashMap<Peer, TimeStamp> getListOfReceivedPeers() {
//		return peersReceived;
//	}
	/*
	 * checkDuplicate()
	 * - Checks to see if a duplicate exists in the peer list or not 
	 * 
	 * PARAMETERS
	 * - Peer peer: The peer to be checked 
	 * 
	 * RETURNS
	 *  - TRUE: if a duplicate is detected
	 *  - FALSE: if no duplicates are detected
	 * 
	 */
	public boolean checkDuplicate(Peer peer) {
		for(int i = 0; i < peersSent.length; i++) {
			String address = peersSent[i].address;
			int port = peersSent[i].port;
			
			if(address.equals(peer.address)) {
				if(port == peer.port) {
					return true;
				}
			}
		}
		return false;
	}
	/*
	 * chooseRandomPeer()
	 * - Chooses a random peer to be spammed to other peers in the peer list
	 * 
	 * PARAMETERS:
	 * - None
	 * 
	 * RETURNS:
	 * - None
	 * 
	 */
	public Peer chooseRandomPeer() {
		int randNum = randomNumberGenerator(peersSent.length);
		// DEBUG
		// System.out.println("GroupManagement.java / chooseRandomPeer() - random number chosen: " + randNum);
		// System.out.println("GroupManagement.java / chooseRandomPeer() - length of peersSent[] " + peersSent.length);
		// System.out.println("GroupManagement.java / chooseRandomPeer() - Peer: " + printPeer(peersSent[randNum]));
		return peersSent[randNum];
	}
	/*
	 * randomNumberGenerator()
	 * - Generates a random number 
	 * 
	 * PARAMETERS: 
	 * - int setSize: The max bound
	 * 
	 * RETURNS
	 * - int: randomly chosen number
	 */
	public int randomNumberGenerator(int setSize) {
		return ThreadLocalRandom.current().nextInt(0, setSize);
	}
	/*
	 * printPeer()
	 * 
	 * Prints the peer
	 */
	public String printPeer(Peer peer) {
		return (peer.address + ":" + peer.port);
	}
	/*
	 * printPeerArray()
	 * 
	 * Prints the peer array
	 */
	public void printPeerArray() {
		for(int i = 0; i < peersSent.length; i++) {
			System.out.println(printPeer(peersSent[i]));
		}
	}
	/*
	 * updatePeerArray()
	 * Updates the peer array
	 */
	public Peer[] updatePeerArray(Peer peer, Peer[] peersArray) {
		if(peersArray == null) {
			Peer[] peersSentTemp = new Peer[1];
			peersSentTemp[0] = peer;
			return peersSentTemp;
		} 
		Peer[] peersSentTemp = new Peer[peersArray.length + 1];
		for(int i = 0; i < peersArray.length; i++) {
			peersSentTemp[i] = peersArray[i];
		}
		peersSentTemp[peersSentTemp.length-1] = peer;
		return peersSentTemp;
	}
}