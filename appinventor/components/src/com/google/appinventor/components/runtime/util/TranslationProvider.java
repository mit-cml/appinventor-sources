// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides the Form operations required by the i18n translation manager.
 */
public interface TranslationProvider {

  /**
   * Opens an asset bundled with the application.
   *
   * @param assetPath path of the bundled asset
   * @return stream containing the asset data
   * @throws IOException if the asset cannot be opened
   */
  InputStream openAsset(String assetPath) throws IOException;

  /**
   * Returns the name of the active screen.
   *
   * @return screen name
   */
  String getFormName();

  /**
   * Looks up a component by its Designer name.
   *
   * @param componentName component name
   * @return component instance, or {@code null} if it does not exist
   */
  Object lookupComponent(String componentName);
}
