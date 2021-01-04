// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package msg;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;


public class BlocklyTranslationGenerator {

  /**
   * The definitions of the arguments used by this script
   *
   * args[0]: the path to AI Blockly translation files
   * args[1]: the path to Blockly's translation files
   * args[2]: destination path
   */
  public static void main(String[] args) throws IOException, JSONException {

    File blockly_dir = new File(args[1]);
    File ai_dir = new File(args[0]);
    File blockly_english = new File(args[1].concat("/en.json"));
    File ai_english = new File(args[0].concat("/messages.json"));

    JSONObject blockly_en_json = new JSONObject(new String(Files.readAllBytes(Paths.get(blockly_english.getPath()))));
    // Omit the the old key prefix for the AI translation keys
    JSONObject ai_en_json = new JSONObject(new String(Files.readAllBytes(Paths.get(ai_english.getPath()))).replaceAll("Blockly.Msg.", ""));

    // All untranslated strings for a given language should default to English, so merge the Blockly and App Inventor
    // translations into one master english file that will be used in all the other translations.
    JSONObject merged_english = merge_string_json(new ArrayList<JSONObject>(Arrays.asList(blockly_en_json, ai_en_json)));
    FileUtils.writeStringToFile(new File(args[2].concat("/messages.json")), merged_english.toString());

    HashMap<String, String> blockly_files = new HashMap<String, String>();

    for (File f : blockly_dir.listFiles()) {
      if (f.isFile() && f.getName().endsWith(".json")) {
        // Blockly i18n filenames are of the form language_code.json, so extracting the language code is easy.
        String lang_code = FilenameUtils.getBaseName(f.getName());
        // We use different codes for Chinese variants than Blockly.
        if ("zh-hans".equals(lang_code)) {
          blockly_files.put("zh_CN", f.getPath());
        } else if ("zh-hant".equals(lang_code)) {
          blockly_files.put("zh_TW", f.getPath());
        } else if ("pt-br".equals(lang_code)) {
          blockly_files.put("pt_BR", f.getPath());
        } else {
          blockly_files.put(lang_code.replace("-", "_"), f.getPath());
        }
      }
    }

    for(File f : ai_dir.listFiles()) {
      if (f.isFile() && f.getName().contains("messages_") && f.getName().endsWith(".json") && !f.getName().equals("messages_default.json")) {
        String file_name = FilenameUtils.getBaseName(f.getName());
        ArrayList<JSONObject> json_to_merge = new ArrayList<JSONObject>();
        json_to_merge.add(merged_english);
        String lang_code = file_name.substring(file_name.indexOf("_") + 1);
        // If a Blockly has a translation for this language (and it should),
        if (blockly_files.containsKey(lang_code)) {
          json_to_merge.add(new JSONObject(new String(Files.readAllBytes(Paths.get(blockly_files.get(lang_code))))));
        }
        JSONObject ai_lang_json = new JSONObject(new String(Files.readAllBytes(Paths.get(f.getPath()))).replaceAll("Blockly.Msg.", ""));
        json_to_merge.add(ai_lang_json);
        JSONObject merged_language = merge_string_json(json_to_merge);
        FileUtils.writeStringToFile(new File(args[2].concat("/messages_").concat(lang_code).concat(".json")), merged_language.toString(2));
      }
    }
  }

  // Merge a list of JSON objects.
  // Objects are expected to be a flat list (no nested JSON) of string key and string value
  // JSON is merged in order, which means duplicate keys are overwritten. The key that occurss in
  // the last object wins. Object should be in the list in order of precedence, with the last entry
  // the highest priority.
  private static JSONObject merge_string_json(ArrayList<JSONObject> json_to_merge) {
    JSONObject merged_json = new JSONObject();
    for (JSONObject obj : json_to_merge) {
      Iterator it = obj.keys();
      while (it.hasNext()) {
        String key = (String) it.next();
        // Values that are not string are ignored.
        if (obj.get(key) instanceof String) {
          merged_json.put(key, obj.getString(key));
        }
      }
    }
    return merged_json;
  }
}
