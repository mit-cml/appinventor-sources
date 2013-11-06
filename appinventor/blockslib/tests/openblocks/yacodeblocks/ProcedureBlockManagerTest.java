// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
// All Rights Reserved.

package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * Test things related to procedure blocks and their caller stubs
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class ProcedureBlockManagerTest extends TestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Initialize the workspace
    TestUtils.setupWorkspace(null, null);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testProcedures() throws Exception {
    String blkString = TestUtils.getFileAsString(
        TestUtils.TESTING_SOURCE_PATH + "procBlock.blk");
    String scmString = TestUtils.getFileAsString(
        TestUtils.TESTING_SOURCE_PATH + "procBlock.scm");
    TestUtils.getController().loadSourceAndProperties("", blkString, scmString, 
        new HashMap<String,String>(), "ProjectName");
    for (Block block : Block.getAllBlocks()) {
      if (block.getBlockLabel().equals("noargs")) {
        checkBlockArgs(block, 0);
      } else if (block.getBlockLabel().equals("onearg")) {
        checkBlockArgs(block, 1);
        if (block.getGenusName().equals("caller")) {
          assertEquals("x", block.getSocketAt(0).getLabel());
        } else {
          assertTrue(ProcedureBlockManager.isProcDeclBlock(block));
          assertTrue(ProcedureBlockManager.isArgSocket(block.getSocketAt(0)));
        }
      } else if (block.getBlockLabel().equals("twoargs")) {
        checkBlockArgs(block, 2);
        if (block.getGenusName().equals("caller")) {
          assertEquals("y", block.getSocketAt(0).getLabel());
          assertEquals("z", block.getSocketAt(1).getLabel());
        } else {
          assertTrue(ProcedureBlockManager.isProcDeclBlock(block));
          assertTrue(ProcedureBlockManager.isArgSocket(block.getSocketAt(0)));
          assertTrue(ProcedureBlockManager.isArgSocket(block.getSocketAt(1)));
         }
      }
    }
    
  }
  
  private void checkBlockArgs(Block block, int num) {
    if (block.getGenusName().equals("define")) {
      // two filled arg sockets + 1 empty arg socket + command sockets
      assertEquals(num + 2, block.getNumSockets());
    } else if (block.getGenusName().equals("caller")) {
      assertEquals(num, block.getNumSockets());
    } else {
      fail("expected block genus to be one of define or caller but it is "
          + block.getGenusName());
    }

  }

}
