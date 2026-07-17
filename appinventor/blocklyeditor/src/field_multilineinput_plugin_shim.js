goog.provide('AI.Blockly.FieldMultilineInput');

/**
 * AI2's multiline text field.
 *
 * Extends the @blockly/field-multilineinput plugin's FieldMultilineInput.
 * 
 * The plugin field only persists the text value across save/reload; it does
 * not remember the width when a user edits it. This subclass adds that on top,
 * so a block doesn't snap back to its auto-sized width every time a project is reopened.
 */
Blockly.FieldMultilineInput = class extends FieldMultilineInput {
  saveState() {
    return this.getValue();
  }

  loadState(state) {
    this.setValue(state);
  }

  toXml(fieldElement) {
    super.toXml(fieldElement);
    if (this.size_.width > 0) {
      fieldElement.setAttribute('width', String(Math.round(this.size_.width)));
    }
    return fieldElement;
  }

  fromXml(fieldElement) {
    super.fromXml(fieldElement);
    const width = Number(fieldElement.getAttribute('width'));
    this.restoredWidth_ = Number.isFinite(width) && width > 0 ? width : null;
  }

  doValueUpdate_(newValue) {
    this.restoredWidth_ = null;
    super.doValueUpdate_(newValue);
  }

  updateSize_() {
    super.updateSize_();
    if (!this.borderRect_ || !this.restoredWidth_ ||
        this.size_.width >= this.restoredWidth_) {
      return;
    }
    this.size_.width = this.restoredWidth_;
    this.borderRect_.setAttribute('width', String(this.restoredWidth_));
    this.positionBorderRect_();
  }
};
