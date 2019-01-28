Rough sketch of the process (as I remember it):

To combine for translation:
1. Run Blockly's js_to_json on the English messages file in blocklyeditor. This should create a message.json file.
2. Build App Inventor, this will output a OdeMessages_en.properties file under appengine/build/extras/ode
3. Run translate.java on messages.json to produce messages.properties
4. Run merge.py to combine the two properties files with prefixed keys (appengine. and blockseditor.)

To split the translated file:
1. Run split.py on the translated file to produce a messages.properties file and a messages.js file.
2. Add the language to appengine/src/com/google/appinventor/client/languages.json
3. Add the language to appengine/src/com/google/appinventor/YaClient.xml
