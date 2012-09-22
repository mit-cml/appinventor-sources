/**
 * Copyright 2012 Massachusetts Institute of Technology. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview language utilities for Blockly, modified for App Inventor
 * @author hal@mit.edu (Hal Abelson)
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) Blockly.Language = {};


// Convert Yail types to Blockly types
// Yail types are represented by strings: number, text, list, any, ...
// Blockly types are represented by objects: Number, String, ...
// and by the string "COMPONENT"
// The Yail type 'any' is repsented by Javascript null, to match
// Blockly's convention
Blockly.Language.YailTypeToBlocklyTypeMap =
    {
        'number':Number,
        'text':String,
        'boolean':Boolean,
        'list':Array,
        'component':"COMPONENT",
        'any':null

        //add  more types here
    }

Blockly.Language.YailTypeToBlocklyType = function(yail) {
    var bType = Blockly.Language.YailTypeToBlocklyTypeMap[yail];
    if (bType != null) {
        return bType;
    } else {
        throw new Error("Unknown Yail type: " + yail + " -- YailTypeToBlocklyType");
    }
}



