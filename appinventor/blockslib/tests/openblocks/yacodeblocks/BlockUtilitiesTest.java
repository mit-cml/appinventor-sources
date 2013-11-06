// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import static org.junit.Assert.assertFalse;
import static org.easymock.EasyMock.*;

import openblocks.renderable.BlockUtilities;
import openblocks.renderable.RenderableBlock;

import junit.framework.TestCase;
import java.util.HashMap;
import org.easymock.EasyMock;

/**
 * Tests for BlockUtilities.deleteBlock(). There were various bug reports
 * related to deleting blocks with and without the phone connected. This test
 * reproduces the known buggy cases.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class BlockUtilitiesTest extends TestCase {

  private PhoneCommManager mockPhoneCommManager;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Initialize the workspace
    mockPhoneCommManager = createMock(PhoneCommManager.class);
    TestUtils.setupWorkspace(null, null);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    EasyMock.verify(mockPhoneCommManager);
  }

  public void testDeleteVarDeclNoPhone() throws Exception {
    // def vartest1 as <- "text"

    // Load blocks source that contains the blocks to be deleted
    initAndTestProject("deleteTest.blk", "deleteTest.scm", false);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 367), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
    assertFalse("FeedbackReporter showed an error message",
        FeedbackReporter.testingShowedErrorMessage);
  }

  public void testDeleteEventNoPhone() throws Exception {
    // when Canvas1.Touched(<- name x, <- name y, <- name touchedSprite)
    //  do <- set Label1.BackgroundColor to <- color Blue

    initAndTestProject("deleteTest.blk", "deleteTest.scm", false);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 441), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
    assertFalse("FeedbackReporter showed an error message",
        FeedbackReporter.testingShowedErrorMessage);
  }

  public void testDeleteProcNoPhone() throws Exception {
    // to procedure
    //   do <- set Lable1.Text to <- "a"
    initAndTestProject("deleteTest.blk", "deleteTest.scm", false);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 513), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
    assertFalse("FeedbackReporter showed an error message",
        FeedbackReporter.testingShowedErrorMessage);
  }

  public void testDeleteProcWithArgNoPhone() throws Exception {
    // to procedure1 <- name name
    //   do <- call remove list item
    initAndTestProject("deleteTest.blk", "deleteTest.scm", false);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 523), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
  }

  public void testDeleteProcWithMultipleArgsNoPhone() throws Exception {
    // to procedure1 <- name name1, <- name name2, <- name name3
    //   do <- call TinyDB1.StoreValue
    initAndTestProject("deleteTest.blk", "deleteTest.scm", false);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 510), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
  }

  public void testDeleteProcWithResultNoPhone() throws Exception {
    // to procedureWithResult
    //   return <- true
    initAndTestProject("deleteTest.blk", "deleteTest.scm", false);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 501), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
    assertFalse("FeedbackReporter showed an error message",
        FeedbackReporter.testingShowedErrorMessage);
  }

  public void testDeleteVarDeclWithPhone() throws Exception {
    // def vartest1 as <- "text"
    // Bug report: b/issue?id=3322709

    // Load blocks source that contains the blocks to be deleted
    initAndTestProject("deleteTest.blk", "deleteTest.scm", true);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 367), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
    assertFalse("FeedbackReporter showed an error message",
        FeedbackReporter.testingShowedErrorMessage);
  }

  public void testDeleteEventWithPhone() throws Exception {
    // when Canvas1.Touched(<- name x, <- name y, <- name touchedSprite)
    //  do <- set Label1.BackgroundColor to <- color Blue
    // Bug report: b/issue?id=3322709

    initAndTestProject("deleteTest.blk", "deleteTest.scm", true);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 441), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
    assertFalse("FeedbackReporter showed an error message",
        FeedbackReporter.testingShowedErrorMessage);
  }

  public void testDeleteProcWithPhone() throws Exception {
    // to procedure
    //   do <- set Lable1.Text to <- "a"
    initAndTestProject("deleteTest.blk", "deleteTest.scm", true);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 513), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
    assertFalse("FeedbackReporter showed an error message",
        FeedbackReporter.testingShowedErrorMessage);
  }

  public void testDeleteProcWithArgWithPhone() throws Exception {
    // to procedure1 <- name name
    //   do <- call remove list item
    // Bug report: b/issue?id=3322709  (not exact reported scenario, but same effect)

    initAndTestProject("deleteTest.blk", "deleteTest.scm", true);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 523), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
  }

  public void testDeleteProcWithMultipleArgsWithPhone() throws Exception {
    // to procedure1 <- name name1, <- name name2, <- name name3
    //   do <- call TinyDB1.StoreValue
    // Bug report: b/issue?3413353

    initAndTestProject("deleteTest.blk", "deleteTest.scm", true);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 510), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
  }

  public void testDeleteProcWithResultWithPhone() throws Exception {
    // to procedureWithResult
    //   return <- true
    // Bug report: b/issue?id=3413296

    initAndTestProject("deleteTest.blk", "deleteTest.scm", true);
    try {
      BlockUtilities.deleteBlock(RenderableBlock.getRenderableBlock((long) 501), false);
    } catch (Exception e) {
      fail("Got exception " + e.getMessage());
    }
    assertFalse("FeedbackReporter showed an error message",
        FeedbackReporter.testingShowedErrorMessage);
  }

  // Initialize a project with the given .blk and .scm file
  private void initAndTestProject(String blkFilePath, String scmFilePath, boolean connectToPhone)
        throws CodeblocksException {
    if (connectToPhone) {
      TestUtils.getController().setPhoneCommManager(mockPhoneCommManager);
      mockPhoneCommManager.replControllerCreateAndSendAsync(EasyMock.<String>anyObject(),
          EasyMock.<String>anyObject(), EasyMock.anyLong(), EasyMock.anyBoolean());
      EasyMock.expectLastCall().atLeastOnce();
      EasyMock.expect(mockPhoneCommManager.connectedToPhone()).andReturn(true).atLeastOnce();
      mockPhoneCommManager.prepareForNewProject(EasyMock.anyBoolean(), EasyMock.eq(false));
      mockPhoneCommManager.updateStatusIndicators();
      EasyMock.expectLastCall().anyTimes();
    } else {
      TestUtils.getController().setPhoneCommManager(new PhoneCommManager());
    }
    EasyMock.replay(mockPhoneCommManager);

    FeedbackReporter.testingShowedErrorMessage = false;
    String blkString = TestUtils.getFileAsString(TestUtils.TESTING_SOURCE_PATH + blkFilePath);
    String scmString = TestUtils.getFileAsString(TestUtils.TESTING_SOURCE_PATH + scmFilePath);
    System.out.println("Trying to load " + blkFilePath);
    TestUtils.getController().loadSourceAndProperties("", blkString, scmString,
        new HashMap<String,String>(), "ProjectName");
  }
}
