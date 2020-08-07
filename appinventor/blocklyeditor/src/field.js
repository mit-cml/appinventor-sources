// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Add additional "class methods" to Blockly.Field
 */

'use strict';

goog.provide('AI.Blockly.Field');

goog.require('Blockly.Field');


/**
 * Sets the value of the field. Since AI runs an older version of blockly,
 * the newValue should always be a string.
 *
 * This is overridden so that the field correctly updates the display text
 * even if the newValue is the same as the old value.
 * @param {string} newValue The new value.
 */
Blockly.Field.prototype.setValue = function(newValue) {
  if (newValue === null) {
    return null;  // No change if null;
  }
  var oldValue = this.getValue();  // Must get value before setting text.
  this.setText(newValue);  // Always update text. See #1238.
  if (this.sourceBlock_ && Blockly.Events.isEnabled() && newValue != oldValue) {
    Blockly.Events.fire(new Blockly.Events.Change(
        this.sourceBlock_, 'field', this.name, oldValue, newValue));
  }
};
