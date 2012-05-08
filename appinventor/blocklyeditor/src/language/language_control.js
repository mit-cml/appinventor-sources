/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/google-blockly/
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
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) {
  Blockly.Language = {};
}

Blockly.Language.controls_if = {
  // If condition.
  category: 'Control',
  helpUrl: 'http://en.wikipedia.org/wiki/Conditional_(programming)',
  init: function() {
    this.setColour('purple');
    this.addTitle('if');
    this.addInput('', '', Blockly.INPUT_VALUE);
    this.addInput('do', '', Blockly.NEXT_STATEMENT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setMutator(new Blockly.Mutator(this));
  },
  toolbox: ['controls_if_elseif', 'controls_if_else']
};

Blockly.Language.controls_if_if = {
  // If condition.
  helpUrl: 'http://en.wikipedia.org/wiki/Conditional_(programming)',
  init: function() {
    this.setColour('purple');
    this.addTitle('if');
    this.addInput('', '', Blockly.NEXT_STATEMENT);
  }
};

Blockly.Language.controls_if_elseif = {
  // Else-If condition.
  helpUrl: 'http://en.wikipedia.org/wiki/Conditional_(programming)',
  init: function() {
    this.setColour('purple');
    this.addTitle('else if');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  }
};

Blockly.Language.controls_if_else = {
  // Else condition.
  helpUrl: 'http://en.wikipedia.org/wiki/Conditional_(programming)',
  init: function() {
    this.setColour('purple');
    this.addTitle('else');
    this.setPreviousStatement(true);
  }
};

Blockly.Language.controls_whileUntil = {
  // Do while/until loop.
  category: 'Control',
  helpUrl: 'http://en.wikipedia.org/wiki/For_loop',
  init: function() {
    this.setColour('purple');
    this.addTitle('repeat');
    var dropdown = new Blockly.FieldDropdown(Blockly.Language.controls_whileUntil.MSG_WHILE, function() {
      return [Blockly.Language.controls_whileUntil.MSG_WHILE,
              Blockly.Language.controls_whileUntil.MSG_UNTIL];
    });
    this.addTitle(dropdown);
    this.addInput('', '', Blockly.INPUT_VALUE);
    this.addInput('do', '', Blockly.NEXT_STATEMENT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  }
};

Blockly.Language.controls_whileUntil.MSG_WHILE = 'while';
Blockly.Language.controls_whileUntil.MSG_UNTIL = 'until';

Blockly.Language.controls_for = {
  // For loop.
  category: 'Control',
  helpUrl: 'http://en.wikipedia.org/wiki/For_loop',
  init: function() {
    this.setColour('purple');
    this.addTitle('count');
    this.addInput('from', '', Blockly.INPUT_VALUE);
    this.addInput('to', '', Blockly.INPUT_VALUE);
    this.addInput('with', '', Blockly.LOCAL_VARIABLE).setText('x');
    this.addInput('do', '', Blockly.NEXT_STATEMENT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setInputsInline(true);
  },
  getVars: function() {
    return [this.getVariableInput(0)];
  },
  renameVar: function(oldName, newName) {
    if (Blockly.Variables.nameEquals(oldName, this.getVariableInput(0))) {
      this.setVariableInput(0, newName);
    }
  }
};

Blockly.Language.controls_forEach = {
  // For each loop.
  category: 'Control',
  helpUrl: 'http://en.wikipedia.org/wiki/For_loop',
  init: function() {
    this.setColour('purple');
    this.addTitle('for each');
    this.addInput('item', '', Blockly.LOCAL_VARIABLE).setText('x');
    this.addInput('in list', '', Blockly.INPUT_VALUE);
    this.addInput('do', '', Blockly.NEXT_STATEMENT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  },
  getVars: function() {
    return [this.getVariableInput(0)];
  },
  renameVar: function(oldName, newName) {
    if (Blockly.Variables.nameEquals(oldName, this.getVariableInput(0))) {
      this.setVariableInput(0, newName);
    }
  }
};
