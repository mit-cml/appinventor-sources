// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

'use strict';

goog.provide('AI.Blockly.FieldInvalidDropdown');

goog.require('Blockly.FieldDropdown');

/**
 * Dropdown field that allows you to display invalid options. Options that are
 * not available (invalid) are displayed, and the block is marked as a
 * badBlock().
 * @param {(!Array.<!Array>|!Function)} menuGenerator A non-empty array of
 *     options for a dropdown list, or a function which generates these options.
 * @param {!Array.<!Array>} opt_invalidOptions A list of invalid options with
 *     identically structure to the menuGenerator options list. These options
 *     will not be available in the dropdown, but the correct human-readable
 *     values will be displayed if they are set some other way (eg via xml).
 *     If an invalid value is set and it does not exist in the set of
 *     invalidOptions then the stringified version of that value will be
 *     displayed.
 * @param {Function=} opt_validator A change listener that is called when a new
 *     option is selected from the dropdown.
 */
Blockly.FieldInvalidDropdown = function(
    menuGenerator, opt_invalidOptions, opt_validator
) {
  Blockly.FieldInvalidDropdown.superClass_.constructor.call(
      this, menuGenerator, opt_validator);
  this.invalidOptions_ = opt_invalidOptions || [];
}
goog.inherits(Blockly.FieldInvalidDropdown, Blockly.FieldDropdown);

/**
 * Displays the invalid value and marks the block this field belongs to as a
 * badBlock();
 * @param {string} invalidValue The invalid value / unavailable option.
 */
Blockly.FieldInvalidDropdown.prototype.doValueInvalid_ = function(invalidValue) {
  this.value_ = invalidValue;
  this.isDirty_ = true;
  this.sourceBlock_ && this.sourceBlock_.badBlock();

  this.selectedOption_ = [invalidValue, invalidValue];
  for (var i = 0, option; (option = this.invalidOptions_[i]); i++) {
    // Options are tuples of human-readable text and language-neutral values.
    if (option[1] == invalidValue) {
      this.selectedOption_ = option;
      break;
    }
  }

  // TODO: Remove the rest of this function after Blockly update.
  this.text_ = this.selectedOption_[0];
  this.size_.width = 0;
  if (this.sourceBlock_ && this.sourceBlock_.rendered) {
    this.sourceBlock_.render();
    this.sourceBlock_.bumpNeighbours_();
  }
}

/**
 * Updates the value of this dropdown and removes badBlock() from the source
 * block if it exists.
 * @param {*} newValue  The value to be saved.
 */
Blockly.FieldInvalidDropdown.prototype.doValueUpdate_ = function(newValue) {
  // TODO: Uncomment this after Blockly update.
  //Blockly.FieldInvalidDropdown.superClass_.doValueUpdate_.call(this, newValue);
  
  // If we get here the value is valid. Make sure the block is not marked as bad.
  this.sourceBlock_ && this.sourceBlock_.notBadBlock();

  // TODO: Remove the rest of this function after Blockly update.
  this.value_ = newValue;
  var options = this.getOptions_();
  for (var i = 0, option; (option = options[i]); i++) {
    // Options are tuples of human-readable text and language-neutral values.
    if (option[1] == newValue) {
      this.selectedOption_ = option;
      break;
    }
  }
  this.text_ = this.selectedOption_[0];
  this.size_.width = 0;
  if (this.sourceBlock_ && this.sourceBlock_.rendered) {
    this.sourceBlock_.render();
    this.sourceBlock_.bumpNeighbours_();
  }
}

// TODO: Remove the rest of this file after the Blockly update.
Blockly.FieldInvalidDropdown.prototype.setValue = function(newValue) {
  var oldValue = this.getValue();
  if (newValue === null) {
    // Not a valid value to check.
    return;
  }

  var validatedValue = this.doClassValidation_(newValue);
  newValue = this.processValidation_(newValue, validatedValue);
  if (newValue instanceof Error) {
    return;
  }

  var localValidator = this.getValidator();
  if (localValidator) {
    validatedValue = localValidator.call(this, newValue);
    newValue = this.processValidation_(newValue, validatedValue);
    if (newValue instanceof Error) {
      return;
    }
  }

  var source = this.sourceBlock_;
  if (source && Blockly.Events.isEnabled()) {
    Blockly.Events.fire(new Blockly.Events.Change(
        source, 'field', this.name || null, oldValue, newValue));
  }
  this.doValueUpdate_(newValue);
}

Blockly.FieldInvalidDropdown.prototype.processValidation_ =
  function(newValue, validatedValue) {
    if (validatedValue === null) {
      this.doValueInvalid_(newValue);
      return Error();
    }
    return newValue;
  };

Blockly.FieldInvalidDropdown.prototype.doClassValidation_ = function(newValue) {
  var isValueValid = false;
  var options = this.getOptions_();
  for (var i = 0, option; (option = options[i]); i++) {
    // Options are tuples of human-readable text and language-neutral values.
    if (option[1] == newValue) {
      isValueValid = true;
      break;
    }
  }
  if (!isValueValid) {
    return null;
  }
  return /** @type {string} */ (newValue);
}
