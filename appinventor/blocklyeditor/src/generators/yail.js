// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012-2019 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Helper functions for generating Yail for blocks.
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * @author sharon@google.com (Sharon Perl)
 */

'use strict';

goog.provide('AI.Yail');

AI.Yail = new Blockly.Generator('Yail');

/**
 * List of illegal variable names. This is not intended to be a security feature.  Blockly is
 * 100% client-side, so bypassing this list is trivial.  This is intended to prevent users from
 * accidentally clobbering a built-in object or function.
 *
 * TODO: fill this in or remove it.
 * @private
 */
AI.Yail.RESERVED_WORDS_ = '';

/**
 * Order of operation ENUMs.
 * https://developer.mozilla.org/en/Yail/Reference/Operators/Operator_Precedence
 */
AI.Yail.ORDER_ATOMIC = 0;         // 0 "" ...
AI.Yail.ORDER_NONE = 99;          // (...)

AI.Yail.YAIL_ACTIVE_FORM = "(SimpleForm:getActiveForm)";
AI.Yail.YAIL_ADD_COMPONENT = "(add-component ";
AI.Yail.YAIL_ADD_TO_LIST = "(add-to-list ";
AI.Yail.YAIL_BEGIN = "(begin ";
// This "break" symbol must match the one that is used in the
// foreach macro, forrange and while macros
AI.Yail.YAIL_BREAK = "*yail-break*";
AI.Yail.YAIL_CALL_COMPONENT_METHOD = "(call-component-method ";
AI.Yail.YAIL_CALL_COMPONENT_METHOD_BLOCKING = "(call-component-method-with-blocking-continuation ";
AI.Yail.YAIL_CALL_COMPONENT_METHOD_CONTINUATION = "(call-component-method-with-continuation ";
AI.Yail.YAIL_CALL_COMPONENT_TYPE_METHOD = "(call-component-type-method ";
AI.Yail.YAIL_CALL_COMPONENT_TYPE_METHOD_BLOCKING = "(call-component-type-method-with-blocking-continuation ";
AI.Yail.YAIL_CALL_COMPONENT_TYPE_METHOD_CONTINUATION = "(call-component-type-method-with-continuation ";
AI.Yail.YAIL_CALL_YAIL_PRIMITIVE = "(call-yail-primitive ";
AI.Yail.YAIL_CLEAR_FORM = "(clear-current-form)";
// The lines below are complicated because we want to support versions of the
// Companion older then 2.20ai2 which do not have set-form-name defined
AI.Yail.YAIL_SET_FORM_NAME_BEGIN = "(try-catch (let ((attempt (delay (set-form-name \"";
AI.Yail.YAIL_SET_FORM_NAME_END = "\")))) (force attempt)) (exception java.lang.Throwable 'notfound))";
AI.Yail.YAIL_CLOSE_COMBINATION = ")";
AI.Yail.YAIL_CLOSE_BLOCK = ")\n";
AI.Yail.YAIL_COMMENT_MAJOR = ";;; ";
AI.Yail.YAIL_COMPONENT_REMOVE = "(remove-component ";
AI.Yail.YAIL_COMPONENT_TYPE = "component";
AI.Yail.YAIL_CONSTANT_ALL = '(static-field com.google.appinventor.components.runtime.util.YailDictionary \'ALL)';
AI.Yail.YAIL_DEFINE = "(def ";
AI.Yail.YAIL_DEFINE_EVENT = "(define-event ";
AI.Yail.YAIL_DEFINE_GENERIC_EVENT = '(define-generic-event ';
AI.Yail.YAIL_DEFINE_FORM = "(define-form ";
AI.Yail.YAIL_DO_AFTER_FORM_CREATION = "(do-after-form-creation ";
AI.Yail.YAIL_DOUBLE_QUOTE = "\"";
AI.Yail.YAIL_FALSE = "#f";
AI.Yail.YAIL_FILTER = "(filter_nondest ";
AI.Yail.YAIL_FOREACH = "(foreach ";
AI.Yail.YAIL_FORRANGE = "(forrange ";
AI.Yail.YAIL_GET_COMPONENT = "(get-component ";
AI.Yail.YAIL_GET_ALL_COMPONENT = "(get-all-components ";
AI.Yail.YAIL_GET_PROPERTY = "(get-property ";
AI.Yail.YAIL_GET_COMPONENT_TYPE_PROPERTY = "(get-property-and-check  ";
AI.Yail.YAIL_GET_VARIABLE = "(get-var ";
AI.Yail.YAIL_AND_DELAYED = "(and-delayed ";
AI.Yail.YAIL_OR_DELAYED = "(or-delayed ";
AI.Yail.YAIL_IF = "(if ";
AI.Yail.YAIL_INIT_RUNTIME = "(init-runtime)";
AI.Yail.YAIL_INITIALIZE_COMPONENTS = "(call-Initialize-of-components";
AI.Yail.YAIL_LET = "(let ";
AI.Yail.YAIL_LEXICAL_VALUE = "(lexical-value ";
AI.Yail.YAIL_SET_LEXICAL_VALUE = "(set-lexical! ";
AI.Yail.YAIL_LINE_FEED = "\n";
AI.Yail.YAIL_MAP = "(map_nondest ";
AI.Yail.YAIL_NULL = "(get-var *the-null-value*)";
AI.Yail.YAIL_EMPTY_LIST = "'()";
AI.Yail.YAIL_EMPTY_YAIL_LIST = "'(*list*)";
AI.Yail.YAIL_EMPTY_DICT = "(make com.google.appinventor.components.runtime.util.YailDictionary)";
AI.Yail.YAIL_OPEN_BLOCK = "(";
AI.Yail.YAIL_OPEN_COMBINATION = "(";
AI.Yail.YAIL_QUOTE = "'";
AI.Yail.YAIL_REDUCE = "(reduceovereach ";
AI.Yail.YAIL_RENAME_COMPONENT = "(rename-component ";
AI.Yail.YAIL_SET_AND_COERCE_PROPERTY = "(set-and-coerce-property! ";
AI.Yail.YAIL_SET_AND_COERCE_COMPONENT_TYPE_PROPERTY = "(set-and-coerce-property-and-check! ";
AI.Yail.YAIL_SET_SUBFORM_LAYOUT_PROPERTY = "(%set-subform-layout-property! ";
AI.Yail.YAIL_SET_VARIABLE = "(set-var! ";
AI.Yail.YAIL_SET_THIS_FORM = "(set-this-form)\n ";
AI.Yail.YAIL_SPACER = " ";
AI.Yail.YAIL_SORT_COMPARATOR_NONDEST = "(sortcomparator_nondest ";
AI.Yail.YAIL_SORT_KEY_NONDEST = "(sortkey_nondest ";
AI.Yail.YAIL_TRUE = "#t";
AI.Yail.YAIL_UNREGISTER =
  "com.google.appinventor.components.runtime.EventDispatcher:unregisterEventForDelegation";
AI.Yail.YAIL_WHILE = "(while ";
AI.Yail.YAIL_LIST_CONSTRUCTOR = "*list-for-runtime*";

AI.Yail.SIMPLE_HEX_PREFIX = "&H";
AI.Yail.YAIL_HEX_PREFIX = "#x";

// permit leading and trailing whitespace for checking that strings are numbers
AI.Yail.INTEGER_REGEXP = "^[\\s]*[-+]?[0-9]+[\\s]*$";
AI.Yail.FLONUM_REGEXP = "^[\\s]*[-+]?([0-9]*)((\\.[0-9]+)|[0-9]\\.)[\\s]*$";


/**
 * Generate the Yail code for this blocks workspace, given its associated form specification.
 *
 * @param {String} formJson JSON string describing the contents of the form. This is the JSON
 *    content from the ".scm" file for this form.
 * @param {String} packageName the name of the package (to put in the define-form call)
 * @param {Boolean} forRepl  true if the code is being generated for the REPL, false if for an apk
 * @param {Blockly.WorkspaceSvg} workspace Workspace to use for generating code.
 * @returns {String} the generated code if there were no errors.
 */
AI.Yail.getFormYail = function(formJson, packageName, forRepl, workspace) {
  var oldForRepl = this.forRepl;
  var code;
  try {
    this.forRepl = forRepl;
    code = AI.Yail.getFormYail_(formJson, packageName, forRepl, workspace);
  } finally {
    this.forRepl = oldForRepl;
  }
  return code;
};

AI.Yail.getFormYail_ = function(formJson, packageName, forRepl, workspace) {
  var jsonObject = JSON.parse(formJson);
  // TODO: check for JSON parse error
  var componentNames = [];
  var formProperties;
  var formName;
  var code = [];
  var propertyNameConverter = function(input) {
    return input;
  };
  if (jsonObject.Properties) {
    formProperties = jsonObject.Properties;
    formName = formProperties.$Name;
  } else {
    throw "Cannot find form properties";
  }
  if (!formName) {
    throw "Unable to determine form name";
  }

  if (!forRepl) {
    code.push(AI.Yail.getYailPrelude(packageName, formName,
      jsonObject.Properties['Theme'] === 'Classic'));
  }

  var componentMap = workspace.buildComponentMap([], [], false, false);
  var globalBlocks = componentMap.globals;
  for (var i = 0, block; block = globalBlocks[i]; i++) {
    code.push(AI.Yail.blockToCode(block));
  }

  if (formProperties) {
    var sourceType = jsonObject.Source;
    if (sourceType == "Form") {
      code = code.concat(AI.Yail.getComponentLines(formName, formProperties, null /*parent*/,
          componentMap, false /*forRepl*/, propertyNameConverter, workspace.getComponentDatabase()));
    } else {
      throw "Source type " + sourceType + " is invalid.";
    }

    // Fetch all of the components in the form, this may result in duplicates
    componentNames = AI.Yail.getDeepNames(formProperties, componentNames);
    // Remove the duplicates
    componentNames = componentNames.filter(function(elem, pos) {
        return componentNames.indexOf(elem) == pos});

    // Add runtime initializations
    code.push(AI.Yail.YAIL_INIT_RUNTIME);

    if (forRepl) {
      code = AI.Yail.wrapForRepl(formName, code, componentNames);
    }

    // TODO?: get rid of empty property assignments? I'm not convinced this is necessary.
    // The original code in YABlockCompiler.java attempts to do this, but it matches on
    // "set-property" rather than "set-and-coerce-property" so I'm not sure it is actually
    // doing anything. If we do need this, something like the call below might work.
    //
    // finalCode = code.join('\n').replace(/\\(set-property.*\"\"\\)\\n*/mg, "");
  }

  return code.join('\n');  // Blank line between each section.
};

AI.Yail.getDeepNames = function(componentJson, componentNames) {
  if (componentJson.$Components) {
    var children = componentJson.$Components;
    for (var i = 0, child; child = children[i]; i++) {
      componentNames.push(child.$Name);
      componentNames = AI.Yail.getDeepNames(child, componentNames);
    }
  }
  return componentNames;
};

/**
 * Generate the beginning Yail code for an APK compilation (i.e., not the REPL)
 *
 * @param {String} packageName  the name of the package for the app
 *     (e.g. "appinventor.ai_somebody.myproject.Screen1")
 * @param {String} formName  (e.g., "Screen1")
 * @returns {String} Yail code
 * @private
*/
AI.Yail.getYailPrelude = function(packageName, formName, classicTheme) {
 return "#|\n$Source $Yail\n|#\n\n"
     + AI.Yail.YAIL_DEFINE_FORM
     + packageName
     + AI.Yail.YAIL_SPACER
     + formName
     + AI.Yail.YAIL_SPACER
     + (classicTheme ? "#t" : "#f")
     + AI.Yail.YAIL_CLOSE_BLOCK
     + "(require <com.google.youngandroid.runtime>)\n";
};

/**
 * Wraps Yail code for use in the REPL and returns the new code as an array of strings
 *
 * @param {String} formName
 * @param {Array} code  code strings to be wrapped
 * @param {Array} componentNames array of component names
 * @returns {Array} wrapped code strings
 * @private
 */
AI.Yail.wrapForRepl = function(formName, code, componentNames) {
  var replCode = [];
  replCode.push(AI.Yail.YAIL_BEGIN);
  replCode.push(AI.Yail.YAIL_CLEAR_FORM);
  if (formName != "Screen1") {
    // If this form is not named Screen1, then the REPL won't be able to resolve any references
    // to it or to any properties on the form itself (such as Title, BackgroundColor, etc) unless
    // we tell it that "Screen1" has been renamed to formName.
    // By generating a call to rename-component here, the REPL will rename "Screen1" to formName
    // in the current environment. See rename-component in runtime.scm.
    replCode.push(AI.Yail.getComponentRenameString("Screen1", formName));
  }
  replCode = replCode.concat(code);
  replCode.push(AI.Yail.getComponentInitializationString(formName, componentNames));
  replCode.push(AI.Yail.YAIL_CLOSE_BLOCK);
  return replCode;
};

/**
 * Return code to initialize all components in componentMap.
 *
 * @param {string} formName name of the current screen
 * @param {Array} componentNames array of names of components in the workspace
 * @returns {Array} code strings
 * @private
 */
AI.Yail.getComponentInitializationString = function(formName, componentNames) {
  var code = AI.Yail.YAIL_INITIALIZE_COMPONENTS;
  code += " " + AI.Yail.YAIL_QUOTE + formName;
  for (var i = 0, cName; cName = componentNames[i]; i++) {  // TODO: will we get non-component fields this way?
    if (cName != formName)                                  // Avoid duplicate initialization of the form
      code = code + " " + AI.Yail.YAIL_QUOTE + cName;
  }
  code = code + ")";
  return code;
};

/**
 * Generate Yail code for the component described by componentJson, and all of its child
 * components (if it has any). componentJson may describe a Form or a regular component. The
 * generated code includes adding each component to the form, as well as generating code for
 * the top-level blocks for that component.
 *
 * @param {String} formName
 * @param {String} componentJson JSON string describing the component
 * @param {String} parentName  the name of the component that contains this component (which may be
 *    its Form, for top-level components).
 * @param {Object} componentMap map from component names to the top-level blocks for that component
 *    in the workspace. See the Blockly.Component.buildComponentMap description for the structure.
 * @param {Boolean} forRepl true iff we're generating code for the REPL rather than an apk.
 * @param {function(string): string} nameConverter function that converts fully-qualified
 *    property names into YAIL names for invocation.
 * @param {!Blockly.ComponentDatabase} componentDb Component database, for type information
 * @returns {Array} code strings
 * @private
 */
AI.Yail.getComponentLines = function(formName, componentJson, parentName, componentMap,
  forRepl, nameConverter, componentDb) {
  var code = [], i, block, child;
  var componentName = componentJson.$Name;
  if (componentJson.$Type == 'Form') {
    code = AI.Yail.getFormPropertiesLines(formName, componentJson, !forRepl, componentDb, forRepl);
  } else {
    code = AI.Yail.getComponentPropertiesLines(formName, componentJson, parentName, !forRepl,
      nameConverter, componentDb, forRepl);
  }

  if (!forRepl) {
    // Generate code for all top-level blocks related to this component
    if (componentMap.components && componentMap.components[componentName]) {
      var componentBlocks = componentMap.components[componentName];
      for (i = 0; block = componentBlocks[i]; i++) {
        code.push(AI.Yail.blockToCode(block));
      }
    }
  }

  // Generate code for child components of this component
  if (componentJson.$Components) {
    var children = componentJson.$Components;
    for (i = 0; child = children[i]; i++) {
      code = code.concat(AI.Yail.getComponentLines(formName, child, componentName,
          componentMap, forRepl, nameConverter, componentDb));
    }
  }
  return code;
};

/**
 * Generate Yail to add the component described by componentJson to its parent, followed by
 * the code that sets each property of the component (for all its properties listed in
 * componentJson).
 *
 * @param {String} formName
 * @param {String} componentJson JSON string describing the component
 * @param {String} parentName  the name of the component that contains this component (which may be
 *    its Form, for top-level components).
 * @param {Boolean} includeComments whether to include comments in the generated code
 * @param {function(string): string} nameConverter function that converts a fully-qualified
 *    property name into a YAIL-compatible name.
 * @param {Blockly.ComponentDatabase} componentDb Component database, for type information
 * @param {boolean} forRepl true iff we're generating code for the REPL rather than an app
 * @returns {Array} code strings
 * @private
 */
AI.Yail.getComponentPropertiesLines = function(formName, componentJson, parentName,
    includeComments, nameConverter, componentDb, forRepl) {
  var code = [];
  var componentName = componentJson.$Name;
  var componentType = componentJson.$Type;
  // generate the yail code that adds the component to its parent, followed by the code that
  // sets each property of the component
  if (includeComments) {
    code.push(AI.Yail.YAIL_COMMENT_MAJOR + componentName + AI.Yail.YAIL_LINE_FEED);
  }
  // Send component type for full class path but first feed it to propertyNameConverter function
  // passed in. This may trim it back to just the simple component name if we are sending this yail
  // to an older companion
  code.push(AI.Yail.YAIL_ADD_COMPONENT + parentName + AI.Yail.YAIL_SPACER +
    nameConverter(componentDb.getType(componentType).type) +
    AI.Yail.YAIL_SPACER + componentName + AI.Yail.YAIL_SPACER);
  code = code.concat(AI.Yail.getPropertySettersLines(componentJson, componentName, componentDb, forRepl));
  code.push(AI.Yail.YAIL_CLOSE_BLOCK);
  return code;
};

/**
 * Generate Yail to set the properties for the Form described by componentJson.
 *
 * @param {String} formName
 * @param {String} componentJson JSON string describing the component
 * @param {Boolean} includeComments whether to include comments in the generated code
 * @param {Blockly.ComponentDatabase} componentDb Component database, for type information
 * @param {boolean} forRepl true iff we're generating code for the REPL rather than an app
 * @returns {Array} code strings
 * @private
 */
AI.Yail.getFormPropertiesLines = function(formName, componentJson, includeComments, componentDb, forRepl) {
  var code = [];
  if (includeComments) {
    code.push(AI.Yail.YAIL_COMMENT_MAJOR + formName + AI.Yail.YAIL_LINE_FEED);
  }
  var yailForComponentProperties = AI.Yail.getPropertySettersLines(componentJson, formName, componentDb, forRepl);
  if (yailForComponentProperties.length > 0) {
    // getPropertySettersLine returns an array of lines.  So we need to
    // concatenate them (using join) before pushing them onto the Yail expression.
    // WARNING:  There may be other type errors of this sort in this file, which
    // (hopefully) will be uncovered in testing. Please
    // be alert for these errors and check carefully.
    code.push(AI.Yail.YAIL_DO_AFTER_FORM_CREATION + yailForComponentProperties.join(" ") +
      AI.Yail.YAIL_CLOSE_BLOCK);
  }
  return code;
};

/**
 * Generate the code to set property values for the specifed component.
 *
 * @param {Object} componentJson JSON String describing the component
 * @param {String} componentName the name of the component (also present in the $Name field in
 *    componentJson)
 * @param {Blockly.ComponentDatabase} componentDb The workspace's database of components and types.
 * @param {boolean} forRepl true iff we're generating code for the REPL rather than an app
 * @returns {Array} code strings
 * @private
 *
 * Hack Note (JIS): We do not output a property setter line for the TutorialURL
 * property. This property is only for use within the designer and has no meaning
 * within an Android app. It is harmless to output it, once we have deployed a new
 * companion (version > 2.41). Once such a Companion is deployed, the exception
 * for TutorialURL below (and this comment) can be removed.
 */
AI.Yail.getPropertySettersLines = function(componentJson, componentName, componentDb, forRepl) {
  var code = [];
  var type = componentDb.getType(componentJson['$Type']);
  function shouldSendProperty(prop, info) {
    return (prop.charAt(0) !== '$' && prop !== 'Uuid' &&
      prop !== 'TutorialURL' && prop !== 'BlocksToolkit') ||
      (info && info['alwaysSend']);
  }
  // Gather all of the properties together
  var propsToSend = Object.keys(componentJson);
  for (var prop in type['properties']) {
    var property = type['properties'][prop];
    if (property['alwaysSend'] && !(prop in componentJson)) {
      propsToSend.push(property['name']);
    }
  }
  // Keep ordering so default properties will still be sent in the right position.
  propsToSend.sort();
  // Construct the code
  propsToSend.forEach(function(prop) {
    var info = type['properties'][prop];
    if (shouldSendProperty(prop, info)) {
      var value = componentJson[prop];
      if (!Boolean(value) && value !== '') {
        value = info['defaultValue'];
      }
      code.push(AI.Yail.getPropertySetterString(componentName, componentJson['$Type'], prop,
        value, componentDb, forRepl));
    }
  });
  return code;
};

/**
 * Generate the code to set a single property value.
 *
 * @param {String} componentName
 * @param {String} componentType
 * @param {String} propertyName
 * @param {String} propertyValue
 * @param {!Blockly.ComponentDatabase} componentDb Component database, for type information
 * @param {boolean} forRepl true iff we're generating code for the REPL rather than an app
 * @returns code string
 * @private
 */
AI.Yail.getPropertySetterString = function(componentName, componentType, propertyName,
    propertyValue, componentDb, forRepl) {
  var code = AI.Yail.YAIL_SET_AND_COERCE_PROPERTY + AI.Yail.YAIL_QUOTE +
    componentName + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + propertyName +
    AI.Yail.YAIL_SPACER;
  var propDef = componentDb.getPropertyForType(componentType, propertyName);
  // If a designer property does not have a corresponding block property, then propDef will be
  // undefined. In this case, we assume "any" as the type. A corresponding fix is included in
  // ComponentProcessor to enforce that newer components/extensions always have both a designer
  // and block definition.
  var propType = AI.Yail.YAIL_QUOTE + (propDef ? propDef.type : "any");
  var value;
  if (propertyName === 'ApiKey' && !forRepl) {
    // Obfuscate API keys in compiled apps
    value = AI.Yail.obfuscateProperty(propertyValue);
  } else {
    value = AI.Yail.getPropertyValueString(propertyValue, propType);
  }
  code = code.concat(value + AI.Yail.YAIL_SPACER + propType + AI.Yail.YAIL_CLOSE_BLOCK);
  return code;
};

/**
 * Obfuscate a designer property's value.
 *
 * @param text the original text to be obfuscated
 * @returns {string} the deobfuscation code following the same logic as the obfuscated_text block
 */
AI.Yail.obfuscateProperty = function(text) {
  var confounder = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 8);
  var setupObfuscation = function(input, confounder) {
    // The algorithm below is also implemented in scheme in runtime.scm
    // If you change it here, you have to change it there!
    // Note: This algorithm is like xor, if applied to its output
    // it regenerates it input.
    var acc = [];
    // First make sure the confounder is long enough...
    while (confounder.length < input.length) {
      confounder += confounder;
    }
    for (var i = 0; i < input.length; i++) {
      var c = (input.charCodeAt(i) ^ confounder.charCodeAt(i)) & 0xFF;
      var b = (c ^ input.length - i) & 0xFF;
      var b2 = ((c >> 8) ^ i) & 0xFF;
      acc.push(String.fromCharCode((b2 << 8 | b) & 0xFF));
    }
    return acc.join('');
  }
  var encoded = setupObfuscation(text, confounder);
  return '(text-deobfuscate ' + [encoded, confounder].map(AI.Yail.quote_).join(' ') + ')';
};

/**
 * Generate the Yail code for a property value. Special case handling when propertyType is
 * "'number", "'boolean", "'component", the empty string, or null. For all other property values
 * it returns the value as converted by AI.Yail.quotifyForREPL().
 *
 * @param {String} propertyValue
 * @param {String} propertyType
 * @returns code string
 * @private
 */
AI.Yail.getPropertyValueString = function(propertyValue, propertyType) {
  if (propertyType == "'number") {
    if (propertyValue.match(AI.Yail.INTEGER_REGEXP)
            || propertyValue.match(AI.Yail.FLONUM_REGEXP)) { // integer
      return propertyValue;
    } else if (propertyValue.match(AI.Yail.SIMPLE_HEX_PREFIX + "[0-9A-F]+")) { // hex
      return AI.Yail.YAIL_HEX_PREFIX +
        propertyValue.substring(AI.Yail.SIMPLE_HEX_PREFIX.length);
    }
  } else if (propertyType == "'boolean") {
    if (-1 != propertyValue.indexOf("False")) {
      return "#f";
    } else if (-1 != propertyValue.indexOf("True")) {
      return "#t";
    }
  } else if (propertyType == "'component") {
    if (propertyValue == "") {
      return "\"\"";
    } else {
      return AI.Yail.YAIL_GET_COMPONENT + propertyValue + ")";
    }
  }

  if (propertyValue == "" || propertyValue == "null") {  // empty string
    return "\"\"";
  }
  return AI.Yail.quotifyForREPL(propertyValue);
};

/**
 * Generate the code to rename a component
 *
 * @param {String} oldName
 * @param {String} newName
 * @returns {String} code
 * @private
 */
AI.Yail.getComponentRenameString = function(oldName, newName) {
  return AI.Yail.YAIL_RENAME_COMPONENT + AI.Yail.quotifyForREPL(oldName)
    + AI.Yail.YAIL_SPACER + AI.Yail.quotifyForREPL(newName)
    + AI.Yail.YAIL_CLOSE_BLOCK;
};

/**
 * Transform a string to the Kawa input representation of the string, for sending to
 * the REPL, by using backslash to escape quotes and backslashes. But do not escape a backslash
 * if it is part of \n. Then enclose the result in quotes.
 * TODO: Extend this to a complete transformation that deals with the full set of formatting
 * characters.
 *
 * @param {String} s string to be quotified
 * @returns {String}
 * @private
 */
AI.Yail.quotifyForREPL = function(s) {
  if (!s) {
    return null;
  } else {
    var sb = [];
    sb.push('"');
    var len = s.length;
    var lastIndex = len - 1;
    for (var i = 0; i < len; i++) {
      c = s.charAt(i);
      if (c == '\\') {
        // If this is \n, \t or \r don't slashify the backslash
        // TODO(user): Make this cleaner and more general
        if (!(i == lastIndex) && (s.charAt(i + 1) == 'n' || s.charAt(i + 1) == 't' || s.charAt(i + 1) == 'r')) {
          sb.push(c);
          sb.push(s.charAt(i + 1));
          i = i + 1;
        } else {
          sb.push('\\');
          sb.push(c);
        }
      } else if (c == '"') {
        sb.push('\\');
        sb.push(c);
      } else {
        var u = s.charCodeAt(i);  // unicode of c
        if (u < ' '.charCodeAt(0) || u > '~'.charCodeAt(0)) {
          // Replace any special chars with \u1234 unicode
          var hex = "000" + u.toString(16);
          hex = hex.substring(hex.length - 4);
          sb.push("\\u" + hex);
        } else {
          sb.push(c);
        }
      }
    }
    sb.push('"');
    return sb.join("");
  }
};

/**
 * Encode a string as a properly escaped Yail string, complete with quotes.
 * @param {String} string Text to encode.
 * @return {String} Yail string.
 * @private
 */

AI.Yail.quote_ = function(string) {
  string = AI.Yail.quotifyForREPL(string);
  if (!string) {                // quotifyForREPL can return null for
    string = '""';              // empty string
  }
  return string;
};

/**
 * Naked values are top-level blocks with outputs that aren't plugged into
 * anything.  A trailing semicolon is needed to make this legal.
 * @param {string} line Line of generated code.
 * @return {string} Legal line of code.
 */
AI.Yail.scrubNakedValue = function(line) {
  return line;
};

/**
 * Handles comments for the specified block and any connected value blocks.
 * Calls any statements following this block.
 * @param {!Blockly.Block} block The current block.
 * @param {string} code The Yail code created for this block.
 * @param {boolean} thisOnly if true, only return code for this block and not any following
 *   statements note that calls of scrub_ with no 3rd parameter are equivalent to thisOnly=false,
 *   which was the behavior before this parameter was added.
 * @return {string} Yail code with comments and subsequent blocks added.
 * @private
 */
AI.Yail.scrub_ = function(block, code, thisOnly) {
  if (code === null) {
    // Block has handled code generation itself.
    return '';
  }
  var commentCode = '';
  /* TODO: fix for Yail comments?
  // Only collect comments for blocks that aren't inline.
  if (!block.outputConnection || !block.outputConnection.targetConnection) {
    // Collect comment for this block.
    var comment = block.getCommentText();
    if (comment) {
      commentCode += Blockly.Generator.prefixLines(comment, '// ') + '\n';
    }
    // Collect comments for all value arguments.
    // Don't collect comments for nested statements.
    for (var x = 0; x < block.inputList.length; x++) {
      if (block.inputList[x].type == Blockly.INPUT_VALUE) {
        var childBlock = block.inputList[x].targetBlock();
        if (childBlock) {
          var comment = Blockly.Generator.allNestedComments(childBlock);
          if (comment) {
            commentCode += Blockly.Generator.prefixLines(comment, '// ');
          }
        }
      }
    }
  }*/
  var nextBlock = block.nextConnection && block.nextConnection.targetBlock();
  var nextCode = thisOnly ? "" : this.blockToCode(nextBlock);
  return commentCode + code + nextCode;
};

AI.Yail.getDebuggingYail = function() {
  var code = [];
  var componentMap = Blockly.Component.buildComponentMap([], [], false, false);

  var globalBlocks = componentMap.globals;
  for (var i = 0; i < globalBlocks.length; i++) {
    code.push(AI.Yail.blockToCode(globalBlocks[i]));
  }

  var blocks = Blockly.common.getMainWorkspace().getTopBlocks(true);
  for (var x = 0, block; block = blocks[x]; x++) {

    // generate Yail for each top-level language block
    if (!block.category) {
      continue;
    }
    code.push(AI.Yail.blockToCode(block));
  }
  return code.join('\n\n');
};

/**
 * Generate code for the specified block but *not* attached blocks.
 * @param {Blockly.Block} block The block to generate code for.
 * @return {string|!Array} For statement blocks, the generated code.
 *     For value blocks, an array containing the generated code and an
 *     operator order value.  Returns '' if block is null.
 */
AI.Yail.blockToCode1 = function(block) {
  if (!block) {
    return '';
  }
  var func = this[block.type];
  if (!func) {
    throw 'Language "' + name + '" does not know how to generate code ' +
        'for block type "' + block.type + '".';
  }
  var code = func.call(block);
  if (code instanceof Array) {
    // Value blocks return tuples of code and operator order.
    if (block.disabled || block.isBadBlock()) {
      code[0] = '';
    }
    return [this.scrub_(block, code[0], true), code[1]];
  } else {
    if (block.disabled || block.isBadBlock()) {
      code = '';
    }
    return this.scrub_(block, code, true);
  }
};

/**
 * Generates YAIL that will unregister an event if the corresponding block is disabled in the
 * workspace.
 *
 * @param {!Blockly.BlockSvg} block
 * @returns {string}
 */
AI.Yail.disabledEventBlockToCode = function(block) {
  return AI.Yail.YAIL_OPEN_BLOCK + AI.Yail.YAIL_UNREGISTER + AI.Yail.YAIL_SPACER +
    AI.Yail.YAIL_ACTIVE_FORM + AI.Yail.YAIL_SPACER +
    AI.Yail.YAIL_QUOTE + block.getFieldValue('COMPONENT_SELECTOR') +
    AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + block.eventName +
    AI.Yail.YAIL_CLOSE_BLOCK;
};
