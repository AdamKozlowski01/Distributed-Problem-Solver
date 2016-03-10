package server.TestServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public class AuthenticatingServer {

	private static ExecutorService executorService = Executors.newFixedThreadPool(20);
	
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
	public void handleConnection() throws IOException, ClassNotFoundException{ //eventually this needs to be multithreaded
		Socket node = server.accept();
		executorService.execute(new HandleNodeConnection(this, node));
	}
	
	//handle getting a job from the user and distributing it
	public void getNewProblem(ProblemModule m){
		problemModulesToSolve.add(m);
	}
	
	public void distributeWork() throws IOException{
		//check if there are enough nodes to solve a problem. Minimum 2.
		if(problemModulesToSolve.size() > 0){
			ProblemModule work = problemModulesToSolve.remove(0);
			ProblemModule[] breakdown = work.breakDown(10); //change to the total number of available nodes
			for(ProblemModule m : breakdown){
				problemModuleBrokenDown.add(m);
				//send the work to individual nodes
				Iterator it = nodeConnectionStatus.entrySet().iterator();
				while(it.hasNext()){
					Map.Entry pair = (Map.Entry)it.next();
					if((boolean) pair.getValue() && !nodeWorkStatus.get(pair.getKey())){
						//send work
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
}

//-----------------------------------------------------------------------------------------------
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
				if(s.nodeConnectionStatus.get(nodeID) != null && !s.nodeConnectionStatus.get(nodeID)){
					System.out.println("New node connected: "+handleNodeReconnect(node, nodeID));
					//keep alive
				}else{ //or remove that client from the list of available clients
					System.out.println("Node disconnecting: "+handleNodeDisconnect(nodeID));
				}
	
			}
		}catch(Exception e){
			
		}
	}
	
}