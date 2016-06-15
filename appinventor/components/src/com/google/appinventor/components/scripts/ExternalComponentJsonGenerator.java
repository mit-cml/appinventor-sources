// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
* Tool to generate component.json for extension.
*
* @author mouha.oumar@gmail.com (Mouhamadou O. Sall)
*/

public class ExternalComponentJsonGenerator {


  /**
  * The definitions of the arguments used by this script
  *
  * args[0]: the path to simple_component.json
  * args[1]: the path to external_components.txt
  * args[2]: the path to ExternalComponentAsset.dir: "${local.build.dir}/ExternalComponents"
  */
  public static void main(String[] args) throws IOException, ParseException, JSONException {
    JSONParser parser = new JSONParser();
    String simple_component_text = readFile(args[0],Charset.defaultCharset());
    Object simple_component_obj = parser.parse(simple_component_text);
    JSONArray simple_component_array = (JSONArray)simple_component_obj;
    ArrayList<String> components = fileToArray(args[1]);
    for(int i = 0; i<simple_component_array.size(); i++){
      JSONObject component = (JSONObject)simple_component_array.get(i);
      if(components.contains(component.get("name"))){  //TODO(Mos): Should test the external boolean here instead
        new File(args[2]+"/"+component.get("name").toString()).mkdirs();
        FileWriter file = new FileWriter(args[2]+"/"+component.get("name")+"/"+"component.json");
        try {
          file.write(component.toJSONString());
          System.out.println("Successfully created "+ component.get("name") +" JSON Object to File...");
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          file.flush();
          file.close();
        }
      }
    }
  }

  /**
  * Read a file and returns its content
  *
  * @param path the path of the file to be read
  * @param encoding the encoding system
  */
  private static String readFile(String path, Charset encoding) throws IOException{
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  /**
  * Read external_components.txt (a file containing the name of the extension)
  * and returns an ArrayList containing those components
  *
  * @param fileName the path of the file to be read
  */
  private static ArrayList<String> fileToArray(String fileName) throws FileNotFoundException{
    Scanner sc = new Scanner(new File(fileName));
    ArrayList<String> components = new ArrayList<String>();
    while (sc.hasNextLine()) {
      components.add(sc.nextLine());
    }
    return components;
  }
}
