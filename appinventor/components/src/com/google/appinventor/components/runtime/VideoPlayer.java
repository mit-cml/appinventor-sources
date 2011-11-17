// Copyright 2008-2009 Google Inc. All Rights Reserved.

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
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
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
 * Implementation of VideoPlayer, using {@link android.widget.VideoView}.
 *
 * @author halabelson@google.com (Hal Abelson)
 */

@DesignerComponent(version = YaVersion.VIDEOPLAYER_COMPONENT_VERSION,
    description = "A multimedia component capable of playing videos. " +
    "When the application is run, the VideoPlayer will be displayed as a " +
    "rectangle on-screen.  If the user touches the rectangle, controls will " +
    "appear to play/pause, skip ahead, and skip backward within the video.  " +
    "The application can also control behavior by calling the " +
    "<code>Start</code>, <code>Pause</code>, and <code>SeekTo</code> methods.  " +
    "<p>Video files should be in Windows Media Video (.wmv) format, " +
    "3GPP (.3gp), or MPEG-4 (.mp4).  For more details about legal " +
    "formats, see " +
    "<a href=\"http://developer.android.com/guide/appendix/media-formats.html\"" +
    " target=\"_blank\">Android Supported Media Formats</a>.</p>" +
    "<p>App Inventor for Android only permits video files under 1 MB and " +
    "limits the total size of an application to 5 MB, not all of which is " +
    "available for media (video, audio, and sound) files.  If your media " +
    "files are too large, you may get errors when packaging or installing " +
    "your application, in which case you should reduce the number of media " +
    "files or their sizes.  Most video editing software, such as Windows " +
    "Movie Maker and Apple iMovie, can help you decrease the size of videos " +
    "by shortening them or re-encoding the video into a more compact format.</p>",
    category = ComponentCategory.MEDIA)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class VideoPlayer extends AndroidViewComponent
    implements OnDestroyListener, Deleteable, OnCompletionListener, OnErrorListener {

  /*
   * Video clip with player controls (touch it to activate)
   */

  private final VideoView videoView;

  private String sourcePath;     // name of media source

  /**
   * Creates a new VideoPlayer component.
   *
   * @param container
   */
  public VideoPlayer(ComponentContainer container) {
    super(container);
    container.$form().registerForOnDestroy(this);
    videoView = new VideoView(container.$context());
    videoView.setMediaController(new MediaController(container.$context()));
    videoView.setOnCompletionListener(this);
    videoView.setOnErrorListener(this);

    // add the component to the designated container
    container.$add(this);
    // set a default size
    container.setChildWidth(this, ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH);
    container.setChildHeight(this, ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT);

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
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path to the video source
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty(
      description = "The \"path\" to the video.  Usually, this will be the " +
      "name of the video file, which should be added in the Designer.",
      category = PropertyCategory.BEHAVIOR)
  public void Source(String path) {
    sourcePath = (path == null) ? "" : path;

    // Clear the previous video.
    if (videoView.isPlaying()) {
      videoView.stopPlayback();
    }
    videoView.setVideoURI(null);
    videoView.clearAnimation();

    if (sourcePath.length() > 0) {
      Log.i("VideoPlayer", "Source path is " + sourcePath);

      try {
        MediaUtil.loadVideoView(videoView, container.$form(), sourcePath);
      } catch (IOException e) {
        container.$form().dispatchErrorOccurredEvent(this, "Source",
            ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
        return;
      }

      Log.i("VideoPlayer", "loading video succeeded");
    }
  }

  /**
   * Plays the media specified by the source.  These won't normally be used in
   * the most elementary applications, because videoView brings up its own
   * player controls when the video is touched.
   */
  @SimpleFunction(
      description = "Starts playback of the video.")
  public void Start() {
    Log.i("VideoPlayer", "Calling Start");
    videoView.start();
  }

  @SimpleFunction(
      description = "Pauses playback of the video.  Playback can be resumed " +
      "at the same location by calling the <code>Start</code> method.")
  public void Pause() {
    Log.i("VideoPlayer", "Calling Pause");
    videoView.pause();
  }

  @SimpleFunction(
      description = "Seeks to the requested time (specified in milliseconds) in the video. " +
      "Note that if the video is paused, the frame shown will not be updated by the seek. ")
  public void SeekTo(int ms) {
    Log.i("VideoPlayer", "Calling SeekTo");
    if (ms < 0) {
      ms = 0;
    }
    // There is no harm if the milliseconds is longer than the duration.
    videoView.seekTo(ms);
  }

  @SimpleFunction(
      description = "Returns duration of the video in milliseconds.")
  public int GetDuration() {
    Log.i("VideoPlayer", "Calling GetDuration");
    return videoView.getDuration();
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
    Log.e("VideoPlayer", "onError: what is " + what + " 0x" + Integer.toHexString(what) +
        ", extra is " + extra + " 0x" + Integer.toHexString(extra));
    container.$form().dispatchErrorOccurredEvent(this, "Source",
        ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
    return true;
  }

  @SimpleEvent(description = "The VideoPlayerError event is no longer used. " +
      "Please use the Screen.ErrorOccurred event instead.",
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
  }

  /*
   *  Unfortunately, to prevent the user from setting the width and height
   *  of the component, we have to also prevent them from getting the width
   *  and height of the component.
   */

  /**
   * Returns the component's horizontal width, measured in pixels.
   *
   * @return  width in pixels
   */
  @Override
  @SimpleProperty(userVisible = false)
  public int Width() {
    return super.Width();
  }

  /**
   * Specifies the component's horizontal width, measured in pixels.
   *
   * @param  width in pixels
   */
  @Override
  @SimpleProperty(userVisible = false)
  public void Width(int width) {
    super.Width(width);
  }

  /**
   * Returns the component's vertical height, measured in pixels.
   *
   * @return  height in pixels
   */
  @Override
  @SimpleProperty(userVisible = false)
  public int Height() {
    return super.Height();
  }

  /**
   * Specifies the component's vertical height, measured in pixels.
   *
   * @param  height in pixels
   */
  @Override
  @SimpleProperty(userVisible = false)
  public void Height(int height) {
    super.Height(height);
  }
}
