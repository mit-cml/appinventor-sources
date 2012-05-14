/**
 * Visual Blocks Language
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

/**
 * @fileoverview Helper functions for generating Dart for blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Dart = Blockly.Generator.get('Dart');

/**
 * List of illegal variable names.
 * This is not intended to be a security feature.  Blockly is 100% client-side,
 * so bypassing this list is trivial.  This is intended to prevent users from
 * accidentally clobbering a built-in object or function.
 * @private
 */
Blockly.Dart.RESERVED_WORDS_ =
    // http://www.dartlang.org/docs/spec/latest/dart-language-specification.pdf
    // Section 14.1.1
    'break,case,catch,class,const,continue,default,do,else,extends,false,final,finally,for,if,in,is,new,null,return,super,switch,this,throw,true,try,var,void,while,' +
    // http://api.dartlang.org/dart_core.html
    'AssertionError,bool,Clock,Collection,Comparable,Completer,Date,double,Duration,Dynamic,Expect,FallThroughError,Function,Future,Futures,Hashable,HashMap,HashSet,int,Iterable,Iterator,LinkedHashMap,List,Map,Match,Math,num,Object,Options,Pattern,Queue,RegExp,Set,Stopwatch,String,StringBuffer,Strings,TimeZone,TypeError,BadNumberFormatException,ClosureArgumentMismatchException,EmptyQueueException,Exception,ExpectException,FutureAlreadyCompleteException,FutureNotCompleteException,IllegalAccessException,IllegalArgumentException,IllegalJSRegExpException,IndexOutOfRangeException,IntegerDivisionByZeroException,NoMoreElementsException,NoSuchMethodException,NotImplementedException,NullPointerException,ObjectNotClosureException,OutOfMemoryException,StackOverflowException,UnsupportedOperationException,WrongArgumentCountException';

/**
 * Initialise the database of variable names.
 */
Blockly.Dart.init = function() {
  if (!Blockly.Dart.variableDB_) {
    Blockly.Dart.variableDB_ =
        new Blockly.Variables(Blockly.Dart.RESERVED_WORDS_.split(','));
  } else {
    Blockly.Dart.variableDB_.reset();
  }

  // Create a dictionary of definitions to be printed before the code.
  Blockly.Dart.definitions_ = {};

  var defvars = [];
  var variables = Blockly.Variables.allVariables();
  for (var x = 0; x < variables.length; x++) {
    defvars[x] = 'var ' +
        Blockly.Dart.variableDB_.getDistinctVariable(variables[x]) + ';';
  }
  Blockly.Dart.definitions_['variables'] = defvars.join('\n');
};

/**
 * Prepend the generated code with the variable definitions.
 * @param {string} code Generated code.
 * @return {string} Completed code.
 */
Blockly.Dart.finish = function(code) {
  // Indent every line.
  code = '  ' + code.replace(/\n/g, '\n  ');
  code = code.replace(/\n\s+$/, '\n');
  code = 'main() {\n' + code + '}';

  // Convert the definitions dictionary into a list.
  var definitions = [];
  for (var name in Blockly.Dart.definitions_) {
    definitions.push(Blockly.Dart.definitions_[name]);
  }
  return definitions.join('\n') + '\n\n' + code;
};

/**
 * Naked values are top-level blocks with outputs that aren't plugged into
 * anything.  A trailing semicolon is needed to make this legal.
 * @param {string} line Line of generated code.
 * @return {string} Legal line of code.
 */
Blockly.Dart.scrubNakedValue = function(line) {
  return line + ';\n';
};

/**
 * Generate Dart code representing the specified value input.
 * @param {!Blockly.Block} block The block containing the input.
 * @param {number} index The index of the input (0-based).
 * @param {?boolean} opt_dropParens If true, don't surround code with paretheses
 *     since the caller already has a safe container.
 * @return {string} Generated code or '' if no blocks are connected.
 * @private
 */
Blockly.Dart.valueToCode_ = function(block, index, opt_dropParens) {
  var input = block.getValueInput(index);
  return this.blockToCode(input, opt_dropParens);
};

/**
 * Generate Dart code representing the statement.  Indent the code.
 * @param {!Blockly.Block} block The block containing the input.
 * @param {number} index The index of the input (0-based).
 * @return {string} Generated code or '' if no blocks are connected.
 * @private
 */
Blockly.Dart.statementToCode_ = function(block, index) {
  var input = block.getStatementInput(index);
  var code = this.blockToCode(input);
  if (code) {
    code = Blockly.Generator.prefixLines(code, '  ');
  }
  return code;
};

/**
 * Encode a string as a properly escaped Dart string, complete with quotes.
 * @param {string} string Text to encode.
 * @return {string} Dart string.
 * @private
 */
Blockly.Dart.quote_ = function(string) {
  // TODO: This is a quick hack.  Replace with goog.string.quote
  string = string.replace(/\\/g, '\\\\')
                 .replace(/\n/g, '\\\n')
                 .replace(/\$/g, '\\$')
                 .replace(/'/g, '\\\'');
  return '\'' + string + '\'';
};

/**
 * Common tasks for generating Dart from blocks.
 * Handles comments for the specified block and any connected value blocks.
 * Calls any statements following this block.
 * @param {!Blockly.Block} block The current block.
 * @param {string} code The Dart code created for this block.
 * @return {string} Dart code with comments and subsequent blocks added.
 * @private
 */
Blockly.Dart.scrub_ = function(block, code) {
  var commentCode = '';
  // Only collect comments for blocks that aren't inline.
  if (!block.outputConnection || !block.outputConnection.targetConnection) {
    // Collect comment for this block.
    var comment = block.getCommentText();
    if (comment) {
      commentCode += Blockly.Generator.prefixLines(comment, '// ') + '\n';
    }
    // Collect comments for all value arguments.
    // Don't collect comments for nested statements.
    for (var x = 0; x < block.inputList.length; x++) {
      if (block.inputList[x].type == Blockly.INPUT_VALUE) {
        var childBlock = block.inputList[x].targetBlock();
        if (childBlock) {
          var comment = Blockly.Generator.allNestedComments(childBlock);
          if (comment) {
            commentCode += Blockly.Generator.prefixLines(comment, '// ');
          }
        }
      }
    }
  }
  var nextBlock = block.nextConnection && block.nextConnection.targetBlock();
  var nextCode = this.blockToCode(nextBlock);
  return commentCode + code + nextCode;
};
