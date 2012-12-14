// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;

/**
 * Tests that verify the content of the generated ya_lang_def.xml file.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class LangDefTest extends TestCase {
  private static List<String> VALID_PLUG_TYPES = Arrays.asList(
      "text",
      "number",
      "boolean",
      "list",
      "InstantInTime",
      "value",
      "argument",
      "component",
      "null");
  private static List<String> VALID_SOCKET_TYPES = Arrays.asList(
      "text",
      "number",
      "boolean",
      "list",
      "InstantInTime",
      "value",
      "argument",
      "component");

  private Document document;
  private Element documentRoot;

  @Override
  public void setUp() throws Exception {

    String yaLangDef = TestUtils.APP_INVENTOR_ROOT_DIR +
        "/build/components/ya_lang_def.xml";

    DocumentBuilder builder = WorkspaceUtils.newDocumentBuilder();
    document = builder.parse(new FileInputStream(yaLangDef));
    documentRoot = document.getDocumentElement();
    assertNotNull(document);
    assertNotNull(documentRoot);

    super.setUp();
  }

  public void testPlugTypes() throws Exception {
    int countPlugTypesTested = 0;

    // Iterate through all BlockGenus nodes in the document.
    NodeList blockGenusList = documentRoot.getElementsByTagName("BlockGenus");
    for (int iBlockGenus = 0; iBlockGenus < blockGenusList.getLength(); iBlockGenus++) {
      Element blockGenus = (Element) blockGenusList.item(iBlockGenus);

      // Iterate through all LangSpecProperty nodes in the BlockGenus node.
      NodeList langSpecPropertyList = blockGenus.getElementsByTagName("LangSpecProperty");
      for (int iLSP = 0; iLSP < langSpecPropertyList.getLength(); iLSP++) {
        Element langSpecProperty = (Element) langSpecPropertyList.item(iLSP);
        // Look for ones whose key starts with "plug-type-"
        if (langSpecProperty.getAttribute("key").startsWith("plug-type-")) {
          // Get the LangSpecProperty value attribute.
          String plugTypeValue = langSpecProperty.getAttribute("value");
          // plugTypeValue must be a valid plug type.
          assertTrue("Encountered unknown plug type " + plugTypeValue,
              VALID_PLUG_TYPES.contains(plugTypeValue));
          countPlugTypesTested++;
        }
      }
    }
    assertTrue(countPlugTypesTested > 1);
  }

  public void testSocketAllowValuesMatchSocketLabels() throws Exception {
    int countSocketAllowValuesTested = 0;

    // Iterate through all BlockGenus nodes in the document.
    NodeList blockGenusList = documentRoot.getElementsByTagName("BlockGenus");
    for (int iBlockGenus = 0; iBlockGenus < blockGenusList.getLength(); iBlockGenus++) {
      Element blockGenus = (Element) blockGenusList.item(iBlockGenus);

      // Collect the socket labels.
      List<String> socketLabels = new ArrayList<String>();

      // Iterate through all BlockConnector nodes in the BlockGenus node.
      NodeList blockConnectorList = blockGenus.getElementsByTagName("BlockConnector");
      for (int iBC = 0; iBC < blockConnectorList.getLength(); iBC++) {
        Element blockConnector = (Element) blockConnectorList.item(iBC);
        // Look for ones whose connector-kind is socket.
        if (blockConnector.getAttribute("connector-kind").equals("socket")) {
          // Get the BlockConnector label attribute.
          socketLabels.add(blockConnector.getAttribute("label"));
        }
      }

      // Check the socket-allow values.
      // Iterate through all LangSpecProperty nodes in the BlockGenus node.
      NodeList langSpecPropertyList = blockGenus.getElementsByTagName("LangSpecProperty");
      for (int iLSP = 0; iLSP < langSpecPropertyList.getLength(); iLSP++) {
        Element langSpecProperty = (Element) langSpecPropertyList.item(iLSP);
        // Look for ones whose key starts with "socket-allow-"
        if (langSpecProperty.getAttribute("key").startsWith("socket-allow-")) {
          // Get the LangSpecProperty value attribute.
          String socketAllowValue = langSpecProperty.getAttribute("value");
          // Split the socketAllowValue at "/".
          String[] socketAllowValueParts = socketAllowValue.split("/");
          assertEquals(2, socketAllowValueParts.length);
          // socketAllowValueParts[0] must either be "default" or one of the socket labels that we
          // collected.
          if (!socketAllowValueParts[0].equals("default")) {
            assertTrue("Encountered unexpected socket label " + socketAllowValueParts[0],
                socketLabels.contains(socketAllowValueParts[0]));
          }
          // socketAllowValueParts[1] must be a valid socket type.
          assertTrue("Encountered invalid socket type " + socketAllowValueParts[1],
              VALID_SOCKET_TYPES.contains(socketAllowValueParts[1]));
          countSocketAllowValuesTested++;
        }
      }
    }
    assertTrue(countSocketAllowValuesTested > 1);
  }
}
