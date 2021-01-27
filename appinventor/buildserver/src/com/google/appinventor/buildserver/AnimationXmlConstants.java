// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.buildserver;

public class AnimationXmlConstants {

  private AnimationXmlConstants() {
  }

  public final static String FADE_IN_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<alpha xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:fromAlpha=\"0.0\" android:toAlpha=\"1.0\"\n" +
      "\tandroid:duration=\"@android:integer/config_longAnimTime\" />";

  public final static String FADE_OUT_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<alpha xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:fromAlpha=\"1.0\" android:toAlpha=\"0.0\"\n" +
      "\tandroid:duration=\"@android:integer/config_longAnimTime\" />";

  public final static String HOLD_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/accelerate_interpolator\"\n" +
      "\tandroid:fromXDelta=\"0\" android:toXDelta=\"0\"\n" +
      "\tandroid:duration=\"@android:integer/config_longAnimTime\" />";

  public final static String SLIDE_EXIT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/overshoot_interpolator\"\n" +
      "\tandroid:fromXDelta=\"0%\" android:toXDelta=\"-100%\"\n" +
      "\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_ENTER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/overshoot_interpolator\"\n" +
      "\t\tandroid:fromXDelta=\"100%\" android:toXDelta=\"0%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_EXIT_REVERSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/overshoot_interpolator\"\n" +
      "\t\tandroid:fromXDelta=\"0%\" android:toXDelta=\"100%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_ENTER_REVERSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/overshoot_interpolator\"\n" +
      "\t\tandroid:fromXDelta=\"-100%\" android:toXDelta=\"0%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_V_EXIT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\tandroid:fromYDelta=\"0%\" android:toYDelta=\"100%\"\n" +
      "\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_V_ENTER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:fromYDelta=\"-100%\" android:toYDelta=\"0%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_V_EXIT_REVERSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:fromYDelta=\"0%\" android:toYDelta=\"-100%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_V_ENTER_REVERSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:fromYDelta=\"100%\" android:toYDelta=\"0%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String ZOOM_ENTER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<set xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\t\tandroid:interpolator=\"@android:anim/decelerate_interpolator\">\n" +
      "\t<scale android:fromXScale=\"2.0\" android:toXScale=\"1.0\"\n" +
      "\t\t\tandroid:fromYScale=\"2.0\" android:toYScale=\"1.0\"\n" +
      "\t\t\tandroid:pivotX=\"50%p\" android:pivotY=\"50%p\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />\n" +
      "</set>";

  public final static String ZOOM_ENTER_REVERSE ="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n"+
      "<set xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\t\tandroid:interpolator=\"@android:anim/decelerate_interpolator\">\n" +
      "\t<scale android:fromXScale=\"0.5\" android:toXScale=\"1.0\"\n" +
      "\t\t\tandroid:fromYScale=\"0.5\" android:toYScale=\"1.0\"\n" +
      "\t\t\tandroid:pivotX=\"50%p\" android:pivotY=\"50%p\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />\n" +
      "</set>";

  public final static String ZOOM_EXIT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<set xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\t\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:zAdjustment=\"top\">\n" +
      "\t<scale android:fromXScale=\"1.0\" android:toXScale=\".5\"\n" +
      "\t\t\tandroid:fromYScale=\"1.0\" android:toYScale=\".5\"\n" +
      "\t\t\tandroid:pivotX=\"50%p\" android:pivotY=\"50%p\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />\n" +
      "\t<alpha android:fromAlpha=\"1.0\" android:toAlpha=\"0\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\"/>\n" +
      "</set>";

  public final static String ZOOM_EXIT_REVERSE ="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<set xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\t\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:zAdjustment=\"top\">\n" +
      "\t<scale android:fromXScale=\"1.0\" android:toXScale=\"2.0\"\n" +
      "\t\t\tandroid:fromYScale=\"1.0\" android:toYScale=\"2.0\"\n" +
      "\t\t\tandroid:pivotX=\"50%p\" android:pivotY=\"50%p\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />\n" +
      "\t<alpha android:fromAlpha=\"1.0\" android:toAlpha=\"0\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\"/>\n" +
      "</set>";

}
