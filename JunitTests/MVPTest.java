import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import MVP.Client.ModularTestClient;
import MVP.Node.NodeType;
import MVP.Node.TestNode;
import MVP.Server.GridServer;



public class MVPTest {
	//minimum viable product tests.
	@Test
	public void test1() throws UnknownHostException, ClassNotFoundException, IOException, InterruptedException {
		ExecutorService Service = Executors.newFixedThreadPool(8);
		GridServer Serv = new GridServer(9090,9091);
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
	}

}
