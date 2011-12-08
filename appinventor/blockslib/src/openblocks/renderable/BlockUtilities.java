package openblocks.renderable;

import java.awt.Container;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import openblocks.workspace.Page;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceWidget;
import openblocks.workspace.typeblocking.FocusTraversalManager;
import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;
import openblocks.codeblocks.BlockGenus;
import openblocks.codeblocks.BlockLink;
import openblocks.codeblocks.BlockLinkChecker;
import openblocks.codeblocks.BlockStub;
import openblocks.yacodeblocks.FeedbackReporter;
import openblocks.yacodeblocks.ProcedureBlockManager;

public class BlockUtilities {
  private static boolean DEBUG = false;

  public static final String CONFIRM_DELETE_MESSAGE = "Really remove selected blocks?";

  // Map from genusName+label -> counter. Keeps an instance counter for each <genus, label>
  // pair. We add the label into the key so that we don't end up with, e.g., x1, y2, z3 instead of
  // x1, y1, z1 for x, y, z arguments.
  private static final Map<String, Integer> instanceCounter = new HashMap<String, Integer>();
  private static double zoom = 1.0;

  public static void reset(){
    zoom = 1.0;
    instanceCounter.clear();
  }
  public static void setZoomLevel(double newZoom) {
    zoom = newZoom;
  }

  /**
   * Returns true if the specified label is valid according to the specifications
   * of this block's genus. For example, if this block's label must be unique
   * (as specified from its genus), this method verifies that its label is
   * unique relative to the other instances present.
   * @param label the String block label to test
   * @return true if the specified label is valid according to the specifications
   * of this block's genus.
   */
  public static boolean isLabelValid(Long blockID, String label){
    Block block = Block.getBlock(blockID);
    if (block == null) {
      return false;
    }
    BlockGenus genus = BlockGenus.getGenusWithName(block.getGenusName());
//    if (block.getGenusName().equals("argument")) {
//      if (!isArgumentLabelValid(block, label)) return false;
//    }
    return isLabelValid(genus, label);
  }

  public static boolean isLabelValid(BlockGenus genus, String label){
    //check to see if the label contains only the characters allowed for its type
    if (genus == null || label == null || (!genus.isEmptyLabelAllowed() && label.equals(""))) {
      return false;
    } else if (!genus.isLabelLegal(label)){
      FeedbackReporter.showErrorMessage("\"" + label + "\" is not a good label for "
          + genus.getGenusName());
      return false;
    }

    if (genus.labelMustBeUnique()) {
      return isLabelUnique(label, genus.getGenusName());
    }
    // label doesn't have to be unique
    return true;
  }

  /*
   * Checks that the label won't be a duplicate with a procedure's arguments
   */
  public static boolean isArgumentLabelValid(Block block, String label){
    Block procedure = ProcedureBlockManager.getProcFromBlock(block);
    if (procedure == null) return true;
    return (!ProcedureBlockManager.hasArgWithName(procedure, label));
  }


  /*
   * Returns true iff label is unique over all blocks of genus genusName
   */
  public static boolean isLabelUnique(String label, String genusName) {
    //search through the current block instances active in the workspace
    for(RenderableBlock rb : Workspace.getInstance().getRenderableBlocksFromGenus(genusName)){
      if(label.equals(rb.getBlock().getBlockLabel()))
        return false;
    }
    return true;
  }

  /**
   * A pre-check to see if a deletion will be allowed on this block.  The
   * user will be prompted with a dialog box either if confirm is true or
   * if the block to be deleted is a procedure declaration and the program has
   * callers, or a variable declaration and the program has getters or setters,
   * or an argument declaration and the program has getters.
   *
   * @param renderable the block to be removed.  The block instance must not be null.
   * @param confirm    force confirm
   * @return true if the user acquiesces (or it is a case where the user
   *     does not need to do so).
   */
  public static boolean deleteBlockUserConfirm(RenderableBlock renderable, boolean confirm) {
    Long blockId = renderable.getBlockID();
    //  ====================>>>>>>>>>>>>>>>>>>>>>>>>>
    //  ====================focus coming in>>>>>>>>>>
    //  ====================>>>>>>>>>>>>>>>>>>>>>>>>>

    //Do not delete null block references.  Otherwise, get Block and RenderableBlock instances.
    if (isNullBlockInstance(blockId)) {
      throw new RuntimeException("BlockUtilities: deleting a null block reference.");
    }
    Block block = Block.getBlock(blockId);

    // Require a special confirmation for deleting a variable declaration if there are
    // program blocks that get or set the variable
    // Two stubs will always exist: the getter and the setter in the My Definitions drawer
    if (block.isVariableDeclBlock() && (BlockStub.countStubsOfParent(blockId) > 2)) {
      String lab = block.getBlockLabel();
      String message = "Really delete the [def " + lab + "] block?\n" +
      "This will also delete all [global " + lab + "] \n" +
      "and [set global " + lab + "] blocks.";
      return FeedbackReporter.getConfirmation(message);
    }

    // Require a special confirmation for deleting a procedure definition if there are
    // program blocks that call the procedure.
    // One stub will always exist: the caller in the My Definitions drawer
    if (ProcedureBlockManager.isProcDeclBlock(block)
        && (BlockStub.countStubsOfParent(blockId) > 1)) {
      String lab = block.getBlockLabel();
      String message = "Really delete the [to " + lab + "] block?\n" +
      "This will also delete all [call " + lab + "] blocks.";
      return FeedbackReporter.getConfirmation(message);
    }

    // Require a special confirmation for deleting an argument definition if there are
    // program blocks that get the argument.
    // One stub will always exist: the getter in the My Definitions drawer
    if (block.isArgumentDeclBlock()
        && (BlockStub.countStubsOfParent(blockId) > 1)) {
      String lab = block.getBlockLabel();
      String message = "Really delete the [name " + lab + "] block?\n" +
      "This will also delete all [value " + lab + "] blocks.";
      return FeedbackReporter.getConfirmation(message);
    }

    if (confirm &&
        ! FeedbackReporter.getConfirmation(CONFIRM_DELETE_MESSAGE)) {
      return false;
    }

    return true;
  }

  /**
   * Removes the specified block and all blocks attached to its sockets.
   * Assumes deleteBlockUserConfirm has been called first
   *
   * Blocks attached vertically above or below are unaffected.
   *
   * @param renderable the block to be removed.  The block instance as
   * well as its parent widget and parent container must not be null.
   * @param deleteAfter if true, delete the blocks "after" this block as well as
   *   the blocks attached to its sockets.
   * @return true if delete happened, false otherwise
   */
   public static boolean deleteBlock(RenderableBlock renderable, boolean deleteAfter) {
     Long blockId = renderable.getBlockID();

     //  ====================>>>>>>>>>>>>>>>>>>>>>>>>>
     //  ====================focus coming in>>>>>>>>>>
     //  ====================>>>>>>>>>>>>>>>>>>>>>>>>>

     // Do not delete null block references.  Otherwise, get Block and RenderableBlock instances.
     if (isNullBlockInstance(blockId)) {
       if (DEBUG) {
         System.out.println("BlockUtilities: Request to delete a null block reference ignored.");
       }
       throw new RuntimeException("BlockUtilities: deleting a null block reference.");
     }

     Block block = Block.getBlock(blockId);
     if (DEBUG) {
       System.out.println("BlockUtilities.deleteBlock(" + blockId + "), blockLabel = " + block.getBlockLabel()
           + ", block genus " + block.getGenusName());
     }
     //get workspace widget associated with current focus
     WorkspaceWidget widget = renderable.getParentWidget();
     //get parent container of this graphical representation
     Container container = renderable.getParent();
     if (widget == null || container == null) {
       // This can happen if the block is being dragged straight from a drawer
       // to the trash. Can it happen any other time?
       // Note: we are assuming here that the block being deleted has no attached
       // blocks (e.g., in its sockets or after). We could check this if it
       // seems important.
       if (DEBUG) {
         System.out.println("Removing block with no parent widget or no container: " + block.getBlockLabel());
       }
       RenderableBlock.removeBlock(renderable, widget, container);
       return true;
     }

     ArrayList<Long> blockIdsToForget = new ArrayList<Long>();

     if ((ProcedureBlockManager.isProcDeclBlock(block)
         && (BlockStub.countStubsOfParent(blockId) > 1)
         || (block.isVariableDeclBlock() && (BlockStub.countStubsOfParent(blockId) > 2))
         || (block.isArgumentDeclBlock() && (BlockStub.countStubsOfParent(blockId) > 1)))) {
       // delete callers of procedures, getters and setters of variable declarations,
       // or getters of arguments
       for(Long stub : BlockStub.getStubsOfParent(blockId)) {
         if (!BlockUtilities.isNullBlockInstance(stub)) {
           RenderableBlock rb = RenderableBlock.getRenderableBlock(stub);
           WorkspaceWidget stubWidget = rb.getParentWidget();
           //do not delete block instances in null widgets
           if (stubWidget == null) {
             FeedbackReporter.showSystemErrorMessage(
                 "BlockUtilities: trying to delete a block with no parent widget [3]. Label is "
                   + block.getBlockLabel());
             return false;
           }
           if (stubWidget instanceof Page) {
             // i.e., not the drawer, which is handled by FactoryManager.workspaceEventOccurred
             Container stubContainer = rb.getParent();
             //do not delete block instances in null parents
             if (stubContainer == null) {
               FeedbackReporter.showSystemErrorMessage(
                   "BlockUtilities: trying to delete a block with no parent widget [4].Label is "
                     + block.getBlockLabel());
               return false;
             }
             // remember ids of blocks to be deleted/removed from stubs map
             // don't do the recursive delete here to avoid ConcurrentModificationExceptions
             // from BlockStub.getStubsOfParent.
             blockIdsToForget.add(rb.getBlockID());
           }
         }
       }
     }

     // Remove blockId from all the lists of stubs associated with blocks.
     // In principle, we can eliminate this Id from all Maps; but the only
     // important one is the one that maps procedures into the ids of their
     // calling blocks, variable defs into their getters and setters, and
     // arguments into their getters.
     // This will be a no-op if blockId is not the id of a stub.
     BlockStub.forgetStub(blockId);
     for(Long otherBlockId: blockIdsToForget) {
       if (BlockStub.hasParent(otherBlockId)) {  // block stub has not already been deleted
         BlockStub.forgetStub(otherBlockId);
         deleteBlock(RenderableBlock.getRenderableBlock(otherBlockId), false);
       }
     }

     // Delete children after stubs. This is important for procedures (and other things?)
     // where the stubs' sockets depend on blocks plugged into the parent's sockets.
     // If we have a procedure with arguments and a stub of that procedure with blocks
     // connected in the argument slots, and we delete the procedure declaration's
     // arguments before handling the stubs, the stubs will lose track of the
     // blocks that were connected in their argument slots when the arguments
     // are deleted from the declaration.
     Iterable<BlockConnector> connectors;
     if (deleteAfter) {
       connectors = BlockLinkChecker.getSocketEquivalents(block);
     } else {
       connectors = block.getSockets();
     }
     for (BlockConnector socketConnector : connectors) {
       if (socketConnector.hasBlock()) {
         deleteBlock(RenderableBlock.getRenderableBlock(socketConnector.getBlockID()), true);
       }
     }

     JComponent blockCanvas = Workspace.getInstance().getBlockCanvas().getCanvas();

     // get the Block's location on the canvas for setting the focus at the end
     Point location = SwingUtilities.convertPoint(renderable, new Point(0,0), blockCanvas);

     //for every valid and active before/after/plug connection, disconnect it.
     Long parentID = null;
     if (validConnection(block.getPlug())) {
       parentID = block.getPlugBlockID();
       disconnectBlock(block, widget);
       if (validConnection(block.getAfterConnector())) {
         disconnectBlock(Block.getBlock(block.getAfterBlockID()), widget);
       }
     } else if (validConnection(block.getBeforeConnector())) {
       parentID = block.getBeforeBlockID();
       BlockConnector parentConnectorToBlock =
         Block.getBlock(parentID).getConnectorTo(block.getBlockID());
       disconnectBlock(block, widget);
       if (validConnection(block.getAfterConnector())) {
         Long afterBlockID = block.getAfterBlockID();
         disconnectBlock(Block.getBlock(afterBlockID), widget);
         if (parentID != null) {
           BlockLink link = BlockLinkChecker.canLink(
               Block.getBlock(parentID),
               Block.getBlock(afterBlockID),
               parentConnectorToBlock,
               Block.getBlock(afterBlockID).getBeforeConnector());
           if (link != null) {
             link.connect();
             Workspace.getInstance().notifyListeners(new WorkspaceEvent(
                 RenderableBlock.getRenderableBlock(link.getPlugBlockID()).getParentWidget(),
                 link, WorkspaceEvent.BLOCKS_CONNECTED));
             RenderableBlock.getRenderableBlock(link.getPlugBlockID()).repaintBlock();
             RenderableBlock.getRenderableBlock(link.getPlugBlockID()).repaint();
             RenderableBlock.getRenderableBlock(link.getPlugBlockID()).moveConnectedBlocks();
             RenderableBlock.getRenderableBlock(link.getSocketBlockID()).repaintBlock();
             RenderableBlock.getRenderableBlock(link.getSocketBlockID()).repaint();

           }
         }
       }
     } else if (validConnection(block.getAfterConnector())) {
       parentID = block.getAfterBlockID();
     }

     //remove from widget and container
     RenderableBlock.removeBlock(renderable, widget, container);

     // If the block is a stub parent, remove it from the map once we
     // no longer might need it.
     BlockStub.removeParentFromStubMap(blockId);

     //  <<<<<<<<<<<<<<<<<<<<<<<<<<==========================
     //  <<<<<<<<<<<<<<<<<<<<<<<<<<focus changing, coming out 
     //  <<<<<<<<<<<<<<<<<<<<<<<<<<==========================
     //If the deleted block had a parent, give the parent the focus,
     //Otherwise, give the focus to the canvas (NOT BLOCK CANVAS)
     FocusTraversalManager focusManager = Workspace.getInstance().getFocusManager();

     if (invalidBlockID(parentID)) {
       focusManager.setFocus(location, Block.NULL);
       blockCanvas.requestFocus();
       return true;
     } else {
       focusManager.setFocus(parentID);
       blockCanvas.requestFocus();
       return true;
     }
   }

   /**
    * Checks if a connection is a valid and ACTIVE connection.
    *
    * @param connection - BlockConnector in question
    *
    * @requires none
    * @return true if and only if connection != null && connection.hasBlock()
    * == true
    */
   private static boolean validConnection(BlockConnector connection){
     if (connection != null) {
       Long blockID = connection.getBlockID();
       if (!isNullBlockInstance(blockID) && connection.hasBlock()) {
         return true;
       }
     }
     return false;
   }

  /**
   * @param childBlock
   * @param widget
   *
   * @requires widget != null
   * @modifies
   * @effects Does nothing if: childBlock is invalid (null)
   *          Otherwise, remove childBlock from it's parent block
   *          if the childBlock has a parent.  If it does not have
   *          a parent, do nothing.
   */
  private static void disconnectBlock(Block childBlock, WorkspaceWidget widget){
    if (childBlock == null || invalidBlockID(childBlock.getBlockID())) {
      return;
    }
    BlockConnector childPlug = BlockLinkChecker.getPlugEquivalent(childBlock);
    if (childPlug == null || !childPlug.hasBlock() || isNullBlockInstance(childPlug.getBlockID())) {
      return;
    }
    Block parentBlock = Block.getBlock(childPlug.getBlockID());
    BlockConnector parentSocket = parentBlock.getConnectorTo(childBlock.getBlockID());
    if (parentSocket == null) {
      return;
    }
    //disconector if child connector exists and has a block connected to it
    BlockLink link = BlockLink.getBlockLink(childBlock, parentBlock, childPlug, parentSocket);
    if (link == null) {
      return;
    }

    link.disconnect();

    RenderableBlock parentRenderable = RenderableBlock.getRenderableBlock(parentBlock.getBlockID());
    if (parentRenderable == null) {
      throw new RuntimeException("INCONSISTENCY VIOLATION: " +
          "parent block was valid, non-null, and existed.\n\tBut yet, when we get it's renderable" +
          "representation, we recieve a null instance.\n\tIf the Block instance of an ID is " +
          "non-null then its graphical RenderableBlock should be non-null as well");
    }

    parentRenderable.blockDisconnected(parentSocket);
    Workspace.getInstance().notifyListeners(new WorkspaceEvent(widget, link,
        WorkspaceEvent.BLOCKS_DISCONNECTED));
  }

  /**
   * @return true if and only if block is invalid (null or ID==-1)
   */
  public static boolean invalidBlockID(Long blockID){
    if (blockID == null) {
      return true;
    } else if (blockID.equals(Block.NULL)) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isNullBlockInstance(Long blockID){
    if (blockID == null) {
      return true;
    } else if (blockID.equals(Block.NULL)) {
      return true;
    } else if (Block.getBlock(blockID) == null) {
      return true;
    } else if (Block.getBlock(blockID).getBlockID() == null) {
      return true;
    } else if (Block.getBlock(blockID).getBlockID().equals(Block.NULL)) {
      return true;
    } else if (RenderableBlock.getRenderableBlock(blockID)==null) {
      return true;
    } else if (RenderableBlock.getRenderableBlock(blockID).getBlockID() == null) {
      return true;
    } else if (RenderableBlock.getRenderableBlock(blockID).getBlockID().equals(Block.NULL)) {
      return true;
    } else {
      return false;
    }
  }

  public static RenderableBlock cloneBlock(Block myBlock){
    String myGenusName = myBlock.getGenusName();
    String label = myBlock.getBlockLabel();

    //sometimes the factory block will have an assigned label different
    //from its genus label.
    if (!myBlock.getInitialLabel().equals(label)) {
      //acquire prefix and suffix length from myBlock label
      int prefixLength = myBlock.getLabelPrefix().length();
      int suffixLength = myBlock.getLabelSuffix().length();
      //we need to set the block label without the prefix and suffix attached because those
      //values are automatically concatenated to the string specified in setBlockLabel.  I know its
      //weird, but its the way block labels were designed.
      //TODO we could do this outside of this method, even in constructor
      if (prefixLength > 0 || suffixLength > 0) {
        label = label.substring(prefixLength, label.length()-suffixLength);
      }
    }

    Block block;
    if (myBlock instanceof BlockStub) {
      //Just grab the first parent, since they all have the same name/genus
      Long parentID = ((BlockStub)myBlock).getParents().iterator().next();
      Block parent = Block.getBlock(parentID);
      block = new BlockStub(parent.getBlockID(),
          parent.getGenusName(), parent.getBlockLabel(), myGenusName, myBlock.getProperties());
    } else {
      label = makeBlockLabel(label, myGenusName);
      block = new Block(myGenusName, label, myBlock.getProperties());
    }

    // note that if myBlock was using the default description (from its genus)
    // this will still cause block to have an overriding description. It shouldn't
    // hurt anything except using up a little extra space.
    block.setBlockDescription(myBlock.getBlockDescription());

    // TODO - djwendel - create a copy of the RB properties too, using an RB copy constructor.  Don't just use the genus.
    //RenderableBlock renderable = new RenderableBlock(this.getParentWidget(), block.getBlockID());
    RenderableBlock renderable = new RenderableBlock(null, block.getBlockID());
    if (!myBlock.activated()) {
      renderable.setActivate(false);
    }
    renderable.setZoomLevel(BlockUtilities.zoom);
    renderable.redrawFromTop();
    renderable.repaint();
    return renderable;
  }

  // TODO(sharon) it would be nice if this code could be shared with unique
  // label creation code in ProcedureBlockManager
  public static String makeBlockLabel(String startLabel, String genusName) {
    //check genus instance counter and if label unique - change label accordingly
    //also check if label already has a value at the end, if so update counter to have the max value
    //TODO ria need to make this smarter
    //some issues to think about:
    // - what if they throw out an instance, such as setup2? should the next time they take out
    //   a setup block, should it have setup2 on it?  but wouldn't that be confusing?
    // - when we load up a new project with some instances with numbered labels, how do we keep
    //   track of new instances relative to these old ones?
    // - the old implementation just iterated through all the instances of a particular genus in the
    //   workspace and compared a possible label to the current labels of that genus.  if there wasn't
    //   any current label that matched the possible label, it returned that label.  do we want to do this?
    //   is there something more efficient?

    String labelWithIndex = startLabel;  // labelWithIndex will have the instance value

    // initialize value that will be appended to the end of the label
    String instanceKey = genusName + startLabel;
    int value = (instanceCounter.containsKey(instanceKey))
                 ? instanceCounter.get(instanceKey).intValue()
                 : 0;
    // begin check for validation of label
    // iterate until label is valid
    while (!isLabelValid(BlockGenus.getGenusWithName(genusName), labelWithIndex)){
      value++;
      labelWithIndex = startLabel + value;
    }

    // if we need to keep a counter for this genus, save current instance number
    if (value != 0) {
      instanceCounter.put(instanceKey, new Integer(value));
    }
    return labelWithIndex;
  }

  /**
   * Creates a string representation for the given RenderableBlock that
   * is disambiguated from string representations for blocks with the same label
   * by appending socket information to the end of the block's label.  The
   * created string is in the form:
   * BlockLabel [socketLabel1, ..., socketLabelN]
   * @param block the FactoryRenderableBlock to create a string representation of
   * @return a String containing the given block's keyword with a
   * list of its socket labels appended to the end.
   */
  public static String disambiguousStringRep(RenderableBlock block) {
    String rep = block.getKeyword();
    String genus = block.getGenus();
    Iterator<BlockConnector> sockets = block.getBlock().getSockets().iterator();
    if (sockets.hasNext()) {
      String socketLabels = " [";
      while (sockets.hasNext()) {
        if (socketLabels.length() > 2)
          socketLabels += ", ";
        socketLabels += sockets.next().getLabel();
      }
      socketLabels += "]";
      //HACK!!! TODO: rewriting typeblocking
      if (genus.equals("sum")) {
        return rep + " [number]";
      } else if (genus.equals("string-append")){
        return rep + " [text]";
      }
      return rep + socketLabels;
    }
    return rep;
  }


  /**
   * @param inputKeyword
   *
   * @requires keyword != null
   * @return List of TextualFactoryBlocks, {T}, such that:
   *                            T.toString contains keyword
   *                        T == null if no matching blocks were found
   *                            T.toString is unique for each T
   */
  public static List<TextualFactoryBlock> getAllMatchingBlocks(String inputKeyword){
    String keyword;
    // This causes multiply to appear in the typblocking dropdown list when the user types *,
    // and minus to appear when the user types -.   Doing the check here, rather than
    // at the lower level of the typeblocking code avoids the issue of testing character by character.
    // For example, if the user types ** that is treated as two stars, rather than as
    // two times signs.
    if (inputKeyword.equals("*")) {
      keyword = "\u00D7";
    } else if (inputKeyword.equals("-")) {
      keyword = "\u2212";
    } else {
      keyword = inputKeyword;
    }

    //Use Set such that we don't get any repeats
    Set<TextualFactoryBlock> matchingBlocks = new TreeSet<TextualFactoryBlock>(new MatchingComparator(keyword));

    //find all FactoryRenderableBlocks and check for a match
    for(RenderableBlock renderable : Workspace.getInstance().getFactoryManager().getBlocks()){

      // TODO: don't assume they're all FactoryRenderableBlocks!  Collisions aren't...
      if(renderable==null || renderable.getBlockID().equals(Block.NULL) || !(renderable instanceof FactoryRenderableBlock)){
        continue;
      }

      // first, check if query matches block keyword
      if(renderable.getKeyword().toLowerCase().contains(keyword.toLowerCase())){
        matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, renderable.getBlock().getBlockLabel()));
      }

      // grabs the quote block needed TODO: needs to be independent!
      if(keyword.startsWith("\"") && renderable.getBlock().getGenusName().equalsIgnoreCase("string")){
        String[] quote = keyword.split("\"");
        // makes sure that there is text after the " so that it can be placed onto the block
        if(quote.length > 1) {
          matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, "\"" + quote[1] + "\""));
        }
      }

      // otherwise, if the keyword is too long, check to see if
      // the user is trying to type extra info for disambiguation
      else if (keyword.length() > renderable.getKeyword().length()) {
        if(disambiguousStringRep(renderable).toLowerCase().contains(keyword.toLowerCase())){
          matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable,disambiguousStringRep(renderable)));
        }
      }

      /////////////////////////////////////
      //TODO: Add code here for nicknames//
      /////////////////////////////////////

    }

    /* if blocks have the same labels, the search results will be ambiguous.
     * the following expands the string representation of the TFB if needed
     * to disambiguate the blocks. */
    ArrayList<TextualFactoryBlock> disambiguatedMatches = new ArrayList<TextualFactoryBlock>(matchingBlocks);
    TextualFactoryBlock t1, t2;
    for (int i = 0; i < disambiguatedMatches.size(); i++) {
      t1 = disambiguatedMatches.get(i);
      if (i > 0) {
        t2 = disambiguatedMatches.get(i-1);
        if (t1.toString().equals(t2.toString())) {
          disambiguatedMatches.set(i, new TextualFactoryBlock(t1.getfactoryBlock(), disambiguousStringRep(t1.getfactoryBlock())));
          disambiguatedMatches.set(i-1, new TextualFactoryBlock(t2.getfactoryBlock(), disambiguousStringRep(t2.getfactoryBlock())));
        }
      }
      if (i < disambiguatedMatches.size() - 1) {
        t2 = disambiguatedMatches.get(i+1);
        if (t1.toString().equals(t2.toString())) {
          disambiguatedMatches.set(i, new TextualFactoryBlock(t1.getfactoryBlock(), disambiguousStringRep(t1.getfactoryBlock())));
          disambiguatedMatches.set(i+1, new TextualFactoryBlock(t2.getfactoryBlock(), disambiguousStringRep(t2.getfactoryBlock())));
        }
      }
    }
    //List<TextualFactoryBlock> f = new ArrayList<TextualFactoryBlock>();
    return disambiguatedMatches;
  }

  /**
   *
   * @param plus
   *
   * @requires plus != null
   * @return List containing the "+" TextualFactoryBlocks
   *                    and any other blocks containing "+"
   */
  public static List<TextualFactoryBlock> getPlusBlocks(String plus){
    Set<TextualFactoryBlock> matchingBlocks = new HashSet<TextualFactoryBlock>();
    // looks through the factory blocks
    for(RenderableBlock renderable : Workspace.getInstance().getFactoryManager().getBlocks()){
      if(renderable==null || renderable.getBlockID().equals(Block.NULL) || !(renderable instanceof FactoryRenderableBlock)){
        continue;
      }
      //TODO genus names are based from TNG, need to figure out a workaround
      // grabs the "+" number block
      if(renderable.getBlock().getGenusName().equalsIgnoreCase("sum")){
        // changes the label so that the search result will not be ambiguous
        matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, "+ [number]"));
      }
      // selects any other block that contains the number (for variables that contains the number)
      if(renderable.getKeyword().toLowerCase().contains(plus.toLowerCase())){
        matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, renderable.getBlock().getBlockLabel()));
      }
    }

    return new ArrayList<TextualFactoryBlock>(matchingBlocks);
  }

  /**
   *
   * @param digits
   *
   * @requires digits != null
   * @return List containing a number TextualFactoryBlock
   *                    and any other blocks containing the numbers
   */
  public static List<TextualFactoryBlock> getDigits(String digits){
    Set<TextualFactoryBlock> matchingBlocks = new TreeSet<TextualFactoryBlock>(new MatchingComparator(digits));
    // looks through the factory blocks
    for(RenderableBlock renderable : Workspace.getInstance().getFactoryManager().getBlocks()){
      if(renderable==null || renderable.getBlockID().equals(Block.NULL) || !(renderable instanceof FactoryRenderableBlock)){
        continue;
      }

      //TODO genus name are based from TNG, need to figure out a workaround
      // selects the number block
      if(renderable.getBlock().getGenusName().equalsIgnoreCase("number")){
        matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, digits));
      }
      // selects any other block that contains the number (for variables that contains the number)
      if(renderable.getKeyword().toLowerCase().contains(digits.toLowerCase())){
        matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, renderable.getBlock().getBlockLabel()));
      }
    }
    return new ArrayList<TextualFactoryBlock>(matchingBlocks);
  }



  /**
   * Comparator used by getAllMatchingBlocks() to sort according to the position in the word that the match occurs,
   * then according to the sort order of TextualFactoryBlocks
   */
  private static class MatchingComparator implements Comparator<TextualFactoryBlock>{
    private String keyword;
    public MatchingComparator(String keyword) {
      this.keyword = keyword.toLowerCase();
    }

    public int compare(TextualFactoryBlock t1, TextualFactoryBlock t2){
      if (t1.compareTo(t2) == 0) return 0;
      if (t1.toString().toLowerCase().indexOf(keyword) == t2.toString().toLowerCase().indexOf(keyword))
        return t1.compareTo(t2);
      return t1.toString().toLowerCase().indexOf(keyword) > t2.toString().toLowerCase().indexOf(keyword) ? 1 : -1;
    }
  }


  /**
   * Returns a new RenderableBlock instance with the matching genusName.
   * New block will also have matching label is label is not-null. May return null.
   *
   * @param genusName
   * @param label
   *
   * @requires if block associated with genusName has a non editable
   *                     or unique block label, then "label" MUST BE NULL.
   * @return  A new RenderableBlock with matching genusName and label (if label is not-null).
   *                    If no matching blocks were found, return null.
   */
  public static RenderableBlock getBlock(String genusName, String label){
    if (genusName == null) {
      return null;
    }

    //          find all blocks on the page and look for any match
    for (Block block : Workspace.getInstance().getBlocks()) {
      //make sure we're not dealing with null blocks
      if (block==null || block.getBlockID() == null || block.getBlockID().equals(Block.NULL)) {
        continue;
      }
      //find the block with matching genus and either a matching label or an editable label
      if (block.getGenusName().equals(genusName) && (block.isLabelEditable() ||
          block.getBlockLabel().equals(label) || block.isInfix())) {
        //for block stubs, need to make sure that the label matches because stubs of the same kind
        //(i.e. global var getters, agent var setters, etc.) have the same genusName
        //but stubs of different parents do not share the same label
        if (block instanceof BlockStub && !block.getBlockLabel().equals(label)) {
          continue;
        }
        //create new renderable block instance
        RenderableBlock renderable = BlockUtilities.cloneBlock(block);
        //make sure renderable block is not a null instance of a block
        if(renderable == null || renderable.getBlockID().equals(Block.NULL)){
          throw new RuntimeException("Invariant Violated: a valid non null blockID just" +
          "returned a null instance of RenderableBlock");
          //please throw an exception here because it wouldn't make any sense
          //if the Block is valid but it's associated RenderableBlock is not
        }
        //do not drop down default arguments
        renderable.ignoreDefaultArguments();
        //get corresponding block
        Block newblock = Block.getBlock(renderable.getBlockID());
        //make sure corresponding block is not a null instance of block
        if(newblock == null || newblock.getBlockID().equals(Block.NULL)){
          throw new RuntimeException("Invariant Violated: a valid non null blockID just" +
          "returned a null instance of Block");
          //please throw an exception here because it wouldn't make any sense
          //if the Block is valid but it's associated RenderableBlock is not
        }
        //attempt to set the label text if possible as defined by the specs
        //should not set the labels of block stubs because their labels are determined by their parent
        if((block.isLabelEditable() || block.getBlockLabel().equals(label))){
          if(label != null && !(block instanceof BlockStub)){
            if(newblock.isLabelEditable() && !newblock.labelMustBeUnique()){
              newblock.setBlockLabel(label);
            }
          }
        }
        //return renderable block
        return renderable;
      }



      /////////////////////////////////////
      //TODO: Add code here for nicknames//
      /////////////////////////////////////



    }
    //TODO: the part below is a hack. If there are other types of blocks, we need to account for them
    return null;
  }

  public static BlockNode makeNodeWithChildren(Long blockID){
    if(isNullBlockInstance(blockID)) return null;
    Block block = Block.getBlock(blockID);
    String genus = block.getGenusName();
    String parentGenus = block instanceof BlockStub ? ((BlockStub)block).getParentGenus() : null;
    String label;
    if (!block.labelMustBeUnique() || block instanceof BlockStub) {
      label = block.getBlockLabel();
    } else {
      label = null;
    }
    BlockNode node = new BlockNode(genus, parentGenus, label);
    for (int i = 0; i < block.getNumSockets(); i++) {
      BlockConnector socket = block.getSocketAt(i);
      if(socket.hasBlock()){
        node.addChild(makeNodeWithStack(socket.getBlockID()), i);
      }
    }
    return node;
  }
  public static BlockNode makeNodeWithStack(Long blockID){
    if(isNullBlockInstance(blockID)) return null;
    Block block = Block.getBlock(blockID);
    String genus = block.getGenusName();
    String parentGenus = block instanceof BlockStub ? ((BlockStub)block).getParentGenus() : null;
    String label;
    if (!block.labelMustBeUnique() || block instanceof BlockStub) {
      label = block.getBlockLabel();
    } else {
      label = null;
    }
    BlockNode node = new BlockNode(genus, parentGenus, label);
    for (int i = 0; i < block.getNumSockets(); i++) {
      BlockConnector socket = block.getSocketAt(i);
      if(socket.hasBlock()){
        node.addChild(makeNodeWithStack(socket.getBlockID()), i);
      }
    }
    if(block.hasAfterConnector()){
      node.setAfter(makeNodeWithStack(block.getAfterBlockID()));
    }
    return node;
  }

  /**
   * Checks to see if the block still exists
   * @return True if renderable block is still there, False otherwise
   */
  public static boolean blockExists(BlockNode node){
    String genusName = node.getGenusName(); //genusName may not be null
    RenderableBlock renderable = BlockUtilities.getBlock(genusName, node.getLabel());
    if(renderable == null){
      return false;
    }
    else{
      return true;
    }
  }

  public static RenderableBlock makeRenderable(BlockNode node, WorkspaceWidget widget){
    String genusName = node.getGenusName(); //genusName may not be null
    RenderableBlock renderable = BlockUtilities.getBlock(genusName, node.getLabel());
    if(renderable == null) {
      throw new RuntimeException(
          "No children block exists for this genus: " +genusName);
    }
    Block block = Block.getBlock(renderable.getBlockID()); //assume not null
    widget.blockDropped(renderable);

    // Add children in order of socket index, so that any expandable sockets
    // will be created by the time we need to link a child to it
    for (int i = 0; i < block.getNumSockets(); i++) {
      BlockConnector socket = block.getSocketAt(i);
      BlockNode child = node.getChildren().get(i);
      if (child != null) {
        RenderableBlock childRenderable = makeRenderable(child, widget);
        Block childBlock = Block.getBlock(childRenderable.getBlockID());

        //link blocks
        BlockLink link;
        if(childBlock.hasPlug()){
          link = BlockLinkChecker.canLink(block, childBlock, socket, childBlock.getPlug());
        }else if(childBlock.hasBeforeConnector()){
          link = BlockLinkChecker.canLink(block, childBlock,socket, childBlock.getBeforeConnector());
        }else{
          link=null;
        }//assume link is not null
        link.connect();
        Workspace.getInstance().notifyListeners(new WorkspaceEvent(
            RenderableBlock.getRenderableBlock(link.getPlugBlockID()).getParentWidget(),
            link, WorkspaceEvent.BLOCKS_CONNECTED));
      } //else the socket is empty.  We assume expandable sockets only have empty sockets last
    }
    if(node.getAfterNode() != null){
      BlockConnector socket = block.getAfterConnector(); //assume has after connector
      BlockNode child = node.getAfterNode();
      RenderableBlock childRenderable = makeRenderable(child, widget);
      Block childBlock = Block.getBlock(childRenderable.getBlockID());

      //link blocks
      BlockLink link;
      if(childBlock.hasPlug()){
        link = BlockLinkChecker.canLink(block, childBlock, socket, childBlock.getPlug());
      }else if(childBlock.hasBeforeConnector()){
        link = BlockLinkChecker.canLink(block, childBlock,socket, childBlock.getBeforeConnector());
      }else{
        link=null;
      }//assume link is not null
      link.connect();
      Workspace.getInstance().notifyListeners(new WorkspaceEvent(
          RenderableBlock.getRenderableBlock(
              link.getPlugBlockID()).getParentWidget(),link,
              WorkspaceEvent.BLOCKS_CONNECTED));

    }
    return renderable;
  }

}
