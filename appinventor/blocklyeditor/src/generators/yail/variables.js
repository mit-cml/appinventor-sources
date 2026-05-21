// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview variables blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Yail.variables');


/**
 * Lyn's History:
 * [lyn, 10/27/13] Modified global variable names to begin with YAIL_GLOBAL_VAR_TAG (currently 'g$')
 *     and local variables to begin with YAIL_LOCAL_VAR_TAG (currently '$').
 *     At least on Kawa-legal first character is necessary to ensure AI identifiers
 *     satisfy Kawa's identifier rules. And the global 'g$' tag is necessary to
 *     distinguish globals from procedures (which use the 'g$' tag).
 * [lyn, 12/27/2012] Abstract over handling of param/local/index prefix
 */

// Variable Blocks
/**
 * The identifier rules specified in Blockly.LexicalVariable.checkIdentifier *REQUIRE*
 * a nonempty prefix (here, "tag") whose first character is a legal Kawa identifier character
 * in the character set [a-zA-Z_\!\$%&\?\^\*~\/>\=<]
 * Note this set does not include the characters [@.-\+], which are special in Kawa
 *  and cannot begin identifiers.
 *
 * Why use '$'?  Because $ means money, which is "valuable", and "valuable" sounds like "variable"!
 */
AI.Yail.YAIL_GLOBAL_VAR_TAG = 'g$';
AI.Yail.YAIL_LOCAL_VAR_TAG = '$';

// Global variable definition block
AI.Yail.forBlock['global_declaration'] = function(block, generator) {
  var name = AI.Yail.YAIL_GLOBAL_VAR_TAG + block.getFieldValue('NAME');
  var argument0 = generator.valueToCode(block, 'VALUE', AI.Yail.ORDER_NONE) || '0';
  var code = AI.Yail.YAIL_DEFINE +  name + AI.Yail.YAIL_SPACER + argument0 + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// Global variable getter block
AI.Yail.forBlock['lexical_variable_get'] = function(block, generator) {
  var code = "";
  var name = block.getFieldValue('VAR');
  if (block.eventparam) {        // If this exists, its the english (default) value
    name = block.eventparam;     // which is what we should use in the Yail
  } else {
    Blockly.LexicalVariable.getEventParam(block);
    if (block.eventparam) {
      name = block.eventparam;
    }
  }
  var commandAndName = AI.Yail.getVariableCommandAndName(name);
  code += commandAndName[0];
  name = commandAndName[1];
  
  code += name + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

// Global variable setter block
AI.Yail.forBlock['lexical_variable_set'] = function(block, generator) {
  var argument0 = generator.valueToCode(block, 'VALUE', AI.Yail.ORDER_NONE) || '0';
  var code = "";
  var name = block.getFieldValue('VAR');
  if (block.eventparam) {        // If this exists, its the english (default) value
    name = block.eventparam;     // which is what we should use in the Yail
  } else {
    Blockly.LexicalVariable.getEventParam(block);
    if (block.eventparam) {
      name = block.eventparam;
    }
  }
  var commandAndName = AI.Yail.setVariableCommandAndName(name);
  code += commandAndName[0];
  name = commandAndName[1];
  code += name + AI.Yail.YAIL_SPACER + argument0
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// [lyn, 12/27/2012] Handle prefixes abstractly
AI.Yail['getVariableCommandAndName'] = function(name){
  var command = "";
  var pair = Blockly.unprefixName(name);
  var prefix = pair[0];
  var unprefixedName = pair[1];
  if (prefix === Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX || prefix === Blockly.GLOBAL_KEYWORD) {
    name = AI.Yail.YAIL_GLOBAL_VAR_TAG + unprefixedName;
    command = AI.Yail.YAIL_GET_VARIABLE;
  } else {
    name = AI.Yail.YAIL_LOCAL_VAR_TAG + (Blockly.possiblyPrefixYailNameWith(prefix))(unprefixedName);
    command = AI.Yail.YAIL_LEXICAL_VALUE;
  }
  return [command,name]
}

// [lyn, 12/27/2012] New
AI.Yail['setVariableCommandAndName'] = function(name){
  var command = "";
  var pair = Blockly.unprefixName(name);
  var prefix = pair[0];
  var unprefixedName = pair[1];
  if (prefix === Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX || prefix === Blockly.GLOBAL_KEYWORD) {
    name = AI.Yail.YAIL_GLOBAL_VAR_TAG + unprefixedName;
    command = AI.Yail.YAIL_SET_VARIABLE;
  } else {
    name = AI.Yail.YAIL_LOCAL_VAR_TAG + (Blockly.possiblyPrefixYailNameWith(prefix))(unprefixedName);
    command = AI.Yail.YAIL_SET_LEXICAL_VALUE;
  }
  return [command,name]
}

AI.Yail.forBlock['local_declaration_statement'] = function(block, generator) {
  return AI.Yail.local_variable(block, generator, false);
};

AI.Yail.forBlock['local_declaration_expression'] = function(block, generator) {
  return AI.Yail.local_variable(block, generator, true);
};

AI.Yail['local_variable'] = function(block, generator, isExpression) {
  var code = AI.Yail.YAIL_LET;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_SPACER;
  for(var i=0;block.getFieldValue("VAR" + i);i++){
    code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LOCAL_VAR_TAG + (Blockly.usePrefixInYail ? "local_" : "") + block.getFieldValue("VAR" + i);
    code += AI.Yail.YAIL_SPACER + ( generator.valueToCode(block, 'DECL' + i, AI.Yail.ORDER_NONE) || '0' );
    code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_SPACER +  AI.Yail.YAIL_CLOSE_COMBINATION;
  // [lyn, 01/15/2013] Added to fix bug in local declaration expressions:
  if(isExpression){
    if(!block.getInputTargetBlock("RETURN")){
      code += AI.Yail.YAIL_SPACER + "0";
    } else {
      code += AI.Yail.YAIL_SPACER + generator.valueToCode(block, 'RETURN', AI.Yail.ORDER_NONE);
    }
  } else {
    var stackCode = generator.statementToCode(block, 'STACK') || AI.Yail.YAIL_FALSE;
    code += AI.Yail.YAIL_SPACER + stackCode;
  }
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  if(!isExpression){
    return code;
  } else {
    return [ code, AI.Yail.ORDER_ATOMIC ];
  }
};
