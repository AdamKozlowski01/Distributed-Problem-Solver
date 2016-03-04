package problemModule;

public class DenialOfService implements ProblemModule {
	

	private static final long serialVersionUID = 1L;
	private int id;
	private int attempts;
	private String hostName;
	private int portNumber;
	
	/*
	 * Default constructor
	 */
	public DenialOfService(int numAttempts, String host, int port) {
		id = 0;
		attempts = numAttempts;
		hostName = host;
		portNumber = port;
	}
	
	public DenialOfService(int numAttempts, String host, int port, int childID) {
		id = childID;
		attempts = numAttempts;
		hostName = host;
		portNumber = port;
	}

	@Override
	public ProblemModule[] breakDown(Integer nodes) {
		ProblemModule[] distributed = new ProblemModule[nodes];
		
		
		return null;
	}

	@Override
	public void Solve() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finalize(ProblemModule[] subproblems) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object TestSolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean TEQ(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void DelaySolve() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}
	
}