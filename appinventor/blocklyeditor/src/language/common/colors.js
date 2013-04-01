
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
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
  },
  onchange: Blockly.WarningHandler.checkErrors
};

Blockly.Language.color_make_color = {
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendValueInput('COLORLIST').appendTitle("make color");
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("A color with the given red, green, blue, and optionally alpha components");
  },
  onchange: Blockly.WarningHandler.checkErrors
};

Blockly.Language.color_split_color = {
  category: "Colors",
  helpUrl: Blockly.LANG_COLOUR_PICKER_HELPURL,
  init: function() {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendValueInput('COLOR').appendTitle("split color");
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip("A list of four elements, each in the range 0 to 255, representing the red, green, blue and alpha components.");
  },
  onchange: Blockly.WarningHandler.checkErrors
};



