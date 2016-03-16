package MVP.Common;

public class IdentificationPacket implements Packets {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long identification;
	
	public IdentificationPacket(long id){
		identification = id;
	}
	
	public long getID(){
		return identification;
	}
}
