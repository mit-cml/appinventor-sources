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
AI.Yail['global_declaration'] = function() {
  var name = AI.Yail.YAIL_GLOBAL_VAR_TAG + this.getFieldValue('NAME');
  var argument0 = AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE) || '0';
  var code = AI.Yail.YAIL_DEFINE +  name + AI.Yail.YAIL_SPACER + argument0 + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// Global variable getter block
AI.Yail['lexical_variable_get'] = function() {
  var code = "";
  var name = this.getFieldValue('VAR');
  if (this.eventparam) {        // If this exists, its the english (default) value
    name = this.eventparam;     // which is what we should use in the Yail
  } else {
    Blockly.LexicalVariable.getEventParam(this);
    if (this.eventparam) {
      name = this.eventparam;
    }
  }
  var commandAndName = AI.Yail.getVariableCommandAndName(name);
  code += commandAndName[0];
  name = commandAndName[1];
  
  code += name + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

// Global variable setter block
AI.Yail['lexical_variable_set'] = function() {
  var argument0 = AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE) || '0';
  var code = "";
  var name = this.getFieldValue('VAR');
  if (this.eventparam) {        // If this exists, its the english (default) value
    name = this.eventparam;     // which is what we should use in the Yail
  } else {
    Blockly.LexicalVariable.getEventParam(this);
    if (this.eventparam) {
      name = this.eventparam;
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

AI.Yail['local_declaration_statement'] = function() {
  return AI.Yail.local_variable(this,false);
}

AI.Yail['local_declaration_expression'] = function() {
  return AI.Yail.local_variable(this,true);
}

AI.Yail['local_variable'] = function(block,isExpression) {
  var code = AI.Yail.YAIL_LET;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_SPACER;
  for(var i=0;block.getFieldValue("VAR" + i);i++){
    code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LOCAL_VAR_TAG + (Blockly.usePrefixInYail ? "local_" : "") + block.getFieldValue("VAR" + i);
    code += AI.Yail.YAIL_SPACER + ( AI.Yail.valueToCode(block, 'DECL' + i, AI.Yail.ORDER_NONE) || '0' );
    code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_SPACER +  AI.Yail.YAIL_CLOSE_COMBINATION;
  // [lyn, 01/15/2013] Added to fix bug in local declaration expressions:
  if(isExpression){
    if(!block.getInputTargetBlock("RETURN")){
      code += AI.Yail.YAIL_SPACER + "0";
    } else {
      code += AI.Yail.YAIL_SPACER + AI.Yail.valueToCode(block, 'RETURN', AI.Yail.ORDER_NONE);
    }
  } else {
    code += AI.Yail.YAIL_SPACER +
      (AI.Yail.statementToCode(block, 'STACK', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE);
  }
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  if(!isExpression){
    return code;
  } else {
    return [ code, AI.Yail.ORDER_ATOMIC ];
  }
};
