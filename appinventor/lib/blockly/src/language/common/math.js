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
 * @fileoverview Math blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 */

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.math_number = {
  // Numeric value.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_NUMBER_HELPURL,
  init: function() {
    this.setColour(230);
    this.appendTitle(new Blockly.FieldTextInput('0',
        Blockly.Language.math_number.validator), 'NUM');
    this.setOutput(true, Number);
    this.setTooltip(Blockly.LANG_MATH_NUMBER_TOOLTIP_1);
  }
};

Blockly.Language.math_number.validator = function(text) {
  // Ensure that only a number may be entered.
  // TODO: Handle cases like 'o', 'ten', '1,234', '3,14', etc.
  var n = window.parseFloat(text || 0);
  return window.isNaN(n) ? null : String(n);
};

Blockly.Language.math_arithmetic = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_ARITHMETIC_HELPURL,
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendInput('', Blockly.INPUT_VALUE, 'A', Number);
    var dropdown = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendInput([dropdown, 'OP'], Blockly.INPUT_VALUE, 'B', Number);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_arithmetic.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_arithmetic.OPERATORS =
    [['+', 'ADD'],
     ['-', 'MINUS'],
     ['\u00D7', 'MULTIPLY'],
     ['\u00F7', 'DIVIDE'],
     ['^', 'POWER']];

Blockly.Language.math_arithmetic.TOOLTIPS = {
  ADD: Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_ADD,
  MINUS: Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS,
  MULTIPLY: Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY,
  DIVIDE: Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE,
  POWER: Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_POWER
};

Blockly.Language.math_change = {
  // Add to a variable in place.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_CHANGE_HELPURL,
  init: function() {
    this.setColour(230);
    this.appendTitle(Blockly.LANG_MATH_CHANGE_TITLE_CHANGE);
    this.appendTitle(new Blockly.FieldDropdown(
        Blockly.Variables.dropdownCreate, Blockly.Variables.dropdownChange),
        'VAR').setText(Blockly.LANG_MATH_CHANGE_TITLE_ITEM);
    this.appendInput(Blockly.LANG_MATH_CHANGE_INPUT_BY,
        Blockly.INPUT_VALUE, 'DELTA', Number);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return Blockly.LANG_MATH_CHANGE_TOOLTIP_1.replace('%1',
          thisBlock.getTitleText('VAR'));
    });
  },
  getVars: function() {
    return [this.getTitleText('VAR')];
  },
  renameVar: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getTitleText('VAR'))) {
      this.setTitleText(newName, 'VAR');
    }
  }
};


Blockly.Language.math_single = {
  // Advanced math operators with single operand.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_SINGLE_HELPURL,
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    var dropdown = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendInput([dropdown, 'OP'], Blockly.INPUT_VALUE, 'NUM', Number);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_single.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_single.OPERATORS =
    [[Blockly.LANG_MATH_SINGLE_OP_ROOT, 'ROOT'],
     [Blockly.LANG_MATH_SINGLE_OP_ABSOLUTE, 'ABS'],
     ['-', 'NEG'],
     ['ln', 'LN'],
     ['log10', 'LOG10'],
     ['e^', 'EXP'],
     ['10^', 'POW10']];

Blockly.Language.math_single.TOOLTIPS = {
  ROOT: Blockly.LANG_MATH_SINGLE_TOOLTIP_ROOT,
  ABS: Blockly.LANG_MATH_SINGLE_TOOLTIP_ABS,
  NEG: Blockly.LANG_MATH_SINGLE_TOOLTIP_NEG,
  LN: Blockly.LANG_MATH_SINGLE_TOOLTIP_LN,
  LOG10: Blockly.LANG_MATH_SINGLE_TOOLTIP_LOG10,
  EXP: Blockly.LANG_MATH_SINGLE_TOOLTIP_EXP,
  POW10: Blockly.LANG_MATH_SINGLE_TOOLTIP_POW10
};


Blockly.Language.math_round = {
  // Rounding functions.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_ROUND_HELPURL,
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    var dropdown = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendInput([dropdown, 'OP'], Blockly.INPUT_VALUE, 'NUM', Number);
    this.setTooltip(Blockly.LANG_MATH_ROUND_TOOLTIP_1);
  }
};

Blockly.Language.math_round.OPERATORS =
    [[Blockly.LANG_MATH_ROUND_OPERATOR_ROUND, 'ROUND'],
     [Blockly.LANG_MATH_ROUND_OPERATOR_ROUNDUP, 'ROUNDUP'],
     [Blockly.LANG_MATH_ROUND_OPERATOR_ROUNDDOWN, 'ROUNDDOWN']];

Blockly.Language.math_trig = {
  // Trigonometry operators.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_TRIG_HELPURL,
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    var dropdown = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendInput([dropdown, 'OP'], Blockly.INPUT_VALUE, 'NUM', Number);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_trig.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_trig.OPERATORS =
    [['sin', 'SIN'],
     ['cos', 'COS'],
     ['tan', 'TAN'],
     ['asin', 'ASIN'],
     ['acos', 'ACOS'],
     ['atan', 'ATAN']];

Blockly.Language.math_trig.TOOLTIPS = {
  SIN: Blockly.LANG_MATH_TRIG_TOOLTIP_SIN,
  COS: Blockly.LANG_MATH_TRIG_TOOLTIP_COS,
  TAN: Blockly.LANG_MATH_TRIG_TOOLTIP_TAN,
  ASIN: Blockly.LANG_MATH_TRIG_TOOLTIP_ASIN,
  ACOS: Blockly.LANG_MATH_TRIG_TOOLTIP_ACOS,
  ATAN: Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN
};

Blockly.Language.math_on_list = {
  // Evaluate a list of numbers to return sum, average, min, max, etc.
  // Some functions also work on text (min, max, mode, median).
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_ONLIST_HELPURL,
  init: function() {
    this.setColour(230);
    this.setOutput(true, [Number, Array]);
    var dropdown = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendTitle(dropdown, 'OP');
    this.appendInput(Blockly.LANG_MATH_ONLIST_INPUT_OFLIST,
        Blockly.INPUT_VALUE, 'LIST', Array);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_on_list.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_on_list.OPERATORS =
    [[Blockly.LANG_MATH_ONLIST_OPERATOR_SUM, 'SUM'],
     [Blockly.LANG_MATH_ONLIST_OPERATOR_MIN, 'MIN'],
     [Blockly.LANG_MATH_ONLIST_OPERATOR_MAX, 'MAX'],
     [Blockly.LANG_MATH_ONLIST_OPERATOR_AVERAGE, 'AVERAGE'],
     [Blockly.LANG_MATH_ONLIST_OPERATOR_MEDIAN, 'MEDIAN'],
     [Blockly.LANG_MATH_ONLIST_OPERATOR_MODE, 'MODE'],
     [Blockly.LANG_MATH_ONLIST_OPERATOR_STD_DEV, 'STD_DEV'],
     [Blockly.LANG_MATH_ONLIST_OPERATOR_RANDOM, 'RANDOM']];

Blockly.Language.math_on_list.TOOLTIPS = {
  SUM: Blockly.LANG_MATH_ONLIST_TOOLTIP_SUM,
  MIN: Blockly.LANG_MATH_ONLIST_TOOLTIP_MIN,
  MAX: Blockly.LANG_MATH_ONLIST_TOOLTIP_MAX,
  AVERAGE: Blockly.LANG_MATH_ONLIST_TOOLTIP_AVERAGE,
  MEDIAN: Blockly.LANG_MATH_ONLIST_TOOLTIP_MEDIAN,
  MODE: Blockly.LANG_MATH_ONLIST_TOOLTIP_MODE,
  STD_DEV: Blockly.LANG_MATH_ONLIST_TOOLTIP_STD_DEV,
  RANDOM: Blockly.LANG_MATH_ONLIST_TOOLTIP_RANDOM
};

Blockly.Language.math_constrain = {
  // Constrain a number between two limits.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_CONSTRAIN_HELPURL,
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendInput(Blockly.LANG_MATH_CONSTRAIN_INPUT_CONSTRAIN,
        Blockly.INPUT_VALUE, 'VALUE', Number);
    this.appendInput(Blockly.LANG_MATH_CONSTRAIN_INPUT_LOW,
        Blockly.INPUT_VALUE, 'LOW', Number);
    this.appendInput(Blockly.LANG_MATH_CONSTRAIN_INPUT_HIGH,
        Blockly.INPUT_VALUE, 'HIGH', Number);
    this.setInputsInline(true);
    this.setTooltip(Blockly.LANG_MATH_CONSTRAIN_TOOLTIP_1);
  }
};

Blockly.Language.math_modulo = {
  // Remainder of a division.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_MODULO_HELPURL,
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendInput(Blockly.LANG_MATH_MODULO_INPUT_DIVIDEND,
        Blockly.INPUT_VALUE, 'DIVIDEND', Number);
    this.appendInput('\u00F7', Blockly.INPUT_VALUE, 'DIVISOR', Number);
    this.setInputsInline(true);
    this.setTooltip(Blockly.LANG_MATH_MODULO_TOOLTIP_1);
  }
};

Blockly.Language.math_random_int = {
  // Random integer between [X] and [Y].
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_RANDOM_INT_HELPURL,
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendTitle(Blockly.LANG_MATH_RANDOM_INT_TITLE_RANDOM);
    this.appendInput(Blockly.LANG_MATH_RANDOM_INT_INPUT_FROM,
        Blockly.INPUT_VALUE, 'FROM', Number);
    this.appendInput(Blockly.LANG_MATH_RANDOM_INT_INPUT_TO,
        Blockly.INPUT_VALUE, 'TO', Number);
    // TODO: Ensure that only number blocks may used to set range.
    this.setInputsInline(true);
    this.setTooltip(Blockly.LANG_MATH_RANDOM_INT_TOOLTIP_1);
  }
};

Blockly.Language.math_random_float = {
  // Random fraction between 0 and 1.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_RANDOM_FLOAT_HELPURL,
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendTitle(Blockly.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM);
    this.setTooltip(Blockly.LANG_MATH_RANDOM_FLOAT_TOOLTIP_1);
  }
};
