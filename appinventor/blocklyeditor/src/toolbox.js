/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
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
 * @fileoverview Toolbox from whence to create blocks.
 * In the interests of a consistent UI, the toolbox shares some functions and
 * properties with the context menu.
 * @author fraser@google.com (Neil Fraser)
 */

// Name space for the toolbox.
Blockly.Toolbox = {};

/**
 * Width of the toolbox.
 */
Blockly.Toolbox.width = 0;

/**
 * The SVG group currently selected.
 * @type {Element}
 * @private
 */
Blockly.Toolbox.selectedOption_ = null;

/**
 * Creates the toolbox's DOM.  Only needs to be called once.
 * @return {!Element} The toolbox's SVG group.
 */
Blockly.Toolbox.createDom = function() {
  Blockly.Toolbox.flyout_ = new Blockly.Flyout();
  /*
  <g>
    [flyout]
    <rect class="blocklyToolboxBackground" height="100%"/>
    <g class="blocklyToolboxOptions">
    </g>
  </g>
  */
  var svgGroup = Blockly.createSvgElement('g', {}, null);
  Blockly.Toolbox.svgGroup_ = svgGroup;
  var flyoutGroup = Blockly.Toolbox.flyout_.createDom();
  svgGroup.appendChild(flyoutGroup);
  Blockly.Toolbox.svgBackground_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyToolboxBackground', height: '100%'}, svgGroup);
  Blockly.Toolbox.svgOptions_ = Blockly.createSvgElement('g',
      {'class': 'blocklyToolboxOptions'}, svgGroup);
  return svgGroup;
};

/**
 * Return an object with all the metrics required to size scrollbars for the
 * toolbox.  The following properties are computed:
 * .viewHeight: Height of the visible rectangle,
 * .viewWidth: Width of the visible rectangle,
 * .contentHeight: Height of the contents,
 * .viewTop: Offset of top edge of visible rectangle from parent,
 * .contentTop: Offset of the top-most content from the y=0 coordinate,
 * .absoluteTop: Top-edge of view.
 * .absoluteLeft: Left-edge of view.
 * @return {!Object} Contains size and position metrics of the toolbox.
 */
Blockly.Toolbox.getMetrics = function() {
  var viewHeight = Blockly.svgSize().height;
  var viewWidth = Blockly.Toolbox.width;
  var optionBox = Blockly.Toolbox.svgOptions_.getBBox();
  return {
    viewHeight: viewHeight,
    viewWidth: viewWidth,
    contentHeight: optionBox.height + optionBox.y,
    viewTop: -Blockly.Toolbox.svgOptions_.scrollY,
    contentTop: 0,
    absoluteTop: 0,
    // absoluteLeft should be 0, but Firefox leaks by a pixel.
    absoluteLeft: Blockly.RTL ? -1 : 1
  };
};

/**
 * Sets the Y translation of the toolbox to match the scrollbars.
 * @param {!Object} yRatio Contains a y property which is a float
 *     between 0 and 1 specifying the degree of scrolling.
 */
Blockly.Toolbox.setMetrics = function(yRatio) {
  var metrics = Blockly.Toolbox.getMetrics();
  if (typeof yRatio.y == 'number') {
    Blockly.Toolbox.svgOptions_.scrollY = -metrics.contentHeight * yRatio.y -
        metrics.contentTop;
  }
  Blockly.Toolbox.svgOptions_.setAttribute('transform', 'translate(0,' +
      (Blockly.Toolbox.svgOptions_.scrollY + metrics.absoluteTop) + ')');
};

/**
 * Initializes the toolbox.
 */
Blockly.Toolbox.init = function() {
  Blockly.Toolbox.flyout_.init(Blockly.mainWorkspace,
                               Blockly.getMainWorkspaceMetrics);
  Blockly.Toolbox.languageTree = Blockly.Toolbox.buildTree_();
  Blockly.Toolbox.populateOptions_(Blockly.Toolbox.languageTree);

  // Add scrollbars.
  new Blockly.Scrollbar(Blockly.Toolbox.svgOptions_,
      Blockly.Toolbox.getMetrics, Blockly.Toolbox.setMetrics,
      false, false);

  Blockly.Toolbox.position_();

  // If the document resizes, reposition the toolbox.
  Blockly.bindEvent_(window, 'resize', null, Blockly.Toolbox.position_);
};

/**
 * Move the toolbox to the edge.
 * @private
 */
Blockly.Toolbox.position_ = function() {
  var svgSize = Blockly.svgSize();
  if (Blockly.RTL) {
    Blockly.Toolbox.svgGroup_.setAttribute('transform',
        'translate(' + (svgSize.width - Blockly.Toolbox.width) + ',0)');
  }
};

/**
 * String to prefix on categories of each block in the toolbox.
 * Used to prevent collisions with built-in properties like 'toString'.
 * @private
 */
Blockly.Toolbox.PREFIX_ = 'cat_';

/**
 * Category used for variables.
 */
Blockly.Toolbox.VARIABLE_CAT = 'variables';

/**
 * Build the hierarchical tree of block types.
 * @return {!Object} Tree object.
 * @private
 */
Blockly.Toolbox.buildTree_ = function() {
  var tree = {};
  // Populate the tree structure.
  for (var name in Blockly.Language) {
    var block = Blockly.Language[name];
    // Blocks without a category are fragments used by the mutator dialog.
    if (block.category) {
      var cat = Blockly.Toolbox.PREFIX_ + window.encodeURI(block.category);
      if (cat in tree) {
        tree[cat].push(name);
      } else {
        tree[cat] = [name];
      }
    }
  }
  return tree;
};

/**
 * Fill the toolbox with options.
 * @param {!Object} tree Hierarchical tree of block types.
 * @private
 */
Blockly.Toolbox.populateOptions_ = function(tree) {
  // Create an option for each category.
  var options = [];
  for (var cat in tree) {
    var option = {};
    option.text =
        window.decodeURI(cat.substring(Blockly.Toolbox.PREFIX_.length));
    option.cat = cat;
    options.push(option);
  }
  var option = {};
  if (Blockly.Language.variables_get && Blockly.Language.variables_set) {
    // Variables have a special category that is dynamic.
    options.push({text: Blockly.MSG_VARIABLE_CATEGORY,
                  cat: Blockly.Toolbox.VARIABLE_CAT});
  }

  function callbackFactory(cat, element) {
    return function(e) {
      var oldSelectedOption = Blockly.Toolbox.selectedOption_;
      Blockly.hideChaff();
      if (oldSelectedOption != element) {
        Blockly.Toolbox.selectOption_(cat, element);
      }
      // This mouse click has been handled, don't bubble up to document.
      e.stopPropagation();
    };
  }

  // Erase all existing options.
  Blockly.removeChildren_(Blockly.Toolbox.svgOptions_);

  var TOP_MARGIN = 4;
  var maxWidth = 0;
  var resizeList = [Blockly.Toolbox.svgBackground_];
  for (var x = 0, option; option = options[x]; x++) {
    var gElement = Blockly.ContextMenu.optionToDom(option.text);
    var rectElement = gElement.firstChild;
    var textElement = gElement.lastChild;
    Blockly.Toolbox.svgOptions_.appendChild(gElement);

    gElement.setAttribute('transform', 'translate(0, ' +
        (x * Blockly.ContextMenu.Y_HEIGHT + TOP_MARGIN) + ')');
    Blockly.bindEvent_(gElement, 'mousedown', null,
                       callbackFactory(option.cat, gElement));
    resizeList.push(rectElement);
    // Compute the length of the longest text length.
    maxWidth = Math.max(maxWidth, textElement.getComputedTextLength());
  }
  // Run a second pass to resize all options to the required width.
  maxWidth += Blockly.ContextMenu.X_PADDING * 2;
  for (var x = 0; x < resizeList.length; x++) {
    resizeList[x].setAttribute('width', maxWidth);
  }
  if (Blockly.RTL) {
    // Right-align the text.
    for (var x = 0, gElement;
         gElement = Blockly.Toolbox.svgOptions_.childNodes[x]; x++) {
      var textElement = gElement.lastChild;
      textElement.setAttribute('x', maxWidth -
          textElement.getComputedTextLength() - Blockly.ContextMenu.X_PADDING);
    }
  }
  Blockly.Toolbox.width = maxWidth;

  // Right-click on empty areas of the toolbox does not generate a context menu.
  Blockly.bindEvent_(Blockly.Toolbox.svgGroup_, 'mousedown', null,
      function(e) {
        if (e.button == 2) {
          Blockly.hideChaff(true);
          e.stopPropagation();
        }
      });

  // Fire a resize event since the toolbox may have changed width and height.
  Blockly.fireUiEvent(Blockly.svgDoc, window, 'resize');
};

/**
 * Highlight the specified option.
 * @param {?string} cat The category name of the newly specified option,
 *     or null to select nothing.
 * @param {Element} newSelectedOption The SVG group for the selected option,
 *     or null to select nothing.
 * @private
 */
Blockly.Toolbox.selectOption_ = function(cat, newSelectedOption) {
  Blockly.Toolbox.selectedOption_ = newSelectedOption;
  if (newSelectedOption) {
    Blockly.addClass_(newSelectedOption, 'blocklyMenuSelected');
    var blockSet = Blockly.Toolbox.languageTree[cat] || cat;
    Blockly.Toolbox.flyout_.show(blockSet);
  }
};

/**
 * Unhighlight any previously specified option.  Hide the flyout.
 */
Blockly.Toolbox.clearSelection = function() {
  var oldSelectedOption = Blockly.Toolbox.selectedOption_;
  if (oldSelectedOption) {
    Blockly.removeClass_(oldSelectedOption, 'blocklyMenuSelected');
    Blockly.Toolbox.flyout_.hide();
    Blockly.Toolbox.selectedOption_ = null;
  }
};
