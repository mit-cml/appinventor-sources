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
 * @implements {Blockly.IPositionable}
 */
Blockly.WarningIndicator = class {
  /**
   * Create a warning indicator.
   * @param workspace
   */
  constructor(workspace) {
    this.workspace_ = workspace;
  }
}

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

  this.svgGroup_ = Blockly.utils.dom.createSvgElement('g',
      {'id': "indicatorWarning"}, null);
  this.warningCount_ = Blockly.utils.dom.createSvgElement('text',
      {'fill': "black", 'transform':"translate(20,-1)"},
      this.svgGroup_);
  this.warningCount_.textContent = "0";


  this.iconGroup_ = Blockly.utils.dom.createSvgElement('g',
      {'class': 'blocklyIconGroup', 'translate':"transform(0,-15)"}, this.svgGroup_);
  var iconShield = Blockly.utils.dom.createSvgElement('path',
      {'class': 'blocklyWarningIconShield',
       'd': 'M 2,0 Q -1,0 0.5,-3 L 6.5,-13.3 Q 8,-16 9.5,-13.3 L 15.5,-3 ' +
       'Q 17,0 14,0 z'},
      this.iconGroup_);
  this.iconMark_ = Blockly.utils.dom.createSvgElement('text',
      {'class': 'blocklyWarningIconMark',
       'x': AI.ErrorIcon.ICON_RADIUS,
       'y': 2 * AI.ErrorIcon.ICON_RADIUS - 18}, this.iconGroup_);
  this.iconMark_.appendChild(document.createTextNode('!'));


  this.errorCount_ = Blockly.utils.dom.createSvgElement('text',
      {'fill': "black", 'transform':"translate(85,-1)"},
      this.svgGroup_);
  this.errorCount_.textContent = "0";

  this.iconErrorGroup_ = Blockly.utils.dom.createSvgElement('g',
      {'class': 'blocklyIconGroup', 'transform':"translate(65,0)"}, this.svgGroup_);
  Blockly.utils.dom.createSvgElement('circle',
      {'class': 'blocklyErrorIconOutline',
       'r': AI.ErrorIcon.ICON_RADIUS,
       'cx': AI.ErrorIcon.ICON_RADIUS,
       'cy': AI.ErrorIcon.ICON_RADIUS - 15}, this.iconErrorGroup_);
  Blockly.utils.dom.createSvgElement('path',
      {'class': 'blocklyErrorIconX',
       'd': 'M 4,-11 12,-3 8,-7 4,-3 12,-11'},
                           // X fills circle vvv
       //'d': 'M 3.1931458,3.1931458 12.756854,12.756854 8,8 3.0931458,12.756854 12.756854,3.0931458'},
      this.iconErrorGroup_);

  this.warningToggleGroup_ = Blockly.utils.dom.createSvgElement('g', {}, this.svgGroup_);
  this.warningToggle_ = Blockly.utils.dom.createSvgElement('rect',
      {'width':"120", 'height':"20", 'x':"-15",'y':"20",'class':"warningNav"},
      this.warningToggleGroup_);
  this.warningToggleText_ = Blockly.utils.dom.createSvgElement('text',
      {'fill': "black", 'transform':"translate(45,35)",'text-anchor':"middle",'style':"font-size:10pt;cursor:pointer;"},
      this.warningToggleGroup_);
  this.warningToggleText_.textContent = Blockly.Msg.SHOW_WARNINGS;

  this.warningNavPrevious_ = Blockly.utils.dom.createSvgElement('path',
      {"d": "M 0,7 L 10,17 L 20,7 Z", 'class':"warningNav"},
      this.svgGroup_);

  this.warningNavNext_ = Blockly.utils.dom.createSvgElement('path',
      {"d": "M 10,-31 L 0,-21 L 20,-21 Z", 'class':"warningNav"},
      this.svgGroup_);

  this.errorNavPrevious_ = Blockly.utils.dom.createSvgElement('path',
      {"d": "M 67,7 L 77,17 L 87,7 Z", 'class':"warningNav"},
      this.svgGroup_);

  this.errorNavNext_ = Blockly.utils.dom.createSvgElement('path',
      {"d": "M 87,-21 L 67,-21 L 77,-31 Z", 'class':"warningNav"},
      this.svgGroup_);

  return this.svgGroup_;
};

/**
 * Initialize the warning indicator.
 */
Blockly.WarningIndicator.prototype.init = function() {
  this.workspace_.getComponentManager().addComponent({
    component: this,
    weight: 2,
    capabilities: [Blockly.ComponentManager.Capability.POSITIONABLE],
  });
  // If the document resizes, reposition the warning indicator.
  // Blockly.browserEvents.bind(window, 'resize', this, this.position_);
  Blockly.browserEvents.bind(this.warningToggleGroup_, 'click', this, Blockly.WarningIndicator.prototype.onclickWarningToggle);
  Blockly.browserEvents.bind(this.warningNavPrevious_, 'click', this, Blockly.WarningIndicator.prototype.onclickWarningNavPrevious);
  Blockly.browserEvents.bind(this.warningNavNext_, 'click', this, Blockly.WarningIndicator.prototype.onclickWarningNavNext);
  Blockly.browserEvents.bind(this.errorNavPrevious_, 'click', this, Blockly.WarningIndicator.prototype.onclickErrorNavPrevious);
  Blockly.browserEvents.bind(this.errorNavNext_, 'click', this, Blockly.WarningIndicator.prototype.onclickErrorNavNext);

  // We stop propagating the mousedown event so that Blockly doesn't prevent click events in Firefox, which breaks
  // the click event handler above.
  Blockly.browserEvents.bind(this.warningToggleGroup_, 'mousedown', this, function(e) { e.stopPropagation() });
  Blockly.browserEvents.bind(this.warningNavPrevious_, 'mousedown', this, function(e) { e.stopPropagation() });
  Blockly.browserEvents.bind(this.warningNavNext_, 'mousedown', this, function(e) { e.stopPropagation() });
  Blockly.browserEvents.bind(this.errorNavPrevious_, 'mousedown', this, function(e) { e.stopPropagation() });
  Blockly.browserEvents.bind(this.errorNavNext_, 'mousedown', this, function(e) { e.stopPropagation() });
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
  this.warningNavPrevious_ = null;
  this.warningNavLeftText_ = null;
  this.warningNavNext_ = null;
  this.warningNavRightText_ = null;

};

/**
 * Move the warning indicator to the bottom-left corner.
 * @param {Blockly.MetricsManager.UiMetrics} metrics The workspace metrics.
 * @private
 */
Blockly.WarningIndicator.prototype.position_ = function(metrics) {
  if (!metrics) {
    // There are no metrics available (workspace is probably not visible).
    return;
  }
  if (Blockly.RTL) {
    this.left_ = this.MARGIN_SIDE_;
  } else {
    this.left_ = metrics.absoluteMetrics.left + this.MARGIN_SIDE_;
  }
  this.top_ = metrics.viewMetrics.height + metrics.absoluteMetrics.top -
      (this.INDICATOR_HEIGHT_) - this.MARGIN_BOTTOM_;
  this.svgGroup_.setAttribute('transform',
      'translate(' + this.left_ + ',' + this.top_ + ')');
};

/**
 * Update the error and warning count on the indicator.
 *
 */
Blockly.WarningIndicator.prototype.updateWarningAndErrorCount = function() {
  this.errorCount_.textContent = this.workspace_.getWarningHandler().errorCount;
  this.warningCount_.textContent = this.workspace_.getWarningHandler().warningCount;
}

Blockly.WarningIndicator.prototype.updateCurrentWarningAndError = function(currentWarning, currentError) {
  var handler = this.workspace_.getWarningHandler();
  currentError++;  // make it 1-based
  currentWarning++;  // make it 1-based
  this.errorCount_.textContent = currentError + "/" + handler.errorCount;
  this.warningCount_.textContent = currentWarning + "/" + handler.warningCount;
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
  Blockly.hideChaff();
  window.parent.BlocklyPanel_callToggleWarning();
};

Blockly.WarningIndicator.prototype.onclickWarningNavPrevious = function() {
  Blockly.hideChaff();
  this.workspace_.getWarningHandler().previousWarning();
};

Blockly.WarningIndicator.prototype.onclickWarningNavNext = function() {
  Blockly.hideChaff();
  this.workspace_.getWarningHandler().nextWarning();
};

Blockly.WarningIndicator.prototype.onclickErrorNavPrevious = function() {
  Blockly.hideChaff();
  this.workspace_.getWarningHandler().previousError();
};

Blockly.WarningIndicator.prototype.onclickErrorNavNext = function() {
  Blockly.hideChaff();
  this.workspace_.getWarningHandler().nextError();
};

// Blockly.IPositionable implementation

/**
 * Positions the warning inicator.
 * It is positioned in the lower left corner of the workspace.
 * @param metrics The workspace metrics.
 * @param savedPositions List of rectangles that
 *     are already on the workspace.
 */
Blockly.WarningIndicator.prototype.position = function(metrics, savedPositions) {
  this.position_(metrics);
}

Blockly.WarningIndicator.prototype.getBoundingRectangle = function() {
  var width = 120;  // TODO: this is a guess
  return new Blockly.utils.Rect(this.left_, this.left_ + width, this.top_, this.top_ + this.INDICATOR_HEIGHT_)
}
