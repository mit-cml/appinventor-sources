
package openblocks.codeblocks;

import openblocks.codeblocks.BlockConnector.PositionType;
import openblocks.renderable.BlockUtilities;
import openblocks.renderable.RenderableBlock;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;
import openblocks.yacodeblocks.FeedbackReporter;
import openblocks.yacodeblocks.ProcedureBlockManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * <code>BlockStub</code> are a special form of blocks that provide a
 * particular reference to its "parent" block.  These references can
 * set, get, or increment the value of its "parent" block.  References
 * may also get the value for a particular agent.  Finally, for a
 * procedure block, its reference is a call block, which executes the
 * procedure
 *
 * The parent instance for a set of stubs is not permanent.  The parent intance
 * may change if the original parent it removed and then a new one with the
 * same parent name is added to the block canvas. BlockStub manages the mapping
 * between stubs and their parent.
 */
public class BlockStub extends Block {

  /**
   * STUB HASH MAPS669 key: parentName + parentGenus
   *
   *  Key includes both parentName and parentGenus because the names of two
   * parents may be the same if they are of different genii blockids of parents
   * are not used as a reference because parents and stubs are connected by the
   * parentName+parentGenus information not the blockID. This connection is more
   * apparent when the parent Block is removed/deleted. The stubs become
   * dangling references. These stubs are resolved when a new parent block is
   * created with the previous parent's name. The value is a Set, since
   * there may be multiple blocks with the same genus and name (as in the case
   * of arguments)
   */
  protected static HashMap<String, Set<Long>> parentNameToParentBlocks =
      new HashMap<String, Set<Long>>();
  protected static HashMap<String, Set<Long>> parentNameToBlockStubs =
      new HashMap<String, Set<Long>>();

  /**
   * Temporary mapping for parent type (caller plugs).
   * TODO(user) remove once BlockUtilities cloneBlock() is finished
   */
  private static Map<String, String> parentToPlugType = new HashMap<String, String>();

  // stub type string constants
  private static final String GETTER_STUB = "getter";
  private static final String SETTER_STUB = "setter";
  private static final String CALLER_STUB = "caller";
  private static final String AGENT_STUB = "agent";
  // this particular stub type is unique to Starlogo TNG - may choose to remove it
  private static final String INC_STUB = "inc";


  protected String parentName;
  protected final String parentGenus;

  private final String stubGenus;

  /**
   * mySocketToParentSocket maps the sockets of this stubs to the
   * sockets of its parent this mapping is used to help in the
   * maintenance this stub's sockets with respect to its parent
   */
  //  private HashMap<BlockConnector,BlockConnector> mySocketToParentSocket =
  //  new HashMap<BlockConnector, BlockConnector>();


  /**
   * Constructs a new <code>BlockStub</code> instance using the
   * specified genus name of its parent block, the block id of its
   * parent, the block name of parent and its stub genus.  The exact
   * reference to the parent through the specified initParentID is
   * needed, in addition to the other specified parameters, to
   * completely construct a new block stub.
   * @param initParentID the Long block ID of its initial parent
   * @param parentGenus the BlockGenus String name of its initial parent
   * @param parentName
   * @param stubGenus
   */
  public BlockStub(Long initParentID, String parentGenus, String parentName, String stubGenus) {
    super(stubGenus);

    // assert initParentID != Block.NULL : "Parent id of stub should not be null");

    this.parentGenus = parentGenus;
    this.parentName = parentName;
    this.stubGenus = stubGenus;

    // initial parent of this
    Block parent = Block.getBlock(initParentID);
    // has parent block label
    this.setBlockLabel(parent.getBlockLabel());
    // initialize stub properties based on stubGenus such as
    // sockets, plugs, and labels this initialization assumes that
    // nothing is connected to the parent yet. note: instead of
    // modifying the stub blocks current sockets, we replace them
    // with whole new ones such that the initkind of the stub
    // blocks connectors are the same as their parents
    if (stubGenus.startsWith(GETTER_STUB)) {
      // set plug to be the single socket of parent or plug if parent has no sockets
      if (parent.getNumSockets() > 0) {
        this.setPlug(parent.getSocketAt(0).getKind(), this.getPlug().getPositionType(),
                     this.getPlugLabel(), this.getPlug().isLabelEditable(), Block.NULL);
      } else {
        this.setPlug(parent.getPlugKind(), this.getPlug().getPositionType(),
                     this.getPlugLabel(), this.getPlug().isLabelEditable(), Block.NULL);
      }
    } else if (stubGenus.startsWith(SETTER_STUB)) {
      BlockConnector mySoc = this.getSocketAt(0);
      // set socket type to be parent socket type or plug if parent has no sockets
      if (parent.getNumSockets() > 0) {
        this.setSocketAt(0, parent.getSocketAt(0).getKind(), mySoc.getPositionType(),
                         mySoc.getLabel(), mySoc.isLabelEditable(), mySoc.isIndented(),
                         mySoc.isExpandable(), mySoc.getBlockID());
      } else {
        this.setSocketAt(0, parent.getPlugKind(), mySoc.getPositionType(),
                         mySoc.getLabel(), mySoc.isLabelEditable(), mySoc.isIndented(),
                         mySoc.isExpandable(), mySoc.getBlockID());
      }
    } else if (stubGenus.startsWith(CALLER_STUB)) {
      updateConnectors();

      // TODO(user): remove the following once BlockUtilities.cloneBlock() is finished
      // If our parent already has a plug type, we want to update
      // Note that we don't need to call renderables, since we are still
      // in the constructor
      String kind = parentToPlugType.get(parent.getBlockLabel() + parent.getGenusName());
      if (kind != null) {
        removeBeforeAndAfter();
        // TODO(user) commented code relates to creating mirror
        // plugs for caller stubs that have no sockets if
        // (this.getNumSockets() == 0){ setPlug(kind,
        // PositionType.MIRROR, "", false, Block.NULL); } else
        // {
        setPlug(kind, PositionType.SINGLE, "", false, Block.NULL);
        // }
      }
    } else if (stubGenus.startsWith(AGENT_STUB)) {
      // getter for specific who
      // set plug to be parent single socket kind or plug kind if parent has no sockets
      if (parent.getNumSockets() > 0) {
        setPlug(parent.getSocketAt(0).getKind(), this.getPlug().getPositionType(),
                this.getPlugLabel(), this.getPlug().isLabelEditable(), this.getPlugBlockID());
      } else {
        setPlug(parent.getPlugKind(), this.getPlug().getPositionType(),
                this.getPlugLabel(), this.getPlug().isLabelEditable(), this.getPlugBlockID());
      }
    } else if (stubGenus.startsWith(INC_STUB)) {
      // only included for number variables
      // do nothing for now
    }

    // has  page label of parent if parent has page label
    this.setPageLabel(parent.getPageLabel());
    // add new stub to hashmaps
    // parent should have existed in hashmap before this stub was created
    // (look at main Block constructor)
    // thus no problem should occur with following line
    parentNameToBlockStubs.get(parentName + parentGenus).add(this.getBlockID());

  }

  /**
   * Constructs a new <code>BlockStub</code> instance using the
   * specified genus name of its parent block, the block id of its
   * parent, the block name of parent and its stub genus.  The exact
   * reference to the parent through the specified initParentID is
   * needed, in addition to the other specified parameters, to
   * completely construct a new block stub.
   * @param initParentID the Long block ID of its initial parent
   * @param parentGenus the BlockGenus String name of its initial parent
   * @param parentName
   * @param stubGenus
   * @param properties the properties to use to initialize this block stub's
   * properties
   */
  public BlockStub(Long initParentID, String parentGenus, String parentName, String stubGenus,
                   HashMap<String, String> properties) {
    this(initParentID, parentGenus, parentName, stubGenus);
    this.properties.putAll(properties);
  }

   /**
   * Constructs a new BlockStub instance.  This contructor is
   * protected as it should only be called while Block loads its
   * information from the save String
   * @param blockID the Long block ID of this
   * @param stubGenus the BlockGenus of this
   * @param label the Block label of this
   * @param parentName the String name of its parent
   * @param parentGenus the String BlockGenus name of its parent
   */
  protected BlockStub(Long blockID, String stubGenus, String label, String parentName,
                      String parentGenus) {
    super(blockID, stubGenus, label, true);   // stubs may have stubs...
    // unlike the above constructor, the blockID specified should already
    // be referencing a fully loaded block with all necessary information
    // such as sockets, plugs, labels, etc.
    // the only information we need to handle is the stub information here.
    this.stubGenus = stubGenus;
    this.parentName = parentName;
    this.parentGenus = parentGenus;

    // there's a chance that the parent for this has not been
    // added to parentNameToBlockStubs mapping
    String key = parentName + parentGenus;
    if (parentNameToBlockStubs.containsKey(key)) {
      parentNameToBlockStubs.get(key).add(this.getBlockID());
    } else {
      HashSet<Long> stubs = new HashSet<Long>();
      stubs.add(this.getBlockID());
      parentNameToBlockStubs.put(key, stubs);
    }
  }

  /**
   * Clears all the mappings between parents and stubs.
   */
  public static void reset() {
    parentNameToBlockStubs.clear();
    parentNameToParentBlocks.clear();
  }

  /**
   * Removes the ID of a deleted block from all the stub lists.
   * Important when deleting procedure definitions.
   */
  public static void forgetStub(long blockID) {
    if (! (Block.getBlock(blockID) instanceof BlockStub)) return;
    for (String key : parentNameToBlockStubs.keySet()) {
      parentNameToBlockStubs.get(key).remove(blockID);
    }
  }

  /**
   * Returns true iff blockID is in the child stub list of some parent
   */
  public static boolean hasParent(long blockID) {
    for (String key : parentNameToBlockStubs.keySet()) {
      Set<Long> childStubs = parentNameToBlockStubs.get(key);
      if (childStubs.contains(blockID)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a list of the block ids of the specified parent's stubs
   * @param blockID
   */
  public static Iterable<Long> getStubsOfParent(Long blockID) {
    Set<Long> stubs = parentNameToBlockStubs.get(
        Block.getBlock(blockID).getBlockLabel() + Block.getBlock(blockID).getGenusName());
    if (stubs != null) {
      return stubs;
    } else {
      return new HashSet<Long>();
    }
  }

  /**
   * Returns the number of the specified parent's stubs
   * @param blockID
   */
  public static int countStubsOfParent(Long blockID) {
    Set<Long> stubs = parentNameToBlockStubs.get(
        Block.getBlock(blockID).getBlockLabel() + Block.getBlock(blockID).getGenusName());
    if (stubs != null) {
      return stubs.size();
    } else {
      return 0;
    }
  }

  /**
   * Saves the parent block information with the specified blockID in the Stub Map
   * @param blockID
   */
  public static void putNewParentInStubMap(Long blockID) {
    String key = Block.getBlock(blockID).getBlockLabel() + Block.getBlock(blockID).getGenusName();
    if (parentNameToParentBlocks.get(key) == null) {
      parentNameToParentBlocks.put(key, new HashSet<Long>());
    }
    parentNameToParentBlocks.get(key).add(blockID);

    if (parentNameToBlockStubs.get(key) == null) {
      parentNameToBlockStubs.put(key, new HashSet<Long>());
    }

    // notify dangling stubs and update their renderables
    // dangling stubs will be waiting to have a parent assigned to them
    // and reflect that graphically
    //TODO(user) Figure out if/how dangling stubs come about and whether they
    // should.
    for (Long stubID : parentNameToBlockStubs.get(key)) {
      BlockStub stub = (BlockStub) Block.getBlock(stubID);
      stub.notifyRenderable();
    }

  }

  /**
   * Removes the blockID from the parentNameToParentBlock map
   * @param blockID
   */
  public static void removeParentFromStubMap(Long blockID) {
    Block block = Block.getBlock(blockID);
    String key = block.getBlockLabel() + block.getGenusName();
    if (parentNameToParentBlocks.containsKey(key)) {
      parentNameToParentBlocks.get(key).remove(blockID);
      if (parentNameToParentBlocks.get(key).size() == 0) {
        parentNameToParentBlocks.remove(key);
      }
    }
  }

  /**
   * Updates BlockStub hashmaps and the BlockStubs of the parent of its new name
   * @param oldParentName
   * @param newParentName
   * @param parentID
   */
  public static void parentNameChanged(String oldParentName, String newParentName,
                                       Long parentID) {
    Block parentBlock = Block.getBlock(parentID);
    if (parentBlock == null) {
      System.out.println(
          "parentNameChanged called with non-existent parent block" + parentID);
      return;
    }
    String parentGenus = parentBlock.getGenusName();
    String oldKey = oldParentName + parentGenus;
    String newKey = newParentName + parentGenus;
    // only update if parents name really did "change" meaning the new parent name is
    // different from the old parent name
    if (oldKey.equals(newKey)) return;
    if (parentNameToParentBlocks.get(newKey) == null) {
      parentNameToParentBlocks.put(newKey, new HashSet<Long>());
    }
    parentNameToParentBlocks.get(newKey).add(parentID);
    // special cases for arguments
    if (parentBlock.isArgument()) {
      argumentNameChanged(newParentName, parentID, parentBlock, oldKey, newKey);
    } else {
      // update the parent name of each stub
      Set<Long> stubs = parentNameToBlockStubs.get(oldKey);
      if (stubs == null) {
        System.out.println(
            "parentNameChanged: can't find old parent in map. duplicate stubs?");
        return;
      }
      for (Long stub : stubs) {
        BlockStub blockStub = (BlockStub) Block.getBlock(stub);
        blockStub.parentName = newParentName;
        // update block label of each
        blockStub.setBlockLabel(newParentName);
        blockStub.notifyRenderable();
      }

      // check if any stubs already exist for new key
      Set<Long> existingStubs = parentNameToBlockStubs.get(newKey);
      if (existingStubs != null) {
        stubs.addAll(existingStubs);
      }
      System.out.println("Put2 :" + newKey);
      parentNameToBlockStubs.put(newKey, stubs);

      // remove old parent name from hash maps
      parentNameToParentBlocks.remove(oldKey);
      parentNameToBlockStubs.remove(oldKey);
    }
  }

  /**Updates hashmaps storing this block, and notifies any stubs of the name
   * change
   * @param newParentName
   * @param parentID
   * @param parentBlock
   * @param oldKey
   * @param newKey
   */
  private static void argumentNameChanged(String newParentName, Long parentID,
      Block parentBlock, String oldKey, String newKey) {
    Set<Long> stubsToMove = new HashSet<Long>();
    List<BlockStub> possibleDefinitions = new ArrayList<BlockStub>();

    for (Long stubID : parentNameToBlockStubs.get(oldKey)) {
      BlockStub blockStub = (BlockStub) Block.getBlock(stubID);
      if (ProcedureBlockManager.isInSameProcedure(parentBlock, blockStub)) {
        stubsToMove.add(stubID);
        //unattached stubs
      } else if (!ProcedureBlockManager.isInProcedure(blockStub) && !blockStub.isInDrawer()) {
        // only move unattached blocks if no other args share this name
        if (blockStub.getParents().size() == 1) {
          stubsToMove.add(stubID);
        }
      } else if (blockStub.isInDrawer()) {
        possibleDefinitions.add(blockStub);
      }
    }
    BlockStub definitionToMove = null;
    // If there were multiple arguments with this name, at least one definition
    // was invisible.  Make one visible, and then move it.
    if (parentNameToParentBlocks.get(oldKey).size() >= 2) {
      for (BlockStub blockStub : possibleDefinitions) {
        if (!blockStub.getRenderableBlock().isVisible()) {
          blockStub.getRenderableBlock().showBlock(true);
          definitionToMove = blockStub;
          break;
        }
      }
    } else {
      definitionToMove = possibleDefinitions.get(0);
    }
    if (definitionToMove != null) { //shouldn't be null
      stubsToMove.add(definitionToMove.getBlockID());
    }

    // If there is already an arg with the new name, make this arg's definition invisible
    if (parentNameToParentBlocks.get(newKey).size() >= 2) {
      definitionToMove.getRenderableBlock().showBlock(false);
      definitionToMove.notifyRenderable();
    }
    for (Long stubID : stubsToMove) {
      BlockStub blockStub = (BlockStub) Block.getBlock(stubID);
      blockStub.parentName = newParentName;
      blockStub.setBlockLabel(newParentName);
      blockStub.notifyRenderable();
    }
    if (parentNameToBlockStubs.get(newKey) == null) {
      parentNameToBlockStubs.put(newKey, new HashSet<Long>());
    }
    parentNameToBlockStubs.get(newKey).addAll(stubsToMove);
    parentNameToBlockStubs.get(oldKey).removeAll(stubsToMove);
    parentNameToParentBlocks.get(oldKey).remove(parentID);
  }

  /**
   * Updates the BlockStubs associated with the parent of its new page label
   * @param newPageLabel
   * @param parentID
   */
  public static void parentPageLabelChanged(String newPageLabel, Long parentID) {
    String key = Block.getBlock(parentID).getBlockLabel() +
        Block.getBlock(parentID).getGenusName();

    // update each stub
    Set<Long> stubs = parentNameToBlockStubs.get(key);
    for (Long stub : stubs) {
      BlockStub blockStub = (BlockStub) Block.getBlock(stub);
      blockStub.setPageLabel(newPageLabel);
      blockStub.notifyRenderable();
    }

  }

  /**
   * Updates the BlocksStubs associated with the parent of its new page label
   * @param parentID
   */
  public static void parentConnectorsChanged(Long parentID) {
    String key = Block.getBlock(parentID).getBlockLabel() +
        Block.getBlock(parentID).getGenusName();

    // update each stub only if stub is a caller (as callers are the only type of stubs that
    // can change its connectors after being created)
    Set<Long> stubs = parentNameToBlockStubs.get(key);
    for (Long stub : stubs) {
      BlockStub blockStub = (BlockStub) Block.getBlock(stub);
      if (blockStub.stubGenus.startsWith(CALLER_STUB)) {
        //System.out.println("updating connectors of: "+blockStub);
        if (blockStub.updateConnectors()) {
          blockStub.notifyRenderable();
        }
      }
    }
  }

  /**
   * Updates the plug on caller stubs associated with the given parent.
   * @param kind the new plug kind that callers should set
   */
  public static void parentPlugChanged(Long parentID, String kind) {
    String key = Block.getBlock(parentID).getBlockLabel() +
        Block.getBlock(parentID).getGenusName();

    //  Update our type mapping.
    if (kind == null) {
      parentToPlugType.remove(key);
    } else {
      parentToPlugType.put(key, kind);
    }

    //  update each stub only if stub is a caller
    Set<Long> stubs = parentNameToBlockStubs.get(key);
    for (Long stub : stubs) {
      BlockStub blockStub = (BlockStub) Block.getBlock(stub);
      if (blockStub.stubGenus.startsWith(CALLER_STUB)) {
        if (kind == null) {
          blockStub.restoreInitConnectors();
        } else {
          blockStub.updatePlug(kind);
        }
      }
    }
  }

  ////////////////////////////////////
  // PARENT INFORMATION AND METHODS //
  ////////////////////////////////////

  /**
   * Returns the parent name of this stub
   * @return the parent name of this stub
   */
  public String getParentName() {
    return parentName;
  }

  /**
   * Returns the parent blocks of this stub
   * @return the parent blocks of this stub
   */
  public Set<Long> getParents() {
    String key = parentName + parentGenus;
    if (!parentNameToParentBlocks.containsKey(key)) {
      return null;
    }
    return parentNameToParentBlocks.get(key);
  }


  /**
   * Returns the number of blocks that share this block's name
   * This is currently only useful for arguments, but could be
   * used for any block type that needs to share a name.
   *
   * @return the parent block of this stub
   */
  public static int countBlocksWithName(Block parent) {
    String key = parent.getBlockLabel() + parent.getGenusName();
    if (!parentNameToParentBlocks.containsKey(key)) {
      return 0;
    }
    return parentNameToParentBlocks.get(key).size();
  }

  /**
   * Returns the parent block genus of this stub
   * @return the parent block genus of this stub
   */
  public String getParentGenus() {
    return parentGenus;
  }

  /**
   *
   */
  public boolean doesParentExist() {
    //TODO(user): needs to check BlockCanvas if parent is "alive"

    return true;
  }

  ///////////////////////////////////
  // METHODS OVERRIDDEN FROM BLOCK //
  ///////////////////////////////////

  /**
   * Overriden from Block.  Can not change the genus of a Stub.
   */
  @Override
  public void changeGenusTo(String genusName, boolean preserveLabel) {
    //return null;
  }

  //////////////////////////////////////////////////
  //BLOCK STUB CONNECTION INFORMATION AND METHODS //
  //////////////////////////////////////////////////

  /**
   * Updates the connectors of this stub according to its parent.
   * For now only caller stubs should update their connector information after
   * being created.
   * Returns true if any changes were made, otherwise false.
   */
  private boolean updateConnectors() {
    boolean changed = false;
    //caller stubs will only have one parent
    Block parent = Block.getBlock(getParents().iterator().next());
    if (parent != null) {
      // get list of parent arg sockets
      ArrayList<BlockConnector> parentArgSockets =
        new ArrayList<BlockConnector>();
      for (BlockConnector parentSocket : parent.getSockets()) {
        if (ProcedureBlockManager.isArgSocket(parentSocket) &&
            !BlockUtilities.invalidBlockID(parentSocket.getBlockID())) {
          parentArgSockets.add(parentSocket);
        }
      }
      if (getNumSockets() <= parentArgSockets.size()) {
        // Synchronize names of initial sockets. If parent has more, add
        // them at the end. Want to avoid disconnecting any already
        // connected blocks if possible
        int i = 0; // socket index
        for (BlockConnector parentSocket : parentArgSockets) {
          if (i < getNumSockets()) {
            BlockConnector con = getSocketAt(i);
            Block parentArgBlock = Block.getBlock(parentSocket.getBlockID());
            if (parentArgBlock != null) {
              String parentLabel = parentArgBlock.getBlockLabel();
              if (!con.getLabel().equals(parentLabel)) {
                con.setLabel(parentLabel);
                changed = true;
              }
            } else {
              System.out.println("updateConnectors: cant find parent arg block "
                  + parentSocket.getBlockID());
            }
          } else {
            addSocket(parentSocket.getKind(), BlockConnector.PositionType.SINGLE,
                Block.getBlock(parentSocket.getBlockID()).getBlockLabel(),
                false, false, false, Block.NULL);
            changed = true;
          }
          i++;
        }
      } else {
        // Parent had sockets removed. Try to determine the missing ones by
        // name and remove those from our set if possible.
        int myIndex = 0; // index into my sockets
        while (myIndex < getNumSockets() && myIndex < parentArgSockets.size()) {
          BlockConnector mySocket = getSocketAt(myIndex);
          BlockConnector parentSocket = parentArgSockets.get(myIndex);
          myIndex++;
          String parentLabel =
            Block.getBlock(parentSocket.getBlockID()).getBlockLabel();
          if (!parentLabel.equals(mySocket.getLabel())) {
            removeSocket(mySocket);
            changed = true;
            myIndex--;
          }
        }
        // remove any extra sockets we have at the end
        while (getNumSockets() > parentArgSockets.size()) {
          removeSocket(getSocketAt(parentArgSockets.size()));
          changed = true;
        }
        if (getNumSockets() != parentArgSockets.size()) {
          // we somehow ended up with the wrong number of sockets. Give up :(
          // TODO(sharon): Is this ever possible?
          FeedbackReporter.showSystemErrorMessage(
              "The blocks world is confused." +
              " We ended up with " + getNumSockets() + " sockets in caller and " +
              parentArgSockets.size() + " sockets in parent for " +
              getBlockLabel());

        }
      }
    }
    return changed;
  }

  /**
   * Restores the initial state of the before, after, and plug. Disconnects
   * any invalid blocks. Only caller stubs should use this method.
   */
  private void restoreInitConnectors() {
    if (!hasPlug()) {
      return;     // Already in original state
    }

    // We have to check for a plug connector.
    Long id = getPlugBlockID();
    if (id != null && !id.equals(Block.NULL)) {
      disconnectBlock(id);
    }

    // Always synchronize! We can't have both a plug and a before.
    removePlug();
    resetBeforeAndAfter();
    RenderableBlock.getRenderableBlock(getBlockID()).updateConnectors();
    notifyRenderable();
  }

  /**
   * Updates the plug type. Disconnects any invalid blocks. Only caller
   * stubs should use this method.
   * @param kind must not be null
   */
  private void updatePlug(String kind) {
    if (hasPlug() && getPlugKind().equals(kind)) {
      return;
    }

    // We have to check for a before and after block.
    Long id = getBeforeBlockID();
    if (id != null && !id.equals(Block.NULL)) {
      disconnectBlock(id);
    }
    id = getAfterBlockID();
    if (id != null && !id.equals(Block.NULL)) {
      disconnectBlock(id);
    }

    // We also need to check the plug, because it may be connected to
    // the wrong type.
    id = getPlugBlockID();
    if (id != null && !id.equals(Block.NULL)) {
      disconnectBlock(id);
    }

    // Always synchronize! We can't have both a plug and a before.
    removeBeforeAndAfter();
    // TODO(user) commented code relates to creating mirror plugs for
    // caller stubs that have no sockets
    /* if (this.getNumSockets() == 0) {
       setPlug(kind, PositionType.MIRROR, kind, false, Block.NULL);
       } else { */
    setPlug(kind, PositionType.SINGLE, kind, false, Block.NULL);
    // }
    RenderableBlock.getRenderableBlock(getBlockID()).updateConnectors();
    notifyRenderable();
  }

  /**
   * Disconnect the given block from us. Must have a valid id.
   */
  private void disconnectBlock(Long id) {
    Block b2 = Block.getBlock(id);
    BlockConnector conn2 = b2.getConnectorTo(getBlockID());
    BlockConnector conn = getConnectorTo(id);
    BlockLink link = BlockLink.getBlockLink(this, b2, conn, conn2);
    RenderableBlock rb = RenderableBlock.getRenderableBlock(link.getSocketBlockID());
    link.disconnect();
    rb.blockDisconnected(link.getSocket());
    Workspace.getInstance().notifyListeners(
        new WorkspaceEvent(rb.getParentWidget(), link, WorkspaceEvent.BLOCKS_DISCONNECTED));

  }

  ////////////////////////////////////////
  // METHODS FROM BLOCK GENUS           //
  ////////////////////////////////////////

  /**
   * Returns the Color of this; May return Color.BLACK if color was unspecified.
   * @return the Color of this; May return Color.BLACK if color was unspecified.
   */

  public Color getColor() {
    Block b = Block.getBlock(getParents().iterator().next());
    if (b == null){
      b = this;
    }
    RenderableBlock rb = RenderableBlock.getRenderableBlock(b.getBlockID());
    return rb == null ? Color.BLACK : rb.getColor();
  }


  /**
   * @return current information about block
   */
  @Override
  public String toString() {
    return "Block Stub +" + getBlockID() + ": " + getBlockLabel() + " with sockets: " +
        getSockets() + " and plug: " + getPlug();
  }

  @Override
  public boolean isCommandBlock() {
    return hasAfterConnector() && hasBeforeConnector();
  }

  @Override
  public boolean isDataBlock() {
    return !hasAfterConnector() && !hasBeforeConnector();
  }


  @Override
  public boolean isFunctionBlock() {
    return hasPlug() && (this.getNumSockets() > 0);
  }

  ////////////////////////
  // SAVING AND LOADING //
  ////////////////////////

  @Override
  public String getSaveString(String renderablesStuff) {
    StringBuffer buf = new StringBuffer();
    buf.append("<BlockStub>");
    buf.append("<StubParentName>");
    buf.append(parentName);
    buf.append("</StubParentName>");
    buf.append("<StubParentGenus>");
    buf.append(parentGenus);
    buf.append("</StubParentGenus>");
    buf.append(super.getSaveString(renderablesStuff));
    buf.append("</BlockStub>\n");
    return buf.toString();
  }
}
