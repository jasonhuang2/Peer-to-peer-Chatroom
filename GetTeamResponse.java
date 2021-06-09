/**
 * CPSC 559 Winter 2021 Project Component ITERATION 1
 * 
 * FILE: getTeamResponse.java
 * getTeamResponse() Function
 * Response for 'get team name' response. Returns the team name back to the registry
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

public class GetTeamResponse {
	
	Socket sock;
	String teamName;
	BufferedWriter writer;
	
	public GetTeamResponse(Socket sock, BufferedWriter writer, String teamName) {
		this.sock = sock;
		this.writer = writer;
		this.teamName = teamName;
	}
	public void sendResponse() throws IOException {
		writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		String message = "";
		//<team name response> ::= <team name><newline>
		message = message + teamName;
		message = message + "\n";
		writer.write(message);
		writer.flush();
	}
}
