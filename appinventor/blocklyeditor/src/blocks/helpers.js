// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


/**
 * @license
 * @fileoverview Helper block definitions. These are created based on the
 *     component database, and are meant to be connected to component
 *     method/setter inputs to make coding faster for users.
 */

'use strict';

goog.provide('Blockly.Blocks.helpers');

goog.require('Blockly.Blocks.Utilities');

Blockly.COLOUR_HELPERS = "#BF4343";

Blockly.Blocks['helpers_dropdown'] = {
  init: function() {
    /**
     * The key to the OptionList associated with this block.
     * @private
     */
    this.key_ = "";
    this.setColour(Blockly.COLOUR_HELPERS);
    // Everything else gets handled by domToMutaiton.
  },
 
  mutationToDom: function() {
    var mutation = document.createElement('mutation');
    mutation.setAttribute('key', this.key_);
    return mutation;
  },

  domToMutation: function(xml) {
    this.key_ = xml.getAttribute('key');
    var type = Blockly.Blocks.Utilities.helperKeyToBlocklyType(
      { type: 'OPTION_LIST', key: this.key_ }, this);

    var optionList = this.getTopWorkspace().getComponentDatabase()
        .getOptionList(this.key_);

    var dropdown = new Blockly.FieldInvalidDropdown(
        this.getValidOptions(), this.getInvalidOptions());

    // Setting the output check to be the OptionList type only allows this block
    // to connect to inputs which expect its specific type of enum. Currently
    // this is only used for Blockly connection checks, as no output types are
    // encoded in Yail.
    this.setOutput(true, type);
    this.appendDummyInput()
        .appendField(optionList.tag)
        .appendField(dropdown, 'OPTION');
    
    this.setFieldValue(optionList.defaultOpt, 'OPTION');
  },

  /**
   * Returns a list of tuples defining the valid dropdown values. The first
   * element in the tuple is the human readable name. The second element is the
   * language neutral value.
   */
  getValidOptions: function() {
    var optionList = this.getTopWorkspace().getComponentDatabase()
        .getOptionList(this.key_);
    var options = [];
    for (var i = 0, option; option = optionList.options[i]; i++) {
      // TODO: First will eventually be the translated name.
      if (!option.deprecated) {
        options.push([option.name, option.name]);
      }
    }
    return options;
  },

  /**
   * Returns a list of tuples defining the /invalid/ dropdown values. The first
   * element in the tuple is the human readable name. The second element is the
   * language neutral value.
   */
  getInvalidOptions: function() {
    var optionList = this.getTopWorkspace().getComponentDatabase()
        .getOptionList(this.key_);
    var options = [];
    for (var i = 0, option; option = optionList.options[i]; i++) {
      // TODO: First will eventually be the translated name.
      if (option.deprecated) {
        options.push([option.name, option.name]);
      }
    }
    return options;
  }
}
