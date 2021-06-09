/**
 * CPSC 559 Winter 2021 Project Component ITERATION 2
 * 
 *  FILE: Peer.java 
 *  DESCRIPTION: 
 *  - Iteration 1: Peer file responsible for listening to registry at IP Address: 136.159.5.22 at port: 55921
 *  ITERATION 1 will focus on communication with the registry. 
 *  
 *  - Iteration 2: Connect to registry for initial peer list. Start UDP server and listen for peer, snip and 
 *  stop UDP messages. Reconnect to registry using TCP and complete the "get report" request.
 *  
 *  
 * @author Jason Huang 
 * @UCID 10149037
 * 
 */
package registry;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class Peer {
	static String address;
	int port;
	static String teamName;
	
	Peer[] peersSent = null; //Peers received from registry
	ConcurrentHashMap<Peer, Peer[]> peerMap = new ConcurrentHashMap<Peer, Peer[]>(); //key: the peer that sent the list of peers. value: the list of peers
	ConcurrentHashMap<Peer, TimeStamp> peerTimeMap = new ConcurrentHashMap<Peer, TimeStamp>(); //key: the peer that sent the list. value: the time the peer sent the list
	Peer[] listofContributers = null; //list of peers that contributed to peersSent list

	BufferedReader reader;
	BufferedWriter writer;
	
	public final static String[] FILES_TO_SEND_ARRAY = {"GetCodeResponse.java", "GetLocationResponse.java", "GetReportResponse.java", "GetTeamResponse.java", "GroupManagement.java", "SnippetManagement.java", "Peer.java", "TimeStamp.java", "UDPServer.java"};

	UDPServer udpServer;
	boolean beforeReconnectRegistry = true;

	static String rAddress, rPort;
	String udpIPAddress, udpPort;
	/*
	 *  main() Function
	 * Starts the peer process 
	 * 
	 * 
	 */
	public static void main (String[] args) throws NumberFormatException, Exception {
		Peer peer = new Peer();
		try {
			teamName = args[2];
			rAddress = args[0];
			rPort = args[1];
			
			peer.start(args[0], Integer.valueOf(args[1]));
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	/*
	 * start() function
	 * The start function
	 * 
	 * FUNCTIONS:
	 * - String registryAddress: the IP address to connect to the registry
	 * - int registryPort: the port to connect to the registry
	 * 
	 * RETURNS:
	 * - None 
	 * 
	 */
	public void start(String registryAddress, int registryPort) throws Exception {
		Socket sock = new Socket(registryAddress , registryPort);
		System.out.println(key() + " - Registry connected");
		
		//Get first message from registry WHICH IS ALWAYS IT ASKING FOR THE TEAM NAME
		reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		String response = "";
		response = reader.readLine();
		System.out.println(key() + " - RESPONSE FROM REGISTRY: " + response);
		String[] responseComponents = response.split("[ \t]+");
		while(!responseComponents[0].equals("close")) {
			switch (responseComponents[0]) {
			// 'get team name' OR 'get code' OR 'get report' 
			case "get":
				handleGetResponses(response, sock);
				break;
			case "receive":
				handleReceivePeersResponse(sock);
				break;
			}
			//Read next response from registry
			response = "";
			response = reader.readLine();	
			responseComponents = response.split("[ \t]+");
			System.out.println(key() + " - RESPONSE FROM REGISTRY: " + response);

		}
		if(beforeReconnectRegistry) {
			System.out.println(key() + " - Close request received. Finished with registry. Starting collaboration...");
			sock.close();
			udpServer.setListofPeersFromRegistry(peersSent);
			udpServer.startGroupManagement(); //Start the Group Management on Thread 1
			udpServer.startUIandSnippet(); //Start the Snippet Management on Thread 2
			udpServer.listen();
			System.out.println(key() + " - Restarting conection to registry...");
			beforeReconnectRegistry = false;
			start(rAddress, Integer.valueOf(rPort));
		} else {
			System.out.println(key() + " - Close request received. Ending connecting to registry...");
			sock.close();
		}
	}
	/*
	 * handleReceivePeersResponse() Function
	 * 
	 * Handles the 'receive peers' request from the registry. EXAMPLE: 2\n 127.0.0.1\n111.222.333.444
	 * First line indicates how many lines I should read from.
	 * If I read beyond line 3, I'll read 'get report'. That is what variable 'counter' is for.
	 * 
	 * PARAMETERS:
	 * - Socket sock: the TCP socket to send/receive information from/to the registry
	 * 
	 * RETURNS:
	 * - None
	 * 
	 */
	public void handleReceivePeersResponse(Socket sock) throws IOException {		
		System.out.println(key() + " - About to receive peers");

		int numberOfPeersResponse = Integer.valueOf(reader.readLine());
		//DEBUG: System.out.println(numberOfPeersResponse);
		int counter = 0;
		String response = "";

		while(((response = reader.readLine()) != null)) {
			String[] responseParts = response.split(":");
			peersSent = updatePeerArray(peerCreater(responseParts[0], Integer.parseInt(responseParts[1])), peersSent); //Insert peer into Peer[] peersSet array
	
			Peer sourcePeer = peerCreater(sock.getInetAddress().getHostAddress(), sock.getPort());
			
			//map(source of peer list, the LIST OF PEERS given by that source)
			peerMap.put(sourcePeer, peersSent);
			
			String[] dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).split("[-]|[ ]|[:]");
			peerTimeMap.put(sourcePeer, new TimeStamp(dateTime[0],dateTime[1],dateTime[2],dateTime[3],dateTime[4],dateTime[5]));
			
			listofContributers = updatePeerArray(sourcePeer, listofContributers);
			counter = counter + 1;
			if(counter == numberOfPeersResponse) {
				break;
			}
		}
		//DEBUG
//		System.out.println("Length of peersSent Peer[] is: " + peersSent.length);
//		for(int i = 0; i < peersSent.length; i++) {
//			System.out.println(printPeer(peersSent[i]));
//		}
	}
	/*
	 * PeerCreater() Function
	 * Creates a new Peer object with parameters...
	 * 
	 * PARAMETERS:
	 * - String address: The IP address
	 * - int port: The port
	 * 
	 * RETURNS:
	 * - The newly created peer with IP address 'address' and port 'port'
	 * 
	 */
	public Peer peerCreater(String address, int port) {
		Peer peerTemp = new Peer();
		peerTemp.address = address;
		peerTemp.port = port;
		return peerTemp;
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
	 * updatePeerArray() Function
	 * Updates existing Peer[] array by adding an peer to the end 
	 * 
	 * PARAMETERS:
	 * - Peer peer: the peer object to be added to the Peer[] array
	 * - Peer[] peersArray: the Peer array to be updated
	 * 
	 * RETURN:
	 * - Peer[]: The updated Peer[] array 
	 * 
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
	/*
	 * handleGetResponses() Function
	 * Responsible for handing get (X) responses from the registry. 
	 * get team || get code || get report
	 * 
	 * PARAMETERS:
	 * - String response: From the registry. get (team or code or report)
	 * - Socket sock: the TCP connection
	 * 
	 * RETURNS:
	 * None
	 */
	public void handleGetResponses(String response, Socket sock) throws IOException {
		String[] responseComponents = response.split("[ \t]+");
		//'get team name'
		if(responseComponents[1].equals("team")) {
			GetTeamResponse getTeamResponse = new GetTeamResponse(sock, writer, teamName);
			getTeamResponse.sendResponse();			
		} else if(responseComponents[1].equals("code")) {
			//'get code'
			GetCodeResponse getCodeResponse = new GetCodeResponse(sock, writer, FILES_TO_SEND_ARRAY);
			getCodeResponse.sendResponse();
		}
		else if (responseComponents[1].equals("report")) {
			String receivedUDPPeersLog = "";
			String sentUDPPeersLog = "";
			String snippetLog = "";

			receivedUDPPeersLog = udpServer.getReceivedUDPPeersLog();
			sentUDPPeersLog = udpServer.getSentUDPPeersLog();
			snippetLog = udpServer.getSnippetLog();

			GetReportResponse getReportResponse = new GetReportResponse(sock, writer, peersSent, listofContributers, peerMap, peerTimeMap, receivedUDPPeersLog, sentUDPPeersLog, snippetLog);
			getReportResponse.sendCode();
		}
		else if (responseComponents[1].equals("location")) {
			if(beforeReconnectRegistry) {
				System.out.println(key() + " - Starting UDP Server");
				udpServer = new UDPServer(0, teamName);
				System.out.println("I'm at location: " + udpServer.getIPAddress() + ":" + udpServer.getPort());
				udpIPAddress = udpServer.getIPAddress();
				udpPort = udpServer.getPort();
				GetLocationResponse getLocationResponse = new GetLocationResponse(sock, writer, udpServer.getIPAddress(), udpServer.getPort());
				getLocationResponse.sendCode();
			} else {
				System.out.println("I'm at location: " + udpIPAddress + ":" + udpPort);
				GetLocationResponse getLocationResponse = new GetLocationResponse(sock, writer, udpIPAddress, udpPort);
				getLocationResponse.sendCode();
			}
		}
	}
 	/*
 	 * printContinueMessage() Function
 	 * Acts as a pause. 
 	 * 
 	 */
//	public void printContinueMessage() {
//		Scanner Pausescanner = new Scanner(System.in);
//		System.out.println("Press any enter to continue...");
//		Pausescanner.nextLine();
//	}
	/*
	 * key() function
	 * RETURN:
	 * - the team name
	 * 
	 */
	String key() {
		return teamName;
	}
	/*
	 * toString() function
	 * RETURN:
	 * - The team name with the address and port
	 * 
	 */
	public String toString() {
		return key() + " " + address + ":" + port;
	}

	public static int countLines(String str){
		String[] lines = str.split("\r\n|\r|\n");
		return  lines.length;
	}
}