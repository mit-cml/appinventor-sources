package android.media;

import java.io.IOException;

public class MediaPlayer {
  public interface OnPreparedListener {
    void onPrepared(MediaPlayer mp);
  }
  public interface OnCompletionListener {
    void onCompletion(MediaPlayer mp);
  }

  public void stop() {}

  public void release () {}

  public void setOnCompletionListener(Object listener) {}

  public void setAudioStreamType(int type) {}

  public boolean isPlaying() { return false; }

  public void setLooping(boolean loop) {} //TODO(lroman10): Real implementation ASAP

  public void setVolume(float left, float right) {}

  public void start() {}

  public void pause() {}

  public void seekTo(int msec) {}

  public void prepare() throws IOException {}
}
