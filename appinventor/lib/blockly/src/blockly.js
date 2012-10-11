/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
 * http://code.google.com/p/blockly/
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
 * @fileoverview Core JavaScript library for Blockly.
 * @author fraser@google.com (Neil Fraser)
 */

// Top level object for Blockly.
var Blockly = {};

/**
 * Path to Blockly's directory.  Can be relative, absolute, or remote.
 * Used for loading additional resources.
 */
Blockly.pathToBlockly = './';

// Required name space for SVG elements.
Blockly.SVG_NS = 'http://www.w3.org/2000/svg';
// Required name space for HTML elements.
Blockly.HTML_NS = 'http://www.w3.org/1999/xhtml';

/**
 * The HSV_SATURATION and HSV_VALUE constants provide Blockly with a consistent
 * colour scheme, regardless of the hue.
 * Both constants must be in the range of 0 (inclusive) to 1 (exclusive).
 */
Blockly.HSV_SATURATION = 0.45;
Blockly.HSV_VALUE = 0.65;

/**
 * Convert a hue (HSV model) into an RGB hex triplet.
 * @param {number} hue Hue on a colour wheel (0-360).
 * @return {string} RGB code, e.g. '#84c'.
 */
Blockly.makeColour = function(hue) {
  hue %= 360;
  var topLimit = Blockly.HSV_VALUE;
  var bottomLimit = Blockly.HSV_VALUE * (1 - Blockly.HSV_SATURATION);
  var rangeUp = (topLimit - bottomLimit) * (hue % 60 / 60) + bottomLimit;
  var rangeDown = (topLimit - bottomLimit) * (1 - hue % 60 / 60) + bottomLimit;
  var r, g, b;
  if (0 <= hue && hue < 60) {
    r = topLimit;
    g = rangeUp;
    b = bottomLimit;
  } else if (60 <= hue && hue < 120) {
    r = rangeDown;
    g = topLimit;
    b = bottomLimit;
  } else if (120 <= hue && hue < 180) {
    r = bottomLimit;
    g = topLimit;
    b = rangeUp;
  } else if (180 <= hue && hue < 240) {
    r = bottomLimit;
    g = rangeDown;
    b = topLimit;
  } else if (240 <= hue && hue < 300) {
    r = rangeUp;
    g = bottomLimit;
    b = topLimit;
  } else if (300 <= hue && hue < 360) {
    r = topLimit;
    g = bottomLimit;
    b = rangeDown;
  } else {
    // Negative number?
    r = 0;
    g = 0;
    b = 0;
  }
  r = Math.floor(r * 16);
  g = Math.floor(g * 16);
  b = Math.floor(b * 16);
  var HEX = '0123456789abcdef';
  return '#' + HEX.charAt(r) + HEX.charAt(g) + HEX.charAt(b);
};

/**
 * ENUM for a right-facing value input.  E.g. 'test' or 'return'.
 */
Blockly.INPUT_VALUE = 1;
/**
 * ENUM for a left-facing value output.  E.g. 'call random'.
 */
Blockly.OUTPUT_VALUE = 2;
/**
 * ENUM for a down-facing block stack.  E.g. 'then-do' or 'else-do'.
 */
Blockly.NEXT_STATEMENT = 3;
/**
 * ENUM for an up-facing block stack.  E.g. 'close screen'.
 */
Blockly.PREVIOUS_STATEMENT = 4;
/**
 * ENUM for an dummy input.  Used to add title(s) with no input.
 */
Blockly.DUMMY_INPUT = 5;

/**
 * ENUM for left alignment.
 */
Blockly.ALIGN_LEFT = -1;
/**
 * ENUM for centre alignment.
 */
Blockly.ALIGN_CENTRE = 0;
/**
 * ENUM for right alignment.
 */
Blockly.ALIGN_RIGHT = 1;

/**
 * Lookup table for determining the opposite type of a connection.
 */
Blockly.OPPOSITE_TYPE = [];
Blockly.OPPOSITE_TYPE[Blockly.INPUT_VALUE] = Blockly.OUTPUT_VALUE;
Blockly.OPPOSITE_TYPE[Blockly.OUTPUT_VALUE] = Blockly.INPUT_VALUE;
Blockly.OPPOSITE_TYPE[Blockly.NEXT_STATEMENT] = Blockly.PREVIOUS_STATEMENT;
Blockly.OPPOSITE_TYPE[Blockly.PREVIOUS_STATEMENT] = Blockly.NEXT_STATEMENT;

/**
 * Database of pre-loaded sounds.
 * @private
 */
Blockly.SOUNDS_ = {};

/**
 * Currently selected block.
 * @type Blockly.Block
 */
Blockly.selected = null;

/**
 * In the future we might want to have display-only block views.
 * Until then, all blocks are considered editable.
 * Note that this property may only be set before init is called.
 * It can't be used to dynamically toggle editability on and off.
 */
Blockly.editable = true;

/**
 * Currently highlighted connection (during a drag).
 * @type {Blockly.Connection}
 * @private
 */
Blockly.highlightedConnection_ = null;

/**
 * Connection on dragged block that matches the highlighted connection.
 * @type {Blockly.Connection}
 * @private
 */
Blockly.localConnection_ = null;

/**
 * Number of pixels the mouse must move before a drag starts.
 */
Blockly.DRAG_RADIUS = 5;

/**
 * Maximum misalignment between connections for them to snap together.
 */
Blockly.SNAP_RADIUS = 12;

/**
 * Delay in ms between trigger and bumping unconnected block out of alignment.
 */
Blockly.BUMP_DELAY = 250;

/**
 * The document object.
 * @type {Document}
 */
Blockly.svgDoc = null;

/**
 * The main workspace (defined by inject.js).
 * @type {Blockly.Workspace}
 */
Blockly.mainWorkspace = null;

/**
 * Contents of the local clipboard.
 * @type {Element}
 * @private
 */
Blockly.clipboard_ = null;

/**
 * Returns the dimensions of the current SVG image.
 * @return {!Object} Contains width, height, top and left properties.
 */
Blockly.svgSize = function() {
  return {width: Blockly.svg.cachedWidth_,
          height: Blockly.svg.cachedHeight_,
          top: Blockly.svg.cachedTop_,
          left: Blockly.svg.cachedLeft_};
};

/**
 * Size the SVG image to completely fill its container.
 * Record both the height/width and the absolute postion of the SVG image.
 */
Blockly.svgResize = function() {
  var width = Blockly.svg.parentNode.offsetWidth;
  var height = Blockly.svg.parentNode.offsetHeight;
  if (Blockly.svg.cachedWidth_ != width) {
    Blockly.svg.setAttribute('width', width + 'px');
    Blockly.svg.cachedWidth_ = width;
  }
  if (Blockly.svg.cachedHeight_ != height) {
    Blockly.svg.setAttribute('height', height + 'px');
    Blockly.svg.cachedHeight_ = height;
  }
  var bBox = Blockly.svg.getBoundingClientRect();
  Blockly.svg.cachedLeft_ = bBox.left;
  Blockly.svg.cachedTop_ = bBox.top;
};

/**
 * Handle a mouse-down on SVG drawing surface.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.onMouseDown_ = function(e) {
  Blockly.Block.terminateDrag_();
  Blockly.hideChaff();
  if (Blockly.isTargetInput_(e) ||
      (Blockly.Mutator && Blockly.Mutator.isOpen)) {
    return;
  }
  if (Blockly.selected && e.target.nodeName == 'svg') {
    // Clicking on the document clears the selection.
    Blockly.selected.unselect();
  }
  if (Blockly.isRightButton(e)) {
    // Right-click.
    if (Blockly.ContextMenu) {
      Blockly.showContextMenu_(e.clientX, e.clientY);
    }
  } else if (e.target.nodeName == 'svg' || !Blockly.editable) {
    // If the workspace is editable, only allow dragging when gripping empty
    // space.  Otherwise, allow dragging when gripping anywhere.
    Blockly.mainWorkspace.dragMode = true;
    // Record the current mouse position.
    Blockly.mainWorkspace.startDragMouseX = e.clientX;
    Blockly.mainWorkspace.startDragMouseY = e.clientY;
    Blockly.mainWorkspace.startDragMetrics =
        Blockly.getMainWorkspaceMetrics();
    Blockly.mainWorkspace.startScrollX = Blockly.mainWorkspace.scrollX;
    Blockly.mainWorkspace.startScrollY = Blockly.mainWorkspace.scrollY;
  }
};

/**
 * Handle a mouse-up on SVG drawing surface.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.onMouseUp_ = function(e) {
  Blockly.setCursorHand_(false);
  Blockly.mainWorkspace.dragMode = false;
};

/**
 * Handle a mouse-move on SVG drawing surface.
 * @param {!Event} e Mouse move event.
 * @private
 */
Blockly.onMouseMove_ = function(e) {
  if (Blockly.mainWorkspace.dragMode) {
    Blockly.removeAllRanges();
    var dx = e.clientX - Blockly.mainWorkspace.startDragMouseX;
    var dy = e.clientY - Blockly.mainWorkspace.startDragMouseY;
    var metrics = Blockly.mainWorkspace.startDragMetrics;
    var x = Blockly.mainWorkspace.startScrollX + dx;
    var y = Blockly.mainWorkspace.startScrollY + dy;
    x = Math.min(x, -metrics.contentLeft);
    y = Math.min(y, -metrics.contentTop);
    x = Math.max(x, metrics.viewWidth - metrics.contentLeft -
                 metrics.contentWidth);
    y = Math.max(y, metrics.viewHeight - metrics.contentTop -
                 metrics.contentHeight);

    // Move the scrollbars and the page will scroll automatically.
    Blockly.mainWorkspace.scrollbar.set(-x - metrics.contentLeft,
                                        -y - metrics.contentTop);
  }
};

/**
 * Handle a key-down on SVG drawing surface.
 * @param {!Event} e Key down event.
 * @private
 */
Blockly.onKeyDown_ = function(e) {
  if (Blockly.isTargetInput_(e)) {
    // When focused on an HTML text input widget, don't trap any keys.
    return;
  }
  // TODO: Add keyboard support for cursoring around the context menu.
  if (e.keyCode == 27) {
    // Pressing esc closes the context menu.
    Blockly.hideChaff();
    if (Blockly.Mutator && Blockly.Mutator.isOpen) {
      Blockly.Mutator.closeDialog();
    }
  } else if (e.keyCode == 8 || e.keyCode == 46) {
    // Delete or backspace.
    if (Blockly.selected && Blockly.selected.editable &&
        (!Blockly.Mutator || !Blockly.Mutator.isOpen)) {
      Blockly.hideChaff();
      Blockly.selected.destroy(true, true);
    }
    // Stop the browser from going back to the previous page.
    e.preventDefault();
  } else if (e.altKey || e.ctrlKey || e.metaKey) {
    if (Blockly.selected && Blockly.selected.editable &&
        Blockly.selected.workspace == Blockly.mainWorkspace) {
      Blockly.hideChaff();
      if (e.keyCode == 67) {
        // 'c' for copy.
        Blockly.copy_(Blockly.selected);
      } else if (e.keyCode == 88) {
        // 'x' for cut.
        Blockly.copy_(Blockly.selected);
        Blockly.selected.destroy(true, true);
      }
    }
    if (e.keyCode == 86) {
      // 'v' for paste.
      if (Blockly.clipboard_) {
        Blockly.mainWorkspace.paste(Blockly.clipboard_);
      }
    }
  }
};

/**
 * Copy a block onto the local clipboard.
 * @param {!Blockly.Block} block Block to be copied.
 * @private
 */
Blockly.copy_ = function(block) {
  var xmlBlock = Blockly.Xml.blockToDom_(block);
  Blockly.Xml.deleteNext(xmlBlock);
  // Encode start position in XML.
  var xy = block.getRelativeToSurfaceXY();
  xmlBlock.setAttribute('x', Blockly.RTL ? -xy.x : xy.x);
  xmlBlock.setAttribute('y', xy.y);
  Blockly.clipboard_ = xmlBlock;
};

/**
 * Show the context menu for the workspace.
 * @param {number} x X-coordinate of mouse click.
 * @param {number} y Y-coordinate of mouse click.
 * @private
 */
Blockly.showContextMenu_ = function(x, y) {
  var options = [];

  // Option to get help.
  var helpOption = {enabled: false};
  helpOption.text = Blockly.MSG_HELP;
  helpOption.callback = function() {};
  options.push(helpOption);

  Blockly.ContextMenu.show(x, y, options);
};

/**
 * Cancel the native context menu, unless the focus is on an HTML input widget.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.onContextMenu_ = function(e) {
  if (!Blockly.isTargetInput_(e) && Blockly.ContextMenu) {
    // When focused on an HTML text input widget, don't cancel the context menu.
    e.preventDefault();
  }
};

/**
 * Close tooltips, context menus, dropdown selections, etc.
 * @param {boolean} opt_allowToolbox If true, don't close the toolbox.
 */
Blockly.hideChaff = function(opt_allowToolbox) {
  Blockly.Tooltip && Blockly.Tooltip.hide();
  Blockly.ContextMenu && Blockly.ContextMenu.hide();
  Blockly.FieldDropdown.hideMenu();
  if (Blockly.Toolbox && !opt_allowToolbox &&
      Blockly.Toolbox.flyout_.autoClose) {
    Blockly.Toolbox.clearSelection();
  }
};

/**
 * Destroy all selections on the webpage.
 * Chrome will select text outside the SVG when double-clicking.
 * Deselect this text, so that it doesn't mess up any subsequent drag.
 */
Blockly.removeAllRanges = function() {
  if (window.getSelection) {  // W3
    var sel = window.getSelection();
    if (sel && sel.removeAllRanges) {
      sel.removeAllRanges();
      window.setTimeout(function() {
          window.getSelection().removeAllRanges();
        }, 0);
    }
  }
};

/**
 * Is this event targetting a text input widget?
 * @param {!Event} e An event.
 * @return {boolean} True if text input.
 * @private
 */
Blockly.isTargetInput_ = function(e) {
  return e.target.type == 'textarea' || e.target.type == 'text';
};

/**
 * Load an audio file.  Cache it, ready for instantaneous playing.
 * @param {string} name Name of sound.
 * @private
 */
Blockly.loadAudio_ = function(name) {
  if (!Audio) {
    // No browser support for Audio.
    return;
  }
  var sound = new Audio(Blockly.pathToBlockly + 'media/' + name + '.wav');
  // To force the browser to load the sound, play it, but stop it immediately.
  // If this starts creating a chirp on startup, turn the sound's volume down,
  // or use another caching method such as XHR.
  if (sound && sound.play) {
    sound.play();
    sound.pause();
    Blockly.SOUNDS_[name] = sound;
  }
};

/**
 * Play an audio file.
 * @param {string} name Name of sound.
 */
Blockly.playAudio = function(name) {
  var sound = Blockly.SOUNDS_[name];
  if (sound) {
    sound.play();
  }
};

/**
 * Set the mouse cursor to be either a closed hand or the default.
 * @param {boolean} closed True for closed hand.
 * @private
 */
Blockly.setCursorHand_ = function(closed) {
  if (!Blockly.editable) {
    return;
  }
  /* Hotspot coordinates are baked into the CUR file, but they are still
     required due to a Chrome bug.
     http://code.google.com/p/chromium/issues/detail?id=1446 */
  var cursor = '';
  if (closed) {
    cursor = 'url(' + Blockly.pathToBlockly + 'media/handclosed.cur) 7 3, auto';
  }
  if (Blockly.selected) {
    Blockly.selected.getSvgRoot().style.cursor = cursor;
  }
  // Set cursor on the SVG surface as well as block so that rapid movements
  // don't result in cursor changing to an arrow momentarily.
  Blockly.svgDoc.getElementsByTagName('svg')[0].style.cursor = cursor;
};

/**
 * Return an object with all the metrics required to size scrollbars for the
 * main workspace.  The following properties are computed:
 * .viewHeight: Height of the visible rectangle,
 * .viewWidth: Width of the visible rectangle,
 * .contentHeight: Height of the contents,
 * .contentWidth: Width of the content,
 * .viewTop: Offset of top edge of visible rectangle from parent,
 * .viewLeft: Offset of left edge of visible rectangle from parent,
 * .contentTop: Offset of the top-most content from the y=0 coordinate,
 * .contentLeft: Offset of the left-most content from the x=0 coordinate.
 * .absoluteTop: Top-edge of view.
 * .absoluteLeft: Left-edge of view.
 * @return {Object} Contains size and position metrics of main workspace.
 */
Blockly.getMainWorkspaceMetrics = function() {
  var hwView = Blockly.svgSize();
  if (Blockly.Toolbox) {
    hwView.width -= Blockly.Toolbox.width;
  }
  var viewWidth = hwView.width - Blockly.Scrollbar.scrollbarThickness;
  var viewHeight = hwView.height - Blockly.Scrollbar.scrollbarThickness;
  try {
    var blockBox = Blockly.mainWorkspace.getCanvas().getBBox();
  } catch (e) {
    // Firefox has trouble with hidden elements (Bug 528969).
    return null;
  }
  if (blockBox.width == -Infinity && blockBox.height == -Infinity) {
    // Opera has trouble with bounding boxes around empty objects.
    blockBox = {width: 0, height: 0, x: 0, y: 0};
  }
  // Add a border around the content that is at least half a screenful wide.
  var leftEdge = Math.min(blockBox.x - viewWidth / 2,
                          blockBox.x + blockBox.width - viewWidth);
  var rightEdge = Math.max(blockBox.x + blockBox.width + viewWidth / 2,
                           blockBox.x + viewWidth);
  var topEdge = Math.min(blockBox.y - viewHeight / 2,
                         blockBox.y + blockBox.height - viewHeight);
  var bottomEdge = Math.max(blockBox.y + blockBox.height + viewHeight / 2,
                            blockBox.y + viewHeight);
  var absoluteLeft = 0;
  if (Blockly.Toolbox && !Blockly.RTL) {
    absoluteLeft = Blockly.Toolbox.width;
  }
  return {
    viewHeight: hwView.height,
    viewWidth: hwView.width,
    contentHeight: bottomEdge - topEdge,
    contentWidth: rightEdge - leftEdge,
    viewTop: -Blockly.mainWorkspace.scrollY,
    viewLeft: -Blockly.mainWorkspace.scrollX,
    contentTop: topEdge,
    contentLeft: leftEdge,
    absoluteTop: 0,
    absoluteLeft: absoluteLeft
  };
};

/**
 * Sets the X/Y translations of the main workspace to match the scrollbars.
 * @param {!Object} xyRatio Contains an x and/or y property which is a float
 *     between 0 and 1 specifying the degree of scrolling.
 */
Blockly.setMainWorkspaceMetrics = function(xyRatio) {
  var metrics = Blockly.getMainWorkspaceMetrics();
  if (typeof xyRatio.x == 'number') {
    Blockly.mainWorkspace.scrollX = -metrics.contentWidth * xyRatio.x -
        metrics.contentLeft;
  }
  if (typeof xyRatio.y == 'number') {
    Blockly.mainWorkspace.scrollY = -metrics.contentHeight * xyRatio.y -
        metrics.contentTop;
  }
  var translation = 'translate(' +
      (Blockly.mainWorkspace.scrollX + metrics.absoluteLeft) + ',' +
      (Blockly.mainWorkspace.scrollY + metrics.absoluteTop) + ')';
  Blockly.mainWorkspace.getCanvas().setAttribute('transform', translation);
  Blockly.mainWorkspace.getBubbleCanvas().setAttribute('transform',
                                                       translation);
};

/**
 * Rerender certain elements which might have had their sizes changed by the
 * CSS file and thus need realigning.
 * Called when the CSS file has finally loaded.
 */
Blockly.cssLoaded = function() {
  Blockly.Toolbox && Blockly.Toolbox.redraw();
};
