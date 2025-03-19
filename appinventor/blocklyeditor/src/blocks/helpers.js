// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @license
 * @fileoverview Helper block definitions. These are created based on the
 *     component database, and are meant to be connected to component
 *     method/setter inputs to make coding faster for users.
 */

'use strict';

goog.provide('AI.Blocks.helpers');

goog.require('AI.Blockly.FieldInvalidDropdown');
goog.require('AI.BlockUtils');

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

    var db = this.getTopWorkspace().getComponentDatabase();
    var optionList = db.getOptionList(this.key_);
    var tag = db.getInternationalizedOptionListTag(optionList.tag);
    var dropdown = new Blockly.FieldInvalidDropdown(
        this.getValidOptions(), this.getInvalidOptions());

    // Setting the output check to be the OptionList type only allows this block
    // to connect to inputs which expect its specific type of enum. Currently
    // this is only used for Blockly connection checks, as no output types are
    // encoded in Yail.
    this.setOutput(true, this.getOutputType());
    this.appendDummyInput()
        .appendField(tag)
        .appendField(dropdown, 'OPTION');

    var value = xml.getAttribute('value') || optionList.defaultOpt;
    this.setFieldValue(value, 'OPTION');
  },

  getOutputType: function() {
    var check = [];
    var blocklyType = AI.BlockUtils.YailTypeToBlocklyType(
        'enum', AI.BlockUtils.OUTPUT);
    if (blocklyType) {
      if (Array.isArray(blocklyType)) {
        // Clone array.
        check = blocklyType.slice();
      } else {
        check.push(blocklyType);
      }
    }

    var helperType = AI.BlockUtils.helperKeyToBlocklyType(
      { type: 'OPTION_LIST', key: this.key_ }, this);
    if (helperType && helperType != blocklyType) {
      check.push(helperType);
    }

    return !check.length ? null : check;
  },

  /**
   * Returns a list of tuples defining the valid dropdown values. The first
   * element in the tuple is the human readable name. The second element is the
   * language neutral value.
   */
  getValidOptions: function() {
    var db = this.getTopWorkspace().getComponentDatabase();
    var optionList = db.getOptionList(this.key_);
    var options = [];
    for (var i = 0, option; option = optionList.options[i]; i++) {
      var key = optionList.tag + option.name;
      var i18nName = db.getInternationalizedOptionName(key, option.name);
      if (!option.deprecated) {
        options.push([i18nName, option.name]);
      }
    }
    if (this.shouldSortOptions()) {
      options.sort(function(a, b) {
        return a[1] < b[1] ? -1 : a[1] > b[1] ? 1 : 0;
      });
    }
    return options;
  },

  /**
   * Returns a list of tuples defining the /invalid/ dropdown values. The first
   * element in the tuple is the human readable name. The second element is the
   * language neutral value.
   */
  getInvalidOptions: function() {
    var db = this.getTopWorkspace().getComponentDatabase();
    var optionList = db.getOptionList(this.key_);
    var options = [];
    for (var i = 0, option; option = optionList.options[i]; i++) {
      var key = optionList.tag + option.name;
      var i18nName = db.getInternationalizedOptionName(key, option.name);
      if (option.deprecated) {
        options.push([i18nName, option.name]);
      }
    }
    return options;
  },

  shouldSortOptions: function() {
    return this.key_ === 'Permission';
  },

  typeblock: function() {
    var db = Blockly.common.getMainWorkspace().getComponentDatabase();
    var tb = [];

    db.forEachOptionList(function(optionList) {
      for (var i = 0, option; option = optionList.options[i]; i++) {
        var tag = db.getInternationalizedOptionListTag(optionList.tag);
        var key = optionList.tag + option.name;
        var i18nName = db.getInternationalizedOptionName(key, option.name);
        tb.push({
          // TODO: This doesn't handle rtl langs, anyway to fix that?
          translatedName: tag + i18nName,
          mutatorAttributes: {
            key: optionList.tag,
            value: option.name
          }
        });
      }
    });

    return tb;
  }
}

Blockly.Blocks['helpers_screen_names'] = {
  init: function() {
    var utils = AI.BlockUtils;
    var dropdown = new Blockly.FieldInvalidDropdown(
        this.generateOptions.bind(this));

    this.setColour(Blockly.COLOUR_HELPERS);

    this.setOutput(true, utils.YailTypeToBlocklyType('text', utils.OUTPUT));
    this.appendDummyInput()
        .appendField(dropdown, 'SCREEN');
  },

  onchange: function(e) {
    if (e.type == AI.Events.SCREEN_SWITCH) {
      this.setFieldValue(this.getFieldValue('SCREEN'), 'SCREEN');
    }
  },

  domToMutation: function(xml) {
    var value = xml.getAttribute('value');
    this.setFieldValue(value, 'SCREEN');
  },

  getScreens: function() {
    return this.workspace.getScreenList();
  },

  generateOptions: function() {
    if (!this.workspace) {
      return [['', '']]
    }
    var screens = this.getScreens();
    if (!screens.length) {
      return [['', '']]
    }
    return screens.map(function (elem) {
      return [elem, elem];
    });
  },

  typeblock: function() {
    var tb = [];

    var screens = Blockly.common.getMainWorkspace().getScreenList();
    for (var i = 0, screen; (screen = screens[i]); i++) {
      tb.push({
        translatedName: Blockly.Msg.LANG_SCREENS_TITLE + screen,
        mutatorAttributes: {
          value: screen
        }
      })
    }

    return tb;
  }
}

Blockly.Blocks['helpers_assets'] = {
  init: function() {
    var utils = AI.BlockUtils;

    this.setColour(Blockly.COLOUR_HELPERS);

    this.setOutput(true, utils.YailTypeToBlocklyType('text', utils.OUTPUT));
    this.appendDummyInput('INPUT')

    this.addField();
  },

  onchange: function(e) {
    if (e.type != AI.Events.SCREEN_SWITCH &&
        !(e.type == Blockly.Events.MOVE)) {
      return;
    }

    if (this.addField()) {
      var value = this.getFieldValue('ASSET');
      var options = this.generateOptions();
      if (this.isInFlyout && options.length) {
        value = options[0][1];
      }
      this.setFieldValue(value, 'ASSET');
    }
  },

  domToMutation: function(xml) {
    var field = this.getField('ASSET');
    if (!field) {
      return;
    }
    var value = xml.getAttribute('value');
    field.setValue(value);
  },

  addField: function() {
    if (!this.workspace) {  // Disposed.
      return;
    }
    var input = this.getInput('INPUT');
    var assets = this.workspace.getAssetList();

    if (assets.length) { // We should have an asset dropdown.
      if (!this.getField('ASSET')) {
        var dropdown = new Blockly.FieldInvalidDropdown(
            this.generateOptions.bind(this));
        input.appendField(dropdown, 'ASSET');
      }
      if (this.getField('TEXT')) {
        input.removeField('TEXT');
      }
    } else {
      if (!this.getField('TEXT')) {
        var label = new Blockly.FieldLabel(Blockly.Msg.LANG_NO_ASSETS);
        input.appendField(label, 'TEXT');
      }
      if (this.getField('ASSET')) {
        input.removeField('ASSET');
      }
    }

    return assets.length;
  },

  generateOptions: function() {
    if (!this.workspace) {
      return [['', '']];
    }

    // Must include the '' so .some returns true if no restrictions.
    var restrictedFormats = [''];
    var types = this.outputConnection.targetConnection &&
        this.outputConnection.targetConnection.getCheck();
    if (types) {
      for (var i = 0, type; type = types[i]; i++) {
        if (Array.isArray(type)) {
          // Not actually a type check. An array in the type check array is used
          // to restrict formats.
          restrictedFormats = type;
        }
      }
    }

    var assets = this.workspace.getAssetList();
    if (assets.length) {
      var values = assets.map(function (elem) {
        var assetValid = restrictedFormats.some(function(fileType) {
          return elem.includes(fileType);
        })
        if (assetValid) {
          return [elem, elem];
        }
        return undefined;  // Not necessary just more explicit.
      });
      values = values.filter(function(elem) {
        return elem !== undefined;
      })
      if (values.length) {
        return values;
      }
    }

    return [['', '']]
  },

  typeblock: function() {
    var tb = [];
    var assets = Blockly.common.getMainWorkspace().getAssetList();
    for (var i = 0, asset; (asset = assets[i]); i++) {
      tb.push({
        translatedName: asset,
        mutatorAttributes: {
          value: asset
        }
      })
    }
    return tb;
  }
}

Blockly.Blocks['helpers_providermodel'] = {
  init: function() {
    var utils = AI.BlockUtils;

    this.setColour(Blockly.COLOUR_HELPERS);

    this.setOutput(true, utils.YailTypeToBlocklyType('text', utils.OUTPUT));
    this.appendDummyInput('INPUT')

    this.addField();
  },

  onchange: function(e) {
    if (e.type != AI.Events.SCREEN_SWITCH &&
        !(e.type == Blockly.Events.MOVE)) {
      return;
    }

    if (this.addField()) {
      var value = this.getFieldValue('PROVIDERMODEL');
      var options = this.generateOptions();
      if (this.isInFlyout && options.length) {
        value = options[0][1];
      }
      this.setFieldValue(value, 'PROVIDERMODEL');
    }
  },

  domToMutation: function(xml) {
    var field = this.getField('PROVIDERMODEL');
    if (!field) {
      return;
    }
    var value = xml.getAttribute('value');
    field.setValue(value);
  },

  addField: function() {
    if (!this.workspace) {  // Disposed.
      return;
    }
    var input = this.getInput('INPUT');
    var modelproviders = this.workspace.getProviderModelList();

    if (modelproviders.length) {
      if (!this.getField('PROVIDERMODEL')) {
        var dropdown = new Blockly.FieldInvalidDropdown(
            this.generateOptions.bind(this));
        input.appendField(dropdown, 'PROVIDERMODEL');
      }
      if (this.getField('TEXT')) {
        input.removeField('TEXT');
      }
    } else {
      if (!this.getField('TEXT')) {
        var label = new Blockly.FieldLabel(Blockly.Msg.LANG_NO_PROVIDERMODEL);
        input.appendField(label, 'TEXT');
      }
      if (this.getField('PROVIDERMODEL')) {
        input.removeField('PROVIDERMODEL');
      }
    }

    return modelproviders.length;
  },

  generateOptions: function() {
    if (!this.workspace) {
      return [['', '']];
    }

    // Must include the '' so .some returns true if no restrictions.
    var restrictedFormats = [''];
    var types = this.outputConnection.targetConnection &&
        this.outputConnection.targetConnection.check_;
    if (types) {
      for (var i = 0, type; type = types[i]; i++) {
        if (Array.isArray(type)) {
          // Not actually a type check. An array in the type check array is used
          // to restrict formats.
          restrictedFormats = type;
        }
      }
    }

    var modelproviders = this.workspace.getProviderModelList();
    if (modelproviders.length) {
      var values = modelproviders.map(function (elem) {
        var modelValid = restrictedFormats.some(function(fileType) {
          return elem.includes(fileType);
        })
        if (modelValid) {
          return [elem, elem];
        }
        return undefined;  // Not necessary just more explicit.
      });
      values = values.filter(function(elem) {
        return elem !== undefined;
      })
      if (values.length) {
        return values;
      }
    }

    return [['', '']]
  },

  typeblock: function() {
    var tb = [];
    var modelproviders = Blockly.common.getMainWorkspace().getProviderModelList();
    for (var i = 0, providermodel; (providermodel = modelproviders[i]); i++) {
      tb.push({
        translatedName: providermodel,
        mutatorAttributes: {
          value: providermodel
        }
      })
    }
    return tb;
  }
}

Blockly.Blocks['helpers_provider'] = {
  init: function() {
    var utils = AI.BlockUtils;

    this.setColour(Blockly.COLOUR_HELPERS);

    this.setOutput(true, utils.YailTypeToBlocklyType('text', utils.OUTPUT));
    this.appendDummyInput('INPUT')

    this.addField();
  },

  onchange: function(e) {
    if (e.type != AI.Events.SCREEN_SWITCH &&
        !(e.type == Blockly.Events.MOVE)) {
      return;
    }

    if (this.addField()) {
      var value = this.getFieldValue('PROVIDER');
      var options = this.generateOptions();
      if (this.isInFlyout && options.length) {
        value = options[0][1];
      }
      this.setFieldValue(value, 'PROVIDER');
    }
  },

  domToMutation: function(xml) {
    var field = this.getField('PROVIDER');
    if (!field) {
      return;
    }
    var value = xml.getAttribute('value');
    field.setValue(value);
  },

  addField: function() {
    if (!this.workspace) {  // Disposed.
      return;
    }
    var input = this.getInput('INPUT');
    var providers = this.workspace.getProviderList();

    if (providers.length) {
      if (!this.getField('PROVIDER')) {
        var dropdown = new Blockly.FieldInvalidDropdown(
          this.generateOptions.bind(this));
        input.appendField(dropdown, 'PROVIDER');
      }
      if (this.getField('TEXT')) {
        input.removeField('TEXT');
      }
    } else {
      if (!this.getField('TEXT')) {
        var label = new Blockly.FieldLabel(Blockly.Msg.LANG_NO_PROVIDERMODEL);
        input.appendField(label, 'TEXT');
      }
      if (this.getField('PROVIDER')) {
        input.removeField('PROVIDER');
      }
    }

    return providers.length;
  },

  generateOptions: function() {
    if (!this.workspace) {
      return [['', '']];
    }

    // Must include the '' so .some returns true if no restrictions.
    var restrictedFormats = [''];
    var types = this.outputConnection.targetConnection &&
        this.outputConnection.targetConnection.check_;
    if (types) {
      for (var i = 0, type; type = types[i]; i++) {
        if (Array.isArray(type)) {
          // Not actually a type check. An array in the type check array is used
          // to restrict formats.
          restrictedFormats = type;
        }
      }
    }

    var providers = this.workspace.getProviderList();
    if (providers.length) {
        var values = providers.map(function (elem) {
          var providerValid = restrictedFormats.some(function(fileType) {
            return elem.includes(fileType);
        })
        if (providerValid) {
          return [elem, elem];
        }
        return undefined;  // Not necessary just more explicit.
      });
      values = values.filter(function(elem) {
        return elem !== undefined;
      })
      if (values.length) {
        return values;
      }
    }

    return [['', '']]
  },

  typeblock: function() {
    var tb = [];
    var providers = Blockly.common.getMainWorkspace().getProviderList();
    for (var i = 0, provider; (provider = providers[i]); i++) {
      tb.push({
        translatedName: provider,
        mutatorAttributes: {
          value: provider
        }
      })
    }
    return tb;
  }
}
