Blockly.FieldTextBlockInput = function(text, opt_changeHandler) {
 // Call parent's constructor.
  Blockly.FieldTextInput.call(this, text);
  this.changeHandler_ = opt_changeHandler;
};

// FieldTextBlockInput is a subclass of FieldTextInput.
goog.inherits(Blockly.FieldTextBlockInput, Blockly.FieldTextInput);

/**
 * Set the text in this field.
 * @param {string} text New text.
 */
Blockly.FieldTextBlockInput.prototype.setText = function(text) {
  if (this.changeHandler_) {
    var validated = this.changeHandler_(text);
    // If the new text is invalid, validation returns null.
    // In this case we still want to display the illegal result.
    if (validated !== null && validated !== undefined) {
      text = validated;
    }
  }
  if(this.sourceBlock_ && this.sourceBlock_.outputConnection){
    this.sourceBlock_.outputConnection.setCheck(this.sourceBlock_.outputConnection.check_)
  }
  Blockly.Field.prototype.setText.call(this, text);
};