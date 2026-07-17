// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Movie;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

/**
 * A Drawable that renders animated GIFs using the {@link Movie} class.
 * Frames are rendered to an offscreen software bitmap and then drawn to the
 * target canvas, so this works on both hardware-accelerated and software canvases.
 */
public class GifMovieDrawable extends Drawable implements Runnable {
  // Cap at 50fps — most GIFs are 10-20fps; no need to poll faster.
  private static final int MIN_FRAME_DELAY_MS = 20;

  private final Movie movie;
  private final int duration;
  private final int frameCount;
  private final int frameDelay;
  private final Bitmap frameBitmap;
  private final Canvas frameCanvas;
  private long startTime;
  private int pausedElapsed;
  private boolean running = true;

  public GifMovieDrawable(Movie movie) {
    this.movie = movie;
    this.duration = movie.duration() > 0 ? movie.duration() : 1000;
    // Estimate frame count from duration (GIF default delay is 100ms)
    this.frameCount = Math.max(1, this.duration / 100);
    this.frameDelay = Math.max(MIN_FRAME_DELAY_MS, this.duration / frameCount);
    this.frameBitmap = Bitmap.createBitmap(movie.width(), movie.height(), Bitmap.Config.ARGB_8888);
    this.frameCanvas = new Canvas(frameBitmap);
  }

  @Override
  public void draw(Canvas canvas) {
    long now = SystemClock.uptimeMillis();
    if (startTime == 0) {
      startTime = now;
    }

    int elapsed;
    if (running) {
      elapsed = (int) ((now - startTime) % duration);
    } else {
      elapsed = pausedElapsed;
    }
    movie.setTime(elapsed);

    // Render current frame to offscreen software bitmap
    frameBitmap.eraseColor(0);
    movie.draw(frameCanvas, 0, 0);

    // Draw the bitmap to the target canvas (works on HW and SW canvases)
    Rect bounds = getBounds();
    if (!bounds.isEmpty()) {
      canvas.drawBitmap(frameBitmap, null, bounds, null);
    }

    if (running) {
      scheduleSelf(this, now + frameDelay);
    }
  }

  @Override
  public void run() {
    invalidateSelf();
  }

  @Override
  public int getIntrinsicWidth() {
    return movie.width();
  }

  @Override
  public int getIntrinsicHeight() {
    return movie.height();
  }

  /**
   * Resumes animation from the current position.
   */
  public void start() {
    if (!running) {
      running = true;
      // Adjust startTime so elapsed picks up where it left off
      startTime = SystemClock.uptimeMillis() - pausedElapsed;
      invalidateSelf();
    }
  }

  /**
   * Pauses animation at the current frame.
   */
  public void stop() {
    if (running) {
      running = false;
      pausedElapsed = (int) ((SystemClock.uptimeMillis() - startTime) % duration);
      unscheduleSelf(this);
    }
  }

  /**
   * Resets animation to the first frame, preserving the current play/pause state.
   */
  public void reset() {
    pausedElapsed = 0;
    startTime = SystemClock.uptimeMillis();
    invalidateSelf();
  }

  public boolean isRunning() {
    return running;
  }

  @Override
  public void setAlpha(int alpha) {
    // Not supported
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    // Not supported
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }
}
