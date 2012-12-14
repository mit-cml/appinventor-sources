// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;
import openblocks.codeblocks.LinkRule;


/**
 * YaRule specifies rules for Block linking in the Young Android language.
 *
 */
public class YaRule implements LinkRule {

  /**
   * Boolean function that returns true if the two sockets being
   * connected follow the Young Android rules for linking, and false otherwise.
   *
   * @param block1 one of the blocks being checked for linking capability
   * @param block2 the other block being checked for linking capability
   * @param socket1 the socket in block1 being checked for linking capability
   * @param socket2 the socket in block2 being checked for linking capability
   */
  public boolean canLink(Block block1, Block block2, BlockConnector socket1,
                         BlockConnector socket2) {
    Block socketBlock;
    Block plugBlock;
    String socketLabel;
    // Determine sockets and plugs
    if (block1.hasPlug() && block1.getPlug() == socket1) {
      plugBlock = block1;
      socketBlock = block2;
      socketLabel = socket2.getLabel();
    } else {
      plugBlock = block2;
      socketBlock = block1;
      socketLabel = socket1.getLabel();
    }
    if (socketLabel.length() == 0) {
      socketLabel = "default";
    }
    // TODO(user): If the blocks cannot link, move them out of the way so
    // the blocks do not appear linked.
    if (!BlockRules.canLink(socketBlock, plugBlock, socketLabel)) {
      // Check if blocks can be linked through coercion
      CoercionResult result = BlockRules.canCoerce(socketBlock, plugBlock, socketLabel);
      if (result.getResult() == false) {
        FeedbackReporter.showErrorMessage(result.getErrorMessage(), "Misplaced block");
        return false;
      }
    }
//    if (plugBlock.getGenusName().equals("argument")) {
//      Block proc = ProcedureBlockManager.getProcFromBlock(socketBlock);
//      if (ProcedureBlockManager.hasArgWithName(proc, plugBlock.getBlockLabel())) {
//        FeedbackReporter.showErrorMessageWithTitle(
//            "An argument with that name already exists in this procedure", 
//            "Error adding argument");
//        return false;
//      }
//    }
//
//    if (plugBlock.getGenusName().equals("getter")) {
//      Block parent = Block.getBlock(((BlockStub) plugBlock).getParents().iterator().next());
//      if (parent.isArgument()) {
//        Block proc = ProcedureBlockManager.getProcFromBlock(socketBlock);
//        if (!ProcedureBlockManager.hasArgWithName(proc, plugBlock.getBlockLabel())) {
//          FeedbackReporter.showErrorMessageWithTitle(
//              "This block doesn't match any procedure arguments", "Error connecting argument");
//          return false;
//        }
//      }
//    }
    return true;
  }

  /**
   * A boolean representing whether this rule must pass for the blocks to connect.
   */
  public boolean isMandatory() {
    return true;
  }
}