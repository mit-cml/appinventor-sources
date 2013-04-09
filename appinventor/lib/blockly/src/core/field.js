/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * http://blockly.googlecode.com/
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
 * @fileoverview Input field.  Used for editable titles, variables, etc.
 * This is an abstract class that defines the UI on the block.  Actual
 * instances would be Blockly.FieldTextInput, Blockly.FieldDropdown, etc.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

goog.provide('Blockly.Field');

// TODO(scr): Fix circular dependencies
// goog.require('Blockly.Block');
goog.require('Blockly.BlockSvg');


/**
 * Class for an editable field.
 * @param {string} text The initial content of the field.
 * @constructor
 */
Blockly.Field = function(text) {
  this.sourceBlock_ = null;
  // Build the DOM.
  this.group_ = Blockly.createSvgElement('g', {}, null);
  this.borderRect_ = Blockly.createSvgElement('rect',
      {'rx': 4,
       'ry': 4,
       'x': -Blockly.BlockSvg.SEP_SPACE_X / 2,
       'y': -12,
       'height': 16}, this.group_);
  this.textElement_ = Blockly.createSvgElement('text',
      {'class': 'blocklyText'}, this.group_);
  if (this.CURSOR) {
    // Different field types show different cursor hints.
    this.group_.style.cursor = this.CURSOR;
  }
  this.size_ = {height: 25, width: 0};
  this.setText(text);
};

/**
 * Non-breaking space.
 */
Blockly.Field.NBSP = '\u00A0';

/**
 * Editable fields are saved by the XML renderer, non-editable fields are not.
 */
Blockly.Field.prototype.EDITABLE = true;

/**
 * Install this field on a block.
 * @param {!Blockly.Block} block The block containing this field.
 */
Blockly.Field.prototype.init = function(block) {
  if (this.sourceBlock_) {
    throw 'Field has already been initialized once.';
  }
  this.sourceBlock_ = block;
  this.group_.setAttribute('class',
      block.editable ? 'blocklyEditableText' : 'blocklyNonEditableText');
  block.getSvgRoot().appendChild(this.group_);
  if (block.editable) {
    this.mouseUpWrapper_ =
        Blockly.bindEvent_(this.group_, 'mouseup', this, this.onMouseUp_);
  }
  // Bump to set the colours for dropdown arrows.
  this.setText(null);
};

/**
 * Dispose of all DOM objects belonging to this editable field.
 */
Blockly.Field.prototype.dispose = function() {
  if (this.mouseUpWrapper_) {
    Blockly.unbindEvent_(this.mouseUpWrapper_);
    this.mouseUpWrapper_ = null;
  }
  this.sourceBlock_ = null;
  goog.dom.removeNode(this.group_);
  this.group_ = null;
  this.textElement_ = null;
  this.borderRect_ = null;
};

/**
 * Sets whether this editable field is visible or not.
 * @param {boolean} visible True if visible.
 */
Blockly.Field.prototype.setVisible = function(visible) {
  this.getRootElement().style.display = visible ? 'block' : 'none';
};

/**
 * Gets the group element for this editable field.
 * Used for measuring the size and for positioning.
 * @return {!Element} The group element.
 */
Blockly.Field.prototype.getRootElement = function() {
  return /** @type {!Element} */ (this.group_);
};

/**
 * Draws the border with the correct width.
 * Saves the computed width in a property.
 * @private
 */
Blockly.Field.prototype.render_ = function() {
  var width = this.textElement_.getComputedTextLength();
  if (this.borderRect_) {
    this.borderRect_.setAttribute('width',
        width + Blockly.BlockSvg.SEP_SPACE_X);
  }
  this.size_.width = width;
};

/**
 * Returns the height and width of the title.
 * @return {!Object} Height and width.
 */
Blockly.Field.prototype.getSize = function() {
  if (!this.size_.width) {
    this.render_();
  }
  return this.size_;
};

/**
 * Get the text from this field.
 * @return {string} Current text.
 */
Blockly.Field.prototype.getText = function() {
  return this.text_;
};

/**
 * Set the text in this field.  Trigger a rerender of the source block.
 * @param {?string} text New text.
 */
Blockly.Field.prototype.setText = function(text) {
  if (text === null) {
    // No change if null.
    return;
  }
  this.text_ = text;
  // Empty the text element.
  goog.dom.removeChildren(/** @type {!Element} */ (this.textElement_));
  // Replace whitespace with non-breaking spaces so the text doesn't collapse.
  text = text.replace(/\s/g, Blockly.Field.NBSP);
  if (!text) {
    // Prevent the field from disappearing if empty.
    text = Blockly.Field.NBSP;
  }
  var textNode = document.createTextNode(text);
  this.textElement_.appendChild(textNode);

  // Cached width is obsolete.  Clear it.
  this.size_.width = 0;

  if (this.sourceBlock_ && this.sourceBlock_.rendered) {
    this.sourceBlock_.render();
    this.sourceBlock_.bumpNeighbours_();
    this.sourceBlock_.workspace.fireChangeEvent();
  }
};

/**
 * By default there is no difference between the human-readable text and
 * the language-neutral values.  Subclasses (such as dropdown) may define this.
 * @return {string} Current text.
 */
Blockly.Field.prototype.getValue = function() {
  return this.getText();
};

/**
 * By default there is no difference between the human-readable text and
 * the language-neutral values.  Subclasses (such as dropdown) may define this.
 * @param {string} text New text.
 */
Blockly.Field.prototype.setValue = function(text) {
  this.setText(text);
};

/**
 * Handle a mouse up event on an editable field.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Field.prototype.onMouseUp_ = function(e) {
  if (Blockly.isRightButton(e)) {
    // Right-click.
    return;
  } else if (Blockly.Block.dragMode_ == 2) {
    // Drag operation is concluding.  Don't open the editor.
    return;
  }
  // Non-abstract sub-classes must define a showEditor_ method.
  this.showEditor_();
};

/**
 * Change the tooltip text for this field.
 * @param {string|!Element} newTip Text for tooltip or a parent element to
 *     link to for its tooltip.
 */
Blockly.Field.prototype.setTooltip = function(newTip) {
  // Non-abstract sub-classes may wish to implement this.  See FieldLabel.
};
