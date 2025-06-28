package android.media;

import com.google.appinventor.components.runtime.util.AssetFetcher;
import com.google.gwt.dom.client.AudioElement;
import java.util.ArrayList;
import java.util.List;

public class SoundPool {
  public interface OnLoadCompleteListener {
    /**
     * Called when a sound has completed loading.
     *
     * @param soundPool SoundPool object from the load() method
     * @param sampleId the sample ID of the sound loaded.
     * @param status the status of the load operation (0 = success)
     */
    void onLoadComplete(SoundPool soundPool, int sampleId, int status);
  }

  private List<AudioElement> elements;
  private OnLoadCompleteListener onLoadCompleteListener;

  public SoundPool(int maxStreams, int streamType, int srcQuality) {
    elements = new ArrayList<>(maxStreams);
  }

  public void setOnLoadCompleteListener(OnLoadCompleteListener listener) {
    this.onLoadCompleteListener = listener;
  }

  public int load(String path, int priority) {
    // Simulate loading a sound from the specified path with the given priority
    System.out.println("Loading sound from " + path + " with priority " + priority);
    AudioElement audioElement = createAudioElement();
    try {
      audioElement.setSrc(AssetFetcher.getLoadedAsset(path));
      elements.add(audioElement);
      return elements.size(); // Return the index as the sound ID
    } catch (Exception e) {
      System.err.println("Error loading sound: " + e.getMessage());
    }
    return -1; // Return a dummy sound ID
  }

  public final int play(int soundID, float leftVolume, float rightVolume,
      int priority, int loop, float rate) {
    if (soundID < 1) {
      return 0;
    } else if (soundID > elements.size()) {
      return 0;
    }
    elements.get(soundID - 1).play();
    return soundID; // Return the sound ID as the stream ID
  }

  public final void resume(int soundID) {
    if (soundID < 1 || soundID > elements.size()) {
      throw new IllegalArgumentException("Invalid sound ID: " + soundID);
    }
    elements.get(soundID).play();
  }

  public final void pause(int soundID) {
    if (soundID < 1 || soundID > elements.size()) {
      throw new IllegalArgumentException("Invalid sound ID: " + soundID);
    }
    elements.get(soundID - 1).pause();
  }

  public final void stop(int soundID) {
    if (soundID < 1 || soundID > elements.size()) {
      throw new IllegalArgumentException("Invalid sound ID: " + soundID);
    }
    elements.get(soundID - 1).pause();
  }

  public final void release() {
    // Simulate releasing resources
    System.out.println("Releasing SoundPool resources");
    for (AudioElement element : elements) {
      if (element != null) {
        element.pause();
        element.removeFromParent();
      }
    }
    elements.clear();
  }

  public final void unload(int soundID) {
    if (soundID < 1 || soundID > elements.size()) {
      throw new IllegalArgumentException("Invalid sound ID: " + soundID);
    }
    AudioElement element = elements.get(soundID - 1);
    if (element != null) {
      element.pause();
      element.removeFromParent();
      elements.set(soundID, null); // Mark as unloaded
    }
  }

  private void onLoadedMetadata(AudioElement element) {
    if (onLoadCompleteListener != null) {
      onLoadCompleteListener.onLoadComplete(this, elements.indexOf(element) + 1, 0);
    }
  }

  private native AudioElement createAudioElement() /*-{
    var el = $doc.createElement("audio");
    var self = this;
    el.addEventListener("loadedmetadata", function(e) {
      setTimeout(function() {
      self.@android.media.SoundPool::onLoadedMetadata(Lcom/google/gwt/dom/client/AudioElement;)(e.target);
      }, 0); // Ensure play is called asynchronously
    });
    $doc.body.appendChild(el);
    return el;
  }-*/;
}
