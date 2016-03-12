package MVP.problemModule;

import java.util.Arrays;

public class HardcodedTestProblem implements MVP.problemModule.ProblemModule{

	
	private static final long serialVersionUID = 5L;
	private Integer id = 0;
	private boolean sub = false;//just for testing
	private Integer SubNStart,SubMStart,SubNEnd,SubMEnd,SubCount; //Sub's are for their position in the parent array.SubCount is to make sure all the subproblems are returned.
	//private Integer[][] A,B;
	//static Integer[][] A = {{1,2,3},{7,8,9}}; //The first matrix 
	//static Integer[][] B = {{5,6,7},{3,4,5}}; //The second matrix
	static Integer[][] A = {{1,2,3},{7,8,9},{10,11,12},{13,14,15}};
	static Integer[][] B = {{5,6,7},{3,4,5},{1,4,6},{1,4,5}};
	private Integer[][] Result;
	Integer N = 4;
	Integer M = 3;
	private boolean GPUReady=false;
	
	//TODO: update breakdown and finalize to utalize multiple nodes.
	public HardcodedTestProblem(){}
	
	@Override
	public ProblemModule[] breakDown(Integer nodes){
		//int used = Math.floorDiv(nodes, 2);
		//if(used == 0 && nodes == 1){used = 1;}
		if(nodes == 1){
			HardcodedTestProblem[] subprobs = new HardcodedTestProblem[1];
			subprobs[0] = this;
			subprobs[0].setSubMEnd(A[0].length);
			return subprobs;
		}else{
		HardcodedTestProblem[] subprobs = new HardcodedTestProblem[2];
			this.setSubCount(2);		
			subprobs[0] = new HardcodedTestProblem();
			HardcodedTestProblem temp = subprobs[0];
			temp.setID(1);
			temp.setSubNStart(0);
			temp.setSubNEnd(1);
			temp.setSubMStart(0);
			temp.setSubMEnd(2);
			
			subprobs[1] = new HardcodedTestProblem();
			temp = subprobs[1];
			temp.setID(2);
			temp.setSubNStart(2);
			temp.setSubNEnd(3);
			temp.setSubMStart(0);
			temp.setSubMEnd(2);
			for(int k = 0; k<subprobs.length; k++){
				temp.setID(k+1);
				temp.setSubNStart(k*this.getSubCount());
				temp.setSubNEnd(N*this.getSubCount());
			}
			Integer[][] temparray;
			for(int i = 0; i<subprobs.length; i++){
				temparray = matrixPartCopy(A,temp.getSubNStart(),temp.getSubMStart(),temp.getSubNEnd(),temp.getSubMEnd());
				subprobs[i].setA(temparray);
				temparray = matrixPartCopy(B,temp.getSubNStart(),temp.getSubMStart(),temp.getSubNEnd(),temp.getSubMEnd());
				subprobs[i].setB(temparray);
			}
			return subprobs;
		}		
	//return subprobs;
	}
	
	private Integer[][] matrixPartCopy(Integer[][] source,int StartN,int StartM,int EndN,int EndM){
		Integer[][] result = new Integer[EndN-StartN][EndM-StartM];
		
		return result;
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
	
	public void setA(Integer[][] a){
		A=a;
	}
	
	public Integer[][] getA(){
		return A;
		
	}
	

	public void setB(Integer[][] b){
		B=b;
	}
	
	public Integer[][] getB(){
		return B;
		
	}

}