/**
 * Visual Blocks Language
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
 * @fileoverview Control blocks for Blockly, modified for App Inventor
 * @author fraser@google.com (Neil Fraser)
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

/*
 Lyn's History: 
   [lyn, 11/29-30/12] 
   * Change forEach and forRange loops to take name as input text rather than via plug. 
   * For these blocks, add extra methods to support renaming. 
*/


if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.controls_if = {
  // If/elseif/else condition.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_IF_HELPURL,
  init: function() {
    this.setColour(120);
    this.appendValueInput('IF0')
        .setCheck(Boolean)
        .appendTitle(Blockly.LANG_CONTROLS_IF_MSG_IF);
    this.appendStatementInput('DO0')
        .appendTitle(Blockly.LANG_CONTROLS_IF_MSG_THEN);
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
    if(xmlElement.getAttribute('elseif') == null){
      this.elseifCount_ = 0;
    } else {
      this.elseifCount_ = window.parseInt(xmlElement.getAttribute('elseif'), 10);
    }
    
    this.elseCount_ = window.parseInt(xmlElement.getAttribute('else'), 10);
    for (var x = 1; x <= this.elseifCount_; x++) {
      this.appendValueInput('IF' + x)
          .setCheck(Boolean)
          .appendTitle(Blockly.LANG_CONTROLS_IF_MSG_ELSEIF);
      this.appendStatementInput('DO' + x)
          .appendTitle(Blockly.LANG_CONTROLS_IF_MSG_THEN);
    }
    if (this.elseCount_) {
      this.appendStatementInput('ELSE')
          .appendTitle(Blockly.LANG_CONTROLS_IF_MSG_ELSE);
    }
  },
  decompose: function(workspace) {
    var containerBlock = new Blockly.Block(workspace, 'controls_if_if');
    containerBlock.initSvg();
    var connection = containerBlock.getInput('STACK').connection;
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
          var ifInput = this.appendValueInput('IF' + this.elseifCount_)
              .setCheck(Boolean)
              .appendTitle(Blockly.LANG_CONTROLS_IF_MSG_ELSEIF);
          var doInput = this.appendStatementInput('DO' + this.elseifCount_);
          doInput.appendTitle(Blockly.LANG_CONTROLS_IF_MSG_THEN);
          // Reconnect any child blocks.
          if (clauseBlock.valueConnection_) {
            ifInput.connection.connect(clauseBlock.valueConnection_);
          }
          if (clauseBlock.statementConnection_) {
            doInput.connection.connect(clauseBlock.statementConnection_);
          }
          break;
        case 'controls_if_else':
          this.elseCount_++;
          var elseInput = this.appendStatementInput('ELSE');
          elseInput.appendTitle(Blockly.LANG_CONTROLS_IF_MSG_ELSE);
          // Reconnect any child blocks.
          if (clauseBlock.statementConnection_) {
            elseInput.connection.connect(clauseBlock.statementConnection_);
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
          clauseBlock.valueConnection_ =
              inputIf && inputIf.connection.targetConnection;
          clauseBlock.statementConnection_ =
              inputDo && inputDo.connection.targetConnection;
          x++;
          break;
        case 'controls_if_else':
          var inputDo = this.getInput('ELSE');
          clauseBlock.statementConnection_ =
              inputDo && inputDo.connection.targetConnection;
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
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_CONTROLS_IF_IF_TITLE_IF);
    this.appendStatementInput('STACK');
    this.setTooltip(Blockly.LANG_CONTROLS_IF_IF_TOOLTIP_1);
    this.contextMenu = false;
  }
};

Blockly.Language.controls_if_elseif = {
  // Else-If condition.
  init: function() {
    this.setColour(120);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF);
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
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_CONTROLS_IF_ELSE_TITLE_ELSE);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.LANG_CONTROLS_IF_ELSE_TOOLTIP_1);
    this.contextMenu = false;
  }
};

// [lyn, 01/15/2013] Remove DO C-sockets because now handled more modularly by DO-THEN-RETURN block. 
Blockly.Language.controls_choose = {
  // Choose.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.setOutput(true, null);
    this.appendValueInput('TEST').setCheck(Boolean).appendTitle('choose').appendTitle('test').setAlign(Blockly.ALIGN_RIGHT);
    // this.appendStatementInput('DO0').appendTitle('then-do').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('THENRETURN').appendTitle('then-return').setAlign(Blockly.ALIGN_RIGHT);
    // this.appendStatementInput('ELSE').appendTitle('else-do').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('ELSERETURN').appendTitle('else-return').setAlign(Blockly.ALIGN_RIGHT);
    /* Blockly.Language.setTooltip(this, 'If the condition being tested is true, the agent will '
       + 'run all the blocks attached to the \'then-do\' section and return the value attached '
       + 'to the \'then-return\'slot. Otherwise, the agent will run all blocks attached to '
       + 'the \'else-do\' section and return the value in the \'else-return\' slot.');
       */
    // [lyn, 01/15/2013] Edit description to be consistent with changes to slots. 
    Blockly.Language.setTooltip(this, 'If the condition being tested is true,'
       + 'return the result of evaluating the expression attached to the \'then-return\' slot;'
       + 'otherwise return the result of evaluating the expression attached to the \'else-return\' slot;'
       + 'at most one of the return slot expressions will be evaluated.');
  }
};

Blockly.Language.controls_forEach = {
  // For each loop.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    //this.setOutput(true, null);
    // [lyn, 11/29/12] Changed variable to be text input box that does renaming right (i.e., avoids variable capture)
    // Old code: 
    //   this.appendValueInput('VAR').appendTitle('for each').appendTitle('variable').setAlign(Blockly.ALIGN_RIGHT);
    //   this.appendStatementInput('DO').appendTitle('do').setAlign(Blockly.ALIGN_RIGHT);
    //   this.appendValueInput('LIST').setCheck(Array).appendTitle('in list').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('LIST')
        .setCheck(Array)
        .appendTitle("for each")
        .appendTitle(new Blockly.FieldTextInput("i", Blockly.LexicalVariable.renameParam), 'VAR')
        .appendTitle('in list')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO')
        .appendTitle('do');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Runs the blocks in the \'do\'  section for each item in '
        + 'the list.  Use the given variable name to refer to the current list item.');
  },
  getVars: function() {
    return [this.getTitleValue('VAR')];
  },
  blocksInScope: function() {
    var doBlock = this.getInputTargetBlock('DO');
    if (doBlock) {
      return [doBlock];
    } else {
      return [];
    }
  },
  declaredNames: function() {
    return [this.getTitleValue('VAR')];
  },
  renameVar: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getTitleValue('VAR'))) {
      this.setTitleValue(newName, 'VAR');
    }
  }
};

Blockly.Language.controls_forRange = {
  // For range.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    //this.setOutput(true, null);
    // Need to deal with variables here
    // [lyn, 11/30/12] Changed variable to be text input box that does renaming right (i.e., avoids variable capture)
    // Old code: 
    // this.appendValueInput('VAR').appendTitle('for range').appendTitle('variable').setAlign(Blockly.ALIGN_RIGHT);
    // this.appendValueInput('START').setCheck(Number).appendTitle('start').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('START')
        .setCheck(Array)
        .appendTitle("for range")
        .appendTitle(new Blockly.FieldTextInput("i", Blockly.LexicalVariable.renameParam), 'VAR')
        .appendTitle('start')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('END').setCheck(Number).appendTitle('end').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('STEP').setCheck(Number).appendTitle('step').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO').appendTitle('do').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Runs the blocks in the \'do\' section for each numeric '
        + 'value in the range from start to end, stepping the value each time.  Use the given '
        + 'variable name to refer to the current value.');
  },
  getVars: function() {
    return [this.getTitleValue('VAR')];
  },
  blocksInScope: function() {
    var doBlock = this.getInputTargetBlock('DO');
    if (doBlock) {
      return [doBlock];
    } else {
      return [];
    }
  },
  declaredNames: function() {
    return [this.getTitleValue('VAR')];
  },
  renameVar: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getTitleValue('VAR'))) {
      this.setTitleValue(newName, 'VAR');
    }
  }
};

Blockly.Language.controls_while = {
  // While condition.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendValueInput('TEST').setCheck(Boolean).appendTitle('while').appendTitle('test').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO').appendTitle('do').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Runs the blocks in the \'do\' section while the test is '
        + 'true.');
  }
};

// [lyn, 01/15/2013] Added
Blockly.Language.controls_do_then_return = {
  // String length.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  init: function() {
    this.setColour(120);
    this.appendStatementInput('STM')
        .appendTitle("do");
    this.appendValueInput('VALUE')
        .appendTitle("then-return")
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setOutput(true, null);
  }
};

// [lyn, 01/15/2013] Added
Blockly.Language.controls_eval_but_ignore = {
  category: Blockly.LANG_CATEGORY_CONTROLS,
  init: function() {
    this.setColour(120);
    this.appendValueInput('VALUE')
        .appendTitle("eval-but-ignore-result");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  }
};

// [lyn, 01/15/2013] Added 
Blockly.Language.controls_do_nothing = {
  // A do-nothing statement, like "skip" in many languages, or nop in assembly code
  category: Blockly.LANG_CATEGORY_CONTROLS,
  init: function() {
    this.setColour(120);
    this.appendDummyInput()
        .appendTitle("do-nothing");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  }
};

// [lyn, 01/15/2013] Added
Blockly.Language.controls_nothing = {
  // Expression for the nothing value
  category: Blockly.LANG_CATEGORY_CONTROLS,
  init: function() {
    this.setColour(120);
    this.appendDummyInput()
        .appendTitle("nothing");
    this.setOutput(true, null);
  }
};


Blockly.Language.controls_openAnotherScreen = {
  // Open another screen
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendValueInput('SCREEN').appendTitle('open another screen').appendTitle('screenName').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, 'Opens a new screen in a multiple screen app.');
  }
};

Blockly.Language.controls_openAnotherScreenWithStartValue = {
  // Open another screen with start value
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendValueInput('SCREENNAME').setCheck(String).appendTitle('open another screen with start value').appendTitle('screenName').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('STARTVALUE').appendTitle('startValue').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, 'Opens a new screen in a multiple screen app and passes the '
        + 'start value to that screen.');
  }
};

Blockly.Language.controls_getStartValue = {
  // Get start value
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.setOutput(true, null);
    this.appendDummyInput().appendTitle('get start value');
    this.appendValueInput('STARTVALUE').appendTitle('startValue').setAlign(Blockly.ALIGN_RIGHT);
    Blockly.Language.setTooltip(this, 'Returns the value that was passed to this screen when it '
        + 'was opened, typically by another screen in a multiple-screen app. If no value was '
        + 'passed, returns the empty text.');
  }
};

Blockly.Language.controls_closeScreen = {
  // Close screen
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendDummyInput().appendTitle('close screen');
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, 'Close the current screen');
  }
};

Blockly.Language.controls_closeScreenWithValue = {
  // Close screen with value
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendValueInput('SCREEN').appendTitle('close screen with value').appendTitle('result').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, 'Closes the current screen and returns a result to the '
        + 'screen that opened this one.');
  }
};

Blockly.Language.controls_closeApplication = {
  // Close application
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendDummyInput().appendTitle('close application');
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, 'Closes all screens in this app and stops the app.');
  }
};

Blockly.Language.controls_getPlainStartText = {
  // Get plain start text
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.setOutput(true, String);
    this.appendDummyInput().appendTitle('get plain start text');
    Blockly.Language.setTooltip(this, 'Returns the plain text that was passed to this screen when '
        + 'it was started by another app. If no value was passed, returns the empty text. For '
        + 'multiple screen apps, use get start value rather than get plain start text.');
  }
};

Blockly.Language.controls_closeScreenWithPlainText = {
  // Close screen with plain text
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendValueInput('TEXT').setCheck(String).appendTitle('close screen with plain text').appendTitle('text').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, 'Closes the current screen and returns text to the app that '
        + 'opened this one. For multiple screen apps, use close screen with value rather than '
        + 'close screen with plain text.');
  }
};
