// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology. All rights reserved.

// Copyright 2012 Google LLC
// SPDX-License-Identifier: Apache-2.0

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Add additional "class methods" to Blockly.Input
 */

/**
 * Add an item to the end of the input's field row.
 *
 * This is overridden so that we can properly set the visibility of the field
 * when it is appended.
 * @param {string|!Blockly.Field} field Something to add as a field.
 * @param {string=} opt_name Language-neutral identifier which may used to find
 *     this field again.  Should be unique to the host block.
 * @return {!Blockly.Input} The input being append to (to allow chaining).
 */
Blockly.Input.prototype.appendField = function(field, opt_name) {
  // Empty string, Null or undefined generates no field, unless field is named.
  if (!field && !opt_name) {
    return this;
  }
  // Generate a FieldLabel when given a plain text field.
  if (goog.isString(field)) {
    field = new Blockly.FieldLabel(/** @type {string} */ (field));
  }
  field.setSourceBlock(this.sourceBlock_);
  if (this.sourceBlock_.rendered) {
    field.init();
  }
  field.name = opt_name;
  field.setVisible(this.isVisible());

  if (field.prefixField) {
    // Add any prefix.
    this.appendField(field.prefixField);
  }
  // Add the field to the field row.
  this.fieldRow.push(field);
  if (field.suffixField) {
    // Add any suffix.
    this.appendField(field.suffixField);
  }

  //If it's a COLLAPSE_TEXT input, hide it by default
  if (opt_name === 'COLLAPSED_TEXT')
    this.sourceBlock_.getTitle_(opt_name).getRootElement().style.display = 'none';

  if (this.sourceBlock_.rendered) {
    this.sourceBlock_.render();
    // Adding a field will cause the block to change shape.
    this.sourceBlock_.bumpNeighbours_();
  }
  return this;
};
