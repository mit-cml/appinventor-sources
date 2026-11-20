// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2026 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

suite('Arrange Blocks', function() {
  test('sorts component references by instance name', function() {
    const container = document.createElement('div');
    container.style.cssText = 'width: 800px; height: 500px';
    document.body.appendChild(container);
    const workspace = Blockly.inject(container, {grid: {spacing: 20}});
    workspace.componentDb_ = new Blockly.ComponentDatabase();
    Blockly.common.setMainWorkspace(workspace);
    try {
      workspace.getComponentDatabase().addInstance('10', 'Button10', 'Button');
      workspace.getComponentDatabase().addInstance('2', 'Button2', 'Button');
      const button10 = blockFromMutation('component_component_block',
          '<mutation component_type="Button" instance_name="Button10" />');
      const button2 = blockFromMutation('component_component_block',
          '<mutation component_type="Button" instance_name="Button2" />');
      const anotherButton2 = blockFromMutation('component_component_block',
          '<mutation component_type="Button" instance_name="Button2" />');

      workspace.arranged_latest_position_ = AI.Blockly.BLKS_HORIZONTAL;
      Blockly.ContextMenuRegistry.registry.getItem('appinventor_sort_category').callback({workspace});

      chai.assert.isBelow(button2.getRelativeToSurfaceXY().x, button10.getRelativeToSurfaceXY().x);
      chai.assert.isBelow(anotherButton2.getRelativeToSurfaceXY().x,
          button10.getRelativeToSurfaceXY().x);
      chai.assert.isFalse(Boolean(Blockly.Events.getGroup()));
    } finally {
      Blockly.Events.setGroup(false);
      workspace.dispose();
      container.remove();
    }
  });
});
