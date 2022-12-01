// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

var fs = require('fs'); //Always required to read from files
var messages = fs.read('../build/blocklyeditor/msg/messages.json');

// PhantomJS page object to open and load an URL
var page = require('webpage').create();
// Some debugging from PhantomJS
page.onConsoleMessage = function (msg) { console.log(msg); };
page.onError = function (msg, trace) {
  console.log(msg);
  trace.forEach(function(item) {
    console.log('  ', item.file, ':', item.line);
  })
};

// Open the actual page and load all the JavaScript in it
// If success is true, all went well
page.open('src/demos/yail/yail_testing_index.html', function(status) {
  if (status !== 'success') {
    console.log('load unsuccessful');
    return false;
  }

  // Evaluate the following:
  var passed = page.evaluate(function() {

    // Set the translation messages object
    Blockly.Msg = JSON.parse(arguments[0]);

    var db = Blockly.mainWorkspace.getComponentDatabase();

    // Register a fake component that we will use for testing.
    db.populateTypes(/** @type {ComponentInfo[]} */ [{
      name: 'TestExtension',
      external: 'true',
      version: '1',
      categoryString: 'UNINITIALIZED',
      helpString: 'TestExtension',
      showOnPalette: 'true',
      nonVisible: 'true',
      iconName: '',
      events: [
        {name: 'TranslatedEvent', description: 'This is a translated test.', deprecated: false, parameters: []},
        {name: 'UntranslatedEvent', description: 'This is an untranslated test.', deprecated: false, parameters: []}
      ],
      methods: [
        {name: 'TranslatedMethod', description: 'This is a translated test.', deprecated: false, parameters: []},
        {name: 'UntranslatedMethod', description: 'This is an untranslated test.', deprecated: false, parameters: []}
      ],
      blockProperties: [
        {name: 'TranslatedProperty', description: 'This is a translated test.', deprecated: true, rw: 'read-write'},
        {name: 'UntranslatedProperty', description: 'This is an untranslated test.', deprecated: false, rw: 'read-write'}
      ],
      properties: [
        {name: 'TranslatedProperty', type: 'text'},
        {name: 'UntranslatedProperty', type: 'text'}
      ]
    }]);

    // Translate some of the component.
    db.populateTranslations({
      'EVENT-TranslatedEvent': 'SuccessfulEvent',
      'METHOD-TranslatedMethod': 'SuccessfulMethod',
      'PROPERTY-TranslatedProperty': 'SuccessfulProperty',
      'EVENTDESC-TranslatedEventEventDescriptions': 'Successfully translated event test.',
      'METHODDESC-TranslatedMethodMethodDescriptions': 'Successfully translated method test.',
      'PROPDESC-TranslatedPropertyPropertyDescriptions': 'Successfully translated property test.'
    });

    var block;
    block = blockFromMutation('component_event', '<mutation component_type="TestExtension" event_name="TranslatedEvent" is_generic="false" />');
    // Event name should be translated
    assertEquals('SuccessfulEvent', getEventBlockPresentedName(block));
    // Event tooltip should be translated
    assertEquals('Successfully translated event test.', block.tooltip);
    block = blockFromMutation('component_event', '<mutation component_type="TestExtension" event_name="UntranslatedEvent" is_generic="false" />');
    // Event name should not be translated
    assertEquals('UntranslatedEvent', getEventBlockPresentedName(block));
    // Event tooltip should not be translated
    assertEquals('This is an untranslated test.', block.tooltip);
    block = blockFromMutation('component_method', '<mutation component_type="TestExtension" method_name="TranslatedMethod" is_generic="false" />');
    // Method name should be translated
    assertEquals('SuccessfulMethod', getMethodBlockPresentedName(block));
    // Method tooltip should be translated
    assertEquals('Successfully translated method test.', block.tooltip);
    block = blockFromMutation('component_method', '<mutation component_type="TestExtension" method_name="UntranslatedMethod" is_generic="false" />');
    // Method name should not be translated
    assertEquals('UntranslatedMethod', getMethodBlockPresentedName(block));
    // Method tooltip should not be translated
    assertEquals('This is an untranslated test.', block.tooltip);
    block = blockFromMutation('component_set_get', '<mutation component_type="TestExtension" property_name="TranslatedProperty" set_or_get="get" is_generic="false" />');
    // Property name should be translated
    assertEquals('SuccessfulProperty', getPropertyBlockPresentedName(block));
    // Property tooltip should be translated
    assertEquals('Successfully translated property test.', block.tooltip);
    block = blockFromMutation('component_set_get', '<mutation component_type="TestExtension" property_name="UntranslatedProperty" set_or_get="get" is_generic="false" />');
    // Property name should not be translated
    assertEquals('UntranslatedProperty', getPropertyBlockPresentedName(block));
    // Property tooltip should not be translated
    assertEquals('This is an untranslated test.', block.tooltip);

    return true;
  }, messages);

  //This is the actual result of the test
  console.log(passed);
  //Exit the phantom process
  phantom.exit();
});
