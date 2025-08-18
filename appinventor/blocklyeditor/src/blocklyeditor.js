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
goog.require('AI.Blockly.CustomizableConnectionChecker');
goog.require('AI.Blockly.Drawer');
goog.require('AI.Blockly.ExportBlocksImage');
goog.require('AI.Blockly.Flydown');
goog.require('AI.Blockly.ProcedureDatabase');
goog.require('AI.Blockly.ReplMgr');
goog.require('AI.Blockly.TypeBlock');
goog.require('AI.Blockly.VariableDatabase');
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

goog.require('AI.Blockly.Themes.darkTheme');

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

goog.provide('AI.Blockly.ContextMenuItems');

// Context menu items for blocks

AI.Blockly.ContextMenuItems.registerExportBlockOption = function() {
  const exportItem = {
    displayText: Blockly.BlocklyEditor.makeMenuItemWithHelp(
        Blockly.Msg['DOWNLOAD_BLOCKS_AS_PNG'],
        '/reference/other/download-pngs.html'),
    callback: function (scope) {
      Blockly.exportBlockAsPng(scope.block);
    },
    preconditionFn: function(scope) {
      if (scope.block.workspace.isFlyout) {
        return 'hidden';
      }
      return 'enabled';
    },
    weight: 100,
    id: 'appinventor_export_block',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.BLOCK,
  };
  Blockly.ContextMenuRegistry.registry.register(exportItem);
}

AI.Blockly.ContextMenuItems.registerDoItOption = function() {
  const doItItem = {
    displayText: Blockly.Msg['DO_IT'],
    callback: function (scope) {
      const myBlock = scope.block;
      const connectedToRepl =
          top.ReplState.state === Blockly.ReplMgr.rsState.CONNECTED;
      if (!connectedToRepl) {
        const dialog = new goog.ui.Dialog(
            null, true, new goog.dom.DomHelper(top.document));
        dialog.setTitle(Blockly.Msg['CAN_NOT_DO_IT']);
        dialog.setTextContent(Blockly.Msg['CONNECT_TO_DO_IT']);
        dialog.setButtonSet(new goog.ui.Dialog.ButtonSet()
            .addButton(goog.ui.Dialog.ButtonSet.DefaultButtons.OK,
                false, true));
        dialog.setVisible(true);
      } else {
        // AI.Yail.blockToCode1 returns a string if the block is a statement
        // and an array if the block is a value
        let yail;
        try {
          AI.Yail.forRepl = true;
          yail = AI.Yail.blockToCode1(myBlock);
        } finally {
          AI.Yail.forRepl = false;
        }
        unboundVariableHandler(myBlock, (yail instanceof Array) ? yail[0] : yail);
      }
    },
    preconditionFn: function(scope) {
      if (scope.block.workspace.isFlyout) {
        return 'hidden';
      }
      const connectedToRepl =
          top.ReplState.state === Blockly.ReplMgr.rsState.CONNECTED;
      const myBlock = scope.block;
      return connectedToRepl && myBlock.isEnabled() ? 'enabled' : 'disabled';
    },
    weight: 100,
    id: 'appinventor_doit',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.BLOCK,
  };
  Blockly.ContextMenuRegistry.registry.register(doItItem);
}

AI.Blockly.ContextMenuItems.RegisterClearDoItOption = function() {
  const clearDoItItem = {
    displayText: Blockly.Msg['CLEAR_DO_IT_ERROR'],
    callback: function (scope) {
      const myBlock = scope.block;
      myBlock.replError = null;
      Blockly.common.getMainWorkspace().getWarningHandler().checkErrors(myBlock);
    },
    preconditionFn: function (scope) {
      const myBlock = scope.block;
      return myBlock.replError ? 'enabled' : 'hidden';
    },
    weight: 100,
    id: 'appinventor_clear_doit',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.BLOCK,
  };
  Blockly.ContextMenuRegistry.registry.register(clearDoItItem);
}

AI.Blockly.ContextMenuItems.registerGenerateYailOption = function() {
  // TODO: eventually create a separate kind of bubble for the generated yail,
  //  which can morph into the bubble for "do it" output once we hook
  //  up to the REPL.
  const generateYailItem = {
      displayText: Blockly.Msg['GENERATE_YAIL'],
      callback: function (scope) {
        const myBlock = scope.block;
        const yail = AI.Yail.blockToCode1(myBlock);
        myBlock.setCommentText((yail instanceof Array) ? yail[0] : yail);
      },
      preconditionFn: function (scope) {
        const myBlock = scope.block;
        if (myBlock.workspace.isFlyout) {
          return 'hidden';
        }
        return window.parent.BlocklyPanel_checkIsAdmin()
            ? 'enabled'
            : 'hidden';
      },
      weight: 100,
      id: 'appinventor_generate_yail',
      scopeType: Blockly.ContextMenuRegistry.ScopeType.BLOCK,
  };
  Blockly.ContextMenuRegistry.registry.register(generateYailItem);
}

AI.Blockly.ContextMenuItems.registerAddToBackpackOption = function() {
  const addToBackpackItem = {
    displayText: function(scope) {
      return Blockly.Msg['COPY_TO_BACKPACK'] +
      " (" + scope.block.workspace.getBackpack().getCount() + ")"
    },
    callback: function (scope) {
      // top.blockSelectionWeakMap comes from the multi-select plugin
      // and is a weak map of workspaces to selected block ids
      const blockSelectionWeakMap = top.blockSelectionWeakMap;

      const myWorkspace = scope.block.workspace;
      const backpack = myWorkspace.getBackpack();
      const blocksToAdd = [];
      if (blockSelectionWeakMap) {
        const selectedBlockIds = blockSelectionWeakMap.get(myWorkspace);
        selectedBlockIds.forEach(id => {
          const selectedBlock = myWorkspace.getBlockById(id);
          blocksToAdd.push(selectedBlock);
        });
      } else {  // no multi-select plugin
        const myBlock = scope.block;
        blocksToAdd.push(myBlock);
      }
      blocksToAdd.forEach(block => {
        if (block.isDeletable) {
          backpack.addToBackpack(block);
        }
      });
    },
    preconditionFn: function (scope) {
      if (!scope.block.workspace.hasBackpack()) {
        return 'hidden'
      }
      return scope.block.isEnabled() ? 'enabled' : 'disabled';
    },
    weight: 100,
    id: 'appinventor_add_to_backpack',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.BLOCK,
  };
  Blockly.ContextMenuRegistry.registry.register(addToBackpackItem);
}

// Context menu items for the workspace

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
    return function(scope) {
      try {
        callback.call(this, scope);
      } finally {
        if (scope.workspace) {
          scope.workspace.resetArrangements();
        }
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
    workspace.arrange_blocks_event_group_ = Blockly.Events.getGroup();
    let event = new AI.Events.EndArrangeBlocks(workspaceId, type, layout);
    let SPACER = 25;
    let topblocks = workspace.getTopBlocks(/* ordered */ false);
    // If the blocks are arranged by Category, sort the array
    if (workspace.arranged_type_ === AI.Blockly.BLKS_CATEGORY) {
      topblocks.sort(sortByCategory);
    }
    let metrics = workspace.getMetrics();
    let spacing = workspace.getGrid().getSpacing()
    let spacingInv = 1 / spacing;
    let snap = workspace.getGrid().shouldSnap() ?
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
    callback: function(scope, opt_type) {
      arrangeBlocks(scope.workspace, opt_type || scope.workspace.arranged_type_,
          AI.Blockly.BLKS_HORIZONTAL);
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
    callback: function(scope, opt_type) {
      arrangeBlocks(scope.workspace, opt_type || scope.workspace.arranged_type_,
          AI.Blockly.BLKS_VERTICAL);
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
        allBlocks.forEach(block => block.setEnabled(true));
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
        allBlocks.forEach(block => block.setEnabled(false));
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
            comment.setBubbleVisible(true);
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
            comment.setBubbleVisible(false);
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
          scope.workspace.getBackpack().getCount() + ")";
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
      let gridOptions = scope.workspace.options.gridOptions;
      const gridPattern = scope.workspace.options.gridPattern;
      gridOptions['enabled'] = !gridOptions['enabled'];
      const grid = scope.workspace.getGrid();
      grid.setSnapToGrid(gridOptions['enabled'] && top.BlocklyPanel_getSnapEnabled());
      if (gridOptions['enabled']) {
        // add grid
        Blockly.common.getMainWorkspace().svgBackground_.setAttribute('style', 'fill: url(#' + gridPattern.id + ');');
      } else {
        // remove grid
        const color = Blockly.common.getMainWorkspace().getTheme().componentStyles.workspaceBackgroundColour ?? "white";
        Blockly.common.getMainWorkspace().svgBackground_.setAttribute('style', `fill: ${color};`);
      }
      if (top.BlocklyPanel_setGridEnabled) {
        top.BlocklyPanel_setGridEnabled(gridOptions['enabled']);
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
      return scope.workspace.getGrid().shouldSnap() ?
        Blockly.Msg['DISABLE_SNAPPING'] : Blockly.Msg['ENABLE_SNAPPING'];
    },
    callback: function(scope) {
      const grid = scope.workspace.getGrid()
      grid.setSnapToGrid(!grid.shouldSnap());
      if (top.BlocklyPanel_setSnapEnabled) {
        top.BlocklyPanel_setSnapEnabled(grid.shouldSnap());
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
    weight: 200
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
        scope.workspace.getAudioManager().play('delete');
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
  // Workspace menu options
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
  // Block menu options
  // AI.Blockly.ContextMenuItems.registerAddToBackpackOption();
  AI.Blockly.ContextMenuItems.registerGenerateYailOption();
  AI.Blockly.ContextMenuItems.registerExportBlockOption();
  AI.Blockly.ContextMenuItems.registerDoItOption();
  AI.Blockly.ContextMenuItems.RegisterClearDoItOption();

  Blockly.ContextMenuRegistry.registry.getItem('blockHelp').weight = 1000;
}

AI.Blockly.ContextMenuItems.registerAll();

Blockly.BlocklyEditor['cssRegistered'] = false;

/**
 * Create a new Blockly workspace but without initializing its DOM.
 * @param container The container that will host the Blockly workspace
 * @param formName The projectId_formName identifier used to name the workspace
 * @param readOnly True if the workspace should be created read-only
 * @param rtl True if the workspace is using a right-to-left language
 * @returns {Blockly.WorkspaceSvg} A newly created workspace
 */
Blockly.BlocklyEditor['create'] = function(container, formName, readOnly, rtl) {
  if (!Blockly.BlocklyEditor['cssRegistered']) {
    Blockly.BlocklyEditor['cssRegistered'] = true;
    try {
      Blockly.Css.register(`
.blocklyZoom:hover, .blocklyTrash:hover, .blocklyMultiselect:hover { cursor: pointer; }
.blocklyZoom>image, .blocklyZoom>image:hover { opacity: 1.0; }
.blocklyMultiselect>image, .blocklyMultiselect>image:hover { opacity: 1.0; }
`);
    } catch (e) {
      // Thrown if we've already registered the CSS. This should only happen in unit tests.
    }
  }
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
    'grid': {'spacing': '20', 'length': '5', 'snap': false, 'colour': '#ccc'},
    'zoom': {'controls': true, 'wheel': true, 'scaleSpeed': 1.1, 'maxScale': 3, 'minScale': 0.1},
    plugins: {
      blockDragger: MultiselectBlockDragger,
      metricsManager: ScrollMetricsManager,
      connectionPreviewer: decoratePreviewer(Blockly.InsertionMarkerPreviewer),
      [Blockly.registry.Type.CONNECTION_CHECKER]: 'CustomizableConnectionChecker',
    },
    baseBlockDragger: ScrollBlockDragger,
    useDoubleClick: true,
    bumpNeighbours: true,
    multiselectIcon: {
      hideIcon: false,
      enabledIcon: 'static/images/select.svg',
      disabledIcon: 'static/images/unselect.svg',
    },
    multiselectCopyPaste: {
      crossTab: true,
      menu: true,
    },
    renderer: 'geras2_renderer',
  };
  var workspace = Blockly.inject(container, options);
  AI.Blockly.multiselect = Multiselect;
  var multiselectPlugin = new AI.Blockly.multiselect(workspace);
  multiselectPlugin.init(options);
  var lexicalVariablesPlugin = LexicalVariablesPlugin;
  lexicalVariablesPlugin.init(workspace);
  var searchPlugin = new WorkspaceSearch(workspace);
  searchPlugin.init();
  const scrollOptions = new ScrollOptions(workspace);
  // Make autoscrolling be based purely on the mouse position ands slow it down a bit.
  scrollOptions.init({
    edgeScrollOptions: {
      oversizeBlockMargin: 0,
      oversizeBlockThreshold: 0,
      slowBlockSpeed: .15
    }
  });
  /*
    Keyboard navigation -- needs to be fixed with multiselect
  if (!AI.Blockly.navigationController) {
    AI.Blockly.navigationController = new NavigationController();
    AI.Blockly.navigationController.init();
  }
  AI.Blockly.navigationController.addWorkspace(workspace);
  */
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
    workspace.addChangeListener(workspaceChanged);
  }
  workspace.addChangeListener(function(e) {
    if (e.type == Blockly.Events.BLOCK_MOVE && e.newParentId !== e.oldParentId) {
      const block = workspace.getBlockById(e.blockId);
      if (!block) {
        // This seems to be the case when the block has been deleted since it is first moved from
        // its parent then removed from the workspace, but both events will be run back to back
        // after the deletion has already happened.
        return;
      }
      block.getDescendants().forEach(function(block) {
        if (block.type === 'lexical_variable_get' || block.type === 'lexical_variable_set') {
          // If the block is a lexical variable, then we need to rebuild the options for the field
          // given the change in scope.
          const field = block.getField('VAR');
          field.getOptions(false);  // rebuild option cache
          field.setValue(field.getValue());
          block.queueRender();
        }
      });
    }
  });
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
  workspace.notYetRendered = true;
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
AI.inject = function(container, workspace, isDarkMode=false) {
  if (isDarkMode) {
    Blockly.common.getMainWorkspace().setTheme(Blockly.Themes.darkTheme);
  }
  Blockly.common.setMainWorkspace(workspace);  // make workspace the 'active' workspace
  workspace.fireChangeListener(new AI.Events.ScreenSwitch(workspace.projectId, workspace.formName));
  var gridEnabled = top.BlocklyPanel_getGridEnabled && top.BlocklyPanel_getGridEnabled();
  var gridSnap = top.BlocklyPanel_getSnapEnabled && top.BlocklyPanel_getSnapEnabled();
  // Note that in current code, workspace.injected is always true.  Before the 2023 Blockly update
  // it was possible for workspace.injected to be false, but that is no longer the case.
  workspace.setGridSettings(gridEnabled, gridSnap);
  // Update the workspace size in case the window was resized while we were hidden
  setTimeout(function() {
    goog.array.forEach(workspace.blocksNeedingRendering, function(block) {
      workspace.getWarningHandler().checkErrors(block);
      block.render();
    });
    workspace.blocksNeedingRendering.splice(0);  // clear the array of pending blocks
    workspace.resizeContents();
    Blockly.common.svgResize(workspace);
    if (workspace.notYetRendered) {
      workspace.notYetRendered = false;
      workspace.scrollCenter();
    }
    //AI.Blockly.navigationController.enable(workspace);
  });
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
    if (classes.contains('blocklyWidgetDiv') || classes.contains('blocklySvg')
        || classes.contains('gwt-TreeItem') || classes.contains('blocklyDropDownDiv')) {
      return;
    }
    target = target.parentElement;
  }
  // Make sure the workspace has been injected.
  if (Blockly.common.getMainWorkspace()) {
    Blockly.hideChaff();
  }
}, false);
