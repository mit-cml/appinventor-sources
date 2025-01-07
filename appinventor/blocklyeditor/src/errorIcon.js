// -*- mode: java; c-basic-offset: 2; -*-
/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * Copyright © 2013-2016 Massachusetts Institute of Technology
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
 * @license
 * @fileoverview Object representing a warning for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author fraser@google.com (Neil Fraser)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.ErrorIcon');

/**
 * Class for an error.
 * @param {!Blockly.Block} block The block associated with this error.
 * @constructor
 */
AI.ErrorIcon = class extends Blockly.icons.Icon {
  constructor(block) {
    super(block);
    this.text_ = {};
    this.textBubble = null;
    this.bubbleSize = new Blockly.utils.Size(160, 80);
  }

  getType() {
    return AI.ErrorIcon.TYPE;
  }

  initView(listener) {
    if (this.svgRoot) {
      return;
    }

    super.initView(listener);
    /* Here's the markup that will be generated:
    <g class="blocklyIconGroup">
      <path class="blocklyIconShield" d="..."/>
      <text class="blocklyIconMark" x="8" y="13">!</text>
    </g>
    */
    Blockly.utils.dom.createSvgElement('circle',
      {'class': 'blocklyErrorIconOutline',
        'r': AI.ErrorIcon.ICON_RADIUS,
        'cx': AI.ErrorIcon.ICON_RADIUS,
        'cy': AI.ErrorIcon.ICON_RADIUS},
      this.svgRoot);
    Blockly.utils.dom.createSvgElement('path',
      {'class': 'blocklyErrorIconX',
        'd': 'M 4,4 12,12 8,8 4,12 12,4'},
      // X fills circle vvv
      //'d': 'M 3.1931458,3.1931458 12.756854,12.756854 8,8 3.0931458,12.756854 12.756854,3.0931458'},
      this.svgRoot);
    Blockly.utils.dom.addClass(this.svgRoot, 'blockly-icon-error');
  }

  getSize() {
    return AI.ErrorIcon.SIZE;
  }

  getWeight() {
    return 0;
  }

  isShownWhenCollapsed() {
    return false;
  }

  updateCollapsed() {
    // Do nothing
  }

  isVisible() {
    return !!this.textBubble;
  }

  setBubbleVisible(visible) {
    if (this.isBubbleVisible() === visible) {
      return;
    }
    if (visible) {
      this.textBubble = new Blockly.bubbles.TextBubble(
        this.getText(),
        this.sourceBlock.workspace,
        this.getAnchorLocation(),
        this.getBubbleOwnerRect());
      this.textBubble.setColour(this.sourceBlock.style.colourPrimary);
    } else {
      this.textBubble.dispose();
      this.textBubble = null;
    }
    Blockly.Events.fire(
      new (Blockly.Events.get(Blockly.Events.BUBBLE_OPEN))(
        this.sourceBlock,
        visible,
        'error'
      )
    );
  }

  getAnchorLocation() {
    const size = this.getSize();
    const midIcon = new Blockly.utils.Coordinate(size.width / 2, size.height / 2);
    return Blockly.utils.Coordinate.sum(this.workspaceLocation, midIcon);
  }

  dispose() {
    super.dispose();
    if (this.textBubble) {
      this.textBubble.dispose();
    }
  }

  onLocationChange(blockOrigin) {
    super.onLocationChange(blockOrigin);
    if (this.textBubble) {
      this.textBubble.setAnchorLocation(this.getAnchorLocation());
    }
  }

  isBubbleVisible() {
    return !!this.textBubble;
  }

  onClick() {
    super.onClick();
    this.setBubbleVisible(!this.isBubbleVisible());
  }

  getBubbleOwnerRect() {
    const bbox = this.sourceBlock.getSvgRoot().getBBox();
    return new Blockly.utils.Rect(bbox.y, bbox.y + bbox.height, bbox.x, bbox.x + bbox.width);
  }

  /**
   * Set this error's text.
   *
   * @param {string} text Error text.
   * @param {string} id Error id.
   */
  setText(text, id) {
    if (this.text_[id] === text) {
      return;
    }
    if (text) {
      this.text_[id] = text;
    } else {
      delete this.text_[id];
    }
    if (this.isVisible()) {
      this.setBubbleVisible(false);
      this.setBubbleVisible(true);
    }
  }

  /**
   * Get this error's texts.
   *
   * @return {string} All texts concatenated into one string.
   */
  getText() {
    const allErrors = [];
    for (const id in this.text_) {
      allErrors.push(this.text_[id]);
    }
    return allErrors.join('\n');
  }
};

/**
 * Radius of the warning icon.
 */
AI.ErrorIcon.ICON_RADIUS = 8;

/**
 * Type for the Error icon.
 */
AI.ErrorIcon.TYPE = new Blockly.icons.IconType('error');

/**
 * Size of the Error icon.
 */
AI.ErrorIcon.SIZE = new Blockly.utils.Size(
  AI.ErrorIcon.ICON_RADIUS * 2, AI.ErrorIcon.ICON_RADIUS * 2);

Blockly.icons.registry.register(AI.ErrorIcon.TYPE, AI.ErrorIcon);
