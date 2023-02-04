// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

goog.provide('AI.Blockly.Msg');
goog.require('Blockly.Msg');

// noinspection JSUnresolvedVariable
Object.keys(_translations).forEach(function (key) {
  // noinspection JSUnresolvedVariable
  Blockly.Msg[key] = _translations[key];
});
