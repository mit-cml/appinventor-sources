// Copyright 2010 Google Inc. All Rights Reserved.

package openblocks.codeblockutil;

import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * A PhoneCommIndicator updates its appearance based on what is happening
 * with the phone communication.
 *
 * @author kerr@google.com (Debby Wallach)
 *
 */
public class PhoneCommIndicator extends JLabel {
  private static final boolean DEBUG = false;

  public enum IndicatorState {
    DISCONNECTED,
    COMMUNICATING,
    CONNECTED,
  }

  // Initializer and descriptor for the info associated with each state.
  private class ImageInfo {
    final ImageIcon icon;
    final String descriptor;
    final String path;
    public ImageInfo(String descriptor, String path) {
      this.descriptor = descriptor;
      this.path = path;
      URL imageURL = PhoneCommIndicator.class.getResource(path);
      if (imageURL != null) {
        // The descriptor will not show, but in theory is useful for people
        // using assistive technologies.
        icon = new ImageIcon(imageURL, descriptor);
      } else {
        // If we can't load the icon for some reason, the descriptor will
        // be shown instead of an image.
        icon = new ImageIcon(descriptor);
      }
    }
  }

  private IndicatorState state;  // the current state of the phone icon

  // map from the current state to the info describing the state
  final private HashMap<IndicatorState, ImageInfo> stateMap =
      new HashMap<IndicatorState, ImageInfo>();

  public PhoneCommIndicator() {
    super();
    stateMap.put(IndicatorState.DISCONNECTED,
        new ImageInfo("phone is disconnected", "images/disconnected.png"));
    stateMap.put(IndicatorState.COMMUNICATING,
        new ImageInfo("communicating with phone", "images/communicating-anim.gif"));
    stateMap.put(IndicatorState.CONNECTED,
        new ImageInfo("phone is connected", "images/connected.png"));
    state = IndicatorState.COMMUNICATING;  // not really, but need it to fool setState
    setState(IndicatorState.DISCONNECTED);
  }

  /**
   * Change the state of the indicator to new state.  Returns the previous state.
   *
   * @param state   The new state to change to.
   * @return state  The old state.
   */
  public IndicatorState setState(IndicatorState state) {
    synchronized(this) {
      if (DEBUG) {
        System.out.println("setState to " + state + " was " + this.state);
        Throwable t = new Throwable();
        t.printStackTrace();
      }
      IndicatorState oldState = this.state;
      if (this.state != state) {
        this.state = state;
        ImageInfo imageInfo = stateMap.get(state);
        setIcon(imageInfo.icon);
      }
      return oldState;
    }
  }

  /**
   * Returns the current state of the indicator.
   *
   * @return  the current state
   */
  public IndicatorState getState() {
    synchronized(this) {
      return state;
    }
  }
}
