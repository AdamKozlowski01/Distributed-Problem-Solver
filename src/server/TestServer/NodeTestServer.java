package server.TestServer;
import java.io.IOException;

import problemModule.HardcodedTestProblem;
import problemModule.ProblemModule;

public class NodeTestServer {

	static String Host;
	static int Port;
	//Known to function, does not implement the Node functionality.
	//Use to debug A new ProblemModule
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		//TestProblemModule Task = new TestProblemModule(TestA, TestA, m, m);
		AuthenticatingServer server = new AuthenticatingServer(9090);
		ProblemModule hardcode = new HardcodedTestProblem();
		server.getNewProblem(hardcode);
		
		while(true){
			try{
				server.handleConnection();
				server.distributeWork();
				server.handleConnection();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}

