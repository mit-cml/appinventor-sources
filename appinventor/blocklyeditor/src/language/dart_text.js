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
 * @fileoverview Generating Dart for text blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Dart = Blockly.Generator.get('Dart');

Blockly.Dart.text = function() {
  // Text value.
  return Blockly.Dart.quote_(this.getTitleText(1));
};

Blockly.Dart.text_length = function() {
  // String length.
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || '\'\'';
  return argument0 + '.length';
};

Blockly.Dart.text_isEmpty = function() {
  // Is the string null?
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || '\'\'';
  return argument0 + '.isEmpty()';
};

Blockly.Dart.text_contains = function(opt_dropParens) {
  // Does the text contain a substring?
  // Using String.contains would be cleaner, but using .indexOf allows this
  // block to be used on lists as well as strings.
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || '\'\'';
  var argument1 = Blockly.Dart.valueToCode_(this, 1) || '\'\'';
  var code = argument1 + '.indexOf(' + argument0 + ') != -1';
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.text_charAt = function() {
  // Get letter at index.
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || '1';
  var argument1 = Blockly.Dart.valueToCode_(this, 1) || '[]';
  // Blockly uses one-based arrays.
  if (argument0.match(/^\d+$/)) {
    // If the index is a naked number, decrement it right now.
    argument0 = parseInt(argument0, 10) - 1;
  } else {
    // If the index is dynamic, decrement it in code.
    argument0 += ' - 1';
  }
  return argument1 + '[' + argument0 + ']';
};

Blockly.Dart.text_changeCase = function() {
  // Change capitalization.
  var operator;
  switch (this.getValueLabel(0)) {
    case Blockly.Language.text_changeCase.MSG_UPPERCASE:
      operator = 'toUpperCase';
      break;
    case Blockly.Language.text_changeCase.MSG_LOWERCASE:
      operator = 'toLowerCase';
      break;
    case Blockly.Language.text_changeCase.MSG_TITLECASE:
      operator = null;
      break;
    default:
      throw 'Unknown operator.';
  }

  var code;
  if (operator) {
    // Upper and lower case are functions built into Dart.
    var argument0 = Blockly.Dart.valueToCode_(this, 0) || '\'\'';
    code = argument0 + '.' + operator + '()';
  } else {
    if (!Blockly.Dart.definitions_['toTitleCase']) {
      // Title case is not a native Dart function.  Define one.
      var func = [];
      func.push('Blockly_toTitleCase(str) {');
      func.push('  RegExp exp = const RegExp(@"(\\w\\S*)");');
      func.push('  List<String> list = str.split(exp);');
      func.push('  String title = \'\';');
      func.push('  for (String part in list) {');
      func.push('    if (part.length > 0) {');
      func.push('      title += part[0].toUpperCase();');
      func.push('      if (part.length > 0) {');
      func.push('        title += part.substring(1).toLowerCase();');
      func.push('      }');
      func.push('    }');
      func.push('  }');
      func.push('  return title;');
      func.push('}');
      Blockly.Dart.definitions_['toTitleCase'] = func.join('\n');
    }
    var argument0 = Blockly.Dart.valueToCode_(this, 0, true) || '\'\'';
    code = 'Blockly_toTitleCase(' + argument0 + ')';
  }
  return code;
};

Blockly.Dart.text_trim = function() {
  // Trim spaces.
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || '\'\'';
  var operator = this.getTitleText(1);
  if (operator == Blockly.Language.text_trim.MSG_BOTH) {
    return argument0 + '.trim()';
  }
  var regex = operator == Blockly.Language.text_trim.MSG_LEFT ? '^\\s+' : '\\s+$';
  return argument0 + '.replaceFirst(new RegExp(@"' + regex + '"), \'\')';
};

Blockly.Dart.text_print = function() {
  // Print statement.
  var argument0 = Blockly.Dart.valueToCode_(this, 0, true) || '\'\'';
  return 'print(' + argument0 + ');\n';
};
