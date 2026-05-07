// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.common.Permission;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

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
   * The BlockXmlAnalyzer interface can be implemented by classes that need to analyze the Blockly
   * XML file and produce a result.
   *
   * @param <T> the type of the analysis result
   */
  public interface BlockXmlAnalyzer<T> extends ContentHandler {

    /**
     * Get the result of the analysis.
     *
     * @return the result, if any
     */
    T getResult();
  }

  /**
   * AbstractBlockXmlAnalyzer provides a basic handling of the Blockly XML files.
   *
   * <p>In particular, it manages processing of the {@code disabled} property. If a block is marked
   * disabled, or is a input-descendant of the disabled block, then {@link #isBlockEnabled()} will
   * return {@code false}.
   *
   * <p>Subclasses must call the super implementations of
   * {@link #startElement(String, String, String, Attributes)} and
   * {@link #endElement(String, String, String)} for the logic to work correctly.
   *
   * @param <T> the type of the analysis result
   */
  abstract static class AbstractBlockXmlAnalyzer<T> extends DefaultHandler
      implements BlockXmlAnalyzer<T> {

    /**
     * Block skip counter. When a block is disabled the count is incremented. The counter will
     * be decremented when either the block is ended or the next statement element is seen.
     */
    private int skipBlocksCounter = 0;

    @Override
    public void startElement(String uri, String localName, String qname,
        Attributes attributes) throws SAXException {
      if ("block".equals(qname)) {
        if ("true".equals(attributes.getValue("disabled")) || skipBlocksCounter > 0) {
          skipBlocksCounter++;
        }
      } else if ("next".equals(qname) && skipBlocksCounter == 1) {
        skipBlocksCounter = 0;
      }
      super.startElement(uri, localName, qname, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qname) throws SAXException {
      if ("block".equals(qname) && skipBlocksCounter > 0) {
        skipBlocksCounter--;
      }
      super.endElement(uri, localName, qname);
    }

    /**
     * Checks whether the current block is enabled or not, possibly inheriting its enabled state
     * from an ancestor block.
     *
     * @return true if the block is enabled, otherwise false
     */
    boolean isBlockEnabled() {
      return skipBlocksCounter == 0;
    }
  }

  /**
   * Constructs a mapping of component types to the blocks of each type used in
   * the project files. Properties specified in the designer are considered
   * blocks for the purposes of this operation.
   */
  public static class ComponentBlocksExtractor
      extends AbstractBlockXmlAnalyzer<Map<String, Set<String>>> {
    private final Map<String, Set<String>> result = new HashMap<>();
    private String blockType;

    @Override
    public void startElement(String uri, String localName, String qname, Attributes attributes)
        throws SAXException {
      super.startElement(uri, localName, qname, attributes);
      if ("block".equals(qname)) {
        blockType = attributes.getValue("type");
      } else if (isBlockEnabled() && "mutation".equals(qname)) {
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
    }

    @Override
    public Map<String, Set<String>> getResult() {
      return result;
    }
  }

  /**
   * HelperBlockExtractor is an abstract class that identifies helper blocks. Subclasses can call
   * {@link #isOptionField()} to determine whether they are within a helper block's option field.
   *
   * @param <T> the type of the analysis result
   */
  abstract static class HelperBlockExtractor<T> extends AbstractBlockXmlAnalyzer<T> {
    private final String key;
    // Flag indicates that we are currently in a helper block
    private boolean inHelperBlock = false;
    // Flag indicates that we are currently processing the desired block
    private boolean inDesiredBlock = false;
    // Flag indicates that we are in the option field of the block
    private boolean inOptionField = false;

    /**
     * Create a new HelperBlockExtractor.
     *
     * {@code key} specifies the helper block key that should be matched.
     * Typically, this is the simple class name of an enum implementing the
     * {@link com.google.appinventor.components.common.OptionList} interface.
     *
     * @param key the target helper block key, for example {@code "Permission"}
     */
    HelperBlockExtractor(String key) {
      this.key = key;
    }

    boolean isOptionField() {
      return inOptionField;
    }

    @Override
    public void startElement(String uri, String localName, String qname,
        Attributes attributes) throws SAXException {
      super.startElement(uri, localName, qname, attributes);
      if ("block".equals(localName)) {
        if (isBlockEnabled() && "helpers_dropdown".equals(attributes.getValue("type"))) {
          inHelperBlock = true;
        }
      } else if (inHelperBlock && "mutation".equals(localName)
          && key.equals(attributes.getValue("key"))) {
        inDesiredBlock = true;
      } else if (inDesiredBlock && "field".equals(localName)) {
        inOptionField = true;
      }
    }

    @Override
    public void endElement(String uri, String localName, String qname) throws SAXException {
      super.endElement(uri, localName, qname);
      if ("block".equals(localName)) {
        inHelperBlock = false;
        inDesiredBlock = false;
        inOptionField = false;
      }
    }
  }

  /**
   * The PermissionBlockExtractor analyzes the helper blocks for Permission related blocks and
   * aggregates the requested permissions into a {@link java.util.Set}.
   */
  public static class PermissionBlockExtractor extends HelperBlockExtractor<Set<String>> {
    private final Set<String> result = new HashSet<>();

    PermissionBlockExtractor() {
      super("Permission");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (isOptionField()) {
        String permissionValue = new String(ch, start, length);
        try {
          Permission permission = Permission.valueOf(permissionValue);
          result.add("android.permission." + permission.toUnderlyingValue());
        } catch (IllegalArgumentException e) {
          // In theory this shouldn't happen because the block is defined by the enum.
          LOG.log(Level.WARNING, "Missing enum value from blocks", e);
        }
      }
      super.characters(ch, start, length);
    }

    public Set<String> getResult() {
      return result;
    }
  }

  /**
   * The ScopeBlockExtractor analyzes the helper blocks for FileScope related blocks and
   * aggregates the requested scopes into a {@link java.util.Set}.
   */
  public static class ScopeBlockExtractor extends HelperBlockExtractor<Set<String>> {
    private final Set<String> result = new HashSet<>();

    ScopeBlockExtractor() {
      super("FileScope");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (isOptionField()) {
        String scopeValue = new String(ch, start, length);
        try {
          FileScope scope = FileScope.valueOf(scopeValue);
          result.add(scope.toUnderlyingValue());
        } catch (IllegalArgumentException e) {
          // In theory this shouldn't happen because the block is defined by the enum.
          LOG.log(Level.WARNING, "Missing enum value from blocks", e);
        }
      }
      super.characters(ch, start, length);
    }

    @Override
    public Set<String> getResult() {
      return result;
    }
  }

  /**
   * Extracts a mapping from component to set of blocks used from the Form's
   * Blocks (.bky) file. This method will <strong>EXCLUDE</strong> any blocks
   * that are marked disabled. This allows a developer to keep the old variation
   * of any blocks that would cause complications in their project for reference.
   *
   * @param source Source contents of the Blockly (XML) file.
   */
  public static void analyzeBlocks(String source, final ContentHandler... analyzers) {
    if (source == null) {
      throw new NullPointerException("Source must be specified");
    }
    if (source.isEmpty()) {
      return;  // Empty blocks file, so nothing to do.
    }
    try {
      XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(new DefaultHandler() {
        @Override
        public void startElement(String uri, String localName, String qname, Attributes attributes)
            throws SAXException {
          for (ContentHandler analyzer : analyzers) {
            analyzer.startElement(uri, localName, qname, attributes);
          }
          super.startElement(uri, localName, qname, attributes);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
          for (ContentHandler analyzer : analyzers) {
            analyzer.characters(ch, start, length);
          }
          super.characters(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qname) throws SAXException {
          for (ContentHandler analyzer : analyzers) {
            analyzer.endElement(uri, localName, qname);
          }
          super.endElement(uri, localName, qname);
        }
      });
      reader.parse(new InputSource(new ByteArrayInputStream(source.getBytes(UTF_8))));
    } catch (SAXException | IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Computes a mapping from form name to desired orientation set by the user.
   *
   * @param source the contents of a .scm file
   * @return screen orientation map
   * @throws JSONException if the form's properties field is missing
   */
  public static String getFormOrientation(String source) throws JSONException {
    JSONObject propertiesObject = parseSourceFile(source);
    JSONObject props = propertiesObject.getJSONObject("Properties");
    return props.optString("ScreenOrientation", "unspecified");
  }
}
