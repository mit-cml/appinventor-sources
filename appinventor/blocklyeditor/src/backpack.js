/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
 * http://blockly.googlecode.com/
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
 * @fileoverview A backpack object that can carry one or more blocks
 *  among workspaces. Blocks can be added to the backpack through the
 *  block's Context menu and retrieved through the workspace's context
 *  menu.
 *
 * This is called from BlocklyPanel.
 *
 *  Backpack contents are stored in Blockly.backpack_
 *
 * @author fraser@google.com (Neil Fraser)
 * @author ram8647@gmail.com (Ralph Morelli)
 * @autor vbrown@wellesley.edu (Tori Brown)
 */
'use strict';

goog.provide('Blockly.Backpack');
goog.require('Blockly.Util');

/**
 * Class for a backpack.
 * @param {!Blockly.Workspace} workspace The Workspace to sit it.
 * @constructor
 */
Blockly.Backpack = function(workspace) {
  this.workspace_ = workspace;
};

/**
 * URL of the small backpack image.
 * @type {string}
 * @private
 */
Blockly.Backpack.prototype.BPACK_CLOSED_ = 'media/backpack-closed.png';

/**
 * URL of the small backpack image.
 * @type {string}
 * @private
 */
//Blockly.Backpack.prototype.BPACK_EMPTY_ = 'media/backpack-small-highlighted.png';
Blockly.Backpack.prototype.BPACK_EMPTY_ = 'media/backpack-empty.png';

/**
 * URL of the full backpack image
 * @type {string}
 * @private
 */
Blockly.Backpack.prototype.BPACK_FULL_ = 'media/backpack-full.png';

/**
 * Width of the image.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.WIDTH_ = 80;

/**
 * Height of the image.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.BODY_HEIGHT_ = 75;

/**
 * Distance between backpack and top edge of workspace.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.MARGIN_TOP_ = 10;

/**
 * Distance between backpack and right edge of workspace.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.MARGIN_SIDE_ = 20;

/**
 * Current small/large state of the backpack.
 * @type {boolean}
 */
Blockly.Backpack.prototype.isLarge = false;

/**
 * Current over state of the mouse over backpack.
 * @type {boolean}
 */
Blockly.Backpack.prototype.isOver = false;

/**
 * Current state whether a block is added to backpack.
 * @type {boolean}
 */
Blockly.Backpack.prototype.isAdded = false;

/**
 * The SVG group containing the backpack.
 * @type {Element}
 * @private
 */
Blockly.Backpack.prototype.svgGroup_ = null;

/**
 * The SVG image element of the backpack body.
 * @type {Element}
 * @private
 */
Blockly.Backpack.prototype.svgBody_ = null;

/**
 * Task ID of small/big animation.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.resizeTask_ = 0;

/**
 * Task ID of opening/closing animation.
 * @type {number}
 * @private
 */
Blockly.Trashcan.prototype.openTask_ = 0;

/**
 * Left coordinate of the backpack.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.left_ = 0;

/**
 * Top coordinate of the backpack.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.top_ = 0;

/**
 * Starting x coordinate for snapping back
 * @type {number}
 * @private
 */

// Commented out, not used
// Blockly.Backpack.prototype.startX = 0;

/**
 * Starting y coordinate for snapping back
 * @type {number}
 * @private
*/

// Commented out, not used
// Blockly.Backpack.prototype.startY = 0;

/**
 * Create the backpack SVG elements.
 * @return {!Element} The backpack's SVG group.
 */
Blockly.Backpack.prototype.createDom = function() {
  Blockly.Backpack.flyout_ = new Blockly.backpackFlyout();
  // insert the flyout after the main workspace (except, there's no
  // svg.insertAfter method, so we need to insert before the thing following
  // the main workspace. Neil Fraser says: this is "less hacky than it looks".
  var flyoutGroup = Blockly.Backpack.flyout_.createDom();
  Blockly.svg.insertBefore(flyoutGroup, Blockly.mainWorkspace.svgGroup_.nextSibling);

  this.svgGroup_ = Blockly.createSvgElement('g',null, null);
  this.svgBody_ = Blockly.createSvgElement('image',
      {'width': this.WIDTH_, 'height': this.BODY_HEIGHT_, 'id': 'backpackIcon'},
      this.svgGroup_);
  this.svgBody_.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href',
      Blockly.pathToBlockly + this.BPACK_CLOSED_);
  return this.svgGroup_;
};

/**
 * Initialize the backpack.
 */
Blockly.Backpack.prototype.init = function() {
  this.position_();
  // If the document resizes, reposition the backpack.
  Blockly.bindEvent_(window, 'resize', this, this.position_);
  Blockly.Backpack.flyout_.init(Blockly.mainWorkspace,
                              Blockly.getMainWorkspaceMetrics_,
                              true /*withScrollbar*/);

  // load files for sound effect
  Blockly.loadAudio_(['media/backpack.mp3', 'media/backpack.ogg', 'media/backpack.wav'], 'backpack');

  if (this.getBackpack() == undefined)
    return;

  var bp_contents = JSON.parse(this.getBackpack());
  var len = bp_contents.length;

  if (len == 0)
    this.shrink();
  else
    this.grow();
};

/**
 * Dispose of this backpack.
 * Unlink from all DOM elements to prevent memory leaks.
 */
Blockly.Backpack.prototype.dispose = function() {
  if (this.svgGroup_) {
    goog.dom.removeNode(this.svgGroup_);
    this.svgGroup_ = null;
  }
  this.svgBody_ = null;
  this.getMetrics_ = null;
  goog.Timer.clear(this.openTask_);
};

/**
 *  Pastes the backpack contents to the current workspace.
 */
Blockly.Backpack.prototype.pasteBackpack = function() {
  if (this.getBackpack() == undefined)
    return;

  bp_contents = JSON.parse(this.getBackpack());
  var len = bp_contents.length;
  for (var i = 0; i < len; i++) {
    var xml = Blockly.Xml.textToDom(bp_contents[i]);
    var blk = xml.childNodes[0];
    var type = blk.getAttribute('type');
    var arr = [];
    this.checkValidBlockTypes(blk,arr);
    var ok = true;
    for (var j = 0; j < arr.length; j++) {
      var type = arr[j];
      if (! Blockly.Blocks[type]) {
        ok = false;
        break;
      }
    }
    if (ok)
      Blockly.mainWorkspace.paste(blk);
    else
      window.alert('Sorry. You cannot paste a block of type "' + type +
        '" because it doesn\'t exist in this workspace.');
  }
}

/**
 * Recursively traverses the tree starting from block returning
 * an array of child blocks.
 *
 * Pre-condition block has nodeName 'block' and some type.
 */
Blockly.Backpack.prototype.checkValidBlockTypes = function(block, arr) {
  if (block.nodeName=='block') {
    arr.push(block.getAttribute('type'));
  }
  var children = block.childNodes;
  for (var i=0; i < children.length; i++) {
    var child = children[i];
    this.checkValidBlockTypes(child,arr);
  }
}

/**
 *  Copy all blocks in the workspace to backpack
 *
 */
Blockly.Backpack.prototype.addAllToBackpack = function() {
  var allBlocks = Blockly.mainWorkspace.getAllBlocks();
  var topBlocks = Blockly.mainWorkspace.getTopBlocks(false);
  for (var x = 0; x < topBlocks.length; x++) {
    block = allBlocks[x];
    this.addToBackpack(block, false);
  }
  // We have to read back the backpack (getBackpack) and store it again
  // this time stating that it should be pushed up to the server
  this.setBackpack(this.getBackpack(), true); // A little klunky but gets the job done
}

/**
 *  The backpack is an array containing 0 or more
 *   blocks
 */
Blockly.Backpack.prototype.addToBackpack = function(block, store) {
  if (this.getBackpack() == undefined) {
    this.setBackpack(JSON.stringify([]), false);
  }

  // Copy is made of the expanded block.
  var isCollapsed = block.collapsed_;
  block.setCollapsed(false);
  var xmlBlock = Blockly.Xml.blockToDom_(block);
  Blockly.Xml.deleteNext(xmlBlock);
  // Encode start position in XML.
  var xy = block.getRelativeToSurfaceXY();
  xmlBlock.setAttribute('x', Blockly.RTL ? -xy.x : xy.x);
  xmlBlock.setAttribute('y', xy.y);
  block.setCollapsed(isCollapsed);

  // Add the block to the backpack
  var backpack = this.getBackpack();
  var bp_contents = JSON.parse(backpack);
  var len = bp_contents.length;
  var newBlock = "<xml>" + Blockly.Xml.domToText(xmlBlock) + "</xml>";
  bp_contents[len] = newBlock;
  this.setBackpack(JSON.stringify(bp_contents), store);
  this.grow();
  Blockly.playAudio('backpack');

  // update the flyout when it's visible
  if (Blockly.Backpack.flyout_.isVisible()) {
    this.isAdded = true;
    this.openBackpack();
    this.isAdded = false;
  }
}

/**
 * Move the backpack to the top right corner.
 * @private
 */
Blockly.Backpack.prototype.position_ = function() {
  var metrics = this.workspace_.getMetrics();
  if (!metrics) {
    // There are no metrics available (workspace is probably not visible).
    return;
  }
  if (Blockly.RTL) {
    this.left_ = this.MARGIN_SIDE_;
  } else {
    this.left_ = metrics.viewWidth + metrics.absoluteLeft -
        this.WIDTH_ - this.MARGIN_SIDE_;
  }
  this.top_ = metrics.viewHeight + metrics.absoluteTop -
     (metrics.viewHeight - this.MARGIN_TOP_);
  this.svgGroup_.setAttribute('transform',
      'translate(' + this.left_ + ',' + this.top_ + ')');
};

/**
 * On right click, open alert and show documentation
 */
Blockly.Backpack.prototype.openBackpackDoc = function(e) {
  var options = [];
  var backpackDoc = {enabled : true};
  backpackDoc.text = Blockly.Msg.SHOW_BACKPACK_DOCUMENTATION;
  backpackDoc.callback = function() {
    var dialog = new Blockly.Util.Dialog(Blockly.Msg.BACKPACK_DOC_TITLE,
                                         Blockly.Msg.BACKPACK_DOCUMENTATION,
                                         Blockly.Msg.REPL_OK, null, 0,
                                         function() {
                                           dialog.hide();
                                         });
  }
  options.push(backpackDoc);
  Blockly.ContextMenu.show(e, options);
};

/**
 * On left click, open backpack and view flyout
 */
Blockly.Backpack.prototype.openBackpack = function(){
  if (!this.isAdded && Blockly.Backpack.flyout_.isVisible()) {
      Blockly.Backpack.flyout_.hide();
  } else {
    var backpack = JSON.parse(this.getBackpack());
    //get backpack contents from java

    var len = backpack.length;
    var newBackpack = []
    for (var i = 0; i < len; i++) {
      var dom = Blockly.Xml.textToDom(backpack[i]).firstChild;
      newBackpack[i] = dom;
    }
    Blockly.Backpack.flyout_.show(newBackpack);
  }
};

/**
 * Obtains starting coordinates so the block can return to spot after copy
 * NOTE: This function does not appear to be invoked when you click on a
 *  block and drag it to the Backpack.  So these values of startX and startY
 *  are not set.
 * @param {!Event} e Mouse down event.
 */
Blockly.Backpack.prototype.onMouseDown = function(e){
  var xy = Blockly.getAbsoluteXY_(this.svgGroup_);
  this.startX = xy.x;
  this.startY = xy.y;
}

/**
 * When block is let go over the backpack, copy it and return to original position
 * @param {!Event} e Mouse up event
 * @param startX x coordinate of the mouseDown event
 * @param startY y coordinate of the mouseDown event
 */
Blockly.Backpack.prototype.onMouseUp = function(e, startX, startY){
  var xy = Blockly.getAbsoluteXY_(this.svgGroup_);
  var mouseX = e.clientX //xy.x;
  var mouseY = e.clientY //xy.y;
  // Note: startX and startY do not give the starting location of the block itself.
  //  They give the location of the mouse click which can be anywhere on the block.
  // So this code will not return the block to its original position.
  Blockly.selected.moveBy((startX - e.clientX), (startY - e.clientY));
  Blockly.mainWorkspace.render();
}

/**
 * Determines if the mouse (with a block) is currently over the backpack.
 * Opens/closes the lid and sets the isLarge flag.
 * @param {!Event} e Mouse move event.
 */
Blockly.Backpack.prototype.onMouseMove = function(e, startX, startY) {
  /*
  An alternative approach would be to use onMouseOver and onMouseOut events.
  However the selected block will be between the mouse and the backpack,
  thus these events won't fire.
  Another approach is to use HTML5's drag & drop API, but it's widely hated.
  Instead, we'll just have the block's drag_ function call us.
  */
  if (!this.svgGroup_) {
    return;
  }
  var xy = Blockly.getAbsoluteXY_(this.svgGroup_);
  var left = xy.x;
  var top = xy.y;

  // Convert the mouse coordinates into SVG coordinates.
  xy = Blockly.convertCoordinates(e.clientX, e.clientY, true);
  var mouseX = xy.x;
  var mouseY = xy.y;

  var over = (mouseX > left) &&
             (mouseX < left + this.WIDTH_) &&
             (mouseY > top) &&
             (mouseY < top + this.BODY_HEIGHT_);
  if (this.isOpen != over) {
     this.setOpen_(over);
  }
};

Blockly.Backpack.prototype.mouseIsOver = function(e) {
  xy = Blockly.convertCoordinates(e.clientX, e.clientY, true);
  var mouseX = xy.x;
  var mouseY = xy.y;
  var over = (mouseX > this.left_) &&
               (mouseX < this.left_ + this.WIDTH_) &&
               (mouseY > this.top_) &&
               (mouseY < this.top_ + this.BODY_HEIGHT_);
  this.isOver = over;
  return over;
};

/**
 * Hide the Backpack flyout
 */
Blockly.Backpack.hide = function() {
  Blockly.Backpack.flyout_.hide();
};


/**
 * Flip the lid open or shut.
 * @param {boolean} state True if open.
 * @private
 */
Blockly.Backpack.prototype.setOpen_ = function(state) {
  if (this.isOpen == state) {
    return;
  }
  goog.Timer.clear(this.openTask_);
  this.isOpen = state;
  this.animateBackpack_();
};

/**
 * Change the image of backpack to one with red outline
 */
Blockly.Backpack.prototype.animateBackpack_ = function() {
  var icon = document.getElementById('backpackIcon');
  if (this.isOpen){
    icon.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_EMPTY_);
  } else {
    if (this.isLarge) {
      icon.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_FULL_);
    } else {
      icon.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_CLOSED_);
    }
  }
}

/**
 * Flip the lid shut.
 * Called externally after a drag.
 */
Blockly.Backpack.prototype.close = function() {
  this.setOpen_(false);
}

/**
 * Scales the backpack to a large size to indicate it contains blocks.
 */
Blockly.Backpack.prototype.grow = function() {
  if (this.isLarge)
    return;
  var icon = document.getElementById('backpackIcon');
  icon.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_FULL_);
  var metrics = this.workspace_.getMetrics();
  this.svgBody_.setAttribute('transform','scale(1.2)');
  this.MARGIN_SIDE_ = this.MARGIN_SIDE_ / 1.2;
  this.BODY_HEIGHT_ = this.BODY_HEIGHT_ * 1.2;
  this.WIDTH_ = this.WIDTH_ * 1.2;
  this.position_();
  this.isLarge = true;
}

/**
 * Scales the backpack to a small size to indicate it is empty.
 */
Blockly.Backpack.prototype.shrink = function() {
  if (!this.isLarge)
    return;
  var icon = document.getElementById('backpackIcon');
  icon.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_CLOSED_);
  var metrics = this.workspace_.getMetrics();
  this.svgBody_.setAttribute('transform','scale(1)');
  this.BODY_HEIGHT_ = this.BODY_HEIGHT_ / 1.2;
  this.WIDTH_ = this.WIDTH_ / 1.2;
  this.MARGIN_SIDE_ = this.MARGIN_SIDE_ * 1.2;
  this.position_();
  this.isLarge = false;
}

/**
 * Empties the backpack and shrinks its image.
 */
Blockly.Backpack.prototype.clear = function() {
  if (Blockly.mainWorkspace.backpack.confirmClear()) {
    this.setBackpack(JSON.stringify([]), true);
    this.shrink();
  }
}

Blockly.Backpack.prototype.confirmClear = function() {
  return confirm(Blockly.Msg.BACKPACK_CONFIRM_EMPTY);
};

/**
 * Returns count of the number of entries in the backpack.
 */
Blockly.Backpack.prototype.count = function() {
  if (this.getBackpack() == null)
    return 0;
  var bp_contents = JSON.parse(this.getBackpack());
  return bp_contents.length;
}

Blockly.Backpack.prototype.getBackpack = function() {
  return window.parent.BlocklyPanel_getBackpack();
}

Blockly.Backpack.prototype.setBackpack = function(backpack, store) {
  window.parent.BlocklyPanel_setBackpack(backpack, store);
}

