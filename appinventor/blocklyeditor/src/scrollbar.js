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
 * @fileoverview Library for creating scrollbars.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a pair of scrollbars.  Horizontal and vertical.
 * @param {!Element} element The SVG element to bind the scrollbars to.
 * @param {!Function} getMetrics A function that returns scrolling metrics.
 * @param {!Function} setMetrics A function that sets scrolling metrics.
 * @constructor
 */
Blockly.ScrollbarPair = function(element, getMetrics, setMetrics) {
  this.element_ = element;
  this.getMetrics_ = getMetrics;
  this.setMetrics_ = setMetrics;
  this.oldHostMetrics_ = {};
  this.hScroll = new Blockly.Scrollbar(element, getMetrics, setMetrics,
                                       true, true);
  this.vScroll = new Blockly.Scrollbar(element, getMetrics, setMetrics,
                                       false, true);
  this.corner_ = this.addCorner_(element);
  this.resize();

  // If the document resizes, reposition the scrollbars.
  var pair = this;
  Blockly.bindEvent_(window, 'resize', pair, function() {pair.resize();});
};

/**
 * Creates a corner element and adds it to the DOM.
 * 'resize' must be called to position it properly.
 * The corner element is a small square at the bottom-right between the two
 * scrollbars.  It prevents content from being visible in that location.
 * @param {!Element} element The SVG element to bind the scrollbars to.
 * @return {!Element} The corner element.
 * @private
 */
Blockly.ScrollbarPair.prototype.addCorner_ = function(element) {
  var corner = Blockly.createSvgElement('rect',
      {height: Blockly.Scrollbar.scrollbarThickness,
      width: Blockly.Scrollbar.scrollbarThickness,
      style: 'fill: #fff'}, null);
  Blockly.Scrollbar.insertAfter_(corner, element);
  return corner;
};

/**
 * Recalculate both of the scrollbars' locations and lengths.
 * Also reposition the corner rectange.
 */
Blockly.ScrollbarPair.prototype.resize = function() {
  // Look up the host metrics once, and use for both scrollbars.
  var hostMetrics = this.getMetrics_();
  if (!hostMetrics) {
    // Host element is likely not visible.
    return;
  }

  // Only change the scrollbars if there has been a change in metrics.
  var resizeH = false;
  var resizeV = false;
  if (this.oldHostMetrics_.viewWidth != hostMetrics.viewWidth ||
      this.oldHostMetrics_.viewHeight != hostMetrics.viewHeight ||
      this.oldHostMetrics_.absoluteTop != hostMetrics.absoluteTop ||
      this.oldHostMetrics_.absoluteLeft != hostMetrics.absoluteLeft) {
    // The window has been resized or repositioned.
    resizeH = true;
    resizeV = true;
  } else {
    // Has the content been resized or moved?
    if (this.oldHostMetrics_.contentWidth != hostMetrics.contentWidth ||
        this.oldHostMetrics_.viewLeft != hostMetrics.viewLeft ||
        this.oldHostMetrics_.contentLeft != hostMetrics.contentLeft) {
      resizeH = true;
    }
    if (this.oldHostMetrics_.contentHeight != hostMetrics.contentHeight ||
        this.oldHostMetrics_.viewTop != hostMetrics.viewTop ||
        this.oldHostMetrics_.contentTop != hostMetrics.contentTop) {
      resizeV = true;
    }
  }
  if (resizeH) {
    this.hScroll.resize(hostMetrics);
  }
  if (resizeV) {
    this.vScroll.resize(hostMetrics);
  }

  // Reposition the corner square.
  if (this.oldHostMetrics_.viewWidth != hostMetrics.viewWidth ||
      this.oldHostMetrics_.absoluteLeft != hostMetrics.absoluteLeft) {
    this.corner_.setAttribute('x', this.vScroll.xCoordinate);
  }
  if (this.oldHostMetrics_.viewHeight != hostMetrics.viewHeight ||
      this.oldHostMetrics_.absoluteTop != hostMetrics.absoluteTop) {
    this.corner_.setAttribute('y', this.hScroll.yCoordinate);
  }

  // Cache the current metrics to potentially short-cut the next resize event.
  this.oldHostMetrics_ = hostMetrics;
};

/**
 * Set the sliders of both scrollbars to be at a certain position.
 * @param {number} x Horizontal scroll value.
 * @param {number} y Vertical scroll value.
 */
Blockly.ScrollbarPair.prototype.set = function(x, y) {
  /* HACK:
   Two scrollbars are about to have their sliders moved.  Moving a scollbar
   will normally result in its onScroll function being called.  That function
   will update the contents.  At issue is what happens when two scrollbars are
   moved.  Calling onScroll twice may result in two rerenderings of the content
   and increase jerkiness during dragging.
   In the case of native scrollbars (currently used only by Firefox), onScroll
   is called as an event, which means two separate renderings of the content are
   performed.  However in the case of SVG scrollbars (currently used by all
   other browsers), onScroll is called as a function and the browser only
   rerenders the contents once at the end of the thread.
  */
  if (Blockly.Scrollbar === Blockly.ScrollbarNative) {
    // Native scrollbar mode.
    // Set both scrollbars and supress their two separate onScroll events.
    this.hScroll.set(x, false);
    this.vScroll.set(y, false);
    // Redraw the surface once with the new settings for both scrollbars.
    var xyRatio = {};
    xyRatio.x = (this.hScroll.outerDiv_.scrollLeft /
                 this.hScroll.innerImg_.offsetWidth) || 0;
    xyRatio.y = (this.vScroll.outerDiv_.scrollTop /
                 this.vScroll.innerImg_.offsetHeight) || 0;
    this.setMetrics_(xyRatio);
  } else {
    // SVG scrollbars.
    // Set both scrollbars and allow each to call a separate onScroll execution.
    this.hScroll.set(x, true);
    this.vScroll.set(y, true);
  }
};

// --------------------------------------------------------------------

/**
 * Class for a native widget scrollbar nested in a foreignObject element.
 * This technique offers a scrollbar that looks and behaves like the system's
 * scrollbars.  However it isn't well supported at the moment.
 * @param {!Element} element The SVG element to bind the scrollbars to.
 * @param {!Function} getMetrics A function that returns scrolling metrics.
 * @param {!Function} setMetrics A function that sets scrolling metrics.
 * @param {boolean} horizontal True if horizontal, false if vertical.
 *     Null is used to create a test scrollbar to measure thickness.
 * @param {boolean} opt_pair True if the scrollbar is part of a horiz/vert pair.
 * @constructor
 */
Blockly.ScrollbarNative = function(element, getMetrics, setMetrics,
                                   horizontal, opt_pair) {
  this.element_ = element;
  this.getMetrics_ = getMetrics;
  this.setMetrics_ = setMetrics;
  this.pair_ = opt_pair || false;
  this.horizontal_ = horizontal;

  this.createDom_(element);
  if (horizontal === null) {
    // Just return a bare-bones scrollbar DOM for thickness testing.
    return;
  }
  if (!Blockly.ScrollbarNative.scrollbarThickness) {
    // The first time a scrollbar is created, we need to measure the thickness.
    Blockly.ScrollbarNative.measureScrollbarThickness_(element);
  }

  if (horizontal) {
    this.foreignObject_.setAttribute('height',
        Blockly.ScrollbarNative.scrollbarThickness);
    this.outerDiv_.style.height =
        Blockly.ScrollbarNative.scrollbarThickness + 'px';
    this.outerDiv_.style.overflowX = 'scroll';
    this.outerDiv_.style.overflowY = 'hidden';
    this.innerImg_.style.height = '1px';
  } else {
    this.foreignObject_.setAttribute('width',
        Blockly.ScrollbarNative.scrollbarThickness);
    this.outerDiv_.style.width =
        Blockly.ScrollbarNative.scrollbarThickness + 'px';
    this.outerDiv_.style.overflowX = 'hidden';
    this.outerDiv_.style.overflowY = 'scroll';
    this.innerImg_.style.width = '1px';
  }
  var scrollbar = this;
  this.onScrollWrapper_ = Blockly.bindEvent_(this.outerDiv_, 'scroll',
      scrollbar, function() {scrollbar.onScroll_();});
  Blockly.bindEvent_(this.foreignObject_, 'mousedown', null, Blockly.noEvent);
  if (!this.pair_) {
    // If this scrollbar is part of a pair, then the ScrollbarPair will handle
    // resizing and event registration.
    this.resize();
    Blockly.bindEvent_(window, 'resize', scrollbar,
                       function() {scrollbar.resize();});
  }
};

/**
 * Recalculate the scrollbar's location and its length.
 * @param {Object} opt_metrics A data structure of from the describing all the
 * required dimensions.  If not provided, it will be fetched from the host
 * object.
 */
Blockly.ScrollbarNative.prototype.resize = function(opt_metrics) {
  // Determine the location, height and width of the host element.
  var hostMetrics = opt_metrics;
  if (!hostMetrics) {
    hostMetrics = this.getMetrics_();
    if (!hostMetrics) {
      // Host element is likely not visible.
      return;
    }
  }
  /* hostMetrics is an object with the following properties.
   * .viewHeight: Height of the visible rectangle,
   * .viewWidth: Width of the visible rectangle,
   * .contentHeight: Height of the contents,
   * .contentWidth: Width of the content,
   * .viewTop: Offset of top edge of visible rectangle from parent,
   * .viewLeft: Offset of left edge of visible rectangle from parent,
   * .contentTop: Offset of the top-most content from the y=0 coordinate,
   * .contentLeft: Offset of the left-most content from the x=0 coordinate,
   * .absoluteTop: Top-edge of view.
   * .absoluteLeft: Left-edge of view.
   */
  if (this.horizontal_) {
    var outerLength = hostMetrics.viewWidth;
    if (this.pair_) {
      // Shorten the scrollbar to make room for the corner square.
      outerLength -= Blockly.ScrollbarNative.scrollbarThickness;
    } else {
      // Only show the scrollbar if needed.
      // Ideally this would also apply to scrollbar pairs, but that's a bigger
      // headache (due to interactions with the corner square).
      this.setVisible(outerLength < hostMetrics.contentHeight);
    }
    this.ratio_ = outerLength / hostMetrics.viewWidth;
    var innerLength = this.ratio_ * hostMetrics.contentWidth;
    var innerOffset = (hostMetrics.viewLeft - hostMetrics.contentLeft) *
        this.ratio_;
    this.outerDiv_.style.width = outerLength + 'px';
    this.innerImg_.style.width = innerLength + 'px';
    this.xCoordinate = hostMetrics.absoluteLeft;
    if (this.pair_ && Blockly.RTL) {
      this.xCoordinate += Blockly.ScrollbarNative.scrollbarThickness;
    }
    this.yCoordinate = hostMetrics.absoluteTop + hostMetrics.viewHeight -
        Blockly.ScrollbarNative.scrollbarThickness;
    this.foreignObject_.setAttribute('x', this.xCoordinate);
    this.foreignObject_.setAttribute('y', this.yCoordinate);
    this.foreignObject_.setAttribute('width', Math.max(0, outerLength));
    this.outerDiv_.scrollLeft = Math.round(innerOffset);
  } else {
    var outerLength = hostMetrics.viewHeight;
    if (this.pair_) {
      // Shorten the scrollbar to make room for the corner square.
      outerLength -= Blockly.ScrollbarNative.scrollbarThickness;
    } else {
      // Only show the scrollbar if needed.
      this.setVisible(outerLength < hostMetrics.contentHeight);
    }
    this.ratio_ = outerLength / hostMetrics.viewHeight;
    var innerLength = this.ratio_ * hostMetrics.contentHeight;
    var innerOffset = (hostMetrics.viewTop - hostMetrics.contentTop) *
        this.ratio_;
    this.outerDiv_.style.height = outerLength + 'px';
    this.innerImg_.style.height = innerLength + 'px';
    this.xCoordinate = hostMetrics.absoluteLeft;
    if (!Blockly.RTL) {
      this.xCoordinate += hostMetrics.viewWidth -
          Blockly.ScrollbarNative.scrollbarThickness;
    }
    this.yCoordinate = hostMetrics.absoluteTop;
    this.foreignObject_.setAttribute('x', this.xCoordinate);
    this.foreignObject_.setAttribute('y', this.yCoordinate);
    this.foreignObject_.setAttribute('height', Math.max(0, outerLength));
    this.outerDiv_.scrollTop = Math.round(innerOffset);
  }
};

/**
 * Create all the DOM elements required for a scrollbar.
 * The resulting widget is not sized.
 * @param {!Element} element The SVG element to bind the scrollbars to.
 * @private
 */
Blockly.ScrollbarNative.prototype.createDom_ = function(element) {
  /* Create the following DOM:
  <foreignObject xmlns="http://www.w3.org/2000/svg">
    <body xmlns="http://www.w3.org/1999/xhtml" class="blocklyMinimalBody">
      <div>
        <img src="1x1.gif">
      </div>
    </body>
  </foreignObject>
  */
  this.foreignObject_ = Blockly.createSvgElement('foreignObject', {}, null);
  var body = Blockly.svgDoc.createElementNS(Blockly.HTML_NS, 'body');
  body.setAttribute('xmlns', Blockly.HTML_NS);
  body.setAttribute('class', 'blocklyMinimalBody');
  var outer = Blockly.svgDoc.createElementNS(Blockly.HTML_NS, 'div');
  this.outerDiv_ = outer;
  var inner = Blockly.svgDoc.createElementNS(Blockly.HTML_NS, 'img');
  inner.setAttribute('src', '1x1.gif');
  this.innerImg_ = inner;

  outer.appendChild(inner);
  body.appendChild(outer);
  this.foreignObject_.appendChild(body);
  Blockly.Scrollbar.insertAfter_(this.foreignObject_, element);
};

/**
 * Is the scrollbar visible.  Non-paired scrollbars disappear when they aren't
 * needed.
 * @return {boolean} True if visible.
 */
Blockly.ScrollbarNative.prototype.isVisible = function() {
  return this.foreignObject_.style.display != 'none';
};

/**
 * Set whether the scrollbar is visible.
 * Only applies to non-paired scrollbars.
 * @param {boolean} visible True if visible.
 */
Blockly.ScrollbarNative.prototype.setVisible = function(visible) {
  if (visible == this.isVisible()) {
    return;
  }
  // Ideally this would also apply to scrollbar pairs, but that's a bigger
  // headache (due to interactions with the corner square).
  if (this.pair_) {
    throw 'Unable to toggle visibility of paired scrollbars.';
  }
  if (visible) {
    this.foreignObject_.style.display = 'block';
    /* HACK:
    For some reason Firefox requires the metrics to be recalculated after
    displaying the scrollbar.  Even though the metrics are identical and
    calculating these metrics has no side effects.  Failure to do so
    results in a scrollbar that's crushed to 0 in an offscale range.
    */
    this.getMetrics_();
  } else {
    // Hide the scrollbar.
    this.setMetrics_({x: 0, y: 0});
    this.foreignObject_.style.display = 'none';
  }
};

/**
 * Called when scrollbar is dragged.
 * @private
 */
Blockly.ScrollbarNative.prototype.onScroll_ = function() {
  var xyRatio = {};
  if (this.horizontal_) {
    xyRatio.x = (this.outerDiv_.scrollLeft / this.innerImg_.offsetWidth) || 0;
  } else {
    xyRatio.y = (this.outerDiv_.scrollTop / this.innerImg_.offsetHeight) || 0;
  }
  this.setMetrics_(xyRatio);
};

/**
 * Set the scrollbar slider's position.
 * @param {number} value The distance from the top/left end of the bar.
 * @param {boolean} fireEvents True if onScroll events should be fired.
 */
Blockly.ScrollbarNative.prototype.set = function(value, fireEvents) {
  // If the scrollbar is part of a pair, it is slightly shorter than the view
  // and the value needs to be scaled accordingly.
  if (!fireEvents) {
    // Temporarily supress the onscroll event handler.
    Blockly.unbindEvent_(this.outerDiv_, 'scroll', this.onScrollWrapper_);
  }
  // Move the scrollbar slider.
  if (this.horizontal_) {
    this.outerDiv_.scrollLeft = value * this.ratio_;
  } else {
    this.outerDiv_.scrollTop = value * this.ratio_;
  }
  if (!fireEvents) {
    // Reenable the onscroll event handler.
    var scrollbar = this;
    Blockly.bindEvent_(this.outerDiv_, 'scroll', scrollbar,
                       this.onScrollWrapper_);
  }
};

/**
 * Width of a vertical scrollbar or height of a horizontal scrollbar.
 * We assume that both the above are the same.
 */
Blockly.ScrollbarNative.scrollbarThickness = 0;

/**
 * Mutilate this scrollbar to measure the thickness of an HTML scrollbar.
 * @param {!Element} element The SVG element to bind the scrollbars to.
 * @private
 */
Blockly.ScrollbarNative.measureScrollbarThickness_ = function(element) {
  var testBar = new Blockly.ScrollbarNative(element, null, null, null, false);

  testBar.outerDiv_.style.width = '100px';
  testBar.outerDiv_.style.height = '100px';
  testBar.innerImg_.style.width = '100%';
  testBar.innerImg_.style.height = '200px';
  // Trivia: failure to set a height and width results in Firefox returning
  // a scrollbar width of -85 instead of 15.
  testBar.foreignObject_.setAttribute('width', 1);
  testBar.foreignObject_.setAttribute('height', 1);

  // Measure the width of the inner-most div.
  testBar.outerDiv_.style.overflowY = 'scroll';
  var w1 = testBar.innerImg_.offsetWidth;
  // Turn off scrollbars and remeasure.
  testBar.outerDiv_.style.overflowY = 'hidden';
  var w2 = testBar.innerImg_.offsetWidth;

  // Destroy the test scrollbar.
  element.parentNode.removeChild(testBar.foreignObject_);

  var thickness = w2 - w1;
  if (thickness <= 0) {
    // Chrome for OS X 10.7 (Lion) floats scrollbars over the content, meaning
    // that there is no way to measure the thickness.  Pick a default.
    thickness = 15;
  }
  Blockly.ScrollbarNative.scrollbarThickness = thickness;
};

// --------------------------------------------------------------------

/**
 * Class for a pure SVG scrollbar.
 * This technique offers a scrollbar that is guaranteed to work, but may not
 * look or behave like the system's scrollbars.
 * @param {!Element} element The SVG element to bind the scrollbars to.
 * @param {!Function} getMetrics A function that returns scrolling metrics.
 * @param {!Function} setMetrics A function that sets scrolling metrics.
 * @param {boolean} horizontal True if horizontal, false if vertical.
 * @param {boolean} opt_pair True if the scrollbar is part of a horiz/vert pair.
 * @constructor
 */
Blockly.ScrollbarSvg = function(element, getMetrics, setMetrics,
                                horizontal, opt_pair) {
  this.element_ = element;
  this.getMetrics_ = getMetrics;
  this.setMetrics_ = setMetrics;
  this.pair_ = opt_pair || false;
  this.horizontal_ = horizontal;

  this.createDom_(element);

  if (horizontal) {
    this.svgBackground_.setAttribute('height',
        Blockly.ScrollbarSvg.scrollbarThickness);
    this.svgKnob_.setAttribute('height',
        Blockly.ScrollbarSvg.scrollbarThickness - 6);
    this.svgKnob_.setAttribute('y', 3);
  } else {
    this.svgBackground_.setAttribute('width',
        Blockly.ScrollbarSvg.scrollbarThickness);
    this.svgKnob_.setAttribute('width',
        Blockly.ScrollbarSvg.scrollbarThickness - 6);
    this.svgKnob_.setAttribute('x', 3);
  }
  var scrollbar = this;
  if (!this.pair_) {
    // If this scrollbar is part of a pair, then the ScrollbarPair will handle
    // resizing and event registration.
    this.resize();
    Blockly.bindEvent_(window, 'resize', scrollbar,
                       function() {scrollbar.resize();});
  }
  Blockly.bindEvent_(this.svgBackground_, 'mousedown', scrollbar,
                     scrollbar.onMouseDownBar_);
  Blockly.bindEvent_(this.svgKnob_, 'mousedown', scrollbar,
                     scrollbar.onMouseDownKnob_);
};

/**
 * Recalculate the scrollbar's location and its length.
 * @param {Object} opt_metrics A data structure of from the describing all the
 * required dimensions.  If not provided, it will be fetched from the host
 * object.
 */
Blockly.ScrollbarSvg.prototype.resize = function(opt_metrics) {
  // Determine the location, height and width of the host element.
  var hostMetrics = opt_metrics;
  if (!hostMetrics) {
    hostMetrics = this.getMetrics_();
    if (!hostMetrics) {
      // Host element is likely not visible.
      return;
    }
  }
  /* hostMetrics is an object with the following properties.
   * .viewHeight: Height of the visible rectangle,
   * .viewWidth: Width of the visible rectangle,
   * .contentHeight: Height of the contents,
   * .contentWidth: Width of the content,
   * .viewTop: Offset of top edge of visible rectangle from parent,
   * .viewLeft: Offset of left edge of visible rectangle from parent,
   * .contentTop: Offset of the top-most content from the y=0 coordinate,
   * .contentLeft: Offset of the left-most content from the x=0 coordinate,
   * .absoluteTop: Top-edge of view.
   * .absoluteLeft: Left-edge of view.
   */
  if (this.horizontal_) {
    var outerLength = hostMetrics.viewWidth;
    if (this.pair_) {
      // Shorten the scrollbar to make room for the corner square.
      outerLength -= Blockly.ScrollbarSvg.scrollbarThickness;
    } else {
      // Only show the scrollbar if needed.
      // Ideally this would also apply to scrollbar pairs, but that's a bigger
      // headache (due to interactions with the corner square).
      this.setVisible(outerLength < hostMetrics.contentHeight);
    }
    this.ratio_ = (outerLength / hostMetrics.contentWidth) || 0;
    var innerLength = hostMetrics.viewWidth * this.ratio_;
    var innerOffset = (hostMetrics.viewLeft - hostMetrics.contentLeft) *
        this.ratio_;
    this.svgKnob_.setAttribute('width', Math.max(0, innerLength));
    this.xCoordinate = hostMetrics.absoluteLeft;
    if (this.pair_ && Blockly.RTL) {
      this.xCoordinate += hostMetrics.absoluteLeft +
          Blockly.ScrollbarSvg.scrollbarThickness;
    }
    this.yCoordinate = hostMetrics.absoluteTop + hostMetrics.viewHeight -
        Blockly.ScrollbarSvg.scrollbarThickness;
    this.svgGroup_.setAttribute('transform',
        'translate(' + this.xCoordinate + ', ' + this.yCoordinate + ')');
    this.svgBackground_.setAttribute('width', Math.max(0, outerLength));
    this.svgKnob_.setAttribute('x', this.constrainKnob_(innerOffset));
  } else {
    var outerLength = hostMetrics.viewHeight;
    if (this.pair_) {
      // Shorten the scrollbar to make room for the corner square.
      outerLength -= Blockly.ScrollbarSvg.scrollbarThickness;
    } else {
      // Only show the scrollbar if needed.
      this.setVisible(outerLength < hostMetrics.contentHeight);
    }
    this.ratio_ = (outerLength / hostMetrics.contentHeight) || 0;
    var innerLength = hostMetrics.viewHeight * this.ratio_;
    var innerOffset = (hostMetrics.viewTop - hostMetrics.contentTop) *
        this.ratio_;
    this.svgKnob_.setAttribute('height', Math.max(0, innerLength));
    this.xCoordinate = hostMetrics.absoluteLeft;
    if (!Blockly.RTL) {
      this.xCoordinate += hostMetrics.viewWidth -
          Blockly.ScrollbarSvg.scrollbarThickness;
    }
    this.yCoordinate = hostMetrics.absoluteTop;
    this.svgGroup_.setAttribute('transform',
        'translate(' + this.xCoordinate + ', ' + this.yCoordinate + ')');
    this.svgBackground_.setAttribute('height', Math.max(0, outerLength));
    this.svgKnob_.setAttribute('y', this.constrainKnob_(innerOffset));
  }
  // Resizing may have caused some scrolling.
  this.onScroll_();
};

/**
 * Create all the DOM elements required for a scrollbar.
 * The resulting widget is not sized.
 * @param {!Element} element The SVG element to bind the scrollbars to.
 * @private
 */
Blockly.ScrollbarSvg.prototype.createDom_ = function(element) {
  /* Create the following DOM:
  <g>
    <rect class="blocklyScrollbarBackground" />
    <rect class="blocklyScrollbarKnob" rx="7" ry="7" />
  </g>
  */
  this.svgGroup_ = Blockly.createSvgElement('g', {}, null);
  this.svgBackground_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyScrollbarBackground'}, this.svgGroup_);
  var radius = Math.floor((Blockly.ScrollbarSvg.scrollbarThickness - 6) / 2);
  this.svgKnob_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyScrollbarKnob', rx: radius, ry: radius},
      this.svgGroup_);
  Blockly.Scrollbar.insertAfter_(this.svgGroup_, element);
};

/**
 * Is the scrollbar visible.  Non-paired scrollbars disappear when they aren't
 * needed.
 * @return {boolean} True if visible.
 */
Blockly.ScrollbarSvg.prototype.isVisible = function() {
  return this.svgGroup_.getAttribute('display') != 'none';
};

/**
 * Set whether the scrollbar is visible.
 * Only applies to non-paired scrollbars.
 * @param {boolean} visible True if visible.
 */
Blockly.ScrollbarSvg.prototype.setVisible = function(visible) {
  if (visible == this.isVisible()) {
    return;
  }
  // Ideally this would also apply to scrollbar pairs, but that's a bigger
  // headache (due to interactions with the corner square).
  if (this.pair_) {
    throw 'Unable to toggle visibility of paired scrollbars.';
  }
  if (visible) {
    this.svgGroup_.setAttribute('display', 'block');
  } else {
    // Hide the scrollbar.
    this.setMetrics_({x: 0, y: 0});
    this.svgGroup_.setAttribute('display', 'none');
  }
};

/**
 * Scroll by one pageful.
 * Called when scrollbar background is clicked.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.ScrollbarSvg.prototype.onMouseDownBar_ = function(e) {
  Blockly.hideChaff(true);
  if (e.button == 2) {
    // Right-click.
    // Scrollbars have no context menu.
    e.stopPropagation();
    return;
  }
  // Update Blockly's knowledge of its own location.
  Blockly.svgResize();
  var svgSize = Blockly.svgSize();
  var mouseLocation = this.horizontal_ ?
      e.x - svgSize.left : e.y - svgSize.top;

  var knobXY = Blockly.getAbsoluteXY_(this.svgKnob_);
  var knobStart = this.horizontal_ ? knobXY.x : knobXY.y;
  var knobLength = parseFloat(
      this.svgKnob_.getAttribute(this.horizontal_ ? 'width' : 'height'));
  var knobValue = parseFloat(
      this.svgKnob_.getAttribute(this.horizontal_ ? 'x' : 'y'));

  var pageLength = knobLength * 0.95;
  if (mouseLocation <= knobStart) {
    // Decrease the scrollbar's value by a page.
    knobValue -= pageLength;
  } else if (mouseLocation >= knobStart + knobLength) {
    // Increase the scrollbar's value by a page.
    knobValue += pageLength;
  }
  this.svgKnob_.setAttribute(this.horizontal_ ? 'x' : 'y',
                             this.constrainKnob_(knobValue));
  this.onScroll_();
  e.stopPropagation();
};

/**
 * Start a dragging operation.
 * Called when scrollbar knob is clicked.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.ScrollbarSvg.prototype.onMouseDownKnob_ = function(e) {
  Blockly.hideChaff(true);
  this.onMouseUpKnob_();
  if (e.button == 2) {
    // Right-click.
    // Scrollbars have no context menu.
    e.stopPropagation();
    return;
  }
  // Look up the current translation and record it.
  this.startDragKnob = parseFloat(
      this.svgKnob_.getAttribute(this.horizontal_ ? 'x' : 'y'));
  // Record the current mouse position.
  this.startDragMouse = this.horizontal_ ? e.clientX : e.clientY;
  Blockly.ScrollbarSvg.onMouseUpWrapper_ = Blockly.bindEvent_(Blockly.svgDoc,
      'mouseup', this, this.onMouseUpKnob_);
  Blockly.ScrollbarSvg.onMouseMoveWrapper_ = Blockly.bindEvent_(Blockly.svgDoc,
      'mousemove', this, this.onMouseMoveKnob_);
  e.stopPropagation();
};

/**
 * Drag the scrollbar's knob.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.ScrollbarSvg.prototype.onMouseMoveKnob_ = function(e) {
  var currentMouse = this.horizontal_ ? e.clientX : e.clientY;
  var mouseDelta = currentMouse - this.startDragMouse;
  var knobValue = this.startDragKnob + mouseDelta;
  // Position the bar.
  this.svgKnob_.setAttribute(this.horizontal_ ? 'x' : 'y',
                             this.constrainKnob_(knobValue));
  this.onScroll_();
};

/**
 * Stop binding to the global mouseup and mousemove events.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.ScrollbarSvg.prototype.onMouseUpKnob_ = function(e) {
  if (Blockly.ScrollbarSvg.onMouseUpWrapper_) {
    Blockly.unbindEvent_(Blockly.svgDoc, 'mouseup',
                         Blockly.ScrollbarSvg.onMouseUpWrapper_);
    Blockly.ScrollbarSvg.onMouseUpWrapper_ = null;
  }
  if (Blockly.ScrollbarSvg.onMouseMoveWrapper_) {
    Blockly.unbindEvent_(Blockly.svgDoc, 'mousemove',
                         Blockly.ScrollbarSvg.onMouseMoveWrapper_);
    Blockly.ScrollbarSvg.onMouseMoveWrapper_ = null;
  }
};

/**
 * Constrain the knob's position within the minimum (0) and maximum
 * (length of scrollbar) values allowed for the scrollbar.
 * @param {number} value Value that is potentially out of bounds.
 * @return {number} Constrained value.
 * @private
 */
Blockly.ScrollbarSvg.prototype.constrainKnob_ = function(value) {
  if (value <= 0 || isNaN(value)) {
    value = 0;
  } else {
    var axis = this.horizontal_ ? 'width' : 'height';
    var barLength = parseFloat(this.svgBackground_.getAttribute(axis));
    var knobLength = parseFloat(this.svgKnob_.getAttribute(axis));
    value = Math.min(value, barLength - knobLength);
  }
  return value;
};

/**
 * Called when scrollbar is moved.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.ScrollbarSvg.prototype.onScroll_ = function(e) {
  var knobValue = parseFloat(
      this.svgKnob_.getAttribute(this.horizontal_ ? 'x' : 'y'));
  var barLength = parseFloat(
      this.svgBackground_.getAttribute(this.horizontal_ ? 'width' : 'height'));
  var ratio = knobValue / barLength;
  if (isNaN(ratio)) {
    ratio = 0;
  }
  var xyRatio = {};
  if (this.horizontal_) {
    xyRatio.x = ratio;
  } else {
    xyRatio.y = ratio;
  }
  this.setMetrics_(xyRatio);
};

/**
 * Set the scrollbar slider's position.
 * @param {number} value The distance from the top/left end of the bar.
 * @param {boolean} fireEvents True if onScroll events should be fired.
 */
Blockly.ScrollbarSvg.prototype.set = function(value, fireEvents) {
  // Move the scrollbar slider.
  this.svgKnob_.setAttribute(this.horizontal_ ? 'x' : 'y', value * this.ratio_);

  if (fireEvents) {
    this.onScroll_();
  }
};

/**
 * Width of a vertical scrollbar or height of a horizontal scrollbar.
 */
Blockly.ScrollbarSvg.scrollbarThickness = 15;

Blockly.Scrollbar = {};

/**
 * Choose between the native and the SVG implementations.  The native one is
 * preferred, provided that the browser supports it.
 * To test, see: tests/native_scrollbar_test.html
 */
(function() {
  var useNative = false;
  var ua = window.navigator.userAgent;
  var isGecko = ua.indexOf('Gecko/') != -1;
  var isMac = window.navigator.platform == 'MacIntel';
  var isLinux = window.navigator.platform.indexOf('Linux') != -1;
  // Known good user agents:
  // Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:10.0.2)
  //     Gecko/20100101 Firefox/10.0.2
  // Mozilla/5.0 (Ubuntu; X11; Linux x86_64; rv:9.0.1)
  //     Gecko/20100101 Firefox/9.0.1
  if (isGecko && (isMac || isLinux)) {
    useNative = true;
  }
  if (useNative) {
    Blockly.Scrollbar = Blockly.ScrollbarNative;
  } else {
    Blockly.Scrollbar = Blockly.ScrollbarSvg;
  }
})();

Blockly.Scrollbar.insertAfter_ = function(newNode, refNode) {
  var siblingNode = refNode.nextSibling;
  var parentNode = refNode.parentNode;
  if (!parentNode) {
    throw 'Reference node has no parent.';
  }
  if (siblingNode) {
    parentNode.insertBefore(newNode, siblingNode);
  } else {
    parentNode.appendChild(newNode);
  }
};

