package MVP.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import MVP.Common.*;
import MVP.problemModule.*;

public class TestClientConnectionManager implements MVP.Client.SingleThreadClientConnectionManager{

	private Socket Client;
	private DataOutputStream DataOut;
	private ObjectOutputStream obOut;
	private DataInputStream DataIn;
	private ObjectInputStream obIn;

	public TestClientConnectionManager(String host,int port) throws UnknownHostException, IOException{
		Client = new Socket(InetAddress.getByName(null),9090);
		System.out.println("ClientSays: Connected");
		DataOut = new DataOutputStream(Client.getOutputStream());
		obOut = new ObjectOutputStream(DataOut);
		DataIn = new DataInputStream(Client.getInputStream());
		obIn = new ObjectInputStream(DataIn);
		System.out.println("ClientSays: Streams setup");
	}

	public Object readObject() throws ClassNotFoundException, IOException {return obIn.readObject();}
	//public void shutdownOutput() throws IOException {Client.shutdownOutput();}
	public void writeObject(TestProblemModule task) throws IOException {obOut.writeObject(task);Client.shutdownOutput();}
	public void close() throws IOException{Client.close();}

	@Override
	public void writeObject(ProblemModule task) throws IOException {
		obOut.writeObject(task);Client.shutdownOutput();
	}

	@Override
	public ProblemModule waitForResult() throws ClassNotFoundException, IOException {
		boolean waiting = true;
		Object recv = null;
		while(waiting){
			if((recv = obIn.readObject()) instanceof ProblemModule){
				waiting = false;
			}else if(recv instanceof Packets){
				if((recv instanceof KeepAlive)){
					//System.out.println("Currently in Posistion "+ ((KeepAlive) recv).getPos());
				}
			}
		}
		return (ProblemModule) recv;
	}
}
