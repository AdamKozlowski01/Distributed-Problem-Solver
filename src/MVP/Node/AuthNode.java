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
public class AuthNode implements MVP.Node.NodeType {

	private static Socket Node;
	private static DataOutputStream DataOut;
	private static ObjectOutputStream obOut;
	private static DataInputStream DataIn;
	private static ObjectInputStream obIn;
	private static boolean Ready = true;
	private static int Status =1;
	private long id = -1;
	private String host;
	private int port;
	private File idFile;
	static ExecutorService Solver = Executors.newFixedThreadPool(8);
	ProblemModule Task;
	Object recv;
	Future<Object> rec = null;
	
	public void Shutdown(){
		Status = 0;
		Ready = false;
	}

	public AuthNode(String host, int port) throws UnknownHostException, IOException {
		this.host = host;
		this.port = port;
		Node = new Socket(InetAddress.getByName(host),port);
	//	System.out.println("NodeSays: Connected setting up streams");
		DataOut = new DataOutputStream(Node.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		DataIn = new DataInputStream(Node.getInputStream());
		obIn = new ObjectInputStream(DataIn);
	//	System.out.println("NodeSays: Streams setup");
		
		
		this.id = -1;
		idFile= new File("id.txt");
		String idFromFile=null;
		
		/*if (idFile.exists() && idFile.canRead() ) {
			BufferedReader input = new BufferedReader(new FileReader(idFile));
			idFromFile=input.readLine();
			this.id = Long.parseLong(idFromFile);		
		}else{*/
			this.id = -1;
			//}		
	}
	
	@Override
	public void startNode(){
		Solver.submit(this);
		Status = 1;
		//System.out.println("Noderunninig");
	}

	@Override
	public void run() {
		try{
			//System.out.println("Noderunninig");
			rec = null;
			Future<ProblemModule> PM = null;
			rec = Solver.submit(new AuthInputService(obIn));
			//send the idPacket
			System.out.println("Node : About to send ID packet");
			obOut.writeObject(new IdentificationPacket(id));
			if(id == -1){
				Thread.sleep(1000);
				recv = rec.get();
				rec = Solver.submit(new AuthInputService(obIn));
				if(recv instanceof IdentificationPacket){
					id = ((IdentificationPacket)recv).getID();
					//write it to file for later use
					FileWriter writer = new FileWriter(idFile);
					writer.write(id+"");
					writer.close();
				}else{
					System.out.println("Bad Packet from Server.  Expected an ID Packet");
				}
			}
			while(Ready){
			//	System.out.println("Noderunninig");
				if(rec.isCancelled()){
					rec = Solver.submit(new AuthInputService(obIn));
				}
				if(rec.isDone() && !Node.isClosed()){
					recv = rec.get();
					rec = Solver.submit(new AuthInputService(obIn));
					if(recv instanceof ProblemModule){
						System.out.println("Node Says: Problem Received");
						Task = (ProblemModule) recv;
						recv = null;
						Status = -1;
						PM = Solver.submit((new AuthSolverService(Task)));
						//we recieved the task now shut down the connection
						obOut.writeObject(new IdentificationPacket(id));
						Node.close();
					} 
				}else{
					if(PM != null){
						if(PM.isDone()){
							reconnect();
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
			System.out.println("Node Says: Node Closing");
			obOut.writeObject(new IdentificationPacket(id));
			Node.close();
		}
		catch (IOException e) {}
		catch (InterruptedException e) {} 
		catch (ExecutionException e) {}	
	}
	
	public void reconnect() throws UnknownHostException, IOException{
		Node = new Socket(InetAddress.getByName(host),port);
		//	System.out.println("NodeSays: Connected setting up streams");
		DataOut = new DataOutputStream(Node.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		DataIn = new DataInputStream(Node.getInputStream());
		obIn = new ObjectInputStream(DataIn);
		obOut.writeObject(new IdentificationPacket(id));
	}
}

class AuthInputService implements Callable<Object>{

	ObjectInputStream ObIn;
	Object recv;
	public AuthInputService(ObjectInputStream obIn){
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

class AuthSolverService implements Callable<ProblemModule>{

	ProblemModule PM;
	AuthSolverService(ProblemModule pm){
		PM = pm;
	}

	@Override
	public ProblemModule call() throws Exception {
		PM.Solve();
		return PM;
	}
}
