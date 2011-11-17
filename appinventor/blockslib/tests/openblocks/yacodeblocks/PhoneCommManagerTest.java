// Copyright 2010 Google Inc. All Rights Reserved.
package openblocks.yacodeblocks;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

import openblocks.renderable.Complaint;
import openblocks.renderable.FactoryRenderableBlock;
import openblocks.renderable.RenderableBlock;
import openblocks.renderable.Report;
import openblocks.yacodeblocks.PhoneCommManager;



/**
 * Tests routine that parses strings sent from the phone to the blocks editor.
 *
 */
public class PhoneCommManagerTest extends TestCase {

  ArrayList<String> errors;

  class ErrorRecorder implements Appendable {
    public Appendable append(CharSequence errorMsg) {
      errors.add(errorMsg.toString());
      return this;
    }
    public Appendable append(CharSequence errorMsg, int x, int y) {
      return this;
    }
    public Appendable append(char c) {
      return this;
    }
  }

  static final String DISPLAY_IT = BlockParser.REPL_DISPLAY_IT;
  static final String OPEN = PhoneCommManager.REPL_OPEN_BRACKET;
  static final String BLOCK_ID = PhoneCommManager.REPL_BLOCK_ID_INDICATOR;
  static final String TAG  = PhoneCommManager.REPL_RETURN_TAG_ENDER;
  static final String RESULT = PhoneCommManager.REPL_RESULT_INDICATOR;
  static final String CLOSE = PhoneCommManager.REPL_CLOSE_BRACKET;
  static final String SUCCESS = PhoneCommManager.REPL_SUCCESS ;
  static final String FAILURE = PhoneCommManager.REPL_FAILURE;
  static final String ENCODED_OPEN = PhoneCommManager.REPL_ENCODED_OPEN_BRACKET;
  static final String ENCODED_CLOSE = PhoneCommManager.REPL_ENCODED_CLOSE_BRACKET;
  static final String ESCAPE = PhoneCommManager.REPL_ESCAPE;
  static final String ENCODED_ESCAPE = PhoneCommManager.REPL_ENCODED_ESCAPE;


  PhoneCommManager rcm = new PhoneCommManager();
  ErrorRecorder err = new ErrorRecorder();

  private String receiver = null;
  private Report report = null;
  private Complaint complaint = null;
  private RenderableBlock messageReceiver = null;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Initialize the workspace
    TestUtils.setupWorkspace(null, null);
    TestUtils.getController().setPhoneCommManager(rcm);

    // Find any old project and block to attach Reports and Complaints to
    initAndTestProject("moreProcs2.blk", "moreProcs2.scm");
    for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
      if (!(rb instanceof FactoryRenderableBlock)) {
        messageReceiver = rb;
        receiver = rb.getBlockID().toString();
        rb.getBlock().setShouldReceiveReport(true);
        report = rb.getReport();
        complaint = rb.getComplaint();
        break;
      }
    }
    errors = new ArrayList<String>();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    report.setText("");
    complaint.setText("");
  }

  public void testParseEmptyResponse() {
    Assert.assertEquals(0, rcm.postProcessREPLResponse("", err));
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  public void testParseLineBreakResponse() {
    Assert.assertEquals(0, rcm.postProcessREPLResponse("\n", err));
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  public void testParsePromptResponse() {
    Assert.assertEquals(0, rcm.postProcessREPLResponse("#|kawa:10|#", err));
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  public void testParseBreakPromptResponse() {
    Assert.assertEquals(0, rcm.postProcessREPLResponse("\n#|kawa:2|#", err));
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  public void testParseDisplayItResponse() {
    String sinput = OPEN + DISPLAY_IT + BLOCK_ID + receiver + TAG + SUCCESS +
         RESULT + 88 + CLOSE;
    Assert.assertEquals(1, rcm.postProcessREPLResponse(sinput, err));
    waitForResponse();
    Assert.assertEquals("88", report.getText());
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  private void waitForResponse() {
    synchronized (messageReceiver) {
      while (messageReceiver.getPendingReplMessages() > 0) {
        try {
          messageReceiver.wait();
        } catch (InterruptedException e) {
          fail("Interrupted");
        }
      }
    }
  }

  public void testParseTwoItemsResponse() {
    String sinput = OPEN + DISPLAY_IT + BLOCK_ID + receiver + TAG + SUCCESS + RESULT + 88 + CLOSE;
    Assert.assertEquals(2, rcm.postProcessREPLResponse(sinput + sinput, err));
    waitForResponse();
    Assert.assertEquals("88", report.getText());
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  public void testParseFailureResponse() {
    String finput = OPEN + "x c 3 4" + BLOCK_ID + receiver + TAG + FAILURE + RESULT + 28 + CLOSE;
    Assert.assertEquals(1, rcm.postProcessREPLResponse(finput, err)) ;
    waitForResponse();
    Assert.assertEquals("Error: 28\n", complaint.getText());
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  public void testParseBadIDResponse() {
    String sinput = OPEN + DISPLAY_IT + BLOCK_ID + "7A" + TAG + SUCCESS + RESULT + 28 + CLOSE;
    Assert.assertEquals(1, rcm.postProcessREPLResponse(sinput, err));
    // Result was dropped because blockID was not integer
    waitForResponse();
    Assert.assertEquals("", report.getText());
    Assert.assertTrue("Unexpected errors: " + errors, errors.size() == 1);
    Assert.assertEquals("Garbled response: " + DISPLAY_IT + BLOCK_ID + "7A" + TAG +
        SUCCESS + RESULT + 28, errors.get(0));
  }

  public void testParseBadPurposeResponse() {
    String finput = OPEN + "x c ,, 4" + BLOCK_ID + "7" + TAG + FAILURE + RESULT + 28 + CLOSE;
    Assert.assertEquals(1, rcm.postProcessREPLResponse(finput, err));
    Assert.assertTrue("Unexpected errors: " + errors, errors.size() == 1);
    Assert.assertEquals("Garbled response: x c ,, 4" + BLOCK_ID + "7" + TAG +
        FAILURE + RESULT + 28, errors.get(0));
    waitForResponse();
    Assert.assertEquals("", complaint.getText());
  }

  // This test is removed until we decide how to deal with linebreaks in report balloons
//   public void testParseBreaksInValueResponse() {
//     String sinput = OPEN + DISPLAY_IT + BLOCK_ID + receiver + TAG + SUCCESS + RESULT +
//     "x\ny\n\n" + CLOSE;
//     Assert.assertEquals(1, rcm.postProcessREPLResponse(sinput, err));
//     waitForResponse();
//     Assert.assertEquals("x\\ny\\n\\n", report.getText());
//     Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
//   }

  public void testParseCloseBracketInValueResponse() {
    String sinput = OPEN + DISPLAY_IT + BLOCK_ID + receiver + TAG + SUCCESS +
    RESULT + ENCODED_CLOSE + CLOSE;
    Assert.assertEquals(1, rcm.postProcessREPLResponse(sinput, err));
    waitForResponse();
    Assert.assertEquals(CLOSE, report.getText());
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  public void testParseEscapeInValueResponse() {
    String sinput = OPEN + DISPLAY_IT + BLOCK_ID + receiver + TAG + SUCCESS +
             RESULT + ENCODED_ESCAPE + CLOSE;
    Assert.assertEquals(1, rcm.postProcessREPLResponse(sinput, err));
    waitForResponse();
    Assert.assertEquals(ESCAPE, report.getText());
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  public void testParseOpenAndCloseInValueResponse() {
    String sinput = OPEN + DISPLAY_IT + BLOCK_ID + receiver + TAG + SUCCESS + RESULT +
    "\"" + ENCODED_OPEN + ENCODED_CLOSE + "\"" +  CLOSE;
    Assert.assertEquals(1, rcm.postProcessREPLResponse(sinput, err));
    waitForResponse();
    Assert.assertEquals("\"" + OPEN + CLOSE + "\"", report.getText());
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }


  // This test is removed until we decide how to deal with linebreaks and
  //formatting in report balloons
//   public void testParseTwoLineResponse() {
//     String sinput = OPEN + DISPLAY_IT + BLOCK_ID + receiver + TAG + SUCCESS + RESULT +
//          "\"" + ENCODED_OPEN + ENCODED_CLOSE + "\"" +  CLOSE;
//     String input2 = "Garbage" + sinput + OPEN + "Garbage" + CLOSE + sinput + "Garbage" + OPEN;
//     Assert.assertEquals(3, rcm.postProcessREPLResponse(input2, err));
//     waitForResponse();
//     Assert.assertEquals("\\\"" + OPEN + CLOSE + "\\\"", report.getText());
//     Assert.assertTrue("Unexpected errors: " + errors, errors.size() == 2);
//     Assert.assertEquals("Ignored \"Garbage\"\n", errors.get(0));
//     Assert.assertEquals("Garbled response: Garbage", errors.get(1));
//     errors.remove(0);
//     errors.remove(0);
    // It's still holding input "Garbage" + OPEN
//     String input3 = DISPLAY_IT + BLOCK_ID + receiver + TAG + SUCCESS + RESULT + 99 + CLOSE;
//     Assert.assertEquals(1, rcm.postProcessREPLResponse(input3, err));
//     waitForResponse();
//     Assert.assertEquals("99", report.getText());
//     Assert.assertTrue("Unexpected errors: " + errors, errors.size() == 1);
//     Assert.assertEquals("Ignored \"Garbage\"\n", errors.get(0));
//   }

  public void testParseTenLineResponse() {
    String input3 = OPEN + DISPLAY_IT + BLOCK_ID + receiver + TAG + SUCCESS + RESULT;
    // Empty leftOver
    // Send response beginning
    Assert.assertEquals(0, rcm.postProcessREPLResponse(input3, err));
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
    // Dribble in a value
    for (int i = 0; i < 10; i++) {
      Assert.assertEquals(0, rcm.postProcessREPLResponse("X", err));
      Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
    }
    // End the value
    Assert.assertEquals(1, rcm.postProcessREPLResponse(CLOSE, err));
    waitForResponse();
    Assert.assertEquals("XXXXXXXXXX", report.getText());
    Assert.assertTrue("Unexpected errors: " + errors, errors.isEmpty());
  }

  // Initialize a project with the given .blk and .scm file
  private void initAndTestProject(String blkFilePath, String scmFilePath)
        throws CodeblocksException {
    String blkString = TestUtils.getFileAsString(TestUtils.TESTING_SOURCE_PATH + blkFilePath);
    String scmString = TestUtils.getFileAsString(TestUtils.TESTING_SOURCE_PATH + scmFilePath);
    System.out.println("Trying to load " + blkFilePath);
    TestUtils.getController().loadSourceAndProperties("", blkString, scmString,
        new HashMap<String,String>(), "ProjectName");
  }

}
