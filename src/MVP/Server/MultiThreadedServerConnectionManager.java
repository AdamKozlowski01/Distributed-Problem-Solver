package MVP.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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


//TODO: Improvements to this class. Need to test with a problem module with a proper breakdown and finalize method.

public class MultiThreadedServerConnectionManager implements MVP.Server.ServerConnectionManager,Runnable {

	private static ExecutorService MTSE = Executors.newFixedThreadPool(40);
	int CPort,NPort,PThreads;
	ClientListener CL;
	NodeListener NL;
	ProblemServicer PS;
	private static ArrayList<Node> PS1 = new ArrayList<Node>();
	private static ConcurrentHashMap<ProblemModule,Client> Clients = new ConcurrentHashMap<ProblemModule,Client>();
	private static ConcurrentHashMap<Integer,Node> Nodes = new ConcurrentHashMap<Integer,Node>();
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
		//System.out.println("ServerSays: Client and Task Added");
	}

	public synchronized void addNode(Node n){
		System.out.println("adding Node " + n.getSocket());
		getNodes().put(n.getSocket().getLocalPort(),n);
		//Nodes.add(n);
	}

	//TODO: public void returnTask(ProblemModule Task){}

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
	public void setCPort(int P){CPort = P;}

	@Override
	public void setNPort(int P){NPort = P;}	

	public Client getTaskfromQueue() throws InterruptedException{
		ProblemModule Task = Tasks.take(); //is a synchronized operation.
		//System.out.println("ServerSays: getting task from queue");
		return Clients.get(Task);
	}

	//this is where the call to the filter strategy would go.
	public synchronized ArrayList<Node> ScheduleNodes(ProblemModule task) throws IOException{
		//System.out.println("ServerSays: SchedulingNodes");
		ArrayList<Node> ReadyNodes = new ArrayList<Node>();
		for(Integer i : getNodes().keySet()){
			Node N = Nodes.get(i);
			if(N.getStatus() == 1 && !N.isScheduled()){
				//	System.out.println(N + " being added to readyNodes");
				ReadyNodes.add(N);
			}	 
		}
		ProblemModule[] subTasks = task.breakDown(ReadyNodes.size());
		//System.out.println("number of ready nodes: " + ReadyNodes.size());
		ArrayList<Node> ScheduledNodes = new ArrayList<Node>();
		for(int i = 0; i<subTasks.length; i++){
			Node n = ReadyNodes.get(i);
			System.out.println("node " + n.getSocket() + " added to scheduled nodes");
			n.setScheduled(true);
			n.sendTask(subTasks[i]);
			ScheduledNodes.add(n);
			System.out.println( "Sending to socket " + ScheduledNodes.get(i).getSocket());
		}
		System.out.println("ServerSays: Scheduling Nodes Complete");
		PS1.addAll(ScheduledNodes);
		//System.out.println("PS1 socket = " + PS1.get(0).getSocket());
		return ScheduledNodes;
	}

	@Override
	public void run() {
		//MTSE.submit(task)
	}

	@Override
	public void shutdown() throws UnknownHostException, IOException {
		CL.Shutdown();
		NL.Shutdown();
	}

	public ConcurrentHashMap<Integer,Node> getNodes() {return Nodes;}

	/*public boolean getTaskReady(Socket n){
		//System.out.println("Checking socket + " + n);
		return Nodes.get(n).problemReady();
	}*/
	
	public boolean getTaskReady(Node n){
		return Nodes.get(n.getSocket().getLocalPort()).problemReady();
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
		Socket[] clients = new Socket[100];
		while(running){
			try {
				System.out.println("ClientListener Waiting for Connection " + CPort);
				for(int i = 0; i<100; i++){
					clients[i] = ClientListener.accept();
					System.out.println("ClientConnected");
					Parent.submitTask(new Client(clients[i],Parent));
				}
			} catch (IOException e) {System.out.println("Client Listener Closed");}	
		}
	}

	public void Shutdown() throws IOException{
		running = false;
		ClientListener.close();
	}

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
		Socket[] nodes = new Socket[100];
		while(running){
			try {
				System.out.println("NodeListener Waiting for Connection: ");
				for(int i = 0; i<100; i++){
					nodes[i] = NodeListener.accept();
					//Socket node = NodeListener.accept();
					System.out.println("NodeConnected");
					Parent.submitTask(new Node(nodes[i],Parent));
				}
			} catch (IOException e) {
				System.out.println("nodeListener Closed");
			}	
		}
	}

	public void Shutdown() throws IOException{
		running = false;
		NodeListener.close();
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
	private Boolean TaskComplete,scheduled;

	
	@Override
	public int hashCode(){
		   int hashcode = 5;
		   hashcode = 89*hashcode + (this.DataOut.hashCode());
		   hashcode = 89*hashcode + (this.DataIn.hashCode());
		   hashcode = 89*hashcode + (this.getSocket().hashCode());
		   return hashcode;
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null || this.getClass() != obj.getClass()) return false;
		if(this.getSocket() == ((Node) obj).getSocket());
		return true;
	}
	
	private void setupStreams() throws IOException{
		DataOut = new DataOutputStream(Node.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		DataIn = new DataInputStream(Node.getInputStream());
		obIn = new ObjectInputStream(DataIn);
		hashCode();
	}

	public Node(Socket node, MultiThreadedServerConnectionManager parent) throws IOException {
		Node = node;
		Parent = parent;
		scheduled = new Boolean(false);
		TaskComplete = new Boolean(false);
	}

	public Socket getSocket() {return Node;}


	public void sendTask(ProblemModule task) throws IOException{
		Task = task;
		TaskComplete = false;
		obOut.writeObject(Task);
	}

	public ProblemModule retrieveTask(){
		System.out.println("ServerSays: ProblemModule collected from node" + this);
		ProblemModule returnMod = Task;
		Task = null;
		TaskComplete= false;
		setScheduled(false);
		return returnMod;	
	}

	public boolean problemReady(){
		System.out.println("Nodes problemReady() " + this.getSocket() + " Problem ready = " + TaskComplete);
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
				if(this.isScheduled()){System.out.println(this + "TaskComplete = " + TaskComplete);}
				Object RecievedObj = obIn.readObject();
				//System.out.println("ServerSays: Object Recieved from node" + this);
				if((RecievedObj) instanceof ProblemModule){
					Task = (ProblemModule) RecievedObj;
					TaskComplete = true;
					System.out.println("ServerSays: PM returned from node " + this.getSocket());
					//System.out.println("ServerSays: node internal call to Parent.getTaskReady(this) returns " + Parent.getTaskReady(this));

				}else if(RecievedObj instanceof Packets){
					if((RecievedObj instanceof Status)){
						status = ((Status) RecievedObj).getStatus();
						System.out.println("ServerSays: Node" + this.getSocket() +" status " + status);
					}
				}
			}
			status = 0;
			Node.close();
		}
		catch (IOException e) {e.printStackTrace();} 
		catch (ClassNotFoundException e) {e.printStackTrace();}
	}

	public boolean isScheduled() {return scheduled;}

	public void setScheduled(boolean scheduled) {this.scheduled = scheduled;}
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
		for(int i=0; i<workers.size();i++){
		//if(!Parent.getTaskReady(workers.get(i))){
			if(!workers.get(i).problemReady()){
				System.out.println("allReady says Node " + workers.get(i) + " not ready");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run() {
		while(running){
			try{
			System.out.println("getting Task from queue");
			C = Parent.getTaskfromQueue();
			Task = C.getProblem();
			workers = Parent.ScheduleNodes(Task);

			//Parent.ScheduleNodes(Task);
			//workers = Parent.getPS1();

			System.out.println("Creating subTask");
			subTasks = new ProblemModule[workers.size()];
			while(!allReady()){Thread.sleep(3000);}
			System.out.println("Problem Servicer says: All subtaks ready");
			for(int i=0; i<subTasks.length;i++){
				subTasks[i] = workers.get(i).retrieveTask();
				//subTasks[i] = Parent.getPS1().get(i).retrieveTask();
			}
			Task.finalize(subTasks);
			C.completeTask(Task);
			System.out.println("TaskCompleted");
		}
		catch (InterruptedException e) {e.printStackTrace();}
		catch (IOException e1) {e1.printStackTrace();}
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