package openblocks.codeblockutil;

public class GlassExplorerEvent implements ExplorerEvent{
	public final static int SLIDING_CONTAINER_FINISHED_OPEN = 1;
	public final static int SLIDING_CONTAINER_FINISHED_CLOSED = 2;
	
	private int eventType;
	private GlassExplorer ge=null;
	
	public GlassExplorerEvent(GlassExplorer ge, int event){
		this.ge =ge;
		this.eventType = event;
	}
		
	public int getEventType(){
		return this.eventType;
	}
	
	public Explorer getSource(){
		return this.ge;
	}
	
}
