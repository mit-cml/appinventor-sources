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

goog.require('AI.Blockly.ExportBlocksImage');
goog.require('AI.Blockly.SaveFile');
goog.require('AI.Blockly.Versioning');
goog.require('AI.Blockly.WarningHandler');
goog.require('AI.Blockly.WarningIndicator');
goog.require('AI.Blockly.Workspace');

/**
 * AI2 Blocks Drawer
 * @type {Blockly.Drawer}
 * @private
 */
Blockly.WorkspaceSvg.prototype.drawer_ = null;

/**
 * The workspace's backpack (if any).
 * @type {AI.Blockly.Backpack}
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
 * @type {AI.Blockly.TypeBlock}
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
 * Whether the workspace elements are hidden
 * @type {boolean}
 */
Blockly.WorkspaceSvg.prototype.chromeHidden = false;

/**
 * Wrap the onMouseClick_ event to handle additional behaviors.
 */
Blockly.WorkspaceSvg.prototype.onMouseDown_ = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var f = function(e) {
      var metrics = Blockly.common.getMainWorkspace().getMetrics();
      var point = Blockly.utils.browserEvents.mouseToSvg(e, this.getParentSvg(), this.getInverseScreenCTM());
      point.x = (point.x + metrics.viewLeft) / this.scale;
      point.y = (point.y + metrics.viewTop) / this.scale;
      this.latestClick = point;
      return func.call(this, e);
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
      var self = /** @type {Blockly.WorkspaceSvg} */ (this);
      var result = func.apply(this, Array.prototype.slice.call(arguments));
      // BEGIN: Configure drag and drop of blocks images to workspace
      result.addEventListener('dragenter', function(e) {
        if (e.dataTransfer.types.indexOf('Files') >= 0 ||
            e.dataTransfer.types.indexOf('text/uri-list') >= 0) {
          self.svgBackground_.style.fill = 'rgba(0, 255, 0, 0.3)';
          e.dataTransfer.dropEffect = 'copy';
          e.preventDefault();
        }
      }, true);
      result.addEventListener('dragover', function(e) {
        if (e.dataTransfer.types.indexOf('Files') >= 0 ||
            e.dataTransfer.types.indexOf('text/uri-list') >= 0) {
          self.svgBackground_.style.fill = 'rgba(0, 255, 0, 0.3)';
          e.dataTransfer.dropEffect = 'copy';
          e.preventDefault();
        }
      }, true);
      result.addEventListener('dragleave', function(e) {
        self.setGridSettings(self.options.gridOptions['enabled'], self.getGrid().shouldSnap());
      }, true);
      result.addEventListener('dragexit', function(e) {
        self.setGridSettings(self.options.gridOptions['enabled'], self.getGrid().shouldSnap());
      }, true);
      result.addEventListener('drop', function(e) {
        self.setGridSettings(self.options.gridOptions['enabled'], self.getGrid().shouldSnap());
        if (e.dataTransfer.types.indexOf('Files') >= 0) {
          if (e.dataTransfer.files.item(0).type === 'image/png') {
            e.preventDefault();
            var metrics = Blockly.common.getMainWorkspace().getMetrics();
            var point = Blockly.utils.browserEvents.mouseToSvg(e, self.getParentSvg(), self.getInverseScreenCTM());
            point.x = (point.x + metrics.viewLeft) / self.scale;
            point.y = (point.y + metrics.viewTop) / self.scale;
            Blockly.importPngAsBlock(self, point, e.dataTransfer.files.item(0));
          }
        } else if (e.dataTransfer.types.indexOf('text/uri-list') >= 0) {
          var data = e.dataTransfer.getData('text/uri-list')
          if (data.match(/\.png$/)) {
            e.preventDefault();
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
              if (xhr.readyState === 4 && xhr.status === 200) {
                var metrics = Blockly.common.getMainWorkspace().getMetrics();
                var point = Blockly.utils.browserEvents.mouseToSvg(e, self.getParentSvg(), self.getInverseScreenCTM());
                point.x = (point.x + metrics.viewLeft) / self.scale;
                point.y = (point.y + metrics.viewTop) / self.scale;
                Blockly.importPngAsBlock(self, point, xhr.response);
              }
            };
            xhr.responseType = 'blob';
            xhr.open('GET', data, true);
            xhr.send();
          }
        }
      });
      // END: Configure drag and drop of blocks images to workspace
      return result;
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
  if (AI.Blockly.Backpack && !this.options.readOnly) {
    this.backpack_ = new AI.Blockly.Backpack(this, {
        scrollbars: true,
        media: './assets/',
        disabledPatternId: this.options.disabledPatternId,
        renderer: 'geras2_renderer',
      });
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
/*
Blockly.WorkspaceSvg.prototype.render = function(blocks) {
  this.rendered = true;
  this.bulkRendering = true;
  Blockly.utils.dom.startTextWidthCache();
  try {
    if (Blockly.Instrument.isOn) {
      var start = new Date().getTime();
    }
    // [lyn, 04/08/14] Get both top and all blocks for stats
    var topBlocks = blocks || this.getTopBlocks(/* ordered * / false);
    var allBlocks = this.getAllBlocks();
    if (Blockly.Instrument.useRenderDown) {
      for (var t = 0, topBlock; topBlock = topBlocks[t]; t++) {
        Blockly.Instrument.timer(
          function () {
            topBlock.render(false);
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
    Blockly.utils.dom.stopTextWidthCache();
  }
};
*/

/**
 * Obtain the {@link Blockly.ComponentDatabase} associated with the workspace.
 *
 * @returns {!Blockly.ComponentDatabase}
 */
Blockly.WorkspaceSvg.prototype.getComponentDatabase = function() {
  if (this.targetWorkspace) {
    return this.targetWorkspace.getComponentDatabase();
  }
  return this.componentDb_;
};

/**
 * Obtain the {@link Blockly.ProcedureDatabase} associated with the workspace.
 * @returns {!Blockly.ProcedureDatabase}
 */
Blockly.WorkspaceSvg.prototype.getProcedureDatabase = function() {
  if (this.targetWorkspace) {
    return this.targetWorkspace.getProcedureDatabase();
  }
  return this.procedureDb_;
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
/**
 * Adds a screen name to the list tracked by the workspace.
 * @param {string} name The name of the new screen.
 */
Blockly.WorkspaceSvg.prototype.addScreen = function(name) {
  if (this.targetWorkspace) {
    return this.targetWorkspace.addScreen(name);
  }
  if (this.screenList_.indexOf(name) == -1) {
    this.screenList_.push(name);
    this.typeBlock_.needsReload.screens = true;
  }
};

//noinspection JSUnusedGlobalSymbols Called from BlocklyPanel.java
/**
 * Removes a screen name from the list tracked by the workspace.
 * @param {string} name The name of the screen to remove.
 */
Blockly.WorkspaceSvg.prototype.removeScreen = function(name) {
  if (this.targetWorkspace) {
    return this.targetWorkspace.removeScreen(name);
  }
  var index = this.screenList_.indexOf(name);
  if (index != -1) {
    this.screenList_.splice(index, 1);
    this.typeBlock_.needsReload.screens = true;
  }
}

/**
 * Returns the list of screen names tracked by the workspace.
 * @return {!Array<string>} The list of screen names.
 */
Blockly.WorkspaceSvg.prototype.getScreenList = function() {
  if (this.targetWorkspace) {
    return this.targetWorkspace.getScreenList();
  }
  return this.screenList_;
};

/**
 * Adds an asset name to the list tracked by the workspace.
 * @param {string} name The name of the new asset.
 */
Blockly.WorkspaceSvg.prototype.addAsset = function(name) {
  if (this.targetWorkspace) {
    return this.targetWorkspace.addAsset(name);
  }
  if (!this.assetList_.includes(name)) {
    this.assetList_.push(name);
    this.typeBlock_.needsReload.assets = true;
  }
};

/**
 * Removes an asset name from the list tracked by the workspace.
 * @param {string} name The name of the asset to remove.
 */
Blockly.WorkspaceSvg.prototype.removeAsset = function(name) {
  if (this.targetWorkspace) {
    return this.targetWorkspace.removeAsset(name);
  }
  var index = this.assetList_.indexOf(name);
  if (index != -1) {  // Make sure it is actually an asset.
    this.assetList_.splice(index, 1);
    this.typeBlock_.needsReload.assets = true;
  }
};

/**
 * Returns the list of asset names tracked by the workspace.
 * @return {!Array<string>} The list of asset names.
 */
Blockly.WorkspaceSvg.prototype.getAssetList = function() {
  if (this.targetWorkspace) {
    return this.targetWorkspace.getAssetList();
  }
  return this.assetList_;
}

/**
 * Returns the list of provider/models tracked by the workspace.
 * @return {!Array<string>} The list of asset names.
 */
Blockly.WorkspaceSvg.prototype.getProviderModelList = function() {
  if (this.targetWorkspace) {
    return this.targetWorkspace.getProviderModelList();
  }
  if(!top.chatproxyinfo) {      // This will happen if the proxy server doesn't respond
    return [];
  }
  var model_object_list = top.chatproxyinfo["model"];
  if (model_object_list) {
    return Object.keys(model_object_list);
  }
  return [];
}

/**
 * Returns the list of providers tracked by the workspace.
 * @return {!Array<string>} The list of asset names.
 */
Blockly.WorkspaceSvg.prototype.getProviderList = function() {
  if (this.targetWorkspace) {
    return this.targetWorkspace.getProviderList();
  }
  if(!top.chatproxyinfo) {      // This will happen if the proxy server doesn't respond
    return [];
  }
  var model_list = top.chatproxyinfo["provider"];
  if (model_list) {
    return model_list;
  }
  return [];
}

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
      this.isLoading = true;
      this.isClearing = true;
      if (Blockly.Versioning.upgrade(formJson, blocksContent, this)) {
        var self = this;
        setTimeout(function() {
          self.fireChangeListener(new AI.Events.ForceSave(self));
        });
      }
      this.getAllBlocks().forEach(function (block) {
        if (block.type == 'lexical_variable_set' || block.type == 'lexical_variable_get') {
          if (block.eventparam) {
            // Potentially apply any new translations for event parameter names
            var untranslatedEventName = block.eventparam;
            block.fieldVar_.setValue(untranslatedEventName);
            // block.fieldVar_.setText(block.workspace.getTopWorkspace().getComponentDatabase().getInternationalizedParameterName(untranslatedEventName));
            block.eventparam = untranslatedEventName;
            block.workspace.requestErrorChecking(block);
          }
        }
      });
    } finally {
      this.isLoading = false;
      this.isClearing = false;
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
 * @param {boolean} prettify Specify true if the resulting workspace should be pretty-printed.
 * @returns {string} XML serialization of the workspace's blocks.
 */
Blockly.WorkspaceSvg.prototype.saveBlocksFile = function(prettify) {
  return Blockly.SaveFile.get(prettify, this);
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
  return AI.Yail.getFormYail(formJson, packageName, !!opt_repl, this);
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
  AI.Blockly.ExportBlocksImage.getUri(cb, this);
};

Blockly.WorkspaceSvg.prototype.getFlydown = function() {
  return this.flydown_;
};

Blockly.WorkspaceSvg.prototype.hideChaff = (function(func) {
  return function(opt_allowToolbox) {
    this.flydown_ && this.flydown_.hide();
    this.typeBlock_ && this.typeBlock_.hide();
    if (!opt_allowToolbox) {  // Fixes #1269
      this.backpack_ && this.backpack_.hide();
    }
    if (this.scrollbar) {
      this.scrollbar.setContainerVisible(true);
    }
    func.apply(this, arguments);
  }
})(Blockly.WorkspaceSvg.prototype.hideChaff);

/**
 * Mark this workspace as the currently focused main workspace.
 *
 * This is the Blockly Core version extended to also reference targetWorkspace,
 * which is used by App Inventor.
 */
Blockly.WorkspaceSvg.prototype.markFocused = function() {
  if (this.options.parentWorkspace) {
    this.options.parentWorkspace.markFocused();
  } else if (this.targetWorkspace) {
    this.targetWorkspace.markFocused();
  } else {
    Blockly.common.setMainWorkspace(this);
  }
};

Blockly.WorkspaceSvg.prototype.buildComponentMap = function(warnings, errors, forRepl, compileUnattachedBlocks) {
  var map = {components: {}, globals: []};
  var blocks = this.getTopBlocks(/* ordered */ true);
  for (var i = 0, block; block = blocks[i]; ++i) {
    if (block.type == 'procedures_defnoreturn' || block.type == 'procedures_defreturn' || block.type == 'global_declaration') {
      map.globals.push(block);
    } else if (block.category == 'Component' && block.type == 'event') {
      if (block.isGeneric) {
        map.globals.push(block);
        continue;
      }
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
    return this;
  };
})(Blockly.WorkspaceSvg.prototype.resize);

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
  this.cancelCurrentGesture();
  if (e.eventPhase == 3) {
    if (e.ctrlKey == true) {
      // multi-touch pinch gesture
      if (e.deltaY == 0) {
        // Multi-stage wheel movement triggers jumpy zoom-in then zoom-out behavior
        e.preventDefault();
        return;
      }
      var delta = e.deltaY > 0 ? -1 : 1;
      var position = Blockly.utils.browserEvents.mouseToSvg(e, this.getParentSvg(),
        this.getInverseScreenCTM());
      this.zoom(position.x, position.y, delta);
    } else {
      // pan using mouse wheel
      this.scrollX -= e.deltaX;
      this.scrollY -= e.deltaY;
      if (this.grid) {
        this.grid.update(this.scale);
      }
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
  this.getGrid().setSnapToGrid(enabled && snap);
  if (this.svgBackground_) {
    if (this.options.gridOptions['enabled']) {
      // add grid
      this.svgBackground_.setAttribute('style', 'fill: url(#' + this.options.gridPattern.id + ');');
    } else {
      // remove grid
      const color = Blockly.common.getMainWorkspace().getTheme().componentStyles.workspaceBackgroundColour ?? "white";
      this.svgBackground_.setAttribute('style', `fill: ${color};`);
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
      if (block.isGeneric) {
        map.globals.push(block);
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
  Blockly.Workspace.prototype.fireChangeListener.call(this, event);
  if (event instanceof Blockly.Events.BlockMove) {
    const workspace = Blockly.Workspace.getById(event.workspaceId);
    if (event.group !== workspace.arrange_blocks_event_group_) {
      // Reset arrangement parameters if we're not in the middle of an rearrangement
      workspace.arranged_latest_position_ = null;
      workspace.arranged_position_ = null;
      workspace.arranged_type_ = null;
    }
    var oldParent = this.blockDB.get(event.oldParentId),
      block = this.blockDB.get(event.blockId);
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
    this.pendingRenderFunc = function() {
      try {
        this.render(this.needsRendering.length === 0 ? undefined : this.needsRendering);
      } finally {
        this.pendingRender = null;
      }
    }.bind(this);
    if (this.svgGroup_.parentElement.parentElement.parentElement.style.display === 'none') {
      this.pendingRender = true;
    } else {
      this.pendingRender = setTimeout(this.pendingRenderFunc, 0);
    }
  }
  if (block) {
    // Rendering uses Blockly.BlockSvg.renderDown, so we only need a list of the topmost blocks
    while (block.getParent()) {
      block = /** @type {Blockly.BlockSvg} */ (block.getParent());
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
      block = /** @type {Blockly.BlockSvg} */ (block.getParent());
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
/*
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
 */

/**
 * Request an update to the connection database's order due to movement of a block while a bulk
 * rendering operation was in progress.
 */
/*
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
*/

/*
* Refresh the state of the backpack. Called from BlocklyPanel.java
*/

Blockly.WorkspaceSvg.prototype.refreshBackpack = function() {
  if (this.backpack_) {
    this.backpack_.resize();
  }
};
