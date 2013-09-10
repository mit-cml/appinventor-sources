/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://blockly.googlecode.com/
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
 * @fileoverview Colour blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

goog.provide('Blockly.Language.colour');

goog.require('Blockly.Language');

Blockly.Language.colour_picker = {
  // Colour picker.
  category: Blockly.LANG_CATEGORY_COLOUR,
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(20);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#ff0000'), 'COLOUR');
    this.setOutput(true, 'Colour');
    this.setTooltip(Blockly.LANG_COLOUR_PICKER_TOOLTIP);
  }
};

Blockly.Language.colour_rgb = {
  // Compose a colour from RGB components.
  category: Blockly.LANG_CATEGORY_COLOUR,
  helpUrl: Blockly.LANG_COLOUR_RGB_HELPURL,
  init: function() {
    this.setColour(20);
    this.appendValueInput('RED')
        .setCheck(Number)
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendTitle(Blockly.LANG_COLOUR_RGB_TITLE)
        .appendTitle(Blockly.LANG_COLOUR_RGB_RED);
    this.appendValueInput('GREEN')
        .setCheck(Number)
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendTitle(Blockly.LANG_COLOUR_RGB_GREEN);
    this.appendValueInput('BLUE')
        .setCheck(Number)
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendTitle(Blockly.LANG_COLOUR_RGB_BLUE);
    this.setOutput(true, 'Colour');
    this.setTooltip(Blockly.LANG_COLOUR_RGB_TOOLTIP);
  }
};

Blockly.Language.colour_blend = {
  // Blend two colours together.
  category: Blockly.LANG_CATEGORY_COLOUR,
  helpUrl: Blockly.LANG_COLOUR_BLEND_HELPURL,
  init: function() {
    this.setColour(20);
    this.appendValueInput('COLOUR1')
        .setCheck('Colour')
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendTitle(Blockly.LANG_COLOUR_BLEND_TITLE)
        .appendTitle(Blockly.LANG_COLOUR_BLEND_COLOUR1);
    this.appendValueInput('COLOUR2')
        .setCheck('Colour')
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendTitle(Blockly.LANG_COLOUR_BLEND_COLOUR2);
    this.appendValueInput('RATIO')
        .setCheck(Number)
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendTitle(Blockly.LANG_COLOUR_BLEND_RATIO);
    this.setOutput(true, 'Colour');
    this.setTooltip(Blockly.LANG_COLOUR_BLEND_TOOLTIP);
  }
};
