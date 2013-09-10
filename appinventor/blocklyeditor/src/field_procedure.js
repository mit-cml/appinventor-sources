Blockly.FieldProcedure = {};
Blockly.AIProcedure = {};

Blockly.FieldProcedure.defaultValue = ["","none"]

Blockly.FieldProcedure.onChange = function(text) {
  var workspace = this.block.workspace;
  if(!this.block.editable){
    workspace = Blockly.Drawer.flyout_.workspace_;
    return;
  }

  if(text == "" || text != this.getValue()) {
    for(var i=0;this.block.getInput('ARG' + i) != null;i++){
      this.block.removeInput('ARG' + i);
    }
    //return;
  }
  this.setValue(text);
  var def = Blockly.Procedures.getDefinition(text, workspace);
  if(def) {
    if(def.paramIds_ == null){
      def.mutator.setVisible(true);
      def.mutator.shouldHide = true;
    }
    this.block.setProcedureParameters(def.arguments_, def.paramIds_,true);
  }
};


Blockly.AIProcedure.getProcedureNames = function(returnValue) {
  var topBlocks = Blockly.mainWorkspace.getTopBlocks();
  var procNameArray = [Blockly.FieldProcedure.defaultValue];
  for(var i=0;i<topBlocks.length;i++){
    var procName = topBlocks[i].getTitleValue('NAME');
    if(topBlocks[i].type == "procedures_defnoreturn" && !returnValue) {
      procNameArray.push([procName,procName]);
    } else if (topBlocks[i].type == "procedures_defreturn" && returnValue) {
      procNameArray.push([procName,procName]);
    }
  }
  if(procNameArray.length > 1 ){
    procNameArray.splice(0,1);
  }
  return procNameArray;

}


Blockly.AIProcedure.removeProcedureValues = function(name, workspace) {
  var blockArray = Blockly.mainWorkspace.getAllBlocks();
  for(var i=0;i<blockArray.length;i++){
    var block = blockArray[i];
    if(block.type == "procedures_callreturn" || block.type == "procedures_callnoreturn") {
      if(block.getTitleValue('PROCNAME') == name) {
        block.removeProcedureValue();
      }
    }
  }
}
