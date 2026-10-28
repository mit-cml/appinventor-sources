suite('XML Serialization Tests', function() {
  setup(function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', false, false));
    initComponentTypes();
  });

  test('Load XML through versioning system', function() {
    var formJson = '{"YaVersion":"76","Source":"Form","Properties":{"$Name":"Screen1","$Type":"Form","$Version":"10","Uuid":"0","$Components":[{"$Name":"Button1","$Type":"Button","$Version":"5","Uuid":"-472538081"},{"$Name":"Camcorder1","$Type":"Camcorder","$Version":"1","Uuid":"-543625407"}]}}';
    var blocksContent = `<xml>
  <block type="Button1_Click" x="19" y="30">
    <title name="COMPONENT_SELECTOR">Button1</title>
    <statement name="DO">
      <block type="Camcorder1_RecordVideo">
        <title name="COMPONENT_SELECTOR">Camcorder1</title>
      </block>
    </statement>
  </block>
</xml>`;

    var workspace = Blockly.common.getMainWorkspace();
    // Process the form JSON to initialize component database before versioning
    processForm(formJson);
    // Use the versioning system to upgrade and load the XML
    Blockly.Versioning.upgrade(formJson, blocksContent, workspace);

    var blocks = workspace.getAllBlocks();
    chai.assert.isAtLeast(blocks.length, 1, 'Should have at least 1 block after versioning');

    // After versioning, Button1_Click should become component_event
    var eventBlock = blocks.find(b => b.type === 'component_event' || b.type === 'Button1_Click');
    chai.assert.isNotNull(eventBlock, 'Should have component event block');

    var doInput = eventBlock.getInput('DO');
    chai.assert.isNotNull(doInput, 'Event block should have DO input');
    chai.assert.isNotNull(doInput.connection.targetBlock(), 'DO input should have connected block');

    var connectedBlock = doInput.connection.targetBlock();
    chai.assert.isNotNull(connectedBlock, 'Should have connected block in DO statement');
  });

  test('Camcorder test - Complex legacy block conversion', function() {
    var formJson = '{"YaVersion":"76","Source":"Form","Properties":{"$Name":"Screen1","$Type":"Form","$Version":"10","Uuid":"0","$Components":[{"$Name":"Button1","$Type":"Button","$Version":"5","Uuid":"-472538081"},{"$Name":"VideoPlayer1","$Type":"VideoPlayer","$Version":"4","Uuid":"1438474778"},{"$Name":"Camcorder1","$Type":"Camcorder","$Version":"1","Uuid":"-543625407"}]}}';
    var blocksContent = `<xml>
  <block type="Camcorder1_AfterRecording" x="361" y="29">
    <title name="COMPONENT_SELECTOR">Camcorder1</title>
    <statement name="DO">
      <block type="VideoPlayer1_setproperty" inline="false">
        <mutation yailtype="text"></mutation>
        <title name="COMPONENT_SELECTOR">VideoPlayer1</title>
        <title name="PROP">Source</title>
        <value name="VALUE">
          <block type="lexical_variable_get">
            <title name="VAR">clip</title>
          </block>
        </value>
      </block>
    </statement>
  </block>
</xml>`;

    var workspace = Blockly.common.getMainWorkspace();
    processForm(formJson);
    Blockly.Versioning.upgrade(formJson, blocksContent, workspace);

    var blocks = workspace.getAllBlocks();
    chai.assert.isAtLeast(blocks.length, 2, 'Should have event block and property setter block');

    var eventBlock = blocks.find(b => b.type === 'component_event' || b.type.includes('AfterRecording'));
    chai.assert.isNotNull(eventBlock, 'Should have AfterRecording event block');

    var doInput = eventBlock.getInput('DO');
    chai.assert.isNotNull(doInput, 'Event should have DO statement');

    var propertyBlock = doInput.connection.targetBlock();
    chai.assert.isNotNull(propertyBlock, 'Should have connected property setter block');

    var valueInput = propertyBlock.getInput('VALUE');
    chai.assert.isNotNull(valueInput, 'Property setter should have VALUE input');
    chai.assert.isNotNull(valueInput.connection.targetBlock(), 'VALUE should have connected variable block');
  });

  test('Clock test - Nested component method calls', function() {
    var formJson = '{"YaVersion":"76","Source":"Form","Properties":{"$Name":"Screen1","$Type":"Form","$Version":"10","Uuid":"0","$Components":[{"$Name":"Button1","$Type":"Button","$Version":"5","Uuid":"-860797114"},{"$Name":"Label2","$Type":"Label","$Version":"2","Uuid":"-1040116711"},{"$Name":"Clock1","$Type":"Clock","$Version":"1","Uuid":"1480433900"}]}}';
    var blocksContent = `<xml>
  <block type="Button1_Click" x="-26" y="-1">
    <title name="COMPONENT_SELECTOR">Button1</title>
    <statement name="DO">
      <block type="Label2_setproperty" inline="false">
        <mutation yailtype="text"></mutation>
        <title name="COMPONENT_SELECTOR">Label2</title>
        <title name="PROP">Text</title>
        <value name="VALUE">
          <block type="Clock1_FormatTime" inline="false">
            <title name="COMPONENT_SELECTOR">Clock1</title>
            <value name="ARG0">
              <block type="Clock1_Now">
                <title name="COMPONENT_SELECTOR">Clock1</title>
              </block>
            </value>
          </block>
        </value>
      </block>
    </statement>
  </block>
</xml>`;

    var workspace = Blockly.common.getMainWorkspace();
    processForm(formJson);
    Blockly.Versioning.upgrade(formJson, blocksContent, workspace);

    var blocks = workspace.getAllBlocks();
    chai.assert.isAtLeast(blocks.length, 3, 'Should have event, property setter, and method blocks');

    var eventBlock = blocks.find(b => b.type === 'component_event' || b.type === 'Button1_Click');
    chai.assert.isNotNull(eventBlock, 'Should have Button1_Click event block');

    var propertyBlock = eventBlock.getInput('DO').connection.targetBlock();
    chai.assert.isNotNull(propertyBlock, 'Should have Label2_setproperty block');

    var formatTimeBlock = propertyBlock.getInput('VALUE').connection.targetBlock();
    chai.assert.isNotNull(formatTimeBlock, 'Should have Clock1_FormatTime block');

    var nowBlock = formatTimeBlock.getInput('ARG0').connection.targetBlock();
    chai.assert.isNotNull(nowBlock, 'Should have Clock1_Now block nested inside FormatTime');
  });

  test('Factorial test - Procedures with component blocks', function() {
    var formJson = '{"YaVersion":"80","Source":"Form","Properties":{"$Name":"Screen1","$Type":"Form","$Version":"11","Uuid":"0","$Components":[{"$Name":"Button1","$Type":"Button","$Version":"5","Uuid":"597056068"},{"$Name":"TextBox1","$Type":"TextBox","$Version":"4","Uuid":"1173731358"},{"$Name":"Label1","$Type":"Label","$Version":"2","Uuid":"-2024760173"}]}}';
    var blocksContent = `<xml xmlns="http://www.w3.org/1999/xhtml">
  <block type="component_event" x="39" y="12">
    <mutation component_type="Button" instance_name="Button1" event_name="Click"></mutation>
    <title name="COMPONENT_SELECTOR">Button1</title>
    <statement name="DO">
      <block type="component_set_get" inline="false">
        <mutation component_type="Label" set_or_get="set" property_name="Text" is_generic="false" instance_name="Label1"></mutation>
        <title name="COMPONENT_SELECTOR">Label1</title>
        <title name="PROP">Text</title>
        <value name="VALUE">
          <block type="procedures_callreturn" inline="false">
            <mutation name="factorial">
              <arg name="x"></arg>
            </mutation>
            <title name="PROCNAME">factorial</title>
            <value name="ARG0">
              <block type="component_set_get">
                <mutation component_type="TextBox" set_or_get="get" property_name="Text" is_generic="false" instance_name="TextBox1"></mutation>
                <title name="COMPONENT_SELECTOR">TextBox1</title>
                <title name="PROP">Text</title>
              </block>
            </value>
          </block>
        </value>
      </block>
    </statement>
  </block>
</xml>`;

    var workspace = Blockly.common.getMainWorkspace();
    processForm(formJson);
    Blockly.Versioning.upgrade(formJson, blocksContent, workspace);

    var blocks = workspace.getAllBlocks();
    chai.assert.isAtLeast(blocks.length, 4, 'Should have event, property setter, procedure call, and getter blocks');

    var eventBlock = blocks.find(b => b.type === 'component_event');
    chai.assert.isNotNull(eventBlock, 'Should have component_event block');

    var setterBlock = eventBlock.getInput('DO').connection.targetBlock();
    chai.assert.equal(setterBlock.type, 'component_set_get', 'Should have component_set_get block');

    var procedureBlock = setterBlock.getInput('VALUE').connection.targetBlock();
    chai.assert.equal(procedureBlock.type, 'procedures_callreturn', 'Should have procedure call block');

    var getterBlock = procedureBlock.getInput('ARG0').connection.targetBlock();
    chai.assert.equal(getterBlock.type, 'component_set_get', 'Should have TextBox getter block');
  });

  test('Component property chains with mutations', function() {
    var formJson = '{"YaVersion":"76","Source":"Form","Properties":{"$Name":"Screen1","$Type":"Form","$Version":"10","Uuid":"0","$Components":[{"$Name":"VideoPlayer1","$Type":"VideoPlayer","$Version":"4","Uuid":"1438474778"}]}}';
    var blocksContent = `<xml>
  <block type="VideoPlayer1_setproperty" inline="false">
    <mutation yailtype="boolean"></mutation>
    <title name="COMPONENT_SELECTOR">VideoPlayer1</title>
    <title name="PROP">Visible</title>
    <value name="VALUE">
      <block type="logic_boolean">
        <title name="BOOL">TRUE</title>
      </block>
    </value>
    <next>
      <block type="VideoPlayer1_setproperty" inline="false">
        <mutation yailtype="text"></mutation>
        <title name="COMPONENT_SELECTOR">VideoPlayer1</title>
        <title name="PROP">Source</title>
        <value name="VALUE">
          <block type="text">
            <title name="TEXT">test.mp4</title>
          </block>
        </value>
      </block>
    </next>
  </block>
</xml>`;

    var workspace = Blockly.common.getMainWorkspace();
    processForm(formJson);
    Blockly.Versioning.upgrade(formJson, blocksContent, workspace);

    var blocks = workspace.getAllBlocks();
    chai.assert.isAtLeast(blocks.length, 2, 'Should have chained property setter blocks');

    var firstBlock = blocks.find(b => b.type.includes('setproperty') || b.type === 'component_set_get');
    chai.assert.isNotNull(firstBlock, 'Should have first property setter block');

    var nextBlock = firstBlock.getNextBlock();
    chai.assert.isNotNull(nextBlock, 'Should have second property setter block connected via next');

    var firstValueBlock = firstBlock.getInput('VALUE').connection.targetBlock();
    chai.assert.equal(firstValueBlock.type, 'logic_boolean', 'First value should be boolean block');

    var secondValueBlock = nextBlock.getInput('VALUE').connection.targetBlock();
    chai.assert.equal(secondValueBlock.type, 'text', 'Second value should be text block');
  });
});
