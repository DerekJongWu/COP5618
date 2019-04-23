package multichat;

import java.io.*;
import java.net.*;
 
/**
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 * */
public class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;
 
    public ReadThread(Socket socket, ChatClient client) {
    	//Create the socket connection between the client and the server
        this.socket = socket;
        this.client = client;
 
        try {
        	//Create the input stream to read from server
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
 
    public void run() {
        while (true) {
            try {
            	//Read message inputs from the client
                String response = reader.readLine();
                if(response!=null)
                	System.out.println("\n" + response);
 
                // prints the username after displaying the server's message
                if (client.getUserName() != null) {
                    System.out.print("[" + client.getUserName() + "]: ");
                }
            }
            catch (SocketException se) {
            	if(se.getMessage().contains("Socket closed"))
            		break;
            }
            catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }
}
