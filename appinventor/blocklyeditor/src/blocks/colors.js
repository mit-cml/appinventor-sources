// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
/**
 * @license
 * @fileoverview Color blocks for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.Blocks.color');

goog.require('Blockly.Blocks.Utilities');

Blockly.Blocks['color_black'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#000000'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_BLACK }]
};

Blockly.Blocks['color_white'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ffffff'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_WHITE }]
};

Blockly.Blocks['color_red'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ff0000'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_RED }]
};

Blockly.Blocks['color_pink'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ffafaf'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_PINK }]
};

Blockly.Blocks['color_orange'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ffc800'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_ORANGE }]
};

Blockly.Blocks['color_yellow'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ffff00'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_YELLOW }]
};

Blockly.Blocks['color_green'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#00ff00'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_GREEN }]
};

Blockly.Blocks['color_cyan'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#00ffff'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_CYAN }]
};

Blockly.Blocks['color_blue'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#0000ff'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_BLUE }]
};

Blockly.Blocks['color_magenta'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ff00ff'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_MAGENTA }]
};

Blockly.Blocks['color_light_gray'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#cccccc'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_LIGHT_GRAY }]
};

Blockly.Blocks['color_gray'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#888888'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_GRAY }]
};


Blockly.Blocks['color_dark_gray'] = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#444444'), 'COLOR');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_DARK_GRAY }]
};

Blockly.Blocks['color_make_color'] = {
  category: "Colors",
  helpUrl: Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendValueInput('COLORLIST')
      .appendField(Blockly.Msg.LANG_COLOUR_MAKE_COLOUR)
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("list", Blockly.Blocks.Utilities.INPUT));
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.OUTPUT));
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
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("list",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR }]
};
