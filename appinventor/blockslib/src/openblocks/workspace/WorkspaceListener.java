// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
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
