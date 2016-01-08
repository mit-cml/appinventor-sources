{
  // "This config file is used to compile the blockly code base."
  "id": "blockly-config",
  "paths": [],
  "inputs": [//'testalert.js'
    //blockly.js must come first
    '../lib/blockly/src/core/blockly.js',

    //msg/js/*.js has to come next (for constants)
    './src/msg/ko_kr/_messages.js',
    './src/msg/es_es/_messages.js',
    './src/msg/zh_tw/_messages.js',
    './src/msg/zh_cn/_messages.js',
    './src/msg/fr_fr/_messages.js',
    './src/msg/it_it/_messages.js',
    './src/msg/ru/_messages.js',
    './src/msg/sv/_messages.js',
    './src/msg/en/_messages.js',

    // Lyn's instrumentation code
    "./src/instrument.js",

    //the rest of the blockly .js files can come in any order

    '../lib/blockly/src/core/block.js',
    '../lib/blockly/src/core/blocks.js',
    '../lib/blockly/src/core/block_svg.js',
    //'../lib/blockly/src/core/blockly.js',
    '../lib/blockly/src/core/bubble.js',
    '../lib/blockly/src/core/comment.js',
    '../lib/blockly/src/core/connection.js',
    '../lib/blockly/src/core/contextmenu.js',
    '../lib/blockly/src/core/css.js',
    '../lib/blockly/src/core/field.js',
    '../lib/blockly/src/core/field_angle.js',
    '../lib/blockly/src/core/field_checkbox.js',
    '../lib/blockly/src/core/field_colour.js',
    '../lib/blockly/src/core/field_dropdown.js',
    '../lib/blockly/src/core/field_image.js',
    '../lib/blockly/src/core/field_label.js',
    '../lib/blockly/src/core/field_textinput.js',
    '../lib/blockly/src/core/field_variable.js',
    '../lib/blockly/src/core/flyout.js',
    '../lib/blockly/src/core/generator.js',
    '../lib/blockly/src/core/icon.js',
    '../lib/blockly/src/core/inject.js',
    '../lib/blockly/src/core/input.js',
    '../lib/blockly/src/core/msg.js',
    '../lib/blockly/src/core/mutator.js',
    '../lib/blockly/src/core/names.js',
    '../lib/blockly/src/core/procedures.js',
    '../lib/blockly/src/core/realtime.js',
    '../lib/blockly/src/core/realtime-client-utils.js',
    '../lib/blockly/src/core/scrollbar.js',
    //'../lib/blockly/src/core/toolbox.js',
    '../lib/blockly/src/core/tooltip.js',
    '../lib/blockly/src/core/trashcan.js',
    '../lib/blockly/src/core/utils.js',
    '../lib/blockly/src/core/variables.js',
    '../lib/blockly/src/core/warning.js',
    '../lib/blockly/src/core/widgetdiv.js',
    '../lib/blockly/src/core/workspace.js',
    '../lib/blockly/src/core/xml.js',
    '../lib/blockly/src/core/typeblock.js',

    //finally, include any of our own .js file in any orde
    "./src/blocklyeditor.js",
    "./src/blockColors.js",
    "./src/translation_properties.js",
    "./src/translation_events.js",
    "./src/translation_methods.js",
    "./src/translation_params.js",
    "./src/component.js",
    "./src/drawer.js",
    "./src/savefile.js",
    "./src/versioning.js",
    "./src/mutators.js",
    "./src/field_lexical_variable.js",
    "./src/errorIcon.js",
    "./src/warningHandler.js",
    "./src/field_procedure.js",
    "./src/field_textblockinput.js",
    "./src/warningIndicator.js",
    "./src/exportBlocksImage.js",
    "./src/flydown.js",
    "./src/field_flydown.js",
    "./src/field_parameter_flydown.js",
    "./src/field_global_flydown.js",
    "./src/field_procedure_flydown.js",
    "./src/nameSet.js",
    "./src/substitution.js",
    "./src/language_switch.js",

    // Dialog Utiltiy
    "./src/util.js",

    // backpack files
    "./src/backpack.js",
    "./src/backpackFlyout.js",

    //blocks files
    './src/blocks/control.js',
    './src/blocks/logic.js',
    './src/blocks/text.js',
    './src/blocks/lists.js',
    './src/blocks/math.js',
    './src/blocks/utilities.js',
    './src/blocks/procedures.js',
    './src/blocks/lexical-variables.js',
    './src/blocks/colors.js',
    './src/blocks/components.js',

    //generator files
    "./src/generators/yail.js",
    "./src/generators/yail/componentblock.js",
    "./src/generators/yail/lists.js",
    "./src/generators/yail/math.js",
    "./src/generators/yail/control.js",
    "./src/generators/yail/logic.js",
    "./src/generators/yail/text.js",
    "./src/generators/yail/colors.js",
    "./src/generators/yail/variables.js",
    "./src/generators/yail/procedures.js",

    // Repl
    "./src/replmgr.js"
    ],

  // This must be specified because datetimesymbols.js from the Closure Library
  // will be included, so when test-raw.html loads each input in RAW mode,
  // it is important that the proper charset be used.
  "output-charset": "UTF-8",
  "mode": "RAW",
//  "mode": "SIMPLE_OPTIMIZATIONS",
//  "mode" : "WHITESPACE_ONLY",
  "closure-library" : "../lib/closure-library/closure/goog",
  "output-file": "../build/blocklyeditor/blockly-all.js"
}
