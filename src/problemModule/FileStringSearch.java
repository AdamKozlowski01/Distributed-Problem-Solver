package problemModule;

public class FileStringSearch implements ProblemModule {
	private String fileName;
	private String target;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/*
	 * Default constructor
	 */
	public FileStringSearch(String file, String targetString) {
		fileName = file;
		target = targetString; 
	}

	@Override
	public ProblemModule[] breakDown(Integer nodes) {
		// TODO Auto-generated method stub
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