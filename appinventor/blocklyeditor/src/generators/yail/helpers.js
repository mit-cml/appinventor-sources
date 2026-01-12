// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

goog.provide('AI.Yail.helpers');

AI.Yail['helpers_dropdown'] = function() {
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
    concreteValue = AI.Yail.quote_(concreteValue);
  } // Otherwise assume it doesn't need to be quoted.

  // protect-enum is a macro which checks if the companion supports OptionLists
  // and if it does it will return the abstract enum value. If the companion
  // does not support OptionLists it will continue to return the concrete value.
  if (AI.Yail.forRepl) {
    var code = '(protect-enum ' + enumValue + ' ' + concreteValue + ')';
  } else {
    code = enumValue;
  }

  return [code, AI.Yail.ORDER_ATOMIC];
}

AI.Yail['helpers_screen_names'] = function() {
  var value = AI.Yail.quote_(this.getFieldValue('SCREEN'));
  return [value, AI.Yail.ORDER_ATOMIC];
}

AI.Yail['helpers_assets'] = function() {
  var field = this.getField('ASSET');
  if (!field) {
    return [AI.Yail.quote_(''), AI.Yail.ORDER_ATOMIC];
  }
  return [AI.Yail.quote_(field.getValue()), AI.Yail.ORDER_ATOMIC];
}

AI.Yail['helpers_providermodel'] = function() {
  var field = this.getField('PROVIDERMODEL');
  if (!field) {
    return [AI.Yail.quote_(''), AI.Yail.ORDER_ATOMIC];
  }
  var fielddisplayvalue = field.getValue();
  var fieldvalue = top.chatproxyinfo['model'][fielddisplayvalue];
  return [AI.Yail.quote_(fieldvalue), AI.Yail.ORDER_ATOMIC];
}

AI.Yail['helpers_provider'] = function() {
  var field = this.getField('PROVIDER');
  if (!field) {
    return [AI.Yail.quote_(''), AI.Yail.ORDER_ATOMIC];
  }
  var fieldvalue = field.getValue();
  return [AI.Yail.quote_(fieldvalue), AI.Yail.ORDER_ATOMIC];
}
