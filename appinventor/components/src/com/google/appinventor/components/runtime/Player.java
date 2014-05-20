// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

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
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.IllegalArgumentError;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

// TODO: This implementation does nothing about releasing the Media
// Player resources when the application stops.  This needs to be handled
// at the application level, not just at the component level.
// We do release a previously used MediaPlayer before creating a new one.
//
// TODO: This implementation fails when there are multiple media
// players in an application.  This appears to be a bug in the
// Android SDK, or possibly in ODE, but we need to investigate more
// fully.
//
// TODO: Do more extensive testing of how state is handled here to see
// if the state restrictions are adequate given the API, and prove that
// there can't be deadlock or starvation.
/**
 * Multimedia component that plays audio and optionally
 * vibrates.  It is built on top of {@link android.media.MediaPlayer}.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
@DesignerComponent(version = YaVersion.PLAYER_COMPONENT_VERSION,
    description = "Multimedia component that plays audio and " +
    "controls phone vibration.  The name of a multimedia field is " +
    "specified in the <code>Source</code> property, which can be set in " +
    "the Designer or in the Blocks Editor.  The length of time for a " +
    "vibration is specified in the Blocks Editor in milliseconds " +
    "(thousandths of a second).\n" +
    "<p>For supported audio formats, see " +
    "<a href=\"http://developer.android.com/guide/appendix/media-formats.html\"" +
    " target=\"_blank\">Android Supported Media Formats</a>.</p>\n" +
    "<p>This component is best for long sound files, such as songs, " +
    "while the <code>Sound</code> component is more efficient for short " +
    "files, such as sound effects.</p>",
    category = ComponentCategory.MEDIA,
    nonVisible = true,
    iconName = "images/player.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.VIBRATE, android.permission.INTERNET")
public final class Player extends AndroidNonvisibleComponent
    implements Component, OnCompletionListener, OnPauseListener, OnResumeListener, OnDestroyListener, OnStopListener, Deleteable {

  private MediaPlayer player;
  private final Vibrator vibe;

  private int playerState;
  private String sourcePath;

  // determines if playing should loop
  private boolean loop;

  /*
   * playerState encodes a simplified version of the full MediaPlayer state space, that should be
   * adequate, given this API:
   * 0: player initial state
   * 1: player prepared but not started
   * 2: player is playing
   * 3: player was playing and is now paused
   * The allowable transitions are:
   * Start: must be called in state 1, 2, or 3, results in state 2
   * Pause: must be called in state 2, results in state 3
   * Stop: must be called in state 1, 2 or 3, results in state 1
   * We can simplify this to remove state 0 and use a simple boolean after we're
   * more confident that there are no start-up problems.
   */

  /**
   * Creates a new Player component.
   *
   * @param container
   */
  public Player(ComponentContainer container) {
    super(container.$form());
    sourcePath = "";
    vibe = (Vibrator) form.getSystemService(Context.VIBRATOR_SERVICE);
    form.registerForOnDestroy(this);
    form.registerForOnResume(this);
    form.registerForOnPause(this);
    form.registerForOnStop(this);
    // Make volume buttons control media, not ringer.
    form.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    loop = false;
  }

  /**
   * Returns the path to the audio source
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String Source() {
    return sourcePath;
  }

  /**
   * Sets the audio source.
   *
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path to the audio source
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty
  public void Source(String path) {
    sourcePath = (path == null) ? "" : path;

    // Clear the previous MediaPlayer.
    if (playerState == 1 || playerState == 2 || playerState == 3) {
      player.stop();
      playerState = 0;
    }
    if (player != null) {
      player.release();
      player = null;
    }

    if (sourcePath.length() > 0) {
      player = new MediaPlayer();
      player.setOnCompletionListener(this);

      try {
        MediaUtil.loadMediaPlayer(player, form, sourcePath);

      } catch (IOException e) {
        player.release();
        player = null;
        form.dispatchErrorOccurredEvent(this, "Source",
            ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
        return;
      }

      player.setAudioStreamType(AudioManager.STREAM_MUSIC);

      // The Simple API is set up so that the user never has to call prepare.
      prepare();
      // Player should now be in state 1. (If prepare failed, we are in state 0.)
    }
  }

  /**
   * Reports whether the media is playing.
   */
  @SimpleProperty(
      description = "Reports whether the media is playing",
      category = PropertyCategory.BEHAVIOR)
  public boolean IsPlaying() {
    if (playerState == 1 || playerState == 2) {
      return player.isPlaying();
    }
    return false;
  }

  /**
   * Reports whether the playing should loop.
   */
  @SimpleProperty(
      description =
      "If true, the player will loop when it plays. Setting Loop while the player " +
      "is playing will affect the current playing.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Loop() {
    return loop;
  }

  /**
   * Sets the looping property to true or false.
   *
   * @param shouldLoop determines if the playing should loop
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void Loop(boolean shouldLoop) {
    // set the desired looping right now if the player is prepared.
    if (playerState == 1 || playerState == 2 || playerState == 3) {
       player.setLooping(shouldLoop);
    }
    // even if the player is not prepared, it will be set according to
    // Loop the next time it is started
    loop = shouldLoop;
    }

  /**
   * Sets the volume property to a number between 0 and 100.
   *
   * @param vol  the desired volume level
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "50")
  @SimpleProperty(
      description = "Sets the volume to a number between 0 and 100")
  public void Volume(int vol) {
    if (playerState == 1 || playerState == 2 || playerState == 3) {
      if (vol > 100 || vol < 0) {
        throw new IllegalArgumentError("Volume must be set to a number between 0 and 100");
      }
      player.setVolume(((float) vol) / 100, ((float) vol) / 100);
    }
  }

  /**
   * Plays the media.  If it was previously paused, the playing is resumed.
   * If it was previously stopped, it starts from the beginning.
   */
  @SimpleFunction
  public void Start() {
    if (playerState == 1 || playerState == 2 || playerState == 3) {
      player.setLooping(loop);
      player.start();
      playerState = 2;
      // Player should now be in state 2
    }
  }

  /**
   * Suspends playing the media if it is playing.
   */
  @SimpleFunction
  public void Pause() {
    if (player == null) return; //Do nothing if the player is not
    boolean wasPlaying = player.isPlaying();
    if (playerState == 2) {
      player.pause();
      if (wasPlaying) {
        playerState = 3;
        // Player should now be in state 3.
      }
    }
  }

  /**
   * Stops playing the media and seeks to the beginning of the song.
   */
  @SimpleFunction
  public void Stop() {
    if (playerState == 1 || playerState == 2 || playerState == 3) {
      player.stop();
      prepare();
      player.seekTo(0);
      // Player should now be in state 1. (If prepare failed, we are in state 0.)
    }
  }

  //  TODO: Reconsider whether vibrate should be here or in a separate component.
  /**
   * Vibrates for specified number of milliseconds.
   */
  @SimpleFunction
  public void Vibrate(long milliseconds) {
    vibe.vibrate(milliseconds);
  }

  @SimpleEvent(description = "The PlayerError event is no longer used. " +
      "Please use the Screen.ErrorOccurred event instead.",
      userVisible = false)
  public void PlayerError(String message) {
  }

  private void prepare() {
    // This should be called only after player.stop() or directly after
    // initialization
    try {
      player.prepare();
      playerState = 1;
    } catch (IOException ioe) {
      player.release();
      player = null;
      playerState = 0;
      form.dispatchErrorOccurredEvent(this, "Source",
          ErrorMessages.ERROR_UNABLE_TO_PREPARE_MEDIA, sourcePath);
    }
  }

  // OnCompletionListener implementation
  @Override
  public void onCompletion(MediaPlayer m) {
    Completed();
  }

  /**
   * Indicates that the media has reached the end
   */
  @SimpleEvent
  public void Completed() {
    EventDispatcher.dispatchEvent(this, "Completed");
  }

  // OnResumeListener implementation
  @Override
  public void onResume() {
    if (playerState == 3) {
      Start();
    }
  }

  // OnPauseListener implementation

  @Override
  public void onPause() {
    if (player == null) return; //Do nothing if the player is not ready
    if (player.isPlaying()) {
      Pause();
    }
  }

  @Override
  public void onStop() {
    if (player == null) return; //Do nothing if the player is not
    if (player.isPlaying()) {
      Pause();
    }
  }

  // OnDestroyListener implementation
  @Override
  public void onDestroy() {
    prepareToDie();
  }

  // Deleteable implementation
  @Override
  public void onDelete() {
    prepareToDie();
  }

  private void prepareToDie() {
    // TODO(lizlooney) - add descriptively named constants for these magic numbers.
    if (playerState != 0) {
      player.stop();
    }
    playerState = 0;
    if (player != null) {
      player.release();
      player = null;
    }
    vibe.cancel();
  }
}
