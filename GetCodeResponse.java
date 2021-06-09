/**
 * CPSC 559 Winter 2021 Project Component ITERATION 1
 * 
 * FILE: getCodeResponse.java 
 * 
 * getCodeResponse() Function
 * Responsible for answering 'get code' response from the registry
 * 
 * @author Jason Huang
 * @UCID 10149037
 * 
 */
package registry;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class GetCodeResponse {
	
	BufferedWriter writer;
	Socket sock; 
	private String[] filesArray;

	public GetCodeResponse(Socket sock, BufferedWriter writer, String[] filesArray) {
		this.sock = sock;
		this.writer = writer;
		this.filesArray = filesArray;
	}
	public void sendResponse() throws IOException {
		writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		String message = "";
		message = message + "Java\n";
		message = message + retrieveCode() + "\n...\n";
		System.out.println("about to send source code");
		writer.write(message);
		writer.flush();
	}
	/*
	 * retrieveCode() Function
	 * Opens up all files used in iteration 1, reads it into a string
	 * 
	 * PARAMETERS:
	 * - None
	 * 
	 * RETURNS:
	 * - String: the read in files in String
	 * 
	 */
	@SuppressWarnings("resource")
	public String retrieveCode() throws FileNotFoundException {
		String content = null; 
		for(int i = 0; i < filesArray.length; i++) {
			content = content + new Scanner(new File("registry/"+filesArray[i])).useDelimiter("\\Z").next();
		}
		return content;	
	}
}