// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
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

goog.provide('AI.Blockly.FieldFlydown');

goog.require('Blockly.FieldTextInput');
goog.require('Blockly.Options');

/**
 * Class for a clickable parameter field.
 * @param {string} text The initial parameter name in the field.
 * @param {boolean} isEditable Whether the user is allowed to change the name
 *     of this parameter or not.
 * @param {string=} opt_displayLocation The location to display the flydown at
 *     Either: Blockly.FieldFlydown.DISPLAY_BELOW,
 *             Blockly.FieldFlydown.DISPLAY_RIGHT
 *     Defaults to DISPLAY_RIGHT.
 * @param {Function} opt_changeHandler An optional function that is called
 *     to validate any constraints on what the user entered.  Takes the new
 *     text as an argument and returns the accepted text or null to abort
 *     the change. E.g., for an associated getter/setter this could change
 *     references to names in this field.
 * @extends {Blockly.FieldTextInput}
 * @constructor
 */
Blockly.FieldFlydown = function(name, isEditable, opt_displayLocation, opt_changeHandler) {
  // This by itself does not control editability
  this.EDITABLE = isEditable;
  // [lyn, 10/27/13] Make flydown direction an instance variable
  this.displayLocation = opt_displayLocation ||
      Blockly.FieldFlydown.DISPLAY_RIGHT;

  Blockly.FieldFlydown.superClass_.constructor.call(
      this, name, opt_changeHandler);
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
 * @type {Blockly.FieldFlydown}
 * @private
 */
Blockly.FieldFlydown.openFieldFlydown_ = null;

// These control the positions of the flydown.
Blockly.FieldFlydown.DISPLAY_BELOW = "BELOW";
Blockly.FieldFlydown.DISPLAY_RIGHT = "RIGHT";
Blockly.FieldFlydown.DISPLAY_LOCATION = Blockly.FieldFlydown.DISPLAY_BELOW;

/**
 * Default CSS class name for the field itself
 * @type {string}
 * @const
 */
Blockly.FieldFlydown.prototype.fieldCSSClassName = 'blocklyFieldFlydownField';

/**
 * Default CSS class name for the flydown that flies down from the field
 * @type {string}
 * @const
 */
Blockly.FieldFlydown.prototype.flyoutCSSClassName = 'blocklyFieldFlydownFlydown';

// Override FieldTextInput's showEditor_ so it's only called for EDITABLE field.
Blockly.FieldFlydown.prototype.showEditor_ = function() {
  if (!this.EDITABLE) {
    return;
  }
  if (Blockly.FieldFlydown.showPid_) {  // cancel a pending flydown for editing
    clearTimeout(Blockly.FieldFlydown.showPid_);
    Blockly.FieldFlydown.showPid_ = 0;
    Blockly.hideChaff();
  }
  Blockly.FieldFlydown.superClass_.showEditor_.call(this);
};

Blockly.FieldFlydown.prototype.init = function(block) {
  Blockly.FieldFlydown.superClass_.init.call(this, block);

  // Remove inherited field css classes ...
  Blockly.utils.removeClass(/** @type {!Element} */ (this.fieldGroup_),
      'blocklyEditableText');
  Blockly.utils.removeClass(/** @type {!Element} */ (this.fieldGroup_),
      'blocklyNoNEditableText');
  // ... and add new ones, so that look and feel of flyout fields can be customized
  Blockly.utils.addClass(/** @type {!Element} */ (this.fieldGroup_),
      this.fieldCSSClassName);

  this.mouseOverWrapper_ =
      Blockly.bindEvent_(this.fieldGroup_, 'mouseover', this, this.onMouseOver_);
  this.mouseOutWrapper_ =
      Blockly.bindEvent_(this.fieldGroup_, 'mouseout', this, this.onMouseOut_);
};

Blockly.FieldFlydown.prototype.onMouseOver_ = function(e) {
  if (!this.sourceBlock_.isInFlyout) { // [lyn, 10/22/13] No flydowns in a flyout!
    Blockly.FieldFlydown.showPid_ =
        window.setTimeout(this.showFlydownMaker_(), Blockly.FieldFlydown.timeout);
  }
  // This event has been handled.  No need to bubble up to the document.
  e.stopPropagation();
};

Blockly.FieldFlydown.prototype.onMouseOut_ = function(e) {
  // Clear any pending timer event to show flydown
  window.clearTimeout(Blockly.FieldFlydown.showPid_);
  e.stopPropagation();
};

/**
 * Returns a thunk that creates a Flydown block of the getter and setter blocks for receiver field.
 *  @return A thunk (zero-parameter function).
 */
Blockly.FieldFlydown.prototype.showFlydownMaker_ = function() {
  var field = this; // Name receiver in variable so can close over this variable in returned thunk
  return function() {
    if (Blockly.FieldFlydown.showPid_ !== 0 &&
        Blockly.dragMode_ === Blockly.DRAG_NONE && 
        !Blockly.FieldTextInput.htmlInput_) {
      try {
        field.showFlydown_();
      } catch (e) {
        console.error('Failed to show flydown', e);
      }
    }
    Blockly.FieldFlydown.showPid_ = 0;
  };
};

/**
 * Shows the blocks generated by flydownBlocksXML_ in the flydown. Xml should be
 * wrapped in <xml> tags.
 */
Blockly.FieldFlydown.prototype.showFlydown_ = function() {
  Blockly.hideChaff();
  var flydown = Blockly.getMainWorkspace().getFlydown();

  // Add flydown to top-level svg, *not* to main workspace svg
  // This is essential for correct positioning of flydown via translation
  // (If it's in workspace svg, it will be additionally translated by
  //  workspace svg translation relative to Blockly.svg.)
  Blockly.getMainWorkspace().getParentSvg().appendChild(flydown.svgGroup_);

  // Adjust scale for current zoom level
  var scale = flydown.targetWorkspace_.scale;
  flydown.workspace_.setScale(scale);

  flydown.setCSSClass(this.flyoutCSSClassName);

  var blocksXMLText = this.flydownBlocksXML_();
  var blocksDom = Blockly.Xml.textToDom(blocksXMLText);
  // [lyn, 11/10/13] Use goog.dom.getChildren rather than .children or
  //    .childNodes to make this code work across browsers.
  var blocksXMLList = goog.dom.getChildren(blocksDom);

  var xy = Blockly.getMainWorkspace().getSvgXY(this.borderRect_);
  var borderBBox = this.borderRect_.getBBox();
  if (this.displayLocation == Blockly.FieldFlydown.DISPLAY_BELOW) {
    xy.y += borderBBox.height * scale;
  } else { // Display right.
    xy.x += borderBBox.width * scale;
  }

  flydown.showAt(blocksXMLList, xy.x, xy.y);
  Blockly.FieldFlydown.openFieldFlydown_ = this;
};

/**
 * Hide the flydown menu and squash any timer-scheduled flyout creation
 */
Blockly.FieldFlydown.hide = function() {
  // Clear any pending timer event to show flydown.
  window.clearTimeout(Blockly.FieldFlydown.showPid_);
  // Hide any displayed flydown.
  var flydown = Blockly.getMainWorkspace().getFlydown();
  if (flydown) {
    flydown.hide();
  }
};


// TODO: Changing how validators work is not very future proof.
// Override Blockly's behavior; they call the validator after setting the text,
// which is incompatible with how our validators work (we expect to be called
// before the change since in order to find the old references to be renamed).
Blockly.FieldFlydown.prototype.onHtmlInputChange_ = function(e) {
  goog.asserts.assertObject(Blockly.FieldTextInput.htmlInput_);
  var htmlInput = Blockly.FieldTextInput.htmlInput_;
  var text = htmlInput.value;
  if (text !== htmlInput.oldValue_) {
    htmlInput.oldValue_ = text;
    var valid = true;
    if (this.sourceBlock_) {
      valid = this.callValidator(htmlInput.value);
    }
    if (valid === null) {
      Blockly.utils.addClass(htmlInput, 'blocklyInvalidInput');
    } else {
      Blockly.utils.removeClass(htmlInput, 'blocklyInvalidInput');
      this.setText(valid);
    }
  } else if (goog.userAgent.WEBKIT) {
    // Cursor key.  Render the source block to show the caret moving.
    // Chrome only (version 26, OS X).
    this.sourceBlock_.render();
  }
  this.resizeEditor_();
  Blockly.svgResize(this.sourceBlock_.workspace);
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
