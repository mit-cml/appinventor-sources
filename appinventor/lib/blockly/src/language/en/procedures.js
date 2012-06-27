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
    this.appendTitle(new Blockly.FieldTextInput(name, Blockly.Procedures.rename), 'NAME');
    this.appendInput('do', Blockly.NEXT_STATEMENT, 'STACK');
    this.setTooltip('A procedure with no return value.');
  },
  destroy: function() {
    var name = this.getTitleText('NAME');
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
    return [this.getTitleText('NAME'), false];
  }
};

Blockly.Language.procedures_defreturn = {
  // Define a procedure with a return value.
  category: null,  // Procedures are handled specially.
  helpUrl: 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29',
  init: function() {
    this.setColour(290);
    var name = Blockly.Procedures.findLegalName('procedure', this);
    this.appendTitle(new Blockly.FieldTextInput(name, Blockly.Procedures.rename), 'NAME');
    this.appendInput('do', Blockly.NEXT_STATEMENT, 'STACK');
    this.appendInput('return', Blockly.INPUT_VALUE, 'RETURN');
    //this.setMutator(new Blockly.Mutator(this, ['procedures_mutatorparam']));
    this.setTooltip('A procedure with a return value.');
  },
  destroy: Blockly.Language.procedures_defnoreturn.destroy,
  getProcedureDef: function() {
    // Return the name of the defined procedure
    // and that it does not have a return value.
    return [this.getTitleText('NAME'), true];
  }
};

Blockly.Language.procedures_callnoreturn = {
  // Call a procedure with no return value.
  category: null,  // Procedures are handled specially.
  helpUrl: 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29',
  init: function() {
    this.setColour(290);
    this.appendTitle('call');
    this.appendTitle('procedure', 'NAME');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Call a procedure with no return value.');
  },
  getProcedureCall: function() {
    return this.getTitleText('NAME');
  },
  renameProcedure: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getTitleText('NAME'))) {
      this.setTitleText(newName, 'NAME');
    }
  },
  mutationToDom: function(workspace) {
    // Save the name.
    var container = document.createElement('mutation');
    container.setAttribute('name', this.getTitleText('NAME'));
    return container;
  },
  domToMutation: function(container) {
    // Restore the name.
    var name = container.getAttribute('name');
    this.setTitleText(name, 'NAME');
  }
};

Blockly.Language.procedures_callreturn = {
  // Call a procedure with a return value.
  category: null,  // Procedures are handled specially.
  helpUrl: 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29',
  init: function() {
    this.setColour(290);
    this.appendTitle('call');
    this.appendTitle('procedure', 'NAME');
    this.setOutput(true);
    this.setTooltip('Call a procedure with a return value.');
  },
  getProcedureCall: Blockly.Language.procedures_callnoreturn.getProcedureCall,
  renameProcedure: Blockly.Language.procedures_callnoreturn.renameProcedure,
  mutationToDom: Blockly.Language.procedures_callnoreturn.mutationToDom,
  domToMutation: Blockly.Language.procedures_callnoreturn.domToMutation
};
