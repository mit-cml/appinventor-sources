// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Unit tests for AI.SExprParser — tokenizer, parser, and helpers.
 */

suite('SExprParser', function() {

  var tokenize = AI.SExprParser.tokenize;
  var parseAll = AI.SExprParser.parseAll;
  var parseOne = AI.SExprParser.parseOne;
  var isForm = AI.SExprParser.isForm;
  var formHead = AI.SExprParser.formHead;
  var TT = AI.SExprParser.TokenType;

  // ---- Tokenizer ----

  suite('Tokenizer', function() {

    test('LPAREN token', function() {
      var tokens = tokenize('(');
      chai.assert.equal(tokens[0].type, TT.LPAREN);
      chai.assert.equal(tokens[0].value, '(');
    });

    test('RPAREN token', function() {
      var tokens = tokenize(')');
      chai.assert.equal(tokens[0].type, TT.RPAREN);
      chai.assert.equal(tokens[0].value, ')');
    });

    test('QUOTE token', function() {
      var tokens = tokenize("'");
      chai.assert.equal(tokens[0].type, TT.QUOTE);
    });

    test('SYMBOL token', function() {
      var tokens = tokenize('abc');
      chai.assert.equal(tokens[0].type, TT.SYMBOL);
      chai.assert.equal(tokens[0].value, 'abc');
    });

    test('NUMBER token', function() {
      var tokens = tokenize('42');
      chai.assert.equal(tokens[0].type, TT.NUMBER);
      chai.assert.equal(tokens[0].numValue, 42);
    });

    test('STRING token', function() {
      var tokens = tokenize('"hello"');
      chai.assert.equal(tokens[0].type, TT.STRING);
      chai.assert.equal(tokens[0].value, 'hello');
    });

    test('#t token', function() {
      var tokens = tokenize('#t');
      chai.assert.equal(tokens[0].type, TT.HASH_T);
    });

    test('#f token', function() {
      var tokens = tokenize('#f');
      chai.assert.equal(tokens[0].type, TT.HASH_F);
    });

    test('EOF token at end', function() {
      var tokens = tokenize('a');
      chai.assert.equal(tokens[tokens.length - 1].type, TT.EOF);
    });

    test('hex literal #xFF00FF', function() {
      var tokens = tokenize('#xFF00FF');
      chai.assert.equal(tokens[0].type, TT.NUMBER);
      chai.assert.equal(tokens[0].numValue, 0xFF00FF);
    });

    test('negative integer -42', function() {
      var tokens = tokenize('-42');
      chai.assert.equal(tokens[0].type, TT.NUMBER);
      chai.assert.equal(tokens[0].numValue, -42);
    });

    test('negative float -3.14', function() {
      var tokens = tokenize('-3.14');
      chai.assert.equal(tokens[0].type, TT.NUMBER);
      chai.assert.closeTo(tokens[0].numValue, -3.14, 0.001);
    });

    test('scientific notation 1e5', function() {
      var tokens = tokenize('1e5');
      chai.assert.equal(tokens[0].type, TT.NUMBER);
      chai.assert.equal(tokens[0].numValue, 100000);
    });

    test('scientific notation 2.5E-3', function() {
      var tokens = tokenize('2.5E-3');
      chai.assert.equal(tokens[0].type, TT.NUMBER);
      chai.assert.closeTo(tokens[0].numValue, 0.0025, 0.0001);
    });

    test('line comment is skipped', function() {
      var tokens = tokenize('; this is a comment\n(a)');
      chai.assert.equal(tokens[0].type, TT.LPAREN);
      chai.assert.equal(tokens[1].type, TT.SYMBOL);
      chai.assert.equal(tokens[1].value, 'a');
    });

    test('block comment is skipped', function() {
      var tokens = tokenize('#| block comment |# (a)');
      chai.assert.equal(tokens[0].type, TT.LPAREN);
      chai.assert.equal(tokens[1].type, TT.SYMBOL);
      chai.assert.equal(tokens[1].value, 'a');
    });

    test('string escape: escaped quote', function() {
      var tokens = tokenize('"hello \\"world\\""');
      chai.assert.equal(tokens[0].type, TT.STRING);
      chai.assert.equal(tokens[0].value, 'hello "world"');
    });

    test('string escape: newline', function() {
      var tokens = tokenize('"line\\nbreak"');
      chai.assert.equal(tokens[0].type, TT.STRING);
      chai.assert.equal(tokens[0].value, 'line\nbreak');
    });

    test('symbol with hyphen: yail-divide', function() {
      var tokens = tokenize('yail-divide');
      chai.assert.equal(tokens[0].type, TT.SYMBOL);
      chai.assert.equal(tokens[0].value, 'yail-divide');
    });

    test('symbol with bang: set-var!', function() {
      var tokens = tokenize('set-var!');
      chai.assert.equal(tokens[0].type, TT.SYMBOL);
      chai.assert.equal(tokens[0].value, 'set-var!');
    });

    test('symbol with asterisks: *list-for-runtime*', function() {
      var tokens = tokenize('*list-for-runtime*');
      chai.assert.equal(tokens[0].type, TT.SYMBOL);
      chai.assert.equal(tokens[0].value, '*list-for-runtime*');
    });

    test('symbol with question mark: string-empty?', function() {
      var tokens = tokenize('string-empty?');
      chai.assert.equal(tokens[0].type, TT.SYMBOL);
      chai.assert.equal(tokens[0].value, 'string-empty?');
    });

    test('symbol with dollar prefix: g$name', function() {
      var tokens = tokenize('g$name');
      chai.assert.equal(tokens[0].type, TT.SYMBOL);
      chai.assert.equal(tokens[0].value, 'g$name');
    });

    test('symbol with dollar and underscore: $param_x', function() {
      var tokens = tokenize('$param_x');
      chai.assert.equal(tokens[0].type, TT.SYMBOL);
      chai.assert.equal(tokens[0].value, '$param_x');
    });

    test('bare - is tokenized as SYMBOL', function() {
      var tokens = tokenize('-');
      chai.assert.equal(tokens[0].type, TT.SYMBOL);
      chai.assert.equal(tokens[0].value, '-');
    });
  });

  // ---- Parser ----

  suite('Parser', function() {

    test('empty list', function() {
      var result = parseAll('()');
      chai.assert.lengthOf(result, 1);
      chai.assert.equal(result[0].type, 'list');
      chai.assert.lengthOf(result[0].elements, 0);
    });

    test('simple list (a b c)', function() {
      var result = parseAll('(a b c)');
      chai.assert.lengthOf(result, 1);
      var list = result[0];
      chai.assert.equal(list.type, 'list');
      chai.assert.lengthOf(list.elements, 3);
      chai.assert.equal(list.elements[0].name, 'a');
      chai.assert.equal(list.elements[1].name, 'b');
      chai.assert.equal(list.elements[2].name, 'c');
    });

    test('nested lists (a (b c) d)', function() {
      var result = parseOne('(a (b c) d)');
      chai.assert.equal(result.elements[0].type, 'symbol');
      chai.assert.equal(result.elements[0].name, 'a');
      chai.assert.equal(result.elements[1].type, 'list');
      chai.assert.equal(result.elements[1].elements[0].name, 'b');
      chai.assert.equal(result.elements[1].elements[1].name, 'c');
      chai.assert.equal(result.elements[2].type, 'symbol');
      chai.assert.equal(result.elements[2].name, 'd');
    });

    test("quoted list '(a b)", function() {
      var result = parseOne("'(a b)");
      chai.assert.equal(result.type, 'quoted');
      chai.assert.equal(result.inner.type, 'list');
      chai.assert.lengthOf(result.inner.elements, 2);
      chai.assert.equal(result.inner.elements[0].name, 'a');
      chai.assert.equal(result.inner.elements[1].name, 'b');
    });

    test("quoted symbol 'text", function() {
      var result = parseOne("'text");
      chai.assert.equal(result.type, 'quoted');
      chai.assert.equal(result.inner.type, 'symbol');
      chai.assert.equal(result.inner.name, 'text');
    });

    test('number literal 42', function() {
      var result = parseOne('42');
      chai.assert.equal(result.type, 'number');
      chai.assert.equal(result.value, 42);
    });

    test('negative number -7', function() {
      var result = parseOne('-7');
      chai.assert.equal(result.type, 'number');
      chai.assert.equal(result.value, -7);
    });

    test('string literal "hello"', function() {
      var result = parseOne('"hello"');
      chai.assert.equal(result.type, 'string');
      chai.assert.equal(result.value, 'hello');
    });

    test('boolean true #t', function() {
      var result = parseOne('#t');
      chai.assert.equal(result.type, 'boolean');
      chai.assert.strictEqual(result.value, true);
    });

    test('boolean false #f', function() {
      var result = parseOne('#f');
      chai.assert.equal(result.type, 'boolean');
      chai.assert.strictEqual(result.value, false);
    });

    test('hex number #xFF0000', function() {
      var result = parseOne('#xFF0000');
      chai.assert.equal(result.type, 'number');
      chai.assert.equal(result.value, 0xFF0000);
    });

    test('multiple top-level forms', function() {
      var result = parseAll('(a) (b)');
      chai.assert.lengthOf(result, 2);
      chai.assert.equal(result[0].type, 'list');
      chai.assert.equal(result[1].type, 'list');
      chai.assert.equal(result[0].elements[0].name, 'a');
      chai.assert.equal(result[1].elements[0].name, 'b');
    });

    test('parseOne returns only first form', function() {
      var result = parseOne('(a) (b)');
      chai.assert.equal(result.type, 'list');
      chai.assert.equal(result.elements[0].name, 'a');
    });

    test('empty string returns empty array', function() {
      var result = parseAll('');
      chai.assert.isArray(result);
      chai.assert.lengthOf(result, 0);
    });

    test('deeply nested (a (b (c (d))))', function() {
      var result = parseOne('(a (b (c (d))))');
      chai.assert.equal(result.elements[0].name, 'a');
      var level1 = result.elements[1];
      chai.assert.equal(level1.elements[0].name, 'b');
      var level2 = level1.elements[1];
      chai.assert.equal(level2.elements[0].name, 'c');
      var level3 = level2.elements[1];
      chai.assert.equal(level3.elements[0].name, 'd');
    });
  });

  // ---- Helpers ----

  suite('Helper functions', function() {

    test('isForm returns true when head matches', function() {
      var node = parseOne('(define-event Button1 Click)');
      chai.assert.isTrue(isForm(node, 'define-event'));
    });

    test('isForm returns false when head does not match', function() {
      var node = parseOne('(define-event Button1 Click)');
      chai.assert.isFalse(isForm(node, 'wrong-name'));
    });

    test('isForm returns false for non-list node', function() {
      var node = parseOne('42');
      chai.assert.isFalse(isForm(node, 'anything'));
    });

    test('isForm returns false for empty list', function() {
      var node = parseOne('()');
      chai.assert.isFalse(isForm(node, 'anything'));
    });

    test('formHead returns symbol name', function() {
      var node = parseOne('(define-event Button1 Click)');
      chai.assert.equal(formHead(node), 'define-event');
    });

    test('formHead returns null for empty list', function() {
      var node = parseOne('()');
      chai.assert.isNull(formHead(node));
    });

    test('formHead returns null for non-list node', function() {
      var node = parseOne('42');
      chai.assert.isNull(formHead(node));
    });
  });

  // ---- Error handling ----

  suite('Error handling', function() {

    test('unterminated string throws', function() {
      chai.assert.throws(function() {
        parseAll('"hello');
      }, Error, /Unterminated string/);
    });

    test('unexpected ) throws', function() {
      chai.assert.throws(function() {
        parseAll(')');
      }, Error, /Unexpected \)/);
    });

    test('unterminated list throws', function() {
      chai.assert.throws(function() {
        parseAll('(a b');
      }, Error, /Unterminated list|missing closing/);
    });

    test('empty input to parseOne throws', function() {
      chai.assert.throws(function() {
        parseOne('');
      }, Error);
    });
  });

  // ---- Real YAIL snippets ----

  suite('Real YAIL snippets', function() {

    test('define-event with call-component-method', function() {
      var input = '(define-event Button1 Click () (set-this-form) ' +
          '(call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* "Hello") \'(text)))';
      var node = parseOne(input);
      chai.assert.equal(node.type, 'list');
      chai.assert.equal(formHead(node), 'define-event');
      chai.assert.equal(node.elements[1].type, 'symbol');
      chai.assert.equal(node.elements[1].name, 'Button1');
      chai.assert.equal(node.elements[2].type, 'symbol');
      chai.assert.equal(node.elements[2].name, 'Click');
      chai.assert.equal(node.elements[3].type, 'list');
      chai.assert.lengthOf(node.elements[3].elements, 0);
      chai.assert.isTrue(isForm(node.elements[4], 'set-this-form'));
      chai.assert.isTrue(isForm(node.elements[5], 'call-component-method'));
    });

    test('def g$score 0', function() {
      var node = parseOne('(def g$score 0)');
      chai.assert.equal(formHead(node), 'def');
      chai.assert.equal(node.elements[1].type, 'symbol');
      chai.assert.equal(node.elements[1].name, 'g$score');
      chai.assert.equal(node.elements[2].type, 'number');
      chai.assert.equal(node.elements[2].value, 0);
    });

    test('call-yail-primitive with nested structure', function() {
      var input = '(call-yail-primitive yail-equal? ' +
          '(*list-for-runtime* (lexical-value $n) 0) ' +
          "'(any any) \"=\")";
      var node = parseOne(input);
      chai.assert.equal(formHead(node), 'call-yail-primitive');
      chai.assert.equal(node.elements[1].name, 'yail-equal?');
      // *list-for-runtime* argument
      var listArg = node.elements[2];
      chai.assert.isTrue(isForm(listArg, '*list-for-runtime*'));
      chai.assert.isTrue(isForm(listArg.elements[1], 'lexical-value'));
      chai.assert.equal(listArg.elements[2].type, 'number');
      chai.assert.equal(listArg.elements[2].value, 0);
      // quoted type list
      chai.assert.equal(node.elements[3].type, 'quoted');
      // operator string
      chai.assert.equal(node.elements[4].type, 'string');
      chai.assert.equal(node.elements[4].value, '=');
    });

    test('def-return with recursive factorial', function() {
      var input = '(def-return (p$factorial $n) ' +
          '(if (call-yail-primitive yail-equal? ' +
          '(*list-for-runtime* (lexical-value $n) 0) ' +
          "'(any any) \"=\") 1 " +
          '(call-yail-primitive * ' +
          '(*list-for-runtime* (lexical-value $n) ' +
          '((get-var p$factorial) ' +
          '(call-yail-primitive - ' +
          '(*list-for-runtime* (lexical-value $n) 1) ' +
          "'(number number) \"-\"))) " +
          "'(number number) \"*\")))";
      var node = parseOne(input);
      chai.assert.equal(node.type, 'list');
      chai.assert.equal(formHead(node), 'def-return');
      // Signature list
      var sig = node.elements[1];
      chai.assert.equal(sig.type, 'list');
      chai.assert.equal(sig.elements[0].name, 'p$factorial');
      chai.assert.equal(sig.elements[1].name, '$n');
      // Body is an if-form
      var body = node.elements[2];
      chai.assert.isTrue(isForm(body, 'if'));
    });
  });
});
