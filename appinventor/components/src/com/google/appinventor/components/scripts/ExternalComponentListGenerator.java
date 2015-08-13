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
import java.util.*;
import java.io.BufferedWriter;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
* Tool to generate the list of the external component.
*
* @author mouha.oumar@gmail.com (Mouhamadou O. Sall)
*/

public class ExternalComponentListGenerator {

  /*
  * args[0]: the path to simple_component_build_info.json
  * args[1]: the path to ExternalComponents folder
  */
  public static void main(String[] args) throws IOException, ParseException, JSONException {
    JSONParser parser = new JSONParser();
    String jsonText = readFile(args[0],Charset.defaultCharset());
    Object obj = parser.parse(jsonText);
    JSONArray array = (JSONArray)obj;
    ArrayList<String> externalComponents = new ArrayList<String>();
    for(int i = 0; i<array.size();i++){
      JSONObject component = (JSONObject)array.get(i);
      if(component.get("categoryString").toString().equals("EXTENSION")){  // TODO(Mos): Should test the external boolean here instead ?
         externalComponents.add(component.get("name").toString());
       }
    }
   write(args[1]+"/"+"external_components.txt",externalComponents);
  }

  public static void write (String filename, ArrayList<String> components) throws IOException{
    BufferedWriter outputWriter = null;
    outputWriter = new BufferedWriter(new FileWriter(filename));
    for (int j = 0; j < components.size(); j++) {
      outputWriter.write(components.get(j));
      outputWriter.newLine();
    }
    outputWriter.flush();
    outputWriter.close();
  }

  private static String readFile(String path, Charset encoding) throws IOException{
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      return new String(encoded, encoding);
  }
}
