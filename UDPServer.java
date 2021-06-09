/**
 * CPSC 559 Winter 2021 Project Component ITERATION 2
 * 
 *  FILE: UDPServer.java
 *  DESCRIPTION: 
 *  
 *  - Iteration 2: Responsible for handling UDP requests, snip, peer, and close
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
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class UDPServer {
	DatagramSocket udpSock; // The UDP socket
	int port; //UDP port
	Peer[] peersSent; //List of peers from registry, will be updated
	volatile ConcurrentHashMap<Peer, TimeStamp> receivedUDPPeers = new ConcurrentHashMap<Peer, TimeStamp>(); //key: the peer that sent the list. value: the time the peer sent the list
	String snippetLog = ""; //snippet log for get report
	String receivedUDPPeersLog = ""; //received udp peer log for get report
	String sentUDPPeersLog = ""; //UDP peer log for get report
	
	GroupManagement groupManagement; 
	SnippetManagement snippetManagement;
	SnippetGUI snippetGUI;
	Thread groupManagementThread;
	Thread snippetManagementThread;
	String snippetIPAddress; //Foreign UDP's IP Address
	int snippetPort; //Foreign UDP port
	String timeStamp; //The foreign lamport timestamp
	String snippetMessage; //The foreign snippet message received
	String teamName;
	boolean isStopACKReceived = true;
	
	public UDPServer(int port, String teamName) throws SocketException {
		this.port = port;
		this.udpSock = new DatagramSocket(this.port);
		this.teamName = teamName;
	}
	/*
	 * startGroupManagement()
	 * - Starts the group mangagement thread that takes care of UDP peer messages
	 * 
	 * PARAMETERS
	 * - None
	 * 
	 * RETURNS
	 * - None
	 */
	public void startGroupManagement() throws SocketException, InterruptedException, NumberFormatException, UnknownHostException {  
		System.out.println("UDPServer.java - Starting Group Management thread");
		groupManagement = new GroupManagement(peersSent, getIPAddress(), Integer.parseInt(getPort()), udpSock);
		groupManagementThread = new Thread(groupManagement);
    	groupManagementThread.start();
	}
	/*
	 * startUIandSnippet()
	 * - Starst the snippet management thread that handles UDP snip messages
	 * 
	 * PARAMETERS
	 * - None
	 * 
	 * RETURNS
	 * - None
	 */
	public void startUIandSnippet() throws SocketException, UnknownHostException, InterruptedException {
		
		System.out.println("UDPServer.java - Starting UI and Snipper Thread");
		snippetManagement = new SnippetManagement(peersSent, udpSock, getIPAddress(), Integer.parseInt(getPort()));
		snippetManagementThread = new Thread(snippetManagement);
		snippetManagementThread.start();

		// snippetGUI = new SnippetGUI();
		// snippetGUI.startGUI();
	}
	/*
	 * listen()
	 * - Listens for incming UDP messages
	 * 
	 * PARAMETERS
	 * - None
	 * 
	 * RETURNS
	 * - None
	 */
	public void listen() throws Exception {
		System.out.println("UDPServer.java / listen() - Starting UDP listen()...");
        String msg;
        boolean keepOnListeningForClose = true; //false when close UDP received
        while (keepOnListeningForClose) {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            udpSock.receive(packet);
            msg = new String(packet.getData()).trim();
            //DEBUG
            //System.out.println("UDPServer.java / listen() - msg received: " + msg);
            snippetIPAddress = packet.getAddress().getHostAddress();
            snippetPort = packet.getPort();
            
            if(msg.substring(0, 4).equals("peer")) {
            	String temp = msg.substring(4, msg.length());
            	String[] responseParts = temp.split(":");
            	groupManagement.updatePeerList(peerCreator(responseParts[0], Integer.parseInt(responseParts[1]))); //Must update peer list in group mangement thread
            	snippetManagement.updatePeerList(groupManagement.peersSent); //Update snippet mangement thread's peer list
            	buildUDPLog(responseParts[0], responseParts[1], snippetIPAddress, snippetPort); //Build our receivedUDPPeersLog
            } else if (msg.substring(0, 4).equals("snip")) {
            	snippetManagement.updatePeerList(groupManagement.peersSent); //Update snippet mangement's pperlist, want to work with the most up to date version	
            	String temp = msg.substring(6, msg.length());
            	int timeStampEndIndex = timeStampParser(msg);
    
            	//Lamport timestamp 1xx, annoying but have to take care of it
            	if(timeStampEndIndex == 4) {
            		timeStamp = String.valueOf(msg.charAt(4));
            	} else {
            		timeStamp = msg.substring(4, timeStampEndIndex);
            	}
            	snippetMessage = temp;
            	//Debug
//            	System.out.println("################");
//            	System.out.println("timeStamp: " + timeStamp);
//            	System.out.println("snippetMessage: " + snippetMessage);
//            	System.out.println("snippetIPAddress: " + snippetIPAddress);
//            	System.out.println("snippetPort: " + snippetPort);
//            	System.out.println("getIPAddress(): " + getIPAddress());
//            	System.out.println("getPort(): " + getPort());
//            	System.out.println("################");
            	if(snippetIPAddress.equals(getIPAddress()) && Integer.toString(snippetPort).equals(getPort())){
            		//Do nothing! you sent a snip to yourself. don't store it in snippet log
            	} else {
                	snippetManagement.retrieveSnippet(timeStamp, snippetMessage, snippetIPAddress, snippetPort);
					String currentSnipLog = snippetManagement.printSnippetMessageLog();
                	System.out.println(currentSnipLog); // Debug print to console
					// snippetGUI.setSnippetLog(currentSnipLog);
					// snippetGUI.updateSnippetLog();
					// send iterative snippet log over to the GUI
            	}
            } else if (msg.substring(0, 4).equals("stop")) {
            	System.out.println("UDPServer.java / listen() - Stop received, initiating shut-down procedure...");
            	keepOnListeningForClose = false;
            	if(isStopACKReceived) {
            		isStopACKReceived = false;
                	sendStopACK();
    				// System.out.println("Closing GUI");
    				// snippetGUI.closeGUI();

//                	System.out.println("######## PRINT OUT THAT LIST FOR DEBUGGIN PURPOSES ########");
//                	System.out.println(countLines(receivedUDPPeersLog));
//                	System.out.print(receivedUDPPeersLog);
                	sentUDPPeersLog = groupManagement.getSentUDPPeersLog();           	
//                	System.out.println(countLines(sentUDPPeersLog));
//                	System.out.print(sentUDPPeersLog);          	
                	snippetLog = SnippetManagement.getReceivedUDPMessages();          	
//                	System.out.println(countLines(snippetLog));
//                	System.out.print(snippetLog);
                	
                	groupManagement.killThread(); //Kill off all these threads
                	groupManagementThread.join(); //Safely kill them off
                	System.out.println("Group Mangement Thread successfully stopped");
                	snippetManagement.killThread();
                	snippetManagementThread.join();
                	System.out.println("Snippet Mangement Thread successfully stopped");
                	continue;
            	} else {
            		continue;
            	}
            }
            buf = new byte[256]; //Clear the buf byte[] for next incming UDP message
        }
        udpSock.close();
	}
	/*
	 * buildUDPLog()
	 * - Builds the receiveUDPPeersLog for get report when we reconnect back to the registry
	 */
	public void buildUDPLog(String ipAddress, String port, String recipientIPAddress, int recipientPort) {
		String[] dateTime   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).split("[-]|[ ]|[:]");
		TimeStamp timeStamp = new TimeStamp(dateTime[0],dateTime[1],dateTime[2],dateTime[3],dateTime[4],dateTime[5]);
		String message = ipAddress + ":" + port + " " + recipientIPAddress + ":" + recipientPort + " " + timeStamp.print() + "\n";
		receivedUDPPeersLog = receivedUDPPeersLog + message;
	}
	
	public void sendStopACK() throws IOException {
		byte[] buf;
		String message = "ack" + teamName + "\n"; 
		buf = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(snippetIPAddress), snippetPort);
		udpSock.send(packet);
		System.out.println("UDPServer.java - sendStopACK() - Sending ACK to IP: " + snippetIPAddress + " and port: " + snippetPort + " the message: " + message);
	}
	/*
	 * countLines
	 * - Count how many lines are in the string
	 * 
	 * PARAMETER
	 * - String str: the string to be counted
	 * 
	 * RETURNS
	 * - int: number of lines
	 */
	public static int countLines(String str){
		String[] lines = str.split("\r\n|\r|\n");
		return  lines.length;
	}
	/*
	 * timeStampParser()
	 * - Get the lamport timestamp from the foreign UDP message
	 * - 1xx 
	 */
	public int timeStampParser(String theString) {
		int indexTracker = 4;
		while(true) {
			if(Character.isDigit(theString.charAt(indexTracker))) {
				//So index at 4 is a integer, check 
				indexTracker = indexTracker + 1;
			} else {
				return indexTracker;
			}
		}
	}
	public Peer peerCreator(String address, int port) {
		Peer peerTemp = new Peer();
		peerTemp.address = address;
		peerTemp.port = port;
		return peerTemp;
	}	
	public String getIPAddress() throws UnknownHostException {
		return udpSock.getInetAddress().getLocalHost().getHostAddress();
	}
	public String getPort() {
		return Integer.toString(udpSock.getLocalPort());
	}
	//After getting initial list of peers, this function will be called
	public void setListofPeersFromRegistry(Peer[] peersSent) {
		this.peersSent = peersSent;
	}
	public String getReceivedUDPPeersLog() {
		return receivedUDPPeersLog;
	}
	public String getSentUDPPeersLog() {
		return sentUDPPeersLog;
	}
	public String getSnippetLog() {
		return snippetLog;
	}
}