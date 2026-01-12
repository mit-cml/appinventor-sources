// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

goog.provide('AI.Blockly.Mixins');

goog.require('AI.Blockly.Mixins.DynamicConnections');
goog.require('AI.Blockly.Mixins.LexicalVariableMethods');

AI.Blockly.Mixins.extend = function (target, source) {
  for (var prop in source) {
    target[prop] = source[prop];
  }
};
