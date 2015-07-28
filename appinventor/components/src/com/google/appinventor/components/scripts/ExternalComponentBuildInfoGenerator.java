// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
* Tool to generate externalComponent.build_info.json.
* And put the libraries used by the extensions in the correct directory.
*
* @author mouha.oumar@gmail.com (Mouhamadou O. Sall)
*/

public class ExternalComponentBuildInfoGenerator {


  public static void main(String[] args) throws IOException, ParseException, JSONException {

    /*
    * args[0]: the path to simple_component_build_info.json
    * args[1]: the external component's name
    * args[2]: the path to ExternalComponents folder
    */
    JSONParser parser = new JSONParser();
    String jsonText = readFile(args[0], Charset.defaultCharset());
    Object obj = parser.parse(jsonText);
    JSONArray array = (JSONArray) obj;
    for (int i = 0; i < array.size(); i++) {
        JSONObject component = (JSONObject) array.get(i);
        if (component.get("name").equals(args[1])) { //The external component we want to create the build_info.json
            new File(args[2]+File.separator+component.get("name")+"/files").mkdirs();
            FileWriter file = new FileWriter(args[2]+File.separator+ component.get("name") + "/files/" + component.get("name") + "_build_info.json");
            try {
                file.write(component.toJSONString());
                System.out.println("Successfully created " + component.get("name") + " build info JSON Object to File...");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                file.flush();
                file.close();
            }
        }
    }
  }

  private static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }
}
