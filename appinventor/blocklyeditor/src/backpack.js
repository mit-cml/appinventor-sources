/**
 * Visual Blocks Editor
 *
 * Copyright © 2011 Google Inc.
 * Copyright © 2011-2016 Massachusetts Institute of Technology
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
 *  Backpack contents are stored in Blockly.Backpack.contents.
 *
 * @author fraser@google.com (Neil Fraser)
 * @author ram8647@gmail.com (Ralph Morelli)
 * @author vbrown@wellesley.edu (Tori Brown)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
'use strict';

goog.provide('AI.Blockly.Backpack');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Util');

/**
 * Class for a backpack.
 * @param {!Blockly.WorkspaceSvg} targetWorkspace The Workspace that
 * receives blocks from the backpack.
 * @param {(Blockly.Options|Object)=} [opt_options={}] Options for the
 * Backpack's flyout workspace.
 * @constructor
 */
Blockly.Backpack = function(targetWorkspace, opt_options) {
  if (opt_options instanceof Blockly.Options) {
    this.options = opt_options;
  } else {
    opt_options = opt_options || {};
    this.options = new Blockly.Options(opt_options);
  }
  this.workspace_ = targetWorkspace;
  this.flyout_ = new Blockly.BackpackFlyout(this.options);
  // NoAsync_: A flag for getContents(). If true, getContents will use the
  // already fetched backpack contents even when using a shared backpack
  // this is used by addAllToBackpack()
  this.NoAsync_ = false;
};

/**
 * URL of the small backpack image.
 * @type {string}
 * @private
 */
Blockly.Backpack.prototype.BPACK_CLOSED_ = 'assets/backpack-closed.png';

/**
 * URL of the small backpack image.
 * @type {string}
 * @private
 */
//Blockly.Backpack.prototype.BPACK_EMPTY_ = 'media/backpack-small-highlighted.png';
Blockly.Backpack.prototype.BPACK_EMPTY_ = 'assets/backpack-empty.png';

/**
 * URL of the full backpack image
 * @type {string}
 * @private
 */
Blockly.Backpack.prototype.BPACK_FULL_ = 'assets/backpack-full.png';

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
 * Task ID of opening/closing animation.
 * @type {number}
 * @private
 */
Blockly.Backpack.prototype.openTask_ = 0;

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
 * Backpack contents across projects/screens.
 * @type {string[]}
 * @public
 *
 */
Blockly.Backpack.contents = [];

/**
 * backPackId -- false if using non-shared backpack
 * set to the backPackId (from Ode.java) if shared backpack
 * in use. Note: we support quasi-simultaneous use of a
 * shared backpack.
 */

Blockly.Backpack.backPackId = false;

/**
 * Create the backpack SVG elements.
 * @return {!Element} The backpack's SVG group.
 */
Blockly.Backpack.prototype.createDom = function(opt_workspace) {
  var workspace = opt_workspace || Blockly.getMainWorkspace();
  // insert the flyout after the main workspace (except, there's no
  // svg.insertAfter method, so we need to insert before the thing following
  // the main workspace. Neil Fraser says: this is "less hacky than it looks".
  var flyoutGroup = this.flyout_.createDom('g');
  this.flyout_.svgBackground_.setAttribute('class', 'blocklybackpackFlyoutBackground');
  if (workspace.svgGroup_.nextSibling) {
    workspace.getParentSvg().insertBefore(flyoutGroup, workspace.svgGroup_.nextSibling);
  } else {
    workspace.getParentSvg().appendChild(flyoutGroup);
  }

  this.svgGroup_ = Blockly.utils.createSvgElement('g', {}, null);
  this.svgBody_ = Blockly.utils.createSvgElement('image',
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
  Blockly.bindEvent_(this.svgBody_, 'click', this, this.openBackpack);
  Blockly.bindEvent_(this.svgBody_, 'contextmenu', this, this.openBackpackDoc);
  this.flyout_.init(this.workspace_);

  // load files for sound effect
  Blockly.getMainWorkspace().loadAudio_(['assets/backpack.mp3', 'assets/backpack.ogg', 'assets/backpack.wav'], 'backpack');

  var p = this;
  this.getContents(function(contents) {
    if (!contents) {
      return;
    }
    if (contents.length === 0) {
      p.shrink();
    } else {
      p.grow();
    }
  });
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
  goog.Timer.clear(this.openTask_);
};

/**
 *  Pastes the backpack contents to the current workspace.
 */
Blockly.Backpack.prototype.pasteBackpack = function() {
  var p = this;
  this.getContents(function(bp_contents) {
    if (bp_contents === undefined || bp_contents.length == 0) {
      return;
    }
    var lastPastedBlock = null;
    try {
      Blockly.Events.setGroup(true);
      for (var i = 0; i < bp_contents.length; i++) {
        var xml = Blockly.Xml.textToDom(bp_contents[i]);
        var blk = xml.childNodes[0];
        var arr = [];
        p.checkValidBlockTypes(blk, arr);
        var ok = true;
        for (var j = 0; j < arr.length; j++) {
          var type = arr[j];
          if (!Blockly.Blocks[type] && !this.workspace_.getComponentDatabase().hasType(type)) {
            ok = false;
            break;
          }
        }
        if (ok) {
          var newBlock = p.workspace_.paste(blk);
          if (newBlock) {
            lastPastedBlock = newBlock;
          }
        }
        else
          window.alert('Sorry. You cannot paste a block of type "' + type +
          '" because it doesn\'t exist in this workspace.');
      }
    } finally {
      Blockly.Events.setGroup(false);
    }
    if (lastPastedBlock) {
      lastPastedBlock.select();
    }
  });
};

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
};

/**
 *  Copy all blocks in the workspace to backpack
 *
 */
Blockly.Backpack.prototype.addAllToBackpack = function() {
  var topBlocks = Blockly.mainWorkspace.getTopBlocks(false);
  var p = this;
  this.getContents(function(contents) {
    var saveAsync = p.NoAsync_;
    try {
      p.NoAsync_ = true;
      for (var x = 0; x < topBlocks.length; x++) {
        p.addToBackpack(topBlocks[x], false);
      }
    } finally {
      p.NoAsync_ = saveAsync;
    }
    p.setContents(Blockly.Backpack.contents, true);
  });
};

/**
 *  The backpack is an array containing 0 or more
 *   blocks
 */
Blockly.Backpack.prototype.addToBackpack = function(block, store) {
  // Copy is made of the expanded block.
  var isCollapsed = block.collapsed_;
  block.setCollapsed(false);
  var xmlBlock = Blockly.Xml.blockToDom(block);
  Blockly.Xml.deleteNext(xmlBlock);
  // Encode start position in XML.
  var xy = block.getRelativeToSurfaceXY();
  xmlBlock.setAttribute('x', Blockly.RTL ? -xy.x : xy.x);
  xmlBlock.setAttribute('y', xy.y);
  block.setCollapsed(isCollapsed);

  // Add the block to the backpack
  var p = this;
  this.getContents(function(bp_contents) {
    if (!bp_contents) {
      bp_contents = [];
    }
    bp_contents.push("<xml>" + Blockly.Xml.domToText(xmlBlock) + "</xml>");
    // We technically do not need to set the contents here since the contents are manipulated by
    // reference, but separating the setting from modifying allows us to use different, non-in-memory
    // storage in the future.
    p.setContents(bp_contents, store);
    p.grow();
    Blockly.getMainWorkspace().playAudio('backpack');

    // update the flyout when it's visible
    if (p.flyout_.isVisible()) {
      p.isAdded = true;
      p.openBackpack();
      p.isAdded = false;
    }
  });

};

Blockly.Backpack.prototype.hide = function() {
  this.flyout_.hide();
};

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
                                         Blockly.Msg.REPL_OK, false, null, 0,
                                         function() {
                                           dialog.hide();
                                         });
  };
  options.push(backpackDoc);
  Blockly.ContextMenu.show(e, options, this.workspace_.RTL);
  // Do not propagate to Blockly, nor show the browser context menu
  //e.stopPropagation();
  //e.preventDefault();
};

/**
 * On left click, open backpack and view flyout
 */
Blockly.Backpack.prototype.openBackpack = function() {
  if (!this.isAdded && this.flyout_.isVisible()) {
    this.flyout_.hide();
  } else {
    var p = this;
    this.getContents(function(backpack) {
      var len = backpack.length;
      var newBackpack = [];
      for (var i = 0; i < len; i++) {
        newBackpack[i] = Blockly.Xml.textToDom(backpack[i]).firstChild;
      }
      p.flyout_.show(newBackpack);
    });
  }
};

/**
 * When block is let go over the backpack, copy it and return to original position
 * @param {!Event} e Mouse up event
 * @param {!goog.math.Coordinate} start coordinate of the mouseDown event
 */
Blockly.Backpack.prototype.onMouseUp = function(e, start){
  var xy = Blockly.selected.getRelativeToSurfaceXY();
  var diffXY = goog.math.Coordinate.difference(start, xy);
  Blockly.selected.moveBy(diffXY.x, diffXY.y);
  Blockly.getMainWorkspace().render();
};

/**
 * Determines if the mouse (with a block) is currently over the backpack.
 * Opens/closes the lid and sets the isLarge flag.
 * @param {!Event} e Mouse move event.
 */
Blockly.Backpack.prototype.onMouseMove = function(e) {
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
  var over = this.mouseIsOver(e);
  if (this.isOpen != over) {
     this.setOpen_(over);
  }
};

Blockly.Backpack.prototype.mouseIsOver = function(e) {
  var xy = Blockly.convertCoordinates(Blockly.getMainWorkspace(), e.clientX, e.clientY, true);
  var mouseX = xy.x;
  var mouseY = xy.y;
  return (mouseX > this.left_) &&
         (mouseX < this.left_ + this.WIDTH_) &&
         (mouseY > this.top_) &&
         (mouseY < this.top_ + this.BODY_HEIGHT_);
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
};

/**
 * Flip the lid shut.
 * Called externally after a drag.
 */
Blockly.Backpack.prototype.close = function() {
  this.setOpen_(false);
};

/**
 * Scales the backpack to a large size to indicate it contains blocks.
 */
Blockly.Backpack.prototype.grow = function() {
  if (this.isLarge)
    return;
  var icon = document.getElementById('backpackIcon');
  icon.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_FULL_);
  this.svgBody_.setAttribute('transform','scale(1.2)');
  this.MARGIN_SIDE_ = this.MARGIN_SIDE_ / 1.2;
  this.BODY_HEIGHT_ = this.BODY_HEIGHT_ * 1.2;
  this.WIDTH_ = this.WIDTH_ * 1.2;
  this.position_();
  this.isLarge = true;
};

/**
 * Scales the backpack to a small size to indicate it is empty.
 */
Blockly.Backpack.prototype.shrink = function() {
  if (!this.isLarge)
    return;
  var icon = document.getElementById('backpackIcon');
  icon.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_CLOSED_);
  this.svgBody_.setAttribute('transform','scale(1)');
  this.BODY_HEIGHT_ = this.BODY_HEIGHT_ / 1.2;
  this.WIDTH_ = this.WIDTH_ / 1.2;
  this.MARGIN_SIDE_ = this.MARGIN_SIDE_ * 1.2;
  this.position_();
  this.isLarge = false;
};

/**
 * Empties the backpack and shrinks its image.
 */
Blockly.Backpack.prototype.clear = function() {
  if (this.confirmClear()) {
    this.setContents([], true);
    this.shrink();
  }
};

Blockly.Backpack.prototype.confirmClear = function() {
  return confirm(Blockly.Msg.BACKPACK_CONFIRM_EMPTY);
};

/**
 * Returns count of the number of entries in the backpack.
 */
Blockly.Backpack.prototype.count = function() {
  var bp_contents = Blockly.Backpack.contents;
  return bp_contents ? bp_contents.length : 0;
};

/**
 * Get the contents of the Backpack.
 * @returns {string[]} Backpack contents encoded as an array of XML strings.
 */
Blockly.Backpack.prototype.getContents = function(callback) {
  // If we are using a shared backpack, we need to fetch the contents
  // from the App Inventor server because another user may have modified
  // it. But if we are using our own personal backpack, we can use the
  // copy loaded when we logged in.
  //
  // Also, it we are running in a context where we know we recently
  // fetched the contents, then we do not have to do it again. This
  // happens in addAllToBackpack()
  var p = this;
  if (Blockly.Backpack.backPackId && !this.NoAsync_) {
    top.BlocklyPanel_getSharedBackpack(Blockly.Backpack.backPackId, function(content) {
      if (!content) {
        Blockly.Backpack.contents = [];
        p.shrink();
        callback([]);
      } else {
        var parsed = JSON.parse(content);
        Blockly.Backpack.contents = parsed;
        if (parsed.length > 0) {
          p.grow();
        } else {
          p.shrink();
        }
        callback(parsed);
      }
    });
  } else {
    callback(Blockly.Backpack.contents);
  }
};

/**
 * Set the contents of the Backpack.
 * @param {string[]} backpack Array of XML strings to set as the new Backpack contents.
 * @param {boolean=false} store If true, store the backpack as a user file.
 */
Blockly.Backpack.prototype.setContents = function(backpack, store) {
  Blockly.Backpack.contents = backpack;
  if (store) {
    if (Blockly.Backpack.backPackId) {
      top.BlocklyPanel_storeSharedBackpack(Blockly.Backpack.backPackId,
                                           JSON.stringify(backpack));
    } else {
      top.BlocklyPanel_storeBackpack(JSON.stringify(backpack));
    }
  }
};

