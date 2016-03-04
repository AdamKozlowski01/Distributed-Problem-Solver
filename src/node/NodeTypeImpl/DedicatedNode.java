package node.NodeTypeImpl;

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


public class DedicatedNode implements NodeType {
	
	
	private static Socket Node;
	private static DataOutputStream DataOut;
	private static ObjectOutputStream obOut;
	private static DataInputStream DataIn;
	private static ObjectInputStream obIn;
	private static boolean Ready = true;
	private static int Status;
	
	public DedicatedNode(InetAddress host, int port) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException, ExecutionException {

		Node = new Socket(host,9090);
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
			writer.write(newIdString);
			//write to file for future reference


		}

		
		ExecutorService Solver = Executors.newCachedThreadPool();
		
		ProblemModule Task;
		Object recv;
		Future<Object> rec = null;
	
		Future<ProblemModule> PM = null;
		InputService IS = new InputService(obIn);
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
		PM.DelaySolve();
		return PM;
	}
}





