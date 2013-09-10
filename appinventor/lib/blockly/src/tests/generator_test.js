/**
 * Blockly Tests
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
'use strict';

function test_get() {
  var language1 = Blockly.Generator.get('INTERCAL');
  var language2 = Blockly.Generator.get('INTERCAL');
  assertTrue('Creating a language.', language1 && (typeof language1 == 'object'));
  assertTrue('Language is singleton.', language1 === language2);
}

function test_prefix() {
  assertEquals('Prefix nothing.', '', Blockly.Generator.prefixLines('', ''));
  assertEquals('Prefix a word.', '@Hello', Blockly.Generator.prefixLines('Hello', '@'));
  assertEquals('Prefix one line.', '12Hello\n', Blockly.Generator.prefixLines('Hello\n', '12'));
  assertEquals('Prefix two lines.', '***Hello\n***World\n', Blockly.Generator.prefixLines('Hello\nWorld\n', '***'));
}
