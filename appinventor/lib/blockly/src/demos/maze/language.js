/**
 * Blockly Demo: Maze
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
  category: 'Maze',
  helpUrl: 'http://code.google.com/p/google-blockly/wiki/Move',
  init: function() {
    this.setColour(290);
    this.addTitle('move');
    var dropdown = new Blockly.FieldDropdown(function() {
      return Blockly.Language.maze_move.DIRECTIONS;
    });
    this.addTitle(dropdown);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Moves the mouse forward or backward one space.');
  }
};

Blockly.Language.maze_move.DIRECTIONS = ['forward', 'backward'];

Blockly.JavaScript.maze_move = function() {
  // Generate JavaScript for moving forward or backwards.
  var direction = Blockly.Language.maze_move.DIRECTIONS
      .indexOf(this.getTitleText(1));
  return 'Maze.move(' + direction + ', "' + this.id + '");\n';
};

Blockly.Language.maze_turnLeft = {
  // Block for turning left or right.
  category: 'Maze',
  helpUrl: 'http://code.google.com/p/google-blockly/wiki/Turn',
  init: function() {
    this.setColour(290);
    this.addTitle('turn');
    var dropdown = new Blockly.FieldDropdown(function() {
      return Blockly.Language.maze_turnLeft.DIRECTIONS;
    });
    this.addTitle(dropdown);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Turns the mouse left or right by 90 degrees.');
  }
};

Blockly.Language.maze_turnLeft.DIRECTIONS = ['left', 'right'];

Blockly.Language.maze_turnRight = {
  // Block for turning left or right.
  category: 'Maze',
  helpUrl: null,
  init: function() {
    this.setColour(290);
    this.addTitle('turn');
    var dropdown = new Blockly.FieldDropdown(function() {
      return Blockly.Language.maze_turnLeft.DIRECTIONS;
    });
    this.addTitle(dropdown);
    this.setTitleText(Blockly.Language.maze_turnLeft.DIRECTIONS[1], 1);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Turns the mouse left or right by 90 degrees.');
  }
};

Blockly.JavaScript.maze_turnLeft = function() {
  // Generate JavaScript for turning left or right.
  var direction = Blockly.Language.maze_turnLeft.DIRECTIONS
      .indexOf(this.getTitleText(1));
  return 'Maze.turn(' + direction + ', "' + this.id + '");\n';
};

// Turning left and right use the same code.
Blockly.JavaScript.maze_turnRight = Blockly.JavaScript.maze_turnLeft;

Blockly.Language.maze_isWall = {
  // Block for checking if there a wall.
  category: 'Maze',
  helpUrl: 'http://code.google.com/p/google-blockly/wiki/Wall',
  init: function() {
    this.setColour(290);
    this.setOutput(true);
    this.addTitle('wall');
    var dropdown = new Blockly.FieldDropdown(function() {
      return Blockly.Language.maze_isWall.DIRECTIONS;
    });
    this.addTitle(dropdown);
    this.setTooltip('Returns true if there is a wall in ' +
                    'the specified direction.');
  }
};

Blockly.Language.maze_isWall.DIRECTIONS =
    ['ahead', 'to the left', 'to the right', 'behind'];

Blockly.JavaScript.maze_isWall = function() {
  // Generate JavaScript for checking if there is a wall.
  var direction = Blockly.Language.maze_isWall.DIRECTIONS
      .indexOf(this.getTitleText(1));
  return 'Maze.isWall(' + direction + ')';
};
