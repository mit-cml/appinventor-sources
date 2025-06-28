package com.google.appinventor.components.runtime.util;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class AssetFetcher {
  public static native String getLoadedAsset(String assetName) throws Exception;
}
