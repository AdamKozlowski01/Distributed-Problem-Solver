package MVP.Server;

import java.io.IOException;

//ServerConnectionManager
public interface ServerConnectionManager {

	void StartServer() throws IOException;

	void shutdown();

	void setCPort(int P);

	void setNPort(int P);
	

	
}
