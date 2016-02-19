package problemModule;

import java.util.Arrays;

//This is the Base ProblemModule
public class TestProblemModule implements ProblemModule{

	private static final long serialVersionUID = 1L;
	private Integer id = 0;
	private boolean sub = false;//just for testing
	private Integer N,M,SubNStart,SubMStart,SubNEnd,SubMEnd,SubCount; //Sub's are for their position in the parent array.SubCount is to make sure all the subproblems are returned.
	private Integer[][] A,B;
	private Integer[][] Result;
	

	public TestProblemModule(Integer[][] a,Integer[][] b,Integer n,Integer m){
		A=a;B=b;N=n;M=m;
	}
	
	
	@Override
	public ProblemModule[] breakDown(Integer nodes){
		//**Breakdown algorithm
		//First divide the array into at most nodes pieces
		//create a ProblemModule containing each sub piece.
		return null;
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
		for(int j = 0; j< N; j++){
			for(int k = 0; k< M; k++){
				Result[j][k] = A[j][k] + B[j][k];
			}
		}
	}

	@Override
	public void finalize(ProblemModule[] subproblems) {
		// TODO Auto-generated method stub
		
	}
	
	private Integer[][] SubCpy(){
		return null;
	}

	private void SetSubMStart(Integer m){
		SubMStart = m;
	}
	
	private void SetSubNStart(Integer n){
		SubNStart = n;
	}
	
	private void SetSubMEnd(Integer m){
		SubMEnd = m;
	}
	
	private void SetSubNEnd(Integer n){
		SubNEnd = n;
	}
	
	public Integer getID(){
		return id;
	}
	
	public void setID(Integer i){
		id = i;
	}
	/* Deprecated with the internalization of the Problem Specific Methods.
	public Integer[][] getMatrixA(){
		return A;
	}

	public Integer[][] getMatrixB(){
		return B;
	}

	public Integer getN(){
		return N;
	}

	public Integer getM(){
		return M;
	}

	public void setResult(Integer[][] result){
		Result = result;
	}

	public Integer[][] getResult(){
		return Result;
	}
	*/
}