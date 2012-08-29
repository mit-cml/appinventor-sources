// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Methods for creating the different kinds of blocks for App Inventor 
 * components: events, methods, property getters and setters, component
 * 
 * TODO(sharon): add support for generic component blocks (per component type)
 * 
 * @author sharon@google.com (Sharon Perl)
 */

Blockly.ComponentBlock = {};

/* 
 * All component blocks have category=='Component'. In addition to the standard blocks fields,
 * All regular component blocks have a field instanceName whose value is the name of their 
 * component. For example, the blocks representing a Button1.Click event has 
 * instanceName=='Button1'. All generic component blocks have a field typeName whose value is 
 * the name of their component type.
 */ 

/**
 * Block Colors Hues (See blockly.js for Saturation and Value - fixed for 
 * all blocks)
 */
Blockly.ComponentBlock.COLOUR_EVENT = 31;
Blockly.ComponentBlock.COLOUR_METHOD = 270;
Blockly.ComponentBlock.COLOUR_GETSET = 151;
Blockly.ComponentBlock.COLOUR_COMPONENT = 180;

// TODO(sharon): add tooltips, I18N for labels

/**
 * Create an event block of the given type for a component with the given 
 * instance name. eventType is one of the "events" objects in a typeJsonString
 * passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.event = function(eventType, instanceName) {
  this.category = 'Component',
  this.helpUrl = 'http://foo', // TODO: fix
  this.instanceName = instanceName;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_EVENT);
    this.appendTitle('when');
    this.appendTitle(instanceName + '.' + eventType.name); 
    // TODO: implement event parameters
    /*
    for (var i = 0, param; param = eventType.params[i]; i++) {
      this.appendInput('', Blockly.LOCAL_VARIABLE, this.paramName(i)).setText(param.name); 
    }
    */
    this.appendInput('do', Blockly.NEXT_STATEMENT, "DO");
    this.setPreviousStatement(false);
    this.setNextStatement(false);
  };
  this.getVars = function() {
    var varList = [];
    for (var i = 0, input; input = this.getInputVariable(this.paramName(i)); i++) {
      varList.push(input);
    }
    return varList;
  };
  this.renameVar = function(oldName, newName) {
    for (var i = 0, param = this.paramName(i), input 
         ; input = this.getInputVariable(param)
         ; i++, param = this.paramName(i)) {
      if (Blockly.Names.equals(oldName, input)) {
        this.setInputVariable(param, newName);
      }
    }
  };
  this.paramName = function(i) {
    return "PARAM" + i;
  };
};

/**
 * Create a method block of the given type for a component with the given instance name. methodType
 * is one of the "methods" objects in a typeJsonString passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.method = function(methodType, instanceName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  // Note: the params and paramTypes arrays are initialized for the language block,
  // and these will be shared with each instance of this block type in the workspace.
  this.params = [];
  this.paramTypes = [];
  for (var i = 0, param; param = methodType.params[i]; i++) {
    this.params.push(param.name);
    this.paramTypes.push(param.type);
  }
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);
    this.appendTitle('call');
    this.appendTitle(instanceName + '.' + methodType.name);
    for (var i = 0, param; param = methodType.params[i]; i++) {
      this.appendInput(param.name, Blockly.INPUT_VALUE, "ARG" + i);
    }
    if (methodType.returnType) {
      this.setOutput(true);
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }
  };
}

/**
 * Create a generic method block of the given type for a component type with the given name. 
 * methodType is one of the "methods" objects in a typeJsonString passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.genericMethod = function(methodType, typeName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.typeName = typeName;
  // Note: the params and paramTypes arrays are initialized for the language block,
  // and these will be shared with each instance of this block type in the workspace.
  this.params = [];
  this.paramTypes = [];
  for (var i = 0, param; param = methodType.params[i]; i++) {
    this.params.push(param.name);
    this.paramTypes.push(param.type);
  }
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);
    this.appendTitle('call');
    this.appendTitle(typeName + '.' + methodType.name);
    this.appendInput('component', Blockly.INPUT_VALUE, "COMPONENT");
    for (var i = 0, param; param = methodType.params[i]; i++) {
      this.appendInput(param.name, Blockly.INPUT_VALUE, "ARG" + i);
    }
    if (methodType.returnType) {
      this.setOutput(true);
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }
  };
}

/**
 * Create a property getter block for a component with the given 
 * instance name. propNames is the list of property names to appear in the 
 * dropdown menu for the getter block.
 */
Blockly.ComponentBlock.getter = function(propNames, propTypes, instanceName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.propTypes = propTypes;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    this.appendTitle(instanceName + '.');
    var dropdown = new Blockly.FieldDropdown(function() {
      return propNames;
    });
    this.appendTitle(dropdown, "PROP");
    this.setOutput(true);
  };
};

/**
 * Create a generic property getter block for a component with the given 
 * type name. propNames is the list of property names to appear in the 
 * dropdown menu for the getter block.
 */
Blockly.ComponentBlock.genericGetter = function(propNames, propTypes, typeName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.typeName = typeName;
  this.propTypes = propTypes;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    this.appendTitle(typeName + '.');
    var dropdown = new Blockly.FieldDropdown(function() {
      return propNames;
    });
    this.appendTitle(dropdown, "PROP");
    this.appendInput('of component', Blockly.INPUT_VALUE, "COMPONENT");
    this.setOutput(true);
  };
};

/**
 * Create a property setter block for a component with the given 
 * instance name. propNames is the list of property names to appear in the 
 * dropdown menu for the setter block.
 * 
 * TODO(hal): implement the type constraints on the sockets
 */
Blockly.ComponentBlock.setter = function(propNames, propTypes, instanceName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.propTypes = propTypes;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    this.appendTitle('set');
    this.appendTitle(instanceName + '.');
    var dropdown = new Blockly.FieldDropdown(function() {
      return propNames;
    });
    this.appendTitle(dropdown, "PROP");
    // this.appendTitle('to');
    this.appendInput('to', Blockly.INPUT_VALUE, "VALUE");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  };
};

/**
 * Create a generic property setter block for a component with the given 
 * type name. propNames is the list of property names to appear in the 
 * dropdown menu for the setter block.
 */
Blockly.ComponentBlock.genericSetter = function(propNames, propTypes, typeName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.typeName = typeName;
  this.propTypes = propTypes;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    this.appendTitle('set');
    this.appendTitle(typeName + '.');
    var dropdown = new Blockly.FieldDropdown(function() {
      return propNames;
    });
    this.appendTitle(dropdown, "PROP");
    this.appendInput('for component', Blockly.INPUT_VALUE, "COMPONENT");
    // this.appendTitle('to');
    this.appendInput('to', Blockly.INPUT_VALUE, "VALUE");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  };
};

/**
 * Create a component (object) block for a component with the given 
 * instance name. 
 */
Blockly.ComponentBlock.component = function(instanceName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_COMPONENT);
    this.appendTitle(instanceName);
    this.setOutput(true);
  };
};

