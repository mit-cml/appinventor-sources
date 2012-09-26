// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblocks;

import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceListener;


/**
 * <code>InfixRule</code> specifies a rule for <code>Block</code> linking.
 * Allows users to insert infix blocks in between two blocks.
 */
public class InfixRule implements LinkRule, WorkspaceListener{
    /**
     * Returns true if the two sockets of the two blocks can link; false if not
     * @param block1 the associated <code>Block</code> of socket1
     * @param block2 the associated <code>Block</code> of socket2
     * @param socket1 a <code>Socket</code> of block1
     * @param socket2 a <code>Socket</code> of block2
     * @return true if the two sockets of the two blocks can link; false if not
     */
    public boolean canLink(Block block1, Block block2, BlockConnector socket1, BlockConnector socket2){
    	//don't even deal with null instances of blocks and sockets
    	if(block1 == null || block2 == null || socket1 == null || socket2 == null){
    		return false;
    	}
    	
    	//first assume that block1 is a parent with children
		Block parent = block1; //block1 is parent
		BlockConnector psocket = socket1;
		Block child = Block.getBlock(socket1.getBlockID()); //child is the block1's socket block
		Block newblock = block2; //the new block that is being dragged to added on top is block2
		BlockConnector nplug = socket2;
    	if( child != null && // make sure child' isn't null
    		!psocket.equals(parent.getAfterConnector()) && //make sure we're dealing with socket connectors no after conectors
    		parent.getNumSockets()>0 && //make sure parent even has sockets
    		psocket != null && //make sure that socket is valid (non-null)
    		nplug.equals(newblock.getPlug()) && //make sure newblock has a plug
    		newblock.hasPlug() && //make sure newblock even has a valid plug (non-null)
    		
    		newblock.getNumSockets()>0 && //make sure newblock has sockets
    		!newblock.getSocketAt(0).hasBlock() && //make sure the socket isn't filled
    		newblock.getSocketAt(0).getKind().equals(psocket.getKind()) && //sockets match kind
			child.hasPlug() && //has has plug
			newblock.getPlug().getKind().equals(child.getPlug().getKind())//child's plug match newblock's plug
    	){
    		return true;
    	}
    	
    	
    	//now assume that block2 is a parent with children
		parent = block2; //block1 is parent
		psocket = socket2;
		child = Block.getBlock(socket2.getBlockID()); //child is the block1's socket block
		newblock = block1; //the new block that is being dragged to added on top is block2
		nplug = socket1;
    	if( child != null && // make sure child' isn't null
    		!psocket.equals(parent.getAfterConnector()) && //make sure we're dealing with socket connectors no after conectors
    		parent.getNumSockets()>0 && //make sure parent even has sockets
    		psocket != null && //make sure that socket is valid (non-null)
    		nplug.equals(newblock.getPlug()) && //make sure newblock has a plug
    		newblock.hasPlug() && //make sure newblock even has a valid plug (non-null)
    		
    		newblock.getNumSockets()>0 && //make sure newblock has sockets
    		!newblock.getSocketAt(0).hasBlock() && //make sure the socket isn't filled
    		newblock.getSocketAt(0).getKind().equals(psocket.getKind()) && //sockets match kind
			child.hasPlug() && //has has plug
			newblock.getPlug().getKind().equals(child.getPlug().getKind())//child's plug match newblock's plug
    	){
    		return true;
    	}
    	
    	
    	//no assumptions work, return false
    	return false;
    }
    
    public boolean isMandatory(){
    	return false;
    }
    
	public void workspaceEventOccurred(WorkspaceEvent e) {
		if (e.getEventType() == WorkspaceEvent.BLOCKS_CONNECTED) {
			BlockLink link = e.getSourceLink();
			if(link == null) return;
			Block oldchild = Block.getBlock(link.getLastBlockID());
			if(invalidBlock(oldchild)) return;
			Block newchild = Block.getBlock(link.getPlugBlockID());
			if(invalidBlock(newchild)) return;
			Block parent = Block.getBlock(link.getSocketBlockID());
			if(invalidBlock(parent)) return;
			if(!oldchild.hasPlug()) return;
			if(!newchild.hasPlug()) return;
			Block newChildPlug = Block.getBlock(newchild.getPlug().getBlockID());
			if(invalidBlock(newChildPlug)) return;
			
			if(newchild.getNumSockets() <= 0) return;
			BlockConnector newChildSocket = newchild.getSocketAt(0);
			if(newChildSocket==null) return;
			if(newChildSocket.hasBlock()) return;
			BlockLink link2 = BlockLink.getBlockLink(
					newchild,
					oldchild,
					newchild.getSocketAt(0),
					oldchild.getPlug());
			
			//finally connect
			if(link2 == null) throw new RuntimeException("Why is this null?");
			if (link2 != null) link2.connect();
		}
	}
	
	private boolean invalidBlock(Block block){
		if(block == null) return true;
		if(block.getBlockID() == null) return true;
		if(block.getBlockID().equals(Block.NULL)) return true;
		return false;
	}
    
}