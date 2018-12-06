// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * App Inventor extensions to Blockly's SVG Workspace
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.WorkspaceSvg');

goog.require('Blockly.WorkspaceSvg');

/**
 * AI2 Blocks Drawer
 * @type {Blockly.Drawer}
 * @private
 */
Blockly.WorkspaceSvg.prototype.drawer_ = null;

/**
 * The workspace's backpack (if any).
 * @type {Blockly.Backpack}
 * @private
 */
Blockly.WorkspaceSvg.prototype.backpack_ = null;

/**
 * The workspace's component database.
 * @type {Blockly.ComponentDatabase}
 * @private
 */
Blockly.WorkspaceSvg.prototype.componentDb_ = null;

/**
 * The workspace's typeblock instance.
 * @type {Blockly.TypeBlock}
 * @private
 */
Blockly.WorkspaceSvg.prototype.typeBlock_ = null;

/**
 * Shared flydown for parameters and variables.
 * @type {Blockly.Flydown}
 * @private
 */
Blockly.WorkspaceSvg.prototype.flydown_ = null;

/**
 * A list of blocks that need rendering the next time the workspace is shown.
 * @type {?Array.<Blockly.BlockSvg>}
 */
Blockly.WorkspaceSvg.prototype.blocksNeedingRendering = null;

/**
 * latest clicked position is used to open the type blocking suggestions window
 * Initial position is 0,0
 * @type {{x: number, y: number}}
 */
Blockly.WorkspaceSvg.prototype.latestClick = { x: 0, y: 0 };

/**
 * Wrap the onMouseClick_ event to handle additional behaviors.
 */
Blockly.WorkspaceSvg.prototype.onMouseDown_ = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var f = function(e) {
      try {
        var metrics = Blockly.mainWorkspace.getMetrics();
        var point = Blockly.utils.mouseToSvg(e, this.getParentSvg(), this.getInverseScreenCTM());
        point.x = (point.x + metrics.viewLeft) / this.scale;
        point.y = (point.y + metrics.viewTop) / this.scale;
        this.latestClick = point;
        return func.call(this, e);
      } finally {
        // focus the workspace's parent typeblocking and other keystrokes
        this.getTopWorkspace().getParentSvg().parentNode.focus();
        //if drawer exists and supposed to close
        if (this.drawer_ && this.drawer_.flyout_.autoClose) {
          this.drawer_.hide();
        }
        if (this.backpack_ && this.backpack_.flyout_.autoClose) {
          this.backpack_.hide();
        }

        //Closes mutators
        var blocks = this.getAllBlocks();
        var numBlocks = blocks.length;
        var temp_block = null;
        for(var i = 0; i < numBlocks; i++) {
          temp_block = blocks[i];
          if(temp_block.mutator){
            //deselect block in mutator workspace
            if(Blockly.selected && Blockly.selected.workspace && Blockly.selected.workspace!=Blockly.mainWorkspace){
              Blockly.selected.unselect();
            }
            blocks[i].mutator.setVisible(false);
          }
        }
      }
    };
    f.isWrapper = true;
    return f;
  }
})(Blockly.WorkspaceSvg.prototype.onMouseDown_);

Blockly.WorkspaceSvg.prototype.createDom = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var f = function() {
      return func.apply(this, Array.prototype.slice.call(arguments));
    };
    f.isWrapper = true;
    return f;
  }
})(Blockly.WorkspaceSvg.prototype.createDom);

Blockly.WorkspaceSvg.prototype.dispose = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var wrappedFunc = function() {
      func.call(this);
      if (this.backpack_) {
        this.backpack_.dispose();
        return null;
      }
    };
    wrappedFunc.isWrapped = true;
    return wrappedFunc;
  }
})(Blockly.WorkspaceSvg.prototype.dispose);

/**
 * Add the warning handler.
 */
Blockly.WorkspaceSvg.prototype.addWarningHandler = function() {
  if (!this.warningHandler_) {
    this.warningHandler_ = new Blockly.WarningHandler(this);
  }
};

/**
 * Adds the warning indicator.
 */
Blockly.WorkspaceSvg.prototype.addWarningIndicator = function() {
  if (!this.options.readOnly && this.warningIndicator_ == null) {
    if (!this.warningHandler_) {
      this.warningHandler_ = new Blockly.WarningHandler(this);
    }
    this.warningIndicator_ = new Blockly.WarningIndicator(this);
    var svgWarningIndicator = this.warningIndicator_.createDom();
    this.svgGroup_.appendChild(svgWarningIndicator);
    this.warningIndicator_.init();
  }
};

/**
 * Add a backpack.
 */
Blockly.WorkspaceSvg.prototype.addBackpack = function() {
  if (Blockly.Backpack && !this.options.readOnly) {
    this.backpack_ = new Blockly.Backpack(this, {scrollbars: true, media: './assets/'});
    var svgBackpack = this.backpack_.createDom(this);
    this.svgGroup_.appendChild(svgBackpack);
    this.backpack_.init();
  }
};

/**
 * Handle backpack rescaling
 */
Blockly.WorkspaceSvg.prototype.setScale = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var wrappedFunction = function(newScale) {
      func.call(this, newScale);
      if (this.backpack_) {
        this.backpack_.flyout_.reflow();
      }
    };
    wrappedFunction.isWrapped = true;
    return wrappedFunction;
  }
})(Blockly.WorkspaceSvg.prototype.setScale);

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
/**
 * Hide the blocks drawer.
 */
Blockly.WorkspaceSvg.prototype.hideDrawer = function() {
  if (this.drawer_)
    this.drawer_.hide();
  return this;
};

/**
 * Show the blocks drawer for the built-in category.
 */
Blockly.WorkspaceSvg.prototype.showBuiltin = function(name) {
  if (this.drawer_)
    this.drawer_.showBuiltin(name);
  return this;
};

/**
 * Show the drawer with generic blocks for a component type.
 */
Blockly.WorkspaceSvg.prototype.showGeneric = function(name) {
  if (this.drawer_)
    this.drawer_.showGeneric(name);
  return this;
};

/**
 * Show the drawer for a component instance.
 */
Blockly.WorkspaceSvg.prototype.showComponent = function(component) {
  if (this.drawer_)
    this.drawer_.showComponent(component);
  return this;
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
/**
 * Check whether the drawer is showing.
 */
Blockly.WorkspaceSvg.prototype.isDrawerShowing = function() {
  if (this.drawer_) {
    return this.drawer_.isShowing();
  } else {
    return false;
  }
};

/**
 * Render the workspace.
 * @param {Array.<Blockly.BlockSvg>=} blocks
 */
// Override Blockly's render with optimized version from lyn
Blockly.WorkspaceSvg.prototype.render = function(blocks) {
  this.rendered = true;
  this.bulkRendering = true;
  // In bulk rendering mode, the database accepts all new connections to be
  // sorted later. If we don't clear this, then we can end up with multiple
  // entries in the DB for the same connection. This isn't necessarily bad,
  // but it may decrease performance.
  this.connectionDBList.forEach(function (db) {
    db.length = 0;  // clear the databases
  });
  Blockly.Field.startCache();
  try {
    if (Blockly.Instrument.isOn) {
      var start = new Date().getTime();
    }
    // [lyn, 04/08/14] Get both top and all blocks for stats
    var topBlocks = blocks || this.getTopBlocks(/* ordered */ false);
    var allBlocks = this.getAllBlocks();
    if (Blockly.Instrument.useRenderDown) {
      for (var t = 0, topBlock; topBlock = topBlocks[t]; t++) {
        Blockly.Instrument.timer(
          function () {
            topBlock.renderDown();
          },
          function (result, timeDiffInner) {
            Blockly.Instrument.stats.renderDownTime += timeDiffInner;
          }
        );
      }
    } else {
      for (var x = 0, block; block = allBlocks[x]; x++) {
        if (!block.getChildren().length) {
          block.render();
        }
      }
    }
    if (Blockly.Instrument.isOn) {
      var stop = new Date().getTime();
      var timeDiffOuter = stop - start;
      Blockly.Instrument.stats.blockCount = allBlocks.length;
      Blockly.Instrument.stats.topBlockCount = topBlocks.length;
      Blockly.Instrument.stats.workspaceRenderCalls++;
      Blockly.Instrument.stats.workspaceRenderTime += timeDiffOuter;
    }
  } finally {
    this.bulkRendering = false;
    this.requestConnectionDBUpdate();
    Blockly.Field.stopCache();  // must balance with startCache() call above
  }
};

/**
 * Obtain the {@link Blockly.ComponentDatabase} associated with the workspace.
 *
 * @returns {!Blockly.ComponentDatabase}
 */
Blockly.WorkspaceSvg.prototype.getComponentDatabase = function() {
  return this.componentDb_;
};

/**
 * Obtain the {@link Blockly.ProcedureDatabase} associated with the workspace.
 * @returns {!Blockly.ProcedureDatabase}
 */
Blockly.WorkspaceSvg.prototype.getProcedureDatabase = function() {
  return this.procedureDb_;
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
/**
 * Add a new component to the workspace.
 *
 * @param {string} uid
 * @param {string} instanceName
 * @param {string} typeName
 * @returns {Blockly.WorkspaceSvg} The workspace for call chaining.
 */
Blockly.WorkspaceSvg.prototype.addComponent = function(uid, instanceName, typeName) {
  if (this.componentDb_.addInstance(uid, instanceName, typeName)) {
    this.typeBlock_.needsReload.components = true;
  }
  return this;
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
/**
 * Remove a component from the workspace.
 *
 * @param {string} uid The component's unique identifier
 * @returns {Blockly.WorkspaceSvg} The workspace for call chaining.
 */
Blockly.WorkspaceSvg.prototype.removeComponent = function(uid) {
  var component = this.componentDb_.getInstance(uid);

  // Fixes #1175
  if (this.drawer_ && component.name === this.drawer_.lastComponent) {
    this.drawer_.hide();
  }

  if (!this.componentDb_.removeInstance(uid)) {
    return this;
  }
  this.typeBlock_.needsReload.components = true;
  var blocks = this.getAllBlocks();
  for (var i = 0, block; block = blocks[i]; ++i) {
    if (block.category == 'Component'
        && block.getFieldValue('COMPONENT_SELECTOR') == component.name) {
      block.dispose(true);
    }
  }
  Blockly.hideChaff();
  return this;
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
/**
 * Rename a component in the workspace.
 *
 * @param {!string} uid The unique identifier of the component.
 * @param {!string} oldName The previous name of the component.
 * @param {!string} newName The new name of the component.
 * @returns {Blockly.WorkspaceSvg} The workspace for call chaining.
 */
Blockly.WorkspaceSvg.prototype.renameComponent = function(uid, oldName, newName) {
  if (!this.componentDb_.renameInstance(uid, oldName, newName)) {
    console.log('Renaming: No such component instance ' + oldName + '; aborting.');
    return this;
  }
  this.typeBlock_.needsReload.components = true;
  var blocks = this.getAllBlocks();
  for (var i = 0, block; block = blocks[i]; ++i) {
    if (block.category == 'Component' && block.rename(oldName, newName)) {
      this.blocksNeedingRendering.push(block);
    }
  }
  Blockly.hideChaff();
  return this;
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java.
/**
 * Populate the component type database with the components encoded by
 * strComponentInfos.
 *
 * @param {string} strComponentInfos String containing JSON-encoded
 * @param {Object.<string, string>} translations Translation dictionary provided by GWT
 * component information.
 */
Blockly.WorkspaceSvg.prototype.populateComponentTypes = function(strComponentInfos, translations) {
  this.componentDb_.populateTypes(JSON.parse(strComponentInfos));
  this.componentDb_.populateTranslations(translations);
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java.
/**
 * Loads the contents of a blocks file into the workspace.
 *
 * @param {!string} formJson JSON string containing structure of the Form
 * @param {!string} blocksContent XML serialization of the blocks
 * @returns {Blockly.WorkspaceSvg} The workspace for call chaining.
 */
Blockly.WorkspaceSvg.prototype.loadBlocksFile = function(formJson, blocksContent) {
  if (blocksContent.length != 0) {
    try {
      Blockly.Events.disable();
      if (Blockly.Versioning.upgrade(formJson, blocksContent, this)) {
        var self = this;
        setTimeout(function() {
          self.fireChangeListener(new AI.Events.ForceSave(self));
        });
      }
    } finally {
      Blockly.Events.enable();
    }
    if (this.getCanvas() != null) {
      this.render();
    }
  }
  return this;
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
/**
 * Verifies all of the blocks on the workspace and adds error icons if
 * any problems are identified.
 *
 * @returns {Blockly.WorkspaceSvg} The workspace for call chaining.
 */
Blockly.WorkspaceSvg.prototype.verifyAllBlocks = function() {
  var blocks = this.getAllBlocks();
  for (var i = 0, block; block = blocks[i]; ++i) {
    if (block.category == 'Component') {
      block.verify();
    }
  }
  return this;
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
/**
 * Saves the workspace as an XML file and returns the contents as a
 * string.
 *
 * @returns {string} XML serialization of the workspace's blocks.
 */
Blockly.WorkspaceSvg.prototype.saveBlocksFile = function() {
  return Blockly.SaveFile.get(this);
};

/**
 * Generate the YAIL for the blocks workspace.
 *
 * @param {string} formJson
 * @param {string} packageName
 * @param {boolean=false} opt_repl
 * @returns String containing YAIL to be sent to the phone.
 */
Blockly.WorkspaceSvg.prototype.getFormYail = function(formJson, packageName, opt_repl) {
  return Blockly.Yail.getFormYail(formJson, packageName, !!opt_repl, this);
};

/**
 * Get the warning handler for the workspace.
 * @returns {Blockly.WarningHandler}
 */
Blockly.WorkspaceSvg.prototype.getWarningHandler = function() {
  if (!this.warningHandler_) {
    this.warningHandler_ = new Blockly.WarningHandler(this);
  }
  return this.warningHandler_;
};

/**
 * Get the warning indicator UI element.
 * @returns {Blockly.WarningIndicator}
 */
Blockly.WorkspaceSvg.prototype.getWarningIndicator = function() {
  return this.warningIndicator_;
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
Blockly.WorkspaceSvg.prototype.exportBlocksImageToUri = function(cb) {
  Blockly.ExportBlocksImage.getUri(cb, this);
};

Blockly.WorkspaceSvg.prototype.getFlydown = function() {
  return this.flydown_;
};

Blockly.WorkspaceSvg.prototype.hideChaff = function(opt_allowToolbox) {
  this.flydown_ && this.flydown_.hide();
  this.typeBlock_ && this.typeBlock_.hide();
  if (!opt_allowToolbox) {  // Fixes #1269
    this.backpack_ && this.backpack_.hide();
  }
  this.setScrollbarsVisible(true);
};

Blockly.WorkspaceSvg.prototype.activate = function() {
  Blockly.mainWorkspace = this;
};

Blockly.WorkspaceSvg.prototype.buildComponentMap = function(warnings, errors, forRepl, compileUnattachedBlocks) {
  var map = {components: {}, globals: []};
  var blocks = this.getTopBlocks(/* ordered */ true);
  for (var i = 0, block; block = blocks[i]; ++i) {
    if (block.type == 'procedures_defnoreturn' || block.type == 'procedures_defreturn' || block.type == 'global_declaration') {
      map.globals.push(block);
    } else if (block.category == 'Component' && block.type == 'event') {
      var instanceName = block.instanceName;
      if (!map.components[instanceName]) {
        map.components[instanceName] = [];
      }
      map.components[instanceName].push(block);
    }
  }
  return map;
};

Blockly.WorkspaceSvg.prototype.resize = (function(resize) {
  return function() {
    resize.call(this);
    if (this.warningIndicator_ && this.warningIndicator_.position_) {
      this.warningIndicator_.position_();
    }
    if (this.backpack_ && this.backpack_.position_) {
      this.backpack_.position_();
    }
    return this;
  };
})(Blockly.WorkspaceSvg.prototype.resize);

Blockly.WorkspaceSvg.prototype.customContextMenu = function(menuOptions) {
  var self = this;
  function addResetArrangements(callback) {
    return function() {
      try {
        callback.call();
      } finally {
        self.resetArrangements();
      }
    };
  }
  function instrument(callback) {
    return function() {
      Blockly.Instrument.initializeStats('expandAllCollapsedBlocks');
      Blockly.Instrument.timer(
        function() { callback.call(self); },
        function(result, timeDiff) {
          Blockly.Instrument.stats.totalTime = timeDiff;
          Blockly.Instrument.displayStats('expandAllCollapsedBlocks');
        });
    };
  }
  for (var i = 0; i < menuOptions.length; ++i) {
    if (menuOptions[i].text == Blockly.Msg.COLLAPSE_ALL) {
      menuOptions[i].callback = addResetArrangements(menuOptions[i].callback);
    } else if (menuOptions[i].text == Blockly.Msg.EXPAND_ALL) {
      menuOptions[i].callback = instrument(addResetArrangements(menuOptions[i].callback));
    }
  }

  var exportOption = {enabled: true};
  exportOption.text = Blockly.Msg.EXPORT_IMAGE;
  exportOption.callback = function() {
    Blockly.ExportBlocksImage.onclickExportBlocks(Blockly.getMainWorkspace().getMetrics());
  };
  menuOptions.splice(3, 0, exportOption);

  // Arrange blocks in row order.
  var arrangeOptionH = {enabled: (Blockly.workspace_arranged_position !== Blockly.BLKS_HORIZONTAL)};
  arrangeOptionH.text = Blockly.Msg.ARRANGE_H;
  arrangeOptionH.callback = function(opt_type) {
    opt_type = opt_type instanceof goog.events.Event ? null : opt_type;
    arrangeBlocks(opt_type? opt_type : Blockly.workspace_arranged_type, Blockly.BLKS_HORIZONTAL);
  };
  menuOptions.push(arrangeOptionH);

  // Arrange blocks in column order.
  var arrangeOptionV = {enabled: (Blockly.workspace_arranged_position !== Blockly.BLKS_VERTICAL)};
  arrangeOptionV.text = Blockly.Msg.ARRANGE_V;
  arrangeOptionV.callback = function(opt_type) {
    opt_type = opt_type instanceof goog.events.Event ? null : opt_type;
    arrangeBlocks(opt_type? opt_type : Blockly.workspace_arranged_type, Blockly.BLKS_VERTICAL);
  };
  menuOptions.push(arrangeOptionV);

  /**
   * Function that returns a name to be used to sort blocks.
   * The general comparator is the block.category attribute.
   * In the case of 'Components' the comparator is the instanceName of the component if it exists
   * (it does not exist for generic components).
   * In the case of Procedures the comparator is the NAME(for definitions) or PROCNAME (for calls)
   * @param {!Blockly.Block} block the block that will be compared in the sortByCategory function
   * @returns {string} text to be used in the comparison
   */
  function comparisonName(block){
    if (block.category === 'Component' && block.instanceName)
      return block.instanceName;
    if (block.category === 'Procedures')
      return (block.getFieldValue('NAME') || block.getFieldValue('PROCNAME'));
    return block.category;
  }

  /**
   * Function used to sort blocks by Category.
   * @param {!Blockly.Block} a first block to be compared
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
  function arrangeBlocks(type, layout) {
    Blockly.Events.setGroup(true);  // group these movements together
    // start arrangement
    var workspaceId = Blockly.mainWorkspace.id;
    Blockly.Events.fire(new AI.Events.StartArrangeBlocks(workspaceId));
    Blockly.workspace_arranged_type = type;
    Blockly.workspace_arranged_position = layout;
    Blockly.workspace_arranged_latest_position = layout;
    var event = new AI.Events.EndArrangeBlocks(workspaceId, type, layout);
    var SPACER = 25;
    var topblocks = Blockly.mainWorkspace.getTopBlocks(/* ordered */ false);
    // If the blocks are arranged by Category, sort the array
    if (Blockly.workspace_arranged_type === Blockly.BLKS_CATEGORY){
      topblocks.sort(sortByCategory);
    }
    var metrics = Blockly.mainWorkspace.getMetrics();
    var spacing = Blockly.mainWorkspace.options.gridOptions.spacing;
    var spacingInv = 1 / spacing;
    var snap = Blockly.mainWorkspace.options.gridOptions.snap ?
      function(x) { return (Math.ceil(x * spacingInv) - .5) * spacing; } : function(x) { return x; };
    var viewLeft = snap(metrics.viewLeft + 5);
    var viewTop = snap(metrics.viewTop + 5);
    var x = viewLeft;
    var y = viewTop;
    var wsRight = viewLeft + metrics.viewWidth / Blockly.mainWorkspace.scale;
    var wsBottom = viewTop + metrics.viewHeight / Blockly.mainWorkspace.scale;
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
          x = snap(x + blkWidth + SPACER);
          if (blkHgt > maxHgt) // Remember highest block
            maxHgt = blkHgt;
        } else {
          y = snap(y + maxHgt + SPACER);
          maxHgt = blkHgt;
          x = viewLeft;
          blk.moveBy(x - blkXY.x, y - blkXY.y);
          blk.select();
          x = snap(x + blkWidth + SPACER);
        }
        break;
      case Blockly.BLKS_VERTICAL:
        if (y < wsBottom) {
          blk.moveBy(x - blkXY.x, y - blkXY.y);
          blk.select();
          y = snap(y + blkHgt + SPACER);
          if (blkWidth > maxWidth)  // Remember widest block
            maxWidth = blkWidth;
        } else {
          x = snap(x + maxWidth + SPACER);
          maxWidth = blkWidth;
          y = viewTop;
          blk.moveBy(x - blkXY.x, y - blkXY.y);
          blk.select();
          y = snap(y + blkHgt + SPACER);
        }
        break;
      }
    }
    Blockly.Events.fire(event);  // end arrangement
    Blockly.Events.setGroup(false);
    setTimeout(function() {
      Blockly.workspace_arranged_type = type;
      Blockly.workspace_arranged_position = layout;
      Blockly.workspace_arranged_latest_position = layout;
    });  // need to run after all events have run
  }

  // Sort by Category.
  var sortOptionCat = {enabled: (Blockly.workspace_arranged_type !== Blockly.BLKS_CATEGORY)};
  sortOptionCat.text = Blockly.Msg.SORT_C;
  sortOptionCat.callback = function() {
    rearrangeWorkspace(Blockly.BLKS_CATEGORY);
  };
  menuOptions.push(sortOptionCat);

  // Called after a sort or collapse/expand to redisplay blocks.
  function rearrangeWorkspace(opt_type) {
    //default arrangement position set to Horizontal if it hasn't been set yet (is null)
    if (Blockly.workspace_arranged_latest_position === null || Blockly.workspace_arranged_latest_position === Blockly.BLKS_HORIZONTAL)
      arrangeOptionH.callback(opt_type);
    else if (Blockly.workspace_arranged_latest_position === Blockly.BLKS_VERTICAL)
      arrangeOptionV.callback(opt_type);
  }

  // Enable all blocks
  var enableAll = {enabled: true};
  enableAll.text = Blockly.Msg.ENABLE_ALL_BLOCKS;
  enableAll.callback = function() {
    var allBlocks = Blockly.mainWorkspace.getAllBlocks();
    Blockly.Events.setGroup(true);
    for (var x = 0, block; block = allBlocks[x]; x++) {
      block.setDisabled(false);
    }
    Blockly.Events.setGroup(false);
  };
  menuOptions.push(enableAll);

  // Disable all blocks
  var disableAll = {enabled: true};
  disableAll.text = Blockly.Msg.DISABLE_ALL_BLOCKS;
  disableAll.callback = function() {
    var allBlocks = Blockly.mainWorkspace.getAllBlocks();
    Blockly.Events.setGroup(true);
    for (var x = 0, block; block = allBlocks[x]; x++) {
      block.setDisabled(true);
    }
    Blockly.Events.setGroup(false);
  };
  menuOptions.push(disableAll);

  // Retrieve from backpack option.
  var backpackRetrieve = {enabled: true};
  backpackRetrieve.text = Blockly.Msg.BACKPACK_GET + " (" +
    Blockly.getMainWorkspace().getBackpack().count() + ")";
  backpackRetrieve.callback = function() {
    if (Blockly.getMainWorkspace().hasBackpack()) {
      Blockly.getMainWorkspace().getBackpack().pasteBackpack();
    }
  };
  menuOptions.push(backpackRetrieve);

  // Copy all blocks to backpack option.
  var backpackCopyAll = {enabled: true};
  backpackCopyAll.text = Blockly.Msg.COPY_ALLBLOCKS;
  backpackCopyAll.callback = function() {
    if (Blockly.getMainWorkspace().hasBackpack()) {
      Blockly.getMainWorkspace().getBackpack().addAllToBackpack();
    }
  };
  menuOptions.push(backpackCopyAll);

  // Clear backpack.
  var backpackClear = {enabled: true};
  backpackClear.text = Blockly.Msg.BACKPACK_EMPTY;
  backpackClear.callback = function() {
    if (Blockly.getMainWorkspace().hasBackpack()) {
      Blockly.getMainWorkspace().getBackpack().clear();
    }
    backpackRetrieve.text = Blockly.Msg.BACKPACK_GET;
  };
  menuOptions.push(backpackClear);

  // Enable grid
  var gridOption = {enabled: true};
  gridOption.text = this.options.gridOptions['enabled'] ? Blockly.Msg.DISABLE_GRID :
    Blockly.Msg.ENABLE_GRID;
  gridOption.callback = function() {
    self.options.gridOptions['enabled'] = !self.options.gridOptions['enabled'];
    self.options.gridOptions['snap'] = self.options.gridOptions['enabled'] && top.BlocklyPanel_getSnapEnabled();
    if (self.options.gridOptions['enabled']) {
      // add grid
      self.svgBackground_.setAttribute('style', 'fill: url(#' + self.options.gridPattern.id + ');');
    } else {
      // remove grid
      self.svgBackground_.setAttribute('style', 'fill: white;');
    }
    if (top.BlocklyPanel_setGridEnabled) {
      top.BlocklyPanel_setGridEnabled(self.options.gridOptions['enabled']);
      top.BlocklyPanel_saveUserSettings();
    }
  };
  menuOptions.push(gridOption);

  if (this.options.gridOptions['enabled']) {
    // Enable Snapping
    var snapOption = {enabled: this.options.gridOptions['enabled']};
    snapOption.text = this.options.gridOptions['snap'] ? Blockly.Msg.DISABLE_SNAPPING :
      Blockly.Msg.ENABLE_SNAPPING;
    snapOption.callback = function() {
      self.options.gridOptions['snap'] = !self.options.gridOptions['snap'];
      if (top.BlocklyPanel_setSnapEnabled) {
        top.BlocklyPanel_setSnapEnabled(self.options.gridOptions['enabled']);
        top.BlocklyPanel_saveUserSettings();
      }
    };
    menuOptions.push(snapOption);
  }

  // Option to get help.
  var helpOption = {enabled: false};
  helpOption.text = Blockly.Msg.HELP;
  helpOption.callback = function() {};
  menuOptions.push(helpOption);
};

Blockly.WorkspaceSvg.prototype.recordDeleteAreas = function() {
  if (this.trashcan) {
    this.deleteAreaTrash_ = this.trashcan.getClientRect();
  } else {
    this.deleteAreaTrash_ = null;
  }
  if (this.isMutator) {
    if (this.flyout_) {
      this.deleteAreaToolbox_ = this.flyout_.getClientRect();
    } else if (this.toolbox_) {
      this.deleteAreaToolbox_ = this.toolbox_.getClientRect();
    } else {
      this.deleteAreaToolbox_ = null;
    }
  } else {
    this.deleteAreaToolbox_ = null;
  }
};

Blockly.WorkspaceSvg.prototype.getBackpack = function() {
  return this.backpack_;
};

Blockly.WorkspaceSvg.prototype.hasBackpack = function() {
  return this.backpack_ != null;
};

Blockly.WorkspaceSvg.prototype.onMouseWheel_ = function(e) {
  Blockly.terminateDrag_();
  if (e.eventPhase == 3) {
    if (e.ctrlKey == true) {
      // multi-touch pinch gesture
      if (e.deltaY == 0) {
        // Multi-stage wheel movement triggers jumpy zoom-in then zoom-out behavior
        e.preventDefault();
        return;
      }
      var delta = e.deltaY > 0 ? -1 : 1;
      var position = Blockly.utils.mouseToSvg(e, this.getParentSvg(),
        this.getInverseScreenCTM());
      this.zoom(position.x, position.y, delta);
    } else {
      // pan using mouse wheel
      this.scrollX -= e.deltaX;
      this.scrollY -= e.deltaY;
      this.updateGridPattern_();
      if (this.scrollbar) {
        // can only pan if scrollbars exist
        this.scrollbar.resize();
      } else {
        this.translate(this.scrollX, this.scrollY);
      }
    }
    e.preventDefault();
  }
};

Blockly.WorkspaceSvg.prototype.setGridSettings = function(enabled, snap) {
  this.options.gridOptions['enabled'] = enabled;
  this.options.gridOptions['snap'] = enabled && snap;
  if (this.svgBackground_) {
    if (this.options.gridOptions['enabled']) {
      // add grid
      this.svgBackground_.setAttribute('style', 'fill: url(#' + this.options.gridPattern.id + ');');
    } else {
      // remove grid
      this.svgBackground_.setAttribute('style', 'fill: white;');
    }
  }
};

/**
 * Builds a map of component name -> top level blocks for that component.
 * A special entry for "globals" maps to top-level global definitions.
 *
 * @param warnings a Map that will be filled with warnings for troublesome blocks
 * @param errors a list that will be filled with error messages
 * @param forRepl whether this is executed for REPL
 * @param compileUnattachedBlocks whether to compile unattached blocks
 * @returns object mapping component names to the top-level blocks for that component in the
 *            workspace. For each component C the object contains a field "component.C" whose
 *            value is an array of blocks. In addition, the object contains a field named "globals"
 *            whose value is an array of all valid top-level blocks not associated with a
 *            component (procedure and variable definitions)
 */
Blockly.WorkspaceSvg.prototype.buildComponentMap = function(warnings, errors, forRepl, compileUnattachedBlocks) {
  var map = {};
  map.components = {};
  map.globals = [];

  // TODO: populate warnings, errors as we traverse the top-level blocks

  var blocks = this.getTopBlocks(true);
  for (var x = 0, block; block = blocks[x]; x++) {

    // TODO: deal with unattached blocks that are not valid top-level definitions. Valid blocks
    // are events, variable definitions, or procedure definitions.

    if (!block.category) {
      continue;
    }
    if (block.type == 'procedures_defnoreturn' || block.type == 'procedures_defreturn' || block.type == 'global_declaration') {
      map.globals.push(block);
      // TODO: eventually deal with variable declarations, once we have them
    } else if (block.category == 'Component') {
      var instanceName = block.instanceName;
      if(block.blockType != "event") {
        continue;
      }
      if (!map.components[instanceName]) {
        map.components[instanceName] = [];  // first block we've found for this component
      }

      // TODO: check for duplicate top-level blocks (e.g., two event handlers with same name) -
      // or better yet, prevent these from happening!

      map.components[instanceName].push(block);
    }
  }
  return map;
};

/**
 * Get the topmost workspace in the workspace hierarchy.
 * @returns {Blockly.WorkspaceSvg}
 */
Blockly.WorkspaceSvg.prototype.getTopWorkspace = function() {
  var parent = this;
  while (parent.targetWorkspace) {
    parent = parent.targetWorkspace;
  }
  return parent;
};

Blockly.WorkspaceSvg.prototype.fireChangeListener = function(event) {
  Blockly.WorkspaceSvg.superClass_.fireChangeListener.call(this, event);
  if (event instanceof Blockly.Events.Move) {
    // Reset arrangement parameters
    Blockly.workspace_arranged_latest_position = null;
    Blockly.workspace_arranged_position = null;
    Blockly.workspace_arranged_type = null;
    var oldParent = this.blockDB_[event.oldParentId],
      block = this.blockDB_[event.blockId];
    oldParent && this.requestErrorChecking(oldParent);
    block && this.requestErrorChecking(block);
  }
};

/**
 * Request a re-render the workspace. If <code>block</code> is provided, only descendants of
 * <code>block</code>'s top-most block will be rendered. This may be called multiple times to queue
 * many blocks to be rendered.
 * @param {Blockly.BlockSvg=} block
 */
Blockly.WorkspaceSvg.prototype.requestRender = function(block) {
  if (!this.pendingRender) {
    this.needsRendering = [];
    this.pendingBlockIds = {};
    this.pendingRender = setTimeout(function() {
      try {
        this.render(this.needsRendering.length === 0 ? undefined : this.needsRendering);
      } finally {
        this.pendingRender = null;
      }
    }.bind(this));
  }
  if (block) {
    // Rendering uses Blockly.BlockSvg.renderDown, so we only need a list of the topmost blocks
    while (block.getParent()) {
      block = /** @type {Blockly.BlockSvg} */ block.getParent();
    }
    if (!(block.id in this.pendingBlockIds)) {
      this.pendingBlockIds[block.id] = true;
      this.needsRendering.push(block);
    }
  }
};

/**
 * Request error checking on the specified block. This will queue error checking events until the
 * next time the JavaScript thread relinquishes control to the UI thread.
 * @param {Blockly.BlockSvg=} block
 */
Blockly.WorkspaceSvg.prototype.requestErrorChecking = function(block) {
  if (!this.warningHandler_) {
    return;  // no error checking before warning handler exists
  }
  if (this.checkAllBlocks) {
    return;  // already planning to check all blocks
  }
  if (!this.pendingErrorCheck) {
    this.needsErrorCheck = [];
    this.pendingErrorBlockIds = {};
    this.checkAllBlocks = !!block;
    this.pendingErrorCheck = setTimeout(function() {
      try {
        var handler = this.getWarningHandler();
        if (handler) {  // not true for flyouts and before the main workspace is rendered.
          goog.array.forEach(this.checkAllBlocks ? this.getAllBlocks() : this.needsErrorCheck,
            function(block) {
              handler.checkErrors(block);
            });
        }
      } finally {
        this.pendingErrorCheck = null;
        this.checkAllBlocks = false;
        // Let any disposed blocks be GCed...
        this.needsErrorCheck = null;
        this.pendingErrorBlockIds = null;
      }
    }.bind(this));
  }
  if (block && !(block.id in this.pendingErrorBlockIds)) {
    while (block.getParent()) {
      block = /** @type {Blockly.BlockSvg} */ block.getParent();
    }
    var pendingBlocks = [block];
    while (pendingBlocks.length > 0) {
      block = pendingBlocks.shift();
      if (!(block.id in this.pendingErrorBlockIds)) {
        this.pendingErrorBlockIds[block.id] = true;
        this.needsErrorCheck.push(block);
      }
      Array.prototype.push.apply(pendingBlocks, block.getChildren());
    }
  } else if (!block) {
    // schedule all blocks
    this.checkAllBlocks = true;
  }
};

/**
 * Sort the workspace's connection database. This only needs to be called if the bulkRendering
 * property of the workspace is set to true to false as any connections that Blockly attempted to
 * update during that time may be incorrectly ordered in the database.
 */
Blockly.WorkspaceSvg.prototype.sortConnectionDB = function() {
  goog.array.forEach(this.connectionDBList, function(connectionDB) {
    connectionDB.sort(function(a, b) {
      return a.y_ - b.y_;
    });
    // If we are rerendering due to a new error, we only redraw the error block, which means that
    // we can't clear the database, otherwise all other connections disappear. Instead, we add
    // the moved connections anyway, and at this point we can remove the duplicate entries in the
    // database. We remove after sorting so that the operation is O(n) rather than O(n^2). This
    // assumption may break in the future if Blockly decides on a different mechanism for indexing
    // connections.
    connectionDB.removeDupes();
  });
};

/**
 * Request an update to the connection database's order due to movement of a block while a bulk
 * rendering operation was in progress.
 */
Blockly.WorkspaceSvg.prototype.requestConnectionDBUpdate = function() {
  if (!this.pendingConnectionDBUpdate) {
    this.pendingConnectionDBUpdate = setTimeout(function() {
      try {
        this.sortConnectionDB();
      } finally {
        this.pendingConnectionDBUpdate = null;
      }
    }.bind(this));
  }
};
