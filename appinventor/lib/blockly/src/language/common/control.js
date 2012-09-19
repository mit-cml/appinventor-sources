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

Blockly.Language.controls_if = {
  // If/elseif/else condition.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_IF_HELPURL,
  init: function() {
    this.setColour(120);
    this.appendInput(this.MSG_IF, Blockly.INPUT_VALUE, 'IF0', Boolean);
    this.appendInput(this.MSG_THEN, Blockly.NEXT_STATEMENT, 'DO0');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setMutator(new Blockly.Mutator(['controls_if_elseif',
                                         'controls_if_else']));
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      if (!thisBlock.elseifCount_ && !thisBlock.elseCount_) {
        return Blockly.LANG_CONTROLS_IF_TOOLTIP_1;
      } else if (!thisBlock.elseifCount_ && thisBlock.elseCount_) {
        return Blockly.LANG_CONTROLS_IF_TOOLTIP_2;
      } else if (thisBlock.elseifCount_ && !thisBlock.elseCount_) {
        return Blockly.LANG_CONTROLS_IF_TOOLTIP_3;
      } else if (thisBlock.elseifCount_ && thisBlock.elseCount_) {
        return Blockly.LANG_CONTROLS_IF_TOOLTIP_4;
      }
      return '';
    });
    this.elseifCount_ = 0;
    this.elseCount_ = 0;
  },
  MSG_IF: Blockly.LANG_CONTROLS_IF_MSG_IF,
  MSG_ELSEIF: Blockly.LANG_CONTROLS_IF_MSG_ELSEIF,
  MSG_ELSE: Blockly.LANG_CONTROLS_IF_MSG_ELSE,
  MSG_THEN: Blockly.LANG_CONTROLS_IF_MSG_THEN,
  mutationToDom: function() {
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
  domToMutation: function(xmlElement) {
    this.elseifCount_ = window.parseInt(xmlElement.getAttribute('elseif'), 10);
    this.elseCount_ = window.parseInt(xmlElement.getAttribute('else'), 10);
    for (var x = 1; x <= this.elseifCount_; x++) {
      this.appendInput(this.MSG_ELSEIF, Blockly.INPUT_VALUE, 'IF' + x, Boolean);
      this.appendInput(this.MSG_THEN, Blockly.NEXT_STATEMENT, 'DO' + x);
    }
    if (this.elseCount_) {
      this.appendInput(this.MSG_ELSE, Blockly.NEXT_STATEMENT, 'ELSE');
    }
  },
  decompose: function(workspace) {
    var containerBlock = new Blockly.Block(workspace, 'controls_if_if');
    containerBlock.initSvg();
    var connection = containerBlock.inputList[0];
    for (var x = 1; x <= this.elseifCount_; x++) {
      var elseifBlock = new Blockly.Block(workspace, 'controls_if_elseif');
      elseifBlock.initSvg();
      connection.connect(elseifBlock.previousConnection);
      connection = elseifBlock.nextConnection;
    }
    if (this.elseCount_) {
      var elseBlock = new Blockly.Block(workspace, 'controls_if_else');
      elseBlock.initSvg();
      connection.connect(elseBlock.previousConnection);
    }
    return containerBlock;
  },
  compose: function(containerBlock) {
    // Disconnect the else input blocks and destroy the inputs.
    if (this.elseCount_) {
      this.removeInput('ELSE');
    }
    this.elseCount_ = 0;
    // Disconnect all the elseif input blocks and destroy the inputs.
    for (var x = this.elseifCount_; x > 0; x--) {
      this.removeInput('IF' + x);
      this.removeInput('DO' + x);
    }
    this.elseifCount_ = 0;
    // Rebuild the block's optional inputs.
    var clauseBlock = containerBlock.getInputTargetBlock('STACK');
    while (clauseBlock) {
      switch (clauseBlock.type) {
        case 'controls_if_elseif':
          this.elseifCount_++;
          var ifInput = this.appendInput(this.MSG_ELSEIF, Blockly.INPUT_VALUE,
              'IF' + this.elseifCount_, Boolean);
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
          var elseInput = this.appendInput(this.MSG_ELSE,
              Blockly.NEXT_STATEMENT, 'ELSE');
          // Reconnect any child blocks.
          if (clauseBlock.statementInput_) {
            elseInput.connect(clauseBlock.statementInput_);
          }
          break;
        default:
          throw 'Unknown block type.';
      }
      clauseBlock = clauseBlock.nextConnection &&
          clauseBlock.nextConnection.targetBlock();
    }
  },
  saveConnections: function(containerBlock) {
    // Store a pointer to any connected child blocks.
    var clauseBlock = containerBlock.getInputTargetBlock('STACK');
    var x = 1;
    while (clauseBlock) {
      switch (clauseBlock.type) {
        case 'controls_if_elseif':
          var inputIf = this.getInput('IF' + x);
          var inputDo = this.getInput('DO' + x);
          clauseBlock.valueInput_ = inputIf && inputIf.targetConnection;
          clauseBlock.statementInput_ = inputDo && inputDo.targetConnection;
          x++;
          break;
        case 'controls_if_else':
          var inputDo = this.getInput('ELSE');
          clauseBlock.statementInput_ = inputDo && inputDo.targetConnection;
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
    this.appendTitle(Blockly.LANG_CONTROLS_IF_IF_TITLE_IF);
    this.appendInput('', Blockly.NEXT_STATEMENT, 'STACK');
    this.setTooltip(Blockly.LANG_CONTROLS_IF_IF_TOOLTIP_1);
    this.contextMenu = false;
  }
};

Blockly.Language.controls_if_elseif = {
  // Else-If condition.
  init: function() {
    this.setColour(120);
    this.appendTitle(Blockly.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.LANG_CONTROLS_IF_ELSEIF_TOOLTIP_1);
    this.contextMenu = false;
  }
};

Blockly.Language.controls_if_else = {
  // Else condition.
  init: function() {
    this.setColour(120);
    this.appendTitle(Blockly.LANG_CONTROLS_IF_ELSE_TITLE_ELSE);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.LANG_CONTROLS_IF_ELSE_TOOLTIP_1);
    this.contextMenu = false;
  }
};

Blockly.Language.controls_whileUntil = {
  // Do while/until loop.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_WHILEUNTIL_HELPURL,
  init: function() {
    this.setColour(120);
    this.appendTitle(Blockly.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT);
    var dropdown = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendTitle(dropdown, 'MODE');
    this.appendInput('', Blockly.INPUT_VALUE, 'BOOL', Boolean);
    this.appendInput(Blockly.LANG_CONTROLS_WHILEUNTIL_INPUT_DO,
                     Blockly.NEXT_STATEMENT, 'DO');
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
    [[Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE, 'WHILE'],
     [Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL, 'UNTIL']];

Blockly.Language.controls_whileUntil.TOOLTIPS = {
  WHILE: Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE,
  UNTIL: Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL
};

Blockly.Language.controls_for = {
  // For loop.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_FOR_HELPURL,
  init: function() {
    this.setColour(120);
    this.appendTitle(Blockly.LANG_CONTROLS_FOR_TITLE_COUNT);
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

Blockly.Language.controls_forEach = {
  // For each loop.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_FOREACH_HELPURL,
  init: function() {
    this.setColour(120);
    this.appendTitle(Blockly.LANG_CONTROLS_FOREACH_TITLE_FOREACH);
    this.appendInput(Blockly.LANG_CONTROLS_FOREACH_INPUT_ITEM,
        Blockly.LOCAL_VARIABLE, 'VAR').setText(
        Blockly.Variables.generateUniqueName());
    this.appendInput(Blockly.LANG_CONTROLS_FOREACH_INPUT_INLIST,
        Blockly.INPUT_VALUE, 'LIST', Array);
    this.appendInput(Blockly.LANG_CONTROLS_FOREACH_INPUT_DO,
        Blockly.NEXT_STATEMENT, 'DO');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return Blockly.LANG_CONTROLS_FOREACH_TOOLTIP_1.replace('%1',
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

Blockly.Language.controls_flow_statements = {
  // Flow statements: continue, break.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL,
  init: function() {
    this.setColour(120);
    var dropdown = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendTitle(dropdown, 'FLOW');
    this.appendTitle(Blockly.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP);
    this.setPreviousStatement(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getTitleValue('FLOW');
      return Blockly.Language.controls_flow_statements.TOOLTIPS[op];
    });
  },
  onchange: function() {
    if (!this.workspace) {
      // Block has been deleted.
      return;
    }
    var legal = false;
    // Is the block nested in a control statement?
    var block = this;
    do {
      if (block.type == 'controls_forEach' ||
          block.type == 'controls_for' ||
          block.type == 'controls_whileUntil') {
        legal = true;
        break;
      }
      block = block.getSurroundParent();
    } while (block);
    if (legal) {
      this.setWarningText(null);
    } else {
      this.setWarningText(Blockly.LANG_CONTROLS_FLOW_STATEMENTS_WARNING);
    }
  }
};

Blockly.Language.controls_flow_statements.OPERATORS =
    [[Blockly.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK, 'BREAK'],
     [Blockly.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE, 'CONTINUE']];

Blockly.Language.controls_flow_statements.TOOLTIPS = {
  BREAK: Blockly.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK,
  CONTINUE: Blockly.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE
};
