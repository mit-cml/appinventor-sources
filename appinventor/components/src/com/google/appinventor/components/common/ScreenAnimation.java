
package com.google.appinventor.components.common;

public enum ScreenAnimation {
  // @Default
  Default("default"),
  Fade("fade"),
  Zoom("zoom"),
  SlideHorizontal("slidehorizontal"),
  SlideVertical("slidevertical"),
  None("none");

  private String value;

  OpenStringAnimation(String anim) {
    this.value = anim;
  }

  private static final Map<String, ScreenAnimation> lookup = new HashMap<>();

  static {
    for(ScreenAnimation anim : ScreenAnimation.values()) {
      lookup.put(anim.getValue(), anim);
    }
  }

  public static ScreenAnimation get(String anim) {
    return lookup.get(anim);
  }
}