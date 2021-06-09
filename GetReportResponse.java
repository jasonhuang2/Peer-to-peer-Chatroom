/**
 * CPSC 559 Winter 2021 Project Component ITERATION 2
 * 
 *  FILE: GetReportResponse.java
 *  DESCRIPTION: 
 *  - Responsible for answering the get report request from registry
 *  - Please look at iteration 1, and 2 for update
 *  
 *  
 * @author Jason Huang 
 * @UCID 10149037
 * 
 * @co-author Thaddeus Chong
 * @UCID 30021830
 * 
 */
package registry;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class GetReportResponse {
	Socket sock;
	BufferedWriter writer;
	Peer[] peersSent = null; //Peers received from registry
	ConcurrentHashMap<Peer, Peer[]> peerMap = new ConcurrentHashMap<Peer, Peer[]>(); //key: the peer that sent the list of peers. value: the list of peers
	Peer[] listofContributers = null; //list of peers that contributed to peersSent list
	ConcurrentHashMap<Peer, TimeStamp> peerTimeMap = new ConcurrentHashMap<Peer, TimeStamp>(); //key: the peer that sent the list. value: the time the peer sent the list
	String receivedUDPPeersLog = ""; //The peers that are received via UDP
	String sentUDPPeersLog 	   = ""; //The peer messages sent via UDP
	String snippetLog 		   = ""; //All snippet (chat log) 
	
	public GetReportResponse(Socket sock, BufferedWriter writer, Peer[] peersSent, Peer[] listofContributers, ConcurrentHashMap<Peer, Peer[]> peerMap, ConcurrentHashMap<Peer, TimeStamp> peerTimeMap, String receivedUDPPeersLog, String sentUDPPeersLog, String snippetLog) {
		this.sock = sock;
		this.writer = writer;
		this.peersSent = peersSent;
		this.listofContributers = listofContributers;
		this.peerMap = peerMap;	
		this.peerTimeMap = peerTimeMap;
		this.receivedUDPPeersLog = receivedUDPPeersLog;
		this.sentUDPPeersLog = sentUDPPeersLog;
		this.snippetLog = snippetLog;
	}
	/*
	 * sendCode()
	 * - Sends the code once "get report" is received from TCP registry
	 * 
	 * PARAMETERS:
	 * - None
	 * 
	 * RETURNS:
	 * - None 
	 */
	public void sendCode() throws IOException {	
		writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
	
		//BASE CASE: If the peer list is empty, we must return it
		if(peersSent == null) {
			peersSent = new Peer[0];
			String message = "";
			peersSent = new Peer[0];
			message = message + "0" + "\n";
			message = message + "0" + "\n";
			System.out.println("about to send report");
			writer.write(message);
			writer.flush();
			System.out.println(message);
		} else {
			// Continue report for iteration 2 ::= 
			// <report response> ::= <peer list><peer list sources><peers recd><peers sent><snippet list>
			String message = "";

			// <numOfPeers>
			message = message + String.valueOf(peersSent.length) + "\n";

			// <peers>
			for(int i = 0; i < peersSent.length; i++) {
				message = message + printPeer(peersSent[i]) + "\n";
			}

			// <numOfSources>
			message = message + String.valueOf(listofContributers.length) + "\n";

			// <sources>
			for(Peer eachPeer : listofContributers) {
				message = message + printPeer(eachPeer) + "\n";			// <source location>
				message = message + peerTimeMap.get(eachPeer).print();  // <date>
				message = message + "\n";

				Peer[] peerList = peerMap.get(eachPeer);				
				message = message + peerList.length + "\n";				// <numOfPeers>
				for (int i = 0; i < peerList.length; i++) {				// <peers>
					message = message + printPeer(peerList[i]) + "\n";
				}
			}

			// <peers recd>
			message = message + String.valueOf(countLines(receivedUDPPeersLog)) + "\n";
			message = message + receivedUDPPeersLog;

			// <peers sent>
			message = message + String.valueOf(countLines(sentUDPPeersLog)) + "\n";
			message = message + sentUDPPeersLog;

			// <snippet list>
			message = message + String.valueOf(countLines(snippetLog)) + "\n";
			message = message + snippetLog;

			//DEBUG
//			System.out.println("####################################################");
//			System.out.print(message);
			System.out.println("about to send report");
			writer.write(message);
			writer.flush();
		}
	}
	/*
	 * printPeer() Function
	 * You cannot print variable type peer because you'll get a null.
	 * You can access its .address() (String) and .port() (int)
	 * 
	 * PARAMETERS:
	 * - Peer peer: The peer object itself
	 * 
	 * RETURNS:
	 * - String: The peer address and port as a string 
	 * 
	 */
	public String printPeer(Peer peer) {
		return (peer.address + ":" + peer.port);
	}

	/*
	 * countLines()
	 * - Basically counts the number of lines in a string
	 * 
	 * PARAMETERS
	 * - String str: The String to be counted
	 * 
	 * RETURNS
	 * - int: The number of lines in the string parameter
	 */
	public static int countLines(String str){
		String[] lines = str.split("\r\n|\r|\n");
		return  lines.length;
	}
}