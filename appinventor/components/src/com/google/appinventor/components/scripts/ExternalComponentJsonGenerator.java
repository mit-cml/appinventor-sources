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

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
* Tool to generate external component descriptors JSON.
*
* @author mouha.oumar@gmail.com (Mouhamadou O. Sall)
*/

public class ExternalComponentJsonGenerator {


  public static void main(String[] args) throws IOException, ParseException, JSONException {
    JSONParser parser = new JSONParser();
    String jsonText = readFile(args[0],Charset.defaultCharset());
    Object obj = parser.parse(jsonText);
    JSONArray array = (JSONArray)obj;
    for(int i = 0; i<array.size();i++){
      JSONObject component = (JSONObject)array.get(i);
      if(component.get("categoryString").equals("EXTERNAL")){  // Should test the external boolean here instead
        FileWriter file = new FileWriter(component.get("name")+".json");
        try {
          file.write(component.toJSONString());
          System.out.println("Successfully Copied "+ component.get("name") +" JSON Object to File...");
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          file.flush();
          file.close();
        }
      }
    }
  }

private static String readFile(String path, Charset encoding) throws IOException{
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }
}
