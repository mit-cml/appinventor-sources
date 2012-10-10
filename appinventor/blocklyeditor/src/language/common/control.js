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

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.controls_if = {
  // If/Then condition.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendValueInput('IF').setCheck(Boolean).appendTitle('if').setAlign(Blockly.ALIGN_LEFT).appendTitle('test').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO').appendTitle('then-do').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Tests a given condition. If the result is true, performs '
        + 'the actions \'then-do\' sequence of blocks.');
  }
}

Blockly.Language.controls_ifelse = {
  // If/elseif/else condition.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendValueInput('IF').setCheck(Boolean).appendTitle('if else').appendTitle('test').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO0').appendTitle('then-do').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO1').appendTitle('else-do').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Tests a given condition. If the result is true, performs '
        + 'the actions \'then-do\' sequence of blocks; otherwise, performs the actions \'else-do\''
        + ' sequence of blocks.');
  }
}

Blockly.Language.controls_choose = {
  // Choose.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.setOutput(true, null);
    this.appendValueInput('IF').setCheck(Boolean).appendTitle('choose').appendTitle('test').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO0').appendTitle('then-do').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('THENRETURN').appendTitle('then-return').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('ELSE').appendTitle('else-do').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('ELSERETURN').appendTitle('else-return').setAlign(Blockly.ALIGN_RIGHT);
    Blockly.Language.setTooltip(this, 'If the condition being tested is true, the agent will '
        + 'run all the blocks attached to the \'then-do\' section and return the value attached '
        + 'to the \'then-return\'slot. Otherwise, the agent will run all blocks attached to '
        + 'the \'else-do\' section and return the value in the \'else-return\' slot.');
  }
};

Blockly.Language.controls_forEach = {
  // For each loop.
  category : Blockly.LANG_CATEGORY_CONTROLS,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    //this.setOutput(true, null);
    // Need to deal with variables here
    this.appendValueInput('VAR').appendTitle('for each').appendTitle('variable').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO').appendTitle('do').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('in list').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Runs the blocks in the \'do\'  section for each item in '
        + 'the list.  Use the given variable name to refer to the current list item.');
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
    this.appendValueInput('VAR').appendTitle('for range').appendTitle('variable').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('START').setCheck(Number).appendTitle('start').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('END').setCheck(Number).appendTitle('end').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('STEP').setCheck(Number).appendTitle('step').setAlign(Blockly.ALIGN_RIGHT);
    this.appendStatementInput('DO').appendTitle('do').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Runs the blocks in the \'do\' section for each numeric '
        + 'value in the range from start to end, stepping the value each time.  Use the given '
        + 'variable name to refer to the current value.');
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