package multichat;

import java.io.*;
import java.net.*;
import java.util.*;
 
/**
 * This is the chat server program.
 * Press Ctrl + C to terminate the program.
 * */
public class ChatServer {
    private int port;
    private Set<String> userNames = new HashSet<>();
    private Set<UserThread> userThreads = new HashSet<>();
    private Object uno = new Object();
    private Object uto = new Object();
 
    public ChatServer(int port) { 
        this.port = port;
    }
 
    public void execute() { 
    	
        try (ServerSocket serverSocket = new ServerSocket(port)) {
        	//Set up the server so that it listens on the port number
            System.out.println("Chat Server is listening on port " + port);
 
            while (true) {
            	//Accept any incoming connections
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");
 
                //Spawn a user thread to handle the input and output streams of the server and client connection
                UserThread newUser = new UserThread(socket, this);
                synchronized(uto){
                	userThreads.add(newUser);
                }
                newUser.start();
            }
 
        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Syntax: java ChatServer <port-number>");
            System.exit(0);
        }
 
        int port = Integer.parseInt(args[0]);
 
        ChatServer server = new ChatServer(port);
        server.execute();
    }
 
    /**
     * Delivers a message from one user to others (broadcasting)
     */
     void broadcast(String message, UserThread excludeUser) {
     	synchronized(uto) {
     		//Send the message to all the connected users 
	        for (UserThread aUser : userThreads) {
	            if (aUser != excludeUser) {
	                aUser.sendMessage(message);
	            }
	        }
     	}
    }
 
    /**
     * Stores username of the newly connected client.
     */
    void addUserName(String userName) {
    	synchronized(uno) {
    		userNames.add(userName);
    	}
    }
 
    /**
     * When a client is disconneted, removes the associated username and UserThread
     */
    void removeUser(String userName, UserThread aUser) {
    	boolean removed = false;
		synchronized(uno) {
	         removed = userNames.remove(userName);
		}
		synchronized(uto) {
	        if (removed) {
	            userThreads.remove(aUser);
	            System.out.println("The user " + userName + " quitted");
	        }
		}
    }
 
    Set<String> getUserNames() {
    	synchronized(uno) {
    		return this.userNames;
    	}
    }
 
    /**
     * Returns true if there are other users connected (not count the currently connected user)
     */
    boolean hasUsers() {
        synchronized(uno){
        	return !this.userNames.isEmpty();
        }
    }
}
