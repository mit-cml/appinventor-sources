// -*- mode: Javascript; js-indent-level: 4; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle warnings in the block editor.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

'use strict';

goog.provide('Blockly.Util');

// Blockly.Util.Dialog -- A way to get GWT Dialogs to appear from the top window.
// There is some hair here because we need this code to work both when the GWT code is
// compiled and optimized and when this code is compiled with the closure compiler.
// So we call up to GWT to create the actual dialog, hide the dialog and change the
// dialog's content. We pass the callback as a GWT "JavaScriptObject" which is then
// passed back to javascript for actual evaluation. The way we do this results in no
// argument being passed to the callback. If in the future we need to pass an arugment
// we can worry about adding that functionality.

Blockly.Util.Dialog = function(title, content, buttonName, cancelButtonName, size, callback) {
    this.title = title;
    this.content = content;
    this.size = size;
    this.buttonName = buttonName;
    this.cancelButtonName = cancelButtonName;
    this.callback = callback;
    if (this.buttonName) {
        this.display();
    }
};

Blockly.Util.Dialog.prototype = {
    'display' : function() {
        this._dialog = top.BlocklyPanel_createDialog(this.title, this.content, this.buttonName, this.cancelButtonName, this.size, this.callback);
    },
    'hide' : function() {
        if (this._dialog) {
            top.BlocklyPanel_hideDialog(this._dialog);
            this._dialog = null;
        }
    },
    'setContent' : function(message) {
        if (this._dialog) {
            top.BlocklyPanel_setDialogContent(this._dialog, message);
        }
    }
};
