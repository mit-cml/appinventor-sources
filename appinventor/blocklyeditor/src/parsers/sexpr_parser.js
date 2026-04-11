// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview S-expression tokenizer and parser for YAIL.
 * Converts YAIL strings into an AST of nested arrays and atoms.
 *
 * @author anthropic
 */

'use strict';

goog.provide('AI.SExprParser');

/**
 * Token types used internally by the tokenizer.
 * @enum {string}
 */
AI.SExprParser.TokenType = {
  LPAREN: 'LPAREN',
  RPAREN: 'RPAREN',
  QUOTE: 'QUOTE',
  STRING: 'STRING',
  NUMBER: 'NUMBER',
  HASH_T: 'HASH_T',
  HASH_F: 'HASH_F',
  SYMBOL: 'SYMBOL',
  EOF: 'EOF'
};

/**
 * AST node types.
 * @enum {string}
 */
AI.SExprParser.NodeType = {
  SYMBOL: 'symbol',
  STRING: 'string',
  NUMBER: 'number',
  BOOLEAN: 'boolean',
  LIST: 'list',
  QUOTED: 'quoted'
};

/**
 * Create a symbol node.
 * @param {string} name
 * @return {{type: string, name: string}}
 */
AI.SExprParser.symbol = function(name) {
  return {type: AI.SExprParser.NodeType.SYMBOL, name: name};
};

/**
 * Create a string node.
 * @param {string} value
 * @return {{type: string, value: string}}
 */
AI.SExprParser.str = function(value) {
  return {type: AI.SExprParser.NodeType.STRING, value: value};
};

/**
 * Create a number node.
 * @param {number} value
 * @param {string} raw The original string representation
 * @return {{type: string, value: number, raw: string}}
 */
AI.SExprParser.num = function(value, raw) {
  return {type: AI.SExprParser.NodeType.NUMBER, value: value, raw: raw};
};

/**
 * Create a boolean node.
 * @param {boolean} value
 * @return {{type: string, value: boolean}}
 */
AI.SExprParser.bool = function(value) {
  return {type: AI.SExprParser.NodeType.BOOLEAN, value: value};
};

/**
 * Create a list node.
 * @param {Array} elements
 * @return {{type: string, elements: Array}}
 */
AI.SExprParser.list = function(elements) {
  return {type: AI.SExprParser.NodeType.LIST, elements: elements};
};

/**
 * Create a quoted node.
 * @param {Object} inner
 * @return {{type: string, inner: Object}}
 */
AI.SExprParser.quoted = function(inner) {
  return {type: AI.SExprParser.NodeType.QUOTED, inner: inner};
};

// ---- Tokenizer ----

/**
 * Tokenize a YAIL string into a list of tokens.
 * @param {string} input
 * @return {Array<{type: string, value: string, pos: number}>}
 */
AI.SExprParser.tokenize = function(input) {
  var tokens = [];
  var i = 0;
  var len = input.length;
  var TT = AI.SExprParser.TokenType;

  while (i < len) {
    var ch = input[i];

    // Skip whitespace
    if (ch === ' ' || ch === '\t' || ch === '\n' || ch === '\r') {
      i++;
      continue;
    }

    // Skip line comments (;; ... \n)
    if (ch === ';') {
      while (i < len && input[i] !== '\n') i++;
      continue;
    }

    // Skip block comments (#| ... |#)
    if (ch === '#' && i + 1 < len && input[i + 1] === '|') {
      i += 2;
      while (i + 1 < len && !(input[i] === '|' && input[i + 1] === '#')) i++;
      i += 2;
      continue;
    }

    if (ch === '(') {
      tokens.push({type: TT.LPAREN, value: '(', pos: i});
      i++;
      continue;
    }

    if (ch === ')') {
      tokens.push({type: TT.RPAREN, value: ')', pos: i});
      i++;
      continue;
    }

    if (ch === '\'') {
      tokens.push({type: TT.QUOTE, value: '\'', pos: i});
      i++;
      continue;
    }

    // String literal
    if (ch === '"') {
      var start = i;
      i++; // skip opening quote
      var str = '';
      while (i < len && input[i] !== '"') {
        if (input[i] === '\\' && i + 1 < len) {
          var next = input[i + 1];
          if (next === '"') { str += '"'; i += 2; }
          else if (next === '\\') { str += '\\'; i += 2; }
          else if (next === 'n') { str += '\n'; i += 2; }
          else if (next === 't') { str += '\t'; i += 2; }
          else { str += input[i]; i++; }
        } else {
          str += input[i];
          i++;
        }
      }
      if (i >= len) {
        throw new Error('Unterminated string starting at position ' + start);
      }
      i++; // skip closing quote
      tokens.push({type: TT.STRING, value: str, pos: start});
      continue;
    }

    // #t, #f, #x hex literals
    if (ch === '#') {
      if (i + 1 < len) {
        var nextCh = input[i + 1];
        if (nextCh === 't' && (i + 2 >= len || isDelimiter(input[i + 2]))) {
          tokens.push({type: TT.HASH_T, value: '#t', pos: i});
          i += 2;
          continue;
        }
        if (nextCh === 'f' && (i + 2 >= len || isDelimiter(input[i + 2]))) {
          tokens.push({type: TT.HASH_F, value: '#f', pos: i});
          i += 2;
          continue;
        }
        // Hex literal: #xNNNN...
        if (nextCh === 'x' || nextCh === 'X') {
          start = i;
          i += 2;
          while (i < len && isHexDigit(input[i])) i++;
          var hexStr = input.substring(start, i);
          var hexVal = parseInt(hexStr.substring(2), 16);
          tokens.push({type: TT.NUMBER, value: hexStr, pos: start, numValue: hexVal});
          continue;
        }
      }
      // Fall through to symbol
    }

    // Number (including negative)
    if (isDigit(ch) || (ch === '-' && i + 1 < len && isDigit(input[i + 1]))) {
      start = i;
      if (ch === '-') i++;
      while (i < len && isDigit(input[i])) i++;
      if (i < len && input[i] === '.') {
        i++;
        while (i < len && isDigit(input[i])) i++;
      }
      // Scientific notation
      if (i < len && (input[i] === 'e' || input[i] === 'E')) {
        i++;
        if (i < len && (input[i] === '+' || input[i] === '-')) i++;
        while (i < len && isDigit(input[i])) i++;
      }
      // Make sure this isn't actually part of a symbol (e.g., "-" by itself)
      if (i > start + (ch === '-' ? 1 : 0)) {
        var numStr = input.substring(start, i);
        tokens.push({type: TT.NUMBER, value: numStr, pos: start, numValue: parseFloat(numStr)});
        continue;
      }
      // Reset if this was just a bare '-'
      i = start;
    }

    // Symbol (any non-delimiter characters)
    if (!isDelimiter(ch)) {
      start = i;
      while (i < len && !isDelimiter(input[i])) i++;
      var sym = input.substring(start, i);
      tokens.push({type: TT.SYMBOL, value: sym, pos: start});
      continue;
    }

    // Unknown character — skip
    i++;
  }

  tokens.push({type: TT.EOF, value: '', pos: i});
  return tokens;

  function isDelimiter(c) {
    return c === ' ' || c === '\t' || c === '\n' || c === '\r' ||
           c === '(' || c === ')' || c === '"' || c === '\'' || c === ';';
  }

  function isDigit(c) {
    return c >= '0' && c <= '9';
  }

  function isHexDigit(c) {
    return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }
};

// ---- Parser ----

/**
 * Parse a YAIL string into an array of S-expression AST nodes.
 * @param {string} input The YAIL string.
 * @return {Array} Array of AST nodes (one per top-level form).
 * @throws {Error} If the input has syntax errors.
 */
AI.SExprParser.parseAll = function(input) {
  var tokens = AI.SExprParser.tokenize(input);
  var pos = 0;
  var TT = AI.SExprParser.TokenType;
  var results = [];

  while (tokens[pos].type !== TT.EOF) {
    results.push(parseExpr());
  }
  return results;

  function parseExpr() {
    var token = tokens[pos];
    switch (token.type) {
      case TT.LPAREN:
        return parseList();
      case TT.QUOTE:
        pos++;
        return AI.SExprParser.quoted(parseExpr());
      case TT.STRING:
        pos++;
        return AI.SExprParser.str(token.value);
      case TT.NUMBER:
        pos++;
        return AI.SExprParser.num(
            token.numValue !== undefined ? token.numValue : parseFloat(token.value),
            token.value);
      case TT.HASH_T:
        pos++;
        return AI.SExprParser.bool(true);
      case TT.HASH_F:
        pos++;
        return AI.SExprParser.bool(false);
      case TT.SYMBOL:
        pos++;
        return AI.SExprParser.symbol(token.value);
      case TT.RPAREN:
        throw new Error('Unexpected ) at position ' + token.pos);
      case TT.EOF:
        throw new Error('Unexpected end of input at position ' + token.pos);
      default:
        throw new Error('Unexpected token: ' + token.type + ' at position ' + token.pos);
    }
  }

  function parseList() {
    var openPos = tokens[pos].pos;
    pos++; // consume LPAREN
    var elements = [];
    while (tokens[pos].type !== TT.RPAREN) {
      if (tokens[pos].type === TT.EOF) {
        throw new Error('Unterminated list starting at position '
            + openPos
            + ' — missing closing parenthesis (reached end at position '
            + tokens[pos].pos + ')');
      }
      elements.push(parseExpr());
    }
    pos++; // consume RPAREN
    return AI.SExprParser.list(elements);
  }
};

/**
 * Parse a single S-expression from a string.
 * @param {string} input
 * @return {Object} A single AST node.
 */
AI.SExprParser.parseOne = function(input) {
  var results = AI.SExprParser.parseAll(input);
  if (results.length === 0) {
    throw new Error('No S-expression found in input');
  }
  return results[0];
};

// ---- Helpers ----

/**
 * Check if a node is a list whose first element is a symbol with the given name.
 * @param {Object} node
 * @param {string} name
 * @return {boolean}
 */
AI.SExprParser.isForm = function(node, name) {
  return node.type === AI.SExprParser.NodeType.LIST &&
         node.elements.length > 0 &&
         node.elements[0].type === AI.SExprParser.NodeType.SYMBOL &&
         node.elements[0].name === name;
};

/**
 * Count the parenthesis deficit in a token stream: opens minus closes.
 * A positive number means that many closing parens are missing.
 * @param {string} input The YAIL string.
 * @return {number} The deficit (opens - closes).
 */
AI.SExprParser.countParenDeficit = function(input) {
  var tokens = AI.SExprParser.tokenize(input);
  var TT = AI.SExprParser.TokenType;
  var opens = 0;
  var closes = 0;
  for (var i = 0; i < tokens.length; i++) {
    if (tokens[i].type === TT.LPAREN) opens++;
    else if (tokens[i].type === TT.RPAREN) closes++;
  }
  return opens - closes;
};

/**
 * Get the symbol name of the first element of a list node, or null.
 * @param {Object} node
 * @return {?string}
 */
AI.SExprParser.formHead = function(node) {
  if (node.type === AI.SExprParser.NodeType.LIST &&
      node.elements.length > 0 &&
      node.elements[0].type === AI.SExprParser.NodeType.SYMBOL) {
    return node.elements[0].name;
  }
  return null;
};
