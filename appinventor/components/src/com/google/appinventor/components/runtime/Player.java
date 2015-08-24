// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
import com.google.appinventor.components.runtime.util.FroyoUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Vibrator;

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

  public State playerState;
  public enum State { INITIAL, PREPARED, PLAYING, PAUSED_BY_USER, PAUSED_BY_EVENT; }
  private String sourcePath;

  // determines if playing should loop
  private boolean loop;

  // choices on player policy: Foreground, Always
  private boolean playOnlyInForeground;
  // status of audio focus
  private boolean focusOn;
  private AudioManager am;
  private final Activity activity;
  // Flag if SDK level >= 8
  private static final boolean audioFocusSupported;
  private Object afChangeListener;

  static{
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_FROYO) {
      audioFocusSupported = true;
    } else {
      audioFocusSupported = false;
    }
  }

  /*
   * playerState encodes a simplified version of the full MediaPlayer state space, that should be
   * adequate, given this API:
   * 0 (INITIAL) : player initial state
   * 1 (PREPARED) : player prepared but not started
   * 2 (PLAYING) : player is playing
   * 3 (PAUSED_BY_USER) : player was playing and is now paused by a user action
   * 4 (PAUSED_BY_EVENT) : player was playing and is now paused by lifecycle events or audio focus interrupts
   * The allowable transitions are:
   * Start: must be called in state 1, 2, 3 or 4, results in state 2
   * Pause (User method): must be called in state 2, results in state 3
   * pause (Lifecycle method): must be called in state 2, results in state 4; will go back to
   *                           state 2 automatically
   * Stop: must be called in state 1, 2, 3 or 4, results in state 1
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
    activity = container.$context();
    sourcePath = "";
    vibe = (Vibrator) form.getSystemService(Context.VIBRATOR_SERVICE);
    form.registerForOnDestroy(this);
    form.registerForOnResume(this);
    form.registerForOnPause(this);
    form.registerForOnStop(this);
    // Make volume buttons control media, not ringer.
    form.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    loop = false;
    playOnlyInForeground = false;
    focusOn = false;
    am = (audioFocusSupported) ? FroyoUtil.setAudioManager(activity) : null;
    afChangeListener = (audioFocusSupported) ? FroyoUtil.setAudioFocusChangeListener(this) : null;
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
    if (playerState == State.PREPARED || playerState == State.PLAYING || playerState == State.PAUSED_BY_USER) {
      player.stop();
      playerState = State.INITIAL;
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
      if (audioFocusSupported) {
        requestPermanentFocus();
      }
      // The Simple API is set up so that the user never has to call prepare.
      prepare();
      // Player should now be in state 1. (If prepare failed, we are in state 0.)
    }
  }

  /**
   * This method relies on FroyoUtil as Focus is only available on API Level 8
   */
  private void requestPermanentFocus() {
    // Request permanent focus on music stream
    focusOn = (FroyoUtil.focusRequestGranted(am, afChangeListener)) ? true : false;
    if (!focusOn)
      form.dispatchErrorOccurredEvent(this, "Source",
          ErrorMessages.ERROR_UNABLE_TO_FOCUS_MEDIA, sourcePath);
  }

  /**
   * Reports whether the media is playing.
   */
  @SimpleProperty(
      description = "Reports whether the media is playing",
      category = PropertyCategory.BEHAVIOR)
  public boolean IsPlaying() {
    if (playerState == State.PREPARED || playerState == State.PLAYING) {
      return player.isPlaying();
    }
    return false;
  }

  /**
   * Reports whether the playing should loop.
   */
  @SimpleProperty(
      description = "If true, the player will loop when it plays. Setting Loop while the player " +
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
    if (playerState == State.PREPARED || playerState == State.PLAYING || playerState == State.PAUSED_BY_USER) {
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
    if (playerState == State.PREPARED || playerState == State.PLAYING || playerState == State.PAUSED_BY_USER) {
      if (vol > 100 || vol < 0) {
        throw new IllegalArgumentError("Volume must be set to a number between 0 and 100");
      }
      player.setVolume(((float) vol) / 100, ((float) vol) / 100);
    }
  }

  /**
   * Gets the policy whether playing should only work in foreground.
   *
   * @return playOnlyInForeground
   */
  @SimpleProperty(
      description = "If true, the player will pause playing when leaving the current screen; " +
          "if false (default option), the player continues playing"+
          " whenever the current screen is displaying or not.",
      category = PropertyCategory.BEHAVIOR)
  public boolean PlayOnlyInForeground() {
    return playOnlyInForeground;
  }

  /**
   * Sets the property PlayOnlyInForeground to true or false.
   *
   * @param shouldForeground determines whether plays only in foreground or always.
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void PlayOnlyInForeground(boolean shouldForeground) {
    playOnlyInForeground = shouldForeground;
  }

  /**
   * Plays the media.  If it was previously paused, the playing is resumed.
   * If it was previously stopped, it starts from the beginning.
   */
  @SimpleFunction
  public void Start() {
    if (audioFocusSupported && !focusOn) {
      requestPermanentFocus();
    }
    if (playerState == State.PREPARED || playerState == State.PLAYING || playerState == State.PAUSED_BY_USER || playerState == State.PAUSED_BY_EVENT ) {
      player.setLooping(loop);
      player.start();
      playerState = State.PLAYING;
      // Player should now be in state 2(PLAYING)
    }
  }

  /**
   * Suspends playing the media if it is playing.
   */
  @SimpleFunction
  public void Pause() {
    if (player == null) return; //Do nothing if the player is not
    boolean wasPlaying = player.isPlaying();
    if (playerState == State.PLAYING) {
      player.pause();
      if (wasPlaying) {
        playerState = State.PAUSED_BY_USER;
        // Player should now be in state 3(PAUSED_BY_USER).
      }
    }
  }

  /**
   * Pauses when leaving the screen or losing focus. Public so that in can be called from
   * FroyoUtil
   */
  public void pause() {
    if (player == null) return; //Do nothing if the player is not playing
    if (playerState == State.PLAYING) {
      player.pause();
      playerState = State.PAUSED_BY_EVENT;
      // Player should now be in state 4(PAUSED_BY_EVENT).
    }
  }

  /**
   * Stops playing the media and seeks to the beginning of the song.
   */
  @SimpleFunction
  public void Stop() {
    if (audioFocusSupported && focusOn) {
      abandonFocus();
    }
    if (playerState == State.PLAYING || playerState == State.PAUSED_BY_USER || playerState == State.PAUSED_BY_EVENT) {
      player.stop();
      prepare();
      if (player != null) {     // If prepare fails, the player is released and set to null
        player.seekTo(0);       // So we cannot seek
      }
      // Player should now be in state 1(PREPARED). (If prepare failed, we are in state 0 (INITIAL).)
    }
  }

  /**
   * This method relies on FroyoUtil as Focus is only available on API Level 8
   */
  private void abandonFocus() {
    // Abandon focus
    FroyoUtil.abandonFocus(am, afChangeListener);
    focusOn = false;
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
      playerState = State.PREPARED;
    } catch (IOException ioe) {
      player.release();
      player = null;
      playerState = State.INITIAL;
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
    //Once you've finished playback be sure to call abandonAudioFocus() according to Android developer reference.
    if (audioFocusSupported && focusOn) {
      abandonFocus();
    }

    EventDispatcher.dispatchEvent(this, "Completed");
  }

  /**
   * Indicates that the other player has requested the focus of media
   */
  @SimpleEvent(description = "This event is signaled when another player has started" +
      " (and the current player is playing or paused, but not stopped).")
  public void OtherPlayerStarted() {
    EventDispatcher.dispatchEvent(this, "OtherPlayerStarted");
  }

  // OnResumeListener implementation
  @Override
  public void onResume() {
    if (playOnlyInForeground && playerState == State.PAUSED_BY_EVENT) {
      Start();
    }
  }

  // OnPauseListener implementation

  @Override
  public void onPause() {
    if (player == null) return; //Do nothing if the player is not ready
    if (playOnlyInForeground && player.isPlaying()) {
      pause();
    }
  }

  @Override
  public void onStop() {
    if (player == null) return; //Do nothing if the player is not
    if (playOnlyInForeground && player.isPlaying()) {
      pause();
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
    if (audioFocusSupported && focusOn) {
      abandonFocus();
    }
    if ((player != null) && (playerState != State.INITIAL)) {
      player.stop();
    }
    playerState = State.INITIAL;
    if (player != null) {
      player.release();
      player = null;
    }
    vibe.cancel();
  }
}
