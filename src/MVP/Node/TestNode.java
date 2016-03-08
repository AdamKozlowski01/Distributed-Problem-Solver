package MVP.Node;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import MVP.problemModule.ProblemModule;
import MVP.Common.*;
//TODO don't disconnect after problem solution.
public class TestNode implements MVP.Node.NodeType {

	private static Socket Node;
	private static DataOutputStream DataOut;
	private static ObjectOutputStream obOut;
	private static DataInputStream DataIn;
	private static ObjectInputStream obIn;
	private static boolean Ready = true;
	private static int Status;
	static ExecutorService Solver = Executors.newFixedThreadPool(8);
	ProblemModule Task;
	Object recv;
	Future<Object> rec = null;
	
	public void Shutdown(){
		Status = 0;
		Ready = false;
	}

	public TestNode(String host, int port) throws UnknownHostException, IOException {
		Node = new Socket(InetAddress.getByName(host),port);
		System.out.println("NodeSays: Connected setting up streams");
		DataOut = new DataOutputStream(Node.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		DataIn = new DataInputStream(Node.getInputStream());
		obIn = new ObjectInputStream(DataIn);
		System.out.println("NodeSays: Streams setup");
	}
	
	@Override
	public void startNode(){
		Solver.submit(this);
		Status = 1;
		System.out.println("Noderunninig");
	}

	@Override
	public void run() {
		try{
			System.out.println("Noderunninig");
			rec = null;
			Future<ProblemModule> PM = null;
			rec = Solver.submit(new InputService(obIn));
			while(Ready){
				System.out.println("Noderunninig");
				if(rec.isCancelled()){
					rec = Solver.submit(new InputService(obIn));
				}
				if(rec.isDone()){
					recv = rec.get();
					rec = Solver.submit(new InputService(obIn));
					if(recv instanceof ProblemModule){
						System.out.println("Node Says: Problem Received");
						Task = (ProblemModule) recv;
						recv = null;
						Status = -1;
						PM = Solver.submit((new SolverService(Task)));
					} 
				}else{
					if(PM != null){
						if(PM.isDone()){
							obOut.writeObject(PM.get());	
							System.out.println("Node Says: problem sent");
							PM=null;
							Status=1;
						}
					}
					obOut.writeObject(new Status(Status));
					Thread.sleep(3000);
				}
			}
			obOut.writeObject(new Status(Status));
			Node.close();
		}
		catch (IOException e) {e.printStackTrace();}
		catch (InterruptedException e) {e.printStackTrace();} 
		catch (ExecutionException e) {e.printStackTrace();}	
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
			System.out.println("NodeSays Object Recieved");
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
