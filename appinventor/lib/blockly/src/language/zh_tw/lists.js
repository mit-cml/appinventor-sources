/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/blockly/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview List blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 */

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.lists_getIndex = {
  // Get element at index.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_GET_INDEX_HELPURL,
  init: function() {
    this.setColour(210);
    this.setOutput(true, null);
    //this.appendTitle(Blockly.LANG_LISTS_GET_INDEX_TITLE);
    this.appendInput(Blockly.LANG_LISTS_GET_INDEX_INPUT_IN_LIST,
        Blockly.INPUT_VALUE, 'VALUE', Array);
    this.appendInput(Blockly.LANG_LISTS_GET_INDEX_INPUT_AT,
        Blockly.INPUT_VALUE, 'AT', Number);
    this.setInputsInline(true);
    this.setTooltip(Blockly.LANG_LISTS_GET_INDEX_TOOLTIP_1);
  }
};

Blockly.Language.lists_setIndex = {
  // Set element at index.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_SET_INDEX_HELPURL,
  init: function() {
    this.setColour(210);
    //this.appendTitle(Blockly.LANG_LISTS_SET_INDEX_TITLE);
    this.appendInput(Blockly.LANG_LISTS_SET_INDEX_INPUT_IN_LIST,
        Blockly.INPUT_VALUE, 'LIST', Array);
    this.appendInput(Blockly.LANG_LISTS_SET_INDEX_INPUT_AT,
        Blockly.INPUT_VALUE, 'AT', Number);
    this.appendInput(Blockly.LANG_LISTS_SET_INDEX_INPUT_TO,
        Blockly.INPUT_VALUE, 'TO', null);
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.LANG_LISTS_SET_INDEX_TOOLTIP_1);
  }
};
