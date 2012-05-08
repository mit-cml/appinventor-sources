/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
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
 * @fileoverview Library to create tooltips for Blockly.
 * First, call Blockly.Tooltip.init() after onload.
 * Second, set the 'tooltip' property on any SVG element that needs a tooltip.
 * If the tooltip is a string, then that message will be displayed.
 * If the tooltip is an SVG element, then that object's tooltip will be used.
 * Third, call Blockly.Tooltip.bindMouseEvents(e) passing the SVG element.
 * @author fraser@google.com (Neil Fraser)
 */

// Tooltip Engine
Blockly.Tooltip = {};

// PID of suspended threads.
Blockly.Tooltip.mouseOutPid = 0;
Blockly.Tooltip.showPid = 0;

// Last observed location of the mouse pointer (freezes when tooltip appears).
Blockly.Tooltip.lastX = 0;
Blockly.Tooltip.lastY = 0;

// Is a tooltip currently showing?
Blockly.Tooltip.visible = false;

// Current element being pointed at.
Blockly.Tooltip.element = null;
Blockly.Tooltip.poisonedElement = null;

// References to the SVG elements.
Blockly.Tooltip.svgGroup_ = null;
Blockly.Tooltip.svgText_ = null;
Blockly.Tooltip.svgBackground_ = null;
Blockly.Tooltip.svgShadow_ = null;

// Various constants.
Blockly.Tooltip.OFFSET_X = 0;  // Offset between mouse cursor and tooltip.
Blockly.Tooltip.OFFSET_Y = 10;
Blockly.Tooltip.RADIUS_OK = 10;  // Radius mouse can move before killing tip.
Blockly.Tooltip.HOVER_MS = 1000;  // Delay before tooltip appears.
Blockly.Tooltip.MARGINS = 5;  // Horizontal padding between text and background.

/**
 * Create the tooltip elements.  Only needs to be called once.
 * @return {!Element} The tooltip's SVG group.
 */
Blockly.Tooltip.createDom = function() {
  /*
  <g class="blocklyHidden">
    <rect class="blocklyTooltipShadow" x="2" y="2"/>
    <rect class="blocklyTooltipBackground"/>
    <text class="blocklyTooltipText"></text>
  </g>
  */
  var svgGroup =
      Blockly.createSvgElement('g', {'class': 'blocklyHidden'}, null);
  Blockly.Tooltip.svgGroup_ = svgGroup;
  Blockly.Tooltip.svgShadow_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyTooltipShadow', x: 2, y: 2}, svgGroup);
  Blockly.Tooltip.svgBackground_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyTooltipBackground'}, svgGroup);
  Blockly.Tooltip.svgText_ = Blockly.createSvgElement('text',
      {'class': 'blocklyTooltipText'}, svgGroup);
  return svgGroup;
};

/**
 * Binds the required mouse events onto an SVG element.
 * @param {!Element} element SVG element onto which tooltip is to be bound.
 */
Blockly.Tooltip.bindMouseEvents = function(element) {
  Blockly.bindEvent_(element, 'mouseover', null, Blockly.Tooltip.onMouseOver_);
  Blockly.bindEvent_(element, 'mouseout', null, Blockly.Tooltip.onMouseOut_);
  Blockly.bindEvent_(element, 'mousemove', null, Blockly.Tooltip.onMouseMove_);
};

/**
 * Hide the tooltip if the mouse is over a different object.
 * Initialize the tooltip to potentially appear for this object.
 * @param {!Event} e Mouse event.
 * @private
 */
Blockly.Tooltip.onMouseOver_ = function(e) {
  // If the tooltip is an object, treat it as a pointer to the next object in
  // the chain to look at.  Terminate when a string is found.
  var element = e.target;
  while (typeof element.tooltip == 'object') {
    element = element.tooltip;
  }
  if (Blockly.Tooltip.element != element) {
    Blockly.Tooltip.hide();
    Blockly.Tooltip.poisonedElement = null;
    Blockly.Tooltip.element = element;
  }
  // Forget about any immediately preceeding mouseOut event.
  window.clearTimeout(Blockly.Tooltip.mouseOutPid);
};

/**
 * Hide the tooltip if the mouse leaves the object and enters the workspace.
 * @param {!Event} e Mouse event.
 * @private
 */
Blockly.Tooltip.onMouseOut_ = function(e) {
  // Moving from one element to another (overlapping or with no gap) generates
  // a mouseOut followed instantly by a mouseOver.  Fork off the mouseOut
  // event and kill it if a mouseOver is received immediately.
  // This way the task only fully executes if mousing into the void.
  Blockly.Tooltip.mouseOutPid = window.setTimeout(function() {
        Blockly.Tooltip.element = null;
        Blockly.Tooltip.poisonedElement = null;
        Blockly.Tooltip.hide();
      }, 1);
  window.clearTimeout(Blockly.Tooltip.showPid);
};

/**
 * When hovering over an element, schedule a tooltip to be shown.  If a tooltip
 * is already visible, hide it if the mouse strays out of a certain radius.
 * @param {!Event} e Mouse event.
 * @private
 */
Blockly.Tooltip.onMouseMove_ = function(e) {
  if (!Blockly.Tooltip.element || !Blockly.Tooltip.element.tooltip) {
    // No tooltip here to show.
    return;
  } else if ((Blockly.ContextMenu && Blockly.ContextMenu.visible) ||
             Blockly.Block.dragMode_ != 0) {
    // Don't display a tooltip when a context menu is active, or during a drag.
    return;
  }
  if (Blockly.Tooltip.visible) {
    // Compute the distance between the mouse position when the tooltip was
    // shown and the current mouse position.  Pythagorean theorem.
    var dx = Blockly.Tooltip.lastX - e.clientX;
    var dy = Blockly.Tooltip.lastY - e.clientY;
    var dr = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    if (dr > Blockly.Tooltip.RADIUS_OK) {
      Blockly.Tooltip.hide();
    }
  } else if (Blockly.Tooltip.poisonedElement != Blockly.Tooltip.element) {
    // The mouse moved, clear any previously scheduled tooltip.
    window.clearTimeout(Blockly.Tooltip.showPid);
    // Maybe this time the mouse will stay put.  Schedule showing of tooltip.
    Blockly.Tooltip.lastX = e.clientX;
    Blockly.Tooltip.lastY = e.clientY;
    Blockly.Tooltip.showPid =
        window.setTimeout(Blockly.Tooltip.show_, Blockly.Tooltip.HOVER_MS);
  }
};

/**
 * Hide the tooltip.
 */
Blockly.Tooltip.hide = function() {
  if (Blockly.Tooltip.visible) {
    Blockly.Tooltip.visible = false;
    if (Blockly.Tooltip.svgGroup_) {
      Blockly.Tooltip.svgGroup_.style.display = 'none';
    }
  }
  window.clearTimeout(Blockly.Tooltip.showPid);
};

/**
 * Create the tooltip and show it.
 * @private
 */
Blockly.Tooltip.show_ = function() {
  Blockly.Tooltip.visible = true;
  Blockly.Tooltip.poisonedElement = Blockly.Tooltip.element;
  if (!Blockly.Tooltip.svgGroup_) {
    return;
  }
  // Erase all existing text.
  Blockly.removeChildren_(Blockly.Tooltip.svgText_);
  // Create new text, line by line.
  var lines = Blockly.Tooltip.element.tooltip.split('\n');
  for (var i = 0; i < lines.length; i++) {
    var tspanElement = Blockly.createSvgElement('tspan',
        {dy: '1em', x: Blockly.Tooltip.MARGINS}, Blockly.Tooltip.svgText_);
    var textNode = Blockly.svgDoc.createTextNode(lines[i]);
    tspanElement.appendChild(textNode);
  }
  // Display the tooltip.
  Blockly.Tooltip.svgGroup_.style.display = 'block';
  // Resize the background and shadow to fit.
  var bb = Blockly.Tooltip.svgText_.getBBox();
  var width = 2 * Blockly.Tooltip.MARGINS + bb.width;
  var height = bb.height;
  Blockly.Tooltip.svgBackground_.setAttribute('width', width);
  Blockly.Tooltip.svgBackground_.setAttribute('height', height);
  Blockly.Tooltip.svgShadow_.setAttribute('width', width);
  Blockly.Tooltip.svgShadow_.setAttribute('height', height);
  // Move the tooltip to just below the cursor.
  var left = Blockly.Tooltip.lastX;
  if (Blockly.RTL) {
    left -= Blockly.Tooltip.OFFSET_X + width;
  } else {
    left += Blockly.Tooltip.OFFSET_X;
  }
  var top = Blockly.Tooltip.lastY + Blockly.Tooltip.OFFSET_Y;
  // Update Blockly's knowledge of its own location.
  Blockly.svgResize();
  var svgSize = Blockly.svgSize();
  left -= svgSize.left;
  top -= svgSize.top;
  Blockly.Tooltip.svgGroup_.setAttribute('transform',
      'translate(' + left + ',' + top + ')');
};
