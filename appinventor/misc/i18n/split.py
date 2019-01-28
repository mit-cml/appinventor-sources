

blockly_header = """
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 Massachusetts Institute of Technology. All rights reserved.

'use strict';

goog.provide('AI.Blockly.Msg.tr');
goog.require('Blockly.Msg.tr');

Blockly.Msg.tr.switch_language_to_tr = {
  // Switch language to Turkish.
  category: '',
  helpUrl: '',
  init: function() {
    Blockly.Msg.tr.switch_blockly_language_to_tr.init();
"""

blockly_footer = """
  }
};
"""

def js_stringify(text):
    return "'" + text.replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r") + "'"

paletteItems = {
    'userInterfaceComponentPallette',
    'layoutComponentPallette',
    'mediaComponentPallette',
    'drawingAndAnimationComponentPallette',
    'mapsComponentPallette',
    'sensorComponentPallette',
    'socialComponentPallette',
    'storageComponentPallette',
    'connectivityComponentPallette',
    'legoMindstormsComponentPallette',
    'experimentalComponentPallette',
    'extensionComponentPallette'
}

with open('messages_tr.properties') as input:
    with open('appengine/src/com/google/appinventor/client/OdeMessages_tr.properties', 'w') as gae_output:
        with open('blocklyeditor/src/msg/tr/_messages.js', 'w') as blockly_output:
            blockly_output.write(blockly_header)
            description = None
            for line in input:
                if len(line) <= 2:
                    pass
                elif line[0] == '#':
                    if description is not None:
                        description += line
                    else:
                        description = line
                elif line.startswith('appengine.switchTo') or line.startswith('appengine.SwitchTo'):
                    pass
                elif line.startswith('appengine.'):
                    if description is not None:
                        gae_output.write(description)
                        description = None
                    line = line[len('appengine.'):]
                    parts = [part.strip() for part in line.split(' = ', 1)]
                    gae_output.write(parts[0])
                    gae_output.write(' = ')
                    if parts[0].endswith('Params') or parts[0].endswith('Properties') or \
                            parts[0].endswith('Methods') or parts[0].endswith('Events') or \
                            (parts[0].endswith('ComponentPallette') and
                             not parts[0].endswith('HelpStringComponentPallette') and
                             not parts[0] in paletteItems):
                        parts[1] = ''.join(parts[1].split())
                    gae_output.write(parts[1].replace("'", "''"))
                    gae_output.write('\n\n')
                else:
                    parts = [part.strip() for part in line[len('blockseditor.'):].split('=', 1)]
                    blockly_output.write('    Blockly.Msg.')
                    blockly_output.write(parts[0])
                    blockly_output.write(' = ')
                    blockly_output.write(js_stringify(parts[1]))
                    blockly_output.write(';\n')
            blockly_output.write(blockly_footer)
