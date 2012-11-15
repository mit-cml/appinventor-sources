/**
.setAlign(Blockly.ALIGN_RIGHT) * Visual Blocks Language
 *
 * Copyright 2012 Massachusetts Institute of Technology. All rights reserved.
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
 * @fileoverview Logic blocks for Blockly, modified for App Inventor
 * @author fraser@google.com (Neil Fraser)
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

// TODO(andrew): Change addition, multiplication, min, and max to take multiple arguments.
// TODO(andrew): Add appropriate helpurls for each block.

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.math_number = {
  // Numeric value.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : '',
  init : function() {
    this.setColour(230);
    this.appendDummyInput().appendTitle(
        new Blockly.FieldTextInput('0', Blockly.Language.math_number.validator), 'NUM');
    this.setOutput(true, Number);
    this.setTooltip("Report the number shown.");
  }
};

Blockly.Language.math_number.validator = function(text) {
  // Ensure that only a number may be entered.
  // TODO: Handle cases like 'o', 'ten', '1,234', '3,14', etc.
  var n = window.parseFloat(text || 0);
  return window.isNaN(n) ? null : String(n);
};

Blockly.Language.math_compare = {
  // Basic arithmetic operator.
  // TODO(Andrew): equality block needs to have any on the sockets.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Boolean);
    this.appendValueInput('A').setCheck(Number);
    this.appendValueInput('B').setCheck(Number).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_compare.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_compare.OPERATORS =
  [['=', 'EQ'],
   ['\u2260', 'NEQ'],
   ['<', 'LT'],
   ['\u2264', 'LTE'],
   ['>', 'GT'],
   ['\u2265', 'GTE']];

Blockly.Language.math_compare.TOOLTIPS = {
  EQ: 'Return true if both numbers are equal to each other.',
  NEQ: 'Return true if both numbers are not equal to each other.',
  LT: 'Return true if the first number is smaller\n' +
      'than the second number.',
  LTE: 'Return true if the first number is smaller\n' +
      'than or equal to the second number.',
  GT: 'Return true if the first number is greater\n' +
      'than the second number.',
  GTE: 'Return true if the first number is greater\n' +
      'than or equal to the second number.'
};

Blockly.Language.math_add = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('NUM0').setCheck(Number);
    this.appendValueInput('NUM1').setCheck(Number).appendTitle("+");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return 'Return the sum of the numbers.';
    });
    this.setMutator(new Blockly.Mutator(['math_mutator_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
    this.itemCount_ = 2;
  },

  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'math_mutator_item',this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){
    var input = this.appendDummyInput(this.emptyInputName);
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Number);
    if(inputNum != 0){
      input.appendTitle("+");
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.inputList[0].titleRow[0].setText("+");
  }
};

Blockly.Language.math_mutator_item = {
  // Add items.
  init: function() {
    this.setColour(230);
    this.appendDummyInput()
        //.appendTitle(Blockly.LANG_LISTS_CREATE_WITH_ITEM_TITLE);
        .appendTitle("number");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    //this.setTooltip(Blockly.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP_1);
    this.contextMenu = false;
  }
};


Blockly.Language.math_subtract = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('A').setCheck(Number);
    this.appendValueInput('B').setCheck(Number).appendTitle("-");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return 'Return the difference of the two numbers.';
    });
  }
};

Blockly.Language.math_multiply = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('NUM0').setCheck(Number);
    this.appendValueInput('NUM1').setCheck(Number).appendTitle("*");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return 'Return the product of the numbers.';
    });
    this.setMutator(new Blockly.Mutator(['math_mutator_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'math_mutator_item',this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){
    var input = this.appendDummyInput(this.emptyInputName);
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Number);
    if(inputNum != 0){
      input.appendTitle("*");
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.inputList[0].titleRow[0].setText("*");
  }
};

Blockly.Language.math_division = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('A').setCheck(Number);
    this.appendValueInput('B').setCheck(Number).appendTitle("/");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return 'Return the quotient of the two numbers.';
    });
  }
};

Blockly.Language.math_power = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('A').setCheck(Number);
    this.appendValueInput('B').setCheck(Number).appendTitle("^");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      //var mode = thisBlock.getTitleValue('OP');
      return 'Return the first number raised to\n' +
      'the power of the second number.';
    });
  }
};

Blockly.Language.math_single = {
  // Advanced math operators with single operand.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('NUM').setCheck(Number).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_single.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_single.OPERATORS =
  [['sqrt', 'ROOT'],
   ['abs', 'ABS'],
   ['-', 'NEG'],
   ['log', 'LN'],
   ['e^', 'EXP']];

Blockly.Language.math_single.TOOLTIPS = {
  ROOT: 'Return the square root of a number.',
  ABS: 'Return the absolute value of a number.',
  NEG: 'Return the negation of a number.',
  LN: 'Return the natural logarithm of a number.',
  EXP: 'Return e to the power of a number.'
};

Blockly.Language.math_random_int = {
  // Random integer between [X] and [Y].
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('FROM').setCheck(Number).appendTitle('random integer').appendTitle('from');
    this.appendValueInput('TO').setCheck(Number).appendTitle('to');
    this.setInputsInline(true);
    this.setTooltip('Returns a random integer between the upper bound\n' +
        'and the lower bound. The bounds will be clipped to be smaller\n' +
        'than 2**30.');
  }
};

Blockly.Language.math_random_float = {
  // Random fraction between 0 and 1.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendDummyInput().appendTitle('random fraction');
    this.setTooltip('Return a random number between 0 and 1.');
  }
};

Blockly.Language.math_random_set_seed = {
  // Set the seed of the radom number generator
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(false, Number);
    this.appendValueInput('NUM').setCheck(Number).appendTitle('random set seed').appendTitle('to');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('specifies a numeric seed\n' +
        'for the random number generator');
  }
};

Blockly.Language.math_round = {
  // Rounding functions.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('NUM').setCheck(Number).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_round.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_round.OPERATORS =
  [['round', 'ROUND'],
   ['ceiling', 'CEILING'],
   ['floor', 'FLOOR']];

Blockly.Language.math_round.TOOLTIPS = {
  ROUND : 'Round a number up or down.',
  CEILING : 'Rounds the input to the smallest\n' +
      'number not less then the input',
  FLOOR : 'Rounds the input to the largest\n' +
      'number not greater then the input'
};

Blockly.Language.math_on_list = {
  // Evaluate a list of numbers to return sum, average, min, max, etc.
  // Some functions also work on text (min, max, mode, median).
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('NUM0').setCheck(Number).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.appendValueInput('NUM1').setCheck(Number);
    this.setInputsInline(false);
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_on_list.TOOLTIPS[mode];
    });
    this.setMutator(new Blockly.Mutator(['math_mutator_item']));
    this.itemCount_ = 2;
    this.valuesToSave = {'OP':null};
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';

  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'math_mutator_item',this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){
    var input = this.appendDummyInput(this.emptyInputName);
    input.appendTitle(new Blockly.FieldDropdown(this.OPERATORS),'OP');
    this.setTitleValue(this.valuesToSave['OP'],'OP');
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Number);
    if(inputNum == 0){
      input.appendTitle(new Blockly.FieldDropdown(this.OPERATORS),'OP');
      this.setTitleValue(this.valuesToSave['OP'],'OP');
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {

    for(var i=0;i<Blockly.Language.math_on_list.OPERATORS.length;i++){
      if(Blockly.Language.math_on_list.OPERATORS[i][1] == this.getTitleValue("OP") ){
        containerBlock.inputList[0].titleRow[0].setText(Blockly.Language.math_on_list.OPERATORS[i][0]);
      }
    }

  }
};

Blockly.Language.math_on_list.OPERATORS =
  [['min', 'MIN'],
   ['max', 'MAX']];

Blockly.Language.math_on_list.TOOLTIPS = {
  MIN: 'Return the smallest of its arguments..',
  MAX: 'Return the largest of its arguments..'
};

Blockly.Language.math_divide = {
  // Remainder or quotient of a division.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : '',
  init : function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('DIVIDEND').setCheck(Number).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.appendValueInput('DIVISOR').setCheck(Number).appendTitle('\u00F7');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_divide.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_divide.OPERATORS = 
  [['modulo of', 'MODULO'],
   ['remainder of', 'REMAINDER'],
   [ 'quotient of', 'QUOTIENT' ]];

Blockly.Language.math_divide.TOOLTIPS = {
  MODULO: 'Return the modulo.',
  REMAINDER: 'Return the remainder.',
  QUOTIENT: 'Return the quotient.'
};

Blockly.Language.math_trig = {
  // Trigonometry operators.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : '',
  init : function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('NUM').setCheck(Number).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_trig.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_trig.OPERATORS =
  [[ 'sin', 'SIN' ], 
   [ 'cos', 'COS' ],
   [ 'tan', 'TAN' ], 
   [ 'asin', 'ASIN' ], 
   [ 'acos', 'ACOS' ],
   [ 'atan', 'ATAN' ]];

Blockly.Language.math_trig.TOOLTIPS = {
  SIN : 'Provides the sine of the given angle in degrees.',
  COS : 'Provides the cosine of the given angle in degrees.',
  TAN : 'Provides the tangent of the given angle in degrees.',
  ASIN : 'Provides the angle in the range (-90,+90]\n' + 
      'degrees with the given sine value.',
  ACOS : 'Provides the angle in the range [0, 180)\n' + 
      'degrees with the given cosine value.',
  ATAN : 'Provides the angle in the range (-90, +90)\n' + 
      'degrees with the given tangent value.'
};

Blockly.Language.math_atan2 = {
  // Trigonometry operators.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : '',
  init : function() {
    this.setColour(230);
    this.setOutput(true);
    this.appendDummyInput().appendTitle('atan2')
    this.appendValueInput('Y').setCheck(Number).appendTitle('y').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('X').setCheck(Number).appendTitle('x').setAlign(Blockly.ALIGN_RIGHT);
    this.setInputsInline(false);
    this.setTooltip('Provides the angle in the range (-180, +180]\n' +
        'degrees with the given rectangular coordinates.');
  }
};

Blockly.Language.math_convert_angles = {
  // Trigonometry operators.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : '',
  init : function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('NUM').setCheck(Number).appendTitle('convert').appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_convert_angles.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_convert_angles.OPERATORS =
  [[ 'radians to degrees', 'RADIANS_TO_DEGREES' ],
   [ 'degrees to radians', 'DEGREES_TO_RADIANS' ]];

Blockly.Language.math_convert_angles.TOOLTIPS = {
  RADIANS_TO_DEGREES : 'Returns the degree value in the range\n' +
      '[0, 360) corresponding to its radians argument.',
  DEGREES_TO_RADIANS : 'Returns the radian value in the range\n' +
      '[-\u03C0, +\u03C0) corresponding to its degrees argument.'
};

Blockly.Language.math_format_as_decimal = {
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : '',
  init : function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendDummyInput().appendTitle('format as decimal');
    this.appendValueInput('NUM').setCheck(Number).appendTitle('number').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('PLACES').setCheck(Number).appendTitle('places').setAlign(Blockly.ALIGN_RIGHT);
    this.setInputsInline(false);
    this.setTooltip('Returns the number formatted as a decimal\n' +
        'with a specified number of places.');
  }
};

Blockly.Language.math_is_a_number = {
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : '',
  init : function() {
    this.setColour(230);
    this.setOutput(true, Number);
    this.appendValueInput('NUM').appendTitle('is a number?');
    this.setTooltip(function() {
      return 'Tests if something is a number.';
    });
  }
};
