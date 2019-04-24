// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

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
    source = source.replaceAll("\r\n", "\n");
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

    // Then, locate the end of the $JSON section
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
    final Map<String, Set<String>> result = new HashMap<>();
    if (source == null || source.isEmpty()) {
      return result;
    }
    try {
      XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(new DefaultHandler() {
        private int skipBlocksCounter = 0;
        private String blockType;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
          if ("block".equals(qName)) {
            if ("true".equals(attributes.getValue("disabled")) || skipBlocksCounter > 0) {
              skipBlocksCounter++;
            }
            blockType = attributes.getValue("type");
          } else if ("next".equals(qName) && skipBlocksCounter == 1) {
            skipBlocksCounter = 0;
          } else if (skipBlocksCounter == 0 && "mutation".equals(qName)) {
            String blockName = null;
            if ("component_event".equals(blockType)) {
              blockName = attributes.getValue("event_name");
            } else if ("component_method".equals(blockType)) {
              blockName = attributes.getValue("method_name");
            } else if ("component_set_get".equals(blockType)) {
              blockName = attributes.getValue("property_name");
            }
            if (blockName != null) {
              String componentType = attributes.getValue("component_type");
              if (!result.containsKey(componentType)) {
                result.put(componentType, new HashSet<String>());
              }
              result.get(componentType).add(blockName);
            }
          }

          super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
          if ("block".equals(qName) && skipBlocksCounter > 0) {
            skipBlocksCounter--;
          }
          super.endElement(uri, localName, qName);
        }
      });
      reader.parse(new InputSource(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8))));
    } catch (SAXException | IOException e) {
      throw new IllegalStateException(e);
    }
    return result;
  }
}
