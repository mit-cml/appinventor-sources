// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Initialize the blocks editor workspace.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author sharon@google.com (Sharon Perl)
 */

'use strict';

goog.provide('Blockly.BlocklyEditor');

goog.require('Blockly.Drawer');
goog.require('Blockly.TypeBlock');

Blockly.BlocklyEditor.startup = function(documentBody, formName) {
  var typeblock_config = {
    frame: 'ai_frame',
    typeBlockDiv: 'ai_type_block',
    inputText: 'ac_input_text'
  };

  //This is what Blockly's init function does when passing options.
  //We are overriding the init process so putting it here
  goog.mixin(Blockly, {
    collapse : true,
    hasScrollbars: true,
    hasTrashcan: true,
    configForTypeBlock: typeblock_config
  });

  Blockly.inject(documentBody);

  Blockly.Drawer.createDom();
  Blockly.Drawer.init();
  //This would also be done in Blockly init, but we need to do it here cause of
  //the different init process in drawer (it'd be undefined at the time it hits
  //init in Blockly)
  if (!Blockly.readOnly)
    Blockly.TypeBlock(Blockly.configForTypeBlock);

  Blockly.BlocklyEditor.formName = formName;

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

  Blockly.globalNamePrefix = "global"; // For names introduced by global variable declarations
  Blockly.procedureParameterPrefix = "input"; // For names introduced by procedure/function declarations
  Blockly.handlerParameterPrefix = "input"; // For names introduced by event handlers
  Blockly.localNamePrefix = "local"; // For names introduced by local variable declarations
  Blockly.loopParameterPrefix = "item"; // For names introduced by for loops
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
    return Blockly.globalNamePrefix + Blockly.menuSeparator + name;
  };

  // Return a list of (1) prefix (if it exists, "" if not) and (2) unprefixed name
  Blockly.unprefixName = function (name) {
    if (name.indexOf(Blockly.globalNamePrefix + Blockly.menuSeparator) == 0) {
      // Globals always have prefix, regardless of flags. Handle these specially
      return [Blockly.globalNamePrefix, name.substring(Blockly.globalNamePrefix.length + Blockly.menuSeparator.length)];
    } else if (!Blockly.showPrefixToUser) {
      return ["", name];
    } else {
      var prefixes = [Blockly.procedureParameterPrefix,
                      Blockly.handlerParameterPrefix,
                      Blockly.localNamePrefix,
                      Blockly.loopParameterPrefix,
                      Blockly.loopRangeParameterPrefix]
      for (i=0; i < prefixes.length; i++) {
        if (name.indexOf(prefixes[i]) == 0) {
          // name begins with prefix
          return [prefixes[i], name.substring(prefixes[i].length + Blockly.menuSeparator.length)]
        }
      }
      // Really an error if get here ...
      return ["", name];
    }
  }

  /******************************************************************************/

  Blockly.bindEvent_(Blockly.mainWorkspace.getCanvas(), 'blocklyWorkspaceChange', this,
      function() {
        if (window.parent.BlocklyPanel_blocklyWorkspaceChanged){
          window.parent.BlocklyPanel_blocklyWorkspaceChanged(Blockly.BlocklyEditor.formName);
        }
        // [lyn 12/31/2103] Check for duplicate component event handlers before
        // running any error handlers to avoid quadratic time behavior.
        Blockly.WarningHandler.determineDuplicateComponentEventHandlers();
  });
};

Blockly.BlocklyEditor.render = function() {
  var start = new Date().getTime();
  Blockly.Instrument.initializeStats("Blockly.BlocklyEditor.render");
  Blockly.mainWorkspace.render();
  var stop = new Date().getTime();
  var timeDiff = stop - start;
  Blockly.Instrument.stats.totalTime = timeDiff;
  Blockly.Instrument.displayStats("Blockly.BlocklyEditor.render");
}

/**
 * Add a "Do It" option to the context menu for every block. If the user is an admin also
 * add a "Generate Yail" option to the context menu for every block. The generated yail will go in
 * the block's comment (if it has one) for now.
 * TODO: eventually create a separate kind of bubble for the generated yail, which can morph into
 * the bubble for "do it" output once we hook up to the REPL.
 */
Blockly.Block.prototype.customContextMenu = function(options) {
  var myBlock = this;
  var doitOption = { enabled: this.disabled?false : true};
  if (window.parent.BlocklyPanel_checkIsAdmin()) {
    var yailOption = {enabled: this.disabled?false : true};
    yailOption.text = "Generate Yail";
    yailOption.callback = function() {
      var yailText;
      //Blockly.Yail.blockToCode1 returns a string if the block is a statement
      //and an array if the block is a value
      var yailTextOrArray = Blockly.Yail.blockToCode1(myBlock);
      if(yailTextOrArray instanceof Array){
        yailText = yailTextOrArray[0];
      } else {
        yailText = yailTextOrArray;
      }
      myBlock.setCommentText(yailText);
    };
    options.push(yailOption);
  }
  doitOption.text = "Do It";
  doitOption.callback = function() {
    var yailText;
    //Blockly.Yail.blockToCode1 returns a string if the block is a statement
    //and an array if the block is a value
    var yailTextOrArray = Blockly.Yail.blockToCode1(myBlock);
    var dialog;
    if (window.parent.ReplState.state != Blockly.ReplMgr.rsState.CONNECTED) {
      dialog = new goog.ui.Dialog(null, true);
      dialog.setTitle("Cannot Do it");
      dialog.setContent('You must be connected to the companion or emulator to use "Do It"');
      dialog.setButtonSet(new goog.ui.Dialog.ButtonSet().
        addButton(goog.ui.Dialog.ButtonSet.DefaultButtons.OK,
          false, true));
      dialog.setVisible(true);
    } else {
      if(yailTextOrArray instanceof Array){
        yailText = yailTextOrArray[0];
      } else {
        yailText = yailTextOrArray;
      }
      Blockly.ReplMgr.putYail(yailText, myBlock);
    }
  };
  options.push(doitOption);
  if(myBlock.procCustomContextMenu){
    myBlock.procCustomContextMenu(options);
  }
};
