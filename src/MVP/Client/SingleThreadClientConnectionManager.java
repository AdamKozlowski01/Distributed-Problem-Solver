package MVP.Client;

import java.io.IOException;

import MVP.problemModule.ProblemModule;

public interface SingleThreadClientConnectionManager {

	public void close()throws IOException;
	public Object readObject() throws ClassNotFoundException, IOException ;
	public void writeObject(ProblemModule tosend)throws IOException;
	public ProblemModule waitForResult() throws ClassNotFoundException, IOException;
}
