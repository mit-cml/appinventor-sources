/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
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
 * @fileoverview Utility methods.
 * These methods are not specific to Blockly, and could be factored out if
 * a JavaScript framework such as Closure were used.
 * @author fraser@google.com (Neil Fraser)
 */

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
 * @param {Object} thisObject The value of 'this' in the function.
 * @param {!Function} func Function to call when event is triggered.
 * @return {!Array.<!Array>} Opaque data that can be passed to unbindEvent_.
 * @private
 */
Blockly.bindEvent_ = function(element, name, thisObject, func) {
  var bindData = [];
  var wrapFunc;
  if (element.addEventListener) {  // W3C
    wrapFunc = function(e) {
      func.apply(thisObject, arguments);
    };
    element.addEventListener(name, wrapFunc, false);
    bindData.push([element, name, wrapFunc]);
    // Add equivalent touch event.
    if (name in Blockly.bindEvent_.TOUCH_MAP) {
      wrapFunc = function(e) {
        // Punt on multitouch events.
        if (e.changedTouches.length == 1) {
          // Map the touch event's properties to the event.
          var touchPoint = e.changedTouches[0];
          e.clientX = touchPoint.clientX;
          e.clientY = touchPoint.clientY;
        }
        func.apply(thisObject, arguments);
        // Stop the browser from scrolling/zooming the page
        e.preventDefault();
      };
      element.addEventListener(Blockly.bindEvent_.TOUCH_MAP[name],
                               wrapFunc, false);
      bindData.push([element, Blockly.bindEvent_.TOUCH_MAP[name], wrapFunc]);
    }
  } else if (element.attachEvent) {  // IE
    wrapFunc = function(e) {
      func.apply(thisObject, arguments);
      e.stopPropagation();
    };
    element.attachEvent('on' + name, wrapFunc);
    bindData.push([element, name, wrapFunc]);
  } else {
    throw 'Element is not a DOM node.';
  }
  return bindData;
};

if ('ontouchstart' in document.documentElement) {
  Blockly.bindEvent_.TOUCH_MAP = {
    mousedown: 'touchstart',
    mousemove: 'touchmove',
    mouseup: 'touchend'
  };
} else {
  Blockly.bindEvent_.TOUCH_MAP = {};
}

/**
 * Unbind one or more events event from a function call.
 * @param {!Array.<!Array>} bindData Opaque data from bindEvent_.  This list is
 *     emptied during the course of calling this function.
 * @private
 */
Blockly.unbindEvent_ = function(bindData) {
  while (bindData.length) {
    var bindDatum = bindData.pop();
    var element = bindDatum[0];
    var name = bindDatum[1];
    var func = bindDatum[2];
    if (element.removeEventListener) {  // W3C
      element.removeEventListener(name, func, false);
    } else {  // IE
      element.detachEvent('on' + name, func);
    }
  }
};

/**
 * Fire a synthetic event.
 * @param {!Element} element The event's target element.
 * @param {string} eventName Name of event (e.g. 'click').
 */
Blockly.fireUiEvent = function(element, eventName) {
  var doc = Blockly.svgDoc;
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
  e.preventDefault();
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

/**
 * Is this event a right-click?
 * @param {!Event} e Mouse event.
 * @return {boolean} True if right-click.
 */
Blockly.isRightButton = function(e) {
  // Control-clicking in WebKit on Mac OS X fails to change button to 2.
  return e.button == 2 || e.ctrlKey;
};

/**
 * Convert the mouse coordinates into SVG coordinates.
 * @param {number} x X mouse coordinate.
 * @param {number} y Y mouse coordinate.
 * @return {!SVGPoint} Object with x and y properties in SVG coordinates.
 */
Blockly.mouseToSvg = function(x, y) {
  var svgPoint = Blockly.svg.createSVGPoint();
  svgPoint.x = x;
  svgPoint.y = y;
  var matrix = Blockly.svg.getScreenCTM().inverse();
  return svgPoint = svgPoint.matrixTransform(matrix);
};
