/**
 * @fileoverview Represent sets of strings and numbers as JavaScript objects
 *   with an elements field that is itself an object mapping each element to true.
 * Note that ECMAScript 6 supports sets, but we cannot rely on sites using this recent a version
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */
'use strict';

/**
 * History:
 * [lyn, 06/30/14] added to ai2inter (should also add to master)
 * [lyn, 11/16/13] created
 */

goog.provide('Blockly.NameSet');

/**
 * Construct a set from a list. If no list is provided, construct the empty set.
 */
Blockly.NameSet = function(names) {
  if (!names) {
    names = [];
  }
  this.elements = {};
  for (var i = 0, name; name = names[i]; i++) {
    this.elements[name] = true;
  }
}

/**
 * Set membership
 * @param x: any value
 * @returns true if x is in set and false otherwise
 */
Blockly.NameSet.prototype.isMember = function(x) {
  return !!this.elements[x]; // !! converts falsey to false
}

/**
 * Set emptiness
 * @returns true if set is empty and false otherwise.
 */
Blockly.NameSet.prototype.isEmpty = function() {
  for(var elt in this.elements) {
    return false;
  }
  return true;
}

/**
 * Set size
 * @returns the number of elements in the set
 */
Blockly.NameSet.prototype.size = function() {
  var size = 0;
  for(var elt in this.elements) {
    size++;
  }
  return size;
}

/**
 * Return a list (i.e. array) of names in this set, in lexicographic order.
 */
Blockly.NameSet.prototype.toList = function() {
  var result = [];
  for (var elt in this.elements) {
    result.push(elt);
  }
  return result.sort();
}

/**
 * @returns a string representation of this set.
 */
Blockly.NameSet.prototype.toString = function() {
  return "Blockly.NameSet{" + this.toList().join(",")  + "}";
}

/**
 * Return a copy of this set
 */
Blockly.NameSet.prototype.copy = function() {
  var result = new Blockly.NameSet();
  for (var elt in this.elements) {
    result.insert(elt);
  }
  return result;
}

/**
 * Change this set to have the same elements as otherSet
 */
Blockly.NameSet.prototype.mirror = function(otherSet) {
  for (var elt in this.elements) {
    delete this.elements[elt];
  }
  for (var elt in otherSet.elements) {
    this.elements[elt] = true;
  }
}

/************************************************************
 * DESTRUCTIVE OPERATIONS
 * Change the existing set
 ************************************************************/

/**
 * Destructive set insertion
 * Insert x into the set. Does not complain if x already in the set.
 * @param x: any value
 */
Blockly.NameSet.prototype.insert = function(x) {
  this.elements[x] = true;
}

/**
 * Destructive set deletion.
 * Removes x from the set. Does not complain if x not in the set.
 * Note: This used to be called just "delete" but delete is a reserved
 * word, so we call this deleteName instead
 *
 * @param x: any value
 */
Blockly.NameSet.prototype.deleteName = function(x) {
  delete this.elements[x];
}

/**
 * Destructive set union
 * Change this set to have the union of its elements with the elements of the other set
 * @param otherSet: a NameSet
 */
Blockly.NameSet.prototype.unite = function(otherSet) {
  for (var elt in otherSet.elements) {
    this.elements[elt] = true;
  }
}

/**
 * Destructive set intersection
 * Change this set to have the intersection of its elements with the elements of the other set
 * @param otherSet: a NameSet
 */
Blockly.NameSet.prototype.intersect = function(otherSet) {
  for (var elt in this.elements) {
    if (!otherSet.elements[elt]) {
      delete this.elements[elt];
    }
  }
}

/**
 * Destructive set difference
 * Change this set to have the difference of its elements with the elements of the other set
 * @param otherSet: a NameSet
 */
Blockly.NameSet.prototype.subtract = function(otherSet) {
  for (var elt in this.elements) {
    if (otherSet.elements[elt]) {
      delete this.elements[elt];
    }
  }
}

/**
 * Destructive set renaming
 * Modifies existing set to rename those elements that are in the given renaming.
 * Since multiple elements may rename to the same element, this may reduce the
 * size of the set.
 * @param renaming: a substitution mapping old names to new names
 *
 */
Blockly.NameSet.prototype.rename = function(substitution) {
  this.mirror(this.renamed(substitution));
}

/************************************************************
 * NONDESTRUCTIVE OPERATIONS
 * Return new sets/lists/strings
 ************************************************************/

/**
 * Nondestructive set insertion
 * Set insertion. Insert x into the set. Does not complain if x already in the set.
 * @param x: any value
 */
Blockly.NameSet.prototype.insertion = function(x) {
  var result = this.copy();
  result.insert(x);
  return result;
}

/**
 * Nondestructive set deletion.
 * Returns a new set containing the elements of this set except for x.
 * * @param x: any value
 */
Blockly.NameSet.prototype.deletion = function(x) {
  var result = this.copy();
  result.deleteName(x);
  return result;
}


/**
 * Nondestructive set union
 * @param otherSet: a NameSet
 * @returns a new set that is the union of this set and the other set.
 */
Blockly.NameSet.prototype.union = function(otherSet) {
  var result = this.copy();
  result.unite(otherSet);
  return result;
}

/**
 * Nondestructive set intersection
 * @param otherSet: a NameSet
 * @returns a new set that is the intersection of this set and the other set.
 */
Blockly.NameSet.prototype.intersection = function(otherSet) {
  var result = this.copy();
  result.intersect(otherSet);
  return result;
}

/**
 * Nondestructive set difference
 * @param otherSet: a NameSet
 * @returns a new set that is the differences of this set and the other set.
 */
Blockly.NameSet.prototype.difference = function(otherSet) {
  var result = this.copy();
  result.subtract(otherSet);
  return result;
}

/**
 * @param renaming: a substitution mapping old names to new names
 * @returns a new set that renames the elements of this set using the given renaming.
 * If a name is not in the dictionary, it is inserted unchange in the output set.
 */
Blockly.NameSet.prototype.renamed = function(substitution) {
  var result = new Blockly.NameSet();
  for (var elt in this.elements) {
    var renamedElt = substitution.apply(elt);
    if (renamedElt) {
      result.insert(renamedElt);
    } else {
      result.insert(elt);
    }
  }
  return result;
}

/**
 * @param setList: an array of NameSets
 * @returns a NameSet that is the union of all the given sets
 */
Blockly.NameSet.unionAll = function(setList) {
  var result = new Blockly.NameSet();
  for (var i = 0, oneSet; oneSet = setList[i]; i++) {
    result.unite(oneSet)
  }
  return result;
}

/**
 * @param setList: an array of NameSets
 * @returns a NameSet that is the intersection of all the given sets
 */
Blockly.NameSet.intersectAll = function(setList) {
  if (setList.length == 0) {
    return new Blockly.NameSet();
  } else {
    var result = setList[0];
    for (var i = 1, oneSet; oneSet = setList[i]; i++) {
      result.intersect(oneSet)
    }
    return result;
  }
}
