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

/**
 * Create a namespace for the maze.
 */
var Maze = {};

/**
 * Pixel height and width of each maze square.
 */
Maze.SIZE = 50;

/**
 * Miliseconds between each animation frame.
 */
Maze.STEP_SPEED = 150;

/**
 * The maze's map is a 2D array of numbers.
 * 0: Empty space.
 * 1: Wall.
 * 2: Starting square.
 * 3. Finish square.
 */
Maze.MAP = [
  [1, 1, 1, 1, 1, 1, 1, 1],
  [1, 0, 0, 1, 0, 1, 3, 1],
  [1, 0, 0, 1, 0, 0, 0, 1],
  [1, 0, 1, 1, 0, 1, 1, 1],
  [1, 0, 0, 0, 0, 0, 0, 1],
  [1, 1, 0, 1, 1, 1, 0, 1],
  [1, 2, 0, 0, 0, 1, 0, 1],
  [1, 1, 1, 1, 1, 1, 1, 1]];

/**
 * Constants for cardinal directions.
 */
Maze.NORTH = 0;
Maze.EAST = 1;
Maze.SOUTH = 2;
Maze.WEST = 3;

/**
 * PIDs of animation tasks currently executing.
 */
Maze.pidList = [];

/**
 * Initialize Blockly and the maze.  Called on page load.
 * @param {!Blockly} blockly Instance of Blockly from iframe.
 */
Maze.init = function(blockly) {
  window.Blockly = blockly;

  window.onbeforeunload = function() {
    if (Blockly.mainWorkspace.getAllBlocks().length > 1) {
      return 'Leaving this page will result in the loss of your work.';
    }
    return null;
  };

  // Load the editor with a starting block.
  var xml = Blockly.Xml.textToDom(
      '<xml>' +
      '  <block type="maze_move" x="85" y="100"></block>' +
      '</xml>');
  Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);

  // Locate the start and finish squares.
  for (var y = 0; y < Maze.MAP.length; y++) {
    for (var x = 0; x < Maze.MAP[0].length; x++) {
      if (Maze.MAP[y][x] == 2) {
        Maze.start_ = {x: x, y: y};
      } else if (Maze.MAP[y][x] == 3) {
        Maze.finish_ = {x: x, y: y};
      }
    }
  }

  // Record the map's offset.
  Maze.mapOffsetLeft_ = 0;
  Maze.mapOffsetTop_ = 0;
  var element = document.getElementById('map');
  while (element) {
    Maze.mapOffsetLeft_ += element.offsetLeft;
    Maze.mapOffsetTop_ += element.offsetTop;
    element = element.offsetParent;
  }

  // Move the finish icon into position.
  var finishIcon = document.getElementById('finish');
  finishIcon.style.top = Maze.mapOffsetTop_ +
      Maze.SIZE * (Maze.finish_.y + 0.5) - finishIcon.offsetHeight;
  finishIcon.style.left = Maze.mapOffsetLeft_ +
      Maze.SIZE * (Maze.finish_.x + 0.5) - finishIcon.offsetWidth / 2;

  Maze.reset();
};

/**
 * Reset the maze to the start position and kill any pending animation tasks.
 */
Maze.reset = function() {
  Maze.pegmanX = Maze.start_.x;
  Maze.pegmanY = Maze.start_.y;
  Maze.pegmanD = Maze.EAST;
  Maze.displayPegman(Maze.pegmanX, Maze.pegmanY, Maze.pegmanD * 4);
  // Kill all tasks.
  for (var x = 0; x < Maze.pidList.length; x++) {
    window.clearTimeout(Maze.pidList[x]);
  }
  Maze.pidList = [];
};

/**
 * Click the run button.  Start the program.
 */
Maze.runButtonClick = function() {
  document.getElementById('runButton').style.display = 'none';
  document.getElementById('resetButton').style.display = 'inline';
  Blockly.mainWorkspace.traceOn(true);
  Maze.execute();
};

/**
 * Click the reset button.  Reset the maze.
 */
Maze.resetButtonClick = function() {
  document.getElementById('runButton').style.display = 'inline';
  document.getElementById('resetButton').style.display = 'none';
  Blockly.mainWorkspace.traceOn(false);
  Maze.reset();
};

/**
 * Execute the user's code.  Heaven help us...
 */
Maze.execute = function() {
  Maze.path = [];
  Maze.ticks = 1000;
  var code = Blockly.Generator.workspaceToCode('JavaScript');
  try {
    eval(code);
  } catch (e) {
    // A boolean is thrown for normal termination.
    // Abnormal termination is a user error.
    if (typeof e != 'boolean') {
      alert(e);
    }
  }
  // Maze.path now contains a transcript of all the user's actions.
  // Reset the maze and animate the transcript.
  Maze.reset();
  Maze.pidList.push(window.setTimeout(Maze.animate, 100));
};

/**
 * Iterate through the recorded path and animate pegman's actions.
 */
Maze.animate = function() {
  // All tasks should be complete now.  Clean up the PID list.
  Maze.pidList = [];
  var action;
  do {
    var pair = Maze.path.shift();
    if (!pair) {
      return;
    }
    action = pair[0];
    Blockly.mainWorkspace.highlightBlock(pair[1]);
  } while (action == 'look');


  if (action == 'north') {
    Maze.schedule([Maze.pegmanX, Maze.pegmanY, Maze.pegmanD * 4],
                  [Maze.pegmanX, Maze.pegmanY - 1, Maze.pegmanD * 4]);
    Maze.pegmanY--;
  } else if (action == 'east') {
    Maze.schedule([Maze.pegmanX, Maze.pegmanY, Maze.pegmanD * 4],
                  [Maze.pegmanX + 1, Maze.pegmanY, Maze.pegmanD * 4]);
    Maze.pegmanX++;
  } else if (action == 'south') {
    Maze.schedule([Maze.pegmanX, Maze.pegmanY, Maze.pegmanD * 4],
                  [Maze.pegmanX, Maze.pegmanY + 1, Maze.pegmanD * 4]);
    Maze.pegmanY++;
  } else if (action == 'west') {
    Maze.schedule([Maze.pegmanX, Maze.pegmanY, Maze.pegmanD * 4],
                  [Maze.pegmanX - 1, Maze.pegmanY, Maze.pegmanD * 4]);
    Maze.pegmanX--;
  } else if (action.substring(0, 4) == 'fail') {
    Maze.scheduleFail(action.substring(5) == 'forward');
  } else if (action == 'left') {
    Maze.schedule([Maze.pegmanX, Maze.pegmanY, Maze.pegmanD * 4],
                  [Maze.pegmanX, Maze.pegmanY, Maze.pegmanD * 4 - 4]);
    Maze.pegmanD = Maze.constrainDirection4(Maze.pegmanD - 1);
  } else if (action == 'right') {
    Maze.schedule([Maze.pegmanX, Maze.pegmanY, Maze.pegmanD * 4],
                  [Maze.pegmanX, Maze.pegmanY, Maze.pegmanD * 4 + 4]);
    Maze.pegmanD = Maze.constrainDirection4(Maze.pegmanD + 1);
  } else if (action == 'finish') {
    Maze.scheduleFinish();
  }

  Maze.pidList.push(window.setTimeout(Maze.animate, Maze.STEP_SPEED * 5));
};

/**
 * Schedule the animations for a move or turn.
 * @param {!Array.<number>} startPos X, Y and direction starting points.
 * @param {!Array.<number>} endPos X, Y and direction ending points.
 */
Maze.schedule = function(startPos, endPos) {
  var deltas = [(endPos[0] - startPos[0]) / 4,
                (endPos[1] - startPos[1]) / 4,
                (endPos[2] - startPos[2]) / 4];
  Maze.displayPegman(startPos[0] + deltas[0],
                     startPos[1] + deltas[1],
                     Maze.constrainDirection16(startPos[2] + deltas[2]));
  Maze.pidList.push(window.setTimeout(function() {
      Maze.displayPegman(startPos[0] + deltas[0] * 2,
          startPos[1] + deltas[1] * 2,
          Maze.constrainDirection16(startPos[2] + deltas[2] * 2));
    }, Maze.STEP_SPEED));
  Maze.pidList.push(window.setTimeout(function() {
      Maze.displayPegman(startPos[0] + deltas[0] * 3,
          startPos[1] + deltas[1] * 3,
          Maze.constrainDirection16(startPos[2] + deltas[2] * 3));
    }, Maze.STEP_SPEED * 2));
  Maze.pidList.push(window.setTimeout(function() {
      Maze.displayPegman(endPos[0], endPos[1],
          Maze.constrainDirection16(endPos[2]));
    }, Maze.STEP_SPEED * 3));
};

/**
 * Schedule the animations for a failed move.
 * @param {boolean} forward True if forward, false if backward.
 */
Maze.scheduleFail = function(forward) {
  var deltaX = 0;
  var deltaY = 0;
  if (Maze.pegmanD == 0) {
    deltaY = -0.25;
  } else if (Maze.pegmanD == 1) {
    deltaX = 0.25;
  } else if (Maze.pegmanD == 2) {
    deltaY = 0.25;
  } else if (Maze.pegmanD == 3) {
    deltaX = -0.25;
  }
  if (!forward) {
    deltaX = - deltaX;
    deltaY = - deltaY;
  }
  var direction16 = Maze.constrainDirection16(Maze.pegmanD * 4);
  Maze.displayPegman(Maze.pegmanX + deltaX,
                     Maze.pegmanY + deltaY,
                     direction16);
  Maze.pidList.push(window.setTimeout(function() {
    Maze.displayPegman(Maze.pegmanX,
                       Maze.pegmanY,
                       direction16);
    }, Maze.STEP_SPEED));
  Maze.pidList.push(window.setTimeout(function() {
    Maze.displayPegman(Maze.pegmanX + deltaX,
                       Maze.pegmanY + deltaY,
                       direction16);
    }, Maze.STEP_SPEED * 2));
  Maze.pidList.push(window.setTimeout(function() {
      Maze.displayPegman(Maze.pegmanX, Maze.pegmanY, direction16);
    }, Maze.STEP_SPEED * 3));
};

/**
 * Schedule the animations for a victory dance.
 */
Maze.scheduleFinish = function() {
  var direction16 = Maze.constrainDirection16(Maze.pegmanD * 4);
  Maze.displayPegman(Maze.pegmanX, Maze.pegmanY, 16);
  Maze.pidList.push(window.setTimeout(function() {
    Maze.displayPegman(Maze.pegmanX, Maze.pegmanY, 17);
    }, Maze.STEP_SPEED));
  Maze.pidList.push(window.setTimeout(function() {
    Maze.displayPegman(Maze.pegmanX, Maze.pegmanY, 16);
    }, Maze.STEP_SPEED * 2));
  Maze.pidList.push(window.setTimeout(function() {
      Maze.displayPegman(Maze.pegmanX, Maze.pegmanY, direction16);
    }, Maze.STEP_SPEED * 3));
};

/**
 * Display Pegman at a the specified location, facing the specified direction.
 * @param {number} x Horizontal grid (or fraction thereof).
 * @param {number} y Vertical grid (or fraction thereof).
 * @param {number} d Direction (0 - 15) or dance (16 - 17).
 */
Maze.displayPegman = function(x, y, d) {
  var pegmanIcon = document.getElementById('pegman');
  pegmanIcon.style.top = Maze.mapOffsetTop_ +
      Maze.SIZE * (y + 0.5) - pegmanIcon.offsetHeight / 2 - 8;
  pegmanIcon.style.left = Maze.mapOffsetLeft_ +
      Maze.SIZE * (x + 0.5) - pegmanIcon.offsetHeight / 2 + 2;
  pegmanIcon.style.backgroundPosition = -d * pegmanIcon.offsetWidth;
};

/**
 * Keep the direction within 0-3, wrapping at both ends.
 * @param {number} d Potentially out-of-bounds direction value.
 * @return {number} Legal direction value.
 */
Maze.constrainDirection4 = function(d) {
  if (d < 0) {
    d += 4;
  } else if (d > 3) {
    d -= 4;
  }
  return d;
};

/**
 * Keep the direction within 0-15, wrapping at both ends.
 * @param {number} d Potentially out-of-bounds direction value.
 * @return {number} Legal direction value.
 */
Maze.constrainDirection16 = function(d) {
  if (d < 0) {
    d += 16;
  } else if (d > 15) {
    d -= 16;
  }
  return d;
};

/**
 * If the user has executed too many actions, we're probably in an infinite
 * loop.  Sadly I wasn't able to solve the Halting Problem for this demo.
 * @throws {false} Throws an error to terminate the user's program.
 */
Maze.checkTimeout = function(id) {
  if (Maze.ticks-- < 0) {
    if (id) {
      // Highlight an infinite loop on death.
      Maze.path.push(['loop', id]);
    }
    throw false;
  }
};

/**
 * Show the user's code in raw JavaScript.
 */
Maze.showCode = function() {
  var code = Blockly.Generator.workspaceToCode('JavaScript');
  code += '\n\n' +
          '[The serial numbers are just used to highlight blocks when run.]';
  alert(code);
};

// API

Maze.moveForward = function(id) {
  Maze.move(0, id);
};

Maze.moveBackward = function(id) {
  Maze.move(2, id);
};

Maze.turnLeft = function(id) {
  Maze.turn(0, id);
};

Maze.turnRight = function(id) {
  Maze.turn(1, id);
};

Maze.isWallForward = function() {
  return Maze.isWall(0);
};

Maze.isWallRight = function() {
  return Maze.isWall(1);
};

Maze.isWallBackward = function() {
  return Maze.isWall(2);
};

Maze.isWallLeft = function() {
  return Maze.isWall(3);
};

//

/**
 * Move pegman forward or backward.
 * @param {number} direction Direction to move (0 = forward, 2 = backward).
 * @param {string} id ID of block that triggered this action.
 */
Maze.move = function(direction, id) {
  if (Maze.isWall(direction)) {
    Maze.path.push(['fail_' + (direction ? 'backward' : 'forward'), id]);
    return;
  }
  // If moving backward, flip the effective direction.
  var effectiveDirection = Maze.pegmanD + direction;
  effectiveDirection = Maze.constrainDirection4(effectiveDirection);
  var command;
  if (effectiveDirection == Maze.NORTH) {
    Maze.pegmanY--;
    command = 'north';
  } else if (effectiveDirection == Maze.EAST) {
    Maze.pegmanX++;
    command = 'east';
  } else if (effectiveDirection == Maze.SOUTH) {
    Maze.pegmanY++;
    command = 'south';
  } else if (effectiveDirection == Maze.WEST) {
    Maze.pegmanX--;
    command = 'west';
  }
  Maze.path.push([command, id]);
  if (Maze.pegmanX == Maze.finish_.x && Maze.pegmanY == Maze.finish_.y) {
    // Finished.  Terminate the user's program.
    Maze.path.push(['finish', null]);
    throw true;
  }
};

/**
 * Turn pegman left or right.
 * @param {number} direction Direction to turn (0 = left, 1 = right).
 * @param {string} id ID of block that triggered this action.
 */
Maze.turn = function(direction, id) {
  if (direction) {
    // Right turn (clockwise).
    Maze.pegmanD++;
    Maze.path.push(['right', id]);
  } else {
    // Left turn (counterclockwise).
    Maze.pegmanD--;
    Maze.path.push(['left', id]);
  }
  Maze.pegmanD = Maze.constrainDirection4(Maze.pegmanD);
};

/**
 * Is there a wall next to pegman?
 * @param {number} direction Direction to look
 *     (0 = forward, 1 = right, 2 = backward, 3 = left).
 * @return {boolean} True if there is a wall.
 */
Maze.isWall = function(direction) {
  var effectiveDirection = Maze.pegmanD + direction;
  effectiveDirection = Maze.constrainDirection4(effectiveDirection);
  var square;
  if (effectiveDirection == Maze.NORTH) {
    square = Maze.MAP[Maze.pegmanY - 1][Maze.pegmanX];
  } else if (effectiveDirection == Maze.EAST) {
    square = Maze.MAP[Maze.pegmanY][Maze.pegmanX + 1];
  } else if (effectiveDirection == Maze.SOUTH) {
    square = Maze.MAP[Maze.pegmanY + 1][Maze.pegmanX];
  } else if (effectiveDirection == Maze.WEST) {
    square = Maze.MAP[Maze.pegmanY][Maze.pegmanX - 1];
  }
  return square == 1;
};
