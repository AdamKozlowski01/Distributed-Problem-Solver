package server.ServerConnectionImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import common.KeepAlive;
import common.Packets;
import common.Status;
import problemModule.ProblemModule;
import server.ServerConnectionManager;

//plan is to use a decorator to decorate a connection as either a client or a node.
//need to synchronize on the node filter, so the threads cannot reserve the same node
//and it will keep all the nodes busy if each problem management thread gets the proper number of nodes returned.


public class MultiThreadedServerConnectionManager implements ServerConnectionManager,Runnable {

	private static ExecutorService MTSE = Executors.newCachedThreadPool();
	int CPort,NPort,PThreads;
	ClientListener CL;
	NodeListener NL;
	//boolean Running;
	//String Host;
	private static ConcurrentHashMap<ProblemModule,Client> Clients = new ConcurrentHashMap<ProblemModule,Client>();
	private static ConcurrentHashMap<Socket,Node> Nodes = new ConcurrentHashMap<Socket,Node>();
	private final BlockingQueue<ProblemModule> Tasks = new LinkedBlockingQueue<ProblemModule>(); 

	//private NodeFilterStrategy Filter = new AllAvailable();

	public MultiThreadedServerConnectionManager(){
		//TODO Default Constructor
	}

	public MultiThreadedServerConnectionManager(int cPort, int nPort,int pThreads) {
		CPort = cPort;
		NPort = nPort;
		PThreads = pThreads;
	}

	public synchronized void addClient(Client c){
		Clients.put(c.getProblem(), c);
	}

	public synchronized void addNode(Node n){
		Nodes.put(n.getSocket(),n);
	}

	public synchronized void submitTask(Runnable R){
		MTSE.submit(R);
	}
	@Override
	public void StartServer() throws IOException{
		CL = new ClientListener(CPort,this);
		NL = new NodeListener(NPort,this);
		MTSE.submit(CL);
		MTSE.submit(NL);
	}

	@Override
	public void setCPort(int P){
		CPort = P;
	}

	@Override
	public void setNPort(int P){
		NPort = P;
	}	

	//this is where the filter strategy would go.
	private synchronized ArrayList<Node> getReadyNodes(){
		ArrayList<Node> ReadyNodes = new ArrayList<Node>();
		
		return ReadyNodes;
	}

	@Override
	public void run() {
		//MTSE.submit(task)

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}
}

class ClientListener implements Runnable{
	int CPort;
	MultiThreadedServerConnectionManager Parent;
	boolean running;
	ServerSocket ClientListener;
	public ClientListener(int cPort, MultiThreadedServerConnectionManager mtscm) throws IOException {
		CPort = cPort;
		Parent = mtscm;
		running = true;
		ClientListener = new ServerSocket(CPort);
	}

	@Override
	public void run() {
		while(running){
			try {
				System.out.println("ClientListener Running on Port: " + CPort);
				Socket client = ClientListener.accept();
				System.out.println("ClientConnected");
				Parent.submitTask(new Client(client,Parent));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		try {
			ClientListener.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void Shutdown(){running = false;}

}
class NodeListener implements Runnable{

	public NodeListener(int nPort, MultiThreadedServerConnectionManager multiThreadedServerConnectionManager) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}


class Client implements Runnable{
	private DataOutputStream DataOut;
	private ObjectOutputStream obOut;
	private DataInputStream DataIn;
	private ObjectInputStream obIn;
	private Socket Client;
	private MultiThreadedServerConnectionManager Parent;
	ProblemModule Task;
	boolean Complete;

	public Client(Socket client, MultiThreadedServerConnectionManager parent) throws IOException {
		Client = client;
		Parent = parent;
		Complete = false;
	}

	public ProblemModule getProblem() {
		return Task;
	}

	public void completeTask(ProblemModule p){
		Task = p;
		Complete=true;
	}

	private void setupStreams() throws IOException{
		DataOut = new DataOutputStream(Client.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		DataIn = new DataInputStream(Client.getInputStream());
		obIn = new ObjectInputStream(DataIn);
	}

	@Override
	public void run() {
		try {
			setupStreams();
			Object RecievedObj = obIn.readObject();
			System.out.println("Object Recieved");
			if(RecievedObj instanceof ProblemModule){
				Task = (ProblemModule) RecievedObj;
			}
			Parent.addClient(this);
			while(!Complete){
				obOut.writeObject(new KeepAlive(0L));
				Thread.sleep(3000);
			}
			obOut.writeObject(Task);
			Client.close();
		} 
		catch (IOException e) {e.printStackTrace();} 
		catch (ClassNotFoundException e) {e.printStackTrace();} 
		catch (InterruptedException e) {e.printStackTrace();}
	}
}

class Node implements Runnable{
	private DataOutputStream DataOut;
	private ObjectOutputStream obOut;
	private DataInputStream DataIn;
	private ObjectInputStream obIn;
	private Socket Node;
	private MultiThreadedServerConnectionManager Parent;
	private int status;
	private ProblemModule Task;
	private boolean TaskComplete;

	private void setupStreams() throws IOException{
		DataOut = new DataOutputStream(Node.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		DataIn = new DataInputStream(Node.getInputStream());
		obIn = new ObjectInputStream(DataIn);
	}

	public Socket getSocket() {return Node;}
	
	public boolean sendTask(ProblemModule task) throws IOException{
		if(status == 1){
		Task = task;
		TaskComplete = false;
		obOut.writeObject(Task);
		return true;
		}else{
			return false;
		}
	}
	
	public ProblemModule retrieveTask(){
		ProblemModule returnMod = Task;
		Task = null;
		TaskComplete= false;

		return returnMod;	
	}
	
	public boolean problemReady(){
		return TaskComplete;
	}
	
	public int getStatus(){
		return status;
	}

	@Override
	public void run() {
		try {
			setupStreams();
			
			while(Node.isConnected() && !Node.isClosed()){		
				Object RecievedObj = obIn.readObject();
				//System.out.println("Object Recieved");
					if((RecievedObj = obIn.readObject()) instanceof ProblemModule){
						Task = (ProblemModule) RecievedObj;
						TaskComplete = true;
						System.out.println("PM recieved");
					}else if(RecievedObj instanceof Packets){
						if((RecievedObj instanceof Status)){
							status = ((Status) RecievedObj).getStatus();
							System.out.println("Node status " + status);
						}
					}
				}
			status = 0;
			Node.close();
		}
		catch (IOException e) {e.printStackTrace();} 
		catch (ClassNotFoundException e) {e.printStackTrace();}
	}
}



class ProblemServicer implements Callable<ProblemModule>{

	@Override
	public ProblemModule call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}