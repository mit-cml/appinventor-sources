/**
 * Visual Blocks Editor
 *
 * Copyright © 2011 Google Inc.
 * Copyright © 2011-2018 Massachusetts Institute of Technology
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
 *  Backpack contents are stored in AI.Blockly.Backpack.contents.
 *
 * @author fraser@google.com (Neil Fraser)
 * @author ram8647@gmail.com (Ralph Morelli)
 * @author vbrown@wellesley.edu (Tori Brown)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
'use strict';

goog.provide('AI.Blockly.Backpack');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.BackpackFlyout');
goog.require('AI.Blockly.Util');
goog.require('goog.Timer');

/**
 * Class for a backpack.
 */
AI.Blockly.Backpack = class extends Blockly.DragTarget {
  /**
   * URL of the small backpack image.
   * @type {string}
   * @private
   */
  BPACK_CLOSED_ = 'static/media/backpack-closed.png';

  /**
   * URL of the small backpack image.
   * @type {string}
   * @private
   */
  BPACK_EMPTY_ = 'static/media/backpack-empty.png';

  /**
   * URL of the full backpack image
   * @type {string}
   * @private
   */
  BPACK_FULL_ = 'static/media/backpack-full.png';

  /**
   * Width of the image.
   * @type {number}
   * @private
   */
  WIDTH_ = 80;

  /**
   * Height of the image.
   * @type {number}
   * @private
   */
  HEIGHT_ = 75;

  /**
   * Distance between backpack and top edge of workspace.
   * @type {number}
   * @private
   */
  MARGIN_TOP_ = 10;

  /**
   * Distance between backpack and right edge of workspace.
   * @type {number}
   * @private
   */
  MARGIN_SIDE_ = 20;

  /**
   * Current small/large state of the backpack.
   * @type {boolean}
   */
  isLarge = false;

  /**
   * Current state whether a block is added to backpack.
   * @type {boolean}
   */
  isAdded = false;

  /**
   * The SVG group containing the backpack.
   * @type {Element}
   * @private
   */
  svgGroup_ = null;

  /**
   * The SVG image element of the backpack body.
   * @type {Element}
   * @private
   */
  svgBody_ = null;

  /**
   * Task ID of opening/closing animation.
   * @type {number}
   * @private
   */
  openTask_ = 0;

  /**
   * Left coordinate of the backpack.
   * @type {number}
   * @private
   */
  left_ = 0;

  /**
   * Top coordinate of the backpack.
   * @type {number}
   * @private
   */
  top_ = 0;

  MARGIN_HORIZONTAL_ = 20;
  MARGIN_VERTICAL_ = 20;

  id = 'aibackpack';

  /**
   *
   * @param {!Blockly.WorkspaceSvg} targetWorkspace The Workspace that
   *     receives blocks from the backpack.
   * @param {(Blockly.Options|Object)=} [opt_options={}] Options for the
   *     Backpack's flyout workspace.
   */
  constructor(targetWorkspace, opt_options) {
    super();
    this.id = 'backpack';
    if (opt_options instanceof Blockly.Options) {
      this.options = opt_options;
    } else {
      opt_options = opt_options || {};
      this.options = new Blockly.Options(opt_options);
      // Parsing loses this option so we have to reassign.
      this.options.disabledPatternId = opt_options.disabledPatternId;
    }
    /**
     * The workspace that the backpack belongs to.
     * @private
     */
    this.workspace_ = targetWorkspace;
    this.flyout_ = new AI.Blockly.BackpackFlyout(this.options);
    // NoAsync_: A flag for getContents(). If true, getContents will use the
    // already fetched backpack contents even when using a shared backpack
    // this is used by addAllToBackpack()
    this.noAsync_ = false;
    this.registerContextMenuItems();
  }

  init() {
    if (this.initialized_) {
      console.trace('Extra call to Backpack.init()');
      return;
    }
    this.initialized_ = true;
    this.workspace_.getComponentManager().addComponent({
      component: this,
      weight: 2,
      capabilities: [
          Blockly.ComponentManager.Capability.AUTOHIDEABLE,
          Blockly.ComponentManager.Capability.DRAG_TARGET,
          Blockly.ComponentManager.Capability.POSITIONABLE
      ]
    });
    this.createDom(this.workspace_);
    Blockly.browserEvents.bind(this.svgGroup_, 'mousedown', this, this.openBackpack);
    Blockly.browserEvents.bind(this.svgGroup_, 'contextmenu', this, this.openBackpackMenu);
    this.initialized_ = true;
    this.workspace_.resize();
  }

  getFlyout() {
    return this.flyout_;
  }

  registerContextMenuItems() {
    if (Blockly.ContextMenuRegistry.registry.getItem('remove_from_backpack')) {
      return;
    }
    const removeFromBackpack = {
      displayText: Blockly.Msg['REMOVE_FROM_BACKPACK'],
      preconditionFn: function(scope) {
        const ws = scope.block.workspace;
        if (ws.isFlyout && ws.targetWorkspace) {
          const backpack = /** @type {AI.Blockly.Backpack} */
              (ws.targetWorkspace.getComponentManager().getComponent('backpack'));
          if (backpack && backpack.getFlyout().getWorkspace().id === ws.id) {
            return 'enabled';
          }
        }
        return 'hidden';
      },
      callback: function(scope) {
        const backpack = /** @type {AI.Blockly.Backpack} */
            (scope.block.workspace.targetWorkspace.getComponentManager()
                .getComponent('backpack'));
        backpack.removeFromBackpack([scope.block.id]);
      },
      scopeType: Blockly.ContextMenuRegistry.ScopeType.BLOCK,
      id: 'remove_from_backpack',
      weight: 6
    }
    Blockly.ContextMenuRegistry.registry.register(removeFromBackpack);
  }

  getClientRect() {
    if (!this.svgGroup_) {
      return null;
    }

    const clientRect = this.svgGroup_.getBoundingClientRect();
    const top = clientRect.top + this.MARGIN_TOP_;
    const bottom = clientRect.bottom + this.MARGIN_TOP_;
    const left = clientRect.left - this.MARGIN_SIDE_;
    const right = clientRect.right - this.MARGIN_SIDE_;
    return new Blockly.utils.Rect(top, bottom, left, right);
  }

  getBoundingRectangle() {
    return new Blockly.utils.Rect(
        this.top_, this.top_ + this.HEIGHT_,
        this.left_, this.left_ + this.WIDTH_);
  }

  position(metrics, savedPositions) {
    if (!this.initialized_) {
      return;
    }
    const hasVerticalScrollbars = this.workspace_.scrollbar &&
        this.workspace_.scrollbar.canScrollHorizontally();
    const hasHorizontalScrollbars = this.workspace_.scrollbar &&
        this.workspace_.scrollbar.canScrollVertically();

    if (metrics.toolboxMetrics.position === Blockly.TOOLBOX_AT_LEFT ||
        (this.workspace_.horizontalLayout && !this.workspace_.RTL)) {
      // Right corner placement.
      this.left_ = metrics.absoluteMetrics.left + metrics.viewMetrics.width -
          this.WIDTH_ - this.MARGIN_HORIZONTAL_;
      if (hasVerticalScrollbars && !this.workspace_.RTL) {
        this.left_ -= Blockly.Scrollbar.scrollbarThickness;
      }
    } else {
      // Left corner placement.
      this.left_ = this.MARGIN_HORIZONTAL_;
      if (hasVerticalScrollbars && this.workspace_.RTL) {
        this.left_ += Blockly.Scrollbar.scrollbarThickness;
      }
    }

    const startAtBottom =
        metrics.toolboxMetrics.position === Blockly.TOOLBOX_AT_BOTTOM;
    if (startAtBottom) {
      // Bottom corner placement
      this.top_ = metrics.absoluteMetrics.top + metrics.viewMetrics.height -
          this.HEIGHT_ - this.MARGIN_VERTICAL_;
      if (hasHorizontalScrollbars) {
        // The horizontal scrollbars are always positioned on the bottom.
        this.top_ -= Blockly.Scrollbar.scrollbarThickness;
      }
    } else {
      // Upper corner placement
      this.top_ = metrics.absoluteMetrics.top + this.MARGIN_VERTICAL_;
    }

    // Check for collision and bump if needed.
    let boundingRect = this.getBoundingRectangle();
    const positionRect = Blockly.uiPosition.bumpPositionRect(
        boundingRect, this.MARGIN_VERTICAL_, Blockly.uiPosition.bumpDirection.DOWN, savedPositions);

    this.top_ = positionRect.top;
    this.left_ = positionRect.left;

    this.svgGroup_.setAttribute('transform',
        'translate(' + this.left_ + ',' + this.top_ + ')');
  }

  onDragEnter(e) {
    if (e instanceof Blockly.BlockSvg) {
      // switch to open backpack icon
      this.setOpen_(true);
    }
  }

  onDragExit() {
    // reset backpack state
    this.setOpen_(false);
  }

  onDrop(dragElement) {
    try {
      if (dragElement instanceof Blockly.BlockSvg) {
        this.addToBackpack(/** @type {!Blockly.BlockSvg} */ (dragElement), true);
      }
    } finally {
      this.setOpen_(false);
    }
  }

  shouldPreventMove(dragElement) {
    console.trace('Drag element is', dragElement);
    return dragElement instanceof Blockly.BlockSvg;
  }

  /**
   * Create the backpack SVG elements.
   * @param {Blockly.WorkspaceSvg=} opt_workspace The workspace to create the backpack in.
   * @return {!Element} The backpack's SVG group.
   */
  createDom(opt_workspace) {
    let workspace = opt_workspace || Blockly.common.getMainWorkspace();
    // insert the flyout after the main workspace (except, there's no
    // svg.insertAfter method, so we need to insert before the thing following
    // the main workspace. Neil Fraser says: this is "less hacky than it looks".
    let flyoutGroup = this.flyout_.createDom(Blockly.utils.Svg.SVG);
    this.flyout_.svgBackground_.setAttribute('class', 'blocklybackpackFlyoutBackground');
    workspace.getParentSvg().parentNode.appendChild(flyoutGroup);
    this.flyout_.init(workspace);

    this.svgGroup_ = Blockly.utils.dom.createSvgElement('g', {}, null);
    this.svgBody_ = Blockly.utils.dom.createSvgElement('image',
        {'width': this.WIDTH_, 'height': this.HEIGHT_},
        this.svgGroup_);
    this.svgBody_.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href',
        Blockly.pathToBlockly + this.BPACK_CLOSED_);

    this.svgBody_.setAttribute('class', 'blocklybackpackImage');

    Blockly.utils.dom.insertAfter(
        this.svgGroup_,
        this.workspace_.getBubbleCanvas(),
    );
  }

  autoHide() {
    this.flyout_.hide();
  }

  dispose() {
    if (this.svgGroup_) {
      this.svgGroup_.remove()
      this.svgGroup_ = null;
    }
    this.flyout_.dispose();
    this.flyout_ = null;
    clearTimeout(this.openTask_);
  }

  /**
   *  Pastes the backpack contents to the current workspace.
   */
  pasteBackpack() {
    this.getContents()
        .then((contents) => {
          if (contents === undefined || contents.length === 0) {
            return;
          }
          let lastPastedBlock = null;
          try {
            Blockly.Events.setGroup(true);
            for (let i = 0; i < contents.length; i++) {
              const xml = Blockly.utils.xml.textToDom(contents[i]);
              const blk = xml.childNodes[0];
              const arr = [];
              this.checkValidBlockTypes(blk, arr);
              let ok = true;
              for (let j = 0; j < arr.length; j++) {
                const type = arr[j];
                if (!Blockly.Blocks[type] && !this.workspace_.getComponentDatabase().hasType(type)) {
                  ok = false;
                  break;
                }
              }
              if (ok) {
                const newBlock = this.workspace_.paste(blk);
                if (newBlock) {
                  lastPastedBlock = newBlock;
                }
              } else {
                window.alert('Sorry. You cannot paste a block of type "' + type +
                    '" because it doesn\'t exist in this workspace.');
              }
            }
          } finally {
            Blockly.Events.setGroup(false);
          }
          if (lastPastedBlock) {
            lastPastedBlock.select();
          }
        });
  }

  /**
   * Recursively traverses the tree starting from block returning
   * an array of child blocks.
   *
   * Pre-condition block has nodeName 'block' and some type.
   */
  checkValidBlockTypes(block, arr) {
    if (block.nodeName === 'block') {
      arr.push(block.getAttribute('type'));
    }
    const children = block.childNodes;
    for (let i=0; i < children.length; i++) {
      const child = children[i];
      this.checkValidBlockTypes(child, arr);
    }
  }

  addBlock(block) {
    this.addToBackpack(block, false);
    if (!this.pendingSave_) {
      this.pendingSave_ = true;
      setTimeout(() => {
        this.pendingSave_ = false;
        this.setContents(AI.Blockly.Backpack.contents, true);
      }, 10);
    }
  }

  /**
   * Add a block to the backpack.
   *
   * @param {Blockly.BlockSvg} block The block to add to the backpack.
   * @param {boolean} store If true, store the backpack to the server.
   */
  addToBackpack(block, store) {
    // Copy is made of the expanded block.
    const isCollapsed = block.isCollapsed();
    block.setCollapsed(false);
    const xmlBlock = Blockly.Xml.blockToDom(block);
    Blockly.Xml.deleteNext(xmlBlock);
    // Encode start position in XML.
    const xy = block.getRelativeToSurfaceXY();
    xmlBlock.setAttribute('x', this.workspace_.RTL ? -xy.x : xy.x);
    xmlBlock.setAttribute('y', xy.y);
    block.setCollapsed(isCollapsed);

    // Add the block to the backpack
    this.getContents()
        .then((contents) => {
          if (!contents) {
            contents = [];
          }
          contents.push("<xml>" + Blockly.Xml.domToText(xmlBlock) + "</xml>");
          this.setContents(contents, store);
          this.grow();
          Blockly.common.getMainWorkspace().getAudioManager().play('backpack');
          if (this.flyout_.isVisible()) {
            this.isAdded = true;
            this.openBackpack();
            this.isAdded = false;
          }
        });
  }

  /**
   *  Copy all blocks in the workspace to backpack
   */
  addAllToBackpack() {
    const topBlocks = this.workspace_.getTopBlocks(false);
    this.getContents().then(() => {
      let saveAsync = this.noAsync_;
      try {
        this.noAsync_ = true;
        for (let x = 0; x < topBlocks.length; x++) {
          this.addToBackpack(topBlocks[x], false);
        }
      } finally {
        this.noAsync_ = saveAsync;
      }
      this.setContents(AI.Blockly.Backpack.contents, true);
    });
  }

  /**
   * Remove the top-level blocks with the given IDs from the backpack.
   * @param {!Array.<string>} ids The block IDs to be removed
   */
  removeFromBackpack(ids) {
    this.getContents()
        .then(contents => {
          if (contents && contents.length) {
            let blockInBackPack = this.flyout_.workspace_.getTopBlocks(true).map(function(elt) {
              return elt.id;
            });
            let index = blockInBackPack.indexOf(ids[0]);
            if (index >= 0) {
              contents.splice(index, 1);
            }
            this.setContents(contents, true);
            if (contents.length === 0) {
              this.shrink();
            }
            if (this.flyout_.isVisible()) {
              this.isAdded = true;
              this.openBackpack();
              this.isAdded = false;
            }
          }
        });
  }

  /**
   * Get the contents of the Backpack.
   * @returns {Promise<string[]>} Backpack contents encoded as an array of XML strings.
   */
  getContents() {
    if (AI.Blockly.Backpack.backPackId && !this.noAsync_) {
      return new Promise((resolve, reject) => {
        top.BlocklyPanel_getSharedBackpack(AI.Blockly.Backpack.backPackId, (content) => {
          if (!content) {
            AI.Blockly.Backpack.contents = [];
          } else {
            AI.Blockly.Backpack.contents = JSON.parse(content);
          }
          this.resize();
          resolve(AI.Blockly.Backpack.contents);
        });
      });
    } else {
      return Promise.resolve(AI.Blockly.Backpack.contents);
    }
  }

  /**
   * Set the contents of the Backpack.
   * @param {string[]} contents Array of XML strings to set as the new Backpack contents.
   * @param {boolean} store If true, store the backpack to the server.
   */
  setContents(contents, store) {
    AI.Blockly.Backpack.contents = contents;
    if (store) {
      if (AI.Blockly.Backpack.backPackId) {
        top.BlocklyPanel_storeSharedBackpack(AI.Blockly.Backpack.backPackId,
            JSON.stringify(contents));
      } else {
        top.BlocklyPanel_storeBackpack(JSON.stringify(contents));
      }
    }
  }

  /**
   * Resize the backpack icon based on whether the backpack has contents or not.
   */
  resize() {
    if (AI.Blockly.Backpack.contents.length > 0) {
      this.grow();
    } else {
      this.shrink();
    }
  }

  hide() {
    this.flyout_.hide();
  }

  /**
   * Flip the lid shut.
   * Called externally after a drag.
   */
  close() {
    this.setOpen_(false);
  }

  /**
   * Empties the backpack and shrinks its image.
   */
  clear() {
    if (this.confirmClear()) {
      this.setContents([], true);
      this.shrink();
    }
  }

  confirmClear() {
    return confirm(Blockly.Msg.BACKPACK_CONFIRM_EMPTY);
  }

  /**
   * Returns count of the number of entries in the backpack.
   */
  getCount() {
    return AI.Blockly.Backpack.contents ?
        AI.Blockly.Backpack.contents.length : 0;
  }

  /**
   * Scales the backpack to a large size to indicate it contains blocks.
   */
  grow() {
    if (this.isLarge) {
      return;
    }
    this.svgBody_.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_FULL_);
    this.svgBody_.setAttribute('transform','scale(1.2)');
    this.MARGIN_SIDE_ = this.MARGIN_SIDE_ / 1.2;
    this.HEIGHT_ = this.HEIGHT_ * 1.2;
    this.WIDTH_ = this.WIDTH_ * 1.2;
    this.isLarge = true;
  }

  /**
   * Scales the backpack to a small size to indicate it is empty.
   */
  shrink() {
    if (!this.isLarge) {
      return;
    }
    this.svgBody_.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', Blockly.pathToBlockly + this.BPACK_CLOSED_);
    this.svgBody_.setAttribute('transform','scale(1)');
    this.HEIGHT_ = this.HEIGHT_ / 1.2;
    this.WIDTH_ = this.WIDTH_ / 1.2;
    this.MARGIN_SIDE_ = this.MARGIN_SIDE_ * 1.2;
    this.isLarge = false;
  }

  /**
   * Change the image of backpack to one with red outline
   * @private
   */
  animateBackpack_() {
    let icon = this.svgBody_;
    if (this.isOpen) {
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
   * Flip the lid open or shut.
   * @param {boolean} state True if open.
   * @private
   */
  setOpen_(state) {
    if (this.isOpen == state) {
      return;
    }
    goog.Timer.clear(this.openTask_);
    this.isOpen = state;
    this.animateBackpack_();
  }

  containsBlock(block) {
    return false;
  }
};

/**
 * Backpack contents across projects/screens.
 * @type {string[]}
 * @public
 *
 */
AI.Blockly.Backpack.contents = [];

/**
 * backPackId -- false if using non-shared backpack
 * set to the backPackId (from Ode.java) if shared backpack
 * in use. Note: we support quasi-simultaneous use of a
 * shared backpack.
 */

AI.Blockly.Backpack.backPackId = false;

/**
 * On right click, open alert and show documentation and backpackClear
 */
AI.Blockly.Backpack.prototype.openBackpackMenu = function(e) {
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

  // Clear backpack.
  var backpackClear = {enabled: true};
  backpackClear.text = Blockly.Msg.BACKPACK_EMPTY;
  backpackClear.callback = function() {
    if (Blockly.common.getMainWorkspace().hasBackpack()) {
      Blockly.common.getMainWorkspace().getBackpack().clear();
    }
  };
  options.push(backpackClear);

  Blockly.ContextMenu.show(e, options, this.workspace_.RTL);
  // Do not propagate to Blockly, nor show the browser context menu
  //e.stopPropagation();
  //e.preventDefault();
};

/**
 * On left click, open backpack and view flyout
 *
 * @param {?MouseEvent=} e Click event if the backpack is being opened in
 * response to a user action.
 */
AI.Blockly.Backpack.prototype.openBackpack = function(e) {
  if (e) {
    e.stopPropagation();
    if (e.button === 2) {
      this.flyout_.hide();
      this.openBackpackMenu(e);
      return;
    }
  }
  if (!this.isAdded && this.flyout_.isVisible()) {
    this.flyout_.hide();
  } else {
    this.getContents().then((contents) => {
      const len = contents.length;
      const newBackpack = [];
      for (let i = 0; i < len; i++) {
        newBackpack[i] = Blockly.Versioning.upgradeComponentMethods(Blockly.utils.xml.textToDom(contents[i]).firstChild);
      }
      Blockly.hideChaff();
      this.flyout_.show(newBackpack);
    });
  }
};

/**
 * When block is let go over the backpack, copy it and return to original position
 * @param {!Event} e Mouse up event
 * @param {!goog.math.Coordinate} start coordinate of the mouseDown event
 */
AI.Blockly.Backpack.prototype.onMouseUp = function(e, start){
  var xy = Blockly.common.getSelected().getRelativeToSurfaceXY();
  var diffXY = goog.math.Coordinate.difference(start, xy);
  Blockly.common.getSelected().moveBy(diffXY.x, diffXY.y);
  Blockly.common.getMainWorkspace().render();
};

/**
 * Determines if the mouse (with a block) is currently over the backpack.
 * Opens/closes the lid and sets the isLarge flag.
 * @param {!Event} e Mouse move event.
 */
AI.Blockly.Backpack.prototype.onMouseMove = function(e) {
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

AI.Blockly.Backpack.prototype.mouseIsOver = function(e) {
  var xy = Blockly.convertCoordinates(Blockly.common.getMainWorkspace(), e.clientX, e.clientY, true);
  var mouseX = xy.x;
  var mouseY = xy.y;
  return (mouseX > this.left_) &&
         (mouseX < this.left_ + this.WIDTH_) &&
         (mouseY > this.top_) &&
         (mouseY < this.top_ + this.HEIGHT_);
};
