// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Performance optimization of the ConnectionDB object for MIT App Inventor.
 * This file provides a check for addConnection/removeConnection so that if we are in the process
 * of a bulk operation (e.g., ai_inject) we don't have Blockly adding/removing connections as
 * each block is processed. The workspace will then make a single pass to reorder the connections
 * to maintain consistency with Blockly's expectations. This reduces the computational complexity
 * of mass operations over all blocks from O(n^2 log n) to O(n log n).
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

goog.provide('AI.Blockly.ConnectionDB');
goog.require('Blockly.ConnectionDB');


/**
 * A version of addConnection that maintains the invariant that the DB will contain the
 * connection after the call to addConnection. Blockly's version throws errors if the
 * connection is already in the database.
 *
 * @param connection The connection to be added
 * @private
 */
Blockly.ConnectionDB.prototype.addConnection = function(connection) {
  if (connection.inDB_) {
    return;  // already in the database
  }

  // Check whether we are in the bulk rendering mode to prevent O(n^2)
  // insertion sort runtime.
  if (connection.getSourceBlock() && connection.getSourceBlock().workspace &&
    connection.getSourceBlock().workspace.bulkRendering) {
    // We are in the middle of a bulk rendering option, so just add the
    // connection and we will sort later using native array quicksort via the
    // Blockly.WorkspaceSvg.requestConnectionDBUpdate() method.
    this.push(connection);
    connection.inDB_ = true;
    return;
  }

  if (connection.getSourceBlock().isInFlyout) {
    // Don't bother maintaining a database of connections in a flyout.
    return;
  }
  var position = this.findPositionForConnection_(connection);
  this.splice(position, 0, connection);
  connection.inDB_ = true;
};

/**
 * A version of removeConnection_ that maintains the invariant that the DB won't contain the
 * connection after the call to removeConnection_. Blockly's version throws errors if the
 * connection isn't in the database.
 *
 * @param connection The connection to be removed
 * @private
 */
Blockly.ConnectionDB.prototype.removeConnection_ = function(connection) {
  if (!connection.inDB_) {
    return;  // connection already not in database
  }

  if (connection.getSourceBlock() && connection.getSourceBlock().workspace &&
    connection.getSourceBlock().workspace.bulkRendering) {
    connection.inDB_ = false;
    return;
  }

  connection.inDB_ = false;
  var removalIndex = this.findConnection(connection);
  if (removalIndex > -1) {
    // connection found, remove from database
    this.splice(removalIndex, 1);
  }
};

/**
 * O(n) removal of duplicate connections.
 */
Blockly.ConnectionDB.prototype.removeDupes = function() {
  for (var i = 0; i < this.length - 1; i++) {
    if (this[i] == this[i+1]) {
      this.splice(i, 1);
      i--;
    }
  }
};
