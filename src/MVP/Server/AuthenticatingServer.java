package MVP.Server;

import java.io.IOException;
import java.net.UnknownHostException;

public class AuthenticatingServer {

	static int CPort,NPort;
	static int PThreads;
	static ServerConnectionManager SCM;
	//static boolean Running = false;

	public AuthenticatingServer(){}

	public void Start() throws IOException{
		SCM = new AuthenticatingConnectionManager(CPort,NPort,PThreads);
		SCM.StartServer();
	}
	
	public AuthenticatingServer(int cPort,int nPort){
		CPort = cPort;
		NPort = nPort;
	}

	public void setClientPort(int port){
		CPort = port;
	}

	public void setNodePort(int port){
		NPort = port;
	}
	
	public void setStartingProblemThreads(int i){
		PThreads = i;
	}

	public void Shutdown() throws UnknownHostException, IOException{
		SCM.shutdown();
	}
}
