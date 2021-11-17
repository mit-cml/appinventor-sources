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
 * @param {!ParameterDescriptor} param The parameter this flydown is representing.
 * @param {!Blockly.ComponentDatabase} componentDb The component database the
 *     previous ParameterDescriptor is associated with.
 * @param {string=} opt_displayLocation The location to display the flydown at
 *     Either: Blockly.FieldFlydown.DISPLAY_BELOW,
 *             Blockly.FieldFlydown.DISPLAY_RIGHT
 *     Defaults to DISPLAY_RIGHT.
 */
Blockly.FieldEventFlydown = function(param, componentDb, opt_displayLocation) {
    this.componentDb = componentDb;
    this.param = param;

    var name = componentDb.getInternationalizedParameterName(param.name);

    Blockly.FieldEventFlydown.superClass_.constructor.call(
        this, name, false, opt_displayLocation);
}
goog.inherits(Blockly.FieldEventFlydown, Blockly.FieldParameterFlydown);

Blockly.FieldEventFlydown.prototype.flydownBlocksXML_ = function() {
  // TODO: Refactor this to use getValue() instead of getText(). getText()
  //   refers to the view, while getValue refers to the model (in MVC terms).

  var name = this.getText();
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
            name +
          '</field>' +
        '</block>' +
        '<block type="lexical_variable_set">' +
          mutation +
          '<field name="VAR">' +
            name +
          '</field>' +
          helper +
        '</block>' +
      '</xml>';
  return getterSetterXML;
}
