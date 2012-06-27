/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
 * http://code.google.com/p/google-blockly/
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
  this.comment = null;
  this.collapsed = false;
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
};

/**
 * Pointer to SVG representation of the block.
 * @type {Blockly.BlockSvg}
 * @private
 */
Blockly.Block.prototype.svg_ = null;

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
 * 1 - Still inside the stickly DRAG_RADIUS.
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
Blockly.Block.unbindDragEvents_ = function(e) {
  if (Blockly.Block.onMouseUpWrapper_) {
    Blockly.unbindEvent_(Blockly.svgDoc, 'mouseup',
                         Blockly.Block.onMouseUpWrapper_);
    Blockly.Block.onMouseUpWrapper_ = null;
  }
  if (Blockly.Block.onMouseMoveWrapper_) {
    Blockly.unbindEvent_(Blockly.svgDoc, 'mousemove',
                         Blockly.Block.onMouseMoveWrapper_);
    Blockly.Block.onMouseMoveWrapper_ = null;
  }
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
  Blockly.fireUiEvent(Blockly.svgDoc, this.workspace.getCanvas(),
                      'blocklySelectChange');
};

/**
 * Unselect this block.  Remove its highlighting.
 */
Blockly.Block.prototype.unselect = function() {
  Blockly.selected = null;
  this.svg_.removeSelect();
  Blockly.fireUiEvent(Blockly.svgDoc, this.workspace.getCanvas(),
                      'blocklySelectChange');
};

/**
 * Destroy this block.
 * @param {boolean} gentle If gentle, then try to heal any gap by connecting
 *     the next statement with the previous statement.  Otherwise, destroy all
 *     children of this block.
 */
Blockly.Block.prototype.destroy = function(gentle) {
  if (this.outputConnection) {
    // Detach this block from the parent's tree.
    this.setParent(null);
  } else {
    var previousTarget = null;
    if (this.previousConnection && this.previousConnection.targetConnection) {
      // Remember the connection that any next statements need to connect to.
      previousTarget = this.previousConnection.targetConnection;
      // Detatch this block from the parent's tree.
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

  //This block is now at the top of the workspace.
  // Remove this block from the workspace's list of top-most blocks.
  this.workspace.removeTopBlock(this);

  // Just deleting this block from the DOM would result in a memory leak as
  // well as corruption of the connection database.  Therefore we must
  // methodically step through the blocks and carefully disassemble them.

  // Switch off rerendering.
  this.rendered = false;

  if (Blockly.selected == this) {
    Blockly.selected = null;
    // If there's a drag in-progress, unlink the mouse events.
    Blockly.Block.unbindDragEvents_();
  }

  // First, destroy all my children.
  for (var x = this.childBlocks_.length - 1; x >= 0; x--) {
    this.childBlocks_[x].destroy(false);
  }
  // Then destroy myself.
  for (var x = 0; x < this.titleRow.length; x++) {
    this.titleRow[x].destroy();
  }
  if (this.comment) {
    this.comment.destroy();
  }
  if (this.mutator) {
    this.mutator.destroy();
  }
  // Destroy all inputs and their labels.
  for (var x = 0; x < this.inputList.length; x++) {
    var input = this.inputList[x];
    if (input.label) {
      input.label.destroy();
    }
    if (input.destroy) {
      input.destroy();
    }
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
 * drawing surface's orgin (0,0).
 * @return {!Object} Object with .x and .y properties.
 */
Blockly.Block.prototype.getRelativeToSurfaceXY = function() {
  var element = this.svg_.getRootNode();
  var x = 0;
  var y = 0;
  do {
    // Loop through this block and every parent.
    var xy = Blockly.getRelativeXY_(element);
    x += xy.x;
    y += xy.y;
    element = element.parentNode;
  } while (element && element != this.workspace.getCanvas());
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

  Blockly.Block.unbindDragEvents_();
  this.select();
  Blockly.hideChaff(this.isInFlyout);
  if (e.button == 2) {
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
    this.draggedComments_ = [];
    var descendants = this.getDescendants();
    for (var x = 0, descendant; descendant = descendants[x]; x++) {
      if (descendant.comment) {
        var data = descendant.comment.getIconLocation();
        data.comment = descendant.comment;
        this.draggedComments_.push(data);
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
  /* BUG:
  In rare cases this onMouseUp event can be lost in Firefox due to a race
  condition.  Possibly: https://bugzilla.mozilla.org/show_bug.cgi?id=672677
  When this happens a dragged/clicked block becomes glued to the mouse
  cursor despite no button being depressed.  This state lasts until the
  user clicks to shake the block off.
  Ideally the mousemove function would check which buttons are depressed.
  Unfortunately Firefox sets e.button=0 and e.which=1 regardless of whether
  the left button is down or not.  Thus it is impossible to know the button
  state during mouse move.
  */
  Blockly.Block.unbindDragEvents_();
  if (Blockly.Block.dragMode_ == 2) {
    if (Blockly.selected != this) {
      throw 'Dragging no object?';
    }
    this.setDragging_(false);
    // Update the connection locations.
    var xy = this.getRelativeToSurfaceXY();
    var dx = xy.x - this.startDragX;
    var dy = xy.y - this.startDragY;
    this.moveConnections_(dx, dy);
    var selected = this;
    // Fire an event to allow scrollbars to resize.
    Blockly.fireUiEvent(Blockly.svgDoc, window, 'resize');
    window.setTimeout(function() {selected.bumpNeighbours_();},
                      Blockly.BUMP_DELAY);
  }
  Blockly.Block.dragMode_ = 0;
  delete this.draggedComments_;
  if (Blockly.selected && Blockly.highlightedConnection_) {
    Blockly.playAudio('click');
    // Connect two blocks together.
    Blockly.localConnection_.connect(Blockly.highlightedConnection_);
    if (this.workspace.trashcan && this.workspace.trashcan.isOpen) {
      // Don't throw an object in the trash can if it just got connected.
      Blockly.Trashcan.close(this.workspace.trashcan);
    }
  } else if (this.workspace.trashcan && this.workspace.trashcan.isOpen) {
    Blockly.playAudio('delete');
    Blockly.selected.destroy(false);
    var trashcan = this.workspace.trashcan;
    var closure = function() {
      Blockly.Trashcan.close(trashcan);
    };
    window.setTimeout(closure, 100);
    // Dropping a block on the trash can will usually cause the workspace to
    // resize to contain the newly positioned block.  Force a second resize now
    // that the block has been deleted.
    Blockly.fireUiEvent(Blockly.svgDoc, window, 'resize');
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
        Blockly.playAudio('delete');
        block.destroy(true);
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
        if (input.type != Blockly.LOCAL_VARIABLE) {
          myConnections.push(input);
        }
      }
    }
  }
  return myConnections;
};

/**
 * Move the connections for this block and all blocks attached under it.
 * Also update any attached comment.
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
  if (this.comment) {
    this.comment.computeIconLocation();
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
    for (var x = 0; x < this.draggedComments_.length; x++) {
      var commentData = this.draggedComments_[x];
      commentData.comment.setIconLocation(commentData.x + dx,
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
    if (connection.targetConnection &&
        (connection.type == Blockly.INPUT_VALUE ||
         connection.type == Blockly.NEXT_STATEMENT)) {
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
          otherConnection.bumpAwayFrom_(connection);
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
 * Excludes any connection on an output tab or any preceeding statement.
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
    newParent.svg_.getRootNode().appendChild(this.svg_.getRootNode());
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
 * Excludes any connection on an output tab or any preceeding statements.
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
  if (this.comment) {
    this.comment.updateColour();
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
 * Returns the human-readable text from the title of a block.
 * @param {string} name The name of the title.
 * @return {!string} Text from the title or null if title does not exist.
 */
Blockly.Block.prototype.getTitleText = function(name) {
  for (var x = 0, title; title = this.titleRow[x]; x++) {
    if (title.name === name) {
      return title.getText();
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
  for (var x = 0, title; title = this.titleRow[x]; x++) {
    if (title.name === name) {
      return title.getValue();
    }
  }
  return null;
};

/**
 * Change the title text for a block (e.g. 'choose' or 'remove list item').
 * @param {string} newText Text to be the new title.
 * @param {string} name The name of the title.
 */
Blockly.Block.prototype.setTitleText = function(newText, name) {
  for (var x = 0, title; title = this.titleRow[x]; x++) {
    if (title.name === name) {
      title.setText(newText);
      return;
    }
  }
  throw 'Title "' + name + '" not found.';
};

/**
 * Change the title value for a block (e.g. 'CHOOSE' or 'REMOVE').
 * @param {string} newValue Value to be the new title.
 * @param {string} name The name of the title.
 */
Blockly.Block.prototype.setTitleValue = function(newValue, name) {
  for (var x = 0, title; title = this.titleRow[x]; x++) {
    if (title.name === name) {
      title.setValue(newValue);
      return;
    }
  }
  throw 'Title "' + name + '" not found.';
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
 */
Blockly.Block.prototype.setPreviousStatement = function(newBoolean) {
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
    this.previousConnection =
        new Blockly.Connection(this, Blockly.PREVIOUS_STATEMENT);
  }
  if (this.rendered) {
    this.render();
    this.bumpNeighbours_();
  }
};

/**
 * Set whether another block can chain onto the bottom of this block.
 * @param {boolean} newBoolean True if there can be a next statement.
 */
Blockly.Block.prototype.setNextStatement = function(newBoolean) {
  if (this.nextConnection) {
    if (this.nextConnection.targetConnection) {
      throw 'Must disconnect next statement before removing connection.';
    }
    this.nextConnection.destroy();
    this.nextConnection = null;
  }
  if (newBoolean) {
    this.nextConnection = new Blockly.Connection(this, Blockly.NEXT_STATEMENT);
  }
  if (this.rendered) {
    this.render();
    this.bumpNeighbours_();
  }
};

/**
 * Set whether this block returns a value.
 * @param {boolean} newBoolean True if there is an output.
 */
Blockly.Block.prototype.setOutput = function(newBoolean) {
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
    this.outputConnection = new Blockly.Connection(this, Blockly.OUTPUT_VALUE);
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
    if (input.label) {
      var labelElement = input.label.getRootElement ?
          input.label.getRootElement() : input.label;
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

  if (collapsed && this.comment) {
    this.comment.setPinned(false);
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
 * @param {string|Blockly.Field} label Printed next to the input
 *     (e.g. 'x' or 'do').  May be an editable field.
 * @param {number} type Either Blockly.INPUT_VALUE or Blockly.NEXT_STATEMENT or
 *     Blockly.LOCAL_VARIABLE.
 * @param {string} name Language-neutral identifier which may used to find this
 *     input again.  Should be unique to this block.
 * @return {!Object} The input object created.
 */
Blockly.Block.prototype.appendInput = function(label, type, name) {
  // Create descriptive text element.
  var textElement = null;
  if (label) {
    if (typeof label == 'string') {
      // Text label.
      textElement = new Blockly.FieldLabel(label);
    } else if (typeof label == 'object') {
      // Editable label.
      textElement = label;
    }
    if (this.svg_) {
      textElement.init(this);
    }
  }
  var input;
  if (type == Blockly.LOCAL_VARIABLE) {
    // Add input to list.
    input = new Blockly.FieldDropdown(
        Blockly.Variables.dropdownCreate, Blockly.Variables.dropdownChange);
    if (this.svg_) {
      input.init(this);
    }
    input.type = Blockly.LOCAL_VARIABLE;
  } else {
    // Add input to list.
    input = new Blockly.Connection(this, type);
  }
  input.label = textElement;
  input.name = name;
  if (typeof opt_index == 'number') {
    if (opt_index < 0 || opt_index > this.inputList.length) {
      throw 'There are ' + this.inputList.length +
            ' input(s), unable to insert at index ' + opt_index + '.';
    }
    this.inputList.splice(opt_index, 0, input);
  } else {
    this.inputList.push(input);
  }
  if (this.rendered) {
    this.render();
    // Adding an input will cause the block to change shape.
    this.bumpNeighbours_();
  }
  return input;
};

/**
 * Remove an input from this block.
 * @param {string} name The name of the input.
 */
Blockly.Block.prototype.removeInput = function(name) {
  for (var x = 0; x < this.inputList.length; x++) {
    var input = this.inputList[x];
    if (input.name == name) {
      if (input.targetConnection) {
        // Disconnect any attached block.
        input.targetBlock().setParent(null);
      }
      var field = input.label;
      if (field) {
        field.destroy();
      }
      if (input.destroy) {
        input.destroy();
      }
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
  for (var x = 0; x < this.inputList.length; x++) {
    if (this.inputList[x].name == name) {
      return this.inputList[x];
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
  return input && input.targetBlock();
};

/**
 * Gets the variable name attached to the named variable input.
 * @param {string} name The name of the input.
 * @return {string} The variable name, or null if the input does not exist.
 */
Blockly.Block.prototype.getInputVariable = function(name) {
  var input = this.getInput(name);
  return input && input.getText();
};

/**
 * Sets the variable name attached to the named variable input.
 * @param {string} name The name of the input.
 * @param {string} text The new variable name.
 */
Blockly.Block.prototype.setInputVariable = function(name, text) {
  var input = this.getInput(name);
  if (!input) {
    throw 'Input does not exist.';
  }
  input.setText(text);
};

/**
 * Fetches the value of the label attached to the named input.
 * @param {string} name The name of the input.
 * @return {string} The label's text, or null if the input does not exist.
 */
Blockly.Block.prototype.getInputLabelValue = function(name) {
  var input = this.getInput(name);
  if (input) {
    var label = input.label;
    if (label) {
      if (label.getText) {
        // Editable field.
        return label.getValue();
      } else {
        // Static text.
        return label.textContent;
      }
    } else {
      // Input exists, but label doesn't.
      return '';
    }
  }
  // This input does not exist.
  return null;
};

/**
 * Give this block a mutator dialog.
 * @param {Blockly.Mutator} mutator A mutator dialog instance or null to remove.
 */
Blockly.Block.prototype.setMutator = function(mutator) {
  if (this.mutator && this.mutator !== mutator) {
    this.mutator.destroy();
  }
  this.mutator = mutator;
  if (this.svg_) {
    mutator.createIcon();
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
      this.comment = new Blockly.Comment(this, Blockly.commentCanvas);
      changedState = true;
    }
    this.comment.setText(text);
  } else {
    if (this.comment) {
      this.comment.destroy();
      this.comment = null;
      changedState = true;
    }
  }
  if (this.rendered) {
    this.render();
    if (changedState) {
      // Adding or removing the comment will cause the block to change shape.
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
