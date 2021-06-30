// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
* Blockly blocks file for app inventor documentation.
* Modify AI component block according to documentation need.
* @author preetvadaliya.ict18@gmail.com (Preet Vadaliya)
*/

// App Inventor blocks color
Blockly.CONTROL_CATEGORY_HUE = "#B18E35";
Blockly.LOGIC_CATEGORY_HUE = "#77AB41"; 
Blockly.MATH_CATEGORY_HUE = "#3F71B5"; 
Blockly.TEXT_CATEGORY_HUE = "#B32D5E";
Blockly.LIST_CATEGORY_HUE = "#49A6D4";
Blockly.COLOR_CATEGORY_HUE = "#7D7D7D";
Blockly.VARIABLE_CATEGORY_HUE = "#D05F2D";
Blockly.PROCEDURE_CATEGORY_HUE = "#7C5385";
Blockly.DICTIONARY_CATEGORY_HUE = "#2D1799";
Blockly.COLOUR_EVENT = "#B18E35";
Blockly.COLOUR_METHOD = "#7C5385";
Blockly.COLOUR_GET = "#439970";
Blockly.COLOUR_SET = "#266643";
Blockly.COLOUR_COMPONENT = "#439970";

// AI component event block
Blockly.Blocks['component_event'] = {
  componentType: "Component",
  eventName: "Event",
  isGeneric: false,
  instanceName: "Component1",
  eventParams: [],
  isHorizontalParameters: true,

  initComponent: function (componentType, eventName, isGeneric, isHorizontalParameters) {
    this.componentType = componentType == "Form" ? "Screen" : componentType;
    this.eventName = eventName;
    this.isGeneric = isGeneric;
    this.instanceName = componentType + '1';
    this.eventParams = BLOCK_PARAMS_INFO[`${componentType}-${eventName}`];
    if (this.isGeneric) {
      this.eventParams = ['component', 'notAlreadyHandled'].concat(BLOCK_PARAMS_INFO[`${this.componentType}-${this.eventName}`]);
    }
    this.isHorizontalParameters = isHorizontalParameters;
    this.getBlock();
  },

  mutationToDom: function () {
    let container = document.createElement('mutation');
    container.setAttribute('component_type', this.componentType);
    container.setAttribute('is_generic', this.isGeneric ? "true" : "false");
    container.setAttribute('event_name', this.eventName);
    if (!this.isGeneric) {
      container.setAttribute('instance_name', this.instanceName);
    }
    if (!this.isHorizontalParameters) {
      container.setAttribute('vertical_parameters', "true");
    }
    return container;
  },

  domToMutation: function (xmlElement) {
    this.componentType = xmlElement.getAttribute('component_type') == "Form" ? "Screen" : xmlElement.getAttribute('component_type');
    this.eventName = xmlElement.getAttribute('event_name');
    this.isGeneric = xmlElement.getAttribute('is_generic') == "true";
    this.isHorizontalParameters = xmlElement.getAttribute('vertical_parameters') !== "true";
    if (!this.isGeneric) {
      this.instanceName = xmlElement.getAttribute('instance_name');
      this.eventParams = BLOCK_PARAMS_INFO[`${this.componentType}-${this.eventName}`];
    } else {
      this.eventParams = ['component', 'notAlreadyHandled'].concat(BLOCK_PARAMS_INFO[`${this.componentType}-${this.eventName}`]);
      delete this.instanceName;
    }
    this.getBlock();
  },

  getBlock: function () {
    this.componentDropDown = new Blockly.FieldDropdown([[this.instanceName, this.instanceName]]);
    this.setColour(Blockly.COLOUR_EVENT);
    if (!this.isGeneric) {
      this.appendDummyInput('WHENTITLE').appendField('when').appendField(this.componentDropDown, 'COMPONENT_SELECTOR').appendField(this.eventName);
    } else {
      this.appendDummyInput('WHENTITLE').appendField('when any ' + this.componentType + "." + this.eventName);
    }
    if (this.isHorizontalParameters) {
      if (this.eventParams.length > 0) {
        let paramsInput = this.appendDummyInput('PARAMETERS').appendField(' ').setAlign(Blockly.ALIGN_LEFT);
        for (let i = 0, param; param = this.eventParams[i]; i++) {
          let field = new Blockly.FieldTextInput(param);
          paramsInput.appendField(field).appendField(' ');
        }
      }
    } else {
      if (this.eventParams.length > 0) {
        for (let i = 0, param; param = this.eventParams[i]; i++) {
          this.appendDummyInput().appendField(new Blockly.FieldTextInput(param)).setAlign(Blockly.ALIGN_RIGHT);
        }
      }
    }
    this.appendStatementInput("DO").setCheck(null).appendField("do");
  }
}

// AI component method block
Blockly.Blocks['component_method'] = {
  componentType: "Component",
  methodName: "Method",
  isGeneric: false,
  instanceName: "Component1",
  methodParams: [],
  isVoid: true,
  timeUnit: "",

  initComponent: function (componentType, methodName, isGeneric) {
    this.componentType = componentType;
    this.methodName = methodName;
    this.isGeneric = isGeneric;
    this.instanceName = componentType + '1';
    this.methodParams = BLOCK_PARAMS_INFO[`${componentType}-${methodName}`]['params'];
    if (this.isGeneric) {
      this.methodParams = ['for component'].concat(BLOCK_PARAMS_INFO[`${this.componentType}-${this.methodName}`]['params']);
    }
    if (!this.isGeneric && this.componentType == "Clock" && this.methodName.startsWith('Add')) {
      this.timeUnit = this.methodName.slice(3);
    }
    this.isVoid = BLOCK_PARAMS_INFO[`${this.componentType}-${this.methodName}`]['isVoid'];
    this.getBlock();
  },

  mutationToDom: function () {
    let container = document.createElement('mutation');
    container.setAttribute('component_type', this.componentType);
    container.setAttribute('method_name', this.methodName);
    let isGenericString = "false";
    if (this.isGeneric) {
      isGenericString = "true";
    }
    container.setAttribute('is_generic', isGenericString);
    if (!this.isGeneric) {
      container.setAttribute('instance_name', this.instanceName);
    }
    if (!this.isGeneric && this.componentType == "Clock" && this.methodName.startsWith('Add')) {
      container.setAttribute('timeUnit', this.methodName.slice(3));
    }
    return container;
  },

  domToMutation: function (xmlElement) {
    this.componentType = xmlElement.getAttribute('component_type');
    this.methodName = xmlElement.getAttribute('method_name');
    this.isGeneric = xmlElement.getAttribute('is_generic') == "true";
    if (!this.isGeneric) {
      this.instanceName = xmlElement.getAttribute('instance_name');
      this.methodParams = BLOCK_PARAMS_INFO[`${this.componentType}-${this.methodName}`]['params'];
    } else {
      this.methodParams = ['for component'].concat(BLOCK_PARAMS_INFO[`${this.componentType}-${this.methodName}`]['params']);
      delete this.instanceName;
    }
    this.isVoid = BLOCK_PARAMS_INFO[`${this.componentType}-${this.methodName}`]['isVoid'];
    if (!this.isGeneric && this.componentType == "Clock" && this.methodName.startsWith('Add')) {
      this.timeUnit = xmlElement.getAttribute('timeUnit');
    }
    this.getBlock();
  },

  getBlock: function () {
    this.componentDropDown = new Blockly.FieldDropdown([[this.instanceName, this.instanceName]]);
    this.setColour(Blockly.COLOUR_METHOD);
    if (this.componentType == "Clock" && this.methodName.startsWith("Add")) {
      if (!this.isGeneric) {
        this.unitDropDown = new Blockly.FieldDropdown([[this.timeUnit, this.timeUnit]]);
        this.appendDummyInput().appendField("call").appendField(this.componentDropDown, "COMPONENT_SELECTOR").appendField("." + "Add").appendField(this.unitDropDown, "TIME_UNIT");
      } else {
        this.appendDummyInput().appendField("call").appendField(this.componentType + "." + this.methodName);
      }
    } else {
      if (!this.isGeneric) {
        this.appendDummyInput().appendField("call").appendField(this.componentDropDown, "COMPONENT_SELECTOR").appendField("." + this.methodName);
      } else {
        this.appendDummyInput().appendField("call").appendField(this.componentType + "." + this.methodName);
      }
    }

    if (this.methodParams.length > 0) {
      for (let i = 0, param; param = this.methodParams[i]; i++) {
        let newInput = this.appendValueInput("ARG" + i).appendField(param);
        newInput.setAlign(Blockly.ALIGN_RIGHT);
      }
    }
    if (this.isVoid) {
      this.setPreviousStatement(true, null);
      this.setNextStatement(true, null);
    } else {
      this.setOutput(true, null);
    }
  }
};

// AI component property set, get block
Blockly.Blocks['component_set_get'] = {
  componentType: "Component",
  setOrGet: "get",
  isGeneric: false,
  propertyName: "PropertyName",
  instanceName: "Component1",

  initComponent: function (componentType, setOrGet, isGeneric, propertyName) {
    this.componentType = componentType;
    this.setOrGet = setOrGet;
    this.isGeneric = isGeneric;
    this.propertyName = propertyName;
    this.instanceName = componentType + '1';
    this.getBlock();
  },

  mutationToDom: function () {
    let container = document.createElement('mutation');
    container.setAttribute('component_type', this.componentType);
    container.setAttribute('set_or_get', this.setOrGet);
    container.setAttribute('property_name', this.propertyName);
    let isGenericString = "false";
    if (this.isGeneric) {
      isGenericString = "true";
    }
    container.setAttribute('is_generic', isGenericString);
    if (!this.isGeneric) {
      container.setAttribute('instance_name', this.instanceName);
    }
    return container;
  },

  domToMutation: function (xmlElement) {
    this.componentType = xmlElement.getAttribute('component_type');
    this.setOrGet = xmlElement.getAttribute('set_or_get');
    this.propertyName = xmlElement.getAttribute('property_name');
    this.isGeneric = xmlElement.getAttribute('is_generic') == "true";
    if (!this.isGeneric) {
      this.instanceName = xmlElement.getAttribute('instance_name');
    } else {
      delete this.instanceName;
    }
    this.getBlock();
  },

  getBlock: function () {
    this.componentDropDown = new Blockly.FieldDropdown([[this.instanceName, this.instanceName]]);
    this.propertyDropDown = new Blockly.FieldDropdown([[this.propertyName, this.propertyName]]);
    if (this.setOrGet == "get") {
      this.setColour(Blockly.COLOUR_GET);
      this.setOutput(true, null);
      if (!this.isGeneric) {
        this.appendDummyInput().appendField(this.componentDropDown, "COMPONENT_SELECTOR").appendField(".").appendField(this.propertyDropDown, "PROP");
      } else {
        this.appendDummyInput().appendField(this.instanceName + ".").appendField(this.propertyDropDown, "PROP");
        this.appendValueInput("COMPONENT").setCheck(null).appendField('of component').setAlign(Blockly.ALIGN_RIGHT);
      }
    }
    if (this.setOrGet == "set") {
      this.setColour(Blockly.COLOUR_SET);
      this.setPreviousStatement(true, null);
      this.setNextStatement(true, null);
      if (!this.isGeneric) {
        this.appendValueInput("VALUE").appendField("set").appendField(this.componentDropDown, "COMPONENT_SELECTOR").appendField(".").appendField(this.propertyDropDown, "PROP").appendField("to");
      } else {
        this.appendDummyInput().appendField("set " + this.componentType + '.').appendField(this.propertyDropDown, "PROP");
        this.appendValueInput("COMPONENT").setCheck(null).appendField('of component').setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("VALUE").appendField('to').setAlign(Blockly.ALIGN_RIGHT);
      }
    }
  }

};

// AI component component block
Blockly.Blocks['component_component_block'] = {
  componentType: "Component",
  instanceName: "Component1",
  initComponent: function (componentType) {
    this.componentType = componentType;
    this.instanceName = componentType + '1';
    this.getBlock();
  },
  mutationToDom: function () {
    let container = document.createElement('mutation');
    container.setAttribute('component_type', this.componentType);
    container.setAttribute('instance_name', this.instanceName);
    return container;
  },
  domToMutation: function (xmlElement) {
    this.componentType = xmlElement.getAttribute('component_type');
    this.instanceName = xmlElement.getAttribute('instance_name');
    this.getBlock();
  },
  getBlock: function () {
    this.setColour(Blockly.COLOUR_COMPONENT);
    this.componentDropDown = new Blockly.FieldDropdown([[this.instanceName, this.instanceName]]);
    this.appendDummyInput().appendField(this.componentDropDown, "COMPONENT_SELECTOR");
    this.setOutput(true, null);
  }
}
