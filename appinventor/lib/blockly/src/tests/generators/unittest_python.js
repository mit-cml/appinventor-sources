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
 * @fileoverview Generating Python for unit test blocks.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

Blockly.Python = Blockly.Generator.get('Python');

Blockly.Python.unittest_main = function() {
  // Container for unit tests.
  var resultsVar = Blockly.Python.variableDB_.getName('unittestResults',
      Blockly.Variables.NAME_TYPE);
  if (!Blockly.Python.definitions_['unittest_report']) {
    var functionName = Blockly.Python.variableDB_.getDistinctName(
        'testReport', Blockly.Generator.NAME_TYPE);
    Blockly.Python.unittest_main.report = functionName;
    var func = [];
    func.push('def ' + functionName + '():');
    func.push('  # Create test report.');
    func.push('  report = []');
    func.push('  summary = []');
    func.push('  fails = 0');
    func.push('  for (success, title, log) in ' + resultsVar + ':');
    func.push('    if success:');
    func.push('      summary.append(".")');
    func.push('    else:');
    func.push('      summary.append("F")');
    func.push('      fails += 1');
    func.push('      report.append("")');
    func.push('      report.append("FAIL: " + title)');
    func.push('      report.append(log)');
    func.push('  report.insert(0, "".join(summary))');
    func.push('  report.append("")');
    func.push('  report.append("Ran %d test." % len(' + resultsVar + '))');
    func.push('  report.append("")');
    func.push('  if fails:');
    func.push('    report.append("FAILED (failures=%d)" % fails)');
    func.push('  else:');
    func.push('    report.append("OK")');
    func.push('  return "\\n".join(report)');
    func.push('');
    Blockly.Python.definitions_['unittest_report'] = func.join('\n');
  }
  // Setup global to hold test results.
  var code = resultsVar + ' = []\n';
  // Run tests (unindented).
  code += Blockly.Python.statementToCode(this, 'DO')
      .replace(/^  /, '').replace(/\n  /g, '\n');
  var reportVar = Blockly.Python.variableDB_.getDistinctName(
      'report', Blockly.Variables.NAME_TYPE);
  code += reportVar + ' = ' + Blockly.Python.unittest_main.report + '()\n';
  // Destroy results.
  code += resultsVar + ' = None\n';
  // Print the report.
  code += 'print(' + reportVar + ')\n';
  return code;
};

Blockly.Python.unittest_assertequals = function() {
  var resultsVar = Blockly.Python.variableDB_.getName('unittestResults',
      Blockly.Variables.NAME_TYPE);
  // Asserts that a value equals another value.
  var message = Blockly.Python.quote_(this.getTitleValue('MESSAGE'));
  if (!Blockly.Python.definitions_['unittest_assertequals']) {
    var functionName = Blockly.Python.variableDB_.getDistinctName(
        'assertEquals', Blockly.Generator.NAME_TYPE);
    Blockly.Python.unittest_assertequals.assert = functionName;
    var func = [];
    func.push('def ' + functionName + '(actual, expected, message):');
    func.push('  # Asserts that a value equals another value.');
    func.push('  if ' + resultsVar + ' == None:');
    func.push('    raise Exception("Orphaned assert equals: ' + message + '")');
    func.push('  if actual == expected:');
    func.push('    ' + resultsVar + '.append((True, "OK", message))');
    func.push('  else:');
    func.push('    ' + resultsVar + '.append((False, ' +
        '"Expected: %s\\nActual: %s" % (expected, actual), message))');
    func.push('');
    Blockly.Python.definitions_['unittest_assertequals'] = func.join('\n');
  }
  var actual = Blockly.Python.valueToCode(this, 'ACTUAL',
      Blockly.Python.ORDER_COMMA) || 'None';
  var expected = Blockly.Python.valueToCode(this, 'EXPECTED',
      Blockly.Python.ORDER_COMMA) || 'None';
  return Blockly.Python.unittest_assertequals.assert + '(' +
      actual + ', ' + expected + ', ' + message + ')\n';
};

Blockly.Python.unittest_asserttrue = function() {
  var resultsVar = Blockly.Python.variableDB_.getName('unittestResults',
      Blockly.Variables.NAME_TYPE);
  // Asserts that a value is true.
  var message = Blockly.Python.quote_(this.getTitleValue('MESSAGE'));
  if (!Blockly.Python.definitions_['unittest_asserttrue']) {
    var functionName = Blockly.Python.variableDB_.getDistinctName(
        'assertTrue', Blockly.Generator.NAME_TYPE);
    Blockly.Python.unittest_asserttrue.assert = functionName;
    var func = [];
    func.push('def ' + functionName + '(actual, message):');
    func.push('  # Asserts that a value is true.');
    func.push('  if ' + resultsVar + ' == None:');
    func.push('    raise Exception("Orphaned assert equals: ' + message + '")');
    func.push('  if actual == True:');
    func.push('    ' + resultsVar + '.append((True, "OK", message))');
    func.push('  else:');
    func.push('    ' + resultsVar + '.append((False, ' +
              '"Expected: true\\nActual: %s" % actual, message))');
    func.push('');
    Blockly.Python.definitions_['unittest_asserttrue'] = func.join('\n');
  }
  var actual = Blockly.Python.valueToCode(this, 'ACTUAL',
      Blockly.Python.ORDER_COMMA) || 'True';
  return Blockly.Python.unittest_asserttrue.assert +
      '(' + actual + ', ' + message + ')\n';
};

Blockly.Python.unittest_assertfalse = function() {
  var resultsVar = Blockly.Python.variableDB_.getName('unittestResults',
      Blockly.Variables.NAME_TYPE);
  // Asserts that a value is false.
  var message = Blockly.Python.quote_(this.getTitleValue('MESSAGE'));
  if (!Blockly.Python.definitions_['unittest_assertfalse']) {
    var functionName = Blockly.Python.variableDB_.getDistinctName(
        'assertFalse', Blockly.Generator.NAME_TYPE);
    Blockly.Python.unittest_assertfalse.assert = functionName;
    var func = [];
    func.push('def ' + functionName + '(actual, message):');
    func.push('  # Asserts that a value is false.');
    func.push('  if ' + resultsVar + ' == None:');
    func.push('    raise Exception("Orphaned assert equals: ' + message + '")');
    func.push('  if actual == False:');
    func.push('    ' + resultsVar + '.append((True, "OK", message))');
    func.push('  else:');
    func.push('    ' + resultsVar + '.append((False, ' +
              '"Expected: false\\nActual: %s" % actual, message))');
    func.push('');
    Blockly.Python.definitions_['unittest_assertfalse'] = func.join('\n');
  }
  var actual = Blockly.Python.valueToCode(this, 'ACTUAL',
      Blockly.Python.ORDER_COMMA) || 'False';
  return Blockly.Python.unittest_assertfalse.assert +
      '(' + actual + ', ' + message + ')\n';
};

Blockly.Python.unittest_fail = function() {
  // Always assert an error.
  var resultsVar = Blockly.Python.variableDB_.getName('unittestResults',
      Blockly.Variables.NAME_TYPE);
  var message = Blockly.Python.quote_(this.getTitleValue('MESSAGE'));
  if (!Blockly.Python.definitions_['unittest_fail']) {
    var functionName = Blockly.Python.variableDB_.getDistinctName(
        'fail', Blockly.Generator.NAME_TYPE);
    Blockly.Python.unittest_fail.assert = functionName;
    var func = [];
    func.push('def ' + functionName + '(message):');
    func.push('  # Always assert an error.');
    func.push('  if ' + resultsVar + ' == None:');
    func.push('    raise Exception("Orphaned assert equals: ' + message + '")');
    func.push('  ' + resultsVar + '.append((False, "Fail.", message))');
    func.push('');
    Blockly.Python.definitions_['unittest_fail'] = func.join('\n');
  }
  return Blockly.Python.unittest_fail.assert + '(' + message + ')\n';
};
