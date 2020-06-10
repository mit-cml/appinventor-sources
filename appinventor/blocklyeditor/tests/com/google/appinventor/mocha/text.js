// Copyright © 2020 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Block behavior tests. Not related to code generation.
 *
 */

suite('Text Blocks', function() {

  setup(function() {
    this.workspace = Blockly.inject('blocklyDiv', {});
  });

  teardown(function() {
    this.workspace.dispose();
    Blockly.mainWorkspace = null;
  })

  suite('Text connection check', function() {

    setup(function() {
      Blockly.mainWorkspace.isLoading = false;
    });

    function mockConnection(value) {
      return {
        sourceBlock_: {
          getFieldValue: function() {
            return value;
          }
        }
      };
    }

    var check = Blockly.Blocks.text.connectionCheck;
    var number = { check_: ['Number'] };
    var boolean = { check_: ['Boolean'] };
    var string = { check_: ['String'] };
    var key = { check_: ['Key'] };
    var many = { check_: ['Number', 'String'] };
    var untyped = { check_: null };
    var invalidNumbers = [
      'cat', '4cat', 'e', '  zero  ', '0x0', '.', '-', '+', '', '-e-', '+e+'
    ];

    test('Should allow String connection', function() {
      chai.assert.isTrue(check(mockConnection(''), string));
    });

    test('Should allow Key connection', function() {
      chai.assert.isTrue(check(mockConnection(''), key));
    });

    test('Should not connect to invalid type', function() {
      chai.assert.isFalse(check(mockConnection(''), boolean));
    });

    test('Should connect to number when integer', function() {
      ['1', '+1', '-1'].forEach(function(value) {
        chai.assert.isTrue(check(mockConnection(value), number));
      });
    });

    test('Should connect to number when floating point', function() {
      ['3.14', '+3.14', '-3.14', '3.', '.14', '1e5', '1e-5', '1.e5', '1.e-5',
        '.1e4', '.1e-4', '.1e+4', '3.14e+3'].forEach(function (value) {
        chai.assert.isTrue(check(mockConnection(value), number));
      });
    });

    test('Should not connect to number when not a number', function() {
      invalidNumbers.forEach(function(value) {
        chai.assert.isFalse(check(mockConnection(value), number));
      })
    });

    test('Should allow invalid numbers during load', function() {
      Blockly.mainWorkspace.isLoading = true;
      invalidNumbers.forEach(function(value) {
        chai.assert.isTrue(check(mockConnection(value), number));
      })
    });

    test('Should connect with many checks', function() {
      chai.assert.isTrue(check(mockConnection('cat'), many));
    });

    test('Should connect to untyped when a number', function() {
      chai.assert.isTrue(check(mockConnection('1'), untyped));
    });

    test('Should connect to untyped when not number', function() {
      chai.assert.isTrue(check(mockConnection('cat'), untyped));
    });
  });
})
