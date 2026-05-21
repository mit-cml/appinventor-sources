// -*- mode: javascript;js-indent-level: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

'use strict';

goog.provide('AI.Blockly.Mixins.DynamicConnections');

/**
 *
 * @extends {Blockly.BlockSvg}
 */
AI.Blockly.Mixins.DynamicConnections = {
  /**
   *
   * @param {Blockly.RenderedConnection} connection
   */
  findInputIndexForConnection: function (connection) {
    if (!connection.targetConnection || connection.targetBlock().isInsertionMarker()) {
      return null;
    }

    var connectionIndex = -1;
    for (var i = 0, input; (input = this.inputList[i]); i++) {
      if (input.connection == connection) {
        connectionIndex = i;
        break;
      }
    }

    if (connectionIndex == this.inputList.length - 1) {
      // this connection is the last one and already has a block in it, so
      // we should add a new connection at the end.
      return this.inputList.length;
    }

    var nextInput = this.inputList[connectionIndex + 1];
    var nextConnection = nextInput.connection && nextInput.connection.targetConnection;
    if (nextConnection && !nextConnection.getSourceBlock().isInsertionMarker()) {
      // there's a block connected to the next input, so we should add a
      // connection before that one.
      return connectionIndex + 1;
    }

    return null;
  },
  onPendingConnection: function (connection) {
    var insertIndex = this.findInputIndexForConnection(connection);
    if (insertIndex === null) {
      return;
    }
    if (!this.tempinput) {
      this.tempinput = this.appendValueInput('TEMPINSERT');
      this.tempinput.init();
    }
    var originalIndex = this.inputList.indexOf(this.tempinput);
    if (insertIndex === 0) {
      this.tempinput.fields[0] = this.inputList[0].fields.splice(0, 1)[0];
    } else if (originalIndex === 0) {
      this.inputList[0].fields[0] = this.tempinput.fields.splice(0, 1)[0];
    }
    if (originalIndex !== insertIndex) {
      this.moveNumberedInputBefore(originalIndex, insertIndex);
    }
  },
  finalizeConnections: function () {
    if (!this.tempinput) {
      return;  // No pending connection.
    }
    if (this.tempinput.connection.targetConnection) {
      var repeatingInputName = this.repeatingInputName;
      this.inputList.forEach(function (input, i) {
        input.name = repeatingInputName + i;
      });
    } else if (this.tempinput) {
      this.removeInput('TEMPINSERT');
    }
    this.tempinput = undefined;
    for (var i = this.inputList.length - 1; i >= 0; i--) {
      if (!this.inputList[i].connection.targetConnection) {
        this.inputList.splice(i, 1);
      }
    }
    this.itemCount_ = this.inputList.length;
  }
}
