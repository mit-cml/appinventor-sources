// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
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
  category: 'Logic',
  init: function () {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.appendDummyInput()
        .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'BOOL');
    var thisBlock = this;
    this.setTooltip(function () {
      var op = thisBlock.getFieldValue('BOOL');
      return Blockly.Blocks.logic_boolean.TOOLTIPS()[op];
    });
  },
  helpUrl: function () {
    var op = this.getFieldValue('BOOL');
    return Blockly.Blocks.logic_boolean.HELPURLS()[op];
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE,
    dropDown: {
      titleName: 'BOOL',
      value: 'TRUE'
    }
  }, {
    translatedName: Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE,
    dropDown: {
      titleName: 'BOOL',
      value: 'FALSE'
    }
  }]
};

Blockly.Blocks.logic_boolean.OPERATORS = function () {
  return [
    [Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE, 'TRUE'],
    [Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE, 'FALSE']
  ];
};

Blockly.Blocks.logic_boolean.TOOLTIPS = function () {
  return {
    TRUE: Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE,
    FALSE: Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE
  }
};

Blockly.Blocks.logic_boolean.HELPURLS = function () {
  return {
    TRUE: Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE_HELPURL,
    FALSE: Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE_HELPURL
  }
};

Blockly.Blocks['logic_false'] = {
  // Boolean data type: true and false.
  category: 'Logic',
  init: function () {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.appendDummyInput()
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.logic_boolean.OPERATORS), 'BOOL');
    this.setFieldValue('FALSE', 'BOOL');
    var thisBlock = this;
    this.setTooltip(function () {
      var op = thisBlock.getFieldValue('BOOL');
      return Blockly.Blocks.logic_boolean.TOOLTIPS()[op];
    });
  },
  helpUrl: function () {
    var op = this.getFieldValue('BOOL');
    return Blockly.Blocks.logic_boolean.HELPURLS()[op];
  }
};

Blockly.Blocks['logic_negate'] = {
  // Negation.
  category: 'Logic',
  helpUrl: Blockly.Msg.LANG_LOGIC_NEGATE_HELPURL,
  init: function () {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('BOOL')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT);
    this.setTooltip(Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT}]
};

Blockly.Blocks['logic_compare'] = {
  // Comparison operator.
  category: 'Logic',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.logic_compare.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('A');
    this.appendValueInput('B')
        .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.logic_compare.TOOLTIPS()[mode];
    });
  },
  // Potential clash with Math =, so using 'logic equal' for now
  typeblock: [{translatedName: Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME}]
};

Blockly.Blocks.logic_compare.TOOLTIPS = function () {
  return {
    EQ: Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ,
    NEQ: Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ
  }
};

Blockly.Blocks.logic_compare.HELPURLS = function () {
  return {
    EQ: Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_EQ,
    NEQ: Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_NEQ
  }
};

Blockly.Blocks.logic_compare.OPERATORS = function () {
  return [
    [Blockly.Msg.LANG_LOGIC_COMPARE_EQ, 'EQ'],
    [Blockly.Msg.LANG_LOGIC_COMPARE_NEQ, 'NEQ']
  ];
};

// Blockly.Blocks['logic_operation'] = {
//   // Logical operations: 'and', 'or'.
//   category: 'Logic',
//   init: function () {
//     this.setColour(Blockly.LOGIC_CATEGORY_HUE);
//     this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
//     this.appendValueInput('A')
//         .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
//     this.appendValueInput('B')
//         .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT))
//         .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
//     this.setInputsInline(true);
//     // Assign 'this' to a variable for use in the tooltip closure below.
//     var thisBlock = this;
//     this.setTooltip(function () {
//       var op = thisBlock.getFieldValue('OP');
//       return Blockly.Blocks.logic_operation.TOOLTIPS()[op];
//     });
//     this.setMutator(new Blockly.Mutator(['logic_mutator_item']));
//     this.emptyInputName = 'EMPTY';
//     this.repeatingInputName = 'BOOL';
//     this.itemCount = 2;
//   },
//   mutationToDom: Blockly.mutationToDom,
//   domToMutation: Blockly.domToMutation,
//   decompose: function (workspace) {
//     return Blockly.decompose(workspace, 'logic_mutator_item', this);
//   },
//   compose: Blockly.compose,
//   saveConnections: Blockly.saveConnections,
//   addEmptyInput: function () {
//     var input = this.appendDummyInput(this.emptyInputName);
//   },
//   addInput: function (inputNum) {
//     var input = this.appendValueInput(this.repeatingInputName + inputNum)
//         .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
//     if (inputNum !== 0) {
//       input.appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
//     }
//     return input;
//   },
//   updateContainerBlock: function (containerBlock) {
//     containerBlock.setFieldValue(this.getFieldValue('OP'), "CONTAINER_TEXT");
//   },
//   helpUrl: function () {
//     var op = this.getFieldValue('OP');
//     return Blockly.Blocks.logic_operation.HELPURLS()[op];
//   },
//   typeblock: [{
//     translatedName: Blockly.Msg.LANG_LOGIC_OPERATION_AND,
//     dropDown: {
//       titleName: 'OP',
//       value: 'AND'
//     }
//   }, {
//     translatedName: Blockly.Msg.LANG_LOGIC_OPERATION_OR,
//     dropDown: {
//       titleName: 'OP',
//       value: 'OR'
//     }
//   }]
// };

// Blockly.Blocks.logic_operation.OPERATORS = function () {
//   return [
//     [Blockly.Msg.LANG_LOGIC_OPERATION_AND, 'AND'],
//     [Blockly.Msg.LANG_LOGIC_OPERATION_OR, 'OR']
//   ]
// };

// Blockly.Blocks.logic_operation.HELPURLS = function () {
//   return {
//     AND: Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND,
//     OR: Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR
//   }
// };
// Blockly.Blocks.logic_operation.TOOLTIPS = function () {
//   return {
//     AND: Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND,
//     OR: Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR
//   }
// };
//
// Blockly.Blocks['logic_or'] = {
//   // Logical operations: 'and', 'or'.
//   category: 'Logic',
//   // helpUrl: Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR,
//   init: function () {
//     this.setColour(Blockly.LOGIC_CATEGORY_HUE);
//     this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
//     this.appendValueInput('A')
//         .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
//     this.appendValueInput('B')
//         .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT))
//         .appendField(new Blockly.FieldDropdown(Blockly.Blocks.logic_operation.OPERATORS), 'OP');
//     this.setFieldValue('OR', 'OP');
//     this.setInputsInline(true);
//     // Assign 'this' to a variable for use in the tooltip closure below.
//     var thisBlock = this;
//     this.setTooltip(function () {
//       var op = thisBlock.getFieldValue('OP');
//       return Blockly.Blocks.logic_operation.TOOLTIPS[op];
//     });
//     this.setMutator(new Blockly.Mutator(['logic_mutator_item']));
//     this.emptyInputName = 'EMPTY';
//     this.repeatingInputName = 'BOOL';
//     this.itemCount = 2;
//   },
//   mutationToDom: Blockly.mutationToDom,
//   domToMutation: Blockly.domToMutation,
//   decompose: function (workspace) {
//     return Blockly.decompose(workspace, 'logic_mutator_item', this);
//   },
//   compose: Blockly.compose,
//   saveConnections: Blockly.saveConnections,
//   addEmptyInput: function () {
//     var input = this.appendDummyInput(this.emptyInputName);
//   },
//   addInput: function (inputNum) {
//     var input = this.appendValueInput(this.repeatingInputName + inputNum)
//         .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
//     if (inputNum !== 0) {
//       //input.appendField(Blockly.Msg.LANG_LOGIC_OPERATION_OR);
//       input.appendField(new Blockly.FieldDropdown(Blockly.Blocks.logic_operation.OPERATORS), 'OP');
//     }
//     return input;
//   },
//   updateContainerBlock: function (containerBlock) {
//     containerBlock.setFieldValue(this.getFieldValue('OP'), "CONTAINER_TEXT");
//   },
//   helpUrl: function () {
//     var op = this.getFieldValue('OP');
//     return Blockly.Blocks.logic_operation.HELPURLS[op];
//   }
// };

Blockly.Blocks['logic_and'] = {
  // Logical operations: 'and'.
  category: 'Logic',
  helpUrl: Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND,
  init: function () {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('BOOL0')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
    this.appendValueInput('BOOL1')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_LOGIC_OPERATION_AND);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      return Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND;
    });
    this.setMutator(new Blockly.Mutator(['logic_mutator_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'BOOL';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function (workspace) {
    return Blockly.decompose(workspace, 'logic_mutator_item', this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function () {
    var input = this.appendDummyInput(this.emptyInputName);
  },
  addInput: function (inputNum) {
    var input = this.appendValueInput(this.repeatingInputName + inputNum)
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
    if (inputNum !== 0) {
      input.appendField(Blockly.Msg.LANG_LOGIC_OPERATION_AND);
    }
    return input;
  },
  updateContainerBlock: function (containerBlock) {
    containerBlock.setFieldValue("and", "CONTAINER_TEXT");
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_LOGIC_OPERATION_AND}]
};

Blockly.Blocks['logic_or'] = {
  // Logical operations: 'or'.
  category: 'Logic',
  helpUrl: Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR,
  init: function () {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('BOOL0')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
    this.appendValueInput('BOOL1')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_LOGIC_OPERATION_OR);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      return Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR;
    });
    this.setMutator(new Blockly.Mutator(['logic_mutator_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'BOOL';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function (workspace) {
    return Blockly.decompose(workspace, 'logic_mutator_item', this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function () {
    var input = this.appendDummyInput(this.emptyInputName);
  },
  addInput: function (inputNum) {
    var input = this.appendValueInput(this.repeatingInputName + inputNum)
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
    if (inputNum !== 0) {
      input.appendField(Blockly.Msg.LANG_LOGIC_OPERATION_OR);
    }
    return input;
  },
  updateContainerBlock: function (containerBlock) {
    containerBlock.setFieldValue("or", "CONTAINER_TEXT");
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_LOGIC_OPERATION_OR}]
};

Blockly.Blocks['logic_mutator_item'] = {
  // Add items.
  init: function () {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.appendDummyInput().appendField("boolean");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.contextMenu = false;
  }
};
