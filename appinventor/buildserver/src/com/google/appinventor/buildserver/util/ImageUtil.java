// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import javax.imageio.ImageIO;

public class ImageUtil {

  public static class ResizeRequest {
    private final int width;
    private final int height;
    private final File output;

    public ResizeRequest(int size, File output) {
      this.width = size;
      this.height = size;
      this.output = output;
    }

    public ResizeRequest(int width, int height, File output) {
      this.width = width;
      this.height = height;
      this.output = output;
    }
  }

  private ImageUtil() {
  }

  public static void resizeImage(URL source, Collection<ResizeRequest> variations) throws IOException {
    BufferedImage original = ImageIO.read(source);
    for (ResizeRequest variant : variations) {
      BufferedImage resized = new BufferedImage(variant.width, variant.height, Image.SCALE_SMOOTH);
      Graphics2D canvas = resized.createGraphics();
      canvas.drawImage(original, 0, 0, variant.width, variant.height, null);
      ImageIO.write(resized, "png", variant.output);
    }
  }

  public static void resizeImage(URL source, ResizeRequest... variations) throws IOException {
    resizeImage(source, Arrays.asList(variations));
  }
}
