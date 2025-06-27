package android.content;

import android.net.Uri;
import android.os.Bundle;

public class Intent {

  public static final int FLAG_ACTIVITY_NEW_TASK = 0x10000000;

  public static final String ACTION_VIEW = "android.intent.action.VIEW";

  String action;
  Uri uri;
  Bundle extras;
  Context ctx;
  int flags;
  public Class intentClass;
  private ComponentName mComponent;

  public Intent() {

  }

  public Intent(String action) {
    this.action = action;
  }

  public Intent(String action, Uri uri) {
    this.action = action;
    this.uri = uri;
  }

  public Intent(Context ctx, Class<?> intentClass) {
    this.ctx = ctx;
    this.intentClass = intentClass;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  public Uri getData() {
    return uri;
  }

  public Class<?> getIntentClass() {
    return intentClass;
  }

  public void putExtras(Bundle extras) {
    this.extras = extras;
  }

  public Bundle getExtras() {
    return extras;
  }

  public String getStringExtra(String key) {
    if (extras == null) {
      return null;
    }
    return extras.getString(key);
  }

  public String getStringExtra(String key, String defaultValue) {
    if (extras == null) {
      return defaultValue;
    }
    return extras.getString(key, defaultValue);
  }

  public int getIntExtra(String key, int defaultValue) {
    if (extras == null) {
      return 0;
    }
    return extras.getInt(key, defaultValue);
  }

  public long getLongExtra(String key) {
    if (extras == null) {
      return 0;
    }
    return extras.getLong(key);
  }

  public long getLongExtra(String key, long defaultValue) {
    if (extras == null) {
      return defaultValue;
    }
    return extras.getLong(key, defaultValue);
  }

  public String[] getStringArrayExtra(String key) {
    if (extras == null) {
      return null;
    }
    return extras.getStringArray(key);
  }

  public boolean hasExtra(String key) {
    if (extras == null) {
      return false;
    }
    return extras.containsKey(key);
  }

  public void putExtra(String key, boolean value) {
    if (extras == null) {
      extras = new Bundle();
    }
    extras.put(key, String.valueOf(value));
  }

  public void putExtra(String key, String value) {
    if (extras == null) {
      extras = new Bundle();
    }
    extras.put(key, value);
  }

  public void putExtra(String key, Integer value) {
    if (extras == null) {
      extras = new Bundle();
    }
    extras.put(key, String.valueOf(value));
  }

  public void putExtra(String key, Long value) {
    if (extras == null) {
      extras = new Bundle();
    }
    extras.put(key, String.valueOf(value));
  }

  public void putExtra(String key, String[] value) {
    if (extras == null) {
      extras = new Bundle();
    }
    extras.put(key, value);
  }

  public void removeExtra(String key) {
    if (extras != null) {
      extras.remove(key);
    }
  }

  public Intent addFlags(int flags) {
    this.flags |= flags;
    return this;
  }

  public Intent setFlags(int flags) {
    this.flags = flags;
    return this;
  }

  public Intent setClassName(Context packageContext,
      String className) {
    mComponent = new ComponentName(packageContext, className);
    // TODO(ewpatton): Need a better design for this. Probably automated using annotation processors
    switch (className) {
      case "com.google.appinventor.components.runtime.ListPickerActivity":
        intentClass = com.google.appinventor.components.runtime.ListPickerActivity.class;
        break;
    }
    return this;
  }
}
