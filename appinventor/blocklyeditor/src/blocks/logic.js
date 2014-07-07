// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
/**
 * @license
 * @fileoverview Logic blocks for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.Blocks.logic');

goog.require('Blockly.Blocks.Utilities');

Blockly.Blocks['logic_boolean'] = {
  // Boolean data type: true and false.
  category : Blockly.Msg.LANG_CATEGORY_LOGIC,
  init : function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendDummyInput().appendField(new Blockly.FieldDropdown(this.OPERATORS), 'BOOL');
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getFieldValue('BOOL');
      return Blockly.Blocks.logic_boolean.TOOLTIPS[op];
    });
  },
  helpUrl : function() {
    var op = this.getFieldValue('BOOL');
    return Blockly.Blocks.logic_boolean.HELPURLS[op];},
  typeblock: [{
    translatedName: Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE,
    dropDown: {
      titleName: 'BOOL',
      value: 'TRUE'
    }
  },{
    translatedName: Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE,
    dropDown: {
      titleName: 'BOOL',
      value: 'FALSE'
    }
  }]
};

Blockly.Blocks.logic_boolean.OPERATORS = [
    [ Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE, 'TRUE' ],
    [ Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE, 'FALSE' ]];

Blockly.Blocks.logic_boolean.TOOLTIPS = {
  TRUE : Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE,
  FALSE : Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE
};

Blockly.Blocks.logic_boolean.HELPURLS = {
  TRUE : Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE_HELPURL,
  FALSE : Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE_HELPURL
};

Blockly.Blocks['logic_false'] = {
  // Boolean data type: true and false.
  category : Blockly.Msg.LANG_CATEGORY_LOGIC,
  init : function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendDummyInput().appendField(new Blockly.FieldDropdown(Blockly.Blocks.logic_boolean.OPERATORS), 'BOOL');
    this.setFieldValue('FALSE','BOOL');
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getFieldValue('BOOL');
      return Blockly.Blocks.logic_boolean.TOOLTIPS[op];
    });
  },
  helpUrl : function() {
    var op = this.getFieldValue('BOOL');
    return Blockly.Blocks.logic_boolean.HELPURLS[op];}
};

Blockly.Blocks['logic_negate'] = {
  // Negation.
  category : Blockly.Msg.LANG_CATEGORY_LOGIC,
  helpUrl : Blockly.Msg.LANG_LOGIC_NEGATE_HELPURL,
  init : function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('BOOL').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.INPUT)).appendField('not');
    this.setTooltip(Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP);
  },
  typeblock: [{ translatedName:Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT }]
};

Blockly.Blocks['logic_compare'] = {
  // Comparison operator.
  category : Blockly.Msg.LANG_CATEGORY_LOGIC,
  helpUrl : function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.logic_compare.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('A');
    this.appendValueInput('B').appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.logic_compare.TOOLTIPS[mode];
    });
  },
  //TODO (user) compare has not been internationalized yet
  // Potential clash with Math =, so using 'logic equal' for now
  typeblock: [{ translatedName: 'logic equal' }]
};

Blockly.Blocks.logic_compare.TOOLTIPS = {
  EQ: 'Tests whether two things are equal. \n' +
        'The things being compared can be any thing, not only numbers.',
  NEQ: 'Tests whether two things are not equal. \n' +
        'The things being compared can be any thing, not only numbers.'
};

Blockly.Blocks.logic_compare.HELPURLS = {
  EQ: Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_EQ,
  NEQ: Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_NEQ
};

Blockly.Blocks.logic_compare.OPERATORS =
  [['=', 'EQ'],
   ['\u2260', 'NEQ']];

Blockly.Blocks['logic_operation'] = {
  // Logical operations: 'and', 'or'.
  category: Blockly.Msg.LANG_CATEGORY_LOGIC,
  init: function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.logic_operation.TOOLTIPS[op];
    });
  },
  helpUrl: function() {
      var op = this.getFieldValue('OP');
      return Blockly.Blocks.logic_operation.HELPURLS[op];
    },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_LOGIC_OPERATION_AND,
    dropDown: {
      titleName: 'OP',
      value: 'AND'
    }
  },{
    translatedName: Blockly.Msg.LANG_LOGIC_OPERATION_OR,
    dropDown: {
      titleName: 'OP',
      value: 'OR'
    }
  }]
};

Blockly.Blocks.logic_operation.OPERATORS =
    [[Blockly.Msg.LANG_LOGIC_OPERATION_AND, 'AND'],
     [Blockly.Msg.LANG_LOGIC_OPERATION_OR, 'OR']];

Blockly.Blocks.logic_operation.HELPURLS = {
  AND : Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND,
  OR : Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR
};
Blockly.Blocks.logic_operation.TOOLTIPS = {
  AND : Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND,
  OR : Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR
};

Blockly.Blocks['logic_or'] = {
  // Logical operations: 'and', 'or'.
  category: Blockly.Msg.LANG_CATEGORY_LOGIC,
  init: function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(Blockly.Blocks.logic_operation.OPERATORS), 'OP');
    this.setFieldValue('OR','OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.logic_operation.TOOLTIPS[op];
    });
  },
  helpUrl: function() {
    var op = this.getFieldValue('OP');
    return Blockly.Blocks.logic_operation.HELPURLS[op];
  }
};