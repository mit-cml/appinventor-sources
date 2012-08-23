/* Copyright 2012 Massachusetts Institute of Technology. All rights reserved. */

/**
 * @fileoverview Generating Yail for component-related blocks.
 *
 * Code generation for component-related blocks works a little differently than for built-in
 * blocks. Since we create component instance blocks dynamically, as components are added to a
 * project, we also create their code generation blocks dynamically. The functions below return
 * functions to generate code for blocks of the appropriate types, binding in the names of
 * the component instances for which they are created. See blocklyeditor/src/component.js for the
 * code that uses these functions when new components are added to a project.
 * 
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * @author sharon@google.com (Sharon Perl)
 */

Blockly.Yail = Blockly.Generator.get('Yail');

/**
 * Returns a function that takes no arguments, generates Yail for an event handler declaration block
 * and returns the generated code string.
 *
 * @param {String} instanceName
 * @param {String} eventName
 * @returns {Function} event code generation function with instanceName and eventName bound in
 */
Blockly.Yail.event = function(instanceName, eventName) {
  return function() {
    var body = Blockly.Yail.statementToCode(this, 'DO', Blockly.Yail.ORDER_NONE);
    var code = Blockly.Yail.YAIL_DEFINE_EVENT
      + instanceName
      + Blockly.Yail.YAIL_SPACER 
      + eventName
      + Blockly.Yail.YAIL_OPEN_COMBINATION 
      // TODO: formal params go here
      + Blockly.Yail.YAIL_CLOSE_COMBINATION
      + Blockly.Yail.YAIL_SET_THIS_FORM 
      + Blockly.Yail.YAIL_SPACER
      + body
      + Blockly.Yail.YAIL_CLOSE_COMBINATION; 
    // TODO: handle deactivated block, null body
    return code;
  }
}

/**
 * Returns a function that generates Yail to call to a method with a return value. The generated
 * function takes no arguments and returns a 2-element Array with the method call code string
 * and the operation order Blockly.Yail.ORDER_ATOMIC.
 * 
 * @param {String} instanceName
 * @param {String} methodName
 * @returns {Function} method call generation function with instanceName and methodName bound in
 */
Blockly.Yail.methodWithReturn = function(instanceName, methodName) {
  return function() {
    return [Blockly.Yail.methodHelper(this, instanceName, methodName), Blockly.Yail.ORDER_ATOMIC];
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
Blockly.Yail.methodNoReturn = function(instanceName, methodName) {
  return function() {
    return Blockly.Yail.methodHelper(this, instanceName, methodName);
  }
}

/**
 * Generate and return the code for a method call. The generated code is the same regardless of
 * whether the method returns a value or not.
 *
 * @param {String} instanceName
 * @param {String} methodName
 * @returns {Function} method call generation function with instanceName and methodName bound in
 */
Blockly.Yail.methodHelper = function(methodBlock, instanceName, methodName) {
  var args = [];  // 
  for (var x = 0; x < methodBlock.params.length; x++) {
    args[x] = Blockly.Yail.YAIL_SPACER 
        + Blockly.Yail.valueToCode(methodBlock, 'ARG' + x, Blockly.Yail.ORDER_NONE);
  }

  // TODO: deal with argument type coercions

  var typelistForYail = Blockly.Yail.YAIL_EMPTY_LIST;

  return Blockly.Yail.YAIL_CALL_COMPONENT_METHOD
    + Blockly.Yail.YAIL_QUOTE
    + instanceName 
    + Blockly.Yail.YAIL_SPACER
    + Blockly.Yail.YAIL_QUOTE
    + methodName
    + Blockly.Yail.YAIL_SPACER 
    + Blockly.Yail.YAIL_OPEN_COMBINATION
    + Blockly.Yail.YAIL_LIST_CONSTRUCTOR
    + args.join(', ') 
    + Blockly.Yail.YAIL_CLOSE_COMBINATION
    + Blockly.Yail.YAIL_SPACER
    + typelistForYail 
    + Blockly.Yail.YAIL_CLOSE_COMBINATION;
}

/**
 * Returns a function that takes no arguments, generates Yail code for setting a component property
 * and returns the code string.
 *
 * @param {String} instanceName
 * @returns {Function} property setter code generation function with instanceName bound in
 */
Blockly.Yail.setproperty = function(instanceName) {
  return function() {
    var propName = this.getTitleText("PROP");
    var propType = Blockly.Yail[instanceName + '_setproperty'].propTypes[propName];
    var assignLabel = Blockly.Yail.YAIL_QUOTE + instanceName + Blockly.Yail.YAIL_SPACER 
      + Blockly.Yail.YAIL_QUOTE + propName;
    var code = Blockly.Yail.YAIL_SET_AND_COERCE_PROPERTY + assignLabel + Blockly.Yail.YAIL_SPACER;
    code = code.concat(Blockly.Yail.valueToCode(this, 'VALUE', Blockly.Yail.ORDER_NONE /*TODO:?*/));
    code = code.concat(Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + propType + Blockly.Yail.YAIL_CLOSE_COMBINATION);
    return code;
  }
}

/**
 * Returns a function that takes no arguments, generates Yail code for getting a component property
 * value and returns a 2-element array containing the property getter code string and the operation
 * order Blockly.Yail.ORDER_ATOMIC.
 *
 * @param {String} instanceName
 * @returns {Function} property getter code generation function with instanceName bound in
 */
Blockly.Yail.getproperty = function(instanceName) {
  return function() {
    var propName = this.getTitleText("PROP");
    var propType = Blockly.Yail[instanceName + '_setproperty'].propTypes[propName];
    var code = Blockly.Yail.YAIL_GET_PROPERTY
      + Blockly.Yail.YAIL_QUOTE
      + instanceName
      + Blockly.Yail.YAIL_SPACER
      + Blockly.Yail.YAIL_QUOTE
      + propName
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
    return [code, Blockly.Yail.ORDER_ATOMIC];    
  }
}


/**
 * Returns a function that takes no arguments, generates Yail to get the value of a component
 * object, and returns a 2-element array containing the component getter code and the operation
 * order Blockly.Yail.ORDER_ATOMIC.
 *
 * @param {String} instanceName
 * @returns {Function} component getter code generation function with instanceName bound in
 */
Blockly.Yail.componentObject = function(instanceName) {
  return function() { 
    return [Blockly.Yail.YAIL_GET_COMPONENT + instanceName + Blockly.Yail.YAIL_CLOSE_COMBINATION,
            Blockly.Yail.ORDER_ATOMIC];
  }
}