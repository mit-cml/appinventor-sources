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
  helpUrl: 'http://en.wikipedia.org/wiki/Conditional_(programming)',
  init: function() {
    this.setColour('purple');
    this.addInput(this.MSG_IF, '', Blockly.INPUT_VALUE);
    this.addInput(this.MSG_THEN, '', Blockly.NEXT_STATEMENT);
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
        return 'If a value is true, then do the first block of statements.\nOtherwise, do the second block of statements.';
      } else if (thisBlock.elseifCount_ && !thisBlock.elseCount_) {
        return 'If the first value is true, then do the first block of statements.\nOtherwise, if the second value is true, do the second block of statements.';
      } else if (thisBlock.elseifCount_ && thisBlock.elseCount_) {
        return 'If the first value is true, then do the first block of statements.\nOtherwise, if the second value is true, do the second block of statements.\nIf none of the values are true, do the last block of statements.';
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
    for (var x = 0; x < this.elseifCount_; x++) {
      this.addInput(this.MSG_ELSEIF, '', Blockly.INPUT_VALUE);
      this.addInput(this.MSG_THEN, '', Blockly.NEXT_STATEMENT);
    }
    if (this.elseCount_) {
      this.addInput(this.MSG_ELSE, '', Blockly.NEXT_STATEMENT);
    }
  },
  decompose: function(workspace) {
    var ifBlock = new Blockly.Block(workspace, 'controls_if_if');
    ifBlock.editable = false;
    var connection = ifBlock.inputList[0];
    var x = 0;
    for (; x < this.elseifCount_; x++) {
      var elseifBlock = new Blockly.Block(workspace, 'controls_if_elseif');
      // Store a pointer to any connected blocks.
      elseifBlock.valueInput_ = this.inputList[2 + (x * 2)].targetConnection;
      elseifBlock.statementInput_ = this.inputList[3 + (x * 2)].targetConnection;
      connection.connect(elseifBlock.previousConnection);
      connection = elseifBlock.nextConnection;
    }
    if (this.elseCount_) {
      var elseBlock = new Blockly.Block(workspace, 'controls_if_else');
      elseBlock.statementInput_ = this.inputList[2 + (x * 2)].targetConnection;
      connection.connect(elseBlock.previousConnection);
    }
    return ifBlock;
  },
  compose: function(ifBlock) {
    // Disconnect all but the first two input blocks.
    for (var x = 2; x < this.inputList.length; x++) {
      var child = this.inputList[x].targetBlock();
      if (child) {
        child.setParent(null);
      }
    }
    // Destroy all optional inputs.
    while (this.inputList.length > 2) {
      this.removeInput(2);
    }
    this.elseifCount_ = 0;
    this.elseCount_ = 0;
    // Rebuild the block's optional inputs.
    var clauseBlock = ifBlock.getStatementInput(0);
    var x = 2;
    while (clauseBlock) {
      switch (clauseBlock.type) {
        case 'controls_if_elseif':
          this.elseifCount_++;
          this.addInput(this.MSG_ELSEIF, '', Blockly.INPUT_VALUE);
          this.addInput(this.MSG_THEN, '', Blockly.NEXT_STATEMENT);
          // Reconnect any child blocks.
          if (clauseBlock.valueInput_) {
            this.inputList[x].connect(clauseBlock.valueInput_);
          }
          x++;
          if (clauseBlock.statementInput_) {
            this.inputList[x].connect(clauseBlock.statementInput_);
          }
          x++;
          break;
        case 'controls_if_else':
          this.elseCount_++;
          this.addInput(this.MSG_ELSE, '', Blockly.NEXT_STATEMENT);
          // Reconnect any child blocks.
          if (clauseBlock.statementInput_) {
            this.inputList[x].connect(clauseBlock.statementInput_);
          }
          x++;
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
    this.setColour('purple');
    this.addTitle('if');
    this.addInput('', '', Blockly.NEXT_STATEMENT);
    this.setTooltip('Add, remove, or reorder sections to reconfigure this if block.');
    this.contextMenu = false;
  }
};

Blockly.Language.controls_if_elseif = {
  // Else-If condition.
  init: function() {
    this.setColour('purple');
    this.addTitle('else if');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Add a condition to the if block.');
    this.contextMenu = false;
  }
};

Blockly.Language.controls_if_else = {
  // Else condition.
  init: function() {
    this.setColour('purple');
    this.addTitle('else');
    this.setPreviousStatement(true);
    this.setTooltip('Add a final, catch-all condition to the if block.');
    this.contextMenu = false;
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
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      switch (thisBlock.getTitleText(1)) {
        case Blockly.Language.controls_whileUntil.MSG_WHILE:
          return 'While a value is true, then do some statements.';
        case Blockly.Language.controls_whileUntil.MSG_UNTIL:
          return 'While a value is false, then do some statements.';
      }
      return '';
    });
  },
  MSG_WHILE: 'while',
  MSG_UNTIL: 'until'
};

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
    this.setTooltip('Count from a start number to an end number.\nFor each count, set the current count number to\na variable, and then do some statements.');
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
    this.setTooltip('For each item in a list, set the item to a\nvariable, and then do some statements.');
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
