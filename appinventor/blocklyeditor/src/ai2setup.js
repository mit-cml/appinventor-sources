// -*- mode: javascript;js-indent-level: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

// Install the translations from AI2.Msg into Blockly.Msg
Object.keys(top.AI2.Msg).forEach(function (key) {
  top.Blockly.Msg[key] = top.AI2.Msg[key];
});
