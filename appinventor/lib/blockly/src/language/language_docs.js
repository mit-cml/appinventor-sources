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
 * @fileoverview Google Docs blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Language.docs_newCalendar = {
  // New Calendar Event.
  category: 'Docs',
  init: function() {
    this.setColour('yellow');
    this.addTitle('new Calendar Event');
    this.setTooltip('Creates a new event in the Calendar.');
    this.addInput('Title', 'Title of the new event.', Blockly.INPUT_VALUE);
    this.addInput('Start Time', 'Date and time when the event starts.', Blockly.INPUT_VALUE);
    this.addInput('End Time', 'Date and time when the event ends.', Blockly.INPUT_VALUE);
    this.addInput('Location', '.', Blockly.INPUT_VALUE);
    this.addInput('Guests', '.', Blockly.INPUT_VALUE);
    this.setOutput(true);
  }
};

Blockly.Language.docs_getSpreadsheetData = {
  // Get all the data for the active spreadsheet.
  category: 'Docs',
  init: function() {
    this.setColour('yellow');
    this.addTitle('get');
    this.addTitle('Spreadsheet Data');
    this.setTooltip('.');
    this.setOutput(true);
  }
};
