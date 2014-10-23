// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * FieldTextBlockInput is a subclass of FieldTextInput
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.FieldTextBlockInput');

Blockly.FieldTextBlockInput = function(text, opt_changeHandler) {
 // Call parent's constructor.
  Blockly.FieldTextInput.call(this, text);
  this.changeHandler_ = opt_changeHandler;
};

// FieldTextBlockInput is a subclass of FieldTextInput.
goog.inherits(Blockly.FieldTextBlockInput, Blockly.FieldTextInput);

/**
 * Set the text in this field.
 * @param {string} text New text.
 */
Blockly.FieldTextBlockInput.prototype.setText = function(text) {
  if (this.changeHandler_) {
    var validated = this.changeHandler_(text);
    // If the new text is invalid, validation returns null.
    // In this case we still want to display the illegal result.
    if (validated !== null && validated !== undefined) {
      text = validated;
    }
  }
  if(this.sourceBlock_ && this.sourceBlock_.outputConnection){
    this.sourceBlock_.outputConnection.setCheck(this.sourceBlock_.outputConnection.check_)
  }
  Blockly.Field.prototype.setText.call(this, text);
};