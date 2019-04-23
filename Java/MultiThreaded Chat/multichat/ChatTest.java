package multichat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChatTest {
	
	private final String hostName = "127.0.0.1";
	private final static int port = 59001;
	private final int num_clients = 100;
	private final static ChatServer chatServer = new ChatServer(port);
	//keeps track of writer threads in order to know when the client quit
	public static volatile List<WriteThread> writers = new ArrayList<>();
	//for the clients to write to when they quit
	public static AtomicInteger clientQuitCount = new AtomicInteger(0);
	
	//start server before tests
	static {
		new Thread(() -> chatServer.execute()).start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//calls before each test. Resets client and writer data.
	@Before
	public void reset() {
		clientQuitCount = new AtomicInteger(0);
		writers.clear();
	}
	
	//Two clients are created. First client sends a message to the server which then broadcasts to the second client
	//Both clients exit after this and the broadcast latency is recorded
	@Test
	public void _1_testServerBroadcastLatency() {
	
		Long start = System.currentTimeMillis();
		ChatClient client1 = new ChatClient(hostName, port), client2 = new ChatClient(hostName, port);
		client1.setTestData(new ArrayList<>(Arrays.asList("[bye]")));
		client1.setUserName("1");
		client2.setUserName("2");
		//start clients 1 and 2
		client1.execute();
		client2.execute();
		//wait for them to finish
		for(WriteThread wt : writers) {
			try {
				wt.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		Assert.assertEquals(1, clientQuitCount.get());
		//elapsed time depicts the broadcast latency of the server
		System.out.println("total elapsed time ====================================  " + (System.currentTimeMillis()-start) + " ms");
	}
	
	/* 'num_clients(=100)' clients are created. Each clients sends 2 messages to the server which then broadcasts the msg to 
	    the remaining clients. All the clients exit after this process and we measure the time recorded to be an indicator 
	    of the chatroom application's bandwidth */
	@Test
	public void _2_testClientChatUsingMultipleClients() {
		
		Long start = System.currentTimeMillis();
		List<ChatClient> chatClients = new ArrayList<>();
		for(int i=0;i<num_clients; i++)
			chatClients.add(new ChatClient(hostName, port));
		
		//start 'num_clients' clients and send message to the server
		for(int i=0;i<num_clients; i++) {
			ChatClient client = chatClients.get(i);
			client.setUserName(Integer.toString(i));
			List<String> testmsg = new ArrayList<>();
			testmsg.add(i + " test message");
			testmsg.add("[bye]");
			client.setTestData(testmsg);
			client.execute();
		}
		//wait for the clients to finish
		for(WriteThread wt : writers) {
			try {
				wt.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Assert.assertEquals(num_clients, clientQuitCount.get());
		//measure the elapsed time
		System.out.println("total elapsed time ==================================== " + (System.currentTimeMillis()-start) + " ms");
	}
}
