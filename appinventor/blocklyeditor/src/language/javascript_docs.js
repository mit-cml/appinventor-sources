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
 * @fileoverview Generating JavaScript for Google Docs blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.JavaScript = Blockly.Generator.get('JavaScript');

Blockly.JavaScript.docs_newCalendar = function() {
  // New Calendar Event.
  function dateArgument(value) {
    if (value === null) {
      return 'new Date()';
    } else if (value[0] == '\'' || value[0] == '"') {
      return 'new Date(' + value + ')';
    } else {
      return 'typeof ' + value + ' == \'object\' ? ' + value +
          ' : new Date(' + value + ')';
    }
  }
  var argument0 = Blockly.JavaScript.valueToCode_(this, 0, true) || '\'\'';
  var argument1 = Blockly.JavaScript.valueToCode_(this, 1) || null;
  var argument2 = Blockly.JavaScript.valueToCode_(this, 2) || null;
  var argument3 = Blockly.JavaScript.valueToCode_(this, 3, true) || '\'\'';
  var argument4 = Blockly.JavaScript.valueToCode_(this, 4, true) || '\'\'';
  var code = 'CalendarApp.getDefaultCalendar().createEvent(' +
      argument0 + ', ' +
      dateArgument(argument1) + ', ' +
      dateArgument(argument2) + ', ' +
      '{location: ' + Blockly.JavaScript.quote_(argument3) + ', ' +
      'guests: ' + Blockly.JavaScript.quote_(argument4) + '})';
  return code;
};

Blockly.JavaScript.docs_getSpreadsheetData = function() {
  // Get all the data for the active spreadsheet.
  return 'SpreadsheetApp.getActiveSheet().getDataRange().getValues()';
};
