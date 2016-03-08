package MVP.Client;

import java.io.IOException;
import java.net.UnknownHostException;

import MVP.problemModule.HardcodedTestProblem;
import MVP.problemModule.ProblemModule;
import MVP.problemModule.TestProblemModule;

//Still only for matrix addition but way easier to run JUnit Tests with for testing different configurations.
public class ModularTestClient {

	private static Integer[][] A;
	private static Integer[][] B; //The second matrix
	private static Integer[][] TestResult; //The expected result from the grid
	private static Integer N = 2; 
	private static Integer M = 3;
	private static String Host;
	private static int Port;
	private static boolean Success;
	private static ProblemModule tosend;
	private static Object recv;
	private static SingleThreadClientConnectionManager CCM;
	//private static MultiThreadedClientconnectionManager MTCCM;

	public ModularTestClient(){

	}

	public ModularTestClient(String host,int port){
		Host = host;
		Port = port;
	}

	public ModularTestClient(String host,int port,Integer[][] a,Integer[][] b){
		Host = host;
		Port = port;
		A = a;
		B = b;
		N = A.length;
		M = A[0].length;
	}

	public void Start() throws IOException, ClassNotFoundException{
		CCM.writeObject(tosend);
		recv = CCM.readObject();
		computeTestResult();
		TEQ();
	}

	public void MultiThreadStart(){
		//TODO: Future/Stretch
	}

	//use this to test known working client config.
	public void startWithDefaults(String host,int port,int n,int m) throws UnknownHostException, IOException, ClassNotFoundException{
		randMatricies(n,m);
		//packProblemModule();
	
		tosend = new HardcodedTestProblem();
		setSingleThreadClientConnectionManager(new TestClientConnectionManager(host,port));
		System.out.println("Client sending module");
		CCM.writeObject(tosend);
		recv = CCM.waitForResult();
		System.out.println("Client Recieved result");
		computeTestResult();
		TEQ();
	}


	//Getters
	public Integer[][] getA(){return A;}
	public Integer[][] getB(){return B;}
	public Integer getN(){return N;}
	public Integer getM(){return M;}
	public String getHost(){return Host;}
	public int getPort(){return Port;}
	public boolean getSuccess(){return Success;}
	public ProblemModule getProblemModule(){return tosend;}
	public Integer[][] getTestResult(){
		TestResult = new Integer[N][M];
		TestResult = (Integer[][]) tosend.TestSolver(); 
		return TestResult;
	}

	//Setters
	public void setSingleThreadClientConnectionManager(SingleThreadClientConnectionManager ccm){CCM = ccm;}
	public void setA(Integer[][] a){A = a;}
	public void setB(Integer[][] b){B = b;}	
	public void setHost(String host){Host=host;}
	public void setPort(int port){Port=port;}
	public void setProblemModule(ProblemModule p){tosend = p;}

	//Utility
	public void computeTestResult(){
		TestResult = new Integer[N][M];
		TestResult = (Integer[][]) tosend.TestSolver(); 
	}

	public void packProblemModule(){
		tosend = new TestProblemModule(A,B,N,M);
	}

	public void randMatricies(int n,int m){
		A=new Integer[n][m];
		B=new Integer[n][m];
		A=getTestMatrix(n,m);
		B=getTestMatrix(n,m);
		recalcNM();
	}

	public void recalcNM(){
		N = A.length;
		M = A[0].length;
	}

	public boolean TEQ(){
		if(recv instanceof ProblemModule){
			if(((ProblemModule) recv).TEQ((Object)TestResult)){
				Success = true;
				return true;
			}else{
				Success = false;
				return false;
			}
		} else { System.out.println("Woah! Object Recieved is not a ProblemModule!");}
		return false;
	}


	public static Integer[][] getTestMatrix(Integer n, Integer m){	
		Integer [][] Matrix = new Integer[n][m];
		int item = 0;
		Integer Item = 0;
		for(int i=0;i<n;i++){
			for(int k=0;k<m;k++){
				item = (int)(Math.random()*100); 
				Item = (Integer) item;
				Matrix[i][k] = Item;
			}
		}
		return Matrix;
	}

	//adding Future MultiThreadedClient Support;
	/*
	public static MultiThreadedClientconnectionManager getMTCCM() {
		return MTCCM;
	}

	public static void setMTCCM(MultiThreadedClientconnectionManager mTCCM) {
		MTCCM = mTCCM;
	}
	*/
}

