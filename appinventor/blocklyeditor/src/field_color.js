/* -*- mode: javascript; js-indent-level 2; -*- */
/**
 * @license
 * Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
 */

/**
 * @fileoverview FieldColor with alpha support, extending FieldColour
 * @author Patryk_F
 */

"use strict";

goog.provide("AI.Blockly.FieldColor");

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
    try {
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
        swatches: {
          "#00000000": "None",
          "#000000FF": "Black",
          "#0000FFFF": "Blue",
          "#00FFFFFF": "Cyan",
          "#444444FF": "Dark Gray",
          "#888888FF": "Gray",
          "#00FF00FF": "Green",
          "#CCCCCCFF": "Light Gray",
          "#FF00FFFF": "Magenta",
          "#FFC800FF": "Orange",
          "#FFAFAFFF": "Pink",
          "#FF0000FF": "Red",
          "#FFFFFFFF": "White",
          "#FFFF00FF": "Yellow",
        },
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

      if (this.pickr_) {
        this.setupPickrEvents_();
        const hideProjectColorsStyle = document.createElement("style");
        hideProjectColorsStyle.textContent = `
          .colorPickerHeading,
          .colorPickerHeading2,
          .pcr-swatches.project-colors {
            display: none !important;
          }

          .pcr-app .pcr-swatches .pcr-swatch,
          .pcr-app .pcr-swatches .pcr-swatch::before,
          .pcr-app .pcr-swatches .pcr-swatch::after,
          .pcr-app .swatch-container button,
          .pcr-app .swatch-container button::before,
          .pcr-app .swatch-container button::after {
            border-radius: 0 !important;
            width: 35px !important;
            height: 15px !important;
            min-width: 0 !important;
            min-height: 0 !important;
            max-width: none !important;
            max-height: none !important;
            box-sizing: border-box !important;
          }
        `;
        document.head.appendChild(hideProjectColorsStyle);

        setTimeout(() => {
          try {
            this.pickr_.show();
          } catch (e) {
            console.error("Pickr show failed", e);
            this.disposePickr_();
          }
        }, 50);
      }
    } catch (e) {
      console.error("Pickr.create failed", e);
      this.disposePickr_();
    }
  }

  createPickrTrigger_() {
    const trigger = document.createElement("div");
    trigger.style.position = "absolute";
    trigger.style.width = "1px";
    trigger.style.height = "1px";
    trigger.style.opacity = "0";
    trigger.style.pointerEvents = "auto";
    trigger.className = "field-test-color-trigger";

    const fieldRect = this.getClickTarget_().getBoundingClientRect();
    trigger.style.left = fieldRect.left + "px";
    trigger.style.top = fieldRect.top + "px";

    document.body.appendChild(trigger);
    this.widget_ = trigger;
  }

  setupPickrEvents_() {
    if (!this.pickr_) return;

    this.pickr_.on("save", (color) => {
      if (color) {
        const rgba = color.toRGBA();
        const r = Math.round(rgba[0])
          .toString(16)
          .padStart(2, "0")
          .toUpperCase();
        const g = Math.round(rgba[1])
          .toString(16)
          .padStart(2, "0")
          .toUpperCase();
        const b = Math.round(rgba[2])
          .toString(16)
          .padStart(2, "0")
          .toUpperCase();
        const a = Math.round(rgba[3] * 255)
          .toString(16)
          .padStart(2, "0")
          .toUpperCase();
        const hexa = `#${r}${g}${b}${a}`;
        this.setValue(hexa);
        this.forceRerender();
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
