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
 * @fileoverview Dropdown input field.  Used for editable titles and variables.
 * In the interests of a consistent UI, the toolbox shares some functions and
 * properties with the context menu.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for an editable dropdown field.
 * @param {!Function} menuGenerator A function that returns an array of options
 *     for a dropdown list.
 * @param {Function} opt_changeHandler A function that is executed when a new
 *     option is selected.
 * @constructor
 */
Blockly.FieldDropdown = function(menuGenerator, opt_changeHandler) {
  var options = menuGenerator.call(this);
  firstOption = options[0][0];
  // Call parent's constructor.
  Blockly.Field.call(this, firstOption);
  this.menuGenerator_ = menuGenerator;
  this.changeHandler_ = opt_changeHandler;
};

// FieldDropdown is a subclass of Field.
Blockly.FieldDropdown.prototype = new Blockly.Field(null);

/**
 * Create the dropdown field's elements.  Only needs to be called once.
 * @return {!Element} The field's SVG group.
 */
Blockly.FieldDropdown.createDom = function() {
  /*
  <g class="blocklyHidden">
    <rect class="blocklyDropdownMenuShadow" x="0" y="1" rx="2" ry="2"/>
    <rect x="-2" y="-1" rx="2" ry="2"/>
    <g class="blocklyDropdownMenuOptions">
    </g>
  </g>
  */
  var svgGroup = Blockly.createSvgElement('g', {'class': 'blocklyHidden'},
                                          null);
  Blockly.FieldDropdown.svgGroup_ = svgGroup;
  Blockly.FieldDropdown.svgShadow_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyDropdownMenuShadow',
      x: 0, y: 1, rx: 2, ry: 2}, svgGroup);
  Blockly.FieldDropdown.svgBackground_ = Blockly.createSvgElement('rect',
      {x: -2, y: -1, rx: 2, ry: 2,
      filter: 'url(#blocklyEmboss)'}, svgGroup);
  Blockly.FieldDropdown.svgOptions_ = Blockly.createSvgElement('g',
      {'class': 'blocklyDropdownMenuOptions'}, svgGroup);
  return svgGroup;
};

/**
 * Corner radius of the dropdown background.
 */
Blockly.FieldDropdown.CORNER_RADIUS = 2;

/**
 * Mouse cursor style when over the hotspot that initiates the editor.
 */
Blockly.FieldDropdown.prototype.CURSOR = 'default';

/**
 * Create a dropdown menu under the text.
 * @private
 */
Blockly.FieldDropdown.prototype.showEditor_ = function() {
  var svgGroup = Blockly.FieldDropdown.svgGroup_;
  var svgOptions = Blockly.FieldDropdown.svgOptions_;
  var svgBackground = Blockly.FieldDropdown.svgBackground_;
  var svgShadow = Blockly.FieldDropdown.svgShadow_;
  // Erase all existing options.
  Blockly.removeChildren_(svgOptions);
  // The menu must be made visible early since otherwise BBox and
  // getComputedTextLength will return 0.
  svgGroup.style.display = 'block';

  function callbackFactory(text) {
    return function(e) {
      if (this.changeHandler_) {
        this.changeHandler_(text);
      } else {
        this.setText(text);
      }
      // This mouse click has been handled, don't bubble up to document.
      e.stopPropagation();
    };
  }

  var maxWidth = 0;
  var resizeList = [];
  var checkElement = null;
  var options = this.menuGenerator_();
  for (var x = 0; x < options.length; x++) {
    var text = options[x][0];  // Human-readable text.
    var value = options[x][1]; // Language-neutral value.
    var gElement = Blockly.ContextMenu.optionToDom(text);
    var rectElement = gElement.firstChild;
    var textElement = gElement.lastChild;
    svgOptions.appendChild(gElement);
    // Add a checkmark next to the current item.
    if (!checkElement && text == this.text_) {
      checkElement = Blockly.createSvgElement('text',
          {'class': 'blocklyMenuText', y: 15}, null);
      // Insert the checkmark between the rect and text, thus preserving the
      // ability to reference them as firstChild and lastChild respectively.
      gElement.insertBefore(checkElement, textElement);
      checkElement.appendChild(Blockly.svgDoc.createTextNode('\u2713'));
    }

    gElement.setAttribute('transform',
        'translate(0, ' + (x * Blockly.ContextMenu.Y_HEIGHT) + ')');
    resizeList.push(rectElement);
    Blockly.bindEvent_(gElement, 'mousedown', null, Blockly.noEvent);
    Blockly.bindEvent_(gElement, 'mouseup', this, callbackFactory(text));
    Blockly.bindEvent_(gElement, 'mouseup', null,
                       Blockly.FieldDropdown.hideMenu);
    // Compute the length of the longest text length.
    maxWidth = Math.max(maxWidth, textElement.getComputedTextLength());
  }
  // Run a second pass to resize all options to the required width.
  maxWidth += Blockly.ContextMenu.X_PADDING * 2;
  for (var x = 0; x < resizeList.length; x++) {
    resizeList[x].setAttribute('width', maxWidth);
  }
  if (Blockly.RTL) {
    // Right-align the text.
    for (var x = 0, gElement; gElement = svgOptions.childNodes[x]; x++) {
      var textElement = gElement.lastChild;
      textElement.setAttribute('x', maxWidth -
          textElement.getComputedTextLength() - Blockly.ContextMenu.X_PADDING);
    }
  }
  if (checkElement) {
    if (Blockly.RTL) {
      // Research indicates that RTL checkmarks are supposed to be drawn the
      // same in the same direction as LTR checkmarks.  It's only the alignment
      // that needs to change.
      checkElement.setAttribute('x',
          maxWidth - 5 - checkElement.getComputedTextLength());
    } else {
      checkElement.setAttribute('x', 5);
    }
  }
  var width = maxWidth + Blockly.FieldDropdown.CORNER_RADIUS * 2;
  var height = options.length * Blockly.ContextMenu.Y_HEIGHT +
               Blockly.FieldDropdown.CORNER_RADIUS + 1;
  svgShadow.setAttribute('width', width);
  svgShadow.setAttribute('height', height);
  svgBackground.setAttribute('width', width);
  svgBackground.setAttribute('height', height);
  var hexColour = Blockly.makeColour(this.sourceBlock_.getColour());
  svgBackground.setAttribute('fill', hexColour);
  // Position the dropdown to line up with the field.
  var xy = Blockly.getAbsoluteXY_(this.borderRect_);
  var borderBBox = this.borderRect_.getBBox();
  var x;
  if (Blockly.RTL) {
    x = xy.x - maxWidth + Blockly.ContextMenu.X_PADDING + borderBBox.width -
        Blockly.BlockSvg.SEP_SPACE_X / 2;
  } else {
    x = xy.x - Blockly.ContextMenu.X_PADDING + Blockly.BlockSvg.SEP_SPACE_X / 2;
  }
  svgGroup.setAttribute('transform',
      'translate(' + x + ', ' + (xy.y + borderBBox.height) + ')');
};

/**
 * Hide the dropdown menu.
 */
Blockly.FieldDropdown.hideMenu = function() {
  Blockly.FieldDropdown.svgGroup_.style.display = 'none';
};

/**
 * Get the language-neutral value from this dropdown menu.
 * @return {string} Current text.
 */
Blockly.FieldDropdown.prototype.getValue = function() {
  var selectedText = this.text_;
  var options = this.menuGenerator_();
  for (var x = 0; x < options.length; x++) {
    // Options are tuples of human-readable text and language-neutral values.
    if (options[x][0] == selectedText) {
      return options[x][1];
    }
  }
  throw '"' + selectedText + '" not valid in dropdown.';
};

/**
 * Set the language-neutral value for this dropdown menu.
 * @param {string} newValue New value to set.
 */
Blockly.FieldDropdown.prototype.setValue = function(newValue) {
  var options = this.menuGenerator_();
  for (var x = 0; x < options.length; x++) {
    // Options are tuples of human-readable text and language-neutral values.
    if (options[x][1] == newValue) {
      this.setText(options[x][0]);
      return;
    }
  }
  throw '"' + newValue + '" not found in dropdown.';
};

