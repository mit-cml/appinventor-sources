// Copyright © 2020 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Block behavior tests. Not related to code generation.
 *
 */

suite('Text Blocks', function() {
  let workspace
  setup(function() {
    workspace = Blockly.inject('blocklyDiv', {
      plugins: {
        [Blockly.registry.Type.CONNECTION_CHECKER]: 'CustomizableConnectionChecker',
      }
    });
  });

  teardown(function() {
    workspace.dispose();
  })

  suite('Text connection check', function() {

    setup(function() {
      workspace.isLoading = false;
    });

    function mockBlock(outputConnection) {
      return {
        outputConnection: outputConnection,
      }
    }

    function check(connection, typeConnection) {
      const block = mockBlock(connection);
      Blockly.Blocks.text.setOutputOnFinishEdit.bind(block)(connection.value);
      return workspace.connectionChecker.doTypeChecks(connection, typeConnection);
    }

    function mockConnection(value, check) {
      return {
        value: value,
        check_: check || AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT),
        setCheck: function(check) { this.check_ = check; },
        getCheck: function() { return this.check_; }
      };
    }

    function mockConnection2(check) {
      return mockConnection(null, check);
    }

    const number = mockConnection2(['Number']);
    const boolean = mockConnection2(['Boolean']);
    const string = mockConnection2(['String']);
    const key = mockConnection2(['Key']);
    const many = mockConnection2(['Number', 'String']);
    const untyped = mockConnection2( null);
    const invalidNumbers = [
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
      workspace.isLoading = true;
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
