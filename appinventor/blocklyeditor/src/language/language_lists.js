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
 * @fileoverview List blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) {
  Blockly.Language = {};
}

Blockly.Language.lists_length = {
  // List length.
  category: 'Lists',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour('blue');
    this.addInput('length', '', Blockly.INPUT_VALUE);
    this.setOutput(true);
  }
};

Blockly.Language.lists_isEmpty = {
  // Is the list empty?
  category: 'Lists',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour('blue');
    this.addInput('is empty', '', Blockly.INPUT_VALUE);
    this.setOutput(true);
  }
};

Blockly.Language.lists_contains = {
  // Does the list contain a value?
  category: 'Lists',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    this.setColour('blue');
    this.setOutput(true);
    this.addInput('is item', '', Blockly.INPUT_VALUE);
    this.addInput('in list', '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
  }
};

Blockly.Language.lists_getIndex = {
  // Get element at index.
  category: 'Lists',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    this.setColour('blue');
    this.setOutput(true);
    this.addInput('item at', '', Blockly.INPUT_VALUE);
    this.addInput('in list', '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
  }
};
