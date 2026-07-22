// -*- mode: java; c-basic-offset: 2; -*-
/// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.ACCESS_NOTIFICATION_POLICY;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.VIBRATE;

import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.RingerMode;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.provider.Settings;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

import com.google.appinventor.components.runtime.util.TiramisuUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A multimedia component that plays sound files and optionally vibrates for the number of
 * milliseconds (thousandths of a second) specified in the Blocks Editor. The name of the sound
 * file to play can be specified either in the Designer or in the Blocks Editor.
 *
 * For supported sound file formats, see
 * [Android Supported Media Formats](//developer.android.com/guide/appendix/media-formats.html).
 *
 * This `Sound` component is best for short sound files, such as sound effects, while the
 * {@link Player} component is more efficient for longer sounds, such as songs.
 *
 * @internaldoc
 * Multimedia component that plays sounds and optionally vibrates.  A
 * sound is specified via filename.  See also
 * {@link android.media.SoundPool}.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author hal@mit.edu (Hal Abelson) added wait for load to complete
 */
@DesignerComponent(version = YaVersion.SOUND_COMPONENT_VERSION,
    description = "<p>A multimedia component that plays sound " +
    "files and optionally vibrates for the number of milliseconds " +
    "(thousandths of a second) specified in the Blocks Editor.  The name of " +
    "the sound file to play can be specified either in the Designer or in " +
    "the Blocks Editor.</p> <p>For supported sound file formats, see " +
    "<a href=\"http://developer.android.com/guide/appendix/media-formats.html\"" +
    " target=\"_blank\">Android Supported Media Formats</a>.</p>" +
    "<p>This <code>Sound</code> component is best for short sound files, such as sound " +
    "effects, while the <code>Player</code> component is more efficient for " +
    "longer sounds, such as songs.</p>" +
    "<p>You might get an error if you attempt to play a sound " +
    "immediately after setting the source.</p>",
    category = ComponentCategory.MEDIA,
    nonVisible = true,
    iconName = "images/soundEffect.png")
@SimpleObject
@UsesPermissions({
    INTERNET,
    VIBRATE,
    ACCESS_NOTIFICATION_POLICY // Needed to let the user enable "Do Not Disturb access" for an app.
})
public class Sound extends AndroidNonvisibleComponent
    implements Component, OnResumeListener, OnStopListener, OnDestroyListener, Deleteable {

  private boolean loadComplete;    // did the sound finish loading

  // The purpose of this class is to avoid getting rejected by the Android verifier when the
  // Sound component code is loaded into a device with API level less than 8, where the verifier
  // will reject OnLoadCompleteListener.  We do this trick by putting
  // the use of OnLoadCompleteListener in the class OnLoadHelper and arranging (see below) for
  // the class to be compiled only if the API level is at least 8.
  private class OnLoadHelper {
    public void setOnloadCompleteListener (SoundPool soundPool) {
      soundPool.setOnLoadCompleteListener(new android.media.SoundPool.OnLoadCompleteListener() {
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
          loadComplete = true;
        }
      });
    }
  }

  private static final int MAX_STREAMS = 10;

  // max number of consecutive delays to wait for a sound to load
  private static final int MAX_PLAY_DELAY_RETRIES = 10;
  // number of ms in each delay before retrying
  private static final int PLAY_DELAY_LENGTH = 50;

  private static final float VOLUME_FULL = 1.0f;
  private static final int LOOP_MODE_NO_LOOP = 0;
  private static final float PLAYBACK_RATE_NORMAL = 1.0f;
  private SoundPool soundPool;

  // soundMap maps sounds (assets, etc) that are loaded into soundPool to their respective
  // soundIds.
  private final Map<String, Integer> soundMap;

  // We will wait for Sound loading to complete before trying to play, but only
  // if the API level is at least 8, because onLoadCompleteListener is not available
  // in earlier APIs. For those early systems, attempting to play a sound before it is loaded
  // will fail to play the sound and there will be no retry, although there might be a "cannot
  // play" error.
  private final boolean waitForLoadToComplete = (SdkLevel.getLevel() >= SdkLevel.LEVEL_FROYO);

  private String sourcePath;              // name of source
  private int soundId;                    // id of sound in the soundPool
  private int streamId;                   // stream id returned from last call to SoundPool.play
  private int minimumInterval;            // minimum interval between Play() calls
  private long timeLastPlayed;            // the system time when Play() was last called
  private final Vibrator vibe;
  private final AudioManager audioManager;
  private final Handler playWaitHandler = new Handler();

  //save a pointer to this Sound component to use in the error in postDelayed below
  private final Component thisComponent;


  public Sound(ComponentContainer container) {
    super(container.$form());
    thisComponent = this;
    soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    soundMap = new HashMap<String, Integer>();
    vibe = (Vibrator) form.getSystemService(Context.VIBRATOR_SERVICE);
    audioManager = (AudioManager) form.getSystemService(Context.AUDIO_SERVICE);
    sourcePath = "";
    loadComplete = true;  //nothing to wait for until we attempt to load
    form.registerForOnResume(this);
    form.registerForOnStop(this);
    form.registerForOnDestroy(this);

    // Make volume buttons control media, not ringer.
    form.setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Default property values
    MinimumInterval(500);

    if (waitForLoadToComplete) {
      new OnLoadHelper().setOnloadCompleteListener(soundPool);
    }
  }



  /**
   * Returns the sound's filename.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "The name of the sound file.  Only certain " +
      "formats are supported.  See http://developer.android.com/guide/appendix/media-formats.html.")
  public String Source() {
    return sourcePath;
  }

  /**
   * The name of the sound file. Only certain formats are supported.
   * See http://developer.android.com/guide/appendix/media-formats.html.
   *
   * @internaldoc
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path to the sound source
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty
  public void Source(@Asset String path) {
    final String tempPath = (path == null) ? "" : path;
    if (TiramisuUtil.requestAudioPermissions(form, path, new PermissionResultHandler() {
      @Override
      public void HandlePermissionResponse(String permission, boolean granted) {
        if (granted) {
          Sound.this.Source(tempPath);
        } else {
          form.dispatchPermissionDeniedEvent(Sound.this, "Source", permission);
        }
      }
    })) {
      return;
    }
    sourcePath = tempPath;

    // Clear the previous sound.
    if (streamId != 0) {
      soundPool.stop(streamId);
      streamId = 0;
    }
    soundId = 0;

    if (sourcePath.length() != 0) {
      Integer existingSoundId = soundMap.get(sourcePath);
      if (existingSoundId != null) {
        soundId = existingSoundId;

      } else {
        Log.i("Sound", "No existing sound with path " + sourcePath + ".");
        try {
          int newSoundId = MediaUtil.loadSoundPool(soundPool, form, sourcePath);
          if (newSoundId != 0) {
            soundMap.put(sourcePath, newSoundId);
            Log.i("Sound", "Successfully began loading sound: setting soundId to " + newSoundId + ".");
            soundId = newSoundId;
            // set flag to show that loading has begun
            loadComplete = false;
          } else {
            form.dispatchErrorOccurredEvent(this, "Source",
                ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
          }
        } catch (PermissionException e) {
          form.dispatchPermissionDeniedEvent(this, "Source", e);
        } catch (IOException e) {
          form.dispatchErrorOccurredEvent(this, "Source",
              ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
        }
      }
    }
  }

  /**
   * Returns the minimum interval required between calls to Play(), in
   * milliseconds.
   * Once the sound starts playing, all further Play() calls will be ignored
   * until the interval has elapsed.
   * @return  minimum interval in ms
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "The minimum interval, in milliseconds, between sounds.  If you play a sound, " +
      "all further Play() calls will be ignored until the interval has elapsed.")
  public int MinimumInterval() {
    return minimumInterval;
  }

  /**
   * Specifies the minimum interval required between calls to {@link #Play()}, in
   * milliseconds.
   * Once the sound starts playing, all further {@link #Play()} calls will be ignored
   * until the interval has elapsed.
   * @param interval  minimum interval in ms
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "500")
  @SimpleProperty
  public void MinimumInterval(int interval) {
    minimumInterval = interval;
  }


  // number of retries remaining before signaling an error
  private int delayRetries;

  /**
   * Plays the sound.
   */
  @SimpleFunction(description = "Plays the sound specified by the Source property.")
  public void Play() {
    if (soundId != 0) {
      long currentTime = System.currentTimeMillis();
      if (timeLastPlayed == 0 || currentTime >= timeLastPlayed + minimumInterval) {
        timeLastPlayed = currentTime;
        delayRetries =  MAX_PLAY_DELAY_RETRIES;
        playWhenLoadComplete();
      } else {
        // fail silently
        Log.i("Sound", "Unable to play because MinimumInterval has not elapsed since last play.");
      }
    } else {
      // Alert the user that the sound is bad, but would need to look in the log to distinguish
      // this error from the UNABLE_TO_PLAY_MEDIA error in playAndCheck.
      Log.i("Sound", "Sound Id was 0. Did you remember to set the Source property?");
      form.dispatchErrorOccurredEvent(this, "Play",
          ErrorMessages.ERROR_UNABLE_TO_PLAY_MEDIA, sourcePath);
    }
  }

  // Attempt to play the sound, possibly after a delay to allow the sound to load.
  private void playWhenLoadComplete() {
    if (loadComplete || !waitForLoadToComplete) {
      playAndCheckResult();
    } else {
      Log.i("Sound", "Sound not ready:  retrying.  Remaining retries = " + delayRetries);
      // if the sound wasn't ready we retry after a delay. We implement the delay by posting
      // to a separate handler: using a loop with a sleep might seem simpler, but it would block
      // the UI thread.
      playWaitHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          if (loadComplete) {
            playAndCheckResult();
          } else if (delayRetries > 0) {
            delayRetries--;
            playWhenLoadComplete();
          } else {
            form.dispatchErrorOccurredEvent(thisComponent, "Play",
                ErrorMessages.ERROR_SOUND_NOT_READY, sourcePath);
          }
        }
      }, PLAY_DELAY_LENGTH);
    }
  }

  private void playAndCheckResult() {
    if (soundPool == null) {
      return;
    }
    streamId = soundPool.play(soundId, VOLUME_FULL, VOLUME_FULL, 0, LOOP_MODE_NO_LOOP,
        PLAYBACK_RATE_NORMAL);
    Log.i("Sound", "SoundPool.play returned stream id " + streamId);
    if (streamId == 0) {
      form.dispatchErrorOccurredEvent(this, "Play",
          ErrorMessages.ERROR_UNABLE_TO_PLAY_MEDIA, sourcePath);
    }
  }


  /**
   * Pauses playing the sound if it is being played.
   */
  @SimpleFunction(description = "Pauses playing the sound if it is being played.")
  public void Pause() {
    if (streamId != 0) {
      soundPool.pause(streamId);
    } else {
      Log.i("Sound", "Unable to pause. Did you remember to call the Play function?");
    }
  }

  /**
   * Resumes playing the sound after a pause.
   */
  @SimpleFunction(description = "Resumes playing the sound after a pause.")
  public void Resume() {
    if (streamId != 0) {
      soundPool.resume(streamId);
    } else {
      Log.i("Sound", "Unable to resume. Did you remember to call the Play function?");
    }
  }

  /**
   * Stops playing the sound if it is being played.
   */
@SimpleFunction(description = "Stops playing the sound if it is being played.")
  public void Stop() {
    if (streamId != 0) {
      soundPool.stop(streamId);
      streamId = 0;
    } else {
      Log.i("Sound", "Unable to stop. Did you remember to call the Play function?");
    }
  }

  /**
   * Vibrates for the specified number of milliseconds.
   */
  @SimpleFunction(description = "Vibrates for the specified number of milliseconds.")
  public void Vibrate(int millisecs) {
    vibe.vibrate(millisecs);
  }

  @SimpleFunction(description = "Vibrate with a given pattern")
  public void VibratePattern(int vibrate, int delay, boolean repeat) {
    int times = 0;
    if (repeat) {
      times = 0;
    } else {
      times = -1;
    }
    long[] pattern = {0, vibrate, delay};
    vibe.vibrate(pattern, times);
  }

  @SimpleFunction(description = "Sets the Ringer mode to normal, silent, or vibrate. "
      + "On devices running Android Nougat or later, ringer mode adjustments that toggle "
      + "Do Not Disturb are not allowed unless the app has been granted Do Not Disturb Access. "
      + "Use the CanToggleDoNotDisturb block to determine whether the policy has been granted. "
      + "Use the ShowDoNotDisturbAccessSettings to allow the user update the setting.")
  public void SetRingerMode(RingerMode ringerMode) {
    setRingerMode(ringerMode);
  }

  /* Ringer mode that may be audible and may vibrate */
  @SimpleFunction(description = "Ringer mode that may be audible and may vibrate.")
  @Deprecated
  public void SoundNormal() {
    setRingerMode(RingerMode.Normal);
  }

  /* Ringer mode that will be silent and will not vibrate */
  @SimpleFunction(description = "Ringer mode that will be silent and will not vibrate.")
  @Deprecated
  public void SoundSilent() {
    setRingerMode(RingerMode.Silent);
  }

  /* Ringer mode that will be silent and will vibrate */
  @SimpleFunction(description = "Ringer mode that will be silent and will vibrate.")
  @Deprecated
  public void SoundVibrate() {
    setRingerMode(RingerMode.Vibrate);
  }

  private void setRingerMode(RingerMode mode) {
    audioManager.setRingerMode(mode.toInt());
  }

  @SimpleFunction(description = "Returns true if ringer mode adjustments that toggle "
      + "Do Not Disturb are allowed, false otherwise. "
      + "On devices running Android Nougat or later, ringer mode adjustments that toggle "
      + "Do Not Disturb are not allowed unless the app has been granted Do Not Disturb Access. "
      + "Use the ShowDoNotDisturbAccessSettings to allow the user update the setting.")
  public boolean CanToggleDoNotDisturb() {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_NOUGAT) {
      NotificationManager notificationManager =
          (NotificationManager) form.getSystemService(Context.NOTIFICATION_SERVICE);
      return notificationManager.isNotificationPolicyAccessGranted();
    }

    // Before N, just return true.
    return true;
  }

  @SimpleFunction(description = "Show the Do Not Disturb access settings.")
  public void ShowDoNotDisturbAccessSettings() {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_MARSHMALLOW) {
      Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
      try {
        form.startActivity(intent);
      } catch (ActivityNotFoundException e) {
        form.dispatchErrorOccurredEvent(this, "ShowDoNotDisturbAccessSettings",
            ErrorMessages.ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY);
      }
    }
  }


  @SimpleEvent(description = "The SoundError event is no longer used. " +
      "Please use the Screen.ErrorOccurred event instead.",
      userVisible = false)
  public void SoundError(String message) {
  }

  // OnStopListener implementation

  @Override
  public void onStop() {
    Log.i("Sound", "Got onStop");
    if (streamId != 0 && soundPool != null) {
      soundPool.pause(streamId);
    }
  }

  // OnResumeListener implementation

  @Override
  public void onResume() {
    Log.i("Sound", "Got onResume");
    if (streamId != 0 && soundPool != null) {
      soundPool.resume(streamId);
    }
  }

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    prepareToDie();
  }

  // Deletable implementation

  @Override
  public void onDelete() {
    prepareToDie();
  }

  private void prepareToDie() {
    if (streamId != 0) {
      soundPool.stop(streamId);
      soundPool.unload(streamId);
    }
    soundPool.release();
    vibe.cancel();
    // The documentation for SoundPool suggests setting the reference to null;
    soundPool = null;
  }
}
