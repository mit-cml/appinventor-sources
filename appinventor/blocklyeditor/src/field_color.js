/* -*- mode: javascript; js-indent-level 2; -*- */
/**
 * @license
 * Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
 */

/**
 * @fileoverview FieldColor with alpha support, extending FieldColour
 * @author patryk84a
 */

"use strict";

goog.provide("AI.Blockly.FieldColor");

const defaultColors = {
  "#FFFFFF00": Blockly.Msg.LANG_COLOUR_NONE,
  "#000000FF": Blockly.Msg.LANG_COLOUR_BLACK,
  "#0000FFFF": Blockly.Msg.LANG_COLOUR_BLUE,
  "#00FFFFFF": Blockly.Msg.LANG_COLOUR_CYAN,
  "#444444FF": Blockly.Msg.LANG_COLOUR_DARK_GRAY,
  "#00000000": Blockly.Msg.LANG_COLOUR_DEFAULT,
  "#888888FF": Blockly.Msg.LANG_COLOUR_GRAY,
  "#00FF00FF": Blockly.Msg.LANG_COLOUR_GREEN,
  "#CCCCCCFF": Blockly.Msg.LANG_COLOUR_LIGHT_GRAY,
  "#FF00FFFF": Blockly.Msg.LANG_COLOUR_MAGENTA,
  "#FFC800FF": Blockly.Msg.LANG_COLOUR_ORANGE,
  "#FFAFAFFF": Blockly.Msg.LANG_COLOUR_PINK,
  "#FF0000FF": Blockly.Msg.LANG_COLOUR_RED,
  "#FFFFFFFF": Blockly.Msg.LANG_COLOUR_WHITE,
  "#FFFF00FF": Blockly.Msg.LANG_COLOUR_YELLOW,
};

Blockly.FieldColor = class extends Blockly.FieldColour {
  constructor(opt_value, opt_validator) {
    const inputValue = opt_value;
    const rgb = inputValue.substring(0, 7);
    super(rgb, opt_validator);
    this.SERIALIZABLE = true;
    this.alpha_ = inputValue.length === 9 ? inputValue.substring(7, 9) : "ff";
    this.isPickerOpen_ = false;
  }

  getAlpha() {
    return this.alpha_;
  }

  getValue() {
    const rgb = super.getValue();
    return rgb + this.getAlpha();
  }

  setValue(newValue) {
    super.setValue(newValue.substring(0, 7));
    this.alpha_ = newValue.length === 9 ? newValue.substring(7, 9).toLowerCase() : "ff";
  }

  getText() {
    return this.getValue();
  }

  static fromJson(options) {
    return new Blockly.FieldColor(options.value);
  }

  showEditor_() {
    if (this.isPickerOpen_) {
      this.disposePickr_();
      return;
    }
    this.createPickrTrigger_();
    this.isPickerOpen_ = true;
    const currentValue = this.getValue();
    
    this.pickr_ = Pickr.create({
      el: this.widget_,
      useAsButton: true,
      theme: "nano",
      showAlways: false,
      default: currentValue,
      defaultRepresentation: "HEXA",
      position: "bottom-start",
      closeOnScroll: true,
      autoReposition: false,
      swatches: defaultColors,
      swatches2: {...(window.top.projectColors || {})},
      components: {
        preview: true,
        opacity: true,
        hue: true,
        interaction: {
          hex: true,
          rgba: true,
          hsla: false,
          hsva: false,
          cmyk: false,
          input: true,
          clear: false,
          cancel: true,
          save: true,
        },
      },
    });    
    this.pickr_.setColor(currentValue);
    this.setupPickrEvents_();
    this.pickr_.show();
  }

  createPickrTrigger_() {
    const trigger = document.createElement("div");
    trigger.style.position = "absolute";
    trigger.style.width = "1px";
    trigger.style.height = "1px";
    trigger.style.opacity = "0";
    trigger.style.pointerEvents = "auto";
    trigger.className = "field-color-trigger";
    const fieldRect = this.getClickTarget_().getBoundingClientRect();
    trigger.style.left = fieldRect.left + "px";
    trigger.style.top = fieldRect.top + "px";
    document.body.appendChild(trigger);
    this.widget_ = trigger;
  }

  setupPickrEvents_() {
    if (!this.pickr_) {
      return;
    }
    window.top.projectColorDeleted = (color) => {
      if (window.top.projectColors) {
        delete window.top.projectColors[color];
      }
      if (window.top.removeProjectColorBlockly) {
        window.top.removeProjectColorBlockly(color);
      }
    };

    this.pickr_.on("save", (color) => {
      if (color) {
        let hexa = color.toHEXA().toString();
        if (hexa.length === 7) {
          hexa += "FF";
        }
        let finalHexa = hexa;
        if (hexa === "#00000000") {
          const block = this.sourceBlock_;          
          if (block.data) {
            finalHexa = block.data;
          }
        }
        this.setValue(finalHexa);
        this.forceRerender();

        if (
          !(hexa in defaultColors) &&
          window.top.projectColors &&
          !(hexa in window.top.projectColors)
        ) {
          const displayName = hexa.substring(7, 9) + hexa.substring(1, 7);
          this.pickr_.addSwatch2(hexa, "#" + displayName);
          if (window.top.addProjectColorBlockly) {
            window.top.addProjectColorBlockly(hexa);
          }
        }
      }
      this.disposePickr_();
    });

    this.pickr_.on("cancel", () => {
      this.disposePickr_();
    });

    this.pickr_.on("hide", () => {
      this.disposePickr_();
    });

    this.clickOutsideHandler_ = (event) => {
      const pickerElement = document.querySelector(".pcr-app");
      const fieldElement = this.getClickTarget_();
      if (
        pickerElement &&
        !pickerElement.contains(event.target) &&
        fieldElement &&
        !fieldElement.contains(event.target)
      ) {
        this.disposePickr_();
      }
    };

    document.addEventListener("click", this.clickOutsideHandler_, true);
  }

  disposePickr_() {
    if (this.clickOutsideHandler_) {
      document.removeEventListener("click", this.clickOutsideHandler_, true);
      this.clickOutsideHandler_ = null;
    }
    if (this.pickr_) {
      this.pickr_.destroyAndRemove();
      this.pickr_ = null;
    }
    this.isPickerOpen_ = false;

    if (this.widget_) {
      document.body.removeChild(this.widget_);
      this.widget_ = null;
    }
  }
};
