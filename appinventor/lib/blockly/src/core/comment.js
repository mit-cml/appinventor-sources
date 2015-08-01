/**
 * @license
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
 * https://blockly.googlecode.com/
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
 * @fileoverview Object representing a code comment.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

goog.provide('Blockly.Comment');

goog.require('Blockly.Bubble');
goog.require('Blockly.Icon');
goog.require('goog.userAgent');


/**
 * Class for a comment.
 * @param {!Blockly.Block} block The block associated with this comment.
 * [08/05/14, lyn] Added 2nd param to support multiple text bubbles on blocks.
 * @param {!String} opt_iconChar: A single character for icon.
 * //above added
 * @extends {Blockly.Icon}
 * @constructor
 */
Blockly.Comment = function(block, opt_iconChar) {
  // options added for different comment boxes: Yail, Watch, DoIt, Standard Comment
  this.myblock = block; // added for tracking which block the comment is on
  this.iconChar = opt_iconChar ? opt_iconChar : '?';      //added for tracking which comment on the block we are on
  Blockly.Comment.superClass_.constructor.call(this, block);
  this.createIcon_();
};
goog.inherits(Blockly.Comment, Blockly.Icon);


/**
 * Comment text (if bubble is not visible).
 * @private
 */
Blockly.Comment.prototype.text_ = '';

/**
 * Width of bubble.
 * @private
 */
Blockly.Comment.prototype.width_ = 160;

/**
 * Height of bubble.
 * @private
 */
Blockly.Comment.prototype.height_ = 80;

/**
 * Create the icon on the block.
 * @private
 */
Blockly.Comment.prototype.createIcon_ = function() {
  Blockly.Icon.prototype.createIcon_.call(this);
  /* Here's the markup that will be generated:
  <circle class="blocklyIconShield" r="8" cx="8" cy="8"/>
  <text class="blocklyIconMark" x="8" y="13">?</text>
  */
  var iconShield = Blockly.createSvgElement('circle',
      {'class': 'blocklyIconShield',
       'r': Blockly.Icon.RADIUS,
       'cx': Blockly.Icon.RADIUS,
       'cy': Blockly.Icon.RADIUS}, this.iconGroup_);
  this.iconMark_ = Blockly.createSvgElement('text',
      {'class': 'blocklyIconMark',
       'x': Blockly.Icon.RADIUS,
       'y': 2 * Blockly.Icon.RADIUS - 3}, this.iconGroup_);
  // this.iconMark_.appendChild(document.createTextNode('?'));
  this.iconMark_.appendChild(document.createTextNode(this.iconChar));
};

/**
 * Create the editor for the comment's bubble.
 * @return {!Element} The top-level node of the editor.
 * @private
 */
Blockly.Comment.prototype.createEditor_ = function() {
  /* Create the editor.  Here's the markup that will be generated:
    <foreignObject x="8" y="8" width="164" height="164">
      <body xmlns="http://www.w3.org/1999/xhtml" class="blocklyMinimalBody">
        <textarea xmlns="http://www.w3.org/1999/xhtml"
            class="blocklyCommentTextarea"
            style="height: 164px; width: 164px;"></textarea>
      </body>
    </foreignObject>
  */
  this.foreignObject_ = Blockly.createSvgElement('foreignObject',
      {'x': Blockly.Bubble.BORDER_WIDTH, 'y': Blockly.Bubble.BORDER_WIDTH},
      null);
  var body = document.createElementNS(Blockly.HTML_NS, 'body');
  body.setAttribute('xmlns', Blockly.HTML_NS);
  body.className = 'blocklyMinimalBody';

  //[emery, 06/10/2015] new close button
  this.closeButton_ = document.createElementNS(Blockly.HTML_NS, 'button');
  if (this.iconChar == Blockly.BlocklyEditor.watchChar) {
  this.closeButton_.appendChild(document.createTextNode('Remove Watch'));
} else if (this.iconChar == Blockly.BlocklyEditor.doitChar) {
  this.closeButton_.appendChild(document.createTextNode('Remove Do It'));
} else if (this.iconChar == Blockly.BlocklyEditor.yailChar) {
  this.closeButton_.appendChild(document.createTextNode('Remove Yail'));
} else {
  this.closeButton_.appendChild(document.createTextNode('Remove Comment'));
}
  body.appendChild(this.closeButton_);
  Blockly.bindEvent_(this.closeButton_, 'mouseup', this, this.closeButtonClick_);  //emery

  //[emery, 06/10/2015] new hide button
  this.hideButton_ = document.createElementNS(Blockly.HTML_NS, 'button');
  this.hideButton_.appendChild(document.createTextNode('Hide'));
  body.appendChild(this.hideButton_);
  Blockly.bindEvent_(this.hideButton_, 'mouseup', this, Blockly.Icon.prototype.iconClick_ );

  // [lyn, 08/18/2014] [edited by emery, 06/2015] new clear button
    this.clearButton_ = document.createElementNS(Blockly.HTML_NS, 'button');
    this.clearButton_.appendChild(document.createTextNode('Clear'));
    body.appendChild(this.clearButton_);
    Blockly.bindEvent_(this.clearButton_, 'mouseup', this, this.clearButtonClick_);  //emery

  // [emery, 06/10/2015] new toggle watch button
  if (this.iconChar == Blockly.BlocklyEditor.watchChar) {
    this.toggleButton_ = document.createElementNS(Blockly.HTML_NS, 'button');
    this.toggleButton_.appendChild(document.createTextNode('Turn Watch Off'));
    body.appendChild(this.toggleButton_);
    Blockly.bindEvent_(this.toggleButton_, 'mouseup', this, this.toggleButtonClick_);  //emery

    this.orderButton_ = document.createElementNS(Blockly.HTML_NS, 'button');
    this.orderButton_.appendChild(document.createTextNode('Print From Bottom'));
    body.appendChild(this.orderButton_);
    Blockly.bindEvent_(this.orderButton_, 'mouseup', this, this.orderButtonClick_);
    this.myblock.order = true;
  }

  // [emery, 06/2015[ new Do It Again button
  if (this.iconChar == Blockly.BlocklyEditor.doitChar) {
    this.doitButton_ = document.createElementNS(Blockly.HTML_NS, 'button');
    this.doitButton_.appendChild(document.createTextNode('Do It Again'));
    body.appendChild(this.doitButton_);
    Blockly.bindEvent_(this.doitButton_, 'mouseup', this, this.doitAgainButtonClick_);  //emery
  }

  // [emery, 06/2015[ new Regenerate Yail Button
  if (this.iconChar == Blockly.BlocklyEditor.yailChar) {
    this.yailButton_ = document.createElementNS(Blockly.HTML_NS, 'button');
    this.yailButton_.appendChild(document.createTextNode('Regenerate'));
    body.appendChild(this.yailButton_);
    Blockly.bindEvent_(this.yailButton_, 'mouseup', this, this.yailButtonClick_);
  }

  this.textarea_ = document.createElementNS(Blockly.HTML_NS, 'textarea');
  this.textarea_.className = 'blocklyCommentTextarea';
  this.textarea_.setAttribute('dir', Blockly.RTL ? 'RTL' : 'LTR');
  body.appendChild(this.textarea_);
  this.foreignObject_.appendChild(body);
  Blockly.bindEvent_(this.textarea_, 'mouseup', this, this.textareaFocus_);
  return this.foreignObject_;
};

/*
 * Close Button: Emery 6/9/15
 */
Blockly.Comment.prototype.closeButtonClick_ = function(e) {
  if (this.iconChar == Blockly.BlocklyEditor.watchChar) {
    this.myblock.watch = false;
  }
  if (this.iconChar == Blockly.BlocklyEditor.commentChar) {
    this.myblock.setCommentText(null);
  } else {
  this.myblock.setTextBubbleText(this.iconChar, null);
  }
}


/*
 * Clear Button: Emery 6/9/15
 */
Blockly.Comment.prototype.clearButtonClick_ = function(e) {
  this.setText("");
}

/*
 * Toggle Watch Button: Emery 6/10/15
 */
Blockly.Comment.prototype.toggleButtonClick_ = function(e) {
  if (this.myblock.watch) {
    this.myblock.watch = false;
    var text = this.myblock.getTextBubbleText(Blockly.BlocklyEditor.watchChar)
    if (this.myblock.order) {
      this.myblock.setTextBubbleText(Blockly.BlocklyEditor.watchChar, "------\n" + text);
    } else {
      this.myblock.setTextBubbleText(Blockly.BlocklyEditor.watchChar, text + "\n------");
    }
    this.toggleButton_.innerHTML = "Turn Watch On";
  } else {
    this.toggleButton_.innerHTML = "Turn Watch Off";
    this.myblock.watch = true;
  }
}

/*
 * Order Button: Emery 6/16/15
 */
Blockly.Comment.prototype.orderButtonClick_ = function(e) {
  this.myblock.order = !this.myblock.order;
  var text = this.getText();
  var split = text.split("\n");
  var string = "";
  for (var i = 0; i < split.length; i++) {
    string = split[i] + "\n" + string;
  }
  this.setText(string);
  if (this.orderButton_.innerHTML == "Print From Bottom") {
    this.orderButton_.innerHTML = "Print From Top";
  } else {
    this.orderButton_.innerHTML = "Print From Bottom";
  }
}

/*
 * Do It Again Button: Emery 6/10/15
 */
Blockly.Comment.prototype.doitAgainButtonClick_ = function(e) {
  this.myblock.doit = true;
  var yailText;
  var yailTextOrArray = Blockly.Yail.blockToCode1(this.myblock);
  if (yailTextOrArray instanceof Array) {
    yailText = yailTextOrArray[0];
  } else {
    yailText = yailTextOrArray;
  }
  Blockly.ReplMgr.putYail(yailText, this.myblock);
}

/*
 * Regenerate Yail Button: Emery 6/10/15
 */
Blockly.Comment.prototype.yailButtonClick_ = function(e) {
  var yailText;
  //Blockly.Yail.blockToCode1 returns a string if the block is a statement
  //and an array if the block is a value
  var yailTextOrArray = Blockly.Yail.blockToCode1(this.myblock);
  if (yailTextOrArray instanceof Array) {
    yailText = yailTextOrArray[0];
  } else {
    yailText = yailTextOrArray;
  }
  this.setText(yailText);
}


/**
 * Add or remove editability of the comment.
 * @override
 */
Blockly.Comment.prototype.updateEditable = function() {
  if (this.isVisible()) {
    // Toggling visibility will force a rerendering.
    this.setVisible(false);
    this.setVisible(true);
  }
  // Allow the icon to update.
  Blockly.Icon.prototype.updateEditable.call(this);
};

/**
 * Callback function triggered when the bubble has resized.
 * Resize the text area accordingly.
 * @private
 */
Blockly.Comment.prototype.resizeBubble_ = function() {
  var size = this.bubble_.getBubbleSize();
  var doubleBorderWidth = 2 * Blockly.Bubble.BORDER_WIDTH;
  this.foreignObject_.setAttribute('width', size.width - doubleBorderWidth);
  this.foreignObject_.setAttribute('height', size.height - doubleBorderWidth);
  this.textarea_.style.width = (size.width - doubleBorderWidth - 4) + 'px';
  this.textarea_.style.height = (size.height - doubleBorderWidth - 4) + 'px';
};

/**
 * Show or hide the comment bubble.
 * @param {boolean} visible True if the bubble should be visible.
 */
Blockly.Comment.prototype.setVisible = function(visible) {
  if (visible == this.isVisible()) {
    // No change.
    return;
  }
  if ((!this.block_.isEditable() && !this.textarea_) || goog.userAgent.IE) {
    // Steal the code from warnings to make an uneditable text bubble.
    // MSIE does not support foreignobject; textareas are impossible.
    // http://msdn.microsoft.com/en-us/library/hh834675%28v=vs.85%29.aspx
    // Always treat comments in IE as uneditable.
    Blockly.Warning.prototype.setVisible.call(this, visible);
    return;
  }
  // Save the bubble stats before the visibility switch.
  var text = this.getText();
  var size = this.getBubbleSize();
  if (visible) {
    // Create the bubble.
    this.bubble_ = new Blockly.Bubble(
        /** @type {!Blockly.Workspace} */ (this.block_.workspace),
        this.createEditor_(), this.block_.svg_.svgPath_,
        this.iconX_, this.iconY_,
        this.width_, this.height_);
    this.bubble_.registerResizeEvent(this, this.resizeBubble_);
    this.updateColour();
    this.text_ = null;
  } else {
    // Dispose of the bubble.
    this.bubble_.dispose();
    this.bubble_ = null;
    this.textarea_ = null;
    this.foreignObject_ = null;
  }
  // Restore the bubble stats after the visibility switch.
  this.setText(text);
  this.setBubbleSize(size.width, size.height);
};

/**
 * Bring the comment to the top of the stack when clicked on.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Comment.prototype.textareaFocus_ = function(e) {
  // Ideally this would be hooked to the focus event for the comment.
  // However doing so in Firefox swallows the cursor for unknown reasons.
  // So this is hooked to mouseup instead.  No big deal.
  this.bubble_.promote_();
  // Since the act of moving this node within the DOM causes a loss of focus,
  // we need to reapply the focus.
  this.textarea_.focus();
};

/**
 * Get the dimensions of this comment's bubble.
 * @return {!Object} Object with width and height properties.
 */
Blockly.Comment.prototype.getBubbleSize = function() {
  if (this.isVisible()) {
    return this.bubble_.getBubbleSize();
  } else {
    return {width: this.width_, height: this.height_};
  }
};

/**
 * Size this comment's bubble.
 * @param {number} width Width of the bubble.
 * @param {number} height Height of the bubble.
 */
Blockly.Comment.prototype.setBubbleSize = function(width, height) {
  if (this.textarea_) {
    this.bubble_.setBubbleSize(width, height);
  } else {
    this.width_ = width;
    this.height_ = height;
  }
};

/**
 * Returns this comment's text.
 * @return {string} Comment text.
 */
Blockly.Comment.prototype.getText = function() {
  return this.textarea_ ? this.textarea_.value : this.text_;
};

/**
 * Set this comment's text.
 * @param {string} text Comment text.
 */
Blockly.Comment.prototype.setText = function(text) {
  if (this.textarea_) {
    this.textarea_.value = text;
  } else {
    this.text_ = text;
  }
};

/**
 * Dispose of this comment.
 * [edited by emery, 6/15/15], include textBubble case
 */
Blockly.Comment.prototype.dispose = function() {
  if (this.iconChar == Blockly.BlocklyEditor.commentChar) {
  this.block_.comment = null;
  } else {
    this.block_.textBubbles[this.iconChar] = null;
  }
  Blockly.Icon.prototype.dispose.call(this);
};
