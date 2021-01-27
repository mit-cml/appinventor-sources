package com.google.appinventor.buildserver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BuildType {
  boolean apk() default false;

  boolean aab() default false;

  public static String APK_EXTENSION = "apk";
  public static String AAB_EXTENSION = "aab";
}
