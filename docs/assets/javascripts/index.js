let options = {
  scrollbars: false,
  readOnly: true,
};

function renderBlock(blockType, workspaceId, options, componentInfo, workspaceElement) {
  let workspace = Blockly.inject(workspaceId, options);
  if (componentInfo != null) {
    Blockly.Blocks[blockType].initComponent(componentInfo);
    if (componentInfo[1] == "get") {
      workspaceElement.style.marginBottom = "8px";
    }
  }
  let block = workspace.newBlock(blockType);
  block.initSvg();
  block.render();
  block.moveBy(8, 8);
  workspaceElement.style.height = block.getHeightWidth()["height"] + 16 + "px";
  Blockly.svgResize(workspace);
}

Array.from(document.querySelectorAll("[block-type = component_method]")).forEach(element => {
  let componentInfo = [];
  componentInfo.push(element.getAttribute("component-selector"));
  componentInfo.push(element.getAttribute("method-selector"));
  componentInfo.push(element.getAttribute("method-params").split("-"));
  componentInfo.push(element.getAttribute("return-type"));
  renderBlock("component_method", element.id, {scrollbars: false,readOnly: true}, componentInfo, element);
});

Array.from(document.querySelectorAll("[block-type = component_set_get]")).forEach(element => {
  let componentInfo = [];
  componentInfo.push(element.getAttribute("component-selector"));
  componentInfo.push(element.getAttribute("property-type"));
  componentInfo.push(element.getAttribute("property-selector"));
  renderBlock("component_set_get", element.id, {scrollbars: false,readOnly: true}, componentInfo, element);
});

Array.from(document.querySelectorAll("[block-type = component_component_block]")).forEach(element => {
  let componentInfo = [];
  componentInfo.push(element.getAttribute("component-selector"));
  renderBlock("component_component_block", element.id, {scrollbars: false,readOnly: true}, componentInfo, element);
});

Array.from(document.querySelectorAll("[block-type = component_event]")).forEach(element => {
  let componentInfo = [];
  componentInfo.push(element.getAttribute("component-selector"));
  componentInfo.push(element.getAttribute("event-selector"));
  componentInfo.push(element.getAttribute("event-params").split("-"));
  renderBlock("component_event", element.id, {scrollbars: false,readOnly: true}, componentInfo, element);
});

Array.from(document.querySelectorAll("[type = ai-2-block]")).forEach(element => {
  renderBlock(element.id, element.id, {scrollbars: false,readOnly: true}, null, element);
});