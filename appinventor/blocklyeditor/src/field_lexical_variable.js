// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Drop-down chooser of variables in the current lexical scope for App Inventor
 * @author fturbak@wellesley.com (Lyn Turbak)
 */

'use strict';

/**
 * Lyn's History:
 *  *  [lyn, written 11/15-17/13 but added 07/01/14] Overhauled parameter renaming:
 *    + Refactored FieldLexicalVariable method getNamesInScope to have same named function that works on any block,
 *      and a separate function getLexicalNamesInScope that works on any block
 *    + Refactored monolithic renameParam into parts that are useful on their own
 *    + Previously, renaming param from oldName to newName might require renaming newName itself
 *      (adding a number to the end) to avoid renaming inner declarations that might be captured
 *      by renaming oldName to newName. Now, there is a choice between this previous behavior and
 *      a new behavior in which newName is *not* renamed and capture is avoided by renaming
 *      the inner declarations when necessary.
 *    + Created Blockly.Lexical.freeVariables for calculating free variables
 *    + Created Blockly.Lexical.renameBound for renaming of boundVariables in declarations
 *    + Created Blockly.Lexical.renameFree for renaming of freeVariables in declarations
 *    + Created Blockly.LexicalVariable.stringListsEqual for testing equality of string lists.
 *  [lyn, 06/11/14] Modify checkIdentifier to work for i8n.
 *  [lyn, 04/13/14] Modify calculation of global variable names:
 *    1. Use getTopBlocks rather than getAllBlocks; the latter, in combination with quadratic memory
 *       allocation space from Neil's getAllBlocks, was leading to cubic memory allocation space,
 *       which led to lots of time wasted due to allocation and GC. This change dramatically
 *       reduces allocation times and GC
 *    2. Introduce caching for Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors().
 *       This change reduces allocation times and GC even further.
 *  [lyn, 10/28/13] Made identifier legality check more restrictive by removing arithmetic
 *     and logical ops as possible identifier characters
 *  [lyn, 10/27/13] Create legality filter & transformer for AI2 variable names
 *  [lyn, 10/26/13] Fixed renaming of globals and lexical vars involving empty strings and names with internal spaces.
 *  [lyn, 12/23-27/12] Updated to:
 *     (1) handle renaming involving local declaration statements/expressions and
 *     (2) treat name prefixes correctly when they're used.
 *  [lyn, 11/29/12] handle control constructs in getNamesInScope and referenceResult
 *  [lyn, 11/24/12] Sort and remove duplicates from namespaces
 *  [lyn, 11/19/12]
 *    + renameGlobal renames global references and prevents duplicates in global names;
 *    + renameParam is similar for procedure and loop names.
 *    + define referenceResult, which is renaming workhorse
 *  [lyn, 11/18/12] nameNotIn function for renaming by adding number at end
 *  [lyn, 11/17/12] handle eventParams in getNamesInScope
 *  [lyn, 11/10/12] getGlobalNames and getNamesInScope
 */

// Get all global names 

/**
 * Class for a variable's dropdown field.
 * @param {!string} varname The default name for the variable.  If null,
 *     a unique variable name will be generated.
 * @extends Blockly.FieldDropdown
 * @constructor
 */
Blockly.FieldLexicalVariable = function(varname) {
 // Call parent's constructor.
  Blockly.FieldDropdown.call(this, Blockly.FieldLexicalVariable.dropdownCreate,
                                   Blockly.FieldLexicalVariable.dropdownChange);
  if (varname) {
    this.setText(varname);
  } else {
    this.setText(Blockly.Variables.generateUniqueName());
  }
};

// FieldLexicalVariable is a subclass of FieldDropdown.
goog.inherits(Blockly.FieldLexicalVariable, Blockly.FieldDropdown);

/**
 * Get the variable's name (use a variableDB to convert into a real name).
 * Unline a regular dropdown, variables are literal and have no neutral value.
 * @return {string} Current text.
 */
Blockly.FieldLexicalVariable.prototype.getValue = function() {
  return this.getText();
};

/**
 * Set the variable name.
 * @param {string} text New text.
 */
Blockly.FieldLexicalVariable.prototype.setValue = function(text) {
  this.value_ = text;
  this.setText(text);
  // The code below is almost certainly in the wrong place
  // but it seems to fix the problem by making sure that any
  // eventparam value in a variable block is removed. The next
  // time it is needed, it will be re-computed. There *has*
  // to be a better place for this code, but I couldn't find it in the
  // short time I had to work on this. So consider this a patch
  // until we figure out where this code really belongs!
  if (this.block_) {
    if (this.block_.eventparam) {
      this.block_.eventparam = undefined; // unset it
    }
  }

};

/**
 * Get the block holding this drop-down variable chooser
 * @return {string} Block holding this drop-down variable chooser. 
 */
Blockly.FieldLexicalVariable.prototype.getBlock = function() {
  return this.block_; 
};

/**
 * Set the block holding this drop-down variable chooser. Also initializes the cachedParent.
 * @param {string} block Block holding this drop-down variable chooser
 */
Blockly.FieldLexicalVariable.prototype.setBlock = function(block) {
  this.block_ = block;
  this.setCachedParent(block.getParent());
};

/**
 * Get the cached parent of the block holding this drop-down variable chooser
 * @return {string} Cached parent of the block holding this drop-down variable chooser. 
 */
Blockly.FieldLexicalVariable.prototype.getCachedParent = function() {
  return this.cachedParent_; 
};

/**
 * Set the cached parent of the block holding this drop-down variable chooser. 
 * This is used for detecting when the parent has changed in the onchange event handler. 
 * @param {string} Parent of the block holding this drop-down variable chooser
 */
Blockly.FieldLexicalVariable.prototype.setCachedParent = function(parent) {
  this.cachedParent_ = parent;
};

// [lyn, 11/10/12] Returns the names of all global definitions as a list of strings
// [lyn, 11/18/12] 
// * Removed from prototype and stripped off "global" prefix (add it elsewhere)
// * Add optional excluded block argument as in Neil's code to avoid global declaration being created
Blockly.FieldLexicalVariable.getGlobalNames = function (optExcludedBlock) {
  if (Blockly.Instrument.useLynCacheGlobalNames && Blockly.WarningHandler.cacheGlobalNames) {
    return Blockly.WarningHandler.cachedGlobalNames;
  }
  var globals = [];
  if (Blockly.mainWorkspace) {
    var blocks = [];
    if (Blockly.Instrument.useLynGetGlobalNamesFix) {
      blocks = Blockly.mainWorkspace.getTopBlocks(); // [lyn, 04/13/14] Only need top blocks, not all blocks!
    } else {
      blocks = Blockly.mainWorkspace.getAllBlocks(); // [lyn, 11/10/12] Is there a better way to get workspace?
    }
    for (var i = 0; i < blocks.length; i++) {
      var block = blocks[i];
      if ((block.type === 'global_declaration') && (block != optExcludedBlock)) {
          globals.push(block.getFieldValue('NAME'));
      }
    }
  }
  return globals;
}

/**
 * @this A FieldLexicalVariable instance
 * @returns {list} A list of all global and lexical names in scope at the point of the getter/setter
 *   block containing this FieldLexicalVariable instance. Global names are listed in sorted
 *   order before lexical names in sorted order.
 */
// [lyn, 12/24/12] Clean up of name prefixes; most work done earlier by paulmw
// [lyn, 11/29/12] Now handle params in control constructs
// [lyn, 11/18/12] Clarified structure of namespaces
// [lyn, 11/17/12]
// * Now handle event params.
// * Commented out loop params because AI doesn't handle loop variables correctly yet. 
// [lyn, 11/10/12]
// Returns the names of all names in lexical scope for the block associated with this menu. 
// including global variable names. 
// * Each global name is prefixed with "global " 
// * If Blockly.showPrefixToUser is false, non-global names are not prefixed. 
// * If Blockly.showPrefixToUser is true, non-global names are prefixed with labels
//   specified in blocklyeditor.js
Blockly.FieldLexicalVariable.prototype.getNamesInScope = function () {
  return Blockly.FieldLexicalVariable.getNamesInScope(this.block_);
}

/**
 * @param block
 * @returns {list} A list of all global and lexical names in scope at the given block.
 *   Global names are listed in sorted order before lexical names in sorted order.
 */
// [lyn, 11/15/13] Refactored to work on any block
Blockly.FieldLexicalVariable.getNamesInScope = function (block) {
  var globalNames = Blockly.FieldLexicalVariable.getGlobalNames(); // from global variable declarations
  // [lyn, 11/24/12] Sort and remove duplicates from namespaces
  globalNames = Blockly.LexicalVariable.sortAndRemoveDuplicates(globalNames);
  var allLexicalNames = Blockly.FieldLexicalVariable.getLexicalNamesInScope(block);
  // Return a list of all names in scope: global names followed by lexical ones.
  return globalNames.map( Blockly.prefixGlobalMenuName ).concat(allLexicalNames);
}

/**
 * @param block
 * @returns {list} A list of all lexical names (in sorted order) in scope at the point of the given block
 *   If Blockly.usePrefixInYail is true, returns names prefixed with labels like "param", "local", "index";
 *   otherwise returns unprefixed names.
 */
// [lyn, 11/15/13] Factored this out from getNamesInScope to work on any block
Blockly.FieldLexicalVariable.getLexicalNamesInScope = function (block) {
  var procedureParamNames = []; // from procedure/function declarations
  var handlerParamNames = []; // from event handlers
  var loopNames = []; // from for loops
  var rangeNames = []; // from range loops
  var localNames = []; // from local variable declaration
  var allLexicalNames = []; // all non-global names
  var innermostPrefix = {}; // paulmw's mechanism for keeping track of innermost prefix in case
                            // where prefix is an annotation rather than a separate namespace
  var parent;
  var child;
  var params;
  var i;

  // [lyn, 12/24/2012] Abstract over name handling  
  function rememberName (name, list, prefix) {
    list.push(name);
    if (!innermostPrefix[name]) { // only set this if not already set from an inner scope.
      innermostPrefix[name] = prefix;
    }
  }
  
  child = block;
  if (child) {
    parent = child.getParent();
    if (parent) {
      while (parent) {
          if ((parent.type === "procedures_defnoreturn") || (parent.type === "procedures_defreturn")) {
            params = parent.declaredNames(); // [lyn, 10/13/13] Names from block, not arguments_ instance var
            for (i = 0; i < params.length; i++) {
              rememberName(params[i], procedureParamNames, Blockly.procedureParameterPrefix);
            }
          } else if (parent.category === "Component" && parent.getEventTypeObject && parent.declaredNames) {
            // Parameter names in event handlers
            params = parent.declaredNames();
            for (var j = 0; j < params.length; j++) {
              rememberName(params[j], handlerParamNames, Blockly.handlerParameterPrefix);
            }
          // [lyn, 11/29/12] Added parameters for control constructs.
          } else if ( (parent.type === "controls_forEach")
                     && (parent.getInputTargetBlock('DO') == child)) {// Only DO is in scope, not other inputs!
              var loopName = parent.getFieldValue('VAR');
              rememberName(loopName, loopNames, Blockly.loopParameterPrefix); 
          } else if ( (parent.type === "controls_forRange")
                     && (parent.getInputTargetBlock('DO') == child)) {// Only DO is in scope, not other inputs!
              var rangeName = parent.getFieldValue('VAR');
              rememberName(rangeName, rangeNames, Blockly.loopRangeParameterPrefix);

          } else if ( ( parent.type === "local_declaration_expression" 
                        && parent.getInputTargetBlock('RETURN') == child ) // only body is in scope of names
                      || ( parent.type === "local_declaration_statement"  
                           && parent.getInputTargetBlock('STACK') == child ) // only body is in scope of names
                           ) {
            params = parent.declaredNames(); // [lyn, 10/13/13] Names from block, not localNames_ instance var
            for (i = 0; i < params.length; i++) {
              rememberName(params[i], localNames, Blockly.localNamePrefix);
            }
          }
          child = parent;
          parent = parent.getParent(); // keep moving up the chain.
      }
    }
  }

  if(!Blockly.usePrefixInYail){ // Only a single namespace
    allLexicalNames = procedureParamNames.concat(handlerParamNames)
                                         .concat(loopNames)
                                         .concat(rangeNames)
                                         .concat(localNames);
    allLexicalNames = Blockly.LexicalVariable.sortAndRemoveDuplicates(allLexicalNames);
    // Add prefix as annotation only when Blockly.showPrefixToUser is true
    allLexicalNames = allLexicalNames.map( 
      function (name) {
        // return ((Blockly.possiblyPrefixNameWith(Blockly.menuSeparator)) (innermostPrefix[name])) (name);
        return (Blockly.possiblyPrefixMenuNameWith (innermostPrefix[name])) (name);
      }
    )
  } else { // multiple namespaces distinguished by prefixes 
           // note: correctly handles case where some prefixes are the same
    allLexicalNames = 
       procedureParamNames.map( Blockly.possiblyPrefixMenuNameWith(Blockly.procedureParameterPrefix) )
       .concat(handlerParamNames.map( Blockly.possiblyPrefixMenuNameWith(Blockly.handlerParameterPrefix) ))
       .concat(loopNames.map( Blockly.possiblyPrefixMenuNameWith(Blockly.loopParameterPrefix) ))
       .concat(rangeNames.map( Blockly.possiblyPrefixMenuNameWith(Blockly.loopRangeParameterPrefix) ))
       .concat(localNames.map( Blockly.possiblyPrefixMenuNameWith(Blockly.localNamePrefix) ));
    allLexicalNames = Blockly.LexicalVariable.sortAndRemoveDuplicates(allLexicalNames);
  }
  return allLexicalNames;
}

/**
 * Return a sorted list of variable names for variable dropdown menus.
 * @return {!Array.<string>} Array of variable names.
 * @this {!Blockly.FieldLexicalVariable}
 */
Blockly.FieldLexicalVariable.dropdownCreate = function() {
  var variableList = this.getNamesInScope(); // [lyn, 11/10/12] Get all global, parameter, and local names
  // Variables are not language-specific, use the name as both the user-facing
  // text and the internal representation.
  var options = [];
  // [lyn, 11/10/12] Ensure variable list isn't empty
  if (variableList.length == 0) variableList = [" "];
  for (var x = 0; x < variableList.length; x++) {
    options[x] = [variableList[x], variableList[x]];
  }
  return options;
};

/**
 * Event handler for a change in variable name.
 * // [lyn, 11/10/12] *** Not clear this needs to do anything for lexically scoped variables. 
 * Special case the 'New variable...' and 'Rename variable...' options.
 * In both of these special cases, prompt the user for a new name.
 * @param {string} text The selected dropdown menu option.
 * @this {!Blockly.FieldLexicalVariable}
 */
Blockly.FieldLexicalVariable.dropdownChange = function(text) {
  if (text) {
    this.setText(text);
    Blockly.WarningHandler.checkErrors.call(this.sourceBlock_);
  }
  // window.setTimeout(Blockly.Variables.refreshFlyoutCategory, 1);
};


// [lyn, 11/18/12]
/**
 * Possibly add a digit to name to disintguish it from names in list. 
 * Used to guarantee that two names aren't the same in situations that prohibit this. 
 * @param {string} name Proposed name.
 * @param {string list} nameList List of names with which name can't conflict
 * @return {string} Non-colliding name.
 */
Blockly.FieldLexicalVariable.nameNotIn = function(name, nameList) {
  // First find the nonempty digit suffixes of all names in nameList that have the same prefix as name
  // e.g. for name "foo3" and nameList = ["foo", "bar4", "foo17", "bar" "foo5"]
  // suffixes is ["17", "5"]
  var namePrefixSuffix = Blockly.FieldLexicalVariable.prefixSuffix(name);
  var namePrefix = namePrefixSuffix[0];
  var nameSuffix = namePrefixSuffix[1];
  var emptySuffixUsed = false; // Tracks whether "" is a suffix. 
  var isConflict = false; // Tracks whether nameSuffix is used 
  var suffixes = [];   
  for (var i = 0; i < nameList.length; i++) {
    var prefixSuffix = Blockly.FieldLexicalVariable.prefixSuffix(nameList[i]);
    var prefix = prefixSuffix[0];
    var suffix = prefixSuffix[1];
    if (prefix === namePrefix) {
      if (suffix === nameSuffix) {
        isConflict = true;
      }
      if (suffix === "") {
        emptySuffixUsed = true;
      } else {
        suffixes.push(suffix); 
      }
    }
  } 
  if (! isConflict) {
    // There is no conflict; just return name
    return name; 
  } else if (! emptySuffixUsed) {
    // There is a conflict, but empty suffix not used, so use that
    return namePrefix;
  } else {
    // There is a possible conflict and empty suffix is not an option.
    // First sort the suffixes as numbers from low to high
    var suffixesAsNumbers = suffixes.map( function (elt, i, arr) { return parseInt(elt,10); } )
    suffixesAsNumbers.sort( function(a,b) { return a-b; } ); 
    // Now find smallest number >= 2 that is unused
    var smallest = 2; // Don't allow 0 or 1 an indices
    var index = 0; 
    while (index < suffixesAsNumbers.length) {
      if (smallest < suffixesAsNumbers[index]) {
        return namePrefix + smallest;
      } else if (smallest == suffixesAsNumbers[index]) {
        smallest++;
        index++;
      } else { // smallest is greater; move on to next one
        index++;
      }
    }
    // Only get here if exit loop
    return namePrefix + smallest;
  }
};

/**
 * Split name into digit suffix and prefix before it. 
 * Return two-element list of prefix and suffix strings. Suffix is empty if no digits. 
 * @param {string} name Input string
 * @return {string list} Two-element list of prefix and suffix
 */
Blockly.FieldLexicalVariable.prefixSuffix = function(name) {
  var prefix = name;
  var suffix = "";
  var matchResult = name.match(/^(.*?)(\d+)$/);
  if (matchResult) 
    return [matchResult[1], matchResult[2]]; // List of prefix and suffix
  else 
    return [name, ""];
}

Blockly.LexicalVariable = {};

// [lyn, 11/19/12] Rename global to a new name.
//
// [lyn, 10/26/13] Modified to replace sequences of internal spaces by underscores
// (none were allowed before), and to replace empty string by '_'.
// Without special handling of empty string, the connection between a declaration field and
// its references is lots.
Blockly.LexicalVariable.renameGlobal = function (newName) {

  // this is bound to field_textinput object 
  var oldName = this.text_;

  // [lyn, 10/27/13] now check legality of identifiers
  newName = Blockly.LexicalVariable.makeLegalIdentifier(newName);

  var globals = Blockly.FieldLexicalVariable.getGlobalNames(this.sourceBlock_); 
    // this.sourceBlock excludes block being renamed from consideration
  // Potentially rename declaration against other occurrences
  newName = Blockly.FieldLexicalVariable.nameNotIn(newName, globals);
  if ((! (newName === oldName)) && this.sourceBlock_.rendered) {
    // Rename getters and setters
    if (Blockly.mainWorkspace) {
      var blocks = Blockly.mainWorkspace.getAllBlocks(); 
      for (var i = 0; i < blocks.length; i++) {
        var block = blocks[i];
        var renamingFunction = block.renameLexicalVar;
        if (renamingFunction) {
            renamingFunction.call(block,
                                  Blockly.globalNamePrefix + Blockly.menuSeparator + oldName,
                                  Blockly.globalNamePrefix + Blockly.menuSeparator + newName);
        }
      }
    }
  }
  return newName;
};

/**
 * Rename the old name currently in this field to newName in the block assembly rooted
 * at the source block of this field (where "this" names the field of interest).
 * See the documentation for renameParamFromTo for more details.
 * @param newName
 * @returns {string} The (possibly changed version of) newName, which may be changed
 *   to avoid variable capture with both external declarations (declared above the
 *   declaration of this name) or internal declarations (declared inside the scope
 *   of this name).
 */
// [lyn, 11/19/12 (revised 10/11/13)]
// Rename procedure parameter, event parameter, local name, or loop index variable to a new name,
// avoiding variable capture in the scope of the param. Consistently renames all 
// references to the name in getter and setter blocks. The proposed new name 
// may be changed (by adding numbers to the end) so that it does not conflict
// with existing names. Returns the (possibly changed) new name.
//
// [lyn, 10/26/13] Modified to replace sequences of internal spaces by underscores
// (none were allowed before), and to replace empty string by '_'.
// Without special handling of empty string, the connection between a declaration field and
// its references is lost.
//
// [lyn, 11/15/13] Refactored monolithic renameParam into parts that are useful on their own
Blockly.LexicalVariable.renameParam = function (newName) {

  var htmlInput = Blockly.FieldTextInput.htmlInput_;
  if(htmlInput && htmlInput.defaultValue == newName){
    return newName;
  }
  // this is bound to field_textinput object 
  var oldName = this.text_; // name being changed to newName

  // [lyn, 10/27/13] now check legality of identifiers
  newName = Blockly.LexicalVariable.makeLegalIdentifier(newName);

  // Default behavior consistent with previous behavior is to use "false" for last argument --
  // I.e., will not rename inner declarations, but may rename newName
  return Blockly.LexicalVariable.renameParamFromTo(this.sourceBlock_, oldName, newName, false);
  // Default should be false (as above), but can also play with true:
  // return Blockly.LexicalVariable.renameParamFromTo(this.sourceBlock_, oldName, newName, true);
}

/**
 * [lyn, written 11/15/13, installed 07/01/14]
 * Refactored from renameParam and extended.
 * Rename oldName to newName in the block assembly rooted at this block
 * (where "this" names the block of interest). The names may refer to any nonglobal
 * parameter name (procedure parameter, event parameter, local name, or loop index variable).
 * This function consistently renames all references to oldName by newName in all
 * getter and setter blocks that refer to oldName, correctly handling inner declarations
 * that use oldName. In cases where renaming oldName to newName would result in variable
 * capture of newName by another declaration, such capture is avoided by either:
 *    1. (If renameCapturables is true):  consistently renaming the capturing declarations
 *       (by adding numbers to the end) so that they do not conflict with newName (or each other).
 *    2. (If renameCapturables is false): renaming the proposed newName (by adding
 *       numbers to the end) so that it does not conflict with capturing declarations).
 * @param block  the root source block containing the parameter being renamed
 * @param oldName
 * @param newName
 * @param renameCapturables in capture situations, determines whether capturing declarations
 *   are renamed (true) or newName is renamed (false)
 * @returns {string} if renameCapturables is true, returns the given newName; if renameCapturables
 *   is false, returns the (possibly renamed version of) newName, which may be changed
 *   to avoid variable capture with both external declarations (declared above the
 *   declaration of this name) or internal declarations (declared inside the scope
 *   of this name).
 */
Blockly.LexicalVariable.renameParamFromTo = function (block, oldName, newName, renameCapturables) {
  if (block.type && block.type.indexOf("mutator") != -1) { // Handle mutator blocks specially
    return Blockly.LexicalVariable.renameParamWithoutRenamingCapturables(block, oldName, newName, []);
  } else if (renameCapturables) {
    Blockly.LexicalVariable.renameParamRenamingCapturables(block, oldName, newName);
    return newName;
  } else {
    return Blockly.LexicalVariable.renameParamWithoutRenamingCapturables(block, oldName, newName, []);
  }
}

/**
 * [lyn, written 11/15/13, installed 07/01/14]
 * Rename oldName to newName in the block assembly rooted at this block.
 * In the case where newName would be captured by an internal declaration,
 *  consistently rename the declaration and all its uses to avoid variable capture.
 * In the case where newName would be captured by an external declaration, throw an exception.
 * @param sourceBlock  the root source block containing the declaration of oldName
 * @param oldName
 * @param newName
 */
Blockly.LexicalVariable.renameParamRenamingCapturables = function (sourceBlock, oldName, newName) {
  if (newName !== oldName) { // Do nothing if names are the same
    var namesDeclaredHere = sourceBlock.declaredNames ? sourceBlock.declaredNames() : [];
    if (namesDeclaredHere.indexOf(oldName) == -1) {
      throw "Blockly.LexicalVariable.renamingCapturables: oldName " + oldName +
          " is not in declarations {" + namesDeclaredHere.join(',') + "}";
    }
    var namesDeclaredAbove = Blockly.FieldLexicalVariable.getNamesInScope(sourceBlock);
    var declaredNames = namesDeclaredHere.concat(namesDeclaredAbove);
    // Should really check which forbidden names are free vars in the body of declBlock.
    if (declaredNames.indexOf(newName) != -1) {
      throw "Blockly.LexicalVariable.renameParamRenamingCapturables: newName " + newName +
            " is in existing declarations {" + declaredNames.join(',') + "}";
    } else {
      if (sourceBlock.renameBound) {
        var boundSubstitution = Blockly.Substitution.simpleSubstitution(oldName,newName);
        var freeSubstitution = new Blockly.Substitution(); // an empty substitution
        sourceBlock.renameBound(boundSubstitution, freeSubstitution);
      } else {
        throw "Blockly.LexicalVariable.renameParamRenamingCapturables: block " + sourceBlock.type +
               " is not a declaration block."
      }
    }
  }
}

/**
 * [lyn, written 11/15/13, installed 07/01/14]
 * Rename all free variables in this block according to the given renaming
 * @param block: any block
 * @param freeRenaming: a dictionary (i.e., object) mapping old names to new names
 */
Blockly.LexicalVariable.renameFree = function (block, freeSubstitution) {
  if (block) { // If block is falsey, do nothing.
    if (block.renameFree) {  // should be defined on every declaration block
      block.renameFree(freeSubstitution);
    } else {
      block.getChildren().map( function(blk) { Blockly.LexicalVariable.renameFree(blk, freeSubstitution); } );
    }
  }
}

/**
 * [lyn, written 11/15/13, installed 07/01/14]
 * Return a nameSet of all free variables in the given block
 * @param block
 * @returns (NameSet) set of all free names in block
 */
Blockly.LexicalVariable.freeVariables = function (block) {
  var result = [];
  if (!block) { // input and next block slots might not empty
    result = new Blockly.NameSet();
  } else if (block.freeVariables) { // should be defined on every declaration block
    result = block.freeVariables();
  } else {
    var nameSets = block.getChildren().map( function(blk) { return Blockly.LexicalVariable.freeVariables(blk); } );
    result =  Blockly.NameSet.unionAll(nameSets);
  }
  // console.log("freeVariables(" + (block ? block.type : "*empty-socket*") + ") = " + result.toString());
  return result;
}

/**
 * [lyn, written 11/15/13, installed 07/01/14] Refactored from renameParam
 * Rename oldName to newName in the block assembly rooted at this block.
 * In the case where newName would be captured by internal or external declaration,
 * change it to a name (with a number suffix) that would not be captured.
 * @param sourceBlock  the root source block containing the declaration of oldName
 * @param oldName
 * @param newName
 *  @returns {string} the (possibly renamed version of) newName, which may be changed
 *   to avoid variable capture with both external declarations (declared above the
 *   declaration of this name) or internal declarations (declared inside the scope
 *   of this name).
 */
Blockly.LexicalVariable.renameParamWithoutRenamingCapturables = function (sourceBlock, oldName, newName, OKNewNames) {
  if (oldName === newName) {
    return oldName;
  }
  var namesDeclaredHere = sourceBlock.declaredNames ? sourceBlock.declaredNames() : [];
//  if (namesDeclaredHere.indexOf(oldName) == -1) {
//    throw "Blockly.LexicalVariable.renamingCapturables: oldName " + oldName +
//        " is not in declarations {" + namesDeclaredHere.join(',') + "}";
//  }
  var sourcePrefix = "";
  if (Blockly.showPrefixToUser) {
    if (sourceBlock.type == "procedures_mutatorarg"
        || sourceBlock.type == "procedures_defnoreturn"
        || sourceBlock.type == "procedures_defreturn") {
      sourcePrefix = Blockly.procedureParameterPrefix;
    } else if (sourceBlock.type == "controls_forEach") {
      sourcePrefix = Blockly.loopParameterPrefix;
    } else if ( sourceBlock.type == "controls_forRange") {
      sourcePrefix = Blockly.loopRangeParameterPrefix;
    } else if (sourceBlock.type == "local_declaration_statement"
               || sourceBlock.type == "local_declaration_expression"
               || sourceBlock.type == "local_mutatorarg") {
      sourcePrefix = Blockly.localNamePrefix;
    }
  }
  var helperInfo = Blockly.LexicalVariable.renameParamWithoutRenamingCapturablesInfo(sourceBlock, oldName, sourcePrefix);
  var blocksToRename = helperInfo[0];
  var capturables = helperInfo[1];
  var declaredNames = []; // declared names in source block, with which newName cannot conflict
  if (sourceBlock.declaredNames) {
    declaredNames = sourceBlock.declaredNames();
    // Remove oldName from list of names. We can rename oldName to itself if we desire!
    var oldIndex = declaredNames.indexOf(oldName);
    if (oldIndex != -1) {
      declaredNames.splice(oldIndex,1);
    }
    // Remove newName from list of declared names if it's in OKNewNames.
    if (OKNewNames.indexOf(newName) != -1) {
      var newIndex = declaredNames.indexOf(newName);
      if (newIndex != -1) {
        declaredNames.splice(newIndex,1);
      }
    }
  }
  var conflicts = Blockly.LexicalVariable.sortAndRemoveDuplicates(capturables.concat(declaredNames));
  newName = Blockly.FieldLexicalVariable.nameNotIn(newName, conflicts);

  /* console.log("LYN: rename Param: oldName = " + oldName + "; newName = " + newName
   + "; sourcePrefix = " + sourcePrefix
   + "; capturables = " + JSON.stringify(capturables)
   + "; declaredNames = " + JSON.stringify(declaredNames)
   + "; conflicts = " + JSON.stringify(conflicts)
   + "; blocksToRename = " + JSON.stringify(blocksToRename.map( function(elt) { return elt.type; })));
   */
  if (! (newName === oldName)) { // Special case: if newName is oldName, we're done!
    // [lyn, 12/27/2012] I don't understand what this code is for.
    //  I think it had something to do with locals that has now been repaired? 
    /* var oldNameInDeclaredNames = false;
      for (var i = 0; i < declaredNames.length; i++) {
      if(oldName === declaredNames[i]){
        oldNameInDeclaredNames = true;
      }
    }
    if(!oldNameInDeclaredNames){ 
    */
    var oldNameValid = (declaredNames.indexOf(oldName) != -1);
    if(!oldNameValid) {
      // Rename getters and setters
      for (var i = 0; i < blocksToRename.length; i++) {
        var block = blocksToRename[i];
        var renamingFunction = block.renameLexicalVar;
        if (renamingFunction) {
          renamingFunction.call(block,
                                (Blockly.possiblyPrefixMenuNameWith(sourcePrefix))(oldName),
                                (Blockly.possiblyPrefixMenuNameWith(sourcePrefix))(newName));
        }
      }
    }
  }
  return newName;
}

/**
 * [lyn, written 11/15/13, installed 07/01/14] Refactored from renameParam()
 * @param oldName
 * @returns {pair} Returns a pair of
 *   (1) All getter/setter blocks that reference oldName
 *   (2) A list of all non-global names to which oldName cannot be renamed because doing
 *       so would change the reference "wiring diagram" and thus the meaning
 *       of the program. This is the union of:
 *          (a) all names declared between the declaration of oldName and a reference to old name; and
 *          (b) all names declared in a parent of the oldName declaration that are referenced in the scope of oldName.
 *       In the case where prefixes are used (e.g., "param a", "index i, "local x")
 *       this is a list of *unprefixed* names.
 */
Blockly.LexicalVariable.renameParamWithoutRenamingCapturablesInfo = function (sourceBlock, oldName, sourcePrefix) {
  // var sourceBlock = this; // The block containing the declaration of oldName
    // sourceBlock is block in which name is being changed. Can be one of:
    // * For procedure param: procedures_mutatorarg, procedures_defnoreturn, procedures_defreturn
    //   (last two added by lyn on 10/11/13).
    // * For local name: local_mutatorarg, local_declaration_statement, local_declaration_expression
    // * For loop name: controls_forEach, controls_forRange
    // * For event param, event handler block (new on 10/13/13)
  var inScopeBlocks = []; // list of root blocks in scope of oldName and in which
                          // renaming must take place.
  if (sourceBlock.blocksInScope) { // Find roots of blocks in scope.
    inScopeBlocks = sourceBlock.blocksInScope();
  }
  // console.log("inScopeBlocksRoots: " + JSON.stringify(inScopeBlocks.map( function(elt) { return elt.type; })));

  // referenceResult is Array of (0) list of getter/setter blocks refering to old name and
  //                             (1) capturable names = names to which oldName cannot be renamed
  //                                 without changing meaning of program.
  var referenceResults = inScopeBlocks.map( function(blk) { return Blockly.LexicalVariable.referenceResult(blk, oldName, sourcePrefix, []); } );
  var blocksToRename = []; // A list of all getter/setter blocks whose that reference oldName
                           // and need to have their name changed to newName
  var capturables = []; // A list of all non-global names to which oldName cannot be renamed because doing
                        // so would change the reference "wiring diagram" and thus the meaning
                        // of the program. This is the union of:
                        // (1) all names declared between the declaration of oldName and a reference
                        //     to old name; and
                        // (2) all names declared in a parent of the oldName declaration that
                        //     are referenced in the scope of oldName.
                        // In the case where prefixes are used (e.g., "param a", "index i, "local x")
                        // this is a list of *unprefixed* names.
  for (var r = 0; r < referenceResults.length; r++) {
    blocksToRename = blocksToRename.concat(referenceResults[r][0]);
    capturables = capturables.concat(referenceResults[r][1]);
  }
  capturables = Blockly.LexicalVariable.sortAndRemoveDuplicates(capturables);
  return [blocksToRename, capturables];
}

/**
 * [lyn, 10/27/13]
 * Checks an identifier for validity. Validity rules are a simplified version of Kawa identifier rules.
 * They assume that the YAIL-generated version of the identifier will be preceded by a legal Kawa prefix:
 *
 *   <identifier> = <first><rest>*
 *   <first> = letter U charsIn("_$?~@")
 *   <rest> = <first> U digit
 *
 *   Note: an earlier version also allowed characters in "!&%.^/+-*>=<",
 *   but we decided to remove these because (1) they may be used for arithmetic,
 *   logic, and selection infix operators in a future AI text language, and we don't want
 *   things like a+b, !c, d.e to be ambiguous between variables and other expressions.
 *   (2) using chars in "><&" causes HTML problems with getters/setters in flydown menu.
 *
 * First transforms the name by removing leading and trailing whitespace and
 * converting nonempty sequences of internal whitespace to '_'.
 * Returns a result object of the form {transformed: <string>, isLegal: <bool>}, where:
 * result.transformed is the transformed name and result.isLegal is whether the transformed
 * named satisfies the above rules.
 */
Blockly.LexicalVariable.checkIdentifier = function(ident) {
  var transformed = ident.trim() // Remove leading and trailing whitespace
                         .replace(/[\s\xa0]+/g, '_'); // Replace nonempty sequences of internal spaces by underscores
  // [lyn, 06/11/14] Previous definition focused on *legal* characters:
  //
  //    var legalRegexp = /^[a-zA-Z_\$\?~@][\w_\$\?~@]*$/;
  //
  // Unfortunately this is geared only to English, and prevents i8n names (such as Chinese identifiers).
  // In order to handle i8n, focus on avoiding illegal chars rather than accepting only legal ones.
  // This is a quick solution. Needs more careful thought to work for every language. In particular,
  // need to look at results of Java's Character.isJavaIdentifierStart(int) and
  // Character.isJavaIdentifierPart(int)
  // Note: to take complement of character set, put ^ first.
  // Note: to include '-' in character set, put it first or right after ^
  var legalRegexp = /^[^-0-9!&%^/>=<`'"#:;,\\\^\*\+\.\(\)\|\{\}\[\]\ ][^-!&%^/>=<'"#:;,\\\^\*\+\.\(\)\|\{\}\[\]\ ]*$/;
  // " Make Emacs Happy
  var isLegal = transformed.search(legalRegexp) == 0;
  return {isLegal: isLegal, transformed: transformed};
}

Blockly.LexicalVariable.makeLegalIdentifier = function(ident) {
  var check = Blockly.LexicalVariable.checkIdentifier(ident);
  if (check.isLegal) {
    return check.transformed;
  } else if (check.transformed === '') {
    return '_';
  } else {
    return 'name' // Use identifier 'name' to replace illegal name
  }
}

// [lyn, 11/19/12] Given a block, return an Array of
//   (0) all getter/setter blocks referring to name in block and its children
//   (1) all (unprefixed) names within block that would be captured if name were renamed to one of those names. 
// If Blockly.showPrefixToUser, prefix is the prefix associated with name; otherwise prefix is "".
// env is a list of internally declared names in scope at this point;
//   if Blockly.usePrefixInYail is true, the env names have prefixes, otherwise they do not.
// [lyn, 12/25-27/2012] Updated to 
//    (1) add prefix argument, 
//    (2) handle local declaration statements/expressions, and
//    (3) treat prefixes correctly when they're used. 
Blockly.LexicalVariable.referenceResult = function (block, name, prefix, env) {
  if (! block) { // special case when block is null
    return [[],[]];
  }
  // [lyn, 11/29/12] Added forEach and forRange loops
  var referenceResults = []; // For collected reference results in subblocks
  // Handle constructs that can introduce names here specially (should figure out a better way to generalize this!)
  if (block.type === "controls_forEach") {
    var loopVar = block.getFieldValue('VAR');
    if (Blockly.usePrefixInYail) { // Invariant: Blockly.showPrefixToUser must also be true!
      loopVar = (Blockly.possiblyPrefixMenuNameWith(Blockly.loopParameterPrefix))(loopVar)
    }
    var newEnv = env.concat([loopVar]);
    var listResults = Blockly.LexicalVariable.referenceResult(block.getInputTargetBlock('LIST'), name, prefix, env);
    var doResults = Blockly.LexicalVariable.referenceResult(block.getInputTargetBlock('DO'), name, prefix, newEnv);
    var nextResults = Blockly.LexicalVariable.referenceResult(Blockly.LexicalVariable.getNextTargetBlock(block), name, prefix, env);
    referenceResults = [listResults,doResults,nextResults];
  } else if (block.type === "controls_forRange") {
    var loopVar = block.getFieldValue('VAR');
    if (Blockly.usePrefixInYail) { // Invariant: Blockly.showPrefixToUser must also be true!
      loopVar = (Blockly.possiblyPrefixMenuNameWith(Blockly.loopRangeParameterPrefix))(loopVar)
    }
    var newEnv = env.concat([loopVar]);
    var startResults = Blockly.LexicalVariable.referenceResult(block.getInputTargetBlock('START'), name, prefix, env);
    var endResults = Blockly.LexicalVariable.referenceResult(block.getInputTargetBlock('END'), name, prefix, env);
    var stepResults = Blockly.LexicalVariable.referenceResult(block.getInputTargetBlock('STEP'), name, prefix, env);
    var doResults = Blockly.LexicalVariable.referenceResult(block.getInputTargetBlock('DO'), name, prefix, newEnv);
    var nextResults = Blockly.LexicalVariable.referenceResult(Blockly.LexicalVariable.getNextTargetBlock(block), name, prefix, env);
    referenceResults = [startResults,endResults,stepResults,doResults,nextResults];
  } else if ((block.type === "local_declaration_statement") || (block.type === "local_declaration_expression")) {
    // Collect locally declared names ... 
    var localDeclNames = [];
    for(var i=0; block.getInput('DECL' + i); i++) {
      var localName = block.getFieldValue('VAR' + i);
      if (Blockly.usePrefixInYail) { // Invariant: Blockly.showPrefixToUser must also be true!
        localName = (Blockly.possiblyPrefixMenuNameWith(Blockly.localNamePrefix))(localName)
      }
      localDeclNames.push(localName);
    }
    var newEnv = env.concat(localDeclNames); // ... and add to environment
    // Collect locally initialization expressions: 
    var localInits = [];
    for(var i=0; block.getInput('DECL' + i); i++) {
      var init = block.getInputTargetBlock('DECL' + i); 
      if (init) { localInits.push(init); }
    }
    var initResults = localInits.map( function(init) { return Blockly.LexicalVariable.referenceResult(init, name, prefix, env); } );
    if (block.type === "local_declaration_statement") {
      var doResults = Blockly.LexicalVariable.referenceResult(block.getInputTargetBlock('STACK'), name, prefix, newEnv);
      var nextResults = Blockly.LexicalVariable.referenceResult(Blockly.LexicalVariable.getNextTargetBlock(block), name, prefix, env);
      referenceResults = initResults.concat([doResults,nextResults]); 
    } else { // (block.type === "local_declaration_expression") {
      var returnResults = Blockly.LexicalVariable.referenceResult(block.getInputTargetBlock('RETURN'), name, prefix, newEnv);
      referenceResults = initResults.concat([returnResults]); 
    }
  } else { // General case for blocks that do not introduce new names
   referenceResults = block.getChildren().map( function(blk) { return Blockly.LexicalVariable.referenceResult(blk, name, prefix, env); } );
  }
  var blocksToRename = [];
  var capturables = [];
  for (var r = 0; r < referenceResults.length; r++) {
    blocksToRename = blocksToRename.concat(referenceResults[r][0]);
    capturables = capturables.concat(referenceResults[r][1]);
  }
  // Base case: getters/setters is where all the interesting action occurs
  if ((block.type === "lexical_variable_get") || (block.type === "lexical_variable_set")) {
    var possiblyPrefixedReferenceName = block.getFieldValue('VAR');
    var unprefixedPair = Blockly.unprefixName(possiblyPrefixedReferenceName);
    var referencePrefix = unprefixedPair[0];
    var referenceName = unprefixedPair[1];
    var referenceNotInEnv = ((Blockly.usePrefixInYail && (env.indexOf(possiblyPrefixedReferenceName) == -1))
                             || ((!Blockly.usePrefixInYail) && (env.indexOf(referenceName) == -1)))
    if (!(referencePrefix === Blockly.globalNamePrefix)) {
      if ((referenceName === name) && referenceNotInEnv) {
        // if referenceName refers to name and not some intervening declaration, it's a reference to be renamed:
        blocksToRename.push(block);
        // Any intervening declared name with the same prefix as the searched for name can be captured:
        if (Blockly.usePrefixInYail) {
          for (var i = 0; i < env.length; i++) {
            // env is a list of prefixed names. 
            var unprefixedEntry = Blockly.unprefixName(env[i]);
            if (prefix === unprefixedEntry[0]) {
              capturables.push(unprefixedEntry[1]);
            }
          }
        } else { // Blockly.usePrefixInYail
          capturables = capturables.concat(env);        
        }
      } else if (referenceNotInEnv && (!Blockly.usePrefixInYail || prefix === referencePrefix)) {
        // If reference is not in environment, it's externally declared and capturable
        // When Blockly.usePrefixInYail is true, only consider names with same prefix to be capturable
        capturables.push(referenceName);
      }
    }
  }
  /* console.log("referenceResult from block of type " + block.type + 
             " with name " + name +
             " with prefix " + prefix +
             " with env " + JSON.stringify(env) +
             ": [" + JSON.stringify(blocksToRename.map( function(elt) { return elt.type; })) +
              ", " + JSON.stringify(capturables) + "]");
  */
  return [blocksToRename,capturables];
};

Blockly.LexicalVariable.sortAndRemoveDuplicates = function (strings) {
  var sorted = strings.sort();
  var nodups = []; 
  if (strings.length >= 1) {
    var prev = sorted[0];
    nodups.push(prev);
    for (var i = 1; i < sorted.length; i++) {
        if (! (sorted[i] === prev)) {
          prev = sorted[i];
          nodups.push(prev);
        }
    }
  }
  return nodups;
};

// [lyn, 11/23/12] Given a block, return the block connected to its next connection;
// If there is no next connection or no block, return null. 
Blockly.LexicalVariable.getNextTargetBlock = function (block) {
  if (block && block.nextConnection && block.nextConnection.targetBlock()) {
    return block.nextConnection.targetBlock();
  } else {
    return null;
  }
}

/**
 * [lyn, 11/16/13] Created
 * @param strings1: an array of strings
 * @param strings2: an array of strings
 * @returns true iff strings1 and strings2 have the same names in the same order; false otherwise
 */
Blockly.LexicalVariable.stringListsEqual = function (strings1, strings2) {
  var len1 = strings1.length;
  var len2 = strings2.length;
  if (len1 !== len2) {
    return false;
  } else {
    for (var i = 0; i < len1; i++) {
      if (strings1[i] !== strings2[i]) {
        return false;
      }
    }
  }
  return true; // get here iff lists are equal
}

/**
 * [lyn, 07/03/14] Created
 * [jis, 09/18/15] Refactored into two procedures
 * @param block: a getter or setter block
 *
 * Creates the mutation for the eventparam attribute
 */
Blockly.LexicalVariable.eventParamMutationToDom = function (block) {
  if (!block.eventparam) {      // Newly created block won't have one
    Blockly.LexicalVariable.getEventParam(block);
  }
  // At this point, if the variable is an event parameter, the
  // eventparam field will be set. Otherwise it won't be and
  // we return null
  if (!block.eventparam) {
    return null;
  }
  var mutation = document.createElement('mutation');
  var eventParam = document.createElement('eventparam');
  eventParam.setAttribute('name', block.eventparam);
  mutation.appendChild(eventParam);
  return mutation;
}

/**
 * @param block: a getter or setter block
 *
 * For getter or setter block of event parameters, sets the eventparam
 * field of the block which contains the default (untranslated =
 * English) name for event parameters.
 *
 */

Blockly.LexicalVariable.getEventParam = function (block) {
  // If it isn't undefined, then we have already computed it.
  if (block.eventparam !== undefined) {
    return block.eventparam;
  }
  block.eventparam = null;      // So if we leave without setting it to
                                // some value, we know we have already
                                // evaluated it.
  var prefixPair = Blockly.unprefixName(block.getFieldValue("VAR"));
  var prefix = prefixPair[0];
  if (prefix !== Blockly.globalNamePrefix) {
    var name = prefixPair[1];
    var child = block;
    var parent = block.getParent();
    while (parent) {
       // Walk up ancestor tree to determine if name is an event parameter name.
       if (parent.type === "component_event") {
         var untranslatedEventParams = parent.getParameters().map( function(param) {return param.name;});
         var translatedEventParams =  untranslatedEventParams.map(
             function (name) {return window.parent.BlocklyPanel_getLocalizedParameterName(name); }
         );
         var index = translatedEventParams.indexOf(name);
         if (index != -1) {
           block.eventparam = untranslatedEventParams[index];
           return null;         // return value is unimportant
         } else {
           return null;
         }
       } else if ( ( parent.type === "local_declaration_expression"
          && parent.getInputTargetBlock('RETURN') == child ) // only body is in scope of names
          || ( parent.type === "local_declaration_statement"
          && parent.getInputTargetBlock('STACK') == child ) // only body is in scope of names
           ) {
          var params = parent.declaredNames(); // [lyn, 10/13/13] Names from block, not localNames_ instance var
          if (params.indexOf(name) != -1) {
            return null; // Name is locally bound, not an event parameter.
          }
       } else if ( ( (parent.type === "controls_forEach") || (parent.type === "controls_forRange") )
                   && (parent.getInputTargetBlock('DO') == child) ) { // Only DO is in scope, not other inputs!
         var loopName = parent.getFieldValue('VAR');
           if (loopName == name) {
             return null; // Name is locally bound, not an event parameter.
         }
       }
      child = parent;
      parent = parent.getParent(); // keep moving up the chain.
    }
    return null; // If get to this point, there is no mutation
  }
}

/**
 * [lyn, 07/03/14] Created
 * @param block: a getter or setter block
 * @param xmlElement: an XML element
 * For getters and setters of event parameters, marks them specially
 * with a eventparam property to support i8n.
 * This is used only by Blockly.LexicalVariable.eventParameterDict
 */
Blockly.LexicalVariable.eventParamDomToMutation = function (block, xmlElement) {
  var children = goog.dom.getChildren(xmlElement);
  if (children.length == 1) { // Should be exactly one eventParam child
    var childNode = children[0];
    if (childNode.nodeName.toLowerCase() == 'eventparam') {
      var untranslatedEventName = childNode.getAttribute('name');
      block.eventparam = untranslatedEventName; // special property viewed by Blockly.LexicalVariable.eventParameterDict
    }
  }
}

/**
 * [lyn, 07/03/14] Created
 * @param block: a block
 * @returns a "dictionary" object that maps all default event parameter names
 *   used in the block to their translated names.
 */
Blockly.LexicalVariable.eventParameterDict = function (block) {
  var dict = {};
  var descendants = block.getDescendants();
  for (var i = 0, descendant; descendant = descendants[i]; i++) {
    if (descendant.eventparam) {
      // descendant.eventparam is the default event parameter name
      // descendant.getFieldValue('VAR') is the possibly translated name
      dict[descendant.eventparam] = descendant.getFieldValue('VAR');
    }
  }
  return dict;
}

