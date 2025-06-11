// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Matrices blocks for Blockly, modified for MIT App Inventor.
 * @author jackyc@mit.edu (Jacky Chen)
 */

'use strict';

goog.provide('AI.Blocks.matrices');

goog.require('AI.BlockUtils');

Blockly.Blocks['matrices_create'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_CREATE_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));

    this.appendDummyInput()
      .appendField(Blockly.Msg.LANG_MATRICES_CREATE)
      .appendField(new Blockly.FieldNumber(2, 1, null, null), 'ROWS')
      .appendField(AI.BlockUtils.times_symbol)
      .appendField(new Blockly.FieldNumber(2, 1, null, null), 'COLS');

    this.setTooltip(Blockly.Msg.LANG_MATRICES_CREATE_TOOLTIP);

    this.matrixValues = [];
    this.createMatrix(2, 2);
  },

  createMatrix: function (rows, cols) {
    var oldMatrix = this.matrixValues || [];

    for (var i = 0; this.getInput('ROW' + i); i++) {
      this.removeInput('ROW' + i);
    }

    this.matrixValues = [];
    for (var i = 0; i < rows; i++) {
      var rowInput = this.appendDummyInput('ROW' + i);
      this.matrixValues[i] = [];

      for (var j = 0; j < cols; j++) {
        var fieldName = 'MATRIX_' + i + '_' + j;
        var oldValue = (oldMatrix[i] && oldMatrix[i][j] !== undefined) ? oldMatrix[i][j] : 0;
        this.matrixValues[i][j] = oldValue;

        rowInput.appendField(new Blockly.FieldNumber(oldValue, null, null, null, this.updateMatrixValue.bind(this, i, j)), fieldName);
      }
    }
  },

  updateMatrixSize: function () {
    var rows = Number(this.getFieldValue('ROWS')) || 2;
    var cols = Number(this.getFieldValue('COLS')) || 2;
    this.createMatrix(rows, cols);
  },

  updateMatrixValue: function (row, col, value) {
    if (this.matrixValues[row] && this.matrixValues[row][col] !== undefined) {
      this.matrixValues[row][col] = Number(value) || 0;
    }
  },

  mutationToDom: function () {
    var container = document.createElement('mutation');
    container.setAttribute('rows', this.getFieldValue('ROWS'));
    container.setAttribute('cols', this.getFieldValue('COLS'));
    container.setAttribute('matrix', JSON.stringify(this.matrixValues));
    return container;
  },

  domToMutation: function (xmlElement) {
    var rows = parseInt(xmlElement.getAttribute('rows'), 10);
    var cols = parseInt(xmlElement.getAttribute('cols'), 10);
    this.matrixValues = JSON.parse(xmlElement.getAttribute('matrix'));
    this.createMatrix(rows, cols);
    this.setFieldValue(rows, 'ROWS');
    this.setFieldValue(cols, 'COLS');
  },

  onchange: function (event) {
    if (event.type === Blockly.Events.CHANGE && event.element === 'field') {
      if (event.name === 'ROWS' || event.name === 'COLS') {
        this.updateMatrixSize();
      } else if (event.name.startsWith('MATRIX_')) {
        var parts = event.name.split('_');
        var row = parseInt(parts[1], 10);
        var col = parseInt(parts[2], 10);
        this.updateMatrixValue(row, col, event.newValue);
      }
    }
  },

  saveConnections: Blockly.saveConnections,

  typeblock: [{ translatedName: Blockly.Msg.LANG_MATRICES_CREATE }]
};

Blockly.Blocks['matrices_create_multidim'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_CREATE_MULTIDIM_HELPURL,
  init: function() {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));

    this.appendValueInput('DIM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATRICES_CREATE_MULTIDIM);
    this.appendValueInput('INITIAL')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATRICES_CREATE_MULTIDIM_INITIAL)
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_MATRICES_CREATE_MULTIDIM_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_MATRICES_CREATE_MULTIDIM }]
};

Blockly.Blocks['matrix_mutator_item_dim'] = {
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField("dimension");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.contextMenu = false;
  }
};

Blockly.Blocks['matrices_get_cell'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_GET_CELL_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));

    this.appendValueInput('MATRIX')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATRICES_GET_CELL)
        .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_MATRIX);
    this.appendValueInput('DIM0')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM + "1")
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('DIM1')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM + "2")
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_MATRICES_GET_CELL_TOOLTIP);

    this.setMutator(new Blockly.icons.MutatorIcon(['matrix_mutator_item_dim'], this));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'DIM';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: function(container) {
    Blockly.domToMutation.call(this, container);
  },
  decompose: function(workspace) {
    return Blockly.decompose(workspace, 'matrix_mutator_item_dim', this);
  },
  compose: function(containerBlock) {
    Blockly.compose.call(this, containerBlock);
  },
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function() {
    var input = this.appendValueInput(this.repeatingInputName + '0')
    .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    input.appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM + "1")
        .setAlign(Blockly.ALIGN_RIGHT);
    this.itemCount_ = 1;
  },
  addInput: function(n) {
    var input = this.appendValueInput(this.repeatingInputName + n)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    input.appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM + (n + 1))
        .setAlign(Blockly.ALIGN_RIGHT);
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.setFieldValue(
      Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM, 'CONTAINER_TEXT');
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_MATRICES_GET_CELL }]
};

Blockly.Blocks['matrices_set_cell'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_SET_CELL_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.appendValueInput('MATRIX')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_SET_CELL)
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_MATRIX);
    this.appendValueInput('VALUE')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_VALUE)
      .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('DIM0')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM + "1")
      .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('DIM1')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM + "2")
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_MATRICES_SET_CELL_TOOLTIP);

    this.setMutator(new Blockly.icons.MutatorIcon(['matrix_mutator_item_dim'], this));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'DIM';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: function(container) {
    Blockly.domToMutation.call(this, container);
  },
  decompose: function(workspace) {
    return Blockly.decompose(workspace, 'matrix_mutator_item_dim', this);
  },
  compose: function(containerBlock) {
    Blockly.compose.call(this, containerBlock);
  },
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function() {
    var input = this.appendValueInput(this.repeatingInputName + '0')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    input.appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM + "1")
         .setAlign(Blockly.ALIGN_RIGHT);
    this.itemCount_ = 1;
  },
  addInput: function(n) {
    var input = this.appendValueInput(this.repeatingInputName + n)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    input.appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM + (n + 1))
        .setAlign(Blockly.ALIGN_RIGHT);
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.setFieldValue(
      Blockly.Msg.LANG_MATRICES_GET_INPUT_DIM, 'CONTAINER_TEXT');
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_MATRICES_SET_CELL }]
};

Blockly.Blocks['matrices_get_row'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_GET_ROW_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.OUTPUT));
    this.appendValueInput('MATRIX')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_ROW)
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_MATRIX);
    this.appendValueInput('ROW')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_ROW)
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_MATRICES_GET_ROW_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATRICES_GET_ROW}]
};

Blockly.Blocks['matrices_get_column'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_GET_COLUMN_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.OUTPUT));
    this.appendValueInput('MATRIX')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_COLUMN)
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_MATRIX);
    this.appendValueInput('COLUMN')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_COLUMN)
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_MATRICES_GET_COLUMN_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATRICES_GET_COLUMN}]
};

Blockly.Blocks['matrix_mutator_item_mat'] = {
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField("matrix");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.contextMenu = false;
  }
};

Blockly.Blocks['matrices_add'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_ADD_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));
    this.appendValueInput('MAT0')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT));
    this.appendValueInput('MAT1')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
    this.setInputsInline(true);
    this.setTooltip(Blockly.Msg.LANG_MATRICES_ARITHMETIC_ADD_TOOLTIP);

    this.setMutator(new Blockly.icons.MutatorIcon(['matrix_mutator_item_mat'], this));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'MAT';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: function(container) {
    Blockly.domToMutation.call(this, container);
    if (this.itemCount_ === 1) {
      this.inputList[0].appendField('0 ' + Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
    }
  },
  decompose: function(workspace) {
    return Blockly.decompose(workspace, 'matrix_mutator_item_mat', this);
  },
  compose: function(containerBlock) {
    Blockly.compose.call(this, containerBlock);
    if (this.itemCount_ === 1) {
      this.inputList[0].appendField('0 ' + Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
    }
  },
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function() {
    this.appendDummyInput(this.emptyInputName)
        .appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
  },
  addInput: function(n) {
    var input = this.appendValueInput(this.repeatingInputName + n)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT));
    if (n !== 0) {
      input.appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.setFieldValue(
      Blockly.Msg.LANG_MATH_ARITHMETIC_ADD, 'CONTAINER_TEXT');
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ARITHMETIC_ADD}]
};

Blockly.Blocks['matrices_subtract'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_SUBTRACT_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));
    this.appendValueInput('A')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT));
    this.appendValueInput('B')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS);
    this.setInputsInline(true);
    this.setTooltip(Blockly.Msg.LANG_MATRICES_ARITHMETIC_SUBTRACT_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS}]
};

Blockly.Blocks['matrices_multiply'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_MULTIPLY_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));
    this.appendValueInput('MAT0')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT));
    this.appendValueInput('MAT1')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT)
          .concat(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT)))
        .appendField(AI.BlockUtils.times_symbol);
    this.setInputsInline(true);
    this.setTooltip(Blockly.Msg.LANG_MATRICES_ARITHMETIC_MULTIPLY_TOOLTIP);

    this.setMutator(new Blockly.icons.MutatorIcon(['matrix_mutator_item_mat'], this));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'MAT';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: function(container) {
    Blockly.domToMutation.call(this, container);
    if (this.itemCount_ === 1) {
      this.inputList[0].appendField('1 ' + AI.BlockUtils.times_symbol);
    }
  },
  decompose: function(workspace) {
    return Blockly.decompose(workspace, 'matrix_mutator_item_mat', this);
  },
  compose: function(containerBlock) {
    Blockly.compose.call(this, containerBlock);
    if (this.itemCount_ === 1) {
      this.inputList[0].appendField('1 ' + AI.BlockUtils.times_symbol);
    }
  },
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function() {
    this.appendDummyInput(this.emptyInputName)
        .appendField(AI.BlockUtils.times_symbol);
  },
  addInput: function(n) {
    var input = this.appendValueInput(this.repeatingInputName + n)
        .setCheck(
          n === 0
            ? AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT)
            : AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT)
                .concat(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        );
    if (n !== 0) {
      input.appendField(AI.BlockUtils.times_symbol);
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.setFieldValue(
      AI.BlockUtils.times_symbol, 'CONTAINER_TEXT');
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY}]
};

Blockly.Blocks['matrices_power'] = {
  category: 'Matrices',
  helpUrl: Blockly.Msg.LANG_MATRICES_POWER_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));
    this.appendValueInput('A')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT));
    this.appendValueInput('B')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_POWER);
    this.setInputsInline(true);
    this.setTooltip(Blockly.Msg.LANG_MATRICES_ARITHMETIC_POWER_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ARITHMETIC_POWER}]
};

Blockly.Blocks['matrices_operations'] = {
  category: 'Matrices',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.matrices_operations.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));
    this.appendValueInput('MATRIX')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
      .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.matrices_operations.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATRICES_OPERATIONS_INVERSE,
    dropDown: {
      titleName: 'OP',
      value: 'INVERSE'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATRICES_OPERATIONS_TRANSPOSE,
    dropDown: {
      titleName: 'OP',
      value: 'TRANSPOSE'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATRICES_OPERATIONS_ROTATE_LEFT,
    dropDown: {
      titleName: 'OP',
      value: 'ROTATE_LEFT'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATRICES_OPERATIONS_ROTATE_RIGHT,
    dropDown: {
      titleName: 'OP',
      value: 'ROTATE_RIGHT'
    }
  }]
};

Blockly.Blocks.matrices_operations.OPERATORS = function () {
  return [[Blockly.Msg.LANG_MATRICES_OPERATIONS_INVERSE, 'INVERSE'],
    [Blockly.Msg.LANG_MATRICES_OPERATIONS_TRANSPOSE, 'TRANSPOSE'],
    [Blockly.Msg.LANG_MATRICES_OPERATIONS_ROTATE_LEFT, 'ROTATE_LEFT'],
    [Blockly.Msg.LANG_MATRICES_OPERATIONS_ROTATE_RIGHT, 'ROTATE_RIGHT']
  ];
};

Blockly.Blocks.matrices_operations.TOOLTIPS = function () {
  return {
    INVERSE: Blockly.Msg.LANG_MATRICES_OPERATIONS_TOOLTIP_INVERSE,
    TRANSPOSE: Blockly.Msg.LANG_MATRICES_OPERATIONS_TOOLTIP_TRANSPOSE,
    ROTATE_LEFT: Blockly.Msg.LANG_MATRICES_OPERATIONS_TOOLTIP_ROTATE_LEFT,
    ROTATE_RIGHT: Blockly.Msg.LANG_MATRICES_OPERATIONS_TOOLTIP_ROTATE_RIGHT
  }
};

Blockly.Blocks.matrices_operations.HELPURLS = function () {
  return {
    INVERSE: Blockly.Msg.LANG_MATRICES_OPERATIONS_HELPURL_INVERSE,
    TRANSPOSE: Blockly.Msg.LANG_MATRICES_OPERATIONS_HELPURL_TRANSPOSE,
    ROTATE_LEFT: Blockly.Msg.LANG_MATRICES_OPERATIONS_HELPURL_ROTATE_LEFT,
    ROTATE_RIGHT: Blockly.Msg.LANG_MATRICES_OPERATIONS_HELPURL_ROTATE_RIGHT
  }
};

Blockly.Blocks['matrices_transpose'] = {
  category: 'Matrices',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.matrices_operations.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));
    this.appendValueInput('MATRIX')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.matrices_operations.OPERATORS), 'OP');
    this.setFieldValue('TRANSPOSE', "OP");
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.matrices_operations.TOOLTIPS()[mode];
    });
  }
};

Blockly.Blocks['matrices_rotate_left'] = {
  category: 'Matrices',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.matrices_operations.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));
    this.appendValueInput('MATRIX')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.matrices_operations.OPERATORS), 'OP');
    this.setFieldValue('ROTATE_LEFT', "OP");
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.matrices_operations.TOOLTIPS()[mode];
    });
  }
};

Blockly.Blocks['matrices_rotate_right'] = {
  category: 'Matrices',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.matrices_operations.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.OUTPUT));
    this.appendValueInput('MATRIX')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("matrix", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.matrices_operations.OPERATORS), 'OP');
    this.setFieldValue('ROTATE_RIGHT', "OP");
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.matrices_operations.TOOLTIPS()[mode];
    });
  }
};