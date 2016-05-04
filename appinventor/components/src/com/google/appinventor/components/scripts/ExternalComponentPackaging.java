// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
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
* Tool to create the folder structure for packaging the extensions.
*
* @author mouha.oumar@gmail.com (Mouhamadou O. Sall)
*/

public class ExternalComponentPackaging {

  /**
  * The definitions of the arguments used by this script
  *
  * args[0]: the path to simple_component_build_info.json
  * args[1]: the path to external_components.txt
  * args[2]: the path to "${AndroidRuntime-class.dir}"
  * args[3]: the path to /build/classes/BuildServer/files
  * args[4]: the path to ExternalComponentTemp directory
  * args[5]: the path to simple_component.json
  */
  public static void main(String[] args) throws IOException, ParseException, JSONException {
    JSONParser parser = new JSONParser();
    ArrayList<String> components = fileToArray(args[1]);

    // Copying related libraries to a given extension into his files folder in (ExternalComponentTemp dir)
    String simple_component_build_info_text = readFile(args[0], Charset.defaultCharset());
    Object simple_component_build_info_obj = parser.parse(simple_component_build_info_text);
    JSONArray simple_component_build_info_array = (JSONArray) simple_component_build_info_obj;
    for (int i = 0; i < simple_component_build_info_array.size(); i++) {
      JSONObject component = (JSONObject) simple_component_build_info_array.get(i);
      String componentType = component.get("type").toString();
      String componentName = componentType.substring(componentType.lastIndexOf(".") + 1);
      if(components.contains(componentName)) {
        String componentTempDirectory = args[4]+File.separator+ componentName;
        new File(componentTempDirectory).mkdirs();
        JSONArray libraryArray = (JSONArray)component.get("libraries");
        for(int j = 0; j<libraryArray.size();j++){
          Object library = libraryArray.get(j);
            copyFile(new File(args[3]+File.separator+library.toString()),
            new File(componentTempDirectory+File.separator+library.toString()));
        }
      }
    }

    // Copying related compiled files to a given extension into his package folder in (ExternalComponentTemp dir)
    String simple_component_text = readFile(args[5], Charset.defaultCharset());
    Object simple_component_obj = parser.parse(simple_component_text);
    JSONArray simple_component_array = (JSONArray) simple_component_obj;
    for (int i = 0; i < simple_component_array.size(); i++) {
      JSONObject component = (JSONObject) simple_component_array.get(i);
      if(components.contains(component.get("name"))) {
        String componentClassPath = component.get("type").toString();
        componentClassPath = componentClassPath.substring(0,componentClassPath.lastIndexOf(".")).replace('.','/');
        String componentTempDirectory = args[4]+File.separator+ component.get("name");
        String componentClassPathDirectory = componentTempDirectory + File.separator+componentClassPath;
        new File(componentClassPathDirectory).mkdirs();
        copyRelatedExternalClasses(new File(args[2]),component.get("name").toString(),componentClassPathDirectory);
      }
    }
  }


  /**
  * Read a file and returns its content
  *
  * @param path the path of the file to be read
  * @param encoding the encoding system
  */
  private static String readFile(String path, Charset encoding) throws IOException {
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

  /**
  * Copy a file from a given source to a given destination
  *
  * @param source the file to be copied
  * @param dest where the file will be copied
  */
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

  /**
  * Copy a compiled classes related to a given extension in his package folder
  *
  * @param srcfolder the folder in which to check compiled classes
  * @param externalComponentName the name of the extension
  * @param destPath where the compiled classes will be copied
  */
  private static void copyRelatedExternalClasses(final File srcfolder, String externalComponentName, final String destPath) throws IOException {
    for (File fileEntry : srcfolder.listFiles()){
      if (fileEntry.isFile()) {
        if (fileEntry.getName().startsWith(externalComponentName)){
          System.out.println(fileEntry.toString());
          copyFile(fileEntry,new File (destPath+File.separator+fileEntry.getName()));
        }
      } else if (fileEntry.isDirectory()) {
        copyRelatedExternalClasses(new File(fileEntry.getAbsolutePath()),externalComponentName, destPath);
      }
    }
  }

}
