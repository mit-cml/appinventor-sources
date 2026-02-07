// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses pseudocode (matching the grammar in {@code pseudocode_grammar.md})
 * and converts it into valid Blockly XML using {@link BlocksXmlGenerator}.
 *
 * <h3>Architecture</h3>
 * <ol>
 *   <li>A line-based tokenizer splits the input into {@link Line} records that
 *       carry the raw text and its indentation level.</li>
 *   <li>A recursive descent parser consumes lines for statements and uses an
 *       operator-precedence sub-parser for expressions.</li>
 *   <li>The parser calls {@link BlocksXmlGenerator} methods to produce Blockly
 *       XML fragments. Statement blocks are chained via
 *       {@link BlocksXmlGenerator#chainStatements}.</li>
 * </ol>
 *
 * <p>The parser is intentionally lenient with whitespace and casing for
 * keywords so that minor LLM formatting variations are tolerated.
 */
public class PseudocodeParser {

  private final BlocksXmlGenerator xmlGen;

  // -------------------------------------------------------------------------
  // Known color names  (lowercase → canonical name for BlocksXmlGenerator)
  // -------------------------------------------------------------------------
  private static final Map<String, String> COLOR_NAMES;

  static {
    Map<String, String> c = new HashMap<>();
    c.put("black", "black");
    c.put("white", "white");
    c.put("red", "red");
    c.put("pink", "pink");
    c.put("orange", "orange");
    c.put("yellow", "yellow");
    c.put("green", "green");
    c.put("cyan", "cyan");
    c.put("blue", "blue");
    c.put("magenta", "magenta");
    c.put("light gray", "light_gray");
    c.put("light_gray", "light_gray");
    c.put("gray", "dark_gray");
    c.put("dark gray", "dark_gray");
    c.put("dark_gray", "dark_gray");
    COLOR_NAMES = Collections.unmodifiableMap(c);
  }

  /** Maps math_single OP field values to pseudocode keywords (op → keyword). */
  private static final Map<String, String> MATH_SINGLE_OPS;

  /** Maps math_trig OP field values to pseudocode keywords (op → keyword). */
  private static final Map<String, String> TRIG_OPS;

  static {
    Map<String, String> ms = new HashMap<>();
    ms.put("NEG", "negate");
    ms.put("ABS", "abs");
    ms.put("SQRT", "sqrt");
    ms.put("LOG", "log");
    ms.put("EXP", "e^");
    ms.put("ROUND", "round");
    ms.put("FLOOR", "floor");
    ms.put("CEILING", "ceiling");
    MATH_SINGLE_OPS = Collections.unmodifiableMap(ms);

    Map<String, String> t = new HashMap<>();
    t.put("SIN", "sin");
    t.put("COS", "cos");
    t.put("TAN", "tan");
    t.put("ASIN", "asin");
    t.put("ACOS", "acos");
    t.put("ATAN", "atan");
    TRIG_OPS = Collections.unmodifiableMap(t);
  }

  // Prefix keywords used to disambiguate unary expression operators
  private static final Set<String> PREFIX_EXPR_KEYWORDS = new HashSet<>(Arrays.asList(
      "negate", "abs", "sqrt", "log", "round", "floor", "ceiling",
      "not", "sin", "cos", "tan", "asin", "acos", "atan",
      "modulo", "remainder", "quotient",
      "bitwise",
      "length of text", "length of list", "length of dict",
      "is text empty", "is list empty", "is a list", "is a number",
      "is a base10", "is a hexadecimal", "is a binary",
      "trim", "upcase", "downcase",
      "reverse text", "reverse list",
      "copy list", "sort",
      "keys of", "values of",
      "split color",
      "random fraction",
      "get start value",
      "csv row to list", "list to csv row",
      "csv table to list", "list to csv table",
      "alist to dict", "dict to alist"
  ));

  // -------------------------------------------------------------------------
  // Compiled patterns for statement recognition
  // -------------------------------------------------------------------------
  private static final Pattern WHEN_PATTERN =
      Pattern.compile("^when\\s+(\\w+)\\.(\\w+)\\(([^)]*)\\)\\s+do\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern WHEN_ANY_PATTERN =
      Pattern.compile("^when\\s+any\\s+(\\w+)\\.(\\w+)\\(([^)]*)\\)\\s+do\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern PROC_DEF_RETURNS_PATTERN =
      Pattern.compile("^procedure\\s+(\\w+)\\(([^)]*)\\)\\s+returns\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern PROC_DEF_PATTERN =
      Pattern.compile("^procedure\\s+(\\w+)\\(([^)]*)\\)\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern IF_PATTERN =
      Pattern.compile("^if\\s+(.+?)\\s+then\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern ELSE_IF_PATTERN =
      Pattern.compile("^else\\s+if\\s+(.+?)\\s+then\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern FOR_EACH_DICT_PATTERN =
      Pattern.compile("^for\\s+each\\s+key\\s+(\\w+)\\s+value\\s+(\\w+)\\s+in\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern FOR_EACH_PATTERN =
      Pattern.compile("^for\\s+each\\s+(\\w+)\\s+in\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern FOR_RANGE_PATTERN =
      Pattern.compile("^for\\s+(\\w+)\\s+from\\s+(.+?)\\s+to\\s+(.+?)\\s+by\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern WHILE_PATTERN =
      Pattern.compile("^while\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern SET_GLOBAL_PATTERN =
      Pattern.compile("^set\\s+global\\s+(\\w+)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern SET_LOCAL_PATTERN =
      Pattern.compile("^set\\s+local\\s+(\\w+)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern INIT_GLOBAL_PATTERN =
      Pattern.compile("^initialize\\s+global\\s+(\\w+)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern INIT_LOCAL_PATTERN =
      Pattern.compile("^initialize\\s+local\\s+(\\w+)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern SET_COMP_PROP_PATTERN =
      Pattern.compile("^set\\s+(\\w+)\\.(\\w+)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern SET_GENERIC_PROP_PATTERN =
      Pattern.compile("^set\\s+(\\w+)\\.(\\w+)\\s+of\\s+(.+?)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern CALL_COMP_METHOD_PATTERN =
      Pattern.compile("^call\\s+(\\w+)\\.(\\w+)\\((.*)\\)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern CALL_GENERIC_METHOD_PATTERN =
      Pattern.compile("^call\\s+(\\w+)\\.(\\w+)\\s+of\\s+(.+?)\\((.*)\\)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern CALL_PROC_PATTERN =
      Pattern.compile("^call\\s+(\\w+)\\((.*)\\)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern RETURN_PATTERN =
      Pattern.compile("^return\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern EVAL_BUT_IGNORE_PATTERN =
      Pattern.compile("^evaluate\\s+but\\s+ignore\\s+(.+)$", Pattern.CASE_INSENSITIVE);

  // List mutation
  private static final Pattern ADD_ITEMS_PATTERN =
      Pattern.compile("^add\\s+items\\s+(.+?)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern INSERT_ITEM_PATTERN =
      Pattern.compile("^insert\\s+item\\s+(.+?)\\s+into\\s+(.+?)\\s+at\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern REPLACE_ITEM_PATTERN =
      Pattern.compile("^replace\\s+item\\s+(.+?)\\s+in\\s+(.+?)\\s+with\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern REMOVE_ITEM_PATTERN =
      Pattern.compile("^remove\\s+item\\s+(.+?)\\s+from\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern APPEND_LIST_PATTERN =
      Pattern.compile("^append\\s+(.+?)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);

  // Dict mutation
  private static final Pattern SET_KEY_IN_DICT_PATTERN =
      Pattern.compile("^set\\s+key\\s+(.+?)\\s+in\\s+(.+?)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern DELETE_KEY_PATTERN =
      Pattern.compile("^delete\\s+key\\s+(.+?)\\s+from\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern SET_PATH_IN_DICT_PATTERN =
      Pattern.compile("^set\\s+path\\s+(.+?)\\s+in\\s+(.+?)\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE);

  // Screen navigation
  private static final Pattern OPEN_SCREEN_VALUE_PATTERN =
      Pattern.compile("^open\\s+another\\s+screen\\s+(.+?)\\s+with\\s+value\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern OPEN_SCREEN_PATTERN =
      Pattern.compile("^open\\s+another\\s+screen\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern CLOSE_SCREEN_VALUE_PATTERN =
      Pattern.compile("^close\\s+screen\\s+with\\s+value\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern CLOSE_SCREEN_PLAIN_TEXT_PATTERN =
      Pattern.compile("^close\\s+screen\\s+with\\s+plain\\s+text\\s+(.+)$", Pattern.CASE_INSENSITIVE);

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  public PseudocodeParser() {
    this.xmlGen = new BlocksXmlGenerator();
  }

  // -------------------------------------------------------------------------
  // Public API
  // -------------------------------------------------------------------------

  /**
   * Parse pseudocode and generate Blockly XML.
   *
   * @param pseudocode the pseudocode string (may contain section headers)
   * @return Blockly XML string wrapped in an {@code <xml>} root element
   * @throws PseudocodeParseException if the pseudocode is malformed
   */
  public String parse(String pseudocode) throws PseudocodeParseException {
    if (pseudocode == null || pseudocode.trim().isEmpty()) {
      return xmlGen.wrapInXmlBlock("");
    }

    List<Line> lines = tokenize(pseudocode);
    ParseState state = new ParseState(lines);

    List<String> topLevelBlocks = new ArrayList<>();

    while (state.hasMore()) {
      Line line = state.peek();

      // Skip section headers and blank lines
      if (line.text.startsWith("==") || line.text.isEmpty()) {
        state.advance();
        continue;
      }

      String blockXml = parseTopLevelBlock(state);
      if (blockXml != null && !blockXml.isEmpty()) {
        topLevelBlocks.add(blockXml);
      }
    }

    return xmlGen.wrapInXmlBlock(String.join("", topLevelBlocks));
  }

  /**
   * Parse a single expression and return its XML.
   *
   * @param expression the pseudocode expression string
   * @return Blockly XML fragment for the expression
   * @throws PseudocodeParseException if the expression is malformed
   */
  public String parseExpression(String expression) throws PseudocodeParseException {
    if (expression == null || expression.trim().isEmpty()) {
      throw new PseudocodeParseException("Empty expression");
    }
    ExprParser ep = new ExprParser(expression.trim());
    return ep.parseExpression();
  }

  // -------------------------------------------------------------------------
  // Line-based tokenizer
  // -------------------------------------------------------------------------

  /**
   * A single line of pseudocode with its indentation level.
   */
  private static class Line {
    final String text;       // trimmed text
    final int indent;        // number of indent levels (each 2 spaces = 1 level)
    final int lineNumber;    // 1-based line number in original input

    Line(String text, int indent, int lineNumber) {
      this.text = text;
      this.indent = indent;
      this.lineNumber = lineNumber;
    }
  }

  /**
   * Mutable cursor over the list of parsed lines.
   */
  private static class ParseState {
    final List<Line> lines;
    int pos;

    ParseState(List<Line> lines) {
      this.lines = lines;
      this.pos = 0;
    }

    boolean hasMore() {
      return pos < lines.size();
    }

    Line peek() {
      return lines.get(pos);
    }

    Line consume() {
      return lines.get(pos++);
    }

    void advance() {
      pos++;
    }
  }

  /**
   * Split pseudocode text into {@link Line} records.
   */
  private List<Line> tokenize(String text) {
    List<Line> result = new ArrayList<>();
    String[] rawLines = text.split("\\r?\\n");
    int lineNum = 0;

    for (String raw : rawLines) {
      lineNum++;
      if (raw.trim().isEmpty()) {
        continue; // skip blank lines
      }
      int spaces = 0;
      for (int i = 0; i < raw.length(); i++) {
        if (raw.charAt(i) == ' ') {
          spaces++;
        } else if (raw.charAt(i) == '\t') {
          spaces += 2;
        } else {
          break;
        }
      }
      int indent = spaces / 2;
      result.add(new Line(raw.trim(), indent, lineNum));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Top-level block parsing
  // -------------------------------------------------------------------------

  private String parseTopLevelBlock(ParseState state) throws PseudocodeParseException {
    Line line = state.peek();

    // Event handler
    Matcher m = WHEN_ANY_PATTERN.matcher(line.text);
    if (m.matches()) {
      return parseGenericEventHandler(state, m);
    }
    m = WHEN_PATTERN.matcher(line.text);
    if (m.matches()) {
      return parseEventHandler(state, m);
    }

    // Procedure definition (check returns variant first)
    m = PROC_DEF_RETURNS_PATTERN.matcher(line.text);
    if (m.matches()) {
      return parseProcedureDef(state, m, true);
    }
    m = PROC_DEF_PATTERN.matcher(line.text);
    if (m.matches()) {
      return parseProcedureDef(state, m, false);
    }

    // Global declaration
    m = INIT_GLOBAL_PATTERN.matcher(line.text);
    if (m.matches()) {
      state.advance();
      String name = m.group(1);
      String valueExpr = m.group(2);
      return xmlGen.generateGlobalDeclaration(name, parseExprInternal(valueExpr));
    }

    // Any other statement at indent 0
    return parseStatementBlock(state, line.indent);
  }

  // -------------------------------------------------------------------------
  // Event handler parsing
  // -------------------------------------------------------------------------

  private String parseEventHandler(ParseState state, Matcher m) throws PseudocodeParseException {
    int baseIndent = state.peek().indent;
    state.advance();

    String instanceName = m.group(1);
    String eventName = m.group(2);
    List<String> params = splitParams(m.group(3));

    String bodyXml = parseBody(state, baseIndent + 1);
    return xmlGen.generateEventHandler(instanceName, eventName, params, bodyXml);
  }

  private String parseGenericEventHandler(ParseState state, Matcher m)
      throws PseudocodeParseException {
    int baseIndent = state.peek().indent;
    state.advance();

    String componentType = m.group(1);
    String eventName = m.group(2);
    List<String> params = splitParams(m.group(3));

    String bodyXml = parseBody(state, baseIndent + 1);
    return xmlGen.generateGenericEventHandler(componentType, eventName, params, bodyXml);
  }

  // -------------------------------------------------------------------------
  // Procedure definition parsing
  // -------------------------------------------------------------------------

  private String parseProcedureDef(ParseState state, Matcher m, boolean hasReturn)
      throws PseudocodeParseException {
    int baseIndent = state.peek().indent;
    state.advance();

    String name = m.group(1);
    List<String> params = splitParams(m.group(2));

    // Parse body lines; if hasReturn, look for a trailing "return <expr>"
    String returnXml = null;
    List<String> bodyStatements = new ArrayList<>();

    while (state.hasMore() && state.peek().indent > baseIndent) {
      Line bodyLine = state.peek();

      // Check for return statement
      if (hasReturn) {
        Matcher retM = RETURN_PATTERN.matcher(bodyLine.text);
        if (retM.matches() && bodyLine.indent == baseIndent + 1) {
          state.advance();
          returnXml = parseExprInternal(retM.group(1));
          continue;
        }
      }

      String stmtXml = parseStatementBlock(state, baseIndent + 1);
      if (stmtXml != null && !stmtXml.isEmpty()) {
        bodyStatements.add(stmtXml);
      }
    }

    String bodyXml = xmlGen.chainStatements(bodyStatements);
    return xmlGen.generateProcedureDef(name, params, bodyXml, hasReturn, returnXml);
  }

  // -------------------------------------------------------------------------
  // Body parsing (indented block of statements)
  // -------------------------------------------------------------------------

  /**
   * Parse all statements at the given indent level or deeper, until a line
   * at a shallower indent is encountered.
   *
   * @return chained Blockly XML for the statement sequence
   */
  private String parseBody(ParseState state, int expectedIndent) throws PseudocodeParseException {
    List<String> statements = new ArrayList<>();
    while (state.hasMore() && state.peek().indent >= expectedIndent) {
      String stmtXml = parseStatementBlock(state, expectedIndent);
      if (stmtXml != null && !stmtXml.isEmpty()) {
        statements.add(stmtXml);
      }
    }
    return xmlGen.chainStatements(statements);
  }

  // -------------------------------------------------------------------------
  // Single statement parsing
  // -------------------------------------------------------------------------

  /**
   * Parse a single statement (which may be a compound statement like if/for/while
   * that consumes multiple lines).
   *
   * @return Blockly XML fragment for the statement
   */
  private String parseStatementBlock(ParseState state, int expectedIndent)
      throws PseudocodeParseException {
    if (!state.hasMore()) {
      return null;
    }

    Line line = state.peek();

    // Skip section headers and blank/comment lines
    if (line.text.startsWith("==") || line.text.startsWith("#")
        || line.text.isEmpty()) {
      state.advance();
      return null;
    }

    String text = line.text;

    // ---- break ----
    if (text.equalsIgnoreCase("break")) {
      state.advance();
      return xmlGen.generateBreak();
    }

    // ---- close screen (no value) ----
    if (text.equalsIgnoreCase("close screen")) {
      state.advance();
      return generateSimpleBlock("controls_closeScreen");
    }

    // ---- close application ----
    if (text.equalsIgnoreCase("close application")) {
      state.advance();
      return generateSimpleBlock("controls_closeApplication");
    }

    // ---- if/else if/else ----
    Matcher m = IF_PATTERN.matcher(text);
    if (m.matches()) {
      return parseIfStatement(state);
    }

    // ---- for each dict ----
    m = FOR_EACH_DICT_PATTERN.matcher(text);
    if (m.matches()) {
      return parseForEachDict(state, m);
    }

    // ---- for each ----
    m = FOR_EACH_PATTERN.matcher(text);
    if (m.matches()) {
      return parseForEach(state, m);
    }

    // ---- for range ----
    m = FOR_RANGE_PATTERN.matcher(text);
    if (m.matches()) {
      return parseForRange(state, m);
    }

    // ---- while ----
    m = WHILE_PATTERN.matcher(text);
    if (m.matches()) {
      return parseWhile(state, m);
    }

    // ---- initialize local (statement) ----
    m = INIT_LOCAL_PATTERN.matcher(text);
    if (m.matches()) {
      return parseLocalDeclStatement(state, m);
    }

    // ---- initialize global ----
    m = INIT_GLOBAL_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String name = m.group(1);
      String valueXml = parseExprInternal(m.group(2));
      return xmlGen.generateGlobalDeclaration(name, valueXml);
    }

    // ---- set global ----
    m = SET_GLOBAL_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String name = m.group(1);
      String valueXml = parseExprInternal(m.group(2));
      return xmlGen.generateGlobalSet(name, valueXml);
    }

    // ---- set local ----
    m = SET_LOCAL_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String name = m.group(1);
      String valueXml = parseExprInternal(m.group(2));
      return xmlGen.generateLocalSet(name, valueXml);
    }

    // ---- set generic property (must come before set component property) ----
    m = SET_GENERIC_PROP_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String typeName = m.group(1);
      String propName = m.group(2);
      String compExprStr = m.group(3);
      String valueExprStr = m.group(4);
      String compXml = parseExprInternal(compExprStr);
      String valueXml = parseExprInternal(valueExprStr);
      return xmlGen.generateGenericPropertySet(typeName, propName, compXml, valueXml);
    }

    // ---- set component property ----
    m = SET_COMP_PROP_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String instName = m.group(1);
      String propName = m.group(2);
      String valueXml = parseExprInternal(m.group(3));
      return xmlGen.generatePropertySet(instName, propName, valueXml);
    }

    // ---- call generic method ----
    m = CALL_GENERIC_METHOD_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String typeName = m.group(1);
      String methodName = m.group(2);
      String compExprStr = m.group(3);
      String argsStr = m.group(4);
      String compXml = parseExprInternal(compExprStr);
      List<String> argXmls = parseArgList(argsStr);
      return xmlGen.generateGenericMethodCall(typeName, methodName, compXml, argXmls, false);
    }

    // ---- call component method ----
    m = CALL_COMP_METHOD_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String instOrType = m.group(1);
      String methodName = m.group(2);
      String argsStr = m.group(3);
      List<String> argXmls = parseArgList(argsStr);

      // Heuristic: if it contains a dot-less name, it's a component method call.
      // Procedure calls are handled separately.
      return xmlGen.generateMethodCall(instOrType, methodName, argXmls, false);
    }

    // ---- call procedure ----
    m = CALL_PROC_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String procName = m.group(1);
      String argsStr = m.group(2);
      List<String> argXmls = parseArgList(argsStr);
      return xmlGen.generateProcedureCall(procName, argXmls, false);
    }

    // ---- evaluate but ignore ----
    m = EVAL_BUT_IGNORE_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String valueXml = parseExprInternal(m.group(1));
      return generateEvalButIgnore(valueXml);
    }

    // ---- list mutations ----
    m = ADD_ITEMS_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      return parseAddItems(m);
    }

    m = INSERT_ITEM_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String itemXml = parseExprInternal(m.group(1));
      String listXml = parseExprInternal(m.group(2));
      String indexXml = parseExprInternal(m.group(3));
      return generateListInsertItem(listXml, indexXml, itemXml);
    }

    m = REPLACE_ITEM_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String indexXml = parseExprInternal(m.group(1));
      String listXml = parseExprInternal(m.group(2));
      String itemXml = parseExprInternal(m.group(3));
      return generateListReplaceItem(listXml, indexXml, itemXml);
    }

    m = REMOVE_ITEM_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String indexXml = parseExprInternal(m.group(1));
      String listXml = parseExprInternal(m.group(2));
      return generateListRemoveItem(listXml, indexXml);
    }

    m = APPEND_LIST_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String list1Xml = parseExprInternal(m.group(1));
      String list0Xml = parseExprInternal(m.group(2));
      return generateListAppend(list0Xml, list1Xml);
    }

    // ---- dict mutations ----
    m = SET_PATH_IN_DICT_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String pathXml = parseExprInternal(m.group(1));
      String dictXml = parseExprInternal(m.group(2));
      String valueXml = parseExprInternal(m.group(3));
      return generateDictSetPath(dictXml, pathXml, valueXml);
    }

    m = SET_KEY_IN_DICT_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String keyXml = parseExprInternal(m.group(1));
      String dictXml = parseExprInternal(m.group(2));
      String valueXml = parseExprInternal(m.group(3));
      return generateDictSetPair(dictXml, keyXml, valueXml);
    }

    m = DELETE_KEY_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String keyXml = parseExprInternal(m.group(1));
      String dictXml = parseExprInternal(m.group(2));
      return generateDictDeletePair(dictXml, keyXml);
    }

    // ---- screen navigation ----
    m = OPEN_SCREEN_VALUE_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String screenXml = parseExprInternal(m.group(1));
      String valueXml = parseExprInternal(m.group(2));
      return generateOpenScreenWithValue(screenXml, valueXml);
    }

    m = OPEN_SCREEN_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String screenXml = parseExprInternal(m.group(1));
      return generateOpenScreen(screenXml);
    }

    m = CLOSE_SCREEN_PLAIN_TEXT_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String textXml = parseExprInternal(m.group(1));
      return generateCloseScreenPlainText(textXml);
    }

    m = CLOSE_SCREEN_VALUE_PATTERN.matcher(text);
    if (m.matches()) {
      state.advance();
      String valueXml = parseExprInternal(m.group(1));
      return generateCloseScreenWithValue(valueXml);
    }

    // ---- Fallback: unknown statement ----
    state.advance();
    throw new PseudocodeParseException("Unrecognized statement: " + text, line.lineNumber);
  }

  // -------------------------------------------------------------------------
  // Compound statement parsers
  // -------------------------------------------------------------------------

  private String parseIfStatement(ParseState state) throws PseudocodeParseException {
    int baseIndent = state.peek().indent;

    List<String> conditionXmls = new ArrayList<>();
    List<String> thenBranchXmls = new ArrayList<>();
    String elseBranchXml = null;

    // Parse "if <expr> then"
    Matcher m = IF_PATTERN.matcher(state.consume().text);
    if (!m.matches()) {
      throw new PseudocodeParseException("Expected 'if ... then'");
    }
    conditionXmls.add(parseExprInternal(m.group(1)));
    thenBranchXmls.add(parseBody(state, baseIndent + 1));

    // Parse "else if" and "else" clauses
    while (state.hasMore() && state.peek().indent == baseIndent) {
      String nextText = state.peek().text;

      Matcher elseIfM = ELSE_IF_PATTERN.matcher(nextText);
      if (elseIfM.matches()) {
        state.advance();
        conditionXmls.add(parseExprInternal(elseIfM.group(1)));
        thenBranchXmls.add(parseBody(state, baseIndent + 1));
        continue;
      }

      if (nextText.equalsIgnoreCase("else")) {
        state.advance();
        elseBranchXml = parseBody(state, baseIndent + 1);
        break;
      }

      break; // Not an else clause; stop
    }

    return xmlGen.generateControlsIf(conditionXmls, thenBranchXmls, elseBranchXml);
  }

  private String parseForEach(ParseState state, Matcher m) throws PseudocodeParseException {
    int baseIndent = state.peek().indent;
    state.advance();

    String varName = m.group(1);
    String listExpr = m.group(2);
    String listXml = parseExprInternal(listExpr);
    String bodyXml = parseBody(state, baseIndent + 1);

    return xmlGen.generateForEach(varName, listXml, bodyXml);
  }

  private String parseForEachDict(ParseState state, Matcher m) throws PseudocodeParseException {
    int baseIndent = state.peek().indent;
    state.advance();

    String keyVar = m.group(1);
    String valueVar = m.group(2);
    String dictExpr = m.group(3);
    String dictXml = parseExprInternal(dictExpr);
    String bodyXml = parseBody(state, baseIndent + 1);

    return generateForEachDict(keyVar, valueVar, dictXml, bodyXml);
  }

  private String parseForRange(ParseState state, Matcher m) throws PseudocodeParseException {
    int baseIndent = state.peek().indent;
    state.advance();

    String varName = m.group(1);
    String fromXml = parseExprInternal(m.group(2));
    String toXml = parseExprInternal(m.group(3));
    String byXml = parseExprInternal(m.group(4));
    String bodyXml = parseBody(state, baseIndent + 1);

    return xmlGen.generateForRange(varName, fromXml, toXml, byXml, bodyXml);
  }

  private String parseWhile(ParseState state, Matcher m) throws PseudocodeParseException {
    int baseIndent = state.peek().indent;
    state.advance();

    String condXml = parseExprInternal(m.group(1));
    String bodyXml = parseBody(state, baseIndent + 1);

    return xmlGen.generateWhile(condXml, bodyXml);
  }

  private String parseLocalDeclStatement(ParseState state, Matcher m)
      throws PseudocodeParseException {
    int baseIndent = state.peek().indent;
    state.advance();

    String varName = m.group(1);
    String initExpr = m.group(2);
    String initXml = parseExprInternal(initExpr);
    String bodyXml = parseBody(state, baseIndent + 1);

    return xmlGen.generateLocalDeclarationStatement(varName, initXml, bodyXml);
  }

  // -------------------------------------------------------------------------
  // Add items (variadic) parser
  // -------------------------------------------------------------------------

  private String parseAddItems(Matcher m) throws PseudocodeParseException {
    String itemsPart = m.group(1);
    String listExpr = m.group(2);
    String listXml = parseExprInternal(listExpr);

    List<String> items = splitTopLevelCommas(itemsPart);
    List<String> itemXmls = new ArrayList<>();
    for (String item : items) {
      itemXmls.add(parseExprInternal(item.trim()));
    }

    return generateListAddItems(listXml, itemXmls);
  }

  // -------------------------------------------------------------------------
  // Expression parsing
  // -------------------------------------------------------------------------

  /**
   * Internal helper that creates an ExprParser for the given expression string
   * and returns the generated XML.
   */
  private String parseExprInternal(String exprStr) throws PseudocodeParseException {
    if (exprStr == null || exprStr.trim().isEmpty()) {
      return xmlGen.generateText("");
    }
    ExprParser ep = new ExprParser(exprStr.trim());
    return ep.parseExpression();
  }

  /**
   * Parse a comma-separated list of argument expressions and return their
   * XML representations.
   */
  private List<String> parseArgList(String argsStr) throws PseudocodeParseException {
    if (argsStr == null || argsStr.trim().isEmpty()) {
      return Collections.emptyList();
    }
    List<String> parts = splitTopLevelCommas(argsStr);
    List<String> result = new ArrayList<>();
    for (String part : parts) {
      result.add(parseExprInternal(part.trim()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Expression parser (recursive descent with operator precedence)
  // -------------------------------------------------------------------------

  /**
   * Recursive-descent expression parser that operates on a character stream.
   * Handles:
   * <ul>
   *   <li>Parenthesized infix: {@code (a + b + ...)}, {@code (a and b)}</li>
   *   <li>Prefix functions: {@code negate e}, {@code not e}, {@code abs e}</li>
   *   <li>Function-call syntax: {@code join(a, b)}, {@code list(a, b)}</li>
   *   <li>Literals: numbers, strings, booleans</li>
   *   <li>Variables: {@code global x}, {@code local x}, bare names</li>
   *   <li>Component expressions: {@code Comp.Prop}, {@code call Comp.Method(...)}</li>
   * </ul>
   */
  private class ExprParser {
    private final String src;
    private int pos;

    ExprParser(String src) {
      this.src = src;
      this.pos = 0;
    }

    String parseExpression() throws PseudocodeParseException {
      skipWhitespace();
      String result = parseAtom();
      skipWhitespace();
      return result;
    }

    private String parseAtom() throws PseudocodeParseException {
      skipWhitespace();
      if (pos >= src.length()) {
        throw new PseudocodeParseException("Unexpected end of expression in: " + src);
      }

      char c = src.charAt(pos);

      // ---- Parenthesized infix expression ----
      if (c == '(') {
        return parseParenExpr();
      }

      // ---- String literal ----
      if (c == '"') {
        return parseStringLiteral();
      }

      // ---- Numeric literal (including negative) ----
      if (Character.isDigit(c) || (c == '-' && pos + 1 < src.length()
          && Character.isDigit(src.charAt(pos + 1)))) {
        return parseNumberLiteral();
      }

      // ---- Hex literal ----
      if (c == '0' && pos + 1 < src.length()
          && (src.charAt(pos + 1) == 'x' || src.charAt(pos + 1) == 'X')) {
        return parseNumberLiteral();
      }

      // ---- Keyword-based expressions ----
      return parseKeywordExpression();
    }

    // -- Parenthesized infix: (a op b op c ...) --
    private String parseParenExpr() throws PseudocodeParseException {
      expect('(');
      skipWhitespace();
      String first = parseAtom();
      skipWhitespace();

      if (pos < src.length() && src.charAt(pos) == ')') {
        // Just a grouped expression
        expect(')');
        return first;
      }

      // Determine operator
      String op = peekOperator();
      if (op == null) {
        // No recognized operator; treat as grouped single expression
        expect(')');
        return first;
      }

      switch (op) {
        case "+": {
          List<String> operands = new ArrayList<>();
          operands.add(first);
          while (pos < src.length() && tryConsume("+")) {
            skipWhitespace();
            operands.add(parseAtom());
            skipWhitespace();
          }
          expect(')');
          return xmlGen.generateMathAdd(operands);
        }
        case "-": {
          consume("-");
          skipWhitespace();
          String second = parseAtom();
          skipWhitespace();
          expect(')');
          return xmlGen.generateMathSubtract(first, second);
        }
        case "*": {
          List<String> operands = new ArrayList<>();
          operands.add(first);
          while (pos < src.length() && tryConsume("*")) {
            skipWhitespace();
            operands.add(parseAtom());
            skipWhitespace();
          }
          expect(')');
          return xmlGen.generateMathMultiply(operands);
        }
        case "/": {
          consume("/");
          skipWhitespace();
          String second = parseAtom();
          skipWhitespace();
          expect(')');
          return xmlGen.generateMathDivision(first, second);
        }
        case "^": {
          consume("^");
          skipWhitespace();
          String second = parseAtom();
          skipWhitespace();
          expect(')');
          return generateMathPower(first, second);
        }
        case "=": {
          consume("=");
          skipWhitespace();
          String second = parseAtom();
          skipWhitespace();
          expect(')');
          return xmlGen.generateMathCompare("EQ", first, second);
        }
        case "!=": {
          consume("!=");
          skipWhitespace();
          String second = parseAtom();
          skipWhitespace();
          expect(')');
          return xmlGen.generateMathCompare("NEQ", first, second);
        }
        case "<=": {
          consume("<=");
          skipWhitespace();
          String second = parseAtom();
          skipWhitespace();
          expect(')');
          return xmlGen.generateMathCompare("LTE", first, second);
        }
        case ">=": {
          consume(">=");
          skipWhitespace();
          String second = parseAtom();
          skipWhitespace();
          expect(')');
          return xmlGen.generateMathCompare("GTE", first, second);
        }
        case "<": {
          consume("<");
          skipWhitespace();
          String second = parseAtom();
          skipWhitespace();
          expect(')');
          return xmlGen.generateMathCompare("LT", first, second);
        }
        case ">": {
          consume(">");
          skipWhitespace();
          String second = parseAtom();
          skipWhitespace();
          expect(')');
          return xmlGen.generateMathCompare("GT", first, second);
        }
        case "and": {
          List<String> operands = new ArrayList<>();
          operands.add(first);
          while (pos < src.length() && tryConsumeWord("and")) {
            skipWhitespace();
            operands.add(parseAtom());
            skipWhitespace();
          }
          expect(')');
          return xmlGen.generateLogicOperation("AND", operands);
        }
        case "or": {
          List<String> operands = new ArrayList<>();
          operands.add(first);
          while (pos < src.length() && tryConsumeWord("or")) {
            skipWhitespace();
            operands.add(parseAtom());
            skipWhitespace();
          }
          expect(')');
          return xmlGen.generateLogicOperation("OR", operands);
        }
        default:
          expect(')');
          return first;
      }
    }

    // -- String literal --
    private String parseStringLiteral() throws PseudocodeParseException {
      expect('"');
      StringBuilder sb = new StringBuilder();
      while (pos < src.length() && src.charAt(pos) != '"') {
        if (src.charAt(pos) == '\\' && pos + 1 < src.length()) {
          pos++;
          char escaped = src.charAt(pos);
          switch (escaped) {
            case 'n': sb.append('\n'); break;
            case 't': sb.append('\t'); break;
            case '\\': sb.append('\\'); break;
            case '"': sb.append('"'); break;
            default: sb.append(escaped); break;
          }
        } else {
          sb.append(src.charAt(pos));
        }
        pos++;
      }
      if (pos >= src.length()) {
        throw new PseudocodeParseException("Unterminated string literal in: " + src);
      }
      expect('"');
      return xmlGen.generateText(sb.toString());
    }

    // -- Number literal --
    private String parseNumberLiteral() {
      int start = pos;
      if (pos < src.length() && src.charAt(pos) == '-') {
        pos++;
      }
      // Hex
      if (pos + 1 < src.length() && src.charAt(pos) == '0'
          && (src.charAt(pos + 1) == 'x' || src.charAt(pos + 1) == 'X')) {
        pos += 2;
        while (pos < src.length() && isHexDigit(src.charAt(pos))) {
          pos++;
        }
      } else {
        while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
          pos++;
        }
        if (pos < src.length() && src.charAt(pos) == '.') {
          pos++;
          while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
            pos++;
          }
        }
      }
      return xmlGen.generateMathNumber(src.substring(start, pos));
    }

    // -- Keyword-based expressions --
    private String parseKeywordExpression() throws PseudocodeParseException {
      // Boolean literals
      if (tryConsumeWord("true")) {
        return xmlGen.generateLogicBoolean(true);
      }
      if (tryConsumeWord("false")) {
        return xmlGen.generateLogicBoolean(false);
      }

      // "global <name>"
      if (tryConsumeWord("global")) {
        skipWhitespace();
        String name = readWord();
        return xmlGen.generateGlobalGet(name);
      }

      // "local <name>"
      if (tryConsumeWord("local")) {
        skipWhitespace();
        String name = readWord();
        return xmlGen.generateLocalGet(name);
      }

      // "get start value"
      if (tryConsumePhrase("get start value")) {
        return generateGetStartValue();
      }

      // "component <Name>"
      if (tryConsumeWord("component")) {
        skipWhitespace();
        String name = readWord();
        return xmlGen.generateComponentBlock(name);
      }

      // "all components of type <Type>"
      if (tryConsumePhrase("all components of type")) {
        skipWhitespace();
        String typeName = readWord();
        return generateAllComponentsOfType(typeName);
      }

      // "not <expr>"
      if (tryConsumeWord("not")) {
        skipWhitespace();
        String operand = parseAtom();
        return xmlGen.generateLogicNegate(operand);
      }

      // "negate <expr>"
      if (tryConsumeWord("negate")) {
        skipWhitespace();
        String operand = parseAtom();
        return generateMathSingle("NEG", operand);
      }

      // Math single-argument functions
      for (Map.Entry<String, String> entry : MATH_SINGLE_OPS.entrySet()) {
        String keyword = entry.getValue();
        String opKey = entry.getKey();
        if ("NEG".equals(opKey)) {
          continue; // handled above
        }
        if (tryConsumeWord(keyword)) {
          skipWhitespace();
          String operand = parseAtom();
          return generateMathSingle(opKey, operand);
        }
      }

      // "e^ <expr>"
      if (tryConsumePhrase("e^")) {
        skipWhitespace();
        String operand = parseAtom();
        return generateMathSingle("EXP", operand);
      }

      // Trig functions
      for (Map.Entry<String, String> entry : TRIG_OPS.entrySet()) {
        if (tryConsumeWord(entry.getValue())) {
          skipWhitespace();
          String operand = parseAtom();
          return generateMathTrig(entry.getKey(), operand);
        }
      }

      // "atan2 <y> <x>"
      if (tryConsumeWord("atan2")) {
        skipWhitespace();
        String y = parseAtom();
        skipWhitespace();
        String x = parseAtom();
        return generateMathAtan2(y, x);
      }

      // "modulo <a> <b>"
      if (tryConsumeWord("modulo")) {
        skipWhitespace();
        String a = parseAtom();
        skipWhitespace();
        String b = parseAtom();
        return generateMathDivide("MODULO", a, b);
      }

      // "remainder <a> <b>"
      if (tryConsumeWord("remainder")) {
        skipWhitespace();
        String a = parseAtom();
        skipWhitespace();
        String b = parseAtom();
        return generateMathDivide("REMAINDER", a, b);
      }

      // "quotient <a> <b>"
      if (tryConsumeWord("quotient")) {
        skipWhitespace();
        String a = parseAtom();
        skipWhitespace();
        String b = parseAtom();
        return generateMathDivide("QUOTIENT", a, b);
      }

      // "bitwise and/or/xor <a> <b>"
      if (tryConsumeWord("bitwise")) {
        skipWhitespace();
        String kind;
        if (tryConsumeWord("and")) {
          kind = "BITAND";
        } else if (tryConsumeWord("or")) {
          kind = "BITOR";
        } else if (tryConsumeWord("xor")) {
          kind = "BITXOR";
        } else {
          throw new PseudocodeParseException("Expected and/or/xor after 'bitwise' in: " + src);
        }
        skipWhitespace();
        String a = parseAtom();
        skipWhitespace();
        String b = parseAtom();
        return generateMathBitwise(kind, a, b);
      }

      // "random integer from <a> to <b>"
      if (tryConsumePhrase("random integer from")) {
        skipWhitespace();
        String from = parseAtom();
        skipWhitespace();
        consumeWord("to");
        skipWhitespace();
        String to = parseAtom();
        return generateRandomInt(from, to);
      }

      // "random fraction"
      if (tryConsumePhrase("random fraction")) {
        return generateRandomFraction();
      }

      // "format as decimal <expr> places <expr>"
      if (tryConsumePhrase("format as decimal")) {
        skipWhitespace();
        String num = parseAtom();
        skipWhitespace();
        consumeWord("places");
        skipWhitespace();
        String places = parseAtom();
        return generateFormatDecimal(num, places);
      }

      // "convert number <expr> from base <expr> to base <expr>"
      if (tryConsumePhrase("convert number")) {
        skipWhitespace();
        String num = parseAtom();
        skipWhitespace();
        consumePhrase("from base");
        skipWhitespace();
        String fromBase = parseAtom();
        skipWhitespace();
        consumePhrase("to base");
        skipWhitespace();
        String toBase = parseAtom();
        return generateConvertNumber(num, fromBase, toBase);
      }

      // "is a number <expr>", "is a base10 <expr>", etc.
      if (tryConsumePhrase("is a number")) {
        skipWhitespace();
        return generateMathIsA("math_is_a_number", parseAtom());
      }
      if (tryConsumePhrase("is a base10")) {
        skipWhitespace();
        return generateMathIsA("math_is_a_decimal", parseAtom());
      }
      if (tryConsumePhrase("is a hexadecimal")) {
        skipWhitespace();
        return generateMathIsA("math_is_a_hexadecimal", parseAtom());
      }
      if (tryConsumePhrase("is a binary")) {
        skipWhitespace();
        return generateMathIsA("math_is_a_binary", parseAtom());
      }
      if (tryConsumePhrase("is a list")) {
        skipWhitespace();
        return generateIsAList(parseAtom());
      }

      // "min(...)" and "max(...)"
      if (tryConsumeWord("min") && pos < src.length() && src.charAt(pos) == '(') {
        return parseFunctionCall("min");
      }
      if (tryConsumeWord("max") && pos < src.length() && src.charAt(pos) == '(') {
        return parseFunctionCall("max");
      }

      // "avg of list <expr>", "sum of list <expr>", etc.
      if (tryConsumePhrase("avg of list")) {
        skipWhitespace();
        return generateMathOnList("AVG", parseAtom());
      }
      if (tryConsumePhrase("sum of list")) {
        skipWhitespace();
        return generateMathOnList("SUM", parseAtom());
      }
      if (tryConsumePhrase("min of list")) {
        skipWhitespace();
        return generateMathOnList("MIN", parseAtom());
      }
      if (tryConsumePhrase("max of list")) {
        skipWhitespace();
        return generateMathOnList("MAX", parseAtom());
      }

      // ---- Text operations ----
      if (tryConsumePhrase("join(") || tryConsumePhrase("join (")) {
        // Back up to parse as function call
        pos -= 1; // re-include the '('
        return parseFunctionCallArgs("text_join", "ADD");
      }
      // More robust: check if "join" followed by "("
      if (tryConsumeWord("join") && pos < src.length() && src.charAt(pos) == '(') {
        return parseFunctionCallArgs("text_join", "ADD");
      }

      if (tryConsumePhrase("length of text")) {
        skipWhitespace();
        return generateTextLength(parseAtom());
      }
      if (tryConsumePhrase("is text empty")) {
        skipWhitespace();
        return generateTextIsEmpty(parseAtom());
      }
      if (tryConsumePhrase("compare text")) {
        skipWhitespace();
        String a = parseAtom();
        skipWhitespace();
        String b = parseAtom();
        return generateTextCompare(a, b);
      }
      if (tryConsumeWord("trim")) {
        skipWhitespace();
        return generateTextTrim(parseAtom());
      }
      if (tryConsumeWord("upcase")) {
        skipWhitespace();
        return generateTextChangeCase("UPCASE", parseAtom());
      }
      if (tryConsumeWord("downcase")) {
        skipWhitespace();
        return generateTextChangeCase("DOWNCASE", parseAtom());
      }
      if (tryConsumePhrase("starts at text")) {
        skipWhitespace();
        String text = parseAtom();
        skipWhitespace();
        consumeWord("piece");
        skipWhitespace();
        String piece = parseAtom();
        return generateTextStartsAt(text, piece);
      }
      if (tryConsumePhrase("contains text")) {
        skipWhitespace();
        String text = parseAtom();
        skipWhitespace();
        consumeWord("piece");
        skipWhitespace();
        String piece = parseAtom();
        return generateTextContains("CONTAINS", text, piece);
      }

      // Text split variants (most specific first)
      if (tryConsumePhrase("split text")) {
        skipWhitespace();
        String text = parseAtom();
        skipWhitespace();
        return parseTextSplitVariant(text);
      }

      if (tryConsumePhrase("segment text")) {
        skipWhitespace();
        String text = parseAtom();
        skipWhitespace();
        consumeWord("start");
        skipWhitespace();
        String start = parseAtom();
        skipWhitespace();
        consumeWord("length");
        skipWhitespace();
        String length = parseAtom();
        return generateTextSegment(text, start, length);
      }

      if (tryConsumePhrase("replace all text")) {
        skipWhitespace();
        String text = parseAtom();
        skipWhitespace();
        consumeWord("segment");
        skipWhitespace();
        String seg = parseAtom();
        skipWhitespace();
        consumeWord("replacement");
        skipWhitespace();
        String rep = parseAtom();
        return generateTextReplaceAll(text, seg, rep);
      }

      if (tryConsumePhrase("reverse text")) {
        skipWhitespace();
        return generateTextReverse(parseAtom());
      }

      // ---- List operations ----
      if (tryConsumeWord("list") && pos < src.length() && src.charAt(pos) == '(') {
        return parseFunctionCallArgs("lists_create_with", "ADD");
      }

      if (tryConsumePhrase("select item")) {
        skipWhitespace();
        String list = parseAtom();
        skipWhitespace();
        consumeWord("index");
        skipWhitespace();
        String index = parseAtom();
        return generateListSelectItem(list, index);
      }

      if (tryConsumePhrase("index of")) {
        skipWhitespace();
        String list = parseAtom();
        skipWhitespace();
        consumeWord("thing");
        skipWhitespace();
        String thing = parseAtom();
        return generateListIndexOf(list, thing);
      }

      if (tryConsumePhrase("pick random")) {
        skipWhitespace();
        return generateListPickRandom(parseAtom());
      }

      if (tryConsumePhrase("length of list")) {
        skipWhitespace();
        return generateListLength(parseAtom());
      }

      if (tryConsumePhrase("is list empty")) {
        skipWhitespace();
        return generateListIsEmpty(parseAtom());
      }

      if (tryConsumePhrase("is in list")) {
        skipWhitespace();
        String list = parseAtom();
        skipWhitespace();
        consumeWord("thing");
        skipWhitespace();
        String thing = parseAtom();
        return generateListIsIn(list, thing);
      }

      if (tryConsumePhrase("copy list")) {
        skipWhitespace();
        return generateListCopy(parseAtom());
      }

      if (tryConsumePhrase("reverse list")) {
        skipWhitespace();
        return generateListReverse(parseAtom());
      }

      if (tryConsumePhrase("csv row to list")) {
        skipWhitespace();
        return generateCsvRowToList(parseAtom());
      }
      if (tryConsumePhrase("list to csv row")) {
        skipWhitespace();
        return generateListToCsvRow(parseAtom());
      }
      if (tryConsumePhrase("csv table to list")) {
        skipWhitespace();
        return generateCsvTableToList(parseAtom());
      }
      if (tryConsumePhrase("list to csv table")) {
        skipWhitespace();
        return generateListToCsvTable(parseAtom());
      }

      if (tryConsumePhrase("lookup in pairs")) {
        skipWhitespace();
        String pairs = parseAtom();
        skipWhitespace();
        consumeWord("key");
        skipWhitespace();
        String key = parseAtom();
        skipWhitespace();
        consumeWord("notFound");
        skipWhitespace();
        String def = parseAtom();
        return generateLookupInPairs(pairs, key, def);
      }

      if (tryConsumePhrase("join items")) {
        skipWhitespace();
        String list = parseAtom();
        skipWhitespace();
        consumeWord("separator");
        skipWhitespace();
        String sep = parseAtom();
        return generateJoinWithSeparator(list, sep);
      }

      if (tryConsumeWord("sort")) {
        skipWhitespace();
        return generateListSort(parseAtom());
      }

      if (tryConsumeWord("map")) {
        skipWhitespace();
        String var = readWord();
        skipWhitespace();
        consumeWord("over");
        skipWhitespace();
        String list = parseAtom();
        skipWhitespace();
        String expr = parseAtom();
        return generateListMap(var, list, expr);
      }

      if (tryConsumeWord("filter")) {
        skipWhitespace();
        String var = readWord();
        skipWhitespace();
        consumeWord("in");
        skipWhitespace();
        String list = parseAtom();
        skipWhitespace();
        String test = parseAtom();
        return generateListFilter(var, list, test);
      }

      if (tryConsumeWord("reduce")) {
        skipWhitespace();
        String var = readWord();
        skipWhitespace();
        consumeWord("over");
        skipWhitespace();
        String list = parseAtom();
        skipWhitespace();
        consumeWord("initial");
        skipWhitespace();
        String init = parseAtom();
        skipWhitespace();
        String combine = parseAtom();
        return generateListReduce(var, list, init, combine);
      }

      // ---- Dictionary operations ----
      if (tryConsumeWord("dict") && pos < src.length() && src.charAt(pos) == '(') {
        return parseDictLiteral();
      }

      if (tryConsumeWord("pair") && pos < src.length() && src.charAt(pos) == '(') {
        return parsePairLiteral();
      }

      if (tryConsumePhrase("lookup key")) {
        skipWhitespace();
        String key = parseAtom();
        skipWhitespace();
        consumeWord("in");
        skipWhitespace();
        String dict = parseAtom();
        skipWhitespace();
        consumeWord("notFound");
        skipWhitespace();
        String def = parseAtom();
        return generateDictLookup(key, dict, def);
      }

      if (tryConsumePhrase("lookup path")) {
        skipWhitespace();
        String path = parseAtom();
        skipWhitespace();
        consumeWord("in");
        skipWhitespace();
        String dict = parseAtom();
        skipWhitespace();
        consumeWord("notFound");
        skipWhitespace();
        String def = parseAtom();
        return generateDictLookupPath(path, dict, def);
      }

      if (tryConsumePhrase("keys of")) {
        skipWhitespace();
        return generateDictGetters("KEYS", parseAtom());
      }
      if (tryConsumePhrase("values of")) {
        skipWhitespace();
        return generateDictGetters("VALUES", parseAtom());
      }

      if (tryConsumePhrase("is key in")) {
        skipWhitespace();
        String dict = parseAtom();
        skipWhitespace();
        consumeWord("key");
        skipWhitespace();
        String key = parseAtom();
        return generateDictIsKeyIn(dict, key);
      }

      if (tryConsumePhrase("length of dict")) {
        skipWhitespace();
        return generateDictLength(parseAtom());
      }

      if (tryConsumePhrase("alist to dict")) {
        skipWhitespace();
        return generateAlistToDict(parseAtom());
      }
      if (tryConsumePhrase("dict to alist")) {
        skipWhitespace();
        return generateDictToAlist(parseAtom());
      }

      if (tryConsumePhrase("combine dicts")) {
        skipWhitespace();
        String d1 = parseAtom();
        skipWhitespace();
        String d2 = parseAtom();
        return generateCombineDicts(d1, d2);
      }

      if (tryConsumePhrase("walk tree")) {
        skipWhitespace();
        String dict = parseAtom();
        skipWhitespace();
        if (tryConsumePhrase("all at level")) {
          skipWhitespace();
          String path = parseAtom();
          return generateDictWalkAll(dict, path);
        } else {
          consumeWord("path");
          skipWhitespace();
          String path = parseAtom();
          return generateDictWalkTree(dict, path);
        }
      }

      // ---- Colors ----
      if (tryConsumeWord("color")) {
        skipWhitespace();
        return parseColorExpression();
      }

      if (tryConsumePhrase("make color") && pos < src.length() && src.charAt(pos) == '(') {
        return parseMakeColor();
      }

      if (tryConsumePhrase("split color")) {
        skipWhitespace();
        return generateSplitColor(parseAtom());
      }

      // ---- Helper blocks ----
      if (tryConsumeWord("option")) {
        skipWhitespace();
        return parseOptionExpression();
      }
      if (tryConsumePhrase("screen name")) {
        skipWhitespace();
        String name = readWord();
        return xmlGen.generateHelpersScreenNames(name);
      }
      if (tryConsumeWord("asset")) {
        skipWhitespace();
        String name = readWord();
        return xmlGen.generateHelpersAssets(name);
      }

      // ---- Composite expressions ----
      // "if <cond> then <expr> else <expr>" (ternary)
      if (tryConsumeWord("if")) {
        skipWhitespace();
        String cond = parseAtom();
        skipWhitespace();
        consumeWord("then");
        skipWhitespace();
        String thenExpr = parseAtom();
        skipWhitespace();
        consumeWord("else");
        skipWhitespace();
        String elseExpr = parseAtom();
        return xmlGen.generateControlsChoose(cond, thenExpr, elseExpr);
      }

      // "do ... then return <expr>"  -- simplified handling
      if (tryConsumeWord("do")) {
        skipWhitespace();
        // skip until "then return"
        consumeWord("then");
        skipWhitespace();
        consumeWord("return");
        skipWhitespace();
        String retExpr = parseAtom();
        return xmlGen.generateControlsDoThenReturn("", retExpr);
      }

      // "initialize local <name> to <expr> in <expr>"
      if (tryConsumePhrase("initialize local")) {
        skipWhitespace();
        String name = readWord();
        skipWhitespace();
        consumeWord("to");
        skipWhitespace();
        String initVal = parseAtom();
        skipWhitespace();
        consumeWord("in");
        skipWhitespace();
        String body = parseAtom();
        return xmlGen.generateLocalDeclarationExpression(name, initVal, body);
      }

      // "call" expression -- procedure or method
      if (tryConsumeWord("call")) {
        skipWhitespace();
        return parseCallExpression();
      }

      // ---- Bare identifier / Component.Property / Component.Property of ... ----
      return parseIdentifierOrComponentExpr();
    }

    // Parse "call <X>.<Y>(...)" or "call <X>(<args>)" in expression context
    private String parseCallExpression() throws PseudocodeParseException {
      String first = readWord();
      skipWhitespace();

      if (pos < src.length() && src.charAt(pos) == '.') {
        // call Component.Method(...)  or  call Type.Method of expr(...)
        pos++; // consume '.'
        String second = readWord();
        skipWhitespace();

        // Check for generic: "of <expr>"
        if (tryConsumeWord("of")) {
          skipWhitespace();
          // Need to parse comp expr and then args
          String compXml = parseAtom();
          skipWhitespace();
          List<String> argXmls = parseFunctionArgsList();
          return xmlGen.generateGenericMethodCall(first, second, compXml, argXmls, true);
        }

        // Instance method call
        List<String> argXmls = parseFunctionArgsList();
        return xmlGen.generateMethodCall(first, second, argXmls, true);
      }

      // Procedure call: call name(args)
      List<String> argXmls = parseFunctionArgsList();
      return xmlGen.generateProcedureCall(first, argXmls, true);
    }

    // Parse Component.Property or bare variable name
    private String parseIdentifierOrComponentExpr() throws PseudocodeParseException {
      String name = readWord();
      if (name.isEmpty()) {
        throw new PseudocodeParseException(
            "Expected identifier at position " + pos + " in: " + src);
      }
      skipWhitespace();

      // Check for Component.Property  or  Type.Property of expr
      if (pos < src.length() && src.charAt(pos) == '.') {
        pos++; // consume '.'
        String propOrMethod = readWord();
        skipWhitespace();

        // "of <expr>" means generic get
        if (tryConsumeWord("of")) {
          skipWhitespace();
          String compExpr = parseAtom();
          return xmlGen.generateGenericPropertyGet(name, propOrMethod, compExpr);
        }

        // Instance property get
        return xmlGen.generatePropertyGet(name, propOrMethod);
      }

      // Bare name -- could be a parameter or local variable reference
      return xmlGen.generateLocalGet(name);
    }

    // Parse text split variants after "split text <text>"
    private String parseTextSplitVariant(String textXml) throws PseudocodeParseException {
      if (tryConsumePhrase("at first any")) {
        skipWhitespace();
        String at = parseAtom();
        return generateTextSplit("SPLITATFIRSTOFANY", textXml, at);
      }
      if (tryConsumePhrase("at first")) {
        skipWhitespace();
        String at = parseAtom();
        return generateTextSplit("SPLITATFIRST", textXml, at);
      }
      if (tryConsumePhrase("at any")) {
        skipWhitespace();
        String at = parseAtom();
        return generateTextSplit("SPLITATANY", textXml, at);
      }
      if (tryConsumePhrase("at spaces")) {
        return generateTextSplit("SPLITATSPACES", textXml, null);
      }
      if (tryConsumeWord("at")) {
        skipWhitespace();
        String at = parseAtom();
        return generateTextSplit("SPLIT", textXml, at);
      }
      throw new PseudocodeParseException("Expected split variant (at/at first/at any/at spaces)");
    }

    // Parse color name after "color" keyword
    private String parseColorExpression() throws PseudocodeParseException {
      // Try two-word colors first
      int savedPos = pos;
      String word1 = readWord();
      skipWhitespace();

      if (pos < src.length() && !isAtEnd()) {
        int savedPos2 = pos;
        String word2 = readWord();
        String twoWord = word1 + " " + word2;
        String canonical = COLOR_NAMES.get(twoWord.toLowerCase());
        if (canonical != null) {
          return xmlGen.generateColorConstant(canonical);
        }
        pos = savedPos2; // backtrack to after first word
      }

      String canonical = COLOR_NAMES.get(word1.toLowerCase());
      if (canonical != null) {
        return xmlGen.generateColorConstant(canonical);
      }
      throw new PseudocodeParseException("Unknown color: " + word1);
    }

    // Parse make color(r, g, b) or make color(r, g, b, a)
    private String parseMakeColor() throws PseudocodeParseException {
      expect('(');
      List<String> args = new ArrayList<>();
      skipWhitespace();
      if (pos < src.length() && src.charAt(pos) != ')') {
        args.add(parseAtom());
        while (tryConsume(",")) {
          skipWhitespace();
          args.add(parseAtom());
          skipWhitespace();
        }
      }
      skipWhitespace();
      expect(')');
      return generateMakeColor(xmlGen.generateListsCreateWith(args));
    }

    // Parse option OptionList.Value
    private String parseOptionExpression() throws PseudocodeParseException {
      String optionPart = readWord();
      if (pos < src.length() && src.charAt(pos) == '.') {
        pos++;
        String value = readWord();
        return xmlGen.generateHelpersDropdown(optionPart, value);
      }
      throw new PseudocodeParseException("Expected OptionList.Value format after 'option'");
    }

    // Parse dict(k1: v1, k2: v2, ...)
    private String parseDictLiteral() throws PseudocodeParseException {
      expect('(');
      skipWhitespace();
      List<String> pairXmls = new ArrayList<>();
      if (pos < src.length() && src.charAt(pos) != ')') {
        pairXmls.add(parseDictPairInline());
        while (tryConsume(",")) {
          skipWhitespace();
          pairXmls.add(parseDictPairInline());
          skipWhitespace();
        }
      }
      skipWhitespace();
      expect(')');
      return xmlGen.generateDictionariesCreateWith(pairXmls);
    }

    // Parse k: v inside dict literal
    private String parseDictPairInline() throws PseudocodeParseException {
      skipWhitespace();
      String key = parseAtom();
      skipWhitespace();
      expect(':');
      skipWhitespace();
      String value = parseAtom();
      return xmlGen.generatePair(key, value);
    }

    // Parse pair(k, v)
    private String parsePairLiteral() throws PseudocodeParseException {
      expect('(');
      skipWhitespace();
      String key = parseAtom();
      skipWhitespace();
      expect(',');
      skipWhitespace();
      String value = parseAtom();
      skipWhitespace();
      expect(')');
      return xmlGen.generatePair(key, value);
    }

    // Parse function call: name(arg1, arg2, ...)
    private String parseFunctionCall(String funcName) throws PseudocodeParseException {
      List<String> args = parseFunctionArgsList();
      if ("min".equals(funcName)) {
        if (args.size() == 2) {
          return generateMathMin(args.get(0), args.get(1));
        }
      } else if ("max".equals(funcName)) {
        if (args.size() == 2) {
          return generateMathMax(args.get(0), args.get(1));
        }
      }
      throw new PseudocodeParseException(
          "Unknown function or wrong number of args: " + funcName + "(" + args.size() + ")");
    }

    // Parse generic function call args and create variadic block
    private String parseFunctionCallArgs(String blockType, String inputPrefix)
        throws PseudocodeParseException {
      List<String> args = parseFunctionArgsList();
      if ("text_join".equals(blockType)) {
        return xmlGen.generateTextJoin(args);
      }
      if ("lists_create_with".equals(blockType)) {
        return xmlGen.generateListsCreateWith(args);
      }
      throw new PseudocodeParseException("Unsupported function block type: " + blockType);
    }

    // Parse (arg1, arg2, ...) consuming the parens
    private List<String> parseFunctionArgsList() throws PseudocodeParseException {
      List<String> args = new ArrayList<>();
      skipWhitespace();
      if (pos >= src.length() || src.charAt(pos) != '(') {
        return args; // no parens = no args
      }
      expect('(');
      skipWhitespace();
      if (pos < src.length() && src.charAt(pos) != ')') {
        args.add(parseAtom());
        while (tryConsume(",")) {
          skipWhitespace();
          args.add(parseAtom());
          skipWhitespace();
        }
      }
      skipWhitespace();
      expect(')');
      return args;
    }

    // ---- Character-level helpers ----

    private void skipWhitespace() {
      while (pos < src.length() && src.charAt(pos) == ' ') {
        pos++;
      }
    }

    private void expect(char c) throws PseudocodeParseException {
      if (pos >= src.length() || src.charAt(pos) != c) {
        throw new PseudocodeParseException(
            "Expected '" + c + "' at position " + pos + " in: " + src);
      }
      pos++;
    }

    private boolean tryConsume(String s) {
      if (src.startsWith(s, pos)) {
        pos += s.length();
        skipWhitespace();
        return true;
      }
      return false;
    }

    private void consume(String s) throws PseudocodeParseException {
      if (!src.startsWith(s, pos)) {
        throw new PseudocodeParseException(
            "Expected '" + s + "' at position " + pos + " in: " + src);
      }
      pos += s.length();
    }

    /**
     * Try to consume a keyword word (must be followed by non-word char or end).
     */
    private boolean tryConsumeWord(String word) {
      if (src.regionMatches(true, pos, word, 0, word.length())) {
        int end = pos + word.length();
        if (end >= src.length() || !Character.isLetterOrDigit(src.charAt(end))) {
          pos = end;
          skipWhitespace();
          return true;
        }
      }
      return false;
    }

    private void consumeWord(String word) throws PseudocodeParseException {
      if (!tryConsumeWord(word)) {
        throw new PseudocodeParseException(
            "Expected '" + word + "' at position " + pos + " in: " + src);
      }
    }

    /**
     * Try to consume a multi-word phrase (case-insensitive).
     */
    private boolean tryConsumePhrase(String phrase) {
      if (src.regionMatches(true, pos, phrase, 0, phrase.length())) {
        int end = pos + phrase.length();
        if (end >= src.length() || !Character.isLetterOrDigit(src.charAt(end))) {
          pos = end;
          skipWhitespace();
          return true;
        }
      }
      return false;
    }

    private void consumePhrase(String phrase) throws PseudocodeParseException {
      if (!tryConsumePhrase(phrase)) {
        throw new PseudocodeParseException(
            "Expected '" + phrase + "' at position " + pos + " in: " + src);
      }
    }

    /**
     * Reads a contiguous word (letters, digits, underscores).
     */
    private String readWord() {
      int start = pos;
      while (pos < src.length()
          && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_')) {
        pos++;
      }
      return src.substring(start, pos);
    }

    private String peekOperator() {
      if (pos >= src.length()) {
        return null;
      }
      // Two-character operators first
      if (pos + 1 < src.length()) {
        String two = src.substring(pos, pos + 2);
        if ("!=".equals(two) || "<=".equals(two) || ">=".equals(two)) {
          return two;
        }
      }
      char c = src.charAt(pos);
      switch (c) {
        case '+': return "+";
        case '-': return "-";
        case '*': return "*";
        case '/': return "/";
        case '^': return "^";
        case '=': return "=";
        case '<': return "<";
        case '>': return ">";
        default:
          break;
      }
      // Check for "and" / "or"
      if (src.regionMatches(true, pos, "and", 0, 3)
          && (pos + 3 >= src.length() || !Character.isLetterOrDigit(src.charAt(pos + 3)))) {
        return "and";
      }
      if (src.regionMatches(true, pos, "or", 0, 2)
          && (pos + 2 >= src.length() || !Character.isLetterOrDigit(src.charAt(pos + 2)))) {
        return "or";
      }
      return null;
    }

    private boolean isAtEnd() {
      return pos >= src.length();
    }

    private boolean isHexDigit(char c) {
      return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
  }

  // -------------------------------------------------------------------------
  // Utility helpers
  // -------------------------------------------------------------------------

  /**
   * Split a comma-separated parameter list, trimming each element.
   */
  private List<String> splitParams(String paramsStr) {
    List<String> result = new ArrayList<>();
    if (paramsStr == null || paramsStr.trim().isEmpty()) {
      return result;
    }
    for (String p : paramsStr.split(",")) {
      String trimmed = p.trim();
      if (!trimmed.isEmpty()) {
        result.add(trimmed);
      }
    }
    return result;
  }

  /**
   * Split a string by top-level commas (respecting parentheses and quotes).
   */
  private List<String> splitTopLevelCommas(String s) {
    List<String> parts = new ArrayList<>();
    int depth = 0;
    boolean inString = false;
    int start = 0;

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
        inString = !inString;
      } else if (!inString) {
        if (c == '(' || c == '[') {
          depth++;
        } else if (c == ')' || c == ']') {
          depth--;
        } else if (c == ',' && depth == 0) {
          parts.add(s.substring(start, i));
          start = i + 1;
        }
      }
    }
    parts.add(s.substring(start));
    return parts;
  }

  // -------------------------------------------------------------------------
  // Low-level XML block generators (for blocks not in BlocksXmlGenerator)
  // -------------------------------------------------------------------------

  private String generateSimpleBlock(String blockType) {
    return "<block type=\"" + blockType + "\"></block>";
  }

  private String generateEvalButIgnore(String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_eval_but_ignore\">");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathPower(String aXml, String bXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_power\">");
    sb.append("<value name=\"A\">").append(aXml).append("</value>");
    sb.append("<value name=\"B\">").append(bXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathSingle(String op, String numXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_single\">");
    sb.append("<field name=\"OP\">").append(op).append("</field>");
    sb.append("<value name=\"NUM\">").append(numXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathTrig(String op, String numXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_trig\">");
    sb.append("<field name=\"OP\">").append(op).append("</field>");
    sb.append("<value name=\"NUM\">").append(numXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathAtan2(String yXml, String xXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_atan2\">");
    sb.append("<value name=\"Y\">").append(yXml).append("</value>");
    sb.append("<value name=\"X\">").append(xXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathDivide(String op, String aXml, String bXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_divide\">");
    sb.append("<field name=\"OP\">").append(op).append("</field>");
    sb.append("<value name=\"A\">").append(aXml).append("</value>");
    sb.append("<value name=\"B\">").append(bXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathBitwise(String op, String aXml, String bXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_bitwise\">");
    sb.append("<field name=\"OP\">").append(op).append("</field>");
    sb.append("<value name=\"A\">").append(aXml).append("</value>");
    sb.append("<value name=\"B\">").append(bXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateRandomInt(String fromXml, String toXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_random_int\">");
    sb.append("<value name=\"FROM\">").append(fromXml).append("</value>");
    sb.append("<value name=\"TO\">").append(toXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateRandomFraction() {
    return "<block type=\"math_random_float\"></block>";
  }

  private String generateFormatDecimal(String numXml, String placesXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_format_as_decimal\">");
    sb.append("<value name=\"NUM\">").append(numXml).append("</value>");
    sb.append("<value name=\"PLACES\">").append(placesXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateConvertNumber(String numXml, String fromXml, String toXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_convert_number\">");
    sb.append("<value name=\"NUM\">").append(numXml).append("</value>");
    sb.append("<value name=\"FROM\">").append(fromXml).append("</value>");
    sb.append("<value name=\"TO\">").append(toXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathIsA(String blockType, String numXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"").append(blockType).append("\">");
    sb.append("<value name=\"NUM\">").append(numXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathOnList(String op, String numXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_on_list\">");
    sb.append("<field name=\"OP\">").append(op).append("</field>");
    sb.append("<value name=\"NUM\">").append(numXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathMin(String aXml, String bXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_min\">");
    sb.append("<value name=\"A\">").append(aXml).append("</value>");
    sb.append("<value name=\"B\">").append(bXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateMathMax(String aXml, String bXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"math_max\">");
    sb.append("<value name=\"A\">").append(aXml).append("</value>");
    sb.append("<value name=\"B\">").append(bXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateGetStartValue() {
    return "<block type=\"controls_getStartValue\"></block>";
  }

  private String generateAllComponentsOfType(String typeName) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"helpers_type_list\">");
    sb.append("<mutation component_type=\"").append(escapeXml(typeName)).append("\"/>");
    sb.append("</block>");
    return sb.toString();
  }

  // ---- Text block generators ----

  private String generateTextLength(String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_length\">");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextIsEmpty(String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_isEmpty\">");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextCompare(String text1Xml, String text2Xml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_compare\">");
    sb.append("<value name=\"TEXT1\">").append(text1Xml).append("</value>");
    sb.append("<value name=\"TEXT2\">").append(text2Xml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextTrim(String textXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_trim\">");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextChangeCase(String op, String textXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_changeCase\">");
    sb.append("<field name=\"OP\">").append(op).append("</field>");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextStartsAt(String textXml, String pieceXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_starts_at\">");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    sb.append("<value name=\"PIECE\">").append(pieceXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextContains(String op, String textXml, String pieceXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_contains\">");
    sb.append("<field name=\"OP\">").append(op).append("</field>");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    sb.append("<value name=\"PIECE\">").append(pieceXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextSplit(String op, String textXml, String atXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_split\">");
    sb.append("<field name=\"OP\">").append(op).append("</field>");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    if (atXml != null) {
      sb.append("<value name=\"AT\">").append(atXml).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextSegment(String textXml, String startXml, String lengthXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_segment\">");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    sb.append("<value name=\"START\">").append(startXml).append("</value>");
    sb.append("<value name=\"LENGTH\">").append(lengthXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextReplaceAll(String textXml, String segXml, String repXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_replace_all\">");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    sb.append("<value name=\"SEGMENT\">").append(segXml).append("</value>");
    sb.append("<value name=\"REPLACEMENT\">").append(repXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateTextReverse(String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"text_reverse\">");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // ---- List block generators ----

  private String generateListSelectItem(String listXml, String indexXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_select_item\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"NUM\">").append(indexXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListIndexOf(String listXml, String itemXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_index_of\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"ITEM\">").append(itemXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListPickRandom(String listXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_pick_random_item\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListLength(String listXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_length\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListIsEmpty(String listXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_is_empty\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListIsIn(String listXml, String itemXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_is_in\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"ITEM\">").append(itemXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateIsAList(String itemXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_is_list\">");
    sb.append("<value name=\"ITEM\">").append(itemXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListCopy(String listXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_copy\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListReverse(String listXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_reverse\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateCsvRowToList(String textXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_from_csv_row\">");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListToCsvRow(String listXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_to_csv_row\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateCsvTableToList(String textXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_from_csv_table\">");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListToCsvTable(String listXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_to_csv_table\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateLookupInPairs(String pairsXml, String keyXml, String defaultXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_lookup_in_pairs\">");
    sb.append("<value name=\"LIST\">").append(pairsXml).append("</value>");
    sb.append("<value name=\"KEY\">").append(keyXml).append("</value>");
    sb.append("<value name=\"NOTFOUND\">").append(defaultXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateJoinWithSeparator(String listXml, String sepXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_join_with_separator\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"SEPARATOR\">").append(sepXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListSort(String listXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_sort\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListMap(String var, String listXml, String exprXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_map\">");
    sb.append("<field name=\"VAR\">").append(escapeXml(var)).append("</field>");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"TO\">").append(exprXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListFilter(String var, String listXml, String testXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_filter\">");
    sb.append("<field name=\"VAR\">").append(escapeXml(var)).append("</field>");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"TEST\">").append(testXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListReduce(String var, String listXml, String initXml, String combineXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_reduce\">");
    sb.append("<field name=\"VAR\">").append(escapeXml(var)).append("</field>");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"INITANSWER\">").append(initXml).append("</value>");
    sb.append("<value name=\"COMBINE\">").append(combineXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // List mutation blocks
  private String generateListAddItems(String listXml, List<String> itemXmls) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_add_items\">");
    sb.append("<mutation items=\"").append(itemXmls.size()).append("\"/>");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    for (int i = 0; i < itemXmls.size(); i++) {
      sb.append("<value name=\"ITEM").append(i).append("\">")
          .append(itemXmls.get(i)).append("</value>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListInsertItem(String listXml, String indexXml, String itemXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_insert_item\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"INDEX\">").append(indexXml).append("</value>");
    sb.append("<value name=\"ITEM\">").append(itemXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListReplaceItem(String listXml, String indexXml, String itemXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_replace_item\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"NUM\">").append(indexXml).append("</value>");
    sb.append("<value name=\"ITEM\">").append(itemXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListRemoveItem(String listXml, String indexXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_remove_item\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("<value name=\"INDEX\">").append(indexXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateListAppend(String list0Xml, String list1Xml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"lists_append_list\">");
    sb.append("<value name=\"LIST0\">").append(list0Xml).append("</value>");
    sb.append("<value name=\"LIST1\">").append(list1Xml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // ---- Dictionary block generators ----

  private String generateDictLookup(String keyXml, String dictXml, String defaultXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_lookup\">");
    sb.append("<value name=\"KEY\">").append(keyXml).append("</value>");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("<value name=\"NOTFOUND\">").append(defaultXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictLookupPath(String pathXml, String dictXml, String defaultXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_recursive_lookup\">");
    sb.append("<value name=\"KEYS\">").append(pathXml).append("</value>");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("<value name=\"NOTFOUND\">").append(defaultXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictGetters(String op, String dictXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_getters\">");
    sb.append("<field name=\"OP\">").append(op).append("</field>");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictIsKeyIn(String dictXml, String keyXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_is_key_in\">");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("<value name=\"KEY\">").append(keyXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictLength(String dictXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_length\">");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateAlistToDict(String listXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_alist_to_dict\">");
    sb.append("<value name=\"LIST\">").append(listXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictToAlist(String dictXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_dict_to_alist\">");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateCombineDicts(String d1Xml, String d2Xml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_combine_dicts\">");
    sb.append("<value name=\"DICT1\">").append(d1Xml).append("</value>");
    sb.append("<value name=\"DICT2\">").append(d2Xml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictWalkTree(String dictXml, String pathXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_walk_tree\">");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("<value name=\"PATH\">").append(pathXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictWalkAll(String dictXml, String pathXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_walk_all\">");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("<value name=\"PATH\">").append(pathXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictSetPair(String dictXml, String keyXml, String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_set_pair\">");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("<value name=\"KEY\">").append(keyXml).append("</value>");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictDeletePair(String dictXml, String keyXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_delete_pair\">");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("<value name=\"KEY\">").append(keyXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateDictSetPath(String dictXml, String pathXml, String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"dictionaries_set_value_for_key_path\">");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    sb.append("<value name=\"KEYS\">").append(pathXml).append("</value>");
    sb.append("<value name=\"VALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // ---- Color block generators ----

  private String generateMakeColor(String colorListXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"color_make_color\">");
    sb.append("<value name=\"COLORLIST\">").append(colorListXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateSplitColor(String colorXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"color_split_color\">");
    sb.append("<value name=\"COLOR\">").append(colorXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // ---- Screen navigation block generators ----

  private String generateOpenScreen(String screenXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_openAnotherScreen\">");
    sb.append("<value name=\"SCREEN\">").append(screenXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateOpenScreenWithValue(String screenXml, String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_openAnotherScreenWithStartValue\">");
    sb.append("<value name=\"SCREENNAME\">").append(screenXml).append("</value>");
    sb.append("<value name=\"STARTVALUE\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateCloseScreenWithValue(String valueXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_closeScreenWithValue\">");
    sb.append("<value name=\"RESULT\">").append(valueXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  private String generateCloseScreenPlainText(String textXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_closeScreenWithPlainText\">");
    sb.append("<value name=\"TEXT\">").append(textXml).append("</value>");
    sb.append("</block>");
    return sb.toString();
  }

  // ---- For each dict block generator ----

  private String generateForEachDict(String keyVar, String valueVar,
      String dictXml, String bodyXml) {
    StringBuilder sb = new StringBuilder();
    sb.append("<block type=\"controls_for_each_dict\">");
    sb.append("<field name=\"KEY\">").append(escapeXml(keyVar)).append("</field>");
    sb.append("<field name=\"VALUE\">").append(escapeXml(valueVar)).append("</field>");
    sb.append("<value name=\"DICT\">").append(dictXml).append("</value>");
    if (bodyXml != null && !bodyXml.isEmpty()) {
      sb.append("<statement name=\"DO\">").append(bodyXml).append("</statement>");
    }
    sb.append("</block>");
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // XML escape helper
  // -------------------------------------------------------------------------

  private String escapeXml(String text) {
    if (text == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch (c) {
        case '&':  sb.append("&amp;"); break;
        case '<':  sb.append("&lt;"); break;
        case '>':  sb.append("&gt;"); break;
        case '"':  sb.append("&quot;"); break;
        case '\'': sb.append("&apos;"); break;
        default:   sb.append(c); break;
      }
    }
    return sb.toString();
  }
}
