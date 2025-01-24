package android.os;

import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public class Bundle {

  HashMap<String, String> values = new HashMap<String, String>();
  Map<String, String[]> arrayValues = new HashMap<>();

  public void put(String key, String value) {
    values.put(key, value);
  }

  public void putString(String key, String value) {
    values.put(key, value);
  }

  public void putBoolean(String key, boolean value) {
    values.put(key, String.valueOf(value));
  }

  public void putInt(String key, int value) {
    values.put(key, String.valueOf(value));
  }

  public void putLong(String key, long value) {
    values.put(key, String.valueOf(value));
  }

  public void put(String key, String[] value) {
    arrayValues.put(key, value);
  }

  public String get(String key) {
    return values.get(key);
  }

  public String getString(String key) {
    return values.get(key);
  }

  public String getString(String key, String defaultValue) {
    return values.containsKey(key) ? values.get(key) : defaultValue;
  }

  public String[] getStringArray(String key) {
    return arrayValues.get(key);
  }

  public boolean getBoolean(String key) {
    return Boolean.valueOf(values.get(key));
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    return values.containsKey(key) ? Boolean.valueOf(values.get(key)) : defaultValue;
  }

  public int getInt(String key) {
    return Integer.valueOf(values.get(key));
  }

  public int getInt(String key, int defaultValue) {
    return values.containsKey(key) ? Integer.valueOf(values.get(key)) : defaultValue;
  }

  public long getLong(String key) {
    return Long.valueOf(values.get(key));
  }

  public long getLong(String key, long defaultValue) {
    return values.containsKey(key) ? Long.valueOf(values.get(key)) : defaultValue;
  }

  public boolean containsKey(String key) {
    return values.containsKey(key);
  }
}
