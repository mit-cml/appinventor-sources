// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.yacodeblocks;

import openblocks.codeblocks.ComplaintDepartment;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONObject;

import openblocks.renderable.FactoryRenderableBlock;
import openblocks.renderable.RenderableBlock;

import openblocks.workspace.FactoryManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Tests YAIL code generation in YaBlockCompiler for JSON properties, individual components,
 * and built-in blocks.
 * A project is defined by its .blk and .scm file.
 * These files are read into Codeblocks, YAIL is generated, and the YAIL output
 * is checked against an oracle for correctness.
 *
 *
 */
public class YaBlockCompilerTest extends TestCase {

  private class MockExternalController implements ExternalController {

    public void writeCodeblocksSourceToServer(String path, String contents) {
      throw new UnsupportedOperationException();
    }

    public void writeYailToServer(String path, String contents) {
      throw new UnsupportedOperationException();
    }

    public String getFormPropertiesForProject() {
      return properties;
    }

    public String downloadContentFromServer(String path) {
      throw new UnsupportedOperationException();
    }
  }

  ComponentBlockManager cbm;

  private final String sourceWithColor =
    "#|\n" +
    "$JSON\n" +
    "{\"Source\":\"Form\",\"Properties\":" +
    "{\"$Name\":\"Form1\",\"$Type\":\"Form\",\"Uuid\":\"0\",\"Title\":\"Form1\",\"" +
    "$Components\":" +
    "[{\"$Name\":\"coloredButton\",\"$Type\":\"Button\",\"Uuid\":\"-542297755\",\"BackgroundColor" +
    "\":\"&HFF0000FF\",\"Text\":\"Button\",\"TextColor\":\"&HFF00FF00\"}," +
    "{\"$Name\":\"noTextButton\",\"$Type\":\"Button\",\"Uuid\":\"-1879206148\"}]}}\n" +
    "|#\n";

  private final HashMap<String, ArrayList<RenderableBlock>> componentMap =
      new HashMap<String, ArrayList<RenderableBlock>>();

  private static final String TEST_PROJECT_PATH =
      "/src/com/google/youngandroid/project1/Form.scm";
  private static final String TEST_PROJECT_YAIL_PATH =
      "/src/com/google/youngandroid/project1/Form.yail";

  private String properties;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Initialize the workspace
    TestUtils.setupWorkspace(null, null);
    // our MockExternalController supplies FormProperties for testing.
    TestUtils.getController().setExternalController(new MockExternalController());
    // Grab the singleton ComponentBlockManager
    cbm = TestUtils.getComponentBlockManager();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testPackageNameGeneration() {
    assertEquals("com.gmail.username.project1.Form",
        YABlockCompiler.packageNameFromPath("/src/com/gmail/username/project1/Form.yail"));
    assertEquals("com.gmail.username.project1.Form",
        YABlockCompiler.packageNameFromPath("src/com/gmail/username/project1/Form.yail"));
    assertEquals("com.gmail.username.project1.Form",
        YABlockCompiler.packageNameFromPath("com/gmail/username/project1/Form.yail"));
    assertEquals("com.gmail.username.project1.Form",
        YABlockCompiler.packageNameFromPath(
            "gibbbbbberish/src/com/gmail/username/project1/Form.yail"));
    assertEquals("com.gmail.username.project1.Form",
        YABlockCompiler.packageNameFromPath(
            "gibbbbbberish/src/com/gmail/username/project1/Form.yailandotherextensionmagic"));
  }

  /**
   * Tests to make sure that color properties are successfully converted from
   * the JSON color format to a YAIL color code.
   */
  public void testPropertiesColorCodeConversion() throws CodeblocksException {
    componentMap.put("coloredButton", new ArrayList<RenderableBlock>());
    componentMap.put("noTextButton", new ArrayList<RenderableBlock>());

    JSONObject jsonProperties = WorkspaceUtils.parseFormProperties(sourceWithColor);
    assertTrue(cbm.syncFromJson(jsonProperties));

    String yail = YABlockCompiler.generateYailForProject(jsonProperties,
        componentMap, false);

    assertTrue(yail.contains("(set-and-coerce-property! 'coloredButton " +
        "'BackgroundColor #xFF0000FF 'number)"));
    assertTrue(yail.contains("(set-and-coerce-property! 'coloredButton " +
        "'TextColor #xFF00FF00 'number)"));
    assertFalse(yail.contains("&HFF00FF00"));
    assertFalse(yail.contains("&HFF0000FF"));
  }

  /**
   * Test for text conversion for components with and without text.
   */
  public void testPropertiesTextConversion() throws CodeblocksException {
    componentMap.put("coloredButton", new ArrayList<RenderableBlock>());
    componentMap.put("noTextButton", new ArrayList<RenderableBlock>());

    JSONObject jsonProperties = WorkspaceUtils.parseFormProperties(sourceWithColor);
    assertTrue(cbm.syncFromJson(jsonProperties));
    String yail = YABlockCompiler.generateYailForProject(jsonProperties,
        componentMap, false);
    assertTrue(yail.contains("(set-and-coerce-property! 'coloredButton 'Text \"Button\" 'text)\n"));
    assertFalse(yail.contains("(set-and-coerce-property! 'noTextButton 'Text )"));
  }

  /**
   * Test proc define/call blocks that return/use results
   */
  public void testProcedureBlocks() throws CodeblocksException {
   initAndTestProject("moreProcs2.blk", "moreProcs2.scm");
   String yail = TestUtils.getController().testGetYail(properties);
   // With results
   assertContainsIgnoreSpace(yail,
     "  (call-yail-primitive + " +
                             " (*list-for-runtime* (get-var counter) (lexical-value incr))" +
                             " '( number number)  \"+\") ");
   assertContainsIgnoreSpace(yail,
                             "(set-var! counter ((get-var counterPlus) 1))");

   // Without results
   assertContainsIgnoreSpace(yail,
     "(def (counterInc ) "          +
         "(set-var! counter ((get-var counterPlus) 1))"    +
         "((get-var counterInc)))");
  }

  // Complaint Testing
  // The following several methods Yailify small projects with errors.
  // To check the messages we have to search for the block that we know
  // had the error, using a label we know identifies it.

  /**
   * Test discovery of unattached block
   */
  public void testUnattached() throws CodeblocksException {
   initAndTestProject("unattached.blk", "unattached.scm");
   try {  // Must call non-test version because test version allows unattached blocks.
     TestUtils.getController()
        .getYailForProject(WorkspaceUtils.parseFormProperties(properties), true, false, false);
   } catch (YailGenerationException e) {
     // This might occur if any of the errors are severe.
   }
   for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
     System.out.println("unattached: " + rb.getBlock().getBlockLabel());
     if (rb.hasComplaint() && rb.getBlock().getBlockLabel().equals("unattached")) {
       assertContainedIn(ComplaintDepartment.UNATTACHED, rb.getComplaint().getText());
       return;
     }
   }
   Assert.fail("Unattached Block Missing");
  }

  /**
   * Test discovery of empty socket
   */
  public void testSocket() throws CodeblocksException {
   initAndTestProject("socket.blk", "socket.scm");
   try {
     TestUtils.getController().testGetYail(properties);
   } catch (YailGenerationException e) {
     // This might occur if any of the errors are severe.
   }
   for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
     if (rb.hasComplaint() && rb.getBlock().getBlockLabel().equals("socket")) {
       assertContainedIn(ComplaintDepartment.EMPTY_SOCKET, rb.getComplaint().getText());
       return;
     }
   }
   Assert.fail("Empty Socket Block Missing");
  }

  /**
   * Test discovery of duplicate handlers
   */
  public void testDuplicate() throws CodeblocksException {
   initAndTestProject("duplicate.blk", "duplicate.scm");
   try {
     TestUtils.getController().testGetYail(properties);
   } catch (YailGenerationException e) {
     // This might occur if any of the errors are severe.
   }
   boolean errorFound = false;
   for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
     if (rb.getBlock().getBlockLabel().contains("Initialize")
         && !(rb.getParentWidget() instanceof FactoryManager)) {
       errorFound |= rb.getComplaint().getText().contains(ComplaintDepartment.DUPLICATE_HANDLER);
       return;
     }
   }
   Assert.assertTrue("Duplicate not detected", errorFound);
  }

  /**
   * Test discovery unbound variable
   */
  public void testUnbound() throws CodeblocksException {
   initAndTestProject("unbound.blk", "unbound.scm");
   try {
     TestUtils.getController().testGetYail(properties);
   } catch (YailGenerationException e) {
     // This might occur if any of the errors are severe.
   }
   for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
     if (rb.hasComplaint() && rb.getBlock().getBlockLabel().equals("unbound") &&
         rb.getBlock().getDecoratorLabel().equals("value")) {
       assertContainedIn(ComplaintDepartment.UNBOUND_VARIABLE,
           rb.getComplaint().getText());
       return;
     }
   }
   Assert.fail("Unbound Variable Block Missing");
  }

  /**
   * Test missing for variable
   */
  public void testForVariable() throws CodeblocksException {
   initAndTestProject("forvar.blk", "forvar.scm");
   try {
     TestUtils.getController().testGetYail(properties);
   } catch (YailGenerationException e) {
     // This might occur if any of the errors are severe.
   }
   for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
     System.out.println(rb.getBlock().getBlockLabel());
     if (rb.hasComplaint() && rb.getBlock().getBlockLabel().equals("foreach")) {
       assertContainedIn(ComplaintDepartment.MISSING_VARIABLE,
           rb.getComplaint().getText());
       return;
     }
   }
   Assert.fail("For Block Missing");
  }

  /**
   * Test discovery improper initializer
   */

  public void testUnboundTorture() throws CodeblocksException {
    initAndTestProject("unboundTorture.blk", "unboundTorture.scm");
    try {
      TestUtils.getController().testGetYail(properties);
    } catch (YailGenerationException e) {
      // This is expected.
    }
    for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
      if (!(rb instanceof FactoryRenderableBlock)) {
        if (rb.getBlock().getBlockLabel().equals("name")) {
          assertContainsIgnoreSpace(rb.getComplaint().getText(),
              ComplaintDepartment.UNBOUND_VARIABLE);
        }
        if (rb.getBlock().getBlockLabel().equals("name1")) {
          assertFalse("Block is OK but has error:", rb.hasComplaint());
        }
      }
    }
  }

  public void testInitialize() throws CodeblocksException {
   initAndTestProject("initialize.blk", "initialize.scm");
   try {
     TestUtils.getController().testGetYail(properties);
   } catch (YailGenerationException e) {
     // This might occur if any of the errors are severe.
   }
   for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
     if (rb.hasComplaint() && rb.getBlock().getBlockLabel().equals("initialize")) {
       assertContainedIn(ComplaintDepartment.BAD_INITIALIZER,
           rb.getComplaint().getText());
       return;
     }
   }
   Assert.fail("Bad initializer missed");
  }


  /**
   * Test proc def/call for large example: tail-recursive addition
   */
  public void testBigProcDef() throws CodeblocksException {
   initAndTestProject("bigProcDef.blk", "bigProcDef.scm");
   String yail = TestUtils.getController().testGetYail(properties);

   // This code writes into the log showing the actual characters codes coming back
   // it's useful to help create the test initially, but not to run now.
   // We'll keep it here for reference
   // for (int i = 0; i < yail.length(); i++) {
   //   char ch = yail.charAt(i);
   //   System.out.println("yail[" + i + "] is " + ch + " 0x" + Integer.toHexString(ch));
   // }

   System.out.println("bigProcDef result: " + yail);
   assertContainsIgnoreSpace(yail,
    "(def (tailRedAdd a b )"     +
    "  (if"     +
    "   (call-yail-primitive yail-equal?" +
    "     (*list-for-runtime* (lexical-value a) 0) '(any any) \"=\" )" +
    "   (begin (lexical-value b))"     +
    "   (begin"     +
    "     ((get-var tailRedAdd)"  +
    "      (call-yail-primitive -" +
    "      (*list-for-runtime* (lexical-value a) 1)"    +
    "                    '( number number)"  +
    "                    \"\\u2212\")"     +
    "      (call-yail-primitive +"     +
    "                    (*list-for-runtime* (lexical-value b) 1)"     +
    "                    '( number number)"     +
    "                    \"+\")"     +
    "      ))))");
}


  /**
   * Test variable define block.
   */
  public void testVariableDefineBlock() throws CodeblocksException {
    initAndTestProject("varDefine.blk", "varDefine.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    assertContainsIgnoreSpace(yail, "(def explodingPants \"dangerous\")");
    String clickEvent = "(define-event button Click ()" +
        " (set-this-form)" +
        " (set-and-coerce-property! 'button 'Text (get-var explodingPants)  'text))";
    assertContainsIgnoreSpace(yail, clickEvent);
  }

  /**
   * Test the argument block.
   */
  public void testArgumentBlock() throws CodeblocksException {
    initAndTestProject("argument.blk", "argument.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(define-event AccelerometerSensor3 AccelerationChanged" +
        "( xAccel yAccel zAccel)" +
        " (set-this-form)";
    assertContainsIgnoreSpace(yail, testString);
    testString = "(set-and-coerce-property! 'xLabel 'Text (lexical-value xAccel)  'text)" +
        "(set-and-coerce-property! 'yLabel 'Text (lexical-value yAccel)  'text)" +
        "(set-and-coerce-property! 'zLabel 'Text (lexical-value zAccel)  'text)";
    assertContainsIgnoreSpace(yail, testString);
  }


   /**
    * Test the text blocks.
    */
   public void testTextBlocks() throws CodeblocksException {
     initAndTestProject("text.blk", "text.scm");
     String yail = TestUtils.getController().testGetYail(properties);
     String testString =
          "(if (and-delayed "           +
          "     (call-yail-primitive "           +
          "      yail-equal? "           +
          "      (*list-for-runtime* \"something\" \"everything\") "           +
          " '( any any) "           +
          " \"=\") "           +
          " (call-yail-primitive "           +
          "  yail-equal? "           +
          "  (*list-for-runtime* (call-yail-primitive string-length " +
                   " (*list-for-runtime* \"universe\") "           +
          " '( text) "           +
          " \"length\") "           +
          " 42) "           +
          " '( any any) "           +
          " \"=\") "           +
          ") "           +
          " (begin  (call-yail-primitive string-append (*list-for-runtime* \"me\" \"you\") " +
          " '( text text) "           +
          " \"join\")) "           +
         " (begin  \"nope\")) ";
     assertContainsIgnoreSpace(yail, testString);
   }

   /**
    * Test text and number labels that contain "." (that they aren't mistaken
    * for component-related blocks)
    * @throws CodeblocksException
    */
   public void testLabelsWithDots() throws CodeblocksException {
     initAndTestProject("dots.blk", "dots.scm");
     String yail = TestUtils.getController().testGetYail(properties);
     System.out.println("LabelsWithDots result: " + yail);
     String testText1 = "(set-and-coerce-property! 'Label1 'Text \"this.that\" 'text)";
     String testText2 = "(set-and-coerce-property! 'Label1 'Text \"Label1.Text\" 'text)";
     String testNumber = "(def numberWithDot 123.45)";
     assertContainsIgnoreSpace(yail, testText1);
     assertContainsIgnoreSpace(yail, testText2);
     assertContainsIgnoreSpace(yail, testNumber);
   }

  /**
   * Test the blocks related to list items.
   */
  public void testMakeListBlock() throws CodeblocksException {
    initAndTestProject("makelist.blk", "makelist.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(call-yail-primitive make-yail-list " +
        " (*list-for-runtime* 1 2 " +
        " (call-yail-primitive make-yail-list " +
        " (*list-for-runtime* \"hello\" \"earth\" ) " +
        "  '( any any) \"make a list\")) " +
        " '( any any any) " +
        "  \"make a list\")";
    assertContainsIgnoreSpace(yail, testString);
  }

  public void testIsListBlock() throws CodeblocksException {
    initAndTestProject("islist.blk", "islist.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(call-yail-primitive " +
        " yail-list? (*list-for-runtime* (get-var x) ) '( any) \"is a list?\")";
    assertContainsIgnoreSpace(yail, testString);
  }

  public void testIsEmptyListBlock() throws CodeblocksException {
    initAndTestProject("isemptylist.blk", "isemptylist.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(call-yail-primitive " +
        "yail-list-empty? (*list-for-runtime* (get-var x) ) '( list) \"is list empty?\")";
    assertContainsIgnoreSpace(yail, testString);
  }

  public void testListLengthBlock() throws CodeblocksException {
    initAndTestProject("listlength.blk", "listlength.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(call-yail-primitive yail-list-length (*list-for-runtime* (get-var x) )" +
        "'( list)" +
        "\"length of list\")";
    assertContainsIgnoreSpace(yail, testString);
  }

// first and rest were removed from list operations
//   public void testListFirstBlock() throws CodeblocksException {
//     initAndTestProject("listfirst.blk", "listfirst.scm");
//     String yail = TestUtils.getController().testGetYail(properties);
//     String testString = "(call-with-coerced-args yail-list-first (*list-for-runtime* x )" +
//         "'( list) " +
//         "\"first in list\")";
//     assertContainsIgnoreSpace(yail, testString);
//   }

//   public void testListRestBlock() throws CodeblocksException {
//     initAndTestProject("listrest.blk", "listrest.scm");
//     String yail = TestUtils.getController().testGetYail(properties);
//     String testString = "(call-with-coerced-args yail-list-rest (*list-for-runtime* x )" +
//         " '( list) " +
//         "\"rest of list\")";
//     assertContainsIgnoreSpace(yail, testString);
//   }

  public void testSetItemBlock() throws CodeblocksException {
    initAndTestProject("setitem.blk", "setitem.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    System.out.println("testSetItemBlock result: " + yail);
    String testString = "(call-yail-primitive yail-list-set-item! " +
        "(*list-for-runtime* (call-yail-primitive make-yail-list  " +
            " (*list-for-runtime* 100) '(any) \"make a list\") " +
        " 0 (get-var x) ) " +
        " '( list number any)" +
        "\"replace list item\")";
    assertContainsIgnoreSpace(yail, testString);
  }

  public void testRemoveItemBlock() throws CodeblocksException {
    initAndTestProject("removeitem.blk", "removeitem.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(call-yail-primitive yail-list-remove-item! " +
        "(*list-for-runtime* (get-var x)  2)" +
        " '( list number) " +
        " \"remove list item\") ";
    assertContainsIgnoreSpace(yail, testString);
  }

  public void testListAppendBlock() throws CodeblocksException {
    initAndTestProject("listappend.blk", "listappend.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(call-yail-primitive yail-list-append! " +
        " (*list-for-runtime* (get-var x)  (call-yail-primitive make-yail-list " +
        " (*list-for-runtime* 2 ) " +
        " '( any) " +
        " \"make a list\")) " +
        " '( list list) " +
        " \"append to list\") ";
    assertContainsIgnoreSpace(yail, testString);
  }

  public void testAddToListBlock() throws CodeblocksException {
    initAndTestProject("addtolist.blk", "addtolist.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(call-yail-primitive yail-list-add-to-list! " +
        "(*list-for-runtime* (get-var x)  2 26 ) " +
        " '( list any any) " +
        " \"add items to list\") ";
    assertContainsIgnoreSpace(yail, testString);
  }

  public void testIsInListBlock() throws CodeblocksException {
    initAndTestProject("isinlist.blk", "isinlist.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(call-yail-primitive yail-list-member? " +
         " (*list-for-runtime* 123 " +
         " (call-yail-primitive make-yail-list  " +
         "(*list-for-runtime* 1 5) '(any any) \"make a list\") " +
         " ) " +
         " '( any list) " +
        " \"is in list?\") ";
    assertContainsIgnoreSpace(yail, testString);
  }

  public void testListIndexBlock() throws CodeblocksException {
    initAndTestProject("listindex.blk", "listindex.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = " (call-yail-primitive yail-list-index " +
           "  (*list-for-runtime* 100 (get-var x) ) '( any list) " +
        " \"position in list\") ";
    assertContainsIgnoreSpace(yail, testString);
  }

  public void testListPickRandomBlock() throws CodeblocksException {
    initAndTestProject("listpickrandom.blk", "listpickrandom.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = "(call-yail-primitive yail-list-pick-random " +
        "(*list-for-runtime* (get-var v) ) " +
        " '( list) " +
        " \"pick random item\") ";
    assertContainsIgnoreSpace(yail, testString);
  }

  // This test does not work:  There is a failure on trying to initialize the project.
  // This presumably has to do with the component.  But why?
//   public void testSpriteAndListBlock() throws CodeblocksException {
//     initAndTestProject("spriteandlist.blk", "spriteandlist.scm");
//     String yail = TestUtils.getController().testGetYail(properties);
//     String testString = "(define-event testequalitybutton Initialize ()" +
//         "(set-var! spritelist (make-yail-list   ImageSprite1  ImageSprite2 )))";
//     assertContainsIgnoreSpace(yail, testString);
//   }


  /**
   * Test the simple math blocks. These include =, >, <, +, -, *, and /.
   * The order doesn't matter since they're all separate clumps.
   */
  public void testSimpleMathBlocks() throws CodeblocksException {
    initAndTestProject("simpleMath.blk", "simpleMath.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    assertContainsIgnoreSpace(yail,
         "(call-yail-primitive yail-equal? (*list-for-runtime* 123 \"123\") "           +
         " '( any any) "           +
        " \"=\") ");
    assertContainsIgnoreSpace(yail, "(call-yail-primitive > (*list-for-runtime* 45 \"0\")" +
        " '( number number)" +
        " \">\")");
    assertContainsIgnoreSpace(yail, "(call-yail-primitive < (*list-for-runtime* 123 456)" +
        " '( number number)" +
        " \"<\")");
    assertContainsIgnoreSpace(yail, "(call-yail-primitive + (*list-for-runtime* 35 5)" +
        " '( number number)" +
        " \"+\")");
    assertContainsIgnoreSpace(yail, "(call-yail-primitive - (*list-for-runtime* 50 \"70\")" +
        " '( number number)" +
        " \"\\u2212\")");
    assertContainsIgnoreSpace(yail, "(call-yail-primitive * (*list-for-runtime* \"60\" 4)" +
        " '( number number)" +
        " \"\\u00D7\")");
    assertContainsIgnoreSpace(yail, "(call-yail-primitive yail-divide (*list-for-runtime* 5 1)" +
        " '( number number)" +
        " \"\\/\")");
        // In the alternate version, have to specify the Java designation of the
        //divides character that should come from Blocks
        //" \" \\u00F7 \")");
  }

  /**
   * Tests min, max, remainder, quotient, modulo, negate, abs, round, floor,
   * ceiling, exp, expt, log, sin, cos, tan, asin, acos, atan and atan2 in
   * a program that will print the result of each of these operations to
   * its own label after clicking an "update" button.  Input for the functions
   * comes from text input boxes.
   */
  public void testOtherMathBlocks() throws CodeblocksException {
    initAndTestProject("otherMath.blk", "otherMath.scm");
    String actualYail = TestUtils.getController().testGetYail(properties);
    String expectedYail = ";;; updateButton\n" +
        "(add-component numberForm Button updateButton\n" +
        "(set-and-coerce-property! 'updateButton 'Text \"update\" 'text)\n)\n" +
        "(define-event updateButton Click ()\n" +
        "(set-this-form) " +
        "(set-and-coerce-property! 'minOutput 'Text (call-yail-primitive min (*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n" +
        "(get-property 'numberInputTwo 'Text)\n)\n" +
        "'( number number)\n" +
        "\"min\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'maxOutput 'Text (call-yail-primitive max (*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n" +
        "(get-property 'numberInputTwo 'Text)\n)\n" +
        "'( number number)\n" +
        "\"max\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'remainderOutput 'Text (call-yail-primitive remainder " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n" +
        "(get-property 'numberInputTwo 'Text)\n)\n" +
        "'( number number)\n" +
        "\"remainder\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'quotientOutput 'Text (call-yail-primitive quotient " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n" +
        "(get-property 'numberInputTwo 'Text)\n)\n" +
        "'( number number)\n" +
        "\"quotient\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'moduloOutput 'Text (call-yail-primitive modulo " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n" +
        "(get-property 'numberInputTwo 'Text)\n)\n" +
        "'( number number)\n" +
        "\"modulo\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'negateOutput 'Text (call-yail-primitive -  " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"negate\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'absOutput 'Text (call-yail-primitive abs (*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"abs\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'roundOutput 'Text (call-yail-primitive yail-round " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"round\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'floorOutput 'Text (call-yail-primitive yail-floor " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"floor\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'ceilingOutput 'Text (call-yail-primitive yail-ceiling " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"ceiling\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'expOutput 'Text (call-yail-primitive exp (*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"exp\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'exptOutput 'Text (call-yail-primitive expt " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n" +
        "(get-property 'numberInputTwo 'Text)\n)\n" +
        "'( number number)\n" +
        "\"expt\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'logOutput 'Text (call-yail-primitive log (*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"log\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'sinOutput 'Text (call-yail-primitive sin-degrees " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"sin\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'cosOutput 'Text (call-yail-primitive cos-degrees " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"cos\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'tanOutput 'Text (call-yail-primitive tan-degrees " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"tan\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'asinOutput 'Text (call-yail-primitive asin-degrees " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"asin\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'acosOutput 'Text (call-yail-primitive acos-degrees " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"acos\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'atanOutput 'Text (call-yail-primitive atan-degrees " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n)\n" +
        "'( number)\n" +
        "\"atan\")\n" +
        "'text)\n" +
        "(set-and-coerce-property! 'atan2Output 'Text (call-yail-primitive atan2-degrees " +
        "(*list-for-runtime* " +
        "(get-property 'numberInput 'Text)\n" +
        "(get-property 'numberInputTwo 'Text)\n)\n" +
        "'( number number)\n" +
        "\"atan2\")\n" +
        "'text)\n" +
        ")\n";
    assertContainsIgnoreSpace(actualYail, expectedYail);
  }

  /**
   * Tests for random and sqrt blocks. Order doesn't matter.
   */
  public void testRandomBlocks() throws CodeblocksException {
    initAndTestProject("randomMath.blk", "randomMath.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    assertContainsIgnoreSpace(yail, "(call-yail-primitive sqrt (*list-for-runtime* 123) " +
        "'( number) \"sqrt\")");
    assertContainsIgnoreSpace(yail, "(call-yail-primitive random-fraction (*list-for-runtime*) " +
        "'() \"random fraction\")");
    assertContainsIgnoreSpace(yail, "(call-yail-primitive random-integer " +
                              "(*list-for-runtime* 1 100) " +
        "'( number number)\"random integer\")");
    assertContainsIgnoreSpace(yail, "(call-yail-primitive random-set-seed " +
                                    "(*list-for-runtime* 50) " +
                                    "'( number) \"random set seed\")");
  }

  /**
   * Tests for logic blocks.
   */
  public void testLogicBlocks() throws CodeblocksException {
    initAndTestProject("logic.blk", "logic.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString =
        "     (if (or-delayed                                                              " +
        "          #t                                                                      " +
        "          (call-yail-primitive yail-not (*list-for-runtime* #f) '( boolean) \"not\") ) " +
        "         (begin (if (and-delayed                                                  " +
        "               (call-yail-primitive yail-equal?                                   " +
        "                                    (*list-for-runtime* #t #t)                    " +
        "                                    '( any any)                                   " +
        "                                    \"=\") )                                      " +
        "              (begin ) ) )                                                        " +
        "         (begin \"didn't work\" ) )                                               " ;
    assertContainsIgnoreSpace(yail, testString);
  }


  /**
   * Test the choose block - added since the logic tests above
   */
  public void testChooseBlock() throws CodeblocksException {
    initAndTestProject("choose.blk", "choose.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    assertContainsIgnoreSpace(yail,
      " (set-and-coerce-property! 'Label1 'Text " +
      "(if  (get-property 'CheckBox1 'Checked)  (begin  " +
      "(set-and-coerce-property! 'Label1 'BackgroundColor -16711936 'number)" +
      "(get-property 'CheckBox1 'Text))" +
      "(begin (set-and-coerce-property! 'Label1 'BackgroundColor " +
      " -256 'number) (get-property 'CheckBox2 'Text))");
  }

  public void testForeachBlock() throws CodeblocksException {
    initAndTestProject("foreach.blk", "foreach.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString = " (foreach  elt (begin  (call-yail-primitive yail-list-set-item! " +
        " (*list-for-runtime* (get-var x)  0 (lexical-value elt) ) '( list number any) " +
        " \"replace list item\")) " +
        " (call-yail-primitive make-yail-list " +
        "(*list-for-runtime* 1 2 3) '(any any any) \"make a list\") ";
    assertContainsIgnoreSpace(yail, testString);
  }

 public void testForRangeBlock() throws CodeblocksException {
    initAndTestProject("forrange.blk", "forrange.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString =
        "(forrange  i  (begin (set-var! v  (lexical-value i) )  )   2  10  6)";
    assertContainsIgnoreSpace(yail, testString);
  }

  // TODO(sharon): implement this
  public void testColors() {
    assertTrue(true);
  }

  public void testComponentEventHandlerBlocks() throws CodeblocksException {
    initAndTestProject("componentHandler.blk", "componentHandler.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String testString1 = "(add-component Screen1 Label Label1" +
        "(set-and-coerce-property! 'Label1 'Text \"Text for Label1\" 'text))";
    String testString2 = "(define-event Screen1 Initialize ()" +
        " (set-this-form) " +
        "(set-and-coerce-property! 'Label1 'Text \"foo\" 'text))";
    assertContainsIgnoreSpace(yail, testString1);
    assertContainsIgnoreSpace(yail, testString2);
  }

  public void testDeactivatedBlocks() throws CodeblocksException {
    initAndTestProject("deactivate.blk", "deactivate.scm");
    String yail = TestUtils.getController().testGetYail(properties);

    String testString1 = "(add-component Screen1 Label Label1" +
    "(set-and-coerce-property! 'Label1 'Text \"Text for Label1\" 'text))";

    String testString2 = "(add-component Screen1 Button Button1" +
    "(set-and-coerce-property! 'Button1 'Text \"Text for Button1\" 'text))";

    String testString3 = "(define-event Button1 Click ()" +
        " (set-this-form)" +
        " (get-var *the-null-value*) " +
        " (set-and-coerce-property! 'Label1 'Text \"a\" 'text) (get-var *the-null-value*) " +
        "(set-var! variable1 \"b\") (get-var *the-null-value*))";

    String testString4 = "(add-component Screen1 TextBox TextBox1" +
    "(set-and-coerce-property! 'TextBox1 'Text \"Text for TextBox1\" 'text))";

    String testString5 = "(add-component Screen1 AccelerometerSensor AccelerometerSensor1)" +
        "(define-event AccelerometerSensor1 AccelerationChanged ( xAccel yAccel zAccel)" +
        " (set-this-form) " +
        "(get-var *the-null-value*))";

    assertContainsIgnoreSpace(yail, testString1);
    assertContainsIgnoreSpace(yail, testString2);
    assertContainsIgnoreSpace(yail, testString3);
    assertContainsIgnoreSpace(yail, testString4);
    assertContainsIgnoreSpace(yail, testString5);
  }

  // TODO(user): implement this
  public void testProjectName()  throws Exception {
    assertTrue(true);
  }

  // TODO(user): implement this
  public void testProjectStartsNonNull() throws Exception {
    assertTrue(true);
  }

  // TODO(user): implement this
  public void testYailGenerationEmpty() throws Exception {
    assertTrue(true);
  }

  // TODO(user): implement this
  public void testYailGenerationSmall() throws Exception {
    assertTrue(true);
  }

  // TODO(user): implement this
  public void testYailGenerationMedium() throws Exception {
    assertTrue(true);
  }

  // TODO(user): implement this
  public void testYailGenerationLarge() throws Exception {
    assertTrue(true);
  }

  // TODO(sharon): Ball components used to have a bug with Ball.CollidingWith 
  // being a command with a plug but it appears to be fixed.
  // This test should probably be enabled now.
  public void disable_testComponentWithParent() throws CodeblocksException {
    initAndTestProject("level.blk", "level.scm");
    String yail = TestUtils.getController().testGetYail(properties);
    String ballParentIsCanvas = "(add-component Canvas1 Ball Ball1";
    String canvasParentIsLevel = "(add-component Level Canvas Canvas1";
    assertContainsIgnoreSpace(yail, ballParentIsCanvas);
    assertContainsIgnoreSpace(yail, canvasParentIsLevel);
  }

  public void testGeneratePropertySetterYail() throws Exception {
    StringBuilder yailCode = new StringBuilder();
    YABlockCompiler.generatePropertySetterYail(yailCode, "Button1", "Button", "Text", "idontcare");
    assertEquals("(set-and-coerce-property! 'Button1 'Text \"idontcare\" 'text)\n",
                 yailCode.toString());

  }

  // Initialize a project with the given .blk and .scm file
  private void initAndTestProject(String blkFilePath, String scmFilePath)
      throws CodeblocksException {
    String blkString = TestUtils.getFileAsString(TestUtils.TESTING_SOURCE_PATH + blkFilePath);
    String scmString = TestUtils.getFileAsString(TestUtils.TESTING_SOURCE_PATH + scmFilePath);
    System.out.println("Trying to load " + blkFilePath);
    TestUtils.getController().loadSourceAndProperties("", blkString, scmString,
        new HashMap<String, String>(), "ProjectName");
    System.out.println("Loaded" + blkFilePath);
    properties = scmString;
  }

  private void assertContainsIgnoreSpace(String text, String term) {
    // Strip blanks and line breaks, then check for a match.
    // TODO(user): Check for the correct amount of whitespace between tokens
    String textStripped = text.toLowerCase().replace(" ", "").replace("\n", "");
    String termStripped = term.toLowerCase().replace(" ", "").replace("\n", "");
    if (!textStripped.contains(termStripped)) {
      Assert.fail("Expected <" + text + "> to contain <" + term  + ">");
    }
  }

  private void assertContainedIn(String term, String text) {
    if (!(text.contains(term))) {
      Assert.fail("Expected <" + term + "> to occur in <" + text  + ">");
    }
  }
}
