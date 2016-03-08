package MVP.problemModule;

import java.io.Serializable;

public interface ProblemModule extends Serializable{
		public ProblemModule[] breakDown(Integer nodes);
		public void Solve();
		public void finalize(ProblemModule[] subproblems);
		//Testing Methods
		public Object TestSolver();
		public boolean TEQ(Object o);
		void DelaySolve() throws InterruptedException;
		//TestMethod
		
	}
