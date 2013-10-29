// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview code for translating early ai2 blocks to 10/20/13 version
 * if there is no version # in the Blockly file, then it is an early ai2 project, prior to
 * 10/21/13, when the blocks internal xml structure was overhauled
 * with descriptive mutator tags. blocksOverhaul translates the blocks
 * Methods to handle serialization of the blocks workspace
 *
 * @author wolber@usfca.edu (David Wolber)
 */


Blockly.Versioning.blocksOverhaul = function(xmlFromFile) {
// otherwise, we loaded in something with no version, we need to translate
  var blocks = xmlFromFile.getElementsByTagName('block');
  for (var i=0, im=blocks.length; i<im; i++)  {
    var blockElem=blocks[i];
    var blockType = blockElem.getAttribute('type');
    // all built-in blocks have an entry already in Blockly.Language
    //  we don't need to translate those, but if null we have some component thing
    if (Blockly.Language[blockType] == null)
    {
      // if above is null we know we don't have a built-in block
      
      // add some translations for language changes
      if (blockType=='procedures_do_then_return')
        blockElem.setAttribute('type',"controls_do_then_return");
      else
      if (blockType=='procedure_lexical_variable_get')
        blockElem.setAttribute('type',"lexical_variable_get");
      else
      if (blockType=='for_lexical_variable_get')
        blockElem.setAttribute('type',"lexical_variable_get");
      else
      {
      
        // 1st check if type has 'any' in it, if so we have a generic method
        var splitComponent=blockType.split('_');
        if (splitComponent[0]!='lexical') {
          // methods on any (generics) have a blocktype of _any_someComponent_method
          if (splitComponent[1]=='any')  // we have a generic method call
            Blockly.Versioning.translateAnyMethod(blockElem);
          else {
            // we have a set, get, component get, event, or method
            // check if the first thing is a component type. If so, we have a (generic)
            //   component set/get
            if (Blockly.ComponentTypes[splitComponent[0]]!=null)  // we have a component
              Blockly.Versioning.translateComponentSetGetProperty(blockElem);
            else {
              instance=splitComponent[0];
              var componentType=Blockly.Component.instanceNameToTypeName(instance);
              rightside=splitComponent[1];
              if (rightside=='setproperty' || rightside=='getproperty')
                Blockly.Versioning.translateSetGetProperty(blockElem);
              else
                if (rightside=='component')
                  Blockly.Versioning.translateComponentGet(blockElem)
                else
                  if (Blockly.ComponentTypes[componentType].eventDictionary[rightside]!=null)
                    Blockly.Versioning.translateEvent(blockElem);
                  else
                    if (Blockly.ComponentTypes[componentType].methodDictionary[rightside]!=null)
                      Blockly.Versioning.translateMethod(blockElem);
             }
          }
        }
      }
    }
  }

};

/**
 * translateEvent is called when we know we have an Event element that
 * needs to be translated.
*/
Blockly.Versioning.translateEvent = function(blockElem) {
  //get the event type and instance name,
  // the type attribute is "component_event"
  var blockType = blockElem.getAttribute('type');
  var component_event = blockType;
  // event block types look like: <block type="Button1_Click" x="132" y="72">
  var splitComponent=component_event.split('_');
  var instance = splitComponent[0];
  var event=splitComponent[1];
  // Paul has a function to convert instance to type
  var componentType=Blockly.Component.instanceNameToTypeName(instance);
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_event');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = goog.dom.createElement('mutation');
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('instance_name', instance);
  mutationElement.setAttribute('event_name', event);
  mutationElement.setAttribute('component_type',componentType);
  blockElem.insertBefore(mutationElement,blockElem.firstChild);

};
/**
 * translateMethod is called when we know we have a component method element that
 * needs to be translated.
*/
Blockly.Versioning.translateMethod = function(blockElem) {
  // the type attribute is "instance_method"
  var blockType = blockElem.getAttribute('type');
  // method block types look like: <block type="TinyDB_StoreValue" ...>
  var splitComponent=blockType.split('_');
  var instance = splitComponent[0];
  var method=splitComponent[1];
  // Paul has a function to convert instance to type
  var componentType=Blockly.Component.instanceNameToTypeName(instance);
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_method');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = goog.dom.createElement('mutation');
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('instance_name', instance);
  mutationElement.setAttribute('method_name', method);
  mutationElement.setAttribute('component_type',componentType);
  mutationElement.setAttribute('is_generic','false');
  blockElem.insertBefore(mutationElement,blockElem.firstChild);

};
/**
 * translateAnyMethod is called when we know we have a method on a generic (any)
 * component.
*/
Blockly.Versioning.translateAnyMethod = function(blockElem) {
  // the type attribute is "instance_method"
  var blockType = blockElem.getAttribute('type');
  // any method block types look like: <block type="_any_ImageSprite_MoveTo" inline="false">
  var splitComponent=blockType.split('_');
  var componentType = splitComponent[2];
  var method=splitComponent[3];
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_method');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = goog.dom.createElement('mutation');
  mutationElement.setAttribute('method_name', method);
  mutationElement.setAttribute('component_type',componentType);
  mutationElement.setAttribute('is_generic','true');
  blockElem.insertBefore(mutationElement,blockElem.firstChild);

};
/**
 * translateComponentGet is called when we know we have a component get, e.g.
 * [Button1]
*/
Blockly.Versioning.translateComponentGet = function(blockElem) {
  // the type attribute is "instance_method"
  var blockType = blockElem.getAttribute('type');
  // method block types look like: <block type="TinyDB_component" x="132" y="72">
  var splitComponent=blockType.split('_');
  var instance = splitComponent[0];
  // if we got here we no splitComponent[1] is "component"
  // Paul has a function to convert instance to type
  var componentType=Blockly.Component.instanceNameToTypeName(instance);
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_component_block');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = goog.dom.createElement('mutation');
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('instance_name', instance);
  mutationElement.setAttribute('component_type',componentType);
  blockElem.insertBefore(mutationElement,blockElem.firstChild);

};

/**
 * translateSetGetProperty is called when we know we have a get or set on an instance
*/
Blockly.Versioning.translateSetGetProperty = function(blockElem) {
  // the type attribute is "instance_setproperty" or "component_getproperty"
  var blockType = blockElem.getAttribute('type');
  // set block look like: <block type="Button1_setproperty" x="132" y="72">
  var splitComponent=blockType.split('_');
  var instance = splitComponent[0];
  var type=splitComponent[1]; //setproperty or getproperty
  // Paul has a function to convert instance to type
  var componentType=Blockly.Component.instanceNameToTypeName(instance);
  // grab titles to find the particular property
  var titles= blockElem.getElementsByTagName('title');
  var propName='unknown';
  for (var i=0, len=titles.length; i<len; i++)
  {
    if (titles[i].getAttribute('name')=='PROP') {
      propName=titles[i].textContent;
      break;
    }
  }
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_set_get');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = blockElem.getElementsByTagName('mutation')[0];
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('instance_name', instance);
  mutationElement.setAttribute('property_name', propName);
  if (type=='setproperty')
    mutationElement.setAttribute('set_or_get', 'set');
  else
    mutationElement.setAttribute('set_or_get','get');
  mutationElement.setAttribute('component_type',componentType);
  mutationElement.setAttribute('is_generic','false');
  // old blocks had a 'yailtype' attribute in mutator, lets get rid of
  if (mutationElement.getAttribute('yailtype')!=null)
    mutationElement.removeAttribute('yailtype');
};

/**
 * translateComponentSetGetProperty is called when we know we have a get or set on a
 * generic component.
*/
Blockly.Versioning.translateComponentSetGetProperty = function(blockElem) {
  // the type attribute is "component_setproperty" or "component_getproperty"
  var blockType = blockElem.getAttribute('type');
  // set block look like: <block type="Button_setproperty" >
  var splitComponent=blockType.split('_');
  var type=splitComponent[1]; //setproperty or getproperty
  var componentType=splitComponent[0];
  // grab titles to find the particular property
  var titles= blockElem.getElementsByTagName('title');
  var propName='unknown';
  for (var i=0, len=titles.length; i<len; i++)
  {
    if (titles[i].getAttribute('name')=='PROP')
      propName=titles[i].text;
  }
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_set_get');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = blockElem.getElementsByTagName('mutation')[0];
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('property_name', propName);
  if (type=='setproperty')
    mutationElement.setAttribute('set_or_get', 'set');
  else
    mutationElement.setAttribute('set_or_get','get');
  mutationElement.setAttribute('component_type',componentType);
  mutationElement.setAttribute('is_generic','true');
  // old blocks had a 'yailtype' attribute in mutator, lets get rid of
  if (mutationElement.getAttribute('yailtype')!=null)
    mutationElement.removeAttribute('yailtype');
};