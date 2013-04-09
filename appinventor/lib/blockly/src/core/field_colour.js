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
 * @fileoverview Colour input field.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

goog.provide('Blockly.FieldColour');

goog.require('Blockly.Field');
goog.require('goog.ui.ColorPicker');


/**
 * Class for a colour input field.
 * @param {string} colour The initial colour in '#rrggbb' format.
 * @param {Function} opt_changeHandler A function that is executed when a new
 *     option is selected.
 * @extends {Blockly.Field}
 * @constructor
 */
Blockly.FieldColour = function(colour, opt_changeHandler) {
  Blockly.FieldColour.superClass_.constructor.call(this, '\u00A0\u00A0\u00A0');

  this.changeHandler_ = opt_changeHandler;
  this.borderRect_.style.fillOpacity = 1;
  // Set the initial state.
  this.setValue(colour);
};
goog.inherits(Blockly.FieldColour, Blockly.Field);

/**
 * Is there a colour palette open?
 * @private
 */
Blockly.FieldColour.isOpen_ = false;

/**
 * Mouse cursor style when over the hotspot that initiates the editor.
 */
Blockly.FieldColour.prototype.CURSOR = 'default';

/**
 * Return the current colour.
 * @return {string} Current colour in '#rrggbb' format.
 */
Blockly.FieldColour.prototype.getValue = function() {
  return this.colour_;
};

/**
 * Set the colour.
 * @param {string} colour The new colour in '#rrggbb' format.
 */
Blockly.FieldColour.prototype.setValue = function(colour) {
  this.colour_ = colour;
  this.borderRect_.style.fill = colour;
  if (this.sourceBlock_ && this.sourceBlock_.rendered) {
    this.sourceBlock_.workspace.fireChangeEvent();
  }
};

/**
 * Create a palette under the colour field.
 * @private
 */
Blockly.FieldColour.prototype.showEditor_ = function() {
  // Create the palette using Closure.
  Blockly.FieldColour.isOpen_ = true;
  goog.dom.removeChildren(Blockly.widgetDiv);
  Blockly.widgetDiv.style.display = 'block';
  var picker = goog.ui.ColorPicker.createSimpleColorGrid();
  picker.render(Blockly.widgetDiv);
  picker.setSelectedColor(this.getValue());

  // Position the palette to line up with the field.
  var xy = Blockly.getAbsoluteXY_(/** @type {!Element} */ (this.borderRect_));
  var borderBBox = this.borderRect_.getBBox();
  if (Blockly.RTL) {
    xy.x += borderBBox.width;
  }
  xy.y += borderBBox.height - 1;
  xy = Blockly.convertCoordinates(xy.x, xy.y, false);
  if (Blockly.RTL) {
    xy.x -= Blockly.widgetDiv.offsetWidth;
  }
  Blockly.widgetDiv.style.left = xy.x + 'px';
  Blockly.widgetDiv.style.top = xy.y + 'px';

  // Configure event handler.
  var thisObj = this;
  Blockly.FieldColour.changeEventKey_ = goog.events.listen(picker,
      goog.ui.ColorPicker.EventType.CHANGE,
      function(event) {
        var colour = event.target.getSelectedColor() || '#000000';
        Blockly.FieldColour.hide();
        if (thisObj.changeHandler_) {
          // Call any change handler, and allow it to override.
          var override = thisObj.changeHandler_(colour);
          if (override !== undefined) {
            colour = override;
          }
        }
        if (colour !== null) {
          thisObj.setValue(colour);
        }
      });
};

/**
 * Hide the colour palette.
 */
Blockly.FieldColour.hide = function() {
  if (Blockly.FieldColour.isOpen_) {
    Blockly.widgetDiv.style.display = 'none';
    goog.dom.removeChildren(Blockly.widgetDiv);
    Blockly.FieldColour.isOpen_ = false;
    if (Blockly.FieldColour.changeEventKey_) {
      goog.events.unlistenByKey(Blockly.FieldColour.changeEventKey_);
    }
  }
};
