/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
 * http://code.google.com/p/blockly/
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
 * @fileoverview The class representing one block.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for one block.
 * @param {Element} workspace The workspace in which to render the block.
 * @param {?string} prototypeName Name of the language object containing
 *     type-specific functions for this block.
 * @constructor
 */
Blockly.Block = function(workspace, prototypeName) {
  this.id = Blockly.uniqueId();
  this.titleRow = [];
  this.outputConnection = null;
  this.nextConnection = null;
  this.previousConnection = null;
  this.inputList = [];
  this.inputsInline = false;
  this.rendered = false;
  this.collapsed = false;
  this.disabled = false;
  this.editable = workspace.editable;
  this.tooltip = '';
  this.contextMenu = true;

  this.parentBlock_ = null;
  this.childBlocks_ = [];

  this.isInFlyout = false;
  this.workspace = workspace;

  workspace.addTopBlock(this);

  // Copy the type-specific functions and data from the prototype.
  if (prototypeName) {
    this.type = prototypeName;
    var prototype = Blockly.Language[prototypeName];
    if (!prototype) {
      throw 'Error: "' + prototypeName + '" is an unknown language block.';
    }
    for (var name in prototype) {
      this[name] = prototype[name];
    }
  }
  // Call an initialization function, if it exists.
  if (typeof this.init == 'function') {
    this.init();
  }
  // Bind an onchange function, if it exists.
  if (this.editable && typeof this.onchange == 'function') {
    Blockly.bindEvent_(workspace.getCanvas(), 'blocklyWorkspaceChange', this,
                       this.onchange);
  }
};

/**
 * Pointer to SVG representation of the block.
 * @type {Blockly.BlockSvg}
 * @private
 */
Blockly.Block.prototype.svg_ = null;

/**
 * Block's mutator icon (if any).
 * @type {Blockly.Mutator}
 */
Blockly.Block.prototype.mutator = null;

/**
 * Block's comment icon (if any).
 * @type {Blockly.Comment}
 */
Blockly.Block.prototype.comment = null;

/**
 * Block's warning icon (if any).
 * @type {Blockly.Warning}
 */
Blockly.Block.prototype.warning = null;

/**
 * Create and initialize the SVG representation of the block.
 */
Blockly.Block.prototype.initSvg = function() {
  this.svg_ = new Blockly.BlockSvg(this);
  this.svg_.init();
  Blockly.bindEvent_(this.svg_.getRootNode(), 'mousedown', this,
                     this.onMouseDown_);
  this.workspace.getCanvas().appendChild(this.svg_.getRootNode());
};

/**
 * Return the root node of the SVG or null if none exists.
 * @return {Node} The root SVG node (probably a group).
 */
Blockly.Block.prototype.getSvgRoot = function() {
  return this.svg_ && this.svg_.getRootNode();
};

/**
 * Is the mouse dragging a block?
 * 0 - No drag operation.
 * 1 - Still inside the sticky DRAG_RADIUS.
 * 2 - Freely draggable.
 * @private
 */
Blockly.Block.dragMode_ = 0;

/**
 * Wrapper function called when a mouseUp occurs during a drag operation.
 * @type {Function}
 * @private
 */
Blockly.Block.onMouseUpWrapper_ = null;

/**
 * Wrapper function called when a mouseMove occurs during a drag operation.
 * @type {Function}
 * @private
 */
Blockly.Block.onMouseMoveWrapper_ = null;

/**
 * Stop binding to the global mouseup and mousemove events.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Block.terminateDrag_ = function(e) {
  if (Blockly.Block.onMouseUpWrapper_) {
    Blockly.unbindEvent_(Blockly.Block.onMouseUpWrapper_);
    Blockly.Block.onMouseUpWrapper_ = null;
  }
  if (Blockly.Block.onMouseMoveWrapper_) {
    Blockly.unbindEvent_(Blockly.Block.onMouseMoveWrapper_);
    Blockly.Block.onMouseMoveWrapper_ = null;
  }
  if (Blockly.Block.dragMode_ == 2) {
    // Terminate a drag operation.
    if (Blockly.selected) {
      var selected = Blockly.selected;
      // Update the connection locations.
      var xy = selected.getRelativeToSurfaceXY();
      var dx = xy.x - selected.startDragX;
      var dy = xy.y - selected.startDragY;
      selected.moveConnections_(dx, dy);
      delete selected.draggedBubbles_;
      selected.setDragging_(false);
      selected.render();
      window.setTimeout(function() {selected.bumpNeighbours_();},
                        Blockly.BUMP_DELAY);
      // Fire an event to allow scrollbars to resize.
      Blockly.fireUiEvent(window, 'resize');
      selected.workspace.fireChangeEvent();
    }
  }
  Blockly.Block.dragMode_ = 0;
};

/**
 * Select this block.  Highlight it visually.
 */
Blockly.Block.prototype.select = function() {
  if (Blockly.selected) {
    // Unselect any previously selected block.
    Blockly.selected.unselect();
  }
  Blockly.selected = this;
  this.svg_.addSelect();
  Blockly.fireUiEvent(this.workspace.getCanvas(), 'blocklySelectChange');
};

/**
 * Unselect this block.  Remove its highlighting.
 */
Blockly.Block.prototype.unselect = function() {
  Blockly.selected = null;
  this.svg_.removeSelect();
  Blockly.fireUiEvent(this.workspace.getCanvas(), 'blocklySelectChange');
};

/**
 * Destroy this block.
 * @param {boolean} gentle If gentle, then try to heal any gap by connecting
 *     the next statement with the previous statement.  Otherwise, destroy all
 *     children of this block.
 * @param {boolean} animate If true, show a destroy animation and sound.
 */
Blockly.Block.prototype.destroy = function(gentle, animate) {
  if (this.outputConnection) {
    // Detach this block from the parent's tree.
    this.setParent(null);
  } else {
    var previousTarget = null;
    if (this.previousConnection && this.previousConnection.targetConnection) {
      // Remember the connection that any next statements need to connect to.
      previousTarget = this.previousConnection.targetConnection;
      // Detach this block from the parent's tree.
      this.setParent(null);
    }
    if (gentle && this.nextConnection && this.nextConnection.targetConnection) {
      // Disconnect the next statement.
      var nextTarget = this.nextConnection.targetConnection;
      var nextBlock = this.nextConnection.targetBlock();
      this.nextConnection.disconnect();
      nextBlock.setParent(null);

      if (previousTarget) {
        // Attach the next statement to the previous statement.
        previousTarget.connect(nextTarget);
      }
    }
  }

  if (animate && this.svg_) {
    this.svg_.destroyUiEffect();
  }

  //This block is now at the top of the workspace.
  // Remove this block from the workspace's list of top-most blocks.
  this.workspace.removeTopBlock(this);
  this.workspace = null;

  // Just deleting this block from the DOM would result in a memory leak as
  // well as corruption of the connection database.  Therefore we must
  // methodically step through the blocks and carefully disassemble them.

  // Switch off rerendering.
  this.rendered = false;

  if (Blockly.selected == this) {
    Blockly.selected = null;
    // If there's a drag in-progress, unlink the mouse events.
    Blockly.Block.terminateDrag_();
  }

  // First, destroy all my children.
  for (var x = this.childBlocks_.length - 1; x >= 0; x--) {
    this.childBlocks_[x].destroy(false);
  }
  // Then destroy myself.
  for (var x = 0; x < this.titleRow.length; x++) {
    this.titleRow[x].destroy();
  }
  if (this.mutator) {
    this.mutator.destroy();
  }
  if (this.comment) {
    this.comment.destroy();
  }
  if (this.warning) {
    this.warning.destroy();
  }
  // Destroy all inputs and their labels.
  for (var x = 0, input; input = this.inputList[x]; x++) {
    input.destroy();
  }
  this.inputList = [];
  // Destroy any remaining connections (next/previous/output).
  var connections = this.getConnections_(true);
  for (var x = 0; x < connections.length; x++) {
    var connection = connections[x];
    if (connection.targetConnection) {
      connection.disconnect();
    }
    connections[x].destroy();
  }
  // Destroy the SVG and break circular references.
  if (this.svg_) {
    this.svg_.destroy();
    this.svg_ = null;
  }
};

/**
 * Return the coordinates of the top-left corner of this block relative to the
 * drawing surface's origin (0,0).
 * @return {!Object} Object with .x and .y properties.
 */
Blockly.Block.prototype.getRelativeToSurfaceXY = function() {
  var x = 0;
  var y = 0;
  if (this.svg_) {
    var element = this.svg_.getRootNode();
    do {
      // Loop through this block and every parent.
      var xy = Blockly.getRelativeXY_(element);
      x += xy.x;
      y += xy.y;
      element = element.parentNode;
    } while (element && element != this.workspace.getCanvas());
  }
  return {x: x, y: y};
};

/**
 * Move a block by a relative offset.
 * @param {number} dx Horizontal offset.
 * @param {number} dy Vertical offset.
 */
Blockly.Block.prototype.moveBy = function(dx, dy) {
  var xy = this.getRelativeToSurfaceXY();
  this.svg_.getRootNode().setAttribute('transform',
      'translate(' + (xy.x + dx) + ', ' + (xy.y + dy) + ')');
  this.moveConnections_(dx, dy);
};

/**
 * Handle a mouse-down on an SVG block.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.Block.prototype.onMouseDown_ = function(e) {
  // Update Blockly's knowledge of its own location.
  Blockly.svgResize();

  Blockly.Block.terminateDrag_();
  this.select();
  Blockly.hideChaff(this.isInFlyout);
  if (Blockly.isRightButton(e)) {
    // Right-click.
    if (Blockly.ContextMenu) {
      this.showContextMenu_(e.clientX, e.clientY);
    }
  } else if (!this.editable) {
    // Allow uneditable blocks to be selected and context menued, but not
    // dragged.  Let this event bubble up to document, so the workspace may be
    // dragged instead.
    return;
  } else {
    // Left-click (or middle click)
    Blockly.removeAllRanges();
    Blockly.setCursorHand_(true);
    // Look up the current translation and record it.
    var xy = this.getRelativeToSurfaceXY();
    this.startDragX = xy.x;
    this.startDragY = xy.y;
    // Record the current mouse position.
    this.startDragMouseX = e.clientX;
    this.startDragMouseY = e.clientY;
    Blockly.Block.dragMode_ = 1;
    Blockly.Block.onMouseUpWrapper_ = Blockly.bindEvent_(Blockly.svgDoc,
        'mouseup', this, this.onMouseUp_);
    Blockly.Block.onMouseMoveWrapper_ = Blockly.bindEvent_(Blockly.svgDoc,
        'mousemove', this, this.onMouseMove_);
    // Build a list of comments that need to be moved and where they started.
    this.draggedBubbles_ = [];
    var descendants = this.getDescendants();
    for (var x = 0, descendant; descendant = descendants[x]; x++) {
      if (descendant.mutator) {
        var data = descendant.mutator.getIconLocation();
        data.bubble = descendant.mutator;
        this.draggedBubbles_.push(data);
      }
      if (descendant.comment) {
        var data = descendant.comment.getIconLocation();
        data.bubble = descendant.comment;
        this.draggedBubbles_.push(data);
      }
      if (descendant.warning) {
        var data = descendant.warning.getIconLocation();
        data.bubble = descendant.warning;
        this.draggedBubbles_.push(data);
      }
    }
  }
  // This event has been handled.  No need to bubble up to the document.
  e.stopPropagation();
};

/**
 * Handle a mouse-up anywhere in the SVG pane.  Is only registered when a
 * block is clicked.  We can't use mouseUp on the block since a fast-moving
 * cursor can briefly escape the block before it catches up.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Block.prototype.onMouseUp_ = function(e) {
  Blockly.Block.terminateDrag_();
  if (Blockly.selected && Blockly.highlightedConnection_) {
    // Connect two blocks together.
    Blockly.localConnection_.connect(Blockly.highlightedConnection_);
    if (this.svg_) {
      // Trigger a connection animation.
      // Determine which connection is inferior (lower in the source stack).
      var inferiorConnection;
      if (Blockly.localConnection_.isSuperior()) {
        inferiorConnection = Blockly.highlightedConnection_;
      } else {
        inferiorConnection = Blockly.localConnection_;
      }
      inferiorConnection.sourceBlock_.svg_.connectionUiEffect();
    }
    if (this.workspace.trashcan && this.workspace.trashcan.isOpen) {
      // Don't throw an object in the trash can if it just got connected.
      Blockly.Trashcan.close(this.workspace.trashcan);
    }
  } else if (this.workspace.trashcan && this.workspace.trashcan.isOpen) {
    var trashcan = this.workspace.trashcan;
    var closure = function() {
      Blockly.Trashcan.close(trashcan);
    };
    window.setTimeout(closure, 100);
    Blockly.selected.destroy(false, true);
    // Dropping a block on the trash can will usually cause the workspace to
    // resize to contain the newly positioned block.  Force a second resize now
    // that the block has been deleted.
    Blockly.fireUiEvent(window, 'resize');
  }
  if (Blockly.highlightedConnection_) {
    Blockly.highlightedConnection_.unhighlight();
    Blockly.highlightedConnection_ = null;
  }
};

/**
 * Load the block's help page in a new window.
 * @private
 */
Blockly.Block.prototype.showHelp_ = function() {
  var url = (typeof this.helpUrl == 'function') ? this.helpUrl() : this.helpUrl;
  if (url) {
    window.open(url);
  }
};

/**
 * Duplicate this block and its children.
 * @private
 */
Blockly.Block.prototype.duplicate_ = function() {
  // Create a duplicate via XML.
  var xmlBlock = Blockly.Xml.blockToDom_(this);
  Blockly.Xml.deleteNext(xmlBlock);
  var newBlock = Blockly.Xml.domToBlock_(this.workspace, xmlBlock);
  // Move the duplicate next to the old block.
  var xy = this.getRelativeToSurfaceXY();
  if (Blockly.RTL) {
    xy.x -= Blockly.SNAP_RADIUS;
  } else {
    xy.x += Blockly.SNAP_RADIUS;
  }
  xy.y += Blockly.SNAP_RADIUS * 2;
  newBlock.moveBy(xy.x, xy.y);
};

/**
 * Show the context menu for this block.
 * @param {number} x X-coordinate of mouse click.
 * @param {number} y Y-coordinate of mouse click.
 * @private
 */
Blockly.Block.prototype.showContextMenu_ = function(x, y) {
  if (!this.contextMenu) {
    return;
  }
  // Save the current block in a variable for use in closures.
  var block = this;
  var options = [];

  if (this.editable) {
    // Option to duplicate this block.
    var duplicateOption = {
      text: Blockly.MSG_DUPLICATE_BLOCK,
      enabled: true,
      callback: function() {
        block.duplicate_();
      }
    };
    options.push(duplicateOption);

    if (Blockly.Comment && !this.collapsed) {
      // Option to add/remove a comment.
      var commentOption = {enabled: true};
      if (this.comment) {
        commentOption.text = Blockly.MSG_REMOVE_COMMENT;
        commentOption.callback = function() {
          block.setCommentText(null);
        };
      } else {
        commentOption.text = Blockly.MSG_ADD_COMMENT;
        commentOption.callback = function() {
          block.setCommentText('');
        };
      }
      options.push(commentOption);
    }

    // Option to make block inline.
    if (!this.collapsed) {
      for (var i = 0; i < this.inputList.length; i++) {
        if (this.inputList[i].type == Blockly.INPUT_VALUE) {
          // Only display this option if there is a value input on the block.
          var inlineOption = {enabled: true};
          inlineOption.text = this.inputsInline ? Blockly.MSG_EXTERNAL_INPUTS :
                                                  Blockly.MSG_INLINE_INPUTS;
          inlineOption.callback = function() {
            block.setInputsInline(!block.inputsInline);
          };
          options.push(inlineOption);
          break;
        }
      }
    }

    // Option to collapse/expand block.
    if (this.collapsed) {
      var expandOption = {enabled: true};
      expandOption.text = Blockly.MSG_EXPAND_BLOCK;
      expandOption.callback = function() {
        block.setCollapsed(false);
      };
      options.push(expandOption);
    } else if (this.inputList.length) {
      // Only display this option if there are inputs on the block.
      var collapseOption = {enabled: true};
      collapseOption.text = Blockly.MSG_COLLAPSE_BLOCK;
      collapseOption.callback = function() {
        block.setCollapsed(true);
      };
      options.push(collapseOption);
    }

    // Option to disable/enable block.
    var disableOption = {
      text: this.disabled ?
          Blockly.MSG_ENABLE_BLOCK : Blockly.MSG_DISABLE_BLOCK,
      enabled: !this.getInheritedDisabled(),
      callback: function() {
        block.setDisabled(!block.disabled);
      }
    };
    options.push(disableOption);

    // Option to delete this block.
    // Count the number of blocks that are nested in this block.
    var descendantCount = this.getDescendants().length;
    if (block.nextConnection && block.nextConnection.targetConnection) {
      // Blocks in the current stack would survive this block's deletion.
      descendantCount -= this.nextConnection.targetBlock().
          getDescendants().length;
    }
    var deleteOption = {
      text: descendantCount == 1 ? Blockly.MSG_DELETE_BLOCK :
          Blockly.MSG_DELETE_X_BLOCKS.replace('%1', descendantCount),
      enabled: true,
      callback: function() {
        block.destroy(true, true);
      }
    };
    options.push(deleteOption);
  }

  // Option to get help.
  var url = (typeof this.helpUrl == 'function') ? this.helpUrl() : this.helpUrl;
  var helpOption = {enabled: !!url};
  helpOption.text = Blockly.MSG_HELP;
  helpOption.callback = function() {
    block.showHelp_();
  };
  options.push(helpOption);

  // Allow the block to add or modify options.
  if (this.customContextMenu) {
    this.customContextMenu(options);
  }

  Blockly.ContextMenu.show(x, y, options);
};

/**
 * Returns all connections originating from this block.
 * @param {boolean} all If true, return all connections even hidden ones.
 *     Otherwise return those that are visible.
 * @return {!Array.<!Blockly.Connection>} Array of connections.
 * @private
 */
Blockly.Block.prototype.getConnections_ = function(all) {
  var myConnections = [];
  if (all || this.rendered) {
    if (this.outputConnection) {
      myConnections.push(this.outputConnection);
    }
    if (this.nextConnection) {
      myConnections.push(this.nextConnection);
    }
    if (this.previousConnection) {
      myConnections.push(this.previousConnection);
    }
    if (all || !this.collapsed) {
      for (var x = 0, input; input = this.inputList[x]; x++) {
        if (input.connection) {
          myConnections.push(input.connection);
        }
      }
    }
  }
  return myConnections;
};

/**
 * Move the connections for this block and all blocks attached under it.
 * Also update any attached bubbles.
 * @param {number} dx Horizontal offset from current location.
 * @param {number} dy Vertical offset from current location.
 * @private
 */
Blockly.Block.prototype.moveConnections_ = function(dx, dy) {
  if (!this.rendered) {
    // Rendering is required to lay out the blocks.
    // This is probably an invisible block attached to a collapsed block.
    return;
  }
  var myConnections = this.getConnections_(false);
  for (var x = 0; x < myConnections.length; x++) {
    myConnections[x].moveBy(dx, dy);
  }
  if (this.mutator) {
    this.mutator.computeIconLocation();
  }
  if (this.comment) {
    this.comment.computeIconLocation();
  }
  if (this.warning) {
    this.warning.computeIconLocation();
  }

  // Recurse through all blocks attached under this one.
  for (var x = 0; x < this.childBlocks_.length; x++) {
    this.childBlocks_[x].moveConnections_(dx, dy);
  }
};

/**
 * Recursively adds or removes the dragging class to this node and its children.
 * @param {boolean} adding True if adding, false if removing.
 * @private
 */
Blockly.Block.prototype.setDragging_ = function(adding) {
  if (adding) {
    this.svg_.addDragging();
  } else {
    this.svg_.removeDragging();
  }
  // Recurse through all blocks attached under this one.
  for (var x = 0; x < this.childBlocks_.length; x++) {
    this.childBlocks_[x].setDragging_(adding);
  }
};

/**
 * Drag this block to follow the mouse.
 * @param {!Event} e Mouse move event.
 * @private
 */
Blockly.Block.prototype.onMouseMove_ = function(e) {
  if (e.type == 'mousemove' && e.x == 1 && e.y == 0 && e.button == 0) {
    /* HACK:
     The current versions of Chrome for Android (18.0) has a bug where finger-
     swipes trigger a rogue 'mousemove' event with invalid x/y coordinates.
     Ignore events with this signature.  This may result in a one-pixel blind
     spot in other browsers, but this shouldn't be noticable.
    */
    e.stopPropagation();
    return;
  }
  Blockly.removeAllRanges();
  var dx = e.clientX - this.startDragMouseX;
  var dy = e.clientY - this.startDragMouseY;
  if (Blockly.Block.dragMode_ == 1) {
    // Still dragging within the sticky DRAG_RADIUS.
    var dr = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    if (dr > Blockly.DRAG_RADIUS) {
      // Switch to unrestricted dragging.
      Blockly.Block.dragMode_ = 2;
      // Push this block to the very top of the stack.
      this.setParent(null);
      this.setDragging_(true);
    }
  }
  if (Blockly.Block.dragMode_ == 2) {
    // Unrestricted dragging.
    var x = this.startDragX + dx;
    var y = this.startDragY + dy;
    this.svg_.getRootNode().setAttribute('transform',
                                     'translate(' + x + ', ' + y + ')');
    // Drag all the nested comments.
    for (var x = 0; x < this.draggedBubbles_.length; x++) {
      var commentData = this.draggedBubbles_[x];
      commentData.bubble.setIconLocation(commentData.x + dx,
                                         commentData.y + dy);
    }

    // Check to see if any of this block's connections are within range of
    // another block's connection.
    var myConnections = this.getConnections_(false);
    var closestConnection = null;
    var localConnection = null;
    var radiusConnection = Blockly.SNAP_RADIUS;
    for (var i = 0; i < myConnections.length; i++) {
      var myConnection = myConnections[i];
      var neighbour = myConnection.closest(radiusConnection, dx, dy);
      if (neighbour.connection) {
        closestConnection = neighbour.connection;
        localConnection = myConnection;
        radiusConnection = neighbour.radius;
      }
    }

    // Remove connection highlighting if needed.
    if (Blockly.highlightedConnection_ &&
        Blockly.highlightedConnection_ != closestConnection) {
      Blockly.highlightedConnection_.unhighlight();
      Blockly.highlightedConnection_ = null;
      Blockly.localConnection_ = null;
    }
    // Add connection highlighting if needed.
    if (closestConnection &&
        closestConnection != Blockly.highlightedConnection_) {
      closestConnection.highlight();
      Blockly.highlightedConnection_ = closestConnection;
      Blockly.localConnection_ = localConnection;
    }
    // Flip the trash can lid if needed.
    this.workspace.trashcan && this.workspace.trashcan.onMouseMove(e);
  }
  // This event has been handled.  No need to bubble up to the document.
  e.stopPropagation();
};

/**
 * Bump unconnected blocks out of alignment.  Two blocks which aren't actually
 * connected should not coincidentally line up on screen.
 * @private
 */
Blockly.Block.prototype.bumpNeighbours_ = function() {
  var rootBlock = this.getRootBlock();
  // Loop though every connection on this block.
  var myConnections = this.getConnections_(false);
  for (var x = 0; x < myConnections.length; x++) {
    var connection = myConnections[x];
    // Spider down from this block bumping all sub-blocks.
    if (connection.targetConnection && connection.isSuperior()) {
      connection.targetBlock().bumpNeighbours_();
    }

    var neighbours = connection.neighbours_(Blockly.SNAP_RADIUS);
    for (var y = 0; y < neighbours.length; y++) {
      var otherConnection = neighbours[y];
      // If both connections are connected, that's probably fine.  But if
      // either one of them is unconnected, then there could be confusion.
      if (!connection.targetConnection || !otherConnection.targetConnection) {
        // Only bump blocks if they are from different tree structures.
        if (otherConnection.sourceBlock_.getRootBlock() != rootBlock) {
          // Always bump the inferior block.
          if (connection.isSuperior()) {
            otherConnection.bumpAwayFrom_(connection);
          } else {
            connection.bumpAwayFrom_(otherConnection);
          }
        }
      }
    }
  }
};

/**
 * Return the parent block or null if this block is at the top level.
 * @return {Blockly.Block} The block that holds the current block.
 */
Blockly.Block.prototype.getParent = function() {
  // Look at the DOM to see if we are nested in another block.
  return this.parentBlock_;
};

/**
 * Return the parent block that surrounds the current block, or null if this
 * block has no surrounding block.  A parent block might just be the previous
 * statement, whereas the surrounding block is an if statement, while loop, etc.
 * @return {Blockly.Block} The block that surrounds the current block.
 */
Blockly.Block.prototype.getSurroundParent = function() {
  var block = this;
  while (true) {
    do {
      var prevBlock = block;
      block = block.getParent();
      if (!block) {
        // Ran off the top.
        return null;
      }
    } while (block.nextConnection &&
             block.nextConnection.targetBlock() == prevBlock);
    // This block is an enclosing parent, not just a statement in a stack.
    return block;
  }
};

/**
 * Return the top-most block in this block's tree.
 * This will return itself if this block is at the top level.
 * @return {!Blockly.Block} The root block.
 */
Blockly.Block.prototype.getRootBlock = function() {
  var rootBlock;
  var block = this;
  do {
    rootBlock = block;
    block = rootBlock.parentBlock_;
  } while (block);
  return rootBlock;
};

/**
 * Find all the blocks that are directly nested inside this one.
 * Includes value and block inputs, as well as any following statement.
 * Excludes any connection on an output tab or any preceding statement.
 * @return {!Array.<!Blockly.Block>} Array of blocks.
 */
Blockly.Block.prototype.getChildren = function() {
  return this.childBlocks_;
};

/**
 * Set parent of this block to be a new block or null.
 * @param {Blockly.Block} newParent New parent block.
 */
Blockly.Block.prototype.setParent = function(newParent) {
  if (this.parentBlock_) {
    // Remove this block from the old parent's child list.
    var children = this.parentBlock_.childBlocks_;
    for (var child, x = 0; child = children[x]; x++) {
      if (child == this) {
        children.splice(x, 1);
        break;
      }
    }
    // Move this block up the DOM.  Keep track of x/y translations.
    var xy = this.getRelativeToSurfaceXY();
    this.workspace.getCanvas().appendChild(this.svg_.getRootNode());
    this.svg_.getRootNode().setAttribute('transform',
        'translate(' + xy.x + ', ' + xy.y + ')');

    // Disconnect from superior blocks.
    this.parentBlock_ = null;
    if (this.previousConnection && this.previousConnection.targetConnection) {
      this.previousConnection.disconnect();
    }
    if (this.outputConnection && this.outputConnection.targetConnection) {
      this.outputConnection.disconnect();
    }
    // This block hasn't actually moved on-screen, so there's no need to update
    // its connection locations.
  } else {
    // Remove this block from the workspace's list of top-most blocks.
    this.workspace.removeTopBlock(this);
  }

  this.parentBlock_ = newParent;
  if (newParent) {
    // Add this block to the new parent's child list.
    newParent.childBlocks_.push(this);

    var oldXY = this.getRelativeToSurfaceXY();
    if (newParent.svg_ && this.svg_) {
      newParent.svg_.getRootNode().appendChild(this.svg_.getRootNode());
    }
    var newXY = this.getRelativeToSurfaceXY();
    // Move the connections to match the child's new position.
    this.moveConnections_(newXY.x - oldXY.x, newXY.y - oldXY.y);
  } else {
    this.workspace.addTopBlock(this);
  }
};

/**
 * Find all the blocks that are directly or indirectly nested inside this one.
 * Includes this block in the list.
 * Includes value and block inputs, as well as any following statements.
 * Excludes any connection on an output tab or any preceding statements.
 * @return {!Array.<!Blockly.Block>} Flattened array of blocks.
 */
Blockly.Block.prototype.getDescendants = function() {
  var blocks = [this];
  for (var child, x = 0; child = this.childBlocks_[x]; x++) {
    blocks = blocks.concat(child.getDescendants());
  }
  return blocks;
};

/**
 * Get the colour of a block.
 * @return {string} HSV hue value.
 */
Blockly.Block.prototype.getColour = function() {
  return this.colourHue_;
};

/**
 * Change the colour of a block.
 * @param {number} colourHue HSV hue value.
 */
Blockly.Block.prototype.setColour = function(colourHue) {
  this.colourHue_ = colourHue;
  if (this.svg_) {
    this.svg_.updateColour();
  }
  if (this.mutator) {
    this.mutator.updateColour();
  }
  if (this.comment) {
    this.comment.updateColour();
  }
  if (this.warning) {
    this.warning.updateColour();
  }
  if (this.rendered) {
    this.render();
  }
};

/**
 * Add an item to the end of the title row.
 * @param {*} title Something to add as a title.
 * @param {string} opt_name Language-neutral identifier which may used to find
 *     this title again.  Should be unique to this block.
 * @return {!Blockly.Field} The title object created.
 */
Blockly.Block.prototype.appendTitle = function(title, opt_name) {
  // Generate a FieldLabel when given a plain text title.
  if (typeof title == 'string') {
    title = new Blockly.FieldLabel(title);
  }
  title.name = opt_name;

  // Add the title to the title row.
  this.titleRow.push(title);

  if (this.svg_) {
    title.init(this);
  }
  if (this.rendered) {
    this.render();
    // Adding a title will cause the block to change shape.
    this.bumpNeighbours_();
  }
  return title;
};

/**
 * Returns the named title or label from a block.
 * @param {string} name The name of the title.
 * @return {*} Named title, or null if title does not exist.
 * @private
 */
Blockly.Block.prototype.getTitle_ = function(name) {
  for (var x = 0, title; title = this.titleRow[x]; x++) {
    if (title.name === name) {
      return title;
    }
  }
  for (var x = 0, input; input = this.inputList[x]; x++) {
    for (var y = 0, title; title = input.titleRow[y]; y++) {
      if (title.name === name) {
        return title;
      }
    }
  }
  return null;
};

/**
 * Returns the language-neutral value from the title of a block.
 * @param {string} name The name of the title.
 * @return {!string} Value from the title or null if title does not exist.
 */
Blockly.Block.prototype.getTitleValue = function(name) {
  var title = this.getTitle_(name);
  if (title) {
    return title.getValue();
  }
  return null;
};

/**
 * Change the title value for a block (e.g. 'CHOOSE' or 'REMOVE').
 * @param {string} newValue Value to be the new title.
 * @param {string} name The name of the title.
 */
Blockly.Block.prototype.setTitleValue = function(newValue, name) {
  var title = this.getTitle_(name);
  if (title) {
    title.setValue(newValue);
  } else {
    throw 'Title "' + name + '" not found.';
  }
};

/**
 * Returns the human-readable text from the title of a block.
 * @param {string} name The name of the title.
 * @return {!string} Text from the title or null if title does not exist.
 * @deprecated Use getValueText instead.
 */
Blockly.Block.prototype.getTitleText = function(name) {
  // In September 2012 getTitleText was deprecated in favour of getTitleValue.
  // At some future date this section should be deleted.
  console.log('Obsolete call to getTitleText.  Please use getTitleValue.');
  var title = this.getTitle_(name);
  if (title) {
    return title.getText();
  }
  return null;
};

/**
 * Change the title text for a block (e.g. 'choose' or 'remove list item').
 * @param {string} newText Text to be the new title.
 * @param {string} name The name of the title.
 * @deprecated Use setValueText instead.
 */
Blockly.Block.prototype.setTitleText = function(newText, name) {
  // In September 2012 setTitleText was deprecated in favour of setTitleValue.
  // At some future date this section should be deleted.
  console.log('Obsolete call to setTitleText.  Please use setTitleValue.');
  var title = this.getTitle_(name);
  if (title) {
    title.setText(newText);
  } else {
    throw 'Title "' + name + '" not found.';
  }
};

/**
 * Change the tooltip text for a block.
 * @param {string|!Element} newTip Text for tooltip or a parent element to
 *     link to for its tooltip.
 */
Blockly.Block.prototype.setTooltip = function(newTip) {
  this.tooltip = newTip;
};

/**
 * Set whether this block can chain onto the bottom of another block.
 * @param {boolean} newBoolean True if there can be a previous statement.
 * @param {Object} opt_check Statement type or list of statement types.
 *     Null or undefined if any type could be connected.
 */
Blockly.Block.prototype.setPreviousStatement = function(newBoolean, opt_check) {
  if (this.previousConnection) {
    if (this.previousConnection.targetConnection) {
      throw 'Must disconnect previous statement before removing connection.';
    }
    this.previousConnection.destroy();
    this.previousConnection = null;
  }
  if (newBoolean) {
    if (this.outputConnection) {
      throw 'Remove output connection prior to adding previous connection.';
    }
    if (opt_check === undefined) {
      opt_check = null;
    }
    this.previousConnection =
        new Blockly.Connection(this, Blockly.PREVIOUS_STATEMENT, opt_check);
  }
  if (this.rendered) {
    this.render();
    this.bumpNeighbours_();
  }
};

/**
 * Set whether another block can chain onto the bottom of this block.
 * @param {boolean} newBoolean True if there can be a next statement.
 * @param {Object} opt_check Statement type or list of statement types.
 *     Null or undefined if any type could be connected.
 */
Blockly.Block.prototype.setNextStatement = function(newBoolean, opt_check) {
  if (this.nextConnection) {
    if (this.nextConnection.targetConnection) {
      throw 'Must disconnect next statement before removing connection.';
    }
    this.nextConnection.destroy();
    this.nextConnection = null;
  }
  if (newBoolean) {
    if (opt_check === undefined) {
      opt_check = null;
    }
    this.nextConnection =
        new Blockly.Connection(this, Blockly.NEXT_STATEMENT, opt_check);
  }
  if (this.rendered) {
    this.render();
    this.bumpNeighbours_();
  }
};

/**
 * Set whether this block returns a value.
 * @param {boolean} newBoolean True if there is an output.
 * @param {Object} opt_check Returned type or list of returned types.
 *     Null or undefined if any type could be returned (e.g. variable get).
 */
Blockly.Block.prototype.setOutput = function(newBoolean, opt_check) {
  if (this.outputConnection) {
    if (this.outputConnection.targetConnection) {
      throw 'Must disconnect output value before removing connection.';
    }
    this.outputConnection.destroy();
    this.outputConnection = null;
  }
  if (newBoolean) {
    if (this.previousConnection) {
      throw 'Remove previous connection prior to adding output connection.';
    }
    if (opt_check === undefined) {
      opt_check = null;
    }
    this.outputConnection =
        new Blockly.Connection(this, Blockly.OUTPUT_VALUE, opt_check);
  }
  if (this.rendered) {
    this.render();
    this.bumpNeighbours_();
  }
};

/**
 * Set whether value inputs are arranged horizontally or vertically.
 * @param {boolean} newBoolean True if inputs are horizontal.
 */
Blockly.Block.prototype.setInputsInline = function(newBoolean) {
  this.inputsInline = newBoolean;
  if (this.rendered) {
    this.render();
    this.bumpNeighbours_();
  }
};

/**
 * Set whether the block is disabled or not.
 * @param {boolean} disabled True if disabled.
 */
Blockly.Block.prototype.setDisabled = function(disabled) {
  if (this.disabled == disabled) {
    return;
  }
  this.disabled = disabled;
  this.svg_.updateDisabled();
  this.workspace.fireChangeEvent();
};

/**
 * Get whether the block is disabled or not due to parents.
 * The block's own disabled property is not considered.
 * @return {boolean} True if disabled.
 */
Blockly.Block.prototype.getInheritedDisabled = function() {
  var block = this;
  while (true) {
    var block = block.getSurroundParent();
    if (!block) {
      // Ran off the top.
      return false;
    } else if (block.disabled) {
      return true;
    }
  }
};

/**
 * Set whether the block is collapsed or not.
 * @param {boolean} collapsed True if collapsed.
 */
Blockly.Block.prototype.setCollapsed = function(collapsed) {
  if (this.collapsed == collapsed) {
    return;
  }
  this.collapsed = collapsed;
  // Show/hide the inputs.
  var display = collapsed ? 'none' : 'block';
  var renderList = [];
  for (var x = 0, input; input = this.inputList[x]; x++) {
    for (var y = 0, title; title = input.titleRow[y]; y++) {
      var labelElement = title.getRootElement ?
          title.getRootElement() : title;
      labelElement.style.display = display;
    }
    if (input.targetBlock) {
      // This is a connection.
      if (collapsed) {
        input.hideAll();
      } else {
        renderList = renderList.concat(input.unhideAll());
      }
      var child = input.targetBlock();
      if (child) {
        child.svg_.getRootNode().style.display = display;
        if (collapsed) {
          child.rendered = false;
        }
      }
    } else if (input.getText) {
      // This is a local variable.
      input.setVisible(!collapsed);
    }
  }

  if (collapsed && this.mutator) {
    this.mutator.setPinned(false);
  }
  if (collapsed && this.comment) {
    this.comment.setPinned(false);
  }
  if (collapsed && this.warning) {
    this.warning.setPinned(false);
  }

  if (renderList.length == 0) {
    // No child blocks, just render this block.
    renderList[0] = this;
  }
  if (this.rendered) {
    for (var x = 0, block; block = renderList[x]; x++) {
      block.render();
    }
    this.bumpNeighbours_();
  }
};

/**
 * Add a value input, statement input or local variable to this block.
 * @param {number} type Either Blockly.INPUT_VALUE or Blockly.NEXT_STATEMENT or
 *     Blockly.DUMMY_INPUT.
 * @param {string} name Language-neutral identifier which may used to find this
 *     input again.  Should be unique to this block.
 * @param {*} opt_check Acceptable value type, or list of value types.
 *     Null or undefined means all values are acceptable.
 * @return {!Blockly.Input} The input object created.
 */
Blockly.Block.prototype.appendInput = function(type, name, opt_check) {
  var connection = null;
  if (type == Blockly.INPUT_VALUE || type == Blockly.NEXT_STATEMENT) {
    connection = new Blockly.Connection(this, type, opt_check);
  }
  var input = new Blockly.Input(type, name, this, connection);
  // Append input to list.
  this.inputList.push(input);
  if (this.rendered) {
    this.render();
    // Adding an input will cause the block to change shape.
    this.bumpNeighbours_();
  }
  return input;
};

/**
 * Move an input to a different location on this block.
 * @param {string} name The name of the input to move.
 * @param {string} refName Name of input that should be after the moved input.
 */
Blockly.Block.prototype.moveInputBefore = function(name, refName) {
  if (name == refName) {
    throw 'Can\'t move "' + name + '" to itself.';
  }
  // Find both inputs.
  var inputIndex = -1;
  var refIndex = -1;
  for (var x = 0, input; input = this.inputList[x]; x++) {
    if (input.name == name) {
      inputIndex = x;
      if (refIndex != -1) {
        break;
      }
    } else if (input.name == refName) {
      refIndex = x;
      if (inputIndex != -1) {
        break;
      }
    }
  }
  if (inputIndex == -1) {
    throw 'Named input "' + name + '" not found.';
  }
  if (refIndex == -1) {
    throw 'Reference input "' + name + '" not found.';
  }
  // Remove input.
  this.inputList.splice(inputIndex, 1);
  if (inputIndex < refIndex) {
    refIndex--;
  }
  // Reinsert input.
  this.inputList.splice(refIndex, 0, input);
  if (this.rendered) {
    this.render();
    // Moving an input will cause the block to change shape.
    this.bumpNeighbours_();
  }
};

/**
 * Remove an input from this block.
 * @param {string} name The name of the input.
 */
Blockly.Block.prototype.removeInput = function(name) {
  for (var x = 0, input; input = this.inputList[x]; x++) {
    if (input.name == name) {
      if (input.connection && input.connection.targetConnection) {
        // Disconnect any attached block.
        input.connection.targetBlock().setParent(null);
      }
      input.destroy();
      this.inputList.splice(x, 1);
      if (this.rendered) {
        this.render();
        // Removing an input will cause the block to change shape.
        this.bumpNeighbours_();
      }
      return;
    }
  }
  throw 'Input "' + name + '" not found.';
};

/**
 * Fetches the named input object.
 * @param {string} name The name of the input.
 * @return {Object} The input object, or null of the input does not exist.
 */
Blockly.Block.prototype.getInput = function(name) {
  for (var x = 0, input; input = this.inputList[x]; x++) {
    if (input.name == name) {
      return input;
    }
  }
  // This input does not exist.
  return null;
};

/**
 * Fetches the block attached to the named input.
 * @param {string} name The name of the input.
 * @return {Blockly.Block} The attached value block, or null if the input is
 *     either disconnected or if the input does not exist.
 */
Blockly.Block.prototype.getInputTargetBlock = function(name) {
  var input = this.getInput(name);
  return input && input.connection && input.connection.targetBlock();
};

/**
 * Give this block a mutator dialog.
 * @param {Blockly.Mutator} mutator A mutator dialog instance or null to remove.
 */
Blockly.Block.prototype.setMutator = function(mutator) {
  if (this.mutator && this.mutator !== mutator) {
    this.mutator.destroy();
  }
  if (mutator) {
    mutator.block_ = this;
    this.mutator = mutator;
    if (this.svg_) {
      mutator.createIcon();
    }
  }
};

/**
 * Returns the comment on this block (or '' if none).
 * @return {string} Block's comment.
 */
Blockly.Block.prototype.getCommentText = function() {
  if (this.comment) {
    var comment = this.comment.getText();
    // Trim off trailing whitespace.
    return comment.replace(/\s+$/, '').replace(/ +\n/g, '\n');
  }
  return '';
};

/**
 * Set this block's comment text.
 * @param {?string} text The text, or null to delete.
 */
Blockly.Block.prototype.setCommentText = function(text) {
  if (!Blockly.Comment) {
    throw 'Comments not supported.';
  }
  var changedState = false;
  if (typeof text == 'string') {
    if (!this.comment) {
      this.comment = new Blockly.Comment(this);
      changedState = true;
    }
    this.comment.setText(text);
  } else {
    if (this.comment) {
      this.comment.destroy();
      changedState = true;
    }
  }
  if (this.rendered) {
    this.render();
    if (changedState) {
      // Adding or removing a comment icon will cause the block to change shape.
      this.bumpNeighbours_();
    }
  }
};

/**
 * Set this block's warning text.
 * @param {?string} text The text, or null to delete.
 */
Blockly.Block.prototype.setWarningText = function(text) {
  if (!Blockly.Warning) {
    throw 'Warnings not supported.';
  }
  var changedState = false;
  if (typeof text == 'string') {
    if (!this.warning) {
      this.warning = new Blockly.Warning(this);
      changedState = true;
    }
    this.warning.setText(text);
  } else {
    if (this.warning) {
      this.warning.destroy();
      changedState = true;
    }
  }
  if (this.rendered) {
    this.render();
    if (changedState) {
      // Adding or removing a warning icon will cause the block to change shape.
      this.bumpNeighbours_();
    }
  }
};

/**
 * Render the block.
 * Lays out and reflows a block based on its contents and settings.
 */
Blockly.Block.prototype.render = function() {
  this.svg_.render();
};
