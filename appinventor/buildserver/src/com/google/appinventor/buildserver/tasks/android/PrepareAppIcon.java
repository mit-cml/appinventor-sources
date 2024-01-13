// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import com.google.common.base.Strings;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;


/**
 * compiler.prepareApplicationIcon()
 */
@BuildType(apk = true, aab = true)
public class PrepareAppIcon implements AndroidTask {
  private static final String ERROR_NO_SUITABLE_ICON =
      "Could not find a suitable app icon. Maybe it's not an image.";

  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    // Create mipmap directories
    context.getReporter().info("Creating mipmap dirs...");
    File mipmapHdpi = ExecutorUtils.createDir(context.getPaths().getResDir(), "mipmap-hdpi");
    File mipmapMdpi = ExecutorUtils.createDir(context.getPaths().getResDir(), "mipmap-mdpi");
    File mipmapXhdpi = ExecutorUtils.createDir(context.getPaths().getResDir(), "mipmap-xhdpi");
    File mipmapXxhdpi = ExecutorUtils.createDir(context.getPaths().getResDir(), "mipmap-xxhdpi");
    File mipmapXxxhdpi = ExecutorUtils.createDir(context.getPaths().getResDir(), "mipmap-xxxhdpi");

    // Create list of mipmaps for all icon types with respective sizes
    List<File> mipmapDirectoriesForIcons = Arrays.asList(mipmapMdpi, mipmapHdpi, mipmapXhdpi,
        mipmapXxhdpi, mipmapXxxhdpi);
    List<Integer> standardSizesForMipmaps = Arrays.asList(48, 72, 96, 144, 192);
    List<Integer> foregroundSizesForMipmaps = Arrays.asList(108, 162, 216, 324, 432);

    context.getReporter().info("Generating icons...");
    if (!this.prepareApplicationIcon(context,
        new File(context.getPaths().getDrawableDir(), "ya.png"),
        mipmapDirectoriesForIcons, standardSizesForMipmaps, foregroundSizesForMipmaps)) {
      return TaskResult.generateError("Could not prepare app icon");
    }
    return TaskResult.generateSuccess();
  }

  /*
   * Loads the icon for the application, either a user provided one or the default one.
   */
  private boolean prepareApplicationIcon(AndroidCompilerContext context, File outputPngFile,
      List<File> mipmapDirectories, List<Integer> standardSizes, List<Integer> foregroundSizes) {
    String userSpecifiedIcon = Strings.nullToEmpty(context.getProject().getIcon());
    try {
      BufferedImage icon;
      if (!userSpecifiedIcon.isEmpty()) {
        File iconFile = new File(context.getProject().getAssetsDirectory(), userSpecifiedIcon);
        icon = ImageIO.read(iconFile);
        if (icon == null) {
          // This can happen if the iconFile isn't an image file.
          // For example, icon is null if the file is a .wav file.
          // TODO(lizlooney) - This happens if the user specifies a .ico file. We should
          // fix that.
          context.getReporter().error(ERROR_NO_SUITABLE_ICON, true);
          return false;
        }
      } else {
        // Load the default image.
        icon = context.getResources().getDefaultIcon();
      }

      BufferedImage roundIcon = produceRoundIcon(icon);
      BufferedImage roundRectIcon = produceRoundedCornerIcon(icon);
      BufferedImage foregroundIcon = produceForegroundImageIcon(icon);

      // For each mipmap directory, create all launcher icons with respective mipmap sizes
      for (int i = 0; i < mipmapDirectories.size(); i++) {
        File mipmapDirectory = mipmapDirectories.get(i);
        context.getReporter().log("Generating icons for " + mipmapDirectory.getName());

        Integer standardSize = standardSizes.get(i);
        Integer foregroundSize = foregroundSizes.get(i);

        BufferedImage round = resizeImage(roundIcon, standardSize, standardSize);
        BufferedImage roundRect = resizeImage(roundRectIcon, standardSize, standardSize);
        BufferedImage foreground = resizeImage(foregroundIcon, foregroundSize, foregroundSize);

        File roundIconPng = new File(mipmapDirectory, "ic_launcher_round.png");
        File roundRectIconPng = new File(mipmapDirectory, "ic_launcher.png");
        File foregroundPng = new File(mipmapDirectory, "ic_launcher_foreground.png");

        ImageIO.write(round, "png", roundIconPng);
        ImageIO.write(roundRect, "png", roundRectIconPng);
        ImageIO.write(foreground, "png", foregroundPng);
      }
      ImageIO.write(icon, "png", outputPngFile);
    } catch (Exception e) {
      e.printStackTrace();
      // If the user specified the icon, this is fatal.
      if (!userSpecifiedIcon.isEmpty()) {
        context.getReporter().error("Something went wrong while getting the default icon", true);
        return false;
      }
    }

    return true;
  }

  /*
   * Creates the circle image of an icon
   */
  private BufferedImage produceRoundIcon(BufferedImage icon) {
    int imageWidth = icon.getWidth();
    // Ratio of icon size to png image size for round icon is 0.80
    double iconWidth = imageWidth * 0.80;
    // Round iconWidth value to even int for a centered png
    int intIconWidth = ((int) Math.round(iconWidth / 2) * 2);
    Image tmp = icon.getScaledInstance(intIconWidth, intIconWidth, Image.SCALE_SMOOTH);
    int marginWidth = ((imageWidth - intIconWidth) / 2);
    BufferedImage roundIcon = new BufferedImage(imageWidth, imageWidth,
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = roundIcon.createGraphics();
    g2.setClip(new Ellipse2D.Float(marginWidth, marginWidth, intIconWidth, intIconWidth));
    g2.drawImage(tmp, marginWidth, marginWidth, null);
    return roundIcon;
  }

  /*
   * Creates the image of an icon with rounded corners
   */
  private BufferedImage produceRoundedCornerIcon(BufferedImage icon) {
    int imageWidth = icon.getWidth();
    // Ratio of icon size to png image size for roundRect icon is 0.93
    double iconWidth = imageWidth * 0.93;
    // Round iconWidth value to even int for a centered png
    int intIconWidth = ((int) Math.round(iconWidth / 2) * 2);
    Image tmp = icon.getScaledInstance(intIconWidth, intIconWidth, Image.SCALE_SMOOTH);
    int marginWidth = ((imageWidth - intIconWidth) / 2);
    // Corner radius of roundedCornerIcon needs to be 1/12 of width according to Android guidelines
    float cornerRadius = intIconWidth / 12;
    BufferedImage roundedCornerIcon = new BufferedImage(imageWidth, imageWidth,
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = roundedCornerIcon.createGraphics();
    g2.setClip(new RoundRectangle2D.Float(marginWidth, marginWidth, intIconWidth, intIconWidth,
        cornerRadius, cornerRadius));
    g2.drawImage(tmp, marginWidth, marginWidth, null);
    return roundedCornerIcon;
  }

  /*
   * Creates the foreground image of an icon
   */
  private BufferedImage produceForegroundImageIcon(BufferedImage icon) {
    int imageWidth = icon.getWidth();
    // According to the adaptive icon documentation, both layers are 108x108dp but only the inner
    // 72x72dp appears in the masked viewport, so we shrink down the size of the image accordingly.
    double iconWidth = imageWidth * 72.0 / 108.0;
    // Round iconWidth value to even int for a centered png
    int intIconWidth = ((int) Math.round(iconWidth / 2) * 2);
    Image tmp = icon.getScaledInstance(intIconWidth, intIconWidth, Image.SCALE_SMOOTH);
    int marginWidth = ((imageWidth - intIconWidth) / 2);
    BufferedImage foregroundImageIcon = new BufferedImage(imageWidth, imageWidth,
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = foregroundImageIcon.createGraphics();
    g2.drawImage(tmp, marginWidth, marginWidth, null);
    return foregroundImageIcon;
  }

  /*
   * Returns a resized image given a new width and height
   */
  private BufferedImage resizeImage(BufferedImage icon, int height, int width) {
    Image tmp = icon.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    BufferedImage finalResized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = finalResized.createGraphics();
    g2.drawImage(tmp, 0, 0, null);
    return finalResized;
  }
}
