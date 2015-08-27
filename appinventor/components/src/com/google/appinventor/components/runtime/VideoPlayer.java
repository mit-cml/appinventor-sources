// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
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
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FullScreenVideoUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.IOException;

/**
 * TODO: This copies the video from the application's asset directory to a temp
 * file, because the Android VideoView class can't handle assets.
 * Marco plans to include that feature in future releases.
 */

/**
 * TODO: Check that player is prepared (is this necessary?)  See if we need to
 * use isPlaying, onErrorListener, and onPreparedListener.
 */

/**
 * TODO: Set up the touch (and trackball?) Simple event handlers so that they
 * interact well with the videoView implementation, i.e., defining handlers
 * should (probably) override videoView making the Mediacontroller appear.
 */

/**
 * TODO: Remove writes of state debugging info to the log after we're sure
 * things are working solidly.
 */

/**
 * TODO: The resizing of the VideoPlayer at runtime does not work well on some
 * devices such as the Motorola Droid. The behavior is almost random when resizing
 * the VideoPlayer on such devices. When App Inventor includes the features to
 * restrict certain devices, the VideoPlayer should be updated.
 */

/**
 * Implementation of VideoPlayer, using {@link android.widget.VideoView}.
 *
 * @author halabelson@google.com (Hal Abelson)
 */

@DesignerComponent(
    version = YaVersion.VIDEOPLAYER_COMPONENT_VERSION,
    description = "A multimedia component capable of playing videos. "
        + "When the application is run, the VideoPlayer will be displayed as a "
        + "rectangle on-screen.  If the user touches the rectangle, controls will "
        + "appear to play/pause, skip ahead, and skip backward within the video.  "
        + "The application can also control behavior by calling the "
        + "<code>Start</code>, <code>Pause</code>, and <code>SeekTo</code> methods.  "
        + "<p>Video files should be in "
        + "3GPP (.3gp) or MPEG-4 (.mp4) formats.  For more details about legal "
        + "formats, see "
        + "<a href=\"http://developer.android.com/guide/appendix/media-formats.html\""
        + " target=\"_blank\">Android Supported Media Formats</a>.</p>"
        + "<p>App Inventor for Android only permits video files under 1 MB and "
        + "limits the total size of an application to 5 MB, not all of which is "
        + "available for media (video, audio, and sound) files.  If your media "
        + "files are too large, you may get errors when packaging or installing "
        + "your application, in which case you should reduce the number of media "
        + "files or their sizes.  Most video editing software, such as Windows "
        + "Movie Maker and Apple iMovie, can help you decrease the size of videos "
        + "by shortening them or re-encoding the video into a more compact format.</p>"
        + "<p>You can also set the media source to a URL that points to a streaming video, "
        + "but the URL must point to the video file itself, not to a program that plays the video.",
    category = ComponentCategory.MEDIA)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class VideoPlayer extends AndroidViewComponent implements
    OnDestroyListener, Deleteable, OnCompletionListener, OnErrorListener,
    OnPreparedListener {

  /*
   * Video clip with player controls (touch it to activate)
   */

  private final ResizableVideoView videoView;

  private String sourcePath; // name of media source

  private boolean inFullScreen = false;

  // The VideoView does not always start playing if Start is called
  // shortly after the source is set. These flags are used to fix this
  // problem.
  private boolean mediaReady = false;

  private boolean delayedStart = false;

  private MediaPlayer mPlayer;

  private final Handler androidUIHandler = new Handler();

  /**
   * Creates a new VideoPlayer component.
   *
   * @param container
   */
  public VideoPlayer(ComponentContainer container) {
    super(container);
    container.$form().registerForOnDestroy(this);
    videoView = new ResizableVideoView(container.$context());
    videoView.setMediaController(new MediaController(container.$context()));
    videoView.setOnCompletionListener(this);
    videoView.setOnErrorListener(this);
    videoView.setOnPreparedListener(this);

    // add the component to the designated container
    container.$add(this);
    // set a default size
    container.setChildWidth(this,
        ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH);
    container.setChildHeight(this,
        ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT);

    // Make volume buttons control media, not ringer.
    container.$form().setVolumeControlStream(AudioManager.STREAM_MUSIC);

    sourcePath = "";
  }

  @Override
  public View getView() {
    return videoView;
  }

  /**
   * Sets the video source.
   *
   * <p/>
   * See {@link MediaUtil#determineMediaSource} for information about what a
   * path can be.
   *
   * @param path
   *          the path to the video source
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty(
      description = "The \"path\" to the video.  Usually, this will be the "
          + "name of the video file, which should be added in the Designer.",
      category = PropertyCategory.BEHAVIOR)
  public void Source(String path) {
    if (inFullScreen) {
      container.$form().fullScreenVideoAction(
          FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_SOURCE, this, path);
    } else {
      sourcePath = (path == null) ? "" : path;

      // The source may change for the MediaPlayer, and
      // getVideoWidth or getVideoHeight may be called
      // creating an error in ResizableVideoView.
      videoView.invalidateMediaPlayer(true);

      // Clear the previous video.
      if (videoView.isPlaying()) {
        videoView.stopPlayback();
      }
      videoView.setVideoURI(null);
      videoView.clearAnimation();

      if (sourcePath.length() > 0) {
        Log.i("VideoPlayer", "Source path is " + sourcePath);

        try {
          mediaReady = false;
          MediaUtil.loadVideoView(videoView, container.$form(), sourcePath);
        } catch (IOException e) {
          container.$form().dispatchErrorOccurredEvent(this, "Source",
              ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
          return;
        }

        Log.i("VideoPlayer", "loading video succeeded");
      }
    }
  }

  /**
   * Plays the media specified by the source. These won't normally be used in
   * the most elementary applications, because videoView brings up its own
   * player controls when the video is touched.
   */
  @SimpleFunction(description = "Starts playback of the video.")
  public void Start() {
    Log.i("VideoPlayer", "Calling Start");
    if (inFullScreen) {
      container.$form().fullScreenVideoAction(
          FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PLAY, this, null);
    } else {
      if (mediaReady) {
        videoView.start();
      } else {
        delayedStart = true;
      }
    }
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
      description = "Sets the volume to a number between 0 and 100. " +
      "Values less than 0 will be treated as 0, and values greater than 100 " +
      "will be treated as 100.")
  public void Volume(int vol) {
    // clip volume to range [0, 100]
    vol = Math.max(vol, 0);
    vol = Math.min(vol, 100);
    if (mPlayer != null) {
      mPlayer.setVolume(((float) vol) / 100, ((float) vol) / 100);
    }
  }


  /**
   * Method for starting the VideoPlayer once the media has been loaded.
   * Not visible to users.
   */
  public void delayedStart() {
    delayedStart = true;
    Start();
  }

  @SimpleFunction(
      description = "Pauses playback of the video.  Playback can be resumed "
      + "at the same location by calling the <code>Start</code> method.")
  public void Pause() {
    Log.i("VideoPlayer", "Calling Pause");
    if (inFullScreen) {
      container.$form().fullScreenVideoAction(
          FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PAUSE, this, null);
      delayedStart = false;
    } else {
      delayedStart = false;
      videoView.pause();
    }
  }

  @SimpleFunction(
      description = "Seeks to the requested time (specified in milliseconds) in the video. " +
      "Note that if the video is paused, the frame shown will not be updated by the seek. ")
  public void SeekTo(int ms) {
    Log.i("VideoPlayer", "Calling SeekTo");
    if (ms < 0) {
      ms = 0;
    }
    if (inFullScreen) {
      container.$form().fullScreenVideoAction(
          FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_SEEK, this, ms);
    } else {
      // There is no harm if the milliseconds is longer than the duration.
      videoView.seekTo(ms);
    }
  }

  @SimpleFunction(
      description = "Returns duration of the video in milliseconds.")
  public int GetDuration() {
    Log.i("VideoPlayer", "Calling GetDuration");
    if (inFullScreen) {
      Bundle result = container.$form().fullScreenVideoAction(
          FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_DURATION, this, null);
      if (result.getBoolean(FullScreenVideoUtil.ACTION_SUCESS)) {
        return result.getInt(FullScreenVideoUtil.ACTION_DATA);
      } else {
        return 0;
      }
    } else {
      return videoView.getDuration();
    }
  }

  // OnCompletionListener implementation

  @Override
  public void onCompletion(MediaPlayer m) {
    Completed();
  }

  /**
   * Indicates that the video has reached the end
   */
  @SimpleEvent
  public void Completed() {
    EventDispatcher.dispatchEvent(this, "Completed");
  }

  // OnErrorListener implementation

  @Override
  public boolean onError(MediaPlayer m, int what, int extra) {

    // The ResizableVideoView onMeasure method attempts to use the MediaPlayer
    // to measure
    // the VideoPlayer; but in the event of an error, the MediaPlayer
    // may report dimensions of zero video width and height.
    // Since VideoPlayer currently (7/10/2012) sets its size always
    // to some non-zero number, the MediaPlayer is invalidated here
    // to prevent onMeasure from setting width and height as zero.
    videoView.invalidateMediaPlayer(true);

    delayedStart = false;
    mediaReady = false;

    Log.e("VideoPlayer",
        "onError: what is " + what + " 0x" + Integer.toHexString(what)
            + ", extra is " + extra + " 0x" + Integer.toHexString(extra));
    container.$form().dispatchErrorOccurredEvent(this, "Source",
        ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
    return true;
  }

  @Override
  public void onPrepared(MediaPlayer newMediaPlayer) {
    mediaReady = true;
    delayedStart = false;
    mPlayer = newMediaPlayer;
    videoView.setMediaPlayer(mPlayer, true);
    if (delayedStart) {
      Start();
    }
  }

  @SimpleEvent(description = "The VideoPlayerError event is no longer used. "
      + "Please use the Screen.ErrorOccurred event instead.",
      userVisible = false)
  public void VideoPlayerError(String message) {
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
    if (videoView.isPlaying()) {
      videoView.stopPlayback();
    }
    videoView.setVideoURI(null);
    videoView.clearAnimation();

    delayedStart = false;
    mediaReady = false;

    if (inFullScreen) {
      Bundle data = new Bundle();
      data.putBoolean(FullScreenVideoUtil.VIDEOPLAYER_FULLSCREEN, false);
      container.$form().fullScreenVideoAction(
          FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_FULLSCREEN, this, data);
    }
  }

  /**
   * Returns the component's horizontal width, measured in pixels.
   *
   * @return width in pixels
   */

  @Override
  @SimpleProperty
  public int Width() {
    return super.Width();
  }

  /**
   * Specifies the component's horizontal width, measured in pixels.
   *
   * @param width in pixels
   */

  @Override
  @SimpleProperty(userVisible = true)
  public void Width(int width) {
    super.Width(width);

    // Forces a layout of the ResizableVideoView
    videoView.changeVideoSize(width, videoView.forcedHeight);
  }

  /**
   * Returns the component's vertical height, measured in pixels.
   *
   * @return height in pixels
   */

  @Override
  @SimpleProperty
  public int Height() {
    return super.Height();
  }

  /**
   * Specifies the component's vertical height, measured in pixels.
   *
   * @param height
   *          in pixels
   */
  @Override
  @SimpleProperty(userVisible = true)
  public void Height(int height) {
    super.Height(height);

    // Forces a layout of the ResizableVideoView
    videoView.changeVideoSize(videoView.forcedWidth, height);
  }

  /**
   * Returns whether the VideoPlayer's video is currently being
   * shown in fullscreen mode or not.
   * @return True if video is being shown in fullscreen. False otherwise.
   */
  @SimpleProperty
  public boolean FullScreen() {
    return inFullScreen;
  }

  /**
   * Sets whether the video should be shown in fullscreen or not.
   *
   * @param value If True, the video will be shown in fullscreen.
   * If False and {@link VideoPlayer#FullScreen()} returns True, fullscreen
   * mode will be exited. If False and {@link VideoPlayer#FullScreen()}
   * returns False, nothing occurs.
   */
  @SimpleProperty(userVisible = true)
  public void FullScreen(boolean value) {

    if (value && (SdkLevel.getLevel() <= SdkLevel.LEVEL_DONUT)) {
      container.$form().dispatchErrorOccurredEvent(this, "FullScreen(true)",
        ErrorMessages.ERROR_VIDEOPLAYER_FULLSCREEN_UNSUPPORTED);
      return;
    }

    if (value != inFullScreen) {
      if (value) {
        Bundle data = new Bundle();
        data.putInt(FullScreenVideoUtil.VIDEOPLAYER_POSITION,
            videoView.getCurrentPosition());
        data.putBoolean(FullScreenVideoUtil.VIDEOPLAYER_PLAYING,
            videoView.isPlaying());
        videoView.pause();
        data.putBoolean(FullScreenVideoUtil.VIDEOPLAYER_FULLSCREEN, true);
        data.putString(FullScreenVideoUtil.VIDEOPLAYER_SOURCE, sourcePath);
        Bundle result = container.$form().fullScreenVideoAction(
            FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_FULLSCREEN, this, data);
        if (result.getBoolean(FullScreenVideoUtil.ACTION_SUCESS)) {
          inFullScreen = true;
        } else {
          inFullScreen = false;
          container.$form().dispatchErrorOccurredEvent(this, "FullScreen",
              ErrorMessages.ERROR_VIDEOPLAYER_FULLSCREEN_UNAVAILBLE, "");
        }
      } else {
        Bundle values = new Bundle();
        values.putBoolean(FullScreenVideoUtil.VIDEOPLAYER_FULLSCREEN, false);
        Bundle result = container.$form().fullScreenVideoAction(
            FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_FULLSCREEN, this,
            values);
        if (result.getBoolean(FullScreenVideoUtil.ACTION_SUCESS)) {
          fullScreenKilled((Bundle) result);
        } else {
          inFullScreen = true;
          container.$form().dispatchErrorOccurredEvent(this, "FullScreen",
              ErrorMessages.ERROR_VIDEOPLAYER_FULLSCREEN_CANT_EXIT, "");
        }
      }
    }
  }

  /**
   * Notify this VideoPlayer that its video is no longer being shown
   * in fullscreen.
   * @param data See {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil}
   * for an example of what data should contain.
   */
  public void fullScreenKilled(Bundle data) {
    inFullScreen = false;
    String newSource = data.getString(FullScreenVideoUtil.VIDEOPLAYER_SOURCE);
    if (!newSource.equals(sourcePath)) {
      Source(newSource);
    }
    videoView.setVisibility(View.VISIBLE);
    videoView.requestLayout();
    SeekTo(data.getInt(FullScreenVideoUtil.VIDEOPLAYER_POSITION));
    if (data.getBoolean(FullScreenVideoUtil.VIDEOPLAYER_PLAYING)) {
      Start();
    }
  }

  /**
   * Get the value passed in {@link VideoPlayer#Width(int)}
   * @return The width value.
   */
  public int getPassedWidth() {
    return videoView.forcedWidth;
  }

  /**
   * Get the value passed in {@link VideoPlayer#Height(int)}
   * @return The height value.
   */
  public int getPassedHeight() {
    return videoView.forcedHeight;
  }

  /**
   * Extends VideoView to allow resizing of the view that ignores the aspect
   * ratio of the video being played.
   *
   * @author Vance Turnewitsch
   */
  class ResizableVideoView extends VideoView {

    private MediaPlayer mVideoPlayer;

    /*
     * Used by onMeasure to determine whether the mVideoPlayer should be used to
     * measure the view.
     */
    private Boolean mFoundMediaPlayer = false;

    /**
     * Used by onMeasure to determine what type of size the VideoPlayer should
     * be.
     */
    public int forcedWidth = LENGTH_PREFERRED;

    /**
     * Used by onMeasure to determine what type of size the VideoPlayer should
     * be.
     */
    public int forcedHeight = LENGTH_PREFERRED;

    public ResizableVideoView(Context context) {
      super(context);
    }

    public void onMeasure(int specwidth, int specheight) {
      onMeasure(specwidth, specheight, 0);
    }

    private void onMeasure(final int specwidth, final int specheight, final int trycount) {
      // Since super.onMeasure uses the aspect ratio of the video being
      // played, it is not called.
      // http://grepcode.com/file/repository.grepcode.com/java/ext/
      // com.google.android/android/2.2.2_r1/android/widget/VideoView.java
      // #VideoView.onMeasure%28int%2Cint%29

      // Log messages in this method are not commented out for testing the
      // changes
      // on other devices.
      boolean scaleHeight = false;
      boolean scaleWidth = false;
      float deviceDensity = container.$form().deviceDensity();
      Log.i("VideoPlayer..onMeasure", "Device Density = " + deviceDensity);
      Log.i("VideoPlayer..onMeasure", "AI setting dimensions as:" + forcedWidth
          + ":" + forcedHeight);
      Log.i("VideoPlayer..onMeasure",
          "Dimenions from super>>" + MeasureSpec.getSize(specwidth) + ":"
              + MeasureSpec.getSize(specheight));

      // The VideoPlayer's dimensions must always be some non-zero number.
      int width = ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
      int height = ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;

      switch (forcedWidth) {
      case LENGTH_FILL_PARENT:
        switch (MeasureSpec.getMode(specwidth)) {
        case MeasureSpec.EXACTLY:
        case MeasureSpec.AT_MOST:
          width = MeasureSpec.getSize(specwidth);
          break;
        case MeasureSpec.UNSPECIFIED:
          try {
            width = ((View) getParent()).getMeasuredWidth();
          } catch (ClassCastException cast) {
            width = ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
          } catch (NullPointerException nullParent) {
            width = ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
          }
        }
        break;
      case LENGTH_PREFERRED:
        if (mFoundMediaPlayer) {
          try {
            width = mVideoPlayer.getVideoWidth();
            Log.i("VideoPlayer.onMeasure", "Got width from MediaPlayer>"
                + width);
          } catch (NullPointerException nullVideoPlayer) {
            Log.e(
                "VideoPlayer..onMeasure",
                "Failed to get MediaPlayer for width:\n"
                    + nullVideoPlayer.getMessage());
            width = ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
          }
        } else {
        }
        break;
      default:
        scaleWidth = true;
        width = forcedWidth;
      }

      if (forcedWidth <= LENGTH_PERCENT_TAG) {
        int cWidth = container.$form().Width();
        if (cWidth == 0 && trycount < 2) {
          Log.d("VideoPlayer...onMeasure", "Width not stable... trying again (onMeasure " + trycount + ")");
          androidUIHandler.postDelayed(new Runnable() {
              @Override
              public void run() {
                onMeasure(specwidth, specheight, trycount + 1);
              }
            }, 100);            // Try again in 1/10 of a second
          setMeasuredDimension(100, 100); // We have to set something or our caller is unhappy
          return;
        }
        width = (int) ((float) (cWidth * (- (width - LENGTH_PERCENT_TAG)) / 100) * deviceDensity);
      } else if (scaleWidth) {
        width = (int) ((float) width * deviceDensity);
      }

      switch (forcedHeight) {
      case LENGTH_FILL_PARENT:
        switch (MeasureSpec.getMode(specheight)) {
        case MeasureSpec.EXACTLY:
        case MeasureSpec.AT_MOST:
          height = MeasureSpec.getSize(specheight);
          break;
        case MeasureSpec.UNSPECIFIED:
          // Use height from ComponentConstants
          // The current measuring of components ignores FILL_PARENT for height,
          // and does not actually fill the height of the parent container.
        }
        break;
      case LENGTH_PREFERRED:
        if (mFoundMediaPlayer) {
          try {
            height = mVideoPlayer.getVideoHeight();
            Log.i("VideoPlayer.onMeasure", "Got height from MediaPlayer>"
                + height);
          } catch (NullPointerException nullVideoPlayer) {
            Log.e(
                "VideoPlayer..onMeasure",
                "Failed to get MediaPlayer for height:\n"
                    + nullVideoPlayer.getMessage());
            height = ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
          }
        }
        break;
      default:
        scaleHeight = true;
        height = forcedHeight;
      }

      if (forcedHeight <= LENGTH_PERCENT_TAG) {
        int cHeight = container.$form().Height();
        if (cHeight == 0 && trycount < 2) {
          Log.d("VideoPlayer...onMeasure", "Height not stable... trying again (onMeasure " + trycount + ")");
          androidUIHandler.postDelayed(new Runnable() {
              @Override
              public void run() {
                onMeasure(specwidth, specheight, trycount + 1);
              }
            }, 100);            // Try again in 1/10 of a second
          setMeasuredDimension(100, 100); // We have to set something or our caller is unhappy
          return;
        }
        height = (int) ((float) (cHeight * (- (height - LENGTH_PERCENT_TAG)) / 100) * deviceDensity);
      } else if (scaleHeight) {
        height = (int) ((float) height * deviceDensity);
      }

      // Forces the video playing in the VideoView to scale.
      // Some Android devices though will not scale the video playing.
      Log.i("VideoPlayer.onMeasure", "Setting dimensions to:" + width + "x"
          + height);
      getHolder().setFixedSize(width, height);

      setMeasuredDimension(width, height);
    }

    /**
     * Resize the view size and request a layout.
     */
    public void changeVideoSize(int newWidth, int newHeight) {
      forcedWidth = newWidth;
      forcedHeight = newHeight;

      forceLayout();
      invalidate();
    }

    /*
     * Used to keep onMeasure from using the mVideoPlayer in measuring.
     */
    public void invalidateMediaPlayer(boolean triggerRedraw) {
      mFoundMediaPlayer = false;
      mVideoPlayer = null;

      if (triggerRedraw) {
        forceLayout();
        invalidate();
      }
    }

    public void
        setMediaPlayer(MediaPlayer newMediaPlayer, boolean triggerRedraw) {
      mVideoPlayer = newMediaPlayer;
      mFoundMediaPlayer = true;

      if (triggerRedraw) {
        forceLayout();
        invalidate();
      }
    }
  }
}
