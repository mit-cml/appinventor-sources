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
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('IF0')
        .setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT))
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
    this.warnings = [{name:"checkEmptySockets",sockets:[{baseName:"IF"},{baseName:"DO"}]}];
    this.appendCollapsedInput().appendTitle(Blockly.LANG_CONTROLS_IF_MSG_IF, 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
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
          .setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT))
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
              .setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT))
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
  },
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_IF_IF_TITLE_IF }]
};

Blockly.Language.controls_if_if = {
  // If condition.
  init: function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_CONTROLS_IF_IF_TITLE_IF);
    this.appendStatementInput('STACK');
    this.setTooltip(Blockly.LANG_CONTROLS_IF_IF_TOOLTIP);
    this.contextMenu = false;
  }
};

Blockly.Language.controls_if_elseif = {
  // Else-If condition.
  init: function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.LANG_CONTROLS_IF_ELSEIF_TOOLTIP);
    this.contextMenu = false;
  }
};

Blockly.Language.controls_if_else = {
  // Else condition.
  init: function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_CONTROLS_IF_ELSE_TITLE_ELSE);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.LANG_CONTROLS_IF_ELSE_TOOLTIP);
    this.contextMenu = false;
  }
};

Blockly.Language.controls_forRange = {
  // For range.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_FORRANGE_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    //this.setOutput(true, null);
    // Need to deal with variables here
    // [lyn, 11/30/12] Changed variable to be text input box that does renaming right (i.e., avoids variable capture)
    // Old code:
    // this.appendValueInput('VAR').appendTitle('for range').appendTitle('variable').setAlign(Blockly.ALIGN_RIGHT);
    // this.appendValueInput('START').setCheck(Number).appendTitle('start').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('START')
        .setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT))
        .appendTitle("for range")
        .appendTitle(new Blockly.FieldTextInput("i", Blockly.LexicalVariable.renameParam), 'VAR')
        .appendTitle('start')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('END').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('end').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('STEP').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('step').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO').appendTitle('do').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_FORRANGE_TOOLTIP);
    this.appendCollapsedInput().appendTitle('for range', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
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
  },
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_FORRANGE_INPUT_ITEM }]
};

Blockly.Language.controls_forEach = {
  // For each loop.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_FOREACH_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    //this.setOutput(true, null);
    // [lyn, 11/29/12] Changed variable to be text input box that does renaming right (i.e., avoids variable capture)
    // Old code: 
    // this.appendValueInput('VAR').appendTitle('for range').appendTitle('variable').setAlign(Blockly.ALIGN_RIGHT);
    // this.appendValueInput('START').setCheck(Number).appendTitle('start').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('LIST')
        .setCheck(Blockly.Language.YailTypeToBlocklyType("list",Blockly.Language.INPUT))
        .appendTitle("for each")
        .appendTitle(new Blockly.FieldTextInput("i", Blockly.LexicalVariable.renameParam), 'VAR')
        .appendTitle('in list')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO')
        .appendTitle('do');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_FOREACH_TOOLTIP);
    this.appendCollapsedInput().appendTitle('for each', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
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
  },
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_FOREACH_INPUT_ITEM }]
};


Blockly.Language.for_lexical_variable_get = {
  // Variable getter.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_GET_HELPURL,
  init: function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.fieldVar_ = new Blockly.FieldLexicalVariable(" ");
    this.fieldVar_.setBlock(this);
    this.appendDummyInput()
        .appendTitle("get")
        .appendTitle(this.fieldVar_, 'VAR');
    this.setOutput(true, null);
    this.setTooltip(Blockly.LANG_VARIABLES_GET_TOOLTIP);
    this.errors = [{name:"checkIsInDefinition"},{name:"checkDropDownContainsValidValue",dropDowns:["VAR"]}]
    this.appendCollapsedInput().appendTitle('get', 'COLLAPSED_TEXT');
  },
  getVars: function() {
    return [this.getTitleValue('VAR')];
  },
  onchange: function() {
     // [lyn, 11/10/12] Checks if parent has changed. If so, checks if curent variable name
     //    is still in scope. If so, keeps it as is; if not, changes to ???
     //    *** NEED TO MAKE THIS BEHAVIOR BETTER!
    if (this.fieldVar_) {
       var currentName = this.fieldVar_.getText();
       var nameList = this.fieldVar_.getNamesInScope();
       var cachedParent = this.fieldVar_.getCachedParent();
       var currentParent = this.fieldVar_.getBlock().getParent();
       // [lyn, 11/10/12] Allow current name to stay if block moved to workspace in "untethered" way.
       //   Only changed to ??? if tether an untethered block.
       if (currentParent != cachedParent) {
         this.fieldVar_.setCachedParent(currentParent);
         if  (currentParent != null) {
           for (var i = 0; i < nameList.length; i++ ) {
             if (nameList[i] === currentName) {
               return; // no change
             }
           }
           // Only get here if name not in list
           this.fieldVar_.setText(" ");
         }
       }
    }
    Blockly.WarningHandler.checkErrors.call(this);
  },
  renameLexicalVar: function(oldName, newName) {
    // console.log("Renaming lexical variable from " + oldName + " to " + newName);
    if (oldName === this.getTitleValue('VAR')) {
        this.setTitleValue(newName, 'VAR');
    }
  }
};


Blockly.Language.controls_while = {
  // While condition.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_WHILE_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('TEST').setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT)).appendTitle('while').appendTitle('test').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO').appendTitle('do').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_WHILE_TOOLTIP);
    this.appendCollapsedInput().appendTitle('while', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_WHILE_TITLE }]
};


// [lyn, 01/15/2013] Remove DO C-sockets because now handled more modularly by DO-THEN-RETURN block.
Blockly.Language.controls_choose = {
  // Choose.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_CHOOSE_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.setOutput(true, null);
    this.appendValueInput('TEST').setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT)).appendTitle('choose').appendTitle('test').setAlign(Blockly.ALIGN_RIGHT);
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
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_CHOOSE_TOOLTIP);
    this.appendCollapsedInput().appendTitle('choose', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_CHOOSE_TITLE }]
};

// [lyn, 01/15/2013] Added
Blockly.Language.controls_do_then_return = {
  // String length.
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_DO_THEN_RETURN_HELPURL,
  init: function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendStatementInput('STM')
        .appendTitle("do");
    this.appendValueInput('VALUE')
        .appendTitle("then-return")
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setOutput(true, null);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP);
    this.appendCollapsedInput().appendTitle('do then-return', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO }]
};

// [lyn, 01/15/2013] Added
Blockly.Language.controls_eval_but_ignore = {
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL,
  init: function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('VALUE')
        .appendTitle("evaluate");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP);
    this.appendCollapsedInput().appendTitle('evaluate', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE }]
};

// [lyn, 01/15/2013] Added
Blockly.Language.controls_nothing = {
  // Expression for the nothing value
  category: Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl: Blockly.LANG_CONTROLS_NOTHING_HELPURL,
  init: function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle("nothing");
    this.setOutput(true, null);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_NOTHING_TOOLTIP);
    this.appendCollapsedInput().appendTitle('nothing', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_NOTHING_TITLE }]
};


Blockly.Language.controls_openAnotherScreen = {
  // Open another screen
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('SCREEN').appendTitle('open another screen').appendTitle('screenName').setAlign(Blockly.ALIGN_RIGHT).setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT));
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP);
    this.appendCollapsedInput().appendTitle('open screen', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE }]
};

Blockly.Language.controls_openAnotherScreenWithStartValue = {
  // Open another screen with start value
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('SCREENNAME').setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT)).appendTitle('open another screen with start value').appendTitle('screenName').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('STARTVALUE').appendTitle('startValue').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP);
    this.appendCollapsedInput().appendTitle('open screen with value', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE }]
};

Blockly.Language.controls_getStartValue = {
  // Get start value
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_GET_START_VALUE_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.setOutput(true, null);
    this.appendDummyInput().appendTitle('get start value');
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_GET_START_VALUE_TOOLTIP);
    this.appendCollapsedInput().appendTitle('get start value', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_GET_START_VALUE_TITLE }]
};

Blockly.Language.controls_closeScreen = {
  // Close screen
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_CLOSE_SCREEN_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput().appendTitle('close screen');
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP);
    this.appendCollapsedInput().appendTitle('close screen', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_CLOSE_SCREEN_TITLE_CLOSE }]
};

Blockly.Language.controls_closeScreenWithValue = {
  // Close screen with value
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('SCREEN').appendTitle('close screen with value').appendTitle('result').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP);
    this.appendCollapsedInput().appendTitle('close screen with value', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE_CLOSE }]
};

Blockly.Language.controls_closeApplication = {
  // Close application
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput().appendTitle('close application');
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP);
    this.appendCollapsedInput().appendTitle('close application', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_CLOSE_APPLICATION_TITLE_CLOSE }]
};

Blockly.Language.controls_getPlainStartText = {
  // Get plain start text
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.OUTPUT));
    this.appendDummyInput().appendTitle('get plain start text');
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP);
    this.appendCollapsedInput().appendTitle('get text', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_INPUT_GET }]
};

Blockly.Language.controls_closeScreenWithPlainText = {
  // Close screen with plain text
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL,
  init : function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('TEXT').setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT)).appendTitle('close screen with plain text').appendTitle('text').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    Blockly.Language.setTooltip(this, Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP);
    this.appendCollapsedInput().appendTitle('close screen with text', 'COLLAPSED_TEXT');
  },
  onchange: Blockly.WarningHandler.checkErrors,
  typeblock: [{ translatedName: Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE_CLOSE }]
};
