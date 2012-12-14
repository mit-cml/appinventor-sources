// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;
import openblocks.workspace.FactoryManager;
import openblocks.workspace.Page;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceListener;

import java.util.HashMap;

/**
 * Methods related to Young Android procedures, which handled a little
 * differently than the way that codeblocks expects. We do use codeblocks'
 * stub mechanism for creating caller stubs and getter/setter stubs
 * for procedure args.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class ProcedureBlockManager implements WorkspaceListener {

  private static final String PROC_GENUS_PREFIX = "define";
  private static final String PROC_GENUS_NORETURN = "define-void";
  private static final String PROC_CALLER_GENUS = "caller";

  private Workspace workspace;
  private HashMap<String, Long> procNameToBlockId;

  /**
   * Creates a manager for procedure blocks
   * @param workspace the current workspace
   */
  public ProcedureBlockManager(Workspace workspace) {
    this.workspace = workspace;
    procNameToBlockId = new HashMap<String, Long>();
  }


  public static boolean isArgSocket(BlockConnector socket) {
    return socket.getLabel().equals("arg");
  }

  public static boolean hasArgWithName(Block procedure, String name) {
    if (procedure == null) return false;
    for (BlockConnector curSocket : procedure.getSockets()) {
      Block argBlock = Block.getBlock(curSocket.getBlockID());
      if (argBlock != null && argBlock.getBlockLabel().equals(name)) {
        return true;
      }
    }
    return false;
  }


  /**
   * If this block is contained in a procedure, return that procedure
   * @param block
   */
  public static Block getProcFromBlock(Block block) {
    Block curBlock = block;
    while (true) {
      if (curBlock == null) {
        return null;
      }
      if (curBlock.hasPlug()) {
        curBlock = Block.getBlock(curBlock.getPlugBlockID());
      } else if (curBlock.hasBeforeConnector()) {
        curBlock = Block.getBlock(curBlock.getBeforeBlockID());
      } else break;
    }
    //check if it's in a procedure declaration or a procedure call
    if (!(isProcDeclBlock(curBlock) || curBlock.isCommandBlock())) return null;
    return curBlock;
  }
/**
 * Checks whether two blocks are in the same procedure
 * @param block1
 * @param block2
 * @return true if they are in the same procedure, false otherwise
 */
  public static boolean isInSameProcedure(Block block1, Block block2) {
    Block procedure1 = getProcFromBlock(block1);
    Block procedure2 = getProcFromBlock(block2);
    if (procedure1 == null || procedure2 == null) return false;
    return (procedure1.equals(procedure2));
  }

 /**
  * Returns whether the block is part of a procedure
  * @param block
  * @return true if the block is part of a procedure, false otherwise
  */
  public static boolean isInProcedure(Block block) {
    return (getProcFromBlock(block) != null);
  }


  /**
   * Return true if this block is a procedure declaration block in the
   * Young Android world; false otherwise.
   */
  public static boolean isProcDeclBlock(Block block) {
    return block.getGenusName().equals(PROC_GENUS_PREFIX) ||
        block.getGenusName().equals(PROC_GENUS_NORETURN);
  }

  public synchronized void workspaceEventOccurred(WorkspaceEvent event) {
    int eventType = event.getEventType();
    FactoryManager fm = workspace.getFactoryManager();
    switch (eventType) {
      case WorkspaceEvent.BLOCK_RENAMED:
        handleBlockRenamed(event);
        break;
      case WorkspaceEvent.BLOCK_REMOVED:
        handleBlockRemoved(event);
        break;
      case WorkspaceEvent.BLOCK_ADDED:
        handleBlockAdded(event);
        break;
      default:
        break;
    }
  }

  public synchronized void reset() {
    procNameToBlockId = new HashMap<String, Long>();
  }

  private void handleBlockAdded(WorkspaceEvent event) {
    if (!(event.getSourceWidget() instanceof Page)) {
      // don't care about blocks added to drawers
      return;
    }
    Block block = Block.getBlock(event.getSourceBlockID());
    if (isProcDeclBlock(block)) {
      // check that block label is unique across all proc genuses
      makeProcBlockNameUnique(block);
    }
  }

  private void handleBlockRemoved(WorkspaceEvent event) {
    Block block = Block.getBlock(event.getSourceBlockID());
    if (isProcDeclBlock(block)) {
      procNameToBlockId.remove(block.getBlockLabel());
      System.out.println("Removed procedure name " + block.getBlockLabel());
    }
  }

  private void handleBlockRenamed(WorkspaceEvent event) {
    if (!(event.getSourceWidget() instanceof Page)) {
      return;
    }

    Block block = Block.getBlock(event.getSourceBlockID());
    if (isProcDeclBlock(block)) {
      makeProcBlockNameUnique(block);
    }
  }

  // TODO(sharon) it would be nice if we could share code with the unique label
  // creation code in BlockUtilities. However, in this case we need labels
  // to be unique across two genuses, and we're catching duplicate labels
  // at block add time rather than at block creation.
  private void makeProcBlockNameUnique(Block block) {
    String newLabel = block.getBlockLabel();
    if (procNameToBlockId.containsKey(newLabel)
        && !procNameToBlockId.get(newLabel).equals(block.getBlockID())) {
      String oldLabel = newLabel;
      String baseLabel;
      if (newLabel.startsWith(block.getInitialLabel())) {
        baseLabel = block.getInitialLabel();
      } else {
        baseLabel = newLabel;
      }
      newLabel = makeUniqueProcLabel(baseLabel);
      block.setBlockLabel(newLabel);
      System.out.println("Changed proc label from " + oldLabel + " to "
          + newLabel + " to make it unique");
    }
    procNameToBlockId.put(newLabel, block.getBlockID());
  }

  private String makeUniqueProcLabel(String baseLabel) {
    int i = 1;
    String newName = baseLabel + i;
    while (procNameToBlockId.containsKey(newName)) {
      i++;
      newName = baseLabel + i;
    }
    return newName;
  }


}
