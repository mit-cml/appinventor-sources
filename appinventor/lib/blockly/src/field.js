/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/google-blockly/
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

/**
 * Class for an editable field.
 * @param {?string} text The initial content of the field.
 * @constructor
 */
Blockly.Field = function(text) {
  if (text === null) {
    // This is a Field instance to be used in inheritance.
    return;
  }
  this.sourceBlock_ = null;
  // Build the DOM.
  this.group_ = Blockly.createSvgElement('g', {}, null);
  this.textElement_ = Blockly.createSvgElement('text',
      {'class': 'blocklyText'}, this.group_);
  this.borderRect_ = Blockly.createSvgElement('rect', {}, this.group_);
  this.highlight_ = Blockly.createSvgElement('path',
      {'class': 'blocklyEditableHighlight'}, this.group_);
  if (this.CURSOR) {
    // Different field types show different cursor hints.
    this.borderRect_.style.cursor = this.CURSOR;
  }
  this.setText(text);
};

/**
 * Non-breaking space.
 */
Blockly.Field.NBSP = '\u00A0';

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
  block.svg_.svgGroup_.appendChild(this.group_);
  if (block.editable) {
    Blockly.bindEvent_(this.borderRect_, 'mouseup', this, this.onMouseUp_);
  }
};

/**
 * Destroy all DOM objects belonging to this editable field.
 */
Blockly.Field.prototype.destroy = function() {
  this.group_.parentNode.removeChild(this.group_);
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
  return this.group_;
};

/**
 * Draws the border in the correct location.
 * Returns the resulting bounding box.
 * @return {!Object} Object containing width/height/x/y properties.
 */
Blockly.Field.prototype.render = function() {
  var bBox = this.textElement_.getBBox();
  if (bBox.height == 0) {
    bBox.height = 18;
  }
  var width = bBox.width + Blockly.BlockSvg.SEP_SPACE_X;
  var height = bBox.height;
  var left = bBox.x - Blockly.BlockSvg.SEP_SPACE_X / 2;
  var top = bBox.y;
  this.borderRect_.setAttribute('width', width);
  this.borderRect_.setAttribute('height', height);
  this.borderRect_.setAttribute('x', left);
  this.borderRect_.setAttribute('y', top);
  var path = 'M ' + (left + width) + ',' + top;
  path += ' v ' + height + ' h -' + width;
  this.highlight_.setAttribute('d', path);
  return bBox;
};

/**
 * Returns the width of the title.
 * @return {number} Width.
 */
Blockly.Field.prototype.width = function() {
  var bBox = this.render();
  if (bBox.width == -Infinity) {
    // Opera has trouble with bounding boxes around empty objects.
    return 0;
  }
  return bBox.width;
};

/**
 * Set the text in this field.  Trigger a rerender of the source block.
 * @param {string} text New text.
 */
Blockly.Field.prototype.setText = function(text) {
  this.text_ = text;
  // Empty the text element.
  Blockly.removeChildren_(this.textElement_);
  // Replace whitespace with non-breaking spaces so the text doesn't collapse.
  text = text.replace(/\s/g, Blockly.Field.NBSP);
  if (!text) {
    // Prevent the field from disappearing if empty.
    text = Blockly.Field.NBSP;
  }
  var textNode = Blockly.svgDoc.createTextNode(text);
  this.textElement_.appendChild(textNode);

  if (this.sourceBlock_ && this.sourceBlock_.rendered) {
    this.sourceBlock_.render();
    this.sourceBlock_.bumpNeighbours_();
  }
};

/**
 * Get the text from this field.
 * @return {string} Current text.
 */
Blockly.Field.prototype.getText = function() {
  return this.text_;
};

/**
 * Handle a mouse up event on an editable field.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Field.prototype.onMouseUp_ = function(e) {
  if (e.button == 2) {
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
