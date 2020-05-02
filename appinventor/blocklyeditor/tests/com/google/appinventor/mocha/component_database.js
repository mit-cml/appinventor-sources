/* -*- mode: javascript; js-indent-level: 2; -*- */
/**
 * Copyright © 2020 Massachusetts Institute of Technology. All rights reserved.
 * @license
 */

/**
 * @fileoverview Tests for the ComponentDatabase implementation.
 * @author Evan W. Patton <ewpatton@mit.edu>
 */

goog.require('AI.Blockly.ComponentDatabase');
goog.require('goog.structs.Set');

suite('ComponentDatabase', function() {
  /** @type {Blockly.ComponentDatabase} */
  var db;
  setup(function() {
    db = new Blockly.ComponentDatabase();
  });

  function populateSampleTypes() {
    db.populateTypes([{
      'type': 'com.example.TestComponent',
      'name': 'TestComponent',
      'external': 'false',
      'version': '1',
      'categoryString': 'USERINTERFACE',
      'helpString': 'test',
      'showOnPalette': 'true',
      'nonVisible': 'false',
      'iconName': 'images/component.png',
      'events': [{
        'name': 'TestEvent',
        'description': 'A test event',
        'deprecated': 'false',
        'params': [{
          'name': 'a',
          'type': 'number'
        }]
      }],
      'properties': [{
        'name': 'TestDesignerProperty',
        'editorType': 'text',
        'defaultValue': '',
        'editorArgs': []
      },{
        'name': 'BadDesignerProperty',
        'editorType': 'text',
        'defaultValue': '',
        'editorArgs': []
      }],
      'blockProperties': [{
        'name': 'TestDesignerProperty',
        'description': 'A test designer property',
        'type': 'text',
        'rw': 'write-only',
        'deprecated': 'false'
      },{
        'name': 'TestBlockProperty',
        'description': 'A test block property',
        'type': 'text',
        'rw': 'read-write',
        'deprecated': 'false'
      },{
        'name': 'TestReadOnlyProperty',
        'description': 'A test read-only property',
        'type': 'text',
        'rw': 'read-only',
        'deprecated': 'false'
      },{
        'name': 'TestDeprecatedProperty',
        'description': 'A deprecated property',
        'type': 'text',
        'rw': 'invisible',
        'deprecated': 'true'
      }],
      'methods': [{
        'name': 'TestVoidMethod',
        'description': '',
        'deprecated': 'false',
        'params': [{
          'name': 'first',
          'type': 'text'
        }]
      },{
        'name': 'TestReturnMethod',
        'description': '',
        'deprecated': 'false',
        'params': [],
        'returnType': 'boolean'
      }]
    }]);
  }

  function populateSampleTranslations() {
    db.populateTranslations({
      'COMPONENT-TestComponent': 'TranslatedComponent',
      'PROPERTY-TestDesignerProperty': 'TranslatedDesignerProperty',
      'PROPERTY-TestBlockProperty': 'TranslatedBlockProperty',
      'METHOD-TestMethod': 'TranslatedMethod',
      'EVENT-TestEvent': 'TranslatedEvent',
      'PARAM-first': 'param1',
      'PARAM-a': 'param2',
      'EVENTDESC-TestEventEventDescriptions': 'Event i18n tooltip',
      'EVENTDESC-TestComponent2.TestEventEventDescriptions': 'Component-dependent event translation',
      'METHODDESC-TestMethodMethodDescriptions': 'Method i18n tooltip',
      'METHODDESC-TestComponent2.TestMethodMethodDescriptions': 'Component-dependent method translation',
      'PROPDESC-TestDesignerPropertyPropertyDescriptions': 'Designer i18n tooltip',
      'PROPDESC-TestComponent2.TestDesignerPropertyPropertyDescriptions': 'Component-dependent property translation',
      'PROPDESC-TestBlockPropertyPropertyDescriptions': 'Block i18n tooltip',
      'PROPDESC-TestComponent2.TestBlockPropertyPropertyDescriptions': 'Component-dependent property translation'
    })
  }

  test('Descriptor Records', function () {
    // for coverage--these are typedefs to describe simple_components.json
    // noinspection JSClosureCompilerSyntax
    new ComponentInfo();
    // noinspection JSClosureCompilerSyntax
    new ParameterDescriptor();
    // noinspection JSClosureCompilerSyntax
    new EventDescriptor();
    // noinspection JSClosureCompilerSyntax
    new MethodDescriptor();
    // noinspection JSClosureCompilerSyntax
    new PropertyDescriptor();
    // noinspection JSClosureCompilerSyntax
    new ComponentTypeDescriptor();
    // noinspection JSClosureCompilerSyntax
    new ComponentInstanceDescriptor();
  });

  suite('#addInstance', function() {
    test('Should return true on add new instance', function() {
      chai.assert.isTrue(db.addInstance('0', 'Screen1', 'Form'));
    });
    test('Should return false if uid exists', function() {
      db.addInstance('0', 'Screen1', 'Form');
      chai.assert.isFalse(db.addInstance('0', 'Button1', 'Button'));
    });
  });

  suite('#hasInstance', function() {
    setup(function() {
      db.addInstance('0', 'Screen1', 'Form');
    });
    test('Should return true if an instance exists', function() {
      chai.assert.isTrue(db.hasInstance('0'));
    });
    test('Should return false if an instance doesn\'t exist', function() {
      chai.assert.isFalse(db.hasInstance('1'));
    })
  });

  suite('#getInstance', function() {
    test('Should return the instance by uid or name', function() {
      db.addInstance('0', 'Screen1', 'Form');
      chai.assert.isObject(db.getInstance('0'));
      chai.assert.isObject(db.getInstance('Screen1'));
      chai.assert.strictEqual(db.getInstance('0'), db.getInstance('Screen1'));
    });
    test('Should return undefined for bad uid or name', function() {
      chai.assert.isUndefined(db.getInstance('NonexistentComponent1'));
      chai.assert.isUndefined(db.getInstance('-1'));
    });
  });

  suite('#renameInstance', function() {
    setup(function() {
      db.addInstance('0', 'Screen1', 'Form');
      db.addInstance('1', 'Button1', 'Button');
    });
    test('Should rename an instance and return true', function() {
      chai.assert.isTrue(db.renameInstance('1', 'Button1', 'Button2'));
      chai.assert.equal('Button2', db.getInstance('1').name);
    });
    test('Should return false if instance doesn\'t exist', function() {
      chai.assert.isFalse(db.renameInstance('2', 'Sound1', 'Sound2'));
    });
    test('Should return false if rename is a no-op', function() {
      chai.assert.isFalse(db.renameInstance('1', 'Button1', 'Button1'));
    });
  });

  suite('#removeInstance', function() {
    setup(function() {
      db.addInstance('0', 'Screen1', 'Form');
      db.addInstance('1', 'Button1', 'Button');
    });
    test('Should remove component and return true', function() {
      chai.assert.isTrue(db.removeInstance('1'));
      chai.assert.isFalse(db.hasInstance('1'));
    });
    test('Should return false if no component was removed', function() {
      chai.assert.isFalse(db.removeInstance('2'));
    });
  });

  suite('#forEachInstance', function() {
    setup(function() {
      db.addInstance('0', 'Screen1', 'Form');
      db.addInstance('1', 'Button1', 'Button');
    });
    test('Should iterate over components', function() {
      var seenItems = [];
      db.forEachInstance(function(instance) {
        seenItems.push(instance.name);
      });
      chai.assert.equal(seenItems[0], 'Screen1');
      chai.assert.equal(seenItems[1], 'Button1');
    });
  });

  suite('#hasType', function() {
    setup(function() {
      populateSampleTypes();
    });
    test('Should have valid types', function() {
      chai.assert.isTrue(db.hasType('TestComponent'));
      chai.assert.isFalse(db.hasType('not a valid component type'));
    });
  });

  suite('#getType', function() {
    setup(function() {
      populateSampleTypes();
    });
    test('Should return a type object for valid type', function() {
      var typeinfo = db.getType('TestComponent');
      chai.assert.isDefined(typeinfo);
      chai.assert.equal(typeinfo.type, 'com.example.TestComponent');  // make sure we got the right thing.
    });
    test('Should return undefined for invalid types', function() {
      chai.assert.isUndefined(db.getType('not a valid component type'));
    })
  });

  suite('#getInstanceNames', function() {
    setup(function() {
      db.addInstance('0', 'Screen1', 'Form');
      db.addInstance('1', 'Button1', 'Button');
    });
    test('Should return an array of component names', function() {
      chai.assert.deepEqual(db.getInstanceNames(), ['Screen1', 'Button1']);
    });
  });

  suite('#instanceNameToTypeName', function() {
    setup(function() {
      db.addInstance('0', 'Screen1', 'Form');
      db.addInstance('1', 'Button1', 'Button');
    });
    test('Should return the typename for a given component', function() {
      chai.assert.equal(db.instanceNameToTypeName('Screen1'), 'Form');
      chai.assert.equal(db.instanceNameToTypeName('Button1'), 'Button');
    });
    test('Should return false if not found', function() {
      chai.assert.isFalse(db.instanceNameToTypeName('invalid component name'));
    });
  });

  suite('#getComponentUidNameMapByType', function() {
    setup(function() {
      db.addInstance('0', 'Screen1', 'Form');
      db.addInstance('1', 'Button1', 'Button');
      db.addInstance('2', 'Button2', 'Button');
    });
    test('Should return an empty array if no items exist', function() {
      chai.assert.isEmpty(db.getComponentUidNameMapByType('Sound'));
    });
    test('Should return a populated array if items exist', function() {
      chai.assert.deepEqual(db.getComponentUidNameMapByType('Form'), [['Screen1', '0']]);
      chai.assert.deepEqual(db.getComponentUidNameMapByType('Button'),
        [['Button1', '1'], ['Button2', '2']]);
    })
  });

  suite('#getComponentNamesByType', function() {
    setup(function() {
      db.addInstance('0', 'Screen1', 'Form');
      db.addInstance('2', 'Button2', 'Button');
      db.addInstance('1', 'Button1', 'Button');
      db.addInstance('3', 'Button3', 'Button');
      db.addInstance('4', 'Button0', 'Button');
    });
    test('Should return empty array for invalid type', function() {
      chai.assert.deepEqual(db.getComponentNamesByType('Invalid'), [[' ', 'none']]);
    });
    test('Should return an empty map if no items exist', function() {
      chai.assert.deepEqual(db.getComponentNamesByType('Sound'), [[' ', 'none']]);
    });
    test('Should return a populated array when items present', function() {
      chai.assert.deepEqual(db.getComponentNamesByType('Button'),
        [['Button0', 'Button0'], ['Button1', 'Button1'], ['Button2', 'Button2'], ['Button3', 'Button3']]);
    });
    test('Coverage', function() {
      db.renameInstance('4', 'Button0', 'Button1');
      chai.assert.deepEqual(db.getComponentNamesByType('Button'),
        [['Button1', 'Button1'], ['Button1', 'Button1'], ['Button2', 'Button2'], ['Button3', 'Button3']]);
    });
  });

  suite('#populateTypes', function() {
    test('Lack of deprecated means no deprecation', function() {
      db.populateTypes([{
        'name': 'TestComponent2',
        'events': [{
          'name': 'TestEvent'
        }],
        'methods': [{
          'name': 'TestMethod'
        }],
        'properties': [],
        'blockProperties': [{
          'name': 'TestProperty'
        }]
      }])
    });

    test('Make use of parameters over params', function() {
      db.populateTypes([{
        'name': 'TestComponent2',
        'events': [{
          'name': 'TestEvent',
          'parameters': [{
            'name': 'GoodParam',
            'type': 'number'
          }],
          'params': [{
            'name': 'BadParam',
            'type': 'number'
          }]
        }],
        'methods': [{
          'name': 'TestMethod',
          'parameters': [{
            'name': 'GoodParam',
            'type': 'number'
          }],
          'params': [{
            'name': 'BadParam',
            'type': 'number'
          }]
        }],
        'properties': [],
        'blockProperties': []
      }]);

    });
  });

  suite('#populateTranslations', function() {
    test('Should ignore invalid entries', function() {
      var db2 = new Blockly.ComponentDatabase();
      db2.populateTranslations({
        'INVALID-Entry': ''
      });
      chai.assert.deepEqual(db, db2);
    })
  });

  suite('#getEventForType', function() {
    setup(function() {
      populateSampleTypes();
    });
    test('Should return undefined if type is invalid', function() {
      chai.assert.isUndefined(db.getEventForType('invalid type', 'Initialize'));
    });
    test('Should return undefined if the event is invalid', function() {
      chai.assert.isUndefined(db.getEventForType('TestComponent', 'invalid event'));
    })
    test('Should return a valid event entry', function() {
      var event = db.getEventForType('TestComponent', 'TestEvent');
      chai.assert.isDefined(event);
      chai.assert.equal(event.name, 'TestEvent');
    });
  });

  suite('#forEventInType', function() {
    setup(function() {
      populateSampleTypes();
    });
    test('Should not call callback for invalid type', function() {
      db.forEventInType('invalid type', function() {
        chai.assert.fail();
      });
    });
    test('Should call callback for each event', function() {
      var names = [];
      db.forEventInType('TestComponent', function(event, name) {
        names.push(name);
      });
      chai.assert.deepEqual(names, ['TestEvent']);
    });
  });

  suite('#getMethodForType', function() {
    setup(function() {
      populateSampleTypes();
    });
    test('Should return undefined if type is invalid', function() {
      chai.assert.isUndefined(db.getMethodForType('invalid type', 'DoStuff'));
    });
    test('Should return undefined if the method is invalid', function() {
      chai.assert.isUndefined(db.getMethodForType('TestComponent', 'invalid method'));
    });
    test('Should return a valid method entry', function() {
      var method = db.getMethodForType('TestComponent', 'TestVoidMethod');
      chai.assert.isDefined(method);
      chai.assert.equal(method.name, 'TestVoidMethod');
    });
  });

  suite('#forMethodInType', function() {
    setup(function() {
      populateSampleTypes();
    });
    test('Should not call callback for invalid type', function() {
      db.forMethodInType('invalid type', function() {
        chai.assert.fail();
      });
    });
    test('Should call callback for each event', function() {
      var names = [];
      db.forMethodInType('TestComponent', function(method, name) {
        names.push(name);
      });
      chai.assert.deepEqual(names, ['TestVoidMethod', 'TestReturnMethod']);
    });
  });

  suite('#getPropertyForType', function() {
    setup(function() {
      populateSampleTypes();
    });
    test('Should return undefined if type is invalid', function() {
      chai.assert.isUndefined(db.getPropertyForType('invalid type', 'Enabled'));
    });
    test('Should return undefined if the property is invalid', function() {
      chai.assert.isUndefined(db.getPropertyForType('TestComponent', 'invalid property'));
    });
    test('Should return a valid property entry', function() {
      var prop = db.getPropertyForType('TestComponent', 'TestDesignerProperty');
      chai.assert.isDefined(prop);
      chai.assert.equal(prop.name, 'TestDesignerProperty');
    });
  });

  suite('#getSetterNamesForType', function() {
    setup(function() {
      populateSampleTypes();
    });
    test('Should return null for invalid type', function() {
      chai.assert.isNull(db.getSetterNamesForType('invalid type'));
    });
    test('Should return a valid list for type', function() {
      chai.assert.deepEqual(db.getSetterNamesForType('TestComponent'),
        ['TestDesignerProperty', 'TestBlockProperty']);
    })
  });

  suite('#getGetterNamesForType', function() {
    setup(function() {
      populateSampleTypes();
    });
    test('Should return null for invalid type', function() {
      chai.assert.isNull(db.getGetterNamesForType('invalid type'));
    });
    test('Should return a valid list for type', function() {
      chai.assert.deepEqual(db.getGetterNamesForType('TestComponent'),
        ['TestBlockProperty', 'TestReadOnlyProperty']);
    });
  });

  suite('Internationalization', function() {
    setup(function() {
      populateSampleTypes();
      populateSampleTranslations();
    });

    suite('#getInternationalizedComponentType', function() {
      test('Should return translated typename', function() {
        chai.assert.equal(db.getInternationalizedComponentType('TestComponent'), 'TranslatedComponent');
      });
      test('Should return default if provided and no translation exists', function() {
        chai.assert.equal(db.getInternationalizedComponentType('BadComponent', 'TranslatedComponent2'), 'TranslatedComponent2');
      });
      test('Should return untranslated name if no translation exists', function() {
        chai.assert.equal(db.getInternationalizedComponentType('BadComponent'), 'BadComponent');
      });
    });

    suite('#getInternationalizedEventName', function() {
      test('Should return translated events', function() {
        chai.assert.equal(db.getInternationalizedEventName('TestEvent'), 'TranslatedEvent');
      });
      test('Should return default if provided and no translation exists', function() {
        chai.assert.equal(db.getInternationalizedEventName('BadEvent', 'TranslatedEvent'), 'TranslatedEvent');
      })
      test('Should return untranslated events if no translation exists', function() {
        chai.assert.equal(db.getInternationalizedEventName('BadEvent'), 'BadEvent');
      });
    });

    suite('#getInternationalizedEventDescription', function() {
      test('Should return component-dependent event description if available', function() {
        chai.assert.equal(db.getInternationalizedEventDescription('TestComponent2', 'TestEvent'), 'Component-dependent event translation');
      });
      test('Should return translated event description', function() {
        chai.assert.equal(db.getInternationalizedEventDescription('TestComponent', 'TestEvent'), 'Event i18n tooltip');
      });
      test('Should return default if provided and no translation exists', function() {
        chai.assert.equal(db.getInternationalizedEventDescription('TestComponent', 'BadEvent', 'Alt description'), 'Alt description');
      });
      test('Should return event if no translation exists', function() {
        chai.assert.equal(db.getInternationalizedEventDescription('TestComponent', 'BadEvent'), 'BadEvent');
      });
    });

    suite('#getInternationalizedMethodName', function() {
      test('Should return translated method name', function() {
        chai.assert.equal(db.getInternationalizedMethodName('TestMethod'), 'TranslatedMethod');
      });
      test('Should return default if provided and no translation exists', function() {
        chai.assert.equal(db.getInternationalizedMethodName('BadMethod', 'TranslatedMethod'), 'TranslatedMethod');
      });
      test('Should return untranslated methods if no translation exists', function() {
        chai.assert.equal(db.getInternationalizedMethodName('BadMethod'), 'BadMethod');
      });
    });

    suite('#getInternationalizedMethodDescription', function() {
      test('Should return component-dependent method description if available', function() {
        chai.assert.equal(db.getInternationalizedMethodDescription('TestComponent2', 'TestMethod'), 'Component-dependent method translation');
      });
      test('Should return translated method description', function() {
        chai.assert.equal(db.getInternationalizedMethodDescription('TestComponent', 'TestMethod'), 'Method i18n tooltip');
      });
      test('Should return default if provided and no translation exists', function() {
        chai.assert.equal(db.getInternationalizedMethodDescription('TestComponent', 'BadMethod', 'Alt description'), 'Alt description');
      });
      test('Should return method if no translation exists', function() {
        chai.assert.equal(db.getInternationalizedMethodDescription('TestComponent', 'BadMethod'), 'BadMethod');
      });
    });

    suite('#getInternationalizedParameterName', function() {
      test('Should return translated parameter name', function() {
        chai.assert.equal(db.getInternationalizedParameterName('first'), 'param1');
      });
      test('Should return default if provided and no translation exists', function() {
        chai.assert.equal(db.getInternationalizedParameterName('randomParam', 'random parameter'), 'random parameter');
      });
      test('Should return untranslated parameter if no translation exists', function() {
        chai.assert.equal(db.getInternationalizedParameterName('randomParam'), 'randomParam');
      });
    });

    suite('#getInternationalizedPropertyName', function() {
      test('Should return translated property name', function() {
        chai.assert.equal(db.getInternationalizedPropertyName('TestDesignerProperty'), 'TranslatedDesignerProperty');
      });
      test('Should return default if provided and no translation exists', function() {
        chai.assert.equal(db.getInternationalizedPropertyName('MissingProperty', 'TranslatedProperty'), 'TranslatedProperty');
      });
      test('Should return untransalted property if no translation exists', function() {
        chai.assert.equal(db.getInternationalizedPropertyName('MissingProperty'), 'MissingProperty');
      });
    });

    suite('#getInternationalizedPropertyDescription', function() {
      test('Should return component-dependent property description if available', function() {
        chai.assert.equal(db.getInternationalizedPropertyDescription('TestComponent2', 'TestDesignerProperty'), 'Component-dependent property translation');
      });
      test('Should return translated property description', function() {
        chai.assert.equal(db.getInternationalizedPropertyDescription('TestComponent', 'TestDesignerProperty'), 'Designer i18n tooltip');
      });
      test('Should return default if provided and no translation exists', function() {
        chai.assert.equal(db.getInternationalizedPropertyDescription('TestComponent', 'MissingProperty', 'Alt description'), 'Alt description');
      });
      test('Should return property if no translation exists', function() {
        chai.assert.equal(db.getInternationalizedPropertyDescription('TestComponent', 'MissingProperty'), 'MissingProperty');
      });
    });
  });
});
