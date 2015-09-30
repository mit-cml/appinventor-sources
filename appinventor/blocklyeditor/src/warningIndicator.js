// -*- mode: Javascript; js-indent-level: 4; -*-
// Copyright 2013 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle warnings in the block editor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.WarningIndicator');

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

  this.svgGroup_ = Blockly.createSvgElement('g',
      {'id': "indicatorWarning"}, null);
  this.warningCount_ = Blockly.createSvgElement('text',
      {'fill': "black", 'transform':"translate(20,14)"},
      this.svgGroup_);
  this.warningCount_.textContent = "0";


  this.iconGroup_ = Blockly.createSvgElement('g',
      {'class': 'blocklyIconGroup', 'translate':"transform(0,0)"}, this.svgGroup_);
  var iconShield = Blockly.createSvgElement('path',
      {'class': 'blocklyWarningIconShield',
       'd': 'M 2,15 Q -1,15 0.5,12 L 6.5,1.7 Q 8,-1 9.5,1.7 L 15.5,12 ' +
       'Q 17,15 14,15 z'},
      this.iconGroup_);
  this.iconMark_ = Blockly.createSvgElement('text',
      {'class': 'blocklyWarningIconMark',
       'x': Blockly.ErrorIcon.ICON_RADIUS,
       'y': 2 * Blockly.ErrorIcon.ICON_RADIUS - 3}, this.iconGroup_);
  this.iconMark_.appendChild(document.createTextNode('!'));


  this.errorCount_ = Blockly.createSvgElement('text',
      {'fill': "black", 'transform':"translate(75,14)"},
      this.svgGroup_);
  this.errorCount_.textContent = "0";

  this.iconErrorGroup_ = Blockly.createSvgElement('g',
      {'class': 'blocklyIconGroup', 'transform':"translate(55,0)"}, this.svgGroup_);
  var iconErrorShield = Blockly.createSvgElement('path',
      {'class': 'blocklyErrorIconShield',
       'd': 'M 2,15 Q -1,15 0.5,12 L 6.5,1.7 Q 8,-1 9.5,1.7 L 15.5,12 ' +
       'Q 17,15 14,15 z','x':20},
      this.iconErrorGroup_);
  this.iconErrorMark_ = Blockly.createSvgElement('text',
      {'class': 'blocklyIconMark',
       'x': Blockly.ErrorIcon.ICON_RADIUS,
       'y': 2 * Blockly.ErrorIcon.ICON_RADIUS - 3}, this.iconErrorGroup_);
  this.iconErrorMark_.appendChild(document.createTextNode('!'));

  this.warningToggle_ = Blockly.createSvgElement('rect',
      {'fill': "#eeeeee",'width':"120", 'height':"20", 'x':"-15",'y':"20",'style':"stroke:black;stroke-width:1;cursor:pointer;"},
      this.svgGroup_);
  this.warningToggleText_ = Blockly.createSvgElement('text',
      {'fill': "black", 'transform':"translate(45,35)",'text-anchor':"middle",'style':"font-size:10pt;cursor:pointer;"},
      this.svgGroup_);
  this.warningToggleText_.textContent = Blockly.Msg.SHOW_WARNINGS;

  return this.svgGroup_;
};

/**
 * Initialize the warning indicator.
 */
Blockly.WarningIndicator.prototype.init = function() {
  this.position_();
  // If the document resizes, reposition the warning indicator.
  Blockly.bindEvent_(window, 'resize', this, this.position_);
  Blockly.bindEvent_(this.warningToggle_, 'mouseup', this, Blockly.WarningIndicator.prototype.onclickWarningToggle);
  Blockly.bindEvent_(this.warningToggleText_, 'mouseup', this, Blockly.WarningIndicator.prototype.onclickWarningToggle);
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
  this.errorCount_.textContent = Blockly.WarningHandler.errorCount;
  this.warningCount_.textContent = Blockly.WarningHandler.warningCount;
}

/**
 * Change the warning toggle button to have the correct text.
 *
 */
Blockly.WarningIndicator.prototype.updateWarningToggleText = function() {
  if(Blockly.WarningHandler.showWarningsToggle) {
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