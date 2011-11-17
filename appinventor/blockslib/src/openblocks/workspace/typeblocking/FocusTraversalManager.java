package openblocks.workspace.typeblocking;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import openblocks.renderable.BlockUtilities;
import openblocks.renderable.RenderableBlock;

import openblocks.workspace.Page;
import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceListener;
import openblocks.workspace.BlockCanvas.Canvas;
import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;


/**
 * The FocusTraversalManager has two function.  First, it
 * maintains a pointer to the block, if any, that has
 * focus and the corresponding focus point on that block.
 * If the focus is not on the block, then it must be set
 * to some point of the block canvas.
 *
 * The second primary function of the FocusTraversalManager
 * is to redirect the focus to the next appropriate block
 * in a particular stack.  One could "traverse" the stack
 * by moving the focus to one of the following:
 *        1. the block after
 *        2. the block before
 *        3. the next block
 *        4. the previous block
 *
 * The exact definition of what "next", "previous",
 * "after", and "before" is described in details in
 * their corresponding method summary.
 * As a focus manager of the entire system, the class
 * must maintain particular invariants at all time.
 * Clients of this module may obtain the focus through
 * three observer (getter) methods.  Clients may also
 * manualy mutate the focus through three modifier (setter)
 * methods.
 *
 * However, BOTH the value returned in the observer
 * methods and the value passed in the modifier methods
 * MUST maintain particular invariants described below.
 *
 * These invariants must hold at all time and check reps
 * should be imposed to ensure that any changes to the
 * system still holds these crucial invariants.  Clients
 * of this module may assume that the invariants mentioned
 * below will always hold.
 *
 * INVARIANT I.
 *      If the canvas has focus, then the block does not. Thus
 *        1. focusBlock == Block.null
 *        2. canvasFocusPoint != null
 *        3. blockFocusPoint == null
 *
 * INVARIANT II.
 *      If the block has focus, then the canvas does not. Thus
 *        1. focusBlock != Block.null
 *        2. canvasFocusPoint == null
 *        3. blockFocusPoint != null
 *
 * @specfield focusBlock : Long //block with focus
 * @specfield canvasFocusPoint : Point //focus point on canvas relative to canvas
 * @specfield blockFocusPoint : Point //focus point on block relative to block
 *
 */
public class FocusTraversalManager implements MouseListener, KeyListener, WorkspaceListener{
  /** this.focuspoint: the point on the block with focus */
  private Point blockFocusPoint = null;
  /** this.focuspoint: the point on the block canvas's last mosue click */
  private Point canvasFocusPoint = new Point(0,0);
  /** this.focusblock: the Block ID that currently has focus */
  private Long focusBlock = Block.NULL;

  /////////////////
  // Constructor //
  /////////////////

  public FocusTraversalManager(){}

  /**
   * Resets the focus traversal manager's state so that it
   * has the correct values when a new project loads.
   */
  public void reset() {
    blockFocusPoint = null;
    canvasFocusPoint = new Point(0,0);
    focusBlock = Block.NULL;
  }

  ///////////////
  // Observers //
  ///////////////

  /**
   * @return the block that has focus, if any.
   *
   * TODO: finish method documentation
   */
  public Long getFocusBlockID() {
    //DO NOT REMOVE CHECK REP BELOW!!!
    //Many client classes depend on this invariant

    if(invalidBlock(focusBlock)){
      //If block is null then, focus should be on block on canvas.
      //To test this, we must recall that when focus is on block,
      //the canvas focus point is set to null.
      if (canvasFocusPoint == null) throw new RuntimeException(
              "Focus has not yet been set to block");
      if (blockFocusPoint != null) throw new RuntimeException(
              "Focus should be set to block");

    }else{
      //if block is not null, then we need to make sure the
      //focus is on the block.  To test this, we recall
      //that when the focus is on the block, the
      //canvas focus point is set to null
      if (canvasFocusPoint != null) throw new RuntimeException(
              "Focus has not yet been set to canvas");
      if (blockFocusPoint == null) throw new RuntimeException(
              "Focus has not been removed from block");
    }
    return focusBlock;
  }
  /**
   * @return point of focus on canvas
   *
   * TODO: finish method documentation
   */
  public Point getCanvasPoint(){
    //DO NOT REMOVE CHECK REP BELOW!!!
    //Many client classes depend on this invariant

    //If focus block is not null, then the focus is
    //currently set on that instance of the block.
    //As a result, you may not request the canvas's
    //focus point because it DOES NOT have focus
    if(!invalidBlock(focusBlock)) throw new RuntimeException(
           "May not request canvas's focus point if " +
           "canvas does not have focus. Focus at: "+focusBlock);
    if(blockFocusPoint != null ) throw new RuntimeException(
           "May not request canvas's focus point if " +
           "canvas does not have focus. Focus at: "+blockFocusPoint);
    if(canvasFocusPoint == null ) throw new RuntimeException(
           "May not request canvas's focus point if " +
           "canvas does not have focus. Canvas focus is null.");
    //Okay, invariant holds.  So return point of focus on canvas.
    return canvasFocusPoint;
  }
  /**
   * @return point of focus on block
   *
   * TODO: finish method documentation
   */
  public Point getBlockPoint(){
    //DO NOT REMOVE CHECK REP BELOW!!!
    //Many client classes depend on this invariant

    //If focus block is null, then the focus is
    //currently set on canvas.  You may not request
    //the block's focus point because it DOES NOT have focus
    if(invalidBlock(focusBlock)) throw new RuntimeException(
           "May not request block's focus point if " +
           "block does not have focus. Focus at: "+focusBlock);
    if(blockFocusPoint == null ) throw new RuntimeException(
           "May not request block's focus point if " +
           "block does not have focus. Focus at: "+blockFocusPoint);
    if(canvasFocusPoint != null ) throw new RuntimeException(
           "Canvas focus is still valid. May not request" +
           "block's focus point if block does not have focus.");
    //Okay, invariant holds.  So return point of focus on canvas.
    return blockFocusPoint;
  }

  //////////////////
  // Focus Set Up //
  //////////////////

  /**
   * Sets focus to block
   * @param block
   *
   * TODO: finish method documentation
   */
  public void setFocus(Block block){
    if(block == null){
      throw new RuntimeException("Invariant Violated:" +
          "may not set focus to a null Block instance");
      //Please do not remove exception above.  This class
      //and many other classes within the typeblocking
      //package requires that the following invariant(s) must hold:
      //    MAY NOT SET FOCUS TO NULL BLOCK INSTANCES
    }else{
      setFocus(block.getBlockID());
    }
  }
  public void setFocus(Long blockID){
    if(blockID == null || blockID == Block.NULL || blockID == -1 || Block.getBlock(blockID) == null ){
      throw new RuntimeException("Invariant Violated:" +
          "may not set focus to a null Block instance");
      //Please do not remove exception above.  This class
      //and many other classes within the typeblocking
      //package requires that the following invariant(s) must hold:
      //    MAY NOT SET FOCUS TO NULL BLOCK INSTANCES
    }
    //remove focus from old block if one existed
    if(!invalidBlock(this.focusBlock)){
      getBlock(this.focusBlock).setFocus(false);
      RenderableBlock.getRenderableBlock(this.focusBlock).repaintBlock();
    }
    //set focus block to blockID
    getBlock(blockID).setFocus(true);
    RenderableBlock.getRenderableBlock(blockID).requestFocus();
    RenderableBlock.getRenderableBlock(blockID).repaintBlock();
    //set canvas focus point to be null; canvas no longer has focus
    this.canvasFocusPoint = null;
    //set blockfocus point to new value
    this.blockFocusPoint = new Point(0,0);
    //set focusblock
    this.focusBlock = blockID;
  }

  /**
   * Set Focus to canvas at canvasPoint.  THE BLOCKID MUST BE BLOCK.NULL!!!
   * @param canvasPoint
   * @param blockID
   *
   * TODO: finish method documentation
   */
  public void setFocus(Point canvasPoint, Long blockID) {
    if(blockID == null || blockID == Block.NULL || blockID == -1 || Block.getBlock(blockID) == null ){
      //remove focus form old block if one existed
      if(!invalidBlock(this.focusBlock)){
        getBlock(this.focusBlock).setFocus(false);
        RenderableBlock.getRenderableBlock(this.focusBlock).repaintBlock();
      }
      //set block ID to null
      this.focusBlock=Block.NULL;
      //set canvas focus point to canvasPoint
      this.canvasFocusPoint = canvasPoint;
      //set block focus point to null
      this.blockFocusPoint = null;

      //System.out.println("FocusManager: Setting focus to canvas: " + this.focusBlock+", "+this.blockFocusPoint+", "+this.canvasFocusPoint);
    }else{
      throw new RuntimeException("Invariant Violated:" +
          "may not set new focus point if focus is on a block");
      //Please do not remove exception above.  This class
      //and many other classes within the typeblocking
      //package requires that the following invariant(s) must hold:
      //    CANVAS POINT MAY NOT BE SET UNLESS BLOCK IS NULL
      //    CANVAS POINT MAY NOT BE SET IF FOCUS IS ON BLOCK
    }
  }
  void setFocus(Point location){
    //please do not remove this method or try to
    //create a method this takes only a point as an argument.
    //The thing is, it's too tricky to create such a method
    //and I want to let users who use this method know that
    //this is a fundalmentally wrong method to invoke.
    //may not use this method as it does not ensure class invariant will hold
    throw new RuntimeException("The use of this method is FORBIDDEN");
  }


  //////////////////////////////////////
  // Focus Traversal Handling Methods //
  //////////////////////////////////////

  /**
   * Reassigns the focus to the "next block" of the current focusBlock.
   * If the current focusblock is at location n of the flatten linear vector
   * of the block tree structure, then the "next block" is located at n+1.
   * In other words, the previous block is the parent block of the next
   * socket of the parent block of the focusblock.
   *
   * @requires  pointFocusOwner != null &&
   *          focusblock.getSockets() != null &&
   *          focusblock.getSockets() is not empty
   * @modifies this.focusblock
   * @effects this.focusblock now points to the "next block"
   *        as described in method overview;
   * @return true if the new focus is on a block that isn't null
   */
  public boolean focusNextBlock() {
    //return focus to canvas if no focusblock does not exist
    if(invalidBlock(focusBlock) || !RenderableBlock.getRenderableBlock(focusBlock).isVisible()){
      setFocus(canvasFocusPoint, Block.NULL);
      return false;
    }
    //give focus to any preceeding socket of current block
    Block currentBlock = getBlock(focusBlock);
    for(BlockConnector socket : currentBlock.getSockets()){
      if(socket != null && !invalidBlock(socket.getBlockID())){
        //give focus to socket block
        setFocus(socket.getBlockID());
        return true;
      }
    }
    //give focus to after block of current block
    Long afterBlock = currentBlock.getAfterBlockID();
    if(!invalidBlock(afterBlock)){
      setFocus(afterBlock);
      return true;
    }
    //current block != null.....invariant checke in getNextNode()
    Block nextBlock = this.getNextNode(currentBlock);
    //check invariant
    if(nextBlock == null) throw new RuntimeException ("Invariant Violated: return value of getNextNode() may not be null");
    //set focus
    setFocus(nextBlock.getBlockID());
    return true;
  }

  /**
   * Reassigns the focus to the "previous block" of the current focusBlock.
   * If the current focusblock is at location n of the flatten linear vector
   * of the block tree structure, then the "previous block" is located at n-1.
   * In other words, the previous block is the innermost block of the previous
   * socket of the parent block of the focusblock.
   *
   * @requires  pointFocusOwner != null &&
   *          focusblock.getSockets() != null &&
   *          focusblock.getSockets() is not empty
   * @modifies this.focusblock
   * @effects this.focusblock now points to the "previous block"
   *        as described in method overview;
   * @return true if the new focus is on a block that isn't null
   */
  public boolean focusPrevBlock() {
    //return focus to canvas if no focusblock does not exist
    if(invalidBlock(focusBlock) || !RenderableBlock.getRenderableBlock(focusBlock).isVisible()){
      setFocus(canvasFocusPoint, Block.NULL);
      return false;
    }

    Block currentBlock = getBlock(focusBlock);
    //set plug to be previous block
    Block previousBlock = getPlugBlock(currentBlock);
    //if plug is null, set before to be previous block
    if(previousBlock == null ) previousBlock = getBeforeBlock(currentBlock);
    //If before is ALSO null, jump to bottom of the stack;
    if(previousBlock == null){
      previousBlock = getBottomRightBlock(currentBlock);
    }else{
      //If at least a plug block OR (but not both) before block exist,
      //then get innermost block of the previous socket of the previous block
      //assumes previousBlock.getSockets is not empty, not null
      Block beforeBlock = previousBlock;
      //ASSUMPTION BEING MADE: assume that the list below is constructed to
      //have all the sockets FOLLOWED by FOLLOWED by the after connector
      //THE ORDER MUST BE KEPT TO WORK CORRECTLY!  We cannot use
      //BlockLinkChecker.getSocketEquivalents because the specification does
      //not guarantee this precise ordering.  Futhermore, an interable
      //has no defined order.  However, as of this writing, the current implementation
      //of that method does seem to produce this ordering.  But we're still not using it.
      List<BlockConnector> connections = new ArrayList<BlockConnector>();
      for(BlockConnector socket : previousBlock.getSockets()){
        connections.add(socket); //add sockets
      }
      connections.add(previousBlock.getAfterConnector()); //add after connector
      //now traverse the connections
      for(BlockConnector connector : connections){
        if(connector == null || connector.getBlockID() == Block.NULL || getBlock(connector.getBlockID()) == null){
          continue; //if null socket, move on to next socket
        }
        if(connector.getBlockID().equals(currentBlock.getBlockID())){ //reached back to current block
          if(!beforeBlock.getBlockID().equals(previousBlock.getBlockID())){
            //if previous block was never updated, go to bottom of stack
            previousBlock = getBottomRightBlock(previousBlock);
          }
          setFocus(previousBlock.getBlockID());
          return true;
        }
        //update previous block
        previousBlock = getBlock(connector.getBlockID());
      }
      //so it seems liek all sockets are null (or sockets exist),
      //so just get the bottom of the stack
      previousBlock = getBottomRightBlock(previousBlock);
    }
    setFocus(previousBlock.getBlockID());
    return true;
  }

  /**
   * Gives focus to the first after block down the tree,
   * that is, the next control block in the stack.
   * If next control block does not exist, then give
   * focus to current focusblock.  Otherwise, give
   * focus to block canvas.
   * @requires focusblock.isMinimized() == false
   * @modifies this.focusblock
   * @effects sets this.focusblock to be the first
   *        after block if possible.  Otherwise, keep
   *        the focus on the current focusblock.
   *        If focus block is an invalid block,
   *        return focus to the default (block canvas)
   * @return true if and only if focus was set to new after block
   * @expects no wrapping to TopOfStack block, do not use this method for infix blocks
   */
  public boolean focusAfterBlock() {
    if(invalidBlock(focusBlock) || !RenderableBlock.getRenderableBlock(focusBlock).isVisible()){
      //return focus to canvas if no focusblock does not exist
      setFocus(canvasFocusPoint, Block.NULL);
      return false;
    }
    Block currentBlock = getBlock(focusBlock);
    while(currentBlock != null){
      if(getAfterBlock(currentBlock) != null){
        //return focus to before block
        setFocus(getAfterBlock(currentBlock));
        return true;
      }
      currentBlock = getPlugBlock(currentBlock);
      if(currentBlock == null){
        //return focus to old block
        setFocus(focusBlock);
        return true;
      }
    }
    return true;
  }

  /**
   * Gives focus to the first beforeblock up the tree,
   * that is, the previous control block in the stack.
   * If no previous control block exists, then give
   * focus to current focusblock.  Otherwise, give
   * focus to block canvas.
   * @requires focusblock.isMinimized() == false
   * @modifies this.focusblock
   * @effects sets this.focusblock to be the first
   *        before block if possible.  Otherwise, keep
   *        the focus on the current focusblock.
   *        If focus block is an invalid block,
   *        return focus to the default (block canvas)
   * @return true if and only if focus was set to new before block
   * @expects no wrapping to bottom block, do not use this method for infix blocks
   */
  public boolean focusBeforeBlock() {
    if(invalidBlock(focusBlock) || !RenderableBlock.getRenderableBlock(focusBlock).isVisible()){
      //return focus to canvas if no focusblock does not exist
      setFocus(canvasFocusPoint, Block.NULL);
      return false;
    }
    Block currentBlock = getBlock(focusBlock);
    while(currentBlock != null){
      if(getBeforeBlock(currentBlock) != null){
        //return focus to before block
        setFocus(getBeforeBlock(currentBlock));
        return true;
      }
      currentBlock = getPlugBlock(currentBlock);
      if(currentBlock == null){
        //return focus to old block
        setFocus(focusBlock);
        return false;
      }
    }
    return false;
  }

  ///////////////////////
  // TRAVERSING STACKS //
  ///////////////////////

  /**
   * @requires currentBlock != null
   * @param currentBlock
   * @return currentBlock or NON-NULL block that is the next node of currentBlock
   */
  private Block getNextNode(Block currentBlock){
    //check invarient
    if(invalidBlock(currentBlock)) throw new RuntimeException("Invariant Violated: may not resurve over a null instance of currentBlock");
    //if plug not null, then let plug be parent of current block
    Block parentBlock = getBlock(currentBlock.getPlugBlockID());
    //otherwise if after not null, then let after be parent of current block
    if (invalidBlock(parentBlock)) parentBlock = getBlock(currentBlock.getBeforeBlockID());
    //if plug and after are both null, then return currentBlock
    if(invalidBlock(parentBlock)) return currentBlock;
    //socket index of current block with respect to its parent
    int i = parentBlock.getSocketIndex(parentBlock.getConnectorTo(currentBlock.getBlockID()));
    //return socket block of parent if one exist
    //int i == 0 if not current block not a socket of parent
    if(i != -1 && i>=0){
      for(BlockConnector parentSocket : parentBlock.getSockets()){
        if(parentSocket == null || invalidBlock(parentSocket.getBlockID()) || parentBlock.getSocketIndex(parentSocket)<=i){
          continue;
        }else{
          return getBlock(parentSocket.getBlockID());
        }
      }
    }
    //return afterblock of parent
    if(invalidBlock(parentBlock.getAfterBlockID())){
      return getNextNode(parentBlock);
    }
    if(parentBlock.getAfterBlockID().equals(currentBlock.getBlockID())){
      return getNextNode(parentBlock);
    }
    //This is top of the block, so return currentBlock
    return getBlock(parentBlock.getAfterBlockID());
  }

  /**
   * For a given block, returns the outermost (top-leftmost)
   * block in the stack.
   * @requires block represented by blockID != null
   * @param blockID any block in a stack.
   * @return  the outermost block (or Top-of-Stack)
   *        such that the outermost  block != null
   */

  Long getTopOfStack(Long blockID) {
    //check invariant
    if (blockID == null || blockID == Block.NULL || Block.getBlock(blockID) == null)
      throw new RuntimeException("Invariant Violated: may not" +
          "iterate for outermost block over a null instance of Block");
    //parentBlock is the topmost block in stack
    Block parentBlock = null;
    //go the top most block
    parentBlock = getBeforeBlock(blockID);
    if (parentBlock !=null ) return getTopOfStack(parentBlock.getBlockID());
    //go to the left most block
    parentBlock = getPlugBlock(blockID);
    if(parentBlock != null ) return getTopOfStack(parentBlock.getBlockID());
    //check invariant
    if (parentBlock != null)
      throw new RuntimeException("Invariant Violated: may not " +
          "return a null instance of block as the outermost block");
    //If we can't traverse any deeper, then this is innermost Block.
    return blockID;
  }

  /**
   * For a given block, returns the innermost (bottom-rightmost)
   * block in the substack.
   * @requires block !=null block.getBlockID != Block.NULL
   * @param block the top block of the substack.
   * @return the innermost block in the substack.
   *         such that the innermost block != null
   */
  private Block getBottomRightBlock (Block block) {
    //check invariant
    if (block == null || block.getBlockID() == Block.NULL )
      throw new RuntimeException("Invariant Violated: may not" +
          "iterate for innermost block over a null instance of Block");
    //returnblock = next deepest node on far right
    Block returnBlock = null;
    // find deepest node, that is, bottom most block in stack.
    returnBlock = getAfterBlock(block);
    if(returnBlock != null) return getBottomRightBlock(returnBlock);
    // move to the next socket in line:
    for(BlockConnector socket : block.getSockets()){//assumes socket!=null
      Block socketBlock = getBlock(socket.getBlockID());
      if(socketBlock !=null ) returnBlock = socketBlock;
    }
    if(returnBlock !=null ) return getBottomRightBlock(returnBlock);
    //check invariant
    if (returnBlock != null)
      throw new RuntimeException("Invariant Violated: may not " +
          "return a null instance of block as the innermost block");
    //If we can't traverse any deeper, then this is innermost Block.
    return block;
  }

  //////////////////////
  //Convienence Method//
  //////////////////////

  /**
   * @param block
   * @return    true if and only if block ==null ||
   *        block.getBlockID == null &&
   *        block.getBLockID == Block.NULL
   */
  private boolean invalidBlock(Block block){
    return block == null || BlockUtilities.invalidBlockID(block.getBlockID());
  }
  private boolean invalidBlock(Long blockID){
    return BlockUtilities.isNullBlockInstance(blockID);
  }

  /**
   * All the private methods below follow a similar
   * specification. They all require that the block
   * referanced by blockID (or block.getBlockID) is
   * non-null.  If getting a socket block, they
   * additionally require that 0<socket< # of sockets in block.
   * All the methods before return a block located
   * at a block connector corresponding to the name
   * of the obserser method.
   *
   * @requires blockID != Block.Null && block !=null
   * @return Block instance located at corresponding
   *        connection or null if non exists
   */
  private Block getBlock(Long blockID) {
    return Block.getBlock(blockID);
  }
  private Block getBeforeBlock(Long blockID) {
    return getBeforeBlock(getBlock(blockID));
  }
  private Block getBeforeBlock(Block block) {
    return getBlock(block.getBeforeBlockID());
  }
  private Block getAfterBlock(Block block) {
    return getBlock(block.getAfterBlockID());
  }
  private Block getPlugBlock(Long blockID) {
    return getPlugBlock(getBlock(blockID));
  }
  private Block getPlugBlock(Block block) {
    return getBlock(block.getPlugBlockID());
  }

  ///////////////////
  // MOUSE METHODS //
  ///////////////////
  /**
   * Action: removes the focus current focused block
   *         and places new focus on e.getSource
   * @requires e != null
   * @modifies this.blockFocusOwner && e.getSource
   * @effects removes focus from this.blockFocusOwner
   *        adds focus to e.getSource iff e.getSource
   *        is instance of BlockCanvas and RenderableBlock
   */
  private void grabFocus(MouseEvent e){
    //System.out.println("FocusManager: Mouse Event at ("+ e.getX()+", "+e.getY()+") on "+e.getSource());
    if(e.getSource() instanceof Canvas){
      //get canvas point
      Point canvasPoint = e.getPoint();
      /*    SwingUtilities.convertPoint(
            (BlockCanvas)e.getSource(),
            e.getPoint(),
            ((BlockCanvas)e.getSource()).getCanvas());*/
      setFocus(canvasPoint, Block.NULL);
      ((Canvas)e.getSource()).grabFocus();
    }else if(e.getSource() instanceof RenderableBlock){
      setFocus(((RenderableBlock)e.getSource()).getBlockID());
      ((RenderableBlock)e.getSource()).grabFocus();
    }
  }
  public void mousePressed(MouseEvent e) {grabFocus(e);}
  public void mouseReleased(MouseEvent e) {grabFocus(e);}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {}


  ///////////////////////////////
  // Key Listeners Method      //
  ///////////////////////////////
  public void keyPressed(KeyEvent e){
    KeyInputMap.processKeyChar(e);}
  public void keyReleased(KeyEvent e){}
  public void keyTyped(KeyEvent e){}

  ///////////////////////////////
  // WORKSPACE LISTENER METHOD //
  ///////////////////////////////

  /**
   * Subscription: BLOCK_ADDED events.
   * Action: add this.mouselistener to the block referanced by event
   * @requires block reference in event is not null
   * @modifies this.blockFocusOwner && event.block
   * @effects Add this.mouselistener to this.blockFocusOwner
   *        removes focus from this.blockFocusOwner
   *        adds focus to e.getSource iff e.getSource
   *        is instance of BlockCanvas and RenderableBlock
   */
  public void workspaceEventOccurred(WorkspaceEvent event) {
    switch(event.getEventType()){
      case WorkspaceEvent.BLOCK_ADDED:
        //System.out.println("FocusManager: Block_Added Event at of "+event.getSourceBlockID()+" on "+event.getSourceWidget());
        //only add focus manager as listener to blocks added to pages
        if(!(event.getSourceWidget() instanceof Page)) break;
        RenderableBlock rb = RenderableBlock.getRenderableBlock(event.getSourceBlockID());
        if(rb == null) break;

        //only add once
        for(MouseListener l : rb.getMouseListeners()){
          if(l.equals(this)){
            return;
            //TODO: this shouldn't return, it should break
            //but you can't double break in java
          }
        }
        rb.addMouseListener(this);
        rb.addKeyListener(this);
        setFocus(event.getSourceBlockID());
        rb.grabFocus();
        break;
    }
  }

  public String toString() {
    return "FocusManager: "+blockFocusPoint+" of "+Block.getBlock(focusBlock);
  }
}
