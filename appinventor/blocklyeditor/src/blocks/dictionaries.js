// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Dictionaries blocks for Blockly, modified for MIT App Inventor.
 * @author data1013@mit.edu (Danny Tang)
 */

'use strict';

goog.provide('AI.Blocks.dictionaries');

goog.require('AI.BlockUtils');

Blockly.Blocks['dictionaries_create_with'] = {
  // Create a dictionary with any number of pairs of any type.
  category: 'Dictionaries',
  helpUrl: function() {
    if (this.itemCount_ > 0) {
      return Blockly.Msg.LANG_DICTIONARIES_CREATE_WITH_EMPTY_HELPURL;
    } else {
      return Blockly.Msg.LANG_DICTIONARIES_MAKE_DICTIONARY_HELPURL;
    }
  },
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.appendValueInput('ADD0')
        .appendField(Blockly.Msg.LANG_DICTIONARIES_MAKE_DICTIONARY_TITLE)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("pair",AI.BlockUtils.INPUT));
    this.appendValueInput('ADD1')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("pair",AI.BlockUtils.INPUT));
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.OUTPUT));
    this.setMutator(new Blockly.icons.MutatorIcon(['dictionaries_mutator_pair'], this));
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_MAKE_DICTIONARY_TOOLTIP);
    this.itemCount_ = 2;
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'ADD';
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'dictionaries_mutator_pair',this);
  },
  compose: function(containerBlock) {
    var oldValues = {};
    for (var i = 0; i < this.itemCount_; i++) {
      var name = this.repeatingInputName + i,
        block = this.getInputTargetBlock(name);
      if (block) {
        oldValues[block.id] = block;
      }
    }

    // Disconnect all input blocks and destroy all inputs.
    if (this.itemCount_ === 0) {
      if(this.emptyInputName != null) {
        this.removeInput(this.emptyInputName);
      }
    } else {
      for (var x = this.itemCount_ - 1; x >= 0; x--) {
        this.removeInput(this.repeatingInputName + x);
      }
    }
    this.itemCount_ = 0;
    // Rebuild the block's inputs.
    var itemBlock = containerBlock.getInputTargetBlock('STACK');
    while (itemBlock) {
      var input = this.addInput(this.itemCount_);

      // Reconnect any child blocks.
      if (itemBlock.valueConnection_ && itemBlock.valueConnection_.getSourceBlock() &&
          // empty key-value pairs get deleted and so won't have a workspace
          itemBlock.valueConnection_.getSourceBlock().workspace != null) {
        input.connection.connect(itemBlock.valueConnection_);
        // Remove this block from the set of old blocks
        delete oldValues[itemBlock.valueConnection_.sourceBlock_.id];
      } else if (Blockly.Events.isEnabled()) {  // false if we are loading a project
        // auto-fill the empty socket with a pair block
        var pairBlock = Blockly.common.getMainWorkspace().newBlock('pair');
        pairBlock.initSvg();
        pairBlock.queueRender();
        input.connection.connect(pairBlock.outputConnection);
      }

      this.itemCount_++;
      itemBlock = itemBlock.nextConnection &&
        itemBlock.nextConnection.targetBlock();
    }
    if (this.itemCount_ === 0) {
      this.addEmptyInput();
    }
    // Clean up any disconnected old pairs with empty key and value sockets
    for (var key in oldValues) {
      if (oldValues.hasOwnProperty(key)) {
        var oldBlock = oldValues[key];
        if (!oldBlock.getInputTargetBlock('KEY') && !oldBlock.getInputTargetBlock('VALUE')) {
          oldBlock.dispose(false, false);
        }
      }
    }
  },
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){
    this.appendDummyInput(this.emptyInputName)
      .appendField(Blockly.Msg.LANG_DICTIONARIES_CREATE_EMPTY_TITLE);
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("pair",AI.BlockUtils.INPUT));
    if(inputNum === 0){
      input.appendField(Blockly.Msg.LANG_DICTIONARIES_MAKE_DICTIONARY_TITLE);
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.setFieldValue(Blockly.Msg.LANG_DICTIONARIES_CREATE_WITH_CONTAINER_TITLE_ADD,"CONTAINER_TEXT");
    containerBlock.setTooltip(Blockly.Msg.LANG_DICTIONARIES_CREATE_WITH_CONTAINER_TOOLTIP);
  },
  /**
   * Create a human-readable text representation of this block and any children.
   * @param {number=} opt_maxLength Truncate the string to this length.
   * @param {string=} opt_emptyToken The placeholder string used to denote an
   *     empty field. If not specified, '?' is used.
   * @return {string} Text of block.
   */
  toString: function(opt_maxLength, opt_emptyToken) {
    var buffer = '{';
    var checkLen = true;
    opt_emptyToken = opt_emptyToken || '?';
    if (!opt_maxLength || opt_maxLength === 0) {
      checkLen = false;
    }
    var sep = '';
    for (var i = 0, input; (input = this.getInput('ADD' + i)) && (!checkLen || buffer.length < opt_maxLength); i++) {
      var target = input.connection.targetBlock();
      if (target) {
        var keyblock = target.getInput('KEY').connection.targetBlock();
        var valueblock = target.getInput('VALUE').connection.targetBlock();
        if (keyblock || valueblock) {
          buffer += sep;
          buffer += keyblock ? keyblock.toString(opt_maxLength, opt_emptyToken) : opt_emptyToken;
          buffer += ':';
          buffer += valueblock ? valueblock.toString(opt_maxLength, opt_emptyToken) : opt_emptyToken;
          sep = ',';
        }
      }
    }
    if (checkLen && buffer.length >= opt_maxLength) {
      buffer = buffer.substring(0, opt_maxLength - 2);
      buffer += '…'
    }
    buffer += '}';
    return buffer;
  },
  // create type blocks for both make a dictionary (two pairs) and create empty dictionary
  typeblock: [
      { translatedName: Blockly.Msg.LANG_DICTIONARIES_MAKE_DICTIONARY_TITLE,
        mutatorAttributes: { items: 2 } },
      { translatedName: Blockly.Msg.LANG_DICTIONARIES_CREATE_EMPTY_TITLE,
        mutatorAttributes: { items: 0 } }]
};

Blockly.Blocks['dictionaries_mutator_pair'] = {
  // Add pairs.
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_DICTIONARIES_PAIR_TITLE);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_PAIR_TOOLTIP);
  },
  contextMenu: false
};

Blockly.Blocks['pair'] = {
  category: 'Dictionaries',
  helpUrl: Blockly.Msg.LANG_DICTIONARIES_PAIR_HELPURL,
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("pair",AI.BlockUtils.OUTPUT));
    var checkTypeAny = AI.BlockUtils.YailTypeToBlocklyType("any",AI.BlockUtils.INPUT);
    var checkTypeKey = AI.BlockUtils.YailTypeToBlocklyType("key",AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_PAIR_INPUT,
            ['KEY', checkTypeKey, Blockly.inputs.Align.RIGHT],
            ['VALUE', checkTypeAny, Blockly.inputs.Align.RIGHT],
            Blockly.inputs.Align.RIGHT);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_PAIR_TOOLTIP);
    this.setInputsInline(true);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_MAKE_PAIR_TITLE }]
};

Blockly.Blocks['dictionaries_lookup'] = {
  // Look up in a dictionary.
  category: 'Dictionaries',
  helpUrl : Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_LOOKUP_HELPURL,
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("any",AI.BlockUtils.OUTPUT));
    var checkTypeDict = AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT);
    var checkTypeAny = AI.BlockUtils.YailTypeToBlocklyType("any",AI.BlockUtils.INPUT);
    var checkTypeKey = AI.BlockUtils.YailTypeToBlocklyType("key",AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_LOOKUP_INPUT,
      ['KEY', checkTypeKey, Blockly.inputs.Align.RIGHT],
      ['DICT', checkTypeDict, Blockly.inputs.Align.RIGHT],
      ['NOTFOUND', checkTypeAny, Blockly.inputs.Align.RIGHT],
      Blockly.inputs.Align.RIGHT);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_LOOKUP_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_LOOKUP_TITLE }]
};

Blockly.Blocks['dictionaries_set_pair'] = {
  category: 'Dictionaries',
  helpUrl: Blockly.Msg.LANG_DICTIONARIES_SET_PAIR_HELPURL,
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    var checkTypeDict = AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT);
    var checkTypeKey = AI.BlockUtils.YailTypeToBlocklyType("key",AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_SET_PAIR_INPUT,
            ['KEY', checkTypeKey, Blockly.inputs.Align.RIGHT],
            ['DICT', checkTypeDict, Blockly.inputs.Align.RIGHT],
            ['VALUE', null, Blockly.inputs.Align.RIGHT],
            Blockly.inputs.Align.RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_SET_PAIR_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_SET_PAIR_TITLE }]
};

Blockly.Blocks['dictionaries_delete_pair'] = {
  category: 'Dictionaries',
  helpUrl: Blockly.Msg.LANG_DICTIONARIES_DELETE_PAIR_HELPURL,
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    var checkTypeDict = AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT);
    var checkTypeKey = AI.BlockUtils.YailTypeToBlocklyType("key",AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_DELETE_PAIR_INPUT,
            ['DICT', checkTypeDict, Blockly.inputs.Align.RIGHT],
            ['KEY', checkTypeKey, Blockly.inputs.Align.RIGHT],
            Blockly.inputs.Align.RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_DELETE_PAIR_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_DELETE_PAIR_TITLE }]
};

Blockly.Blocks['dictionaries_recursive_lookup'] = {
  // Look up in a dictionary.
  category: 'Dictionaries',
  helpUrl : Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_LOOKUP_HELPURL,
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("any",AI.BlockUtils.OUTPUT));
    var checkTypeDict = AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT);
    var checkTypeAny = AI.BlockUtils.YailTypeToBlocklyType("any",AI.BlockUtils.INPUT);
    var checkTypeList = AI.BlockUtils.YailTypeToBlocklyType("list",AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_LOOKUP_INPUT,
            ['KEYS', checkTypeList, Blockly.inputs.Align.RIGHT],
            ['DICT', checkTypeDict, Blockly.inputs.Align.RIGHT],
            ['NOTFOUND', checkTypeAny, Blockly.inputs.Align.RIGHT],
            Blockly.inputs.Align.RIGHT);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_LOOKUP_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_LOOKUP_TITLE }]
};

Blockly.Blocks['dictionaries_recursive_set'] = {
  category: 'Dictionaries',
  helpUrl: Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_SET_HELPURL,
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    var checkTypeDict = AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT);
    var checkTypeAny = AI.BlockUtils.YailTypeToBlocklyType("any",AI.BlockUtils.INPUT);
    var checkTypeList = AI.BlockUtils.YailTypeToBlocklyType("list",AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_SET_INPUT,
      ['KEYS', checkTypeList, Blockly.inputs.Align.RIGHT],
      ['DICT', checkTypeDict, Blockly.inputs.Align.RIGHT],
      ['VALUE', checkTypeAny, Blockly.inputs.Align.RIGHT],
      Blockly.inputs.Align.RIGHT);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_SET_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_SET_TITLE }]
};

Blockly.Blocks['dictionaries_getters'] = {
  category: 'Dictionaries',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks['dictionaries_getters'].HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.OUTPUT));
    this.appendValueInput('DICT')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("dictionary", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_DICTIONARIES_GETTERS_TITLE)
        .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks['dictionaries_getters'].TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_TYPEBLOCK,
    dropDown: {
      titleName: 'OP',
      value: 'KEYS'
    }
  }, {
    translatedName: Blockly.Msg.LANG_DICTIONARIES_GET_VALUES_TYPEBLOCK,
    dropDown: {
      titleName: 'OP',
      value: 'VALUES'
    }
  }]
};

Blockly.Blocks['dictionaries_getters'].OPERATORS = function () {
  return [[Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_TITLE, 'KEYS'],
    [Blockly.Msg.LANG_DICTIONARIES_GET_VALUES_TITLE, 'VALUES']];
};

Blockly.Blocks['dictionaries_getters'].TOOLTIPS = function () {
  return {
    'KEYS': Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_TOOLTIP,
    'VALUES': Blockly.Msg.LANG_DICTIONARIES_GET_VALUES_TOOLTIP
  }
};

Blockly.Blocks['dictionaries_getters'].HELPURLS = function() {
  return {
    'KEYS': Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_HELPURL,
    'VALUES': Blockly.Msg.LANG_DICTIONARIES_GET_VALUES_HELPURL
  }
};

Blockly.Blocks['dictionaries_get_values'] = {
  category: 'Dictionaries',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks['dictionaries_getters'].HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.OUTPUT));
    this.appendValueInput('DICT')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("dictionary", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_DICTIONARIES_GETTERS_TITLE)
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.dictionaries_getters.OPERATORS), 'OP');
    this.setFieldValue('VALUES', "OP");
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.dictionaries_getters.TOOLTIPS()[mode];
    });  }
};

Blockly.Blocks['dictionaries_is_key_in'] = {
   // Checks if a key is in a dictionary
  category : 'Dictionaries',
  // helpUrl : Blockly.Msg.LANG_LISTS_IS_IN_HELPURL,
  init : function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    var checkTypeDict = AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT);
    var checkTypeKey = AI.BlockUtils.YailTypeToBlocklyType("key",AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_IS_KEY_IN_INPUT,
            ['KEY', checkTypeKey, Blockly.inputs.Align.RIGHT],
            ['DICT', checkTypeDict, Blockly.inputs.Align.RIGHT],
            Blockly.inputs.Align.RIGHT);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("boolean",AI.BlockUtils.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_IS_KEY_IN_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_IS_KEY_IN_TITLE }]
};

Blockly.Blocks['dictionaries_length'] = {
   // Gets all the values in a dictionary
  category : 'Dictionaries',
  //helpUrl : Blockly.Msg.LANG_LISTS_LENGTH_HELPURL,
  init : function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number",AI.BlockUtils.OUTPUT));
    this.appendValueInput('DICT')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_DICTIONARIES_LENGTH_TITLE)
      .appendField(Blockly.Msg.LANG_DICTIONARIES_LENGTH_INPUT);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_LENGTH_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_LENGTH_TITLE }]
};

Blockly.Blocks['dictionaries_alist_to_dict'] = {
   // Gets all the values in a dictionary
  category : 'Dictionaries',
  //helpUrl : Blockly.Msg.LANG_LISTS_LENGTH_HELPURL,
  init : function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.OUTPUT));
    this.appendValueInput('PAIRS')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("list",AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_DICTIONARIES_ALIST_TO_DICT_TITLE)
      .appendField(Blockly.Msg.LANG_DICTIONARIES_ALIST_TO_DICT_INPUT);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_ALIST_TO_DICT_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_ALIST_TO_DICT_TITLE }]
};

Blockly.Blocks['dictionaries_dict_to_alist'] = {
   // Gets all the values in a dictionary
  category : 'Dictionaries',
  //helpUrl : Blockly.Msg.LANG_LISTS_LENGTH_HELPURL,
  init : function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list",AI.BlockUtils.OUTPUT));
    this.appendValueInput('DICT')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_DICTIONARIES_DICT_TO_ALIST_TITLE)
      .appendField(Blockly.Msg.LANG_DICTIONARIES_DICT_TO_ALIST_INPUT);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_DICT_TO_ALIST_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_DICT_TO_ALIST_TITLE }]
};

Blockly.Blocks['dictionaries_copy'] = {
   // Gets all the values in a dictionary
  category : 'Dictionaries',
  //helpUrl : Blockly.Msg.LANG_LISTS_LENGTH_HELPURL,
  init : function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.OUTPUT));
    this.appendValueInput('DICT')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT))
      .appendField(Blockly.Msg.LANG_DICTIONARIES_COPY_TITLE)
      .appendField(Blockly.Msg.LANG_DICTIONARIES_COPY_INPUT);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_COPY_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_COPY_TITLE }]
};

Blockly.Blocks['dictionaries_combine_dicts'] = {
   // Checks if a key is in a dictionary
  category : 'Dictionaries',
  // helpUrl : Blockly.Msg.LANG_LISTS_IS_IN_HELPURL,
  init : function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    var checkTypeDict = AI.BlockUtils.YailTypeToBlocklyType("dictionary",AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_COMBINE_DICTS_INPUT,
            ['DICT1', checkTypeDict, Blockly.inputs.Align.RIGHT],
            ['DICT2', checkTypeDict, Blockly.inputs.Align.RIGHT],
            Blockly.inputs.Align.RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_COMBINE_DICTS_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_COMBINE_DICTS_TITLE }]
};

Blockly.Blocks['dictionaries_walk_tree'] = {
  category: 'Dictionaries',
  helpUrl: Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_HELPURL,
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    var checkTypeDict = AI.BlockUtils.YailTypeToBlocklyType('dictionary', AI.BlockUtils.INPUT);
    var checkTypeList = AI.BlockUtils.YailTypeToBlocklyType('list', AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_TITLE,
      ['PATH', checkTypeList, Blockly.inputs.Align.RIGHT],
      ['DICT', checkTypeDict, Blockly.inputs.Align.RIGHT],
      Blockly.inputs.Align.RIGHT);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType('list', AI.BlockUtils.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_TITLE }]
};

Blockly.Blocks['dictionaries_walk_all'] = {
  category: 'Dictionaries',
  helpUrl: Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_ALL_HELPURL,
  init: function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_ALL_TITLE,
      Blockly.inputs.Align.LEFT);
    this.setOutput(true, 'ALL_OPERATOR');
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_ALL_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_ALL_TITLE }]
};

Blockly.Blocks['dictionaries_is_dict'] = {
   // Gets all the values in a dictionary
  category : 'Dictionaries',
  //helpUrl : Blockly.Msg.LANG_LISTS_LENGTH_HELPURL,
  init : function() {
    this.setColour(Blockly.DICTIONARY_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("boolean",AI.BlockUtils.OUTPUT));
    this.interpolateMsg(Blockly.Msg.LANG_DICTIONARIES_IS_DICT_TITLE,
      ['THING', null, Blockly.inputs.Align.RIGHT], Blockly.inputs.Align.RIGHT);
    this.setTooltip(Blockly.Msg.LANG_DICTIONARIES_IS_DICT_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_DICTIONARIES_IS_DICT_TITLE }]
};
