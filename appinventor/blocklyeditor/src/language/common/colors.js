
if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.color_black = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#000000'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_BLACK }]
};

Blockly.Language.color_white = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#ffffff'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_WHITE }]
};

Blockly.Language.color_red = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#ff0000'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_RED }]
};

Blockly.Language.color_pink = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#ffafaf'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_PINK }]
};

Blockly.Language.color_orange = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#ffc800'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_ORANGE }]
};

Blockly.Language.color_yellow = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#ffff00'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_YELLOW }]
};

Blockly.Language.color_green = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#00ff00'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_GREEN }]
};

Blockly.Language.color_cyan = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#00ffff'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_CYAN }]
};


Blockly.Language.color_blue = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#0000ff'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_BLUE }]
};

Blockly.Language.color_magenta = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#ff00ff'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_MAGENTA }]
};

Blockly.Language.color_light_gray = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#cccccc'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_LIGHT_GRAY }]
};

Blockly.Language.color_gray = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#888888'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_GRAY }]
};


Blockly.Language.color_dark_gray = {
  // Colour picker.
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldColour('#444444'), 'COLOR');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("Click the square to pick a color.");
    this.appendCollapsedInput().appendTitle(' ', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_DARK_GRAY }]
};

Blockly.Language.color_make_color = {
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_MAKE_COLOUR_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendValueInput('COLORLIST').appendTitle("make color").setCheck(Blockly.Language.YailTypeToBlocklyType("list",Blockly.Language.INPUT));
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip(Blockly.LANG_COLOUR_MAKE_COLOUR_TOOLTIP);
    this.appendCollapsedInput().appendTitle(Blockly.LANG_COLOUR_MAKE_COLOUR, 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_MAKE_COLOUR }]
};

Blockly.Language.color_split_color = {
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_SPLIT_COLOUR_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendValueInput('COLOR').appendTitle("split color").setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("list",Blockly.Language.OUTPUT));
    this.setTooltip(Blockly.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP);
    this.appendCollapsedInput().appendTitle(Blockly.LANG_COLOUR_SPLIT_COLOUR, 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_COLOUR_SPLIT_COLOUR }]
};



