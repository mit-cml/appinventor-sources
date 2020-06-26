if (!document.getElementById('#blocklyDiv')) {
  var div = document.createElement('div');
  div.setAttribute('id', 'blocklyDiv');
  document.body.appendChild(div);
}

goog.require('AI.Blockly.BlocklyEditor');
