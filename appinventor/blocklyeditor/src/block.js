// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Add additional "class methods" to Blockly.Block
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.Block');

goog.require('Blockly.Block');

Blockly.Block.mutationToDom = function() {
  var container = details.mutationToDomFunc ? details.mutationToDomFunc()
    : document.createElement('mutation');
  container.setAttribute('is_statement', this['isStatement'] || false);
  return container;
};

/**
 * Interpolate a message string, creating fields and inputs.
 * @param {string} msg The message string to parse.  %1, %2, etc. are symbols
 *     for value inputs or for Fields, such as an instance of
 *     Blockly.FieldDropdown, which would be placed as a field in either the
 *     following value input or a dummy input.  The newline character forces
 *     the creation of an unnamed dummy input if any fields need placement.
 *     Note that '%10' would be interpreted as a reference to the tenth
 *     argument.  To show the first argument followed by a zero, use '%1 0'.
 *     (Spaces around tokens are stripped.)  To display a percentage sign
 *     followed by a number (e.g., "%123"), put that text in a
 *     Blockly.FieldLabel (as described below).
 * @param {!Array.<?string|number|Array.<string>|Blockly.Field>|number} var_args
 *     A series of tuples that each specify the value inputs to create.  Each
 *     tuple has at least two elements.  The first is its name; the second is
 *     its type, which can be any of:
 *     - A string (such as 'Number'), denoting the one type allowed in the
 *       corresponding socket.
 *     - An array of strings (such as ['Number', 'List']), denoting the
 *       different types allowed in the corresponding socket.
 *     - null, denoting that any type is allowed in the corresponding socket.
 *     - Blockly.Field, in which case that field instance, such as an
 *       instance of Blockly.FieldDropdown, appears (instead of a socket).
 *     If the type is any of the first three options (which are legal arguments
 *     to setCheck()), there should be a third element in the tuple, giving its
 *     alignment.
 *     The final parameter is not a tuple, but just an alignment for any
 *     trailing dummy inputs.  This last parameter is mandatory; there may be
 *     any number of tuples (though the number of tuples must match the symbols
 *     in msg).
 */
Blockly.Block.prototype.interpolateMsg = function(msg, var_args) {
  /**
   * Add a field to this input.
   * @this !Blockly.Input
   * @param {Blockly.Field|Array.<string|Blockly.Field>} field
   *     This is either a Field or a tuple of a name and a Field.
   */
  function addFieldToInput(field) {
    if (field instanceof Blockly.Field) {
      this.appendField(field);
    } else {
      goog.asserts.assert(goog.isArray(field));
      this.appendField(field[1], field[0]);
    }
  }

  // Validate the msg at the start and the dummy alignment at the end,
  // and remove the latter.
  goog.asserts.assertString(msg);
  var dummyAlign = arguments[arguments.length - 1];
  goog.asserts.assert(
      dummyAlign === Blockly.ALIGN_LEFT ||
      dummyAlign === Blockly.ALIGN_CENTRE ||
      dummyAlign === Blockly.ALIGN_RIGHT,
      'Illegal final argument "%d" is not an alignment.', dummyAlign);
  arguments.length = arguments.length - 1;

  var tokens = msg.split(this.interpolateMsg.SPLIT_REGEX_);
  var fields = [];
  for (var i = 0; i < tokens.length; i += 2) {
    var text = goog.string.trim(tokens[i]);
    var input = undefined;
    if (text) {
      fields.push(new Blockly.FieldLabel(text));
    }
    var symbol = tokens[i + 1];
    if (symbol && symbol.charAt(0) == '%') {
      // Numeric field.
      var number = parseInt(symbol.substring(1), 10);
      var tuple = arguments[number];
      goog.asserts.assertArray(tuple,
          'Message symbol "%s" is out of range.', symbol);
      goog.asserts.assertArray(tuple,
          'Argument "%s" is not a tuple.', symbol);
      if (tuple[1] instanceof Blockly.Field) {
        fields.push([tuple[0], tuple[1]]);
      } else {
        input = this.appendValueInput(tuple[0])
            .setCheck(tuple[1])
            .setAlign(tuple[2]);
      }
      arguments[number] = null;  // Inputs may not be reused.
    } else if (symbol == '\n' && fields.length) {
      // Create a dummy input.
      input = this.appendDummyInput();
    }
    // If we just added an input, hang any pending fields on it.
    if (input && fields.length) {
      fields.forEach(addFieldToInput, input);
      fields = [];
    }
  }
  // If any fields remain, create a trailing dummy input.
  if (fields.length) {
    var input = this.appendDummyInput()
        .setAlign(dummyAlign);
    fields.forEach(addFieldToInput, input);
  }

  // Verify that all inputs were used.
  for (var i = 1; i < arguments.length - 1; i++) {
    goog.asserts.assert(arguments[i] === null,
        'Input "%%s" not used in message: "%s"', i, msg);
  }
  // Make the inputs inline unless there is only one input and
  // no text follows it.
  this.setInputsInline(!msg.match(this.interpolateMsg.INLINE_REGEX_));
};

/**
 Unplug this block from every block connected to it.
 */
Blockly.Block.prototype.isolate = function(healStack) {
  this.unplug(healStack);
  for (var x = this.childBlocks_.length - 1; x >= 0; x--) {
    this.childBlocks_[x].unplug(healStack);
  }
};

Blockly.Block.prototype.interpolateMsg.SPLIT_REGEX_ = /(%\d+|\n)/;
Blockly.Block.prototype.interpolateMsg.INLINE_REGEX_ = /%1\s*$/;

/**
 * Walk the tree of blocks starting with this block.
 *
 * @param {function(Blockly.Block, number)} callback  the callback function to evaluate for each
 * block. The function receives the block and the logical depth of the block in the tree.
 */
Blockly.Block.prototype.walk = function(callback) {
  function doWalk(block, depth) {
    callback(block, depth);
    block.inputList.forEach(function(input) {
      if ((input.type === Blockly.INPUT_VALUE || input.type === Blockly.NEXT_STATEMENT) &&
          input.connection && input.connection.targetBlock()) {
        doWalk(input.connection.targetBlock(), depth + 1);
      }
      if (block.nextConnection && block.nextConnection.targetBlock()) {
        doWalk(block.nextConnection.targetBlock(), depth);
      }
    })
  }
  doWalk(this, 0);
};

/**
 * @type {?function(this: Blockly.BlockSvg, !Element)}
 */
Blockly.Block.prototype.domToMutation = null;

/**
 * @type {?function(this: Blockly.BlockSvg):!Element}
 */
Blockly.Block.prototype.mutationToDom = null;
