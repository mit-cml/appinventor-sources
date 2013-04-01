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
Blockly.ComponentBlock.COLOUR_EVENT = Blockly.CONTROL_CATEGORY_HUE;
Blockly.ComponentBlock.COLOUR_METHOD = Blockly.PROCEDURE_CATEGORY_HUE;
Blockly.ComponentBlock.COLOUR_GETSET = Blockly.COLOR_CATEGORY_HUE;
Blockly.ComponentBlock.COLOUR_COMPONENT = Blockly.COLOR_CATEGORY_HUE;


//TODO(): add I18N

/**
 * Create an event block of the given type for a component with the given
 * instance name. eventType is one of the "events" objects in a typeJsonString
 * passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.event = function(eventType, instanceName, typeName) {
  this.category = 'Component',
  this.blockType = 'event',
  this.helpUrl = 'http://foo', // TODO: fix
  this.instanceName = instanceName;
  this.eventType = eventType;
  this.typeName = typeName;

  // Initializes an event BlocklyBlock
  // This is called by the BlocklyBlock constructor where its type field is set to, e.g., 'Button1_Click'
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_EVENT);

    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        this.setValue(value);
        this.block.instanceName = value;
      }

    };

    this.appendDummyInput().appendTitle('when ')
    .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
    .appendTitle('.' + this.eventType.name);
    this.componentDropDown.setValue(this.instanceName);
    
    if(eventType.params.length != 0){
      var paramInput = this.appendDummyInput();
    }

    // TODO: implement event callback parameters.  Need to figure out how to do procedures and 
    // make callback parameters consistent with that.
    var paramLength = eventType.params.length;
    for (var i = 0, param; param = eventType.params[i]; i++) {
      paramInput.appendTitle(param.name + (i != paramLength -1 ? "," : ""));
    }
    
    this.appendStatementInput("DO").appendTitle('do');
    Blockly.Language.setTooltip(this, eventType.description);
    this.setPreviousStatement(false);
    this.setNextStatement(false);

    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Renames the block's instanceName and type (set in BlocklyBlock constructor), and revises its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        //var title = this.inputList[0].titleRow[0];
        //title.setText('when ' + this.instanceName + '.' + this.eventType.name);  // Revise title
        this.componentDropDown.setValue(this.instanceName);
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
Blockly.ComponentBlock.method = function(methodType, instanceName, typeName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.methodType = methodType;
  this.typeName = typeName;

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
    paramTypes.push(Blockly.Language.YailTypeToBlocklyType(param.type,Blockly.Language.INPUT));
    this.yailTypes.push(param.type);
  }
  this.paramTypes = paramTypes;

  // Initializes a method BlocklyBlock
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);

    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        this.setValue(value);
        this.block.instanceName = value;
      }

    };

    this.appendDummyInput().appendTitle('call ')
    .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
    .appendTitle('.' + this.methodType.name);
    this.componentDropDown.setValue(this.instanceName);

    Blockly.Language.setTooltip(this, this.methodType.description);
    for (var i = 0, param; param = this.params[i]; i++) {
      var newInput = this.appendValueInput("ARG" + i).appendTitle(param);
      newInput.connection.setCheck(this.paramTypes[i]);
    }
    // methodType.returnType is a Yail type
    if (this.methodType.returnType) {
      this.setOutput(true, Blockly.Language.YailTypeToBlocklyType(this.methodType.returnType,Blockly.Language.OUTPUT));
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }
    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Rename the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        //var title = this.inputList[0].titleRow[0];
        //title.setText('call ' + this.instanceName + '.' + this.methodType.name);
        this.componentDropDown.setValue(this.instanceName);
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
    paramTypes.push(Blockly.Language.YailTypeToBlocklyType(param.type,Blockly.Language.INPUT));
    this.yailTypes.push(param.type);
  }
  this.paramTypes = paramTypes;

  // Initializes a generic method BlocklyBlock
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);
    this.appendDummyInput().appendTitle('call');
    this.appendDummyInput().appendTitle(this.typeName + '.' + this.methodType.name);

    var compInput = this.appendValueInput("COMPONENT").setCheck(this.typeName).appendTitle('for component');

    for (var i = 0, param; param = this.params[i]; i++) {
      var newInput = this.appendValueInput("ARG" + i).appendTitle(param);
      newInput.connection.setCheck(this.paramTypes[i]);
    }
    // methodType.returnType is a Yail type
    if (this.methodType.returnType) {
      this.setOutput(true, Blockly.Language.YailTypeToBlocklyType(this.methodType.returnType,Blockly.Language.OUTPUT));
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }
    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
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

Blockly.ComponentBlock.getter = function(propNames, propYailTypes, propTooltips, instanceName, typeName) {
  this.category = 'Component';
  this.blockType = 'getter',
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.typeName = typeName;

  // Initializes a getter BlocklyBlock
  // Called by the BlocklyBlock constructor where its type field is set to, e.g., 'TextBox1_getproperty'
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    var thisBlock = this;
    this.propYailTypes = propYailTypes;
    var dropdown = new Blockly.FieldDropdown(

        function() {return propNames; },
        // change the output type and tooltip to match the new selection
        function(selection) {this.setValue(selection);

        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes,Blockly.Language.OUTPUT);
        // this will disconnect the block if the new outputType doesn't match the
        // socket the block is plugged into

        thisBlock.outputConnection.setCheck(newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    );

    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        this.setValue(value);
        this.block.instanceName = value;
      }

    };


    this.appendDummyInput().appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
    .appendTitle('.').appendTitle(dropdown, "PROP");
    this.componentDropDown.setValue(this.instanceName);
    // Set the initial output type and tooltip since they won't be set in the dropdown callback
    var newType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes,Blockly.Language.OUTPUT);
    if(newType == Number || newType == String){
      newType = [Number,String];
    }
    thisBlock.setOutput(true, newType);

    this.mutationToDom = Blockly.ComponentBlock.componentPropertyMutationToDom;
    this.domToMutation = Blockly.ComponentBlock.componentPropertyDomToMutation;

    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));

    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Rename the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        //var title = this.inputList[0].titleRow[0];
        //title.setText(this.instanceName + '.');
        this.componentDropDown.setValue(this.instanceName);
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
  this.typeName = typeName;

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
        {this.setValue(selection);
        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes,Blockly.Language.OUTPUT);
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
    var compInput = this.appendValueInput("COMPONENT").setCheck(this.typeName).appendTitle('of component').setAlign(Blockly.ALIGN_RIGHT);
    // Set the initial and tooltip type since they won't be set in the dropdown callback
    var newType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes,Blockly.Language.OUTPUT);

    thisBlock.setOutput(true, newType);

    this.mutationToDom = Blockly.ComponentBlock.componentPropertyMutationToDom;
    this.domToMutation = Blockly.ComponentBlock.componentPropertyDomToMutation;

    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));

    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
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

Blockly.ComponentBlock.setter = function(propNames, propYailTypes, propTooltips, instanceName, typeName) {
  this.category = 'Component';
  this.blockType = 'setter',
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.propYailTypes = propYailTypes;
  this.propTooltips = propTooltips;
  this.typeName = typeName;

  // Initializes a setter BlocklyBlock
  // The type field is set in BlocklyBlock constructor to, e.g., Label1_setproperty
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(
        function() {return propNames; },
        function(selection)
        // change the input type to match the new selection
        {this.setValue(selection);
        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes,Blockly.Language.INPUT);
        // this will set the socket arg type and also disconnect any plugged in block
        // where the type doesn't match the socket type

        thisBlock.getInput("VALUE").connection.setCheck(newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    );

    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        this.setValue(value);
        this.block.instanceName = value;
      }

    };

    var initialArgType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes,Blockly.Language.INPUT);
    var valueInput = this.appendValueInput("VALUE", initialArgType)
    .appendTitle('set ')
    .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
    .appendTitle('.').appendTitle(dropdown, "PROP").appendTitle(' to');
    valueInput.connection.setCheck(initialArgType);
    this.componentDropDown.setValue(this.instanceName);
    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
    this.setPreviousStatement(true);
    this.setNextStatement(true);

    this.mutationToDom = Blockly.ComponentBlock.componentPropertyMutationToDom;
    this.domToMutation = Blockly.ComponentBlock.componentPropertyDomToMutation;

    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Renames the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName =  newname;
        //var title = this.inputList[0].titleRow[0];
        //title.setText('set ' + this.instanceName + '.');
        this.componentDropDown.setValue(this.instanceName);
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
        {this.setValue(selection);
        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes,Blockly.Language.INPUT);
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
    var compInput = this.appendValueInput("COMPONENT").setCheck(this.typeName).appendTitle('of component').setAlign(Blockly.ALIGN_RIGHT);

    var initialArgType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes,Blockly.Language.INPUT);
    var valueInput = this.appendValueInput("VALUE").appendTitle('to').setAlign(Blockly.ALIGN_RIGHT);
    valueInput.connection.setCheck(initialArgType);

    Blockly.Language.setTooltip(thisBlock, Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
    this.setPreviousStatement(true);
    this.setNextStatement(true);

    this.mutationToDom = Blockly.ComponentBlock.componentPropertyMutationToDom;
    this.domToMutation = Blockly.ComponentBlock.componentPropertyDomToMutation;
    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
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
Blockly.ComponentBlock.getCurrentArgType = function (fieldDropdown, propNames, propYailTypes,inputOrOutput) {
  var propertyName = fieldDropdown.getValue();
  var yailType = propYailTypes[propertyName];
  var blocklyType = Blockly.Language.YailTypeToBlocklyType(yailType,inputOrOutput);
  return blocklyType;
}


// Save the yail type of the property socket
Blockly.ComponentBlock.componentPropertyMutationToDom = function() {
  var propertyName = this.getTitleValue("PROP");
  var yailType = this.propYailTypes[propertyName]
  var container = document.createElement('mutation');
  container.setAttribute('yailtype', yailType);
  return container;
}

// Restore the blockly type of the property socket from the yail type.
Blockly.ComponentBlock.componentPropertyDomToMutation = function(xmlElement) {
  var yailType = xmlElement.getAttribute('yailtype');
  if(yailType) {
    var inputOrOutput = (this.blockType == "setter" || this.blockType == "genericsetter" ? Blockly.Language.INPUT : Blockly.Language.OUTPUT);

    var blocklyType = Blockly.Language.YailTypeToBlocklyType(yailType,inputOrOutput);
    if(inputOrOutput == Blockly.Language.OUTPUT){
      this.outputConnection.setCheck(blocklyType);
    } else {
      this.getInput("VALUE").connection.setCheck(blocklyType);
    }
  }
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
Blockly.ComponentBlock.component = function(instanceName, typeName) {
  this.category = 'Component';
  this.blockType = 'component',
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.typeName = typeName;

  // Initialize this type of ComponentBlock
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_COMPONENT);
    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        this.setValue(value);
        this.block.instanceName = value;
      }

    };
    this.appendDummyInput().appendTitle(this.componentDropDown, "COMPONENT_SELECTOR");
    this.componentDropDown.setValue(this.instanceName);
    this.setOutput(true, this.typeName);
    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Renames the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        //var title = this.inputList[0].titleRow[0];
        //title.setText(this.instanceName);
        this.componentDropDown.setValue(this.instanceName);
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
  };
};

