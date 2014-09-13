// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
/**
 * @license
 * @fileoverview Drop-down chooser of variables in the current lexical scope for App Inventor
 * @author fturbak@wellesley.com (Lyn Turbak)
 */

'use strict';

/**
 * Lyn's History:
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
  var globalNames = Blockly.FieldLexicalVariable.getGlobalNames(); // from global variable declarations
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
  var params
  var i;

  // [lyn, 12/24/2012] Abstract over name handling  
  function rememberName (name, list, prefix) {
    list.push(name);
    if (!innermostPrefix[name]) { // only set this if not already set from an inner scope.
      innermostPrefix[name] = prefix;
    }
  }
  
  child = this.block_;
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
  // [lyn, 11/24/12] Sort and remove duplicates from namespaces
  globalNames = Blockly.LexicalVariable.sortAndRemoveDuplicates(globalNames);
    
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
  // Return a list of all names in scope: global names followed by lexical ones.
  return globalNames.map( Blockly.prefixGlobalMenuName ).concat(allLexicalNames);
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
            renamingFunction.call(block, "global " + oldName, "global " + newName);
        }
      }
    }
  }
  return newName;
};

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
// its references is lots.
Blockly.LexicalVariable.renameParam = function (newName) {

  var htmlInput = Blockly.FieldTextInput.htmlInput_;
  if(htmlInput && htmlInput.defaultValue == newName){
    return newName;
  }
  // this is bound to field_textinput object 
  var oldName = this.text_; // name being changed to newName

  // [lyn, 10/27/13] now check legality of identifiers
  newName = Blockly.LexicalVariable.makeLegalIdentifier(newName);

  var sourceBlock = this.sourceBlock_; 
    // sourceBlock is block in which name is being changed. Can be one of:
    // * For procedure param: procedures_mutatorarg, procedures_defnoreturn, procedures_defreturn
    //   (last two added by lyn on 10/11/13).
    // * For local name: local_mutatorarg, local_declaration_statement, local_declaration_expression
    // * For loop name: controls_forEach, controls_forRange
    // * For event param, event handler block (new on 10/13/13)
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
  var inScopeBlocks = []; // list of root blocks in scope of oldName and in which 
                          // renaming must take place. 
  var declaredNames = []; // declared names in source block, with which newName cannot conflict
  if (sourceBlock.declaredNames) {
    declaredNames = sourceBlock.declaredNames();
    // Remove oldName from list of names. We can rename oldName to itself if we desire!
    var oldIndex = declaredNames.indexOf(oldName);
    if (oldIndex != -1) {
      declaredNames.splice(oldIndex,1);
    }
  }
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
  var conflicts = Blockly.LexicalVariable.sortAndRemoveDuplicates(capturables.concat(declaredNames));
  // Potentially rename declaration against capturables
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
 * [lyn, 10/27/13]
 * Checks an identifier for validity. Validity rules are a simplified version of Kawa identifier rules.
 * They assume that the YAIL-generated version of the identifier will be preceded by a legal Kawa prefix:
 *
 *   <identifier> = <first><rest>*
 *   <first> = letter U charsIn("_$?~@")
 *   <rest> = <first> U digit
 *
 *   Note: an earlier verison also allowed characters in "!&%.^/+-*>=<",
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
  var regexp = /^[a-zA-Z_\$\?~@][\w_\$\?~@]*$/;
  var isLegal = transformed.search(regexp) == 0;
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

