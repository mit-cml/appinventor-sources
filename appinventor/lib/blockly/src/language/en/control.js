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
  // If/elseif/else condition.
  category: 'Control',
  helpUrl: 'http://code.google.com/p/google-blockly/wiki/If_Then',
  init: function() {
    this.setColour(120);
    this.appendInput(this.MSG_IF, Blockly.INPUT_VALUE, 'IF0');
    this.appendInput(this.MSG_THEN, Blockly.NEXT_STATEMENT, 'DO0');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setMutator(new Blockly.Mutator(this,
        ['controls_if_elseif', 'controls_if_else']));
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      if (!thisBlock.elseifCount_ && !thisBlock.elseCount_) {
        return 'If a value is true, then do some statements.';
      } else if (!thisBlock.elseifCount_ && thisBlock.elseCount_) {
        return 'If a value is true, then do the first block of statements.\n' +
               'Otherwise, do the second block of statements.';
      } else if (thisBlock.elseifCount_ && !thisBlock.elseCount_) {
        return 'If the first value is true, then do the first block of statements.\n' +
               'Otherwise, if the second value is true, do the second block of statements.';
      } else if (thisBlock.elseifCount_ && thisBlock.elseCount_) {
        return 'If the first value is true, then do the first block of statements.\n' +
               'Otherwise, if the second value is true, do the second block of statements.\n' +
               'If none of the values are true, do the last block of statements.';
      }
      return '';
    });
    this.elseifCount_ = 0;
    this.elseCount_ = 0;
  },
  MSG_IF: 'if',
  MSG_ELSEIF: 'else if',
  MSG_ELSE: 'else',
  MSG_THEN: 'then',
  mutationToDom: function(workspace) {
    if (!this.elseifCount_ && !this.elseCount_) {
      return null;
    }
    var container = document.createElement('mutation');
    if (this.elseifCount_) {
      container.setAttribute('elseif', this.elseifCount_);
    }
    if (this.elseCount_) {
      container.setAttribute('else', 1);
    }
    return container;
  },
  domToMutation: function(container) {
    this.elseifCount_ = window.parseInt(container.getAttribute('elseif'), 10);
    this.elseCount_ = window.parseInt(container.getAttribute('else'), 10);
    for (var x = 1; x <= this.elseifCount_; x++) {
      this.appendInput(this.MSG_ELSEIF, Blockly.INPUT_VALUE, 'IF' + x);
      this.appendInput(this.MSG_THEN, Blockly.NEXT_STATEMENT, 'DO' + x);
    }
    if (this.elseCount_) {
      this.appendInput(this.MSG_ELSE, Blockly.NEXT_STATEMENT, 'ELSE');
    }
  },
  decompose: function(workspace) {
    var ifBlock = new Blockly.Block(workspace, 'controls_if_if');
    ifBlock.editable = false;
    ifBlock.initSvg();
    var connection = ifBlock.inputList[0];
    for (var x = 1; x <= this.elseifCount_; x++) {
      var elseifBlock = new Blockly.Block(workspace, 'controls_if_elseif');
      elseifBlock.initSvg();
      // Store a pointer to any connected blocks.
      elseifBlock.valueInput_ = this.getInput('IF' + x).targetConnection;
      elseifBlock.statementInput_ = this.getInput('DO' + x).targetConnection;
      connection.connect(elseifBlock.previousConnection);
      connection = elseifBlock.nextConnection;
    }
    if (this.elseCount_) {
      var elseBlock = new Blockly.Block(workspace, 'controls_if_else');
      elseBlock.initSvg();
      elseBlock.statementInput_ = this.getInput('ELSE').targetConnection;
      connection.connect(elseBlock.previousConnection);
    }
    return ifBlock;
  },
  compose: function(ifBlock) {
    // Disconnect all the elseif input blocks and destroy the inputs.
    for (var x = 1; x <= this.elseifCount_; x++) {
      this.removeInput('IF' + x);
      this.removeInput('DO' + x);
    }
    // Disconnect the else input blocks and destroy the inputs.
    if (this.elseCount_) {
      this.removeInput('ELSE');
    }
    this.elseifCount_ = 0;
    this.elseCount_ = 0;
    // Rebuild the block's optional inputs.
    var clauseBlock = ifBlock.getInputTargetBlock('STACK');
    while (clauseBlock) {
      switch (clauseBlock.type) {
        case 'controls_if_elseif':
          this.elseifCount_++;
          var ifInput = this.appendInput(this.MSG_ELSEIF, Blockly.INPUT_VALUE,
              'IF' + this.elseifCount_);
          var doInput = this.appendInput(this.MSG_THEN, Blockly.NEXT_STATEMENT,
              'DO' + this.elseifCount_);
          // Reconnect any child blocks.
          if (clauseBlock.valueInput_) {
            ifInput.connect(clauseBlock.valueInput_);
          }
          if (clauseBlock.statementInput_) {
            doInput.connect(clauseBlock.statementInput_);
          }
          break;
        case 'controls_if_else':
          this.elseCount_++;
          this.appendInput(this.MSG_ELSE, Blockly.NEXT_STATEMENT, 'ELSE');
          // Reconnect any child blocks.
          if (clauseBlock.statementInput_) {
            this.inputList[x].connect(clauseBlock.statementInput_);
          }
          break;
        default:
          throw 'Unknown block type.';
      }
      clauseBlock = clauseBlock.nextConnection &&
          clauseBlock.nextConnection.targetBlock();
    }
  }
};

Blockly.Language.controls_if_if = {
  // If condition.
  init: function() {
    this.setColour(120);
    this.appendTitle('if');
    this.appendInput('', Blockly.NEXT_STATEMENT, 'STACK');
    this.setTooltip('Add, remove, or reorder sections\n' +
                    'to reconfigure this if block.');
    this.contextMenu = false;
  }
};

Blockly.Language.controls_if_elseif = {
  // Else-If condition.
  init: function() {
    this.setColour(120);
    this.appendTitle('else if');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Add a condition to the if block.');
    this.contextMenu = false;
  }
};

Blockly.Language.controls_if_else = {
  // Else condition.
  init: function() {
    this.setColour(120);
    this.appendTitle('else');
    this.setPreviousStatement(true);
    this.setTooltip('Add a final, catch-all condition to the if block.');
    this.contextMenu = false;
  }
};

Blockly.Language.controls_whileUntil = {
  // Do while/until loop.
  category: 'Control',
  helpUrl: 'http://code.google.com/p/google-blockly/wiki/Repeat',
  init: function() {
    this.setColour(120);
    this.appendTitle('repeat');
    var dropdown = new Blockly.FieldDropdown(function() {
      return Blockly.Language.controls_whileUntil.OPERATORS;
    });
    this.appendTitle(dropdown, 'MODE');
    this.appendInput('', Blockly.INPUT_VALUE, 'BOOL');
    this.appendInput('do', Blockly.NEXT_STATEMENT, 'DO');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getTitleValue('MODE');
      return Blockly.Language.controls_whileUntil.TOOLTIPS[op];
    });
  }
};

Blockly.Language.controls_whileUntil.OPERATORS =
    [['while', 'WHILE'],
     ['until', 'UNTIL']];

Blockly.Language.controls_whileUntil.TOOLTIPS = {
  WHILE: 'While a value is true, then do some statements.',
  UNTIL: 'While a value is false, then do some statements.'
};

Blockly.Language.controls_for = {
  // For loop.
  category: 'Control',
  helpUrl: 'http://en.wikipedia.org/wiki/For_loop',
  init: function() {
    this.setColour(120);
    this.appendTitle('count');
    this.appendInput('with', Blockly.LOCAL_VARIABLE, 'VAR').setText('x');
    this.appendInput('from', Blockly.INPUT_VALUE, 'FROM');
    this.appendInput('to', Blockly.INPUT_VALUE, 'TO');
    this.appendInput('do', Blockly.NEXT_STATEMENT, 'DO');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return 'Count from a start number to an end number.\n' +
          'For each count, set the current count number to\n' +
          'variable "' + thisBlock.getInputVariable('VAR') + '", and then do some statements.';
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

Blockly.Language.controls_forEach = {
  // For each loop.
  category: 'Control',
  helpUrl: 'http://en.wikipedia.org/wiki/For_loop',
  init: function() {
    this.setColour(120);
    this.appendTitle('for each');
    this.appendInput('item', Blockly.LOCAL_VARIABLE, 'VAR').setText('x');
    this.appendInput('in list', Blockly.INPUT_VALUE, 'LIST');
    this.appendInput('do', Blockly.NEXT_STATEMENT, 'DO');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return 'For each item in a list, set the item to\nvariable "' +
          thisBlock.getInputVariable('VAR') + '", and then do some statements.';
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


Blockly.Language.controls_flow_statements = {
  // Flow statements: continue, break.
  category: 'Control',
  helpUrl: 'http://en.wikipedia.org/wiki/Control_flow',
  init: function() {
    this.setColour(120);
    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(function() {
      return Blockly.Language.controls_flow_statements.OPERATORS;
    });
    this.appendTitle(dropdown, 'FLOW');
    this.appendTitle('of loop');
    this.setPreviousStatement(true);
    this.setTooltip(function() {
      var op = thisBlock.getTitleValue('FLOW');
      return Blockly.Language.controls_flow_statements.TOOLTIPS[op];
    });
  }
};

Blockly.Language.controls_flow_statements.OPERATORS =
    [['break out', 'BREAK'], ['continue with next iteration', 'CONTINUE']];

Blockly.Language.controls_flow_statements.TOOLTIPS = {
  BREAK: 'Break out of the containing loop.',
  CONTINUE: 'Skip the rest of this loop, and\ncontinue with the next iteration.'
};
