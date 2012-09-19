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
 * @fileoverview Blocks for building blocks.
 * @author fraser@google.com (Neil Fraser)
 */

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.factory_base = {
  // Base of new block.
  category: null,
  init: function() {
    this.setColour(120);
    this.appendTitle('category');
    this.appendTitle(new Blockly.FieldTextInput('Math'), 'CAT');
    this.appendTitle('name');
    this.appendTitle(new Blockly.FieldTextInput('foo'), 'NAME');
    this.appendInput('titles', Blockly.NEXT_STATEMENT, 'TITLES');
    this.appendInput('inputs', Blockly.NEXT_STATEMENT, 'INPUTS');
    var dropdown = new Blockly.FieldDropdown([
        ['external inputs', 'EXT'],
        ['inline inputs', 'INT']]);
    this.appendInput([dropdown, 'INLINE'], Blockly.DUMMY_INPUT);
    dropdown = new Blockly.FieldDropdown([
        ['no connections', 'NONE'],
        ['left output', 'LEFT'],
        ['top+bottom connections', 'BOTH'],
        ['top connection', 'TOP'],
        ['bottom connection', 'BOTTOM']],
        function(option) {
          var block = this.sourceBlock_;
          var exists = block.getInput('OUTPUTTYPE');
          if (option == 'left output') {
            if (!exists) {
              block.appendInput('output type', Blockly.INPUT_VALUE,
                                'OUTPUTTYPE', 'Type');
              block.moveInputBefore('OUTPUTTYPE', 'COLOUR');
            }
          } else {
            if (exists) {
              block.removeInput('OUTPUTTYPE', 'COLOUR');
            }
          }
          this.setText(option);
        });
    this.appendInput([dropdown, 'CONNECTIONS'], Blockly.DUMMY_INPUT);
    this.appendInput('colour', Blockly.INPUT_VALUE, 'COLOUR', 'Colour');
    this.appendInput('tooltip', Blockly.INPUT_VALUE, 'TOOLLTIP', String);
    this.appendInput('help url', Blockly.INPUT_VALUE, 'HELP', String);
    this.setTooltip('');
  }
};

Blockly.Language.text_static = {
  // Text value.
  category: 'Title',
  init: function() {
    this.setColour(160);
    this.appendTitle('text');
    this.appendTitle(new Blockly.FieldTextInput(''), 'TEXT');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

Blockly.Language.text_input = {
  // Text input.
  category: 'Title',
  init: function() {
    this.setColour(160);
    this.appendTitle('text input');
    this.appendTitle(new Blockly.FieldTextInput('default'), 'TEXT');
    this.appendTitle(',');
    this.appendTitle(new Blockly.FieldTextInput('NAME'), 'NAME');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

Blockly.Language.dropdown = {
  // Dropdown menu.
  category: 'Title',
  init: function() {
    this.setColour(160);
    this.appendTitle('dropdown');
    this.appendTitle(new Blockly.FieldTextInput('NAME'), 'NAME');
    this.appendInput('options', Blockly.NEXT_STATEMENT, 'OPTIONS');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

Blockly.Language.dropdown_option = {
  // Dropdown menu option.
  category: 'Title',
  init: function() {
    this.setColour(160);
    this.appendTitle('option');
    this.appendTitle(new Blockly.FieldTextInput('value'), 'VALUE');
    this.appendTitle(',');
    this.appendTitle(new Blockly.FieldTextInput('NAME'), 'NAME');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

Blockly.Language.input_value = {
  // Value input.
  category: 'Input',
  init: function() {
    this.setColour(210);
    this.appendTitle('value input');
    this.appendTitle(new Blockly.FieldTextInput('NAME'), 'NAME');
    this.appendInput('titles', Blockly.NEXT_STATEMENT, 'TITLES');
    this.appendInput('type', Blockly.INPUT_VALUE, 'TYPE', 'Type');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

Blockly.Language.input_statement = {
  // Statement input.
  category: 'Input',
  init: function() {
    this.setColour(210);
    this.appendTitle('statement input');
    this.appendTitle(new Blockly.FieldTextInput('NAME'), 'NAME');
    this.appendInput('titles', Blockly.NEXT_STATEMENT, 'TITLES');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

Blockly.Language.input_variable = {
  // Variable input.
  category: 'Input',
  init: function() {
    this.setColour(210);
    this.appendTitle('variable input');
    this.appendTitle(new Blockly.FieldTextInput('NAME'), 'NAME');
    this.appendTitle('varname');
    this.appendTitle(new Blockly.FieldTextInput('x'), 'VARNAME');
    this.appendInput('titles', Blockly.NEXT_STATEMENT, 'TITLES');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

(function() {
  var ColourBlock = function(hue) {
    this.colourHue = hue;
  };
  ColourBlock.prototype.category = 'Colour';
  ColourBlock.prototype.init = function() {
    this.setColour(this.colourHue);
    this.appendTitle('hue:');
    this.appendTitle(new Blockly.FieldTextInput(String(this.colourHue),
                                                ColourBlock.validator), 'HUE');
    this.setOutput(true, 'Colour');
    this.setTooltip('');
  };
  ColourBlock.validator = function(text) {
    // Ensure that only a number may be entered.
    var n = window.parseInt(text || 0, 10);
    if (window.isNaN(n) || n < 0) {
      return null;
    }
    n %= 360;
    this.sourceBlock_.setColour(n);
    return String(n);
  };

  var colours = [65, 120, 160, 210, 230, 290, 330];
  for (var x = 0; x < colours.length; x++) {
    Blockly.Language['colour_' + x] = new ColourBlock(colours[x]);
  }
})();

Blockly.Language.type_group = {
  // Group of types.
  category: 'Type',
  init: function() {
    this.setColour(230);
    this.appendTitle('any of');
    this.appendInput('type #1', Blockly.INPUT_VALUE, 'TYPE1', 'Type');
    this.appendInput('type #2', Blockly.INPUT_VALUE, 'TYPE2', 'Type');
    this.setOutput(true, 'Type');
    this.setTooltip('');
  }
};

Blockly.Language.type_null = {
  // Null type.
  category: 'Type',
  valueType: 'null',
  init: function() {
    this.setColour(230);
    this.appendTitle('any');
    this.setOutput(true, 'Type');
    this.setTooltip('');
  }
};

Blockly.Language.type_boolean = {
  // Boolean type.
  category: 'Type',
  valueType: 'Boolean',
  init: function() {
    this.setColour(230);
    this.appendTitle('boolean');
    this.setOutput(true, 'Type');
    this.setTooltip('');
  }
};

Blockly.Language.type_number = {
  // Number type.
  category: 'Type',
  valueType: 'Number',
  init: function() {
    this.setColour(230);
    this.appendTitle('number');
    this.setOutput(true, 'Type');
    this.setTooltip('');
  }
};

Blockly.Language.type_string = {
  // String type.
  category: 'Type',
  valueType: 'String',
  init: function() {
    this.setColour(230);
    this.appendTitle('string');
    this.setOutput(true, 'Type');
    this.setTooltip('');
  }
};

Blockly.Language.type_list = {
  // List type.
  category: 'Type',
  valueType: 'Array',
  init: function() {
    this.setColour(230);
    this.appendTitle('list');
    this.setOutput(true, 'Type');
    this.setTooltip('');
  }
};

Blockly.Language.type_other = {
  // Other type.
  category: 'Type',
  valueType: undefined,
  init: function() {
    this.setColour(230);
    this.appendTitle('other');
    this.appendTitle(new Blockly.FieldTextInput(''), 'TYPE');
    this.setOutput(true, 'Type');
    this.setTooltip('');
  }
};
