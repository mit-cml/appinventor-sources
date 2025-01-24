package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

public class FullScreenVideoUtil implements MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {

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

  public static final String ACTION_SUCCESS = "ActionSuccess";

  public static final String ACTION_DATA = "ActionData";

  public FullScreenVideoUtil(Activity activity, Handler handler) {

  }

  public Bundle performAction(int action, Object... args) {
    return null;
  }

  public Dialog createFullScreenVideoDialog() {
    return null;
  }

  public void prepareFullScreenVideoDialog(Dialog dialog) {

  }

  @Override
  public void onCompletion(MediaPlayer mediaPlayer) {

  }

  @Override
  public void onPrepared(MediaPlayer mediaPlayer) {

  }
}
