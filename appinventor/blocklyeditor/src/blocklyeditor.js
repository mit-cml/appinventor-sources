// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2012-2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Initialize the blocks editor workspace.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author sharon@google.com (Sharon Perl)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.BlocklyEditor');

goog.require('goog.ui.HsvaPalette');

// App Inventor extensions to Blockly
goog.require('AI.Blockly');
goog.require('AI.Blockly.Backpack');
goog.require('AI.Blockly.BlockSvg')
goog.require('AI.Blockly.ComponentDatabase');
goog.require('AI.Blockly.ConnectionDB');
goog.require('AI.Blockly.Drawer');
goog.require('AI.Blockly.ExportBlocksImage');
goog.require('AI.Blockly.Field');
goog.require('AI.Blockly.Flydown');
goog.require('AI.Blockly.ProcedureDatabase');
goog.require('AI.Blockly.ReplMgr');
goog.require('AI.Blockly.TypeBlock');
goog.require('AI.Blockly.VariableDatabase');
goog.require('AI.Blockly.Warning');
goog.require('AI.Blockly.WorkspaceSvg');
goog.require('AI.Events');

// App Inventor Blocks
goog.require('AI.Blocks.mutators');
goog.require('AI.Blocks.color');
goog.require('AI.Blocks.components');
goog.require('AI.Blocks.control');
goog.require('AI.Blocks.dictionaries');
goog.require('AI.Blocks.helpers');
goog.require('AI.Blocks.lexicalvariables');
goog.require('AI.Blocks.lists');
goog.require('AI.Blocks.logic');
goog.require('AI.Blocks.math');
goog.require('AI.Blocks.procedures');
goog.require('AI.Blocks.text');

// Make dragging a block from flyout work in any direction (default: 70)
Blockly.Flyout.prototype.dragAngleRange_ = 360;

if (Blockly.BlocklyEditor === undefined) {
  Blockly.BlocklyEditor = {};
}

Blockly.allWorkspaces = {};

Blockly.configForTypeBlock = {
  frame: 'ai_frame',
  typeBlockDiv: 'ai_type_block',
  inputText: 'ac_input_text'
};

Blockly.BlocklyEditor.HELP_IFRAME = null;

top.addEventListener('mousedown', function(e) {
  if (e.target.tagName === 'IMG' &&
      e.target.parentElement.tagName === 'A' &&
      e.target.parentElement.className === 'menu-help-item') {
    e.stopPropagation();
    if (Blockly.BlocklyEditor.HELP_IFRAME) {
      Blockly.BlocklyEditor.HELP_IFRAME.parentNode.removeChild(Blockly.BlocklyEditor.HELP_IFRAME);
      Blockly.BlocklyEditor.HELP_IFRAME = null;
    }
    if (e.shiftKey || e.metaKey) {
      return;
    }
    e.preventDefault();
    var div = document.createElement('div');
    div.style.width = '400px';
    div.style.height = '300px';
    div.style.resize = 'both';
    div.style.zIndex = '99999';
    div.style.border = '1px solid gray';
    div.style.boxSizing = 'border-box';
    div.style.position = 'absolute';
    div.style.top = e.clientY + 'px';
    div.style.left = e.clientX + 'px';
    div.style.backgroundColor = 'white';
    div.style.overflow = 'hidden';
    Blockly.BlocklyEditor.HELP_IFRAME = div;
    var iframe = document.createElement('iframe');
    iframe.src = e.target.parentElement.href;
    iframe.style.width = '100%';
    iframe.style.height = '100%';
    iframe.style.border = 'none';
    div.appendChild(iframe);
    Blockly.WidgetDiv.show(Blockly.BlocklyEditor, Blockly.common.getMainWorkspace().options.rtl, function() {
      Blockly.BlocklyEditor.HELP_IFRAME.parentNode.removeChild(Blockly.BlocklyEditor.HELP_IFRAME);
      Blockly.BlocklyEditor.HELP_IFRAME = null;
    });
    top.document.body.appendChild(div);
  }
  return true;
}, true);

Blockly.BlocklyEditor.makeMenuItemWithHelp = function(text, helpUrl) {
  var span = document.createElement("span");
  var a = document.createElement("a");
  var img = document.createElement('img');
  img.src = '/static/images/help.png';
  a.href = helpUrl;
  a.target = '_blank';
  a.className = 'menu-help-item';
  a.style.position = 'absolute';
  a.addEventListener('click', function(e) {
    if (!e.shiftKey && !e.metaKey) {
      e.preventDefault()
    }
  });
  a.appendChild(img);
  span.appendChild(document.createTextNode(text));
  span.appendChild(a);
  return span;
};

function unboundVariableHandler(myBlock, yailText) {
  var unbound_vars = Blockly.LexicalVariable.freeVariables(myBlock);
  unbound_vars = unbound_vars.toList();
  if (unbound_vars.length == 0) {
    try {
      AI.Yail.forRepl = true;
      Blockly.ReplMgr.putYail(yailText, myBlock);
    } finally {
      AI.Yail.forRepl = false;
    }
  } else {
    var form = "<form onsubmit='return false;'>" + Blockly.Msg.DIALOG_ENTER_VALUES + "<br>";
    for (var v in unbound_vars) {
      form  +=  unbound_vars[v] + ' = <input type=text name=' + unbound_vars[v] + '><br>';
    }
    form += "</form>";
    var dialog = new Blockly.Util.Dialog(Blockly.Msg.DIALOG_UNBOUND_VAR, form, Blockly.Msg.DO_IT, false, Blockly.Msg.REPL_CANCEL, 10, function (button) {
      if (button == Blockly.Msg.DO_IT) {
        var code = "(let (";
        for (var i in unbound_vars) {
          code += '($' + unbound_vars[i] + ' ' + AI.Yail.quotifyForREPL(document.querySelector('input[name="' + unbound_vars[i] + '"]').value) + ') ';
        }
        code += ")" + yailText + ")";
        try {
          AI.Yail.forRepl = true;
          Blockly.ReplMgr.putYail(code, myBlock);
        } finally {
          AI.Yail.forRepl = false;
        }
      }
      dialog.hide();
    });
  }
}

/**
 * Adds an option to the block's context menu to export it to a PNG.
 * @param {!Blockly.BlockSvg} myBlock The block to export to PNG.
 * @param {!Array<!Object>} options The option list to add to.
 */
Blockly.BlocklyEditor.addPngExportOption = function(myBlock, options) {
  var downloadBlockOption = {
    enabled: true,
    text: Blockly.BlocklyEditor.makeMenuItemWithHelp(
        Blockly.Msg.DOWNLOAD_BLOCKS_AS_PNG,
        '/reference/other/download-pngs.html'),
    callback: function() {
      Blockly.exportBlockAsPng(myBlock);
    }
  };
  // Add it above the help option.
  options.splice(options.length - 1, 0, downloadBlockOption);
};

/**
 * Adds an option to the block's context menu to generate its yail.
 * @param {!Blockly.BlockSvg} myBlock The block to generate yail for.
 * @param {!Array<!Object>} options The option list to add to.
 */
Blockly.BlocklyEditor.addGenerateYailOption = function(myBlock, options) {
  if (!window.parent.BlocklyPanel_checkIsAdmin()) {
    return;
  }

  // TODO: eventually create a separate kind of bubble for the generated yail,
  //  which can morph into the bubble for "do it" output once we hook
  //  up to the REPL.
  var yailOption = {enabled: !this.disabled};
  yailOption.text = Blockly.Msg.GENERATE_YAIL;
  yailOption.callback = function() {
    // AI.Yail.blockToCode1 returns a string if the block is a statement
    // and an array if the block is a value
    var yail = AI.Yail.blockToCode1(myBlock);
    myBlock.setCommentText((yail instanceof Array) ? yail[0] : yail);
  };

  options.push(yailOption);
};

/**
 * Adds an option to the block's context menu to execute the block.
 * @param {!Blockly.BlockSvg} myBlock The block to execute.
 * @param {!Array<!Object>} options The option list to add to.
 */
Blockly.BlocklyEditor.addDoItOption = function(myBlock, options) {
  var connectedToRepl =
      top.ReplState.state === Blockly.ReplMgr.rsState.CONNECTED;

  var doitOption = { enabled: !this.disabled && connectedToRepl};
  doitOption.text = Blockly.Msg.DO_IT;
  doitOption.callback = function() {
    if (!connectedToRepl) {
      var dialog = new goog.ui.Dialog(
          null, true, new goog.dom.DomHelper(top.document));
      dialog.setTitle(Blockly.Msg.CAN_NOT_DO_IT);
      dialog.setTextContent(Blockly.Msg.CONNECT_TO_DO_IT);
      dialog.setButtonSet(new goog.ui.Dialog.ButtonSet()
          .addButton(goog.ui.Dialog.ButtonSet.DefaultButtons.OK,
              false, true));
      dialog.setVisible(true);
    } else {
      // AI.Yail.blockToCode1 returns a string if the block is a statement
      // and an array if the block is a value
      var yail;
      try {
        AI.Yail.forRepl = true;
        yail = AI.Yail.blockToCode1(myBlock);
      } finally {
        AI.Yail.forRepl = false;
      }
      unboundVariableHandler(myBlock, (yail instanceof Array) ? yail[0] : yail);
    }
  };
  options.push(doitOption);
};

/**
 * Adds an option to the block's context menu to clear the result of a "Do It"
 * operation, if the result exists.
 * @param {!Blockly.BlockSvg} myBlock The block clean up.
 * @param {!Array<!Object>} options The option list to add to.
 */
Blockly.BlocklyEditor.addClearDoItOption = function(myBlock, options) {
  if (!myBlock.replError) {
    return;
  }
  var clearDoitOption = {enabled: true};
  clearDoitOption.text = Blockly.Msg.CLEAR_DO_IT_ERROR;
  clearDoitOption.callback = function() {
    myBlock.replError = null;
    Blockly.common.getMainWorkspace().getWarningHandler().checkErrors(myBlock);
  };
  options.push(clearDoitOption);
};

/**
 * Adds extra context menu options to all blocks. Current options include:
 *   - Png Export
 *   - Generate Yail (admin only)
 *   - Do It
 *   - Clear Do It (only if Do It is appended)
 * @this {!Blockly.BlockSvg}
 */
Blockly.Block.prototype.customContextMenu = function(options) {
  Blockly.BlocklyEditor.addPngExportOption(this, options);
  Blockly.BlocklyEditor.addGenerateYailOption(this, options);
  Blockly.BlocklyEditor.addDoItOption(this, options);
  Blockly.BlocklyEditor.addClearDoItOption(this, options);

  if(this.procCustomContextMenu){
    this.procCustomContextMenu(options);
  }
};

Blockly.Block.prototype.flyoutCustomContextMenu = function(menuOptions) {
  // Option for the backpack.
  if (this.workspace.isBackpack) {
    var id = this.id;
    var removeOption = {enabled: true};
    removeOption.text = Blockly.Msg.REMOVE_FROM_BACKPACK;
    var backpack = this.workspace.targetWorkspace.backpack_;
    removeOption.callback = function() {
      backpack.removeFromBackpack([id]);
    };
    menuOptions.splice(menuOptions.length - 1, 0, removeOption);
  }
};

goog.provide('AI.Blockly.ContextMenuItems');

AI.Blockly.ContextMenuItems.registerExportBlocksOption = function() {
  let menuItem = {
    id: 'appinventor_export_blocks',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function(scope) {
      return scope.workspace.getAllBlocks().length ? 'enabled' : 'hidden';
    },
    displayText: Blockly.Msg['EXPORT_IMAGE'],
    callback: function() {
      AI.Blockly.ExportBlocksImage.onclickExportBlocks(Blockly.common.getMainWorkspace().getMetrics());
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(menuItem);
};

AI.Blockly.ContextMenuItems.registerWorkspaceControlsOption = function() {
  let menuItem = {
    id: 'appinventor_workspaceControls',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function() {
      return 'enabled';
    },
    displayText: function(scope) {
      return scope.workspace.chromeHidden ? Blockly.Msg['SHOW'] : Blockly.Msg['HIDE'];
    },
    callback: function(scope) {
      let displayStyle = scope.workspace.chromeHidden ? 'block' : 'none';
      scope.workspace.backpack_.svgGroup_.style.display = displayStyle;
      scope.workspace.trashcan.svgGroup.style.display = displayStyle;
      scope.workspace.zoomControls_.svgGroup.style.display = displayStyle;
      scope.workspace.warningIndicator_.svgGroup_.style.display = displayStyle;
      scope.workspace.chromeHidden = !scope.workspace.chromeHidden;
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(menuItem);
};

AI.Blockly.ContextMenuItems.registerResetArrangementOptions = function() {
  function addResetArrangements(callback) {
    return function() {
      try {
        callback.call();
      } finally {
        self.resetArrangements();
      }
    };
  }

  let collapse = Blockly.ContextMenuRegistry.registry.getItem('collapseWorkspace');
  collapse.callback = addResetArrangements(collapse.callback);

  let expand = Blockly.ContextMenuRegistry.registry.getItem('expandWorkspace');
  expand.callback = addResetArrangements(expand.callback);
};

AI.Blockly.ContextMenuItems.registerArrangeOptions = function() {
  /**
   * Function that returns a name to be used to sort blocks.
   *
   * The general comparator is the <code>block.category</code> attribute.
   * In the case of Procedures the comparator is the NAME(for definitions) or PROCNAME (for calls)
   * In the case of 'Components' the comparator is the type name, instance name, then event name
   *
   * @param {!Blockly.Block} block the block that will be compared in the sortByCategory function
   * @returns {string} text to be used in the comparison
   */
  function comparisonName(block) {
    // Add trailing numbers to represent their sequence
    if (block.category == 'Variables') {
      return ('a,' + block.type + ',' + block.getVars().join(','));
    }
    if (block.category === 'Procedures') {
      // sort procedure definitions before calls
      if (block.type.indexOf('procedures_def') === 0) {
        return ('b,a:' + (block.getFieldValue('NAME') || block.getFieldValue('PROCNAME')));
      } else {
        return ('b,b:'+ (block.getFieldValue('NAME') || block.getFieldValue('PROCNAME')));
      }
    }
    if (block.category == 'Component') {
      let component = block.type + ',' + block.typeName + ','
        + (block.isGeneric ? '!GENERIC!' : block.instanceName) + ',';
      // sort Component blocks first, then events, methods, getters, or setters
      if (block.type == 'component_event') {
        component += block.eventName;
      } else if (block.type == 'component_method') {
        component += block.methodName;
      } else if (block.type == 'component_set_get') {
        component += block.setOrGet + block.propertyName;
      } else {
        // component blocks
        component += '.Component';
      }
      return ('c,' + component);
    }
    // Floating blocks that are not Component
    return ('d,' + block.type);
  }

  /**
   * Function used to compare two strings with text and numbers.
   *
   * @param {string} strA first string to be compared
   * @param {string} strB second string to be compared
   * @returns {number} returns 0 if the strings are equal, and -1 or 1 if they are not
   */
  function compareStrTextNum(strA, strB) {
    // Use Regular Expression to match text and numbers
    let regexStrA = strA.match(/^(.*?)([0-9]+)$/i);
    let regexStrB = strB.match(/^(.*?)([0-9]+)$/i);

    // There are numbers in the strings, compare numbers
    if (regexStrA && regexStrB) {
      if (regexStrA[1] < regexStrB[1]) {
        return -1;
      } else if (regexStrA[1] > regexStrB[1]) {
        return 1;
      } else {
        return parseInt(regexStrA[2]) - parseInt(regexStrB[2]);
      }
    } else {
      return strA.localeCompare(strB, undefined, {numeric:true});
    }
  }

  /**
   * Function used to sort blocks by Category.
   * @param {!Blockly.Block} a first block to be compared
   * @param {!Blockly.Block} b second block to be compared
   * @returns {number} returns 0 if the blocks are equal, and -1 or 1 if they are not
   */
  function sortByCategory(a,b) {
    let comparatorA = comparisonName(a).toLowerCase();
    let comparatorB = comparisonName(b).toLowerCase();

    if (a.category != b.category) {
      return comparatorA.localeCompare(comparatorB, undefined, {numeric:true});
    }

    // Sort by Category First, also handles other floating blocks
    if (a.category == b.category && a.category != "Component") {
      // Remove '1,'
      comparatorA = comparatorA.substr(2);
      comparatorB = comparatorB.substr(2);
      let res = compareStrTextNum(comparatorA, comparatorB);
      if (a.category == "Variables" && a.type == b.type) {
        // Sort Variables
        if (a.type == "global_declaration") {
          // initialize variables, extract just global variable names
          let nameA = a.getSvgRoot().textContent;
          // remove substring "initialize global<varname>to" and only keep <varname>
          nameA = nameA.substring(17, nameA.length - 2);
          let nameB = b.getSvgRoot().textContent;
          nameB = nameB.substring(17, nameB.length - 2);
          res = compareStrTextNum(nameA, nameB);
        } else {
          let nameA = a.fieldVar_.text_;
          let nameB = b.fieldVar_.text_;
          if (nameA.includes("global") && nameB.includes("global")) {
            // Global Variables and get variable names, remove "global"
            res = compareStrTextNum(nameA.substring(6), nameB.substring(6));
          }else {
            // Other floating variables
            res = compareStrTextNum(nameA, nameB);
          }
        }
      }
      return res;
    }

    // 3.Component event handlers, lexicographically sorted by
    // type name, instance name, then event name
    if (a.category == "Component" && b.category == "Component" && a.eventName && b.eventName) {
      if (a.typeName == b.typeName) {
        if (a.instanceName == b.instanceName) {
          return 0;
        } else if (!a.instanceName) {
          return -1;
        } else if (!b.instanceName) {
          return 1;
        }
        return compareStrTextNum(a.instanceName, b.instanceName);
      }
      return comparatorA.localeCompare(comparatorB, undefined, {numeric:true});
    }

    // 4. For Component blocks, sorted internally first by type,
    // whether they are generic (generics precede specifics),
    // then by instance name (for specific blocks),
    // then by method/property name.
    if (a.category == "Component" && b.category == "Component") {
      let geneA = ',2';
      if (a.isGeneric) {
        geneA = ',1';
      }

      let geneB = ',2';
      if (b.isGeneric) {
        geneB = ',1';
      }

      let componentA = a.type + geneA;
      let componentB = b.type + geneB;

      let res = componentA.localeCompare(componentB, undefined, {numeric:true});
      if (res === 0) {
        // compare type names
        res = compareStrTextNum(a.typeName, b.typeName);
      }
      //the comparator is the type name, instance name, then event name
      if (res === 0) {
        if (a.instanceName && b.instanceName) {
          res = compareStrTextNum(a.instanceName, b.instanceName);
        }
        // Compare property names
        let prop_method_A = a.propertyName || a.methodName;
        let prop_method_B = b.propertyName || b.methodName;
        res = prop_method_A.toLowerCase().localeCompare(prop_method_B.toLowerCase(), undefined, {numeric:true});
      }
      return res;
    }
  }

  // Arranges block in layout (Horizontal or Vertical).
  function arrangeBlocks(workspace, type, layout) {
    Blockly.Events.setGroup(true);  // group these movements together
    // start arrangement
    let workspaceId = workspace.id;
    Blockly.Events.fire(new AI.Events.StartArrangeBlocks(workspaceId));
    workspace.arranged_type_ = type;
    workspace.arranged_position_ = layout;
    workspace.arranged_latest_position_ = layout;
    let event = new AI.Events.EndArrangeBlocks(workspaceId, type, layout);
    let SPACER = 25;
    let topblocks = workspace.getTopBlocks(/* ordered */ false);
    // If the blocks are arranged by Category, sort the array
    if (workspace.arranged_type_ === AI.Blockly.BLKS_CATEGORY) {
      topblocks.sort(sortByCategory);
    }
    let metrics = workspace.getMetrics();
    let spacing = workspace.options.gridOptions.spacing;
    let spacingInv = 1 / spacing;
    let snap = workspace.options.gridOptions.snap ?
      function(x) { return (Math.ceil(x * spacingInv) - .5) * spacing; } : function(x) { return x; };
    let viewLeft = snap(metrics.viewLeft + 5);
    let viewTop = snap(metrics.viewTop + 5);
    let x = viewLeft;
    let y = viewTop;
    let wsRight = viewLeft + metrics.viewWidth / workspace.scale;
    let wsBottom = viewTop + metrics.viewHeight / workspace.scale;
    let maxHgt = 0;
    let maxWidth = 0;
    for (let i = 0, len = topblocks.length; i < len; i++) {
      let blk = topblocks[i];
      let blkXY = blk.getRelativeToSurfaceXY();
      let blockHW = blk.getHeightWidth();
      let blkHgt = blockHW.height;
      let blkWidth = blockHW.width;
      switch (layout) {
        case AI.Blockly.BLKS_HORIZONTAL:
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
        case AI.Blockly.BLKS_VERTICAL:
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
      workspace.arranged_type_ = type;
      workspace.arranged_latest_position_ = layout;
      workspace.arranged_position_ = layout;
    });  // need to run after all events have run
  }

  // Arrange blocks in row order.
  let arrangeOptionH = {
    id: 'appinventor_arrange_horizontal',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function(scope) {
      return scope.workspace.arranged_position_ !== AI.Blockly.BLKS_HORIZONTAL ? 'enabled' : 'hidden';
    },
    displayText: Blockly.Msg['ARRANGE_H'],
    callback: function(scope) {
      arrangeBlocks(scope.workspace, scope.workspace.arranged_type_, AI.Blockly.BLKS_HORIZONTAL);
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(arrangeOptionH);

  // Arrange blocks in column order.
  let arrangeOptionV = {
    id: 'appinventor_arrange_vertical',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function(scope) {
      return scope.workspace.arranged_position_ !== AI.Blockly.BLKS_VERTICAL ? 'enabled' : 'hidden';
    },
    displayText: Blockly.Msg['ARRANGE_V'],
    callback: function(scope) {
      arrangeBlocks(scope.workspace, scope.workspace.arranged_position_, AI.Blockly.BLKS_VERTICAL);
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(arrangeOptionV);

  // Called after a sort or collapse/expand to redisplay blocks.
  function rearrangeWorkspace(scope, opt_type) {
    //default arrangement position set to Horizontal if it hasn't been set yet (is null)
    if (scope.workspace.arranged_latest_position_ === null ||
        scope.workspace.arranged_latest_position_ === AI.Blockly.BLKS_HORIZONTAL) {
      arrangeOptionH.callback(scope, opt_type);
    } else if (scope.workspace.arranged_latest_position_ === AI.Blockly.BLKS_VERTICAL) {
      arrangeOptionV.callback(scope, opt_type);
    }
  }

  // Sort by Category.
  let sortOptionCat = {
    id: 'appinventor_sort_category',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function(scope) {
      return scope.workspace.arranged_type_ !== AI.Blockly.BLKS_CATEGORY ? 'enabled' : 'hidden';
    },
    displayText: Blockly.Msg['SORT_C'],
    callback: function(scope) {
      rearrangeWorkspace(scope, AI.Blockly.BLKS_CATEGORY);
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(sortOptionCat);
};

AI.Blockly.ContextMenuItems.registerEnableDisableAllBlocksOption = function() {
  // Enable all blocks
  let enableAll = {
    id: 'appinventor_enable_all_blocks',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function() {
      return 'enabled';
    },
    displayText: Blockly.Msg['ENABLE_ALL_BLOCKS'],
    callback: function(scope) {
      let allBlocks = scope.workspace.getAllBlocks();
      try {
        Blockly.Events.setGroup(true);
        allBlocks.forEach(block => block.setDisabled(false));
      } finally {
        Blockly.Events.setGroup(false);
      }
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(enableAll);

  // Disable all blocks
  let disableAll = {
    id: 'appinventor_disable_all_blocks',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function() {
      return 'enabled';
    },
    displayText: Blockly.Msg['DISABLE_ALL_BLOCKS'],
    callback: function(scope) {
      let allBlocks = scope.workspace.getAllBlocks();
      try {
        Blockly.Events.setGroup(true);
        allBlocks.forEach(block => block.setDisabled(true));
      } finally {
        Blockly.Events.setGroup(false);
      }
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(disableAll);
};

AI.Blockly.ContextMenuItems.registerCommentOptions = function() {

  // Show all comments
  let showAll = {
    id: 'appinventor_show_all_comments',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function() {
      return 'enabled';
    },
    displayText: Blockly.Msg['SHOW_ALL_COMMENTS'],
    callback: function(scope) {
      let allBlocks = scope.workspace.getAllBlocks();
      try {
        Blockly.Events.setGroup(true);
        allBlocks.forEach(block => {
          let comment = block.getIcon(Blockly.icons.CommentIcon.TYPE);
          if (comment && !block.isCollapsed()) {
            comment.setBubbleVisible(false);
          }
        })
      } finally {
        Blockly.Events.setGroup(false);
      }
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(showAll);

  // Hide all comments
  let hideAll = {
    id: 'appinventor_hide_all_comments',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function() {
      return 'enabled';
    },
    displayText: Blockly.Msg['HIDE_ALL_COMMENTS'],
    callback: function(scope) {
      let allBlocks = scope.workspace.getAllBlocks();
      try {
        Blockly.Events.setGroup(true);
        allBlocks.forEach(block => {
          let comment = block.getIcon(Blockly.icons.CommentIcon.TYPE);
          if (comment && !block.isCollapsed()) {
            comment.setBubbleVisible(true);
          }
        })
      } finally {
        Blockly.Events.setGroup(false);
      }
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(hideAll);
}

AI.Blockly.ContextMenuItems.registerBackpackOptions = function() {
  // Copy all blocks to backpack option.
  let backpackCopyAll = {
    id: 'appinventor_copy_all_blocks',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function(scope) {
      return scope.workspace.hasBackpack() ? 'enabled' : 'hidden';
    },
    displayText: Blockly.Msg['COPY_ALLBLOCKS'],
    callback: function(scope) {
      if (scope.workspace.hasBackpack()) {
        scope.workspace.getBackpack().addAllToBackpack();
      }
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(backpackCopyAll);

  // Retrieve from backpack option.
  let backpackRetrieve = {
    id: 'appinventor_retrieve_from_backpack',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function(scope) {
      return scope.workspace.hasBackpack() ? 'enabled' : 'hidden';
    },
    displayText: function(scope) {
      return Blockly.Msg['BACKPACK_GET'] + " (" +
          scope.workspace.getBackpack().count() + ")";
    },
    callback: function(scope) {
      if (scope.workspace.hasBackpack()) {
        scope.workspace.getBackpack().pasteBackpack();
      }
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(backpackRetrieve);
}

AI.Blockly.ContextMenuItems.registerGridOptions = function() {
  // Enable grid
  let gridOption = {
    id: 'appinventor_grid',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function() {
      return 'enabled';
    },
    displayText: function(scope) {
      return scope.workspace.options.gridOptions['enabled'] ?
        Blockly.Msg['DISABLE_GRID'] : Blockly.Msg['ENABLE_GRID']
    },
    callback: function(scope) {
      let grid = scope.workspace.options.gridOptions;
      grid['enabled'] = !grid['enabled'];
      grid['snap'] = grid['enabled'] && top.BlocklyPanel_getSnapEnabled();
      if (grid['enabled']) {
        // add grid
        Blockly.common.getMainWorkspace().svgBackground_.setAttribute('style', 'fill: url(#' + grid['pattern'].id + ');');
      } else {
        // remove grid
        Blockly.common.getMainWorkspace().svgBackground_.setAttribute('style', 'fill: white;');
      }
      if (top.BlocklyPanel_setGridEnabled) {
        top.BlocklyPanel_setGridEnabled(grid['enabled']);
        top.BlocklyPanel_saveUserSettings();
      }
    },
    weight: 100
  };
  Blockly.ContextMenuRegistry.registry.register(gridOption);

  // Enable snapping
  let snapOption = {
    id: 'appinventor_enable_snap',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function(scope) {
      return scope.workspace.options.gridOptions['enabled'] ? 'enabled' : 'hidden';
    },
    displayText: function(scope) {
      return scope.workspace.options.gridOptions['snap'] ?
        Blockly.Msg['DISABLE_SNAPPING'] : Blockly.Msg['ENABLE_SNAPPING'];
    },
    callback: function(scope) {
      let grid = scope.workspace.options.gridOptions;
      grid['snap'] = !grid['snap'];
      if (top.BlocklyPanel_setSnapEnabled) {
        top.BlocklyPanel_setSnapEnabled(grid['snap']);
        top.BlocklyPanel_saveUserSettings();
      }
    },
    weight: 100
  }
  Blockly.ContextMenuRegistry.registry.register(snapOption);
}

AI.Blockly.ContextMenuItems.registerHelpOption = function() {
  // Option to get help.
  let helpOption = {
    id: 'appinventor_help',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function() {
      return 'disabled';
    },
    displayText: Blockly.Msg['HELP'],
    callback: function() {
      window.open(Blockly.Msg['HELPURL']);
    },
    weight: 100
  }
};

AI.Blockly.ContextMenuItems.registerClearUnusedBlocksOption = function() {
  // Option to clear unused blocks.
  let clearOption = {
    id: 'appinventor_clear_unused_blocks',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.WORKSPACE,
    preconditionFn: function(scope) {
      return scope.workspace.getAllBlocks().length ? 'enabled' : 'hidden';
    },
    displayText: Blockly.Msg['REMOVE_UNUSED_BLOCKS'],
    callback: function(scope) {
      let allBlocks = scope.workspace.getTopBlocks();
      let removeList = [];
      allBlocks.forEach(block => {
        if (block.previousConnection || block.outputConnection) {
          removeList.push(block);
        }
      });
      if (removeList.length === 0) {
        return;
      }
      try {
        Blockly.Events.setGroup(true);
        scope.workspace.playAudio('delete');
        removeList.forEach(block => {
          block.dispose(false);
        })
      } finally {
        Blockly.Events.setGroup(false);
      }
    },
    weight: 100
  }
  Blockly.ContextMenuRegistry.registry.register(clearOption);
}

AI.Blockly.ContextMenuItems.registerAll = function() {
  AI.Blockly.ContextMenuItems.registerResetArrangementOptions();
  AI.Blockly.ContextMenuItems.registerExportBlocksOption();
  AI.Blockly.ContextMenuItems.registerWorkspaceControlsOption();
  AI.Blockly.ContextMenuItems.registerArrangeOptions();
  AI.Blockly.ContextMenuItems.registerEnableDisableAllBlocksOption();
  AI.Blockly.ContextMenuItems.registerCommentOptions();
  AI.Blockly.ContextMenuItems.registerBackpackOptions();
  AI.Blockly.ContextMenuItems.registerGridOptions();
  AI.Blockly.ContextMenuItems.registerHelpOption();
  AI.Blockly.ContextMenuItems.registerClearUnusedBlocksOption();
}

AI.Blockly.ContextMenuItems.registerAll();

/**
 * Create a new Blockly workspace but without initializing its DOM.
 * @param container The container that will host the Blockly workspace
 * @param formName The projectId_formName identifier used to name the workspace
 * @param readOnly True if the workspace should be created read-only
 * @param rtl True if the workspace is using a right-to-left language
 * @returns {Blockly.WorkspaceSvg} A newly created workspace
 */
Blockly.BlocklyEditor['create'] = function(container, formName, readOnly, rtl) {
  var options = {
    'toolbox': {
      'kind': 'flyoutToolbox',
      'contents': []
    },
    'readOnly': readOnly,
    'rtl': rtl,
    'collapse': true,
    'scrollbars': true,
    'trashcan': true,
    'comments': true,
    'disable': true,
    'media': './static/media/',
    'grid': {'spacing': '20', 'length': '5', 'snap': true, 'colour': '#ccc'},
    'zoom': {'controls': true, 'wheel': true, 'scaleSpeed': 1.1, 'maxScale': 3, 'minScale': 0.1},
    plugins: {
      blockDragger: top.MultiselectBlockDragger,
      metricsManager: top.ScrollMetricsManager,
    },
    baseBlockDragger: top.ScrollBlockDragger,
    useDoubleClick: true,
    bumpNeighbours: true,
    multiselectIcon: {
      hideIcon: false,
      enabledIcon: 'https://github.com/mit-cml/workspace-multiselect/raw/main/test/media/select.svg',
      disabledIcon: 'https://github.com/mit-cml/workspace-multiselect/raw/main/test/media/unselect.svg',
    },
    multiselectCopyPaste: {
      crossTab: true,
      menu: true,
    },
    renderer: 'geras2_renderer',
  };
  var workspace = Blockly.inject(container, options);
  var multiselectPlugin = new top.Multiselect(workspace);
  multiselectPlugin.init(options);
  var lexicalVariablesPlugin = top.LexicalVariablesPlugin;
  lexicalVariablesPlugin.init(workspace);
  var searchPlugin = new top.WorkspaceSearch(workspace);
  searchPlugin.init();
  Blockly.allWorkspaces[formName] = workspace;
  workspace.formName = formName;
  workspace.screenList_ = [];
  workspace.assetList_ = [];
  workspace.componentDb_ = new Blockly.ComponentDatabase();
  workspace.procedureDb_ = new Blockly.ProcedureDatabase(workspace);
  workspace.variableDb_ = new Blockly.VariableDatabase();
  workspace.blocksNeedingRendering = [];
  workspace.addWarningHandler();
  if (!readOnly) {
    var ai_type_block = goog.dom.createElement('div'),
      p = goog.dom.createElement('p'),
      ac_input_text = goog.dom.createElement('input'),
      typeblockOpts = {
        frame: container,
        typeBlockDiv: ai_type_block,
        inputText: ac_input_text
      };
    // build dom for typeblock (adapted from blocklyframe.html)
    goog.style.setElementShown(ai_type_block, false);
    goog.dom.classlist.add(ai_type_block, "ai_type_block");
    goog.dom.insertChildAt(container, ai_type_block, 0);
    goog.dom.appendChild(ai_type_block, p);
    goog.dom.appendChild(p, ac_input_text);
    workspace.typeBlock_ = new AI.Blockly.TypeBlock(typeblockOpts, workspace);
    var workspaceChanged = function() {
      if (this.workspace && !this.workspace.isDragging()) {
        var metrics = workspace.getMetrics();
        var edgeLeft = metrics.viewLeft + metrics.absoluteLeft;
        var edgeTop = metrics.viewTop + metrics.absoluteTop;
        if (metrics.contentTop < edgeTop ||
            metrics.contentTop + metrics.contentHeight > metrics.viewHeight + edgeTop ||
            metrics.contentLeft < (options.RTL ? metrics.viewLeft : edgeLeft) ||
            metrics.contentLeft + metrics.contentWidth > (options.RTL ?
                metrics.viewWidth : metrics.viewWidth + edgeLeft)) {
          // One or more blocks may be out of bounds.  Bump them back in.
          var MARGIN = 25;
          var blocks = workspace.getTopBlocks(false);
          for (var b = 0, block; block = blocks[b]; b++) {
            var blockXY = block.getRelativeToSurfaceXY();
            var blockHW = block.getHeightWidth();
            // Bump any block that's above the top back inside.
            var overflowTop = edgeTop + MARGIN - blockHW.height - blockXY.y;
            if (overflowTop > 0) {
              block.moveBy(0, overflowTop);
            }
            // Bump any block that's below the bottom back inside.
            var overflowBottom =
                edgeTop + metrics.viewHeight - MARGIN - blockXY.y;
            if (overflowBottom < 0) {
              block.moveBy(0, overflowBottom);
            }
            // Bump any block that's off the left back inside.
            var overflowLeft = MARGIN + edgeLeft -
                blockXY.x - (options.RTL ? 0 : blockHW.width);
            if (overflowLeft > 0) {
              block.moveBy(overflowLeft, 0);
            }
            // Bump any block that's off the right back inside.
            var overflowRight = edgeLeft + metrics.viewWidth - MARGIN -
                blockXY.x + (options.RTL ? blockHW.width : 0);
            if (overflowRight < 0) {
              block.moveBy(overflowRight, 0);
            }
          }
        }
      }
    };
    //workspace.addChangeListener(workspaceChanged);
  }
  workspace.drawer_ = new Blockly.Drawer(workspace, { scrollbars: true });
  workspace.flyout_ = workspace.getFlyout();
  workspace.addWarningIndicator();
  workspace.addBackpack();
  Blockly.browserEvents.bind(workspace.svgGroup_, 'focus', workspace, workspace.markFocused);
  // Hide scrollbars by default (otherwise ghost rectangles intercept mouse events)
  workspace.flyout_.scrollbar_ && workspace.flyout_.scrollbar_.setContainerVisible(false);
  workspace.backpack_.flyout_.scrollbar_ && workspace.backpack_.flyout_.scrollbar_.setContainerVisible(false);
  workspace.flydown_.scrollbar_ && workspace.flydown_.scrollbar_.setContainerVisible(false);
  // Render blocks created prior to the workspace being rendered.
  workspace.injecting = false;
  workspace.injected = true;
  return workspace;
};

/**
 * Inject a previously constructed workspace into the designated
 * container. This implementation is adapted from Blockly's
 * implementation and is required due to the fact that browsers such as
 * Firefox do not initialize SVG elements unless they are visible.
 *
 * @param {!Element|string} container
 * @param {!Blockly.WorkspaceSvg} workspace
 */
Blockly.ai_inject = function(container, workspace) {
  Blockly.common.setMainWorkspace(workspace);  // make workspace the 'active' workspace
  workspace.fireChangeListener(new AI.Events.ScreenSwitch(workspace.projectId, workspace.formName));
  var gridEnabled = top.BlocklyPanel_getGridEnabled && top.BlocklyPanel_getGridEnabled();
  var gridSnap = top.BlocklyPanel_getSnapEnabled && top.BlocklyPanel_getSnapEnabled();
  if (workspace.injected) {
    workspace.setGridSettings(gridEnabled, gridSnap);
    // Update the workspace size in case the window was resized while we were hidden
    setTimeout(function() {
      goog.array.forEach(workspace.blocksNeedingRendering, function(block) {
        workspace.getWarningHandler().checkErrors(block);
        block.render();
      });
      workspace.blocksNeedingRendering.splice(0);  // clear the array of pending blocks
      workspace.resizeContents();
      Blockly.svgResize(workspace);
    });
    return;
  }
  var options = workspace.options;
  var svg = container.querySelector('svg.blocklySvg');
  svg.cachedWidth_ = svg.clientWidth;
  svg.cachedHeight_ = svg.clientHeight;
  // svg.appendChild(workspace.createDom('blocklyMainBackground'));
  workspace.setGridSettings(gridEnabled, gridSnap);
  workspace.translate(0, 0);
  // The SVG is now fully assembled.
  Blockly.WidgetDiv.createDom();
  Blockly.Tooltip.createDom();
  workspace.addWarningIndicator();
  workspace.addBackpack();
  // Blockly.init_(workspace);
  workspace.markFocused();
  Blockly.browserEvents.bind(svg, 'focus', workspace, workspace.markFocused);
  workspace.resize();
  workspace.rendered = true;
  var blocks = workspace.getAllBlocks();

  /**
   * Creates a new helper function to render a comment set to visible but deferred during workspace
   * generation.
   * @param {!Blockly.icons.CommentIcon} comment The Blockly Comment object to be made visible.
   * @returns {Function}
   */
  function commentRenderer(comment) {
    return function() {
      comment.setVisible(comment.visible);
    }
  }

  for (var i = blocks.length - 1; i >= 0; i--) {
    var block = blocks[i];
    block.initSvg();
    block.rendered = true;
    if (block.disabled && block.updateDisabled) {
      block.updateDisabled();
    }
    if (!isNaN(block.x) && !isNaN(block.y)) {
      var xy = block.getRelativeToSurfaceXY();
      block.getSvgRoot().setAttribute('transform',
        'translate(' + block.x + ',' + block.y + ')');
      block.moveConnections_(block.x - xy.x, block.y - xy.y);
    }
    if (block.comment && block.comment.visible && block.comment.setVisible) {
      setTimeout(commentRenderer(block.comment), 1);
    }
  }
  workspace.render();
  // blocks = workspace.getTopBlocks();
  // for (var i = blocks.length - 1; i >= 0; i--) {
  //   var block = blocks[i];
  //   block.render(false);
  // }
  workspace.getWarningHandler().determineDuplicateComponentEventHandlers();
  workspace.getWarningHandler().checkAllBlocksForWarningsAndErrors();
  // center on blocks
  workspace.setScale(1);
  workspace.scrollCenter();
  // done injection
  workspace.injecting = false;
  workspace.injected = true;
  // Add pending resize event to fix positioning issue in Firefox.
  setTimeout(function() { workspace.resizeContents(); Blockly.svgResize(workspace); });
  return workspace;
};

// Preserve Blockly during Closure and GWT optimizations
window['Blockly'] = Blockly;
top['Blockly'] = Blockly;
window['AI'] = AI;
top['AI'] = AI;

/*
 * Calls hideChaff() on the blocks editor iff we receive the mousedown event on
 * an element that is not contained by the blocks editor.
 */
top.document.addEventListener('mousedown', function(e) {
  if (!e.target) return;
  var target = e.target;
  while (target) {
    var classes = target.classList;
    // Use 'contains' in case the elements gain extra classes in the future.
    if (classes.contains('blocklyWidgetDiv') || classes.contains('blocklySvg') || classes.contains('gwt-TreeItem')) {
      return;
    }
    target = target.parentElement;
  }
  // Make sure the workspace has been injected.
  if (Blockly.common.getMainWorkspace()) {
    Blockly.hideChaff();
  }
}, false);
