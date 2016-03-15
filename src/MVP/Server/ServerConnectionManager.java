package MVP.Server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import problemModule.ProblemModule;

//ServerConnectionManager
public interface ServerConnectionManager {

	void StartServer() throws IOException;

	void shutdown() throws UnknownHostException, IOException;

	void setCPort(int P);

	void setNPort(int P);


	
}
