// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.renderable;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.JComponentDragHandler;

import openblocks.workspace.Page;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceWidget;

import openblocks.yacodeblocks.FeedbackReporter;
import openblocks.yacodeblocks.WorkspaceControllerHolder;


/**
 * FactoryRenderableBlock extends RenderableBlock and is used within FactoryBlockDrawers.
 * Unlike its superclass RenderableBlock, FactoryRenderableBlock does not move or
 * connect to any blocks.  Instead it has one function only, to produce new RenderableBlocks
 * and their associated Block instances.  It's block labels are also uneditable.
 *
 * When a mouse is pressed over a FactoryRenderableBlock, a new RenderableBlock instance is
 * created on top of it to receive further mouse events and a new Block instance is created
 * in the background.
 */
public class FactoryRenderableBlock extends RenderableBlock {

        private static final long serialVersionUID = 1L;

    //the RenderableBlock to produce
    private RenderableBlock createdRB = null;
    private boolean createdRB_dragged = false;

    //we have this instance of the dragHandler so that we can use it mouse entered
    //mouseexited methods to change the cursor appropriately, so that we can make it
    //"seem" that this block is draggable
    private JComponentDragHandler dragHandler;

    /**
     * Constructs a new FactoryRenderableBlock instance.
     * @param widget the parent widget of this
     * @param blockID the Long ID of its associated Block instance
     */
    public FactoryRenderableBlock(WorkspaceWidget widget, Long blockID){
        super(widget, blockID);
        this.setBlockLabelUneditable();
        if (!WorkspaceControllerHolder.isHeadless()) {
          dragHandler = new JComponentDragHandler(this);
        }
    }

    /**
     * Returns a new RenderableBlock instance (and creates its associated Block) instance of the same genus as this.
     * @return a new RenderableBlock instance with a new associated Block instance of the same genus as this.
     */
    public RenderableBlock createNewInstance(){
        return BlockUtilities.cloneBlock(Block.getBlock(super.getBlockID()));
    }


    /**
     *
     * @return false because FactoryRenderableBlocks can't have complaints
     */
    @Override
    public boolean hasComplaint() {
      return false;
    }

    ///////////////////
    //MOUSE EVENTS (Overriding mouse events in super)
    ///////////////////

    @Override
    public void mousePressed(MouseEvent e) {
        if (!WorkspaceControllerHolder.get().haveProject()) {
            return;
        }
        requestFocus();
        //create new renderable block and associated block
        createdRB = createNewInstance();
        Workspace workspace = Workspace.getInstance();
        // createdRB has no location yet, move it to the workspace.
        workspace.addToBlockLayer(createdRB);
        createdRB.setLocation(SwingUtilities.convertPoint(getParent(), getX(),
            getY(), workspace));
        //send the event to the mouseDragged() of new block
        MouseEvent newE = SwingUtilities.convertMouseEvent(this, e, createdRB);
        createdRB.mousePressed(newE);
        mouseDragged(e); // immediately make the RB appear under the mouse cursor
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(createdRB != null){
            //translate this e to a MouseEvent for createdRB
            MouseEvent newE = SwingUtilities.convertMouseEvent(this, e, createdRB);
            createdRB.mouseDragged(newE);
            createdRB_dragged = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!WorkspaceControllerHolder.get().haveProject()) {
            FeedbackReporter.showInfoMessage(FeedbackReporter.NO_PROJECT_MESSAGE);
            return;
        }
        if(createdRB != null){
            if(!createdRB_dragged){
                Container parent = createdRB.getParent();
                parent.remove(createdRB);
                parent.validate();
                parent.repaint();
            }else{
                //translate this e to a MouseEvent for createdRB
                MouseEvent newE = SwingUtilities.convertMouseEvent(this, e, createdRB);
                createdRB.mouseReleased(newE);
            }
            createdRB_dragged = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        dragHandler.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        dragHandler.mouseExited(e);
    }
}
