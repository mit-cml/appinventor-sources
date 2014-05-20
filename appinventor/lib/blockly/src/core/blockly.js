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
 * @fileoverview Core JavaScript library for Blockly.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

/**
 * [lyn, 10/10/13] Modified Blockly.hideChaff() method to hide single instance of Blockly.FieldFlydown.
 */

// Top level object for Blockly.
goog.provide('Blockly');

// Blockly core dependencies.
goog.require('Blockly.Block');
goog.require('Blockly.Connection');
goog.require('Blockly.FieldAngle');
goog.require('Blockly.FieldCheckbox');
goog.require('Blockly.FieldColour');
goog.require('Blockly.FieldDropdown');
goog.require('Blockly.FieldImage');
goog.require('Blockly.FieldTextInput');
goog.require('Blockly.FieldVariable');
goog.require('Blockly.Generator');
goog.require('Blockly.inject');
goog.require('Blockly.Procedures');
//goog.require('Blockly.Toolbox');
goog.require('Blockly.TypeBlock');
goog.require('Blockly.utils');
goog.require('Blockly.WidgetDiv');
goog.require('Blockly.Workspace');

// Closure dependencies.
goog.require('goog.dom');
goog.require('goog.color');
goog.require('goog.events');
goog.require('goog.string');
goog.require('goog.ui.ColorPicker');
goog.require('goog.ui.tree.TreeControl');
goog.require('goog.userAgent');


/**
 * Path to Blockly's directory.  Can be relative, absolute, or remote.
 * Used for loading additional resources.
 */
Blockly.pathToBlockly = './';

/**
 * Required name space for SVG elements.
 * @const
 */
Blockly.SVG_NS = 'http://www.w3.org/2000/svg';
/**
 * Required name space for HTML elements.
 * @const
 */
Blockly.HTML_NS = 'http://www.w3.org/1999/xhtml';

/**
 * The richness of block colours, regardless of the hue.
 * Must be in the range of 0 (inclusive) to 1 (exclusive).
 */
Blockly.HSV_SATURATION = 0.45;
/**
 * The intensity of block colours, regardless of the hue.
 * Must be in the range of 0 (inclusive) to 1 (exclusive).
 */
Blockly.HSV_VALUE = 0.65;

/**
 * Convert a hue (HSV model) or an RGB Array
 * into an RGB hex triplet.
 * @param {number|Array} hueOrRGBArray Hue on a colour wheel (0-360) or RGB array.
 * @return {string} RGB code, e.g. '#5ba65b'.
 */
Blockly.makeColour = function(hueOrRGBArray) {
  if(Array.isArray(hueOrRGBArray)){
    return goog.color.rgbArrayToHex(hueOrRGBArray);
  } else {
    return goog.color.hsvToHex(hueOrRGBArray, Blockly.HSV_SATURATION,
      Blockly.HSV_VALUE * 256);
  }
};

/**
 * ENUM for a right-facing value input.  E.g. 'test' or 'return'.
 * @const
 */
Blockly.INPUT_VALUE = 1;
/**
 * ENUM for a left-facing value output.  E.g. 'call random'.
 * @const
 */
Blockly.OUTPUT_VALUE = 2;
/**
 * ENUM for a down-facing block stack.  E.g. 'then-do' or 'else-do'.
 * @const
 */
Blockly.NEXT_STATEMENT = 3;
/**
 * ENUM for an up-facing block stack.  E.g. 'close screen'.
 * @const
 */
Blockly.PREVIOUS_STATEMENT = 4;
/**
 * ENUM for an dummy input.  Used to add title(s) with no input.
 * @const
 */
Blockly.DUMMY_INPUT = 5;

/**
 * ENUM for an indented value input.  Similar to next_statement but with value
 * input shape.
 * @const
 */

Blockly.INDENTED_VALUE = 6;

/**
 * ENUM for a collapsed dummy input.  Used to show block titles while collapsed.
 * @const
 */
Blockly.DUMMY_COLLAPSED_INPUT = 7;

/**
 * ENUM for left alignment.
 * @const
 */
Blockly.ALIGN_LEFT = -1;
/**
 * ENUM for centre alignment.
 * @const
 */
Blockly.ALIGN_CENTRE = 0;
/**
 * ENUM for right alignment.
 * @const
 */
Blockly.ALIGN_RIGHT = 1;

/**
 * Lookup table for determining the opposite type of a connection.
 * @const
 */
Blockly.OPPOSITE_TYPE = [];
Blockly.OPPOSITE_TYPE[Blockly.INPUT_VALUE] = Blockly.OUTPUT_VALUE;
Blockly.OPPOSITE_TYPE[Blockly.OUTPUT_VALUE] = Blockly.INPUT_VALUE;
Blockly.OPPOSITE_TYPE[Blockly.NEXT_STATEMENT] = Blockly.PREVIOUS_STATEMENT;
Blockly.OPPOSITE_TYPE[Blockly.PREVIOUS_STATEMENT] = Blockly.NEXT_STATEMENT;

/**
 * Database of pre-loaded sounds.
 * @private
 * @const
 */
Blockly.SOUNDS_ = {};

/**
 * Workspace blocks arrangements
 */
Blockly.BLKS_HORIZONTAL = 0;
Blockly.BLKS_VERTICAL = 1;
Blockly.BLKS_CATEGORY = 2;

/**
 * Current Workspace arrangement state for position (horizontal or vertical),
 * and for type (category)
 */
Blockly.workspace_arranged_position = null;
Blockly.workspace_arranged_latest_position = null; //used to default to (previous is used for menus)
Blockly.workspace_arranged_type = null;

/**
 * Currently selected block.
 * @type {Blockly.Block}
 */
Blockly.selected = null;

/**
 * Is Blockly in a read-only, non-editable mode?
 * Note that this property may only be set before init is called.
 * It can't be used to dynamically toggle editability on and off.
 */
Blockly.readOnly = false;

/**
 * Currently highlighted connection (during a drag).
 * @type {Blockly.Connection}
 * @private
 */
Blockly.highlightedConnection_ = null;

/**
 * Connection on dragged block that matches the highlighted connection.
 * @type {Blockly.Connection}
 * @private
 */
Blockly.localConnection_ = null;

/**
 * Number of pixels the mouse must move before a drag starts.
 * @const
 */
Blockly.DRAG_RADIUS = 5;

/**
 * Maximum misalignment between connections for them to snap together.
 * @const
 */
Blockly.SNAP_RADIUS = 15;

/**
 * Delay in ms between trigger and bumping unconnected block out of alignment.
 * @const
 */
Blockly.BUMP_DELAY = 250;

/**
 * The main workspace (defined by inject.js).
 * @type {Blockly.Workspace}
 */
Blockly.mainWorkspace = null;

/**
 * Contents of the local clipboard.
 * @type {Element}
 * @private
 */
Blockly.clipboard_ = null;

/**
 * Returns the dimensions of the current SVG image.
 * @return {!Object} Contains width, height, top and left properties.
 */
Blockly.svgSize = function() {
  return {width: Blockly.svg.cachedWidth_,
          height: Blockly.svg.cachedHeight_};
};

/**
 * Size the SVG image to completely fill its container.  Record both
 * the height/width and the absolute position of the SVG image.
 */
Blockly.svgResize = function() {
  var svg = Blockly.svg;
  var div = svg.parentNode;
  var width = div.offsetWidth;
  var height = div.offsetHeight;
  if (svg.cachedWidth_ != width) {
    svg.setAttribute('width', width + 'px');
    svg.cachedWidth_ = width;
  }
  if (svg.cachedHeight_ != height) {
    svg.setAttribute('height', height + 'px');
    svg.cachedHeight_ = height;
  }
  // Update the scrollbars (if they exist).
  if (Blockly.mainWorkspace.scrollbar) {
    Blockly.mainWorkspace.scrollbar.resize();
  }
};

/**
 * latest clicked position is used to open the type blocking suggestions window
 * Initial position is 0,0
 * @type {{x: number, y:number}}
 */
Blockly.latestClick = { x: 0, y: 0 };

/**
 * Handle a mouse-down on SVG drawing surface.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.onMouseDown_ = function(e) {
  Blockly.latestClick = { x: e.clientX, y: e.clientY };
  Blockly.terminateDrag_(); // In case mouse-up event was lost.
  Blockly.hideChaff();
  //if drawer exists and supposed to close
  if(Blockly.Drawer && Blockly.Drawer.flyout_.autoClose) {
    Blockly.Drawer.hide();
  }

  //Closes mutators
  var blocks = Blockly.mainWorkspace.getAllBlocks();
  var numBlocks = blocks.length;
  var temp_block = null;
  for(var i =0; i<numBlocks; i++){
    temp_block = blocks[i];
    if(temp_block.mutator){
      //deselect block in mutator workspace
      if(Blockly.selected && Blockly.selected.workspace && Blockly.selected.workspace!=Blockly.mainWorkspace){
        Blockly.selected.unselect();
      }
      blocks[i].mutator.setVisible(false);
    }
  }

  var isTargetSvg = e.target && e.target.nodeName &&
      e.target.nodeName.toLowerCase() == 'svg';
  if (!Blockly.readOnly && Blockly.selected && isTargetSvg) {
    // Clicking on the document clears the selection.
    Blockly.selected.unselect();
  }
  if (Blockly.isRightButton(e)) {
    // Right-click.
    if (Blockly.ContextMenu) {
      Blockly.showContextMenu_(Blockly.mouseToSvg(e));
    }
  } else if ((Blockly.readOnly || isTargetSvg) &&
             Blockly.mainWorkspace.scrollbar) {
    // If the workspace is editable, only allow dragging when gripping empty
    // space.  Otherwise, allow dragging when gripping anywhere.
    Blockly.mainWorkspace.dragMode = true;
    // Record the current mouse position.
    Blockly.mainWorkspace.startDragMouseX = e.clientX;
    Blockly.mainWorkspace.startDragMouseY = e.clientY;
    Blockly.mainWorkspace.startDragMetrics = Blockly.mainWorkspace.getMetrics();
    Blockly.mainWorkspace.startScrollX = Blockly.mainWorkspace.scrollX;
    Blockly.mainWorkspace.startScrollY = Blockly.mainWorkspace.scrollY;
  }
};

/**
 * Handle a mouse-up on SVG drawing surface.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.onMouseUp_ = function(e) {
  Blockly.setCursorHand_(false);
  Blockly.mainWorkspace.dragMode = false;
};

/**
 * Handle a mouse-move on SVG drawing surface.
 * @param {!Event} e Mouse move event.
 * @private
 */
Blockly.onMouseMove_ = function(e) {
  if (Blockly.mainWorkspace.dragMode) {
    Blockly.removeAllRanges();
    var dx = e.clientX - Blockly.mainWorkspace.startDragMouseX;
    var dy = e.clientY - Blockly.mainWorkspace.startDragMouseY;
    var metrics = Blockly.mainWorkspace.startDragMetrics;
    var x = Blockly.mainWorkspace.startScrollX + dx;
    var y = Blockly.mainWorkspace.startScrollY + dy;
    x = Math.min(x, -metrics.contentLeft);
    y = Math.min(y, -metrics.contentTop);
    x = Math.max(x, metrics.viewWidth - metrics.contentLeft -
                 metrics.contentWidth);
    y = Math.max(y, metrics.viewHeight - metrics.contentTop -
                 metrics.contentHeight);

    // Move the scrollbars and the page will scroll automatically.
    Blockly.mainWorkspace.scrollbar.set(-x - metrics.contentLeft,
                                        -y - metrics.contentTop);
  }
};

/**
 * Handle a key-down on SVG drawing surface.
 * @param {!Event} e Key down event.
 * @private
 */
Blockly.onKeyDown_ = function(e) {
  if (Blockly.isTargetInput_(e)) {
    // When focused on an HTML text input widget, don't trap any keys.
    return;
  }
  // TODO: Add keyboard support for cursoring around the context menu.
  if (e.keyCode == 27) {
    // Pressing esc closes the context menu.
    Blockly.hideChaff();
  } else if (e.keyCode == 8 || e.keyCode == 46) {
    // Delete or backspace.
    try {
      if (Blockly.selected && Blockly.selected.isDeletable()) {
        var descendantCount = Blockly.selected.getDescendants().length;
        if (Blockly.selected.nextConnection && Blockly.selected.nextConnection.targetConnection) {
          descendantCount -= Blockly.selected.nextConnection.targetBlock().
            getDescendants().length;
        }
        // Ask for confirmation before deleting 3 or more blocks
        if (descendantCount >= 3) {
          if (confirm("Are you sure you want to delete all " + descendantCount + " of these blocks?")) {
            Blockly.hideChaff();
            Blockly.selected.dispose(true, true);
          }
        }
        else {
          Blockly.hideChaff();
          Blockly.selected.dispose(true, true);
        }
      }
    } finally {
      // Stop the browser from going back to the previous page.
      // Use a finally so that any error in delete code above doesn't disappear
      // from the console when the page rolls back.
      e.preventDefault();
    }
  } else if (e.altKey || e.ctrlKey || e.metaKey) {
    if (Blockly.selected && Blockly.selected.isDeletable() &&
        Blockly.selected.workspace == Blockly.mainWorkspace) {
      Blockly.hideChaff();
      if (e.keyCode == 67) {
        // 'c' for copy.
        Blockly.copy_(Blockly.selected);
      } else if (e.keyCode == 88) {
        // 'x' for cut.
        Blockly.copy_(Blockly.selected);
        Blockly.selected.dispose(true, true);
      }
    }
    if (e.keyCode == 86) {
      // 'v' for paste.
      if (Blockly.clipboard_) {
        Blockly.mainWorkspace.paste(Blockly.clipboard_);
      }
    }
  }
};

/**
 * Stop binding to the global mouseup and mousemove events.
 * @private
 */
Blockly.terminateDrag_ = function() {
  Blockly.Block.terminateDrag_();
  Blockly.Flyout.terminateDrag_();
};

/**
 * Copy a block onto the local clipboard.
 * @param {!Blockly.Block} block Block to be copied.
 * @private
 */
Blockly.copy_ = function(block) {
  var xmlBlock = Blockly.Xml.blockToDom_(block);
  Blockly.Xml.deleteNext(xmlBlock);
  // Encode start position in XML.
  var xy = block.getRelativeToSurfaceXY();
  xmlBlock.setAttribute('x', Blockly.RTL ? -xy.x : xy.x);
  xmlBlock.setAttribute('y', xy.y);
  Blockly.clipboard_ = xmlBlock;
};

/**
 * Show the context menu for the workspace.
 * @param {!Object} xy Coordinates of mouse click, contains x and y properties.
 * @private
 */
Blockly.showContextMenu_ = function(xy) {
  if (Blockly.readOnly) {
    return;
  }
  var options = [];

  if (Blockly.collapse) {
    var hasCollapsedBlocks = false;
    var hasExpandedBlocks = false;
    var topBlocks = Blockly.mainWorkspace.getTopBlocks(false);
    for (var i = 0; i < topBlocks.length; i++) {
      if (topBlocks[i].collapsed) {
        hasCollapsedBlocks = true;
      } else {
        hasExpandedBlocks = true;
      }
    }

    // Option to collapse top blocks.
    var collapseOption = {enabled: hasExpandedBlocks};
    collapseOption.text = Blockly.MSG_COLLAPSE_ALL;
    collapseOption.callback = function() {
      for (var i = 0; i < topBlocks.length; i++) {
        topBlocks[i].setCollapsed(true);
      }
      Blockly.resetWorkspaceArrangements();
    };
    options.push(collapseOption);
    // Option to expand top blocks.
    var expandOption = {enabled: hasCollapsedBlocks};

    expandOption.text = Blockly.MSG_EXPAND_ALL;
    expandOption.callback = function() {
      for (var i = 0; i < topBlocks.length; i++) {
        topBlocks[i].setCollapsed(false);
      }
      Blockly.resetWorkspaceArrangements();
    };
    options.push(expandOption);
  }

  // Arrange blocks in row order.
  var arrangeOptionH = {enabled: (Blockly.workspace_arranged_position !== Blockly.BLKS_HORIZONTAL)};
  arrangeOptionH.text = Blockly.MSG_ARRANGE_H;
  arrangeOptionH.callback = function() {
    Blockly.workspace_arranged_position = Blockly.BLKS_HORIZONTAL;
    Blockly.workspace_arranged_latest_position= Blockly.BLKS_HORIZONTAL;
    arrangeBlocks(Blockly.BLKS_HORIZONTAL);
  };
  options.push(arrangeOptionH);

  // Arrange blocks in column order.
  var arrangeOptionV = {enabled: (Blockly.workspace_arranged_position !== Blockly.BLKS_VERTICAL)};
  arrangeOptionV.text = Blockly.MSG_ARRANGE_V;
  arrangeOptionV.callback = function() {
    Blockly.workspace_arranged_position = Blockly.BLKS_VERTICAL;
    Blockly.workspace_arranged_latest_position = Blockly.BLKS_VERTICAL;
    arrangeBlocks(Blockly.BLKS_VERTICAL);
  };
  options.push(arrangeOptionV);

  /**
   * Function that returns a name to be used to sort blocks.
   * The general comparator is the block.category attribute.
   * In the case of 'Components' the comparator is the instanceName of the component if it exists
   * (it does not exist for generic components).
   * In the case of Procedures the comparator is the NAME(for definitions) or PROCNAME (for calls)
   * @param {!Blockly.Block} the block that will be compared in the sortByCategory function
   * @returns {string} text to be used in the comparison
   */
  function comparisonName(block){
    if (block.category === 'Component' && block.instanceName)
      return block.instanceName;
    if (block.category === 'Procedures')
      return (block.getTitleValue('NAME') || block.getTitleValue('PROCNAME'));
    return block.category;
  }

  /**
   * Function used to sort blocks by Category.
   * @param {!Blockly.Block} first block to be compared
   * @param {!Blockly.Block} b second block to be compared
   * @returns {number} returns 0 if the blocks are equal, and -1 or 1 if they are not
   */
  function sortByCategory(a,b) {
    var comparatorA = comparisonName(a).toLowerCase();
    var comparatorB = comparisonName(b).toLowerCase();

    if (comparatorA < comparatorB) return -1;
    else if (comparatorA > comparatorB) return +1;
    else return 0;
  }

  // Arranges block in layout (Horizontal or Vertical).
  function arrangeBlocks(layout) {
    var SPACER = 25;
    var topblocks = Blockly.mainWorkspace.getTopBlocks(false);
    // If the blocks are arranged by Category, sort the array
    if (Blockly.workspace_arranged_type === Blockly.BLKS_CATEGORY){
      topblocks.sort(sortByCategory);
    }
    var metrics = Blockly.mainWorkspace.getMetrics();
    var viewLeft = metrics.viewLeft + 5;
    var viewTop = metrics.viewTop + 5;
    var x = viewLeft;
    var y = viewTop;
    var wsRight = viewLeft + metrics.viewWidth;
    var wsBottom = viewTop + metrics.viewHeight;
    var maxHgt = 0;
    var maxWidth = 0;
    for (var i = 0, len = topblocks.length; i < len; i++) {
      var blk = topblocks[i];
      var blkXY = blk.getRelativeToSurfaceXY();
      var blockHW = blk.getHeightWidth();
      var blkHgt = blockHW.height;
      var blkWidth = blockHW.width;
      switch (layout) {
        case Blockly.BLKS_HORIZONTAL:
          if (x < wsRight) {
            blk.moveBy(x - blkXY.x, y - blkXY.y);
            blk.select();
            x += blkWidth + SPACER;
            if (blkHgt > maxHgt) // Remember highest block
              maxHgt = blkHgt;
          } else {
            y += maxHgt + SPACER;
            maxHgt = blkHgt;
            x = viewLeft;
            blk.moveBy(x - blkXY.x, y - blkXY.y);
            blk.select();
            x += blkWidth + SPACER;
          }
          break;
        case Blockly.BLKS_VERTICAL:
          if (y < wsBottom) {
            blk.moveBy(x - blkXY.x, y - blkXY.y);
            blk.select();
            y += blkHgt + SPACER;
            if (blkWidth > maxWidth)  // Remember widest block
              maxWidth = blkWidth;
          } else {
            x += maxWidth + SPACER;
            maxWidth = blkWidth;
            y = viewTop;
            blk.moveBy(x - blkXY.x, y - blkXY.y);
            blk.select();
            y += blkHgt + SPACER;
          }
          break;
      }
    }
  }

  // Sort by Category.
  var sortOptionCat = {enabled: (Blockly.workspace_arranged_type !== Blockly.BLKS_CATEGORY)};
  sortOptionCat.text = Blockly.MSG_SORT_C;
  sortOptionCat.callback = function() {
    Blockly.workspace_arranged_type = Blockly.BLKS_CATEGORY;
    rearrangeWorkspace();
  };
  options.push(sortOptionCat);

  // Called after a sort or collapse/expand to redisplay blocks.
  function rearrangeWorkspace() {
    //default arrangement position set to Horizontal if it hasn't been set yet (is null)
    if (Blockly.workspace_arranged_latest_position === null || Blockly.workspace_arranged_latest_position === Blockly.BLKS_HORIZONTAL)
      arrangeOptionH.callback();
    else if (Blockly.workspace_arranged_latest_position === Blockly.BLKS_VERTICAL)
      arrangeOptionV.callback();
  }

  // Option to get help.
  var helpOption = {enabled: false};
  helpOption.text = Blockly.MSG_HELP;
  helpOption.callback = function() {};
  options.push(helpOption);

  Blockly.ContextMenu.show(xy, options);
};

/**
 * reset arrangement state; to be called when blocks in the workspace change
 */
Blockly.resetWorkspaceArrangements = function(){
  // reset the variables used for menus, but keep the latest position, so the current horizontal or
  // vertical state can be kept
  Blockly.workspace_arranged_type = null;
  Blockly.workspace_arranged_position = null;
};

/**
 * Cancel the native context menu, unless the focus is on an HTML input widget.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.onContextMenu_ = function(e) {
  if (!Blockly.isTargetInput_(e) && Blockly.ContextMenu) {
    // When focused on an HTML text input widget, don't cancel the context menu.
    e.preventDefault();
  }
};

/**
 * Close tooltips, context menus, dropdown selections, etc.
 * @param {boolean=} opt_allowToolbox If true, don't close the toolbox.
 */
Blockly.hideChaff = function(opt_allowToolbox) {
  Blockly.Tooltip && Blockly.Tooltip.hide();
  Blockly.ContextMenu && Blockly.ContextMenu.hide();
  Blockly.FieldDropdown && Blockly.FieldDropdown.hide();
  Blockly.FieldFlydown && Blockly.FieldFlydown.hide(); // [lyn, 10/06/13] for handling parameter & procedure flydowns
  Blockly.WidgetDiv.hide();
  //if (!opt_allowToolbox &&
  //    Blockly.Toolbox.flyout_ && Blockly.Toolbox.flyout_.autoClose) {
  //Blockly.Toolbox.clearSelection();
  //}
  Blockly.TypeBlock && Blockly.TypeBlock.hide();
};

/**
 * Deselect any selections on the webpage.
 * Chrome will select text outside the SVG when double-clicking.
 * Deselect this text, so that it doesn't mess up any subsequent drag.
 */
Blockly.removeAllRanges = function() {
  if (window.getSelection) {  // W3
    var sel = window.getSelection();
    if (sel && sel.removeAllRanges) {
      sel.removeAllRanges();
      window.setTimeout(function() {
          window.getSelection().removeAllRanges();
        }, 0);
    }
  }
};

/**
 * Is this event targeting a text input widget?
 * @param {!Event} e An event.
 * @return {boolean} True if text input.
 * @private
 */
Blockly.isTargetInput_ = function(e) {
  return e.target.type == 'textarea' || e.target.type == 'text';
};

/**
 * Load an audio file.  Cache it, ready for instantaneous playing.
 * @param {!Array.<string>} filenames List of file types in decreasing order of
 *   preference (i.e. increasing size).  E.g. ['media/go.mp3', 'media/go.wav']
 *   Filenames include path from Blockly's root.  File extensions matter.
 * @param {string} name Name of sound.
 * @private
 */
Blockly.loadAudio_ = function(filenames, name) {
  if (!window.Audio || !filenames.length) {
    // No browser support for Audio.
    return;
  }
  var sound;
  var audioTest = new window.Audio();
  for (var i = 0; i < filenames.length; i++) {
    var filename = filenames[i];
    var ext = filename.match(/\.(\w+)$/);
    if (ext && audioTest.canPlayType('audio/' + ext[1])) {
      // Found an audio format we can play.
      sound = new window.Audio(Blockly.pathToBlockly + filename);
      break;
    }
  }
  // To force the browser to load the sound, play it, but at nearly zero volume.
  if (sound && sound.play) {
    sound.play();
    sound.volume = 0.01;
    Blockly.SOUNDS_[name] = sound;
  }
};

/**
 * Play an audio file at specified value.  If volume is not specified,
 * use full volume (1).
 * @param {string} name Name of sound.
 * @param {?number} opt_volume Volume of sound (0-1).
 */
Blockly.playAudio = function(name, opt_volume) {
  var sound = Blockly.SOUNDS_[name];
  if (sound) {
    var mySound;
    var ie9 = goog.userAgent.DOCUMENT_MODE &&
              goog.userAgent.DOCUMENT_MODE === 9;
    if (ie9 || goog.userAgent.IPAD || goog.userAgent.ANDROID) {
      // Creating a new audio node causes lag in IE9, Android and iPad. Android
      // and IE9 refetch the file from the server, iPad uses a singleton audio
      // node which must be deleted and recreated for each new audio tag.
      mySound = sound;
    } else {
      mySound = sound.cloneNode();
    }
    mySound.volume = (opt_volume === undefined ? 1 : opt_volume);
    mySound.play();
  }
};

/**
 * Set the mouse cursor to be either a closed hand or the default.
 * @param {boolean} closed True for closed hand.
 * @private
 */
Blockly.setCursorHand_ = function(closed) {
  if (Blockly.readOnly) {
    return;
  }
  /* Hotspot coordinates are baked into the CUR file, but they are still
     required due to a Chrome bug.
     http://code.google.com/p/chromium/issues/detail?id=1446 */
  var cursor = '';
  if (closed) {
    cursor = 'url(' + Blockly.pathToBlockly + 'media/handclosed.cur) 7 3, auto';
  }
  if (Blockly.selected) {
    Blockly.selected.getSvgRoot().style.cursor = cursor;
  }
  // Set cursor on the SVG surface as well as block so that rapid movements
  // don't result in cursor changing to an arrow momentarily.
  Blockly.svg.style.cursor = cursor;
};

/**
 * Return an object with all the metrics required to size scrollbars for the
 * main workspace.  The following properties are computed:
 * .viewHeight: Height of the visible rectangle,
 * .viewWidth: Width of the visible rectangle,
 * .contentHeight: Height of the contents,
 * .contentWidth: Width of the content,
 * .viewTop: Offset of top edge of visible rectangle from parent,
 * .viewLeft: Offset of left edge of visible rectangle from parent,
 * .contentTop: Offset of the top-most content from the y=0 coordinate,
 * .contentLeft: Offset of the left-most content from the x=0 coordinate.
 * .absoluteTop: Top-edge of view.
 * .absoluteLeft: Left-edge of view.
 * @return {Object} Contains size and position metrics of main workspace.
 * @private
 */
Blockly.getMainWorkspaceMetrics_ = function() {
  var svgSize = Blockly.svgSize();
  //We don't use Blockly.Toolbox in our version of Blockly instead we use drawer.js
  //svgSize.width -= Blockly.Toolbox.width;  // Zero if no Toolbox.
  svgSize.width -= 0;  // Zero if no Toolbox.
  var viewWidth = svgSize.width - Blockly.Scrollbar.scrollbarThickness;
  var viewHeight = svgSize.height - Blockly.Scrollbar.scrollbarThickness;
  try {
    var blockBox = Blockly.mainWorkspace.getCanvas().getBBox();
  } catch (e) {
    // Firefox has trouble with hidden elements (Bug 528969).
    return null;
  }
  if (Blockly.mainWorkspace.scrollbar) {
    // Add a border around the content that is at least half a screenful wide.
    var leftEdge = Math.min(blockBox.x - viewWidth / 2,
                            blockBox.x + blockBox.width - viewWidth);
    var rightEdge = Math.max(blockBox.x + blockBox.width + viewWidth / 2,
                             blockBox.x + viewWidth);
    var topEdge = Math.min(blockBox.y - viewHeight / 2,
                           blockBox.y + blockBox.height - viewHeight);
    var bottomEdge = Math.max(blockBox.y + blockBox.height + viewHeight / 2, blockBox.y + viewHeight);
  } else {
    var leftEdge = blockBox.x;
    var rightEdge = leftEdge + blockBox.width;
    var topEdge = blockBox.y;
    var bottomEdge = topEdge + blockBox.height;
  }
  //We don't use Blockly.Toolbox in our version of Blockly instead we use drawer.js
  //var absoluteLeft = Blockly.RTL ? 0 : Blockly.Toolbox.width;
  var absoluteLeft = Blockly.RTL ? 0 : 0;
  return {
    viewHeight: svgSize.height,
    viewWidth: svgSize.width,
    contentHeight: bottomEdge - topEdge,
    contentWidth: rightEdge - leftEdge,
    viewTop: -Blockly.mainWorkspace.scrollY,
    viewLeft: -Blockly.mainWorkspace.scrollX,
    contentTop: topEdge,
    contentLeft: leftEdge,
    absoluteTop: 0,
    absoluteLeft: absoluteLeft
  };
};

/**
 * Sets the X/Y translations of the main workspace to match the scrollbars.
 * @param {!Object} xyRatio Contains an x and/or y property which is a float
 *     between 0 and 1 specifying the degree of scrolling.
 * @private
 */
Blockly.setMainWorkspaceMetrics_ = function(xyRatio) {
  if (!Blockly.mainWorkspace.scrollbar) {
    throw 'Attempt to set main workspace scroll without scrollbars.';
  }
  var metrics = Blockly.getMainWorkspaceMetrics_();
  if (goog.isNumber(xyRatio.x)) {
    Blockly.mainWorkspace.scrollX = -metrics.contentWidth * xyRatio.x -
        metrics.contentLeft;
  }
  if (goog.isNumber(xyRatio.y)) {
    Blockly.mainWorkspace.scrollY = -metrics.contentHeight * xyRatio.y -
        metrics.contentTop;
  }
  var translation = 'translate(' +
      (Blockly.mainWorkspace.scrollX + metrics.absoluteLeft) + ',' +
      (Blockly.mainWorkspace.scrollY + metrics.absoluteTop) + ')';
  Blockly.mainWorkspace.getCanvas().setAttribute('transform', translation);
  Blockly.mainWorkspace.getBubbleCanvas().setAttribute('transform',
                                                       translation);
};

/**
 * When something in Blockly's workspace changes, call a function.
 * @param {!Function} func Function to call.
 * @return {!Array.<!Array>} Opaque data that can be passed to
 *     removeChangeListener.
 */
Blockly.addChangeListener = function(func) {
  return Blockly.bindEvent_(Blockly.mainWorkspace.getCanvas(),
                            'blocklyWorkspaceChange', null, func);
};

/**
 * Stop listening for Blockly's workspace changes.
 * @param {!Array.<!Array>} bindData Opaque data from addChangeListener.
 */
Blockly.removeChangeListener = function(bindData) {
  Blockly.unbindEvent_(bindData);
};
