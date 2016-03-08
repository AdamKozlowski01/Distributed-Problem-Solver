package MVP.Common;

public class KeepAlive implements Packets{

	private static final long serialVersionUID = 1L;
	long QueuePos;
	
	public KeepAlive(){
		QueuePos = 0;
	}
	
	public KeepAlive(long Q){
		QueuePos = Q;
	}
	
	public long getPos(){
	return QueuePos;
	}
	
	public void setPos(long L){
		QueuePos=L;
	}
	
	
	
}
