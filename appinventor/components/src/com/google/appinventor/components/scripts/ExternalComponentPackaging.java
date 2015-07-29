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
* Tool to create the folder structure for packaging the external components.
*
* @author mouha.oumar@gmail.com (Mouhamadou O. Sall)
*/

public class ExternalComponentPackaging {

    public static void main(String[] args) throws IOException, ParseException, JSONException {
      /*
      * args[0]: the path to simple_component_build_info.json
      * args[1]: the path to external_components.txt
      * args[2]: the path to "${AndroidRuntime-class.dir}/${components.pkg}/runtime"
      * args[3]: the path to /build/classes/BuildServer/files
      * args[4]: the path to ExternalComponentTemp directory
      */
      JSONParser parser = new JSONParser();
      String jsonText = readFile(args[0], Charset.defaultCharset());
      Object obj = parser.parse(jsonText);
      JSONArray array = (JSONArray) obj;
      ArrayList<String> components = fileToArray(args[1]);
      for (int i = 0; i < array.size(); i++) {
        JSONObject component = (JSONObject) array.get(i);
        String componentClassPath = "com/google/appinventor/components/runtime"; //TODO(Mos): Is this fixed or variable ?
        String componentTempDirectory = args[4]+File.separator+ component.get("name");
        String componentClassPathDirectory = componentTempDirectory + File.separator+componentClassPath;
          if(components.contains(component.get("name"))) {
              new File(componentClassPathDirectory).mkdirs();
            // Copying related libraries to a given extension into his files folder  in (ExternalComponentTemp dir)
            JSONArray libraryArray = (JSONArray)component.get("libraries");
            for(int j = 0; j<libraryArray.size();j++){
              Object library = libraryArray.get(j);
              copyFile(new File(args[3]+File.separator+library.toString()),
                       new File(componentTempDirectory+File.separator+library.toString()));
            }

            // Copying related libraries to a given extension into his package Name folder in (ExternalComponentTemp dir)
            copyRelatedExternalClasses(new File(args[2]),component.get("name").toString(),componentClassPathDirectory);
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

    private static void copyRelatedExternalClasses(final File srcfolder, String externalComponentName,final String destPath) throws IOException {
          for (File fileEntry : srcfolder.listFiles()){
              if (fileEntry.getName().contains(externalComponentName)){
              System.out.println(fileEntry.toString()/*fileEntry.getName()*/);
              copyFile(fileEntry,new File (destPath+File.separator+fileEntry.getName()));
              }
          }
    }

}
