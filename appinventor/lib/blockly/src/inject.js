/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
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
 * @fileoverview Functions for injecting Blockly into a web page.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Initialize the SVG document with various handlers.
 * @param {!Element} container Containing element.
 * @param {Object} opt_options Optional dictionary of options.
 */
Blockly.inject = function(container, opt_options) {
  if (opt_options) {
    Blockly.parseOptions_(opt_options);
  }
  Blockly.createDom_(container);
  Blockly.init_();
};

/**
 * Configure Blockly to behave according to a set of options.
 * @param {!Object} options Dictionary of options.
 * @private
 */
Blockly.parseOptions_ = function(options) {
  Blockly.RTL = !!options['rtl'];
  Blockly.editable = !options['readOnly'];
  Blockly.pathToBlockly = options['path'] || './';
};

/**
 * Create the SVG image.
 * @param {!Element} container Containing element.
 * @private
 */
Blockly.createDom_ = function(container) {
  // Find the document for the container.
  var doc = container;
  while (doc.parentNode) {
    doc = doc.parentNode;
  }
  Blockly.svgDoc = doc;

  // Load CSS.
  //<link href="blockly.css" rel="stylesheet" type="text/css" />
  var link = doc.createElement('link');
  link.setAttribute('href', Blockly.pathToBlockly + 'blockly.css');
  link.setAttribute('rel', 'stylesheet');
  link.setAttribute('type', 'text/css');
  link.setAttribute('onload', 'Blockly.cssLoaded()');
  var head = doc.head || doc.getElementsByTagName('head')[0];
  if (!head) {
    throw 'No head in document.';
  }
  head.appendChild(link);

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
    <!--
      Blocks are highlighted from a light source at the top-left.
      In RTL languages we wish to keep this top-left light source.
    -->
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
  filter = Blockly.createSvgElement('filter', {id: 'blocklyEmboss'}, defs);
  Blockly.createSvgElement('feGaussianBlur',
      {'in': 'SourceAlpha', stdDeviation: 1, result: 'blur'}, filter);
  feSpecularLighting = Blockly.createSvgElement('feSpecularLighting',
      {'in': 'blur', surfaceScale: 1, specularConstant: 0.5,
      specularExponent: 10, 'lighting-color': 'white', result: 'specOut'},
      filter);
  Blockly.createSvgElement('fePointLight',
      {x: -5000, y: -10000, z: 20000}, feSpecularLighting);
  Blockly.createSvgElement('feComposite',
      {'in': 'specOut', in2: 'SourceAlpha', operator: 'in', result: 'specOut'},
      filter);
  Blockly.createSvgElement('feComposite',
      {'in': 'SourceGraphic', in2: 'specOut', operator: 'arithmetic',
      k1: 0, k2: 1, k3: 1, k4: 0}, filter);
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
      {id: 'blocklyTrashcanShadowFilter'}, defs);
  Blockly.createSvgElement('feGaussianBlur',
      {'in': 'SourceAlpha', stdDeviation: 2, result: 'blur'}, filter);
  Blockly.createSvgElement('feOffset',
      {'in': 'blur', dx: 1, dy: 1, result: 'offsetBlur'}, filter);
  feMerge = Blockly.createSvgElement('feMerge', {}, filter);
  Blockly.createSvgElement('feMergeNode', {'in': 'offsetBlur'}, feMerge);
  Blockly.createSvgElement('feMergeNode', {'in': 'SourceGraphic'}, feMerge);
  /*
    <filter id="blocklyShadowFilter">
      <feGaussianBlur stdDeviation="2"/>
    </filter>
  */
  filter = Blockly.createSvgElement('filter',
      {id: 'blocklyShadowFilter'}, defs);
  Blockly.createSvgElement('feGaussianBlur', {stdDeviation: 2}, filter);
  /*
    <filter id="blocklyGrayscale">
      <feColorMatrix type="saturate" values="0"/>
    </filter>
  */
  filter = Blockly.createSvgElement('filter',
      {id: 'blocklyGrayscale'}, defs);
  // Grey out (.3) and lighten (.2) the bubble.
  Blockly.createSvgElement('feColorMatrix',
      {type: 'matrix', values:
      '.3 .3 .3 0 .2 ' +
      '.3 .3 .3 0 .2 ' +
      '.3 .3 .3 0 .2 ' +
      '0 0 0 1 0'}, filter);
  /*
    <pattern id="blocklyDisabledPattern" patternUnits="userSpaceOnUse"
             width="10" height="10">
      <rect width="10" height="10" fill="#aaa" />
      <path d="M 0 0 L 10 10 M 10 0 L 0 10" stroke="#cc0" />
    </pattern>
  */
  pattern = Blockly.createSvgElement('pattern',
      {id: 'blocklyDisabledPattern', patternUnits: 'userSpaceOnUse',
       width: 10, height: 10}, defs);
  Blockly.createSvgElement('rect',
      {width: 10, height: 10, fill: '#aaa'}, pattern);
  Blockly.createSvgElement('path',
      {d: 'M 0 0 L 10 10 M 10 0 L 0 10', stroke: '#cc0'}, pattern);

  Blockly.mainWorkspace = new Blockly.Workspace(Blockly.editable);
  svg.appendChild(Blockly.mainWorkspace.createDom());
  if (Blockly.Toolbox && Blockly.editable) {
    svg.appendChild(Blockly.Toolbox.createDom());
  }
  Blockly.Tooltip && svg.appendChild(Blockly.Tooltip.createDom());
  if (Blockly.editable) {
    svg.appendChild(Blockly.FieldDropdown.createDom());
  }
  if (Blockly.ContextMenu) {
    svg.appendChild(Blockly.ContextMenu.createDom());
  }

  // The SVG is now fully assembled.  Add it to the container.
  container.appendChild(svg);
  Blockly.svg = svg;
  Blockly.svgResize();
};


/**
 * Initialize Blockly with various handlers.
 * @private
 */
Blockly.init_ = function() {
  var doc = Blockly.svgDoc;

  Blockly.bindEvent_(window, 'resize', doc, Blockly.svgResize);
  // Bind events for scrolling the workspace.
  // Most of these events should be bound to the SVG's surface.
  // However, 'mouseup' has to be on the whole document so that a block dragged
  // out of bounds and released will know that it has been released.
  // Also, 'keydown' has to be on the whole document since the browser doesn't
  // understand a concept of focus on the SVG image.
  Blockly.bindEvent_(Blockly.svg, 'mousedown', null, Blockly.onMouseDown_);
  Blockly.bindEvent_(doc, 'mouseup', null, Blockly.onMouseUp_);
  Blockly.bindEvent_(Blockly.svg, 'mousemove', null, Blockly.onMouseMove_);
  Blockly.bindEvent_(Blockly.svg, 'contextmenu', null, Blockly.onContextMenu_);
  Blockly.bindEvent_(doc, 'keydown', null, Blockly.onKeyDown_);

  if (Blockly.editable) {
    Blockly.Toolbox && Blockly.Toolbox.init();
  }

  Blockly.mainWorkspace.addTrashcan(Blockly.getMainWorkspaceMetrics);
  Blockly.mainWorkspace.scrollbar = new Blockly.ScrollbarPair(
      Blockly.mainWorkspace.getBubbleCanvas(),
      Blockly.getMainWorkspaceMetrics, this.setMainWorkspaceMetrics);

  // Load the sounds.
  Blockly.loadAudio_('click');
  Blockly.loadAudio_('delete');
};
