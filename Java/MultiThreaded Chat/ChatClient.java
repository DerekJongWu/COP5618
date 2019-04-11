package multichat;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
 
/**
 * This is the chat client program.
 * Type 'bye' to terminte the program.
 *
 * @author www.codejava.net
 */
public class ChatClient {
    private String hostname;
    private int port;
    private String userName; 
    private List<String> testData;
 
    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    
 
    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);
 
            System.out.println("Connected to the chat server");
 
            new ReadThread(socket, this).start();
            WriteThread wt =  new WriteThread(socket, this);
            if(testData!=null)
            	ChatTest.writers.add(wt);
            wt.setTestData(testData); 
            wt.start();
 
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
 
    }
 
    void setUserName(String userName) {
        this.userName = userName;
    }
 
    String getUserName() {
        return this.userName;
    }
 
    public void setTestData(List<String> data) {
    	this.testData=data;
    }
    
    public static void main(String[] args) {
    	
        if (args.length < 2) return;
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
 
        ChatClient client = new ChatClient(hostname, port);
        client.execute();
    }
    
    
}