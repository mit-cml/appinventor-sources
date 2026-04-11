// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview YAIL primitive name-to-block mapping table and the
 * call-yail-primitive converter for the YAIL-to-Blocks converter.
 *
 * @author anthropic
 */

'use strict';

goog.provide('AI.YailToBlocks.Primitives');

goog.require('AI.SExprParser');
goog.require('AI.YailToBlocks');

// ---- Primitive dispatch ----

/**
 * Primitive name to Blockly block type mapping.
 * @private
 */
AI.YailToBlocks.PRIMITIVE_MAP_ = {
  // Math — basic arithmetic
  '+': {block: 'math_add', arity: 'variadic'},
  '-': {block: 'math_subtract', arity: 2},
  '*': {block: 'math_multiply', arity: 'variadic'},
  'yail-divide': {block: 'math_division', arity: 2},
  '/': {block: 'math_division', arity: 2},
  'expt': {block: 'math_power', arity: 2},
  'modulo': {block: 'math_modulo', arity: 2},
  'remainder': {block: 'math_remainder', arity: 2},
  'quotient': {block: 'math_quotient', arity: 2},

  // Math — single-argument operations
  'sqrt': {block: 'math_single', mode: 'ROOT', arity: 1},
  'abs': {block: 'math_single', mode: 'ABS', arity: 1},
  'log': {block: 'math_single', mode: 'LN', arity: 1},
  'exp': {block: 'math_single', mode: 'EXP', arity: 1},
  'yail-round': {block: 'math_single', mode: 'ROUND', arity: 1},
  'yail-ceiling': {block: 'math_single', mode: 'CEILING', arity: 1},
  'yail-floor': {block: 'math_single', mode: 'FLOOR', arity: 1},

  // Math — bitwise operations
  'bitwise-and': {block: 'math_bitwise', mode: 'BITAND', arity: 'variadic'},
  'bitwise-ior': {block: 'math_bitwise', mode: 'BITIOR', arity: 'variadic'},
  'bitwise-xor': {block: 'math_bitwise', mode: 'BITXOR', arity: 'variadic'},

  // Math — trigonometry
  'sin-degrees': {block: 'math_trig', mode: 'SIN', arity: 1},
  'cos-degrees': {block: 'math_trig', mode: 'COS', arity: 1},
  'tan-degrees': {block: 'math_trig', mode: 'TAN', arity: 1},
  'asin-degrees': {block: 'math_trig', mode: 'ASIN', arity: 1},
  'acos-degrees': {block: 'math_trig', mode: 'ACOS', arity: 1},
  'atan-degrees': {block: 'math_trig', mode: 'ATAN', arity: 1},
  'atan2-degrees': {block: 'math_atan2', arity: 2},

  // Math — random
  'random-integer': {block: 'math_random_int', arity: 2},
  'random-fraction': {block: 'math_random_float', arity: 0},
  'random-set-seed': {block: 'math_random_seed', arity: 1},

  // Math — min/max (variadic, two-input version)
  'min': {block: 'math_on_list', mode: 'MIN', arity: 'variadic'},
  'max': {block: 'math_on_list', mode: 'MAX', arity: 'variadic'},
  'yail-min': {block: 'math_on_list', mode: 'MIN', arity: 'variadic'},  // alias
  'yail-max': {block: 'math_on_list', mode: 'MAX', arity: 'variadic'},  // alias

  // Math — list statistics (single list input)
  'avg': {block: 'math_on_list2', mode: 'AVG', arity: 1},
  'minl': {block: 'math_on_list2', mode: 'MIN', arity: 1},
  'maxl': {block: 'math_on_list2', mode: 'MAX', arity: 1},
  'gm': {block: 'math_on_list2', mode: 'GM', arity: 1},
  'std-dev': {block: 'math_on_list2', mode: 'SD', arity: 1},
  'std-err': {block: 'math_on_list2', mode: 'SE', arity: 1},
  'mode': {block: 'math_mode_of_list', arity: 1},

  // Math — angle conversion
  'radians->degrees': {block: 'math_convert_angles', mode: 'RADIANS_TO_DEGREES', arity: 1},
  'degrees->radians': {block: 'math_convert_angles', mode: 'DEGREES_TO_RADIANS', arity: 1},

  // Math — number/base conversion
  'math-convert-dec-hex': {block: 'math_convert_number', mode: 'DEC_TO_HEX', arity: 1},
  'math-convert-hex-dec': {block: 'math_convert_number', mode: 'HEX_TO_DEC', arity: 1},
  'math-convert-dec-bin': {block: 'math_convert_number', mode: 'DEC_TO_BIN', arity: 1},
  'math-convert-bin-dec': {block: 'math_convert_number', mode: 'BIN_TO_DEC', arity: 1},
  'format-as-decimal': {block: 'math_format_as_decimal', arity: 2},

  // Math — number type checks
  'is-number?': {block: 'math_is_a_number', mode: 'NUMBER', arity: 1},
  'is-base10?': {block: 'math_is_a_number', mode: 'BASE10', arity: 1},
  'is-hexadecimal?': {block: 'math_is_a_number', mode: 'HEXADECIMAL', arity: 1},
  'is-binary?': {block: 'math_is_a_number', mode: 'BINARY', arity: 1},

  // Comparison
  'yail-equal?': {block: 'math_compare', mode: 'EQ', arity: 2},
  'yail-not-equal?': {block: 'math_compare', mode: 'NEQ', arity: 2},
  '<': {block: 'math_compare', mode: 'LT', arity: 2},
  '<=': {block: 'math_compare', mode: 'LTE', arity: 2},
  '>': {block: 'math_compare', mode: 'GT', arity: 2},
  '>=': {block: 'math_compare', mode: 'GTE', arity: 2},

  // Logic
  'yail-not': {block: 'logic_negate', arity: 1},
  'not': {block: 'logic_negate', arity: 1},          // alias — used in text NEQ wrapping

  // Text
  'string-append': {block: 'text_join', arity: 'variadic'},
  'string-length': {block: 'text_length', arity: 1},
  'string-empty?': {block: 'text_isEmpty', arity: 1},
  'string-contains': {block: 'text_contains', mode: 'CONTAINS', arity: 2},
  'string-contains-any': {block: 'text_contains', mode: 'CONTAINS_ANY', arity: 2},
  'string-contains-all': {block: 'text_contains', mode: 'CONTAINS_ALL', arity: 2},
  'string-starts-at': {block: 'text_starts_at', arity: 2},
  'string-replace-all': {block: 'text_replace_all', arity: 3},
  'string-replace-mappings-longest-string': {block: 'text_replace_mappings', mode: 'LONGEST_STRING_FIRST', arity: 2},
  'string-replace-mappings-dictionary': {block: 'text_replace_mappings', mode: 'DICTIONARY_ORDER', arity: 2},
  'string-split': {block: 'text_split', mode: 'SPLIT', arity: 2},
  'string-split-at-first': {block: 'text_split', mode: 'SPLITATFIRST', arity: 2},
  'string-split-at-first-of-any': {block: 'text_split', mode: 'SPLITATFIRSTOFANY', arity: 2},
  'string-split-at-any': {block: 'text_split', mode: 'SPLITATANY', arity: 2},
  'string-split-at-spaces': {block: 'text_split_at_spaces', arity: 1},
  'string-trim': {block: 'text_trim', arity: 1},
  'string-substring': {block: 'text_segment', arity: 3},
  'string-reverse': {block: 'text_reverse', arity: 1},
  'string-to-upper-case': {block: 'text_changeCase', mode: 'UPCASE', arity: 1},
  'string-to-lower-case': {block: 'text_changeCase', mode: 'DOWNCASE', arity: 1},
  'upcase': {block: 'text_changeCase', mode: 'UPCASE', arity: 1},      // alias
  'downcase': {block: 'text_changeCase', mode: 'DOWNCASE', arity: 1},  // alias

  // Text — comparison
  'string<?': {block: 'text_compare', mode: 'LT', arity: 2},
  'string>?': {block: 'text_compare', mode: 'GT', arity: 2},
  'string=?': {block: 'text_compare', mode: 'EQUAL', arity: 2},

  // Text — type check and obfuscation
  'string?': {block: 'text_is_string', arity: 1},
  'text-deobfuscate': {block: 'obfuscated_text', arity: 2},

  // Lists
  'make-yail-list': {block: 'lists_create_with', arity: 'variadic'},
  'yail-list-get-item': {block: 'lists_select_item', arity: 2},
  'yail-list-set-item!': {block: 'lists_replace_item', arity: 3},
  'yail-list-length': {block: 'lists_length', arity: 1},
  'yail-list-empty?': {block: 'lists_is_empty', arity: 1},
  'yail-list-add-to-list!': {block: 'lists_add_items', arity: 'variadic', fixedInputs: 1},
  'yail-list-remove-item!': {block: 'lists_remove_item', arity: 2},
  'yail-list-insert-item!': {block: 'lists_insert_item', arity: 3},
  'yail-list-append!': {block: 'lists_append_list', arity: 2},
  'yail-list-append': {block: 'lists_append_list', arity: 2},   // alias (without bang)
  'yail-list-copy': {block: 'lists_copy', arity: 1},
  'yail-list-member?': {block: 'lists_is_in', arity: 2},
  'yail-list-index': {block: 'lists_position_in', arity: 2},
  'yail-list-pick-random': {block: 'lists_pick_random_item', arity: 1},
  'yail-list?': {block: 'lists_is_list', arity: 1},
  'yail-list-is-list?': {block: 'lists_is_list', arity: 1},     // alias
  'yail-list-reverse': {block: 'lists_reverse', arity: 1},
  'yail-list-join-with-separator': {block: 'lists_join_with_separator', arity: 2},
  'yail-list-to-csv-row': {block: 'lists_to_csv_row', arity: 1},
  'yail-list-to-csv-table': {block: 'lists_to_csv_table', arity: 1},
  'yail-list-from-csv-row': {block: 'lists_from_csv_row', arity: 1},
  'yail-list-from-csv-table': {block: 'lists_from_csv_table', arity: 1},
  'yail-alist-lookup': {block: 'lists_lookup_in_pairs', arity: 3},
  'yail-list-sort': {block: 'lists_sort', arity: 1},
  'yail-list-but-first': {block: 'lists_but_first', arity: 1},
  'yail-list-but-last': {block: 'lists_but_last', arity: 1},
  'yail-list-slice': {block: 'lists_slice', arity: 3},

  // Dictionaries
  'make-yail-dictionary': {block: 'dictionaries_create_with', arity: 'variadic'},
  'make-dictionary-pair': {block: 'pair', arity: 2},
  'yail-dictionary-lookup': {block: 'dictionaries_lookup', arity: 3},
  'yail-dictionary-recursive-lookup': {block: 'dictionaries_recursive_lookup', arity: 3},
  'yail-dictionary-set-pair': {block: 'dictionaries_set_pair', arity: 3},
  'yail-dictionary-recursive-set': {block: 'dictionaries_recursive_set', arity: 3},
  'yail-dictionary-delete-pair': {block: 'dictionaries_delete_pair', arity: 2},
  'yail-dictionary-get-keys': {block: 'dictionaries_getters', mode: 'KEYS', arity: 1},
  'yail-dictionary-get-values': {block: 'dictionaries_getters', mode: 'VALUES', arity: 1},
  'yail-dictionary-is-key-in': {block: 'dictionaries_is_key_in', arity: 2},
  'yail-dictionary-is-key-in?': {block: 'dictionaries_is_key_in', arity: 2},  // alias
  'yail-dictionary-length': {block: 'dictionaries_length', arity: 1},
  'yail-dictionary?': {block: 'dictionaries_is_dict', arity: 1},
  'yail-dictionary-is-dict?': {block: 'dictionaries_is_dict', arity: 1},      // alias
  'yail-dictionary-alist-to-dict': {block: 'dictionaries_alist_to_dict', arity: 1},
  'yail-dictionary-dict-to-alist': {block: 'dictionaries_dict_to_alist', arity: 1},
  'yail-dictionary-copy': {block: 'dictionaries_copy', arity: 1},
  'yail-dictionary-combine-dicts': {block: 'dictionaries_combine_dicts', arity: 2},
  'yail-dictionary-walk': {block: 'dictionaries_walk_tree', arity: 2},

  // Colors
  'make-color': {block: 'color_make_color', arity: 1},
  'split-color': {block: 'color_split_color', arity: 1},

  // Screen control
  'open-another-screen': {block: 'controls_openAnotherScreen', arity: 1},
  'open-another-screen-with-start-value': {block: 'controls_openAnotherScreenWithStartValue', arity: 2},
  'close-screen': {block: 'controls_closeScreen', arity: 0},
  'close-screen-with-value': {block: 'controls_closeScreenWithValue', arity: 1},
  'close-screen-with-plain-text': {block: 'controls_closeScreenWithPlainText', arity: 1},
  'close-application': {block: 'controls_closeApplication', arity: 0},
  'get-start-value': {block: 'controls_getStartValue', arity: 0},
  'get-plain-start-text': {block: 'controls_getPlainStartText', arity: 0},

  // Matrices
  'make-yail-matrix-multidim': {block: 'matrices_create_multidim', arity: 2},
  'yail-matrix-get-row': {block: 'matrices_get_row', arity: 2},
  'yail-matrix-get-column': {block: 'matrices_get_column', arity: 2},
  'yail-matrix-get-dims': {block: 'matrices_get_dims', arity: 1},
  'yail-matrix-get-cell': {block: 'matrices_get_cell', arity: 'variadic', fixedInputs: 1},
  'yail-matrix-set-cell!': {block: 'matrices_set_cell', arity: 'variadic', fixedInputs: 2},
  'yail-matrix-add': {block: 'matrices_add', arity: 'variadic'},
  'yail-matrix-subtract': {block: 'matrices_subtract', arity: 2},
  'yail-matrix-multiply': {block: 'matrices_multiply', arity: 'variadic'},
  'yail-matrix-power': {block: 'matrices_power', arity: 2},
  'yail-matrix-inverse': {block: 'matrices_operations', mode: 'INVERSE', arity: 1},
  'yail-matrix-transpose': {block: 'matrices_operations', mode: 'TRANSPOSE', arity: 1},
  'yail-matrix-rotate-left': {block: 'matrices_operations', mode: 'ROTATE_LEFT', arity: 1},
  'yail-matrix-rotate-right': {block: 'matrices_operations', mode: 'ROTATE_RIGHT', arity: 1},
  'yail-matrix?': {block: 'matrices_is_matrix', arity: 1},

  // Control — new async blocks
  'run-in-background': {block: 'controls_run_in_background', arity: 2},
  'run-after-period': {block: 'controls_run_after_period', arity: 2},

  // Procedures — anonymous
  'num-args-yail-procedure': {block: 'procedures_numArgs', arity: 1},
  'create-yail-procedure-with-name': {block: 'procedures_getWithName', arity: 1},
  'call-yail-procedure-input-list': {block: 'procedures_callanonnoreturn_inputlist', arity: 2}
};

/** @private */
AI.YailToBlocks.convertPrimitive_ = function(workspace, node, asExpression) {
  var els = node.elements;
  // (call-yail-primitive name (*list-for-runtime* args...) '(types...) "desc")
  if (els.length < 2) return null;

  var primName = els[1].name;

  // make-exact-yail-integer is a type-coercion wrapper, not a real block.
  // Unwrap it: extract the single argument and return it as a number or color.
  if (primName === 'make-exact-yail-integer' || primName === 'make-exact-yail-real-number') {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    if (args.length > 0) {
      var colorBlock = AI.YailToBlocks.tryMakeColorBlock_(workspace, args[0]);
      if (colorBlock) return colorBlock;
      return AI.YailToBlocks.convertExpression_(workspace, args[0]);
    }
    return AI.YailToBlocks.makeNumberBlock_(workspace, 0);
  }

  // make-yail-matrix uses FieldNumber fields, not input connections.
  if (primName === 'make-yail-matrix') {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    var rows = (args.length > 0 && args[0].type === 'number') ? args[0].value : 2;
    var cols = (args.length > 1 && args[1].type === 'number') ? args[1].value : 2;
    var block = workspace.newBlock('matrices_create');
    var matrixValues = [];
    for (var r = 0; r < rows; r++) {
      matrixValues[r] = [];
      for (var c = 0; c < cols; c++) {
        var idx = 2 + r * cols + c;
        matrixValues[r][c] = (idx < args.length && args[idx].type === 'number')
            ? args[idx].value : 0;
      }
    }
    var mutation = document.createElement('mutation');
    mutation.setAttribute('rows', String(rows));
    mutation.setAttribute('cols', String(cols));
    mutation.setAttribute('matrix', JSON.stringify(matrixValues));
    block.domToMutation(mutation);
    block.initSvg();
    return block;
  }

  // call-yail-procedure-input-list: expression variant overrides PRIMITIVE_MAP_
  if (primName === 'call-yail-procedure-input-list' && asExpression) {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    var block = workspace.newBlock('procedures_callanonreturn_inputlist');
    block.initSvg();
    if (args.length > 0) {
      var procBlock = AI.YailToBlocks.convertExpression_(workspace, args[0]);
      if (procBlock && block.getInput('PROCEDURE')) {
        block.getInput('PROCEDURE').connection.connect(procBlock.outputConnection);
      }
    }
    if (args.length > 1) {
      var listBlock = AI.YailToBlocks.convertExpression_(workspace, args[1]);
      if (listBlock && block.getInput('INPUTLIST')) {
        block.getInput('INPUTLIST').connection.connect(listBlock.outputConnection);
      }
    }
    return block;
  }

  // call-yail-procedure: variadic — first arg is procedure, rest are call args.
  if (primName === 'call-yail-procedure') {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    var callArgCount = args.length - 1;  // exclude procedure arg
    var blockType = asExpression
        ? 'procedures_callanonreturn'
        : 'procedures_callanonnoreturn';
    var block = workspace.newBlock(blockType);
    if (callArgCount > 0 && block.domToMutation) {
      var mutation = document.createElement('mutation');
      mutation.setAttribute('items', String(callArgCount));
      block.domToMutation(mutation);
    }
    block.initSvg();
    if (args.length > 0) {
      var procBlock = AI.YailToBlocks.convertExpression_(workspace, args[0]);
      if (procBlock && block.getInput('PROCEDURE')) {
        block.getInput('PROCEDURE').connection.connect(procBlock.outputConnection);
      }
    }
    for (var i = 1; i < args.length; i++) {
      var argBlock = AI.YailToBlocks.convertExpression_(workspace, args[i]);
      var inputName = 'ARG' + (i - 1);
      if (argBlock && block.getInput(inputName)) {
        block.getInput(inputName).connection.connect(argBlock.outputConnection);
      }
    }
    return block;
  }

  // create-yail-procedure: two cases based on argument form.
  if (primName === 'create-yail-procedure') {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    if (args.length === 0) return null;
    var firstArg = args[0];

    // Case 2: (get-var p$name) → procedures_getWithDropdown
    if (AI.SExprParser.isForm(firstArg, 'get-var')) {
      var varName = firstArg.elements[1].name || '';
      var procName = varName.replace(/^p\$/, '');
      var block = workspace.newBlock('procedures_getWithDropdown');
      block.initSvg();
      if (block.getField('PROCNAME')) {
        block.setFieldValue(procName, 'PROCNAME');
      }
      return block;
    }

    // Case 1: (lambda ($p1 $p2 ...) body) → anonymous procedure def
    if (AI.SExprParser.isForm(firstArg, 'lambda')) {
      var lambdaEls = firstArg.elements;
      var paramNames = [];
      if (lambdaEls.length > 1 && lambdaEls[1].type === 'list') {
        for (var p = 0; p < lambdaEls[1].elements.length; p++) {
          var pName = lambdaEls[1].elements[p].name || '';
          paramNames.push(pName.replace(/^\$(?:param_)?/, ''));
        }
      }
      var bodyNode = lambdaEls.length > 2 ? lambdaEls[2] : null;

      // Heuristic: if body is a statement form, use no-return; otherwise return.
      var isStatement = false;
      if (bodyNode) {
        var bodyHead = AI.SExprParser.formHead(bodyNode);
        if (bodyHead === 'begin' || bodyHead === 'set-var!'
            || bodyHead === 'set-and-coerce-property!'
            || bodyHead === 'call-component-method'
            || bodyHead === 'set-lexical!'
            || bodyHead === 'while' || bodyHead === 'foreach'
            || bodyHead === 'forrange' || bodyHead === 'let') {
          isStatement = true;
        }
        if (bodyHead === 'if' && bodyNode.elements.length > 2) {
          var branch = bodyNode.elements[2];
          if (AI.SExprParser.isForm(branch, 'begin')) {
            isStatement = true;
          }
        }
      }

      var blockType = isStatement
          ? 'procedures_defanonnoreturn'
          : 'procedures_defanonreturn';
      var block = workspace.newBlock(blockType);

      if (paramNames.length > 0 && block.updateParams_) {
        block.updateParams_(paramNames);
      }
      block.initSvg();

      if (bodyNode) {
        if (isStatement) {
          var bodyBlock = AI.YailToBlocks.convertBeginToStatements_(
              workspace, bodyNode);
          if (bodyBlock && block.getInput('STACK')) {
            block.getInput('STACK').connection.connect(
                bodyBlock.previousConnection);
          }
        } else {
          var bodyBlock = AI.YailToBlocks.convertExpression_(
              workspace, bodyNode);
          if (bodyBlock && block.getInput('RETURN')) {
            block.getInput('RETURN').connection.connect(
                bodyBlock.outputConnection);
          }
        }
      }
      return block;
    }

    // Fallback: unknown argument form — treat as expression
    return AI.YailToBlocks.convertExpression_(workspace, firstArg);
  }

  var info = AI.YailToBlocks.PRIMITIVE_MAP_[primName];
  if (!info) {
    throw new Error('Unknown YAIL primitive: ' + primName);
  }

  // Extract arguments from *list-for-runtime* first (needed for variadic mutation)
  var args = [];
  if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
    var argList = els[2].elements;
    for (var i = 1; i < argList.length; i++) {
      args.push(argList[i]);
    }
  } else if (els.length > 2) {
    // LLM tolerance: some models emit call-yail-primitive without the
    // *list-for-runtime* wrapper, passing arguments directly.
    var raw = els.slice(2);
    if (raw.length >= 2
        && raw[raw.length - 1].type === 'string'
        && raw[raw.length - 2].type === 'quoted') {
      raw = raw.slice(0, raw.length - 2);
    }
    for (var i = 0; i < raw.length; i++) {
      args.push(raw[i]);
    }
  }

  var block = workspace.newBlock(info.block);
  if (info.mode && block.getField && block.getField('OP')) {
    block.setFieldValue(info.mode, 'OP');
  }

  // For variadic blocks, set up mutation with the correct item count before initSvg.
  if (info.arity === 'variadic' && block.domToMutation) {
    var mutation = document.createElement('mutation');
    var itemCount = args.length - (info.fixedInputs || 0);
    mutation.setAttribute('items', String(itemCount));
    block.domToMutation(mutation);
  }

  block.initSvg();

  // Connect arguments to inputs
  var inputNames = AI.YailToBlocks.getInputNames_(info, args.length, block);
  for (var i = 0; i < args.length && i < inputNames.length; i++) {
    var argBlock = AI.YailToBlocks.convertExpression_(workspace, args[i]);
    if (argBlock && block.getInput(inputNames[i])) {
      block.getInput(inputNames[i]).connection.connect(argBlock.outputConnection);
    }
  }

  return block;
};

/**
 * Convert a shortened primitive form like (+ a b) where the LLM omitted
 * the full call-yail-primitive wrapper.
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {Object} node AST node whose head matched PRIMITIVE_MAP_.
 * @param {Object} info Entry from PRIMITIVE_MAP_ ({block, arity, mode?}).
 * @return {?Blockly.Block}
 * @private
 */
AI.YailToBlocks.convertShortPrimitive_ = function(workspace, node, info) {
  var args = node.elements.slice(1);

  var block = workspace.newBlock(info.block);
  if (info.mode && block.getField && block.getField('OP')) {
    block.setFieldValue(info.mode, 'OP');
  }

  if (info.arity === 'variadic' && block.domToMutation) {
    var mutation = document.createElement('mutation');
    var itemCount = args.length - (info.fixedInputs || 0);
    mutation.setAttribute('items', String(itemCount));
    block.domToMutation(mutation);
  }

  block.initSvg();

  var inputNames = AI.YailToBlocks.getInputNames_(info, args.length, block);
  for (var i = 0; i < args.length && i < inputNames.length; i++) {
    var argBlock = AI.YailToBlocks.convertExpression_(workspace, args[i]);
    if (argBlock && block.getInput(inputNames[i])) {
      block.getInput(inputNames[i]).connection.connect(argBlock.outputConnection);
    }
  }

  return block;
};

/**
 * Get the input names for a primitive block based on its arity.
 * Uses the block's actual inputs to discover names when possible.
 * @private
 */
AI.YailToBlocks.getInputNames_ = function(info, argCount, block) {
  if (info.arity === 'variadic') {
    if (info.fixedInputs && block && block.inputList) {
      var valueInputs = [];
      for (var i = 0; i < block.inputList.length; i++) {
        var input = block.inputList[i];
        if (input.type === Blockly.INPUT_VALUE) {
          valueInputs.push(input.name);
        }
      }
      return valueInputs.slice(0, argCount);
    }
    var prefix = (block && block.repeatingInputName) || 'NUM';
    var names = [];
    for (var i = 0; i < argCount; i++) {
      names.push(prefix + i);
    }
    return names;
  }

  // For fixed-arity blocks, discover input names from the block itself.
  if (block && block.inputList) {
    var valueInputs = [];
    for (var i = 0; i < block.inputList.length; i++) {
      var input = block.inputList[i];
      if (input.type === Blockly.INPUT_VALUE) {
        valueInputs.push(input.name);
      }
    }
    if (valueInputs.length >= argCount) {
      return valueInputs.slice(0, argCount);
    }
  }

  // Fallback to common naming patterns
  if (info.arity === 1) {
    return ['NUM'];
  }
  if (info.arity === 2) {
    return ['A', 'B'];
  }
  if (info.arity === 3) {
    return ['A', 'B', 'C'];
  }
  return [];
};
