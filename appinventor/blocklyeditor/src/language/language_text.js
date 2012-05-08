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
 * @fileoverview Text blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) {
  Blockly.Language = {};
}

Blockly.Language.text = {
  // Text value.
  category: 'Text',
  helpUrl: 'http://en.wikipedia.org/wiki/String_(computer_science)',
  init: function() {
    this.setColour('brown');
    this.addTitle('\u201C');
    this.addTitle(new Blockly.FieldTextInput(''));
    this.addTitle('\u201D');
    this.setOutput(true);
    this.setTooltip('A letter, word, or line of text.');
  }
};

Blockly.Language.text_length = {
  // String length.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour('brown');
    this.addInput('length', '', Blockly.INPUT_VALUE);
    this.setOutput(true);
  }
};

Blockly.Language.text_isEmpty = {
  // Is the string null?
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour('brown');
    this.addInput('is empty', '', Blockly.INPUT_VALUE);
    this.setOutput(true);
  }
};

Blockly.Language.text_contains = {
  // Does the text contain a substring?
  category: 'Text',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    this.setColour('brown');
    this.setOutput(true);
    this.addInput('is text', '', Blockly.INPUT_VALUE);
    this.addInput('in text', '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
  }
};

Blockly.Language.text_charAt = {
  // Get a character from the string.
  category: 'Text',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    this.setColour('brown');
    this.addTitle('letter');
    this.setOutput(true);
    this.addInput('at', '', Blockly.INPUT_VALUE);
    this.addInput('in text', '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
  }
};

Blockly.Language.text_changeCase = {
  // Change capitalization.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour('brown');
    this.addTitle('to');
    var menu = new Blockly.FieldDropdown(Blockly.Language.text_changeCase.MSG_UPPERCASE, function() {
      return [Blockly.Language.text_changeCase.MSG_UPPERCASE,
              Blockly.Language.text_changeCase.MSG_LOWERCASE,
              Blockly.Language.text_changeCase.MSG_TITLECASE];
    });
    this.addInput(menu, '', Blockly.INPUT_VALUE);
    this.setOutput(true);
  }
};

Blockly.Language.text_changeCase.MSG_UPPERCASE = 'UPPER CASE';
Blockly.Language.text_changeCase.MSG_LOWERCASE = 'lower case';
Blockly.Language.text_changeCase.MSG_TITLECASE = 'Title Case';

Blockly.Language.text_trim = {
  // Trim spaces.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour('brown');
    this.addTitle('trim spaces from');
    var menu = new Blockly.FieldDropdown(Blockly.Language.text_trim.MSG_BOTH, function() {
      return [Blockly.Language.text_trim.MSG_LEFT,
              Blockly.Language.text_trim.MSG_RIGHT,
              Blockly.Language.text_trim.MSG_BOTH];
    }, function(text) {
      var newTitle = (text == Blockly.Language.text_trim.MSG_BOTH) ? 'sides' : 'side';
      this.sourceBlock_.setTitleText(newTitle, 2);
      this.setText(text);
    });
    this.addTitle(menu);
    this.addTitle('sides');
    this.addInput('', '', Blockly.INPUT_VALUE);
    this.setOutput(true);
  }
};

Blockly.Language.text_trim.MSG_LEFT = 'left';
Blockly.Language.text_trim.MSG_RIGHT = 'right';
Blockly.Language.text_trim.MSG_BOTH = 'both';

Blockly.Language.text_print = {
  // Print statement.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour('brown');
    this.addTitle('print');
    this.addInput('', '', Blockly.INPUT_VALUE);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  }
};
