// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Field in which mouseover displays flyout-like menu of blocks
 * and mouse click edits the field name.
 * Flydowns are used in App Inventor for displaying get/set blocks for parameter names
 * and callers for procedure declarations.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */

'use strict';

goog.provide('Blockly.FieldFlydown');

goog.require('Blockly.FieldTextInput');

/**
 * Class for a clickable parameter field.
 * @param {string} text The initial parameter name in the field.
 * @param {Function} opt_changeHandler An optional function that is called
 *     to validate any constraints on what the user entered.  Takes the new
 *     text as an argument and returns the accepted text or null to abort
 *     the change. E.g., for an associated getter/setter this could change
 *     references to names in this field.
 * @extends {Blockly.Field}
 * @constructor
 */

Blockly.FieldFlydown = function(name, isEditable, displayLocation, opt_changeHandler) {
  Blockly.FieldFlydown.superClass_.constructor.call(this, name, opt_changeHandler);

  this.EDITABLE = isEditable; // This by itself does not control editability
  this.displayLocation = displayLocation; // [lyn, 10/27/13] Make flydown direction an instance variable
  // this.fieldGroup_.style.cursor = '';

  // Remove inherited field css classes ...
  Blockly.removeClass_(/** @type {!Element} */ (this.fieldGroup_),
      'blocklyEditableText');
  Blockly.removeClass_(/** @type {!Element} */ (this.fieldGroup_),
      'blocklyNoNEditableText');
  // ... and add new one, so that look and feel of flyout fields can be customized
  Blockly.addClass_(/** @type {!Element} */ (this.fieldGroup_),
      this.fieldCSSClassName);

  // Only want one flydown object and associated svg per workspace
  if (! Blockly.mainWorkspace.FieldFlydown) {
    var flydown = new Blockly.Flydown();
    // ***** [lyn, 10/05/2013] NEED TO WORRY ABOUT MULTIPLE BLOCKLIES! *****
    Blockly.mainWorkspace.FieldFlydown = flydown;
    var flydownSvg = flydown.createDom(this.flyoutCSSClassName);
    Blockly.svg.appendChild(flydownSvg); // Add flydown to top-level svg, *not* to main workspace svg
                                         // This is essential for correct positioning of flydown via translation
                                         // (If it's in workspace svg, it will be additionally translated by
                                         //  workspace svg translation relative to Blockly.svg.)
    flydown.init(Blockly.mainWorkspace, false); // false means no scrollbar
    flydown.autoClose = true; // Flydown closes after selecting a block
  }
};
goog.inherits(Blockly.FieldFlydown, Blockly.FieldTextInput);

/**
 * Milliseconds to wait before showing flydown after mouseover event on flydown field.
 * @type {number}
 * @const
 */
Blockly.FieldFlydown.timeout = 500;

/**
 * Process ID for timer event to show flydown (scheduled by mouseover event)
 * @type {number}
 * @const
 */
Blockly.FieldFlydown.showPid_ = 0;

/**
 * Which instance of FieldFlydown (or a subclass) is an open flydown attached to?
 * @type {Blockly.FieldFlydown (or subclass)}
 * @private
 */
Blockly.FieldFlydown.openFieldFlydown_ = null;

/**
 * Control positioning of flydown
 */
Blockly.FieldFlydown.DISPLAY_BELOW = "BELOW";
Blockly.FieldFlydown.DISPLAY_RIGHT = "RIGHT";
Blockly.FieldFlydown.DISPLAY_LOCATION = Blockly.FieldFlydown.DISPLAY_BELOW; // [lyn, 10/14/13] Make global for now, change in future

/**
 * Default CSS class name for the field itself
 * @type {String}
 * @const
 */
Blockly.FieldFlydown.prototype.fieldCSSClassName = 'blocklyFieldFlydownField';

/**
 * Default CSS class name for the flydown that flies down from the field
 * @type {String}
 * @const
 */
Blockly.FieldFlydown.prototype.flyoutCSSClassName = 'blocklyFieldFlydownFlydown';

// Override FieldTextInput's showEditor_ so it's only called for EDITABLE field.
Blockly.FieldFlydown.prototype.showEditor_ = function() {
  if (this.EDITABLE) {
    Blockly.FieldFlydown.superClass_.showEditor_.call(this);
  }
}

Blockly.FieldFlydown.prototype.init = function(block) {
  Blockly.FieldFlydown.superClass_.init.call(this, block);
  this.mouseOverWrapper_ =
      Blockly.bindEvent_(this.fieldGroup_, 'mouseover', this, this.onMouseOver_);
  this.mouseOutWrapper_ =
      Blockly.bindEvent_(this.fieldGroup_, 'mouseout', this, this.onMouseOut_);
};

Blockly.FieldFlydown.prototype.onMouseOver_ = function(e) {
  // alert("FieldFlydown mouseover");
  if (! this.sourceBlock_.isInFlyout) { // [lyn, 10/22/13] No flydowns in a flyout!
    Blockly.FieldFlydown.showPid_ =
        window.setTimeout(this.showFlydownMaker_(), Blockly.FieldFlydown.timeout);
    // This event has been handled.  No need to bubble up to the document.
  }
  e.stopPropagation();
};

Blockly.FieldFlydown.prototype.onMouseOut_ = function(e) {
  // Clear any pending timer event to show flydown
  window.clearTimeout(Blockly.FieldFlydown.showPid_);
  var flydown = Blockly.mainWorkspace.FieldFlydown;
  e.stopPropagation();
};

/**
 * Returns a thunk that creates a Flydown block of the getter and setter blocks for receiver field.
 *  @return A thunk (zero-parameter function).
 */
Blockly.FieldFlydown.prototype.showFlydownMaker_ = function() {
  var field = this; // Name receiver in variable so can close over this variable in returned thunk
  return function() {
    if (Blockly.FieldFlydown.showPid_ != 0) {
      field.showFlydown_();
      Blockly.FieldFlydown.showPid_ = 0;
    }
  };
};

/**
 * Creates a Flydown block of the getter and setter blocks for the parameter name in this field.
 */
Blockly.FieldFlydown.prototype.showFlydown_ = function() {
  // Create XML elements from blocks and then create the blocks from the XML elements.
  // This is a bit crazy, but it's simplest that way. Otherwise, we'd have to duplicate
  // much of the code in Blockly.Flydown.prototype.show.
  // alert("FieldFlydown show Flydown");
  Blockly.hideChaff(); // Hide open context menus, dropDowns, flyouts, and other flydowns
  Blockly.FieldFlydown.openFieldFlydown_ = this; // Remember field to which flydown is attached
  var flydown = Blockly.mainWorkspace.FieldFlydown;
  flydown.setCSSClass(this.flyoutCSSClassName); // This could have been changed by another field.
  var blocksXMLText = this.flydownBlocksXML_()
  var blocksDom = Blockly.Xml.textToDom(blocksXMLText);
  // [lyn, 11/10/13] Use goog.dom.getChildren rather than .children or .childNodes
  //   to make this code work across browsers.
  var blocksXMLList = goog.dom.getChildren(blocksDom); // List of blocks for flydown
  var xy = Blockly.getSvgXY_(this.borderRect_);
  var borderBBox = this.borderRect_.getBBox();
  var x = xy.x;
  var y = xy.y;
  if (this.displayLocation === Blockly.FieldFlydown.DISPLAY_BELOW) {
    y = y + borderBBox.height;
  } else { // if (this.displayLocation === Blockly.FieldFlydown.DISPLAY_RIGHT) {
    x = x + borderBBox.width;
  }
  Blockly.mainWorkspace.FieldFlydown.showAt(blocksXMLList, x, y);
};

/**
 * Hide the flydown menu and squash any timer-scheduled flyout creation
 */
Blockly.FieldFlydown.hide = function() {
  // Clear any pending timer event to show flydown
  window.clearTimeout(Blockly.FieldFlydown.showPid_);
  // Clear any displayed flydown
  var flydown = Blockly.mainWorkspace.FieldFlydown;
  if (flydown) {
    flydown.hide();
  }
  Blockly.FieldDropdown.openFieldFlydown_ = null;
};

/**
 * Close the flydown and dispose of all UI.
 */
Blockly.FieldFlydown.prototype.dispose = function() {
  if (Blockly.FieldFlydown.openFieldFlydown_ == this) {
    Blockly.FieldFlydown.hide();
  }
  // Call parent's destructor.
  Blockly.FieldTextInput.prototype.dispose.call(this);
};


