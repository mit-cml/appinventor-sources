/**
 * @fileoverview A substitution is an abstract set of input/output name pairs used for renaming. 
 * The inputs form the domain of the substitution; the outputs form the range. 
 * Applying a substitution to a name that's an input in its domain maps it to the associated output;
 * Applying a substitution to a name that's not in its domain returns the name unchanged.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */

/**
 * History:
 * [lyn, 06/30/14] added to ai2inter (should also add to master)
 * [lyn, 11/16-17/13] created
 */

goog.provide('Blockly.Substitution');

/**
 * If the arguments are two equal-length arrays of strings, construct a substitution
 *    by creating a bindings object that maps input strings from the first array
 *    to corresponding strings in the second.
 * If the argument is a single object describing the input/output bindings,
 *   construct a substitution from that.
 * If all other cases (e.g., no argument is provided)/ construct the empty substitution.
 */
Blockly.Substitution = function(arg1, arg2) {
  this.bindings = {}; // empty substitution is default.
  // Test that arg1 and arg2 are equal length arrays of strings
  if (Blockly.Substitution.isAllStringsArray(arg2)
      && Blockly.Substitution.isAllStringsArray(arg1)
      && arg1.length === arg2.length) {
    for (var i = 0; i < arg1.length; i++) {
      this.bindings[arg1[i]] = arg2[i];
    }
  } else if (!arg2 && Blockly.Substitution.isBindingsObject(arg1)) {
    // Make a copy of the bindings so not sharing binding structure with argument.
    this.bindings = {};
    for (var oldName in arg1) {
      this.bindings[oldName] = arg1[oldName];
    }
  }
}

/**
 * @param things
 * @returns true iff things is an array containing only strings. Otherwise returns false.
 */
Blockly.Substitution.isAllStringsArray = function(things) {
  // [lyn, 11/17/13] This fails for things that are obviously arrays. Dunno why
  // if (!(things instanceof Array)) {
  //  return false;
  //}
  if (typeof(things) !== "object" || !things.length) { // Say it's not an array if it's not an object with a length field.
    return false;
  }
  for (var i = 0; i < things.length; i++) {
    if (typeof(things[i]) !== "string") {
      return false;
    }
  }
  return true;
}

/**
 * @param obj An object
 * @returns true iff obj is an Object containting only string properties with string values.
 *   Otherwise returns false.
 */
Blockly.Substitution.isBindingsObject = function(thing) {
  // [lyn, 11/17/13] This fails for things that are obviously Objects. Dunno why
  // if (!(obj instanceof Object)) {
  //  return false;
  if (typeof(thing) != "object") {
    return false;
  } else {
    for (var prop in thing) {
      if (! (typeof(prop) === "string")
          || !(typeof(thing[prop] === "string"))) {
        return false
      }
    }
  }
  return true;
}

/**
 * @param oldName
 * @param newName
 * @returns {Substitution} A substitution with one pair from oldName to newName
 */
Blockly.Substitution.simpleSubstitution = function(oldName, newName) {
  var bindings = {};
  bindings[oldName] = newName;
  return new Blockly.Substitution(bindings);
}

/**
 * Apply a substitution to a name.
 * @param name: a string
 * @returns if the name is in the domain of the substition, returns the corresponding
 *   element in the range; otherwise, returns name unchanged.
 */
Blockly.Substitution.prototype.apply = function (name) {
  var output = this.bindings[name];
  if (output) {
    return output;
  } else {
    return name;
  }
}

/**
 * @param names: A list of strings
 * @returns {Array of strings} the result of applying this substitution to each element of names
 */
Blockly.Substitution.prototype.map = function(names) {
  thisSubst = this; // Need to name "this" for use in function closure passed to map.
  return names.map( function(name) { return thisSubst.apply(name); } );
}

/**
 * @returns {string} A string representation of this substitution
 */
Blockly.Substitution.prototype.toString = function() {
  var bindingStrings = [];
  for (var oldName in this.bindings) {
    bindingStrings.push(oldName + ":" + this.bindings[oldName]);
  }
  return "Blockly.Substitution{" + bindingStrings.sort().join(",") + "}";
}

/**
 * @returns {Substitution} a new copy of this substitution
 */
Blockly.Substitution.prototype.copy = function() {
  var newSubst = new Blockly.Substitution();
  for (var oldName in this.bindings) {
    newSubst.bindings[oldName] = this.bindings[oldName];
  }
  return newSubst;
}

/**
 * @param names: A list of strings
 * @returns {Substitution} a new substitution whose domain is the intersection of
 *   names and the domain of this substitution.
 */
Blockly.Substitution.prototype.restrictDomain = function(names) {
  var newSubst = new Blockly.Substitution();
  for (var i = 0; i < names.length; i++) {
    var result = this.bindings[names[i]];
    if (result) {
      newSubst.bindings[names[i]] = result;
    }
  }
  return newSubst;
}

/**
 * @param names: A list of strings
 * @returns {Substitution} a new substitution whose domain is the difference of
 *   the domain of this substitution and names
 */
Blockly.Substitution.prototype.remove = function(names) {
  var newSubst = new Blockly.Substitution();
  for (var oldName in this.bindings) {
    if (names.indexOf(oldName) == -1) {
      newSubst.bindings[oldName] = this.bindings[oldName];
    }
  }
  return newSubst;
}

/**
 * @param otherSubst: A substitution
 * @returns {Substitution} a new substitution whose domain is the union of the domains of
 *   this substitution and otherSubst. Any input/output mapping in otherSubst whose
 *   input is in this substitution overrides the input in this substitution.
 */
Blockly.Substitution.prototype.extend = function(otherSubst) {
  var newSubst = this.copy();
  for (var oldName in otherSubst.bindings) {
    newSubst.bindings[oldName] = otherSubst.bindings[oldName];
  }
  return newSubst;
}

/**
 * @returns {Array of String} a list of all the old names in the domain of this substitution.
 */
Blockly.Substitution.prototype.domain = function() {
  var oldNames = [];
  for (var oldName in this.bindings) {
    oldNames.push(oldName);
  }
  return oldNames.sort();
}

/**
 * @returns {Array of String} a copy of the input/output bindings in this substitution.
 */
Blockly.Substitution.prototype.getBindings = function() {
  var bindings = {};
  for (var oldName in this.bindings) {
    bindings[oldName] = this.bindings[oldName];
  }
  return bindings;
}





