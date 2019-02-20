
To create English translation template:
1. Build App Inventor, this will output a OdeMessages_en.properties file under appengine/build/extras/ode
2. Run i18n.py combine
3. Upload the resulting translation_template_en.properties to Google Translation Toolkit

To split the translated file:
1. Download appropriate translated file from Google Translation Toolkit
2. Run i18n.py [translated_file].properties --lang [two character language code] --lang_name [full name of language]
3. Add the language to appengine/src/com/google/appinventor/client/languages.json
4. Add the language to appengine/src/com/google/appinventor/YaClient.xml
5. Add the language to blocklyeditor/src/language_switch.js
6. Add the language to blocklyeditor/ploverConfig.js

To add an existing translation to Google Translator Toolkit:
1. Build App App Inventor
2. Run i18n.py combine [language_code] where language_code is the case-sensitive identifier used by the OdeMessages file. Don't worry that the blocklyeditor file is lower case. The script will handle that.
3. Convert the out to tmx (I have been using http://converter.webtranslateit.com/)
4. Open the translation_template_[language_code].properties. The language code will be defaulted to en because the converter doesn't know what language you're using. Search-replace the language code with the correct one and save.
5. If it is not already done, convert translation_template_en.properties the same way.
6. Run i18n.py tmxcombine using the target language and english tmx files.
7. In Google Translator Toolkit, upload the combied tmx file as a Translation Memory.
8. Upload a new active translation using translation_template_en.properties and selecting the Translation Memory.
