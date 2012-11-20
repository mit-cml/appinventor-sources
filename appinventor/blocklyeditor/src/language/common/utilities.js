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

// Create a unique object to represent the type InstantInTime,
// used in the Clock component
Blockly.Language.InstantInTime = function () { return 'InstantInTime'; };


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
	'InstantInTime':Blockly.Language.InstantInTime,
        'any':null

        //add  more types here
    }

Blockly.Language.YailTypeToBlocklyType = function(yail) {
    var bType = Blockly.Language.YailTypeToBlocklyTypeMap[yail];
    if (bType != null || yail == 'any') {
        return bType;
    } else {
        throw new Error("Unknown Yail type: " + yail + " -- YailTypeToBlocklyType");
    }
}


// Blockly doesn't wrap tooltips, so these can get too wide.  We'll create our own tooltip setter
// that wraps to length 60.

Blockly.Language.setTooltip = function(block, tooltip) {
    block.setTooltip(Blockly.Language.wrapSentence(tooltip, 60));
}

// Wrap a string by splitting at spaces. Permit long chunks if there
// are no spaces.

Blockly.Language.wrapSentence = function(str, len) {
  str = str.trim();
  if (str.length < len) return str;
  place = (str.lastIndexOf(" ", len));
  if (place == -1) {
    return str.substring(0, len).trim() + Blockly.Language.wrapSentence(str.substring(len), len);
  } else {
    return str.substring(0, place).trim() + "\n" + 
           Blockly.Language.wrapSentence(str.substring(place), len);
  }
}






