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

goog.require('AI.Blockly.Drawer');

// App Inventor extensions to Blockly
goog.require('Blockly.TypeBlock');

goog.require('Blockly.Flyout');

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

Blockly.BlocklyEditor.render = function() {
};

Blockly.BlocklyEditor.HELP_IFRAME = null;

top.addEventListener('mousedown', function(e) {
  if (e.target.tagName === 'IMG' &&
      e.target.parentElement.tagName === 'A' &&
      e.target.parentElement.className === 'menu-help-item') {
    e.stopPropagation();
    if (Blockly.BlocklyEditor.HELP_IFRAME != null) {
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
    top.document.body.appendChild(div);
    Blockly.WidgetDiv.dispose_ = function() {
      Blockly.BlocklyEditor.HELP_IFRAME.parentNode.removeChild(Blockly.BlocklyEditor.HELP_IFRAME);
      Blockly.BlocklyEditor.HELP_IFRAME = null;
    };
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
  a.style.right = '5em';
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
      Blockly.Yail.forRepl = true;
      Blockly.ReplMgr.putYail(yailText, myBlock);
    } finally {
      Blockly.Yail.forRepl = false;
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
          code += '($' + unbound_vars[i] + ' ' + Blockly.Yail.quotifyForREPL(document.querySelector('input[name="' + unbound_vars[i] + '"]').value) + ') ';
        }
        code += ")" + yailText + ")";
        try {
          Blockly.Yail.forRepl = true;
          Blockly.ReplMgr.putYail(code, myBlock);
        } finally {
          Blockly.Yail.forRepl = false;
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
    // Blockly.Yail.blockToCode1 returns a string if the block is a statement
    // and an array if the block is a value
    var yail = Blockly.Yail.blockToCode1(myBlock);
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
      // Blockly.Yail.blockToCode1 returns a string if the block is a statement
      // and an array if the block is a value
      var yail;
      try {
        Blockly.Yail.forRepl = true;
        yail = Blockly.Yail.blockToCode1(myBlock);
      } finally {
        Blockly.Yail.forRepl = false;
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
    Blockly.getMainWorkspace().getWarningHandler().checkErrors(myBlock);
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

/* [Added by paulmw in patch 15]
   There are three ways that you can change how lexical variables
   are handled:

   1. Show prefixes to users, and separate namespace in yail
   Blockly.showPrefixToUser = true;
   Blockly.usePrefixInYail = true;

   2. Show prefixes to users, lexical variables share namespace yail
   Blockly.showPrefixToUser = true;
   Blockly.usePrefixInYail = false;

   3. Hide prefixes from users, lexical variables share namespace yail
   //The default (as of 12/21/12)
   Blockly.showPrefixToUser = false;
   Blockly.usePrefixInYail = false;

   It is not possible to hide the prefix and have separate namespaces
   because Blockly does not allow to items in a list to have the same name
   (plus it would be confusing...)

*/

Blockly.showPrefixToUser = false;
Blockly.usePrefixInYail = false;

/******************************************************************************
   [lyn, 12/23-27/2012, patch 16]
     Prefix labels for parameters, locals, and index variables,
     Might want to experiment with different combintations of these. E.g.,
     + maybe all non global parameters have prefix "local" or all have prefix "param".
     + maybe index variables have prefix "index", or maybe instead they are treated as "param"
*/

/**
 * The global keyword. Users may be shown a translated keyword instead but this is the internal
 * token used to identify global variables.
 * @type {string}
 * @const
 */
Blockly.GLOBAL_KEYWORD = 'global';  // used internally to identify global variables; not translated
Blockly.procedureParameterPrefix = "input"; // For names introduced by procedure/function declarations
Blockly.handlerParameterPrefix = "input"; // For names introduced by event handlers
Blockly.localNamePrefix = "local"; // For names introduced by local variable declarations
Blockly.loopParameterPrefix = "item"; // For names introduced by for loops
Blockly.loopKeyParameterPrefix = 'key'; // For keys introduced by dict for loops.
Blockly.loopValueParameterPrefix = 'value'; // For values introduced by dict for loops.
Blockly.loopRangeParameterPrefix = "counter"; // For names introduced by for range loops

Blockly.menuSeparator = " "; // Separate prefix from name with this. E.g., space in "param x"
Blockly.yailSeparator = "_"; // Separate prefix from name with this. E.g., underscore "param_ x"

// Curried for convenient use in field_lexical_variable.js
Blockly.possiblyPrefixMenuNameWith = // e.g., "param x" vs "x"
function (prefix) {
  return function (name) {
    return (Blockly.showPrefixToUser ? (prefix + Blockly.menuSeparator) : "") + name;
  }
};

// Curried for convenient use in generators/yail/variables.js
Blockly.possiblyPrefixYailNameWith = // e.g., "param_x" vs "x"
function (prefix) {
  return function (name) {
    return (Blockly.usePrefixInYail ? (prefix + Blockly.yailSeparator) : "") + name;
  }
};

Blockly.prefixGlobalMenuName = function (name) {
  return Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX + Blockly.menuSeparator + name;
};

// Return a list of (1) prefix (if it exists, "" if not) and (2) unprefixed name
Blockly.unprefixName = function (name) {
  if (name.indexOf(Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX + Blockly.menuSeparator) == 0) {
    // Globals always have prefix, regardless of flags. Handle these specially
    return [Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX, name.substring(Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX.length + Blockly.menuSeparator.length)];
  } else if (name.indexOf(Blockly.GLOBAL_KEYWORD + Blockly.menuSeparator) === 0) {
    return [Blockly.GLOBAL_KEYWORD, name.substring(6 + Blockly.menuSeparator.length)];
  } else if (!Blockly.showPrefixToUser) {
    return ["", name];
  } else {
    var prefixes = [Blockly.procedureParameterPrefix,
                    Blockly.handlerParameterPrefix,
                    Blockly.localNamePrefix,
                    Blockly.loopParameterPrefix,
                    Blockly.loopRangeParameterPrefix];
    for (var i=0; i < prefixes.length; i++) {
      if (name.indexOf(prefixes[i]) == 0) {
        // name begins with prefix
        return [prefixes[i], name.substring(prefixes[i].length + Blockly.menuSeparator.length)]
      }
    }
    // Really an error if get here ...
    return ["", name];
  }
};

/**
 * Create a new Blockly workspace but without initializing its DOM.
 * @param container The container that will host the Blockly workspace
 * @param formName The projectId_formName identifier used to name the workspace
 * @param readOnly True if the workspace should be created read-only
 * @param rtl True if the workspace is using a right-to-left language
 * @returns {Blockly.WorkspaceSvg} A newly created workspace
 */
Blockly.BlocklyEditor['create'] = function(container, formName, readOnly, rtl) {
  var options = new Blockly.Options({
    'readOnly': readOnly,
    'rtl': rtl,
    'collapse': true,
    'scrollbars': true,
    'trashcan': true,
    'comments': true,
    'disable': true,
    'media': './static/media/',
    'grid': {'spacing': '20', 'length': '5', 'snap': true, 'colour': '#ccc'},
    'zoom': {'controls': true, 'wheel': true, 'scaleSpeed': 1.1, 'maxScale': 3, 'minScale': 0.1}
  });

  var subContainer = goog.dom.createDom('div', 'injectionDiv');
  subContainer.setAttribute('tabindex', '0');  // make injection div focusable
  container.appendChild(subContainer);
  var svg = Blockly.createDom_(subContainer, options);
  svg.setAttribute('width', '100%');
  svg.setAttribute('height', '100%');

  // Create surfaces for dragging things. These are optimizations
  // so that the broowser does not repaint during the drag.
  var blockDragSurface = new Blockly.BlockDragSurfaceSvg(subContainer);
  var workspaceDragSurface = new Blockly.workspaceDragSurfaceSvg(subContainer);

  var workspace = new Blockly.WorkspaceSvg(options, blockDragSurface, workspaceDragSurface);
  Blockly.allWorkspaces[formName] = workspace;
  workspace.formName = formName;
  workspace.rendered = false;
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
    workspace.typeBlock_ = new Blockly.TypeBlock(typeblockOpts, workspace);
  }
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
  Blockly.mainWorkspace = workspace;  // make workspace the 'active' workspace
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
  svg.appendChild(workspace.createDom('blocklyMainBackground'));
  workspace.setGridSettings(gridEnabled, gridSnap);
  workspace.translate(0, 0);
  if (!options.readOnly && !options.hasScrollbars) {
    var workspaceChanged = function() {
      if (Blockly.dragMode_ == Blockly.DRAG_NONE) {
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
  // The SVG is now fully assembled.
  Blockly.WidgetDiv.createDom();
  Blockly.Tooltip.createDom();
  workspace.drawer_ = new Blockly.Drawer(workspace, { scrollbars: true });
  workspace.flyout_ = workspace.drawer_.flyout_;
  var flydown = new Blockly.Flydown(new Blockly.Options({scrollbars: false}));
  // ***** [lyn, 10/05/2013] NEED TO WORRY ABOUT MULTIPLE BLOCKLIES! *****
  workspace.flydown_ = flydown;
  Blockly.utils.insertAfter_(flydown.createDom('g'), workspace.svgBubbleCanvas_);
  flydown.init(workspace);
  flydown.autoClose = true; // Flydown closes after selecting a block
  workspace.addWarningIndicator();
  workspace.addBackpack();
  Blockly.init_(workspace);
  workspace.markFocused();
  Blockly.bindEvent_(svg, 'focus', workspace, workspace.markFocused);
  workspace.resize();
  // Hide scrollbars by default (otherwise ghost rectangles intercept mouse events)
  workspace.flyout_.scrollbar_ && workspace.flyout_.scrollbar_.setContainerVisible(false);
  workspace.backpack_.flyout_.scrollbar_ && workspace.backpack_.flyout_.scrollbar_.setContainerVisible(false);
  workspace.flydown_.scrollbar_ && workspace.flydown_.scrollbar_.setContainerVisible(false);
  // Render blocks created prior to the workspace being rendered.
  workspace.rendered = true;
  var blocks = workspace.getAllBlocks();

  /**
   * Creates a new helper function to render a comment set to visible but deferred during workspace
   * generation.
   * @param {!Blockly.Comment} comment The Blockly Comment object to be made visible.
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
    if (classes.contains('blocklyWidgetDiv') || classes.contains('blocklySvg')) {
      return;
    }
    target = target.parentElement;
  }
  // Make sure the workspace has been injected.
  if (Blockly.mainWorkspace) {
    Blockly.hideChaff();
  }
}, false);
