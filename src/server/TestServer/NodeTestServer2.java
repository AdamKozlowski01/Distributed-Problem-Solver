package server.TestServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import common.KeepAlive;
import common.Packets;
import common.Status;
import problemModule.HardcodedTestProblem;
import problemModule.ProblemModule;

public class NodeTestServer2 {

	static String Host;
	static int Port;
	//Known to function, does not implement the Node functionality.
	//Use to debug A new ProblemModule
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		//TestProblemModule Task = new TestProblemModule(TestA, TestA, m, m);
		ServerSocket Server = new ServerSocket(9090);
		Socket Node = Server.accept();
		System.out.println("Node Connected");
		DataOutputStream DataOut = new DataOutputStream(Node.getOutputStream());
		ObjectOutputStream obOut = new ObjectOutputStream(DataOut);
		DataInputStream DataIn = new DataInputStream(Node.getInputStream());
		ObjectInputStream obIn = new ObjectInputStream(DataIn);

		Object Precv = null;
		for(int i = 0; i < 5; i++){
			if((Precv = obIn.readObject()) instanceof ProblemModule){
				System.out.println("WTF Problem Module originated from node");
			}else if(Precv instanceof Packets){
				if((Precv instanceof Status)){
					System.out.println("PreStatus = "+ ((Status) Precv).getStatus());
					System.out.println("presend loop " + i);
				}
			}
		}

		obOut.writeObject(new HardcodedTestProblem());	

		boolean waiting = true;
		Object recv = null;
		while(waiting){
			if((recv = obIn.readObject()) instanceof ProblemModule){
				waiting = false;
				ProblemModule PM = (ProblemModule) recv;
				System.out.println("PM recieved");
			}else if(recv instanceof Packets){
				if((recv instanceof Status)){
					System.out.println("PostStatus = "+ ((Status) recv).getStatus());
				}
			}
		}
		recv = obIn.readObject();
		if(recv instanceof Packets){
			if((recv instanceof Status)){
				System.out.println("ResetStatus = "+ ((Status) recv).getStatus());
			}

		}
	}
}

