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

goog.provide('Blockly.Blocks.matrices');

goog.require('Blockly.Blocks.Utilities');

Blockly.Blocks['matrices_create'] = {
  category: 'Matrices',
  // helpUrl: Blockly.Msg.LANG_MATRICES_CREATE_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.OUTPUT));
    this.appendDummyInput()
      .appendField(Blockly.Msg.LANG_MATRICES_CREATE)
      .appendField(new Blockly.FieldNumber(2, 1, null, null, this.triggerMatrixSizeUpdate.bind(this)), 'ROWS')
      .appendField(Blockly.Blocks.Utilities.times_symbol)
      .appendField(new Blockly.FieldNumber(2, 1, null, null, this.triggerMatrixSizeUpdate.bind(this)), 'COLS');

    this.matrixValues = [[0, 0], [0, 0]];
    this.createMatrix(2, 2);
  },

  createMatrix: function(rows, cols) {
    var newMatrix = [];

    for (var i = 0; i < rows; i++) {
      var row = [];
      for (var j = 0; j < cols; j++) {
        var value = (this.matrixValues && this.matrixValues[i] && this.matrixValues[i][j] !== undefined) ? this.matrixValues[i][j] : 0;
        row.push(value);
      }
      newMatrix.push(row);
    }

    this.matrixValues = newMatrix;

    // Update the block's UI with matrix values
    for (var i = 0; i < rows; i++) {
      var rowInput;
      if (!this.getInput('ROW' + i)) {
        rowInput = this.appendDummyInput('ROW' + i);  // Create new row input if not exists
      } else {
        rowInput = this.getInput('ROW' + i);  // Use existing row input
      }

      // Clear old fields
      while (rowInput.fieldRow.length > 1) {
        rowInput.removeField(rowInput.fieldRow[1].name);
      }

      // Add fields for each cell in the row
      for (var j = 0; j < cols; j++) {
        var fieldName = 'MATRIX_' + i + '_' + j;
        var self = this;

        // Create number input for each matrix cell
        var field = new Blockly.FieldNumber(this.matrixValues[i][j], null, null, null, function(value) {
          self.updateMatrixValue(i, j, value);  // Update matrix value on change
        });

        // Add or update the field
        if (!this.getField(fieldName)) {
          rowInput.appendField(field, fieldName);
        } else {
          this.setFieldValue(this.matrixValues[i][j], fieldName);  // Update field value
        }
      }
    }

    // Remove excess rows if matrix size is reduced
    for (var i = rows; this.getInput('ROW' + i); i++) {
      this.removeInput('ROW' + i);
    }

    this.setInputsInline(false);
  },

  // Update matrix values when a cell changes
  updateMatrixValue: function(row, col, value) {
    if (this.matrixValues[row] && this.matrixValues[row][col] !== undefined) {
      this.matrixValues[row][col] = Number(value) || 0;
    }
  },

  // Trigger matrix size update only when the user confirms the input change
  triggerMatrixSizeUpdate: function() {
    var rows = Number(this.getFieldValue('ROWS')) || 2;
    var cols = Number(this.getFieldValue('COLS')) || 2;

    if (!this.matrixValues || !Array.isArray(this.matrixValues) || this.matrixValues.length === 0) {
      this.matrixValues = [];
      for (var i = 0; i < rows; i++) {
        var row = [];
        for (var j = 0; j < cols; j++) {
          row.push(0);
        }
        this.matrixValues.push(row);
      }
    }

    if (rows !== this.matrixValues.length || cols !== this.matrixValues[0].length) {
      this.createMatrix(rows, cols);
    }
  },

  onchange: function(event) {
    if (event.type === Blockly.Events.CHANGE && event.element === 'field' && (event.name === 'ROWS' || event.name === 'COLS')) {
      this.triggerMatrixSizeUpdate();
    }
  },

  typeblock: [{translatedName: Blockly.Msg.LANG_MATRICES_CREATE}]
};

Blockly.Blocks['matrices_get_row'] = {
  category: 'Matrices',
  // helpUrl: Blockly.Msg.LANG_MATRICES_GET_ROW_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("list", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('MATRIX')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_ROW)
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_MATRIX);
    this.appendValueInput('ROW')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_ROW)
      .setAlign(Blockly.ALIGN_RIGHT);
    // this.setTooltip(Blockly.Msg.LANG_MATRICES_GET_ROW_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATRICES_GET_ROW}]
}

Blockly.Blocks['matrices_get_column'] = {
  category: 'Matrices',
  // helpUrl: Blockly.Msg.LANG_MATRICES_GET_COLUMN_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("list", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('MATRIX')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_COLUMN)
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_MATRIX);
    this.appendValueInput('COL')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_COLUMN)
      .setAlign(Blockly.ALIGN_RIGHT);
    // this.setTooltip(Blockly.Msg.LANG_MATRICES_GET_COLUMN_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATRICES_GET_COLUMN}]
}

Blockly.Blocks['matrices_get_cell'] = {
  category: 'Matrices',
  // helpUrl: Blockly.Msg.LANG_MATRICES_GET_CELL_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('MATRIX')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_CELL)
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_MATRIX);
    this.appendValueInput('ROW')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_ROW)
      .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('COL')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_COLUMN)
      .setAlign(Blockly.ALIGN_RIGHT);
    // this.setTooltip(Blockly.Msg.LANG_MATRICES_GET_CELL_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATRICES_GET_CELL}]
}

Blockly.Blocks['matrices_set_cell'] = {
  category: 'Matrices',
  // helpUrl: Blockly.Msg.LANG_MATRICES_SET_CELL_HELPURL,
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.appendValueInput('MATRIX')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_SET_CELL)
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_MATRIX);
    this.appendValueInput('ROW')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_ROW)
      .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('COLUMN')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_COLUMN)
      .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('VALUE')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_MATRICES_GET_INPUT_VALUE)
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    // this.setTooltip(Blockly.Msg.LANG_MATRICES_SET_CELL_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATRICES_SET_CELL}]
}

Blockly.Blocks['matrices_operations'] = {
  category: 'Matrices',
  // helpUrl: function () {
  //   var mode = this.getFieldValue('OP');
  //   return Blockly.Blocks.matrices_operations.HELPURLS()[mode];
  // },
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('MATRIX')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.INPUT))
      .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    var thisBlock = this;
    // this.setTooltip(function () {
    //   var mode = thisBlock.getFieldValue('OP');
    //   return Blockly.Blocks.matrices_operations.TOOLTIPS()[mode];
    // });
  },
  // typeblock: [{
  //   translatedName: Blockly.Msg.LANG_MATRICES_OPERATIONS_INVERSE,
  //   dropDown: {
  //     titleName: 'OP',
  //     value: 'INVERSE'
  //   }
  // }, {
  //   translatedName: Blockly.Msg.LANG_MATRICES_OPERATIONS_TRANSPOSE,
  //   dropDown: {
  //     titleName: 'OP',
  //     value: 'TRANSPOSE'
  //   }
  // }]
};

Blockly.Blocks.matrices_operations.OPERATORS = function () {
  return [[Blockly.Msg.LANG_MATRICES_OPERATIONS_INVERSE, 'INVERSE'],
    [Blockly.Msg.LANG_MATRICES_OPERATIONS_TRANSPOSE, 'TRANSPOSE']];
};

// Blockly.Blocks.matrices_operations.TOOLTIPS = function () {
//   return {
//     INVERSE: Blockly.Msg.LANG_MATRICES_OPERATIONS_TOOLTIP_INVERSE,
//     TRANSPOSE: Blockly.Msg.LANG_MATRICES_OPERATIONS_TOOLTIP_TRANSPOSE
//   }
// };

// Blockly.Blocks.matrices_operations.HELPURLS = function () {
//   return {
//     INVERSE: Blockly.Msg.LANG_MATRICES_OPERATIONS_HELPURL_INVERSE,
//     TRANSPOSE: Blockly.Msg.LANG_MATRICES_OPERATIONS_HELPURL_TRANSPOSE
//   }
// };

Blockly.Blocks['matrices_inverse'] = {
  category: 'Matrices',
  // helpUrl: function () {
  //   var mode = this.getFieldValue('OP');
  //   return Blockly.Blocks.matrices_operations.HELPURLS()[mode];
  // },
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('MATRIX')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.matrices_operations.OPERATORS), 'OP');
    this.setFieldValue('INVERSE', "OP");
    var thisBlock = this;
    // this.setTooltip(function () {
    //   var mode = thisBlock.getFieldValue('OP');
    //   return Blockly.Blocks.matrices_operations.TOOLTIPS()[mode];
    // });
  }
};

Blockly.Blocks['matrices_transpose'] = {
  category: 'Matrices',
  // helpUrl: function () {
  //   var mode = this.getFieldValue('OP');
  //   return Blockly.Blocks.matrices_operations.HELPURLS()[mode];
  // },
  init: function () {
    this.setColour(Blockly.MATRIX_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('MATRIX')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("matrix", Blockly.Blocks.Utilities.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.matrices_operations.OPERATORS), 'OP');
    this.setFieldValue('TRANSPOSE', "OP");
    var thisBlock = this;
    // this.setTooltip(function () {
    //   var mode = thisBlock.getFieldValue('OP');
    //   return Blockly.Blocks.matrices_operations.TOOLTIPS()[mode];
    // });
  }
};