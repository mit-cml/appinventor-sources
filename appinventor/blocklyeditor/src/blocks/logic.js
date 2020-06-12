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

goog.require('Blockly.Mutator');
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

Blockly.Blocks['logic_operation'] = {
  // Logical operations: 'and', 'or'.
  category: 'Logic',
  init: function (op) {
    op = op || 'AND';
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.opField = new Blockly.FieldDropdown(
      Blockly.Blocks.logic_operation.OPERATORS, function(op) {
        return thisBlock.updateFields(op);
      });
    /**
     * Reference to the last mutator workspace so we can update the container block's label when
     * the dropdown value changes.
     *
     * @type {Blockly.WorkspaceSvg}
     */
    this.lastMutator = null;
    // NOTE(ewp): Blockly doesn't trigger the validation function when the field is set during
    // load, so we override setValue here to make sure that the additional and/or labels (if
    // present) match the dropdown's value.
    var oldSetValue = this.opField.setValue;
    this.opField.setValue = function(newValue) {
      oldSetValue.call(this, newValue);
      thisBlock.updateFields(newValue);
    };
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('A')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
    this.appendValueInput('B')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT))
        .appendField(this.opField, 'OP');
    this.setFieldValue(op, 'OP');
    this.setInputsInline(true);
    this.setTooltip(function () {
      return Blockly.Blocks.logic_operation.TOOLTIPS()[thisBlock.getFieldValue('OP')];
    });
    this.setMutator(new Blockly.Mutator(['logic_mutator_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'BOOL';
    this.itemCount_ = 2;
    this.valuesToSave = {'op': op};
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: function(container) {
    if (this.valuesToSave != null) {
      for (var name in this.valuesToSave) {
        this.valuesToSave[name] = this.getFieldValue(name);
      }
    }

    for (var x = 2; x < this.itemCount_; x++) {
      this.removeInput(this.repeatingInputName + x);
    }
    this.itemCount_ = window.parseInt(container.getAttribute('items'), 10);
    for (var x = 2; x < this.itemCount_; x++) {
      this.addInput(x);
    }
  },
  decompose: function(workspace) {
    var containerBlockName = 'mutator_container';
    var containerBlock = workspace.newBlock(containerBlockName);
    containerBlock.setColour(this.getColour());
    containerBlock.setFieldValue(this.opField.getText(), 'CONTAINER_TEXT');
    containerBlock.initSvg();
    var connection = containerBlock.getInput('STACK').connection;
    for (var x = 0; x < this.itemCount_; x++) {
      var itemBlock = workspace.newBlock('logic_mutator_item');
      itemBlock.initSvg();
      connection.connect(itemBlock.previousConnection);
      connection = itemBlock.nextConnection;
    }
    this.lastMutator = workspace;
    return containerBlock;
  },
  compose: function(containerBlock) {
    if (this.valuesToSave != null) {
      for (var name in this.valuesToSave) {
        this.valuesToSave[name] = this.getFieldValue(name);
      }
    }
    // Disconnect all input blocks and destroy all inputs.
    for (var x = this.itemCount_ - 1; x >= 0; x--) {
      this.removeInput(x > 1 ? this.repeatingInputName + x : ['A', 'B'][x]);
    }
    this.itemCount_ = 0;
    // Rebuild the block's inputs.
    var itemBlock = containerBlock.getInputTargetBlock('STACK')
    while (itemBlock) {

      var input = this.addInput(this.itemCount_)

      // Reconnect any child blocks.
      if (itemBlock.valueConnection_) {
        input.connection.connect(itemBlock.valueConnection_);
      }
      this.itemCount_++;
      itemBlock = itemBlock.nextConnection &&
        itemBlock.nextConnection.targetBlock();
    }
  },
  saveConnections: function(containerBlock) {
    // Store a pointer to any connected child blocks.
    var itemBlock = containerBlock.getInputTargetBlock('STACK');
    var x = 0;
    while (itemBlock) {
      var input = this.getInput(x > 1 ? this.repeatingInputName + x : ['A', 'B'][x]);
      itemBlock.valueConnection_ = input && input.connection.targetConnection;
      x++;
      itemBlock = itemBlock.nextConnection && itemBlock.nextConnection.targetBlock();
    }
  },
  addInput: function (inputNum) {
    var name = inputNum > 1 ? this.repeatingInputName + inputNum : ['A', 'B'][inputNum];
    var input = this.appendValueInput(name)
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.INPUT));
    if (this.getInputsInline()) {
      if (inputNum == 1) {
        var op = this.opField.getValue();
        this.opField = new Blockly.FieldDropdown(
          Blockly.Blocks.logic_operation.OPERATORS(),
          this.updateFields.bind(this));
        this.opField.setValue(op);
        input.appendField(this.opField, 'OP');
        this.opField.init();
      } else if (inputNum > 1) {
        var field = new Blockly.FieldLabel(this.opField.getText());
        input.appendField(field);
        field.init();
      }
    } else if (inputNum == 0) {
      var op = this.opField.getValue();
      this.opField = new Blockly.FieldDropdown(
        Blockly.Blocks.logic_operation.OPERATORS.OPERATORS,
        this.updateFields.bind(this));
      this.opField.setValue(op);
      input.appendField(this.opField, 'OP');
      this.opField.init();
    }
    return input;
  },
  helpUrl: function () {
    var op = this.getFieldValue('OP');
    return Blockly.Blocks.logic_operation.HELPURLS()[op];
  },
  setInputsInline: function(inline) {
    if (inline) {
      var ainput = this.getInput('A');
      if (ainput.fieldRow.length > 0) {
        ainput.fieldRow.splice(0, 1);
        var binput = this.getInput('B');
        binput.fieldRow.splice(0, 0, this.opField);
      }
      for (var input, i = 2; (input = this.inputList[i]); i++) {
        var field = new Blockly.FieldLabel(this.opField.getText());
        input.appendField(field);
        field.init();
      }
    } else {
      var binput = this.getInput('B');
      if (binput.fieldRow.length > 0) {
        binput.fieldRow.splice(0, 1);
        var ainput = this.getInput('A');
        ainput.fieldRow.splice(0, 0, this.opField);
      }
      for (var input, i = 2; (input = this.inputList[i]); i++) {
        input.fieldRow[0].dispose();
        input.fieldRow.splice(0, 1);
      }
    }
    Blockly.BlockSvg.prototype.setInputsInline.call(this, inline);
  },
  updateFields: function(op) {
    if (this.getInputsInline()) {
      var text = op == 'AND' ? Blockly.Msg.LANG_LOGIC_OPERATION_AND :
        Blockly.Msg.LANG_LOGIC_OPERATION_OR;
      for (var input, i = 2; (input = this.inputList[i]); i++) {
        input.fieldRow[0].setText(text);
      }
    }
    // Update the mutator container block if the mutator is open
    if (this.lastMutator) {
      var mutatorBlock = this.lastMutator.getTopBlocks()[0];
      var title = op === 'AND' ? Blockly.Msg.LANG_LOGIC_OPERATION_AND :
        Blockly.Msg.LANG_LOGIC_OPERATION_OR;
      mutatorBlock.setFieldValue(title, 'CONTAINER_TEXT');
    }
    return op;
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_LOGIC_OPERATION_AND,
    dropDown: {
      titleName: 'OP',
      value: 'AND'
    }
  }, {
    translatedName: Blockly.Msg.LANG_LOGIC_OPERATION_OR,
    dropDown: {
      titleName: 'OP',
      value: 'OR'
    }
  }]
};

Blockly.Blocks.logic_operation.OPERATORS = function () {
  return [
    [Blockly.Msg.LANG_LOGIC_OPERATION_AND, 'AND'],
    [Blockly.Msg.LANG_LOGIC_OPERATION_OR, 'OR']
  ]
};

Blockly.Blocks.logic_operation.HELPURLS = function () {
  return {
    AND: Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND,
    OR: Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR
  }
};
Blockly.Blocks.logic_operation.TOOLTIPS = function () {
  return {
    AND: Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND,
    OR: Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR
  }
};

Blockly.Blocks['logic_or'] = {
  // Logical operations: 'and', 'or'.
  category: 'Logic',
  init: function () {
    Blockly.Blocks['logic_operation'].init.call(this, 'OR');
  },
  mutationToDom: Blockly.Blocks['logic_operation'].mutationToDom,
  domToMutation: Blockly.Blocks['logic_operation'].domToMutation,
  decompose: Blockly.Blocks['logic_operation'].decompose,
  compose: Blockly.Blocks['logic_operation'].compose,
  saveConnections: Blockly.Blocks['logic_operation'].saveConnections,
  addInput: Blockly.Blocks['logic_operation'].addInput,
  helpUrl: Blockly.Blocks['logic_operation'].helpUrl,
  setInputsInline: Blockly.Blocks['logic_operation'].setInputsInline,
  updateFields: Blockly.Blocks['logic_operation'].updateFields
};

Blockly.Blocks['logic_mutator_item'] = {
  // Add items.
  init: function () {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.appendDummyInput().appendField("boolean");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.contextMenu = false;
  },
  isMovable: function() {
    if (this.previousConnection.targetBlock()) {
      var parent = this.previousConnection.targetBlock();
      if (parent.type == 'mutator_container') {
        return false;
      } else if(parent.previousConnection.targetBlock() &&
        parent.previousConnection.targetBlock().type == 'mutator_container') {
        return false;
      }
    }
    return true;
  }
};
