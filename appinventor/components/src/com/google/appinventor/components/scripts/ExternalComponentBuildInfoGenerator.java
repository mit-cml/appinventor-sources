// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.BufferedWriter;
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
import java.util.Properties;
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
* Tool to generate component.build_info.json for extensions.
* And put the libraries used by the extension in the correct directory for packaging.
*
* @author mouha.oumar@gmail.com (Mouhamadou O. Sall)
*/

public class ExternalComponentBuildInfoGenerator {


  /**
  * The definitions of the arguments used by this script
  *
  * args[0]: the path to simple_component_build_info.json
  * args[1]: the path to external_components.txt
  * args[2]: the path to ExternalComponents folder
  * args[3]: the path to /build/classes/BuildServer/files
  * args[4]: the path to external componentsTemp directory
  * args[5]: the path to simple_component.json
  */
  public static void main(String[] args) throws IOException, ParseException, JSONException {
    JSONParser parser = new JSONParser();
    String simple_component_build_text = readFile(args[0], Charset.defaultCharset());
    Object simple_component_build_obj = parser.parse(simple_component_build_text);
    JSONArray simple_component_build_info_array = (JSONArray) simple_component_build_obj;
    ArrayList<String> components = fileToArray(args[1]);
    for (int i = 0; i < simple_component_build_info_array.size(); i++) {
      JSONObject component = (JSONObject)simple_component_build_info_array.get(i);
      String componentType = component.get("type").toString();
      JSONArray libraries = (JSONArray) component.get("libraries");
      String componentName = componentType.substring(componentType.lastIndexOf(".") + 1);
      String componentFileDirectory = args[2]+File.separator+ componentName+ File.separator+"files";
        if(components.contains(componentName)) {
            // Rename "libraries" to "externallibraries"
            // We will use "externallibraries" later in the build process so
            // we know which libraries we need to extract so they get "JarJar'd" into
            // AndroidRuntime.jar. The Buildserver looks at "libraries" so we need to
            // make sure it is an empty JSON array.
            component.put("externallibraries", libraries);
            component.put("libraries", new JSONArray());
            new File(componentFileDirectory).mkdirs();
            FileWriter file = new FileWriter(componentFileDirectory + File.separator + "component_build_info.json");
            try {
                file.write(component.toJSONString());
                System.out.println("Successfully created component_build_info.json for "+componentName);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                file.flush();
                file.close();
            }

          // Copying related libraries to a given extension into his files folder
          JSONArray libraryArray = (JSONArray)component.get("externallibraries");
          for(int j = 0; j<libraryArray.size();j++){
            Object library = libraryArray.get(j);
            copyFile(new File(args[3]+File.separator+library.toString()),
                    new File(componentFileDirectory+File.separator+library.toString()));
          }

          // Copying ComponentName.jar into his files folder
          copyFile(new File(args[4]+File.separator+componentName+".jar"),
                     new File(componentFileDirectory+File.separator+"AndroidRuntime.jar"));

          //Adding extension.properties
          String componentDirectory = args[2] + File.separator + componentName;
          StringBuilder sb = new StringBuilder();
          sb.append("type=external\n");
          BufferedWriter writer = new BufferedWriter(new FileWriter(new File(componentDirectory + File.separator + "extension.properties")));
          writer.write(sb.toString());
          writer.flush();
          writer.close();

        //Renaming folder accordingly
        File extensionDir = new File(args[2] + File.separator + componentName);
        File newExtensionDir = new File(args[2] + File.separator + componentType);
        extensionDir.renameTo(newExtensionDir);
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
  * Delete a directory
  *
  * Code posted on http://stackoverflow.com/questions/3775694/deleting-folder-from-java
  * @param directory the folder to be removed
  */
  public static boolean deleteDirectory(File directory) {
    if(directory.exists()){
      File[] files = directory.listFiles();
      if(null!=files){
        for(int i=0; i<files.length; i++) {
          if(files[i].isDirectory()) {
            deleteDirectory(files[i]);
          }
          else {
            files[i].delete();
          }
        }
      }
    }
    return(directory.delete());
  }

}
