package multichat;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
 
/**
 * This thread is responsible for reading user's input and send it
 * to the server.
 * It runs in an infinite loop until the user types 'bye' to quit.
 * */
public class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;
    private List<String> testData;
 
    public WriteThread(Socket socket, ChatClient client) {
    	//Establish the server-client connection 
        this.socket = socket;
        this.client = client; 
 
        try {
        	//Create the output stream 
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
 
    public void run() {
    	
    	if(testData==null || testData.isEmpty()) {
	        Console console = System.console();
	        
	        //Create the reading stream 
	        InputStreamReader streamReader = new InputStreamReader(System.in);
	        BufferedReader bufferedReader = new BufferedReader(streamReader);
	        String userName=null;
	        
	        //Submit your username as your first act of connecting to the server
	        try {
	        	System.out.println("Enter your name: ");
				userName = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        client.setUserName(userName);
	        writer.println(userName);
	        
	        //write to the output stream to be sent to the server
	        String text = null;
	        do {
	            try {
					text = "[" + bufferedReader.readLine() + "]";
				} catch (IOException e) {
					e.printStackTrace();
				}
	            writer.println(text);
	            
	        } while (!text.equals("[bye]"));
	        
	        try {
	            socket.close();
	        } catch (IOException ex) {
	 
	            System.out.println("Error writing to server: " + ex.getMessage());
	        }
	        
    	}
    	else {
    		writer.println(client.getUserName());
    		for(String data:testData) {
	            writer.println(data);
    		}
            try {
	            socket.close();
	            
	        } catch (IOException ex) {
	 
	            System.out.println("Error writing to server: " + ex.getMessage());
	        }finally {
	        	if(testData!=null)
		        	ChatTest.clientQuitCount.getAndIncrement();
	        }
    	}
    }
    
    public void setTestData(List<String> data) {
    	this.testData = data;
    }
    
}
