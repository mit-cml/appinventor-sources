// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;
import openblocks.codeblocks.BlockStub;
import openblocks.codeblocks.ComplaintDepartment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The BlockParser handles the table-driven compilation of Blocks to YAIL code.
 * It contains virtually all the features of the language design, mapping blocks
 * into Yail (Young Android Intermediate Language) which is translated into
 * Scheme, then sent to the phone or compiled by Kawa into
 * Java byte codes, depending on the forRepl flag.
 *
 *
 *
 */

// TODO(halabelson): Examine all Yail code that is
// generated here and make sure there can be no symbol conflcits
// with identifiers in user procedures.  See list vs. *list-for-runtime*
// as an example of the issue.   We might be able to fix this for real by generating
// code of the form (eval 'list interaction-environment)


public class BlockParser {
  private static final boolean DEBUG = false;

  /**
   * To signal that a socket is empty.
   * during code generation for Repl.
   *
   */

  private class EmptySocketException extends Exception {
    public EmptySocketException() {
      super("");
      if (DEBUG) {
        System.out.println("Throwing EmptySocketException");
      }
    }
  }

  private class BadBlockException extends Exception {
    public BadBlockException(String msg) {
      super(msg == null ? "" : msg);
      if (DEBUG) {
        System.out.println("Throwing BadBlockException");
      }
    }
  }

  static final String EMPTY_SOCKET = "This clump contains an empty socket" +
                                     " and won't be sent to the phone.";
  static final String BAD_BLOCK = "This clump contains a bad block" +
                                  " and won't be sent to the phone.";

  // A large number of strings of YAIL fragments are declared at the end

  // Mapping of Block ya-kind (see lang_def) to function name
  private static final HashMap<String, String> blockKindToMethodMap = new HashMap<String, String>();
  static {
    blockKindToMethodMap.put("argument", "makeArgumentBlock");
    blockKindToMethodMap.put("call", "makeUserProcedureCallBlock");
    blockKindToMethodMap.put("choose", "makeIfElseBlock");
    blockKindToMethodMap.put("color", "makeColorBlock");
    blockKindToMethodMap.put("component", "makeComponentBlock");
    blockKindToMethodMap.put("componentEvent", "makeComponentEventBlock");
    blockKindToMethodMap.put("componentGetter", "makeCompVarGetBlock");
    blockKindToMethodMap.put("componentMethod", "makeComponentMethodCallBlock");
    blockKindToMethodMap.put("componentSetter", "makeCompVarSetBlock");
    blockKindToMethodMap.put("componentTypeMethod", "makeComponentTypeMethodCallBlock");
    blockKindToMethodMap.put("def", "makeDefineVarBlock");
    blockKindToMethodMap.put("define", "makeDefineProcBlock");
    blockKindToMethodMap.put("false", "makeFalseBlock");
    blockKindToMethodMap.put("foreach", "makeForEachBlock");
    blockKindToMethodMap.put("forrange", "makeForRangeBlock");
    blockKindToMethodMap.put("getter", "makeGetterBlock");
    blockKindToMethodMap.put("glue", "makeGlueBlock");
    blockKindToMethodMap.put("and", "makeAndBlock");
    blockKindToMethodMap.put("or", "makeOrBlock");
    blockKindToMethodMap.put("if", "makeIfBlock");
    blockKindToMethodMap.put("ifelse", "makeIfElseBlock");
    blockKindToMethodMap.put("makeList", "makeListBlock");
    blockKindToMethodMap.put("addToList", "makeAddToListBlock");
    blockKindToMethodMap.put("null", "makeNullBlock");
    blockKindToMethodMap.put("number", "makeNumberBlock");
    blockKindToMethodMap.put("primitive", "makeYailPrimitiveCallBlock");
    blockKindToMethodMap.put("setter", "makeVarSetBlock");
    blockKindToMethodMap.put("text", "makeStringBlock");
    blockKindToMethodMap.put("true", "makeTrueBlock");
    blockKindToMethodMap.put("while", "makeWhileBlock");
  }

  private static StringBuilder emptyStringBuilder = new StringBuilder("");

  private final boolean forRepl;  // We're more permissive if it's true.

  private List<String> formalParameters = new LinkedList<String>();
  // Invariant: formalParameters contains all the names that are lexically
  // defined at the moment. Care must be taken to add and remove them as
  // name-defining constructs start and end, even in the presence of exceptions.

  /**
   * Constructor
   * @param forRepl simply save which purpose the Yailification is for to
   *        control permissiveness.
   */

  public BlockParser(boolean forRepl) {
    this.forRepl = forRepl;
  }

  /**
   * General call for everyone to use. It ALWAYS produces something, but may
   * have logged errors and warnings in ComplaintDepartment which is called
   * by callers of genYail to display and count errors.
   * If ComplaintDepartment.displayComplaints() > 0 they should not proceed
   * with the code.
   * Return the empty string if an empty socket was found. This should also
   * abort further compilation but didn't signal a hard via ComplaintDeparment.
   *
   * @param block
   * @return Yail code
   */
  public String genYail(Block block) {
    StringBuilder codeStringBuilder = new StringBuilder();
    try {
      genCodeForSingleBlock(block, codeStringBuilder);
    } catch (EmptySocketException e) {
      block.postWarning(EMPTY_SOCKET);
      return "";
    } catch (BadBlockException e) {
      block.postWarning(BAD_BLOCK + " " + e.getMessage());
      return "";
    }
    return codeStringBuilder.toString();
  }

  /**
   * Generates yail code to get a variable's value given its declaration
   * block. Very special case for handling watching of variable declarations
   * in the REPL.
   * @param block a variable decl block
   */
  public String genVarGetYailFromDecl(Block block) {
    return makeVarGetBlock(block).toString();
  }

  /*
   * Generate a null declaration for block. block.isDeclaration() should be
   * true.
   */
  public String genNullDecl(Block block) {
    StringBuilder code = new StringBuilder();
    if (block.isProcedureDeclBlock()) {
      code.append(YAIL_DEFINE)
        .append(YAIL_OPEN_COMBINATION)
        .append(block.getBlockLabel())
        .append(YAIL_CLOSE_COMBINATION)
        .append(YAIL_SPACER)
        .append(YAIL_NULL)
        .append(YAIL_CLOSE_COMBINATION);
    } else if (block.isEventHandlerBlock()) {
      try {
        String[] parts = getComponentBlockLabelParts(block);
        code.append(YAIL_DEFINE_EVENT)
            .append(parts[0])
            .append(YAIL_SPACER)
            .append(parts[1])
            .append(YAIL_OPEN_COMBINATION);
      } catch (YailGenerationSystemException e) {
        return "";
      }
      for (BlockConnector con : block.getSockets()) {
        if (!"do".equals(con.getLabel())) {
          Block varBlock = Block.getBlock(con.getBlockID());
          if (varBlock != null && varBlock.isArgument()) {
            code.append(YAIL_SPACER)
            .append(varBlock.getBlockLabel())
            .append(YAIL_SPACER);
          }
        }
      }
      code.append(YAIL_CLOSE_COMBINATION)
          .append(YAIL_NULL);
      code.append(YAIL_CLOSE_COMBINATION);
    } else if (block.isVariableDeclBlock()) {
      String label = block.getBlockLabel();
      code.append(YAIL_DEFINE)
          .append(label)
          .append(YAIL_SPACER)
          .append(YAIL_NULL)
          .append(YAIL_CLOSE_COMBINATION);
    } else {
      // should not be in this case really!
    }
    return code.toString();
  }


  private void genCodeForSingleBlock(Block block, StringBuilder code)
      throws EmptySocketException, BadBlockException {
    if (forRepl && block.isBad()) {
      throw new BadBlockException(block.getBadMsg());
    }
    String blockKind = block.getProperty("ya-kind");
    // Generate YAIL_NULL for bad, undefined, or deactivated blocks.
    // Except, a deactivated componentEvent must generate its own code; see
    // makeComponentEventBlock.
    StringBuilder basicCode = defined(blockKind) && !block.isBad()
                                && (block.activated() || "componentEvent".equals(blockKind))
                               ? invokeBlocksGenerator(block)
                               : YAIL_NULL;
    // Check to see if reporting is required.
    // Declaration blocks are sent reports from other blocks or generate their own reports
    if (forRepl && block.shouldReceiveReport() && !block.isDeclaration()) {
      code.append(getReportCall(basicCode, block));
    } else {
      code.append(basicCode);
    }
  }

  // This is shared with RenderableBlock who received messages
  public static final String REPL_DISPLAY_IT = "Display It";

  public String getReportCall(StringBuilder basicCode, Block block) {
    StringBuilder code = new StringBuilder();
    code.append("(report ")
        .append("\"")
        .append(REPL_DISPLAY_IT)
        .append(PhoneCommManager.REPL_BLOCK_ID_INDICATOR)
        .append(block.getBlockID().toString())
        .append("\" ")
        .append(basicCode)
        .append(")");
    return code.toString();
  }

  private void genCodeForStackOfBlocks(Block currentBlock, boolean nullOK,
      StringBuilder output) throws EmptySocketException, BadBlockException {
    while (currentBlock != null) {
      genCodeForSingleBlock(currentBlock, output);
      output.append("\n");
      currentBlock = Block.getBlock(currentBlock.getAfterBlockID());
    }
    if (output.length() == 0 && !nullOK) {
      output.append(YAIL_NULL);  // To mollify Kawa/Scheme
    }
  }

  private StringBuilder invokeBlocksGenerator(Block block)
      throws EmptySocketException, BadBlockException {
    String kind = block.getProperty("ya-kind");
    String methodName = getMethodFor(kind);
    if (methodName == null) {
      FeedbackReporter.showSystemErrorMessage("Invalid block kind: " + kind);
      return emptyStringBuilder;
    }
    Method method;
    try {
      method = getClass().getDeclaredMethod(methodName, Block.class);
    } catch (SecurityException e) {
      FeedbackReporter.showSystemErrorMessage("Security exception " + e.getMessage());
      return emptyStringBuilder;
    } catch (NoSuchMethodException e) {
      FeedbackReporter.showSystemErrorMessage("No such method. " + e.getMessage());
      return emptyStringBuilder;
    }
    try {
      return (StringBuilder) method.invoke(this, block);
    } catch (IllegalArgumentException e) {
      FeedbackReporter.showSystemErrorMessage("Illegal argument " + e.getMessage());
      e.printStackTrace();
      return emptyStringBuilder;
    } catch (IllegalAccessException e) {
      FeedbackReporter.showSystemErrorMessage("Illegal access " + e.getMessage());
      e.printStackTrace();
      return emptyStringBuilder;
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      while (cause instanceof InvocationTargetException) {
        cause = cause.getCause();
      }
      if (cause instanceof EmptySocketException) {
        throw (EmptySocketException) cause;
      } else if (cause instanceof BadBlockException) {
        throw (BadBlockException) cause;
      }
      cause.printStackTrace();
      FeedbackReporter.showSystemErrorMessage("Invocation target exception for method " + method +
          " " + cause.getMessage());
      return emptyStringBuilder;
    }
  }

  /**
   * Check if BlockParser can handle the given block kind
   *
   * @return If this class can handle the type
   */
  private boolean defined(String kind) {
    return blockKindToMethodMap.containsKey(kind) || "primitive".equals(kind);
  }

  /**
   * Return the method call for a given type
   *
   * @return The method call
   */
  private String getMethodFor(String type) {
    return blockKindToMethodMap.get(type);
  }

  // Many of the makeXXX routines that follow are written in a style that suggests
  // that blocks could have a variable number (including 0) of sockets, labeled with the same
  // names, and occurring in arbitrary order. Of course, this is not true because
  // the language syntax doesn't allow it, e.g. a while block has one test and one
  // "do" section. See OUTPUT_HEADER.txt to discover the language's syntax.


  /**
   * Creates a YAIL representation for the String block
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeStringBlock(Block block) {
    StringBuilder output = new StringBuilder();
    String label = block.getBlockLabel();
    return output.append(YABlockCompiler.quotifyForREPL(label));
  }

  /**
   * Creates a YAIL representation for the true block. Note that although the
   * argument page is not used, it must be kept in the method signature.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeWhileBlock(Block block)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    StringBuilder doBlock = new StringBuilder();
    code.append(YAIL_WHILE);
    for (BlockConnector con : block.getSockets()) {
      if ("test".equals(con.getLabel())) {
        code.append(YAIL_SPACER);
        getYailForConnector(con, block, code);
      } else {
        genCodeForStackOfBlocks(Block.getBlock(con.getBlockID()), false, doBlock);
      }
    }
    code.append(YAIL_SPACER)
        .append(YAIL_BEGIN)
        .append(doBlock)
        .append(YAIL_CLOSE_COMBINATION)
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the Argument block
   *
   * @return String representation of the block
   * @param block the current block
   */
  private StringBuilder makeArgumentBlock(Block block) {
    return new StringBuilder(block.getBlockLabel());
  }

  /**
   * Creates a YAIL representation for the true block.
   *
   * @return String representation of the block
   * @param block the current block
   */
  private StringBuilder makeTrueBlock(Block block) {
    return YAIL_TRUE;
  }

  /**
   * Creates a YAIL representation for the false block.
   *
   * @return String representation of the block
   * @param block the current block
   */
  private StringBuilder makeFalseBlock(Block block) {
    return YAIL_FALSE;
  }


  /**
   * Creates a YAIL representation for a component's event block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeComponentEventBlock(Block block) throws YailGenerationSystemException {
    StringBuilder code = new StringBuilder();
    String[] parts = getComponentBlockLabelParts(block);
    if (DEBUG) {
      if (formalParameters.size() != 0) {
        System.out.println("Non-empty formalParameters = " + formalParameters.toString());
      }
    }
    code.append(YAIL_DEFINE_EVENT)
        .append(parts[0])
        .append(YAIL_SPACER)
        .append(parts[1])
        .append(YAIL_OPEN_COMBINATION);
    formalParameters = new LinkedList<String>(); // See comments in makeDefineProcedure
    StringBuilder argsBlock = new StringBuilder();
    StringBuilder argsReportBlock = new StringBuilder();
    StringBuilder doBlock = new StringBuilder();
    try {
      for (BlockConnector con : block.getSockets()) {
        if ("do".equals(con.getLabel())) {
          genCodeForStackOfBlocks(Block.getBlock(con.getBlockID()), false, doBlock);
        } else {
          // The labels of args are the names chosen by the component writer
          // so they could be anything.
          Block varBlock = Block.getBlock(con.getBlockID());
          if (varBlock == null || !varBlock.isArgument()) {
            block.postWarning(ComplaintDepartment.MISSING_VARIABLE);
          } else {
            genArgsCode(varBlock, argsBlock, argsReportBlock);
          }
        }
      }
    } catch (EmptySocketException e) {
      block.postWarning(EMPTY_SOCKET);
      return emptyStringBuilder;
    } catch (BadBlockException e) {
      block.postWarning(BAD_BLOCK + " " + e.getMessage());
      return emptyStringBuilder;
    } finally {
      formalParameters = new LinkedList<String>();
    }
    code.append(argsBlock)
        .append(YAIL_CLOSE_COMBINATION);
    if (!block.activated()) {
      code.append(YAIL_NULL);
    } else {
      code.append(YAIL_SPACER)
          .append(argsReportBlock);
      if (doBlock.length() == 0) {
        code.append(YAIL_NULL);
      } else {
        code.append(YAIL_SET_THIS_FORM)
            .append(doBlock);
      }
    }
    code.append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for a component's method call block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeComponentMethodCallBlock(Block block)
    throws YailGenerationSystemException, EmptySocketException, BadBlockException  {
    return makeComponentMethodCallBlockInternal(block, /* hasComponentArg */ false);
  }


  /**
   * Creates a YAIL representation for a component type's method call block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeComponentTypeMethodCallBlock(Block block)
    throws YailGenerationSystemException, EmptySocketException, BadBlockException  {
    return makeComponentMethodCallBlockInternal(block, /* hasComponentArg */ true);
  }


  /**
   * Creates a YAIL representation for a component or component type method call.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   * @param hasComponentArg whether the block contains a socket for a component argument
   */
  private StringBuilder makeComponentMethodCallBlockInternal(Block block, boolean hasComponentArg)
    throws YailGenerationSystemException, EmptySocketException, BadBlockException  {
    // Component methods are expected in the form Blockname.MethodName
    StringBuilder code = new StringBuilder();
    StringBuilder args = new StringBuilder();
    String parts[] = getComponentBlockLabelParts(block);
    boolean firstTime = true;
    for (BlockConnector con : block.getSockets()) {
      // If we have a component arg it is in the first socket is the component value, so skip it.
      // We'll get it later.
      if (hasComponentArg && firstTime) {
        firstTime = false;
        continue;
      }
      args.append(YAIL_SPACER);
      getYailForConnector(con, block, args);
    }
    String genus = block.getGenusName();
    // System.out.println("Calling makeComponentMethodBlockCall: " + label);
    StringBuilder typelistForYail;
    if (BlockRules.genusToSocketRules.get(genus) != null) {
      // this block has possible coercions
      typelistForYail = getArgTypesAsSchemeList(block);
    } else {
      // if there are no coercions, we pass a token to the runtime that
      // tells it not to attempt any coercions.  Currently, the only time there
      // would have been no coercions is when there were no args, so it would have also
      // worked to pass the empty list.  But it seems safer going forward to be explicit.
      //TODO(halabelson): In the case of no args, getArgTypesAsSchemeList returns
      // ( #f).  Maybe it should be changed to return the empty list.
      typelistForYail = new StringBuilder(YAIL_EMPTY_LIST);
    }
    if (hasComponentArg) {
      code.append(YAIL_CALL_COMPONENT_TYPE_METHOD);
      getYailForConnector(block.getSocketAt(0), block, code); // the value of the component
      code.append(YAIL_SPACER)
          .append(YAIL_QUOTE)  // the expected type of the component
          .append(parts[0]);
    } else {
      code.append(YAIL_CALL_COMPONENT_METHOD)
          .append(YAIL_QUOTE).append(parts[0]); // the name of the component
    }
    code.append(YAIL_SPACER).append(YAIL_QUOTE).append(parts[1]) // the method
        .append(YAIL_SPACER).append(YAIL_OPEN_COMBINATION).append(YAIL_LIST_CONSTRUCTOR)
        .append(args).append(YAIL_CLOSE_COMBINATION)
        .append(YAIL_SPACER).append(typelistForYail)
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the add-to-list block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeAddToListBlock(Block block)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    StringBuilder listBlock = new StringBuilder();
    StringBuilder appendBlock = new StringBuilder();
    code.append(YAIL_ADD_TO_LIST);
    for (BlockConnector con : block.getSockets()) {
      if ("list".equals(con.getLabel())) {
        listBlock.append(YAIL_SPACER);
        } else {
        appendBlock.append(YAIL_SPACER);
        getYailForConnector(con, block, appendBlock);
      }
    }
    code.append(YAIL_SPACER).append(listBlock).append(appendBlock).append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the null block.
   *
   * @return String representation of the block
   * @param block the current block
   */
  private StringBuilder makeNullBlock(Block block) {
    return YAIL_NULL;
  }

  /**
   * Creates a YAIL representation for the glue block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeGlueBlock(Block block)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    for (BlockConnector con : block.getSockets()) {
      code.append(YAIL_SPACER);
      getYailForConnector(con, block, code);
    }
    return code;
  }

  /**
   * Creates a YAIL representation for the number block.
   *
   * @return String representation of the block
   * @param block the current block
   */
  private StringBuilder makeNumberBlock(Block block) {
    String label = block.getBlockLabel();
    // note that we deal with number conversions in kawa
    return new StringBuilder(label);
  }

  /**
   * Creates a YAIL representation for the procedure call block,
   * which calls user-defined procedures.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeUserProcedureCallBlock(Block block)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    StringBuilder args = new StringBuilder();
    for (BlockConnector con : block.getSockets()) {
      args.append(YAIL_SPACER);
      getYailForConnector(con, block, args);
    }
    // This in-lines
    // ((GET-VAR procname) arg ... )
    // The alternative non-inlined version is
    //     code.append(YAIL_CALL_USER_PROCEDURE).append(YAIL_QUOTE).append(procname)
    //           .append(YAIL_SPACER).append(YAIL_OPEN_COMBINATION)
    //            .append(YAIL_LIST_CONSTRUCTOR).append(args).append(YAIL_CLOSE_COMBINATION);
    // System.out.println("makeUserProcedreCallBlock: " + code.toString());
    return getCallCode(block, args);
  }

  /*
   * Convenience for ComponentBlockManager to generate DO_IT call.
   * @param procBlock the procedure decl block

   */
  public StringBuilder getCallCode(Block procBlock, StringBuilder argsCode) {
    StringBuilder code = new StringBuilder();
    code.append(YAIL_OPEN_COMBINATION).append(YAIL_GET_VARIABLE)
        .append(procBlock.getBlockLabel())
        .append(YAIL_CLOSE)
        .append(argsCode)
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the define procedure block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeDefineProcBlock(Block block) {
    StringBuilder code = new StringBuilder();
    StringBuilder argsBlock = new StringBuilder();
    StringBuilder argsReportBlock = new StringBuilder();
    StringBuilder doBlock = new StringBuilder();
    StringBuilder retBlock = new StringBuilder();
    formalParameters = new LinkedList<String>();
    // Because the language requires that procedure decls are at the top level
    // we know that no names are defined now.
    code.append(YAIL_DEFINE).append(YAIL_OPEN_COMBINATION).append(block.getBlockLabel());
    try {
      for (BlockConnector con : block.getSockets()) {
        if ("do".equals(con.getLabel())) {
          genCodeForStackOfBlocks(Block.getBlock(con.getBlockID()), true, doBlock);
        } else if ("arg".equals(con.getLabel())) {
          // Last arg slot is always empty, others must have kind "argument"
          Block argName = Block.getBlock(con.getBlockID());
          if (argName != null && argName.isArgument()) {
            genArgsCode(argName, argsBlock, argsReportBlock);
          }
        } else if ("return".equals(con.getLabel())) {
          getYailForConnector(con, block, retBlock);
        }
      }
    } catch (EmptySocketException e) {
      block.postWarning(EMPTY_SOCKET);
      return emptyStringBuilder;
    } catch (BadBlockException e) {
      block.postWarning(BAD_BLOCK + " " + e.getMessage());
      return emptyStringBuilder;
    } finally {
      formalParameters = new LinkedList<String>();
      // Remove all names, because we're at the top level.
    }
    // We avoided adding spacers to doBlock and retBlock so this test works.
    if (doBlock.length() == 0 && retBlock.length() == 0) {
      doBlock.append(YAIL_NULL);
    }
    code.append(argsBlock).append(YAIL_CLOSE_COMBINATION).append(argsReportBlock)
    .append(doBlock).append(YAIL_SPACER).append(retBlock).append(YAIL_CLOSE_COMBINATION);
    return code;
  }


  /**
   * Creates a YAIL representation for the for-each block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeForEachBlock(Block block)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    StringBuilder listBlock = new StringBuilder();
    StringBuilder doBlock = new StringBuilder();
    StringBuilder itemBlock = new StringBuilder();
    StringBuilder argReportBlock = new StringBuilder();
    String label = block.getBlockLabel();
    Block varBlock = null;
    code.append(YAIL_FOREACH);
    try {
      for (BlockConnector con : block.getSockets()) {
        if ("do".equals(con.getLabel())) {
          genCodeForStackOfBlocks(Block.getBlock(con.getBlockID()), false, doBlock);
        } else if ("in list".equals(con.getLabel())) {
          listBlock.append(YAIL_SPACER);
          getYailForConnector(con, block, listBlock);
        } else if ("variable".equals(con.getLabel())) {
          varBlock = Block.getBlock(con.getBlockID());
          if (varBlock == null || !varBlock.isArgument()) {
            block.postWarning(ComplaintDepartment.MISSING_VARIABLE);
          } else {
            genArgsCode(varBlock, itemBlock, argReportBlock);
          }
        }
      }
    } finally {
      if (varBlock != null) {
        formalParameters.remove(varBlock.getBlockLabel());
      }
    }
    code.append(itemBlock)
        .append(YAIL_SPACER)
        .append(YAIL_BEGIN)
        .append(argReportBlock)
        .append(doBlock)
        .append(YAIL_CLOSE_COMBINATION)
        .append(YAIL_SPACER)
        .append(listBlock)
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  private void genArgsCode(Block argName, StringBuilder itemBlock, StringBuilder argReportBlock) {
    String argNameLabel = argName.getBlockLabel();
    itemBlock.append(YAIL_SPACER)
        .append(argNameLabel)
        .append(YAIL_SPACER);
    formalParameters.add(argNameLabel);
    if (forRepl && argName.shouldReceiveReport()) {
      argReportBlock.append(getReportCall(new StringBuilder("(lexical-value ")
                                              .append(argNameLabel)
                                              .append(")"),
                                         argName))
                   .append("\n");
    }
  }

  /**
   * Creates a YAIL representation for the for-range block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeForRangeBlock(Block block)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    StringBuilder startBlock = new StringBuilder();
    StringBuilder endBlock = new StringBuilder();
    StringBuilder stepBlock = new StringBuilder();
    StringBuilder doBlock = new StringBuilder();
    StringBuilder itemBlock = new StringBuilder();
    StringBuilder argReportBlock = new StringBuilder();
    String label = block.getBlockLabel();
    Block varBlock = null;
    code.append(YAIL_FORRANGE);
    try {
      for (BlockConnector con : block.getSockets()) {
        if ("do".equals(con.getLabel())) {
          genCodeForStackOfBlocks(Block.getBlock(con.getBlockID()), false, doBlock);
        } else if ("start".equals(con.getLabel())) {
          startBlock.append(YAIL_SPACER);
          getYailForConnector(con, block, startBlock);
        } else if ("end".equals(con.getLabel())) {
          endBlock.append(YAIL_SPACER);
          getYailForConnector(con, block, endBlock);
        } else if ("step".equals(con.getLabel())) {
          endBlock.append(YAIL_SPACER);
          getYailForConnector(con, block, stepBlock);
        } else if ("variable".equals(con.getLabel())) {
          varBlock = Block.getBlock(con.getBlockID());
          if (varBlock == null || !varBlock.isArgument()) {
            block.postWarning(ComplaintDepartment.MISSING_VARIABLE);
          } else {
            genArgsCode(varBlock, itemBlock, argReportBlock);
          }
        }
      }
    } finally {
      if (varBlock != null) {
        formalParameters.remove(varBlock.getBlockLabel());
      }
    }
    code.append(itemBlock)
        .append(YAIL_SPACER)
        .append(YAIL_BEGIN)
        .append(argReportBlock)
        .append(doBlock)
        .append(YAIL_CLOSE_COMBINATION)
        .append(YAIL_SPACER)
        .append(startBlock)
        .append(YAIL_SPACER)
        .append(endBlock)
        .append(YAIL_SPACER)
        .append(stepBlock)
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }



  /**
   * Creates a YAIL representation for the and block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeAndBlock(Block block) throws EmptySocketException, BadBlockException {
    return makeAndOrBlockHelper(block, YAIL_AND_DELAYED);
  }

  /**
   * Creates a YAIL representation for the or block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeOrBlock(Block block) throws EmptySocketException, BadBlockException {
    return makeAndOrBlockHelper(block, YAIL_OR_DELAYED);
  }

  // Helper method for the And and Or blocks.
  private StringBuilder makeAndOrBlockHelper(Block block, String and_or)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    StringBuilder args = new StringBuilder();
    for (BlockConnector con : block.getSockets()) {
      // The last socket on an expandable primitive block is always empty
      // and should be ignored, so we must bypass getYailForConnector.
      if (!(con.isExpandable() && Block.getBlock(con.getBlockID()) == null)) {
        args.append(YAIL_SPACER);
        getYailForConnector(con, block, args);
      }
    }
    // assemble the call
    code.append(and_or);
    // create the arglist
    code.append(YAIL_SPACER).append(args);
    code.append(YAIL_CLOSE_COMBINATION);
    // System.out.println("makeAndOrBlockHelper: " + code.toString());
    return code;
  }


  /**
   * Creates a YAIL representation for the if block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeIfBlock(Block block) throws EmptySocketException, BadBlockException {
    return makeIfBlockHelper(block, false);
  }

  /**
   * Creates a YAIL representation for the if-else block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeIfElseBlock(Block block)
      throws EmptySocketException, BadBlockException {
    return makeIfBlockHelper(block, true);
  }

  // Helper methods for the if, if-else, and choose blocks.
  private StringBuilder makeIfBlockHelper(Block block, boolean hasElse)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    StringBuilder doBlock = new StringBuilder();
    StringBuilder elseBlock = new StringBuilder();
    StringBuilder testBlock = new StringBuilder();
    StringBuilder doReturnBlock = new StringBuilder();
    StringBuilder elseReturnBlock = new StringBuilder();
    String label = block.getBlockLabel();
    code.append(YAIL_IF);
    for (BlockConnector con : block.getSockets()) {
      if ("test".equals(con.getLabel())) {
        //TODO(user) Insert coerce-to-boolean call if we want runtime quibbling.
        getYailForConnector(con, block, testBlock);
      } else if ("then-do".equals(con.getLabel())) {
        genCodeForStackOfBlocks(Block.getBlock(con.getBlockID()),
            block.getProperty("ya-kind").equals("choose"), doBlock);
      } else if ("then-return".equals(con.getLabel())) {
        doReturnBlock.append(YAIL_SPACER);
        getYailForConnector(con, block, doReturnBlock);
      } else if ("else-do".equals(con.getLabel())) {
        genCodeForStackOfBlocks(Block.getBlock(con.getBlockID()),
            block.getProperty("ya-kind").equals("choose"), elseBlock);
      } else if ("else-return".equals(con.getLabel())) {
        elseReturnBlock.append(YAIL_SPACER);
        getYailForConnector(con, block, elseReturnBlock);
      }
    }
    code.append(testBlock).append(YAIL_SPACER).append(YAIL_BEGIN).append(doBlock)
        .append(doReturnBlock).append(YAIL_CLOSE_COMBINATION);
    if (hasElse) {
      code.append(YAIL_SPACER).append(YAIL_BEGIN).append(elseBlock)
          .append(elseReturnBlock).append(YAIL_CLOSE_COMBINATION);
    }
    code.append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the the define variable block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeDefineVarBlock(Block block) {
    StringBuilder code = new StringBuilder();
    String label = block.getBlockLabel();
    code.append(YAIL_DEFINE)
        .append(label)
        .append(YAIL_SPACER);
    StringBuilder basic = new StringBuilder();
    try {
      getYailForConnector(block.getSockets().iterator().next(), block, basic);
    } catch (EmptySocketException e) {
      block.postWarning(EMPTY_SOCKET);
      return emptyStringBuilder;
    } catch (BadBlockException e) {
      block.postWarning(BAD_BLOCK + " " + e.getMessage());
      return emptyStringBuilder;
    }
    if (forRepl && block.shouldReceiveReport()) {
      code.append(getReportCall(basic, block));
    } else {
      code.append(basic);
    }
    code.append(YAIL_CLOSE_COMBINATION)
      .append(YAIL_LINE_FEED);
    // We'll let the code get generated and then check it to
    // ensure it doesn't use operations that are forbidden in
    // variable definitions -- currently those operations that
    // access components.
    // TODO(user): This check can be subverted by a procedure call.
    if (DEBUG) {
      System.out.println("Definition code: " + code);
    }
    String codeString = code.toString();
    for (String op : forbiddenInVariableDef) {
      if (codeString.contains(op)) {
        block.postError(ComplaintDepartment.BAD_INITIALIZER);
      }
    }
    return code;
  }

  /**
   * Creates a YAIL representation for the variable getter block, dispatching on whether it's a
   * global or lexical getter.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeGetterBlock(Block block) {
    if ("getterGlobal".equals(block.getGenusName())) {
      return makeVarGetBlock(block);
    } else {
      return makeLexicalValueBlock(block);
    }
  }

  /**
   * Creates a YAIL representation for the variable get-var block.
   * Also okay to pass in the variable's decl block since we only use the label
   * from the block. Returns the code to get the variable's value.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeVarGetBlock(Block block) {
    StringBuilder code = new StringBuilder();
    String label = block.getBlockLabel();
    code.append(YAIL_GET_VARIABLE)
        .append(label)
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the variable lexical value block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeLexicalValueBlock(Block block) {
    StringBuilder code = new StringBuilder();
    String label = block.getBlockLabel();
    if (formalParameters.contains(label)) {
      code.append(YAIL_LEXICAL_VALUE)
          .append(label)
          .append(YAIL_CLOSE_COMBINATION);
    } else {
      code.append(YAIL_NULL);
      block.postWarning(ComplaintDepartment.UNBOUND_VARIABLE);
    }
    return code;
  }

  /**
   * Creates a YAIL representation for the component property get block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeCompVarGetBlock(Block block)
      throws YailGenerationSystemException, BadBlockException, EmptySocketException {
    if (ComponentBlockManager.isComponentTypeRelated(block)) {
      return makeCompTypeVarGetBlock(block);
    }
    // Component variable references in the form Blockname.Property
    StringBuilder code = new StringBuilder();
    String parts[] = getComponentBlockLabelParts(block);
    code.append(YAIL_GET_PROPERTY)
        .append(YAIL_QUOTE)
        .append(parts[0])
        .append(YAIL_SPACER)
        .append(YAIL_QUOTE)
        .append(parts[1])
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the component type property get block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeCompTypeVarGetBlock(Block block)
      throws YailGenerationSystemException, BadBlockException, EmptySocketException {
    // Component variable references in the form Blockname.Property
    StringBuilder code = new StringBuilder();
    String parts[] = getComponentBlockLabelParts(block);
    code.append(YAIL_GET_COMPONENT_TYPE_PROPERTY)
        .append(YAIL_SPACER);
    getYailForConnector(block.getSocketAt(0), block, code);
    code.append(YAIL_SPACER)
        .append(YAIL_QUOTE)
        .append(parts[0])
        .append(YAIL_SPACER)
        .append(YAIL_QUOTE)
        .append(parts[1])
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the component block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeComponentBlock(Block block) {
    StringBuilder code = new StringBuilder();
    String label = block.getBlockLabel();
    // Component name lookups are treated like global variable name lookups
    code.append(YAIL_GET_COMPONENT)
        .append(label)
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the variable set block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeVarSetBlock(Block block)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    StringBuilder valCode = new StringBuilder();
    for (BlockConnector con : block.getSockets()) {
      // I (jmorris) doubt there is more than one socket.
      valCode.append(YAIL_SPACER);
      getYailForConnector(con, block, valCode);
    }
    String value = valCode.toString();
    // Check to see if value watching is needed. A setter is always a stub.
    Set<Long> parents = ((BlockStub) block).getParents();
    if (parents != null && parents.size() > 0) {
      Block varDecl = Block.getBlock(parents.iterator().next());
      if (forRepl && varDecl.shouldReceiveReport()) {
        value = getReportCall(valCode, varDecl).toString();
      }
    }
    code.append(YAIL_SET_VARIABLE)
        .append(block.getBlockLabel())
        .append(YAIL_SPACER)
        .append(value)
        .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the component property set block.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeCompVarSetBlock(Block block)
    throws YailGenerationSystemException, EmptySocketException, BadBlockException {
    if (ComponentBlockManager.isComponentTypeRelated(block)) {
      return makeCompTypeVarSetBlock(block);
    }
    // Component variable references in the form Blockname.Property
    StringBuilder code = new StringBuilder();
    String parts[] = getComponentBlockLabelParts(block);
    StringBuilder assignLabel = new StringBuilder().append(YAIL_QUOTE)
                                                   .append(parts[0])
                                                   .append(YAIL_SPACER)
                                                   .append(YAIL_QUOTE)
                                                   .append(parts[1]);
    code.append(YAIL_SET_AND_COERCE_PROPERTY).append(assignLabel);
    for (BlockConnector con : block.getSockets()) {
      code.append(YAIL_SPACER);
      getYailForConnector(con, block, code);
    }
    code.append(YAIL_SPACER).append(getPropertyType(parts[0], parts[1]))
      .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for the component type property set block.
   *
   * This is the setter which takes a component object as an argument
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeCompTypeVarSetBlock(Block block)
    throws YailGenerationSystemException, EmptySocketException, BadBlockException {
    // Component variable references in the form Blockname.Property
    StringBuilder code = new StringBuilder();
    code.append(YAIL_SET_AND_COERCE_COMPONENT_TYPE_PROPERTY);
    // The first socket will be the component value expression
    getYailForConnector(block.getSocketAt(0), block, code);
    String parts[] = getComponentBlockLabelParts(block);
    code.append(YAIL_SPACER)
        .append(YAIL_QUOTE)
        .append(parts[0])
        .append(YAIL_SPACER)
        .append(YAIL_QUOTE)
        .append(parts[1])
        .append(YAIL_SPACER);
    // The second socket will be the new property value
    getYailForConnector(block.getSocketAt(1), block, code);
    code.append(YAIL_SPACER).append(getPropertyType(parts[0], parts[1]))
      .append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /**
   * Creates a YAIL representation for a color block
   *
   * @return String representation of the block
   * @param block the current block
   */
  private StringBuilder makeColorBlock(Block block) {
    String hexRep = block.getProperty("ya-hex-value");
    // The hexRep must be 8 characters.
    if (hexRep == null || hexRep.length() != 8) {
      FeedbackReporter.showSystemErrorMessage("Invalid hex value for color block "
          + block.getBlockLabel());
      return new StringBuilder("0"); // TODO(sharon): is there something better to return?
    }

    // The first two hex digits are for the alpha, the rest are the color.
    String alphaString = hexRep.substring(0, 2);
    String colorString = hexRep.substring(2);

    try {
      // First, parse the alpha and color separately.
      int alpha = Integer.parseInt(alphaString, 16);
      int color = Integer.parseInt(colorString, 16);

      // Then, put them together to avoid signed/unsigned problems.
      int alphaAndColor = (alpha << 24) | color;
      return new StringBuilder(Integer.toString(alphaAndColor));
    } catch (NumberFormatException e) {
      FeedbackReporter.showSystemErrorMessage("NumberFormatException for hex " + hexRep);
      return emptyStringBuilder;
    }
  }

  /**
   * Creates a YAIL representation for a block that represents a call to a Yail primitive.
   *
   * @return StringBuilder representation of the block
   * @param block the current block
   */
  private StringBuilder makeYailPrimitiveCallBlock(Block block)
      throws EmptySocketException, BadBlockException {
    StringBuilder code = new StringBuilder();
    StringBuilder args = new StringBuilder();
    String primitive = block.getProperty("ya-rep");
    String codeblocksName = block.getBlockLabel();
    // create the list of types (or signal no coercions)
    StringBuilder typelistForYail = new StringBuilder();
    if (block.getNumSockets() == 0) {
      typelistForYail = typelistForYail.append(YAIL_EMPTY_LIST);
    } else {
      typelistForYail = getArgTypesAsSchemeList(block);
    }
    for (BlockConnector con : block.getSockets()) {
      // The last socket on an expandable primitive block is always empty
      // and should be ignored, so we must bypass getYailForConnector.
      if (!(con.isExpandable() && Block.getBlock(con.getBlockID()) == null)) {
        args.append(YAIL_SPACER);
        getYailForConnector(con, block, args);
      }
    }
    // TODO(halabelson): In-line the call to call-yail-primitive, similar to
    // what was done with call-user-procedure.  Optimize out coercion checks
    // for constants.

    // assemble the call
    code.append(YAIL_CALL_YAIL_PRIMITIVE).append(primitive);
    // create the arglist
    code.append(YAIL_SPACER).append(YAIL_OPEN_COMBINATION).append(YAIL_LIST_CONSTRUCTOR
               ).append(args).append(YAIL_CLOSE_COMBINATION);
    // the list of types
    code.append(YAIL_SPACER).append(typelistForYail).append(YAIL_SPACER);
    // The user-visible name of this primitive may contain non-ascii characters, for example the
    // multiplication and subtractions signs. We need to encode them.
    codeblocksName = Escapers.encodeInternationalCharacters(codeblocksName);
    code.append(YAIL_DOUBLE_QUOTE).append(codeblocksName).append(YAIL_DOUBLE_QUOTE);
    code.append(YAIL_CLOSE_COMBINATION);
    // System.out.println("makeYailPrimitiveCallBlock: " + code.toString());
    return code;
  }

  private String getPropertyType(String componentName, String property) {
    String componentGenus =
        WorkspaceControllerHolder.get().getComponentBlockManager().getGenusFromComponentName(
            componentName);
    // TODO(user): remove 'value' hack
    HashMap<String, HashSet<String>> propertyRules =
        BlockRules.genusToPropertyRules.get(componentGenus);
    String type = "";
    // TODO(user): we currently only allow properties to have one type, yet
    // they are
    // stored in a hashset. We should either store them as a pair, or create
    // some mechanism
    // for a hierarchy of Codeblocks types.
    for (String propType : propertyRules.get(property)) {
      type = propType;
    }
    return YAIL_QUOTE + type;
  }

  // The set of all Yail runtime types
  // This is used in the temporary fix to permit "any" as a runtme type
  // constraint
  // TODO(halabelson,user,user): Possibly remove this after reworking the
  // type constraint mechanism.
  private static final HashSet<String> allYailRuntimeTypes =
      new HashSet<String>(Arrays.asList("text", "number", "list", "boolean", "component",
          "InstantInTime"));


  private StringBuilder getArgTypesAsSchemeList(Block block) {
    // TODO(halabelson,user,user): Rework the coercion to treat functions
    // and components in a uniform way and simplify the socketrules mechanism
    StringBuilder code = new StringBuilder();
    String genus = block.getGenusName();
    HashMap<String, HashSet<String>> socketRules = BlockRules.genusToSocketRules.get(genus);
    String type = "#f";
    if (socketRules != null) {
      code.append(YAIL_QUOTE).append(YAIL_OPEN_COMBINATION);
      for (BlockConnector con : block.getSockets()) {
        if (con.hasBlock()) {
          String socketLabel = con.getLabel();
          if ("".equals(socketLabel)) {
            socketLabel = "default";
          }
          HashSet<String> socketTypes = new HashSet<String>(socketRules.get(socketLabel));
          socketTypes.retainAll(allYailRuntimeTypes);
          if (socketTypes.isEmpty()) {
            // There are no permissible types.
            System.out.println("App Inventor bug: No valid types for block: " + genus);
            type = "any"; // Try to minimize impact.
          }
          // Generate "any" if all runtime types are permitted
          if (socketTypes.containsAll(allYailRuntimeTypes)) {
            type = "any";
          } else {
            // otherwise just generate the first legal type
            // There should be only one
            type = new ArrayList<String>(socketTypes).get(0);
          }
          code.append(YAIL_SPACER).append(type);
        }
      }
    } else {
      // no rules for this genus
      code.append(YAIL_OPEN_COMBINATION).append(YAIL_SPACER).append(YAIL_FALSE);
    }
    code.append(YAIL_CLOSE_COMBINATION);
    return code;
  }

  /*
   * @param con a BlockConnector that might lead to null
   * @param block owning the socket
   * @param output string builder to which the generated code is appended
   * @effect logs an error or warning if needed
   */
  private void getYailForConnector(BlockConnector con, Block block, StringBuilder output)
     throws EmptySocketException, BadBlockException {
    Block socketBlock = Block.getBlock(con.getBlockID());
    if (!nullSocket(socketBlock)) {
      genCodeForSingleBlock(socketBlock, output);
    } else if (forRepl) {
      throw new EmptySocketException();
    } else {
      block.postError(ComplaintDepartment.EMPTY_SOCKET);
      output.append(YAIL_NULL);
    }
  }

  /**
   * Returns a component label split into the component name and the
   * qualifier (part that comes after the ".").
   * @param block component-related block
   * @throws YailGenerationSystemException if the block label does not
   *    contain a "."
   */
  private String[] getComponentBlockLabelParts(Block block)
    throws YailGenerationSystemException {
    String label = block.getBlockLabel();
    String baseLabel;
    if (!label.contains(".")) {
      throw new YailGenerationSystemException(
          "Expected component-related block label to contain \".\"");
    }
    return label.split("\\.");
  }

  /*
   * @param socketBlock a block from a socket or null
   * @return true if the block is null in any way
   */
  private boolean nullSocket(Block socketBlock) {
    return socketBlock == null;
  }

  //TODO(user) IMHO many of these definitions should be eliminated because
  // they are used only once, obfuscate the code, and give the false impression
  // that they might change someday. Google "refuctoring". Email me (james.morris@cmu.edu)
  // when YAIL_SPACER changes.
  private static final String YAIL_ADD_TO_LIST = "(add-to-list ";
  private static final String YAIL_BEGIN = "(begin ";
  private static final String YAIL_CALL_COMPONENT_METHOD = "(call-component-method ";
  private static final String YAIL_CALL_COMPONENT_TYPE_METHOD = "(call-component-type-method ";
  private static final String YAIL_CALL_YAIL_PRIMITIVE = "(call-yail-primitive ";
  private static final String YAIL_CLOSE_COMBINATION = ")\n";
  private static final String YAIL_CLOSE = ")";
  private static final String YAIL_DEFINE = "(def ";
  private static final String YAIL_DEFINE_EVENT = "(define-event ";
  private static final String YAIL_SET_THIS_FORM = "(set-this-form)\n ";
  private static final String YAIL_DOUBLE_QUOTE = "\"";
  private static final StringBuilder YAIL_FALSE = new StringBuilder("#f");
  private static final String YAIL_FOREACH = "(foreach ";
  private static final String YAIL_FORRANGE = "(forrange ";
  private static final String YAIL_GET_COMPONENT = "(get-component ";
  private static final String YAIL_GET_PROPERTY = "(get-property ";
  private static final String YAIL_GET_COMPONENT_TYPE_PROPERTY = "(get-property-and-check  ";
  private static final String YAIL_GET_VARIABLE = "(get-var ";
  private static final String YAIL_AND_DELAYED = "(and-delayed ";
  private static final String YAIL_OR_DELAYED = "(or-delayed ";
  private static final String YAIL_IF = "(if ";
  private static final String YAIL_LEXICAL_VALUE = "(lexical-value ";
  private static final String YAIL_LINE_FEED = "\n";
  private static final StringBuilder YAIL_NULL = new StringBuilder("(get-var *the-null-value*)");
  private static final String YAIL_EMPTY_LIST = "'()";
  private static final String YAIL_OPEN_COMBINATION = "(";
  private static final String YAIL_QUOTE = "'";
  private static final String YAIL_SET_AND_COERCE_PROPERTY = "(set-and-coerce-property! ";
  private static final String YAIL_SET_AND_COERCE_COMPONENT_TYPE_PROPERTY =
      "(set-and-coerce-property-and-check! ";
  private static final String YAIL_SET_VARIABLE = "(set-var! ";
  private static final String YAIL_SPACER = " ";
  private static final StringBuilder YAIL_TRUE = new StringBuilder("#t");
  private static final String YAIL_WHILE = "(while ";

  // Use *list-for-runtime* rather than list here, to avoid a bug if the codeblocks
  // user creates a procedure with a parameter named "list".   See the comment about
  // call-yail-primitive in runtime.scm
  private static final String YAIL_LIST_CONSTRUCTOR = "*list-for-runtime*";

  // These operations cannot be used in variable definitions because they reference
  // components and definitions are currently run before the components are created.
  private static final String [] forbiddenInVariableDef = {
    YAIL_CALL_COMPONENT_METHOD, YAIL_CALL_COMPONENT_TYPE_METHOD, YAIL_GET_COMPONENT,
    YAIL_GET_PROPERTY, YAIL_SET_AND_COERCE_PROPERTY, YAIL_GET_COMPONENT_TYPE_PROPERTY,
    YAIL_SET_AND_COERCE_COMPONENT_TYPE_PROPERTY,
    YAIL_GET_VARIABLE
  };

}
