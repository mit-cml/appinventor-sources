// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnectorShape;
import openblocks.codeblocks.BlockGenus;
import openblocks.codeblocks.BlockLinkChecker;
import openblocks.codeblocks.CommandRule;
import openblocks.codeblocks.ComplaintDepartment;
import openblocks.codeblocks.SocketRule;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import openblocks.renderable.RenderableBlock;
import openblocks.workspace.Page;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceWidget;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author lizlooney@google.com (Liz Looney)
 *
 */
public class WorkspaceUtils {
  private static final boolean DEBUG = false;
  private static final String LANG_DEF_XML_PATH = "support/ya_lang_def.xml";
  private static final String GLOBALS = "Global Definitions";

  private WorkspaceUtils() {
  }

  /**
   * Parse the JSON properties for this form.
   * @return the JSONObject, or null if there was a problem locating or parsing
   * the $JSON section.
   */
  public static JSONObject parseFormProperties(String formProperties) {
    // First, locate the beginning of the $JSON section.
    String jsonSectionPrefix = "#|\n$JSON\n";
    int beginningOfJsonSection = formProperties.lastIndexOf(jsonSectionPrefix);
    if (beginningOfJsonSection == -1) {
      FeedbackReporter.showSystemErrorMessage(
          "Unable to parse form properties - cannot locate beginning of $JSON section");
      return null;
    }
    beginningOfJsonSection += jsonSectionPrefix.length();

    // Then, locate the end of the $JSON section;
    String jsonSectionSuffix = "\n|#";
    int endOfJsonSection = formProperties.lastIndexOf(jsonSectionSuffix);
    if (endOfJsonSection == -1) {
      FeedbackReporter.showSystemErrorMessage(
          "Unable to parse form properties - cannot locate end of $JSON section");
      return null;
    }

    String jsonPropertiesString = formProperties.substring(beginningOfJsonSection,
        endOfJsonSection);
    try {
      return new JSONObject(jsonPropertiesString);
    } catch (JSONException e) {
      e.printStackTrace();
      FeedbackReporter.showSystemErrorMessage(
          "Unable to parse form properties - error parsing JSON " + jsonPropertiesString);
      return null;
    }
  }

  /**
   * Creates a DocumentBuilder that uses our DTDResolver.
   */
  public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    // Allow us to load an "external" dtd.
    // Without this, our DTDResolver is never called on GoogleJDK6Release4.
    try {
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
    } catch (ParserConfigurationException e) {
      // Feature is not supported.
      // This is probably ok, because prior to the feature being supported, it would always load
      // the dtd.
    }

    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(new DTDResolver());
    return builder;
  }

  /**
   * Loads the ya_lang_def.xml resource and returns the root DOM Element.
   */
  public static Element loadLangDef()
      throws IOException, ParserConfigurationException, SAXException {
    //System.out.println("Loading language def from jar resource: " + LANG_DEF_XML_PATH);
    InputStream langDefStream = WorkspaceUtils.class.getResourceAsStream(LANG_DEF_XML_PATH);
    try {
      return loadLangDef(langDefStream);
    } finally {
      langDefStream.close();
    }
  }

  /**
   * Loads the language definitions from the given InputStream and returns the
   * root DOM Element.
   */
  public static Element loadLangDef(InputStream langDefStream)
      throws IOException, ParserConfigurationException, SAXException {
    DocumentBuilder builder = newDocumentBuilder();
    Document doc = builder.parse(langDefStream);
    return doc.getDocumentElement();
  }

  /**
   * Returns the ya-version attribute that was specified in the root node
   * (BlockLangDef) of ya_lang_def.xml; 0 if there was no ya-version.
   */
  public static int getYoungAndroidVersion(Element langDefRoot) {
    String version = langDefRoot.getAttribute("ya-version");
    try {
      return Integer.parseInt(version);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Returns the lang-version attribute that was specified in the root node
   * (BlockLangDef) of ya_lang_def.xml; 0 if there was no lang-version.
   */
  public static int getBlocksLanguageVersion(Element langDefRoot) {
    String version = langDefRoot.getAttribute("lang-version");
    try {
      return Integer.parseInt(version);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Returns the LangSpecProperty component-version that was specified in
   * ya_lang_def.xml for the BlockGenus with the given componentName; 0 if there
   * was no component-version.
   *
   * @param componentGenus the component genus
   * @return the component version
   */
  public static int getComponentVersion(String componentGenus) {
    BlockGenus blockGenus = BlockGenus.getGenusWithName(componentGenus);
    if (blockGenus != null) {
      String yaKind = blockGenus.getProperty("ya-kind");
      if (yaKind.equals("component")) {
        String componentVersion = blockGenus.getProperty("component-version");
        if (componentVersion != null) {
          return Integer.parseInt(componentVersion);
        }
      }
    }
    // Many block genus don't have (or need) a component version number.
    return 0;
  }

  /**
   * Resets the current language within the active Workspace.
   */
  public static void resetLanguage() {
    // Clear shape mappings.
    BlockConnectorShape.resetConnectorShapeMappings();
    // Clear block genuses.
    BlockGenus.resetAllGenuses();
    // Clear all link rules.
    BlockLinkChecker.reset();
  }

  /**
   * Loads block connector shapes, block colors, block genuses, and block rules
   * from the given langDefRoot.
   */
  public static void loadLanguage(Element langDefRoot) {
    //load connector shapes
    //MUST load shapes before genuses in order to initialize connectors within
    //each block correctly
    BlockConnectorShape.loadBlockConnectorShapes(langDefRoot);

    // load color names
    BlockColor.loadBlockColors(langDefRoot);

    // load genuses
    BlockGenus.loadBlockGenera(langDefRoot);

    // load rules
    BlockLinkChecker.addRule(new CommandRule());
    BlockLinkChecker.addRule(new SocketRule());
    BlockLinkChecker.addRule(new PolyRule());
    BlockLinkChecker.addRule(new YaRule());
    // Initialize block rules
    BlockRules.initializeRules();
  }

  /**
   * Builds a map of components -> top level blocks for that component.
   * A special entry for GLOBALS maps to top-level global definitions.
   *
   * @param componentMap the HashMap to populate with components and their top level blocks
   * @param warnings a Map that will be filled with warnings for troublesome blocks
   * @param errors a list that will be filled with error messages
   * @param forRepl whether this is executed for REPL
   * @param compileUnattachedBlocks whether to compile unattached blocks
   * @param cbm the component block manager
   */
  public static void populateComponentMap(HashMap<String, ArrayList<RenderableBlock>> componentMap,
      Map<Block, String> warnings, List<String> errors, boolean forRepl,
      boolean compileUnattachedBlocks, ComponentBlockManager cbm) {
    // Initialize the component map with all known components
    for (String componentName : cbm.getComponentNames()) {
      componentMap.put(componentName, new ArrayList<RenderableBlock>());
    }
    componentMap.put(GLOBALS, new ArrayList<RenderableBlock>());
    for (WorkspaceWidget widget : Workspace.getInstance().getWorkspaceWidgets()) {
      if (widget instanceof Page) {
        Page p = (Page) widget;
        Set<String> topBlocks = new HashSet<String>();
        for (RenderableBlock rb : p.getTopLevelBlocks()) {
          String label = rb.getBlock().getBlockLabel();
          if (!compileUnattachedBlocks) {
            if (!isTopLevelDefinition(rb.getBlock().getProperty("ya-kind"))) {
              if (!forRepl) {
                warnings.put(rb.getBlock(), ComplaintDepartment.UNATTACHED);
              }
              continue;
            }
          }
          if (topBlocks.contains(label)) {
            warnings.put(rb.getBlock(), ComplaintDepartment.DUPLICATE_HANDLER);
            continue;
          } else {
            topBlocks.add(label);
          }

          String componentName = blockComponentName(label);
          if (componentName == null) {
            componentName = GLOBALS;
          }
          if (!componentMap.containsKey(componentName)) {
            errors.add("Ignoring block for unknown component " + rb.getBlock().getBlockLabel());
            continue;
          }
          componentMap.get(componentName).add(rb);
        }
      }
    }
  }

  /**
   * Returns true if the given ya-kind value indicates that it is a top level
   * definition.
   */
  private static boolean isTopLevelDefinition(String yaKind) {
    return yaKind.equals("def") || yaKind.equals("componentEvent") || yaKind.equals("define")
        || yaKind.equals("define-void");
  }

  /**
   * Returns the name of the component associated with the given block label,
   * or null if the block is not associated with a component.
   */
  private static String blockComponentName(String blockLabel) {
    int index = blockLabel.indexOf('.');
    if (index >= 0) {
      if (DEBUG) {
        System.out.print("Component name for " + blockLabel + " is " +
            blockLabel.substring(0, index) + "\n");
      }
      return blockLabel.substring(0, index);
    } else {
      return null;
    }
  }
}
