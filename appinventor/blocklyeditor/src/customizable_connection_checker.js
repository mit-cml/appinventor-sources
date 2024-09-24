// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology. All rights reserved.

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
        if (super.doTypeChecks(connA, connB)) {
            return true;
        }
        // Check if any of the connA checks are functions, amd if so call them
        for (let i = 0; i < connA.length; i++) {
            if (typeof connA[i] == "function" && connA[i](connB)) {
                return true;
            }
        }
        // Check if any of the connB checks are functions, amd if so call them
        for (let i = 0; i < connB.length; i++) {
            if (typeof connB[i] == "function" && connB[i](connB)) {
                return true;
            }
        }
        return false;
    }
}

// function connectionCheck(myConnection, otherConnection, opt_value) {
//     const otherTypeArray = otherConnection.getCheck();
//     if (!otherTypeArray) {  // Other connection accepts everything.
//         return true;
//     }
//
//     var block = myConnection.sourceBlock_;
//     var shouldIgnoreError = Blockly.mainWorkspace.isLoading;
//     var value = opt_value || block.getFieldValue('TEXT');
//
//     for (var i = 0; i < otherTypeArray.length; i++) {
//         if (otherTypeArray[i] == "String") {
//             return true;
//         } else if (otherTypeArray[i] == "Number") {
//             if (shouldIgnoreError) {
//                 // Error may be noted by WarningHandler's checkInvalidNumber
//                 return true;
//             } else if (Blockly.Blocks.Utilities.NUMBER_REGEX.test(value)) {
//                 // Value passes a floating point regex
//                 return !isNaN(parseFloat(value));
//             }
//         } else if (otherTypeArray[i] == "Key") {
//             return true;
//         } else if (otherTypeArray[i] == "Key") {
//             return true;
//         }
//     }
//     return false;
// };

const registrationType = Blockly.registry.Type.CONNECTION_CHECKER;
const registrationName = 'CustomizableConnectionChecker';

// Register the checker so that it can be used by name.
Blockly.registry.register(
    registrationType,
    registrationName,
    CustomizableConnectionChecker,
);

// export const pluginInfo = {
//     [registrationType]: registrationName,
// };
