// -*- mode: Javascript; js-indent-level: 2; -*-
// Copyright © 2013-2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle warnings in the block editor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author ewpatton@mit.edu (Evan W. Patton);
 */

'use strict';

goog.provide('AI.Blockly.WarningIndicator');

goog.require('goog.Timer');


/**
 * Class for a warning indicator.
 * @param {!Function} getMetrics A function that returns workspace's metrics.
 * @constructor
 */
Blockly.WarningIndicator = function(workspace) {
  this.workspace_ = workspace;
};

/**
 * Height of the warning indicator.
 * @type {number}
 * @private
 */
Blockly.WarningIndicator.prototype.INDICATOR_HEIGHT_ = 40;

/**
 * Distance between warning indicator and bottom edge of workspace.
 * @type {number}
 * @private
 *///
Blockly.WarningIndicator.prototype.MARGIN_BOTTOM_ = 35;

/**
 * Distance between warning indicator and right edge of workspace.
 * @type {number}
 * @private
 */
Blockly.WarningIndicator.prototype.MARGIN_SIDE_ = 35;

/**
 * The SVG group containing the warning indicator.
 * @type {Element}
 * @private
 */
Blockly.WarningIndicator.prototype.svgGroup_ = null;

/**
 * Left coordinate of the warning indicator.
 * @type {number}
 * @private
 */
Blockly.WarningIndicator.prototype.left_ = 0;

/**
 * Top coordinate of the warning indicator.
 * @type {number}
 * @private
 */
Blockly.WarningIndicator.prototype.top_ = 0;

/**
 * Create the warning indicator elements.
 * @return {!Element} The warning indicator's SVG group.
 */
Blockly.WarningIndicator.prototype.createDom = function() {

  this.svgGroup_ = Blockly.utils.createSvgElement('g',
      {'id': "indicatorWarning"}, null);
  this.warningCount_ = Blockly.utils.createSvgElement('text',
      {'fill': "black", 'transform':"translate(20,14)"},
      this.svgGroup_);
  this.warningCount_.textContent = "0";


  this.iconGroup_ = Blockly.utils.createSvgElement('g',
      {'class': 'blocklyIconGroup', 'translate':"transform(0,0)"}, this.svgGroup_);
  var iconShield = Blockly.utils.createSvgElement('path',
      {'class': 'blocklyWarningIconShield',
       'd': 'M 2,15 Q -1,15 0.5,12 L 6.5,1.7 Q 8,-1 9.5,1.7 L 15.5,12 ' +
       'Q 17,15 14,15 z'},
      this.iconGroup_);
  this.iconMark_ = Blockly.utils.createSvgElement('text',
      {'class': 'blocklyWarningIconMark',
       'x': Blockly.Error.ICON_RADIUS,
       'y': 2 * Blockly.Error.ICON_RADIUS - 3}, this.iconGroup_);
  this.iconMark_.appendChild(document.createTextNode('!'));


  this.errorCount_ = Blockly.utils.createSvgElement('text',
      {'fill': "black", 'transform':"translate(85,14)"},
      this.svgGroup_);
  this.errorCount_.textContent = "0";

  this.iconErrorGroup_ = Blockly.utils.createSvgElement('g',
      {'class': 'blocklyIconGroup', 'transform':"translate(65,0)"}, this.svgGroup_);
  Blockly.utils.createSvgElement('circle',
      {'class': 'blocklyErrorIconOutline',
       'r': Blockly.Error.ICON_RADIUS,
       'cx': Blockly.Error.ICON_RADIUS,
       'cy': Blockly.Error.ICON_RADIUS}, this.iconErrorGroup_);
  Blockly.utils.createSvgElement('path',
      {'class': 'blocklyErrorIconX',
       'd': 'M 4,4 12,12 8,8 4,12 12,4'},
                           // X fills circle vvv
       //'d': 'M 3.1931458,3.1931458 12.756854,12.756854 8,8 3.0931458,12.756854 12.756854,3.0931458'},
      this.iconErrorGroup_);

  this.warningToggleGroup_ = Blockly.utils.createSvgElement('g', {}, this.svgGroup_);
  this.warningToggle_ = Blockly.utils.createSvgElement('rect',
      {'fill': "#eeeeee",'width':"120", 'height':"20", 'x':"-15",'y':"20",'style':"stroke:black;stroke-width:1;cursor:pointer;"},
      this.warningToggleGroup_);
  this.warningToggleText_ = Blockly.utils.createSvgElement('text',
      {'fill': "black", 'transform':"translate(45,35)",'text-anchor':"middle",'style':"font-size:10pt;cursor:pointer;"},
      this.warningToggleGroup_);
  this.warningToggleText_.textContent = Blockly.Msg.SHOW_WARNINGS;

  this.warningNavLeft_ = Blockly.utils.createSvgElement('path',
      {'fill': "#eeeeee", "d": "M -15,8 L -5,18 L -5,-1 Z", 'style':"stroke:black;stroke-width:1;cursor:pointer;"},
      this.svgGroup_);

  this.warningNavRight_ = Blockly.utils.createSvgElement('path',
      {'fill': "#eeeeee", "d": "M 40,8 L 30,18 L 30,-1 Z", 'style':"stroke:black;stroke-width:1;cursor:pointer;"},
      this.svgGroup_);

  this.errorNavLeft_ = Blockly.utils.createSvgElement('path',
      {'fill': "#eeeeee", "d": "M 52,8 L 62,18 L 62,-1 Z", 'style':"stroke:black;stroke-width:1;cursor:pointer;"},
      this.svgGroup_);

  this.errorNavRight_ = Blockly.utils.createSvgElement('path',
      {'fill': "#eeeeee", "d": "M 105,8 L 95,18 L 95,-1 Z", 'style':"stroke:black;stroke-width:1;cursor:pointer;"},
      this.svgGroup_);

  return this.svgGroup_;
};

Blockly.WarningIndicator.prototype.hideWarningNav = function() {

}

/**
 * Initialize the warning indicator.
 */
Blockly.WarningIndicator.prototype.init = function() {
  this.position_();
  // If the document resizes, reposition the warning indicator.
  Blockly.bindEvent_(window, 'resize', this, this.position_);
  Blockly.bindEvent_(this.warningToggleGroup_, 'click', this, Blockly.WarningIndicator.prototype.onclickWarningToggle);
  Blockly.bindEvent_(this.warningNavLeft_, 'click', this, Blockly.WarningIndicator.prototype.onclickWarningNavLeft);
  Blockly.bindEvent_(this.warningNavRight_, 'click', this, Blockly.WarningIndicator.prototype.onclickWarningNavRight);
  Blockly.bindEvent_(this.errorNavLeft_, 'click', this, Blockly.WarningIndicator.prototype.onclickErrorNavLeft);
  Blockly.bindEvent_(this.errorNavRight_, 'click', this, Blockly.WarningIndicator.prototype.onclickErrorNavRight);

  // We stop propagating the mousedown event so that Blockly doesn't prevent click events in Firefox, which breaks
  // the click event handler above.
  Blockly.bindEvent_(this.warningToggleGroup_, 'mousedown', this, function(e) { e.stopPropagation() });
  Blockly.bindEvent_(this.warningNavLeft_, 'mousedown', this, function(e) { e.stopPropagation() });
  Blockly.bindEvent_(this.warningNavRight_, 'mousedown', this, function(e) { e.stopPropagation() });
  Blockly.bindEvent_(this.errorNavLeft_, 'mousedown', this, function(e) { e.stopPropagation() });
  Blockly.bindEvent_(this.errorNavRight_, 'mousedown', this, function(e) { e.stopPropagation() });

  // Stopping propagation of the mousedown event breaks touch events on tablets. We register here for touchend on the
  // toggle button so that we can simulate a click event.
  Blockly.bindEvent_(this.warningToggleGroup_, 'touchend', this, Blockly.WarningIndicator.prototype.onclickWarningToggle);
  Blockly.bindEvent_(this.warningNavLeft_, 'touchend', this, function(e) { e.stopPropagation() });
  Blockly.bindEvent_(this.warningNavRight_, 'touchend', this, function(e) { e.stopPropagation() });
  Blockly.bindEvent_(this.errorNavLeft_, 'touchend', this, function(e) { e.stopPropagation() });
  Blockly.bindEvent_(this.errorNavRight_, 'touchend', this, function(e) { e.stopPropagation() });
};

/**
 * Dispose of this warning indicator.
 * Unlink from all DOM elements to prevent memory leaks.
 */
Blockly.WarningIndicator.prototype.dispose = function() {
  if (this.svgGroup_) {
    goog.dom.removeNode(this.svgGroup_);
    this.svgGroup_ = null;
  }

  this.getMetrics_ = null;

  this.warningCount_ = null;
  this.iconGroup_ = null;
  this.iconMark_ = null;

  this.errorCount_ = null;
  this.iconErrorGroup_ = null;
  this.iconErrorMark_ = null;

  this.warningToggle_ = null;
  this.warningToggleText_ = null;
  this.warningNavLeft_ = null;
  this.warningNavLeftText_ = null;
  this.warningNavRight_ = null;
  this.warningNavRightText_ = null;

};

/**
 * Move the warning indicator to the bottom-left corner.
 * @private
 */
Blockly.WarningIndicator.prototype.position_ = function() {
  var metrics = this.workspace_.getMetrics();
  if (!metrics) {
    // There are no metrics available (workspace is probably not visible).
    return;
  }
  if (Blockly.RTL) {
    this.left_ = this.MARGIN_SIDE_;
  } else {
    this.left_ = metrics.absoluteLeft + this.MARGIN_SIDE_;
  }
  this.top_ = metrics.viewHeight + metrics.absoluteTop -
      (this.INDICATOR_HEIGHT_) - this.MARGIN_BOTTOM_;
  this.svgGroup_.setAttribute('transform',
      'translate(' + this.left_ + ',' + this.top_ + ')');
};


/**
 * Update the error and warning count on the indicator.
 *
 */
Blockly.WarningIndicator.prototype.updateWarningAndErrorCount = function() {
//  this.errorCount_.textContent = this.workspace_.getWarningHandler().errorCount;
  this.errorCount_.textContent = this.workspace_.getWarningHandler().errorCount;
  this.warningCount_.textContent = this.workspace_.getWarningHandler().warningCount;
}

/**
 * Change the warning toggle button to have the correct text.
 *
 */
Blockly.WarningIndicator.prototype.updateWarningToggleText = function() {
  if(this.workspace_.getWarningHandler().showWarningsToggle) {
    this.warningToggleText_.textContent = Blockly.Msg.HIDE_WARNINGS;
  } else {
    this.warningToggleText_.textContent = Blockly.Msg.SHOW_WARNINGS;
  }
}

/**
 * Call to change the current warning state on all screens.
 *
 */
Blockly.WarningIndicator.prototype.onclickWarningToggle = function() {
  window.parent.BlocklyPanel_callToggleWarning();
}

Blockly.WarningIndicator.prototype.onclickWarningNavLeft = function() {
  this.workspace_.getWarningHandler().warningNavLeft();
}
Blockly.WarningIndicator.prototype.onclickWarningNavRight = function() {
  this.workspace_.getWarningHandler().warningNavRight();
}


Blockly.WarningIndicator.prototype.onclickErrorNavLeft = function() {
  this.workspace_.getWarningHandler().errorNavLeft();
}
Blockly.WarningIndicator.prototype.onclickErrorNavRight = function() {
  this.workspace_.getWarningHandler().errorNavRight();
}
