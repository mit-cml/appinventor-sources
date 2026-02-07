// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts structured block descriptions (from PseudocodeParser AST) into valid
 * Blockly XML strings suitable for {@code Blockly.Xml.domToWorkspace}.
 *
 * <p>Each block type maps to a generation method that returns an XML string fragment.
 * Mutations are emitted where required (variable input counts, mode-based mutations,
 * procedure parameters, component metadata, etc.).
 *
 * <p>Generated XML is intended to be loaded into the App Inventor Blockly workspace
 * via the standard {@code Blockly.Xml.domToWorkspace} API.
 */
public class BlocksXmlGenerator {

  private static final String BLOCKLY_XML_NS = "https://developers.google.com/blockly/xml";

  /** Maps color names to their corresponding Blockly block type names. */
  private static final Map<String, String> COLOR_NAME_TO_BLOCK;

  static {
    Map<String, String> map = new HashMap<>();
    map.put("black", "color_black");
    map.put("white", "color_white");
    map.put("red", "color_red");
    map.put("pink", "color_pink");
    map.put("orange", "color_orange");
    map.put("yellow", "color_yellow");
    map.put("green", "color_green");
    map.put("cyan", "color_cyan");
    map.put("blue", "color_blue");
    map.put("magenta", "color_magenta");
    map.put("light_gray", "color_light_gray");
    map.put("dark_gray", "color_dark_gray");
    COLOR_NAME_TO_BLOCK = Collections.unmodifiableMap(map);
  }

  // -------------------------------------------------------------------------
  // Component event handlers
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code component_event} block for a specific component instance.
   *
   * @param instanceName the component instance name (e.g. "Button1")
   * @param eventName the event name (e.g. "Click")
   * @param params the event parameter names
   * @param bodyXml the XML string for the statement body (already generated blocks)
   * @return the Blockly XML fragment
   */
  public String generateEventHandler(String instanceName, String eventName,
      List<String> params, String bodyXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"component_event\">");
    sb.append("<mutation component_type=\"\" instance_name=\"")
        .append(escapeXml(instanceName))
        .append("\" event_name=\"").append(escapeXml(eventName)).append("\"/>");
    sb.append("<field name=\"COMPONENT_SELECTOR\">").append(escapeXml(instanceName))
        .append("</field>");
    for (int i = 0; i < params.size(); i++) {
      sb.append("<field name=\"VAR").append(i).append("\">")
          .append(escapeXml(params.get(i))).append("</field>");
    }
    if (bodyXml != null && !bodyXml.isEmpty()) {
      sb.append("<statement name=\"DO\">").append(bodyXml).append("</statement>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a generic {@code component_event} block (Any Component event).
   *
   * @param componentType the component type name (e.g. "Button")
   * @param eventName the event name (e.g. "Click")
   * @param params the event parameter names
   * @param bodyXml the XML string for the statement body
   * @return the Blockly XML fragment
   */
  public String generateGenericEventHandler(String componentType, String eventName,
      List<String> params, String bodyXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"component_event\">");
    sb.append("<mutation component_type=\"").append(escapeXml(componentType))
        .append("\" is_generic=\"true\" event_name=\"").append(escapeXml(eventName))
        .append("\"/>");
    for (int i = 0; i < params.size(); i++) {
      sb.append("<field name=\"VAR").append(i).append("\">")
          .append(escapeXml(params.get(i))).append("</field>");
    }
    if (bodyXml != null && !bodyXml.isEmpty()) {
      sb.append("<statement name=\"DO\">").append(bodyXml).append("</statement>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Component method calls
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code component_method} block for a specific component instance.
   *
   * @param instanceName the component instance name
   * @param methodName the method name
   * @param argXmls XML fragments for each argument value input
   * @param hasReturn true if the method returns a value
   * @return the Blockly XML fragment
   */
  public String generateMethodCall(String instanceName, String methodName,
      List<String> argXmls, boolean hasReturn) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"component_method\">");
    sb.append("<mutation component_type=\"\" method_name=\"")
        .append(escapeXml(methodName))
        .append("\" is_generic=\"false\" instance_name=\"")
        .append(escapeXml(instanceName)).append("\"/>");
    sb.append("<field name=\"COMPONENT_SELECTOR\">").append(escapeXml(instanceName))
        .append("</field>");
    for (int i = 0; i < argXmls.size(); i++) {
      sb.append("<value name=\"ARG").append(i).append("\">")
          .append(argXmls.get(i)).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a generic {@code component_method} block (Any Component method call).
   *
   * @param componentType the component type name
   * @param methodName the method name
   * @param componentExprXml XML fragment for the COMPONENT value input
   * @param argXmls XML fragments for each argument value input
   * @param hasReturn true if the method returns a value
   * @return the Blockly XML fragment
   */
  public String generateGenericMethodCall(String componentType, String methodName,
      String componentExprXml, List<String> argXmls, boolean hasReturn) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"component_method\">");
    sb.append("<mutation component_type=\"").append(escapeXml(componentType))
        .append("\" method_name=\"").append(escapeXml(methodName))
        .append("\" is_generic=\"true\"/>");
    sb.append("<value name=\"COMPONENT\">").append(componentExprXml).append("</value>");
    for (int i = 0; i < argXmls.size(); i++) {
      sb.append("<value name=\"ARG").append(i).append("\">")
          .append(argXmls.get(i)).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Component property get/set
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code component_set_get} block in "get" mode for a specific instance.
   *
   * @param instanceName the component instance name
   * @param propertyName the property name
   * @return the Blockly XML fragment
   */
  public String generatePropertyGet(String instanceName, String propertyName) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"component_set_get\">");
    sb.append("<mutation component_type=\"\" set_or_get=\"get\" property_name=\"")
        .append(escapeXml(propertyName))
        .append("\" is_generic=\"false\" instance_name=\"")
        .append(escapeXml(instanceName)).append("\"/>");
    sb.append("<field name=\"COMPONENT_SELECTOR\">").append(escapeXml(instanceName))
        .append("</field>");
    sb.append("<field name=\"PROP\">").append(escapeXml(propertyName)).append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code component_set_get} block in "set" mode for a specific instance.
   *
   * @param instanceName the component instance name
   * @param propertyName the property name
   * @param valueXml XML fragment for the VALUE input
   * @return the Blockly XML fragment
   */
  public String generatePropertySet(String instanceName, String propertyName,
      String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"component_set_get\">");
    sb.append("<mutation component_type=\"\" set_or_get=\"set\" property_name=\"")
        .append(escapeXml(propertyName))
        .append("\" is_generic=\"false\" instance_name=\"")
        .append(escapeXml(instanceName)).append("\"/>");
    sb.append("<field name=\"COMPONENT_SELECTOR\">").append(escapeXml(instanceName))
        .append("</field>");
    sb.append("<field name=\"PROP\">").append(escapeXml(propertyName)).append("</field>");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a generic {@code component_set_get} block in "get" mode.
   *
   * @param componentType the component type name
   * @param propertyName the property name
   * @param componentExprXml XML fragment for the COMPONENT value input
   * @return the Blockly XML fragment
   */
  public String generateGenericPropertyGet(String componentType, String propertyName,
      String componentExprXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"component_set_get\">");
    sb.append("<mutation component_type=\"").append(escapeXml(componentType))
        .append("\" set_or_get=\"get\" property_name=\"")
        .append(escapeXml(propertyName))
        .append("\" is_generic=\"true\"/>");
    sb.append("<field name=\"PROP\">").append(escapeXml(propertyName)).append("</field>");
    sb.append("<value name=\"COMPONENT\">").append(componentExprXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a generic {@code component_set_get} block in "set" mode.
   *
   * @param componentType the component type name
   * @param propertyName the property name
   * @param componentExprXml XML fragment for the COMPONENT value input
   * @param valueXml XML fragment for the VALUE input
   * @return the Blockly XML fragment
   */
  public String generateGenericPropertySet(String componentType, String propertyName,
      String componentExprXml, String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"component_set_get\">");
    sb.append("<mutation component_type=\"").append(escapeXml(componentType))
        .append("\" set_or_get=\"set\" property_name=\"")
        .append(escapeXml(propertyName))
        .append("\" is_generic=\"true\"/>");
    sb.append("<field name=\"PROP\">").append(escapeXml(propertyName)).append("</field>");
    sb.append("<value name=\"COMPONENT\">").append(componentExprXml).append("</value>");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Global variables
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code global_declaration} block.
   *
   * @param name the variable name
   * @param initValueXml XML fragment for the initial value
   * @return the Blockly XML fragment
   */
  public String generateGlobalDeclaration(String name, String initValueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"global_declaration\">");
    sb.append("<field name=\"NAME\">").append(escapeXml(name)).append("</field>");
    sb.append("<value name=\"VALUE\">").append(initValueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code lexical_variable_get} block for a global variable.
   *
   * @param name the global variable name (without "global " prefix)
   * @return the Blockly XML fragment
   */
  public String generateGlobalGet(String name) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lexical_variable_get\">");
    sb.append("<field name=\"VAR\">global ").append(escapeXml(name)).append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code lexical_variable_set} block for a global variable.
   *
   * @param name the global variable name (without "global " prefix)
   * @param valueXml XML fragment for the value to set
   * @return the Blockly XML fragment
   */
  public String generateGlobalSet(String name, String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lexical_variable_set\">");
    sb.append("<field name=\"VAR\">global ").append(escapeXml(name)).append("</field>");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Local variables
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code lexical_variable_get} block for a local variable.
   *
   * @param name the local variable name
   * @return the Blockly XML fragment
   */
  public String generateLocalGet(String name) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lexical_variable_get\">");
    sb.append("<field name=\"VAR\">").append(escapeXml(name)).append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code lexical_variable_set} block for a local variable.
   *
   * @param name the local variable name
   * @param valueXml XML fragment for the value to set
   * @return the Blockly XML fragment
   */
  public String generateLocalSet(String name, String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lexical_variable_set\">");
    sb.append("<field name=\"VAR\">").append(escapeXml(name)).append("</field>");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Procedures (definitions and calls)
  // -------------------------------------------------------------------------

  /**
   * Generates a procedure definition block.
   * Uses {@code procedures_defnoreturn} when {@code hasReturn} is false,
   * or {@code procedures_defreturn} when true.
   *
   * @param name the procedure name
   * @param params parameter names
   * @param bodyXml XML fragment for the procedure body statements
   * @param hasReturn true if the procedure returns a value
   * @param returnXml XML fragment for the return value (only used if hasReturn is true)
   * @return the Blockly XML fragment
   */
  public String generateProcedureDef(String name, List<String> params,
      String bodyXml, boolean hasReturn, String returnXml) {
    StringBuilder sb = new StringBuilder();
    String blockType = hasReturn ? "procedures_defreturn" : "procedures_defnoreturn";
    sb.append("<block type=\"").append(blockType).append("\">");

    // Mutation with parameter names
    sb.append("<mutation>");
    for (String param : params) {
      sb.append("<arg name=\"").append(escapeXml(param)).append("\"/>");
    }
    sb.append("</mutation>");

    sb.append("<field name=\"NAME\">").append(escapeXml(name)).append("</field>");

    if (hasReturn) {
      // procedures_defreturn uses a RETURN value input for the body + return value
      if (bodyXml != null && !bodyXml.isEmpty()) {
        sb.append("<statement name=\"STACK\">").append(bodyXml).append("</statement>");
      }
      if (returnXml != null && !returnXml.isEmpty()) {
        sb.append("<value name=\"RETURN\">").append(returnXml).append("</value>");
      }
    } else {
      // procedures_defnoreturn uses a STACK statement input for the body
      if (bodyXml != null && !bodyXml.isEmpty()) {
        sb.append("<statement name=\"STACK\">").append(bodyXml).append("</statement>");
      }
    }

    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a procedure call block.
   * Uses {@code procedures_callnoreturn} when {@code hasReturn} is false,
   * or {@code procedures_callreturn} when true.
   *
   * @param name the procedure name
   * @param argXmls XML fragments for each argument value input
   * @param hasReturn true if calling a procedure that returns a value
   * @return the Blockly XML fragment
   */
  public String generateProcedureCall(String name, List<String> argXmls,
      boolean hasReturn) {
    StringBuilder sb = new StringBuilder();
    String blockType = hasReturn ? "procedures_callreturn" : "procedures_callnoreturn";
    sb.append("<block type=\"").append(blockType).append("\">");

    // Mutation with procedure name and argument names
    sb.append("<mutation name=\"").append(escapeXml(name)).append("\">");
    for (int i = 0; i < argXmls.size(); i++) {
      sb.append("<arg name=\"x").append(i).append("\"/>");
    }
    sb.append("</mutation>");

    sb.append("<field name=\"PROCNAME\">").append(escapeXml(name)).append("</field>");
    for (int i = 0; i < argXmls.size(); i++) {
      sb.append("<value name=\"ARG").append(i).append("\">")
          .append(argXmls.get(i)).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Control flow
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code controls_if} block with proper mutation for
   * elseif/else branches.
   *
   * <p>The first condition/then pair populates IF0/DO0. Additional pairs
   * populate IF1/DO1, IF2/DO2, etc. as elseif branches.
   *
   * @param conditionXmls XML fragments for each condition (at least one)
   * @param thenBranchXmls XML fragments for each then-branch body
   * @param elseBranchXml XML fragment for the else branch body, or null if no else
   * @return the Blockly XML fragment
   */
  public String generateControlsIf(List<String> conditionXmls,
      List<String> thenBranchXmls, String elseBranchXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_if\">");

    int elseifCount = conditionXmls.size() - 1;
    boolean hasElse = elseBranchXml != null && !elseBranchXml.isEmpty();

    if (elseifCount > 0 || hasElse) {
      sb.append("<mutation");
      if (elseifCount > 0) {
        sb.append(" elseif=\"").append(elseifCount).append("\"");
      }
      if (hasElse) {
        sb.append(" else=\"1\"");
      }
      sb.append("/>");
    }

    for (int i = 0; i < conditionXmls.size(); i++) {
      sb.append("<value name=\"IF").append(i).append("\">")
          .append(conditionXmls.get(i)).append("</value>");
      if (i < thenBranchXmls.size()
          && thenBranchXmls.get(i) != null && !thenBranchXmls.get(i).isEmpty()) {
        sb.append("<statement name=\"DO").append(i).append("\">")
            .append(thenBranchXmls.get(i)).append("</statement>");
      }
    }

    if (hasElse) {
      sb.append("<statement name=\"ELSE\">").append(elseBranchXml).append("</statement>");
    }

    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code controls_forEach} block.
   *
   * @param varName the loop variable name
   * @param listXml XML fragment for the LIST value input
   * @param bodyXml XML fragment for the DO statement body
   * @return the Blockly XML fragment
   */
  public String generateForEach(String varName, String listXml, String bodyXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_forEach\">");
    sb.append("<field name=\"VAR\">").append(escapeXml(varName)).append("</field>");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    if (bodyXml != null && !bodyXml.isEmpty()) {
      sb.append("<statement name=\"DO\">").append(bodyXml).append("</statement>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code controls_forRange} block.
   *
   * @param varName the loop variable name
   * @param fromXml XML fragment for the START value input
   * @param toXml XML fragment for the END value input
   * @param byXml XML fragment for the STEP value input
   * @param bodyXml XML fragment for the DO statement body
   * @return the Blockly XML fragment
   */
  public String generateForRange(String varName, String fromXml, String toXml,
      String byXml, String bodyXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_forRange\">");
    sb.append("<field name=\"VAR\">").append(escapeXml(varName)).append("</field>");
    sb.append("<value name=\"START\">").append(fromXml).append("</value>");
    sb.append("<value name=\"END\">").append(toXml).append("</value>");
    sb.append("<value name=\"STEP\">").append(byXml).append("</value>");
    if (bodyXml != null && !bodyXml.isEmpty()) {
      sb.append("<statement name=\"DO\">").append(bodyXml).append("</statement>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code controls_while} block.
   *
   * @param condXml XML fragment for the TEST value input
   * @param bodyXml XML fragment for the DO statement body
   * @return the Blockly XML fragment
   */
  public String generateWhile(String condXml, String bodyXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_while\">");
    sb.append("<value name=\"TEST\">").append(condXml).append("</value>");
    if (bodyXml != null && !bodyXml.isEmpty()) {
      sb.append("<statement name=\"DO\">").append(bodyXml).append("</statement>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code controls_choose} block (ternary if-then-else expression).
   *
   * @param condXml XML fragment for the TEST value input
   * @param thenXml XML fragment for the THENRETURN value input
   * @param elseXml XML fragment for the ELSERETURN value input
   * @return the Blockly XML fragment
   */
  public String generateControlsChoose(String condXml, String thenXml, String elseXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_choose\">");
    sb.append("<value name=\"TEST\">").append(condXml).append("</value>");
    sb.append("<value name=\"THENRETURN\">").append(thenXml).append("</value>");
    sb.append("<value name=\"ELSERETURN\">").append(elseXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code controls_do_then_return} block.
   *
   * @param bodyXml XML fragment for the STM statement input (do-block)
   * @param returnXml XML fragment for the VALUE value input (return expression)
   * @return the Blockly XML fragment
   */
  public String generateControlsDoThenReturn(String bodyXml, String returnXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_do_then_return\">");
    if (bodyXml != null && !bodyXml.isEmpty()) {
      sb.append("<statement name=\"STM\">").append(bodyXml).append("</statement>");
    }
    sb.append("<value name=\"VALUE\">").append(returnXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code controls_break} block (loop escape).
   *
   * @return the Blockly XML fragment
   */
  public String generateBreak() {
    return "<block type=\"controls_break\"></block>";
  }

  // -------------------------------------------------------------------------
  // Math blocks
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code math_number} block.
   *
   * @param value the numeric value as a string
   * @return the Blockly XML fragment
   */
  public String generateMathNumber(String value) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_number\">");
    sb.append("<field name=\"NUM\">").append(escapeXml(value)).append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code math_add} block with a mutator for N operands.
   * Input names are NUM0, NUM1, NUM2, etc.
   *
   * @param operandXmls XML fragments for each operand
   * @return the Blockly XML fragment
   */
  public String generateMathAdd(List<String> operandXmls) {
    return generateVariadicMathBlock("math_add", "NUM", operandXmls);
  }

  /**
   * Generates a {@code math_subtract} block (binary subtraction).
   *
   * @param aXml XML fragment for the A value input
   * @param bXml XML fragment for the B value input
   * @return the Blockly XML fragment
   */
  public String generateMathSubtract(String aXml, String bXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_subtract\">");
    sb.append("<value name=\"A\">").append(aXml).append("</value>");
    sb.append("<value name=\"B\">").append(bXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code math_multiply} block with a mutator for N operands.
   * Input names are NUM0, NUM1, NUM2, etc.
   *
   * @param operandXmls XML fragments for each operand
   * @return the Blockly XML fragment
   */
  public String generateMathMultiply(List<String> operandXmls) {
    return generateVariadicMathBlock("math_multiply", "NUM", operandXmls);
  }

  /**
   * Generates a {@code math_division} block (binary division).
   *
   * @param aXml XML fragment for the A value input
   * @param bXml XML fragment for the B value input
   * @return the Blockly XML fragment
   */
  public String generateMathDivision(String aXml, String bXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_division\">");
    sb.append("<value name=\"A\">").append(aXml).append("</value>");
    sb.append("<value name=\"B\">").append(bXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code math_compare} block.
   *
   * @param op the comparison operator: "EQ", "NEQ", "LT", "LTE", "GT", or "GTE"
   * @param aXml XML fragment for the A value input
   * @param bXml XML fragment for the B value input
   * @return the Blockly XML fragment
   */
  public String generateMathCompare(String op, String aXml, String bXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_compare\">");
    sb.append("<field name=\"OP\">").append(escapeXml(op)).append("</field>");
    sb.append("<value name=\"A\">").append(aXml).append("</value>");
    sb.append("<value name=\"B\">").append(bXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Text blocks
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code text} block.
   *
   * @param value the text value
   * @return the Blockly XML fragment
   */
  public String generateText(String value) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text\">");
    sb.append("<field name=\"TEXT\">").append(escapeXml(value)).append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code text_join} block with a mutator for N text inputs.
   * Input names are ADD0, ADD1, ADD2, etc.
   *
   * @param textXmls XML fragments for each text operand
   * @return the Blockly XML fragment
   */
  public String generateTextJoin(List<String> textXmls) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_join\">");
    sb.append("<mutation items=\"").append(textXmls.size()).append("\"/>");
    for (int i = 0; i < textXmls.size(); i++) {
      sb.append("<value name=\"ADD").append(i).append("\">")
          .append(textXmls.get(i)).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Logic blocks
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code logic_boolean} block.
   *
   * @param value true for TRUE, false for FALSE
   * @return the Blockly XML fragment
   */
  public String generateLogicBoolean(boolean value) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"logic_boolean\">");
    sb.append("<field name=\"BOOL\">").append(value ? "TRUE" : "FALSE").append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code logic_operation} block (AND/OR) with a mutator for
   * variable operands. Input names are BOOL0, BOOL1, BOOL2, etc.
   *
   * @param op the logic operator: "AND" or "OR"
   * @param operandXmls XML fragments for each boolean operand
   * @return the Blockly XML fragment
   */
  public String generateLogicOperation(String op, List<String> operandXmls) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"logic_operation\">");
    sb.append("<mutation items=\"").append(operandXmls.size()).append("\"/>");
    sb.append("<field name=\"OP\">").append(escapeXml(op)).append("</field>");
    for (int i = 0; i < operandXmls.size(); i++) {
      sb.append("<value name=\"BOOL").append(i).append("\">")
          .append(operandXmls.get(i)).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code logic_negate} block.
   *
   * @param valueXml XML fragment for the BOOL value input
   * @return the Blockly XML fragment
   */
  public String generateLogicNegate(String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"logic_negate\">");
    sb.append("<value name=\"BOOL\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // List blocks
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code lists_create_with} block with a mutator for N items.
   * Input names are ADD0, ADD1, ADD2, etc.
   *
   * @param itemXmls XML fragments for each list item
   * @return the Blockly XML fragment
   */
  public String generateListsCreateWith(List<String> itemXmls) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_create_with\">");
    sb.append("<mutation items=\"").append(itemXmls.size()).append("\"/>");
    for (int i = 0; i < itemXmls.size(); i++) {
      sb.append("<value name=\"ADD").append(i).append("\">")
          .append(itemXmls.get(i)).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Dictionary blocks
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code dictionaries_create_with} block with a mutator for N pairs.
   * Input names are ADD0, ADD1, ADD2, etc. Each input should be a pair block.
   *
   * @param pairXmls XML fragments for each pair block
   * @return the Blockly XML fragment
   */
  public String generateDictionariesCreateWith(List<String> pairXmls) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_create_with\">");
    sb.append("<mutation items=\"").append(pairXmls.size()).append("\"/>");
    for (int i = 0; i < pairXmls.size(); i++) {
      sb.append("<value name=\"ADD").append(i).append("\">")
          .append(pairXmls.get(i)).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code pair} block (key-value pair for dictionaries).
   *
   * @param keyXml XML fragment for the KEY value input
   * @param valueXml XML fragment for the VALUE value input
   * @return the Blockly XML fragment
   */
  public String generatePair(String keyXml, String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"pair\">");
    sb.append("<value name=\"KEY\">").append(keyXml).append("</value>");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Color blocks
  // -------------------------------------------------------------------------

  /**
   * Generates a color constant block (e.g. {@code color_black}, {@code color_red}).
   *
   * @param colorName the color name in lowercase (e.g. "black", "red", "light_gray")
   * @return the Blockly XML fragment
   * @throws IllegalArgumentException if the color name is not recognized
   */
  public String generateColorConstant(String colorName) {
    String blockType = COLOR_NAME_TO_BLOCK.get(colorName.toLowerCase());
    if (blockType == null) {
      throw new IllegalArgumentException("Unknown color name: " + colorName);
    }
    return "<block type=\"" + blockType + "\"></block>";
  }

  // -------------------------------------------------------------------------
  // Component reference block
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code component_component_block} that represents a reference to
   * a specific component instance (used as input for generic blocks).
   *
   * @param componentName the component instance name
   * @return the Blockly XML fragment
   */
  public String generateComponentBlock(String componentName) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"component_component_block\">");
    sb.append("<mutation component_type=\"\" instance_name=\"")
        .append(escapeXml(componentName)).append("\"/>");
    sb.append("<field name=\"COMPONENT_SELECTOR\">")
        .append(escapeXml(componentName)).append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Helper blocks
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code helpers_dropdown} block for enum/option list values.
   *
   * @param optionListType the OptionList key identifier
   * @param value the selected option value
   * @return the Blockly XML fragment
   */
  public String generateHelpersDropdown(String optionListType, String value) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"helpers_dropdown\">");
    sb.append("<mutation key=\"").append(escapeXml(optionListType)).append("\"/>");
    sb.append("<field name=\"OPTION\">").append(escapeXml(value)).append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code helpers_screen_names} block.
   *
   * @param screenName the screen name to select
   * @return the Blockly XML fragment
   */
  public String generateHelpersScreenNames(String screenName) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"helpers_screen_names\">");
    sb.append("<mutation value=\"").append(escapeXml(screenName)).append("\"/>");
    sb.append("<field name=\"SCREEN\">").append(escapeXml(screenName)).append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code helpers_assets} block.
   *
   * @param assetName the asset file name to select
   * @return the Blockly XML fragment
   */
  public String generateHelpersAssets(String assetName) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"helpers_assets\">");
    sb.append("<mutation value=\"").append(escapeXml(assetName)).append("\"/>");
    sb.append("<field name=\"ASSET\">").append(escapeXml(assetName)).append("</field>");
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Local variable declaration blocks
  // -------------------------------------------------------------------------

  /**
   * Generates a {@code local_declaration_statement} block (local variable in statement context).
   *
   * @param varName the local variable name
   * @param initValueXml XML fragment for the DECL0 initial value
   * @param bodyXml XML fragment for the STACK statement body
   * @return the Blockly XML fragment
   */
  public String generateLocalDeclarationStatement(String varName, String initValueXml,
      String bodyXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"local_declaration_statement\">");
    sb.append("<mutation>");
    sb.append("<localname name=\"").append(escapeXml(varName)).append("\"/>");
    sb.append("</mutation>");
    sb.append("<field name=\"VAR0\">").append(escapeXml(varName)).append("</field>");
    sb.append("<value name=\"DECL0\">").append(initValueXml).append("</value>");
    if (bodyXml != null && !bodyXml.isEmpty()) {
      sb.append("<statement name=\"STACK\">").append(bodyXml).append("</statement>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Generates a {@code local_declaration_expression} block (local variable in expression context).
   *
   * @param varName the local variable name
   * @param initValueXml XML fragment for the DECL0 initial value
   * @param returnExprXml XML fragment for the RETURN value input
   * @return the Blockly XML fragment
   */
  public String generateLocalDeclarationExpression(String varName, String initValueXml,
      String returnExprXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"local_declaration_expression\">");
    sb.append("<mutation>");
    sb.append("<localname name=\"").append(escapeXml(varName)).append("\"/>");
    sb.append("</mutation>");
    sb.append("<field name=\"VAR0\">").append(escapeXml(varName)).append("</field>");
    sb.append("<value name=\"DECL0\">").append(initValueXml).append("</value>");
    sb.append("<value name=\"RETURN\">").append(returnExprXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // XML wrapper and statement chaining utilities
  // -------------------------------------------------------------------------

  /**
   * Wraps inner XML content in the Blockly XML root element with the proper namespace.
   *
   * @param innerXml the XML content to wrap
   * @return the complete Blockly XML document string
   */
  public String wrapInXmlBlock(String innerXml) {
    return "<xml xmlns=\"" + BLOCKLY_XML_NS + "\">" + innerXml + "</xml>";
  }

  /**
   * Chains multiple statement block XML fragments together using {@code <next>} elements.
   * This produces the proper nesting structure that Blockly expects for sequential
   * statement blocks.
   *
   * <p>For example, given blocks [A, B, C], produces:
   * <pre>
   * A
   *   &lt;next&gt;
   *     B
   *       &lt;next&gt;
   *         C
   *       &lt;/next&gt;
   *   &lt;/next&gt;
   * </pre>
   *
   * @param statementXmls list of statement block XML fragments to chain
   * @return the chained XML, or empty string if the list is empty
   */
  public String chainStatements(List<String> statementXmls) {
    if (statementXmls == null || statementXmls.isEmpty()) {
      return "";
    }
    if (statementXmls.size() == 1) {
      return statementXmls.get(0);
    }
    return chainStatementsRecursive(statementXmls, 0);
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  /**
   * Recursively chains statement blocks via {@code <next>} elements.
   *
   * @param stmts the list of statement block XML fragments
   * @param index current index into the list
   * @return the chained XML starting from the given index
   */
  private String chainStatementsRecursive(List<String> stmts, int index) {
    if (index >= stmts.size()) {
      return "";
    }

    String blockXml = stmts.get(index);
    if (index == stmts.size() - 1) {
      // Last block, no <next> wrapper needed
      return blockXml;
    }

    // Insert <next>remaining chain</next> before the closing </block>
    String rest = chainStatementsRecursive(stmts, index + 1);
    return insertBeforeClosingBlockTag(blockXml, "<next>" + rest + "</next>");
  }

  /**
   * Inserts additional XML content just before the final {@code </block>} tag of a block
   * XML string.
   *
   * @param blockXml the block XML string ending with {@code </block>}
   * @param toInsert the XML content to insert before the closing tag
   * @return the modified block XML string
   */
  private String insertBeforeClosingBlockTag(String blockXml, String toInsert) {
    int lastClose = blockXml.lastIndexOf("</block>");
    if (lastClose == -1) {
      // Fallback: just append
      return blockXml + toInsert;
    }
    return blockXml.substring(0, lastClose) + toInsert + blockXml.substring(lastClose);
  }

  /**
   * Generates a variadic math block (add, multiply) with an items mutation.
   * These blocks use repeating input names like NUM0, NUM1, NUM2, etc.
   *
   * @param blockType the block type name
   * @param inputPrefix the input name prefix (e.g. "NUM")
   * @param operandXmls XML fragments for each operand
   * @return the Blockly XML fragment
   */
  private String generateVariadicMathBlock(String blockType, String inputPrefix,
      List<String> operandXmls) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"").append(blockType).append("\">");
    sb.append("<mutation items=\"").append(operandXmls.size()).append("\"/>");
    for (int i = 0; i < operandXmls.size(); i++) {
      sb.append("<value name=\"").append(inputPrefix).append(i).append("\">")
          .append(operandXmls.get(i)).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  /**
   * Escapes a string for safe inclusion in XML attribute values and text content.
   * Handles the five standard XML entities.
   *
   * @param text the text to escape
   * @return the escaped text
   */
  private String escapeXml(String text) {
    if (text == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch (c) {
        case '&':
          sb.append("&amp;");
          break;
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        case '\'':
          sb.append("&apos;");
          break;
        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }
}
