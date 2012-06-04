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
 * @fileoverview Methods for graphically rendering a block as SVG.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a block's SVG representation.
 * @param {!Blockly.Block} block The underlying block object.
 * @constructor
 */
Blockly.BlockSvg = function(block) {
  this.block_ = block;
  // Create core elements for the block.
  this.svgGroup_ = Blockly.createSvgElement('g', {}, null);
  this.svgPathDark_ = Blockly.createSvgElement('path',
      {transform: 'translate(1, 1)'}, this.svgGroup_);
  this.svgPath_ = Blockly.createSvgElement('path', {}, this.svgGroup_);
  this.svgPathLight_ = Blockly.createSvgElement('path',
      {'fill': 'none', 'stroke-width': 2, 'stroke-linecap': 'round'},
      this.svgGroup_);
  this.svgPath_.tooltip = this.block_;
  Blockly.Tooltip && Blockly.Tooltip.bindMouseEvents(this.svgPath_);
  if (block.editable) {
    Blockly.addClass_(this.svgGroup_, 'blocklyDraggable');
  }
};

/**
 * Initialize the SVG representation with any block attributes which have
 * already been defined.
 */
Blockly.BlockSvg.prototype.init = function() {
  var block = this.block_;
  this.updateColour();
  for (var x = 0; x < block.titleRow.length; x++) {
    block.titleRow[x].init(block, this);
  }
  for (var x = 0, input; input = block.inputList[x]; x++) {
    if (input.label) {
      input.label.init(block, this);
    }
    if (input.type == Blockly.LOCAL_VARIABLE) {
      input.init(block, this);
    }
  }
  if (block.mutator) {
    block.mutator.createIcon();
  }
};

/**
 * Get the root SVG node.
 * @return {!Node} The root SVG node.
 */
Blockly.BlockSvg.prototype.getRootNode = function() {
  return this.svgGroup_;
};

// UI constants for rendering blocks.
Blockly.BlockSvg.SEP_SPACE_X = 10;   // Horizontal space between elements.
Blockly.BlockSvg.SEP_SPACE_Y = 5;    // Vertical space between elements.
Blockly.BlockSvg.MIN_BLOCK_Y = 25;   // Minimum height of a block.
Blockly.BlockSvg.TAB_HEIGHT = 20;    // Height of horizontal puzzle tab.
Blockly.BlockSvg.TAB_WIDTH = 8;      // Width of horizontal puzzle tab.
Blockly.BlockSvg.NOTCH_WIDTH = 30;   // Width of vertical tab (inc left margin).
Blockly.BlockSvg.CORNER_RADIUS = 8;  // Rounded corner radius.
// Distance from shape edge to intersect with a curved corner at 45 degrees.
// Applies to highlighting on around the inside of a curve.
Blockly.BlockSvg.DISTANCE_45_INSIDE = (1 - Math.SQRT1_2) *
      (Blockly.BlockSvg.CORNER_RADIUS - 1) + 1;
// Distance from shape edge to intersect with a curved corner at 45 degrees.
// Applies to highlighting on around the outside of a curve.
Blockly.BlockSvg.DISTANCE_45_OUTSIDE = (1 - Math.SQRT1_2) *
      (Blockly.BlockSvg.CORNER_RADIUS + 1) - 1;

/**
 * SVG path for drawing next/previous notch from left to right.
 */
Blockly.BlockSvg.NOTCH_PATH_LEFT = 'l 6,4 3,0 6,-4';
/**
 * SVG path for drawing next/previous notch from left to right with
 * highlighting.
 */
Blockly.BlockSvg.NOTCH_PATH_LEFT_HIGHLIGHT = 'l 6.5,4 2,0 6.5,-4';
/**
 * SVG path for drawing next/previous notch from right to left.
 */
Blockly.BlockSvg.NOTCH_PATH_RIGHT = 'l -6,4 -3,0 -6,-4';
/**
 * SVG path for drawing jagged teeth at the end of collapsed blocks.
 */
Blockly.BlockSvg.JAGGED_TEETH = 'l 8,0 0,4 8,4 -16,8 8,4';
/**
 * SVG path for drawing a horizontal puzzle tab from top to bottom.
 */
Blockly.BlockSvg.TAB_PATH_DOWN = 'v 5 c 0,10 -' + Blockly.BlockSvg.TAB_WIDTH +
    ',-8 -' + Blockly.BlockSvg.TAB_WIDTH + ',7.5 s ' +
    Blockly.BlockSvg.TAB_WIDTH + ',-2.5 ' + Blockly.BlockSvg.TAB_WIDTH + ',7.5';
/**
 * SVG path for drawing a horizontal puzzle tab from top to bottom with
 * highlighting from the upper-right.
 */
Blockly.BlockSvg.TAB_PATH_DOWN_HIGHLIGHT_RTL = 'v 6.5 m -' +
    (Blockly.BlockSvg.TAB_WIDTH * 0.98) + ',2.5 q -' +
    (Blockly.BlockSvg.TAB_WIDTH * .05) + ',10 ' +
    (Blockly.BlockSvg.TAB_WIDTH * .27) + ',10 m ' +
    (Blockly.BlockSvg.TAB_WIDTH * .71) + ',-2.5 v 1.5';

/**
 * Destroy this SVG block.
 */
Blockly.BlockSvg.prototype.destroy = function() {
  this.svgGroup_.parentNode.removeChild(this.svgGroup_);
  // Sever JavaScript to DOM connections.
  this.svgGroup_ = null;
  this.svgPath_ = null;
  this.svgPathLight_ = null;
  this.svgPathDark_ = null;
  // Break circular references.
  this.block_ = null;
};

/**
 * Change the colour of a block.
 */
Blockly.BlockSvg.prototype.updateColour = function() {
  var hexColour = Blockly.makeColour(this.block_.getColour());
  var r = window.parseInt(hexColour.charAt(1), 16);
  var g = window.parseInt(hexColour.charAt(2), 16);
  var b = window.parseInt(hexColour.charAt(3), 16);
  var HEX = '0123456789abcdef';
  var rLight = HEX.charAt(Math.min(r + 3, 15));
  var gLight = HEX.charAt(Math.min(g + 2, 15));
  var bLight = HEX.charAt(Math.min(b + 2, 15));
  var rDark = HEX.charAt(Math.max(r - 4, 0));
  var gDark = HEX.charAt(Math.max(g - 4, 0));
  var bDark = HEX.charAt(Math.max(b - 4, 0));
  this.svgPathLight_.setAttribute('stroke', '#' + rLight + gLight + bLight);
  this.svgPathDark_.setAttribute('fill', '#' + rDark + gDark + bDark);
  this.svgPath_.setAttribute('fill', hexColour);
};

/**
 * Select this block.  Highlight it visually.
 */
Blockly.BlockSvg.prototype.addSelect = function() {
  Blockly.addClass_(this.svgPath_, 'blocklySelected');
  this.svgPathLight_.setAttribute('display', 'none');
  // Move the selected block to the top of the stack.
  this.svgGroup_.parentNode.appendChild(this.svgGroup_);
};

/**
 * Unselect this block.  Remove its highlighting.
 */
Blockly.BlockSvg.prototype.removeSelect = function() {
  Blockly.removeClass_(this.svgPath_, 'blocklySelected');
  this.svgPathLight_.setAttribute('display', 'block');
};

/**
 * Adds the dragging class to this block.
 * Also disables the highlights/shadows to improve performance.
 */
Blockly.BlockSvg.prototype.addDragging = function() {
  Blockly.addClass_(this.svgPath_, 'blocklyDragging');
  Blockly.addClass_(this.svgPathLight_, 'blocklyDragging');
  this.svgPathDark_.style.display = 'none';
};

/**
 * Removes the dragging class from this block.
 */
Blockly.BlockSvg.prototype.removeDragging = function() {
  Blockly.removeClass_(this.svgPath_, 'blocklyDragging');
  Blockly.removeClass_(this.svgPathLight_, 'blocklyDragging');
  this.svgPathDark_.style.display = 'block';
};

/**
 * Render the block.
 * Lays out and reflows a block based on its contents and settings.
 */
Blockly.BlockSvg.prototype.render = function() {
  this.block_.rendered = true;
  var titleY = 18;
  if (!this.block_.collapsed &&
      this.block_.inputsInline && this.block_.inputList.length &&
      this.block_.inputList[0].type == Blockly.INPUT_VALUE) {
    // Lower the title elements a bit in order to line up with the first
    // row of inline labels.
    titleY += Blockly.BlockSvg.SEP_SPACE_Y;
  }
  var titleX = Blockly.RTL ?
      this.renderTitleRTL_(titleY) : this.renderTitleLTR_(titleY);
  var inputRows = this.renderCompute_(this.block_.inputList);
  this.renderDraw_(titleX, inputRows);

  // Render all blocks above this one (propagate a reflow).
  var parentBlock = this.block_.getParent();
  if (parentBlock) {
    parentBlock.render();
  } else {
    // Top-most block.  Fire an event to allow scrollbars to resize.
    Blockly.fireUiEvent(Blockly.svgDoc, window, 'resize');
  }
};

/**
 * Render the title row as right-to-left.
 * @param {number} titleY Vertical offset for text.
 * @return {number} Width of row.
 * @private
 */
Blockly.BlockSvg.prototype.renderTitleRTL_ = function(titleY) {
  var titleX = -Blockly.BlockSvg.SEP_SPACE_X;
  var iconWidth;
  // Move the comment icon into position.
  if (this.block_.comment) {
    iconWidth = this.block_.comment.renderIcon(titleX);
    if (iconWidth) {
      titleX -= iconWidth + Blockly.BlockSvg.SEP_SPACE_X;
    }
  }
  // Move the mutator icon into position.
  if (this.block_.mutator) {
    iconWidth = this.block_.mutator.renderIcon(titleX);
    if (iconWidth) {
      titleX -= iconWidth + Blockly.BlockSvg.SEP_SPACE_X;
    }
  }

  // Move the title element(s) into position.
  for (var x = 0, title; title = this.block_.titleRow[x]; x++) {
    var titleWidth = title.width();
    titleX -= titleWidth;
    title.getRootElement().setAttribute('transform',
        'translate(' + titleX + ', ' + titleY + ')');
    if (titleWidth) {
      titleX -= Blockly.BlockSvg.SEP_SPACE_X;
    }
  }

  if (this.block_.previousConnection || this.block_.nextConnection) {
    titleX = Math.min(titleX, -Blockly.BlockSvg.NOTCH_WIDTH -
                              Blockly.BlockSvg.SEP_SPACE_X);
  }
  return -titleX;
};

/**
 * Render the title row as left-to-right.
 * @param {number} titleY Vertical offset for text.
 * @return {number} Width of row.
 * @private
 */
Blockly.BlockSvg.prototype.renderTitleLTR_ = function(titleY) {
  var titleX = Blockly.BlockSvg.SEP_SPACE_X;
  var iconWidth;
  // Move the comment icon into position.
  if (this.block_.comment) {
    iconWidth = this.block_.comment.renderIcon(titleX);
    if (iconWidth) {
      titleX += iconWidth + Blockly.BlockSvg.SEP_SPACE_X;
    }
  }
  // Move the mutator icon into position.
  if (this.block_.mutator) {
    iconWidth = this.block_.mutator.renderIcon(titleX);
    if (iconWidth) {
      titleX += iconWidth + Blockly.BlockSvg.SEP_SPACE_X;
    }
  }

  // Move the title element(s) into position.
  for (var x = 0, title; title = this.block_.titleRow[x]; x++) {
    title.getRootElement().setAttribute('transform',
        'translate(' + titleX + ', ' + titleY + ')');
    var titleWidth = title.width();
    if (titleWidth) {
      titleX += titleWidth + Blockly.BlockSvg.SEP_SPACE_X;
    }
  }

  if (this.block_.previousConnection || this.block_.nextConnection) {
    titleX = Math.max(titleX, Blockly.BlockSvg.NOTCH_WIDTH +
                              Blockly.BlockSvg.SEP_SPACE_X);
  }
  return titleX;
};

/**
 * Computes the locations for all input elements.
 * @param {!Array.<!Array>} inputList Tuples containing the label element and
 *     the input type.
 * @return {!Array.<!Array.<!Object>>} 2D array of objects, each containing
 *     position information.
 * @private
 */
Blockly.BlockSvg.prototype.renderCompute_ = function(inputList) {
  var inputRows = [];
  inputRows.labelValueWidth = 0;  // Width of longest value 1st input label.
  inputRows.labelVariableWidth = 0;  // Width of longest variable label.
  inputRows.labelStatementWidth = 0;  // Width of longest statement label.
  inputRows.hasValue = false;
  inputRows.hasVariable = false;
  inputRows.hasStatement = false;
  var lastType = null;
  if (this.block_.collapsed) {
    // Collapsed blocks have no visible inputs.
    return inputRows;
  }
  for (var i = 0; i < inputList.length; i++) {
    var input = inputList[i];
    var row;
    if (lastType != Blockly.INPUT_VALUE || input.type != Blockly.INPUT_VALUE ||
        !this.block_.inputsInline) {
      // Create new row.
      lastType = input.type;
      var row = [];
      row.type = input.type;
      row.height = 0;
      inputRows.push(row);
    } else {
      row = inputRows[inputRows.length - 1];
    }
    row.push(input);

    // Compute minimum input size.
    input.height = Blockly.BlockSvg.MIN_BLOCK_Y;
    if (row.type == Blockly.INPUT_VALUE) {
      input.width = Blockly.BlockSvg.TAB_WIDTH + Blockly.BlockSvg.SEP_SPACE_X;
    } else {
      // The width is currently only needed for inline value inputs.
      input.width = NaN;
    }
    // Expand input size if there is a connection.
    if (input.targetConnection) {
      var linkedBlock = input.targetBlock().getSvgRoot();
      try {
        var bBox = linkedBlock.getBBox();
      } catch (e) {
        // Firefox has trouble with hidden elements (Bug 528969).
        var bBox = {height: 0, width: 0};
      }
      if (window.navigator.userAgent.indexOf('AppleWebKit/') != -1) {
        /* HACK:
         The current versions of Chrome (16.0) and Safari (5.1) with a common
         root of WebKit 535 has a size reporting bug where the height of a
         block is 5 pixels too large.  If WebKit browsers start under-sizing
         connections to other blocks, then delete this entire hack.
        */
        bBox.height -= 5;
      }
      // Subtract one from the height due to the shadow.
      input.height = Math.max(input.height, bBox.height - 1);
      input.width = Math.max(input.width, bBox.width);
    }

    row.height = Math.max(row.height, input.height);
    if (input.label) {
      if (input.label.getComputedTextLength) {
        // Plain text label.
        input.labelWidth = input.label.getComputedTextLength();
      } else {
        // Editable label.
        var labelBox = input.label.render();
        input.labelWidth = labelBox ? labelBox.width : 0;
      }
    } else {
      // No label.
      input.labelWidth = 0;
    }

    if (row.type == Blockly.INPUT_VALUE) {
      inputRows.hasValue = true;
      if (row.length == 1) {
        inputRows.labelValueWidth = Math.max(inputRows.labelValueWidth,
                                             input.labelWidth);
      }
    } else if (row.type == Blockly.NEXT_STATEMENT) {
      inputRows.hasStatement = true;
      inputRows.labelStatementWidth = Math.max(inputRows.labelStatementWidth,
                                               input.labelWidth);
    } else if (row.type == Blockly.LOCAL_VARIABLE) {
      inputRows.hasVariable = true;
      inputRows.labelVariableWidth = Math.max(inputRows.labelVariableWidth,
                                           input.labelWidth);
    }
  }

  // Make inline rows a bit thicker in order to enclose the values.
  if (this.block_.inputsInline && inputRows.hasValue) {
    for (var y = 0; y < inputRows.length; y++) {
      var row = inputRows[y];
      if (row.type == Blockly.INPUT_VALUE) {
        row.height += 2 * Blockly.BlockSvg.SEP_SPACE_Y;
      }
    }
  }
  return inputRows;
};


/**
 * Draw the path of the block.
 * Move the labels to the correct locations.
 * @param {number} titleX Horizontal space taken up by the title.
 * @param {!Array.<!Array.<!Object>>} inputRows 2D array of objects, each
 *     containing position information.
 * @private
 */
Blockly.BlockSvg.prototype.renderDraw_ = function(titleX, inputRows) {
  // Fetch the block's coordinates on the surface for use in anchoring
  // the connections.
  var connectionsXY = this.block_.getRelativeToSurfaceXY();
  // Compute the preferred right edge.  Inline blocks may extend beyond.
  var rightEdge = titleX;
  if (inputRows.hasStatement) {
    rightEdge = Math.max(rightEdge,
        Blockly.BlockSvg.SEP_SPACE_X + inputRows.labelStatementWidth +
        Blockly.BlockSvg.SEP_SPACE_X + Blockly.BlockSvg.NOTCH_WIDTH);
  }
  if (inputRows.hasValue) {
    rightEdge = Math.max(rightEdge, titleX +
        (inputRows.labelValueWidth ? inputRows.labelValueWidth +
        Blockly.BlockSvg.SEP_SPACE_X : 0) + Blockly.BlockSvg.TAB_WIDTH);
  }
  if (inputRows.hasVariable) {
    rightEdge = Math.max(rightEdge, titleX +
        (inputRows.labelVariableWidth ? inputRows.labelVariableWidth +
        Blockly.BlockSvg.SEP_SPACE_X : 0) + Blockly.BlockSvg.TAB_WIDTH);
  }

  // Assemble the block's path.
  var steps = [];
  var inlineSteps = [];
  // The highlighting applies to edges facing the upper-left corner.
  // Since highlighting is a two-pixel wide border, it would normally overhang
  // the edge of the block by a pixel. So undersize all measurements by a pixel.
  var highlightSteps = [];
  var highlightInlineSteps = [];

  this.renderDrawTop_(steps, highlightSteps, connectionsXY, rightEdge);
  var cursorY = this.renderDrawRight_(steps, highlightSteps, inlineSteps,
      highlightInlineSteps, connectionsXY, rightEdge, inputRows, titleX);
  this.renderDrawBottom_(steps, highlightSteps, connectionsXY, cursorY);
  this.renderDrawLeft_(steps, highlightSteps, connectionsXY, cursorY);

  var pathString = steps.join(' ') + '\n' + inlineSteps.join(' ');
  this.svgPath_.setAttribute('d', pathString);
  this.svgPathDark_.setAttribute('d', pathString);
  pathString = highlightSteps.join(' ') + '\n' + highlightInlineSteps.join(' ');
  this.svgPathLight_.setAttribute('d', pathString);
  if (Blockly.RTL) {
    // Mirror the block's path.
    this.svgPath_.setAttribute('transform', 'scale(-1 1)');
    this.svgPathLight_.setAttribute('transform', 'scale(-1 1)');
    this.svgPathDark_.setAttribute('transform', 'translate(1,1) scale(-1 1)');
  }
};

/**
 * Render the top edge of the block.
 * @param {!Array.<string>} steps Path of block outline.
 * @param {!Array.<string>} highlightSteps Path of block highlights.
 * @param {!Object} connectionsXY Location of block.
 * @param {number} rightEdge Minimum width of block.
 * @private
 */
Blockly.BlockSvg.prototype.renderDrawTop_ = function(steps, highlightSteps,
                                                   connectionsXY, rightEdge) {
  // Position the cursor at the top-left starting point.
  if (this.block_.outputConnection) {
    steps.push('m 0,0');
    highlightSteps.push('m 1,1');
  } else {
    steps.push('m 0,' + Blockly.BlockSvg.CORNER_RADIUS);
    if (Blockly.RTL) {
      highlightSteps.push('m ' + Blockly.BlockSvg.DISTANCE_45_INSIDE + ',' +
                          Blockly.BlockSvg.DISTANCE_45_INSIDE);
    } else {
      highlightSteps.push('m 1,' + (Blockly.BlockSvg.CORNER_RADIUS - 1));
    }
    // Top-left rounded corner.
    if (Blockly.BlockSvg.CORNER_RADIUS) {
      steps.push('A', Blockly.BlockSvg.CORNER_RADIUS + ',' +
             Blockly.BlockSvg.CORNER_RADIUS + ' 0 0,1 ' +
             Blockly.BlockSvg.CORNER_RADIUS + ',0');
      highlightSteps.push('A', (Blockly.BlockSvg.CORNER_RADIUS - 1) + ',' +
           (Blockly.BlockSvg.CORNER_RADIUS - 1) + ' 0 0,1 ' +
           Blockly.BlockSvg.CORNER_RADIUS + ',1');
    }
  }

  // Top edge.
  if (this.block_.previousConnection) {
    steps.push('H', Blockly.BlockSvg.NOTCH_WIDTH - 15);
    highlightSteps.push('H', Blockly.BlockSvg.NOTCH_WIDTH - 15);
    steps.push(Blockly.BlockSvg.NOTCH_PATH_LEFT);
    highlightSteps.push(Blockly.BlockSvg.NOTCH_PATH_LEFT_HIGHLIGHT);
    // Create previous block connection.
    var connectionX = connectionsXY.x + (Blockly.RTL ?
        -Blockly.BlockSvg.NOTCH_WIDTH : Blockly.BlockSvg.NOTCH_WIDTH);
    var connectionY = connectionsXY.y;
    this.block_.previousConnection.moveTo(connectionX, connectionY);
    // This connection will be tightened when the parent renders.
  }
  steps.push('H', rightEdge);
  highlightSteps.push('H', rightEdge + (Blockly.RTL ? -1 : 0));
};

/**
 * Render the right edge of the block.
 * @param {!Array.<string>} steps Path of block outline.
 * @param {!Array.<string>} highlightSteps Path of block highlights.
 * @param {!Array.<string>} inlineSteps Inline block outlines.
 * @param {!Array.<string>} highlightInlineSteps Inline block highlights.
 * @param {!Object} connectionsXY Location of block.
 * @param {number} rightEdge Minimum width of block.
 * @param {!Array.<!Array.<!Object>>} inputRows 2D array of objects, each
 *     containing position information.
 * @param {number} titleX Horizontal space taken up by the title.
 * @return {number} Height of block.
 * @private
 */
Blockly.BlockSvg.prototype.renderDrawRight_ = function(steps, highlightSteps,
    inlineSteps, highlightInlineSteps, connectionsXY, rightEdge, inputRows,
    titleX) {
  var cursorX = 0;
  var cursorY = 0;
  var connectionX, connectionY;
  if (inputRows.length) {
    for (var y = 0; y < inputRows.length; y++) {
      highlightSteps.push('M', (rightEdge - 1) + ',' + (cursorY + 1));
      var row = inputRows[y];
      if (row.type == Blockly.INPUT_VALUE) {
        if (this.block_.inputsInline) {
          // Inline inputs.
          cursorX = Math.max(titleX + inputRows.labelValueWidth,
                             inputRows.labelStatementWidth);
          cursorX -= row[0].labelWidth;
          for (var x = 0; x < row.length; x++) {
            var input = row[x];
            if (input.label) {
              var labelElement = input.label.getRootElement();
              var labelX = Blockly.RTL ? -cursorX - input.labelWidth : cursorX;
              var labelY = cursorY + 18 + Blockly.BlockSvg.SEP_SPACE_Y;
              labelElement.setAttribute('transform',
                  'translate(' + labelX + ',' + labelY + ')');
              cursorX += input.labelWidth + Blockly.BlockSvg.SEP_SPACE_X;
            }
            cursorX += input.width + Blockly.BlockSvg.SEP_SPACE_X;
            inlineSteps.push('M', (cursorX - Blockly.BlockSvg.SEP_SPACE_X) +
                             ',' + (cursorY + Blockly.BlockSvg.SEP_SPACE_Y));
            inlineSteps.push('h', Blockly.BlockSvg.TAB_WIDTH - input.width);
            inlineSteps.push(Blockly.BlockSvg.TAB_PATH_DOWN);
            inlineSteps.push('v', input.height - Blockly.BlockSvg.TAB_HEIGHT);
            inlineSteps.push('h', input.width - Blockly.BlockSvg.TAB_WIDTH);
            inlineSteps.push('z');
            if (Blockly.RTL) {
              // Highlight right edge, around back of tab, and bottom.
              highlightInlineSteps.push('M',
                  (cursorX - Blockly.BlockSvg.SEP_SPACE_X +
                   Blockly.BlockSvg.TAB_WIDTH - input.width - 1) + ',' +
                  (cursorY + Blockly.BlockSvg.SEP_SPACE_Y + 1));
              highlightInlineSteps.push(
                  Blockly.BlockSvg.TAB_PATH_DOWN_HIGHLIGHT_RTL);
              highlightInlineSteps.push('v',
                  input.height - Blockly.BlockSvg.TAB_HEIGHT + 2);
              highlightInlineSteps.push('h',
                  input.width - Blockly.BlockSvg.TAB_WIDTH);
            } else {
              // Highlight right edge, bottom, and glint at bottom of tab.
              highlightInlineSteps.push('M',
                  (cursorX - Blockly.BlockSvg.SEP_SPACE_X + 1) + ',' +
                  (cursorY + Blockly.BlockSvg.SEP_SPACE_Y + 1));
              highlightInlineSteps.push('v', input.height);
              highlightInlineSteps.push('h', Blockly.BlockSvg.TAB_WIDTH -
                                             input.width);
              highlightInlineSteps.push('M',
                  (cursorX - input.width - Blockly.BlockSvg.SEP_SPACE_X + 3.8) +
                  ',' + (cursorY + Blockly.BlockSvg.SEP_SPACE_Y +
                         Blockly.BlockSvg.TAB_HEIGHT - 0.4));
              highlightInlineSteps.push('l',
                  (Blockly.BlockSvg.TAB_WIDTH * 0.42) + ',-1.8');
            }
            // Create inline input connection.
            if (Blockly.RTL) {
              connectionX = connectionsXY.x - cursorX -
                  Blockly.BlockSvg.TAB_WIDTH + Blockly.BlockSvg.SEP_SPACE_X +
                  input.width - 1;
            } else {
              connectionX = connectionsXY.x + cursorX +
                  Blockly.BlockSvg.TAB_WIDTH - Blockly.BlockSvg.SEP_SPACE_X -
                  input.width + 1;
            }
            connectionY = connectionsXY.y + cursorY +
                Blockly.BlockSvg.SEP_SPACE_Y;
            input.moveTo(connectionX, connectionY);
            if (input.targetConnection) {
              input.tighten_();
            }
          }
          steps.push('H', cursorX);
          highlightSteps.push('H', cursorX + (Blockly.RTL ? -1 : 0));
          steps.push('v', row.height);
          if (Blockly.RTL) {
            highlightSteps.push('v', row.height - 2);
          }
        } else {
          // External input.
          var input = row[0];
          if (input.label) {
            var labelElement = input.label.getRootElement();
            var labelX = Blockly.RTL ? -rightEdge + Blockly.BlockSvg.TAB_WIDTH +
                Blockly.BlockSvg.SEP_SPACE_X :
                rightEdge - Blockly.BlockSvg.TAB_WIDTH -
                Blockly.BlockSvg.SEP_SPACE_X - input.labelWidth;
            var labelY = cursorY + 18;
            labelElement.setAttribute('transform',
                'translate(' + labelX + ',' + labelY + ')');
          }
          steps.push(Blockly.BlockSvg.TAB_PATH_DOWN);
          steps.push('v', row.height - Blockly.BlockSvg.TAB_HEIGHT);
          if (Blockly.RTL) {
            // Highlight around back of tab.
            highlightSteps.push(Blockly.BlockSvg.TAB_PATH_DOWN_HIGHLIGHT_RTL);
            highlightSteps.push('v', row.height - Blockly.BlockSvg.TAB_HEIGHT);
          } else {
            // Short highlight glint at bottom of tab.
            highlightSteps.push('M', (rightEdge - 4.2) + ',' +
                (cursorY + Blockly.BlockSvg.TAB_HEIGHT - 0.4));
            highlightSteps.push('l', (Blockly.BlockSvg.TAB_WIDTH * 0.42) +
                ',-1.8');
          }
          // Create external input connection.
          connectionX = connectionsXY.x +
              (Blockly.RTL ? -rightEdge - 1 : rightEdge + 1);
          connectionY = connectionsXY.y + cursorY;
          input.moveTo(connectionX, connectionY);
          if (input.targetConnection) {
            input.tighten_();
          }
        }
      } else if (row.type == Blockly.LOCAL_VARIABLE) {
        var input = row[0];
        if (input.label) {
          var labelElement = input.label.getRootElement();
          if (Blockly.RTL) {
            labelElement.setAttribute('x', -rightEdge +
                Blockly.BlockSvg.TAB_WIDTH + Blockly.BlockSvg.SEP_SPACE_X);
          } else {
            labelElement.setAttribute('x', rightEdge -
                Blockly.BlockSvg.TAB_WIDTH - Blockly.BlockSvg.SEP_SPACE_X -
                input.labelWidth);
          }
          labelElement.setAttribute('y', cursorY + 18);
        }
        var fieldGroup = input.getRootElement();
        var bBox = input.render() || {height: 0, width: 0};
        if (Blockly.RTL) {
          fieldGroup.setAttribute('transform', 'translate(' +
              (-rightEdge - bBox.width + Blockly.BlockSvg.SEP_SPACE_X / 2) +
              ', ' + (cursorY + 18) + ')');
        } else {
          fieldGroup.setAttribute('transform', 'translate(' +
              (rightEdge - Blockly.BlockSvg.SEP_SPACE_X / 2) +
              ', ' + (cursorY + 18) + ')');
        }
        var width = bBox.width + Blockly.BlockSvg.SEP_SPACE_X / 2;
        steps.push('H', rightEdge + width);
        steps.push('v', row.height);
        steps.push('h', -width);
        if (Blockly.RTL) {
          highlightSteps.push('h', width);
          highlightSteps.push('v', row.height - 2);
          highlightSteps.push('m', -width + ',0');
        } else {
          highlightSteps.push('h', width + 1);
        }
      } else if (row.type == Blockly.NEXT_STATEMENT) {
        // Nested statement.
        var input = row[0];
        // If the first row is a block, add a header row on top.
        if (y == 0) {
          steps.push('v', Blockly.BlockSvg.MIN_BLOCK_Y);
          if (Blockly.RTL) {
            highlightSteps.push('v', Blockly.BlockSvg.MIN_BLOCK_Y - 2);
          }
          cursorY += Blockly.BlockSvg.MIN_BLOCK_Y;
        }
        if (input.label) {
          var labelElement = input.label.getRootElement();
          if (Blockly.RTL) {
            labelElement.setAttribute('x', -Blockly.BlockSvg.SEP_SPACE_X -
                inputRows.labelStatementWidth);
          } else {
            labelElement.setAttribute('x', Blockly.BlockSvg.SEP_SPACE_X +
                inputRows.labelStatementWidth - input.labelWidth);
          }
          labelElement.setAttribute('y', cursorY + 18);
        }
        cursorX = Blockly.BlockSvg.SEP_SPACE_X + inputRows.labelStatementWidth +
                  Blockly.BlockSvg.SEP_SPACE_X + Blockly.BlockSvg.NOTCH_WIDTH;
        steps.push('H', cursorX);
        steps.push(Blockly.BlockSvg.NOTCH_PATH_RIGHT + ' h -' +
            (Blockly.BlockSvg.NOTCH_WIDTH - 15 -
             Blockly.BlockSvg.CORNER_RADIUS));
        if (Blockly.BlockSvg.CORNER_RADIUS) {
          steps.push('a', Blockly.BlockSvg.CORNER_RADIUS + ',' +
                     Blockly.BlockSvg.CORNER_RADIUS + ' 0 0,0 -' +
                     Blockly.BlockSvg.CORNER_RADIUS + ',' +
                     Blockly.BlockSvg.CORNER_RADIUS);
        }
        steps.push('v', row.height - 2 * Blockly.BlockSvg.CORNER_RADIUS);
        if (Blockly.BlockSvg.CORNER_RADIUS) {
          steps.push('a', Blockly.BlockSvg.CORNER_RADIUS + ',' +
                     Blockly.BlockSvg.CORNER_RADIUS + ' 0 0,0 ' +
                     Blockly.BlockSvg.CORNER_RADIUS + ',' +
                     Blockly.BlockSvg.CORNER_RADIUS);
        }
        steps.push('H', rightEdge);
        if (Blockly.RTL) {
          highlightSteps.push('M',
              (cursorX - Blockly.BlockSvg.NOTCH_WIDTH +
               Blockly.BlockSvg.DISTANCE_45_OUTSIDE) +
              ',' + (cursorY + Blockly.BlockSvg.DISTANCE_45_OUTSIDE));
          highlightSteps.push('a', (Blockly.BlockSvg.CORNER_RADIUS + 1) + ',' +
              (Blockly.BlockSvg.CORNER_RADIUS + 1) + ' 0 0,0 ' +
              (-Blockly.BlockSvg.DISTANCE_45_OUTSIDE - 1) + ',' +
              (Blockly.BlockSvg.CORNER_RADIUS -
               Blockly.BlockSvg.DISTANCE_45_OUTSIDE));
          highlightSteps.push('v',
              row.height - 2 * Blockly.BlockSvg.CORNER_RADIUS);
          highlightSteps.push('a', (Blockly.BlockSvg.CORNER_RADIUS + 1) + ',' +
              (Blockly.BlockSvg.CORNER_RADIUS + 1) + ' 0 0,0 ' +
              (Blockly.BlockSvg.CORNER_RADIUS + 1) + ',' +
              (Blockly.BlockSvg.CORNER_RADIUS + 1));
          highlightSteps.push('H', rightEdge - 1);
        } else {
          highlightSteps.push('M',
              (cursorX - Blockly.BlockSvg.NOTCH_WIDTH +
               Blockly.BlockSvg.DISTANCE_45_OUTSIDE) +
              ',' + (cursorY + row.height -
                     Blockly.BlockSvg.DISTANCE_45_OUTSIDE));
          highlightSteps.push('a', (Blockly.BlockSvg.CORNER_RADIUS + 1) + ',' +
              (Blockly.BlockSvg.CORNER_RADIUS + 1) + ' 0 0,0 ' +
              (Blockly.BlockSvg.CORNER_RADIUS -
               Blockly.BlockSvg.DISTANCE_45_OUTSIDE) + ',' +
              (Blockly.BlockSvg.DISTANCE_45_OUTSIDE + 1));
          highlightSteps.push('H', rightEdge);
        }
        // Create statement connection.
        connectionX = connectionsXY.x + (Blockly.RTL ? -cursorX : cursorX);
        connectionY = connectionsXY.y + cursorY + 1;
        input.moveTo(connectionX, connectionY);
        if (input.targetConnection) {
          input.tighten_();
        }
        if (y == inputRows.length - 1 ||
            inputRows[y + 1].type == Blockly.NEXT_STATEMENT) {
          // If the final input is a block, add a small row underneath.
          // Consecutive blocks are also separated by a small divider.
          steps.push('v', Blockly.BlockSvg.SEP_SPACE_Y);
          if (Blockly.RTL) {
            highlightSteps.push('v', Blockly.BlockSvg.SEP_SPACE_Y - 1);
          }
          cursorY += Blockly.BlockSvg.SEP_SPACE_Y;
        }
      }
      cursorY += row.height;
    }
  } else {
    if (this.block_.collapsed) {
      steps.push(Blockly.BlockSvg.JAGGED_TEETH);
      if (Blockly.RTL) {
        highlightSteps.push('l 8,0 0,3.8 7,3.2 m -14.5,9 l 8,4');
      } else {
        highlightSteps.push('h 8');
      }
    }
    steps.push('V', Blockly.BlockSvg.MIN_BLOCK_Y);
    if (Blockly.RTL) {
      highlightSteps.push('V', Blockly.BlockSvg.MIN_BLOCK_Y - 1);
    }
    cursorY = Blockly.BlockSvg.MIN_BLOCK_Y;
  }
  return cursorY;
};

/**
 * Render the bottom edge of the block.
 * @param {!Array.<string>} steps Path of block outline.
 * @param {!Array.<string>} highlightSteps Path of block highlights.
 * @param {!Object} connectionsXY Location of block.
 * @param {number} cursorY Height of block.
 * @private
 */
Blockly.BlockSvg.prototype.renderDrawBottom_ = function(steps, highlightSteps,
                                                     connectionsXY, cursorY) {
  if (this.block_.nextConnection) {
    steps.push('H', Blockly.BlockSvg.NOTCH_WIDTH + ' ' +
        Blockly.BlockSvg.NOTCH_PATH_RIGHT);
    // Create next block connection.
    var connectionX;
    if (Blockly.RTL) {
      connectionX = connectionsXY.x - Blockly.BlockSvg.NOTCH_WIDTH;
    } else {
      connectionX = connectionsXY.x + Blockly.BlockSvg.NOTCH_WIDTH;
    }
    var connectionY = connectionsXY.y + cursorY + 1;
    this.block_.nextConnection.moveTo(connectionX, connectionY);
    if (this.block_.nextConnection.targetConnection) {
      this.block_.nextConnection.tighten_();
    }
  }
  if (this.block_.outputConnection) {
    steps.push('H 0');
  } else {
    steps.push('H', Blockly.BlockSvg.CORNER_RADIUS);
    if (Blockly.BlockSvg.CORNER_RADIUS) {
      steps.push('a', Blockly.BlockSvg.CORNER_RADIUS + ',' +
                 Blockly.BlockSvg.CORNER_RADIUS + ' 0 0,1 -' +
                 Blockly.BlockSvg.CORNER_RADIUS + ',-' +
                 Blockly.BlockSvg.CORNER_RADIUS);
      if (!Blockly.RTL) {
        highlightSteps.push('M', Blockly.BlockSvg.DISTANCE_45_INSIDE + ',' +
            (cursorY - Blockly.BlockSvg.DISTANCE_45_INSIDE));
        highlightSteps.push('A', (Blockly.BlockSvg.CORNER_RADIUS - 1) + ',' +
            (Blockly.BlockSvg.CORNER_RADIUS - 1) + ' 0 0,1 ' +
            '1,' + (cursorY - Blockly.BlockSvg.CORNER_RADIUS));
      }
    }
  }
};

/**
 * Render the left edge of the block.
 * @param {!Array.<string>} steps Path of block outline.
 * @param {!Array.<string>} highlightSteps Path of block highlights.
 * @param {!Object} connectionsXY Location of block.
 * @param {number} cursorY Height of block.
 * @private
 */
Blockly.BlockSvg.prototype.renderDrawLeft_ = function(steps, highlightSteps,
                                                   connectionsXY, cursorY) {
  if (this.block_.outputConnection) {
    steps.push('V', Blockly.BlockSvg.TAB_HEIGHT);
    steps.push('c 0,-10 -' + Blockly.BlockSvg.TAB_WIDTH + ',8 -' +
        Blockly.BlockSvg.TAB_WIDTH + ',-7.5 s ' + Blockly.BlockSvg.TAB_WIDTH +
        ',2.5 ' + Blockly.BlockSvg.TAB_WIDTH + ',-7.5');
    if (Blockly.RTL) {
      highlightSteps.push('M', (Blockly.BlockSvg.TAB_WIDTH * -0.3) + ',8.9');
      highlightSteps.push('l', (Blockly.BlockSvg.TAB_WIDTH * -0.45) + ',-2.1');
    } else {
      highlightSteps.push('M', '1,' + cursorY);
      highlightSteps.push('V', Blockly.BlockSvg.TAB_HEIGHT - 1);
      highlightSteps.push('m', (Blockly.BlockSvg.TAB_WIDTH * -0.92) +
                          ',-1 q ' + (Blockly.BlockSvg.TAB_WIDTH * -0.19) +
                          ',-5.5 0,-11');
      highlightSteps.push('m', (Blockly.BlockSvg.TAB_WIDTH * 0.92) +
                          ',1 V 1 H 2');
    }
    // Create output connection.
    this.block_.outputConnection.moveTo(connectionsXY.x, connectionsXY.y);
    // This connection will be tightened when the parent renders.
  } else {
    if (!Blockly.RTL) {
      highlightSteps.push('V', Blockly.BlockSvg.CORNER_RADIUS);
    }
  }
  steps.push('z');
};
