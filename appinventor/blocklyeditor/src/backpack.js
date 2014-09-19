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

// These used to got in blocklyeditor/src/language/en/_messages.js but can't find it
Blockly.MSG_COPY_TO_BACKPACK = 'Add to Backpack';
Blockly.MSG_BACKPACK_GET = 'Paste Blocks from Backpack';
Blockly.MSG_BACKPACK_EMPTY = 'Empty the Backpack';

/**
 * Class for a backpack.
 * @param {!Function} getMetrics A function that returns workspace's metrics.
 * @constructor
 */
Blockly.Backpack = function(getMetrics) {
  this.getMetrics_ = getMetrics;
};

/**
 * URL of the small backpack image.
 * @type {string}
 * @private
 */
Blockly.Backpack.prototype.BPACK_SMALL_ = 'media/backpack-small.png';

/**
 * URL of the small backpack image.
 * @type {string}
 * @private
 */
Blockly.Backpack.prototype.BPACK_OVER_ = 'media/backpack-small-over.png';

/**
 * Width of the image.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.WIDTH_ = 37;

/**
 * Height of the image.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.BODY_HEIGHT_ = 35; 

/**
 * Distance between backpack and top edge of workspace.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.MARGIN_TOP_ = 10;   //65

/**
 * Distance between backpack and right edge of workspace.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.MARGIN_SIDE_ = 65;  //135

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
  
Blockly.Backpack.prototype.startX = 0;


 * Starting y coordinate for snapping back
 * @type {number}
 * @private
 
Blockly.Backpack.prototype.startY = 0;
*/

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
      Blockly.pathToBlockly + this.BPACK_SMALL_);
  return this.svgGroup_;
};

/**
 * Called from BlocklyPanel to restore the contents of the backpack
 *  when the workspace is initialized. Allows carrying blocks
 *  between workspaces
 *
 * @param Either null or "[]" indicate an empty backpack. All other
 *  states indicate backpack contains blocks.
 */
Blockly.Backpack.prototype.restore = function(backpack) {
  Blockly.backpack_ = backpack;
  if (!backpack || backpack == "[]")  // Initialize the icon
    this.shrink();
  else
    this.grow();
}

/**
 * Initialize the backpack.
 */
Blockly.Backpack.prototype.init = function() {
  this.position_();
  // If the document resizes, reposition the backpack.
  Blockly.bindEvent_(window, 'resize', this, this.position_);
  //Blockly.Backpack.flyout_ = new Blockly.Flyout();
  Blockly.Backpack.flyout_.init(Blockly.mainWorkspace,
                              Blockly.getMainWorkspaceMetrics,
                              true /*withScrollbar*/);  
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
Blockly.Backpack.prototype.pasteBackpack = function(backpack) {
  if (backpack == undefined)
    return;

  bp_contents = JSON.parse(backpack);
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
      if (! Blockly.Language[type]) {
        ok = false
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
 *  The backpack is an array containing 0 or more
 *   blocks 
 */
Blockly.Backpack.prototype.addToBackpack = function(block) {
  if (Blockly.backpack_ == undefined) {
    Blockly.backpack_ = JSON.stringify([]);
  }

  // Copy is made of the expanded block.
  var isCollapsed = block.collapsed;
  block.setCollapsed(false);
  var xmlBlock = Blockly.Xml.blockToDom_(block);
  Blockly.Xml.deleteNext(xmlBlock);
  // Encode start position in XML.
  var xy = block.getRelativeToSurfaceXY();
  xmlBlock.setAttribute('x', Blockly.RTL ? -xy.x : xy.x);
  xmlBlock.setAttribute('y', xy.y);
  block.setCollapsed(isCollapsed);

  // Add the block to the backpack
  var backpack = Blockly.backpack_;
  var bp_contents = JSON.parse(backpack)
  var len = bp_contents.length;
  bp_contents[len] = "<xml>" + Blockly.Xml.domToText(xmlBlock) + "</xml>";
  Blockly.backpack_ = JSON.stringify(bp_contents);
  this.grow();
}

/**
 * Move the backpack to the top right corner.
 * @private
 */
Blockly.Backpack.prototype.position_ = function() {
  var metrics = this.getMetrics_();
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
 * On left click, open backpack and view flyout
 */
Blockly.Backpack.prototype.openBackpack = function(){
  console.log(Blockly.Backpack.flyout_.isVisible());
  //if (Blockly.Backpack.flyout_.isVisible()) {
  //    Blockly.Backpack.flyout_.hide();
  //}
  var backpack = JSON.parse(Blockly.backpack_);

  var len = backpack.length;
  var newBackpack = []
  for (var i = 0; i < len; i++) {
    var dom = Blockly.Xml.textToDom(backpack[i]).firstChild;
    newBackpack[i] = dom;
    }

  Blockly.Backpack.flyout_.show(newBackpack);
};



/**
 * Obtains starting coordinates so the block can return to spot after copy
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
 */
Blockly.Backpack.prototype.onMouseUp = function(e, startX, startY){
  var xy = Blockly.getAbsoluteXY_(this.svgGroup_);
  //var xy = Blockly.convertCoordinates(e.clientX, e.clientY, true);
  var mouseX = e.clientX //xy.x;
  var mouseY = e.clientY //xy.y;
  console.log("X coord: " + mouseX + " Y coord: " + mouseY + 
    "\nStartx coord: " + startX + " Starty coord: " + startY +
    "\n dx: " + (startX - mouseX) + " dy: " + (startY - mouseY));
  Blockly.selected.moveBy((startX - e.clientX), (startY - e.clientY));

   
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
    icon.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_OVER_);  
  } else {
    icon.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_SMALL_);
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
  var metrics = this.getMetrics_();
  this.svgBody_.setAttribute('transform','scale(2)');
  //this.MARGIN_TOP_ = (this.BODY_HEIGHT_ - this.MARGIN_TOP_);
  console.log("grow:");
  console.log(this.MARGIN_TOP_);
  this.MARGIN_SIDE_ = this.MARGIN_SIDE_ - this.WIDTH_;
  this.BODY_HEIGHT_ = this.BODY_HEIGHT_ * 2;
  this.WIDTH_ = this.WIDTH_ * 2;
  this.isLarge = true;
}

/**
 * Scales the backpack to a small size to indicate it is empty.
 */
Blockly.Backpack.prototype.shrink = function() {
  if (!this.isLarge)
    return;
  var metrics = this.getMetrics_();
  this.svgBody_.setAttribute('transform','scale(1)');
  this.BODY_HEIGHT_ = this.BODY_HEIGHT_ / 2;
  this.WIDTH_ = this.WIDTH_ / 2;
  //this.MARGIN_TOP_ = (metrics.viewHeight) - (this.MARGIN_TOP_ - this.BODY_HEIGHT_);
  console.log("shrink");
  console.log(this.MARGIN_TOP_)
  this.MARGIN_SIDE_ = this.MARGIN_SIDE_ + this.WIDTH_;
  this.isLarge = false;
}

/**
 * Empties the backpack and shrinks its image.
 */
Blockly.Backpack.prototype.clear = function() {
  Blockly.backpack_ = JSON.stringify([]); 
  this.shrink();
}

/**
 * Returns count of the number of entries in the backpack.
 */
Blockly.Backpack.prototype.count = function() {
  if (Blockly.backpack_ == null)
    return 0;
  var bp_contents = JSON.parse(Blockly.backpack_);
  return bp_contents.length;  
}

