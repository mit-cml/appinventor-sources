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
  var blocks = blockGroup.getTopBlocks(false);
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
  if (block.mutationToDom) {
    // Custom data for an advanced block.
    var mutation = block.mutationToDom();
    if (mutation) {
      element.appendChild(mutation);
    }
  }
  for (var i = 0, title; title = block.titleRow[i]; i++) {
    if (title.EDITABLE) {
      var container = document.createElement('title');
      container.setAttribute('i', i);
      var titleText = document.createTextNode(title.getText());
      container.appendChild(titleText);
      element.appendChild(container);
    }
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

  var variableCount = 0;
  var valueCount = 0;
  var statementCount = 0;
  for (var i = 0, input; input = block.inputList[i]; i++) {
    var container;
    var empty = true;
    if (input.type == Blockly.LOCAL_VARIABLE) {
      container = document.createElement('variable');
      container.setAttribute('name', input.getText());
      container.setAttribute('i', variableCount);
      variableCount++;
      empty = false;
    } else {
      var childBlock = input.targetBlock();
      if (input.type == Blockly.INPUT_VALUE) {
        container = document.createElement('value');
        container.setAttribute('i', valueCount);
        valueCount++;
      } else if (input.type == Blockly.NEXT_STATEMENT) {
        container = document.createElement('statement');
        container.setAttribute('i', statementCount);
        statementCount++;
      }
      if (childBlock) {
        container.appendChild(Blockly.Xml.blockToDom_(childBlock));
        empty = false;
      }
    }
    if (input.label && input.label.EDITABLE && input.label.getText) {
      container.setAttribute('label', input.label.getText());
      empty = false;
    }
    if (!empty) {
      element.appendChild(container);
    }
  }
  if (valueCount) {
    element.setAttribute('inline', block.inputsInline);
  }
  if (block.collapsed) {
    element.setAttribute('collapsed', true);
  }

  if (block.nextConnection) {
    var nextBlock = block.nextConnection.targetBlock();
    if (nextBlock) {
      var container = document.createElement('next');
      container.appendChild(Blockly.Xml.blockToDom_(nextBlock));
      element.appendChild(container);
    }
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
 * Converts a DOM structure into properly indented text.
 * @param {!Element} dom A tree of XML elements.
 * @return {string} Text representation.
 */
Blockly.Xml.domToPrettyText = function(dom) {
  // This function is not guaranteed to be correct for all XML.
  // But it handles the XML that Blockly generates.
  var line = Blockly.Xml.domToText(dom);
  // Add place every open and close tag on its own line.
  var lines = line.split('<');
  // Indent every line.
  var indent = '';
  for (var x = 1; x < lines.length; x++) {
    var nextChar = lines[x][0];
    if (nextChar == '/') {
      indent = indent.substring(2);
    }
    lines[x] = indent + '<' + lines[x];
    if (nextChar != '/') {
      indent += '  ';
    }
  }
  // Pull simple tags back together.
  // E.g. <foo></foo>
  var text = lines.join('\n');
  text = text.replace(/(<(\w+)[^>]*>[^\n]*)\n *<\/\2>/g, '$1</$2>');
  // Trim leading blank line.
  return text.replace(/^\n/, '');
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
    if (xmlChild.nodeName && xmlChild.nodeName.toLowerCase() == 'block') {
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
  block.initSvg();

  var inline = xmlBlock.getAttribute('inline');
  if (inline) {
    block.setInputsInline(inline == 'true');
  }

  var collapsed = xmlBlock.getAttribute('collapsed');
  if (collapsed) {
    block.setCollapsed(collapsed == 'true');
  }

  /**
   * Returns the next input of a given type.
   * Closure: uses block from external scope.
   * @param {number} type The type of input to search for.
   * @param {number} n The index of this input in its type.
   * @return {Object} The next input or null if none.
   */
  function getInput(type, n) {
    for (var i = 0; i < block.inputList.length; i++) {
      if (block.inputList[i].type == type) {
        if (!n) {
          return block.inputList[i];
        }
        n--;
      }
    }
    return null;
  }

  for (var x = 0, xmlChild; xmlChild = xmlBlock.childNodes[x]; x++) {
    if (xmlChild.nodeType == 3 && xmlChild.data.match(/^\s*$/)) {
      // Extra whitespace between tags does not concern us.
      continue;
    }
    var blockChild = null;
    var input;

    // Find the first 'real' grandchild node (that isn't whitespace).
    var firstRealGrandchild = null;
    for (var y = 0, grandchildNode; grandchildNode = xmlChild.childNodes[y]; y++) {
      if (grandchildNode.nodeType != 3 || !grandchildNode.data.match(/^\s*$/)) {
        firstRealGrandchild = grandchildNode;
      }
    }

    switch (xmlChild.tagName.toLowerCase()) {
      case 'mutation':
        // Custom data for an advanced block.
        if (block.domToMutation) {
          block.domToMutation(xmlChild);
        }
        break;
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
        var i = parseInt(xmlChild.getAttribute('i'), 10);
        block.setTitleText(xmlChild.textContent, i);
        break;
      case 'variable':
        var i = parseInt(xmlChild.getAttribute('i'), 10);
        var data = xmlChild.getAttribute('name');
        if (data !== null) {
          input = getInput(Blockly.LOCAL_VARIABLE, i);
          if (!input) {
            throw 'Variable input does not exist.';
          }
          input.setText(data);
        }
        break;
      case 'value':
        var i = parseInt(xmlChild.getAttribute('i'), 10);
        input = getInput(Blockly.INPUT_VALUE, i);
        if (!input) {
          throw 'Value input does not exist.';
        }
        if (firstRealGrandchild && firstRealGrandchild.tagName &&
            firstRealGrandchild.tagName.toLowerCase() == 'block') {
          blockChild = Blockly.Xml.domToBlock_(blockGroup, firstRealGrandchild);
          if (!blockChild.outputConnection) {
            throw 'Child block does not have output value.';
          }
          input.connect(blockChild.outputConnection);
        }
        break;
      case 'statement':
        var i = parseInt(xmlChild.getAttribute('i'), 10);
        input = getInput(Blockly.NEXT_STATEMENT, i);
        if (!input) {
          throw 'Statement input does not exist.';
        }
        if (firstRealGrandchild && firstRealGrandchild.tagName &&
            firstRealGrandchild.tagName.toLowerCase() == 'block') {
          blockChild = Blockly.Xml.domToBlock_(blockGroup, firstRealGrandchild);
          if (!blockChild.previousConnection) {
            throw 'Child block does not have previous statement.';
          }
          input.connect(blockChild.previousConnection);
        }
        break;
      case 'next':
        if (firstRealGrandchild && firstRealGrandchild.tagName &&
            firstRealGrandchild.tagName.toLowerCase() == 'block') {
          if (!block.nextConnection) {
            throw 'Next statement does not exist.';
          } else if (block.nextConnection.targetConnection) {
            // This could happen if there is more than one XML 'next' tag.
            throw 'Next statement is already connected.';
          }
          blockChild = Blockly.Xml.domToBlock_(blockGroup, firstRealGrandchild);
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

/**
 * Find the first 'real' child of a node, skipping whitespace text nodes.
 * Return true if that child is of the the specified type (case insensitive).
 * @param {!Node} parentNode The parent node.
 * @param {string} tagName The node type to check for.
 * @return {boolean} True if the first real child is the specified type.
 * @private
 */
Blockly.Xml.isFirstRealChild_ = function(parentNode, tagName) {
  for (var x = 0, childNode; childNode = parentNode.childNodes[x]; x++) {
    if (childNode.nodeType != 3 || !childNode.data.match(/^\s*$/)) {
      return childNode.tagName && childNode.tagName.toLowerCase() == tagName;
    }
  }
  return false;
};
