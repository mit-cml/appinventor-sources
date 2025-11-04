// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import com.google.appinventor.buildserver.util.Execution;
import com.google.appinventor.buildserver.util.ExecutorUtils;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@BuildType(ipa = true, asc = true)
public class PrepareAppIcon implements IosTask {

  private static final String IDIOM_IPHONE = "iphone";
  private static final String IDIOM_IPAD = "ipad";
  private static final String IDIOM_MARKETING = "ios-marketing";

  private static class IconSize {
    private final double iSize;
    private final String size;
    private final String idiom;
    private final String filename;
    private final int iScale;
    private final String scale;

    private IconSize(double size, String idiom, String filename, int scale) {
      iSize = size;
      if (size == Math.round(size)) {
        this.size = ((int) size) + "x" + ((int) size);
      } else {
        this.size = size + "x" + size;
      }
      this.idiom = idiom;
      this.filename = filename;
      iScale = scale;
      this.scale = scale + "x";
    }

    private JSONObject toJson() throws JSONException {
      JSONObject content = new JSONObject();
      content.put("size", size);
      content.put("idiom", idiom);
      content.put("filename", filename);
      content.put("scale", scale);
      return content;
    }
  }

  private static final List<IconSize> NEEDED_SIZES = new ArrayList<>();

  static {
    NEEDED_SIZES.add(new IconSize(20, IDIOM_IPHONE, "AppIcon-40.png", 2));
    NEEDED_SIZES.add(new IconSize(20, IDIOM_IPHONE, "AppIcon-60.png", 3));
    NEEDED_SIZES.add(new IconSize(29, IDIOM_IPHONE, "AppIcon-58.png", 2));
    NEEDED_SIZES.add(new IconSize(29, IDIOM_IPHONE, "AppIcon-87.png", 3));
    NEEDED_SIZES.add(new IconSize(40, IDIOM_IPHONE, "AppIcon-80.png", 2));
    NEEDED_SIZES.add(new IconSize(40, IDIOM_IPHONE, "AppIcon-120.png", 3));
    NEEDED_SIZES.add(new IconSize(60, IDIOM_IPHONE, "AppIcon-120.png", 2));
    NEEDED_SIZES.add(new IconSize(60, IDIOM_IPHONE, "AppIcon-180.png", 3));
    NEEDED_SIZES.add(new IconSize(20, IDIOM_IPAD, "AppIcon-20.png", 1));
    NEEDED_SIZES.add(new IconSize(20, IDIOM_IPAD, "AppIcon-40.png", 2));
    NEEDED_SIZES.add(new IconSize(29, IDIOM_IPAD, "AppIcon-29.png", 1));
    NEEDED_SIZES.add(new IconSize(29, IDIOM_IPAD, "AppIcon-58.png", 2));
    NEEDED_SIZES.add(new IconSize(40, IDIOM_IPAD, "AppIcon-40.png", 1));
    NEEDED_SIZES.add(new IconSize(40, IDIOM_IPAD, "AppIcon-80.png", 2));
    NEEDED_SIZES.add(new IconSize(76, IDIOM_IPAD, "AppIcon-76.png", 1));
    NEEDED_SIZES.add(new IconSize(76, IDIOM_IPAD, "AppIcon-152.png", 2));
    NEEDED_SIZES.add(new IconSize(83.5, IDIOM_IPAD, "AppIcon-167.png", 2));
    NEEDED_SIZES.add(new IconSize(1024, IDIOM_MARKETING, "AppIcon-1024.png", 1));
  }

  @Override
  public TaskResult execute(IosCompilerContext context) {
    TaskResult result;

    context.getReporter().info("Creating app asset dirs...");
    File xcassets = ExecutorUtils.createDir(context.getPaths().getAssetsDir(), "Assets.xcassets");
    if (!(result = createAssetsJson(xcassets)).isSuccess()) {
      return result;
    }

    if (!context.getProject().getIcon().isEmpty()) {
      context.getReporter().info("Adding app icon to bundle...");
      File src = new File(context.getPaths().getAssetsDir(), context.getProject().getIcon());
      File dest = new File(context.getPaths().getAppDir(), context.getProject().getIcon());
      if (!ExecutorUtils.copyFile(src.getAbsolutePath(), dest.getAbsolutePath())) {
        return TaskResult.generateError("Unable to copy app icon to bundle");
      }
    }

    File appicon = ExecutorUtils.createDir(xcassets, "AppIcon.appiconset");
    if (!(result = createAppIconJson(appicon)).isSuccess()) {
      return result;
    }

    context.getReporter().info("Generating icons...");
    for (IconSize size : NEEDED_SIZES) {
      if (!(result = createAppIcon(context, appicon, size)).isSuccess()) {
        return result;
      }
    }

    context.getReporter().info("Compiling icon asset set...");
    return compileAppIcon(context, xcassets);
  }

  private TaskResult createAssetsJson(File xcassets) {
    try (FileOutputStream fis = new FileOutputStream(new File(xcassets, "Contents.json"))) {
      JSONObject info = new JSONObject();
      info.put("version", 1);
      info.put("author", "xcode");
      JSONObject payload = new JSONObject();
      payload.put("info", info);
      fis.write(payload.toString().getBytes(StandardCharsets.UTF_8));
      return TaskResult.generateSuccess();
    } catch (IOException | JSONException e) {
      return TaskResult.generateError(e);
    }
  }

  private TaskResult createAppIconJson(File appicon) {
    try (FileOutputStream fis = new FileOutputStream(new File(appicon, "Contents.json"))) {
      JSONObject contentsJson = new JSONObject();
      JSONArray imageInfo = new JSONArray();
      for (IconSize size : NEEDED_SIZES) {
        imageInfo.put(size.toJson());
      }
      contentsJson.put("images", imageInfo);
      JSONObject infoJson = new JSONObject();
      infoJson.put("version", 1);
      infoJson.put("author", "xcode");
      contentsJson.put("info", infoJson);
      fis.write(contentsJson.toString().getBytes(StandardCharsets.UTF_8));
      return TaskResult.generateSuccess();
    } catch (IOException | JSONException e) {
      return TaskResult.generateError(e);
    }
  }

  private TaskResult createAppIcon(IosCompilerContext context, File iconDir, IconSize size) {
    String iconName = context.getProject().getIcon();
    File outIconPath = new File(iconDir, size.filename);
    try {
      BufferedImage icon;
      if (iconName.isEmpty()) {
        icon = context.getResources().getDefaultIcon();
      } else {
        icon = ImageIO.read(new File(context.getPaths().getAssetsDir(), iconName));
      }
      int imageSize = (int) Math.floor(size.iSize * size.iScale);
      BufferedImage resizedIcon = new BufferedImage(imageSize, imageSize, Image.SCALE_SMOOTH);
      Graphics2D g2 = resizedIcon.createGraphics();
      g2.drawImage(icon, 0, 0, imageSize, imageSize, null);
      ImageIO.write(resizedIcon, "png", outIconPath);
      return TaskResult.generateSuccess();
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }
  }

  private TaskResult compileAppIcon(IosCompilerContext context, File xcassetsDir) {
    /* /Applications/Xcode.app/Contents/Developer/usr/bin/actool
         --output-format human-readable-text --notices --warnings
         --export-dependency-info /Users/ewpatton/Programming/mit/app-inventor-private-ios/appinventor/components-ios/build/App\ Inventor/Build/Intermediates.noindex/ArchiveIntermediates/AICompanionApp/IntermediateBuildFilesPath/AICompanionApp.build/Release-iphoneos/AICompanionApp.build/assetcatalog_dependencies
         --output-partial-info-plist /Users/ewpatton/Programming/mit/app-inventor-private-ios/appinventor/components-ios/build/App\ Inventor/Build/Intermediates.noindex/ArchiveIntermediates/AICompanionApp/IntermediateBuildFilesPath/AICompanionApp.build/Release-iphoneos/AICompanionApp.build/assetcatalog_generated_info.plist
         --app-icon AppIcon --compress-pngs --enable-on-demand-resources YES --sticker-pack-identifier-prefix edu.mit.appinventor.aicompanion3.sticker-pack.
         --development-region en --target-device iphone --target-device ipad --minimum-deployment-target 9.0 --platform iphoneos
         --product-type com.apple.product-type.application
         --compile /Users/ewpatton/Programming/mit/app-inventor-private-ios/appinventor/components-ios/build/App\ Inventor/Build/Intermediates.noindex/ArchiveIntermediates/AICompanionApp/InstallationBuildProductsLocation/Applications/AICompanionApp.app
         /Users/ewpatton/Programming/mit/app-inventor-private-ios/appinventor/components-ios/AICompanionApp/Assets.xcassets
     */
    String depInfo = new File(context.getPaths().getTmpDir(), "assetcatalog_dependencies").getAbsolutePath();
    String partialPlist = new File(context.getPaths().getTmpDir(), "assetcatalog_generated_info.plist").getAbsolutePath();
    String[] args = new String[] {
        "/Applications/Xcode.app/Contents/Developer/usr/bin/actool",
        "--output-format", "human-readable-text",
        "--notices", "--warnings",
        "--export-dependency-info", depInfo,
        "--output-partial-info-plist", partialPlist,
        "--app-icon", "AppIcon",
        "--compress-pngs", "--enable-on-demand-resources", "YES",
        "--development-region", "en",
        "--target-device", "iphone",
        "--target-device", "ipad",
        "--minimum-deployment-target", "12.0",
        "--platform", "iphoneos",
        "--product-type", "com.apple.product-type.application",
        "--compile", context.getPaths().getAppDir().getAbsolutePath(),
        xcassetsDir.getAbsolutePath()
    };
    if (Execution.execute(context.getPaths().getBuildDir(), args,
        context.getReporter().getSystemOut(), System.err)) {
      return TaskResult.generateSuccess();
    } else {
      return TaskResult.generateError("Unable to compile app icon");
    }
  }
}
