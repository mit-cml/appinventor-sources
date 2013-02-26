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

Blockly.Yail.color_yellow = function() {
  return Blockly.Yail.color.call(this);
};

