// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

goog.provide('AI.Blockly.CustomizableConnectionChecker');

/**
 * A connection checker that enables custom per-connection type checking, by allowing
 * connection types to be functions that can be called to check the connection.
 * @implements {Blockly.IConnectionChecker}
 */
class CustomizableConnectionChecker extends Blockly.ConnectionChecker {
  /**
   * Constructor for the connection checker.
   */
  constructor() {
    super();
  }

  /**
   * Type check arrays must either intersect or both be null.
   * @override
   */
  doTypeChecks(connA, connB) {
    if (Blockly.getMainWorkspace().isLoading) {
      return true;
    }
    if (super.doTypeChecks(connA, connB)) {
      return true;
    }
    // Check if any of the connA checks are functions, and if so call them
    for (let i = 0; i < connA.length; i++) {
      if (typeof connA[i] == "function" && connA[i](connB)) {
        return true;
      }
    }
    // Check if any of the connB checks are functions, and if so call them
    for (let i = 0; i < connB.length; i++) {
      if (typeof connB[i] == "function" && connB[i](connB)) {
        return true;
      }
    }
    return false;
  }
}

const registrationType = Blockly.registry.Type.CONNECTION_CHECKER;
const registrationName = 'CustomizableConnectionChecker';

// Register the checker so that it can be used by name.
Blockly.registry.register(
    registrationType,
    registrationName,
    CustomizableConnectionChecker,
);

