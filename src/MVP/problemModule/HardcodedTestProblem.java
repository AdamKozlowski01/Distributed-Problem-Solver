package MVP.problemModule;

import java.util.Arrays;

public class HardcodedTestProblem implements MVP.problemModule.ProblemModule{

	
	private static final long serialVersionUID = 1L;
	private Integer id = 0;
	private boolean sub = false;//just for testing
	private Integer SubNStart,SubMStart,SubNEnd,SubMEnd,SubCount; //Sub's are for their position in the parent array.SubCount is to make sure all the subproblems are returned.
	//private Integer[][] A,B;
	static Integer[][] A = {{1,2,3},{7,8,9}}; //The first matrix 
	static Integer[][] B = {{5,6,7},{3,4,5}}; //The second matrix
	private Integer[][] Result;
	Integer N = 2;
	Integer M = 3;
	private boolean GPUReady=false;
	

	public HardcodedTestProblem(){}
	
	@Override
	public ProblemModule[] breakDown(Integer nodes){
		//int used = Math.floorDiv(nodes, 2);
		int used = 1;
		ProblemModule[] subprobs = new ProblemModule[used];
		subprobs[0] = this;
		return subprobs;
	}
	
	@Override
	public Integer[][] TestSolver(){
		Integer[][]TestResult = new Integer[N][M];
		for(int j = 0; j< N; j++){
			for(int k = 0; k< M; k++){
				TestResult[j][k] = A[j][k] + B[j][k];
			}
		}
		return TestResult;
	}
	
	@Override
	public boolean TEQ(Object TestResult){
		if(TestResult instanceof Integer[][]){
		return Arrays.deepEquals((Integer[][])TestResult, this.Result);
		}
		return false;
	}

	@Override
	public void Solve() {
		Result = new Integer[N][M];
		for(int j = 0; j< N; j++){
			for(int k = 0; k< M; k++){
				Result[j][k] = A[j][k] + B[j][k];
			}
		}
	}
	
	@Override
	public void DelaySolve() throws InterruptedException {
		Result = new Integer[N][M];
		for(int j = 0; j< N; j++){
			for(int k = 0; k< M; k++){
				Result[j][k] = A[j][k] + B[j][k];
			}
		}
		Thread.sleep(1000*10);
	}


	@Override
	public void finalize(ProblemModule[] subproblems) {
		 HardcodedTestProblem T = (HardcodedTestProblem) subproblems[0];
		 Result = T.getResult();
	}
	
	public Integer[][] getResult(){
		return Result;
	}
	
	public Integer getID(){
		return id;
	}
	
	public void setID(Integer i){
		id = i;
	}
	
	public Integer getSubNStart() {
		return SubNStart;
	}


	public void setSubNStart(Integer subNStart) {
		SubNStart = subNStart;
	}


	public Integer getSubMStart() {
		return SubMStart;
	}


	public void setSubMStart(Integer subMStart) {
		SubMStart = subMStart;
	}


	public boolean isSub() {
		return sub;
	}


	public void setSub(boolean sub) {
		this.sub = sub;
	}


	public boolean isGPUReady() {
		return GPUReady;
	}


	public void setGPUReady(boolean gPUReady) {
		GPUReady = gPUReady;
	}


	public Integer getSubNEnd() {
		return SubNEnd;
	}


	public void setSubNEnd(Integer subNEnd) {
		SubNEnd = subNEnd;
	}


	public Integer getSubMEnd() {
		return SubMEnd;
	}


	public void setSubMEnd(Integer subMEnd) {
		SubMEnd = subMEnd;
	}


	public Integer getSubCount() {
		return SubCount;
	}


	public void setSubCount(Integer subCount) {
		SubCount = subCount;
	}

}