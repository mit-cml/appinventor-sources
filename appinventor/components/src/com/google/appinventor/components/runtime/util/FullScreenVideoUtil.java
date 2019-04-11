// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.VideoPlayer;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.R;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.VideoView;

import java.io.IOException;

/**
 * Used by the {@link com.google.appinventor.components.runtime.Form} class to
 * display videos in fullscreen.
 *
 * @author Vance Turnewitsch
 */
public class FullScreenVideoUtil implements OnCompletionListener,
    OnPreparedListener {

  // Constants
  public static final int FULLSCREEN_VIDEO_DIALOG_FLAG = 189;

  public static final int FULLSCREEN_VIDEO_ACTION_SEEK = 190;

  public static final int FULLSCREEN_VIDEO_ACTION_PLAY = 191;

  public static final int FULLSCREEN_VIDEO_ACTION_PAUSE = 192;

  public static final int FULLSCREEN_VIDEO_ACTION_STOP = 193;

  public static final int FULLSCREEN_VIDEO_ACTION_SOURCE = 194;

  public static final int FULLSCREEN_VIDEO_ACTION_FULLSCREEN = 195;

  public static final int FULLSCREEN_VIDEO_ACTION_DURATION = 196;

  public static final String VIDEOPLAYER_FULLSCREEN = "FullScreenKey";

  public static final String VIDEOPLAYER_PLAYING = "PlayingKey";

  public static final String VIDEOPLAYER_POSITION = "PositionKey";

  public static final String VIDEOPLAYER_SOURCE = "SourceKey";

  public static final String ACTION_SUCESS = "ActionSuccess";

  public static final String ACTION_DATA = "ActionData";

  // The Dialog and other components used in the Dialog for displaying the
  // video.

  private Dialog mFullScreenVideoDialog;
  private FrameLayout mFullScreenVideoHolder;
  private VideoView mFullScreenVideoView;
  private CustomMediaController mFullScreenVideoController;
  private FrameLayout.LayoutParams mMediaControllerParams = new FrameLayout.LayoutParams(
      LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);

  private Form mForm;

  // The player whose video is currently being shown.
  private VideoPlayer mFullScreenPlayer = null;

  // The data passed in by the player that requested
  // fullscreen display of its video.
  private Bundle mFullScreenVideoBundle;

  // Used for showing a preview of a paused video.
  private Handler mHandler;

  /**
   * @param form
   *          The {@link com.google.appinventor.components.runtime.Form} that
   *          this FullScreenVideoUtil will use.
   * @param handler
   *          A {@link android.os.Handler} created on the UI thread. Used for
   *          displaying a preview of a paused video.
   */
  public FullScreenVideoUtil(Form form, Handler handler) {

    mForm = form;
    mHandler = handler;

    if (SdkLevel.getLevel() > SdkLevel.LEVEL_DONUT) {
      mFullScreenVideoDialog = new Dialog(mForm,
          R.style.Theme_NoTitleBar_Fullscreen) {
          public void onBackPressed() {
            // Allows the user to force exiting full-screen.
            Bundle values = new Bundle();
            values.putInt(VIDEOPLAYER_POSITION,
              mFullScreenVideoView.getCurrentPosition());
            values.putBoolean(VIDEOPLAYER_PLAYING,
              mFullScreenVideoView.isPlaying());
            values.putString(VIDEOPLAYER_SOURCE,
              mFullScreenVideoBundle.getString(VIDEOPLAYER_SOURCE));
            mFullScreenPlayer.fullScreenKilled(values);
              super.onBackPressed();
          }

          public void onStart() {
            super.onStart();
            // Prepare the Dialog media.
            startDialog();
          }
        };
    } else {
      mFullScreenVideoDialog = new Dialog(mForm,
          R.style.Theme_NoTitleBar_Fullscreen) {
          protected void onStop() {
            Bundle values = new Bundle();
            values.putInt(VIDEOPLAYER_POSITION,
              mFullScreenVideoView.getCurrentPosition());
            values.putBoolean(VIDEOPLAYER_PLAYING,
              mFullScreenVideoView.isPlaying());
            values.putString(VIDEOPLAYER_SOURCE,
             mFullScreenVideoBundle.getString(VIDEOPLAYER_SOURCE));
            mFullScreenPlayer.fullScreenKilled(values);
            super.onStop();
          }

          public void onStart() {
            super.onStart();
            // Prepare the Dialog media.
            startDialog();
          }
        };
    }
  }

  /**
   * Perform some action and get a result. The data to pass in and the result
   * returned are controlled by what the action is.
   *
   * @param action
   *          Can be any of the following:
   *          <ul>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_DURATION}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_FULLSCREEN}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_PAUSE}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_PLAY}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_SEEK}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_SOURCE}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_STOP}
   *          </li>
   *          </ul>
   * @param source
   *          The VideoPlayer to use in some actions.
   * @param data
   *          Used by the method. This object varies depending on the action.
   * @return Varies depending on what action was passed in.
   */
  public synchronized Bundle performAction(int action, VideoPlayer source,
      Object data) {
    Log.i("Form.fullScreenVideoAction", "Actions:" + action + " Source:"
        + source + ": Current Source:" + mFullScreenPlayer + " Data:" + data);
    Bundle result = new Bundle();
    result.putBoolean(ACTION_SUCESS, true);
    if (source == mFullScreenPlayer) {
      switch (action) {
      case FULLSCREEN_VIDEO_ACTION_FULLSCREEN:
        return doFullScreenVideoAction(source, (Bundle) data);
      case FULLSCREEN_VIDEO_ACTION_PAUSE:
        if (showing()) {
          mFullScreenVideoView.pause();
          return result;
        }
        result.putBoolean(ACTION_SUCESS, false);
        return result;
      case FULLSCREEN_VIDEO_ACTION_PLAY:
        if (showing()) {
          mFullScreenVideoView.start();
          return result;
        }
        result.putBoolean(ACTION_SUCESS, false);
        return result;
      case FULLSCREEN_VIDEO_ACTION_SEEK:
        if (showing()) {
          mFullScreenVideoView.seekTo((Integer) data);
          return result;
        }
        result.putBoolean(ACTION_SUCESS, false);
        return result;
      case FULLSCREEN_VIDEO_ACTION_STOP:
        if (showing()) {
          mFullScreenVideoView.stopPlayback();
          return result;
        }
        result.putBoolean(ACTION_SUCESS, false);
        return result;
      case FULLSCREEN_VIDEO_ACTION_SOURCE:
        if (showing()) {
          result.putBoolean(ACTION_SUCESS,setSource((String) data, true));
          return result;
        }
        result.putBoolean(ACTION_SUCESS, false);
        return result;
      case FULLSCREEN_VIDEO_ACTION_DURATION:
        if (showing()) {
          result.putInt(ACTION_DATA, mFullScreenVideoView.getDuration());
          return result;
        }
        result.putBoolean(ACTION_SUCESS, false);
        return result;
      }
    } else if (action == FULLSCREEN_VIDEO_ACTION_FULLSCREEN) {
      // There may be a dialog already being shown.
      if (showing() && mFullScreenPlayer != null) {
        Bundle values = new Bundle();
        values.putInt(VIDEOPLAYER_POSITION,
            mFullScreenVideoView.getCurrentPosition());
        values.putBoolean(VIDEOPLAYER_PLAYING,
            mFullScreenVideoView.isPlaying());
        values.putString(VIDEOPLAYER_SOURCE,
            mFullScreenVideoBundle
                .getString(VIDEOPLAYER_SOURCE));
        mFullScreenPlayer.fullScreenKilled(values);
      }
      return doFullScreenVideoAction(source, (Bundle) data);
    }

    // This should never be called.
    result.putBoolean(ACTION_SUCESS, false);
    return result;
  }

  /*
   * Displays or hides a full-screen video.
   */
  private Bundle doFullScreenVideoAction(VideoPlayer source, Bundle data) {
    Log.i("Form.doFullScreenVideoAction", "Source:" + source + " Data:" + data);
    Bundle result = new Bundle();
    result.putBoolean(ACTION_SUCESS, true);
    if (data.getBoolean(VIDEOPLAYER_FULLSCREEN) == true) {
      mFullScreenPlayer = source;
      mFullScreenVideoBundle = data;
      if (!mFullScreenVideoDialog.isShowing()) {
        mForm.showDialog(FULLSCREEN_VIDEO_DIALOG_FLAG);
        return result;
      } else {
        mFullScreenVideoView.pause();
        result.putBoolean(ACTION_SUCESS, setSource(
            mFullScreenVideoBundle.getString(VIDEOPLAYER_SOURCE),false));
        return result;
      }
    } else {
      if (showing()) {
        result.putBoolean(VIDEOPLAYER_PLAYING,
            mFullScreenVideoView.isPlaying());
        result.putInt(VIDEOPLAYER_POSITION,
            mFullScreenVideoView.getCurrentPosition());
        result.putString(VIDEOPLAYER_SOURCE,
            mFullScreenVideoBundle
                .getString(VIDEOPLAYER_SOURCE));

        mFullScreenPlayer = null;
        mFullScreenVideoBundle = null;

        mForm.dismissDialog(FULLSCREEN_VIDEO_DIALOG_FLAG);
        return result;
      }
    }
    result.putBoolean(ACTION_SUCESS, false);
    return result;
  }

  /**
   * Creates the dialog for displaying a fullscreen VideoView.
   *
   * @return The created Dialog
   */
  public Dialog createFullScreenVideoDialog() {

    if (mFullScreenVideoBundle == null)
      Log.i("Form.createFullScreenVideoDialog", "mFullScreenVideoBundle is null");

    mFullScreenVideoView = new VideoView(mForm);
    mFullScreenVideoHolder = new FrameLayout(mForm);
    mFullScreenVideoController = new CustomMediaController(mForm);

    mFullScreenVideoView.setId(mFullScreenVideoView.hashCode());
    mFullScreenVideoHolder.setId(mFullScreenVideoHolder.hashCode());

    mFullScreenVideoView.setMediaController(mFullScreenVideoController);

    mFullScreenVideoView.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View arg0, MotionEvent arg1) {
        Log.i("FullScreenVideoUtil..onTouch", "Video Touched!!");
        return false;
      }
    });
    mFullScreenVideoController.setAnchorView(mFullScreenVideoView);

    String orientation = mForm.ScreenOrientation();
    if (orientation.equals("landscape")
        || orientation.equals("sensorLandscape")
        || orientation.equals("reverseLandscape")) {
      mFullScreenVideoView.setLayoutParams(new FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.WRAP_CONTENT,
          FrameLayout.LayoutParams.FILL_PARENT, Gravity.CENTER));
    } else {
      mFullScreenVideoView.setLayoutParams(new FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.FILL_PARENT,
          FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    }
    mFullScreenVideoHolder
        .setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.FILL_PARENT,
            ViewGroup.LayoutParams.FILL_PARENT));

    mFullScreenVideoHolder.addView(mFullScreenVideoView);

    // Add the MediaController to the Dialog
    mFullScreenVideoController.addTo(mFullScreenVideoHolder,
        mMediaControllerParams);

    mFullScreenVideoDialog.setContentView(mFullScreenVideoHolder);
    return mFullScreenVideoDialog;
  }

  /**
   * Call just before displaying a fullscreen video Dialog. This method
   * sets up some listeners.
   *
   * @param dia
   *          The dialog that will display the video.
   */
  public void prepareFullScreenVideoDialog(Dialog dia) {
    mFullScreenVideoView.setOnPreparedListener(this);
    mFullScreenVideoView.setOnCompletionListener(this);
  }

  /**
   * @return True if the internal Dialog has been created. False otherwise.
   */
  public boolean dialogInitialized() {
    return mFullScreenVideoDialog != null;
  }

  /**
   * @return True if {@link FullScreenVideoUtil#dialogInitialized()} is true and
   *         the Dialog is showing. False otherwise.
   */
  public boolean showing() {
    return dialogInitialized() && mFullScreenVideoDialog.isShowing();
  }

  /**
   * Sets the source to be used by the fullscreen video Dialog. This method
   * also attempts to load the internal VideoView with the source.
   *
   * @param source
   *          The source path to use. The {@link MediaUtil} is used to load the
   *          source.
   * @param clearSeek
   *          If True, the video will start playing at position zero. If False,
   *          the video will start playing from the
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#VIDEOPLAYER_POSITION}
   *          value of the Bundle passed in the
   *          {@link FullScreenVideoUtil#performAction(int, VideoPlayer, Object)}
   *          or {@link FullScreenVideoUtil#createFullScreenVideoDialog()}
   * @return True if the video was successfully loaded. False otherwise.
   */
  public boolean setSource(String source, boolean clearSeek) {
    try {
      if (clearSeek) {
        mFullScreenVideoBundle.putInt(VIDEOPLAYER_POSITION,
            0);
      }
      MediaUtil.loadVideoView(mFullScreenVideoView, mForm, (String) source);

      mFullScreenVideoBundle.putString(VIDEOPLAYER_SOURCE,
          source);
      return true;
    } catch (PermissionException e) {
      mForm.dispatchPermissionDeniedEvent(mFullScreenPlayer, "Source", e);
      return false;
    } catch (IOException e) {
      mForm.dispatchErrorOccurredEvent(mFullScreenPlayer, "Source",
          ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, source);
      return false;
    }
  }

  /**
   * Called when the video has finished playing.
   */
  @Override
  public void onCompletion(MediaPlayer arg0) {
    if (mFullScreenPlayer != null) {
      mFullScreenPlayer.Completed();
    }
  }

  /**
   * Called when the video has been loaded.
   */
  @Override
  public void onPrepared(MediaPlayer arg0) {
    Log.i(
        "FullScreenVideoUtil..onPrepared",
        "Seeking to:"
            + mFullScreenVideoBundle
                .getInt(VIDEOPLAYER_POSITION));
    mFullScreenVideoView.seekTo(mFullScreenVideoBundle
        .getInt(VIDEOPLAYER_POSITION));
    if (mFullScreenVideoBundle
        .getBoolean(VIDEOPLAYER_PLAYING)) {
      mFullScreenVideoView.start();
    } else {
      mFullScreenVideoView.start();
      mHandler.postDelayed(new Runnable() {

        @Override
        public void run() {
          mFullScreenVideoView.pause();
        }
      }, 100);
    }
  }

  /**
   * Called when the Dialog is about to be shown.
   */
  public void startDialog() {
    try {
      MediaUtil.loadVideoView(mFullScreenVideoView, mForm,
          mFullScreenVideoBundle
              .getString(VIDEOPLAYER_SOURCE));
    } catch (PermissionException e) {
      mForm.dispatchPermissionDeniedEvent(mFullScreenPlayer, "Source", e);
    } catch (IOException e) {
      mForm.dispatchErrorOccurredEvent(mFullScreenPlayer, "Source",
          ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, mFullScreenVideoBundle
              .getString(VIDEOPLAYER_SOURCE));
      return;
    }
  }
}
