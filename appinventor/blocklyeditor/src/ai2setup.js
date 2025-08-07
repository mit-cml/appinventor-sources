// -*- mode: javascript;js-indent-level: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

top.Blockly = Blockly;
top.LexicalVariablesPlugin = LexicalVariablesPlugin;

// Compatibility shim for Blockly v11: expose inputTypes directly on Blockly for legacy AI2 code
// TODO: might not be needed anymore after changing it in a number of other places
if (!Blockly.inputTypes && Blockly.inputs && Blockly.inputs.inputTypes) {
  Blockly.inputTypes = Blockly.inputs.inputTypes;
}

// Install the translations from AI2.Msg into Blockly.Msg
Object.keys(top.AI2.Msg).forEach(function (key) {
  Blockly.Msg[key] = top.AI2.Msg[key];
});
