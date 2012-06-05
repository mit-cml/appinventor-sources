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
    this.setColour(230);
    this.addTitle(new Blockly.FieldTextInput('0', function(text) {
      // Ensure that only a number may be entered.
      // TODO: Handle cases like 'o', 'ten', '1,234', '3,14', etc.
      var n = window.parseFloat(text || 0);
      return window.isNaN(n) ? null : String(n);
    }));
    this.setOutput(true);
    this.setTooltip('A number.');
  }
};

Blockly.Language.math_arithmetic = {
  // Basic arithmetic operator.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Arithmetic',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(230);
    this.setOutput(true);
    this.addInput('', '', Blockly.INPUT_VALUE);
    var dropdown = new Blockly.FieldDropdown(function() {
      return [
          thisBlock.MSG_ADD,
          thisBlock.MSG_MINUS,
          thisBlock.MSG_MULTIPLY,
          thisBlock.MSG_DIVIDE,
          thisBlock.MSG_POW
        ];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
    this.setTooltip(function() {
      switch (thisBlock.getValueLabel(1)) {
        case thisBlock.MSG_ADD:
          return 'Return the sum of the two numbers.';
        case thisBlock.MSG_MINUS:
          return 'Return the difference of the two numbers.';
        case thisBlock.MSG_MULTIPLY:
          return 'Return the product of the two numbers.';
        case thisBlock.MSG_DIVIDE:
          return 'Return the quotient of the two numbers.';
        case thisBlock.MSG_POW:
          return 'Return the first number raised to\nthe power of the second number.';
      }
      return '';
    });
  },
  MSG_ADD: '+',
  MSG_MINUS: '-',
  MSG_MULTIPLY: '\u00D7',
  MSG_DIVIDE: '\u00F7',
  MSG_POW: '^'
};

Blockly.Language.math_change = {
  // Add to a variable in place.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Negation',
  init: function() {
    this.setColour(230);
    this.addTitle('change');
    this.addTitle(new Blockly.FieldDropdown(
        Blockly.Variables.dropdownCreate, Blockly.Variables.dropdownChange))
        .setText('item');
    this.addInput('by', '', Blockly.INPUT_VALUE);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return 'Add a number to variable "' + thisBlock.getTitleText(1) + '".';
    });
  },
  getVars: function() {
    return [this.getTitleText(1)];
  },
  renameVar: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getTitleText(1))) {
      this.setTitleText(newName, 1);
    }
  }
};


Blockly.Language.math_single = {
  // Advanced math operators with single operand.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Square_root',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(230);
    this.setOutput(true);
    var dropdown = new Blockly.FieldDropdown(function() {
      return [thisBlock.MSG_ROOT,
              thisBlock.MSG_ABS,
              thisBlock.MSG_NEG,
              thisBlock.MSG_LN,
              thisBlock.MSG_LOG10,
              thisBlock.MSG_EXP,
              thisBlock.MSG_10POW];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
    this.setTooltip(function() {
      switch (thisBlock.getValueLabel(0)) {
        case thisBlock.MSG_ROOT:
          return 'Return the square root of a number.';
        case thisBlock.MSG_ABS:
          return 'Return the absolute value of a number.';
        case thisBlock.MSG_NEG:
          return 'Return the negation of a number.';
        case thisBlock.MSG_LN:
          return 'Return the natural logarithm of a number.';
        case thisBlock.MSG_LOG10:
          return 'Return the base 10 logarithm of a number.';
        case thisBlock.MSG_EXP:
          return 'Return e to the power of a number.';
        case thisBlock.MSG_10POW:
          return 'Return 10 to the power of a number.';
      }
      return '';
    });
  },
  MSG_ROOT: 'square root',
  MSG_ABS: 'absolute',
  MSG_NEG: '-',
  MSG_LN: 'ln',
  MSG_LOG10: 'log10',
  MSG_EXP: 'e^',
  MSG_10POW: '10^'
};

Blockly.Language.math_round = {
  // Rounding functions.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Rounding',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(230);
    this.setOutput(true);
    var dropdown = new Blockly.FieldDropdown(function() {
      return [thisBlock.MSG_ROUND,
              thisBlock.MSG_ROUNDUP,
              thisBlock.MSG_ROUNDDOWN];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
    this.setTooltip('Round a number up or down.');
  },
  MSG_ROUND: 'round',
  MSG_ROUNDUP: 'round up',
  MSG_ROUNDDOWN: 'round down'
};


Blockly.Language.math_trig = {
  // Trigonometry operators.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Trigonometric_functions',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(230);
    this.setOutput(true);
    var dropdown = new Blockly.FieldDropdown(function() {
      return [thisBlock.MSG_SIN,
              thisBlock.MSG_COS,
              thisBlock.MSG_TAN,
              thisBlock.MSG_ASIN,
              thisBlock.MSG_ACOS,
              thisBlock.MSG_ATAN];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
    this.setTooltip(function() {
      switch (thisBlock.getValueLabel(0)) {
        case thisBlock.MSG_SIN:
          return 'Return the sine of a degree.';
        case thisBlock.MSG_COS:
          return 'Return the cosine of a degree.';
        case thisBlock.MSG_TAN:
          return 'Return the tangent of a degree.';
        case thisBlock.MSG_ASIN:
          return 'Return the arcsine of a number.';
        case thisBlock.MSG_ACOS:
          return 'Return the arccosine of a number.';
        case thisBlock.MSG_ATAN:
          return 'Return the arctangent of a number.';
      }
      return '';
    });
  },
  MSG_SIN: 'sin',
  MSG_COS: 'cos',
  MSG_TAN: 'tan',
  MSG_ASIN: 'asin',
  MSG_ACOS: 'acos',
  MSG_ATAN: 'atan'
};

Blockly.Language.math_on_list = {
  // Evaluate a list of numbers to return sum, average, min, max, etc.
  // Some functions also work on text (min, max, mode, median).
  category: 'Math',
  helpUrl: '',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(230);
    this.setOutput(true);
    var dropdown = new Blockly.FieldDropdown(function() {
      return [thisBlock.MSG_SUM,
              thisBlock.MSG_MIN,
              thisBlock.MSG_MAX,
              thisBlock.MSG_AVERAGE,
              thisBlock.MSG_MEDIAN,
              thisBlock.MSG_MODE,
              thisBlock.MSG_STD_DEV,
              thisBlock.MSG_RANDOM_ITEM
              ];
    });
    this.addTitle(dropdown);
    this.addInput('of list', '', Blockly.INPUT_VALUE);
    this.setTooltip('Evaluate a list of numbers and return a member,\nor an aggregated result.');
  },
  MSG_SUM: 'sum',
  MSG_MIN: 'min',
  MSG_MAX: 'max',
  MSG_AVERAGE: 'average',
  MSG_MEDIAN: 'median',
  MSG_MODE: 'modes',
  MSG_STD_DEV: 'standard deviation',
  MSG_RANDOM_ITEM: 'random item'
};

Blockly.Language.math_constrain = {
  // Constrain a number between two limits.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Clamping_%28graphics%29',
  init: function() {
    this.setColour(230);
    this.setOutput(true);
    this.addInput('constrain', '', Blockly.INPUT_VALUE);
    this.addInput('between (low)', '', Blockly.INPUT_VALUE);
    this.addInput('and (high)', '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
    this.setTooltip('Constrain a number to be between the specified limits (inclusive).');
  }
};

Blockly.Language.math_modulo = {
  // Remainder of a division.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Modulo_operation',
  init: function() {
    this.setColour(230);
    this.setOutput(true);
    this.addInput('remainder of', '', Blockly.INPUT_VALUE);
    this.addInput('\u00F7', '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
    this.setTooltip('Return the remainder of dividing both numbers.');
  }
};

Blockly.Language.math_random_int = {
  // Random integer between [X] and [Y].
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Random_number_generation',
  init: function() {
    this.setColour(230);
    this.setOutput(true);
    this.addTitle('random integer');
    this.addInput('from', '', Blockly.INPUT_VALUE);
    this.addInput('to', '', Blockly.INPUT_VALUE);
    // TODO: Ensure that only number blocks may used to set range.
    this.setInputsInline(true);
    this.setTooltip('Return a random integer between the two\n specified limits, inclusive.');
  }
};

Blockly.Language.math_random_float = {
  // Random fraction between 0 and 1.
  category: 'Math',
  helpUrl: 'http://en.wikipedia.org/wiki/Random_number_generation',
  init: function() {
    this.setColour(230);
    this.setOutput(true);
    this.addTitle('random fraction');
    this.setTooltip('Return a random fraction between\n0.0 (inclusive) and 1.0 (exclusive).');
  }
};
