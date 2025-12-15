// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Color blocks for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Blocks.color');
goog.require("AI.Blockly.FieldColor")
goog.require('AI.BlockUtils');

createColorBlock('color_black', '#000000ff', Blockly.Msg.LANG_COLOUR_BLACK);
createColorBlock('color_white', '#ffffffff', Blockly.Msg.LANG_COLOUR_WHITE);
createColorBlock('color_red', '#ff0000ff', Blockly.Msg.LANG_COLOUR_RED);
createColorBlock('color_pink', '#ffafafff', Blockly.Msg.LANG_COLOUR_PINK);
createColorBlock('color_orange', '#ffc800ff', Blockly.Msg.LANG_COLOUR_ORANGE);
createColorBlock('color_yellow', '#ffff00ff', Blockly.Msg.LANG_COLOUR_YELLOW);
createColorBlock('color_green', '#00ff00ff', Blockly.Msg.LANG_COLOUR_GREEN);
createColorBlock('color_cyan', '#00ffffff', Blockly.Msg.LANG_COLOUR_CYAN);
createColorBlock('color_blue', '#0000ffff', Blockly.Msg.LANG_COLOUR_BLUE);
createColorBlock('color_magenta', '#ff00ffff', Blockly.Msg.LANG_COLOUR_MAGENTA);
createColorBlock('color_light_gray', '#ccccccff', Blockly.Msg.LANG_COLOUR_LIGHT_GRAY);
createColorBlock('color_gray', '#888888ff', Blockly.Msg.LANG_COLOUR_GRAY);
createColorBlock('color_dark_gray', '#444444ff', Blockly.Msg.LANG_COLOUR_DARK_GRAY);

function createColorBlock(name, color, lang) {
  Blockly.Blocks[name] = {
    category: "Colors",
    helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
    init: function() {
      this.setColour(Blockly.COLOR_CATEGORY_HUE);
      this.appendDummyInput().appendField(new Blockly.FieldColor(color), 'COLOR');
      this.data = this.data || "";
      this.data = color;
      this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
      this.setTooltip(Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP);
    },
    typeblock: [{ translatedName: lang }]
  };
}

Blockly.Blocks['color_make_color'] = {
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendValueInput('COLORLIST')
      .appendField(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR)
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.INPUT));
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_MAKE_COLOUR }]
};

Blockly.Blocks['color_split_color'] = {
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendValueInput('COLOR')
      .appendField(Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR)
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number",AI.BlockUtils.INPUT));
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list",AI.BlockUtils.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR }]
};
