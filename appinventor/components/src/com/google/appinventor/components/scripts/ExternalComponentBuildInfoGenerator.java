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
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.*;
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
    * args[1]: the path to external_components.txt
    * args[2]: the path to ExternalComponents folder
    * args[3]: the path to /build/classes/BuildServer/files
    * args[4]: the path to external componentsTemp directory
    * args[5]: the path to simple_component.json
    */
    JSONParser parser = new JSONParser();
    String jsonText = readFile(args[0], Charset.defaultCharset());
    Object obj = parser.parse(jsonText);
    JSONArray array = (JSONArray) obj;
    ArrayList<String> components = fileToArray(args[1]);
    for (int i = 0; i < array.size(); i++) {
      JSONObject component = (JSONObject) array.get(i);
      String componentFileDirectory = args[2]+File.separator+ component.get("name") + File.separator+"files";
      String componentType = component.get("type").toString();
      String componentName = componentType.substring(componentType.lastIndexOf(".") + 1);
        if(components.contains(componentName)) {
            new File(componentFileDirectory).mkdirs();
            FileWriter file = new FileWriter(componentFileDirectory + File.separator + "component_build_info.json");
            try {
                file.write(component.toJSONString());
                System.out.println("Successfully created build_info.json for "+component.get("name"));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                file.flush();
                file.close();
            }

          // Copying related libraries to a given extension into his files folder
          JSONArray libraryArray = (JSONArray)component.get("libraries");
          for(int j = 0; j<libraryArray.size();j++){
            Object library = libraryArray.get(j);
            copyFile(new File(args[3]+File.separator+library.toString()),
                     new File(componentFileDirectory+File.separator+library.toString()));
          }

          // Copying ComponentName.jar into his files folder
            copyFile(new File(args[4]+File.separator+component.get("name")+".jar"),
                     new File(componentFileDirectory+File.separator+"AndroidRuntime.jar"));
        }
    }
      // Renaming folder accordingly
      String simple_component_text = readFile(args[5], Charset.defaultCharset());
      Object simple_component_obj = parser.parse(simple_component_text);
      JSONArray simple_component_array = (JSONArray) simple_component_obj;
      for (int i = 0; i < simple_component_array.size(); i++) {
        JSONObject component = (JSONObject) simple_component_array.get(i);
        if(components.contains(component.get("name"))) {
            File extensionDir = new File(args[2]+File.separator + component.get("name"));
            File newExtensionDir = new File(args[2]+File.separator + component.get("type"));
            extensionDir.renameTo(newExtensionDir);
        }
      }
  }

  private static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  private static ArrayList<String> fileToArray(String fileName) throws FileNotFoundException{
    Scanner sc = new Scanner(new File(fileName));
    ArrayList<String> components = new ArrayList<String>();
    while (sc.hasNextLine()) {
      components.add(sc.nextLine());
    }
    return components;
  }

  private static void copyFile(File source, File dest) throws IOException {
      InputStream is = null;
      OutputStream os = null;
      try {
          is = new FileInputStream(source);
          os = new FileOutputStream(dest);
          byte[] buffer = new byte[1024];
          int length;
          while ((length = is.read(buffer)) > 0) {
              os.write(buffer, 0, length);
          }
      } finally {
          is.close();
          os.close();
      }
  }

}
