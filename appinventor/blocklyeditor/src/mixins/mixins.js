goog.provide('AI.Blockly.Mixins');

goog.require('AI.Blockly.Mixins.DynamicConnections');

AI.Blockly.Mixins.extend = function (target, source) {
  for (var prop in source) {
    target[prop] = source[prop];
  }
};
