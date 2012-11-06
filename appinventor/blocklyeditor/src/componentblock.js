//Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Methods for creating the different kinds of blocks for App Inventor
 * components: events, methods, property getters and setters, component
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


//TODO(): add I18N

/**
 * Create an event block of the given type for a component with the given
 * instance name. eventType is one of the "events" objects in a typeJsonString
 * passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.event = function(eventType, instanceName) {
  this.category = 'Component',
  this.blockType = 'event',
  this.helpUrl = 'http://foo', // TODO: fix
  this.instanceName = instanceName;
  this.eventType = eventType;

  // Initializes an event BlocklyBlock
  // This is called by the BlocklyBlock constructor where its type field is set to, e.g., 'Button1_Click'
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_EVENT);
    this.appendDummyInput().appendTitle('when ' + this.instanceName + '.' + this.eventType.name);

    // TODO: implement event callback parameters.  Need to figure out how to do procedures and 
    // make callback parameters consistent with that.
    for (var i = 0, param; param = eventType.params[i]; i++) {
      this.appendValueInput(Blockly.INPUT_VALUE, this.paramName(i)).appendTitle(param.name);
    }
    this.appendStatementInput("DO").appendTitle('do');
    Blockly.Language.setTooltip(this, eventType.description);
    this.setPreviousStatement(false);
    this.setNextStatement(false);

    // Renames the block's instanceName and type (set in BlocklyBlock constructor), and revises its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText('when ' + this.instanceName + '.' + this.eventType.name);  // Revise title
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
  };

  this.getVars = function() {
    var varList = [];
    for (var i = 0, input; input = this.getInputVariable(this.paramName(i)); i++) {
      varList.push(input);
    }
    return varList;
  };

  this.getVarString = function() {
    var varString = "";
    for (var i = 0, param; param = this.eventType.params[i]; i++) {
      if(i != 0){
        varString += " ";
      }
      varString += param.name;
    }
    return varString;
  }

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
  this.methodType = methodType;

  if (methodType.returnType) {
    this.blockType = 'methodwithreturn';
  } else {
    this.blockType = 'methodnoreturn';
  }

  // Note: the params and paramTypes arrays are initialized for the language block,
  // and these will be shared with each instance of this block type in the workspace.
  this.params = [];
  paramTypes = [];
  this.yailTypes = [];
  for (var i = 0, param; param = methodType.params[i]; i++) {
    this.params.push(param.name);
    // param.type is a yail type, and so must be converted to a Blockly type
    paramTypes.push(Blockly.Language.YailTypeToBlocklyType(param.type));
    this.yailTypes.push(param.type);
  }
  this.paramTypes = paramTypes;

  // Initializes a method BlocklyBlock
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);
    this.appendDummyInput().appendTitle('call ' + this.instanceName + '.' + this.methodType.name);
    Blockly.Language.setTooltip(this, this.methodType.description);
    for (var i = 0, param; param = this.params[i]; i++) {
      var newInput = this.appendValueInput("ARG" + i).appendTitle(param);
      newInput.connection.setCheck(this.paramTypes[i]);
    }
    // methodType.returnType is a Yail type
    if (this.methodType.returnType) {
      this.setOutput(true, Blockly.Language.YailTypeToBlocklyType(this.methodType.returnType));
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }

    // Rename the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText('call ' + this.instanceName + '.' + this.methodType.name);
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
  };
}

/**
 * Create a generic method block of the given type for a component type with the given name.
 * methodType is one of the "methods" objects in a typeJsonString passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.genericMethod = function(methodType, typeName) {
  this.category = 'Component';
  this.blockType = 'genericmethod',
  this.helpUrl = "http://foo";  // TODO: fix
  this.typeName = typeName;
  this.methodType = methodType;

  // Note: the params and paramTypes arrays are initialized for the language block,
  // and these will be shared with each instance of this block type in the workspace.
  this.params = [];
  paramTypes = [];
  this.yailTypes = [];
  for (var i = 0, param; param = this.methodType.params[i]; i++) {
    this.params.push(param.name);
    // param.type is a yail type, and so must be converted to a Blockly type
    paramTypes.push(Blockly.Language.YailTypeToBlocklyType(param.type));
    this.yailTypes.push(param.type);
  }
  this.paramTypes = paramTypes;

  // Initializes a generic method BlocklyBlock
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);
    this.appendDummyInput().appendTitle('call');
    this.appendDummyInput().appendTitle(this.typeName + '.' + this.methodType.name);

    var compInput = this.appendValueInput("COMPONENT").appendTitle('for component');
    compInput.connection.setCheck(Blockly.Language.YailTypeToBlocklyTypeMap.component);

    for (var i = 0, param; param = this.params[i]; i++) {
      var newInput = this.appendValueInput("ARG" + i).appendTitle(param);
      newInput.connection.setCheck(this.paramTypes[i]);
    }
    // methodType.returnType is a Yail type
    if (this.methodType.returnType) {
      this.setOutput(true, Blockly.Language.YailTypeToBlocklyType(this.methodType.returnType));
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }

    // Renames the block's typeNam, and revises its title
    this.rename = function(oldname, newname) {
      if (this.typeName == oldname) {
        this.typeName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText('call' + this.typeName + '.' + this.methodType.name);
      }
    };
  };
}


/**
 * Create a property getter block for a component with the given
 * instance name. propNames is the list of property names to appear in the
 * dropdown menu for the getter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */

Blockly.ComponentBlock.getter = function(propNames, propYailTypes, propTooltips, instanceName) {
  this.category = 'Component';
  this.blockType = 'getter',
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;

  // Initializes a getter BlocklyBlock
  // Called by the BlocklyBlock constructor where its type field is set to, e.g., 'TextBox1_getproperty'
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    var thisBlock = this;
    this.propYailTypes = propYailTypes;
    var dropdown = new Blockly.FieldDropdown(

        function() {return propNames; },
        // change the output type and tooltip to match the new selection
        function(selection) {this.setText(selection);

        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes);
        // this will disconnect the block if the new outputType doesn't match the
        // socket the block is plugged into
        thisBlock.outputConnection.setCheck(newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    );

    this.appendDummyInput().appendTitle(this.instanceName + '.').appendTitle(dropdown, "PROP");
    // Set the initial output type and tooltip since they won't be set in the dropdown callback
    var newType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes);
    thisBlock.setOutput(true, newType);
    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));

    // Rename the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText(this.instanceName + '.');
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
  };
};

/**
 * Create a generic property getter block for a component with the given
 * type name. propNames is the list of property names to appear in the
 * dropdown menu for the getter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */
Blockly.ComponentBlock.genericGetter = function(propNames, propYailTypes, propTooltips, typeName) {
  this.category = 'Component';
  this.blockType = 'genericgetter',
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = typeName;
  this.propYailTypes = propYailTypes;

  // Initializes a genericGetter BlocklyBlock
  // Its type field is set in the BlocklyBlock constructor
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    //    this.appendDummyInput().appendTitle(this.instanceName + '.');
    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(

        function() {return propNames; },
        // change the output type and tooltip to match the new selection
        function(selection)
        {this.setText(selection);
        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes);
        // this will disconnect the block if the new outputType doesn't match the
        // socket the block is plugged into
        thisBlock.outputConnection.setCheck(newType);
        thisBlock.setOutput(true, newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    ); 

    this.appendDummyInput().appendTitle(this.instanceName + '.').appendTitle(dropdown, "PROP");

    // the argument input type on the COMPONENT socket is COMPONENT
    var compInput = this.appendValueInput("COMPONENT").appendTitle('of component').setAlign(Blockly.ALIGN_RIGHT);
    compInput.connection.setCheck(Blockly.Language.YailTypeToBlocklyTypeMap.component);
    // Set the initial and tooltip type since they won't be set in the dropdown callback
    var newType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes);
    thisBlock.setOutput(true, newType);
    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));

    // Rename the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText(this.instanceName + '.');
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
  };
};


/**
 * Create a property setter block for a component with the given
 * instance name. propNames is the list of property names to appear in the
 * dropdown menu for the setter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */

Blockly.ComponentBlock.setter = function(propNames, propYailTypes, propTooltips, instanceName) {
  this.category = 'Component';
  this.blockType = 'setter',
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.propYailTypes = propYailTypes;
  this.propTooltips = propTooltips;

  // Initializes a setter BlocklyBlock
  // The type field is set in BlocklyBlock constructor to, e.g., Label1_setproperty
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(
        function() {return propNames; },
        function(selection)
        // change the input type to match the new selection
        {this.setText(selection);
        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes);
        // this will set the socket arg type and also disconnect any plugged in block
        // where the type doesn't match the socket type
        thisBlock.getInput("VALUE").connection.setCheck(newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    );

    var initialArgType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes);
    var valueInput = this.appendValueInput("VALUE", initialArgType).
    appendTitle('set ' + this.instanceName + '.').appendTitle(dropdown, "PROP").appendTitle(' to');
    valueInput.connection.setCheck(initialArgType);
    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
    this.setPreviousStatement(true);
    this.setNextStatement(true);

    // Renames the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName =  newname;
        var title = this.inputList[0].titleRow[0];
        title.setText('set ' + this.instanceName + '.');
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
  };
};

/**
 * Create a generic property setter block for a component with the given
 * type name. propNames is the list of property names to appear in the
 * dropdown menu for the setter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */

Blockly.ComponentBlock.genericSetter = function(propNames, propYailTypes, propTooltips, typeName) {
  this.category = 'Component';
  this.blockType = 'genericsetter',
  this.helpUrl = "http://foo";  // TODO: fix
  this.typeName = typeName;
  this.propYailTypes = propYailTypes;

  // Initializes a generic setter BlocklyBlock
  this.init = function() {
    var thisBlock = this;
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    var dropdown = new Blockly.FieldDropdown(
        function() {return propNames; },
        function(selection)
        // change the input type to match the new selection
        {this.setText(selection);
        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes);
        // this will set the socket arg type and also disconnect any plugged in block
        // where the type doesn't match the socket type
        thisBlock.getInput("VALUE").connection.setCheck(newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    );

    this.appendDummyInput().appendTitle('set ' +  this.typeName + '.').appendTitle(dropdown, "PROP");
    // the argument input type on the COMPONENT socket is COMPONENT
    var compInput = this.appendValueInput("COMPONENT").appendTitle('of component').setAlign(Blockly.ALIGN_RIGHT);
    compInput.connection.setCheck(Blockly.Language.YailTypeToBlocklyTypeMap.component);
    var initialArgType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes);
    var valueInput = this.appendValueInput("VALUE").appendTitle('to').setAlign(Blockly.ALIGN_RIGHT);
    valueInput.connection.setCheck(initialArgType);

    Blockly.Language.setTooltip(thisBlock, Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
    this.setPreviousStatement(true);
    this.setNextStatement(true);

    // Renames the block's typeName, and revises its title
    this.rename = function(oldname, newname) {
      if (this.typeName == oldname) {
        this.typeName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText('set ' + this.typeName + '.');
      }
    };
  };
};

//Get the Blockly type of the argument for the property name that is
//selected with the dropdown
Blockly.ComponentBlock.getCurrentArgType = function (fieldDropdown, propNames, propYailTypes) {
  var propertyName = fieldDropdown.getValue();
  var yailType = propYailTypes[propertyName];
  var blocklyType = Blockly.Language.YailTypeToBlocklyType(yailType);
  return blocklyType;
}

//Get the tooltip for the property name that is
//selected with the dropdown
Blockly.ComponentBlock.getCurrentTooltip = function (fieldDropdown, propNames, propTooltips) {
  return propTooltips[fieldDropdown.getValue()];
}

/**
 * Create a component (object) block for a component with the given
 * instance name.
 */
Blockly.ComponentBlock.component = function(instanceName) {
  this.category = 'Component';
  this.blockType = 'component',
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;

  // Initialize this type of ComponentBlock
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_COMPONENT);
    this.appendDummyInput().appendTitle(this.instanceName);
    this.setOutput(true, "COMPONENT");

    // Renames the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText(this.instanceName);
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
  };
};

