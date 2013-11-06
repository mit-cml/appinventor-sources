// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.workspace.typeblocking;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import openblocks.renderable.RenderableBlock;
import openblocks.workspace.Page;
import openblocks.workspace.PageChangeEventManager;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;
import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockLink;

/**
 * The BlockDropAnimator has two function, to fly a block to
 * it's location and then attempt to link the child if possible.
 * It takes in a RenderableBlock, drags the block, and drops it
 * if possible.  This class uses a timer to assist in the
 * smooth animation of flight.
 */
public class BlockDropAnimator implements ActionListener{
	private final RenderableBlock childBlock;
	private final RenderableBlock parentBlock;
	private final Point focusPoint;
	/**timer to assist in animation**/
	private final javax.swing.Timer timer;
	/**
	 * Constructs a TypeBlockTimer.
	 * @requires focusPoint != null && childBlock != null && parentBlock != null 
	 */
	public BlockDropAnimator(Point focusPoint, RenderableBlock childBlock, RenderableBlock parentBlock){
		this.childBlock=childBlock;
		this.parentBlock=parentBlock;
		this.focusPoint=focusPoint;
		this.timer = new javax.swing.Timer(25, this);
		if(childBlock == null || childBlock.getBlockID()== Block.NULL) {
			throw new RuntimeException("may not drop a null block");
		}
		timer.start();
	}
	//Keep moving the block until it reaches it's destination
	public void actionPerformed(ActionEvent e){
		if(childBlock.getLocation().distance(focusPoint) < 75){
	        //if parent block exist, then preform automatic linking
	        childBlock.setLocation(focusPoint);
			if(parentBlock != null  && parentBlock.getBlockID() != null && !parentBlock.getBlockID().equals(Block.NULL)){
		        BlockLink link = LinkFinderUtil.connectBlocks(Block.getBlock(childBlock.getBlockID()), Block.getBlock(parentBlock.getBlockID()));
		        if (link == null){
		        	dropBlock(childBlock);
		        	childBlock.repaintBlock();
			        childBlock.repaint();
		        }else{		        	
		        	// drop and link the new block
		        	link.connect();
		        	dropBlock(childBlock);
		        	
		        	Workspace.getInstance().notifyListeners(new WorkspaceEvent(
		        			RenderableBlock.getRenderableBlock(link.getPlugBlockID()).getParentWidget(),
		        			link, WorkspaceEvent.BLOCKS_CONNECTED));
		        	RenderableBlock.getRenderableBlock(link.getSocketBlockID()).moveConnectedBlocks();
		        	RenderableBlock.getRenderableBlock(link.getSocketBlockID()).repaintBlock();
		        	RenderableBlock.getRenderableBlock(link.getSocketBlockID()).repaint();
		        }
	        }else{
	        	dropBlock(childBlock);
	        	childBlock.repaintBlock();
		        childBlock.repaint();
	        }

			//stop the timer
			timer.stop();
	        if(Block.getBlock(childBlock.getBlockID()).getGenusName().equals("number")){
	        	childBlock.switchToLabelEditingMode(false);			
	    	}else{													
	    		childBlock.switchToLabelEditingMode(true);		
	    	}
	    	
	        //TODO: check if focumanager's before parent is same as
	        //the parent we have here and check if new focusblock is child block
	        PageChangeEventManager.notifyListeners();
		}else{
			//childBlock.setLocation(focusPoint);
			// TODO: This needs to change if the parent block doesn't have any more sockets for children
			// Need to adjust focusPoint somehow.
			childBlock.setLocation(
					(int)(focusPoint.getX()*0.67)+(int)(childBlock.getX()*0.34),
					(int)(focusPoint.getY()*0.67)+(int)(childBlock.getY()*0.34));
			
		}
	}
	/**
	 * drops block by adding to widget and throwing workspace event
	 * @param block
	 * 
	 * @requires block != null; block.blockID != null; block.blockID != Block.NULL
	 * 			 workspace widget != null
	 */
	private static void dropBlock(RenderableBlock block){
		if(block == null){
    		throw new RuntimeException("Invariant Violated: child block was null");
		}
		Page p = Workspace.getInstance().getCurrentPage(block);
    	if(p==null){
    		throw new RuntimeException("Invariant Violated: child block was located on a null widget");
    	}
    	//add this block to that page.
        p.blockDropped(block);
	}
}
