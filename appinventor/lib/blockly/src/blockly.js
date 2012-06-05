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

// Text strings (factored out to make multi-language easier).
Blockly.MSG_REMOVE_COMMENT = 'Remove Comment';
Blockly.MSG_ADD_COMMENT = 'Add Comment';
Blockly.MSG_EXTERNAL_INPUTS = 'External Inputs';
Blockly.MSG_INLINE_INPUTS = 'Inline Inputs';
Blockly.MSG_DELETE_BLOCK = 'Delete Block';
Blockly.MSG_DELETE_X_BLOCKS = 'Delete %1 Blocks';
Blockly.MSG_COLLAPSE_BLOCK = 'Collapse Block';
Blockly.MSG_EXPAND_BLOCK = 'Expand Block';
Blockly.MSG_HELP = 'Help';

Blockly.MSG_CHANGE_VALUE_TITLE = 'Change value:';
Blockly.MSG_NEW_VARIABLE = 'New variable...';
Blockly.MSG_NEW_VARIABLE_TITLE = 'New variable name:';
Blockly.MSG_RENAME_VARIABLE = 'Rename variable...';
Blockly.MSG_RENAME_VARIABLE_TITLE = 'Rename all "%1" variables to:';
Blockly.MSG_VARIABLE_CATEGORY = 'Variables';

Blockly.MSG_PROCEDURE_CATEGORY = 'Procedures';

Blockly.MSG_MUTATOR_TOOLTIP = 'Edit this block';
Blockly.MSG_MUTATOR_HEADER = 'Block Editor';
Blockly.MSG_MUTATOR_CHANGE = 'Change';
Blockly.MSG_MUTATOR_CANCEL = 'Cancel';

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
 * ENUM for an local variable.  E.g. 'for x in list'.
 */
Blockly.LOCAL_VARIABLE = 5;

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
  Blockly.hideChaff();
  Blockly.removeAllRanges();
  if (Blockly.isTargetInput_(e) ||
      (Blockly.Mutator && Blockly.Mutator.isOpen)) {
    return;
  }
  if (Blockly.selected && e.target.nodeName == 'svg') {
    // Clicking on the document clears the selection.
    Blockly.selected.unselect();
  }
  if (e.button == 2) {
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
      Blockly.playAudio('delete');
      Blockly.selected.destroy(true);
    }
    // Stop the browser from going back to the previous page.
    e.preventDefault();
  }
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
  if (Blockly.Toolbox && !opt_allowToolbox) {
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
  var sound = new Audio(Blockly.pathToBlockly + 'media/' + name + '.wav');
  // To force the browser to load the sound, play it, but stop it immediately.
  // If this starts creating a chip on startup, turn the sound's volume down,
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
  Blockly.commentCanvas.setAttribute('transform', translation);
};

/**
 * Rerender certain elements which might have had their sizes changed by the
 * CSS file and thus need realigning.
 * Called when the CSS file has finally loaded.
 */
Blockly.cssLoaded = function() {
  Blockly.Toolbox && Blockly.Toolbox.redraw();
};

// Utility methods.
// These methods are not specific to Blockly, and could be factored out if
// a JavaScript framework such as Closure were used.

/**
 * Removes all the child nodes on a DOM node.
 * Copied from Closure's goog.dom.removeChildren
 * @param {!Node} node Node to remove children from.
 * @private
 */
Blockly.removeChildren_ = function(node) {
  var child;
  while ((child = node.firstChild)) {
    node.removeChild(child);
  }
};

/**
 * Add a CSS class to a node.
 * Similar to Closure's goog.dom.classes.add
 * @param {!Node} node DOM node to add class to.
 * @param {string} className Name of class to add.
 * @private
 */
Blockly.addClass_ = function(node, className) {
  var classes = node.getAttribute('class') || '';
  if ((' ' + classes + ' ').indexOf(' ' + className + ' ') == -1) {
    if (classes) {
      classes += ' ';
    }
    node.setAttribute('class', classes + className);
  }
};

/**
 * Remove a CSS class from a node.
 * Similar to Closure's goog.dom.classes.remove
 * @param {!Node} node DOM node to remove class from.
 * @param {string} className Name of class to remove.
 * @private
 */
Blockly.removeClass_ = function(node, className) {
  var classes = node.getAttribute('class');
  if ((' ' + classes + ' ').indexOf(' ' + className + ' ') != -1) {
    var classList = classes.split(/\s+/);
    for (var x = 0; x < classList.length; x++) {
      if (!classList[x] || classList[x] == className) {
        classList.splice(x, 1);
        x--;
      }
    }
    if (classList.length) {
      node.setAttribute('class', classList.join(' '));
    } else {
      node.removeAttribute('class');
    }
  }
};

/**
 * Bind an event to a function call.
 * @param {!Element} element Element upon which to listen to.
 * @param {string} name Event name to listen to (e.g. 'mousedown').
 * @param {Object} object The value of 'this' in the function.
 * @param {!Function} func Function to call when event is triggered.
 * @return {!Function} Function wrapper that was bound.  Used for unbindEvent_.
 * @private
 */
Blockly.bindEvent_ = function(element, name, object, func) {
  var wrapFunc;
  if (element.addEventListener) {  // W3C
    wrapFunc = function(e) {
      func.apply(object, arguments);
    };
    element.addEventListener(name, wrapFunc, false);
  } else {  // IE
    wrapFunc = function(e) {
      func.apply(object, arguments);
      e.stopPropagation();
    };
    element.attachEvent('on' + name, wrapFunc);
  }
  return wrapFunc;
};

/**
 * Unbind an event from a function call.
 * @param {!Element} element Element from which to unlisten.
 * @param {string} name Event name to listen to (e.g. 'mousedown').
 * @param {!Function} func Function to stop calling when event is triggered.
 * @private
 */
Blockly.unbindEvent_ = function(element, name, func) {
  if (element.removeEventListener) {  // W3C
    element.removeEventListener(name, func, false);
  } else {  // IE
    element.detachEvent('on' + name, func);
  }
};

/**
 * Fire a synthetic event.
 * @param {!Element} doc Window's document for the event.
 * @param {!Element} element The event's target element.
 * @param {string} eventName Name of event (e.g. 'click').
 */
Blockly.fireUiEvent = function(doc, element, eventName) {
  if (doc.createEvent) {
    // W3
    var evt = doc.createEvent('UIEvents');
    evt.initEvent(eventName, true, true);  // event type, bubbling, cancelable
    element.dispatchEvent(evt);
  } else if (doc.createEventObject) {
    // MSIE
    var evt = doc.createEventObject();
    element.fireEvent('on' + eventName, evt);
  } else {
    throw 'FireEvent: No event creation mechanism.';
  }
};

/**
 * Don't do anything for this event, just halt propagation.
 * @param {!Event} e An event.
 */
Blockly.noEvent = function(e) {
  // This event has been handled.  No need to bubble up to the document.
  e.stopPropagation();
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
  // Note that Firefox returns 'translate(12)' instead of 'translate(12, 0)'.
  var r = transform &&
          transform.match(/translate\(\s*([-\d.]+)(,\s*([-\d.]+)\s*\))?/);
  if (r) {
    xy.x += parseInt(r[1], 10);
    if (r[3]) {
      xy.y += parseInt(r[3], 10);
    }
  }
  return xy;
};

/**
 * Return the absolute coordinates of the top-left corner of this element.
 * @param {!Element} element Element to find the coordinates of.
 * @return {!Object} Object with .x and .y properties.
 * @private
 */
Blockly.getAbsoluteXY_ = function(element) {
  var x = 0;
  var y = 0;
  do {
    // Loop through this block and every parent.
    var xy = Blockly.getRelativeXY_(element);
    x += xy.x;
    y += xy.y;
    element = element.parentNode;
  } while (element && element != Blockly.svgDoc);
  return {x: x, y: y};
};

/**
 * Helper method for creating SVG elements.
 * @param {string} name Element's tag name.
 * @param {!Object} attrs Dictionary of attribute names and values.
 * @param {Element} parent Optional parent on which to append the element.
 * @return {!Element} Newly created SVG element.
 */
Blockly.createSvgElement = function(name, attrs, parent) {
  var e = Blockly.svgDoc.createElementNS(Blockly.SVG_NS, name);
  for (var key in attrs) {
    e.setAttribute(key, attrs[key]);
  }
  if (parent) {
    parent.appendChild(e);
  }
  return e;
};

/**
 * Comparison function that is case-insensitive.
 * Designed to be used by Array.sort()
 * @param {string} a First argument.
 * @param {string} b Second argument.
 * @return {number} 1 if a is bigger, -1 if b is bigger, 0 if equal.
 */
Blockly.caseInsensitiveComparator = function(a, b) {
  a = a.toLowerCase();
  b = b.toLowerCase();
  if (a > b) {
    return 1;
  }
  if (a < b) {
    return -1;
  }
  return 0;
};

/**
 * Return a random id that's 8 letters long.
 * 26*(26+10+4)^7 = 4,259,840,000,000
 * @return {string} Random id.
 */
Blockly.uniqueId = function() {
  // First character must be a letter.
  // IE is case insensitive (in violation of the W3 spec).
  var soup = 'abcdefghijklmnopqrstuvwxyz';
  var id = soup.charAt(Math.random() * soup.length);
  // Subsequent characters may include these.
  soup += '0123456789-_:.';
  for (var x = 1; x < 8; x++) {
    id += soup.charAt(Math.random() * soup.length);
  }
  // Don't allow IDs with '--' in them since it might close a comment.
  if (id.indexOf('--') != -1) {
    id = Blockly.uniqueId();
  }
  return id;
};
