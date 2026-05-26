// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import com.google.appinventor.components.runtime.Form;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility methods for loading and rasterizing SVG images.
 *
 * <p>SVG files are rasterized into {@link BitmapDrawable} objects so that all
 * existing code paths in Image, ImageSprite, and Canvas that expect a
 * {@link BitmapDrawable} or plain {@link android.graphics.drawable.Drawable}
 * continue to work without further changes.
 *
 * <p>Rasterization is performed by the <a href="https://bigbadaboom.github.io/androidsvg/">
 * androidsvg</a> library (already bundled in the App Inventor build).
 */
public final class SvgUtil {

  private static final String LOG_TAG = "SvgUtil";

  /** Prevent instantiation. */
  private SvgUtil() {}

  /**
   * Returns {@code true} if {@code mediaPath} refers to an SVG file (based on its extension).
   *
   * @param mediaPath the path or URL of the media asset
   * @return whether the path ends with {@code .svg} (case-insensitive)
   */
  public static boolean isSvg(String mediaPath) {
    if (mediaPath == null) {
      return false;
    }
    // Strip any query string before checking extension (e.g. "foo.svg?t=12345")
    String path = mediaPath;
    int qIndex = path.indexOf('?');
    if (qIndex >= 0) {
      path = path.substring(0, qIndex);
    }
    return path.toLowerCase().endsWith(".svg");
  }

  /**
   * Loads an SVG from the given media path and rasterizes it into a {@link BitmapDrawable}.
   *
   * <p>The raster dimensions are chosen as follows:
   * <ol>
   *   <li>If {@code desiredWidth} &gt; 0 and {@code desiredHeight} &gt; 0, those dimensions
   *       are used (scaled by device density).</li>
   *   <li>Otherwise, the SVG's intrinsic width/height (from its {@code width}/{@code height}
   *       attributes or {@code viewBox}) are used.</li>
   *   <li>If neither is available, a default of 100 × 100 density-independent pixels is used.</li>
   * </ol>
   *
   * @param form          the active {@link Form} (used for resources and to open media)
   * @param mediaPath     the path or URL of the SVG asset
   * @param desiredWidth  the desired width in density-independent pixels, or ≤ 0 for intrinsic
   * @param desiredHeight the desired height in density-independent pixels, or ≤ 0 for intrinsic
   * @return a {@link BitmapDrawable} containing the rasterized SVG
   * @throws IOException       if the SVG cannot be opened
   * @throws SVGParseException if the SVG content is malformed
   */
  public static BitmapDrawable getBitmapDrawableFromSvg(
      Form form, String mediaPath, int desiredWidth, int desiredHeight)
      throws IOException, SVGParseException {

    InputStream is = null;
    try {
      is = MediaUtil.openMedia(form, mediaPath);
      SVG svg = SVG.getFromInputStream(is);

      float density = form.getResources().getDisplayMetrics().density;
      float DEFAULT_SIZE_DP = 100f;

      // Determine raster width in pixels
      float rasterW;
      if (desiredWidth > 0) {
        rasterW = desiredWidth * density;
      } else {
        float intrinsicW = svg.getDocumentWidth();
        rasterW = (intrinsicW > 0) ? intrinsicW : DEFAULT_SIZE_DP * density;
      }

      // Determine raster height in pixels
      float rasterH;
      if (desiredHeight > 0) {
        rasterH = desiredHeight * density;
      } else {
        float intrinsicH = svg.getDocumentHeight();
        rasterH = (intrinsicH > 0) ? intrinsicH : DEFAULT_SIZE_DP * density;
      }

      int bitmapW = Math.max(1, Math.round(rasterW));
      int bitmapH = Math.max(1, Math.round(rasterH));

      // Tell the SVG library the target canvas size so it scales the viewBox correctly
      svg.setDocumentWidth(bitmapW);
      svg.setDocumentHeight(bitmapH);

      Bitmap bitmap = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      svg.renderToCanvas(canvas);

      BitmapDrawable drawable = new BitmapDrawable(form.getResources(), bitmap);
      drawable.setTargetDensity(form.getResources().getDisplayMetrics());
      return drawable;

    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          Log.w(LOG_TAG, "Failed to close SVG input stream", e);
        }
      }
    }
  }

  /**
   * Convenience overload that uses intrinsic SVG dimensions (no desired size specified).
   *
   * @param form      the active {@link Form}
   * @param mediaPath the path or URL of the SVG asset
   * @return a {@link BitmapDrawable} containing the rasterized SVG
   * @throws IOException       if the SVG cannot be opened
   * @throws SVGParseException if the SVG content is malformed
   */
  public static BitmapDrawable getBitmapDrawableFromSvg(Form form, String mediaPath)
      throws IOException, SVGParseException {
    return getBitmapDrawableFromSvg(form, mediaPath, -1, -1);
  }
}
