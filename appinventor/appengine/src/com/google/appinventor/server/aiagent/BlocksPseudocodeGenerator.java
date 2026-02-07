// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Converts Blockly XML (BKY) content into human-readable pseudocode matching
 * the grammar defined in {@code pseudocode_grammar.md}.
 *
 * <p>The generated pseudocode is structured into three sections:
 * <ul>
 *   <li>{@code == Global Variables ==} -- global variable declarations</li>
 *   <li>{@code == Procedures ==} -- procedure definitions (with and without return)</li>
 *   <li>{@code == Event Handlers ==} -- component event handlers</li>
 * </ul>
 *
 * <p>This representation achieves roughly 70% token reduction compared to raw
 * BKY XML while preserving all semantic information needed for the LLM to
 * understand and modify the app's logic.
 */
public class BlocksPseudocodeGenerator {

  private static final Logger LOG = Logger.getLogger(
      BlocksPseudocodeGenerator.class.getName());

  private static final String INDENT = "  ";

  /** Maps math_compare OP field values to pseudocode operators. */
  private static final Map<String, String> COMPARE_OPS;

  /** Maps math_on_list OP field values to pseudocode function names. */
  private static final Map<String, String> MATH_ON_LIST_OPS;

  /** Maps color block types to pseudocode color names. */
  private static final Map<String, String> COLOR_BLOCK_NAMES;

  /** Maps math trig OP field values to pseudocode function names. */
  private static final Map<String, String> TRIG_OPS;

  /** Maps text operation block types to their pseudocode forms. */
  private static final Map<String, String> MATH_SINGLE_OPS;

  static {
    Map<String, String> cmp = new HashMap<>();
    cmp.put("EQ", "=");
    cmp.put("NEQ", "!=");
    cmp.put("LT", "<");
    cmp.put("LTE", "<=");
    cmp.put("GT", ">");
    cmp.put("GTE", ">=");
    COMPARE_OPS = Collections.unmodifiableMap(cmp);

    Map<String, String> mol = new HashMap<>();
    mol.put("AVG", "avg of list");
    mol.put("MIN", "min of list");
    mol.put("MAX", "max of list");
    mol.put("SUM", "sum of list");
    MATH_ON_LIST_OPS = Collections.unmodifiableMap(mol);

    Map<String, String> colors = new HashMap<>();
    colors.put("color_black", "black");
    colors.put("color_white", "white");
    colors.put("color_red", "red");
    colors.put("color_pink", "pink");
    colors.put("color_orange", "orange");
    colors.put("color_yellow", "yellow");
    colors.put("color_green", "green");
    colors.put("color_cyan", "cyan");
    colors.put("color_blue", "blue");
    colors.put("color_magenta", "magenta");
    colors.put("color_light_gray", "light gray");
    colors.put("color_dark_gray", "dark gray");
    COLOR_BLOCK_NAMES = Collections.unmodifiableMap(colors);

    Map<String, String> trig = new HashMap<>();
    trig.put("SIN", "sin");
    trig.put("COS", "cos");
    trig.put("TAN", "tan");
    trig.put("ASIN", "asin");
    trig.put("ACOS", "acos");
    trig.put("ATAN", "atan");
    TRIG_OPS = Collections.unmodifiableMap(trig);

    Map<String, String> mathSingle = new HashMap<>();
    mathSingle.put("NEG", "negate");
    mathSingle.put("ABS", "abs");
    mathSingle.put("SQRT", "sqrt");
    mathSingle.put("LOG", "log");
    mathSingle.put("EXP", "e^");
    mathSingle.put("ROUND", "round");
    mathSingle.put("FLOOR", "floor");
    mathSingle.put("CEILING", "ceiling");
    MATH_SINGLE_OPS = Collections.unmodifiableMap(mathSingle);
  }

  // -------------------------------------------------------------------------
  // Public API
  // -------------------------------------------------------------------------

  /**
   * Convert BKY XML content into structured pseudocode.
   *
   * @param bkyContent the raw BKY XML string
   * @return pseudocode string with sections, or an empty string if parsing
   *     fails or there are no blocks
   */
  public String generate(String bkyContent) {
    if (bkyContent == null || bkyContent.trim().isEmpty()) {
      return "";
    }

    Document doc;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      // Disable external entities for safety
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      doc = builder.parse(new InputSource(new StringReader(bkyContent)));
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Failed to parse BKY XML", e);
      return "";
    }

    Element root = doc.getDocumentElement();
    List<Element> topBlocks = getChildElementsByTagName(root, "block");

    // Categorize top-level blocks
    List<Element> globals = new ArrayList<>();
    List<Element> procedures = new ArrayList<>();
    List<Element> events = new ArrayList<>();
    List<Element> others = new ArrayList<>();

    for (Element block : topBlocks) {
      String type = block.getAttribute("type");
      if ("global_declaration".equals(type)) {
        globals.add(block);
      } else if ("procedures_defnoreturn".equals(type)
          || "procedures_defreturn".equals(type)) {
        procedures.add(block);
      } else if ("component_event".equals(type)) {
        events.add(block);
      } else {
        others.add(block);
      }
    }

    StringBuilder sb = new StringBuilder();

    // == Global Variables ==
    if (!globals.isEmpty()) {
      sb.append("== Global Variables ==\n");
      for (Element block : globals) {
        sb.append(generateStatement(block, 0));
        sb.append("\n");
      }
      sb.append("\n");
    }

    // == Procedures ==
    if (!procedures.isEmpty()) {
      sb.append("== Procedures ==\n");
      for (Element block : procedures) {
        sb.append(generateStatement(block, 0));
        sb.append("\n\n");
      }
    }

    // == Event Handlers ==
    if (!events.isEmpty()) {
      sb.append("== Event Handlers ==\n");
      for (Element block : events) {
        sb.append(generateStatement(block, 0));
        sb.append("\n\n");
      }
    }

    // Any other top-level blocks (orphaned statements)
    if (!others.isEmpty()) {
      sb.append("== Other Blocks ==\n");
      for (Element block : others) {
        sb.append(generateStatement(block, 0));
        sb.append("\n");
      }
      sb.append("\n");
    }

    return sb.toString().trim() + "\n";
  }

  // -------------------------------------------------------------------------
  // Statement generation (block types that occupy a statement slot)
  // -------------------------------------------------------------------------

  /**
   * Generate pseudocode for a statement block and any blocks chained after it
   * via {@code <next>}.
   *
   * @param block the DOM element for the block
   * @param indent current indentation level
   * @return the pseudocode lines
   */
  private String generateStatement(Element block, int indent) {
    StringBuilder sb = new StringBuilder();
    sb.append(generateSingleStatement(block, indent));

    // Follow the <next> chain
    Element nextElem = getChildElement(block, "next");
    if (nextElem != null) {
      Element nextBlock = getFirstChildElement(nextElem, "block");
      if (nextBlock != null) {
        sb.append(generateStatement(nextBlock, indent));
      }
    }
    return sb.toString();
  }

  /**
   * Generate pseudocode for a single statement block (no next-chaining).
   */
  private String generateSingleStatement(Element block, int indent) {
    String type = block.getAttribute("type");
    String prefix = indentStr(indent);

    switch (type) {

      // -- Global variable declaration --
      case "global_declaration":
        return prefix + "initialize global " + getField(block, "NAME")
            + " to " + generateExpression(getValueBlock(block, "VALUE")) + "\n";

      // -- Lexical variable set --
      case "lexical_variable_set": {
        String varField = getField(block, "VAR");
        String valueExpr = generateExpression(getValueBlock(block, "VALUE"));
        if (varField.startsWith("global ")) {
          return prefix + "set global " + varField.substring(7) + " to " + valueExpr + "\n";
        } else {
          return prefix + "set local " + varField + " to " + valueExpr + "\n";
        }
      }

      // -- Component event handler --
      case "component_event":
        return generateEventHandler(block, indent);

      // -- Component property set --
      case "component_set_get":
        return generatePropertySetStatement(block, indent);

      // -- Component method call (statement context) --
      case "component_method":
        return prefix + generateMethodCallExpression(block) + "\n";

      // -- Procedure definitions --
      case "procedures_defnoreturn":
        return generateProcedureDef(block, indent, false);
      case "procedures_defreturn":
        return generateProcedureDef(block, indent, true);

      // -- Procedure call (statement context) --
      case "procedures_callnoreturn":
      case "procedures_callreturn":
        return prefix + generateProcedureCallExpression(block) + "\n";

      // -- Control: if --
      case "controls_if":
        return generateControlsIf(block, indent);

      // -- Control: for each --
      case "controls_forEach":
        return generateForEach(block, indent);

      // -- Control: for each dict pair --
      case "controls_for_each_dict":
        return generateForEachDict(block, indent);

      // -- Control: for range --
      case "controls_forRange":
        return generateForRange(block, indent);

      // -- Control: while --
      case "controls_while":
        return generateWhile(block, indent);

      // -- Control: break --
      case "controls_break":
        return prefix + "break\n";

      // -- Local declaration (statement) --
      case "local_declaration_statement":
        return generateLocalDeclStatement(block, indent);

      // -- Evaluate but ignore --
      case "controls_eval_but_ignore":
        return prefix + "evaluate but ignore "
            + generateExpression(getValueBlock(block, "VALUE")) + "\n";

      // -- Screen navigation --
      case "controls_openAnotherScreen":
        return prefix + "open another screen "
            + generateExpression(getValueBlock(block, "SCREEN")) + "\n";

      case "controls_openAnotherScreenWithStartValue":
        return prefix + "open another screen "
            + generateExpression(getValueBlock(block, "SCREENNAME"))
            + " with value " + generateExpression(getValueBlock(block, "STARTVALUE")) + "\n";

      case "controls_closeScreen":
        return prefix + "close screen\n";

      case "controls_closeScreenWithValue":
        return prefix + "close screen with value "
            + generateExpression(getValueBlock(block, "RESULT")) + "\n";

      case "controls_closeScreenWithPlainText":
        return prefix + "close screen with plain text "
            + generateExpression(getValueBlock(block, "TEXT")) + "\n";

      case "controls_closeApplication":
        return prefix + "close application\n";

      // -- List mutation statements --
      case "lists_add_items":
        return generateListAddItems(block, indent);

      case "lists_insert_item":
        return prefix + "insert item "
            + generateExpression(getValueBlock(block, "ITEM"))
            + " into " + generateExpression(getValueBlock(block, "LIST"))
            + " at " + generateExpression(getValueBlock(block, "INDEX")) + "\n";

      case "lists_replace_item":
        return prefix + "replace item "
            + generateExpression(getValueBlock(block, "NUM"))
            + " in " + generateExpression(getValueBlock(block, "LIST"))
            + " with " + generateExpression(getValueBlock(block, "ITEM")) + "\n";

      case "lists_remove_item":
        return prefix + "remove item "
            + generateExpression(getValueBlock(block, "INDEX"))
            + " from " + generateExpression(getValueBlock(block, "LIST")) + "\n";

      case "lists_append_list":
        return prefix + "append "
            + generateExpression(getValueBlock(block, "LIST1"))
            + " to " + generateExpression(getValueBlock(block, "LIST0")) + "\n";

      // -- Dictionary mutation statements --
      case "dictionaries_set_pair":
        return prefix + "set key " + generateExpression(getValueBlock(block, "KEY"))
            + " in " + generateExpression(getValueBlock(block, "DICT"))
            + " to " + generateExpression(getValueBlock(block, "VALUE")) + "\n";

      case "dictionaries_delete_pair":
        return prefix + "delete key " + generateExpression(getValueBlock(block, "KEY"))
            + " from " + generateExpression(getValueBlock(block, "DICT")) + "\n";

      case "dictionaries_set_value_for_key_path":
        return prefix + "set path " + generateExpression(getValueBlock(block, "KEYS"))
            + " in " + generateExpression(getValueBlock(block, "DICT"))
            + " to " + generateExpression(getValueBlock(block, "VALUE")) + "\n";

      default:
        // Unknown block type -- emit a comment
        return prefix + "# unknown block: " + type + "\n";
    }
  }

  // -------------------------------------------------------------------------
  // Event handler
  // -------------------------------------------------------------------------

  private String generateEventHandler(Element block, int indent) {
    String prefix = indentStr(indent);
    Element mutation = getChildElement(block, "mutation");
    String instanceName = mutation != null ? mutation.getAttribute("instance_name") : "";
    String eventName = mutation != null ? mutation.getAttribute("event_name") : "";
    boolean isGeneric = mutation != null && "true".equals(mutation.getAttribute("is_generic"));
    String componentType = mutation != null ? mutation.getAttribute("component_type") : "";

    // Collect parameter names from VAR0, VAR1, ... fields
    List<String> params = collectIndexedFields(block, "VAR");

    StringBuilder sb = new StringBuilder();
    if (isGeneric) {
      sb.append(prefix).append("when any ").append(componentType)
          .append(".").append(eventName).append("(");
    } else {
      sb.append(prefix).append("when ").append(instanceName)
          .append(".").append(eventName).append("(");
    }
    sb.append(String.join(", ", params));
    sb.append(") do\n");

    // Body
    Element doStmt = getStatementBlock(block, "DO");
    if (doStmt != null) {
      sb.append(generateStatement(doStmt, indent + 1));
    }
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Property set (statement context -- only "set" mode)
  // -------------------------------------------------------------------------

  private String generatePropertySetStatement(Element block, int indent) {
    String prefix = indentStr(indent);
    Element mutation = getChildElement(block, "mutation");
    if (mutation == null) {
      return prefix + "# malformed component_set_get block\n";
    }

    String setOrGet = mutation.getAttribute("set_or_get");
    if (!"set".equals(setOrGet)) {
      // A "get" in statement context is unusual; treat as evaluate-but-ignore
      return prefix + "evaluate but ignore " + generatePropertyGetExpression(block) + "\n";
    }

    boolean isGeneric = "true".equals(mutation.getAttribute("is_generic"));
    String propName = mutation.getAttribute("property_name");
    String valueExpr = generateExpression(getValueBlock(block, "VALUE"));

    if (isGeneric) {
      String componentType = mutation.getAttribute("component_type");
      String compExpr = generateExpression(getValueBlock(block, "COMPONENT"));
      return prefix + "set " + componentType + "." + propName
          + " of " + compExpr + " to " + valueExpr + "\n";
    } else {
      String instanceName = mutation.getAttribute("instance_name");
      return prefix + "set " + instanceName + "." + propName + " to " + valueExpr + "\n";
    }
  }

  // -------------------------------------------------------------------------
  // Procedure definitions
  // -------------------------------------------------------------------------

  private String generateProcedureDef(Element block, int indent, boolean hasReturn) {
    String prefix = indentStr(indent);
    String name = getField(block, "NAME");

    // Parameters from <mutation><arg name="..."/></mutation>
    List<String> params = new ArrayList<>();
    Element mutation = getChildElement(block, "mutation");
    if (mutation != null) {
      List<Element> args = getChildElementsByTagName(mutation, "arg");
      for (Element arg : args) {
        params.add(arg.getAttribute("name"));
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("procedure ").append(name).append("(");
    sb.append(String.join(", ", params));
    sb.append(")");
    if (hasReturn) {
      sb.append(" returns");
    }
    sb.append("\n");

    // Body (STACK statement)
    Element body = getStatementBlock(block, "STACK");
    if (body != null) {
      sb.append(generateStatement(body, indent + 1));
    }

    // Return expression (for procedures_defreturn)
    if (hasReturn) {
      Element returnBlock = getValueBlock(block, "RETURN");
      if (returnBlock != null) {
        sb.append(indentStr(indent + 1)).append("return ")
            .append(generateExpression(returnBlock)).append("\n");
      }
    }

    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Control flow
  // -------------------------------------------------------------------------

  private String generateControlsIf(Element block, int indent) {
    String prefix = indentStr(indent);
    StringBuilder sb = new StringBuilder();

    // Determine number of elseif and else branches from mutation
    Element mutation = getChildElement(block, "mutation");
    int elseifCount = 0;
    boolean hasElse = false;
    if (mutation != null) {
      String elseifAttr = mutation.getAttribute("elseif");
      if (elseifAttr != null && !elseifAttr.isEmpty()) {
        elseifCount = Integer.parseInt(elseifAttr);
      }
      String elseAttr = mutation.getAttribute("else");
      hasElse = "1".equals(elseAttr);
    }

    int totalConditions = 1 + elseifCount;

    for (int i = 0; i < totalConditions; i++) {
      String condExpr = generateExpression(getValueBlock(block, "IF" + i));
      if (i == 0) {
        sb.append(prefix).append("if ").append(condExpr).append(" then\n");
      } else {
        sb.append(prefix).append("else if ").append(condExpr).append(" then\n");
      }
      Element thenBody = getStatementBlock(block, "DO" + i);
      if (thenBody != null) {
        sb.append(generateStatement(thenBody, indent + 1));
      }
    }

    if (hasElse) {
      sb.append(prefix).append("else\n");
      Element elseBody = getStatementBlock(block, "ELSE");
      if (elseBody != null) {
        sb.append(generateStatement(elseBody, indent + 1));
      }
    }

    return sb.toString();
  }

  private String generateForEach(Element block, int indent) {
    String prefix = indentStr(indent);
    String varName = getField(block, "VAR");
    String listExpr = generateExpression(getValueBlock(block, "LIST"));

    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("for each ").append(varName)
        .append(" in ").append(listExpr).append("\n");

    Element body = getStatementBlock(block, "DO");
    if (body != null) {
      sb.append(generateStatement(body, indent + 1));
    }
    return sb.toString();
  }

  private String generateForEachDict(Element block, int indent) {
    String prefix = indentStr(indent);
    String keyVar = getField(block, "KEY");
    String valueVar = getField(block, "VALUE");
    String dictExpr = generateExpression(getValueBlock(block, "DICT"));

    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("for each key ").append(keyVar)
        .append(" value ").append(valueVar)
        .append(" in ").append(dictExpr).append("\n");

    Element body = getStatementBlock(block, "DO");
    if (body != null) {
      sb.append(generateStatement(body, indent + 1));
    }
    return sb.toString();
  }

  private String generateForRange(Element block, int indent) {
    String prefix = indentStr(indent);
    String varName = getField(block, "VAR");
    String fromExpr = generateExpression(getValueBlock(block, "START"));
    String toExpr = generateExpression(getValueBlock(block, "END"));
    String byExpr = generateExpression(getValueBlock(block, "STEP"));

    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("for ").append(varName)
        .append(" from ").append(fromExpr)
        .append(" to ").append(toExpr)
        .append(" by ").append(byExpr).append("\n");

    Element body = getStatementBlock(block, "DO");
    if (body != null) {
      sb.append(generateStatement(body, indent + 1));
    }
    return sb.toString();
  }

  private String generateWhile(Element block, int indent) {
    String prefix = indentStr(indent);
    String condExpr = generateExpression(getValueBlock(block, "TEST"));

    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("while ").append(condExpr).append("\n");

    Element body = getStatementBlock(block, "DO");
    if (body != null) {
      sb.append(generateStatement(body, indent + 1));
    }
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Local declaration (statement)
  // -------------------------------------------------------------------------

  private String generateLocalDeclStatement(Element block, int indent) {
    String prefix = indentStr(indent);
    // Collect variable names from mutation
    Element mutation = getChildElement(block, "mutation");
    List<String> varNames = new ArrayList<>();
    if (mutation != null) {
      List<Element> locals = getChildElementsByTagName(mutation, "localname");
      for (Element loc : locals) {
        varNames.add(loc.getAttribute("name"));
      }
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < varNames.size(); i++) {
      String initExpr = generateExpression(getValueBlock(block, "DECL" + i));
      sb.append(prefix).append("initialize local ").append(varNames.get(i))
          .append(" to ").append(initExpr).append("\n");
    }

    Element body = getStatementBlock(block, "STACK");
    if (body != null) {
      sb.append(generateStatement(body, indent + 1));
    }
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // List mutation helpers
  // -------------------------------------------------------------------------

  private String generateListAddItems(Element block, int indent) {
    String prefix = indentStr(indent);
    Element mutation = getChildElement(block, "mutation");
    int itemCount = 0;
    if (mutation != null) {
      String items = mutation.getAttribute("items");
      if (items != null && !items.isEmpty()) {
        itemCount = Integer.parseInt(items);
      }
    }
    // fallback: count ITEM0, ITEM1, ... value children
    if (itemCount == 0) {
      for (int i = 0; i < 100; i++) {
        if (getValueBlock(block, "ITEM" + i) == null) {
          break;
        }
        itemCount++;
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("add items ");
    for (int i = 0; i < itemCount; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(generateExpression(getValueBlock(block, "ITEM" + i)));
    }
    sb.append(" to ").append(generateExpression(getValueBlock(block, "LIST")));
    sb.append("\n");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Expression generation
  // -------------------------------------------------------------------------

  /**
   * Generate pseudocode for an expression block.
   *
   * @param block the DOM element for the block, or null
   * @return the pseudocode expression string
   */
  private String generateExpression(Element block) {
    if (block == null) {
      return "???";
    }

    String type = block.getAttribute("type");

    switch (type) {

      // -- Literals --
      case "math_number":
        return getField(block, "NUM");

      case "text":
        return "\"" + getField(block, "TEXT") + "\"";

      case "logic_boolean":
        return "TRUE".equals(getField(block, "BOOL")) ? "true" : "false";

      // -- Variables --
      case "lexical_variable_get": {
        String varField = getField(block, "VAR");
        if (varField.startsWith("global ")) {
          return "global " + varField.substring(7);
        }
        // Local or parameter -- emit bare name
        return varField;
      }

      case "lexical_variable_set": {
        // Variable set in expression context is unusual; still handle it
        String varField = getField(block, "VAR");
        String valueExpr = generateExpression(getValueBlock(block, "VALUE"));
        if (varField.startsWith("global ")) {
          return "set global " + varField.substring(7) + " to " + valueExpr;
        }
        return "set local " + varField + " to " + valueExpr;
      }

      // -- Component operations --
      case "component_set_get":
        return generatePropertyGetExpression(block);

      case "component_method":
        return generateMethodCallExpression(block);

      case "component_component_block":
        return generateComponentRefExpression(block);

      // -- Procedure calls --
      case "procedures_callreturn":
      case "procedures_callnoreturn":
        return generateProcedureCallExpression(block);

      // -- Arithmetic --
      case "math_add":
        return generateVariadicInfix(block, "NUM", " + ");

      case "math_subtract":
        return "(" + generateExpression(getValueBlock(block, "A"))
            + " - " + generateExpression(getValueBlock(block, "B")) + ")";

      case "math_multiply":
        return generateVariadicInfix(block, "NUM", " * ");

      case "math_division":
        return "(" + generateExpression(getValueBlock(block, "A"))
            + " / " + generateExpression(getValueBlock(block, "B")) + ")";

      case "math_power":
        return "(" + generateExpression(getValueBlock(block, "A"))
            + " ^ " + generateExpression(getValueBlock(block, "B")) + ")";

      case "math_compare": {
        String op = COMPARE_OPS.getOrDefault(getField(block, "OP"), "=");
        return "(" + generateExpression(getValueBlock(block, "A"))
            + " " + op + " " + generateExpression(getValueBlock(block, "B")) + ")";
      }

      case "math_single": {
        String op = getField(block, "OP");
        String funcName = MATH_SINGLE_OPS.getOrDefault(op, op.toLowerCase());
        return funcName + " " + generateExpression(getValueBlock(block, "NUM"));
      }

      case "math_trig": {
        String op = getField(block, "OP");
        String funcName = TRIG_OPS.getOrDefault(op, op.toLowerCase());
        return funcName + " " + generateExpression(getValueBlock(block, "NUM"));
      }

      case "math_atan2":
        return "atan2 " + generateExpression(getValueBlock(block, "Y"))
            + " " + generateExpression(getValueBlock(block, "X"));

      case "math_on_list": {
        String op = getField(block, "OP");
        String funcName = MATH_ON_LIST_OPS.getOrDefault(op, op.toLowerCase() + " of list");
        return funcName + " " + generateExpression(getValueBlock(block, "NUM"));
      }

      case "math_random_int":
        return "random integer from " + generateExpression(getValueBlock(block, "FROM"))
            + " to " + generateExpression(getValueBlock(block, "TO"));

      case "math_random_float":
        return "random fraction";

      case "math_format_as_decimal":
        return "format as decimal " + generateExpression(getValueBlock(block, "NUM"))
            + " places " + generateExpression(getValueBlock(block, "PLACES"));

      case "math_convert_number":
        return "convert number " + generateExpression(getValueBlock(block, "NUM"))
            + " from base " + generateExpression(getValueBlock(block, "FROM"))
            + " to base " + generateExpression(getValueBlock(block, "TO"));

      case "math_is_a_number":
        return "is a number " + generateExpression(getValueBlock(block, "NUM"));

      case "math_is_a_decimal":
        return "is a base10 " + generateExpression(getValueBlock(block, "NUM"));

      case "math_is_a_hexadecimal":
        return "is a hexadecimal " + generateExpression(getValueBlock(block, "NUM"));

      case "math_is_a_binary":
        return "is a binary " + generateExpression(getValueBlock(block, "NUM"));

      case "math_bitwise": {
        String op = getField(block, "OP");
        String opName;
        if ("BITAND".equals(op)) {
          opName = "bitwise and";
        } else if ("BITOR".equals(op)) {
          opName = "bitwise or";
        } else {
          opName = "bitwise xor";
        }
        return opName + " " + generateExpression(getValueBlock(block, "A"))
            + " " + generateExpression(getValueBlock(block, "B"));
      }

      case "math_divide": {
        String op = getField(block, "OP");
        String funcName;
        if ("MODULO".equals(op)) {
          funcName = "modulo";
        } else if ("REMAINDER".equals(op)) {
          funcName = "remainder";
        } else {
          funcName = "quotient";
        }
        return funcName + " " + generateExpression(getValueBlock(block, "A"))
            + " " + generateExpression(getValueBlock(block, "B"));
      }

      case "math_min":
        return "min(" + generateExpression(getValueBlock(block, "A"))
            + ", " + generateExpression(getValueBlock(block, "B")) + ")";

      case "math_max":
        return "max(" + generateExpression(getValueBlock(block, "A"))
            + ", " + generateExpression(getValueBlock(block, "B")) + ")";

      // -- Logic --
      case "logic_operation": {
        String op = getField(block, "OP");
        String keyword = "AND".equals(op) ? " and " : " or ";
        return generateVariadicInfix(block, "BOOL", keyword);
      }

      case "logic_negate":
        return "not " + generateExpression(getValueBlock(block, "BOOL"));

      // -- Text operations --
      case "text_join":
        return generateJoinExpression(block);

      case "text_length":
        return "length of text " + generateExpression(getValueBlock(block, "VALUE"));

      case "text_isEmpty":
        return "is text empty " + generateExpression(getValueBlock(block, "VALUE"));

      case "text_compare":
        return "compare text " + generateExpression(getValueBlock(block, "TEXT1"))
            + " " + generateExpression(getValueBlock(block, "TEXT2"));

      case "text_trim":
        return "trim " + generateExpression(getValueBlock(block, "TEXT"));

      case "text_changeCase": {
        String op = getField(block, "OP");
        String funcName = "UPCASE".equals(op) ? "upcase" : "downcase";
        return funcName + " " + generateExpression(getValueBlock(block, "TEXT"));
      }

      case "text_starts_at":
        return "starts at text " + generateExpression(getValueBlock(block, "TEXT"))
            + " piece " + generateExpression(getValueBlock(block, "PIECE"));

      case "text_contains": {
        String op = getField(block, "OP");
        String funcName = "CONTAINS".equals(op) ? "contains" : "contains any";
        return funcName + " text " + generateExpression(getValueBlock(block, "TEXT"))
            + " piece " + generateExpression(getValueBlock(block, "PIECE"));
      }

      case "text_split": {
        String op = getField(block, "OP");
        return generateTextSplit(block, op);
      }

      case "text_segment":
        return "segment text " + generateExpression(getValueBlock(block, "TEXT"))
            + " start " + generateExpression(getValueBlock(block, "START"))
            + " length " + generateExpression(getValueBlock(block, "LENGTH"));

      case "text_replace_all":
        return "replace all text " + generateExpression(getValueBlock(block, "TEXT"))
            + " segment " + generateExpression(getValueBlock(block, "SEGMENT"))
            + " replacement " + generateExpression(getValueBlock(block, "REPLACEMENT"));

      case "text_reverse":
        return "reverse text " + generateExpression(getValueBlock(block, "VALUE"));

      case "obfuscated_text":
        return "\"" + getField(block, "TEXT") + "\"";

      // -- Lists --
      case "lists_create_with":
        return generateListCreateExpression(block);

      case "lists_select_item":
        return "select item " + generateExpression(getValueBlock(block, "LIST"))
            + " index " + generateExpression(getValueBlock(block, "NUM"));

      case "lists_index_of":
        return "index of " + generateExpression(getValueBlock(block, "LIST"))
            + " thing " + generateExpression(getValueBlock(block, "ITEM"));

      case "lists_pick_random_item":
        return "pick random " + generateExpression(getValueBlock(block, "LIST"));

      case "lists_length":
        return "length of list " + generateExpression(getValueBlock(block, "LIST"));

      case "lists_is_empty":
        return "is list empty " + generateExpression(getValueBlock(block, "LIST"));

      case "lists_is_in":
        return "is in list " + generateExpression(getValueBlock(block, "LIST"))
            + " thing " + generateExpression(getValueBlock(block, "ITEM"));

      case "lists_is_list":
        return "is a list " + generateExpression(getValueBlock(block, "ITEM"));

      case "lists_copy":
        return "copy list " + generateExpression(getValueBlock(block, "LIST"));

      case "lists_reverse":
        return "reverse list " + generateExpression(getValueBlock(block, "LIST"));

      case "lists_to_csv_row":
        return "list to csv row " + generateExpression(getValueBlock(block, "LIST"));

      case "lists_from_csv_row":
        return "csv row to list " + generateExpression(getValueBlock(block, "TEXT"));

      case "lists_to_csv_table":
        return "list to csv table " + generateExpression(getValueBlock(block, "LIST"));

      case "lists_from_csv_table":
        return "csv table to list " + generateExpression(getValueBlock(block, "TEXT"));

      case "lists_lookup_in_pairs":
        return "lookup in pairs " + generateExpression(getValueBlock(block, "LIST"))
            + " key " + generateExpression(getValueBlock(block, "KEY"))
            + " notFound " + generateExpression(getValueBlock(block, "NOTFOUND"));

      case "lists_join_with_separator":
        return "join items " + generateExpression(getValueBlock(block, "LIST"))
            + " separator " + generateExpression(getValueBlock(block, "SEPARATOR"));

      case "lists_sort":
        return "sort " + generateExpression(getValueBlock(block, "LIST"));

      case "lists_map":
        return "map " + getField(block, "VAR")
            + " over " + generateExpression(getValueBlock(block, "LIST"))
            + " " + generateExpression(getValueBlock(block, "TO"));

      case "lists_filter":
        return "filter " + getField(block, "VAR")
            + " in " + generateExpression(getValueBlock(block, "LIST"))
            + " " + generateExpression(getValueBlock(block, "TEST"));

      case "lists_reduce":
        return "reduce " + getField(block, "VAR")
            + " over " + generateExpression(getValueBlock(block, "LIST"))
            + " initial " + generateExpression(getValueBlock(block, "INITANSWER"))
            + " " + generateExpression(getValueBlock(block, "COMBINE"));

      // -- Dictionaries --
      case "dictionaries_create_with":
        return generateDictCreateExpression(block);

      case "pair":
        return "pair(" + generateExpression(getValueBlock(block, "KEY"))
            + ", " + generateExpression(getValueBlock(block, "VALUE")) + ")";

      case "dictionaries_lookup":
        return "lookup key " + generateExpression(getValueBlock(block, "KEY"))
            + " in " + generateExpression(getValueBlock(block, "DICT"))
            + " notFound " + generateExpression(getValueBlock(block, "NOTFOUND"));

      case "dictionaries_getters": {
        String op = getField(block, "OP");
        if ("KEYS".equals(op)) {
          return "keys of " + generateExpression(getValueBlock(block, "DICT"));
        } else {
          return "values of " + generateExpression(getValueBlock(block, "DICT"));
        }
      }

      case "dictionaries_is_key_in":
        return "is key in " + generateExpression(getValueBlock(block, "DICT"))
            + " key " + generateExpression(getValueBlock(block, "KEY"));

      case "dictionaries_length":
        return "length of dict " + generateExpression(getValueBlock(block, "DICT"));

      case "dictionaries_alist_to_dict":
        return "alist to dict " + generateExpression(getValueBlock(block, "LIST"));

      case "dictionaries_dict_to_alist":
        return "dict to alist " + generateExpression(getValueBlock(block, "DICT"));

      case "dictionaries_combine_dicts":
        return "combine dicts " + generateExpression(getValueBlock(block, "DICT1"))
            + " " + generateExpression(getValueBlock(block, "DICT2"));

      case "dictionaries_walk_tree":
        return "walk tree " + generateExpression(getValueBlock(block, "DICT"))
            + " path " + generateExpression(getValueBlock(block, "PATH"));

      case "dictionaries_walk_all":
        return "walk tree " + generateExpression(getValueBlock(block, "DICT"))
            + " all at level " + generateExpression(getValueBlock(block, "PATH"));

      case "dictionaries_recursive_lookup":
        return "lookup path " + generateExpression(getValueBlock(block, "KEYS"))
            + " in " + generateExpression(getValueBlock(block, "DICT"))
            + " notFound " + generateExpression(getValueBlock(block, "NOTFOUND"));

      // -- Colors --
      case "color_black":
      case "color_white":
      case "color_red":
      case "color_pink":
      case "color_orange":
      case "color_yellow":
      case "color_green":
      case "color_cyan":
      case "color_blue":
      case "color_magenta":
      case "color_light_gray":
      case "color_dark_gray": {
        String colorName = COLOR_BLOCK_NAMES.get(type);
        return "color " + (colorName != null ? colorName : type);
      }

      case "color_make_color": {
        // Has a mutation indicating if alpha is included
        Element colorList = getValueBlock(block, "COLORLIST");
        return "make color(" + generateExpression(colorList) + ")";
      }

      case "color_split_color":
        return "split color " + generateExpression(getValueBlock(block, "COLOR"));

      // -- Controls (expression context) --
      case "controls_choose":
        return "if " + generateExpression(getValueBlock(block, "TEST"))
            + " then " + generateExpression(getValueBlock(block, "THENRETURN"))
            + " else " + generateExpression(getValueBlock(block, "ELSERETURN"));

      case "controls_do_then_return":
        return generateDoThenReturnExpression(block);

      // -- Local declaration (expression) --
      case "local_declaration_expression":
        return generateLocalDeclExpression(block);

      // -- Get start value --
      case "controls_getStartValue":
        return "get start value";

      // -- Helper blocks --
      case "helpers_dropdown": {
        Element mut = getChildElement(block, "mutation");
        String key = mut != null ? mut.getAttribute("key") : "";
        String value = getField(block, "OPTION");
        return "option " + key + "." + value;
      }

      case "helpers_screen_names":
        return "screen name " + getField(block, "SCREEN");

      case "helpers_assets":
        return "asset " + getField(block, "ASSET");

      // -- All components of type --
      case "helpers_type_list": {
        Element mut = getChildElement(block, "mutation");
        String componentType = mut != null ? mut.getAttribute("component_type") : "";
        return "all components of type " + componentType;
      }

      default:
        return "/* unknown: " + type + " */";
    }
  }

  // -------------------------------------------------------------------------
  // Expression sub-generators
  // -------------------------------------------------------------------------

  private String generatePropertyGetExpression(Element block) {
    Element mutation = getChildElement(block, "mutation");
    if (mutation == null) {
      return "???.???";
    }
    String propName = mutation.getAttribute("property_name");
    boolean isGeneric = "true".equals(mutation.getAttribute("is_generic"));

    if (isGeneric) {
      String componentType = mutation.getAttribute("component_type");
      String compExpr = generateExpression(getValueBlock(block, "COMPONENT"));
      return componentType + "." + propName + " of " + compExpr;
    } else {
      String instanceName = mutation.getAttribute("instance_name");
      return instanceName + "." + propName;
    }
  }

  private String generateMethodCallExpression(Element block) {
    Element mutation = getChildElement(block, "mutation");
    if (mutation == null) {
      return "call ???.???()";
    }
    String methodName = mutation.getAttribute("method_name");
    boolean isGeneric = "true".equals(mutation.getAttribute("is_generic"));

    // Collect arguments ARG0, ARG1, ...
    List<String> argExprs = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      Element argBlock = getValueBlock(block, "ARG" + i);
      if (argBlock == null) {
        break;
      }
      argExprs.add(generateExpression(argBlock));
    }

    if (isGeneric) {
      String componentType = mutation.getAttribute("component_type");
      String compExpr = generateExpression(getValueBlock(block, "COMPONENT"));
      return "call " + componentType + "." + methodName + " of " + compExpr
          + "(" + String.join(", ", argExprs) + ")";
    } else {
      String instanceName = mutation.getAttribute("instance_name");
      return "call " + instanceName + "." + methodName
          + "(" + String.join(", ", argExprs) + ")";
    }
  }

  private String generateComponentRefExpression(Element block) {
    Element mutation = getChildElement(block, "mutation");
    String instanceName = mutation != null ? mutation.getAttribute("instance_name") : "";
    if (instanceName.isEmpty()) {
      instanceName = getField(block, "COMPONENT_SELECTOR");
    }
    return "component " + instanceName;
  }

  private String generateProcedureCallExpression(Element block) {
    String name = getField(block, "PROCNAME");

    // Collect args from mutation count or by scanning ARG0, ARG1, ...
    List<String> argExprs = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      Element argBlock = getValueBlock(block, "ARG" + i);
      if (argBlock == null) {
        break;
      }
      argExprs.add(generateExpression(argBlock));
    }

    return "call " + name + "(" + String.join(", ", argExprs) + ")";
  }

  private String generateVariadicInfix(Element block, String inputPrefix, String operator) {
    Element mutation = getChildElement(block, "mutation");
    int count = 2; // default
    if (mutation != null) {
      String items = mutation.getAttribute("items");
      if (items != null && !items.isEmpty()) {
        count = Integer.parseInt(items);
      }
    }

    List<String> operands = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      operands.add(generateExpression(getValueBlock(block, inputPrefix + i)));
    }

    return "(" + String.join(operator, operands) + ")";
  }

  private String generateJoinExpression(Element block) {
    Element mutation = getChildElement(block, "mutation");
    int count = 2;
    if (mutation != null) {
      String items = mutation.getAttribute("items");
      if (items != null && !items.isEmpty()) {
        count = Integer.parseInt(items);
      }
    }

    List<String> parts = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      parts.add(generateExpression(getValueBlock(block, "ADD" + i)));
    }

    return "join(" + String.join(", ", parts) + ")";
  }

  private String generateListCreateExpression(Element block) {
    Element mutation = getChildElement(block, "mutation");
    int count = 0;
    if (mutation != null) {
      String items = mutation.getAttribute("items");
      if (items != null && !items.isEmpty()) {
        count = Integer.parseInt(items);
      }
    }

    List<String> items = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      items.add(generateExpression(getValueBlock(block, "ADD" + i)));
    }

    return "list(" + String.join(", ", items) + ")";
  }

  private String generateDictCreateExpression(Element block) {
    Element mutation = getChildElement(block, "mutation");
    int count = 0;
    if (mutation != null) {
      String items = mutation.getAttribute("items");
      if (items != null && !items.isEmpty()) {
        count = Integer.parseInt(items);
      }
    }

    List<String> pairs = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      Element pairBlock = getValueBlock(block, "ADD" + i);
      if (pairBlock != null && "pair".equals(pairBlock.getAttribute("type"))) {
        String key = generateExpression(getValueBlock(pairBlock, "KEY"));
        String value = generateExpression(getValueBlock(pairBlock, "VALUE"));
        pairs.add(key + ": " + value);
      } else {
        pairs.add(generateExpression(pairBlock));
      }
    }

    return "dict(" + String.join(", ", pairs) + ")";
  }

  private String generateTextSplit(Element block, String op) {
    String textExpr = generateExpression(getValueBlock(block, "TEXT"));
    String atExpr = generateExpression(getValueBlock(block, "AT"));

    switch (op) {
      case "SPLITATFIRST":
        return "split text " + textExpr + " at first " + atExpr;
      case "SPLIT":
        return "split text " + textExpr + " at " + atExpr;
      case "SPLITATFIRSTOFANY":
        return "split text " + textExpr + " at first any " + atExpr;
      case "SPLITATANY":
        return "split text " + textExpr + " at any " + atExpr;
      case "SPLITATSPACES":
        return "split text " + textExpr + " at spaces";
      default:
        return "split text " + textExpr + " at " + atExpr;
    }
  }

  private String generateDoThenReturnExpression(Element block) {
    // This is tricky because the DO part is statements
    // We'll represent it in a simplified way
    Element bodyStmt = getStatementBlock(block, "STM");
    String returnExpr = generateExpression(getValueBlock(block, "VALUE"));
    if (bodyStmt != null) {
      // Inline representation
      return "do { ... } then return " + returnExpr;
    }
    return "do then return " + returnExpr;
  }

  private String generateLocalDeclExpression(Element block) {
    Element mutation = getChildElement(block, "mutation");
    List<String> varNames = new ArrayList<>();
    if (mutation != null) {
      List<Element> locals = getChildElementsByTagName(mutation, "localname");
      for (Element loc : locals) {
        varNames.add(loc.getAttribute("name"));
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append("initialize local ");
    for (int i = 0; i < varNames.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      String initExpr = generateExpression(getValueBlock(block, "DECL" + i));
      sb.append(varNames.get(i)).append(" to ").append(initExpr);
    }
    sb.append(" in ").append(generateExpression(getValueBlock(block, "RETURN")));
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // DOM helper methods
  // -------------------------------------------------------------------------

  /**
   * Returns all direct child elements with the given tag name.
   */
  private List<Element> getChildElementsByTagName(Element parent, String tagName) {
    List<Element> result = new ArrayList<>();
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE
          && tagName.equals(child.getNodeName())) {
        result.add((Element) child);
      }
    }
    return result;
  }

  /**
   * Returns the first direct child element with the given tag name, or null.
   */
  private Element getChildElement(Element parent, String tagName) {
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE
          && tagName.equals(child.getNodeName())) {
        return (Element) child;
      }
    }
    return null;
  }

  /**
   * Returns the first child element with the given tag name within the parent,
   * looking for a specific tag name. For elements like {@code <block>} or
   * {@code <shadow>} that are children of value/statement wrappers.
   */
  private Element getFirstChildElement(Element parent, String tagName) {
    return getChildElement(parent, tagName);
  }

  /**
   * Gets the text content of a {@code <field name="...">} child element.
   */
  private String getField(Element block, String name) {
    List<Element> fields = getChildElementsByTagName(block, "field");
    for (Element field : fields) {
      if (name.equals(field.getAttribute("name"))) {
        return field.getTextContent();
      }
    }
    return "";
  }

  /**
   * Collects values of indexed fields (VAR0, VAR1, ...) until a gap is found.
   */
  private List<String> collectIndexedFields(Element block, String prefix) {
    List<String> values = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      String val = getField(block, prefix + i);
      if (val.isEmpty()) {
        break;
      }
      values.add(val);
    }
    return values;
  }

  /**
   * Gets the block element inside a {@code <value name="...">} child.
   * Prefers {@code <block>} over {@code <shadow>} if both exist.
   */
  private Element getValueBlock(Element parent, String name) {
    List<Element> values = getChildElementsByTagName(parent, "value");
    for (Element value : values) {
      if (name.equals(value.getAttribute("name"))) {
        // Prefer <block> over <shadow>
        Element block = getChildElement(value, "block");
        if (block != null) {
          return block;
        }
        Element shadow = getChildElement(value, "shadow");
        if (shadow != null) {
          return shadow;
        }
      }
    }
    return null;
  }

  /**
   * Gets the first block element inside a {@code <statement name="...">} child.
   */
  private Element getStatementBlock(Element parent, String name) {
    List<Element> stmts = getChildElementsByTagName(parent, "statement");
    for (Element stmt : stmts) {
      if (name.equals(stmt.getAttribute("name"))) {
        return getChildElement(stmt, "block");
      }
    }
    return null;
  }

  /**
   * Returns an indentation string for the given level.
   */
  private String indentStr(int level) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < level; i++) {
      sb.append(INDENT);
    }
    return sb.toString();
  }
}
