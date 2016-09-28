// -*- mode: java; c-basic-offset: 2; -*-
/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * Copyright © 2013-2016 Massachusetts Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @license
 * @fileoverview Object representing a warning for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author fraser@google.com (Neil Fraser)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.Error');

goog.require('Blockly.Bubble');
goog.require('Blockly.Icon');


/**
 * Class for an error.
 * @param {!Blockly.Block} block The block associated with this error.
 * @constructor
 */
Blockly.Error = function(block) {
  Blockly.Error.superClass_.constructor.call(this, block);
  this.createIcon();
  this.text_ = {};
};
goog.inherits(Blockly.Error, Blockly.Icon);

/**
 * If set to true, the icon will be hidden when the block it is
 * attached to is collapsed. Otherwise, the icon will be visible even
 * if the block is collapsed.
 */
Blockly.Error.prototype.collapseHidden = false;

/**
 * Radius of the warning icon.
 */
Blockly.Error.ICON_RADIUS = 8;

/**
 * Create the icon on the block.
 * @private
 */
Blockly.Error.prototype.drawIcon_ = function(group) {
  /* Here's the markup that will be generated:
  <g class="blocklyIconGroup">
    <path class="blocklyIconShield" d="..."/>
    <text class="blocklyIconMark" x="8" y="13">!</text>
  </g>
  */
  Blockly.utils.createSvgElement('circle',
      {'class': 'blocklyErrorIconOutline',
       'r': Blockly.Error.ICON_RADIUS,
       'cx': Blockly.Error.ICON_RADIUS,
       'cy': Blockly.Error.ICON_RADIUS}, group);
  Blockly.utils.createSvgElement('path',
      {'class': 'blocklyErrorIconX',
       'd': 'M 4,4 12,12 8,8 4,12 12,4'},
                           // X fills circle vvv
       //'d': 'M 3.1931458,3.1931458 12.756854,12.756854 8,8 3.0931458,12.756854 12.756854,3.0931458'},
      group);
};

/**
 * Create the text for the error's bubble.
 * @param {string} text The text to display.
 * @return {!Element} The top-level node of the text.
 * @private
 */
Blockly.Error.textToDom_ = function(text) {
  var paragraph = Blockly.utils.createSvgElement('text',
      {'class': 'blocklyText blocklyBubbleText', 'y': Blockly.Bubble.BORDER_WIDTH}, null);
  var lines = text.split('\n');
  for (var i = 0; i < lines.length; i++) {
    var tspanElement = Blockly.utils.createSvgElement('tspan',
        {'dy': '1em', 'x': Blockly.Bubble.BORDER_WIDTH}, paragraph);
    var textNode = document.createTextNode(lines[i]);
    tspanElement.appendChild(textNode);
  }
  return paragraph;
};

/**
 * Show or hide the error bubble.
 * @param {boolean} visible True if the bubble should be visible.
 */
Blockly.Error.prototype.setVisible = function(visible) {
  if (visible == this.isVisible()) {
    // No change.
    return;
  }
  if (visible) {
    // Create the bubble.
    var paragraph = Blockly.Error.textToDom_(this.getText());
    this.bubble_ = new Blockly.Bubble(
        /** @type {!Blockly.Workspace} */ (this.block_.workspace),
        paragraph, this.block_.svgPath_, this.iconXY_, null, null);
    if (this.block_.RTL) {
      // Right-align the paragraph.
      // This cannot be done until the bubble is rendered on screen.
      var maxWidth = paragraph.getBBox().width;
      for (var x = 0, textElement; textElement = paragraph.childNodes[x]; x++) {
        textElement.setAttribute('text-anchor', 'end');
        textElement.setAttribute('x', maxWidth + Blockly.Bubble.BORDER_WIDTH);
      }
    }
    this.updateColour();
    // Bump the warning into the right location.
    var size = this.bubble_.getBubbleSize();
    this.bubble_.setBubbleSize(size.width, size.height);
  } else {
    // Dispose of the bubble.
    this.bubble_.dispose();
    this.bubble_ = null;
    this.body_ = null;
  }
};

/**
 * Bring the target to the top of the stack when clicked on.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Error.prototype.bodyFocus_ = function(e) {
  this.bubble_.promote_();
};

/**
 * Set this error's text.
 * @param {string} text Error text.
 */
Blockly.Error.prototype.setText = function(text, id) {
  if (this.text_[id] == text) {
    return;
  }
  if (text) {
    this.text_[id] = text;
  } else {
    delete this.text_[id];
  }
  if (this.isVisible()) {
    this.setVisible(false);
    this.setVisible(true);
  }
};

/**
 * Get this error's texts.
 * @return {string} All texts concatenated into one string.
 */
Blockly.Error.prototype.getText = function() {
  var allErrors = [];
  for (var id in this.text_) {
    allErrors.push(this.text_[id]);
  }
  return allErrors.join('\n');
};

/**
 * Dispose of this error.
 */
Blockly.Error.prototype.dispose = function() {
  this.block_.error = null;
  Blockly.Icon.prototype.dispose.call(this);
};
