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
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import MVP.Common.ID;
import MVP.Common.KeepAlive;
import MVP.Common.Packets;
import MVP.Common.Status;
import problemModule.HardcodedTestProblem;
import problemModule.ProblemModule;
import problemModule.TestProblemModule;

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
public class AuthenticatingServer implements ServerConnectionManager {

	//TODO:Choose one of the two executor services and stick with it
	private static ExecutorService executorService = Executors.newFixedThreadPool(20);
	private static ExecutorService MTSE = Executors.newFixedThreadPool(40);
	
	//TODO: I changed the signatures on the hashmaps already, and the support classes at the end of the file.
	int CPort,NPort,PThreads;
	AuthClientListener CL;
	AuthNodeListener NL;
	ProblemServicer PS;
	private static ConcurrentHashMap<ProblemModule,AuthClient> Clients = new ConcurrentHashMap<ProblemModule,AuthClient>();
	private static ConcurrentHashMap<Integer,AuthNode> Nodes = new ConcurrentHashMap<Integer,AuthNode>();
	private static final BlockingQueue<ProblemModule> Tasks = new LinkedBlockingQueue<ProblemModule>(); 

	
	/*
	public ConcurrentHashMap<Long, Socket>  nodeInfo;
	public ConcurrentHashMap<Long, Boolean> nodeWorkStatus;
	public ConcurrentHashMap<Long, Boolean> nodeConnectionStatus;
	public ConcurrentHashMap<Long, Boolean> nodeIDTaken;
	public ConcurrentHashMap<Long, InetAddress> nodeAddress;

	//keep track of ProblemModule pieces
	private ArrayList<ProblemModule> problemModulesToSolve;
	public ArrayList<ProblemModule> problemModuleBrokenDown;
	public ArrayList<ProblemModule> problemModuleSolved;
	 
	private static ServerSocket server;
	private static int port;
	private ObjectOutputStream objectOut;
	*/

	
	public AuthenticatingServer(int port) throws IOException{
		this.port = port;
		server = new ServerSocket(port);
		nodeInfo = new ConcurrentHashMap<Long, Socket>();
		nodeWorkStatus = new ConcurrentHashMap<Long, Boolean>();
		nodeConnectionStatus = new ConcurrentHashMap<Long, Boolean>();
		nodeIDTaken = new ConcurrentHashMap<Long, Boolean>();
		problemModulesToSolve = new ArrayList<ProblemModule>();
		problemModuleBrokenDown = new ArrayList<ProblemModule>();
		problemModuleSolved = new ArrayList<ProblemModule>();
	}

	//listen for a new connection from a node
	//TODO: Take my Connection logic from MultiThreadedServerConnectionManager
	/*public void handleConnection() throws IOException, ClassNotFoundException{ //eventually this needs to be multithreaded
		Socket node = server.accept();
		executorService.execute(new HandleNodeConnection(this, node));
	}*/

	//handle getting a job from the user and distributing it
	public void getNewProblem(ProblemModule m){
		problemModulesToSolve.add(m);
	}

	public void distributeWork() throws IOException{ //TODO: My equivalent method is the ScheduleNodes Method.
		//check if there are enough nodes to solve a problem. Minimum 2.
		if(problemModulesToSolve.size() > 0 && nodeInfo.size() > 0){
			ProblemModule work = problemModulesToSolve.remove(0);
			ProblemModule[] breakdown = work.breakDown(1); //change to the total number of available nodes
			for(ProblemModule m : breakdown){
				problemModuleBrokenDown.add(m);
				//send the work to individual nodes
				Iterator it = nodeConnectionStatus.entrySet().iterator();
				while(it.hasNext()){
					Map.Entry pair = (Map.Entry)it.next();
					if((boolean) pair.getValue() && !nodeWorkStatus.get(pair.getKey())){
						//send work
						//objectOut = pair.getValue()
						objectOut.writeObject(m);
						nodeWorkStatus.replace((Long) pair.getKey(), true);
						nodeInfo.get(pair.getKey()).close();
						break;
					}
					//get a list of the hashmap's entries.
					//check if the current one is busy
					//if it's not busy send that one work
					//set the node's work flag to on
					//store any other info about the node's work that we need to.

					//put a queue here so that future nodes that connect can be given work immediately (FOR THE FUTURE)
				}

			}
		}
	}
	
	
	
	
	
	
	

	@Override
	public void StartServer() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() throws UnknownHostException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCPort(int P) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNPort(int P) {
		// TODO Auto-generated method stub

	}

	public AuthClient getTaskfromQueue() {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<AuthNode> ScheduleNodes(ProblemModule task) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addNode(AuthNode authNode) {
		// TODO Auto-generated method stub
		
	}

	public void addClient(AuthClient authClient) {
		// TODO Auto-generated method stub
		
	}
}

//-----------------------------------------------------------------------------------------------
class AuthClientListener implements Runnable{
	int CPort;
	MultiThreadedServerConnectionManager Parent;
	boolean running;
	ServerSocket ClientListener;
	public AuthClientListener(int cPort, MultiThreadedServerConnectionManager mtscm) throws IOException {
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

class AuthNodeListener implements Runnable{ //TODO: Change to authenticate.
	int NPort;
	MultiThreadedServerConnectionManager Parent;
	boolean running;
	ServerSocket NodeListener;

	public AuthNodeListener(int nPort, MultiThreadedServerConnectionManager mtscm) throws IOException {
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

class AuthClient implements Runnable{
	private DataOutputStream DataOut;
	private ObjectOutputStream obOut;
	private DataInputStream DataIn;
	private ObjectInputStream obIn;
	private Socket Client;
	private AuthenticatingServer Parent;
	ProblemModule Task;
	boolean Complete;

	public AuthClient(Socket client, AuthenticatingServer parent) throws IOException {
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
			Parent.addClient(this);//TODO: Implement this method into your Server body, Can literally take my client related methods unchanged from my implmentation.
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
	private AuthenticatingServer Parent;
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

	public AuthNode(Socket node, AuthenticatingServer parent) throws IOException {
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
			//TODO: here you should add the logic to handle not assigned ID's BEFORE you add the new node to the hashmap,
			Parent.addNode(this); //TODO: implement this Method in your server body. This adds the node to the hashmap.
			while(Node.isConnected() && !Node.isClosed()){ //TODO:Maybe change this condition, as the node will close when the PM is sent to the node.
				if(this.isScheduled()){System.out.println(this + "TaskComplete = " + TaskComplete);}
				Object RecievedObj = obIn.readObject();
				//System.out.println("ServerSays: Object Received from node" + this);
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
					
					if((RecievedObj instanceof ID)){ //TODO: implement a concrete packet called ID that facilitates your connection logic.
							//status = ((Status) RecievedObj).getStatus();
							//System.out.println("ServerSays: Node" + this.getSocket() +" status " + status);
						
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

class AuthProblemServicer implements Runnable{

	private AuthenticatingServer Parent;
	private boolean running = true;
	private ProblemModule Task;
	private AuthClient C;
	private ArrayList<AuthNode> workers;
	private ProblemModule[] subTasks;


	public AuthProblemServicer(AuthenticatingServer mtscm){
		System.out.println("Problem Servicer Started");
		Parent = mtscm;
		workers = new ArrayList<AuthNode>();
	}

	private boolean allReady(){
		for(int i=0; i<workers.size();i++){
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
				C = Parent.getTaskfromQueue(); //TODO: Implement this method in your main server body.
				Task = C.getProblem();
				workers = Parent.ScheduleNodes(Task); //TODO:Implement this method in your main server body.
				System.out.println("Creating subTask");
				subTasks = new ProblemModule[workers.size()];
				while(!allReady()){Thread.sleep(3000);}
				System.out.println("Problem Servicer says: All subtaks ready");
				for(int i=0; i<subTasks.length;i++){
					subTasks[i] = workers.get(i).retrieveTask(); 
				}
				Task.finalize(subTasks);
				C.completeTask(Task);
				System.out.println("TaskCompleted");
			}
			catch (InterruptedException e) {e.printStackTrace();}
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