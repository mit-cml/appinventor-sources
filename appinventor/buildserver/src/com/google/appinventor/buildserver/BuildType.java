package com.google.appinventor.buildserver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BuildType {
  /**
   * Indicates that the annotated task is used for apk builds.
   */
  boolean apk() default false;

  /**
   * Indicates that the annotated task is used for aab builds.
   * @return
   */
  boolean aab() default false;

  String APK_EXTENSION = "apk";
  String AAB_EXTENSION = "aab";
}
