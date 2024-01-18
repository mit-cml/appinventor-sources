// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

'use strict';

goog.provide('AI.Blockly.FieldInvalidDropdown');

/**
 * Dropdown field that allows you to display invalid options. Options that are
 * not available (invalid) are displayed, and the block is marked as a
 * badBlock().
 */
Blockly.FieldInvalidDropdown = class extends Blockly.FieldDropdown {
  /**
   * Create a new FieldInvalidDropdown.
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
  constructor(menuGenerator, opt_invalidOptions, opt_validator) {
    super(menuGenerator, opt_validator);
    this.invalidOptions_ = opt_invalidOptions || [];
  }

  /**
   * Displays the invalid value and marks the block this field belongs to as a
   * badBlock();
   * @param {string} invalidValue The invalid value / unavailable option.
   */
  doValueInvalid_(invalidValue) {
    this.value_ = invalidValue;
    this.isDirty_ = true;
    this.sourceBlock_ && this.sourceBlock_.badBlock();

    this.selectedOption_ = [invalidValue, invalidValue];
    for (let i = 0, option; (option = this.invalidOptions_[i]); i++) {
      // Options are tuples of human-readable text and language-neutral values.
      if (option[1] == invalidValue) {
        this.selectedOption_ = option;
        break;
      }
    }
  }

  /**
   * Updates the value of this dropdown and removes badBlock() from the source
   * block if it exists.
   * @param {*} newValue  The value to be saved.
   */
  doValueUpdate_(newValue) {
    super.doValueUpdate_(newValue);

    // If we get here the value is valid. Make sure the block is not marked as bad.
    this.sourceBlock_ && this.sourceBlock_.notBadBlock();
  }
}

