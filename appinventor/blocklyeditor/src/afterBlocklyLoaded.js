
//Everything that used to be in the init function on blocklyframe
if (Blockly['ReplState'] == undefined)
  Blockly.ReplState = new Blockly.ReplStateObj();
var formName = window.location.hash.substr(1);
Blockly.BlocklyEditor.startup(document.body, formName);
if (!window.parent.Blocklies) {
  // We keep a set of Blockly objects indexed by name
  // of the form for which they are the blocks editor
  window.parent.Blocklies = {};
  window.parent.ReplState = new Blockly.ReplStateObj(); // There should be one of these for the whole system
}
window.parent.Blocklies[formName] = Blockly;
Blockly.ReplMgr.ReplState = window.parent.ReplState;
Blockly.ReplMgr.formName = formName; <!-- So we can tell the AssetManager which form we are on. -->
window.parent.BlocklyPanel_initBlocksArea(formName);
window.blocklyLoaded = true;
window.parent.BlocklyPanel_afterBlocklyInit(formName);