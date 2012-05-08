/**
 * Visual Blocks Editor
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
 * @fileoverview XML reader and writer.
 * @author fraser@google.com (Neil Fraser)
 */

Blockly.Xml = {};

/**
 * Encode a block tree as XML.
 * @param {!Object} blockGroup The SVG workspace.
 * @return {!Element} XML document.
 */
Blockly.Xml.workspaceToDom = function(blockGroup) {
  var xml = document.createElement('xml');
  var blocks = blockGroup.getTopBlocks();
  for (var i = 0, block; block = blocks[i]; i++) {
    var element = Blockly.Xml.blockToDom_(block);
    var xy = block.getRelativeToSurfaceXY();
    element.setAttribute('x', Blockly.RTL ? -xy.x : xy.x);
    element.setAttribute('y', xy.y);
    xml.appendChild(element);
  }
  return xml;
};

/**
 * Encode a block subtree as XML.
 * @param {!Blockly.Block} block The root block to encode.
 * @return {!Element} Tree of XML elements.
 * @private
 */
Blockly.Xml.blockToDom_ = function(block) {
  var element = document.createElement('block');
  element.setAttribute('type', block.type);
  for (var i = 0, title; title = block.titleRow[i]; i++) {
    var container = document.createElement('title');
    var titleText = document.createTextNode(title.getText());
    container.appendChild(titleText);
    element.appendChild(container);
  }

  if (block.comment) {
    var commentElement = document.createElement('comment');
    var commentText = document.createTextNode(block.comment.getText());
    commentElement.appendChild(commentText);
    commentElement.setAttribute('pinned', block.comment.isPinned());
    var xy = block.comment.getBubbleLocation();
    commentElement.setAttribute('x', xy.x);
    commentElement.setAttribute('y', xy.y);
    var hw = block.comment.getBubbleSize();
    commentElement.setAttribute('h', hw.height);
    commentElement.setAttribute('w', hw.width);
    element.appendChild(commentElement);
  }

  var valueInputs = false;
  for (var i = 0, input; input = block.inputList[i]; i++) {
    var container;
    if (input.type == Blockly.LOCAL_VARIABLE) {
      container = document.createElement('variable');
      container.setAttribute('data', input.getText());
    } else {
      if (input.type == Blockly.INPUT_VALUE) {
        valueInputs = true;
        container = document.createElement('value');
      } else if (input.type == Blockly.NEXT_STATEMENT) {
        container = document.createElement('statement');
      }
      var childBlock = input.targetBlock();
      if (childBlock) {
        container.appendChild(Blockly.Xml.blockToDom_(childBlock));
      }
    }
    if (input.label && input.label.getText) {
      container.setAttribute('label', input.label.getText());
    }
    element.appendChild(container);
  }
  if (valueInputs) {
    element.setAttribute('inline', block.inputsInline);
  }
  if (block.collapsed) {
    element.setAttribute('collapsed', true);
  }

  if (block.nextConnection) {
    var container = document.createElement('next');
    var nextBlock = block.nextConnection.targetBlock();
    if (nextBlock) {
      container.appendChild(Blockly.Xml.blockToDom_(nextBlock));
    }
    element.appendChild(container);
  }

  return element;
};

/**
 * Converts a DOM structure into plain text.
 * Currently the text format is fairly ugly: all one line with no whitespace.
 * @param {!Element} dom A tree of XML elements.
 * @return {string} Text representation.
 */
Blockly.Xml.domToText = function(dom) {
  var oSerializer = new XMLSerializer();
  return oSerializer.serializeToString(dom);
};

/**
 * Converts plain text into a DOM structure.
 * Throws an error if XML doesn't parse.
 * @param {string} text Text representation.
 * @return {!Element} A tree of XML elements.
 */
Blockly.Xml.textToDom = function(text) {
  var oParser = new DOMParser();
  var dom = oParser.parseFromString(text, 'text/xml');
  // The DOM should have one and only one top-level node, an XML tag.
  if (!dom || !dom.firstChild || dom.firstChild.tagName != 'xml' ||
      dom.firstChild !== dom.lastChild) {
    // Whatever we got back from the parser is not XML.
    throw 'Blockly.Xml.textToDom did not obtain a valid XML tree.';
  }
  return dom.firstChild;
};

/**
 * Decode an XML DOM and create blocks on the workspace.
 * @param {!Object} blockGroup The SVG workspace.
 * @param {!Element} xml XML DOM.
 */
Blockly.Xml.domToWorkspace = function(blockGroup, xml) {
  for (var x = 0, xmlChild; xmlChild = xml.childNodes[x]; x++) {
    if (xmlChild.nodeName == 'block') {
      var block = Blockly.Xml.domToBlock_(blockGroup, xmlChild);
      var blockX = parseInt(xmlChild.getAttribute('x'), 10);
      var blockY = parseInt(xmlChild.getAttribute('y'), 10);
      if (!isNaN(blockX) && !isNaN(blockY)) {
        block.moveBy(Blockly.RTL ? -blockX : blockX, blockY);
      }
    }
  }
};

/**
 * Decode an XML block tag and create a block (and possibly sub blocks) on the
 * workspace.
 * @param {!Object} blockGroup The SVG workspace.
 * @param {!Element} xmlBlock XML block element.
 * @return {!Blockly.Block} The root block created.
 * @private
 */
Blockly.Xml.domToBlock_ = function(blockGroup, xmlBlock) {
  var prototypeName = xmlBlock.getAttribute('type');
  var block = new Blockly.Block(blockGroup, prototypeName);

  var inline = xmlBlock.getAttribute('inline');
  if (inline) {
    block.setInputsInline(inline == 'true');
  }

  var collapsed = xmlBlock.getAttribute('collapsed');
  if (collapsed) {
    block.setCollapsed(collapsed == 'true');
  }

  var titleIndex = -1;
  var inputIndicies = {};
  inputIndicies[Blockly.LOCAL_VARIABLE] = -1;
  inputIndicies[Blockly.INPUT_VALUE] = -1;
  inputIndicies[Blockly.NEXT_STATEMENT] = -1;
  /**
   * Returns the next input of a given type.
   * Closure: uses inputIndicies and block from external scope.
   * @param {number} type The type of input to search for.
   * @return {Object} The next input or null if none.
   */
  function nextInput(type) {
    var startIndex = inputIndicies[type];
    for (var i = startIndex + 1; i < block.inputList.length; i++) {
      if (block.inputList[i].type == type) {
        inputIndicies[type] = i;
        return block.inputList[i];
      }
    }
    return null;
  }

  for (var x = 0, xmlChild; xmlChild = xmlBlock.childNodes[x]; x++) {
    var blockChild = null;
    var input;
    switch (xmlChild.tagName.toLowerCase()) {
      case 'comment':
        block.setCommentText(xmlChild.textContent);
        var pinned = xmlChild.getAttribute('pinned');
        if (pinned) {
          block.comment.setPinned(pinned == 'true');
        }
        var bubbleX = parseInt(xmlChild.getAttribute('x'), 10);
        var bubbleY = parseInt(xmlChild.getAttribute('y'), 10);
        if (!isNaN(bubbleX) && !isNaN(bubbleY)) {
          block.comment.setBubbleLocation(bubbleX, bubbleY, false);
        }
        var bubbleW = parseInt(xmlChild.getAttribute('w'), 10);
        var bubbleH = parseInt(xmlChild.getAttribute('h'), 10);
        if (!isNaN(bubbleW) && !isNaN(bubbleH)) {
          block.comment.setBubbleSize(bubbleW, bubbleH);
        }
        break;
      case 'title':
        block.setTitleText(xmlChild.textContent, ++titleIndex);
        break;
      case 'variable':
        var data = xmlChild.getAttribute('data');
        if (data !== null) {
          input = nextInput(Blockly.LOCAL_VARIABLE);
          if (!input) {
            throw 'Variable input does not exist.';
          }
          input.setText(data);
        }
        break;
      case 'value':
        input = nextInput(Blockly.INPUT_VALUE);
        if (!input) {
          throw 'Value input does not exist.';
        }
        if (xmlChild.firstChild && xmlChild.firstChild.tagName == 'block') {
          blockChild = Blockly.Xml.domToBlock_(blockGroup, xmlChild.firstChild);
          if (!blockChild.outputConnection) {
            throw 'Child block does not have output value.';
          }
          input.connect(blockChild.outputConnection);
        }
        break;
      case 'statement':
        input = nextInput(Blockly.NEXT_STATEMENT);
        if (!input) {
          throw 'Statement input does not exist.';
        }
        if (xmlChild.firstChild && xmlChild.firstChild.tagName == 'block') {
          blockChild = Blockly.Xml.domToBlock_(blockGroup, xmlChild.firstChild);
          if (!blockChild.previousConnection) {
            throw 'Child block does not have previous statement.';
          }
          input.connect(blockChild.previousConnection);
        }
        break;
      case 'next':
        if (xmlChild.firstChild && xmlChild.firstChild.tagName == 'block') {
          if (!block.nextConnection) {
            throw 'Next statement does not exist.';
          } else if (block.nextConnection.targetConnection) {
            // This could happen if there is more than one XML 'next' tag.
            throw 'Next statement is already connected.';
          }
          blockChild = Blockly.Xml.domToBlock_(blockGroup, xmlChild.firstChild);
          if (!blockChild.previousConnection) {
            throw 'Next block does not have previous statement.';
          }
          block.nextConnection.connect(blockChild.previousConnection);
        }
        break;
      default:
        // Unknown tag; ignore.  Same principle as HTML parsers.
    }
    var labelText = xmlChild.getAttribute('label');
    if (labelText !== null && input && input.label && input.label.setText) {
      input.label.setText(labelText);
    }
  }
  block.render();
  return block;
};
