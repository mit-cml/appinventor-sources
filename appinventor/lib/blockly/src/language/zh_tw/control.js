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
 * @fileoverview Control blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 */

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.controls_for = {
  // For loop.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_FOR_HELPURL,
  init: function() {
    this.setColour(120);
    //this.appendTitle(Blockly.LANG_CONTROLS_FOR_TITLE_COUNT);
    this.appendInput(Blockly.LANG_CONTROLS_FOR_INPUT_WITH,
        Blockly.LOCAL_VARIABLE, 'VAR').setText(
        Blockly.Variables.generateUniqueName());
    this.appendInput(Blockly.LANG_CONTROLS_FOR_INPUT_FROM,
        Blockly.INPUT_VALUE, 'FROM', Number);
    this.appendInput(Blockly.LANG_CONTROLS_FOR_INPUT_TO,
        Blockly.INPUT_VALUE, 'TO', Number);
    this.appendInput(Blockly.LANG_CONTROLS_FOR_INPUT_DO,
        Blockly.NEXT_STATEMENT, 'DO');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return Blockly.LANG_CONTROLS_FOR_TOOLTIP_1.replace('%1',
          thisBlock.getInputVariable('VAR'));
    });
  },
  getVars: function() {
    return [this.getInputVariable('VAR')];
  },
  renameVar: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getInputVariable('VAR'))) {
      this.setInputVariable('VAR', newName);
    }
  }
};
