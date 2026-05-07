// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import com.google.appinventor.buildserver.util.ImageUtil;
import com.google.appinventor.buildserver.util.ImageUtil.ResizeRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

@BuildType(ipa = true)
public class CreateManifest implements IosTask {
  /*
  <?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
        <key>items</key>
        <array>
                <dict>
                        <key>assets</key>
                        <array>
                                <dict>
                                        <key>kind</key>
                                        <string>software-package</string>
                                        <key>url</key>
                                        <string>https://ai2-buildserver-nightly-ewp.csail.mit.edu/ios-companion/0.9.5-dev/MITAI2Companion.ipa</string>
                                </dict>
                                <dict>
                                        <key>kind</key>
                                        <string>display-image</string>
                                        <key>url</key>
                                        <string>https://ai2-buildserver-nightly-ewp.csail.mit.edu/ios-companion/static/icon.57x57.png</string>
                                </dict>
                                <dict>
                                        <key>kind</key>
                                        <string>full-size-image</string>
                                        <key>url</key>
                                        <string>https://ai2-buildserver-nightly-ewp.csail.mit.edu/ios-companion/static/icon.512x512.png</string>
                                </dict>
                        </array>
                        <key>metadata</key>
                        <dict>
                                <key>bundle-identifier</key>
                                <string>com.evanpatton.aicompanion4</string>
                                <key>bundle-version</key>
                                <string>0.9</string>
                                <key>kind</key>
                                <string>software</string>
                                <key>platform-identifier</key>
                                <string>com.apple.platform.iphoneos</string>
                                <key>title</key>
                                <string>MIT App Inventor</string>
                        </dict>
                </dict>
        </array>
</dict>
</plist>
   */

  @Override
  public TaskResult execute(IosCompilerContext context) {
    File smallIcon = new File(context.getPaths().getBuildDir(), "small.png");
    File largeIcon = new File(context.getPaths().getBuildDir(), "large.png");
    URL appIcon = context.getAppIcon();
    try {
      System.err.println("appIcon is " + appIcon);
      ImageUtil.resizeImage(appIcon, new ResizeRequest(57, smallIcon), new ResizeRequest(512, largeIcon));
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }
    File file = new File(context.getPaths().getBuildDir(), "manifest.plist");
    NSDictionary root = new NSDictionary();
    NSDictionary items = new NSDictionary();
    NSDictionary pkg = new NSDictionary();
    pkg.put("kind", "software-package");
    pkg.put("url", "PACKAGE-URL");
    NSDictionary image = new NSDictionary();
    image.put("kind", "display-image");
    image.put("url", "ICON-URL");
    NSDictionary fullImage = new NSDictionary();
    fullImage.put("kind", "full-size-image");
    fullImage.put("url", "FULL-IMAGE-URL");
    NSArray assets = new NSArray(pkg, image, fullImage);
    items.put("assets", assets);
    NSDictionary metadata = new NSDictionary();
    metadata.put("bundle-identifier", context.getBundleId());
    metadata.put("bundle-version", context.getProject().getVName());
    metadata.put("kind", "software");
    metadata.put("platform-identifier", "com.apple.platform.iphoneos");
    metadata.put("title", context.getProject().getAName());
    items.put("metadata", metadata);
    root.put("items", new NSArray(items));
    try (PrintStream ps = new PrintStream(new FileOutputStream(file))) {
      ps.println(root.toXMLPropertyList());
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }
    context.getOutputFiles().add(file);
    context.getOutputFiles().add(smallIcon);
    context.getOutputFiles().add(largeIcon);
    return TaskResult.generateSuccess();
  }
}
