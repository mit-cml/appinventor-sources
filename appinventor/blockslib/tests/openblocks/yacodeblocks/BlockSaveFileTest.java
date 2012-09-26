// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import com.google.common.testing.junit4.JUnitAsserts;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Small tests for BlockSaveFile
 *
 * @author sharon@google.com (Sharon Perl)
 * @author lizlooney@google.com (Liz Looney)
 */
public class BlockSaveFileTest extends TestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    // This is necessary because the BlockSaveFile constructor transitively
    // calls BlockGenus.getGenusWithName(), which only gets initialized
    // when the language definition file gets read in, which occurs during
    // workspace setup.  For more detail, see the review log for CL 18903627.
    TestUtils.setupWorkspace(null, null);
  }

  private BlockSaveFile createBlockSaveFile(String filename) throws IOException {
    String fullFilename = TestUtils.TESTING_SOURCE_PATH + filename;
    return new BlockSaveFile(TestUtils.getLangDefRoot(),
        FileUtils.readFileToString(new File(fullFilename), "UTF-8"));
  }

  public void testNoVersionInfo() throws Exception {
    BlockSaveFile saveFile = createBlockSaveFile("blocksNoVersion.blk");
    assertTrue(saveFile.wasUpgraded());
  }

  public void testUpgradeFromBlocksVersion1() throws Exception {
    BlockSaveFile saveFile = createBlockSaveFile("blocksVersion1.blk");
    assertTrue(saveFile.wasUpgraded());
  }

  public void testUpgradeTrigBlocksFromBlockVersion4() throws Exception {
    // Read in version 4 block file.
    BlockSaveFile saveFile = createBlockSaveFile("blocksVersion4.blk");

    // Check that the need to upgrade was recognized.
    assertTrue(saveFile.wasUpgraded());

    // Check that the appropriate changes were made to trig blocks.
    Element documentRoot = saveFile.getRoot();
    NodeList blocks = documentRoot.getElementsByTagName("Block");
    for (int i = 0; i < blocks.getLength(); i++) {
      Element block = (Element) blocks.item(i);
      String genusName = block.getAttribute("genus-name");
      boolean matched = false;

      // Check that the argument label was changed (from "") to "degrees".
      if (genusName.equals("number-sin") ||
          genusName.equals("number-cos") ||
          genusName.equals("number-tan")) {
        matched = true;
        JUnitAsserts.assertContentsInOrder(getSocketLabels(block), "degrees");
      }

      // Check that a CompilerErrorMsg was added.
      if (matched ||
          genusName.equals("number-asin") ||
          genusName.equals("number-acos") ||
          genusName.equals("number-atan") ||
          genusName.equals("number-atan2")) {
        assertEquals(1, block.getElementsByTagName("CompilerErrorMsg").getLength());
      } else {
        // Check that an error was NOT added to other nodes.
        assertEquals(0, block.getElementsByTagName("CompilerErrorMsg").getLength());
      }
    }
  }

  public void testUpgradeTextBlocksFromBlockVersion7() throws Exception {
    // Read in version 7 block file.
    BlockSaveFile saveFile = createBlockSaveFile("blocksVersion7.blk");

    // Check that the need to upgrade was recognized.
    assertTrue(saveFile.wasUpgraded());

    List<String> checkedBlocks = new ArrayList<String>();

    // Check that the appropriate changes were made to text blocks.
    Element documentRoot = saveFile.getRoot();
    NodeList blocks = documentRoot.getElementsByTagName("Block");
    for (int i = 0; i < blocks.getLength(); i++) {
      Element block = (Element) blocks.item(i);
      String genusName = block.getAttribute("genus-name");

      if (genusName.equals("string-less-than") ||
          genusName.equals("string-equal") ||
          genusName.equals("string-greater-than")) {
        // Check that the argument labels were changed (from both "") to "text1" and "text2".
        JUnitAsserts.assertContentsInOrder(getSocketLabels(block), "text1", "text2");
        checkedBlocks.add(genusName);
      }

      if (genusName.equals("string-upcase") ||
          genusName.equals("string-downcase") ||
          genusName.equals("string-trim")) {
        // Check that the argument label was changed (from "") to "text".
        JUnitAsserts.assertContentsInOrder(getSocketLabels(block), "text");
        checkedBlocks.add(genusName);
      }

      if (genusName.equals("string-replace-all")) {
        // Check that the second argument label was changed (from "substring") to "segment".
        JUnitAsserts.assertContentsInOrder(getSocketLabels(block), "text", "segment", "replacement");
        checkedBlocks.add(genusName);
      }
    }

    assertTrue(checkedBlocks.contains("string-less-than"));
    assertTrue(checkedBlocks.contains("string-equal"));
    assertTrue(checkedBlocks.contains("string-greater-than"));
    assertTrue(checkedBlocks.contains("string-upcase"));
    assertTrue(checkedBlocks.contains("string-downcase"));
    assertTrue(checkedBlocks.contains("string-trim"));
    assertTrue(checkedBlocks.contains("string-replace-all"));
  }

  public void testUpgradeRadiansBlocksFromBlockVersion8() throws Exception {
    // Read in version 8 block file.
    BlockSaveFile saveFile = createBlockSaveFile("blocksVersion8.blk");

    // Check that the need to upgrade was recognized.
    assertTrue(saveFile.wasUpgraded());

    List<String> checkedBlocks = new ArrayList<String>();

    // Check that the appropriate changes were made to degrees/radians blocks.
    Element documentRoot = saveFile.getRoot();
    NodeList blocks = documentRoot.getElementsByTagName("Block");
    for (int i = 0; i < blocks.getLength(); i++) {
      Element block = (Element) blocks.item(i);
      String genusName = block.getAttribute("genus-name");

      if (genusName.equals("number-degrees-to-radians")) {
        // Check that the argument label was changed (from "tangent") to "degrees".
        JUnitAsserts.assertContentsInOrder(getSocketLabels(block), "degrees");
        checkedBlocks.add(genusName);
      }

      if (genusName.equals("number-radians-to-degrees")) {
        // Check that the argument label was changed (from "tangent") to "radians".
        JUnitAsserts.assertContentsInOrder(getSocketLabels(block), "radians");
        checkedBlocks.add(genusName);
      }
    }

    assertTrue(checkedBlocks.contains("number-degrees-to-radians"));
    assertTrue(checkedBlocks.contains("number-radians-to-degrees"));
  }

  public void testUpgradeMinusAndTimesBlocksFromBlockVersion10() throws Exception {
    // Read in version 10 block file with minus and times blocks.
    BlockSaveFile saveFile = createBlockSaveFile("blocksVersion10.blk");

    // Check that the need to upgrade was recognized.
    assertTrue(saveFile.wasUpgraded());

    List<String> checkedBlocks = new ArrayList<String>();

    // Check that the appropriate changes were made to minus and times blocks.
    Element documentRoot = saveFile.getRoot();
    NodeList blocks = documentRoot.getElementsByTagName("Block");
    for (int i = 0; i < blocks.getLength(); i++) {
      Element block = (Element) blocks.item(i);
      String genusName = block.getAttribute("genus-name");

      if (genusName.equals("number-minus")) {
        assertEquals("\\u2212", getBlockLabel(block));
        checkedBlocks.add(genusName);

      } else if (genusName.equals("number-times")) {
        assertEquals("\\u00D7", getBlockLabel(block));
        checkedBlocks.add(genusName);
      }
    }

    assertTrue(checkedBlocks.contains("number-minus"));
    assertTrue(checkedBlocks.contains("number-times"));
  }

  public void testUpgradedBlocksHaveOnlyAsciiCharacters() throws Exception {
    // Read in version 10 block file with minus and times blocks, as well as text and comments with
    // I18N characters.
    BlockSaveFile saveFile = createBlockSaveFile("blocksI18N.blk");

    // Check that the need to upgrade was recognized.
    assertTrue(saveFile.wasUpgraded());

    // Check that the xml has only ascii characters.
    assertNodeContainsOnlyAsciiCharacters(saveFile.getRoot());
  }

  public void testUpgradeFormRenamedScreen() throws Exception {
    // Read in version 13 block file.
    BlockSaveFile saveFile = createBlockSaveFile("blocksVersion13.blk");

    // Check that the need to upgrade was recognized.
    assertTrue(saveFile.wasUpgraded());


    List<String> screenBlocks = new ArrayList<String>();

    // Check that no blocks have genus that equals "Form" or starts with "Form-".
    Element documentRoot = saveFile.getRoot();
    NodeList blocks = documentRoot.getElementsByTagName("Block");
    for (int i = 0; i < blocks.getLength(); i++) {
      Element block = (Element) blocks.item(i);
      String genusName = block.getAttribute("genus-name");
      assertFalse("There should not be any blocks whose genus is \"Form\"!",
          genusName.equals("Form"));
      assertFalse("There should not be any blocks whose genus starts with \"Form-\"!",
          genusName.startsWith("Form-"));
      if (genusName.startsWith("Screen-")) {
        screenBlocks.add(genusName);
      }
    }
    // Check that we found blocks whose genus is "Screen-Initialize", "Screen-OtherScreenClosed",
    // "Screen-ScreenOrientationChanged", "Screen-ErrorOccurred".
    assertTrue("There should be a block whose genus is \"Screen-Initialize\"!",
        screenBlocks.contains("Screen-Initialize"));
    assertTrue("There should be a block whose genus is \"Screen-OtherScreenClosed\"!",
        screenBlocks.contains("Screen-OtherScreenClosed"));
    assertTrue("There should be a block whose genus is \"Screen-ScreenOrientationChanged\"!",
        screenBlocks.contains("Screen-ScreenOrientationChanged"));
    assertTrue("There should be a block whose genus is \"Screen-ErrorOccurred\"!",
        screenBlocks.contains("Screen-ErrorOccurred"));

    // Check that the YoungAndroidUuidEntry with component-id Screen1 still has component-genus
    // Form.
    boolean foundScreen1 = false;
    NodeList entries = documentRoot.getElementsByTagName("YoungAndroidUuidEntry");
    for (int i = 0; i < entries.getLength(); i++) {
      Element entry = (Element) entries.item(i);
      String componentId = entry.getAttribute("component-id");
      if (componentId.equals("Screen1")) {
        foundScreen1 = true;
        assertEquals("Form", entry.getAttribute("component-genus"));
      }
    }
    // Check that we found an entry whose component-id is Screen1.
    assertTrue(foundScreen1);
  }

  public void testUpgradeOrientationSensorFromComponentVersion1() throws Exception {
    // Read in block file with version 1 of the OrientationSensor component.
    // The block file has two calls to
    // OrientationSensor.OrientationChanged(yaw, pitch, roll)
    // and an unrelated procedure named OrientationChanged with a yaw parameter.
    BlockSaveFile saveFile = createBlockSaveFile("yaw-azimuth.blk");

    // Check that the need to upgrade was recognized.
    assertTrue(saveFile.wasUpgraded());

    // Check that the first three sockets for both of the calls to
    // OrientationSensor.OrientationChanged are (azimuth, pitch, roll).
    Element documentRoot = saveFile.getRoot();
    NodeList blocks = documentRoot.getElementsByTagName("Block");
    int numCalls = 0;
    for (int i = 0; i < blocks.getLength(); i++) {
      assert blocks.item(i) instanceof Element;
      Element block = (Element) blocks.item(i);
      String genusName = block.getAttribute("genus-name");
      if (genusName.equals("OrientationSensor-OrientationChanged")) {
        numCalls++;
        JUnitAsserts.assertContentsInOrder(
            getSocketLabels(block), "azimuth", "pitch", "roll", "do");
      } else if (genusName.equals("caller-command")) {
        // The name of the procedure parameter should not have changed.
        JUnitAsserts.assertContentsInOrder(getSocketLabels(block), "yaw");
      }
    }
    // There should be exactly two calls.
    assertEquals(2, numCalls);
  }

  public void testUpgradeWebFromComponentVersion1() throws Exception {
    // Read in block file with version 1 of the Web component.
    // The block file has one call to Web1.PostText(text, encoding).
    BlockSaveFile saveFile = createBlockSaveFile("WebVersion1.blk");

    // Check that the need to upgrade was recognized.
    assertTrue(saveFile.wasUpgraded());

    // Check that the Web1.PostText block was changed to Web1.PostTextWithEncoding.
    Element documentRoot = saveFile.getRoot();
    NodeList blocks = documentRoot.getElementsByTagName("Block");
    int numCalls = 0;
    for (int i = 0; i < blocks.getLength(); i++) {
      assert blocks.item(i) instanceof Element;
      Element block = (Element) blocks.item(i);
      String genusName = block.getAttribute("genus-name");
      if (genusName.equals("Web-PostTextWithEncoding")) {
        numCalls++;
        // The label of the block should have been updated.
        assertEquals("Web1.PostTextWithEncoding", getBlockLabel(block));
      } else if (genusName.equals("Web-PostText")) {
        fail("Should not have found a block with genus name \"Web-PostText\".");
      }
    }
    // There should be exactly one call.
    assertEquals(1, numCalls);
  }

  /*
   * Checks whether the given block has been marked bad by
   * BlockSaveFile.markBlockBad().
   */
  static boolean blockMarkedBad(Node block) {
    NodeList childNodes = block.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i).getNodeName().equals(BlockSaveFile.BAD_BLOCK)) {
        return true;
      }
    }
    return false;
  }

  /*
   * Finds the sockets for this block and returns the values of their "label"
   * attributes as a List<String>.  If none is found, this returns an empty
   * List.
   */
  private List<String> getSocketLabels(Element block) {
    List<String> labels = new ArrayList<String>();
    NodeList connectors = block.getElementsByTagName("BlockConnector");
    for (int i = 0; i < connectors.getLength(); i++) {
      Element connector = (Element) connectors.item(i);
      if (connector.getAttribute("connector-kind").equals("socket")) {
        labels.add(connector.getAttribute("label"));
      }
    }
    return labels;
  }

  private String getBlockLabel(Element block) {
    return BlockSaveFile.getBlockLabelChild(block).getNodeValue();
  }

  private void assertNodeContainsOnlyAsciiCharacters(Node node) throws Exception {
    if (node instanceof Element) {
      assertElementContainsOnlyAsciiCharacters((Element) node);
    } else if (node instanceof Attr) {
      assertAttrContainsOnlyAsciiCharacters((Attr) node);
    } else if (node instanceof Text) {
      assertTextContainsOnlyAsciiCharacters((Text) node);
    } else {
      fail("Found a node that is not an Element, Attr, or Text");
    }
  }

  private void assertElementContainsOnlyAsciiCharacters(Element element) throws Exception {
    assertStringContainsOnlyAsciiCharacters(element.getNodeName(), "name", element);

    if (element.hasAttributes()) {
      NamedNodeMap attributes = element.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++) {
        assertNodeContainsOnlyAsciiCharacters(attributes.item(i));
      }
    }

    if (element.hasChildNodes()) {
      NodeList childNodes = element.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        assertNodeContainsOnlyAsciiCharacters(childNodes.item(i));
      }
    }
  }

  private void assertAttrContainsOnlyAsciiCharacters(Attr attr) throws Exception {
    assertStringContainsOnlyAsciiCharacters(attr.getName(), "name", attr);
    assertStringContainsOnlyAsciiCharacters(attr.getValue(), "value", attr);
  }

  private void assertTextContainsOnlyAsciiCharacters(Text text) throws Exception {
    assertStringContainsOnlyAsciiCharacters(text.getNodeValue(), "text value", text);
  }

  private void assertStringContainsOnlyAsciiCharacters(String s, String description, Node node) {
    if (s != null) {
      for (int i = 0; i < s.length(); i++) {
        char ch = s.charAt(i);
        if (ch > '~') {
          // Build a failure message that tells where the non-ascii character was found.
          StringBuilder failureMessage = new StringBuilder();

          // Find the id attribute of the containing block.
          Node parent = node;
          while (parent != null) {
            if (parent.getNodeName().equals("Block") && parent instanceof Element) {
              String blockId = ((Element) parent).getAttribute("id");
              failureMessage.append("In the Block whose id attribute is " + blockId + ", ");
              break;
            }
            if (parent instanceof Attr) {
              parent = ((Attr) parent).getOwnerElement();
            } else {
              parent = parent.getParentNode();
            }
          }

          if (failureMessage.length() == 0) {
            failureMessage.append("The ");
          } else {
            failureMessage.append("the ");
          }

          failureMessage.append(description).append(" of the ").append(describeNode(node))
              .append(" contains the non-ascii character 0x").append(Integer.toHexString(ch))
              .append(" at index ").append(i).append(": ").append(s).append(".");

          fail(failureMessage.toString());
        }
      }
    }
  }

  private String describeNode(Node node) {
    if (node instanceof Element) {
      return "<" + node.getNodeName() + "> element";
    } else if (node instanceof Attr) {
      return node.getNodeName() + " attribute of the " +
          describeNode(((Attr) node).getOwnerElement());
    } else if (node instanceof Text) {
      return describeNode(node.getParentNode());
    }
    throw new IllegalArgumentException("node must an Element, an Attr, or a Text: " +
        node.getClass().getName());
  }
}
