// -*- mode: Javascript; js-indent-level: 2; -*-
// Copyright © 2015-2016 MIT, All rights reserved
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

goog.provide('AI.Blockly.Util');

Blockly.Util = {};

// Blockly.Util.Dialog -- A way to get GWT Dialogs to appear from the top window.
// There is some hair here because we need this code to work both when the GWT code is
// compiled and optimized and when this code is compiled with the closure compiler.
// So we call up to GWT to create the actual dialog, hide the dialog and change the
// dialog's content. We pass the callback as a GWT "JavaScriptObject" which is then
// passed back to javascript for actual evaluation. The way we do this results in no
// argument being passed to the callback. If in the future we need to pass an arugment
// we can worry about adding that functionality.

Blockly.Util.Dialog = function(title, content, buttonName, destructive, cancelButtonName, size, callback) {
    this.title = title;
    this.content = content;
    this.size = size;
    this.buttonName = buttonName;
    this.destructive = destructive;
    this.cancelButtonName = cancelButtonName;
    this.callback = callback;
    if (this.buttonName) {
        this.display();
    }
};

Blockly.Util.Dialog.prototype = {
    'display' : function() {
        this._dialog = top.BlocklyPanel_createDialog(this.title, this.content, this.buttonName, this.destructive, this.cancelButtonName, this.size, this.callback);
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

/**
 * Convert between HTML coordinates and SVG coordinates.
 * @param {number} x X input coordinate.
 * @param {number} y Y input coordinate.
 * @param {boolean} toSvg True to convert to SVG coordinates.
 *     False to convert to mouse/HTML coordinates.
 * @return {!Object} Object with x and y properties in output coordinates.
 */
Blockly.convertCoordinates = function(workspace, x, y, toSvg) {
  if (toSvg) {
    x -= window.scrollX || window.pageXOffset;
    y -= window.scrollY || window.pageYOffset;
  }
  var svg = workspace.getParentSvg();
  var svgPoint = svg.createSVGPoint();
  svgPoint.x = x;
  svgPoint.y = y;
  var matrix = svg.getScreenCTM();
  if (toSvg) {
    matrix = matrix.inverse();
  }
  var xy = svgPoint.matrixTransform(matrix);
  if (!toSvg) {
    xy.x += window.scrollX || window.pageXOffset;
    xy.y += window.scrollY || window.pageYOffset;
  }
  return xy;
};

/**
 * Return the absolute coordinates of the top-left corner of this element.
 * The origin (0,0) is the top-left corner of the page body.
 * @param {!Element} element Element to find the coordinates of.
 * @param {!Blockly.Workspace} workspace Source workspace to use as a reference frame.
 * @return {!Object} Object with .x and .y properties.
 * @private
 */
Blockly.getAbsoluteXY_ = function(element, workspace) {
  var xy = workspace.getSvgXY(element);
  return Blockly.convertCoordinates(workspace, xy.x, xy.y, false);
};

/**
 * Return the coordinates of the top-left corner of this element relative to
 * its parent.
 * @param {!Element} element Element to find the coordinates of.
 * @return {!Object} Object with .x and .y properties.
 * @private
 */
Blockly.getRelativeXY_ = function(element) {
  var xy = {x: 0, y: 0};
  // First, check for x and y attributes.
  var x = element.getAttribute('x');
  if (x) {
    xy.x = parseInt(x, 10);
  }
  var y = element.getAttribute('y');
  if (y) {
    xy.y = parseInt(y, 10);
  }
  // Second, check for transform="translate(...)" attribute.
  var transform = element.getAttribute('transform');
  // Note that Firefox and IE (9,10) return 'translate(12)' instead of
  // 'translate(12, 0)'.
  // Note that IE (9,10) returns 'translate(16 8)' instead of
  // 'translate(16, 8)'.
  var r = transform &&
          transform.match(/translate\(\s*([-\d.]+)([ ,]\s*([-\d.]+)\s*\))?/);
  if (r) {
    xy.x += parseInt(r[1], 10);
    if (r[3]) {
      xy.y += parseInt(r[3], 10);
    }
  }
  return xy;
};
