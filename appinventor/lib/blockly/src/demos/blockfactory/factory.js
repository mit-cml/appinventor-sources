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
 * @fileoverview Factory for building blocks.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * The uneditable container block that everything else attaches to.
 * @type {Blockly.Block}
 */
var rootBlock = null;

/**
 * The uneditable preview block.
 * @type {Blockly.Block}
 */
var previewBlock = null;

/**
 * The type of the generated block.
 */
var blockType = '';

/**
 * Initialize Blockly.  Called on page load.
 * @param {!Blockly} blockly Instance of Blockly from iframe.
 */
function initPreview(blockly) {
  window.Blockly = blockly;
  if (window.EditorBlockly) {
    // If the main editor has already loaded, update the preview.
    updatePreview();
  }
}

/**
 * Initialize Blockly.  Called on page load.
 * @param {!Blockly} blockly Instance of Blockly from iframe.
 */
function initEditor(blockly) {
  //window.onbeforeunload = function() {
  //  return 'Leaving this page will result in the loss of your work.';
  //};

  window.EditorBlockly = blockly;

  // Create the root block.
  rootBlock = new EditorBlockly.Block(EditorBlockly.mainWorkspace,
                                      'factory_base');
  rootBlock.initSvg();
  rootBlock.render();
  rootBlock.editable = false;

  EditorBlockly.bindEvent_(EditorBlockly.mainWorkspace.getCanvas(),
      'blocklyWorkspaceChange', null, onchange);
}

/**
 * When the workspace changes, update the three other displays.
 */
function onchange() {
  var cat = rootBlock.getTitleValue('CAT');
  var name = rootBlock.getTitleValue('NAME');
  var code = [];
  var type;
  if (cat) {
    type = cat + '_' + name;
  } else {
    type = name;
  }
  blockType = type.replace(/\W/g, '_').replace(/^(d)/, '_\\1').toLowerCase();
  updateLanguage();
  updateGenerator();
  updatePreview();
}

/**
 * Update the language code.
 */
function updateLanguage() {
  // Generate name and category.
  var cat = rootBlock.getTitleValue('CAT');
  var code = [];
  if (cat) {
    cat = cat.replace(/\\/g, '\\\\').replace(/'/g, '\\\'');
    cat = '\'' + cat + '\'';
  } else {
    cat = 'null';
  }
  code.push('Blockly.Language.' + blockType + ' = {');
  code.push('  category: ' + cat + ',');
  code.push('  helpUrl: \'http://www.example.com/\',');
  code.push('  init: function() {');
  // Generate colour.
  var colourBlock = rootBlock.getInputTargetBlock('COLOUR');
  if (colourBlock) {
    code.push('    this.setColour(' + colourBlock.getTitleValue('HUE') + ');');
  }
  // Generate titles.
  var titles = getTitles(rootBlock.getInputTargetBlock('TITLES'));
  for (var x = 0; x < titles.length; x++) {
    code.push('    this' + titles[x]);
  }
  // Generate inputs.
  var TYPES = {'input_value': 'Blockly.INPUT_VALUE',
               'input_statement': 'Blockly.NEXT_STATEMENT',
               'input_dummy': 'Blockly.DUMMY_INPUT'};
  var inputVarDefined = false;
  var contentsBlock = rootBlock.getInputTargetBlock('INPUTS');
  while (contentsBlock) {
    var titles = getTitles(contentsBlock.getInputTargetBlock('TITLES'));
    var inputVar = '';
    if (titles.length) {
      if (inputVarDefined) {
        inputVar = 'input = ';
      } else {
        inputVar = 'var input = ';
      }
      inputVarDefined = true;
    }
    var name = escapeString(contentsBlock.getTitleValue('INPUTNAME') || '');
    var check = getOptTypesFrom(contentsBlock, 'TYPE');
    code.push('    ' + inputVar + 'this.appendInput(' +
              TYPES[contentsBlock.type] + ', ' + name + check + ');');
    for (var x = 0; x < titles.length; x++) {
      code.push('    input' + titles[x]);
    }
    contentsBlock = contentsBlock.nextConnection &&
        contentsBlock.nextConnection.targetBlock();
  }
  // Generate inline/external switch.
  if (rootBlock.getTitleValue('INLINE') == 'INT') {
    code.push('    this.setInputsInline(true);');
  }
  // Generate output, or next/previous connections.
  switch (rootBlock.getTitleValue('CONNECTIONS')) {
    case 'LEFT':
      var outputType = getOptTypesFrom(rootBlock, 'OUTPUTTYPE');
      code.push('    this.setOutput(true' + outputType + ');');
      break;
    case 'BOTH':
      var topType = getOptTypesFrom(rootBlock, 'TOPTYPE');
      code.push('    this.setPreviousStatement(true' + topType + ');');
      var bottomType = getOptTypesFrom(rootBlock, 'BOTTOMTYPE');
      code.push('    this.setNextStatement(true' + bottomType + ');');
      break;
    case 'TOP':
      var topType = getOptTypesFrom(rootBlock, 'TOPTYPE');
      code.push('    this.setPreviousStatement(true' + topType + ');');
      break;
    case 'BOTTOM':
      var bottomType = getOptTypesFrom(rootBlock, 'BOTTOMTYPE');
      code.push('    this.setNextStatement(true' + bottomType + ');');
      break;
  }
  code.push('    this.setTooltip(\'\');');
  code.push('  }');
  code.push('};');

  var ta = document.getElementById('languageTextarea');
  ta.value = code.join('\n');
}

/**
 * Returns a title string and any config.
 * @param {!Blockly.Block} block Title block.
 * @return {string} Title string.
 */
function getTitles(block) {
  var titles = [];
  while (block) {
    switch (block.type) {
      case 'title_static':
        // Result: .appendTitle('hello');
        titles.push('.appendTitle(' + escapeString(block.getTitleValue('TEXT')) +
                    ');');
        break;
      case 'title_input':
        // Result: .appendTitle(new Blockly.FieldTextInput('Hello'), 'GREET');
        titles.push('.appendTitle(new Blockly.FieldTextInput(' +
                    escapeString(block.getTitleValue('TEXT')) + '), ' +
                    escapeString(block.getTitleValue('TITLENAME')) + ');');
        break;
      case 'title_variable':
        // Result:
        // .appendTitle(new Blockly.FieldVariable('item'), 'VAR');
        var varname = block.getTitleValue('TEXT');
        varname = varname ? escapeString(varname) : 'null';
        titles.push('.appendTitle(new Blockly.FieldVariable(' +
            varname + '), ' + escapeString(block.getTitleValue('TITLENAME')) + ');');
        break;
      case 'title_dropdown':
        // Result:
        // .appendTitle(new Blockly.FieldDropdown(
        //     [['yes', '1'], ['no', '0']]), 'TOGGLE');
        var options = [];
        for (var x = 0; x < block.optionCount_; x++) {
          options[x] = '[' + escapeString(block.getTitleValue('USER' + x)) +
              ', ' + escapeString(block.getTitleValue('CPU' + x)) + ']';
        }
        if (options.length) {
          titles.push('.appendTitle(new Blockly.FieldDropdown([' +
              options.join(', ') + ']), ' +
              escapeString(block.getTitleValue('TITLENAME')) + ');');
        }
        break;
    }
    block = block.nextConnection && block.nextConnection.targetBlock();
  }
  return titles;
}

/**
 * Escape a string.
 * @param {string} String to escape.
 * @return {string} Escaped string surrouned by quotes.
 */
function escapeString(string) {
  if (JSON && JSON.stringify) {
    return JSON.stringify(string);
  }
  // Hello MSIE 8.
  return '"' + string.replace(/\\/g, '\\\\').replace(/"/g, '\\"') + '"';
}

/**
 * Fetch the type(s) defined in the given input.
 * Format as a string for appending to the generated code.
 * @param {!Blockly.Block} block Block with input. 
 * @param {string} name Name of the input.
 * @return {string} String defining the types.
 */
function getOptTypesFrom(block, name) {
  var types = getTypesFrom_(block, name);
  if (types.length == 0) {
    return '';
  } else if (types.length == 1) {
    return ', ' + types[0];
  } else if (types.indexOf('null') != -1) {
    return ['null'];
  } else {
    return ', [' + types.join(', ') + ']';
  }
}

/**
 * Fetch the type(s) defined in the given input.
 * @param {!Blockly.Block} block Block with input. 
 * @param {string} name Name of the input.
 * @return {!Array.<string>} List of types.
 * @private
 */
function getTypesFrom_(block, name) {
  var typeBlock = block.getInputTargetBlock(name);
  var types;
  if (!typeBlock) {
    types = [];
  } else if (typeBlock.type == 'type_other') {
    types = [escapeString(typeBlock.getTitleValue('TYPE'))];
  } else if (typeBlock.type == 'type_group') {
    types = [];
    for (var n = 0; n < typeBlock.typeCount_; n++) {
      types = types.concat(getTypesFrom_(typeBlock, 'TYPE' + n));
    }
    // Remove duplicates.
    var hash = {};
    for (var n = types.length - 1; n >= 0; n--) {
      if (hash['X_' + types[n]]) {
        types.splice(n, 1);
        continue;
      }
      hash['X_' + types[n]] = true;
    }
  } else {
    types = [typeBlock.valueType];
  }
  return types;
}

/**
 * Update the generator code.
 */
function updateGenerator() {
  function makeVar(root, name) {
    name = name.toLowerCase().replace(/\W/g, '_');
    return '  var ' + root + '_' + name;
  }
  var language = document.getElementById('language').value;
  var code = [];
  code.push('Blockly.' + language + '.' + blockType + ' = {');
  // Loop through every block, and generate getters for any fields or inputs.
  var blocks = rootBlock.getDescendants();
  for (var x = 0, block; block = blocks[x]; x++) {
    switch (block.type) {
      case 'title_input':
        var name = block.getTitleValue('TITLENAME');
        code.push(makeVar('text', name) +
                  ' = this.getTitleValue(\'' + name + '\');');
        break;
      case 'title_dropdown':
        var name = block.getTitleValue('TITLENAME');
        code.push(makeVar('dropdown', name) +
                  ' = this.getTitleValue(\'' + name + '\');');
        break;
      case 'title_variable':
        var name = block.getTitleValue('TITLENAME');
        code.push(makeVar('variable', name) +
                  ' = Blockly.' + language +
                  '.variableDB_.getName(this.getTitleValue(\'' + name +
                  '\'), Blockly.Variables.NAME_TYPE);');
        break;
      case 'input_value':
        var name = block.getTitleValue('INPUTNAME');
        code.push(makeVar('value', name) +
                  ' = Blockly.' + language + '.valueToCode(this, \'' + name +
                  '\', Blockly.' + language + '.ORDER_ATOMIC);');
        break;
      case 'input_statement':
        var name = block.getTitleValue('INPUTNAME');
        code.push(makeVar('statements', name) +
                  ' = Blockly.' + language + '.statementToCode(this, \'' +
                  name + '\');');
        break;
    }
  }
  code.push('  // TODO: Assemble ' + language + ' into code variable.');
  code.push('  var code = \'...\'');
  if (rootBlock.getTitleValue('CONNECTIONS') == 'LEFT') {
    code.push('  // TODO: Change ORDER_NONE to the correct strength.');
    code.push('  return [code, Blockly.' + language + '.ORDER_NONE];');
  } else {
    code.push('  return code;');
  }
  code.push('};');

  var ta = document.getElementById('generatorTextarea');
  ta.value = code.join('\n');
}

/**
 * Update the preview display.
 */
function updatePreview() {
  if (!Blockly) {
    // If the preview frame hasn't loaded yet, don't try to update.
    return;
  }
  if (previewBlock) {
    previewBlock.destroy();
  }
  var type = blockType;
  var code = document.getElementById('languageTextarea').value;
  eval(code);
  // Create the preview block.
  previewBlock = new Blockly.Block(Blockly.mainWorkspace, type);
  previewBlock.initSvg();
  previewBlock.render();
  previewBlock.editable = false;
}

