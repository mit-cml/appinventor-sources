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

  var code = option.value;
  if (optionList.underlyingType == "java.lang.String") {
    code = Blockly.Yail.quote_(code);
  } // Otherwise assume it doesn't need to be quoted.

  return [code, Blockly.Yail.ORDER_ATOMIC]

  // TODO: This will be used after we add abstraction.
  // See https://www.gnu.org/software/kawa/Enumerations.html
  // var code = optionList.className + ":" + enumConstantName;

  // Currently we are returning the concrete values of the optionList option for
  // easy backwards compatibility with the companion. But in the future we will
  // return a macro which checks if the companion supports OptionLists and if
  // it does it will return the abstract enum value. If the companion does not
  // support OptionLists it will continue to return the concrete value.
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
