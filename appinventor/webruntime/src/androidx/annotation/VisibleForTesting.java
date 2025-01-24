//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package androidx.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface VisibleForTesting {
  int PRIVATE = 2;
  int PACKAGE_PRIVATE = 3;
  int PROTECTED = 4;
  int NONE = 5;

  int otherwise() default 2;
}
