/**
 * Blockly Tests
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/google-blockly/
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

function test_safeName() {
  var varDB = new Blockly.Variables(['window', 'door']);
  assertEquals('SafeName empty.', 'var_unnamed', varDB.safeName_(''));
  assertEquals('SafeName ok.', 'foobar', varDB.safeName_('foobar'));
  assertEquals('SafeName number start.', 'var_9lives',
               varDB.safeName_('9lives'));
  assertEquals('SafeName number end.', 'lives9', varDB.safeName_('lives9'));
  assertEquals('SafeName special chars.', '____', varDB.safeName_('!@#$'));
  assertEquals('SafeName reserved.', 'var_door', varDB.safeName_('door'));
}

function test_getVariable() {
  var varDB = new Blockly.Variables(['window', 'door']);
  assertEquals('Variable add #1.', 'Foo_bar', varDB.getVariable('Foo.bar'));
  assertEquals('Variable get #1.', 'Foo_bar', varDB.getVariable('Foo.bar'));
  assertEquals('Variable add #2.', 'Foo_bar2', varDB.getVariable('Foo bar'));
  assertEquals('Variable get #2.', 'Foo_bar2', varDB.getVariable('foo BAR'));
  assertEquals('Variable add #3.', 'var_door', varDB.getVariable('door'));
}

function test_getDistinctVariable() {
  var varDB = new Blockly.Variables(['window', 'door']);
  assertEquals('Variable distinct #1.', 'Foo_bar',
               varDB.getDistinctVariable('Foo.bar'));
  assertEquals('Variable distinct #2.', 'Foo_bar2',
               varDB.getDistinctVariable('Foo.bar'));
  varDB.reset();
  assertEquals('Variable distinct #3.', 'Foo_bar',
               varDB.getDistinctVariable('Foo.bar'));
}
