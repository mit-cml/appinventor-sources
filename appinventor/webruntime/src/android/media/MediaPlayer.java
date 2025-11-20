package android.media;

import java.io.IOException;

import com.google.appinventor.components.runtime.util.AssetFetcher;
import com.google.gwt.dom.client.AudioElement;

public class MediaPlayer {

  public interface OnPreparedListener {
    void onPrepared(MediaPlayer mp);
  }

  public interface OnCompletionListener {
    void onCompletion(MediaPlayer mp);
  }

  private AudioElement audio;
  private OnCompletionListener completionListener;
  private OnPreparedListener preparedListener;

  public MediaPlayer() {
    audio = createAudioElement();
  }

  public void load(String path) {
    System.out.println("Loading sound from " + path);
    try {
      audio.setSrc(AssetFetcher.getLoadedAsset(path));
    } catch (Exception e) {
      System.err.println("Error loading sound: " + e.getMessage());
      audio.setSrc(path);
    }
  }

  public void prepare() throws IOException {
    if (audio != null) {
      audio.load();
    }
  }

  public void start() {
    if (audio != null) {
      audio.play();
    }
  }

  public void pause() {
    if (audio != null) {
      audio.pause();
    }
  }

  public void stop() {
    if (audio != null) {
      audio.pause();
    }
  }

  public void release() {
    if (audio != null) {
      audio.pause();
      audio.removeFromParent();
      audio = null;
    }
    preparedListener = null;
    completionListener = null;
  }

  public void setLooping(boolean loop) {
    if (audio != null) {
      audio.setLoop(loop);
    }
  }

  public void setVolume(float left, float right) {
    if (audio != null) {
      audio.setVolume(left);
    }
  }

  public boolean isPlaying() {
    return audio != null && !audio.isPaused();
  }

  public void seekTo(int msec) {
    if (audio != null) {
      audio.setCurrentTime(msec / 1000.0);
    }
  }

  public void setOnPreparedListener(OnPreparedListener listener) {
    this.preparedListener = listener;
  }

  public void setOnCompletionListener(OnCompletionListener listener) {
    this.completionListener = listener;
  }

  public void setAudioStreamType(int type) {}

  private void onLoadedMetadata() {
    if (preparedListener != null) {
      preparedListener.onPrepared(this);
    }
  }

  private void onEnded() {
    if (completionListener != null) {
      completionListener.onCompletion(this);
    }
  }

  private native AudioElement createAudioElement() /*-{
    var el = $doc.createElement("audio");
    var self = this;

    el.addEventListener("loadedmetadata", function(e) {
      setTimeout(function() {
        self.@android.media.MediaPlayer::onLoadedMetadata()();
      }, 0);
    });

    el.addEventListener("ended", function(e) {
      self.@android.media.MediaPlayer::onEnded()();
    });

    $doc.body.appendChild(el);
    return el;
  }-*/;
}
