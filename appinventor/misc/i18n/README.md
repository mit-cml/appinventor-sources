
To combine to create translation template:
1. Build App Inventor, this will output a OdeMessages_en.properties file under appengine/build/extras/ode
2. Run i18n.py combine
3. Upload the resulting translation_template.properties to Google Translation Toolkit

To split the translated file:
1. Download appropriate translated faile from Google Translation Toolkit
2. Run i18n.py [translated_file].properties --lang [two character language code] --lang_name [full name of language]
3. Add the language to appengine/src/com/google/appinventor/client/languages.json
4. Add the language to appengine/src/com/google/appinventor/YaClient.xml
5. Add the language to blocklyeditor/src/language_switch.js
6. Add the language to blocklyeditor/ploverConfig.js
