import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import MVP.Client.FileStringSearchTestClient;
//import MVP.Client.FileStringSearchTestClient;
import MVP.Client.ModularTestClient;
import MVP.Node.AuthNode;
import MVP.Node.NodeType;
import MVP.Node.TestNode;
import MVP.Server.AuthenticatingServer;
import MVP.Server.GridServer;
import MVP.problemModule.FileStringSearch;



public class MVPTest {
	//minimum viable product tests.
	@Test
	public void authTest() throws IOException, ClassNotFoundException, InterruptedException {
		ExecutorService Service = Executors.newFixedThreadPool(8);
		AuthenticatingServer server = new AuthenticatingServer(9090, 9091);
		server.setStartingProblemThreads(1);
		server.Start();
		NodeType n1 = new AuthNode(null, 9091);
		Service.execute(n1);
		ModularTestClient MTC = new ModularTestClient();
		MTC.startWithDefaults(null, 9090, 2, 3);
		assertTrue(MTC.getSuccess());
		server.Shutdown();
		Thread.sleep(4000);
	}
	
	/*@Test
	public void authTestFileSearch() throws IOException, ClassNotFoundException {
		ExecutorService Service = Executors.newFixedThreadPool(8);
		AuthenticatingServer server = new AuthenticatingServer(9090, 9091);
		server.setStartingProblemThreads(1);
		server.Start();
		NodeType n1 = new AuthNode(null, 9091);
		Service.execute(n1);
		FileStringSearchTestClient MTC = new FileStringSearchTestClient();
		MTC.startWithDefaults(null, 9090, "test.txt", "The truth of each thing is a property of the essence");
		assertTrue(MTC.getSuccess());
		server.Shutdown();
	}*/
	
	@Test
	public void test1() throws UnknownHostException, ClassNotFoundException, IOException, InterruptedException {
		ExecutorService Service = Executors.newFixedThreadPool(8);
		//GridServer Serv = new GridServer(9090,9091);
		AuthenticatingServer Serv = new AuthenticatingServer(9090, 9091);
		Serv.setStartingProblemThreads(1);
		Serv.Start();
		NodeType n1 = new AuthNode(null, 9091);
		Service.execute(n1);
		ModularTestClient MTC = new ModularTestClient();
		MTC.startWithDefaults(null, 9090, 2, 3);
		assertTrue(MTC.getSuccess());
		Serv.Shutdown();
		Thread.sleep(4000);
	}
	
	@Test
	public void test2() throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException{
		ExecutorService Service = Executors.newFixedThreadPool(8);
		GridServer Serv = new GridServer(9090,9091);
		//AuthenticatingServer Serv = new AuthenticatingServer(9090, 9091);
		Serv.setStartingProblemThreads(1);
		Serv.Start();
		NodeType n1 = new TestNode(null, 9091);
		Service.execute(n1);
		Thread.sleep(1000);
		NodeType n2 = new TestNode(null, 9091);
		Service.execute(n2);
		ModularTestClient MTC = new ModularTestClient();
		MTC.startWithDefaults(null, 9090, 2, 3);
		assertTrue(MTC.getSuccess());
		Serv.Shutdown();
		Thread.sleep(4000);
	}

	@Test
	public void test3() throws IOException, InterruptedException, ClassNotFoundException{
		ExecutorService Service = Executors.newFixedThreadPool(8);
		GridServer Serv = new GridServer(9090,9091);
		//AuthenticatingServer Serv = new AuthenticatingServer(9090, 9091);
		Serv.setStartingProblemThreads(1);
		Serv.Start();
		NodeType n1 = new TestNode(null, 9091);
		Service.execute(n1);
		Thread.sleep(1000);//this is because I need to fix a bug in the connection logic. I know the bug and the fix.
		NodeType n2 = new TestNode(null, 9091);
		Service.execute(n2);
		Thread.sleep(1000);
		NodeType n3 = new TestNode(null,9091);
		Service.execute(n3);
		Thread.sleep(1000);
		NodeType n4 = new TestNode(null,9091);
		Service.execute(n4);
		Thread.sleep(1000);
		ModularTestClient MTC = new ModularTestClient();
		MTC.startWithDefaults(null, 9090, 2, 3);
		assertTrue(MTC.getSuccess());
		Serv.Shutdown();
		Thread.sleep(4000);
	}
	
	
	
	@Test
	public void fileStringSearchTest() throws IOException, InterruptedException, ClassNotFoundException{
		ExecutorService Service = Executors.newFixedThreadPool(8);
		GridServer Serv = new GridServer(9090,9091);
		Serv.setStartingProblemThreads(1);
		Serv.Start();
		NodeType n1 = new TestNode(null, 9091);
		Service.execute(n1);
		Thread.sleep(1000);//this is because I need to fix a bug in the connection logic. I know the bug and the fix.
		NodeType n2 = new TestNode(null, 9091);
		Service.execute(n2);
		Thread.sleep(1000);
		NodeType n3 = new TestNode(null,9091);
		Service.execute(n3);
		Thread.sleep(1000);
		NodeType n4 = new TestNode(null,9091);
		Service.execute(n4);
		Thread.sleep(1000);
		FileStringSearchTestClient FSSTC = new FileStringSearchTestClient();
		FSSTC.startWithDefaults(null, 9090, "test.txt", "The truth of each thing is a property of the essence");
		assertTrue(FSSTC.getSuccess());
		Serv.Shutdown();
		Thread.sleep(3000);
	}
	
}
