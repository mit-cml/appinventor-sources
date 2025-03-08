
var componentTypes = {};
var YOUNG_ANDROID_VERSION;
var BLOCKS_LANGUAGE_VERSION;

function initComponentTypes() {
  // Note: var componentTypeJson comes from component-types.js
  // for (var i = 0, comp; comp = componentTypeJson[i]; i++) {
  //   componentTypes[comp.name] = comp;
  // }
  const workspace = Blockly.common.getMainWorkspace();
  workspace.populateComponentTypes(JSON.stringify(componentTypeJson), {});
}

function processVersion(bl, yav) {
  BLOCKS_LANGUAGE_VERSION = bl;
  YOUNG_ANDROID_VERSION = yav;
}

function processBlocks(formJsonString, blocks){ // [lyn, 2015/01/01] Modified to handled upgrader changes
  return Blockly.SaveFile.load(formJsonString, blocks);
}

function processForm(formo){
  formJson = formo;
  getFormComponents(formJson);
}

function getFormComponents(formJson) {
  var formJsonObj = JSON.parse(formJson);
  defineComponents(formJsonObj.Properties);
}

function defineComponents(componentJsonObj) {
  Blockly.common.getMainWorkspace().addComponent(componentJsonObj.Uuid, componentJsonObj.$Name, componentJsonObj.$Type);
//    Blockly.Component.add(JSON.stringify(componentTypes[componentJsonObj.$Type]),
//            componentJsonObj.$Name, componentJsonObj.Uuid);
  if (componentJsonObj.$Components) {
    for (var i = 0, comp; comp = componentJsonObj.$Components[i]; i++) {
      defineComponents(comp);
    }
  }
}

function setOutput(text) {
  var output = document.getElementById('importExport');
  output.value = text;
  output.focus();
  output.select();
}

function toAppYail() {
  var code = AI.Yail.getFormYail(formJson, "fakepackagename", false, Blockly.common.getMainWorkspace());
  // setOutput(code);
  return code;
}

// -----------------
// Start of Yail Comparison functions
// -----------------

var defs = '(def ' // Definitions
var elements = ';;;' // Screen Elements
var eProperties = '(set-and-coerce-property' // Element Properties
var events = '(define-event';

// Takes the startIndex of the first parenthesis (or before) and
// returns the final index of the closing parens.
function endParens(startIndex, text) {
  var leftP = '(';
  var rightP = ')';
  var parenCounter = 1;
  var index = text.indexOf(leftP, startIndex) + 1;

  while (parenCounter != 0) {
    switch (text[index]) {
      case leftP:
        parenCounter+=1;
        break;
      case rightP:
        parenCounter+=-1;
        break;
    }
    index++
  }

  return index;
}

// Smooshes the text together, removing lines and spaces.
function smoosh(text) {
  var smooshed = text.replace(/\s/g, '');
  smooshed = smooshed.replace(/\\u2212/g, '-');
  smooshed = smooshed.replace(/\\u00d7/g, '*');
  return smooshed;
}

// Input the text of the yail definition, beginning with (def ...)
// Returns the name of the definition
function findDefName(text) {
  var name = '';

  // Check if the definition is in a parenthesis
  if (text[5] == '(') {
    name = text.substring(6, endParens(5, text) - 1);
  } else {
    // Else, the definition is a variable and not a method
    lastIndex = text.indexOf(' ', 5);
    name = text.substring(5, lastIndex);
  }
  return name;
}

function findEleName(element) {
  return element.substring(4, element.indexOf('\n'));
}

function findEventName(event) {
  return event.substring(event.indexOf(' ') + 1, endParens(6, event));
}

function yFind(type, text) {
  // Find the maximum index of definitions, ending at the declaration
  // of the first screen element.
  var firstIndex = text.indexOf(type);
  var workingText = text.substring(firstIndex, text.length);

  // Store the definitions in an array
  var things = new Array();

  var i = 0;

  while (workingText.indexOf(type) > -1) {
    firstIndex = workingText.indexOf(type);
    var end = endParens(firstIndex, workingText);
    things[i] = workingText.substring(firstIndex, end);
    workingText = workingText.substring(end, workingText.length);
    i++;
  }

  things.sort();

  return things;
}

// Compare the Element Properties, given elements
function compareEleProperties(expected, given) {

  var expectedElements = yFind(eProperties, expected);
  var givenElements = yFind(eProperties, given);

  if (expectedElements.length != givenElements.length) {
    console.log("Not the same number of Properties: " + findEleName(expected));
    return false;
  }

  for (var i = expectedElements.length - 1; i >= 0; i--) {
    if (smoosh(expectedElements[i]) != smoosh(givenElements[i])) {
      console.log("Element properties do not match:", findEleName(expected));
      return false;
    }
  }

  return true;
}

// Compare the Element Definitions, given elements
function compareEvents(expected, given) {

  var expectedEvents = yFind(events, expected);
  var givenEvents = yFind(events, given);

  if (expectedEvents.length != givenEvents.length) {
    console.log("Not the same number of Events");
    return false;
  }

  var flag = true;

  for (var i = expectedEvents.length - 1; i >= 0; i--) {
    if (smoosh(expectedEvents[i]) != smoosh(givenEvents[i])) {
      console.log("Events do not match: ", findEventName(expectedEvents[i]));
      flag = false;
    }
  }

  return flag;
}

// Compares the Definitions of expected (classic) vs newblocks
function compareDefinitions(expected, given) {

  expectedDefs = yFind(defs, expected);
  givenDefs = yFind(defs, given);


  if (expectedDefs.length != givenDefs.length) {
    console.log("Not the same number of Definitions");
    return false;
  }

  var flag = true;

  for (var i = expectedDefs.length - 1; i >= 0; i--) {
    if (smoosh(expectedDefs[i]) != smoosh(givenDefs[i])) {
      console.log("Failed to match: " + findDefName(expectedDefs[i]), (smoosh(expectedDefs[i]) == smoosh(givenDefs[i])));
      flag = false;
    }
  }

  return flag;
}

// Compare the Elements of the classic and NB
function compareElements(expected, given) {

  var expectedEles = yFind(elements, expected);
  var givenEles = yFind(elements, given);

  if (expectedEles.length != givenEles.length) {
    console.log("Not the same number of Elements");
    return false;
  }

  var flag = true;

  for (var i = expectedEles.length - 1; i >= 0; i--) {

    if (smoosh(expectedEles[i]) != smoosh(givenEles[i])) {
      // Check if the properties do not match
      if (!compareEleProperties(expectedEles[i], givenEles[i])) {
        flag = false;
      }
    }
  }
  return flag;
}

// Find out if the NB yail matches Classic Yail
function doTheyMatch(expected, given) {
  return compareElements(expected, given) && compareDefinitions(expected, given) && compareEvents(expected, given);
}

/**
 * Creates a Blockly block of the given type with the given mutation.
 *
 * @param {!string} type The type of the block to create, e.g., component_event
 * @param {?string=} mutation Any mutation information required for the block
 * @returns {!Blockly.Block} A freshly constructed Blockly block.
 */
function blockFromMutation(type, mutation) {
  mutation = mutation || ''
  var block_text = "<xml><block type='" + type + "'>" + mutation + "</block></xml>";
  var block_xml = Blockly.utils.xml.textToDom(block_text);
  return Blockly.Xml.domToBlock(block_xml.firstElementChild, Blockly.common.getMainWorkspace());
}

/**
 * Gets the rendered name of an event block.
 *
 * @param {!Blockly.Block} block An instance of a component_event block
 * @returns {string} The name shown on the block (after the period)
 */
function getEventBlockPresentedName(block) {
  return block.inputList[0].fieldRow[2].getText().substring(1);  // strip the leading .
}

/**
 * Gets the rendered name of a method block.
 *
 * @param {!Blockly.Block} block An instance of a component_method block
 * @returns {string} The name shown on the block (after the period)
 */
function getMethodBlockPresentedName(block) {
  return block.inputList[0].fieldRow[2].getText().substring(1);  // strip the leading .
}

/**
 * Gets the rendered name of a property block.
 *
 * @param {!Blockly.Block} block An instance of a component_set_get block
 * @returns {string} The name shown on the block (after the period)
 */
function getPropertyBlockPresentedName(block) {
  if (block.set_or_get === 'get') {
    return block.inputList[0].fieldRow[3].getText();
  } else {
    return block.inputList[0].fieldRow[2].getText();
  }
}
