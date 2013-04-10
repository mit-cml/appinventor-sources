/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
 * http://blockly.googlecode.com/
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
 * @fileoverview Functions for injecting Blockly into a web page.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

goog.provide('Blockly.inject');

goog.require('Blockly.Css');
goog.require('goog.dom');


/**
 * Initialize the SVG document with various handlers.
 * @param {!Element} container Containing element.
 * @param {Object} opt_options Optional dictionary of options.
 */
Blockly.inject = function(container, opt_options) {
  // Verify that the container is in document.
  if (!goog.dom.contains(document, container)) {
    throw 'Error: container is not in current document.';
  }
  if (opt_options) {
    // TODO(scr): don't mix this in to global variables.
    goog.mixin(Blockly, Blockly.parseOptions_(opt_options));
  }
  Blockly.createDom_(container);
  Blockly.init_();
};

/**
 * Configure Blockly to behave according to a set of options.
 * @param {!Object} options Dictionary of options.
 * @return {Object} Parsed options.
 * @private
 */
Blockly.parseOptions_ = function(options) {
  var editable = !options['readOnly'];
  if (editable) {
    var tree = options['toolbox'] || '<xml />';
    if (typeof tree == 'string') {
      tree = Blockly.Xml.textToDom(tree);
    }
    var hasCategories = !!tree.getElementsByTagName('category').length;
    var hasTrashcan = options['trashcan'];
    if (hasTrashcan === undefined) {
      hasTrashcan = hasCategories;
    }
  } else {
    var hasCategories = false;
    var hasTrashcan = false;
    var tree = null;
  }
  return {
    RTL: !!options['rtl'],
    editable: editable,
    maxBlocks: options['maxBlocks'] || Infinity,
    pathToBlockly: options['path'] || './',
    Toolbox: hasCategories ? Blockly.Toolbox : undefined,
    Trashcan: hasTrashcan ? Blockly.Trashcan : undefined,
    languageTree: tree
  };
};

/**
 * Create the SVG image.
 * @param {!Element} container Containing element.
 * @private
 */
Blockly.createDom_ = function(container) {
  // Sadly browsers (Chrome vs Firefox) are currently inconsistent in laying
  // out content in RTL mode.  Therefore Blockly forces the use of LTR,
  // then manually positions content in RTL as needed.
  container.setAttribute('dir', 'LTR');
  // Closure can be trusted to create HTML widgets with the proper direction.
  goog.ui.Component.setDefaultRightToLeft(Blockly.RTL);

  // Load CSS.
  Blockly.Css.inject();

  // Build the SVG DOM.
  /*
  <svg
    xmlns="http://www.w3.org/2000/svg"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    version="1.1"
    class="blocklySvg">
    ...
  </svg>
  */
  var svg = Blockly.createSvgElement('svg', {
    'xmlns': 'http://www.w3.org/2000/svg',
    'xmlns:html': 'http://www.w3.org/1999/xhtml',
    'xmlns:xlink': 'http://www.w3.org/1999/xlink',
    'version': '1.1',
    'class': 'blocklySvg'
  }, null);
  /*
  <defs>
    ... filters go here ...
  </defs>
  */
  var defs = Blockly.createSvgElement('defs', {}, svg);
  var filter, feSpecularLighting, feMerge, pattern;
  /*
    <filter id="blocklyEmboss">
      <feGaussianBlur in="SourceAlpha" stdDeviation="1" result="blur"/>
      <feSpecularLighting in="blur" surfaceScale="1" specularConstant="0.5"
                          specularExponent="10" lighting-color="white"
                          result="specOut">
        <fePointLight x="-5000" y="-10000" z="20000"/>
      </feSpecularLighting>
      <feComposite in="specOut" in2="SourceAlpha" operator="in"
                   result="specOut"/>
      <feComposite in="SourceGraphic" in2="specOut" operator="arithmetic"
                   k1="0" k2="1" k3="1" k4="0"/>
    </filter>
  */
  filter = Blockly.createSvgElement('filter', {'id': 'blocklyEmboss'}, defs);
  Blockly.createSvgElement('feGaussianBlur',
      {'in': 'SourceAlpha', 'stdDeviation': 1, 'result': 'blur'}, filter);
  feSpecularLighting = Blockly.createSvgElement('feSpecularLighting',
      {'in': 'blur', 'surfaceScale': 1, 'specularConstant': 0.5,
      'specularExponent': 10, 'lighting-color': 'white', 'result': 'specOut'},
      filter);
  Blockly.createSvgElement('fePointLight',
      {'x': -5000, 'y': -10000, 'z': 20000}, feSpecularLighting);
  Blockly.createSvgElement('feComposite',
      {'in': 'specOut', 'in2': 'SourceAlpha', 'operator': 'in',
      'result': 'specOut'}, filter);
  Blockly.createSvgElement('feComposite',
      {'in': 'SourceGraphic', 'in2': 'specOut', 'operator': 'arithmetic',
      'k1': 0, 'k2': 1, 'k3': 1, 'k4': 0}, filter);
  /*
    <filter id="blocklyTrashcanShadowFilter">
      <feGaussianBlur in="SourceAlpha" stdDeviation="2" result="blur"/>
      <feOffset in="blur" dx="1" dy="1" result="offsetBlur"/>
      <feMerge>
        <feMergeNode in="offsetBlur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
  */
  filter = Blockly.createSvgElement('filter',
      {'id': 'blocklyTrashcanShadowFilter'}, defs);
  Blockly.createSvgElement('feGaussianBlur',
      {'in': 'SourceAlpha', 'stdDeviation': 2, 'result': 'blur'}, filter);
  Blockly.createSvgElement('feOffset',
      {'in': 'blur', 'dx': 1, 'dy': 1, 'result': 'offsetBlur'}, filter);
  feMerge = Blockly.createSvgElement('feMerge', {}, filter);
  Blockly.createSvgElement('feMergeNode', {'in': 'offsetBlur'}, feMerge);
  Blockly.createSvgElement('feMergeNode', {'in': 'SourceGraphic'}, feMerge);
  /*
    <filter id="blocklyShadowFilter">
      <feGaussianBlur stdDeviation="2"/>
    </filter>
  */
  filter = Blockly.createSvgElement('filter',
      {'id': 'blocklyShadowFilter'}, defs);
  Blockly.createSvgElement('feGaussianBlur', {'stdDeviation': 2}, filter);
  /*
    <pattern id="blocklyDisabledPattern" patternUnits="userSpaceOnUse"
             width="10" height="10">
      <rect width="10" height="10" fill="#aaa" />
      <path d="M 0 0 L 10 10 M 10 0 L 0 10" stroke="#cc0" />
    </pattern>
  */
  pattern = Blockly.createSvgElement('pattern',
      {'id': 'blocklyDisabledPattern', 'patternUnits': 'userSpaceOnUse',
       'width': 10, 'height': 10}, defs);
  Blockly.createSvgElement('rect',
      {'width': 10, 'height': 10, 'fill': '#aaa'}, pattern);
  Blockly.createSvgElement('path',
      {'d': 'M 0 0 L 10 10 M 10 0 L 0 10', 'stroke': '#cc0'}, pattern);
  Blockly.mainWorkspace = new Blockly.Workspace(Blockly.editable);
  svg.appendChild(Blockly.mainWorkspace.createDom());
  Blockly.mainWorkspace.maxBlocks = Blockly.maxBlocks;

  if (Blockly.editable) {
    // Determine if there needs to be a category tree, or a simple list of
    // blocks.  This cannot be changed later, since the UI is very different.
    if (Blockly.Toolbox) {
      Blockly.Toolbox.createDom(svg, container);
    } else {
      /**
       * @type {!Blockly.Flyout}
       * @private
       */

       /*
      Blockly.mainWorkspace.flyout_ = new Blockly.Flyout();
      var flyout = Blockly.mainWorkspace.flyout_;
      var flyoutSvg = flyout.createDom();
      flyout.init(Blockly.mainWorkspace,
          Blockly.getMainWorkspaceMetrics, true);
      flyout.autoClose = false;
      // Insert the flyout behind the workspace so that blocks appear on top.
      goog.dom.insertSiblingBefore(flyoutSvg, Blockly.mainWorkspace.svgGroup_);
      var workspaceChanged = function() {
        // Delete any block that's sitting on top of the flyout, or off window.
        if (Blockly.Block.dragMode_ == 0) {
          var svgSize = Blockly.svgSize();
          var MARGIN = 20;
          var blocks = Blockly.mainWorkspace.getTopBlocks(false);
          for (var b = 0, block; block = blocks[b]; b++) {
            var xy = block.getRelativeToSurfaceXY();
            var bBox = block.getSvgRoot().getBBox();
            if ((xy.y < MARGIN - bBox.height) ||  // Off the top.
                (Blockly.RTL ?
                 xy.x > svgSize.width - flyout.width_ + MARGIN * 2 :
                 xy.x < flyout.width_ - MARGIN * 2) ||  // Over the flyout.
                (xy.y > svgSize.height - MARGIN) ||  // Off the bottom.
                (Blockly.RTL ? xy.x < MARGIN :
                 xy.x > svgSize.width - MARGIN)  // Off the far edge.
                ) {
              block.dispose(false, true);
            }
          }
        }
      }
      Blockly.bindEvent_(Blockly.mainWorkspace.getCanvas(),
          'blocklyWorkspaceChange', Blockly.mainWorkspace, workspaceChanged);
        */
    }
  } else {
    // Not editable.  Neither of these will be needed.
    delete Blockly.Toolbox;
    delete Blockly.Flyout;
  }

  Blockly.Tooltip && svg.appendChild(Blockly.Tooltip.createDom());
  if (Blockly.editable && Blockly.FieldDropdown) {
    svg.appendChild(Blockly.FieldDropdown.createDom());
  }

  if (Blockly.ContextMenu && Blockly.ContextMenu) {
    svg.appendChild(Blockly.ContextMenu.createDom());
  }

  // The SVG is now fully assembled.  Add it to the container.
  container.appendChild(svg);
  Blockly.svg = svg;
  Blockly.svgResize();

  // Create an HTML container for popup overlays (e.g. editor widgets).
  Blockly.widgetDiv = goog.dom.createDom('div', {
      'class': 'blocklyWidgetDiv'});
  container.appendChild(Blockly.widgetDiv);
};


/**
 * Initialize Blockly with various handlers.
 * @private
 */
Blockly.init_ = function() {
  Blockly.bindEvent_(window, 'resize', document, Blockly.svgResize);
  // Bind events for scrolling the workspace.
  // Most of these events should be bound to the SVG's surface.
  // However, 'mouseup' has to be on the whole document so that a block dragged
  // out of bounds and released will know that it has been released.
  // Also, 'keydown' has to be on the whole document since the browser doesn't
  // understand a concept of focus on the SVG image.
  Blockly.bindEvent_(Blockly.svg, 'mousedown', null, Blockly.onMouseDown_);
  Blockly.bindEvent_(document, 'mouseup', null, Blockly.onMouseUp_);
  Blockly.bindEvent_(Blockly.svg, 'mousemove', null, Blockly.onMouseMove_);
  Blockly.bindEvent_(Blockly.svg, 'contextmenu', null, Blockly.onContextMenu_);
  Blockly.bindEvent_(document, 'keydown', null, Blockly.onKeyDown_);

  var addScrollbars = true;
  if (Blockly.languageTree) {
    if (Blockly.Toolbox) {
      Blockly.Toolbox.init();
    } else if (Blockly.Flyout) {
      // Build a fixed flyout with the root blocks.
      Blockly.mainWorkspace.flyout_.init(Blockly.mainWorkspace,
          Blockly.getMainWorkspaceMetrics, true);
      Blockly.mainWorkspace.flyout_.show(Blockly.languageTree.childNodes);
      addScrollbars = false;
    }
  }
  if (addScrollbars) {
    Blockly.mainWorkspace.scrollbar = new Blockly.ScrollbarPair(
        Blockly.mainWorkspace.getBubbleCanvas(),
        Blockly.getMainWorkspaceMetrics, Blockly.setMainWorkspaceMetrics);
  }

  Blockly.mainWorkspace.addTrashcan(Blockly.getMainWorkspaceMetrics);

  // Load the sounds.
  Blockly.loadAudio_('media/click.wav', 'click');
  Blockly.loadAudio_('media/delete.wav', 'delete');
};
