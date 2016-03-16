package MVP.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import MVP.Common.IdentificationPacket;
import MVP.Common.KeepAlive;
import MVP.Common.Packets;
import MVP.Common.Status;
import MVP.problemModule.ProblemModule;

/*
 * Made by: Adam Kozlowski
 * Made on: 2/28/2016
 * Last Updated: 2/28/2016
 * 
 *  Command and Control Server for the Problem Distribution Net.
 *  The server listens for a connection from a node.
 *  If the node sends a -1 as UTF String, the server assigns it a uniqueID number and then uses that ID to store
 *  info about the node in hash tables.
 *  
 *  If a node is in the network it can be sent work at anytime.
 *  Work is sent by serializing a ProblemModule Implementation.
 *  Once the server has sent work to a node it flags that node as
 *  working, and will not bother that node with anymore jobs.
 *  Once the server gets a new connection with a node with work out 
 *  it will immediately expect the class it sent back solved.
 *  
 *  Then the server needs to put the distributed jobs back in order,
 *  call finalize and then print a result.
 *  
 *  TO DO: 
 *  move a lot of the input/output and connection managing to other classes than can be multithreaed
 *  handle job distribution and job return
 *  
 *  Needs to keep track of nodes which are ready for work and keep the connection alive.
 * 	
 *  */


//TODO: the biggest thing is to change all the variable names to the proper hash maps, and make your logic more like my Multithreaded Server Connection Manager
//TODO: You can take my server connection manger entirely and tweak it to add the authentication logic.
public class AuthenticatingConnectionManager implements ServerConnectionManager {

	//TODO:Choose one of the two executor services and stick with it
	private static ExecutorService MTSE = Executors.newFixedThreadPool(40);

	//TODO: I changed the signatures on the hashmaps already, and the support classes at the end of the file.
	int CPort,NPort,PThreads;
	AuthClientListener CL;
	AuthNodeListener NL;
	AuthProblemServicer PS;

	private static ArrayList<Long> idsTaken = new ArrayList<Long>();
	private static ConcurrentHashMap<ProblemModule,AuthClient> Clients = new ConcurrentHashMap<ProblemModule,AuthClient>();
	private static ConcurrentHashMap<Long,AuthNode> Nodes = new ConcurrentHashMap<Long,AuthNode>();
	private static final BlockingQueue<ProblemModule> Tasks = new LinkedBlockingQueue<ProblemModule>(); 

	private ConcurrentHashMap<Long, Boolean> Working = new ConcurrentHashMap<Long, Boolean>();

	private Random rngesus;

	@Override
	public void StartServer() throws IOException {
		CL = new AuthClientListener(CPort,this);
		NL = new AuthNodeListener(NPort,this);
		PS = new AuthProblemServicer(this);
		MTSE.execute(CL);
		MTSE.execute(NL);
		MTSE.execute(PS);
		rngesus = new Random();
	}

	public AuthenticatingConnectionManager(int cPort, int nPort,int pThreads) {
		CPort = cPort;
		NPort = nPort;
		PThreads = pThreads;

	}

	public boolean isNodeWorking(long id){
		return Working.get(id);
	}

	public long generateNewID(){
		long nextID = rngesus.nextLong();
		while(nextID == -1 || idsTaken.contains(nextID)){
			rngesus.nextLong(); //if only I could wrap that in a method called takeTheWheel();
		}
		idsTaken.add(nextID);
		return nextID;

	}

	@Override
	public void shutdown() throws UnknownHostException, IOException {
		CL.Shutdown();
		NL.Shutdown();

	}

	@Override
	public void setCPort(int P) {
		CPort = P;
	}

	@Override
	public void setNPort(int P) {
		NPort = P;
	}

	public AuthClient getTaskfromQueue() throws InterruptedException {
		ProblemModule Task = Tasks.take(); //is a synchronized operation.
		//System.out.println("ServerSays: getting task from queue");
		return Clients.get(Task);
	}

	public synchronized ArrayList<AuthNode> ScheduleNodes(ProblemModule task) throws IOException, InterruptedException {
		//System.out.println("ServerSays: SchedulingNodes");
		ArrayList<AuthNode> ReadyNodes = new ArrayList<AuthNode>();
		int count =0;
		//bad hack I am about to do.
		while(ReadyNodes.size()==0){
			if(count != 0){
				Thread.sleep(3000);
			}
			for(Long i : getNodes().keySet()){
				AuthNode N = Nodes.get(i);
				if(N.getStatus() == 1 && !N.isScheduled()){
					//	System.out.println(N + " being added to readyNodes");
					ReadyNodes.add(N);
				}	 
			}
			count++;
		}





		ProblemModule[] subTasks = task.breakDown(ReadyNodes.size());
		//System.out.println("number of ready nodes: " + ReadyNodes.size());
		ArrayList<AuthNode> ScheduledNodes = new ArrayList<AuthNode>();
		for(int i = 0; i<subTasks.length; i++){
			AuthNode n = null;
			if(i < ReadyNodes.size())
				n = ReadyNodes.get(i);
			else
				break;
			System.out.println("node " + n.getID() + " added to scheduled nodes");
			n.setScheduled(true);
			this.Working.replace(n.getID(), true);
			n.sendTask(subTasks[i]);
			ScheduledNodes.add(n);
			System.out.println( "Sending to Node " + ScheduledNodes.get(i).getID());
		}
		System.out.println("ServerSays: Scheduling Nodes Complete");
		//PS1.addAll(ScheduledNodes);
		//System.out.println("PS1 socket = " + PS1.get(0).getSocket());
		return ScheduledNodes;
	}

	public void addNode(AuthNode authNode, long id) {
		//System.out.println("adding Node " + authNode.getSocket());
		getNodes().put(id,authNode);
		//Nodes.add(n);

	}

	public void addNodeToWorkers(long id){
		Working.put(id, false);
	}

	public ConcurrentHashMap<Long, AuthNode> getNodes() {return Nodes;}

	public void addClient(AuthClient c) {
		Clients.put(c.getProblem(), c);
		Tasks.add(c.getProblem());
	}

	public synchronized void submitTask(Runnable R){
		MTSE.execute(R);
	}


	public void removeNode(long id) {
		Nodes.remove(id);
	}
	public boolean hasWorkOut(long id){
		return Working.get(id);
	}

	public boolean idAssigned(long id){
		return idsTaken.contains(id);
	}

	public void setWorkerStatus(long id, boolean status){
		Working.replace(id, status);
	}
}

//-----------------------------------------------------------------------------------------------
class AuthClientListener implements Runnable{
	int CPort;
	AuthenticatingConnectionManager Parent;
	boolean running;
	ServerSocket ClientListener;
	public AuthClientListener(int cPort, AuthenticatingConnectionManager acm) throws IOException {
		CPort = cPort;
		Parent = acm;
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
					Parent.submitTask(new AuthClient(clients[i],Parent));
				}
			} catch (IOException e) {System.out.println("Client Listener Closed");}	
		}
	}

	public void Shutdown() throws IOException{
		running = false;
		ClientListener.close();
	}

}

class AuthNodeListener implements Runnable{ //TODO: Change to authenticate.
	int NPort;
	AuthenticatingConnectionManager Parent;
	boolean running;
	ServerSocket NodeListener;

	public AuthNodeListener(int nPort, AuthenticatingConnectionManager mtscm) throws IOException {
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
					System.out.println("Node Connected");
					Parent.submitTask(new AuthNode(nodes[i],Parent));
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

class AuthClient implements Runnable{
	private DataOutputStream DataOut;
	private ObjectOutputStream obOut;
	private DataInputStream DataIn;
	private ObjectInputStream obIn;
	private Socket Client;
	private AuthenticatingConnectionManager Parent;
	ProblemModule Task;
	boolean Complete;

	public AuthClient(Socket client, AuthenticatingConnectionManager parent) throws IOException {
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

class AuthNode implements Runnable{
	private DataOutputStream DataOut;
	private ObjectOutputStream obOut;
	private DataInputStream DataIn;
	private ObjectInputStream obIn;
	private Socket Node;
	private AuthenticatingConnectionManager Parent;
	private int status;
	private ProblemModule Task;
	private Boolean TaskComplete,scheduled;
	private long id;


	private void setupStreams() throws IOException{
		DataOut = new DataOutputStream(Node.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		DataIn = new DataInputStream(Node.getInputStream());
		obIn = new ObjectInputStream(DataIn);
		hashCode();
	}

	public AuthNode(Socket node, AuthenticatingConnectionManager parent) throws IOException {
		Node = node;
		Parent = parent;
		scheduled = new Boolean(false);
		TaskComplete = new Boolean(false);
		id = -1;
	}

	public Socket getSocket() {return Node;}


	public void sendTask(ProblemModule task) throws IOException{
		Task = task;
		TaskComplete = false;
		obOut.writeObject(Task);
	}

	public ProblemModule retrieveTask(){
		System.out.println("ServerSays: ProblemModule collected from node" + this.getID());
		ProblemModule returnMod = Task;
		Task = null;
		setScheduled(false);
		return returnMod;	
	}

	public boolean problemReady(){
		System.out.println("Nodes problemReady() " + this.getID() + " Problem ready = " + TaskComplete);
		return TaskComplete;
	}

	public int getStatus(){
		return status;
	}

	public boolean taskComplete(){
		return TaskComplete;
	}
	@Override
	public void run() {
		try {
			setupStreams();
			Object recv = obIn.readObject();
			if(recv instanceof IdentificationPacket){
				id = ((IdentificationPacket)recv).getID();
				if(id == -1){
					id = Parent.generateNewID();
					obOut.writeObject(new IdentificationPacket(id));
					Parent.addNodeToWorkers(id);
					System.out.println("New Node joined net.  ID " + id + " assigned.");
				}
				if(!Parent.idAssigned(id)){
					System.out.println("Attempt to join the network with id " + id + " when id has not been assigned.\nShutting Down Connection");
					Node.close();
					return;
				}
				System.out.println("Node " + id +" reconnected");
				Parent.addNode(this, id);
				while(Node.isConnected() && !Node.isClosed()){ 
					Object RecievedObj = obIn.readObject();
					if(RecievedObj instanceof ProblemModule){
						//check to see if we gave work to that node
						if(Parent.hasWorkOut(id)){
							Task = (ProblemModule) RecievedObj;
							TaskComplete = true;
							System.out.println("PM returned from node " + this.id);
							Parent.PS.collect(this);

						}else{
							System.out.println("Recieved a PM from Node " + id + " when Node did not have work out.");
						}
					}
					else if(RecievedObj instanceof Packets){
						if((RecievedObj instanceof Status)){
							status = ((Status) RecievedObj).getStatus();
							System.out.println("ServerSays: Node " + id +" status " + status);
						}							
						if((RecievedObj instanceof IdentificationPacket)){ //TODO: implement a concrete packet called ID that facilitates your connection logic.
							//assume the node is disconnecting and remove it from the hash tables.
							System.out.println("Node " + id + " disconnected");
							Parent.removeNode(id);
							Node.close();

						}
					}
				}
				status = 0;
				Node.close();
			}else{
				System.out.println("Expected ID packet on thread: " + Thread.currentThread().getName() + "\nShutting Down Connection");
				Node.close();
				return;
			}
		}
		catch (IOException e) {e.printStackTrace();} 
		catch (ClassNotFoundException e) {e.printStackTrace();}
	}

	public boolean isScheduled() {return scheduled;}

	public void setScheduled(boolean scheduled) {this.scheduled = scheduled;}

	public long getID(){
		return id;
	}
}

class AuthProblemServicer implements Runnable{

	private AuthenticatingConnectionManager Parent;
	private boolean running = true;
	private ProblemModule Task;
	private AuthClient C;
	private ArrayList<AuthNode> workers;
	//private ArrayList<Long> workerIDs;
	private ProblemModule[] subTasks;
	private int subTaskIndex;
	private int numberOfProblems;


	public void collect(AuthNode n){
		subTasks[subTaskIndex] = n.retrieveTask();
		if(subTaskIndex == numberOfProblems){
			Task.finalize(subTasks);
			C.completeTask(Task);
			System.out.println("TaskCompleted");
			return;
		}
		subTaskIndex++;
	}

	public AuthProblemServicer(AuthenticatingConnectionManager mtscm){
		System.out.println("Problem Servicer Started");
		Parent = mtscm;
		workers = new ArrayList<AuthNode>();
		subTaskIndex = 0;
	}

	private boolean allReady(){
		for(int i=0; i<workers.size();i++){
			if(!workers.get(i).problemReady()){
				System.out.println("allReady says Node " + workers.get(i).getID() + " not ready");
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
				System.out.println("Creating subTask");
				subTasks = new ProblemModule[workers.size()];
				subTaskIndex = 0;
				//int numberOfProblems = workers.size();
				//while(!allReady()){Thread.sleep(3000);}
				System.out.println("Problem Servicer says: All subtaks ready");
				//ArrayList<Long> workerIDs = new ArrayList<Long>();

				/*for(AuthNode n : workers){
					workerIDs.add(n.getID());
				}

				if(index == numberOfProblems){
					Task.finalize(subTasks);
					C.completeTask(Task);
					System.out.println("TaskCompleted");
				}*/
			}
			catch (Exception e) {e.printStackTrace();}
			//	catch (IOException e1) {e1.printStackTrace();}
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
//I'm leaving this here so you can refrence your design, but don't use this class.
/*
class HandleNodeConnection implements Runnable{

	//needs a pointer to all of the Concurrent hashtables in the main server so this class can edit them


	private AuthenticatingServer s;
	private DataInputStream input;
	private ObjectInputStream objectIn;
	private DataOutputStream output;
	private ObjectOutputStream objectOut;
	private Socket node;

	HandleNodeConnection(AuthenticatingServer s, Socket node){
		this.s = s;
		this.node = node;
	}


	private Long handleNodeReconnect(Socket node, Long id){

		s.nodeConnectionStatus.replace(id, true);
		s.nodeWorkStatus.replace(id, false);
		s.nodeInfo.replace(id, node);

		return id;
	}

	private Long handleNodeDisconnect(Long id) throws IOException{

		s.nodeConnectionStatus.replace(id, false);
		s.nodeWorkStatus.replace(id, false);

		input.close();
		output.close();
		s.nodeInfo.get(id).close();
		s.nodeInfo.replace(id, null);

		return id;
	}

	//handle getting a job back solved
	private long handleAnswerReturned(Long id) throws ClassNotFoundException, IOException{

		ProblemModule solvedProblem = (ProblemModule) objectIn.readObject();

		s.nodeWorkStatus.replace(id, false);

		s.problemModuleSolved.add(solvedProblem);
		//sort the problemModuleSolved so our answers are in the right order
		if(s.problemModuleSolved.size() == s.problemModuleBrokenDown.size())
		{
			ProblemModule[] solvedArray = (ProblemModule[]) s.problemModuleSolved.toArray();
			ProblemModule answer = new HardcodedTestProblem(); //for testing only
			answer.finalize(solvedArray);
		}

		return id;

	}

	private long handleNewNode(Socket node) throws IOException{

		//assign a unique ID
		boolean idIsGood = false;
		long randomID = 0;
		Random r = new Random();
		while(!idIsGood){

			//generate a random number
			randomID = r.nextLong();

			if(s.nodeIDTaken.get(randomID) == null)
				idIsGood = true;
			else if(randomID-1 > 0)
				randomID--;
			else
				randomID = r.nextLong();
				//generate a new random number
		}

		s.nodeInfo.put(randomID, node);
		s.nodeConnectionStatus.put(randomID, true);
		s.nodeWorkStatus.put(randomID, false);
		s.nodeIDTaken.put(randomID, true);

		output.writeLong(randomID);

		return randomID;
	}

	//handle a node leaving the network
	@Override
	public void run() {

		try{
			input = new DataInputStream(node.getInputStream());
			objectIn = new ObjectInputStream(input);
			output = new DataOutputStream(node.getOutputStream());
			objectOut = new ObjectOutputStream(output);

			String idAsString;
			idAsString = input.readUTF();
			Long nodeID = Long.parseLong(idAsString);

			if(nodeID == -1L){
				//handle a new node joining the network
				System.out.println("New node connected, uniqueID " + handleNewNode(node) + " assigned");
				//keep alive
			}else if( s.nodeWorkStatus.get(nodeID) != null && s.nodeWorkStatus.get(nodeID) ){ //check if we have a pending job for them
				//handle getting a job back solved
				System.out.println("Getting solution back from: " + handleAnswerReturned(nodeID));
			}else{
				//handle an already ID'd client be available for work
				if(s.nodeConnectionStatus.get(nodeID) != null)
					System.out.println("New node connected: "+handleNodeReconnect(node, nodeID));
					//keep alive
			}
		}catch(Exception e){

		}
	}

}*/