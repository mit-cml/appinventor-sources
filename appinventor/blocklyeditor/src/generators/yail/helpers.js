// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

Blockly.Yail['helpers_dropdown'] = function() {
  var optionList = this.workspace.getComponentDatabase()
      .getOptionList(this.key_);
  var enumConstantName = this.getFieldValue('OPTION');

  var option = optionList.options.find(function(opt) {
    return opt.name == enumConstantName;
  });

  // See https://www.gnu.org/software/kawa/Enumerations.html
  var enumValue = '(static-field ' + optionList.className + ' "' + enumConstantName + '")';

  var concreteValue = option.value;
  if (optionList.underlyingType == "java.lang.String") {
    concreteValue = Blockly.Yail.quote_(concreteValue);
  } // Otherwise assume it doesn't need to be quoted.

  // protect-enum is a macro which checks if the companion supports OptionLists
  // and if it does it will return the abstract enum value. If the companion
  // does not support OptionLists it will continue to return the concrete value.
  if (Blockly.Yail.forRepl) {
    var code = '(protect-enum ' + enumValue + ' ' + concreteValue + ')';
  } else {
    code = enumValue;
  }

  return [code, Blockly.Yail.ORDER_ATOMIC];
}

Blockly.Yail['helpers_screen_names'] = function() {
  var value = Blockly.Yail.quote_(this.getFieldValue('SCREEN'));
  return [value, Blockly.Yail.ORDER_ATOMIC];
}

Blockly.Yail['helpers_assets'] = function() {
  var field = this.getField('ASSET');
  if (!field) {
    return [Blockly.Yail.quote_(''), Blockly.Yail.ORDER_ATOMIC];
  }
  return [Blockly.Yail.quote_(field.getValue()), Blockly.Yail.ORDER_ATOMIC];
}
