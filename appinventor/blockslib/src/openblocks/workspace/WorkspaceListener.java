package openblocks.workspace;


/**
 * This listener interface is for receiving "interesting" events on the workspace.  
 */
public interface WorkspaceListener {

    /**
     * Invoked whenever any of the enumerated events specified within 
     * WorkspaceEvent occur.  We leave it to the implementor to look 
     * at the event type and determine what to do from there.  We chose this 
     * design so that listeners need not implement all X methods, where X is the 
     * number of workspace events.  
     * @param event WorkspaceEvent object holding information regarding the triggered
     * event.
     */
    public void workspaceEventOccurred(WorkspaceEvent event);
    
}
