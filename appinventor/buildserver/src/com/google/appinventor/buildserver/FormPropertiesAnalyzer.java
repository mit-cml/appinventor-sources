// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Methods for analyzing the contents of a Young Android Form file.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class FormPropertiesAnalyzer {

  private static final String FORM_PROPERTIES_PREFIX = "#|\n";
  private static final String FORM_PROPERTIES_SUFFIX = "\n|#";
  
  // Logging support
  private static final Logger LOG = Logger.getLogger(FormPropertiesAnalyzer.class.getName());

  private FormPropertiesAnalyzer() {
  }

  /**
   * Parses a complete source file and return the properties as a JSONObject.
   *
   * @param source a complete source file
   * @return the properties as a JSONObject
   */
  public static JSONObject parseSourceFile(String source) {
    // First, locate the beginning of the $JSON section.
    // Older files have a $Properties before the $JSON section and we need to make sure we skip
    // that.
    String jsonSectionPrefix = FORM_PROPERTIES_PREFIX + "$JSON\n";
    int beginningOfJsonSection = source.lastIndexOf(jsonSectionPrefix);
    if (beginningOfJsonSection == -1) {
      throw new IllegalArgumentException(
          "Unable to parse file - cannot locate beginning of $JSON section");
    }
    beginningOfJsonSection += jsonSectionPrefix.length();

    // Then, locate the end of the $JSON section;
    String jsonSectionSuffix = FORM_PROPERTIES_SUFFIX;
    int endOfJsonSection = source.lastIndexOf(jsonSectionSuffix);
    if (endOfJsonSection == -1) {
      throw new IllegalArgumentException(
          "Unable to parse file - cannot locate end of $JSON section");
    }

    String jsonPropertiesString = source.substring(beginningOfJsonSection,
        endOfJsonSection);
    try {
      return new JSONObject(jsonPropertiesString);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse file - invalid $JSON section syntax");
    }
  }

  /**
   * Returns the Set of component types used in the given form file source.
   */
  public static Set<String> getComponentTypesFromFormFile(String source) {
    Set<String> componentTypes = new HashSet<String>();
    JSONObject propertiesObject = parseSourceFile(source);
    try {
      collectComponentTypes(propertiesObject.getJSONObject("Properties"), componentTypes);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse file - invalid $JSON section syntax");
    }
    return componentTypes;
  }

  private static void collectComponentTypes(JSONObject componentProperties,
      Set<String> componentTypes) throws JSONException {
    String componentType = componentProperties.getString("$Type");
    componentTypes.add(componentType);

    // Recursive call to collect nested components.
    if (componentProperties.has("$Components")) {
      JSONArray components = componentProperties.getJSONArray("$Components");
      for (int i = 0; i < components.length(); i++) {
        collectComponentTypes(components.getJSONObject(i), componentTypes);
      }
    }
  }

  /**
   * Extracts a mapping from component to set of blocks used from the Form's
   * Scheme (.scm) file, which is actually a JSON dictionary contained within
   * a block comment. Any property that is expressed in the properties section
   * (i.e., not the default value) is considered used by the function.
   *
   * @param source Source contents of the Scheme file
   * @return A mapping of component type names to sets of blocks used
   */
  public static Map<String, Set<String>> getComponentBlocksFromSchemeFile(String source) {
    Map<String, Set<String>> result = new HashMap<>();
    JSONObject propertiesObject = parseSourceFile(source);
    try {
      Queue<JSONObject> toProcess = new LinkedList<JSONObject>();
      toProcess.add(propertiesObject.getJSONObject("Properties"));
      while ((propertiesObject = toProcess.poll()) != null) {
        String type = propertiesObject.getString("$Type");
        if (!result.containsKey(type)) {
          result.put(type, new HashSet<String>());
        }
        Set<String> typeProps = result.get(type);
        Iterator<String> it = propertiesObject.keys();
        while (it.hasNext()) {
          String key = it.next();
          if (!key.startsWith("$")) {
            typeProps.add(key);
          }
        }
        if (propertiesObject.has("$Components")) {
          JSONArray components = propertiesObject.getJSONArray("$Components");
          for (int i = 0; i < components.length(); i++) {
            toProcess.add(components.getJSONObject(i));
          }
        }
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse file - invalid $JSON section syntax");
    }
    return result;
  }

  /**
   * Extracts a mapping from component to set of blocks used from the Form's
   * Blocks (.bky) file. This method will <strong>EXCLUDE</strong> any blocks
   * that are marked disabled. This allows a developer to keep the old variation
   * of any blocks that would cause complications in their project for reference.
   *
   * @param source Source contents of the Blockly (XML) file.
   * @return A mapping of component type names to sets of blocks used
   */
  public static Map<String, Set<String>> getComponentBlocksFromBlocksFile(String source) {
    Map<String, Set<String>> result = new HashMap<>();
    if (source == null || source.isEmpty()) {
      return result;
    }
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
      NodeList blocks = doc.getElementsByTagName("block");
      for (int i = 0; i < blocks.getLength(); i++) {
        Element block = (Element) blocks.item(i);
        String type = block.getAttribute("type");

        // Skip non-component and disabled blocks
        if (!isComponentBlock(type) || "true".equals(block.getAttribute("disabled"))) continue;

        Element mutation = getMutation(block);
        if (mutation != null) {  // Should always be true, in theory...
          String componentType = mutation.getAttribute("component_type");
          String blockName = getBlockName(type, mutation);
          if (!result.containsKey(componentType)) {
            result.put(componentType, new HashSet<String>());
          }
          result.get(componentType).add(blockName);
        }
      }
    } catch(SAXException | IOException | ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
    return result;
  }

  /**
   * Tests a block to see if it is a component block.
   * @param type The type string from the block element
   * @return true if the type string represents a component block, otherwise
   * false.
   */
  private static boolean isComponentBlock(String type) {
    return type != null && type.startsWith("component_");
  }

  /**
   * Extracts the &lt;mutation&gt; element from the block XML.
   *
   * @param block The &lt;block&gt; element being processed
   * @return An Element representing the &lt;mutation&gt; element of the block
   * if one exists, otherwise null.
   */
  private static Element getMutation(Element block) {
    NodeList children = block.getChildNodes();
    for (int j = 0; j < children.getLength(); j++) {
      Node child = children.item(j);
      if (child instanceof Element) {
        Element childEl = (Element) child;
        if ("mutation".equals(childEl.getTagName())) {
          return childEl;
        }
      }
    }
    return null;
  }

  /**
   * Gets the name of a component block from the mutation.
   *
   * @param type The type of the block, e.g. component_event
   * @param mutation The mutation element extracted from the XML
   * @return the name of the block's event, method, or property, if the block
   * is one of those types (either a specific instance or generic). Otherwise,
   * null.
   */
  private static String getBlockName(String type, Element mutation) {
    if ("component_event".equals(type)) {
      return mutation.getAttribute("event_name");
    } else if ("component_method".equals(type)) {
      return mutation.getAttribute("method_name");
    } else if ("component_set_get".equals(type)) {
      return mutation.getAttribute("property_name");
    } else {
      return null;
    }
  }
}
