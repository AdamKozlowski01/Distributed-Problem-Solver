package MVP.Client;

import java.io.IOException;
import java.net.UnknownHostException;

import MVP.problemModule.FileStringSearch;
import MVP.problemModule.ProblemModule;

public class FileStringSearchTestClient {
	
	private static String fileName;
	private static String targetString;
	private static String TestResult;
	private static String Host;
	private static int Port;
	private static boolean Success;
	private static ProblemModule tosend;
	private static Object recv;
	private static SingleThreadClientConnectionManager CCM;
	//private static MultiThreadedClientconnectionManager MTCCM;

	public FileStringSearchTestClient(){

	}

	public FileStringSearchTestClient(String host,int port){
		Host = host;
		Port = port;
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
	public void startWithDefaults(String host, int port, String file, String target) throws UnknownHostException, IOException, ClassNotFoundException{
		//packProblemModule();
		
		fileName = file;
		targetString = target;
		tosend = new FileStringSearch(fileName, targetString);
		setSingleThreadClientConnectionManager(new TestClientConnectionManager(host,port));
		System.out.println("Client sending module");
		CCM.writeObject(tosend);
		recv = CCM.waitForResult();
		System.out.println("Client Recieved result");
		computeTestResult();
		TEQ();
	}


	//Getters
	public String getHost(){return Host;}
	public int getPort(){return Port;}
	public boolean getSuccess(){return Success;}
	public ProblemModule getProblemModule(){return tosend;}
	public String getTestResult(){
		TestResult = "";
		TestResult = (String) tosend.TestSolver(); 
		
		return TestResult;
	}

	//Setters
	public void setSingleThreadClientConnectionManager(SingleThreadClientConnectionManager ccm){CCM = ccm;}
	public void setHost(String host){Host=host;}
	public void setPort(int port){Port=port;}
	public void setProblemModule(ProblemModule p){tosend = p;}

	//Utility
	public void computeTestResult(){
		TestResult = "";
		TestResult = (String)((FileStringSearch)tosend).TestSolver();
	}

	public boolean TEQ(){
		if(recv instanceof ProblemModule){
			if(((ProblemModule) recv).TEQ((Object)TestResult)){
				Success = true;
				System.out.println(TestResult);
				return true;
			}else{
				Success = false;
				return false;
			}
		} else { System.out.println("Woah! Object Recieved is not a ProblemModule!");}
		return false;
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

