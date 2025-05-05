// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @license
 * @fileoverview Non-editable field with flydown representing a component event
 *     parameter. Necessary to support eventparam mutation and helper blocks in
 *     the flydown.
 */

'use strict';

goog.provide('AI.Blockly.FieldEventFlydown');

goog.require('AI.Blockly.FieldParameterFlydown');

/**
 * Flydown field representing a component event parameter.
 */
Blockly.FieldEventFlydown = class extends Blockly.FieldParameterFlydown {
  /**
   * Create a new FieldEventFlydown.
   * @param {!ParameterDescriptor} param The parameter this flydown is representing.
   * @param {!Blockly.ComponentDatabase} componentDb The component database the
   *     previous ParameterDescriptor is associated with.
   * @param {string=} opt_displayLocation The location to display the flydown at
   *     Either: Blockly.FieldFlydown.DISPLAY_BELOW,
   *             Blockly.FieldFlydown.DISPLAY_RIGHT
   *     Defaults to DISPLAY_RIGHT.   */
  constructor(param, componentDb, opt_displayLocation) {
    super(componentDb.getInternationalizedParameterName(param.name), false, opt_displayLocation);
    this.componentDb = componentDb;
    this.param = param;
  }

  isSerializable() {
    return false;
  }
}

Blockly.FieldEventFlydown.prototype.flydownBlocksXML_ = function() {
  var mutation =
      '<mutation>' +
        '<eventparam name="' + this.param.name + '" />' +
      '</mutation>';
  var helper = '';
  if (this.param.helperKey) {
    var xml = Blockly.Util.xml.valueWithHelperXML('VALUE', this.param.helperKey);
    helper = Blockly.Xml.domToText(xml.firstChild);
  }

  var getterSetterXML =
      '<xml>' +
        '<block type="lexical_variable_get">' +
          mutation +
          '<field name="VAR">' +
            this.param.name +
          '</field>' +
        '</block>' +
        '<block type="lexical_variable_set">' +
          mutation +
          '<field name="VAR">' +
            this.param.name +
          '</field>' +
          helper +
        '</block>' +
      '</xml>';
  return getterSetterXML;
}
