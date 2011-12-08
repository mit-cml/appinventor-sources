// Copyright 2009 Google Inc. All Rights Reserved.
package openblocks.yacodeblocks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import openblocks.renderable.RenderableBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YABlockCompiler is responsible for translating the codeblocks block structure
 * into the Young Android Intermediate Language (YAIL)
 *
 * @author sharon@google.com (Sharon Perl) - initial version that just generates text
 *      representation of the block structure
 *
 */
public class YABlockCompiler {

  private static final boolean DEBUG = false;

  private static final String YAIL_ADD_COMPONENT = "(add-component ";
  private static final String YAIL_BEGIN = "(begin ";
  private static final String YAIL_COMMENT_MAJOR = ";;; ";
  private static final String YAIL_CLEAR_FORM = "(clear-current-form)";
  private static final String YAIL_CLOSE_BLOCK = ")\n";
  private static final String YAIL_COMPONENT_REMOVE = "(remove-component ";
  private static final String YAIL_DEFINE_FORM = "(define-form ";
  private static final String YAIL_DO_AFTER_FORM_CREATION = "(do-after-form-creation ";
  private static final String YAIL_FALSE = "#f";
  private static final String YAIL_GET_COMPONENT = "(get-component ";
  private static final String YAIL_GET_PROPERTY = "(get-property ";
  private static final String YAIL_INIT_RUNTIME = "(init-runtime)";
  private static final String YAIL_INITIALIZE_COMPONENTS =
                                        "(call-Initialize-of-components";
  private static final String YAIL_LINE_FEED = "\n";
  private static final String YAIL_OPEN_BLOCK = "(";
  private static final String YAIL_QUOTE = "'";
  private static final String YAIL_RENAME_COMPONENT = "(rename-component ";
  private static final String YAIL_SET_AND_COERCE_PROPERTY = "(set-and-coerce-property! ";
  private static final String YAIL_SET_SUBFORM_LAYOUT_PROPERTY =
                              "(%set-subform-layout-property! ";
  public static final String YAIL_SPACER = " ";
  private static final String YAIL_TRUE = "#t";

  private static final String SIMPLE_HEX_PREFIX = "&H";
  private static final int SIMPLE_HEX_PREFIX_LEN = SIMPLE_HEX_PREFIX.length();
  private static final String YAIL_HEX_PREFIX = "#x";

  // permit leading and trailing whitespace for checking that strings
  // are numbers
  protected static final String INTEGER_REGEXP = "^[\\s]*[-+]?[0-9]+[\\s]*$";
  protected static final String FLONUM_REGEXP = "^[\\s]*[-+]?([0-9]*)((\\.[0-9]+)|[0-9]\\.)[\\s]*$";

  private static final String GLOBALS = "Global Definitions";

  // TODO(user): convert file to use StringBuilder

  private static BlockParser blockParser;

  /**
   *
   * @param yailPath the place in permanent storage, just to stuff in file
   * @param formName
   * @return the Yail for the beginning of a file for an APK compilation
   */
  public static String getYailPrelude(String yailPath, String formName) {
    StringBuilder code = new StringBuilder();
    // NOTE(lizlooney) - if this prelude changes, YailGeneratorTool will need to be updated.
    code.append("#|\n$Source $Yail\n|#\n\n")
        .append(YAIL_DEFINE_FORM)
        .append(packageNameFromPath(yailPath))
        .append(YAIL_SPACER)
        .append(formName)
        .append(YAIL_CLOSE_BLOCK)
        .append("(require <com.google.youngandroid.runtime>)\n");
    return code.toString();
  }

  /**
   * Generate the Yail representation of the current Codeblocks code
   * @param formProperties a JSONObject representation of the components
   * on this form and their properties.
   * @param componentMap a map of renderable blocks.
   * @param forRepl true if minimizing complaints, for the Repl.
   * @return the Yail representation
   */
  public static String generateYailForProject(JSONObject formProperties,
      HashMap<String, ArrayList<RenderableBlock>> componentMap, boolean forRepl)
      throws YailGenerationException {
    String formName;
    try {
      formName = formProperties.getJSONObject("Properties").getString("$Name");
    } catch (JSONException e) {
      throw new YailGenerationException("Unable to determine form name");
    }
    StringBuilder code = new StringBuilder();
    blockParser = new BlockParser(forRepl);

    // Add 'My Program' globals
    if (componentMap.containsKey(GLOBALS)) {
      code.append(getYail(GLOBALS, componentMap.get(GLOBALS)));
    }
    // Walk the properties in a breadth-first manner to ensure that parent components
    // generate YAIL before their children.
    if (formProperties == null) {
      return code.toString();
    }
    try {
      String sourceType = formProperties.getString("Source");
      formProperties = formProperties.getJSONObject("Properties");
      if (sourceType.equals("Form")) {
        code.append(generateYailHelper(formName, formProperties, formProperties,
            componentMap, forRepl));
    } else {
        FeedbackReporter.showSystemErrorMessage("Source type " + sourceType + " is invalid.");
      }
    } catch (JSONException e) {
      FeedbackReporter.showSystemErrorMessage("Error parsing JSON " + formProperties.toString());
    }


    // Add runtime initializations
    code.append(genRuntimeInit());

    // System.out.println("Generate yail returned: " + finalCode);

    if (forRepl) {
      code = wrapForRepl(code, formName, componentMap);
    }
    String finalCode = code.toString();
    // Get rid of empty property assignments
    Matcher propertyMatcher = Pattern.compile("\\(set-property.*\"\"\\)\\n*",
                                       Pattern.MULTILINE).matcher(finalCode);
    finalCode = propertyMatcher.replaceAll("");
    return finalCode;
  }

  private static StringBuilder wrapForRepl(StringBuilder code, String formName,
      HashMap<String, ArrayList<RenderableBlock>> componentMap) {
    StringBuilder replCode = new StringBuilder();
    replCode.append(YAIL_BEGIN)
        .append(generateYailClearForm());
    if (!formName.equals("Screen1")) {
      // If this form is not named Screen1, then the REPL won't be able to resolve any references
      // to it or to any properties on the form itself (such as Title, BackgroundColor, etc) unless
      // we tell it that "Screen1" has been renamed to formName.
      // By generating a call to rename-component here, the REPL will rename "Screen1" to formName
      // in the current environment. See rename-component in runtime.scm.
      replCode.append(generateComponentRename("Screen1", formName));
    }
    replCode.append(code)
        .append(generateComponentIntialization(componentMap.keySet()))
        .append(YAIL_CLOSE_BLOCK);
    return replCode;
  }

  private static StringBuilder generateComponentIntialization(Set<String> componentNames) {
    StringBuilder initCall = new StringBuilder(YAIL_INITIALIZE_COMPONENTS);
    for (String cName : componentNames) {
      if (!cName.equals(GLOBALS)) {
        initCall.append(" ").append(YAIL_QUOTE).append(cName);
      }
    }
    initCall.append(")");
    return initCall;
  }

  private static String genRuntimeInit() {
    return YAIL_INIT_RUNTIME;
  }


  // Helper method for generating YAIL from a map of component name -> blocks
  private static String generateYailHelper(String formName, final JSONObject formProperties,
      JSONObject jsonToWalk, HashMap<String, ArrayList<RenderableBlock>> componentMap,
      boolean forRepl)
      throws YailGenerationException {
    StringBuilder code = new StringBuilder();
    try {
      String name = jsonToWalk.getString("$Name");
      // note that the main "Form" is not in the componentMap, so its
      // properties will not be handled
      code.append(getYailForComponentProperties(formName, formProperties, jsonToWalk, !forRepl));
      if (componentMap.containsKey(name)) {
        //TODO(halabelson): As far as I can see, this next append is not doing anything: the
        // getYail is always empty.  Is there are case I am missing?
        code.append(getYail(name, componentMap.get(name)));
      }
      if (jsonToWalk.has("$Components")) {
        JSONArray components = jsonToWalk.getJSONArray("$Components");
        for (int i = 0; i < components.length(); i++) {
          code.append(generateYailHelper(formName, formProperties, components.getJSONObject(i),
              componentMap, forRepl));
        }
      }
    } catch (JSONException e) {
      FeedbackReporter.showSystemErrorMessage("Error generating Yail for source "
          + jsonToWalk);
    }
    return code.toString();
  }

  /**
   * Get the Yail expression to evaluate to set the properties of a
   * component, and (except in the case of a form) to add the component to
   * it parent. This code does not recurse into the subcomponents.  The callers of
   * this procedure need to do the recursion.
   * @param formName The name of the form that contains the component
   * @param formProperties a JSON description of the form
   * @param jsonToWalk a JSON description of this component
   * @param includeComments a boolean saying whether top include the block comments
   * in the code as Yail comments
   * @return the Yail code as a string
   */
  public static String getYailForComponentProperties(String formName,
      final JSONObject formProperties, JSONObject jsonToWalk, boolean includeComments) {
    StringBuilder yailCode = new StringBuilder();
    try {
      String componentName = jsonToWalk.getString("$Name");

      String genus = jsonToWalk.getString("$Type");
      if (genus.equals("Form")){
        // the Yail code for a form is different from other components.
        appendYailForFormProperties(formName, jsonToWalk, includeComments, yailCode);
      } else {
        // if this component is not itself a form, generate the
        // Yail code that adds the component to its parent, followed by
        // the code that sets each property of the component
        String parent = getComponentParent(componentName, formName, formProperties);
        String code = "";
        // add the component
        if (includeComments) {
          code += YAIL_COMMENT_MAJOR + componentName + YAIL_LINE_FEED;
        }
        yailCode
            .append(code)
            .append(YAIL_ADD_COMPONENT)
            .append(parent).append(YAIL_SPACER)
            .append(genus).append(YAIL_SPACER)
            .append(componentName).append(YAIL_SPACER);
        getYailForComponentPropertiesHelper(formName, jsonToWalk, yailCode, componentName);
        yailCode.append(YAIL_CLOSE_BLOCK);
      }
    } catch (JSONException e) {
      FeedbackReporter.showSystemErrorMessage("Error generating component Yail for source "
          + jsonToWalk);
    }
    return yailCode.toString();
  }

  // generate the Yail code for setting a form's properties.

  private static void appendYailForFormProperties(String formName,
      JSONObject jsonToWalk, Boolean includeComments, StringBuilder yailCode) {
    try {
      String name = jsonToWalk.getString("$Name");
      String genus = jsonToWalk.getString("$Type");
      if (includeComments) {
        yailCode.append(YAIL_COMMENT_MAJOR + name + YAIL_LINE_FEED);
      }
      StringBuilder yailForComponentProperties = new StringBuilder();
      getYailForComponentPropertiesHelper(formName, jsonToWalk, yailForComponentProperties, name);
      if (yailForComponentProperties.length() > 0) {
        yailCode.append(YAIL_DO_AFTER_FORM_CREATION)
            .append(yailForComponentProperties)
            .append(YAIL_CLOSE_BLOCK);
      }
    } catch (JSONException e) {
      FeedbackReporter.showSystemErrorMessage("Error generating component Yail for form "
          + jsonToWalk);
    }
  }

  private static void generatePropertySetterYailHelper(StringBuilder yailCode, String componentName,
      String componentType, String propertyName, String propertyValue) {
    yailCode
        .append(YAIL_SET_AND_COERCE_PROPERTY)
        .append(YAIL_QUOTE).append(componentName).append(YAIL_SPACER)
        .append(YAIL_QUOTE).append(propertyName).append(YAIL_SPACER);
    String type = getPropertyType(componentType, propertyName);
    String value = getPropertyValueYail(propertyValue, type);
    yailCode
        .append(value).append(YAIL_SPACER)
        .append(type)
        .append(YAIL_CLOSE_BLOCK);
  }

  private static void getYailForComponentPropertiesHelper(String formName,
      JSONObject jsonToWalk,
      StringBuilder yailCode,
      String componentName) throws JSONException {
    for (String key : sortPropertyNames(jsonToWalk.keys())) {
      if (!key.startsWith("$") && !key.equals("Uuid")) {
        generatePropertySetterYailHelper(yailCode, componentName, jsonToWalk.getString("$Type"),
            key, jsonToWalk.getString(key));
      }
    }
  }

  static void generatePropertySetterYail(StringBuilder yailCode, String componentName,
                                         String componentType, String propertyName,
                                         String propertyValue) {
    if (!propertyName.startsWith("$") && !propertyName.equals("Uuid")) {
      generatePropertySetterYailHelper(yailCode, componentName, componentType,
          propertyName, propertyValue);
    }
  }

  public static String generateYailClearForm() {
    return YAIL_CLEAR_FORM;
  }

  private static String generateComponentYail(String formName, JSONObject formProperties,
      JSONObject componentJson) {
    JSONArray components;
    StringBuilder componentYail = new StringBuilder();
    String componentYailStr;
    try {
      // generate the code for the component/form
      componentYailStr = YABlockCompiler.getYailForComponentProperties(formName,
          formProperties,
          componentJson,
          false);
      componentYail.append(componentYailStr);
      // generate the Yail for the component's children
      if (componentJson.has("$Components")) {
        components = componentJson.getJSONArray("$Components");
        for (int i = 0; i < components.length(); i++) {
          componentYailStr =
            generateComponentYail(formName, formProperties, components.getJSONObject(i));
          componentYail.append(componentYailStr);
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
      return "";
    }
    return componentYail.toString();
  }

  private static SortedSet<String> sortPropertyNames(Iterator<String> sourceIter) {
    SortedSet<String> sortedKeySet = new TreeSet<String>();

    while (sourceIter.hasNext()) {
      sortedKeySet.add(sourceIter.next());
    }

    return sortedKeySet;
  }

  private static String getYail(String name, ArrayList<RenderableBlock> componentBlocks) {
    StringBuilder code = new StringBuilder();
    for (int i = componentBlocks.size() - 1; i >= 0; --i) {
      RenderableBlock rb = componentBlocks.get(i);
      code.append(blockParser.genYail(rb.getBlock()));
    }
    return code.toString();
  }

  private static String getPropertyValueYail(String value, String type) {
    if (type.equals("'number")) {
      if (value.matches(INTEGER_REGEXP) || value.matches(FLONUM_REGEXP)) { // integer
        return value;
      } else if (value.matches(SIMPLE_HEX_PREFIX + "[0-9A-F]+")) { // hex
        return YAIL_HEX_PREFIX + value.substring(SIMPLE_HEX_PREFIX_LEN);
      }
    } else if (type.equals("'boolean")) {
      if (value.contains("False")) {
        return "#f";
      } else if (value.contains("True")) {
        return "#t";
      }
    } else if (type.equals("'component")) {
      if (value.equals("")) {
        return "\"\"";
      } else {
        return YAIL_GET_COMPONENT + value + ")";
      }
    }

    if (value.equals("") || value.equals("null")) {  // empty string
      return "\"\"";
    }
    return quotifyForREPL(value);
  }

  // Return the name of this component's parent.
  // This method requires that you send the Properties section of the JSON object for the entire
  // form that the component is on.
  private static String getComponentParent(String componentName, String formName,
      final JSONObject fullPropertiesObject) {
    if (fullPropertiesObject == null) {
      return formName;
    }
    try {
        return getComponentParentHelper(componentName, fullPropertiesObject, formName);
    } catch (JSONException e) {
      FeedbackReporter.showSystemErrorMessage("Error parsing JSON " +
          fullPropertiesObject.toString());
    }
    System.out.println("Made it through all components with no match, returning main form");
    return formName;
  }

  /**
   * Converts a path of the form
   * gibberish/src/com/gmail/username/project1/Form.extension into package
   * names of the form com.gmail.username.project1.Form
   * @param path the path to convert.
   * @return a dot separated package name.
   */
  public static String packageNameFromPath(String path) {
    path = path.replaceFirst(".*?/?src/", "");
    int extensionIndex = path.lastIndexOf(".");
    if (extensionIndex != -1) {
      return path.substring(0, extensionIndex).replaceAll("/", ".");
    }
    return path.replaceAll("/", ".");
  }

  // Helper for finding the parent of a given component
  private static String getComponentParentHelper(String componentName,
                                                 JSONObject sourceProperties, String parent)
                                                 throws JSONException {
    JSONArray components;
    String thisName = sourceProperties.getString("$Name");
    if (thisName.equals(componentName)) {
      return parent;
    }
    if (sourceProperties.has("$Components")) {
      components = sourceProperties.getJSONArray("$Components");
      for (int i = 0; i < components.length(); i++) {
        String theParent =
            getComponentParentHelper(componentName, components.getJSONObject(i), thisName);
        if (theParent != null) {
          return theParent;
        }
      }
    }
    // parent not in this branch
    return null;
  }

  private static String getPropertyType(String componentGenus, String property) {
    // TODO(user): remove 'value' hack
    HashMap<String, HashSet<String>> propertyRules =
        BlockRules.genusToPropertyRules.get(componentGenus);
    String type = "";
    // TODO(user): If we don't have a type for this property, we assume it is of type 'text.'
    // There should be a better reporting mechanism back to the user that the type is missing.
    if (propertyRules == null || propertyRules.get(property) == null) {
      FeedbackReporter.logError("No type found for property " +
                         property + " in genus " + componentGenus);
      return YAIL_QUOTE + "text";
    }
    // TODO(user): we currently only allow properties to have one type, yet they are
    // stored in a hashset. We should either store them as a pair, or create some mechanism
    // for a hierarchy of Codeblocks types.
    for (String propType : propertyRules.get(property)) {
      type = propType;
    }
    return YAIL_QUOTE + type;
  }

  public static String generateComponentRename(String oldName, String newName) {
    return YAIL_RENAME_COMPONENT + quotifyForREPL(oldName) + YAIL_SPACER
           + quotifyForREPL(newName) + YAIL_CLOSE_BLOCK;
  }

  public static String generateComponentRemoval(String name) {
    return YAIL_COMPONENT_REMOVE + quotifyForREPL(name) + YAIL_CLOSE_BLOCK;
  }

  // Transform a string to the Kawa input representation of the string, for sending to
  // the REPL, by using backslash to escape quotes and backslashes.
  // But do not escape a backslash if it is part of \n.
  // Then enclose the result in quotes.
  // TODO(halabelson): Extend this to a complete transformation that deals with
  // the full set of formatting characters.

  public static String quotifyForREPL(String s) {
    if (s == null) {
      return null;
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append('"');
      int len = s.length();
      int lastIndex = len - 1;
      for (int i = 0; i < len; i++) {
        char c = s.charAt(i);
        switch (c) {
          case '\\':
            // If this is \n don't slashify the backslash
            // TODO(user): Make this cleaner and more general
            if (!(i == lastIndex) && s.charAt(i + 1) == 'n') {
              sb.append(c);
              sb.append(s.charAt(i + 1));
              i = i + 1;
            } else {
            sb.append('\\').append(c);
            }
            break;
          case '"':
            sb.append('\\').append(c);
            break;
          default:
            if (c < ' ' || c > '~') {
              // Replace any special chars with \u1234 unicode
              String hex = "000" + Integer.toHexString(c);
              hex = hex.substring(hex.length() - 4);
              sb.append("\\u" + hex);
            } else {
              sb.append(c);
            }
            break;
        }
      }
      sb.append('"');
      return sb.toString();
    }
  }

}
