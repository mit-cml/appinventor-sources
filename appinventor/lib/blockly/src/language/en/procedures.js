/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/google-blockly/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Procedure blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) {
  Blockly.Language = {};
}

Blockly.Language.procedures_defnoreturn = {
  // Define a procedure with no return value.
  category: null,  // Procedures are handled specially.
  helpUrl: 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29',
  init: function() {
    this.setColour(290);
    var name = Blockly.Procedures.findLegalName('procedure', this);
    this.addTitle(new Blockly.FieldTextInput(name, Blockly.Procedures.rename));
    this.addInput('do', '', Blockly.NEXT_STATEMENT);
    this.setTooltip('A procedure with no return value.');
  },
  destroy: function() {
    var name = this.getTitleText(0);
    var editable = this.editable;
    var workspace = this.workspace;
    // Call parent's destructor.
    Blockly.Block.prototype.destroy.call(this);
    if (this.editable) {
      // Destroy any callers.
      Blockly.Procedures.destroyCallers(name, workspace);
    }
  },
  getProcedureDef: function() {
    // Return the name of the defined procedure
    // and that it does not have a return value.
    return [this.getTitleText(0), false];
  }
};

Blockly.Language.procedures_defreturn = {
  // Define a procedure with a return value.
  category: null,  // Procedures are handled specially.
  helpUrl: 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29',
  init: function() {
    this.setColour(290);
    var name = Blockly.Procedures.findLegalName('procedure', this);
    this.addTitle(new Blockly.FieldTextInput(name, Blockly.Procedures.rename));
    this.addInput('do', '', Blockly.NEXT_STATEMENT);
    this.addInput('return', '', Blockly.INPUT_VALUE);
    //this.setMutator(new Blockly.Mutator(this, ['procedures_mutatorparam']));
    this.setTooltip('A procedure with a return value.');
  },
  destroy: Blockly.Language.procedures_defnoreturn.destroy,
  getProcedureDef: function() {
    // Return the name of the defined procedure
    // and that it does not have a return value.
    return [this.getTitleText(0), true];
  }
};

Blockly.Language.procedures_callnoreturn = {
  // Call a procedure with no return value.
  category: null,  // Procedures are handled specially.
  helpUrl: 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29',
  init: function() {
    this.setColour(290);
    this.addTitle('call');
    this.addTitle('procedure');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Call a procedure with no return value.');
  },
  getProcedureCall: function() {
    return this.getTitleText(1);
  },
  renameProcedure: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getTitleText(1))) {
      this.setTitleText(newName, 1);
    }
  },
  mutationToDom: function(workspace) {
    // Save the name.
    var container = document.createElement('mutation');
    container.setAttribute('name', this.getTitleText(1));
    return container;
  },
  domToMutation: function(container) {
    // Restore the name.
    var name = container.getAttribute('name')
    this.setTitleText(name, 1);
  }
};

Blockly.Language.procedures_callreturn = {
  // Call a procedure with a return value.
  category: null,  // Procedures are handled specially.
  helpUrl: 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29',
  init: function() {
    this.setColour(290);
    this.addTitle('call');
    this.addTitle('procedure');
    this.setOutput(true);
    this.setTooltip('Call a procedure with a return value.');
  },
  getProcedureCall: Blockly.Language.procedures_callnoreturn.getProcedureCall,
  renameProcedure: Blockly.Language.procedures_callnoreturn.renameProcedure,
  mutationToDom: Blockly.Language.procedures_callnoreturn.mutationToDom,
  domToMutation: Blockly.Language.procedures_callnoreturn.domToMutation
};
