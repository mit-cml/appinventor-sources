Blockly.Yail = Blockly.Generator.get('Yail');

Blockly.Yail.color = function() {
  // Convert hex value to numeric value
  var code = -1 * (window.Math.pow(16,6) - window.parseInt("0x" + this.getTitleValue('COLOR').substr(1)));
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail.color_black = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_blue = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_cyan = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_dark_gray = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_gray = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_green = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_light_gray = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_magenta = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_pink = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_red = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_white = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_orange = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_yellow = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail.color_make_color = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'RED', Blockly.Yail.ORDER_NONE) || 1;
  var argument1 = Blockly.Yail.valueToCode(this, 'GREEN', Blockly.Yail.ORDER_NONE) || 1;
  var argument2 = Blockly.Yail.valueToCode(this, 'BLUE', Blockly.Yail.ORDER_NONE) || 1;
  var argument3 = Blockly.Yail.valueToCode(this, 'OPACITY', Blockly.Yail.ORDER_NONE) || 1;
  var code ="(call-yail-primitive make-color (*list-for-runtime* (call-yail-primitive make-yail-list (*list-for-runtime* "
           + argument0 + Blockly.Yail.YAIL_SPACER
           + argument1 + Blockly.Yail.YAIL_SPACER
           + argument2 + Blockly.Yail.YAIL_SPACER
           + argument3
           +") '(any any any any) \"make a list\")) '(list) \"make-color\")";
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.color_split_color = function() {
  var mode = this.getTitleValue('OP');
  var tuple = Blockly.Yail.color_split_color.OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = Blockly.Yail.valueToCode(this, 'COLOR', order) || 0;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator2
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.color_split_color.OPERATORS = {
  RED: ['split-color-red', 'split the red from the color', Blockly.Yail.ORDER_NONE],
  GREEN: ['split-color-green', 'split the green from the color', Blockly.Yail.ORDER_NONE],
  BLUE: ['split-color-blue', 'split the blue from the color', Blockly.Yail.ORDER_NONE],
  OPACITY: ['split-color-opacity', 'split the opacity from the color', Blockly.Yail.ORDER_NONE]
};
