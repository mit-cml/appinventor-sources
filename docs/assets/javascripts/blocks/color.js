
Blockly.COLOR_CATEGORY_HUE = "#7D7D7D";

Blockly.Blocks['color_black'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#000000'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_white'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ffffff'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_red'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ff0000'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_pink'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ffafaf'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_orange'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ffc800'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_yellow'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ffff00'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_green'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#00ff00'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_cyan'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#00ffff'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_blue'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#0000ff'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_magenta'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#ff00ff'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_light_gray'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#cccccc'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_gray'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#888888'), 'COLOR');
    this.setOutput(true, null);
  },
};


Blockly.Blocks['color_dark_gray'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendDummyInput().appendField(new Blockly.FieldColour('#444444'), 'COLOR');
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_make_color'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendValueInput('COLORLIST')
      .appendField("make color")
      .setCheck(null);
    this.setOutput(true, null);
  },
};

Blockly.Blocks['color_split_color'] = {
  init: function () {
    this.setColour(Blockly.COLOR_CATEGORY_HUE);
    this.appendValueInput('COLOR')
      .appendField("split color")
      .setCheck(null);
    this.setOutput(true, null);
  },
};