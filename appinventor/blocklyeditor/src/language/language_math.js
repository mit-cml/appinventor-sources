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
 * @fileoverview Math blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) {
  Blockly.Language = {};
}

Blockly.Language.math_number = {
  // Numeric value.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Number',
  init: function() {
    this.setColour('baby');
    this.addTitle(new Blockly.FieldTextInput('0', function(text) {
      // Ensure that only a number may be entered.
      // TODO: Handle cases like 'o', 'ten', '1,234', '3,14', etc.
      var n = window.parseFloat(text || 0);
      return window.isNaN(n) ? null : String(n);
    }));
    this.setOutput(true);
  }
};

Blockly.Language.math_arithmetic = {
  // Basic arithmetic operator.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Arithmetic',
  init: function() {
    this.setColour('baby');
    this.setOutput(true);
    this.addInput('', '', Blockly.INPUT_VALUE);
    var dropdown = new Blockly.FieldDropdown('+', function() {
      return [
          Blockly.Language.math_arithmetic.MSG_ADD,
          Blockly.Language.math_arithmetic.MSG_MINUS,
          Blockly.Language.math_arithmetic.MSG_MULTIPLY,
          Blockly.Language.math_arithmetic.MSG_DIVIDE
        ];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
  }
};

Blockly.Language.math_arithmetic.MSG_ADD = '+';
Blockly.Language.math_arithmetic.MSG_MINUS = '-';
Blockly.Language.math_arithmetic.MSG_MULTIPLY = '\u00D7';
Blockly.Language.math_arithmetic.MSG_DIVIDE = '\u00F7';

Blockly.Language.math_change = {
  // Add to a variable in place.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Negation',
  init: function() {
    this.setColour('baby');
    this.addTitle('change');
    this.addTitle(new Blockly.FieldDropdown('item',
        Blockly.Variables.dropdownCreate, Blockly.Variables.dropdownChange));
    this.addInput('by', '', Blockly.INPUT_VALUE);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  },
  getVars: function() {
    return [this.getTitleText(1)];
  },
  renameVar: function(oldName, newName) {
    if (Blockly.Variables.nameEquals(oldName, this.getTitleText(1))) {
      this.setTitleText(newName, 1);
    }
  }
};

Blockly.Language.math_negate = {
  // Negation operator.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Negation',
  init: function() {
    this.setColour('baby');
    this.setOutput(true);
    this.addInput('-', '', Blockly.INPUT_VALUE);
  }
};

Blockly.Language.math_single = {
  // Advanced math operators with single operand.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Square_root',
  init: function() {
    this.setColour('baby');
    this.setOutput(true);
    var dropdown = new Blockly.FieldDropdown(Blockly.Language.math_single.MSG_ROOT, function() {
      return [Blockly.Language.math_single.MSG_ABS,
              Blockly.Language.math_single.MSG_ROOT,
              Blockly.Language.math_single.MSG_SIN,
              Blockly.Language.math_single.MSG_COS,
              Blockly.Language.math_single.MSG_TAN,
              Blockly.Language.math_single.MSG_ASIN,
              Blockly.Language.math_single.MSG_ACOS,
              Blockly.Language.math_single.MSG_ATAN];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
  }
};

Blockly.Language.math_single.MSG_ABS = 'absolute';
Blockly.Language.math_single.MSG_ROOT = '\u221A';
Blockly.Language.math_single.MSG_SIN = 'sin';
Blockly.Language.math_single.MSG_COS = 'cos';
Blockly.Language.math_single.MSG_TAN = 'tan';
Blockly.Language.math_single.MSG_ASIN = 'asin';
Blockly.Language.math_single.MSG_ACOS = 'acos';
Blockly.Language.math_single.MSG_ATAN = 'atan';

Blockly.Language.math_modulo = {
  // Remainder of a division.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Modulo_operation',
  init: function() {
    this.setColour('baby');
    this.setOutput(true);
    this.addInput('remainder of', '', Blockly.INPUT_VALUE);
    this.addInput('\u00F7', '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
  }
};

Blockly.Language.math_round = {
  // Rounding functions.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Rounding',
  init: function() {
    this.setColour('baby');
    this.setOutput(true);
    var dropdown = new Blockly.FieldDropdown(Blockly.Language.math_round.MSG_ROUND, function() {
      return [Blockly.Language.math_round.MSG_ROUND,
              Blockly.Language.math_round.MSG_ROUNDUP,
              Blockly.Language.math_round.MSG_ROUNDDOWN];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
  }
};

Blockly.Language.math_round.MSG_ROUND = 'round';
Blockly.Language.math_round.MSG_ROUNDUP = 'round up';
Blockly.Language.math_round.MSG_ROUNDDOWN = 'round down';

Blockly.Language.math_random_int = {
  // Random integer between [X] and [Y].
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Random_number_generation',
  init: function() {
    this.setColour('baby');
    this.setOutput(true);
    this.addTitle('random integer');
    this.addInput('from', '', Blockly.INPUT_VALUE);
    this.addInput('to', '', Blockly.INPUT_VALUE);
    // TODO: Ensure that only number blocks may used to set range.
    this.setInputsInline(true);
  }
};

Blockly.Language.math_random_float = {
  // Random fraction between 0 and 1.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Random_number_generation',
  init: function() {
    this.setColour('baby');
    this.setOutput(true);
    this.addTitle('random fraction');
  }
};
