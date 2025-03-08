// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Component blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Yail.componentblock');

/**
 * Lyn's History:
 * [lyn, 10/27/13] Modified event parameter names to begin with YAIL_LOCAL_VAR_TAG (currently '$').
 *     All setters/getters assume such a tag. At least on Kawa-legal first character is necessary to
 *     ensure AI identifiers satisfy Kawa's identifier rules.
 */

/**
 * Returns a function that takes no arguments, generates Yail for an event handler declaration block
 * and returns the generated code string.
 *
 * @param {String} instanceName the block's instance name, e.g., Button1
 * @param {String} eventName  the type of event, e.g., Click
 * @returns {Function} event code generation function with instanceName and eventName bound in
 */
AI.Yail.component_event = function() {

  var preamble;
  if (this.isGeneric) {
    preamble = AI.Yail.YAIL_DEFINE_GENERIC_EVENT
      + this.typeName
      + AI.Yail.YAIL_SPACER
      + this.eventName;
  } else {
    preamble = AI.Yail.YAIL_DEFINE_EVENT
      + this.getFieldValue("COMPONENT_SELECTOR")
      + AI.Yail.YAIL_SPACER
      + this.eventName;
  }

  var body = AI.Yail.statementToCode(this, 'DO');
  // TODO: handle deactivated block, null body
  if(body == ""){
    body = AI.Yail.YAIL_NULL;
  }


  var code = preamble
    + AI.Yail.YAIL_OPEN_COMBINATION
    // TODO: formal params go here
    // declaredNames gives us names in local language, but we want the default
    // + this.declaredNames()
    //       .map(function (name) {return AI.Yail.YAIL_LOCAL_VAR_TAG+name;})
    //       .join(' ')
    // So we do this instead:
    + this.getParameters()
          .map(function (param) {return AI.Yail.YAIL_LOCAL_VAR_TAG+param.name;})
          .join(' ')
    + AI.Yail.YAIL_CLOSE_COMBINATION
    + AI.Yail.YAIL_SET_THIS_FORM
    + AI.Yail.YAIL_SPACER
    + body
    + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
}

AI.Yail.component_method = function() {
  var methodHelperYailString = AI.Yail.methodHelper(this, (this.isGeneric ? this.typeName : this.instanceName), this.methodName, this.isGeneric);
  //if the method returns a value
  if(this.getMethodTypeObject() && this.getMethodTypeObject().returnType) {
    return [methodHelperYailString, AI.Yail.ORDER_ATOMIC];
  } else {
    return methodHelperYailString;
  }
}

/**
 * Returns a function that generates Yail to call to a method with a return value. The generated
 * function takes no arguments and returns a 2-element Array with the method call code string
 * and the operation order AI.Yail.ORDER_ATOMIC.
 *
 * @param {String} instanceName
 * @param {String} methodName
 * @returns {Function} method call generation function with instanceName and methodName bound in
 */
AI.Yail.methodWithReturn = function(instanceName, methodName) {
  return function() {
    return [AI.Yail.methodHelper(this, instanceName, methodName, false),
            AI.Yail.ORDER_ATOMIC];
  }
}

/**
 * Returns a function that generates Yail to call to a method with no return value. The generated
 * function takes no arguments and returns the method call code string.
 *
 * @param {String} instanceName
 * @param {String} methodName
 * @returns {Function} method call generation function with instanceName and methodName bound in
 */
AI.Yail.methodNoReturn = function(instanceName, methodName) {
  return function() {
    return AI.Yail.methodHelper(this, instanceName, methodName, false);
  }
}

/**
 * Returns a function that generates Yail to call to a generic method with a return value.
 * The generated function takes no arguments and returns a 2-element Array with the method call
 * code string and the operation order AI.Yail.ORDER_ATOMIC.
 *
 * @param {String} instanceName
 * @param {String} methodName
 * @returns {Function} method call generation function with instanceName and methodName bound in
 */
AI.Yail.genericMethodWithReturn = function(typeName, methodName) {
  return function() {
    return [AI.Yail.methodHelper(this, typeName, methodName, true), AI.Yail.ORDER_ATOMIC];
  }
}

/**
 * Returns a function that generates Yail to call to a generic method with no return value.
 * The generated function takes no arguments and returns the method call code string.
 *
 * @param {String} instanceName
 * @param {String} methodName
 * @returns {Function} method call generation function with instanceName and methodName bound in
 */
AI.Yail.genericMethodNoReturn = function(typeName, methodName) {
  return function() {
    return AI.Yail.methodHelper(this, typeName, methodName, true);
  }
}

/**
 * Generate and return the code for a method call. The generated code is the same regardless of
 * whether the method returns a value or not.
 * @param {!Blockly.Blocks.component_method} methodBlock  block for which we're generating code
 * @param {String} name instance or type name
 * @param {String} methodName
 * @param {String} generic true if this is for a generic method block, false if for an instance
 * @returns {Function} method call generation function with instanceName and methodName bound in
 */
AI.Yail.methodHelper = function(methodBlock, name, methodName, generic) {
  var componentDb = methodBlock.workspace.getComponentDatabase();

// TODO: the following line  may be a bit of a hack because it hard-codes "component" as the
// first argument type when we're generating yail for a generic block, instead of using
// type information associated with the socket. The component parameter is treated differently
// here than the other method parameters. This may be fine, but consider whether
// to get the type for the first socket in a more general way in this case.
  var methodObject = methodBlock.getMethodTypeObject();
  var continuation = methodObject['continuation'];
  var paramObjects = methodObject.parameters;
  var numOfParams = paramObjects.length;
  var yailTypes = [];
  if(generic) {
    yailTypes.push(AI.Yail.YAIL_COMPONENT_TYPE);
  }
  for(var i=0;i<paramObjects.length;i++) {
    yailTypes.push(paramObjects[i].type);
  }
  //var yailTypes = (generic ? [AI.Yail.YAIL_COMPONENT_TYPE] : []).concat(methodBlock.yailTypes);
  var callPrefix;
  if (generic) {
    name = componentDb.getType(name).type;
    callPrefix = continuation ? AI.Yail.YAIL_CALL_COMPONENT_TYPE_METHOD_BLOCKING : AI.Yail.YAIL_CALL_COMPONENT_TYPE_METHOD
        // TODO(hal, andrew): check for empty socket and generate error if necessary
        + AI.Yail.valueToCode(methodBlock, 'COMPONENT', AI.Yail.ORDER_NONE)
        + AI.Yail.YAIL_SPACER;
  } else {
    callPrefix = continuation ? AI.Yail.YAIL_CALL_COMPONENT_METHOD_BLOCKING : AI.Yail.YAIL_CALL_COMPONENT_METHOD;
    name = methodBlock.getFieldValue("COMPONENT_SELECTOR");
    // special case for handling Clock.Add
    var timeUnit = methodBlock.getFieldValue("TIME_UNIT");
    if (timeUnit) {
      if (Blockly.ComponentBlock.isClockMethodName(methodName)) {
        methodName = "Add"+timeUnit; // For example, AddDays
      }
    }
  }

  var args = [];
  for (var x = 0; x < numOfParams; x++) {
    // TODO(hal, andrew): check for empty socket and generate error if necessary
    args.push(AI.Yail.YAIL_SPACER
              + AI.Yail.valueToCode(methodBlock, 'ARG' + x, AI.Yail.ORDER_NONE));
  }

  return callPrefix
    + AI.Yail.YAIL_QUOTE
    + name
    + AI.Yail.YAIL_SPACER
    + AI.Yail.YAIL_QUOTE
    + methodName
    + AI.Yail.YAIL_SPACER
    + AI.Yail.YAIL_OPEN_COMBINATION
    + AI.Yail.YAIL_LIST_CONSTRUCTOR
    + args.join(' ')
    + AI.Yail.YAIL_CLOSE_COMBINATION
    + AI.Yail.YAIL_SPACER
    + AI.Yail.YAIL_QUOTE
    + AI.Yail.YAIL_OPEN_COMBINATION
    + yailTypes.join(' ')
    + AI.Yail.YAIL_CLOSE_COMBINATION
    + AI.Yail.YAIL_CLOSE_COMBINATION;
};

AI.Yail.component_set_get = function() {
  if(this.setOrGet == "set") {
    if(this.isGeneric) {
      return AI.Yail.genericSetproperty.call(this);
    } else {
      return AI.Yail.setproperty.call(this);
    }
  } else {
    if(this.isGeneric) {
      return AI.Yail.genericGetproperty.call(this);
    } else {
      return AI.Yail.getproperty.call(this);
    }
  }
};

/**
 * Returns a function that takes no arguments, generates Yail code for setting a component property
 * and returns the code string.
 *
 * @param {String} instanceName
 * @returns {Function} property setter code generation function with instanceName bound in
 */
AI.Yail.setproperty = function() {
  var propertyName = this.getFieldValue("PROP");
  var propType = this.getPropertyObject(propertyName).type;
  var assignLabel = AI.Yail.YAIL_QUOTE + this.getFieldValue("COMPONENT_SELECTOR") + AI.Yail.YAIL_SPACER
    + AI.Yail.YAIL_QUOTE + propertyName;
  var code = AI.Yail.YAIL_SET_AND_COERCE_PROPERTY + assignLabel + AI.Yail.YAIL_SPACER;
  // TODO(hal, andrew): check for empty socket and generate error if necessary
  code = code.concat(AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE /*TODO:?*/));
  code = code.concat(AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
    + propType + AI.Yail.YAIL_CLOSE_COMBINATION);
  return code;
};


/**
 * Returns a function that takes no arguments, generates Yail code for setting a generic component's
 * property and returns the code string.
 *
 * @param {String} instanceName
 * @returns {string} property setter code generation function with instanceName bound in
 */
AI.Yail.genericSetproperty = function() {
  var propertyName = this.getFieldValue("PROP");
  var propType = this.getPropertyObject(propertyName).type;
  var assignLabel = AI.Yail.YAIL_QUOTE
    + this.workspace.getComponentDatabase().getType(this.typeName).type + AI.Yail.YAIL_SPACER
    + AI.Yail.YAIL_QUOTE + propertyName;
  var code = AI.Yail.YAIL_SET_AND_COERCE_COMPONENT_TYPE_PROPERTY
    // TODO(hal, andrew): check for empty socket and generate error if necessary
    + AI.Yail.valueToCode(this, 'COMPONENT', AI.Yail.ORDER_NONE)
    + AI.Yail.YAIL_SPACER
    + assignLabel
    + AI.Yail.YAIL_SPACER;
  // TODO(hal, andrew): check for empty socket and generate error if necessary
  code = code.concat(AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE /*TODO:?*/));
  code = code.concat(AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
    + propType + AI.Yail.YAIL_CLOSE_COMBINATION);
  return code;
};


/**
 * Returns a function that takes no arguments, generates Yail code for getting a component's
 * property value and returns a 2-element array containing the property getter code string and the
 * operation order AI.Yail.ORDER_ATOMIC.
 *
 * @param {String} instanceName
 * @returns {Function} property getter code generation function with instanceName bound in
 */
AI.Yail.getproperty = function(instanceName) {
  var propertyName = this.getFieldValue("PROP");
  var propType = this.getPropertyObject(propertyName).type;
  var code = AI.Yail.YAIL_GET_PROPERTY
    + AI.Yail.YAIL_QUOTE
    + this.getFieldValue("COMPONENT_SELECTOR")
    + AI.Yail.YAIL_SPACER
    + AI.Yail.YAIL_QUOTE
    + propertyName
    + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};


/**
 * Returns a function that takes no arguments, generates Yail code for getting a generic component's
 * property value and returns a 2-element array containing the property getter code string and the
 * operation order AI.Yail.ORDER_ATOMIC.
 *
 * @param {String} instanceName
 * @returns {Function} property getter code generation function with instanceName bound in
 */
AI.Yail.genericGetproperty = function(typeName) {
  var propertyName = this.getFieldValue("PROP");
  var propType = this.getPropertyObject(propertyName).type;
  var code = AI.Yail.YAIL_GET_COMPONENT_TYPE_PROPERTY
    // TODO(hal, andrew): check for empty socket and generate error if necessary
    + AI.Yail.valueToCode(this, 'COMPONENT', AI.Yail.ORDER_NONE)
    + AI.Yail.YAIL_SPACER
    + AI.Yail.YAIL_QUOTE
    + this.workspace.getComponentDatabase().getType(this.typeName).type
    + AI.Yail.YAIL_SPACER
    + AI.Yail.YAIL_QUOTE
    + propertyName
    + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};



/**
 * Returns a function that takes no arguments, generates Yail to get the value of a component
 * object, and returns a 2-element array containing the component getter code and the operation
 * order AI.Yail.ORDER_ATOMIC.
 *
 * @param {String} instanceName
 * @returns {Function} component getter code generation function with instanceName bound in
 */
AI.Yail.component_component_block = function() {
  return [AI.Yail.YAIL_GET_COMPONENT + this.getFieldValue("COMPONENT_SELECTOR") + AI.Yail.YAIL_CLOSE_COMBINATION,
          AI.Yail.ORDER_ATOMIC];
};

/**
 * Returns a function that takes no arguments, generates Yail to get a list of all
 * components of a type, and returns a 2-element array containing the component
 * getter code and the operation order AI.Yail.ORDER_ATOMIC.
 *
 * @param {String} instanceName
 * @returns {Function} component getter code generation function with instanceName bound in
 */
AI.Yail['component_all_component_block'] = function() {
  var fqcn = this.workspace.getComponentDatabase().getType(this.getFieldValue("COMPONENT_TYPE_SELECTOR")).componentInfo.type;
  return [AI.Yail.YAIL_GET_ALL_COMPONENT + fqcn + AI.Yail.YAIL_CLOSE_COMBINATION,
          AI.Yail.ORDER_ATOMIC];
};
