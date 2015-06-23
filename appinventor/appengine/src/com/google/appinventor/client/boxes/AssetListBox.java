// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.youngandroid.AssetList;
import com.google.appinventor.client.widgets.boxes.Box;

/**
 * Box implementation for asset list.
 *
 */
public final class AssetListBox extends Box {
  // Singleton asset list box (only one asset list box allowed)
  private static final AssetListBox INSTANCE = new AssetListBox();

  // Assets list for young android
  private final AssetList alist;

  /**
   * Returns the singleton asset list box.
   *
   * @return  asset list box
   */
  public static AssetListBox getAssetListBox() {
    return INSTANCE;
  }

  /**
   * Creates new asset list box.
   */
  private AssetListBox() {
    super(MESSAGES.assetListBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    alist = new AssetList();
    setContent(alist);
  }

  /**
   * Returns the asset list associated with the asset list box.
   *
   * @return  asset list
   */

  public AssetList getAssetList() {
    return alist;
  }
}
