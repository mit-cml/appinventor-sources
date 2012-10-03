/**
 * Blockly Demo: Maze
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
 * @fileoverview Demonstration of Blockly: Solving a maze.
 * @author fraser@google.com (Neil Fraser)
 */

// Extensions to Blockly's language and JavaScript generator.

// Define Language and JavaScript, in case this file is loaded too early.
if (!Blockly.Language) {
  Blockly.Language = {};
}
Blockly.JavaScript = Blockly.Generator.get('JavaScript');

Blockly.Language.maze_move = {
  // Block for moving forward or backwards.
  category: 'Commands',
  helpUrl: 'http://code.google.com/p/blockly/wiki/Move',
  init: function() {
    this.setColour(290);
    this.appendTitle('move');
    var dropdown = new Blockly.FieldDropdown(this.DIRECTIONS);
    this.appendTitle(dropdown, 'DIR');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Moves Pegman forward or backward one space.');
  }
};

Blockly.Language.maze_move.DIRECTIONS =
    [['forward', 'moveForward'], ['backward', 'moveBackward']];

Blockly.JavaScript.maze_move = function() {
  // Generate JavaScript for moving forward or backwards.
  return 'Maze.' + this.getTitleValue('DIR') + '("' + this.id + '");\n';
};

Blockly.Language.maze_turnLeft = {
  // Block for turning left or right.
  category: 'Commands',
  helpUrl: 'http://code.google.com/p/blockly/wiki/Turn',
  init: function() {
    this.setColour(290);
    this.appendTitle('turn');
    var dropdown = new Blockly.FieldDropdown(this.DIRECTIONS);
    this.appendTitle(dropdown, 'DIR');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Turns Pegman left or right by 90 degrees.');
  }
};

Blockly.Language.maze_turnLeft.DIRECTIONS =
    [['left', 'turnLeft'], ['right', 'turnRight'], ['randomly', 'random']];

Blockly.Language.maze_turnRight = {
  // Block for turning left or right.
  category: 'Commands',
  helpUrl: null,
  init: function() {
    this.setColour(290);
    this.appendTitle('turn');
    var dropdown =
        new Blockly.FieldDropdown(Blockly.Language.maze_turnLeft.DIRECTIONS);
    this.appendTitle(dropdown, 'DIR');
    this.setTitleValue(Blockly.Language.maze_turnLeft.DIRECTIONS[1][1], 'DIR');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Turns Pegman left or right by 90 degrees.');
  }
};

Blockly.JavaScript.maze_turnLeft = function() {
  // Generate JavaScript for turning left or right.
  var dir = this.getTitleValue('DIR');
  var code;
  if (dir == 'random') {
    code = 'if (Math.random() < 0.5) {\n' +
           '  Maze.turnLeft("' + this.id + '");\n' +
           '} else {\n' +
           '  Maze.turnRight("' + this.id + '");\n' +
           '}\n';
  } else {
    code = 'Maze.' + dir + '("' + this.id + '");\n';
  }
  return code;
};

// Turning left and right use the same code.
Blockly.JavaScript.maze_turnRight = Blockly.JavaScript.maze_turnLeft;

Blockly.Language.maze_isWall = {
  // Block for checking if there a wall.
  category: 'Logic',
  helpUrl: 'http://code.google.com/p/blockly/wiki/Wall',
  init: function() {
    this.setColour(120);
    this.setOutput(true, Boolean);
    this.appendTitle('wall');
    var dropdown = new Blockly.FieldDropdown(this.DIRECTIONS);
    this.appendTitle(dropdown, 'DIR');
    this.setTooltip('Returns true if there is a wall in ' +
                    'the specified direction.');
  }
};

Blockly.Language.maze_isWall.DIRECTIONS =
    [['ahead', 'isWallForward'],
     ['to the left', 'isWallLeft'],
     ['to the right', 'isWallRight'],
     ['behind', 'isWallBackward']];

Blockly.JavaScript.maze_isWall = function() {
  // Generate JavaScript for checking if there is a wall.
  var code = 'Maze.' + this.getTitleValue('DIR') + '()';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.Language.controls_forever = {
  // Do forever loop.
  category: 'Logic',
  helpUrl: 'http://code.google.com/p/blockly/wiki/Repeat',
  init: function() {
    this.setColour(120);
    this.appendTitle('repeat until finished');
    this.appendInput(Blockly.NEXT_STATEMENT, 'DO').appendTitle('do');
    this.setPreviousStatement(true);
    this.setTooltip('Repeat the enclosed steps until finish point is reached.');
  }
};

Blockly.JavaScript.controls_forever = function() {
  // Generate JavaScript for do forever loop.
  var branch0 = Blockly.JavaScript.statementToCode(this, 'DO');
  return 'while (true) {\n' + branch0 +
      '  Maze.checkTimeout("' + this.id + '");\n}\n';
};

Blockly.JavaScript.controls_whileUntil = function() {
  // Do while/until loop.
  var argument0 = Blockly.JavaScript.valueToCode(this, 'BOOL',
      Blockly.JavaScript.ORDER_NONE) || 'false';
  var branch0 = Blockly.JavaScript.statementToCode(this, 'DO');
  if (this.getTitleValue('MODE') == 'UNTIL') {
    if (!argument0.match(/^\w+$/)) {
      argument0 = '(' + argument0 + ')';
    }
    argument0 = '!' + argument0;
  }
  return 'while (' + argument0 + ') {\n' + branch0 +
      '  Maze.checkTimeout("' + this.id + '");\n}\n';
};
