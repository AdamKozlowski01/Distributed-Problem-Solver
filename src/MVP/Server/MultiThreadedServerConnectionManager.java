package MVP.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import MVP.Common.*;
import MVP.problemModule.ProblemModule;

//plan is to use a decorator to decorate a connection as either a client or a node.
//need to synchronize on the node filter, so the threads cannot reserve the same node
//and it will keep all the nodes busy if each problem management thread gets the proper number of nodes returned.


public class MultiThreadedServerConnectionManager implements MVP.Server.ServerConnectionManager,Runnable {

	private static ExecutorService MTSE = Executors.newFixedThreadPool(40);
	int CPort,NPort,PThreads;
	ClientListener CL;
	NodeListener NL;
	ProblemServicer PS;
	private static ConcurrentHashMap<ProblemModule,Client> Clients = new ConcurrentHashMap<ProblemModule,Client>();
	private static ConcurrentHashMap<Node,Node> Nodes = new ConcurrentHashMap<Node,Node>();
	private static final BlockingQueue<ProblemModule> Tasks = new LinkedBlockingQueue<ProblemModule>(); 

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
		Tasks.add(c.getProblem());
		System.out.println("ServerSays: Client and Task Added");
	}

	public synchronized void addNode(Node n){
		System.out.println("adding Node");
		Nodes.put(n,n);
	}
	
	public void returnTask(ProblemModule Task){
		
	}

	public synchronized void submitTask(Runnable R){
		MTSE.execute(R);
	}
	@Override
	public void StartServer() throws IOException{
		CL = new ClientListener(CPort,this);
		NL = new NodeListener(NPort,this);
		PS = new ProblemServicer(this);
		MTSE.execute(CL);
		MTSE.execute(NL);
		MTSE.execute(PS);
	}

	@Override
	public void setCPort(int P){
		CPort = P;
	}

	@Override
	public void setNPort(int P){
		NPort = P;
	}	

	public Client getTaskfromQueue() throws InterruptedException{
		ProblemModule Task = Tasks.take();
		System.out.println("ServerSays: getting task from queue");
		return Clients.get(Task);
	}
	
	//this is where the filter strategy would go.
	public synchronized ArrayList<Node> ScheduleNodes(ProblemModule task) throws IOException{
		System.out.println("ServerSays: SchedulingNodes");
		ArrayList<Node> ReadyNodes = new ArrayList<Node>();
		 for(Node i :Nodes.keySet()){
			 if(i.getStatus() == 0 && !i.isScheduled()){
				 ReadyNodes.add(i);
			 }	 
		 }
		ProblemModule[] subTasks = task.breakDown(ReadyNodes.size());
		ArrayList<Node> ScheduledNodes = new ArrayList<Node>();
		for(int i = 0; i<subTasks.length ;i++){
			Node n = Nodes.get(ReadyNodes.get(i));
			n.setScheduled(true);
			n.sendTask(subTasks[i]);
			ScheduledNodes.add(n);
		}
		System.out.println("ServerSays: SchedulingNodes Complete");
		return ScheduledNodes;
	}

	@Override
	public void run() {
		//MTSE.submit(task)

	}

	@Override
	public void shutdown() {
		CL.Shutdown();
		NL.Shutdown();
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
		System.out.println("ClientListener Running on Port: " + CPort);
		while(running){
			try {
				System.out.println("ClientListener Waiting for Connection " + CPort);
				Socket client = ClientListener.accept();
				System.out.println("ClientConnected");
				Parent.submitTask(new Client(client,Parent));
			} catch (IOException e) {e.printStackTrace();}	
		}
		try {
			ClientListener.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	public void Shutdown(){running = false;}

}
class NodeListener implements Runnable{
	int NPort;
	MultiThreadedServerConnectionManager Parent;
	boolean running;
	ServerSocket NodeListener;

	public NodeListener(int nPort, MultiThreadedServerConnectionManager mtscm) throws IOException {
		NPort = nPort;
		Parent = mtscm;
		running = true;
		NodeListener = new ServerSocket(NPort);
	}

	@Override
	public void run() {
		System.out.println("NodeListener Running on Port: " + NPort);
		while(running){
			try {
				System.out.println("NodeListener Waiting for Connection: ");
				Socket node = NodeListener.accept();
				System.out.println("NodeConnected");
				Parent.submitTask(new Node(node,Parent));
			} catch (IOException e) {e.printStackTrace();}	
		}
		try {
			NodeListener.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public void Shutdown(){running = false;}
	
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
			System.out.println("ServerSays: Object Recieved from client");
			if(RecievedObj instanceof ProblemModule){
				Task = (ProblemModule) RecievedObj;
				System.out.println("ServerSays: Object is Problem Module");
			}
			Parent.addClient(this);
			while(!Complete){
				obOut.writeObject(new KeepAlive(0L));
				Thread.sleep(1000);
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
	private boolean TaskComplete,scheduled;

	private void setupStreams() throws IOException{
		DataOut = new DataOutputStream(Node.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		DataIn = new DataInputStream(Node.getInputStream());
		obIn = new ObjectInputStream(DataIn);
	}
	
	public Node(Socket node, MultiThreadedServerConnectionManager parent) throws IOException {
		Node = node;
		Parent = parent;
	}

	public Socket getSocket() {return Node;}
	
	public void sendTask(ProblemModule task) throws IOException{
		Task = task;
		TaskComplete = false;
		obOut.writeObject(Task);
	}
	
	public ProblemModule retrieveTask(){
		ProblemModule returnMod = Task;
		Task = null;
		TaskComplete= false;
		setScheduled(false);
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
			Parent.addNode(this);
			
			while(Node.isConnected() && !Node.isClosed()){		
				Object RecievedObj = obIn.readObject();
				System.out.println("ServerSays: Object Recieved from node");
					if((RecievedObj) instanceof ProblemModule){
						Task = (ProblemModule) RecievedObj;
						TaskComplete = true;
						System.out.println("ServerSays: PM recieved from node");
					}else if(RecievedObj instanceof Packets){
						if((RecievedObj instanceof Status)){
							status = ((Status) RecievedObj).getStatus();
							System.out.println("ServerSays: Node status " + status);
						}
					}
				}
			status = 0;
			Node.close();
		}
		catch (IOException e) {e.printStackTrace();} 
		catch (ClassNotFoundException e) {e.printStackTrace();}
	}

	public boolean isScheduled() {
		return scheduled;
	}

	public void setScheduled(boolean scheduled) {
		this.scheduled = scheduled;
	}
}


//TODO: finish ProblemServicer

class ProblemServicer implements Runnable{
	
	private MultiThreadedServerConnectionManager Parent;
	private boolean running = true;
	private ProblemModule Task;
	private Client C;
	private ArrayList<Node> workers;
	private ProblemModule[] subTasks;
	public ProblemServicer(MultiThreadedServerConnectionManager mtscm){
		System.out.println("Problem Servicer Started");
		Parent = mtscm;
		workers = new ArrayList<Node>();
	}
	
	private boolean allReady(){
		for(Node n: workers){
			if(n.problemReady()){
				System.out.println("All Ready");
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void run() {
	 while(running){
		 try {
			System.out.println("getting Task from queue");
			C = Parent.getTaskfromQueue();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 Task = C.getProblem();
		 try {
			workers = Parent.ScheduleNodes(Task);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 System.out.println("Creating subTask");
		 subTasks = new ProblemModule[workers.size()];
		 for(int i = 0; i<workers.size();i++){
			 try {
				workers.get(i).sendTask(subTasks[i]);
			} catch (IOException e) {e.printStackTrace();}
		 }
		 while(!allReady()){
			 try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 for(int i=0; i<subTasks.length;i++){
			 subTasks[i] = workers.get(i).retrieveTask();
		 }
		 Task.finalize(subTasks);
		 C.completeTask(Task);
		 System.out.println("TaskCompleted");
	 	}
	}

	public void killThread(){
		this.running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}