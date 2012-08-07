// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Set of drawers for holding factory blocks (blocks that create
 * other blocks when dragged onto the workspace). The set of drawers
 * includes the built-in drawers that we get from the blocks language, as
 * well as a drawer per component instance that was added to this workspace.
 * 
 * TODO(sharon): add support for advanced drawer that contains generic
 * component blocks
 * 
 * @author Sharon Perl (sharon@google.com)
 */

Blockly.Drawer = {};

/**
 * Create the dom for the drawer. Creates a flyout Blockly.Drawer.flyout_,
 * and initializes its dom.
 */
Blockly.Drawer.createDom = function() {
  Blockly.Drawer.flyout_ = new Blockly.Flyout();
  // insert the flyout after the main workspace (except, there's no 
  // svg.insertAfter method, so we need to insert before the thing following
  // the main workspace. Neil Fraser says: this is "less hacky than it looks".
  var flyoutGroup = Blockly.Drawer.flyout_.createDom();
  Blockly.svg.insertBefore(flyoutGroup, Blockly.mainWorkspace.svgGroup_.nextSibling);
};

/**
 * Initializes the drawer by initializing the flyout and creating the
 * language tree. Call after calling createDom.
 */
Blockly.Drawer.init = function() {
  Blockly.Drawer.flyout_.init(Blockly.mainWorkspace,
                              Blockly.getMainWorkspaceMetrics);
  Blockly.Drawer.languageTree = Blockly.Drawer.buildTree_();
};

/**
 * String to prefix on categories of each potential block in the drawer.
 * Used to prevent collisions with built-in properties like 'toString'.
 * @private
 */
Blockly.Drawer.PREFIX_ = 'cat_';

/**
 * Build the hierarchical tree of block types. 
 * Note: taken from Blockly's toolbox.js
 * @return {!Object} Tree object.
 * @private
 */
Blockly.Drawer.buildTree_ = function() {
  var tree = {};
  // Populate the tree structure.
  for (var name in Blockly.Language) {
    var block = Blockly.Language[name];
    // Blocks without a category are fragments used by the mutator dialog.
    if (block.category) {
      var cat = Blockly.Drawer.PREFIX_ + window.encodeURI(block.category);
      if (cat in tree) {
        tree[cat].push(name);
      } else {
        tree[cat] = [name];
      }
    }
  }
  return tree;
};

/**
 * Show the contents of the built-in drawer named drawerName. drawerName
 * should be one of Blockly.MSG_VARIABLE_CATEGORY, 
 * Blockly.MSG_PROCEDURE_CATEGORY, or one of the built-in block categories.
 * @param drawerName
 */
Blockly.Drawer.showBuiltin = function(drawerName) {
  if (drawerName != Blockly.MSG_VARIABLE_CATEGORY &&
      drawerName != Blockly.MSG_PROCEDURE_CATEGORY) {
      drawerName = Blockly.Drawer.PREFIX_ + drawerName;
  }
  var blockSet = Blockly.Drawer.languageTree[drawerName] || drawerName;
  Blockly.Drawer.flyout_.show(blockSet);
};

/**
 * Show the blocks drawer for the component with give instance name. If no
 * such component is found, currently we just log a message to the console
 * and do nothing.
 */
Blockly.Drawer.showComponent = function(instanceName) {
  if (Blockly.ComponentInstances[instanceName]) {
    Blockly.Drawer.flyout_.hide();
    Blockly.Drawer.flyout_.show(Blockly.ComponentInstances[instanceName].blocks);
  } else {
    //throw 'Error: "' + name + '" is an unknown component name.';
    console.log("Got call to Blockly.Component.show(" +  name + 
                ") - unknown component name");
  }
};

/**
 * Hide the Drawer flyout
 */
Blockly.Drawer.hide = function() {
  Blockly.Drawer.flyout_.hide();
};

/**
 * @returns  true if the Drawer flyout is currently open, false otherwise.
 */
Blockly.Drawer.isShowing = function() {
  return Blockly.Drawer.flyout_.isVisible();
};
