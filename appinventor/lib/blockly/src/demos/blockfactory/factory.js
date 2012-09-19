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
  var cat = rootBlock.getTitleText('CAT');
  var name = rootBlock.getTitleText('NAME');
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
  var cat = rootBlock.getTitleText('CAT');
  var code = [];
  if (cat) {
    cat = cat.replace(/\\/g, '\\\\').replace(/'/g, '\\\'');
    cat = '\'' + cat + '\'';
  } else {
    cat = 'null';
  }
  code.push('Blockly.Language.' + blockType + ' = {');
  code.push('  category: ' + cat + ',');
  code.push('  init: function() {');
  var colourBlock = rootBlock.getInputTargetBlock('COLOUR');
  if (colourBlock) {
    code.push('    this.setColour(' + colourBlock.getTitleValue('HUE') + ');');
  }
  var contentsBlock = rootBlock.getInputTargetBlock('INPUTS');
  while (contentsBlock) {
    switch (contentsBlock.type) {
      case 'input_value':
        var name = escapeString(contentsBlock.getTitleText('NAME'));
        var type = getTypesFrom(contentsBlock, 'TYPE');
        if (type.length == 1) {
          type = type[0];
        } else {
          type = '[' + type.join(', ') + ']';
        }
        var titles = getTitles(contentsBlock.getInputTargetBlock('TITLES'));
        code.push('    this.appendInput(' + titles +
                  ', Blockly.INPUT_VALUE, ' + name + ', ' + type + ');');
        break;
      case 'input_statement':
        var name = escapeString(contentsBlock.getTitleText('NAME'));
        var titles = getTitles(contentsBlock.getInputTargetBlock('TITLES'));
        code.push('    this.appendInput(' + titles +
                  ', Blockly.NEXT_STATEMENT, ' + name + ');');
        break;
      case 'input_variable':
        break;
    }
    contentsBlock = contentsBlock.nextConnection &&
        contentsBlock.nextConnection.targetBlock();
  }
  if (rootBlock.getTitleValue('INLINE') == 'INT') {
    code.push('    this.setInputsInline(true);');
  }
  switch (rootBlock.getTitleValue('CONNECTIONS')) {
    case 'LEFT':
      var type = getTypesFrom(rootBlock, 'OUTPUTTYPE');
      if (type.length == 1) {
        type = type[0];
      } else {
        type = '[' + type.join(', ') + ']';
      }
      code.push('    this.setOutput(true, ' + type + ');');
      break;
    case 'BOTH':
      code.push('    this.setPreviousStatement(true);');
      code.push('    this.setNextStatement(true);');
      break;
    case 'TOP':
      code.push('    this.setPreviousStatement(true);');
      break;
    case 'BOTTOM':
      code.push('    this.setNextStatement(true);');
      break;
  }
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
  if (!block) {
    return '\'\'';
  }
  return escapeString(block.getTitleText('TEXT'));
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
 * @param {!Blockly.Block} block Block with input. 
 * @param {string} name Name of the input.
 * @return {!Array.<string>} List of types.
 */
function getTypesFrom(block, name) {
  var typeBlock = block.getInputTargetBlock(name);
  var type;
  if (!typeBlock) {
    type = 'null';
  } else if (typeBlock.type == 'type_other') {
    type = escapeString(typeBlock.getTitleText('TYPE'));
  } else {
    type = typeBlock.valueType;
  }
  return [type];
}

/**
 * Update the generator code.
 */
function updateGenerator() {
  var language = document.getElementById('language').value;
  var code = [];
  code.push('Blockly.' + language + '.' + blockType + ' = {');
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

