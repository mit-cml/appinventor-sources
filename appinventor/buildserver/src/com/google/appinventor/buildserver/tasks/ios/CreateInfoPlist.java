// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.BinaryPropertyListWriter;
import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.google.appinventor.buildserver.Project;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

@BuildType(ipa = true, asc = true)
public class CreateInfoPlist implements IosTask {
  private static final String DEFAULT_MESSAGE =
      "This feature is requested by a library but not used by this app.";
  private static final String PLAYER = "com.google.appinventor.components.runtime.Player";

  @Override
  public TaskResult execute(IosCompilerContext context) {
    File plist = new File(context.getPaths().getAppDir(), "Info.plist");
    NSDictionary root;
    try (FileInputStream in = new FileInputStream(plist)) {
      byte[] contents = IOUtils.toByteArray(in);
      if (contents[0] == '<') {
        root = (NSDictionary) PropertyListParser.parse(contents);
      } else {
        root = (NSDictionary) BinaryPropertyListParser.parse(contents);
      }
      final Project project = context.getProject();
      root.put("CFBundleName", project.getProjectName());
      root.put("CFBundleDisplayName", project.getAName());
      root.put("CFBundleIdentifier", context.getBundleId());
      root.put("CFBundleShortVersionString", project.getVName());
      root.put("CFBundleVersion", project.getVCode());
      root.put("NSBluetoothAlwaysUsageDescription",
          project.getProperty("NSBluetoothAlwaysUsageDescription", DEFAULT_MESSAGE));
      root.put("NSBluetoothPeripheralUsageDescription",
          project.getProperty("NSBluetoothPeripheralUsageDescription", DEFAULT_MESSAGE));
      root.put("NSContactsUsageDescription",
          project.getProperty("NSContactsUsageDescription", DEFAULT_MESSAGE));
      root.put("NSMicrophoneUsageDescription",
          project.getProperty("NSMicrophoneUsageDescription", DEFAULT_MESSAGE));
      root.put("NSCameraUsageDescription",
          project.getProperty("NSCameraUsageDescription", DEFAULT_MESSAGE));
      root.put("NSSpeechRecognitionUsageDescription",
          project.getProperty("NSSpeechRecognitionUsageDescription", DEFAULT_MESSAGE));
      root.put("NSLocationWhenInUseUsageDescription",
          project.getProperty("NSLocationWhenInUseUsageDescription", DEFAULT_MESSAGE));
      if (context.getCompTypes().contains(PLAYER)) {
        NSArray backgroundModes = new NSArray(new NSString("audio"));
        root.put("UIBackgroundModes", backgroundModes);
      }
    } catch (IOException | PropertyListFormatException | ParseException
             | ParserConfigurationException | SAXException e) {
      return TaskResult.generateError(e);
    }
    try (FileOutputStream out = new FileOutputStream(plist)) {
      BinaryPropertyListWriter.write(out, root);
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }
    return TaskResult.generateSuccess();
  }
}
