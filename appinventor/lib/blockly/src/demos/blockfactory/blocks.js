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
    var row = this.appendInput(Blockly.DUMMY_INPUT, '');
    row.appendTitle('category');
    row.appendTitle(new Blockly.FieldTextInput('Math'), 'CAT');
    row = this.appendInput(Blockly.DUMMY_INPUT, '');
    row.appendTitle('name');
    row.appendTitle(new Blockly.FieldTextInput('foo'), 'NAME');
    this.appendInput(Blockly.NEXT_STATEMENT, 'TITLES', 'Title')
        .appendTitle('titles');
    this.appendInput(Blockly.NEXT_STATEMENT, 'INPUTS', 'Input')
        .appendTitle('inputs');
    var dropdown = new Blockly.FieldDropdown([
        ['external inputs', 'EXT'],
        ['inline inputs', 'INT']]);
    this.appendInput(Blockly.DUMMY_INPUT, '')
        .appendTitle(dropdown, 'INLINE');
    dropdown = new Blockly.FieldDropdown([
        ['no connections', 'NONE'],
        ['left output', 'LEFT'],
        ['top+bottom connections', 'BOTH'],
        ['top connection', 'TOP'],
        ['bottom connection', 'BOTTOM']],
        function(option) {
          var block = this.sourceBlock_;
          var outputExists = block.getInput('OUTPUTTYPE');
          var topExists = block.getInput('TOPTYPE');
          var bottomExists = block.getInput('BOTTOMTYPE');
          if (option == 'left output') {
            if (!outputExists) {
              block.appendInput(Blockly.INPUT_VALUE, 'OUTPUTTYPE', 'Type')
                  .appendTitle('output type');
              block.moveInputBefore('OUTPUTTYPE', 'COLOUR');
            }
          } else if (outputExists) {
            block.removeInput('OUTPUTTYPE');
          }
          if (option == 'top connection' ||
              option == 'top+bottom connections') {
            if (!topExists) {
              block.appendInput(Blockly.INPUT_VALUE, 'TOPTYPE', 'Type')
                  .appendTitle('top type');
              block.moveInputBefore('TOPTYPE', 'COLOUR');
            }
          } else if (topExists) {
            block.removeInput('TOPTYPE');
          }
          if (option == 'bottom connection' ||
              option == 'top+bottom connections') {
            if (!bottomExists) {
              block.appendInput(Blockly.INPUT_VALUE, 'BOTTOMTYPE', 'Type')
                  .appendTitle('bottom type');
              block.moveInputBefore('BOTTOMTYPE', 'COLOUR');
            }
          } else if (bottomExists) {
            block.removeInput('BOTTOMTYPE');
          }
          this.setText(option);
        });
    this.appendInput(Blockly.DUMMY_INPUT, '')
        .appendTitle(dropdown, 'CONNECTIONS');
    this.appendInput(Blockly.INPUT_VALUE, 'COLOUR', 'Colour')
        .appendTitle('colour');
    /*
    this.appendInput(Blockly.INPUT_VALUE, 'TOOLLTIP', String)
        .appendTitle('tooltip');
    this.appendInput(Blockly.INPUT_VALUE, 'HELP', String)
        .appendTitle('help url');
    */
    this.setTooltip('');
  }
};

Blockly.Language.title_static = {
  // Text value.
  category: 'Title',
  init: function() {
    this.setColour(160);
    this.appendTitle('text');
    this.appendTitle(new Blockly.FieldTextInput(''), 'TEXT');
    this.setPreviousStatement(true, 'Title');
    this.setNextStatement(true, 'Title');
    this.setTooltip('');
  }
};

Blockly.Language.title_input = {
  // Text input.
  category: 'Title',
  init: function() {
    this.setColour(160);
    this.appendTitle('text input');
    this.appendTitle(new Blockly.FieldTextInput('default'), 'TEXT');
    this.appendTitle(',');
    this.appendTitle(new Blockly.FieldTextInput('NAME'), 'TITLENAME');
    this.setPreviousStatement(true, 'Title');
    this.setNextStatement(true, 'Title');
    this.setTooltip('');
  },
  onchange: function() {
    if (!this.workspace) {
      // Block has been deleted.
      return;
    }
    titleNameCheck(this);
  }
};

Blockly.Language.title_dropdown = {
  // Dropdown menu.
  category: 'Title',
  init: function() {
    this.setColour(160);
    this.appendTitle('dropdown');
    var input = this.appendInput(Blockly.DUMMY_INPUT, '');
    input.appendTitle(new Blockly.FieldTextInput('NAME'), 'TITLENAME');
    input = this.appendInput(Blockly.DUMMY_INPUT, 'OPTION0');
    input.appendTitle(new Blockly.FieldTextInput('option'), 'USER0');
    input.appendTitle(',');
    input.appendTitle(new Blockly.FieldTextInput('OPTIONNAME'), 'CPU0');
    input = this.appendInput(Blockly.DUMMY_INPUT, 'OPTION1');
    input.appendTitle(new Blockly.FieldTextInput('option'), 'USER1');
    input.appendTitle(',');
    input.appendTitle(new Blockly.FieldTextInput('OPTIONNAME'), 'CPU1');
    input = this.appendInput(Blockly.DUMMY_INPUT, 'OPTION2');
    input.appendTitle(new Blockly.FieldTextInput('option'), 'USER2');
    input.appendTitle(',');
    input.appendTitle(new Blockly.FieldTextInput('OPTIONNAME'), 'CPU2');
    this.setPreviousStatement(true, 'Title');
    this.setNextStatement(true, 'Title');
    this.setMutator(new Blockly.Mutator(['title_dropdown_option']));
    this.setTooltip('');
    this.optionCount_ = 3;
  },
  mutationToDom: function(workspace) {
    var container = document.createElement('mutation');
    container.setAttribute('options', this.optionCount_);
    return container;
  },
  domToMutation: function(container) {
    for (var x = 0; x < this.optionCount_; x++) {
      this.removeInput('OPTION' + x);
    }
    this.optionCount_ = window.parseInt(container.getAttribute('options'), 10);
    for (var x = 0; x < this.optionCount_; x++) {
      var input = this.appendInput(Blockly.DUMMY_INPUT, 'OPTION' + x);
      input.appendTitle(new Blockly.FieldTextInput('option'), 'USER' + x);
      input.appendTitle(',');
      input.appendTitle(new Blockly.FieldTextInput('OPTIONNAME'), 'CPU' + x);
    }
  },
  decompose: function(workspace) {
    var containerBlock = new Blockly.Block(workspace,
                                           'title_dropdown_container');
    containerBlock.initSvg();
    var connection = containerBlock.getInput('STACK').connection;
    for (var x = 0; x < this.optionCount_; x++) {
      var optionBlock = new Blockly.Block(workspace, 'title_dropdown_option');
      optionBlock.initSvg();
      connection.connect(optionBlock.previousConnection);
      connection = optionBlock.nextConnection;
    }
    return containerBlock;
  },
  compose: function(containerBlock) {
    // Disconnect all input blocks and destroy all inputs.
    for (var x = this.optionCount_ - 1; x >= 0; x--) {
      this.removeInput('OPTION' + x);
    }
    this.optionCount_ = 0;
    // Rebuild the block's inputs.
    var optionBlock = containerBlock.getInputTargetBlock('STACK');
    while (optionBlock) {
      var input = this.appendInput(Blockly.DUMMY_INPUT,
          'OPTION' + this.optionCount_);
      input.appendTitle(
          new Blockly.FieldTextInput(optionBlock.userData_ || 'option'),
          'USER' + this.optionCount_);
      input.appendTitle(',');
      input.appendTitle(
          new Blockly.FieldTextInput(optionBlock.cpuData_ || 'OPTIONNAME'),
          'CPU' + this.optionCount_);
      this.optionCount_++;
      optionBlock = optionBlock.nextConnection &&
          optionBlock.nextConnection.targetBlock();
    }
  },
  saveConnections: function(containerBlock) {
    // Store names and values for each option.
    var optionBlock = containerBlock.getInputTargetBlock('STACK');
    var x = 0;
    while (optionBlock) {
      optionBlock.userData_ = this.getTitleValue('USER' + x);
      optionBlock.cpuData_ = this.getTitleValue('CPU' + x);
      x++;
      optionBlock = optionBlock.nextConnection &&
          optionBlock.nextConnection.targetBlock();
    }
  },
  onchange: function() {
    if (!this.workspace) {
      // Block has been deleted.
      return;
    }
    if (this.optionCount_ < 1) {
      this.setWarningText('Drop down menu must\nhave at least one option.');
    } else {
      titleNameCheck(this);
    }
  }
};

Blockly.Language.title_dropdown_container = {
  // Container.
  init: function() {
    this.setColour(160);
    this.appendTitle('add options');
    this.appendInput(Blockly.NEXT_STATEMENT, 'STACK');
    this.setTooltip('');
    this.contextMenu = false;
  }
};

Blockly.Language.title_dropdown_option = {
  // Add option.
  init: function() {
    this.setColour(160);
    this.appendTitle('option');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
    this.contextMenu = false;
  }
};

Blockly.Language.title_variable = {
  // Dropdown for variables.
  category: 'Title',
  init: function() {
    this.setColour(160);
    this.appendTitle('variable');
    this.appendTitle(new Blockly.FieldTextInput('item'), 'TEXT');
    this.appendTitle(',');
    this.appendTitle(new Blockly.FieldTextInput('NAME'), 'TITLENAME');
    this.setPreviousStatement(true, 'Title');
    this.setNextStatement(true, 'Title');
    this.setTooltip('');
  },
  onchange: function() {
    if (!this.workspace) {
      // Block has been deleted.
      return;
    }
    titleNameCheck(this);
  }
};

Blockly.Language.title_image = {
  // Image.
  category: 'Title',
  init: function() {
    this.setColour(160);
    this.appendTitle('image');
    var src = 'http://www.gstatic.com/codesite/ph/images/star_on.gif';
    var input = this.appendInput(Blockly.DUMMY_INPUT, '');
    input.appendTitle(new Blockly.FieldTextInput(src), 'SRC');
    input = this.appendInput(Blockly.DUMMY_INPUT, '');
    input.appendTitle('width');
    input.appendTitle(new Blockly.FieldTextInput('15'), 'WIDTH');
    input.appendTitle('height');
    input.appendTitle(new Blockly.FieldTextInput('15'), 'HEIGHT');
    this.setPreviousStatement(true, 'Title');
    this.setNextStatement(true, 'Title');
    this.setTooltip('');
  }
};

Blockly.Language.input_value = {
  // Value input.
  category: 'Input',
  init: function() {
    this.setColour(210);
    this.appendTitle('value input');
    this.appendInput(Blockly.DUMMY_INPUT, '')
        .appendTitle(new Blockly.FieldTextInput('NAME'), 'INPUTNAME');
    this.appendInput(Blockly.NEXT_STATEMENT, 'TITLES', 'Title')
        .appendTitle('titles');
    this.appendInput(Blockly.INPUT_VALUE, 'TYPE', 'Type')
        .appendTitle('type');
    this.setPreviousStatement(true, 'Input');
    this.setNextStatement(true, 'Input');
    this.setTooltip('');
  },
  onchange: function() {
    if (!this.workspace) {
      // Block has been deleted.
      return;
    }
    inputNameCheck(this);
  }
};

Blockly.Language.input_statement = {
  // Statement input.
  category: 'Input',
  init: function() {
    this.setColour(210);
    this.appendTitle('statement input');
    this.appendInput(Blockly.DUMMY_INPUT, '')
        .appendTitle(new Blockly.FieldTextInput('NAME'), 'INPUTNAME');
    this.appendInput(Blockly.NEXT_STATEMENT, 'TITLES', 'Title')
        .appendTitle('titles');
    this.appendInput(Blockly.INPUT_VALUE, 'TYPE', 'Type')
        .appendTitle('type');
    this.setPreviousStatement(true, 'Input');
    this.setNextStatement(true, 'Input');
    this.setTooltip('');
  },
  onchange: function() {
    if (!this.workspace) {
      // Block has been deleted.
      return;
    }
    inputNameCheck(this);
  }
};

Blockly.Language.input_dummy = {
  // Dummy input.
  category: 'Input',
  init: function() {
    this.setColour(210);
    this.appendTitle('dummy input');
    this.appendInput(Blockly.NEXT_STATEMENT, 'TITLES', 'Title')
        .appendTitle('titles');
    this.setPreviousStatement(true, 'Input');
    this.setNextStatement(true, 'Input');
    this.setTooltip('');
  }
};

Blockly.Language.type_group = {
  // Group of types.
  category: 'Type',
  init: function() {
    this.setColour(230);
    this.appendTitle('any of');
    this.appendInput(Blockly.INPUT_VALUE, 'TYPE0', 'Type');
    this.appendInput(Blockly.INPUT_VALUE, 'TYPE1', 'Type');
    this.setOutput(true, 'Type');
    this.setMutator(new Blockly.Mutator(['type_group_item']));
    this.setTooltip('');
    this.typeCount_ = 2;
  },
  mutationToDom: function(workspace) {
    var container = document.createElement('mutation');
    container.setAttribute('types', this.typeCount_);
    return container;
  },
  domToMutation: function(container) {
    for (var x = 0; x < this.typeCount_; x++) {
      this.removeInput('TYPE' + x);
    }
    this.typeCount_ = window.parseInt(container.getAttribute('types'), 10);
    for (var x = 0; x < this.typeCount_; x++) {
      this.appendInput(Blockly.INPUT_VALUE, 'TYPE' + x, null);
    }
  },
  decompose: function(workspace) {
    var containerBlock = new Blockly.Block(workspace,
                                           'type_group_container');
    containerBlock.initSvg();
    var connection = containerBlock.getInput('STACK').connection;
    for (var x = 0; x < this.typeCount_; x++) {
      var typeBlock = new Blockly.Block(workspace, 'type_group_item');
      typeBlock.initSvg();
      connection.connect(typeBlock.previousConnection);
      connection = typeBlock.nextConnection;
    }
    return containerBlock;
  },
  compose: function(containerBlock) {
    // Disconnect all input blocks and destroy all inputs.
    for (var x = this.typeCount_ - 1; x >= 0; x--) {
      this.removeInput('TYPE' + x);
    }
    this.typeCount_ = 0;
    // Rebuild the block's inputs.
    var typeBlock = containerBlock.getInputTargetBlock('STACK');
    while (typeBlock) {
      var input = this.appendInput(Blockly.INPUT_VALUE,
                                   'TYPE' + this.typeCount_, null);
      // Reconnect any child blocks.
      if (typeBlock.valueConnection_) {
        input.connection.connect(typeBlock.valueConnection_);
      }
      this.typeCount_++;
      typeBlock = typeBlock.nextConnection &&
          typeBlock.nextConnection.targetBlock();
    }
  },
  saveConnections: function(containerBlock) {
    // Store a pointer to any connected child blocks.
    var typeBlock = containerBlock.getInputTargetBlock('STACK');
    var x = 0;
    while (typeBlock) {
      var input = this.getInput('TYPE' + x);
      typeBlock.valueConnection_ = input && input.connection.targetConnection;
      x++;
      typeBlock = typeBlock.nextConnection &&
          typeBlock.nextConnection.targetBlock();
    }
  }
};

Blockly.Language.type_group_container = {
  // Container.
  init: function() {
    this.setColour(230);
    this.appendTitle('add types');
    this.appendInput(Blockly.NEXT_STATEMENT, 'STACK');
    this.setTooltip('');
    this.contextMenu = false;
  }
};

Blockly.Language.type_group_item = {
  // Add type.
  init: function() {
    this.setColour(230);
    this.appendTitle('type');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
    this.contextMenu = false;
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

/**
 * Check to see if more than one title has this name.
 * Highly inefficient (On^2), but n is small.
 * @param {!Blockly.Block} referenceBlock Block to check.
 */
function titleNameCheck(referenceBlock) {
  var name = referenceBlock.getTitleValue('TITLENAME').toLowerCase();
  count = 0;
  var blocks = referenceBlock.workspace.getAllBlocks();
  for (var x = 0, block; block = blocks[x]; x++) {
    var otherName = block.getTitleValue('TITLENAME');
    if (otherName) {
      if (otherName.toLowerCase() == name) {
        count++;
      }
    }
  }
  var msg = (count > 1) ?
      'There are ' + count + ' title blocks\n with this name.' : null;
  referenceBlock.setWarningText(msg);
}

/**
 * Check to see if more than one input has this name.
 * Highly inefficient (On^2), but n is small.
 * @param {!Blockly.Block} referenceBlock Block to check.
 */
function inputNameCheck(referenceBlock) {
  var name = referenceBlock.getTitleValue('INPUTNAME').toLowerCase();
  count = 0;
  var blocks = referenceBlock.workspace.getAllBlocks();
  for (var x = 0, block; block = blocks[x]; x++) {
    var otherName = block.getTitleValue('INPUTNAME');
    if (otherName) {
      if (otherName.toLowerCase() == name) {
        count++;
      }
    }
  }
  var msg = (count > 1) ?
      'There are ' + count + ' input blocks\n with this name.' : null;
  referenceBlock.setWarningText(msg);
}
