// Copyright 2012 Massachusetts Institute of Technology. All rights reserved

/**
 * Text built-in blocks
 * 
 * @author andrew.f.mckinney@gmail.com  (Andrew McKinney)
 */

if (!Blockly.Language) Blockly.Language = {};

/* if (!Blockly.DrawerInit) Blockly.DrawerInit = {}; */

Blockly.Language.text = {
  // Text value.
  category: Blockly.LANG_CATEGORY_TEXT,
  helpUrl: Blockly.LANG_TEXT_TEXT_HELPURL,
  init: function() {
    this.setColour(160);
    this.appendTitle('\u201C');
    this.appendTitle(new Blockly.FieldTextInput(''), 'TEXT');
    this.appendTitle('\u201D');
    this.setOutput(true, String);
    this.setTooltip(Blockly.LANG_TEXT_TEXT_TOOLTIP_1);
  }
};

/*
Blockly.DrawerInit.text = function init() {
  Blockly.Language.generic_join = Blockly.Language.makeTwoArgumentPrimitive(Blockly.LANG_CATEGORY_TEXT, Blockly.LANG_TEXT_JOIN_HELPURL, Blockly.LANG_TEXT_JOIN_TOOLTIP_1, 160, String, 'join', String, String, true);

  Blockly.Language.generic_length = Blockly.Language.makeOneArgumentPrimitive(Blockly.LANG_CATEGORY_TEXT, Blockly.LANG_TEXT_LENGTH_HELPURL, Blockly.LANG_TEXT_LENGTH_TOOLTIP_1, 160, String, Blockly.LANG_TEXT_LENGTH_INPUT_LENGTH, String);
	
  Blockly.Language.generic_isTextEmpty = Blockly.Language.makeOneArgumentPrimitive(Blockly.LANG_CATEGORY_TEXT, Blockly.LANG_TEXT_ISEMPTY_HELPURL, Blockly.LANG_TEXT_ISEMPTY_TOOLTIP_1, 160, String, Blockly.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY, String);
	
  Blockly.Language.generic_textLessThen = Blockly.Language.makeTwoArgumentPrimitive(Blockly.LANG_CATEGORY_TEXT, ' ', ' ', 160, String, 'text <', String, String, true);

  Blockly.Language.generic_textGreaterThen = Blockly.Language.makeTwoArgumentPrimitive(Blockly.LANG_CATEGORY_TEXT, ' ', ' ', 160, String, 'text >', String, String, true);

  Blockly.Language.generic_textEquals = Blockly.Language.makeTwoArgumentPrimitive(Blockly.LANG_CATEGORY_TEXT, ' ', ' ', 160, String, 'text =', String, String, true);

  Blockly.Language.generic_trim = Blockly.Language.makeOneArgumentPrimitive(Blockly.LANG_CATEGORY_TEXT, ' ', ' ', 160, String, 'trim', String);
	
  Blockly.Language.generic_upCase = Blockly.Language.makeOneArgumentPrimitive(Blockly.LANG_CATEGORY_TEXT, ' ', ' ', 160, String, 'upcase', String);
		
  Blockly.Language.generic_downCase = Blockly.Language.makeOneArgumentPrimitive(Blockly.LANG_CATEGORY_TEXT, ' ', ' ', 160, String, 'downcase', String);
};
*/