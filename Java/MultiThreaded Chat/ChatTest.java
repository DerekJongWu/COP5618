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
	public static volatile List<WriteThread> writers = new ArrayList<>();
	//for the clients to write to when they quit
	public static AtomicInteger clientQuitCount = new AtomicInteger(0);
	
	static {
		new Thread(() -> chatServer.execute()).start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void reset() {
		clientQuitCount = new AtomicInteger(0);
		writers.clear();
	}
	
	@Test
	public void _1_testServerBroadcastLatency() {
	
		Long start = System.currentTimeMillis();
		ChatClient client1 = new ChatClient(hostName, port), client2 = new ChatClient(hostName, port);
		client1.setTestData(new ArrayList<>(Arrays.asList("[bye]")));
		client1.setUserName("1");
		client2.setUserName("2");
		client1.execute();
		client2.execute();
		for(WriteThread wt : writers) {
			try {
				wt.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		Assert.assertEquals(1, clientQuitCount.get());
		System.out.println("total elapsed time ====================================  " + (System.currentTimeMillis()-start) + " ms");
	}
	
	@Test
	public void _2_testClientChatUsingMultipleClients() {
		
		Long start = System.currentTimeMillis();
		List<ChatClient> chatClients = new ArrayList<>();
		for(int i=0;i<num_clients; i++)
			chatClients.add(new ChatClient(hostName, port));
		
		for(int i=0;i<num_clients; i++) {
			ChatClient client = chatClients.get(i);
			client.setUserName(Integer.toString(i));
			List<String> testmsg = new ArrayList<>();
			testmsg.add(i + " test message");
			testmsg.add("[bye]");
			client.setTestData(testmsg);
			client.execute();
		}
		for(WriteThread wt : writers) {
			try {
				wt.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Assert.assertEquals(num_clients, clientQuitCount.get());
		System.out.println("total elapsed time ==================================== " + (System.currentTimeMillis()-start) + " ms");
	}
}
