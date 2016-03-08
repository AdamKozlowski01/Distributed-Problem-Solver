package server;


public interface NodeFilterStrategy {

	//TODO: I think the node filter strategy should include GPU ready and CPU only options in one implementation
	//some problems may not need or benefit from breakdown AND GPU acceleration and some may almost certainly require it like rendering.
	//GPU solver is a Future/Stretch Goal so it need not be implemented currently.
	
	//public Node[] getNodes();
	
	
}
