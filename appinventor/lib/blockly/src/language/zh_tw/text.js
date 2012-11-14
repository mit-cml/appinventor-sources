/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/blockly/
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
 * @fileoverview Custom text blocks for Traditional Chinese.
 * @author gasolin@gmail.com (Fred Lin)
 */
'use strict';

Blockly.Language.text_charAt = {
  // Get a character from the string.
  category: Blockly.LANG_CATEGORY_TEXT,
  helpUrl: Blockly.LANG_TEXT_CHARAT_HELPURL,
  init: function() {
    this.setColour(160);
    this.setOutput(true, String);
    this.appendValueInput('AT')
        .setCheck(Number)
        .appendTitle(Blockly.LANG_TEXT_CHARAT_INPUT_AT);
    this.appendValueInput('VALUE')
        .setCheck(String)
        .appendTitle(Blockly.LANG_TEXT_CHARAT_INPUT_INTEXT);
    this.setInputsInline(true);
    this.setTooltip(Blockly.LANG_TEXT_CHARAT_TOOLTIP_1);
  }
};
