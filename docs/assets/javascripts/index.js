// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
* Main Index file for rendering process.
* @author preetvadaliya.ict18@gmail.com (Preet Vadaliya)
*/

function renderBlock(block, workspaceElement) {
  block.initSvg();
  block.render();
  if (block.outputConnection != null) {
    block.moveBy(16, 8);
  } else {
    block.moveBy(8, 8);
  }
  workspaceElement.style.height = block.getHeightWidth()["height"] + 16 + "px";
}

// Render event blocks.
Array.from(document.querySelectorAll("[block-type = component_event]")).forEach(element => {
  let componentType = element.getAttribute("component-selector");
  let eventName = element.getAttribute("event-selector");
  let workspace = Blockly.inject(element.id, { scrollbars: false, readOnly: true });
  let block = workspace.newBlock('component_event');
  block.initComponent(componentType, eventName, false, true);
  renderBlock(block, element);
});

// Render method blocks.
Array.from(document.querySelectorAll("[block-type = component_method]")).forEach(element => {
  let componentType = element.getAttribute("component-selector");
  let methodName = element.getAttribute("method-selector");
  let workspace = Blockly.inject(element.id, { scrollbars: false, readOnly: true });
  let block = workspace.newBlock('component_method');
  block.initComponent(componentType, methodName, false);
  renderBlock(block, element);
});

// Render property set or get blocks.
Array.from(document.querySelectorAll("[block-type = component_set_get]")).forEach(element => {
  let componentType = element.getAttribute("component-selector");
  let propertyName = element.getAttribute("property-selector");
  let setOrGet = element.getAttribute("property-type");
  let workspace = Blockly.inject(element.id, { scrollbars: false, readOnly: true });
  let block = workspace.newBlock('component_set_get');
  block.initComponent(componentType, setOrGet, false, propertyName);
  if (setOrGet == "get") {
    element.style.marginBottom = "8px";
  }
  renderBlock(block, element);
});

// Render component blocks.
Array.from(document.querySelectorAll("[block-type = component_component_block]")).forEach(element => {
  let componentType = element.getAttribute("component-selector");
  let workspace = Blockly.inject(element.id, { scrollbars: false, readOnly: true });
  let block = workspace.newBlock('component_component_block');
  block.initComponent(componentType);
  renderBlock(block, element);
});

// Render default blocks.
Array.from(document.querySelectorAll("[type = ai-2-block]")).forEach(element => {
  let workspace = Blockly.inject(element.id, { scrollbars: false, readOnly: true });
  let block = workspace.newBlock(element.id);
  renderBlock(block, element);
});

Array.from(document.querySelectorAll("[type = ai-2-default-block]")).forEach(element => {
  let workspace = Blockly.inject(element.id, { scrollbars: false, readOnly: true });
  let blockXML = Blockly.Xml.textToDom(DEFAULT_BLOCKS[element.id]);
  blockXML = blockXML.firstElementChild;
  let block = Blockly.Xml.domToBlock(blockXML, workspace);
  renderBlock(block, element);
});
