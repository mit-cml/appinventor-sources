// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
* Blockly blocks file for app inventor documentation.
* @author preetvadaliya.ict18@gmail.com (Preet Vadaliya)
*/

// App Inventor blocks color
const COLOR_EVENT = "#B18E35";
const COLOR_METHOD = "#7C5385";
const COLOR_GET = "#439970";
const COLOR_SET = "#266643";
const COLOR_COMPONENT = "#439970";

Blockly.Blocks['component_event'] = {
  initComponent: function (componentInfo) {
    this.componentType = componentInfo[0];
    this.eventName = componentInfo[1];
    this.isGeneric = "false";
    this.instanceName = this.componentType + "1";
    this.params = componentInfo[2][0] == "" ? this.params = [] : componentInfo[2];
  },
  init: function () {
    this.componentDropDown = new Blockly.FieldDropdown([[this.instanceName, this.instanceName]]);
    this.appendDummyInput('WHENTITLE')
      .appendField("when")
      .appendField(this.componentDropDown, "COMPONENT_SELECTOR")
      .appendField(this.eventName);
      if (this.params.length > 0) {
        let paramInput = this.appendDummyInput('PARAMETERS').appendField(" ").setAlign(Blockly.ALIGN_LEFT)
        for (let i = 0, param; param = this.params[i]; i++) {
          let field = new Blockly.FieldTextInput(param);
          paramInput.appendField(field, 'VAR' + i).appendField(" ");
        }
      }
    this.appendStatementInput("DO")
      .setCheck(null)
      .appendField("do");
    this.setColour(COLOR_EVENT);
  },
  mutationToDom: function () {
    let container = document.createElement('mutation');
    container.setAttribute('component_type', this.componentType);
    container.setAttribute('is_generic', this.isGeneric);
    container.setAttribute('event_name', this.eventName);
    container.setAttribute('instance_name', this.instanceName);
    return container;
  }
};

Blockly.Blocks['component_set_get'] = {
  initComponent: function (componentInfo) {
    this.componentType = componentInfo[0];
    this.setOrGet = componentInfo[1];
    this.isGeneric = "false";
    this.propertyName = componentInfo[2];
    this.instanceName = this.componentType + "1";
  },
  init: function () {
    this.componentDropDown = new Blockly.FieldDropdown([[this.instanceName, this.instanceName]]);
    this.propertyDropDown = new Blockly.FieldDropdown([[this.propertyName, this.propertyName]]);
    if (this.setOrGet == "get") {
      this.appendDummyInput()
        .appendField(this.componentDropDown, "COMPONENT_SELECTOR")
        .appendField(".")
        .appendField(this.propertyDropDown, "PROP");
      this.setOutput(true, null);
      this.setColour(COLOR_GET);
    }
    if (this.setOrGet == "set") {
      this.appendValueInput("VALUE")
        .appendField("set")
        .appendField(this.componentDropDown, "COMPONENT_SELECTOR")
        .appendField(".")
        .appendField(this.propertyDropDown, "PROP")
        .appendField("to");
      this.setPreviousStatement(true, null);
      this.setNextStatement(true, null);
      this.setColour(COLOR_SET);
    }
  },
  mutationToDom: function () {
    let container = document.createElement('mutation');
    container.setAttribute('component_type', this.componentType);
    container.setAttribute('is_generic', this.isGeneric);
    container.setAttribute('set_or_get', this.setOrGet);
    container.setAttribute('property_name', this.propertyName);
    container.setAttribute('instance_name', this.instanceName);
    return container;
  }
};

Blockly.Blocks['component_method'] = {
  initComponent: function (componentInfo) {
    this.componentType = componentInfo[0];
    this.methodName = componentInfo[1];
    this.isGeneric = "false";
    this.instanceName = this.componentType + "1";
    this.params = componentInfo[2];
    this.isVoid = componentInfo[3];
  },
  init: function () {
    this.componentDropDown = new Blockly.FieldDropdown([[this.instanceName, this.instanceName]]);
    if (this.componentType == "Clock" && this.methodName.startsWith("Add")) {
      this.unitDropDown = new Blockly.FieldDropdown([[this.methodName.slice(3), this.methodName.slice(3)]]);
      this.appendDummyInput()
        .appendField("call")
        .appendField(this.componentDropDown, "COMPONENT_SELECTOR")
        .appendField("." + "Add")
        .appendField(this.unitDropDown, "TIME_UNIT");
    } else {
      this.appendDummyInput()
        .appendField("call")
        .appendField(this.componentDropDown, "COMPONENT_SELECTOR")
        .appendField("." + this.methodName);
    }
    this.setColour(COLOR_METHOD);
    if (this.isVoid == "undefined") {
      this.setPreviousStatement(true, null);
      this.setNextStatement(true, null);
    } else {
      this.setOutput(true, null);
    }
    if (this.params.length > 0) {
      for (let i = 0, param; param = this.params[i]; i++) {
        let newInput = this.appendValueInput("ARG" + i).appendField(param);
        newInput.setAlign(Blockly.ALIGN_RIGHT);
      }
    }
  },
  mutationToDom: function () {
    let container = document.createElement('mutation');
    container.setAttribute('component_type', this.componentType);
    container.setAttribute('is_generic', this.isGeneric);
    container.setAttribute('method_name', this.methodName);
    container.setAttribute('instance_name', this.instanceName);
    if (this.componentType == "Clock") {
      container.setAttribute('timeunit', this.methodName.slice(3));
    }
    return container;
  }
};

Blockly.Blocks['component_component_block'] = {
  initComponent: function (componentInfo) {
    this.componentType = componentInfo[0];
    this.instanceName = this.componentType + "1";
  },
  init: function () {
    this.componentDropDown = new Blockly.FieldDropdown([[this.instanceName, this.instanceName]]);
    this.appendDummyInput()
      .appendField(this.componentDropDown, "COMPONENT_SELECTOR");
    this.setOutput(true, null);
    this.setColour(COLOR_COMPONENT);
  },
  mutationToDom: function () {
    let container = document.createElement('mutation');
    container.setAttribute('component_type', this.componentType);
    container.setAttribute('instance_name', this.instanceName);
    return container;
  }
}