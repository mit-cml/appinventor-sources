// -*- mode: java; c-basic-offset: 2; -*-

/**
 * @license
 * @fileoverview UDOO Blockly yail generators for MIT App Inventor.
 * @author francesco.monte@gmail.com (Francesco Montefoschi)
 */

'use strict';

Blockly.Yail['arduino_pin_mode'] = function() {
  var code = Blockly.Yail.quote_(this.getFieldValue('ARDUINO_PIN_MODE'));
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['arduino_pin_value'] = function() {
  var code = Blockly.Yail.quote_(this.getFieldValue('ARDUINO_PIN_VALUE'));
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['udoo_analog_pinout'] = function() {
  var code = Blockly.Yail.quote_(this.getFieldValue('UDOO_ANALOG_PINOUT'));
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['udoo_digital_pinout'] = function() {
  var code = Blockly.Yail.quote_(this.getFieldValue('UDOO_DIGITAL_PINOUT'));
  return [code, Blockly.Yail.ORDER_ATOMIC];
};
