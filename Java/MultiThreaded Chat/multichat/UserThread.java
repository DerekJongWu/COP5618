package multichat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
 
/**
 * This thread handles connection for each connected client, so the server
 * can handle multiple clients at the same time.
 * */
public class UserThread extends Thread {
    private Socket socket;
    private ChatServer server;
    private PrintWriter writer;
 
    public UserThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }
 
    public void run() {
        try {
        	//Create input and output streams for reading and writing
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
 
            //Send a list of connected users to the newly connected client
            printUsers();
 
            //Read the username submitted by the client
            String userName = reader.readLine();
            server.addUserName(userName); 
            
            //Alert all connected users that a new client has joined.
            String serverMessage = "New user connected: " + userName;
            server.broadcast(serverMessage, this);
 
            //Process all incoming messages
            String clientMessage;
 
            do {
                clientMessage = reader.readLine();
                if(clientMessage!=null && !clientMessage.isEmpty()) {
	                String broadcastCM = "[" + userName + "]: " + clientMessage; 
	                server.broadcast(broadcastCM, this);
                }
            } while (!clientMessage.equals("[bye]"));
 
            System.out.println("Client quitted. Current time = " + System.currentTimeMillis());
            server.removeUser(userName, this);
 
            serverMessage = userName + " has quitted.";
            server.broadcast(serverMessage, this);
            socket.close();
            
        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace(); 
        }
    }
 
    /**
     * Sends a list of online users to the newly connected user.
     */
    void printUsers() {
        if (server.hasUsers()) {
            writer.println("Connected users: " + server.getUserNames());
        } else {
            writer.println("No other users connected");
        }
    }
 
    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
    	if(writer!=null)//client may have exited. removeUser on server might take some more time and this gets called
    		writer.println(message);
    }
}
