package node.TestNode;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import node.NodeType;
import problemModule.ProblemModule;

public class TestNode implements NodeType {

	private static Socket Node;
	private static DataOutputStream DataOut;
	private static ObjectOutputStream obOut;
	private static DataInputStream DataIn;
	private static ObjectInputStream obIn;
	private static boolean Ready = true;
	private static int Status;

	//TESTNODE this is a basic test of the layered services that can keep solve problems on one thread, check for incoming problems on another, and send node status on the main.
	//essentially I made a Node that will keep its connection alive, even if its sitting idle for a long period of time and it can process tasks that can take it longer than the sockets timeout
	//by sending status packets that tell the server its busy while a separate thread does the work.
	//I have to check to see if the read timeout on the Input service will cause a fatal error, The error is technically unhandled but the isCancelled() should return true and create a new InputService
	//to wait for a problem from the server.
	
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException, ExecutionException{
		
		
		
		Node = new Socket(InetAddress.getByName(null),9090);
		System.out.println("Connected");
		DataOut = new DataOutputStream(Node.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		
		DataIn = new DataInputStream(Node.getInputStream());
		
		obIn = new ObjectInputStream(DataIn);
		
	
		long newid;
		
		File file= new File("id.txt");
		String id=null;
		
		if (file.exists() && file.canRead() ) {
			BufferedReader input = new BufferedReader(new FileReader(file));
			id=input.readLine();
			DataOut.writeUTF(id);
			//sending ID
			//output.println( "Found...transmitting contents..." );			
		}
		else
		{
			DataOut.writeUTF("-1");
			//sends -1 if not found
			long newId= DataIn.readLong();
			FileWriter writer = new FileWriter(file);
			String newIdString = newId+"";
			//convert long to string
			
			System.out.println(newIdString);
			writer.write(newIdString);
			writer.close();
			//write to file for future reference
		}
	
		
		ExecutorService Solver = Executors.newCachedThreadPool();
		
		ProblemModule Task;
		Object recv;
		Future<Object> rec = null;
	
		Future<ProblemModule> PM = null;
		//InputService IS = new InputService(obIn);
		rec = Solver.submit(new InputService(obIn));
 		while(Ready){
			if(rec.isCancelled()){
			rec = Solver.submit(new InputService(obIn));
			}
			 if(rec.isDone()){
				recv = rec.get();
				rec = Solver.submit(new InputService(obIn));
				if(recv instanceof ProblemModule){
					System.out.println("Problem Received");
					Task = (ProblemModule) recv;
					recv = null;
					Status = -1;
					PM = Solver.submit((new SolverService(Task)));
				} 
			}else{
				if(PM != null){
					if(PM.isDone()){
						obOut.writeObject(PM.get());	
						System.out.println("problem sent");
						PM=null;
						Status=1;
					}
				}
			obOut.writeObject(new common.Status(Status));
			Thread.sleep(3000);
			}
		}
 		obOut.writeObject(new common.Status(Status));
 		Node.close();
	}
	
	public void Shutdown(){
		Status = 0;
		Ready = false;
	}

	public TestNode(String host, int port) {
		
		// TODO Auto-generated constructor stub
		
		
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		
		
	}
}

class InputService implements Callable<Object>{

	ObjectInputStream ObIn;
	Object recv;
	public InputService(ObjectInputStream obIn){
		ObIn = obIn;
	}
	
	@Override
	public Object call() throws Exception {
		if((recv = ObIn.readObject()) != null){
			return recv;
		}
		return null;
	}
}

class SolverService implements Callable<ProblemModule>{

	ProblemModule PM;
	SolverService(ProblemModule pm){
		PM = pm;
	}

	@Override
	public ProblemModule call() throws Exception {
		PM.Solve();
		return PM;
	}
}
