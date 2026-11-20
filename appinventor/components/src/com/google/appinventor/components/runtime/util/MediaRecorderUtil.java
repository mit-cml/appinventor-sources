// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.media.MediaRecorder;

/** Platform-specific completion of a recording. */
public final class MediaRecorderUtil {
  private MediaRecorderUtil() {
  }

  /** Runs onSaved after stop succeeds, or onFailure if stopping fails. */
  public static void stop(MediaRecorder recorder, Runnable onSaved, Runnable onFailure) {
    try {
      recorder.stop();
    } catch (RuntimeException e) {
      onFailure.run();
      return;
    }
    onSaved.run();
  }
}
