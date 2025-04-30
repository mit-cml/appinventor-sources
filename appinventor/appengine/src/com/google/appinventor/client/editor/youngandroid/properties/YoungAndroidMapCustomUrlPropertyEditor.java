// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;

/**
 * Property editor for Map custom URL matching a particular format.
 */
public class YoungAndroidMapCustomUrlPropertyEditor extends TextPropertyEditor {

  public YoungAndroidMapCustomUrlPropertyEditor() {
  }

  @Override
  protected void validate(String text) throws InvalidTextException {
    // Check that the custom URL looks vaguely correct
    if (!(text.startsWith("https://") || text.startsWith("http://"))
        || !text.contains("{x}")
        || !text.contains("{y}")
        || !text.contains("{z}")) {
      throw new InvalidTextException(MESSAGES.customUrlNoPlaceholders(text, "{x}, {y} and {z}"));
    }

    // Try to request a single tile from the custom URL source as a final validation, only report errors
    String urlString = text.replace("{x}", "0")
                           .replace("{y}", "0")
                           .replace("{z}", "0");
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, urlString);
    try {
      builder.sendRequest(null, new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
          handleResponseCode(urlString, response.getStatusCode());
        }

        @Override
        public void onError(Request request, Throwable exception) {
          handleRequestError(urlString, exception);
        }
      });
    } catch (RequestException e) {
      throw new InvalidTextException(MESSAGES.customUrlException(urlString, e.getMessage()));
    }
  }

  // Window.alert is used here, rather than throw InvalidTextException, due to RequestBuilder Override signatures
  private void handleResponseCode(String urlString, int responseCode) {
    if (responseCode == 401 || responseCode == 403) {
      Window.alert(MESSAGES.customUrlBadAuthentication(urlString, responseCode));
    } else if (responseCode >= 400) {
      Window.alert(MESSAGES.customUrlBadStatusCode(urlString, responseCode));
    }
  }

  private void handleRequestError(String urlString, Throwable exception) {
    Window.alert(MESSAGES.customUrlException(urlString, exception.getMessage()));
  }
}
