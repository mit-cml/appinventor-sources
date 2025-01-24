package android.media;

public class MediaPlayer {
  public interface OnPreparedListener {
    void onPrepared(MediaPlayer mp);
  }
  public interface OnCompletionListener {
    void onCompletion(MediaPlayer mp);
  }
}
